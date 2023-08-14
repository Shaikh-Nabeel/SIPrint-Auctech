package com.auctech.siprint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.auctech.siprint.databinding.ActivityLoginBinding
import com.auctech.siprint.databinding.ActivitySignUpBinding
import com.auctech.siprint.home.activity.MainActivity

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}