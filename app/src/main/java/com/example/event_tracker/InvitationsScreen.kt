package com.example.event_tracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


@Composable
fun InvitationsScreen(){
    var invitations by remember{
        mutableStateOf<List<InviteResponse>>(emptyList())
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try{
            invitations = RetrofitClient.inviteApi.getMyInvites().content
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load invitations"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF383838)).padding(16.dp)
    ){
        Text(
            text = "My Invitations",
            fontSize = 30.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        if(errorMessage != null){
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        LazyColumn{
            items(invitations){ invitation ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp)

                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = invitation.eventName,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = invitation.eventDescription ?: "", textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Status: ${invitation.status}", textAlign = TextAlign.Center)

                        if (invitation.status == "PENDING") {
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try{
                                                val updatedInvite = RetrofitClient.inviteApi.respondToInvite(
                                                    eventId = invitation.eventId,
                                                    request = MyInviteRequest("ACCEPTED")
                                                )

                                                invitations = invitations.map{
                                                    if(it.eventId == updatedInvite.eventId){
                                                        updatedInvite
                                                    }else{
                                                        it
                                                    }
                                                }
                                                errorMessage = null
                                            } catch (e: Exception){
                                                errorMessage = "Failed to accept invitation"
                                            }
                                        }
                                    }
                                ) {
                                    Text("Accept")
                                }
                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            try{
                                                val updatedInvite = RetrofitClient.inviteApi.respondToInvite(
                                                    eventId = invitation.eventId,
                                                    request = MyInviteRequest("DECLINED")
                                                )

                                                invitations = invitations.map{
                                                    if(it.eventId == updatedInvite.eventId){
                                                        updatedInvite
                                                    }else{
                                                        it
                                                    }
                                                }
                                                errorMessage = null
                                            } catch (e: Exception){
                                                errorMessage = "Failed to decline invitation"
                                            }
                                        }
                                    }
                                ) {
                                    Text("Decline")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InvitationsScreenPreview(){
    InvitationsScreen()
}
