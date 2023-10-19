package com.example.birdview.validation

import android.R.attr.password
import android.widget.EditText
import java.util.regex.Matcher
import java.util.regex.Pattern


class Validation {
    companion object {
        public fun validateEmail(etEmail: EditText): Boolean {
            if (etEmail.text.isNullOrEmpty()) {
                etEmail.error = "Please enter your email address."
                return false
            } else {
                //code attribution
                //the following code was taken from Stack Overflow and adapted
                //https://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address
                //mindriot
                //https://stackoverflow.com/users/1011746/mindriot
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text).matches()) {
                    etEmail.error = "Please enter a valid email address."
                    return false
                } else {
                    return true
                }
            }
        }

        public fun validateInput(etInput: EditText, message : String): Boolean{
            if (etInput.text.isNullOrEmpty()) {
                etInput.error = message
                return false
            } else {
                etInput.error = null
                return true
            }
        }

        public fun validatePasswordSignUp(etPassword: EditText): Boolean{
            if (etPassword.text.isNullOrEmpty()) {
                etPassword.error = "Please enter your password."
                return false
            } else {
                if(etPassword.text.length < 8){
                    etPassword.error = "Please enter a password with at least 8 characters."
                    return false
                }
                else{
                    val pattern: Pattern
                    val matcher: Matcher

                    //code attribution
                    //the following code was taken from Stack Overflow and adapted
                    //https://stackoverflow.com/questions/23214434/regular-expression-in-android-for-password-field
                    //Ana Laura Anguiano Cruz
                    //https://stackoverflow.com/users/13617864/ana-laura-anguiano-cruz
                    val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[#,~`'!@$%^&*()_+={}|;<>.?:])(?=\\S+$).{4,}$"

                    pattern = Pattern.compile(passwordPattern)
                    matcher = pattern.matcher(etPassword.text.toString())
                    if(matcher.matches()){
                        etPassword.error = null
                        return true
                    }
                    else{
                        etPassword.error = "Your password needs to contain at least one special character and number."
                        return false
                    }
                }
            }
        }

        public fun validatePasswordSignIn(etPassword: EditText): Boolean{
            if (etPassword.text.isNullOrEmpty()) {
                etPassword.error = "Please enter your password."
                return false
            }
            else{
                return true
            }
        }

        public fun validateConfirmPassword(etPassword: EditText, etConfirmPassword: EditText): Boolean{
            if(etPassword.text.length > 0){
                if(!etPassword.text.toString().equals(etConfirmPassword.text.toString())){
                    etConfirmPassword.error = "Passwords do not match."
                    return false
                }
                else{
                    return true
                }
            }
            else{
                return false
            }
        }
    }
}