package com.example.event_tracker
//import cst438.project2.backend.repository.InviteRepository

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class GuestHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EventManagerApp()
        }
    }
}

@Composable
fun EventManagerApp() {
    var showAdminScreen by rememberSaveable {
        mutableStateOf(false)
    }

    if (showAdminScreen) {
        AdminHomeScreen(
            onCreateEvent = {},
            onEditEvent = {}
        )
    } else {
        GuestHomeScreen(
            onManageScreen = {
                showAdminScreen = true
            },
//            onEditResponse = {},
//            onDetails = {},
//            onPublicEvents = {}
        )
    }
}

@Composable
fun GuestHomeScreen(
    onManageScreen: () -> Unit = {},
    onEditResponse: () -> Unit = {},
    onDetails: () -> Unit = {},
    onPublicEvents: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF3F3F3F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WELCOME, GUEST!", //replace with username
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(120.dp))

            ManageEventsButton(
                onClick = onManageScreen
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onPublicEvents
            ) {
                Text(text = "VIEW PUBLIC EVENTS",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = "JOINED EVENTS",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(16.dp))

            JoinedEventCard(
                eventName = "EXAMPLE EVENT",
                date = "OCT. 14, 2026",
                time = "3:00PM-8:00PM",
                attendees = 99, //replace with attendee count from
                onEditResponse = onEditResponse,
                onDetails = onDetails
            )
        }
    }
}

@Composable
fun AdminHomeScreen(
    onCreateEvent: () -> Unit = {},
    onEditEvent: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF3F3F3F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Manage Events",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(120.dp))

            Button(
                onClick = onCreateEvent
            ) {
                Text(
                    text = "CREATE EVENT",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = "MANAGED EVENTS",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(16.dp))

            EventCard(
                eventName = "EXAMPLE EVENT",
                date = "OCT. 14, 2026",
                time = "3:00PM-8:00PM",
                attendees = 99, //replace with attendee count from
                onEdit = onEditEvent
            )
        }
    }
}

@Composable
fun ManageEventsButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(
            text = "MANAGE EVENTS",
            fontSize = 18.sp
        )
    }
}

@Composable
fun JoinedEventCard(
    eventName: String,
    date: String,
    time: String,
    attendees: Int,
    onEditResponse: () -> Unit,
    onDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF929292)
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = eventName,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "DATE: $date\nTIME: $time",
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Details",
                    modifier = Modifier.weight(1f),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onEditResponse) {
                    Text(
                        text = "Edit Response",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GuestHomeActivityPreview() {
    GuestHomeScreen()
}