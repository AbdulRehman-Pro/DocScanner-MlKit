package com.rehman.docscan

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rehman.docscan.ui.ContainerActivity
import com.rehman.utilities.Utils

private const val SPLASH_TIME: Long = 2000
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {


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