package com.auctech.siprint.home.activity

import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.auctech.siprint.R
import com.auctech.siprint.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment?
        val navController = navHostFragment?.navController

        val menu = PopupMenu(this, null)
        menu.inflate(R.menu.bottom_navigation)
        if (navController != null) {
            binding.navigation.setupWithNavController(menu.menu, navController)
        }

    }
}