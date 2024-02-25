package com.auctech.siprint.wallet.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityAddCreditBinding
import com.auctech.siprint.databinding.ActivityLoginBinding

class AddCreditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCreditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCreditBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Show soft keyboard after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            binding.editTextAmount.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editTextAmount, InputMethodManager.SHOW_IMPLICIT)
        }, 300)

        binding.textViewAmount1.setOnClickListener{
            binding.editTextAmount.setText(binding.textViewAmount1.text.toString())
        }

        binding.textViewAmount2.setOnClickListener{
            binding.editTextAmount.setText(binding.textViewAmount2.text.toString())
        }

        binding.textViewAmount3.setOnClickListener{
            binding.editTextAmount.setText(binding.textViewAmount3.text.toString())
        }

    }
}