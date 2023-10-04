package com.auctech.siprint.home.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityMainBinding
import com.auctech.siprint.home.response.ResponseDashboard
import com.auctech.siprint.initials.activity.LoginActivity
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment?
        val navController = navHostFragment?.navController

        val menu = PopupMenu(this, null)
        menu.inflate(R.menu.bottom_navigation)
        if (navController != null) {
            binding.navigation.setupWithNavController(menu.menu, navController)
        }
        if(intent.extras != null){
            if(intent.getStringExtra("notification") == "from"){
                    navController?.navigate(R.id.notificationFragment)
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(it.isSuccessful){
                token = it.result
                updateFcmToken()
            }
        }

//        updateFcmToken()
    }

    private fun updateFcmToken() {
        try {

            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val body = JsonObject()
            body.addProperty("user_id", PreferenceManager.getStringValue(Constants.USER_ID))
            body.addProperty("fcmToken", token)
            apiService.updateFcmToken(body)
                .enqueue(object : Callback<ResponseSignup> {
                    override fun onResponse(
                        call: Call<ResponseSignup>,
                        response: Response<ResponseSignup>
                    ) {
//                        Toast.makeText(
//                            this@MainActivity,
//                            response.body().toString(),
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }

                    override fun onFailure(call: Call<ResponseSignup>, t: Throwable) {
                        t.printStackTrace()
                    }

                })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}