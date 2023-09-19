package com.example.birdview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SplashPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_page)

        val btn_sign_in : Button = findViewById(R.id.btn_sign_in)
        val btn_sign_up : Button = findViewById(R.id.btn_sign_up)

        btn_sign_in.setOnClickListener(){
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_sign_up.setOnClickListener(){
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}