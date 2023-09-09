package com.auctech.siprint.home.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.auctech.siprint.Constants
import com.auctech.siprint.initials.activity.LoginActivity
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.databinding.FragmentSettingBinding
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
        Glide.with(requireContext())
            .load(PreferenceManager.getStringValue(Constants.USER_PHOTO))
            .circleCrop()
            .into(binding.profileIV)
        binding.noOfCredits.text = PreferenceManager.getIntValue(Constants.USER_LIMIT).toString().plus("\uD83D\uDD25")

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
                activity?.finish();
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