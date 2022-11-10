package com.project24.animexapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.project24.animexapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logInBtn.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        binding.signUpBtn.setOnClickListener {
            val userEmail = binding.newEmailInput.text.toString()
            val userPass = binding.newPasswordInput.text.toString()
            val confirmUserPass = binding.newRetypePasswordInput.text.toString()

            if(userEmail.isNotEmpty() && userPass.isNotEmpty() && confirmUserPass.isNotEmpty()) {
                if (userPass == confirmUserPass) {
                    firebaseAuth.createUserWithEmailAndPassword(userEmail, userPass).addOnCompleteListener {
                        if(it.isSuccessful) {
                            val intent = Intent(this, LogInActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Account successfully created", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_LONG).show()
            }
        }
    }
}