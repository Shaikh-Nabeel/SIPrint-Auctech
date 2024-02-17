package com.auctech.siprint.initials.activity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityLoginBinding
import com.auctech.siprint.home.activity.MainActivity
import com.auctech.siprint.initials.FetchCountryDataTask
import com.auctech.siprint.initials.IClickListener
import com.auctech.siprint.initials.adapters.CountryCodeLV
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
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.Locale
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private var phoneNumber: String = ""
    private var countryCode: String = "+996"
    private var jsonArrayOfCountries: JSONArray? = null

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
                    sendPhoneNumber("$countryCode$phoneNumber")
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
                showProgressBar()
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

        binding.lostPhone.setOnClickListener{
            startActivity(Intent(this@LoginActivity, EmailVerificationForPhone::class.java))
        }

        // Request notification permission only for Android level 13 or higher
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        } else {
            requestStoragePermission()
        }

        binding.selectedCountryCode.setOnClickListener{
            openSelectCountryCodeDialog()
        }

        val country = getUserCountry()
        val fetchCountryDataTask = FetchCountryDataTask(object:
            FetchCountryDataTask.OnDataFetchedListener {
            override fun onDataFetched(jsonData: String?) {
                jsonArrayOfCountries = JSONArray(jsonData)
                country?.apply {
                    getCountryCode(jsonArrayOfCountries!!, country).apply {
                        if(this != null){
                            countryCode = this
                            binding.selectedCountryCode.text = this
                        }
                    }
                }
            }

            override fun onError(errorMessage: String) {
//                Toast.makeText(this@LoginActivity, "Failed to load data.", Toast.LENGTH_SHORT).show()
            }
        })

        fetchCountryDataTask.execute()

    }

    // A function that takes a JSONArray of JSONObjects and a country name as parameters
// and returns the country code for that country if it exists in the array, or null otherwise
    fun getCountryCode(jsonArray: JSONArray, countryName: String): String? {
        // Iterate through the JSONObjects in the array
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            // Check if the country name matches the one in the parameter
            if (jsonObject.getString("name").equals(countryName,true)) {
                // Return the country code
                return jsonObject.getString("dial_code")
            }
        }
        // Return null if no match is found
        return null
    }


    private fun getUserCountry(): String? {
        // Get an instance of the TelephonyManager
        val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        // Try to get the country code from the SIM card or the network operator
        val countryCode = try {
            telephonyManager.simCountryIso ?: telephonyManager.networkCountryIso
        } catch (e: Exception) {
            // Handle any exception that may occur
            e.printStackTrace()
            null
        }
        // Check if the country code is valid
        if (countryCode != null && countryCode.length == 2) {
            // Convert the country code to upper case
            val upperCountryCode = countryCode.uppercase(Locale.US)
            // Get the country name from the Locale class
            // Return the country name
            return Locale(Locale.ENGLISH.language, upperCountryCode).displayCountry
        }
        return null
    }


    private fun openSelectCountryCodeDialog() {
        val uploadDialog = Dialog(this)
        uploadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        uploadDialog.setContentView(
            LayoutInflater.from(this)
                .inflate(R.layout.dialog_select_country, null, false)
        )
        uploadDialog.window!!.setGravity(Gravity.CENTER)
        uploadDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.85).toInt()
        uploadDialog.window!!.setLayout(width, height)

        val listView = uploadDialog.findViewById<ListView>(R.id.countryCode)

        val countrySelectListener = object: IClickListener{
            override fun onCountrySelected(code: String, name: String) {
                uploadDialog.dismiss()
                countryCode = code
                binding.selectedCountryCode.text = countryCode
                Toast.makeText(this@LoginActivity,"$name selected", Toast.LENGTH_SHORT).show()
            }
        }

        val countryCodeAdapter = CountryCodeLV(this@LoginActivity,countrySelectListener, jsonArrayOfCountries!!)
        listView.adapter = countryCodeAdapter

        uploadDialog.show()

    }


 /*   private fun makeOtpVerificationCall(otp: String) {
        val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)

        val call: Call<ResponseOtpVerification> = apiService.otpVerification(otp, "$countryCode$phoneNumber")
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

    }*/

    private fun userLogin2() {
        try{
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val call: Call<ResponseOtpVerification> = apiService.userLogin2("${countryCode.substring(1,countryCode.length)}$phoneNumber")
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
                                    "${countryCode.substring(1,countryCode.length)}$phoneNumber"
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
                                    "${countryCode.substring(1,countryCode.length)}$phoneNumber"
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
        showProgressBar()
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
                        hideProgressBar()
                    }

                    override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                        mVerificationId = s
                        resendingToken = forceResendingToken.toString()
                        hideProgressBar()
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
                    hideProgressBar()
                    userLogin2()
                    // startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                } else {
                    // Sign in failed.
                    hideProgressBar()
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
                Manifest.permission.READ_EXTERNAL_STORAGE
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
                    Manifest.permission.READ_EXTERNAL_STORAGE,
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
                            Manifest.permission.READ_EXTERNAL_STORAGE
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

    private fun showProgressBar() {
        try {
            binding.loading.visibility = View.VISIBLE
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
            binding.loading.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}