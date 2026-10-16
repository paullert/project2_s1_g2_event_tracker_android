package com.example.event_tracker

data class InviteResponse(
    val inviteId: Long,
    val eventId: Long,
    val eventName: String,
    val eventDescription: String?,
    val startsOn: String?,
    val status: String,
    val invitedAt: String?
)