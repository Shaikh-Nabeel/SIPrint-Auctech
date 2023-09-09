package com.auctech.siprint.home.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.VISIBLE
import android.view.View.GONE
import android.widget.Toast
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.databinding.ActivityEditPdfBinding
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPdfBinding
    private lateinit var docID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEditPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        docID = intent.getStringExtra(Constants.DOC_ID)!!
        binding.labelET.setText(intent.getStringExtra(Constants.DOC_LABEL))
        binding.descriptionET.setText(intent.getStringExtra(Constants.DOC_DESCRIPTION))
        binding.pdfName.setText(intent.getStringExtra(Constants.DOC_NAME))

        binding.signup.setOnClickListener {
            val label = binding.labelET.text.toString().trim()
            val description = binding.descriptionET.text.toString().trim()

            if(label.isNotEmpty() && description.isNotEmpty()){
                callPdfUpdateApi(label, description)
            }

        }

    }

    private fun callPdfUpdateApi(label: String, description: String) {
        try {
            binding.loading.visibility = VISIBLE
            binding.signup.isEnabled = false
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val userId = PreferenceManager.getStringValue(Constants.USER_ID)!!
            apiService.updatePdf(docID,userId,label,description).enqueue(
                object : Callback<ResponseSignup>{
                    override fun onResponse(
                        call: Call<ResponseSignup>,
                        response: Response<ResponseSignup>
                    ) {
                        binding.loading.visibility = GONE
                        binding.signup.isEnabled = true
                        if(response.code() == 200 && response.body() != null){
                            val responseBody= response.body()
                            Toast.makeText(applicationContext, responseBody?.message, Toast.LENGTH_SHORT).show()
                            if(responseBody?.status == 200){
                                finish()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseSignup>, t: Throwable) {
                        Toast.makeText(applicationContext, Constants.ERR_MESSAGE, Toast.LENGTH_SHORT).show()
                        binding.loading.visibility = GONE
                        binding.signup.isEnabled = true
                        t.printStackTrace()
                    }
                }
            )
        }catch (e: Exception){
            binding.loading.visibility = GONE
            binding.signup.isEnabled = true
            e.printStackTrace()
        }
    }
}