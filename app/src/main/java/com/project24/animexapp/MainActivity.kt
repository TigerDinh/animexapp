package com.project24.animexapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = Firebase.firestore
        val navView: BottomNavigationView = binding.navView
        val navAccount = binding.navAccount
        val currentUserID = firebaseAuth.currentUser?.uid.toString()
        val userName = ""
        Toast.makeText(this, userName, Toast.LENGTH_SHORT).show()

        if(firebaseAuth.currentUser !== null) {
            navAccount.text = "Welcome, " + firebaseAuth.currentUser?.email.toString()
        } else {
            navAccount.text = ""
        }
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

    }

}
