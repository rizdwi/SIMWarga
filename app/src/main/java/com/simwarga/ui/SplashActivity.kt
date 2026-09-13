package com.simwarga.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simwarga.util.PinHelper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isSet = PinHelper.isPinSet(this)
        val intent = Intent(this, PinActivity::class.java).apply {
            putExtra(
                PinActivity.EXTRA_MODE,
                if (isSet) PinActivity.MODE_VERIFY else PinActivity.MODE_CREATE
            )
        }
        startActivity(intent)
        finish()
    }
}
