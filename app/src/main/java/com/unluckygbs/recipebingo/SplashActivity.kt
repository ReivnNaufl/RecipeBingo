package com.unluckygbs.recipebingo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            gotToMainActivity()
        }, 1000L)
    }

    private fun gotToMainActivity() {
        Intent(this, MainActivity::class.java). also {
            startActivity(it)
            finish()
        }
    }
}