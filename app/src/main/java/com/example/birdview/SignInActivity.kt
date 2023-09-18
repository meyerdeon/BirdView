package com.example.birdview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        //for testing purposes
        val signIn = findViewById<Button>(R.id.btn_sign_in)
        signIn.setOnClickListener(){
            val mainActivity = Intent(this, MainActivity::class.java)
            startActivity(mainActivity)
            finish()
        }
    }
}