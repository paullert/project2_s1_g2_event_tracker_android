package com.example.event_tracker

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

data class MyInviteRequest(
    val status: String
)
interface InviteApiService{
    @GET("api/v1/invites/me")
    suspend fun getMyInvites(): InvitePageResponse

    @PATCH("api/v1/invites/me/{eventId}")
    suspend fun respondToInvite(
        @Path("eventId") eventId: Long,
        @Body request: MyInviteRequest
    ): InviteResponse
}