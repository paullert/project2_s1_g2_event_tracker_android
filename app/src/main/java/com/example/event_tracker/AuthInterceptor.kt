package com.example.event_tracker

import okhttp3.Authenticator
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

interface BearerTokenProvider {
    fun token(): String?
    fun refresh(rejectedToken: String): String?
    fun clear()
}

private val backendURL = BuildConfig.API_BASE_URL.toHttpUrl()

private fun HttpUrl.sameOrigin(other: HttpUrl) =
    scheme == other.scheme && host == other.host && port == other.port



class AuthInterceptor(
    private val tokens: BearerTokenProvider = AuthSession,
    private val apiUrl: HttpUrl = backendURL
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.url.sameOrigin(apiUrl)) return chain.proceed(request)
        val builder = request.newBuilder().removeHeader("Authorization")
        tokens.token()?.let { builder.header("Authorization", "Bearer $it") }
        return chain.proceed(builder.build())
    }
}

class TokenAuthenticator(
    private val tokens: BearerTokenProvider = AuthSession,
    private val apiUrl: HttpUrl = backendURL
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (!response.request.url.sameOrigin(apiUrl)) return null
        val rejected = response.request.header("Authorization")?.removePrefix("Bearer ")
            ?: return null
        // A retry that is also rejected must return control to sign-in, not loop.
        if (generateSequence(response) { it.priorResponse }.count { it.code == 401 } >= 2) {
            tokens.clear()
            return null
        }
        val fresh = tokens.refresh(rejected) ?: return null
        if (fresh == rejected) return null
        return response.request.newBuilder().header("Authorization", "Bearer $fresh").build()
    }
}
