package com.project24.animexapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project24.animexapp.api.*
import com.project24.animexapp.databinding.ActivityMainBinding
import com.project24.animexapp.ui.home.AnimeRVAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView



        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }
}

//firebaseAuth = FirebaseAuth.getInstance()
//
//val homeLogInBtn = findViewById<Button>(R.id.homeLogInBtn)
//val homeLogOutBtn = findViewById<Button>(R.id.homeLogOutBtn)
//
//homeLogInBtn.setOnClickListener {
//    val intent = Intent(this, LogInActivity::class.java)
//    startActivity(intent)
//}
//
//if(firebaseAuth.currentUser !== null) {
//    homeLogOutBtn.visibility = View.VISIBLE
//    homeLogInBtn.visibility = View.INVISIBLE
//} else {
//    homeLogOutBtn.visibility = View.INVISIBLE
//    homeLogInBtn.visibility = View.VISIBLE
//}
//
//homeLogOutBtn.setOnClickListener {
//    firebaseAuth.signOut()
//    Toast.makeText(this, "Logged out", Toast.LENGTH_LONG).show()
//}
//
//val user = firebaseAuth.currentUser?.email.toString()
//Toast.makeText(this, "Logged in as $user", Toast.LENGTH_LONG).show()