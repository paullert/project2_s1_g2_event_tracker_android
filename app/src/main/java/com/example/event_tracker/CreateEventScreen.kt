package com.example.event_tracker

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class CreateEventRequest(
    val eventName: String,
    val eventDescription: String,
    val startsOn: String,
    val locationId: Long
)

interface CreateEventApi {
    @POST("api/v1/events")
    suspend fun createEvent(
        @Body event: CreateEventRequest
    ): Response<Any>
}

object CreateEventClient {
    val api: CreateEventApi get() = RetrofitClient.createEventApi
}

@Composable
fun CreateEventScreen(
    modifier: Modifier = Modifier,
    onEventCreated: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Create Event")

        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Event name") },
            singleLine = true
        )

        OutlinedTextField(
            value = eventDate,
            onValueChange = { eventDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date") },
            placeholder = { Text("YYYY-MM-DD") },
            singleLine = true
        )

        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Start time") },
            placeholder = { Text("HH:mm") },
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description") },
            singleLine = true
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Location number") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (
                    eventName.isBlank() ||
                    eventDate.isBlank() ||
                    startTime.isBlank() ||
                    description.isBlank() ||
                    location.isBlank()
                ) {
                    Toast.makeText(
                        context,
                        "Please complete every field.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                scope.launch {
                    isSaving = true

                    try {
                        val response = CreateEventClient.api.createEvent(
                            CreateEventRequest(
                                eventName = eventName.trim(),
                                eventDescription = description.trim(),
                                startsOn = LocalDateTime.parse("${eventDate.trim()}T${startTime.trim()}")
                                    .atZone(ZoneId.of("America/Los_Angeles"))
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                locationId = location.trim().toLong().also {
                                    require(it > 0) { "Enter a valid location number" }
                                }
                            )
                        )

                        if (!response.isSuccessful) {
                            response.errorBody()?.close()
                            throw IllegalStateException(
                                "Server returned ${response.code()}"
                            )
                        }

                        Toast.makeText(
                            context,
                            "Event created.",
                            Toast.LENGTH_SHORT
                        ).show()

                        onEventCreated()
                    } catch (exception: Exception) {
                        Toast.makeText(
                            context,
                            "Could not create event: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        isSaving = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator()
            } else {
                Text("Create event")
            }
        }
    }
}
