package com.example.event_tracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri


enum class AuthMode {
    HomePage,
    ManageEvents,
    Details,
    PublicEvents
}

class MainActivity : ComponentActivity() {
    private lateinit var authorizationService: AuthorizationService
    private val auth: AuthViewModel by viewModels()
    private val googleLogin = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val response = data?.let(AuthorizationResponse::fromIntent)
        val error = data?.let(AuthorizationException::fromIntent)
        if (response != null) auth.completeLogin(response)
        else {
            if (BuildConfig.DEBUG) {
                Log.w("EventTrackerAuth", "Authorization did not complete: type=${error?.type}, code=${error?.code}")
            }
            auth.loginFailed(error?.errorDescription ?: "Google sign-in was canceled.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizationService = AuthorizationService(this)
        setContent {
            MaterialTheme {
                val user = auth.user
                when {
                    user != null -> SignedInScreen(user, onSignOut = auth::signOut)
                    else -> MainPageComposable(
                        busy = auth.busy,
                        error = auth.error,
                        registrationRequired = auth.registrationRequired,
                        username = auth.username,
                        onUsernameChange = { auth.username = it },
                        onRegister = auth::register,
                        onRetry = auth::restore,
                        onGoogleLogin = {
                            try {
                                auth.startLogin()
                                googleLogin.launch(GooglePkceManager(authorizationService).createLoginIntent())
                            } catch (exception: Exception) {
                                auth.loginFailed("Unable to open Google sign-in. Install or enable a browser.")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        authorizationService.dispose()
        super.onDestroy()
    }
}

@Composable
private fun SignedInScreen(user: UserResponse, onSignOut: () -> Unit) {
    var screen by rememberSaveable(user.userId) { mutableStateOf("home") }
    BackHandler(screen != "home") { screen = "home" }
    Column(Modifier.fillMaxSize().background(Color(0xFF3F3F3F))) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${user.username} (${user.role})", color = Color.White, modifier = Modifier.weight(1f))
            if (screen != "home") TextButton(onClick = { screen = "home" }) { Text("Back") }
            TextButton(onClick = onSignOut) { Text("Sign out") }
        }
        when (screen) {
            "invites" -> InvitationsScreen()
            "public" -> PublicEventList()
            "manage" -> AdminHomeScreen(onCreateEvent = { screen = "create" })
            "create" -> CreateEventScreen(onEventCreated = { screen = "manage" })
            else -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { screen = "invites" }) { Text("My invitations") }
                Button(onClick = { screen = "public" }) { Text("Public events") }
                if (user.role == "MANAGER" || user.role == "ADMIN") {
                    Button(onClick = { screen = "manage" }) { Text("Manage events") }
                }
            }
        }
    }
}

@Composable
fun MainPageComposable(
    modifier: Modifier = Modifier,
    onGoogleLogin: () -> Unit = {},
    busy: Boolean = false,
    error: String? = null,
    registrationRequired: Boolean = false,
    username: String = "",
    onUsernameChange: (String) -> Unit = {},
    onRegister: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Column(
        modifier.fillMaxSize().background(Color(0xFF383838)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        Text("Event Manager", color = Color.White, fontSize = 40.sp, fontFamily = FontFamily.Serif)
        if (busy) CircularProgressIndicator()
        error?.let { Text(it, color = Color(0xFFFFB4AB)) }
        if (registrationRequired) {
            Text("Choose a display name for your new account.", color = Color.White)
            OutlinedTextField(value = username, onValueChange = onUsernameChange,
                label = { Text("Display name") }, singleLine = true, enabled = !busy)
            Button(onClick = onRegister, enabled = !busy && username.isNotBlank()) { Text("Create account") }
        }
        Button(onClick = onGoogleLogin, enabled = !busy) { Text("Continue with Google") }
        if (error != null && !registrationRequired) {
            TextButton(onClick = onRetry, enabled = !busy) { Text("Retry connection") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPageComposablePreview() { MainPageComposable() }
