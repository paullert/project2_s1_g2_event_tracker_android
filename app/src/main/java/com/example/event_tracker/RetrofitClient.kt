package com.example.event_tracker

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Interceptor
import android.content.Context

object RetrofitClient{

    var googleIdToken: String? = null
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            googleIdToken?.let { token ->
                requestBuilder.addHeader(
                    "Authorization",
                    "Bearer $token"
                )
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val eventApi: EventApi by lazy{
        retrofit.create(EventApi::class.java)
    }

    val inviteApi: InviteApiService by lazy{
        retrofit.create(InviteApiService::class.java)
    }

    fun eventApi(context: Context): EventApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(
                        AuthInterceptor(context.applicationContext)
                    )
                    .build()
            )
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(EventApi::class.java)
    }
}