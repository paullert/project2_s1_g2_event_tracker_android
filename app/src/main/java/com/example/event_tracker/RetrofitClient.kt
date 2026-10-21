package com.example.event_tracker

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient{

    private const val BASE_URL = "http://10.0.2.2:8080/"
    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
        GsonConverterFactory.create()).build()

    val eventApi: EventApi by lazy{
        retrofit.create(EventApi::class.java)
    }

    val inviteApi: InviteApiService by lazy{
        retrofit.create(InviteApiService::class.java)
    }
}