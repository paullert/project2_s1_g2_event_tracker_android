package com.example.event_tracker

import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import net.openid.appauth.AuthorizationService
import net.openid.appauth.CodeVerifierUtil

@RunWith(AndroidJUnit4::class)
class PkceConfigurationTest {
    @Test fun eachAuthorizationUsesFreshS256PkceStateAndNonce() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val service = AuthorizationService(context)
        try {
            val first = GooglePkceManager(service).createAuthorizationRequest()
            val second = GooglePkceManager(service).createAuthorizationRequest()
            assertEquals("code", first.responseType)
            assertEquals("S256", first.codeVerifierChallengeMethod)
            assertNotNull(first.codeVerifier)
            assertEquals(CodeVerifierUtil.deriveCodeVerifierChallenge(first.codeVerifier!!), first.codeVerifierChallenge)
            assertNotEquals(first.codeVerifier, second.codeVerifier)
            assertNotEquals(first.state, second.state)
            assertNotNull(first.nonce)
            assertNotEquals(first.nonce, second.nonce)
            assertEquals(BuildConfig.GOOGLE_CLIENT_ID, first.clientId)
            assertEquals(BuildConfig.GOOGLE_REDIRECT_URI, first.redirectUri.toString())
            assertEquals("offline", first.additionalParameters["access_type"])
            assertTrue(first.scope!!.split(' ').containsAll(listOf("openid", "email", "profile")))
        } finally { service.dispose() }
    }

    @Test fun manifestRoutesTheExactGoogleCallbackToAppAuth() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(Intent.ACTION_VIEW, BuildConfig.GOOGLE_REDIRECT_URI.toUri())
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setPackage(context.packageName)
        val activities = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        assertTrue(activities.any { it.activityInfo.name == "net.openid.appauth.RedirectUriReceiverActivity" })
    }
}
