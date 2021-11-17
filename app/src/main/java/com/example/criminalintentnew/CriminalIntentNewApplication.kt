package com.example.criminalintentnew

import android.app.Application

class CriminalIntentNewApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}