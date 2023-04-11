package com.example.ims

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ims.databinding.ActivityMainBinding

// This is a comment
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.controlButton.setOnClickListener{
            val intent = Intent(this, ControlActivity::class.java)
            startActivity(intent)
        }

    }
}