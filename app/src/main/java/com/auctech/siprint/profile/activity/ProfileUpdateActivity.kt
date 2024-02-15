package com.auctech.siprint.profile.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityProfileUpdateBinding
import com.auctech.siprint.home.activity.MainActivity
import com.auctech.siprint.profile.response.ReponseUpdateDetail
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ProfileUpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.yourNameET.setText(PreferenceManager.getStringValue(Constants.USER_NAME))
        binding.mobileNo.text = PreferenceManager.getStringValue(Constants.USER_NUMBER)
        binding.mobileNo.setOnClickListener {
            startActivity(Intent(this, ChangeMobileActivity::class.java))
        }
        val photoUrl = PreferenceManager.getStringValue(Constants.USER_PHOTO)
        if(photoUrl.isNullOrEmpty()){
            if(PreferenceManager.getStringValue(Constants.USER_GENDER).equals("Male", true)){
                Glide.with(this)
                    .load(R.mipmap.male)
                    .circleCrop()
                    .into(binding.profileIv)
            }else{
                Glide.with(this)
                    .load(R.mipmap.female)
                    .circleCrop()
                    .into(binding.profileIv)
            }
        }else{
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.mipmap.profile)
                .into(binding.profileIv)
        }

        binding.email.setText(PreferenceManager.getStringValue(Constants.USER_EMAIL))

        val gender = PreferenceManager.getStringValue(Constants.USER_GENDER)

        val listOfPassenger: MutableList<String> = ArrayList()
        listOfPassenger.add("Male")
        listOfPassenger.add("Female")
        val adapter1: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, R.layout.drop_down_tv, listOfPassenger as List<Any?>)
        binding.gender.setAdapter(adapter1)

