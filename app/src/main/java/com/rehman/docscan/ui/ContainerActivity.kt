package com.rehman.docscan.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.rehman.docscan.R
import com.rehman.docscan.databinding.ActivityContainerBinding
import com.rehman.utilities.Utils

class ContainerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContainerBinding
    private var doubleBackToExitPressedOnce = false
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> binding.titleAppBar.text = getString(R.string.docscan)
                R.id.exploreFragment -> binding.titleAppBar.text = getString(R.string.explore)
                R.id.settingFragment -> binding.titleAppBar.text = getString(R.string.setting)
            }
        }


        // Handle the back press logic
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.homeFragment) {
                    if (doubleBackToExitPressedOnce) {
                        finish() // or you can call super.onBackPressed() if you want to use the default back behavior
                    } else {
                        doubleBackToExitPressedOnce = true
                        Toast.makeText(this@ContainerActivity, "Press BACK again to exit", Toast.LENGTH_SHORT).show()

                        handler.postDelayed({
                            doubleBackToExitPressedOnce = false
                        }, 2000) // 2 seconds delay
                    }
                } else {
                    navController.navigateUp() // navigate to the previous fragment in the stack
                }
            }
        })


    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

}