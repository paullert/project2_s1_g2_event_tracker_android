package com.example.event_tracker

import android.content.Intent
import androidx.core.net.toUri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.CodeVerifierUtil
import net.openid.appauth.ResponseTypeValues

class GooglePkceManager(private val authorizationService: AuthorizationService) {
    fun createLoginIntent(): Intent {
        return authorizationService.getAuthorizationRequestIntent(createAuthorizationRequest())
    }

    fun createAuthorizationRequest(): AuthorizationRequest {
        val configuration = AuthorizationServiceConfiguration(
            "https://accounts.google.com/o/oauth2/v2/auth".toUri(),
            "https://oauth2.googleapis.com/token".toUri()
        )
        return AuthorizationRequest.Builder(
            configuration,
            BuildConfig.GOOGLE_CLIENT_ID,
            ResponseTypeValues.CODE,
            BuildConfig.GOOGLE_REDIRECT_URI.toUri()
        )
            .setScope("openid email profile")
            .setCodeVerifier(CodeVerifierUtil.generateRandomCodeVerifier())
            .setPrompt("consent select_account")
            .setAdditionalParameters(mapOf("access_type" to "offline"))
            .build()
    }
}
