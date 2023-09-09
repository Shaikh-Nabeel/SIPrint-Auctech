package com.auctech.siprint

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MySiPrintApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        PreferenceManager.init(this)
//        FirebaseApp.initializeApp(applicationContext)
    }
}