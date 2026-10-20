package com.example.event_tracker

import retrofit2.http.GET
import retrofit2.http.Path

interface InviteApiService{
    @GET("api/v1/users/me/invites")
    suspend fun getMyInvites(): List<EventInvitations>
}