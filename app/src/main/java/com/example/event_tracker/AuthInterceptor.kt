package com.example.event_tracker

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    context: Context
) : Interceptor {

    private val preferences = context.getSharedPreferences(
        "auth",
        Context.MODE_PRIVATE
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = preferences.getString("access_token", null)

        val request = chain.request()
            .newBuilder()
            .apply {
                if (!token.isNullOrBlank()) {
                    addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                }
            }
            .build()

        return chain.proceed(request)
    }
}