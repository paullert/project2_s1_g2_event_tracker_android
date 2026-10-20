package com.example.event_tracker

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

object ApiClient {

    private const val BASE_URL =
        "https://your-backend-domain.com/" //would put render link here

    val eventApi: EventApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventApi::class.java)
    }
}

interface EventApi {
    @GET("api/v1/events/{eventId}/totalAttendees")
    suspend fun getTotalAttendees(
        @Path("eventId") eventId: Long
    ): Int
    @GET("data")
    suspend fun getData(): Response<DataModel>
}

annotation class DataModel
