package com.auctech.siprint.initials.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityEmailVerificationForPhoneBinding
import com.auctech.siprint.databinding.ActivityLoginBinding
import com.auctech.siprint.home.activity.MainActivity
import com.auctech.siprint.initials.response.ResponseEmailOtpVerification
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.profile.activity.ChangeMobileActivity
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class EmailVerificationForPhone : AppCompatActivity() {

    private lateinit var binding: ActivityEmailVerificationForPhoneBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerificationForPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.emailId.requestFocus()


        binding.sendOtp.setOnClickListener{
            if(binding.sendOtp.text == "Get OTP"){
                val email = binding.emailId.text.toString()
                if(!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    this@EmailVerificationForPhone.email = email
                    sendOtpToEmail()
                }else{
                    Toast.makeText(this@EmailVerificationForPhone, "Enter valid email", Toast.LENGTH_SHORT).show()
                }
            }else{
                val otp = collectOtpFromEditTexts()
                if (!TextUtils.isEmpty(otp) && otp.length == 6) {
                    verifyEmailOtp(otp)
                } else {
                    Toast.makeText(
                        this@EmailVerificationForPhone,
                        "Enter otp first",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }

        setupOtpEditTexts()

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                if(binding.emailLl.visibility == View.GONE) {
                    binding.emailLl.visibility = View.VISIBLE
                    binding.numberTv.visibility = View.GONE
                    binding.sendOtp.text = "Get OTP"
                    binding.otpTv.visibility = View.GONE
                    binding.otpLl.visibility = View.GONE
                }else{
                    finish()
                }
            }
        } else {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    if(binding.emailLl.visibility == View.GONE) {
                        binding.emailLl.visibility = View.VISIBLE
                        binding.numberTv.visibility = View.GONE
                        binding.sendOtp.text = "Get OTP"
                        binding.otpTv.visibility = View.GONE
                        binding.otpLl.visibility = View.GONE
                    }else{
                        finish()
                    }
                }

            })
        }
        showKeyboard()
    }

    private fun showKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.emailId, InputMethodManager.SHOW_IMPLICIT)
    }

    private var email: String? = null
    private fun sendOtpToEmail() {
        try {
            showProgressBar()
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("email", email)
            val call = apiService.emailOtpForMobileChange(jsonObject)
            call.enqueue(object : Callback<ResponseSignup> {
                override fun onResponse(
                    call: Call<ResponseSignup>,
                    response: Response<ResponseSignup>
                ) {
                    hideProgressBar()
                    if (response.code() == 200 && response.body() != null) {
                        val responseBody = response.body()
                        if (responseBody?.status == 200) {
                            binding.emailLl.visibility = View.GONE
                            binding.numberTv.visibility = View.GONE
                            slideUpAnimation(binding.otpLl)
                            binding.otpLl.visibility = View.VISIBLE
                            binding.otpTv.visibility = View.VISIBLE
                            binding.sendOtp.text = "Verify OTP"
                        } else {
                            Toast.makeText(
                                this@EmailVerificationForPhone,
                                responseBody?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
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

    private fun verifyEmailOtp(otp: String) {
        try {
            showProgressBar()
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("email", email)
            jsonObject.addProperty("otp", otp)
            val call = apiService.verifyOtpForMobileChange(jsonObject)
            call.enqueue(object : Callback<ResponseEmailOtpVerification> {
                override fun onResponse(
                    call: Call<ResponseEmailOtpVerification>,
                    response: Response<ResponseEmailOtpVerification>
                ) {
                    hideProgressBar()
                    if (response.code() == 200 && response.body() != null) {
                        val responseBody = response.body()
                        if (responseBody?.status == 200) {
                            val intent = Intent(this@EmailVerificationForPhone, ChangeMobileActivity::class.java)
                            PreferenceManager.setStringValue(Constants.USER_ID, responseBody.data!!.id!!)
                            intent.putExtra("from", "EmailVerification")
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@EmailVerificationForPhone,
                                responseBody?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseEmailOtpVerification>, t: Throwable) {
                    hideProgressBar()
                    t.printStackTrace()
                }

            })
        } catch (e: Exception) {
            hideProgressBar()
            e.printStackTrace()
        }
    }

    private fun slideUpAnimation(view: View) {
        val animate = TranslateAnimation(
            0f, // fromXDelta
            0f, // toXDelta
            view.height.toFloat(), // fromYDelta
            0f // toYDelta
        )
        animate.duration = 1000
        view.startAnimation(animate)
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

}