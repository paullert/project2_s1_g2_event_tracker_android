package com.example.event_tracker

import android.content.Context
import android.content.Intent
import android.net.Uri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.CodeVerifierUtil
import net.openid.appauth.ResponseTypeValues
import androidx.core.net.toUri

class GooglePkceManager(
    private val context: Context
) {
    companion object {
        const val REQUEST_CODE = 1001

        private const val CLIENT_ID =
            "433179782438-nsl6671oqkia8ofcqdjm1hde0fhk8k9n.apps.googleusercontent.com"

        private const val REDIRECT_URI =
            "https://project2-s1-g4-event-tracker-api.onrender.com/oauth2redirect"
    }

    private val configuration =
        AuthorizationServiceConfiguration(
            "https://accounts.google.com/o/oauth2/v2/auth".toUri(),
            "https://project2-s1-g4-event-tracker-api.onrender.com/oauth2redirect".toUri()
        )

    fun startLogin() {
        val verifier =
            CodeVerifierUtil.generateRandomCodeVerifier()

        val request = AuthorizationRequest.Builder(
            configuration,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            REDIRECT_URI.toUri()
        )
            .setScope("openid email profile")
            .setCodeVerifier(verifier)
            .build()

        val authorizationService =
            AuthorizationService(context)

        val intent =
            authorizationService.getAuthorizationRequestIntent(request)

        if (context is android.app.Activity) {
            context.startActivityForResult(intent, REQUEST_CODE)
        }
    }
}