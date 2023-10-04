package com.auctech.siprint.home.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivitySelectUserBinding
import com.auctech.siprint.home.adapters.UserAdapter
import com.auctech.siprint.home.response.ResponseSearchUser
import com.auctech.siprint.home.response.UserItem
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectUserActivity : AppCompatActivity() {

    lateinit var binding: ActivitySelectUserBinding
    private var docID: Int? = null
    private var listOfUser = ArrayList<UserItem>()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.extras != null){
            docID = intent.getIntExtra(Constants.DOC_ID, -1)
            binding.docName.text = intent.getStringExtra(Constants.DOC_NAME)
        }
        binding.userRV.layoutManager = LinearLayoutManager(this)
        binding.searchIV.setOnClickListener {
            val search = binding.searchET.text.toString().trim()
            if(search.isNotEmpty()){
                callUserSearchApi(search)
            }
        }
    }

    private fun callUserSearchApi(search: String) {
        try {
            binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("search" , search)
            apiService.searchUser(jsonObject).enqueue(object : Callback<ResponseSearchUser> {
                override fun onResponse(
                    call: Call<ResponseSearchUser>,
                    response: Response<ResponseSearchUser>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        val responseSearch = response.body()
                        if(responseSearch?.status == 200){
                            listOfUser.clear()
                            listOfUser.addAll(responseSearch.data)
                            adapter = UserAdapter(this@SelectUserActivity, listOfUser, docID!!)
                            binding.userRV.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }else{
                            Toast.makeText(
                                this@SelectUserActivity,
                                responseSearch?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    binding.loading.visibility = View.GONE
                }

                override fun onFailure(call: Call<ResponseSearchUser>, t: Throwable) {
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