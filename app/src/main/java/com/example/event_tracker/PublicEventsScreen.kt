package com.example.event_tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily.Companion.Serif
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EventDto(
    val id: Long,
    val name: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val location: String?
)

@Composable
fun PublicEventList(modifier: Modifier = Modifier) {
    var events by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            events = ApiClient.eventApi.getPublicEvents().content.map { event ->
                EventDto(
                    id = event.eventId,
                    name = event.eventName,
                    date = event.startsOn.substringBefore('T'),
                    startTime = event.startsOn.substringAfter('T', "").take(5),
                    endTime = "",
                    location = event.locationId?.let { "Location $it" }
                )
            }
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Unable to load events"
        } finally {
            isLoading = false
        }
    }

    PublicEventContent(
        modifier = modifier,
        events = events,
        isLoading = isLoading,
        errorMessage = errorMessage
    )
}

@Composable
private fun PublicEventContent(
    modifier: Modifier = Modifier,
    events: List<EventDto>,
    isLoading: Boolean,
    errorMessage: String?
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4A4A4A), Color(0xFF383838))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Public Events",
                color = Color.White,
                fontSize = 32.sp,
                fontFamily = Serif
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(
                    text = "Error: $errorMessage",
                    color = Color.Red
                )
                events.isEmpty() -> Text(
                    text = "No public events found.",
                    color = Color.White
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(events) { event -> EventCard(event) }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: EventDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF929292))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.name, color = Color.Black, fontSize = 20.sp)
            Text(text = "Date: ${event.date}", color = Color.Black)
            Text(
                text = "Time: ${event.startTime}",
                color = Color.Black
            )
            Text(
                text = "Location: ${event.location ?: "Not specified"}",
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PublicEventScreenPreview() {
    PublicEventContent(
        events = listOf(
            EventDto(
                id = 1L,
                name = "Example Event",
                date = "OCT. 14, 2026",
                startTime = "3:00 PM",
                endTime = "8:00 PM",
                location = "Main Hall"
            )
        ),
        isLoading = false,
        errorMessage = null
    )
}
