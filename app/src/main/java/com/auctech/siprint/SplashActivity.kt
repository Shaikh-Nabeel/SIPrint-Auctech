package com.auctech.siprint

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.auctech.siprint.databinding.ActivitySplashBinding
import com.auctech.siprint.home.activity.MainActivity
import com.auctech.siprint.initials.activity.LoginActivity
import com.auctech.siprint.initials.activity.SignUpActivity
import com.bumptech.glide.Glide
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .asGif()
            .load(R.drawable.splash)
            .into(binding.video)

        Handler().postDelayed({
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
        }, 3000)



//        val start  = System.currentTimeMillis()
//        val videoPath = "android.resource://" + packageName + "/" + R.raw.splashvideo;
//        binding.video.setVideoPath(videoPath)
//        binding.video.start()



//        binding.video.setVideoURI(Uri.parse(videoPath))
//        binding.video.setOnCompletionListener {
//            if (PreferenceManager.getBoolValue(Constants.IS_LOGIN)) {
//                if (PreferenceManager.getBoolValue(Constants.IS_SIGNUP)) {
//                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//                } else {
//                    startActivity(Intent(this@SplashActivity, SignUpActivity::class.java))
//                }
//            } else {
//                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
//            }
//            finish()
//        }
//        binding.video.start()

//        binding.root.background = ColorDrawable(ContextCompat.getColor(this,R.color.light))

//        val firebaseMessaging = FirebaseMessaging.getInstance()
//        firebaseMessaging.token.addOnCompleteListener {
//            System.out.println(it.result);
//            Log.d("fbTokengcm", it.result)
//        }

//        executor.schedule({
//
//        }, 1500, TimeUnit.MILLISECONDS)

//        var standardIntegrityManager = IntegrityManagerFactory.createStandard(applicationContext);
//
//        var integrityTokenProvider: StandardIntegrityTokenProvider? = null
//        var cloudProjectNumber = 0L
//
//// Prepare integrity token. Can be called once in a while to keep internal
//// state fresh.
//        standardIntegrityManager.prepareIntegrityToken(
//            StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
//                .setCloudProjectNumber(cloudProjectNumber)
//                .build())
//            .addOnSuccessListener{
//                integrityTokenProvider = it
//            }
//        .addOnFailureListener{
//            println("Integrity Error: ======================> =============> ${it.message}")
//        }

    }
}