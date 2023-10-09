package com.auctech.siprint.initials.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.databinding.ActivityLoginBinding
import com.auctech.siprint.home.activity.MainActivity
import com.auctech.siprint.initials.response.ResponseLogin
import com.auctech.siprint.initials.response.ResponseOtpVerification
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.numberEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for your case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 10) {
                    phoneNumber = s.toString()
//                    loginUser()
                    sendPhoneNumber("+91$phoneNumber")
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed for your case
            }
        })

        setupOtpEditTexts()

        binding.login.setOnClickListener {
            if (phoneNumber.isEmpty() || phoneNumber.length != 10) {
                Toast.makeText(
                    this@LoginActivity,
                    "Enter your phone number first.",
                    Toast.LENGTH_LONG
                ).show()
            }
            val otp = collectOtpFromEditTexts()
            if (!TextUtils.isEmpty(otp) && otp.length == 6) {
                if (mVerificationId == null){
                    Toast.makeText(this@LoginActivity,
                        "Verify mobile number before submitting otp",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                binding.login.isEnabled = false
                val credential = PhoneAuthProvider.getCredential(mVerificationId?:"", otp)
                binding.loading.visibility = View.VISIBLE
                signInWithPhoneAuthCredential(credential)
//                makeOtpVerificationCall(otp)
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Enter otp first",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Request notification permission only for Android level 13 or higher
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        } else {
            requestStoragePermission()
        }

    }

    private fun makeOtpVerificationCall(otp: String) {
        val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)

        val call: Call<ResponseOtpVerification> = apiService.otpVerification(otp, "91$phoneNumber")
        binding.loading.visibility = View.VISIBLE
        call.enqueue(object : Callback<ResponseOtpVerification> {
            override fun onResponse(
                call: Call<ResponseOtpVerification>,
                response: Response<ResponseOtpVerification>
            ) {
                binding.loading.visibility = View.GONE
                binding.login.isEnabled = true
                if (response.isSuccessful || response.code() == 200 && response.body() != null) {
                    val responseOtpVerification = response.body()
                    if (responseOtpVerification?.status == 200) {
                        val statusOfUser = responseOtpVerification.data?.status
                        if (statusOfUser.equals(Constants.BLOCKED)) {
                            Toast.makeText(
                                this@LoginActivity,
                                responseOtpVerification.message,
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (statusOfUser.equals(Constants.NEW_REGISTRATION)) {
                            PreferenceManager.setStringValue(
                                Constants.USER_NUMBER,
                                "91$phoneNumber"
                            )
                            PreferenceManager.setStringValue(
                                Constants.USER_ID,
                                responseOtpVerification.data?.id
                            )
                            PreferenceManager.setBoolValue(
                                Constants.IS_LOGIN,
                                true
                            )
                            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
                            finish()
                        } else if (statusOfUser.equals(Constants.VALID)) {
                            PreferenceManager.setStringValue(
                                Constants.USER_NUMBER,
                                "91$phoneNumber"
                            )
                            PreferenceManager.setStringValue(
                                Constants.USER_ID,
                                responseOtpVerification.data?.id
                            )
                            PreferenceManager.setBoolValue(
                                Constants.IS_LOGIN,
                                true
                            )
                            PreferenceManager.setBoolValue(
                                Constants.IS_SIGNUP,
                                true
                            )
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            responseOtpVerification?.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        Constants.ERR_MESSAGE,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseOtpVerification>, t: Throwable) {
                binding.login.isEnabled = true
                binding.loading.visibility = View.GONE
                t.printStackTrace()
            }
        })
    }


    private fun loginUser() {

        val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
        val call: Call<ResponseLogin> = apiService.userLogin("91$phoneNumber")
        binding.loading.visibility = View.VISIBLE
        call.enqueue(object : Callback<ResponseLogin> {
            override fun onResponse(call: Call<ResponseLogin>, response: Response<ResponseLogin>) {
                binding.loading.visibility = View.GONE
                if (response.body() != null && response.isSuccessful || response.code() == 200) {
                    val loginResponse = response.body()
                    if (loginResponse?.status == 200) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Your otp is " + loginResponse.data,
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            loginResponse?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        Constants.ERR_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                binding.loading.visibility = View.GONE
                t.printStackTrace()
            }
        })

    }

    private fun userLogin2() {
        try{
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val call: Call<ResponseOtpVerification> = apiService.userLogin2("91$phoneNumber")
            binding.loading.visibility = View.VISIBLE
            call.enqueue(object : Callback<ResponseOtpVerification> {
                override fun onResponse(call: Call<ResponseOtpVerification>, response: Response<ResponseOtpVerification>) {

                    if (response.isSuccessful || response.code() == 200 && response.body() != null) {
                        val responseOtpVerification = response.body()
                        if (responseOtpVerification?.status == 200) {
                            val statusOfUser = responseOtpVerification.data?.status
                            if (statusOfUser.equals(Constants.BLOCKED)) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    responseOtpVerification.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.loading.visibility = View.GONE
                            } else if (statusOfUser.equals(Constants.NEW_REGISTRATION)) {
                                PreferenceManager.setStringValue(
                                    Constants.USER_NUMBER,
                                    "91$phoneNumber"
                                )
                                PreferenceManager.setStringValue(
                                    Constants.USER_ID,
                                    responseOtpVerification.data?.id
                                )
                                PreferenceManager.setBoolValue(
                                    Constants.IS_LOGIN,
                                    true
                                )
                                binding.loading.visibility = View.GONE
                                startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
                                finish()
                            } else if (statusOfUser.equals(Constants.VALID)) {
                                PreferenceManager.setStringValue(
                                    Constants.USER_NUMBER,
                                    "91$phoneNumber"
                                )
                                PreferenceManager.setStringValue(
                                    Constants.USER_ID,
                                    responseOtpVerification.data?.id
                                )
                                PreferenceManager.setBoolValue(
                                    Constants.IS_LOGIN,
                                    true
                                )
                                PreferenceManager.setBoolValue(
                                    Constants.IS_SIGNUP,
                                    true
                                )
                                binding.loading.visibility = View.GONE
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            binding.loading.visibility = View.GONE
                            Toast.makeText(
                                this@LoginActivity,
                                responseOtpVerification?.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            Constants.ERR_MESSAGE,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.login.isEnabled = true
                }

                override fun onFailure(call: Call<ResponseOtpVerification>, t: Throwable) {
                    binding.login.isEnabled = true
                    binding.loading.visibility = View.GONE
                    t.printStackTrace()
                }
            })
        }catch (e: Exception){
            binding.login.isEnabled = true
            binding.loading.visibility = View.GONE
            e.printStackTrace()
        }

    }

    var auth = FirebaseAuth.getInstance()
    private var mVerificationId: String? = null
    private var resendingToken: String? = null
    private fun sendPhoneNumber(phoneNumber: String) {
        binding.loading.visibility = View.VISIBLE
        if (phoneNumber.isNotEmpty()) {
            val options = PhoneAuthOptions
                .newBuilder().setTimeout(60L, TimeUnit.SECONDS)
                .setPhoneNumber(phoneNumber)
                .setActivity(this)
                .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Verification failed: " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.loading.visibility = View.GONE
                    }

                    override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                        mVerificationId = s
                        resendingToken = forceResendingToken.toString()
                        binding.loading.visibility = View.GONE
                        Toast.makeText(
                            this@LoginActivity,
                            "OTP sent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }).build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(
                this@LoginActivity,
                "Please enter a phone number",
                Toast.LENGTH_SHORT
            ).show()
            binding.loading.visibility = View.GONE
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    Log.d("adsfsdf", "reached")
                    // User successfully signed in.
                    val user = task.result.user
                    Log.d("AlreadySignIn", "number: " + user!!.phoneNumber)
//                    Toast.makeText(
//                        this@LoginActivity,
//                        "Sign in successful",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    binding.loading.setVisibility(View.GONE)
                    userLogin2()
                    // startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                } else {
                    // Sign in failed.
                    binding.loading.setVisibility(View.GONE)
                    Toast.makeText(
                        this@LoginActivity,
                        "Sign in failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun collectOtpFromEditTexts(): String {
        val editTexts = arrayOf(binding.otpET1, binding.otpET2, binding.otpET3, binding.otpET4,binding.otpET5,binding.otpET6)
        val otpStringBuilder = StringBuilder()

        for (editText in editTexts) {
            val otpDigit = editText.text.toString()
            otpStringBuilder.append(otpDigit)
        }

        return otpStringBuilder.toString()
    }


    private fun setupOtpEditTexts() {
        val editTexts = arrayOf(binding.otpET1, binding.otpET2, binding.otpET3, binding.otpET4,binding.otpET5,binding.otpET6)

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // Not needed for your case
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        // Move focus to the next EditText, if available
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.isEmpty() == true) {
                        // Move focus to the previous EditText, if available
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Not needed for your case
                }
            })
        }
    }

    // Define constants for permission request codes
    private val STORAGE_PERMISSION_REQUEST_CODE = 1
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 2

    // Define a function to request storage permission
    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    // Override the onRequestPermissionsResult function to handle the result of permission requests

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // Permission is denied, check if the user checked the "Don't ask again" option
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        // User did not check the option, show the permission request again
                        requestStoragePermission()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Grant storage permission to continue",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                    }
                }
            }

            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    ) {
                        // User did not check the option, show the permission request again
                        requestNotificationPermission()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Grant notification permission to continue",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        startActivityForResult(intent, NOTIFICATION_PERMISSION_REQUEST_CODE)
                    }
                }
            }
        }
    }

    // Override the onActivityResult function to handle the result of notification permission request
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                // Check if the user granted or denied the permission
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                } else {
                    Toast.makeText(
                        this,
                        "Please enable notification permission for this app",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}