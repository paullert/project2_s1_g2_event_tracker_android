package com.example.event_tracker

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
//import androidx.credentials.GetGoogleIdOption
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch


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
    onLogin: (String, String) -> Unit = { _, _ -> },
    onGoogleLogin: () -> Unit = {}
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

            var username by rememberSaveable {
                mutableStateOf("")
            }

            var password by rememberSaveable {
                mutableStateOf("")
            }

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
                onClick = { onLogin(username, password) },
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember {
        CredentialManager.create(context)
    }

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
        MainPageComposable(
            onLogin = { username, password ->
                if (username.isBlank() || password.isBlank()) {
                    Toast.makeText(
                        context,
                        "Username and password cannot be empty.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Call your backend username/password API here
                    isLoggedIn = true
                }
            },
            onGoogleLogin = {
                scope.launch {
                    try {
                        val googleOption = GetGoogleIdOption.Builder()
                            .setServerClientId(
                                "433179782438-nsl6671oqkia8ofcqdjm1hde0fhk8k9n.apps.googleusercontent.com"
                            )
                            .setFilterByAuthorizedAccounts(false)
                            .build()

                        val request = GetCredentialRequest(
                            listOf(googleOption)
                        )

                        val result = credentialManager.getCredential(
                            context = context,
                            request = request
                        )

                        val credential = result.credential

                        if (
                            credential is CustomCredential &&
                            credential.type ==
                            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleCredential =
                                GoogleIdTokenCredential.createFrom(
                                    credential.data
                                )

                            val response =
                                ApiClient.authApi.loginWithGoogle(
                                    GoogleLoginRequest(
                                        idToken = googleCredential.idToken
                                    )
                                )

                            println("Logged in as ${response.email}")
                            isLoggedIn = true
                        }

                    } catch (exception: Exception) {
                        Toast.makeText(
                            context,
                            "Google login failed: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }
}


    /*private fun signInWithGoogle() {
    val credentialManager = CredentialManager.create(androidContext)


    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false) // Use false to allow selecting any Google account
        .setServerClientId("YOUR_SERVER_CLIENT_ID") // From Google Cloud Console
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    lifecycleScope.launch {
        try {
            val result = credentialManager.getCredential(androidContext, request)
            handleSignIn(result)
        } catch (e: GetCredentialException) {
            // Handle error
        }
    }
}

private fun handleSignIn(result: GetCredentialResponse) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            // Send idToken to your backend for verification
            sendTokenToBackend(idToken)
        } catch (e: GoogleIdTokenParsingException) {
            // Handle parsing error
        }
    }
}*/

    /*@Composable
private fun LoginButton(

)*/

    /*@Composable
fun Context(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}*/
// Usage: context.toast("Message")

    @Preview(showBackground = true)
    @Composable
    fun MainPageComposablePreview() {
        MainPageComposable(
            onLogin = { _, _ -> },
            onGoogleLogin = {}
        )
    }

