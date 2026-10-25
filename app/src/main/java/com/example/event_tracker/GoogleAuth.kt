package com.example.event_tracker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class GooglePkceRequest(
    val code: String,
    val codeVerifier: String,
    val redirectUri: String
)

data class AuthResponse(
    val accessToken: String,
    val email: String?,
    val name: String?
)

interface AuthApi {
    @POST("auth/google/exchange")
    suspend fun exchangeGoogleCode(
        @Body request: GooglePkceRequest
    ): AuthResponse
}

object ApiClient {
    val eventApi: Any
        get() {
            TODO()
        }

    //val eventApi: Any
    private const val BASE_URL =
        "https://project2-s1-g4-event-tracker-api.onrender.com/"

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(AuthApi::class.java)
    }
}