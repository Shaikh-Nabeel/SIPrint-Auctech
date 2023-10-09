package com.auctech.siprint.wallet.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.FragmentUsageBinding
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.auctech.siprint.wallet.adapter.TransactionAdapter
import com.auctech.siprint.wallet.adapter.UsageAdapter
import com.auctech.siprint.wallet.response.DataItem
import com.auctech.siprint.wallet.response.DataItemUsage
import com.auctech.siprint.wallet.response.ResponseUsage
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsageFragment : Fragment() {

    private lateinit var binding: FragmentUsageBinding
    private val listOfTransaction = ArrayList<DataItemUsage>()
    private lateinit var adapter: UsageAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsageBinding.inflate(inflater,container, false)

        binding.transactionRV.layoutManager = LinearLayoutManager(requireContext())
        adapter = UsageAdapter(requireContext(), listOfTransaction)
        binding.transactionRV.adapter = adapter
        getTransactions()

        return binding.root
    }

    private fun getTransactions() {
        try {
            showProgressBar()
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("user_id" , PreferenceManager.getStringValue(Constants.USER_ID))
            jsonObject.addProperty("offset", 0)
            apiService.getLimitUsage(jsonObject).enqueue(object : Callback<ResponseUsage> {
                override fun onResponse(
                    call: Call<ResponseUsage>,
                    response: Response<ResponseUsage>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        val responseSearch = response.body()
                        if(responseSearch?.status == 200){
                            listOfTransaction.addAll(responseSearch.data!!)
                            adapter.notifyDataSetChanged()
                            if(listOfTransaction.isEmpty()){
                                Toast.makeText(
                                    requireContext(),
                                    "No Transactions",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }else{
                            Toast.makeText(
                                requireContext(),
                                responseSearch?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    hideProgressBar()
                }

                override fun onFailure(call: Call<ResponseUsage>, t: Throwable) {
                    hideProgressBar()
                    t.printStackTrace()
                }

            })
        } catch (e: Exception) {
            hideProgressBar()
            e.printStackTrace()
        }
    }


    private fun showProgressBar() {
        try {
            binding.loading.visibility = View.VISIBLE
            requireActivity().window.setFlags(
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
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}