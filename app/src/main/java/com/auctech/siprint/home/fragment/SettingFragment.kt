package com.auctech.siprint.home.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.FragmentSettingBinding
import com.auctech.siprint.initials.activity.LoginActivity
import com.bumptech.glide.Glide

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater,container,false)

        binding.username.text = PreferenceManager.getStringValue(Constants.USER_NAME)
        binding.userNumber.text = PreferenceManager.getStringValue(Constants.USER_NUMBER)
        Glide.with(requireActivity().applicationContext)
            .load(PreferenceManager.getStringValue(Constants.USER_PHOTO))
            .circleCrop()
            .placeholder(R.mipmap.profile)
            .into(binding.profileIV)
        binding.noOfCredits.text = PreferenceManager.getIntValue(Constants.USER_LIMIT).toString().plus("\uD83D\uDD25")

        val customTabsIntent = CustomTabsIntent.Builder().build()

        binding.contactLl.setOnClickListener {
            customTabsIntent.launchUrl(
                requireActivity(),
                Uri.parse(Constants.CONTACT_URL)
            )
        }
        binding.privacyPolicyLl.setOnClickListener {
            customTabsIntent.launchUrl(
                requireActivity(),
                Uri.parse(Constants.PRIVACY_URL)
            )
        }
        binding.termsAndConditionLl.setOnClickListener {
            customTabsIntent.launchUrl(
                requireActivity(),
                Uri.parse(Constants.TERMS_URL)
            )
        }
        binding.aboutLl.setOnClickListener {
            customTabsIntent.launchUrl(
                requireActivity(),
                Uri.parse(Constants.ABOUT_URL)
            )
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
                dialog?.dismiss()
            }
            alertDialogBuilder.setNegativeButton(
                "No"
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        return binding.root
    }

}