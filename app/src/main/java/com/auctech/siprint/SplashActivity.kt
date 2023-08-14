package com.auctech.siprint

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.auctech.siprint.home.activity.MainActivity
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

//        val executor = Executors.newSingleThreadScheduledExecutor()

        Handler().postDelayed ({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)

//        executor.schedule({
//
//        }, 1500, TimeUnit.MILLISECONDS)

    }
}