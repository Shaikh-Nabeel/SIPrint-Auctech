package com.auctech.siprint.profile.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityChangeMobileBinding
import com.auctech.siprint.home.activity.MainActivity
import com.auctech.siprint.initials.FetchCountryDataTask
import com.auctech.siprint.initials.IClickListener
import com.auctech.siprint.initials.activity.SignUpActivity
import com.auctech.siprint.initials.adapters.CountryCodeLV
import com.auctech.siprint.initials.response.ResponseOtpVerification
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.JsonObject
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.concurrent.TimeUnit

class ChangeMobileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeMobileBinding
    private var phoneNumber: String = ""
    private var countryCode = "+996"
    private var fromEmailScreen = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeMobileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent != null && intent.hasExtra("from")) {
            fromEmailScreen = true
        }
        if(!fromEmailScreen){
            val mobNo = PreferenceManager.getStringValue(Constants.USER_NUMBER)
            binding.numberEt.setText(mobNo?.substring(2, mobNo.length))
            binding.selectedCountryCode.text = "+${mobNo?.substring(0,2)}"
            countryCode = "+${mobNo?.substring(0,2)}"
        }

        binding.numberEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for your case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 10) {
                    phoneNumber = s.toString()
                    if("${countryCode.substring(1,countryCode.length)}$phoneNumber" == PreferenceManager.getStringValue(Constants.USER_NUMBER)){
                        Toast.makeText(
                            this@ChangeMobileActivity,
                            "Enter different mobile number",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
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
                    this@ChangeMobileActivity,
                    "Enter your phone number first.",
                    Toast.LENGTH_LONG
                ).show()
            }
            val otp = collectOtpFromEditTexts()
            if (!TextUtils.isEmpty(otp) && otp.length == 6) {
                if (mVerificationId == null){
                    Toast.makeText(this@ChangeMobileActivity,
                    "Verify mobile number before submitting otp",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                binding.login.isEnabled = false
                val credential = PhoneAuthProvider.getCredential(mVerificationId ?: "", otp)
                showProgressBar()
                signInWithPhoneAuthCredential(credential)
//                makeOtpVerificationCall(otp)
            } else {
                Toast.makeText(
                    this@ChangeMobileActivity,
                    "Enter OTP to continue",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.selectedCountryCode.setOnClickListener{
            openSelectCountryCodeDialog()
        }
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

        val countrySelectListener = object: IClickListener {
            override fun onCountrySelected(code: String, name: String) {
                uploadDialog.dismiss()
                countryCode = code
                binding.selectedCountryCode.text = countryCode
                Toast.makeText(this@ChangeMobileActivity,"$name selected", Toast.LENGTH_SHORT).show()
            }
        }

        val fetchCountryDataTask = FetchCountryDataTask(object:
            FetchCountryDataTask.OnDataFetchedListener {
            override fun onDataFetched(jsonData: String?) {
                hideProgressBar()
                val countryCodeAdapter = CountryCodeLV(this@ChangeMobileActivity,countrySelectListener, JSONArray(jsonData))
                listView.adapter = countryCodeAdapter
            }

            override fun onError(errorMessage: String) {
                hideProgressBar()
                Toast.makeText(this@ChangeMobileActivity, "Failed to load data.", Toast.LENGTH_SHORT).show()
            }
        })

        fetchCountryDataTask.execute()
        showProgressBar()

        uploadDialog.show()

    }

    private fun userLogin2() {
        try {
            showProgressBar()
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("user_id", PreferenceManager.getStringValue(Constants.USER_ID))
            jsonObject.addProperty("phone", "${countryCode.substring(1,countryCode.length)}$phoneNumber")
            apiService.updatePhone(jsonObject).enqueue(object : Callback<ResponseSignup> {
                override fun onResponse(
                    call: Call<ResponseSignup>,
                    response: Response<ResponseSignup>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        val responseSearch = response.body()
                        if (responseSearch?.status == 200) {
                            PreferenceManager.setStringValue(Constants.USER_NUMBER, "${countryCode.substring(1,countryCode.length)}$phoneNumber")
                            PreferenceManager.setBoolValue(
                                Constants.IS_LOGIN,
                                true
                            )
                            PreferenceManager.setBoolValue(
                                Constants.IS_SIGNUP,
                                true
                            )
                            Toast.makeText(
                                this@ChangeMobileActivity,
                                "Phone updated successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                            if(fromEmailScreen){
                                startActivity(Intent(this@ChangeMobileActivity, MainActivity::class.java))
                                finish()
                            }else{
                                finish()
                            }
                        } else {
                            Toast.makeText(
                                this@ChangeMobileActivity,
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
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(
                            this@ChangeMobileActivity,
                            "Verification failed: " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        hideProgressBar()
                    }

                    override fun onCodeSent(
                        s: String,
                        forceResendingToken: PhoneAuthProvider.ForceResendingToken
                    ) {
                        mVerificationId = s
                        resendingToken = forceResendingToken.toString()
                        hideProgressBar()
                        Toast.makeText(
                            this@ChangeMobileActivity,
                            "OTP sent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }).build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(
                this@ChangeMobileActivity,
                "Please enter a phone number",
                Toast.LENGTH_SHORT
            ).show()
            hideProgressBar()
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
//                        this@ChangeMobileActivity,
//                        "Sign in successful",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    userLogin2()
                    // startActivity(Intent(this@ChangeMobileActivity, HomeActivity::class.java))
                } else {
                    // Sign in failed.
                    Toast.makeText(
                        this@ChangeMobileActivity,
                        "Sign in failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.loading.setVisibility(View.GONE)
            }
    }

    private fun collectOtpFromEditTexts(): String {
        val editTexts = arrayOf(
            binding.otpET1,
            binding.otpET2,
            binding.otpET3,
            binding.otpET4,
            binding.otpET5,
            binding.otpET6
        )
        val otpStringBuilder = StringBuilder()

        for (editText in editTexts) {
            val otpDigit = editText.text.toString()
            otpStringBuilder.append(otpDigit)
        }

        return otpStringBuilder.toString()
    }

    private fun setupOtpEditTexts() {
        val editTexts = arrayOf(
            binding.otpET1,
            binding.otpET2,
            binding.otpET3,
            binding.otpET4,
            binding.otpET5,
            binding.otpET6
        )

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