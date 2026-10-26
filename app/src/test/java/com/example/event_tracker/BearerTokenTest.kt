package com.example.event_tracker

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Base64
import java.util.concurrent.TimeUnit

class BearerTokenTest {
    private lateinit var server: MockWebServer
    private lateinit var tokens: FakeTokens

    private class FakeTokens : BearerTokenProvider {
        var current: String? = "original-id-token"
        var replacement: String? = "refreshed-id-token"
        var refreshes = 0
        var cleared = false
        override fun token() = current
        override fun refresh(rejectedToken: String): String? {
            assertEquals(current, rejectedToken)
            refreshes++
            current = replacement
            return current
        }
        override fun clear() { cleared = true; current = null }
    }

    @Before fun setUp() { server = MockWebServer(); server.start(); tokens = FakeTokens() }
    @After fun tearDown() { server.shutdown() }

    private fun client(apiUrl: HttpUrl = server.url("/")) = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokens, apiUrl))
        .authenticator(TokenAuthenticator(tokens, apiUrl))
        .callTimeout(5, TimeUnit.SECONDS)
        .build()

    @Test fun allApiRequestsUseOneBearerHeader() {
        val client = client()
        for (path in listOf("auth/register", "auth/me", "api/v1/events", "api/v1/invites/me")) {
            server.enqueue(MockResponse().setResponseCode(200))
            client.newCall(Request.Builder().url(server.url(path))
                .header("Authorization", "Bearer stale-token").build()).execute().close()
            assertEquals(listOf("Bearer original-id-token"), server.takeRequest().headers.values("Authorization"))
        }
    }

    @Test fun noSessionSendsNoAuthorizationAndDoesNotRetry401() {
        tokens.current = null
        server.enqueue(MockResponse().setResponseCode(401))
        client().newCall(Request.Builder().url(server.url("/auth/me")).build()).execute().use {
            assertEquals(401, it.code)
        }
        assertNull(server.takeRequest().getHeader("Authorization"))
        assertEquals(0, tokens.refreshes)
    }

    @Test fun rejectionRefreshesAndRetriesWithTheNewToken() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(200))
        client().newCall(Request.Builder().url(server.url("/auth/me")).build()).execute().use {
            assertEquals(200, it.code)
        }
        assertEquals("Bearer original-id-token", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer refreshed-id-token", server.takeRequest().getHeader("Authorization"))
        assertEquals(1, tokens.refreshes)
    }

    @Test fun aSecond401StopsRetryingAndClearsTheSession() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))
        client().newCall(Request.Builder().url(server.url("/auth/me")).build()).execute().use {
            assertEquals(401, it.code)
        }
        assertEquals(2, server.requestCount)
        assertEquals(1, tokens.refreshes)
        assertTrue(tokens.cleared)
    }

    @Test fun roleDenialDoesNotRefreshOrSignOut() {
        server.enqueue(MockResponse().setResponseCode(403))
        client().newCall(Request.Builder().url(server.url("/api/v1/users")).build()).execute().use {
            assertEquals(403, it.code)
        }
        assertEquals(0, tokens.refreshes)
        assertFalse(tokens.cleared)
    }

    @Test fun tokensAreNotSentToAnotherOrigin() {
        val otherOrigin = server.url("/").newBuilder().port(server.port + 1).build()
        server.enqueue(MockResponse().setResponseCode(401))
        client(otherOrigin).newCall(Request.Builder().url(server.url("/auth/me")).build()).execute().use {
            assertEquals(401, it.code)
        }
        assertNull(server.takeRequest().getHeader("Authorization"))
        assertEquals(0, tokens.refreshes)
    }

    @Test fun noReplacementTokenDoesNotLoop() {
        tokens.replacement = null
        server.enqueue(MockResponse().setResponseCode(401))
        client().newCall(Request.Builder().url(server.url("/auth/me")).build()).execute().use {
            assertEquals(401, it.code)
        }
        assertEquals(1, server.requestCount)
    }

    private fun token(json: String) = "header." + Base64.getUrlEncoder().withoutPadding()
        .encodeToString(json.toByteArray()) + ".signature"

    @Test fun expiryUsesTheIdTokenAndRefreshesBeforeItExpires() {
        assertTrue(AuthSession.isIdTokenFresh(token("{\"exp\":2000}"), 1_000_000))
        assertFalse(AuthSession.isIdTokenFresh(token("{\"exp\":1059}"), 1_000_000))
        assertFalse(AuthSession.isIdTokenFresh(token("{\"exp\":1000}"), 1_000_000))
        assertFalse(AuthSession.isIdTokenFresh(token("{}"), 1_000_000))
        assertFalse(AuthSession.isIdTokenFresh("not-a-jwt", 1_000_000))
    }

    @Test fun displayNameHintsDoNotRequireAUserPassword() {
        assertEquals("Alex", AuthSession.suggestedUsername(token("{\"name\":\"Alex\"}")))
        assertEquals("alex", AuthSession.suggestedUsername(token("{\"email\":\"alex@example.com\"}")))
    }
}
