package com.auctech.siprint

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.auctech.siprint.databinding.ActivitySplashBinding
import com.auctech.siprint.home.activity.MainActivity
import com.auctech.siprint.initials.activity.LoginActivity
import com.auctech.siprint.initials.activity.SignUpActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val start  = System.currentTimeMillis()
        val videoPath = "android.resource://" + packageName + "/" + R.raw.splashvideo;
//        binding.video.setVideoPath(videoPath)
//        binding.video.start()

        binding.video.setVideoURI(Uri.parse(videoPath))
        binding.video.setOnCompletionListener {
            val end  = System.currentTimeMillis()
            Log.d("timeTakeVid", "${start - end}")
            if (PreferenceManager.getBoolValue(Constants.IS_LOGIN)) {
                if (PreferenceManager.getBoolValue(Constants.IS_SIGNUP)) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, SignUpActivity::class.java))
                }
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
        binding.video.start()

        binding.root.background = ColorDrawable(ContextCompat.getColor(this,R.color.light))

//        executor.schedule({
//
//        }, 1500, TimeUnit.MILLISECONDS)

    }
}