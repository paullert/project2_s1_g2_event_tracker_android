package com.example.event_tracker

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class RegisterRequest(val username: String)

data class UserResponse(
    val userId: Long,
    val username: String,
    val email: String,
    val subject: String,
    val role: String
)

interface AuthApi {
    @GET("auth/me")
    suspend fun me(): Response<UserResponse>

    @POST("auth/register")
    suspend fun registerWithGoogle(@Body request: RegisterRequest): Response<UserResponse>
}

object ApiClient {
    val eventApi: EventApi get() = RetrofitClient.eventApi
    val authApi: AuthApi get() = RetrofitClient.authApi
}
