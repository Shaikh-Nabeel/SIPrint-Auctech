package com.auctech.siprint.wallet.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.FragmentWalletBinding
import com.auctech.siprint.wallet.activity.AddCreditActivity

class WalletFragment : Fragment() {

    private lateinit var binding: FragmentWalletBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWalletBinding.inflate(inflater,container, false)

        binding.limit.text = PreferenceManager.getIntValue(Constants.USER_LIMIT).toString()
        binding.addCredit.setOnClickListener{
            startActivity(Intent(activity, AddCreditActivity::class.java))
        }
        return binding.root
    }
}