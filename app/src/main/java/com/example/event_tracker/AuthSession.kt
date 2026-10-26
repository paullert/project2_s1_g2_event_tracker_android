package com.example.event_tracker

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenResponse
import java.io.IOException
import java.util.Base64
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** One token store for registration, events, and invitations. Never log its contents. */
object AuthSession : BearerTokenProvider {
    private lateinit var preferences: SharedPreferences
    private lateinit var authorizationService: AuthorizationService
    private var state = AuthState()
    private val mutex = Mutex()
    private val authorized = MutableStateFlow(false)
    val isAuthorized = authorized.asStateFlow()

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        authorizationService = AuthorizationService(context.applicationContext)
        state = runCatching {
            preferences.getString("auth_state", null)?.let { AuthState.jsonDeserialize(it) }
                ?: AuthState()
        }.getOrElse { AuthState() }
        if (state.lastAuthorizationResponse?.request?.clientId != BuildConfig.GOOGLE_CLIENT_ID) {
            state = AuthState()
            preferences.edit().remove("auth_state").apply()
        }
        // The legacy access_token key actually held an ID token and had no refresh state.
        preferences.edit().remove("access_token").apply()
        authorized.value = state.isAuthorized
    }

    suspend fun save(response: AuthorizationResponse, tokens: TokenResponse) = mutex.withLock {
        require(tokens.idToken?.let { isIdTokenFresh(it) } == true) {
            "Google did not return a usable ID token"
        }
        state = AuthState(response, tokens, null)
        persist()
    }

    suspend fun exchangeAuthorizationCode(response: AuthorizationResponse) {
        require(response.authorizationCode != null && response.request.codeVerifier != null) {
            "Google did not return a PKCE authorization code"
        }
        val (tokens, error) = suspendCoroutine<Pair<TokenResponse?, AuthorizationException?>> { continuation ->
            authorizationService.performTokenRequest(response.createTokenExchangeRequest()) { result, ex ->
                continuation.resume(result to ex)
            }
        }
        if (error != null || tokens == null) {
            if (BuildConfig.DEBUG) {
                Log.w("EventTrackerAuth", "Token exchange failed: type=${error?.type}, code=${error?.code}")
            }
            throw IOException("Google token exchange failed: ${error?.errorDescription ?: "No token returned"}", error)
        }
        save(response, tokens)
    }

    suspend fun freshIdToken(rejectedToken: String? = null): String? = mutex.withLock {
        val current = state.idToken
        if (current != null && current != rejectedToken && isIdTokenFresh(current)) {
            return@withLock current
        }
        if (state.refreshToken == null) {
            clearLocked()
            return@withLock null
        }

        // Serialize refreshes; AppAuth's token connection bypasses the API interceptor.
        val (tokens, error) = suspendCoroutine<Pair<TokenResponse?, AuthorizationException?>> { continuation ->
            authorizationService.performTokenRequest(state.createTokenRefreshRequest()) { result, ex ->
                continuation.resume(result to ex)
            }
        }
        if (error != null) {
            if (error.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR &&
                error.error == "invalid_grant") {
                clearLocked()
                return@withLock null
            }
            throw IOException("Could not refresh Google sign-in. Try again when connected.", error)
        }
        val idToken = tokens?.idToken
        if (idToken == null || !isIdTokenFresh(idToken)) {
            clearLocked()
            return@withLock null
        }
        state.update(tokens, null)
        persist()
        idToken
    }

    suspend fun signOut() = mutex.withLock { clearLocked() }

    // OkHttp invokes these on worker threads, never the UI thread.
    override fun token(): String? = runBlocking { freshIdToken() }
    override fun refresh(rejectedToken: String): String? = runBlocking { freshIdToken(rejectedToken) }
    override fun clear() = runBlocking { signOut() }

    private fun persist() {
        preferences.edit().putString("auth_state", state.jsonSerializeString()).apply()
        authorized.value = state.isAuthorized
    }

    private fun clearLocked() {
        state = AuthState()
        preferences.edit().clear().apply()
        authorized.value = false
    }

    /** Local expiry/profile hints only. The API verifies the signature and identity. */
    private fun tokenPayload(token: String) = runCatching {
        val parts = token.split('.')
        require(parts.size == 3)
        JsonParser.parseString(String(Base64.getUrlDecoder().decode(parts[1]), Charsets.UTF_8))
            .asJsonObject
    }.getOrNull()

    internal fun isIdTokenFresh(token: String, nowMillis: Long = System.currentTimeMillis()): Boolean =
        runCatching {
            val expiry = tokenPayload(token)?.get("exp")?.asLong ?: return false
            expiry > nowMillis / 1000 + 60
        }.getOrDefault(false)

    internal fun suggestedUsername(token: String): String {
        val claims = tokenPayload(token)
        val name = claims?.get("name")?.asString?.trim().orEmpty()
        val emailName = claims?.get("email")?.asString?.substringBefore('@').orEmpty()
        return name.ifBlank { emailName }.ifBlank { "google-user" }.take(100)
    }
}
