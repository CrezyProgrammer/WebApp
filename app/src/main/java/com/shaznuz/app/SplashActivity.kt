package com.shaznuz.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progress.showNow()

        Handler().postDelayed({
                                      startActivity(Intent(this, MainActivity2::class.java))
            finish()
                                      
        }, 2000)
    }
}