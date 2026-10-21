package com.example.event_tracker

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private const val GOOGLE_WEB_CLIENT_ID =
    "433179782438-nsl6671oqkia8ofcqdjm1hde0fhk8k9n.apps.googleusercontent.com"

private const val BACKEND_URL =
    "https://YOUR_BACKEND_URL/" //Replace with render

data class GoogleLoginRequest(
    val idToken: String
)

data class AuthResponse(
    val accessToken: String,
    val email: String,
    val name: String
)

interface AuthApi {

    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): AuthResponse
}

object ApiClient {

    val eventApi: Any
        get() {
            TODO()
        }
    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}

@Composable
fun GoogleLoginButton(
    onLoginSuccess: (AuthResponse) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val credentialManager = CredentialManager.create(context)

    Button(
        onClick = {
            scope.launch {
                try {
                    val googleOption = GetGoogleIdOption.Builder()
                        .setServerClientId(GOOGLE_WEB_CLIENT_ID)
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

                        val response = ApiClient.authApi.loginWithGoogle(
                            GoogleLoginRequest(
                                idToken = googleCredential.idToken
                            )
                        )

                        onLoginSuccess(response)
                    } else {
                        Toast.makeText(
                            context,
                            "Invalid Google credential.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (exception: Exception) {
                    Log.e(
                        "GoogleLogin",
                        "Google login failed",
                        exception
                    )

                    Toast.makeText(
                        context,
                        "Google login failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    ) {
        Text("Continue with Google")
    }
}