package com.auctech.siprint.initials.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.databinding.ActivitySignUpBinding
import com.auctech.siprint.home.activity.MainActivity
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.bumptech.glide.Glide
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.lang.Exception

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signup.setOnClickListener {
            val name = binding.yourNameET.text.toString().trim()
            val email = binding.email.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && mUri != null && imageFile != null && isImageSelected)
                userRegistrationApi(name, email)
            else
                Toast.makeText(
                    this@SignUpActivity,
                    "All fields are mandatory",
                    Toast.LENGTH_SHORT
                ).show()
        }

        binding.profileIv.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK);
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private var mUri: Uri? = null
    private var isImageSelected = false
    private var imageFile: File? = null
    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            try {
                if (result.resultCode != RESULT_OK) {
                    mUri = null
                    isImageSelected = false
                    return@registerForActivityResult
                }
                mUri = result.data!!.data
                Glide.with(binding.profileIv.context).load(mUri).circleCrop()
                    .into(binding.profileIv)


                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(mUri!!, projection, null, null, null)
                val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                if (cursor != null && cursor.moveToFirst() && columnIndex != null) {
                    val filePath = cursor.getString(columnIndex)
                    imageFile = File(filePath)
                    isImageSelected = true
                } else {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Image is not valid.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                cursor?.close()


            } catch (e: IOException) {
                Log.d("ImagePick", "IOException : " + e.message)
                e.printStackTrace()
            }
        }

    private fun userRegistrationApi(name: String, email: String) {

        try {
            binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val userId = PreferenceManager.getStringValue(Constants.USER_ID) ?: ""
            val userIdRequestBody = userId.toRequestBody(MultipartBody.FORM)
            val nameRequestBody = name.toRequestBody(MultipartBody.FORM)
            val emailRequestBody = email.toRequestBody(MultipartBody.FORM)

            val imageRequestBody = imageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart =
                MultipartBody.Part.createFormData("photo", imageFile!!.name, imageRequestBody)

            val call = apiService.userRegistration(
                userIdRequestBody,
                nameRequestBody,
                emailRequestBody,
                imagePart
            )
            call.enqueue(object : Callback<ResponseSignup> {
                override fun onResponse(
                    call: Call<ResponseSignup>,
                    response: Response<ResponseSignup>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        val responseBody = response.body()
                        if (responseBody?.status == 200) {
                            PreferenceManager.setBoolValue(Constants.IS_SIGNUP, true)
                            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                responseBody?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    binding.loading.visibility = View.GONE
                }

                override fun onFailure(call: Call<ResponseSignup>, t: Throwable) {
                    binding.loading.visibility = View.GONE
                    t.printStackTrace()
                }

            })
        } catch (e: Exception) {
            binding.loading.visibility = View.GONE
            e.printStackTrace()
        }

    }
}