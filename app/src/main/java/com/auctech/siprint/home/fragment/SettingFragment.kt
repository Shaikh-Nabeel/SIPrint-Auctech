package com.auctech.siprint.home.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.auctech.siprint.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater,container,false)

        binding.helpLl.setOnClickListener { v ->
            val alertDialogBuilder =
                AlertDialog.Builder(activity)
            alertDialogBuilder.setTitle("Log out")
            alertDialogBuilder.setMessage("Are you sure?")
            alertDialogBuilder.setPositiveButton(
                "Yes"
            ) { dialog: DialogInterface?, which: Int ->
               //signout code
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