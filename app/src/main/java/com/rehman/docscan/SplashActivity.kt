package com.rehman.docscan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rehman.docscan.ui.containerActivity.ContainerActivity
import com.rehman.utilities.Utils

private const val SPLASH_TIME: Long = 2000
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        this.installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Utils.hideSystemUiVisibility(this)


        Handler(Looper.getMainLooper()).postDelayed({
            startActivity( Intent(this, ContainerActivity::class.java))
            finish()
        }, SPLASH_TIME)
    }
}