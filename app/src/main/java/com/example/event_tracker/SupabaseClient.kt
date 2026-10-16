package com.example.event_tracker

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient

val supabase = createSupabaseClient(
    supabaseUrl = "YOUR_SUPABASE_URL",
    supabaseKey = "YOUR_SUPABASE_PUBLISHABLE_KEY"
) {
    install(Auth)

    install(ComposeAuth) {
        googleNativeLogin(
            serverClientId =
                "433179782438-p68gd5nr62ucsn83tqtove2lhibhcrqi.apps.googleusercontent.com"
        )
    }
}