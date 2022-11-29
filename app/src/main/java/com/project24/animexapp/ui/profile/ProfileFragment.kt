package com.project24.animexapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.AnimeDetails
import com.project24.animexapp.LogInActivity
import com.project24.animexapp.MainActivity
import com.project24.animexapp.R
import com.project24.animexapp.api.*
import com.project24.animexapp.databinding.FragmentProfileBinding
import dev.failsafe.RetryPolicy
import dev.failsafe.retrofit.FailsafeCall
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var favoritesList: List<LocalAnime>
    private lateinit var favoritesAnimeRV: RecyclerView
    private lateinit var favoritesAnimeAdapter: LocalAnimeRVAdapter

    private lateinit var watchLaterList: List<LocalAnime>
    private lateinit var watchLaterAnimeRV: RecyclerView
    private lateinit var watchLaterAnimeAdapter: LocalAnimeRVAdapter

    private lateinit var watchingList: List<LocalAnime>
    private lateinit var watchingAnimeRV: RecyclerView
    private lateinit var watchingAnimeAdapter: LocalAnimeRVAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val profileEmail = binding.profileEmail
        val currentUserEmail = firebaseAuth.currentUser?.email.toString()
        var currentUserID = firebaseAuth.currentUser?.uid.toString()
        val favEmptyText = binding.emptyFavText
        val watchingEmptyText = binding.emptyWatchingText
        val watchLaterEmptyText = binding.emptyWatchLaterText
        val profileContent = binding.profileContent
        val profileLogoutButton = binding.profileLogoutButton
        val noAccountContent = binding.noAccountContent
        val profileLoginButton = binding.profileLoginButton

        profileLogoutButton.setOnClickListener {
            firebaseAuth.signOut()
            currentUserID = null.toString()
            Toast.makeText(activity, "Logged out", Toast.LENGTH_SHORT).show()
            profileContent.visibility = View.GONE
            profileLogoutButton.visibility = View.GONE
            noAccountContent.visibility = View.VISIBLE
            profileEmail.text = "Guest"
            profileLoginButton.isClickable
            activity?.recreate()
        }

        profileEmail.text = if(currentUserEmail=="null") "Guest" else currentUserEmail

        favoritesList = emptyList()
        favoritesAnimeRV = binding.favoritesRecyclerView
        favoritesAnimeAdapter = LocalAnimeRVAdapter(favoritesList)

        watchLaterList = emptyList()
        watchLaterAnimeRV = binding.watchLaterRecyclerView
        watchLaterAnimeAdapter = LocalAnimeRVAdapter(watchLaterList)

        watchingList = emptyList()
        watchingAnimeRV = binding.watchingRecyclerView
        watchingAnimeAdapter = LocalAnimeRVAdapter(watchingList)


        favoritesAnimeRV.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL, false
        )
        favoritesAnimeRV.adapter = favoritesAnimeAdapter

        watchLaterAnimeRV.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL, false
        )
        watchLaterAnimeRV.adapter = watchLaterAnimeAdapter

        watchingAnimeRV.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL, false
        )
        watchingAnimeRV.adapter = watchingAnimeAdapter

        if(currentUserID!=="null") {

            val favDocRef = db.collection("Users").document(currentUserID).collection("Favourites")
            val watchLaterDocRef = db.collection("Users").document(currentUserID).collection("WatchLater")
            val watchingDocRef = db.collection("Users").document(currentUserID).collection("Watching")

            noAccountContent.visibility = View.GONE
            profileLogoutButton.visibility = View.VISIBLE

            favDocRef.get().addOnSuccessListener() {
                if(it.isEmpty) {
                    favEmptyText.text = "You have no items on your favourites yet."
                } else {
                    favEmptyText.visibility = View.GONE
                }
            }

            watchingDocRef.get().addOnSuccessListener() {
                if(it.isEmpty) {
                    watchingEmptyText.text = "You have no items on your watching yet."
                } else {
                    watchingEmptyText.visibility = View.GONE
                }
            }

            watchLaterDocRef.get().addOnSuccessListener() {
                if(it.isEmpty) {
                    watchLaterEmptyText.text = "You have no items on your watch later yet."
                } else {
                    watchLaterEmptyText.visibility = View.GONE
                }
            }

            db.collection("Users").document(currentUserID).collection("Favourites").get()
                .addOnSuccessListener { favourite ->
                    //Log.d("favorite",favourite.documents.)
                    //var idList = emptyList<Long>()
                    for (document in favourite) {
                        var malID: Long = document.data.getValue("mal_id") as Long
                        var imgURL: String = document.data.getValue("image_url") as String
                        var animeTitle: String = document.data.getValue("anime_title") as String
                        favoritesList = favoritesList + LocalAnime(malID, animeTitle, imgURL)
                        favoritesAnimeAdapter.animeList = favoritesList
                        favoritesAnimeAdapter.notifyDataSetChanged()
                        //idList = idList.plus(malID)
                        //Log.d("MAL_IDFAV", malID.toString())

                    }
                    //Log.d("MAL_IDFAV", idList.toString())
                }

            db.collection("Users").document(currentUserID).collection("WatchLater").get()
                .addOnSuccessListener { watchLater ->
                    //Log.d("favorite",favourite.documents.)
                    //var idList = emptyList<Long>()
                    for (document in watchLater) {
                        var malID: Long = document.data.getValue("mal_id") as Long
                        var imgURL: String = document.data.getValue("image_url") as String
                        var animeTitle: String = document.data.getValue("anime_title") as String
                        watchLaterList = watchLaterList + LocalAnime(malID, animeTitle, imgURL)
                        watchLaterAnimeAdapter.animeList = watchLaterList
                        watchLaterAnimeAdapter.notifyDataSetChanged()
                        //idList = idList.plus(malID)
                        //Log.d("MAL_IDFAV", malID.toString())

                    }
                    //Log.d("MAL_IDFAV", idList.toString())
                }

            db.collection("Users").document(currentUserID).collection("Watching").get()
                .addOnSuccessListener { watching ->
                    //Log.d("favorite",favourite.documents.)
                    //var idList = emptyList<Long>()
                    for (document in watching) {
                        var malID: Long = document.data.getValue("mal_id") as Long
                        var imgURL: String = document.data.getValue("image_url") as String
                        var animeTitle: String = document.data.getValue("anime_title") as String
                        watchingList = watchingList + LocalAnime(malID, animeTitle, imgURL)
                        watchingAnimeAdapter.animeList = watchingList
                        watchingAnimeAdapter.notifyDataSetChanged()
                        //idList = idList.plus(malID)
                        //Log.d("MAL_IDFAV", malID.toString())
                    }
                    //Log.d("MAL_IDFAV", idList.toString())
                }
        } else {
            profileContent.visibility = View.GONE
            profileLogoutButton.visibility = View.GONE

            profileLoginButton.setOnClickListener {
                val intent = Intent(activity, LogInActivity::class.java)
                startActivity(intent)

            }
        }


        return root
    }
    fun logUserFavourites(username: String){
        val client = JikanApiClient.apiService.requestUserFavourites(username = username)

        val retryPolicy = RetryPolicy.builder<Response<UserFavouritesResponse>>()
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            if(it.isSuccessful){
                if(it.body() != null){
                    val userFavs = it.body()!!.result
                    Log.d("USER FAVS ANIME",""+userFavs.toString())
                }
            }
        }
        /*
        client.enqueue(object: Callback<UserFavouritesResponse> {
            override fun onResponse(
                call: Call<UserFavouritesResponse>,
                response: Response<UserFavouritesResponse>
            ){
                if(response.isSuccessful){
                    if(response.body() != null){
                        val userFavs = response.body()!!.result
                        Log.d("USER FAVS ANIME",""+userFavs.toString())
                    }
                }else{
                    Log.e("USER FAVS ANIME", response.message()+" "+call.request().url)
                }
            }
            override fun onFailure(call: Call<UserFavouritesResponse>, t: Throwable) {
                Log.e("USER FAVS API FAIL",""+t.message)
            }
        })

         */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
