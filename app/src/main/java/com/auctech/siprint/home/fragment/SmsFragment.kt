package com.auctech.siprint.home.fragment

import android.app.Dialog
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.CaseMap.Title
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.FragmentSmsBinding
import com.auctech.siprint.home.activity.EditPdfActivity
import com.auctech.siprint.home.adapters.SmsAdapter
import com.auctech.siprint.home.interfaces.Mvp

import com.auctech.siprint.home.response.ResponseSms
import com.auctech.siprint.home.response.SmsItem
import com.auctech.siprint.initials.activity.LoginActivity
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

class SmsFragment : Fragment(), Mvp {


    private lateinit var binding: FragmentSmsBinding
    private var offset = 0
    private var userId: String? = null
    private var GSmsList = ArrayList<SmsItem>()
    private lateinit var adapter: SmsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSmsBinding.inflate(inflater,container,false)
        
        userId = PreferenceManager.getStringValue(Constants.USER_ID).toString()
        adapter = SmsAdapter(GSmsList,this)
        binding.smsRV.layoutManager = LinearLayoutManager(context)
        binding.smsRV.adapter = adapter
        binding.smsRV.addOnScrollListener(scrollObs)

        fetchSms()

        return binding.root
    }

    //scrolling observer for recycler view
    private val scrollObs = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            // If user has scrolled to the end of the list
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                offset++
                fetchSms()
            }
        }
    }

    private var isDataAvailable = false

    private fun fetchSms() {
        try {
            binding.loading.visibility = View.VISIBLE
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)

            apiService.getReceivedSms(userId!!, offset)
                .enqueue(object : Callback<ResponseSms> {
                    override fun onResponse(
                        call: Call<ResponseSms>,
                        response: Response<ResponseSms>
                    ) {
                        if (response.code() == 200 && response.body() != null) {
                            val smsResponse = response.body()
                            if (smsResponse?.status == 200) {
                                val smsList = smsResponse.data
                                if (smsList != null) {
                                    if(smsList.isNotEmpty() && !isDataAvailable) {
                                        isDataAvailable = true
                                    }
                                    if(!isDataAvailable){
                                        binding.noNotification.visibility = View.VISIBLE
                                        binding.smsRV.visibility = View.GONE
                                    }
                                }
                                setUpSmsRV(smsList)
                            } else {
                                Toast.makeText(
                                    context,
                                    smsResponse?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        binding.loading.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<ResponseSms>, t: Throwable) {
                        binding.loading.visibility = View.GONE
                        t.printStackTrace()
                    }

                })

        } catch (e: Exception) {
            binding.loading.visibility = View.GONE
            e.printStackTrace()
        }
    }

    private fun setUpSmsRV(smsList: List<SmsItem>?) {
        smsList?.let {
            if (it.size < 20) {
                binding.smsRV.removeOnScrollListener(scrollObs)
            }
            GSmsList.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onClick(pos: Int) {
        openDetailDialog(pos)
    }

    private fun openDetailDialog(pos: Int) {

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        val view: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_sms_details, null, false)
        dialog.setContentView(view)
        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog.window!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title: TextView = dialog.findViewById(R.id.titleTextView)
        val shareButton: ImageView = dialog.findViewById(R.id.shareButton)
        val descriptionTv: TextView = dialog.findViewById(R.id.description)
        val dateTime: TextView = dialog.findViewById(R.id.dateTime)

        val smsItem = GSmsList[pos]
        title.text = smsItem.label
        descriptionTv.text = smsItem.description
        dateTime.text = formatDate(smsItem.date!!)

        shareButton.setOnClickListener {
            shareSms(smsItem.label!!,smsItem.description!!)
        }

        dialog.show()
    }

    private fun shareSms(title: String, description: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "$title \n$description");
        startActivity(Intent.createChooser(shareIntent, "Share using:"))
    }

    private fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    }

}