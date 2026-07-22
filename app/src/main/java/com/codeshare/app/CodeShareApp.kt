package com.codeshare.app

import android.app.Application
import com.google.firebase.FirebaseApp

class CodeShareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
