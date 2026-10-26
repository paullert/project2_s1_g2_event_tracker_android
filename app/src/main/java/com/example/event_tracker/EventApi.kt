package com.example.event_tracker

import retrofit2.http.GET
import retrofit2.http.Path

interface EventApi{
    @GET("api/v1/events/{eventId}/totalAttendees")
    suspend fun getTotalAttendees(
        @Path("eventId") eventId: Long
    ): Int

    @GET("api/v1/events")
    suspend fun getPublicEvents(): EventPageResponse
}

data class EventPageResponse(val content: List<ApiEvent>)

data class ApiEvent(
    val eventId: Long,
    val eventName: String,
    val startsOn: String,
    val locationId: Long?
)
