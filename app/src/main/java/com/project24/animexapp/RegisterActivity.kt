package com.project24.animexapp

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val db = Firebase.firestore

        binding.logInBtn.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        binding.signUpBtn.setOnClickListener {
            val userEmail = binding.newEmailInput.text.toString()
            val userName = binding.newUsernameInput.text.toString()
            val userPass = binding.newPasswordInput.text.toString()
            val confirmUserPass = binding.newRetypePasswordInput.text.toString()



            if(userEmail.isNotEmpty() && userPass.isNotEmpty() && confirmUserPass.isNotEmpty()) {

                if (userPass == confirmUserPass) {
                    firebaseAuth.createUserWithEmailAndPassword(userEmail, userPass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val userID = firebaseAuth.currentUser?.uid.toString()

                            // create hashmap with user email and ID
                            val user = hashMapOf(
                                "uid" to userID,
                                "email" to userEmail,
                                "username" to userName
                            )

                            // add user to user database in Firebase
                            db.collection("Users").document(userID)
                                .set(user)
                                .addOnSuccessListener { documentReference ->
                                    Toast.makeText(this, "Added to $documentReference database", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error $e", Toast.LENGTH_SHORT).show()
                                }

                            // switch to Login activity after registering
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