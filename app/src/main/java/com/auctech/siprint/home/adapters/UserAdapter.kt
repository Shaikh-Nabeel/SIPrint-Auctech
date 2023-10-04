package com.auctech.siprint.home.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.home.activity.SelectUserActivity
import com.auctech.siprint.home.response.ResponseSearchUser
import com.auctech.siprint.home.response.UserItem
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserAdapter(
    private val activity: Activity,
    private val users: List<UserItem>,
    private val docID: Int
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userProfile: ImageView = itemView.findViewById(R.id.userProfile)
        val username: TextView = itemView.findViewById(R.id.username)
        val userNumber: TextView = itemView.findViewById(R.id.userNumber)
        val shareBtn: TextView = itemView.findViewById(R.id.shareBtn)

        fun bind(user: UserItem) {
            Glide.with(itemView.context)
                .load(user.photo)
                .placeholder(R.mipmap.profile)
                .into(userProfile)
            username.text = user.name
            userNumber.text = user.phone

            shareBtn.setOnClickListener {
                showShareConfirmationDialog(user.name, user.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    private fun showShareConfirmationDialog(username: String, userId: Int) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("Are you sure?")
        alertDialogBuilder.setMessage("You want to share this invoice with $username?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            shareDocWithUser(userId)
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun shareDocWithUser(userId: Int) {
        try {
            (activity as SelectUserActivity).binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("shareTo" , userId)
            jsonObject.addProperty("docID" , docID)
            jsonObject.addProperty("shareBy" , PreferenceManager.getStringValue(Constants.USER_ID))

            apiService.shareDoc(jsonObject).enqueue(object : Callback<ResponseSignup> {
                override fun onResponse(
                    call: Call<ResponseSignup>,
                    response: Response<ResponseSignup>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        val responseSearch = response.body()
                        if(responseSearch?.status == 200){
                            Toast.makeText(
                                activity.applicationContext,
                                "Shared file successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            activity.finish()
                        }else{
                            Toast.makeText(
                                activity.applicationContext,
                                responseSearch?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    activity.binding.loading.visibility = View.GONE
                }

                override fun onFailure(call: Call<ResponseSignup>, t: Throwable) {
                    activity.binding.loading.visibility = View.GONE
                    t.printStackTrace()
                }

            })
        } catch (e: Exception) {
            (activity as SelectUserActivity).binding.loading.visibility = View.GONE
            e.printStackTrace()
        }
    }
}
