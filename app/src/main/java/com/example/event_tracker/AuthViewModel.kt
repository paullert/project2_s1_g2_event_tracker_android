package com.example.event_tracker

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationResponse
import retrofit2.Response
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException

/** Keeps code exchange and account loading alive across Activity recreation. */
class AuthViewModel : ViewModel() {
    var user by mutableStateOf<UserResponse?>(null)
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var registrationRequired by mutableStateOf(false)
        private set
    var username by mutableStateOf("")

    init {
        viewModelScope.launch {
            AuthSession.isAuthorized.collect { authorized -> if (!authorized) user = null }
        }
        restore()
    }

    fun startLogin() { busy = true; error = null }
    fun loginFailed(message: String) { busy = false; error = message }

    fun completeLogin(response: AuthorizationResponse) = runAuth {
        AuthSession.exchangeAuthorizationCode(response)
        if (BuildConfig.DEBUG) Log.d("EventTrackerAuth", "Google token exchange completed")
        loadAccount()
    }

    fun restore() = runAuth { loadAccount() }

    private suspend fun loadAccount() {
        val idToken = AuthSession.freshIdToken() ?: return
        val result = backendRequest("GET /auth/me", "loading your account") {
            RetrofitClient.authApi.me()
        }
        if (BuildConfig.DEBUG) Log.d("EventTrackerAuth", "GET /auth/me returned HTTP ${result.code()}")
        if (result.code() == 404) {
            result.errorBody()?.close()
            username = AuthSession.suggestedUsername(idToken)
            registerAccount()
        } else acceptAccount(result)
    }

    fun register() = runAuth { registerAccount() }

    private suspend fun registerAccount() {
        val result = backendRequest("POST /auth/register", "creating your account") {
            RetrofitClient.authApi.registerWithGoogle(RegisterRequest(username.trim()))
        }
        if (BuildConfig.DEBUG) Log.d("EventTrackerAuth", "POST /auth/register returned HTTP ${result.code()}")
        if (result.code() == 409) {
            registrationRequired = true
            error = "That display name is already in use. Choose another."
            result.errorBody()?.close()
        } else acceptAccount(result)
    }

    private fun acceptAccount(result: Response<UserResponse>) {
        if (!result.isSuccessful) {
            result.errorBody()?.close()
            throw IllegalStateException(when (result.code()) {
                401 -> "Google sign-in was rejected by the server. Please try again."
                403 -> "Your account does not have permission for this operation."
                else -> "Could not load your account (HTTP ${result.code()}). Try again."
            })
        }
        user = result.body() ?: throw IllegalStateException("The API returned an empty account.")
        registrationRequired = false
    }

    fun signOut() = runAuth {
        AuthSession.signOut()
        user = null
        registrationRequired = false
    }

    private suspend fun <T> backendRequest(endpoint: String, action: String, request: suspend () -> T): T {
        val started = System.nanoTime()
        if (BuildConfig.DEBUG) Log.d("EventTrackerAuth", "$endpoint started")
        try {
            return request()
        } catch (exception: IOException) {
            if (BuildConfig.DEBUG) {
                val elapsed = (System.nanoTime() - started) / 1_000_000
                Log.w("EventTrackerAuth", "$endpoint failed after ${elapsed}ms (${exception.javaClass.simpleName})")
            }
            val message = when (exception) {
                is InterruptedIOException -> "The Event Tracker server timed out while $action. Tap Retry connection."
                is UnknownHostException -> "Unable to find the Event Tracker server. Check your internet connection."
                is ConnectException -> "Unable to connect to the Event Tracker server. Check your connection and try again."
                else -> throw exception
            }
            throw IOException(message, exception)
        }
    }

    private fun runAuth(action: suspend () -> Unit) {
        viewModelScope.launch {
            busy = true
            error = null
            try {
                action()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.w("EventTrackerAuth", "Sign-in failed (${exception.javaClass.simpleName})")
                }
                error = exception.message ?: "Sign-in failed. Try again."
            } finally {
                busy = false
            }
        }
    }
}
