package com.example.event_tracker

import retrofit2.http.GET

interface InviteApiService{
    @GET("api/v1/invites/me")
    suspend fun getMyInvites(): InvitePageResponse
}