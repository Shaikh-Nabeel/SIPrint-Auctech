package com.auctech.siprint.home.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityContactUsBinding
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactUsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phone.setText(PreferenceManager.getStringValue(Constants.USER_NUMBER))
        binding.yourNameET.setText(PreferenceManager.getStringValue(Constants.USER_NAME))
        binding.email.setText(PreferenceManager.getStringValue(Constants.USER_EMAIL)?:"")

        binding.signup.setOnClickListener {
            validateAndSubmitRequest()
        }
    }

    private fun validateAndSubmitRequest() {
        val name = binding.yourNameET.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val phone = binding.phone.text.toString().trim()
        val message = binding.message.text.toString().trim()

        // Validate name
        if (name.isEmpty()) {
            showToast("Name is required")
            return
        }

        // Validate email
        if (email.isEmpty()) {
            showToast("Email is required")
            return
        } else if (!isValidEmail(email)) {
            showToast("Enter a valid email address")
            return
        }

        // Validate phone
        if (phone.isEmpty()) {
            showToast("Phone is required")
            return
        }

        // Validate message
        if (message.isEmpty()) {
            showToast("Message is required")
            return
        }

        contactUs(name,email)

    }

    private fun contactUs(name: String,email: String) {
        try {
            showProgressBar()
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("mailfrom", email)
            jsonObject.addProperty("mailto", Constants.COMPANY_MAIL)
            jsonObject.addProperty("subject", name)
            jsonObject.addProperty("mailbody", createFormattedString())

            apiService.contactUs(jsonObject).enqueue(object : Callback<ResponseSignup> {
                override fun onResponse(
                    call: Call<ResponseSignup>,
                    response: Response<ResponseSignup>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        val responseSearch = response.body()
                        if (responseSearch?.status == 200) {
                            Toast.makeText(
                                this@ContactUsActivity,
                                "Request sent successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@ContactUsActivity,
                                responseSearch?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    hideProgressBar()
                }

                override fun onFailure(call: Call<ResponseSignup>, t: Throwable) {
                    hideProgressBar()
                    t.printStackTrace()
                }

            })
        } catch (e: Exception) {
            hideProgressBar()
            e.printStackTrace()
        }
    }

    private fun createFormattedString(): String {
        val name = binding.yourNameET.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val phone = binding.phone.text.toString().trim()
//        val website = binding.website.text.toString().trim()
        val message = binding.message.text.toString().trim()

        val formattedString = StringBuilder()

        if (name.isNotEmpty()) {
            formattedString.append("Name: $name\n")
        }

        if (email.isNotEmpty()) {
            formattedString.append("Email: $email\n")
        }

        if (phone.isNotEmpty()) {
            formattedString.append("Phone: $phone\n")
        }

//        if (website.isNotEmpty()) {
//            formattedString.append("Website: $website\n")
//        }

        if (message.isNotEmpty()) {
            formattedString.append("Message: $message\n")
        }

        return formattedString.toString()
    }


    private fun showToast(message: String) {
        Toast.makeText(this@ContactUsActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showProgressBar() {
        try {
            binding.loading.setVisibility(View.VISIBLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {
        try {
            binding.loading.setVisibility(View.GONE)
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}