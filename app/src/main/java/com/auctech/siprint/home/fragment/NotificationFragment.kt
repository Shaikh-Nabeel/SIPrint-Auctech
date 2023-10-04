package com.auctech.siprint.home.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.FragmentNotificationBinding
import com.auctech.siprint.home.adapters.NotificationAdapter
import com.auctech.siprint.home.response.Data
import com.auctech.siprint.home.response.ItemNotifications
import com.auctech.siprint.home.response.ResponseDashboard
import com.auctech.siprint.home.response.ResponseNotification
import com.auctech.siprint.initials.activity.LoginActivity
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private val listOfNotification = ArrayList<ItemNotifications>()
    private val adapter = NotificationAdapter(listOfNotification)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationBinding.inflate(inflater,container,false)

        binding.notificationRV.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRV.adapter = adapter

        getNotifications()
        return binding.root
    }

    private fun getNotifications() {
        try {
            binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)

            apiService.getNotifications(PreferenceManager.getStringValue(Constants.USER_ID).toString())
                .enqueue(object : Callback<ResponseNotification> {
                    override fun onResponse(
                        call: Call<ResponseNotification>,
                        response: Response<ResponseNotification>
                    ) {
                        if (response.code() == 200 && response.body() != null) {
                            val notification = response.body()
                            if (notification?.status == 200) {
                                if(notification.data?.isEmpty() == true){
                                    binding.noNotification.visibility = View.VISIBLE
                                    binding.notificationRV.visibility = View.GONE
                                }else{
                                    binding.noNotification.visibility = View.GONE
                                    binding.notificationRV.visibility = View.VISIBLE
                                }
                                listOfNotification.addAll(notification.data!!)
                                adapter.notifyDataSetChanged()
                            } else {
                                Toast.makeText(
                                    context,
                                    notification?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        binding.loading.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<ResponseNotification>, t: Throwable) {
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