package com.example.event_tracker

data class InvitePageResponse(
    val content: List<InviteResponse>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)
