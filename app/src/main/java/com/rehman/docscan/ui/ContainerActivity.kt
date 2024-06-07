package com.rehman.docscan.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    private lateinit var storagePermissions: Array<String>
    private val STORAGE_REQUEST_CODE = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        if (checkStoragePermission()){
//            requestStoragePermission()
//        }


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> binding.titleAppBar.text = "DocScan"
                R.id.exploreFragment -> binding.titleAppBar.text = "Explore"
                R.id.settingFragment -> binding.titleAppBar.text = "Setting"
            }
        }


    }

    fun checkStoragePermission(): Boolean {
        val minSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        var storageResult = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        storageResult = storageResult || minSDK
        return storageResult
    }

    fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this, storagePermissions, STORAGE_REQUEST_CODE
        )
    }

}