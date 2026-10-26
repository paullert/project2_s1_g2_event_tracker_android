package com.example.event_tracker

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .authenticator(TokenAuthenticator())
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS) // A hosted API may need time to wake from idle.
        .callTimeout(120, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val eventApi: EventApi by lazy { retrofit.create(EventApi::class.java) }
    val inviteApi: InviteApiService by lazy { retrofit.create(InviteApiService::class.java) }
    val createEventApi: CreateEventApi by lazy { retrofit.create(CreateEventApi::class.java) }
}
