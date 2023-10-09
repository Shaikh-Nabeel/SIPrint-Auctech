package com.auctech.siprint.home.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.auctech.siprint.CommonWebActivity
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.FragmentSettingBinding
import com.auctech.siprint.home.activity.ContactUsActivity
import com.auctech.siprint.initials.activity.LoginActivity
import com.auctech.siprint.initials.response.ResponseSignup
import com.auctech.siprint.profile.activity.ProfileUpdateActivity
import com.auctech.siprint.services.ApiClient
import com.auctech.siprint.services.RetrofitClient
import com.auctech.siprint.wallet.activity.WalletActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        if (PreferenceManager.getBoolValue(Constants.IS_HOST_DRIVER)) {
            binding.activateAccount.visibility = View.GONE
            binding.wallet.visibility = View.VISIBLE
        } else {
            binding.activateAccount.visibility = View.VISIBLE
            binding.wallet.visibility = View.GONE
        }

        setupUi()

        val customTabsIntent = CustomTabsIntent.Builder().build()

        binding.contactLl.setOnClickListener {
//            customTabsIntent.launchUrl(
//                requireActivity(),
//                Uri.parse(Constants.CONTACT_URL)
//            )
            val intent = Intent(requireContext(), ContactUsActivity::class.java)
            startActivity(intent)
        }
        binding.privacyPolicyLl.setOnClickListener {
//            customTabsIntent.launchUrl(
//                requireActivity(),
//                Uri.parse(Constants.PRIVACY_URL)
//            )

            val intent = Intent(requireContext(), CommonWebActivity::class.java)
            intent.putExtra("url", Constants.PRIVACY_URL)
            startActivity(intent)
        }
        binding.termsAndConditionLl.setOnClickListener {
//            customTabsIntent.launchUrl(
//                requireActivity(),
//                Uri.parse(Constants.TERMS_URL)
//            )
            val intent = Intent(requireContext(), CommonWebActivity::class.java)
            intent.putExtra("url", Constants.TERMS_URL)
            startActivity(intent)
        }
        binding.aboutLl.setOnClickListener {
//            customTabsIntent.launchUrl(
//                requireActivity(),
//                Uri.parse(Constants.ABOUT_URL)
//            )

            val intent = Intent(requireContext(), CommonWebActivity::class.java)
            intent.putExtra("url", Constants.ABOUT_URL)
            startActivity(intent)
        }

        binding.wallet.setOnClickListener {
            startActivity(Intent(requireActivity(), WalletActivity::class.java))
        }

        binding.helpLl.setOnClickListener {
            val alertDialogBuilder =
                AlertDialog.Builder(activity)
            alertDialogBuilder.setTitle("Log out")
            alertDialogBuilder.setMessage("Are you sure?")
            alertDialogBuilder.setPositiveButton(
                "Yes"
            ) { dialog: DialogInterface?, which: Int ->
                PreferenceManager.deletePref()
                activity?.startActivity(Intent(requireActivity(), LoginActivity::class.java))
                activity?.finish()
                FirebaseAuth.getInstance().signOut()
                dialog?.dismiss()
            }
            alertDialogBuilder.setNegativeButton(
                "No"
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        binding.activateAccount.setOnClickListener {
            val alertDialogBuilder =
                AlertDialog.Builder(activity)
            alertDialogBuilder.setTitle("Host Driver Account")
            alertDialogBuilder.setMessage("Are you sure you want to activate Host Driver Account?")
            alertDialogBuilder.setPositiveButton(
                "Yes"
            ) { dialog: DialogInterface?, _: Int ->
                updateToHost()
                dialog?.dismiss()
            }
            alertDialogBuilder.setNegativeButton(
                "No"
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        return binding.root
    }

    private fun updateToHost() {
        try {
            showProgressBar()
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val jsonObject = JsonObject()
            jsonObject.addProperty("user_id", PreferenceManager.getStringValue(Constants.USER_ID))
            jsonObject.addProperty("userType", "HOST")
            apiService.makeHost(jsonObject).enqueue(object : Callback<ResponseSignup> {
                override fun onResponse(
                    call: Call<ResponseSignup>,
                    response: Response<ResponseSignup>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        val responseSearch = response.body()
                        if (responseSearch?.status == 200) {
                            binding.wallet.visibility = View.VISIBLE
                            binding.activateAccount.visibility = View.GONE
                            PreferenceManager.setBoolValue(Constants.IS_HOST_DRIVER, true)
                            Toast.makeText(
                                requireContext(),
                                responseSearch.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
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

    private fun setupUi() {
        binding.username.text = PreferenceManager.getStringValue(Constants.USER_NAME)
        binding.userNumber.text = PreferenceManager.getStringValue(Constants.USER_NUMBER)
        val photoUrl = PreferenceManager.getStringValue(Constants.USER_PHOTO)
//        if(photoUrl.isNullOrEmpty()){
        if (PreferenceManager.getStringValue(Constants.USER_GENDER).equals("Male", true)) {
            Glide.with(requireActivity().applicationContext)
                .load(photoUrl)
                .placeholder(R.mipmap.male)
                .circleCrop()
                .into(binding.profileIV)
        } else {
            Glide.with(requireActivity().applicationContext)
                .load(photoUrl)
                .placeholder(R.mipmap.female)
                .circleCrop()
                .into(binding.profileIV)
        }
//        }else{
//            Glide.with(requireActivity().applicationContext)
//                .load(photoUrl)
//                .circleCrop()
//                .placeholder(R.mipmap.profile)
//                .into(binding.profileIV)
//        }

        val uploadBySelf = PreferenceManager.getIntValue(Constants.SELF_UPLOAD)
        val uploadByParty = PreferenceManager.getIntValue(Constants.PARTY_UPLOAD)

        binding.totalFiles.text = "Total Files - ${uploadByParty + uploadBySelf}"
        binding.uploadBySelf.text = "$uploadBySelf Files"
        binding.uploadByParty.text = "$uploadByParty Files"

        binding.icon.setOnClickListener {
            startActivity(Intent(requireActivity(), ProfileUpdateActivity::class.java))
        }

    }

    private fun showProgressBar() {
        try {
            binding.loading.setVisibility(View.VISIBLE)
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
            binding.loading.setVisibility(View.GONE)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        setupUi()
    }

}