//        binding.gender.setText("Gender", false)

        if(gender.equals("Male", true)){
            binding.gender.setText("Male",false)
        }else{
            binding.gender.setText("Female",false)
        }

        binding.icon.setOnClickListener {
            openImagePicker()
        }

        binding.update.setOnClickListener {
            val name = binding.yourNameET.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val phone = binding.mobileNo.text.toString().trim()
            val gender1 = binding.gender.text.toString().trim()

            if(validateInputs(name,gender1)){
                updateDetails(name,email,phone,gender1)
            }
        }


    }

    private fun updateDetails(name: String, email: String, phone: String, gender1: String) {

        try {
            binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val userId = PreferenceManager.getStringValue(Constants.USER_ID) ?: ""
            val userIdRequestBody = userId.toRequestBody(MultipartBody.FORM)
            val nameRequestBody = name.toRequestBody(MultipartBody.FORM)
            val emailRequestBody = email.toRequestBody(MultipartBody.FORM)
            val genderRequestBody = gender1.toRequestBody(MultipartBody.FORM)
            val phoneRequestBody = phone.toRequestBody(MultipartBody.FORM)

//            val imageRequestBody = imageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
//            val imageRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), contentResolver?.openInputStream(mUri!!)!!.readBytes())

//            val imagePart =
//                MultipartBody.Part.createFormData("photo", imageFile!!.name, imageRequestBody)

//            val imagePart =
//                MultipartBody.Part.createFormData("photo", fileName, imageRequestBody)

            var imagePart: MultipartBody.Part? = null

            // Check if the user has chosen a photo
            if (mUri != null) {
                val imageRequestBody =
                    RequestBody.create("image/*".toMediaTypeOrNull(), contentResolver?.openInputStream(
                        mUri!!
                    )!!.readBytes())
                imagePart = MultipartBody.Part.createFormData("photo", fileName, imageRequestBody)
            }


            val call = apiService.updateUserDetail(
                userIdRequestBody,
                nameRequestBody,
                emailRequestBody,
                phoneRequestBody,
                genderRequestBody,
                imagePart
            )
            call.enqueue(object : Callback<ReponseUpdateDetail> {
                override fun onResponse(
                    call: Call<ReponseUpdateDetail>,
                    response: Response<ReponseUpdateDetail>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        val responseBody = response.body()
                        if (responseBody?.status == 200) {

                            val userData = responseBody.data!!

                            PreferenceManager.setStringValue(
                                Constants.USER_NAME,
                                userData.name!!
                            )
                            PreferenceManager.setStringValue(
                                Constants.USER_EMAIL,
                                userData.email!!
                            )
                            PreferenceManager.setStringValue(
                                Constants.USER_PHOTO,
                                userData.photo!!
                            )
                            PreferenceManager.setStringValue(
                                Constants.USER_GENDER,
                                userData.gender!!
                            )

//                            startActivity(Intent(this@ProfileUpdateActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@ProfileUpdateActivity,
                                responseBody?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    binding.loading.visibility = View.GONE
                }

                override fun onFailure(call: Call<ReponseUpdateDetail>, t: Throwable) {
                    binding.loading.visibility = View.GONE
                    t.printStackTrace()
                }

            })
        } catch (e: Exception) {
            binding.loading.visibility = View.GONE
            e.printStackTrace()
        }

    }

    private fun openImagePicker() {
        //commented code not working one plus.

//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
//        galleryLauncher.launch(intent)

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        galleryLauncher.launch(intent)
    }

    private var mUri: Uri? = null
    private var isImageSelected = false
    private var fileName = ""
    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            try {
                if (result.resultCode != RESULT_OK) {
                    mUri = null
                    isImageSelected = false
                    Glide.with(binding.profileIv.context).load(R.mipmap.profile).circleCrop()
                        .into(binding.profileIv)
                    return@registerForActivityResult
                }
                mUri = result.data!!.data
                Glide.with(binding.profileIv.context).load(mUri).circleCrop()
                    .into(binding.profileIv)


                val cursor = contentResolver?.query(mUri!!, null, null, null, null)

                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    fileName = cursor.getString(index)
//                    fileSize = cursor.getLong(sizeIndex)
//                    val filePath = cursor.getString(index)
//                    imageFile = File(filePath)
                    isImageSelected = true
//                    if (fileName != null && fileSize != null && fileName!!.endsWith(".pdf")) {
//                        if (isFileLessThan1MB(fileSize!!)) {
//                            showUploadConfirmationDialog()
//                        } else {
//                            Toast.makeText(
//                                activity,
//                                "File should be less than 1MB",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    } else {
//                        Toast.makeText(activity, "PDF file is supported only.", Toast.LENGTH_SHORT)
//                            .show()
//                    }
                } else {
                    isImageSelected = false
                    Toast.makeText(
                        this@ProfileUpdateActivity,
                        "Image not valid.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                cursor?.close()

//              This code is for the intent which is commented above

//                val projection = arrayOf(MediaStore.Images.Media.DATA)
//                val cursor = contentResolver.query(mUri!!, projection, null, null, null)
//                val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//
//                if (cursor != null && cursor.moveToFirst() && columnIndex != null) {
//                    val filePath = cursor.getString(columnIndex)
//                    imageFile = File(filePath)
//                    isImageSelected = true
//                } else {
//                    Toast.makeText(
//                        this@SignUpActivity,
//                        "Image is not valid.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                cursor?.close()


            } catch (e: IOException) {
                Log.d("ImagePick", "IOException : " + e.message)
                e.printStackTrace()
            }
        }

    private fun validateInputs(name: String?, gender : String?): Boolean {
        if (name.isNullOrEmpty()) {
            showToast("Name Cannot be Empty!")
            return false
        }

        if (binding.email.text.toString().isEmpty()) {
            showToast("Email is mandatory")
            return false
        }

        if(gender.isNullOrEmpty() || gender.contentEquals("Gender")){
            showToast("Please select your gender")
            return false
        }
//        if(password.isNullOrEmpty()){
//            showToast("Please enter your password.")
//            return false
//        }
//        if(password.length < 8){
//            showToast("Password should atleast of 8 characters.")
//            return false
//        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onResume() {
        super.onResume()
        binding.mobileNo.text = PreferenceManager.getStringValue(Constants.USER_NUMBER)
    }
}