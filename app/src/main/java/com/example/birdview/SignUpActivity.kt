package com.example.birdview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.birdview.databinding.ActivitySignUpBinding
import com.example.birdview.models.User
import com.example.birdview.validation.Validation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private var isAllFieldsValid : Boolean = true
    private lateinit var database : FirebaseDatabase
    private lateinit var databaseReference : DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var dateFormatted : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {

            binding.etEmail.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {
                    Validation.validateEmail(binding.etEmail)
                }
            })

            binding.etName.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {
                    Validation.validateInput(binding.etName, "Please enter your name.")
                }
            })

            binding.etPassword.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {
                   Validation.validatePasswordSignUp(binding.etPassword)
                }
            })

            binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {
                    Validation.validateConfirmPassword(binding.etPassword, binding.etConfirmPassword)
                }
            })

            binding.btnSignUp.setOnClickListener() {
                val isEmailValid = Validation.validateEmail(binding.etEmail)
                val isNameValid =  Validation.validateInput(binding.etName, "Please enter your name.")
                val isPasswordValid = Validation.validatePasswordSignUp(binding.etPassword)
                val isConfirmPasswordValid = Validation.validateConfirmPassword(binding.etPassword, binding.etConfirmPassword)
                if(isEmailValid && isNameValid && isPasswordValid && isConfirmPasswordValid)
                {
                    val localDate = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                    dateFormatted = localDate.format(formatter)
                    //Toast.makeText(applicationContext, formattedString, Toast.LENGTH_SHORT).show()
                    createUser()
                }
                else {
                    Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show()
                }
            }

            binding.tvSignIn.setOnClickListener(){
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        catch (ex : Exception){
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createUser(){
        val name : String = binding.etName.text.toString()
        val email : String = binding.etEmail.text.toString()
        val password : String = binding.etPassword.text.toString()
        var exceptionMessage : String? = null
        mAuth = FirebaseAuth.getInstance()
        try{

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(){
                if(it.isSuccessful){
                    addUserToDatabase(mAuth.uid)
                    Toast.makeText(this, "User signed up successfully.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    exceptionMessage = it.exception?.message.toString()
                }
            }.addOnFailureListener(){
                Toast.makeText(this, "User sign up error. " + exceptionMessage, Toast.LENGTH_SHORT).show()
            }
        }
        catch (ex : Exception){
            Toast.makeText(this, ex.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUserToDatabase(user_uid : String?){
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Users")
        val user = User(binding.etEmail.text.toString(), binding.etName.text.toString(), dateFormatted)
        databaseReference.child(user_uid.toString()).setValue(user).addOnCompleteListener() {
            //code attribution
            //the following code was taken from Stack Overflow and adapted
            //https://stackoverflow.com/questions/7426443/how-to-clear-the-text-in-edittext#:~:text=Use%20editText.,clear()%20.&text=This%20is%20the%20correct%20way%20to%20do%20this.
            //Gibolt
            //https://stackoverflow.com/users/974045/gibolt
            if (it.isComplete){
                binding.etEmail.text?.clear()
                binding.etPassword.text?.clear()
                binding.etName.text?.clear()
                binding.etName.error = null
                binding.etEmail.error = null
                binding.etPassword.error = null
            }
        }.addOnFailureListener(){
            Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
        }
    }
}