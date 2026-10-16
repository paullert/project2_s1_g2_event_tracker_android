package com.example.event_tracker

import retrofit2.http.GET
import retrofit2.http.Path

interface InviteApi{
    @GET("api/v1/users/{userId}/invites")
    suspend fun getInvitations(
        @Path("userId") userId: Long
    ): List<InviteResponse>
}