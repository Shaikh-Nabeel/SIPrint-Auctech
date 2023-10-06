package com.auctech.siprint.wallet.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityWalletBinding
import com.auctech.siprint.wallet.fragment.TransactionFragment
import com.auctech.siprint.wallet.fragment.WalletFragment

class WalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.wallet.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_wallet, WalletFragment())
                .commit()
        }

        binding.transactions.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_wallet, TransactionFragment())
                .commit()
        }


    }
}