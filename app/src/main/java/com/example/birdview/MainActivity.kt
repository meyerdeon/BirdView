package com.example.birdview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn_sign_in : Button = findViewById(R.id.btn_sign_in)
        val btn_sign_up : Button = findViewById(R.id.btn_sign_up)

        btn_sign_in.setOnClickListener(){
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        btn_sign_up.setOnClickListener(){
            val intent = Intent(this, ObservationsActivity::class.java)
            startActivity(intent)
        }
    }
}