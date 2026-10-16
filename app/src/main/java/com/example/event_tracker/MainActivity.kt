package com.example.event_tracker

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontFamily.Companion.Serif
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


enum class AuthMode {
    Login,
    HomePage,
    ManageEvents,
    Details,
    PublicEvents
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RuntimeLoginScreen()
        }
    }
}


@Composable
fun MainPageComposable(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit = {},
    onGoogleLogin: () -> Unit = {},
    //RuntimeLoginScreen: () -> Unit = {}

) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A4A4A),
                        Color(0xFF383838)
                    )
                )
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Event Manager",
                color = Color.White,
                fontSize = 52.sp,
                fontFamily = Serif,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Login",
                color = Color.White,
                fontSize = 30.sp,
                fontFamily = Serif,
                lineHeight = 62.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(38.dp))

            var username = ""
            var password = ""

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.username_label)) },

                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF7DD3FC),
                    unfocusedIndicatorColor = Color(0xFF475569),
                    focusedLabelColor = Color(0xFF7DD3FC),
                    unfocusedLabelColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password_label)) },

                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF7DD3FC),
                    unfocusedIndicatorColor = Color(0xFF475569),
                    focusedLabelColor = Color(0xFF7DD3FC),
                    unfocusedLabelColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(16.dp))

            GoogleLoginButton(
                onClick = onGoogleLogin
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello, $name!",
        modifier = modifier
    )
}

@Composable
fun GoogleLoginButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick
    ) {
        Text("Continue with Google")
    }
}

@Composable
fun RuntimeLoginScreen() {
    var isLoggedIn by rememberSaveable {
        mutableStateOf(false)
    }

    if (isLoggedIn) {
        GuestHomeScreen(
            onManageScreen = {
                // Navigate to admin screen
            },
            onEditResponse = {},
            onDetails = {},
            onPublicEvents = {}
        )
    } else {
        val googleLogin = supabase.composeAuth.rememberSignInWithGoogle(
            onResult = { result ->
                when (result) {
                    is NativeSignInResult.Success -> {
                        isLoggedIn = true
                    }

                    is NativeSignInResult.Error -> {
                        println("Google login failed")
                    }

                    is NativeSignInResult.NetworkError -> {
                        println("Network error")
                    }

                    NativeSignInResult.ClosedByUser -> {
                        println("Login cancelled")
                    }
                }
            }
        )

        MainPageComposable(
            onLogin = {
                // After successful email/password authentication:
                isLoggedIn = true
            },
            onGoogleLogin = {
                googleLogin.startFlow()
                isLoggedIn = true
            }
        )
    }
}

/*@Composable
private fun LoginButton(

)*/

@Preview(showBackground = true)
@Composable
fun MainPageComposablePreview() {
    MainPageComposable(
        onLogin = {},
        onGoogleLogin = {}
    )
}