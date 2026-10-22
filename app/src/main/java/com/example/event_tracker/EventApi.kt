package com.example.event_tracker

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface EventApi{
    @GET("api/v1/events/{eventId}/totalAttendees")
    suspend fun getTotalAttendees(
        @Path("eventId") eventId: Long
    ): Int

    @GET("data")
    suspend fun getData(): Response<DataModel>
}

annotation class DataModel