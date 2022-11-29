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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //In an Async fashion, get all the info needed
        //Trending Anime: Kitsu
        //Ongoing Anime
        //Recommended For You Details
        //Explore Anime
        /*
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                Log.d("BEFORE","HERE")
                getOngoingAnime()
                Log.d("AFTER","HERE")
                delay(1000)
                Log.d("AFTER DELAY","HERE")
            }
        }

         */
    }
    /*
    private fun getOngoingAnime(){
        val client = JikanApiClient.apiService.requestAnime(status = "airing")

        client.enqueue(object: Callback<AnimeSearchResponse> {
            override fun onResponse(
                call: Call<AnimeSearchResponse>,
                response: Response<AnimeSearchResponse>
            ){
                if(response.isSuccessful){
                    if(response.body() != null){
                        ongoingList = response.body()!!.result

                        //PASS THE LIST TO THE ADAPTER AND REFRESH IT

                        //ongoingAnimeAdapter.animeList = ongoingList
                        //ongoingAnimeAdapter.notifyDataSetChanged()
                        //viewModel.ongoingList.value = ongoingList

                        //Log.d("ONGOING ANIME",""+ viewModel.ongoingList.value)
                    }
                }
            }
            override fun onFailure(call: Call<AnimeSearchResponse>, t: Throwable) {
                Log.e("ONGOING ANIME API FAIL",""+t.message)
            }
        })
    }
    */
}
