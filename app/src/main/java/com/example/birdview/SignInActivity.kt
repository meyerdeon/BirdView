package com.example.birdview

import android.content.Intent
import android.icu.util.ValueIterator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import com.example.birdview.databinding.ActivitySignInBinding
import com.example.birdview.databinding.ActivitySignUpBinding
import com.example.birdview.validation.Validation
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var mAuth : FirebaseAuth
    private var isAllFieldsValid : Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                Validation.validateEmail(binding.etEmail)
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                Validation.validatePasswordSignUp(binding.etPassword)
            }
        })

        binding.btnSignIn.setOnClickListener(){
            isAllFieldsValid = Validation.validateEmail(binding.etEmail)
            isAllFieldsValid = Validation.validatePasswordSignIn(binding.etPassword)

            if(isAllFieldsValid)
            {
                authenticateUser()
            }
            else {
                Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSignUp.setOnClickListener(){
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun authenticateUser(){
        val email : String = binding.etEmail.text.toString()
        val password : String = binding.etPassword.text.toString()
        var exceptionMessage : String? = null
        mAuth = FirebaseAuth.getInstance()
        try{
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(){
                if(it.isSuccessful){
                    // Toast.makeText(applicationContext, "Here", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "User sign in successful.", Toast.LENGTH_SHORT).show()
                    GlobalVariables.userUID = mAuth.currentUser?.uid.toString()
                    val mainActivity = Intent(this, MainActivity::class.java)
                    startActivity(mainActivity)
                    finish()
                }
                else{
                    exceptionMessage = it.exception?.message.toString()
                }
            }.addOnFailureListener(){
                Toast.makeText(this, "User sign in failed. " + exceptionMessage, Toast.LENGTH_SHORT).show()
            }
        }
        catch (ex : Exception){
            Toast.makeText(this, ex.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}