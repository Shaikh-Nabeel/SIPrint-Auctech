package com.auctech.siprint.wallet.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityWalletBinding
import com.auctech.siprint.wallet.fragment.TransactionFragment
import com.auctech.siprint.wallet.fragment.UsageFragment
import com.auctech.siprint.wallet.fragment.WalletFragment

class WalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.wallet.setOnClickListener {
            updateButtonState(binding.wallet,"WALLET")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_wallet, WalletFragment())
                .commit()
        }

        binding.transactions.setOnClickListener {
            updateButtonState(binding.transactions,"TRANSACTIONS")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_wallet, TransactionFragment())
                .commit()
        }

        binding.usage.setOnClickListener {
            updateButtonState(binding.usage,"USAGE")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_wallet, UsageFragment())
                .commit()
        }


    }

    fun updateButtonState(selectedButton: LinearLayout,heading: String) {
        // Set all buttons to borderless background
        binding.wallet.setBackgroundResource(R.drawable.wallet_card_borderless)
        binding.transactions.setBackgroundResource(R.drawable.wallet_card_borderless)
        binding.usage.setBackgroundResource(R.drawable.wallet_card_borderless)


        // Set the selected button to bordered background
        selectedButton.setBackgroundResource(R.drawable.wallet_card_bordered)

        // Update the heading TextView with the text of the selected button
        binding.heading.text = heading
    }
}