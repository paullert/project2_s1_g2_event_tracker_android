package com.example.event_tracker

import android.app.Application

class EventTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthSession.initialize(this)
    }
}
