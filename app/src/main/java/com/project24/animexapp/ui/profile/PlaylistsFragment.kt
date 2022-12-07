package com.project24.animexapp.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.LogInActivity
import com.project24.animexapp.R
import com.project24.animexapp.api.JikanApiClient
import com.project24.animexapp.api.LocalAnime
import com.project24.animexapp.api.UserFavouritesResponse
import com.project24.animexapp.databinding.FragmentPlaylistsBinding
import com.project24.animexapp.databinding.FragmentProfileBinding
import dev.failsafe.RetryPolicy
import dev.failsafe.retrofit.FailsafeCall
import retrofit2.Response
import java.time.Duration

class PlaylistsFragment : Fragment() {
    private var _binding: FragmentPlaylistsBinding? = null
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


    private lateinit var currentUserID : String
    private lateinit var chosenLanguage : String

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore



        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        currentUserID = firebaseAuth.currentUser?.uid.toString()
        val englishBtn = binding.englishBtn
        val japaneseBtn = binding.japaneseBtn
        val favEmptyText = binding.emptyFavText
        val watchingEmptyText = binding.emptyWatchingText
        val watchLaterEmptyText = binding.emptyWatchLaterText
        val chosenLanguagePreferences = requireActivity().getSharedPreferences(getString(R.string.shared_preference_language_key),
            Context.MODE_PRIVATE
        )
        chosenLanguage =
            chosenLanguagePreferences.getString(getString(R.string.chosen_language_key), getString(R.string.english))!!


        englishBtn.setOnClickListener{
            englishBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.light_green))
            japaneseBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.back_tint_gray))

            if (currentUserID != "null" && chosenLanguage == getString(R.string.japanese)){
                val updateUiThread = Thread(){
                    val handler = Handler(Looper.getMainLooper())
                    val myRunnable = Runnable {
                        updateFavourites(currentUserID, chosenLanguage)
                        updateWatchingLater(currentUserID, chosenLanguage)
                        updateCurrentlyWatching(currentUserID, chosenLanguage)
                    }
                    handler.post(myRunnable)
                }
                updateUiThread.start()
                updateUiThread.join()
            }


            val prefsEditor = chosenLanguagePreferences.edit()
            prefsEditor.clear()
            prefsEditor.putString(getString(R.string.chosen_language_key), getString(R.string.english))
            chosenLanguage = getString(R.string.english)
            prefsEditor.apply()
        }

        japaneseBtn.setOnClickListener{
            englishBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.back_tint_gray))
            japaneseBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.main_color))

            if (currentUserID != "null" && chosenLanguage == getString(R.string.english)){
                val updateUiThread = Thread(){
                    val handler = Handler(Looper.getMainLooper())
                    val myRunnable = Runnable {
                        updateFavourites(currentUserID, chosenLanguage)
                        updateWatchingLater(currentUserID, chosenLanguage)
                        updateCurrentlyWatching(currentUserID, chosenLanguage)
                    }
                    handler.post(myRunnable)
                }
                updateUiThread.start()
                updateUiThread.join()
            }

            val prefsEditor = chosenLanguagePreferences.edit()
            prefsEditor.clear()
            prefsEditor.putString(getString(R.string.chosen_language_key), getString(R.string.japanese))
            chosenLanguage = getString(R.string.japanese)
            prefsEditor.apply()
        }

        if (chosenLanguage == getString(R.string.english)){
            englishBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.light_green))
        }
        else {
            japaneseBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.main_color))
        }

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


        val favDocRef = db.collection("Users").document(currentUserID).collection("Favourites")
        val watchLaterDocRef = db.collection("Users").document(currentUserID).collection("WatchLater")
        val watchingDocRef = db.collection("Users").document(currentUserID).collection("Watching")


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
        updateFavourites(currentUserID, chosenLanguage)
        updateWatchingLater(currentUserID, chosenLanguage)
        updateCurrentlyWatching(currentUserID, chosenLanguage)

        return root
    }

    override fun onResume() {
        if (currentUserID!=="null") {
            val updateUiThread = Thread(){
                val handler = Handler(Looper.getMainLooper())
                val myRunnable = Runnable {
                    updateFavourites(currentUserID, chosenLanguage)
                    updateWatchingLater(currentUserID, chosenLanguage)
                    updateCurrentlyWatching(currentUserID, chosenLanguage)
                }
                handler.post(myRunnable)
            }
            updateUiThread.start()
            updateUiThread.join()
        }

        super.onResume()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateFavourites(currentUserID: String, chosenLanguage: String?) {
        val db = Firebase.firestore
        favoritesList = emptyList()

        db.collection("Users").document(currentUserID).collection("Favourites").get()
            .addOnSuccessListener { favourite ->
                for (document in favourite) {
                    var malID: Long = document.data.getValue("mal_id") as Long
                    var imgURL: String = document.data.getValue("image_url") as String

                    // To prevent older version of database from crashing
                    if (document.data.size > 3) {
                        if (chosenLanguage == getString(R.string.english)
                            && document.data.getValue("anime_english_title") != null
                        ) {
                            var animeTitle: String =
                                document.data.getValue("anime_english_title") as String
                            favoritesList =
                                favoritesList + LocalAnime(malID, animeTitle, imgURL)
                            // DELETE THIS
                            Log.d("ARRR", favoritesList.toString())
                        } else {
                            var animeTitle: String =
                                document.data.getValue("anime_title") as String
                            favoritesList =
                                favoritesList + LocalAnime(malID, animeTitle, imgURL)
                        }
                    }
                    else{
                        var animeTitle: String =
                            document.data.getValue("anime_title") as String
                        favoritesList =
                            favoritesList + LocalAnime(malID, animeTitle, imgURL)
                    }

                    favoritesAnimeAdapter.animeList = favoritesList
                    favoritesAnimeAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun updateWatchingLater(currentUserID: String, chosenLanguage: String?) {
        val db = Firebase.firestore
        watchLaterList = emptyList()

        db.collection("Users").document(currentUserID).collection("WatchLater").get()
            .addOnSuccessListener { watchLater ->
                for (document in watchLater) {
                    var malID: Long = document.data.getValue("mal_id") as Long
                    var imgURL: String = document.data.getValue("image_url") as String

                    // To prevent older version of database from crashing
                    if (document.data.size > 3) {
                        if (chosenLanguage == getString(R.string.english)
                            && document.data.getValue("anime_english_title")  != null
                        ) {
                            var animeTitle: String =
                                document.data.getValue("anime_english_title") as String
                            watchLaterList =
                                watchLaterList + LocalAnime(malID, animeTitle, imgURL)
                        }
                        else {
                            var animeTitle: String =
                                document.data.getValue("anime_title") as String
                            watchLaterList =
                                watchLaterList + LocalAnime(malID, animeTitle, imgURL)
                        }
                    }
                    else{
                        var animeTitle: String =
                            document.data.getValue("anime_title") as String
                        watchLaterList =
                            watchLaterList + LocalAnime(malID, animeTitle, imgURL)
                    }
                    watchLaterAnimeAdapter.animeList = watchLaterList
                    watchLaterAnimeAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun updateCurrentlyWatching(currentUserID: String, chosenLanguage: String?) {
        val db = Firebase.firestore
        watchingList = emptyList()
        db.collection("Users").document(currentUserID).collection("Watching").get()
            .addOnSuccessListener { watching ->
                //Log.d("favorite",favourite.documents.)
                //var idList = emptyList<Long>()
                for (document in watching) {
                    var malID: Long = document.data.getValue("mal_id") as Long
                    var imgURL: String = document.data.getValue("image_url") as String

                    // To prevent older version of database from crashing
                    if (document.data.size > 3) {
                        if (chosenLanguage == getString(R.string.english)
                            && document.data.getValue("anime_english_title") != null
                        ) {
                            var animeTitle: String =
                                document.data.getValue("anime_english_title") as String
                            watchingList = watchingList + LocalAnime(malID, animeTitle, imgURL)
                        }
                        else {
                            var animeTitle: String =
                                document.data.getValue("anime_title") as String
                            watchingList = watchingList + LocalAnime(malID, animeTitle, imgURL)
                        }
                    }
                    else {
                        var animeTitle: String =
                            document.data.getValue("anime_title") as String
                        watchingList = watchingList + LocalAnime(malID, animeTitle, imgURL)
                    }
                    watchingAnimeAdapter.animeList = watchingList
                    watchingAnimeAdapter.notifyDataSetChanged()
                }
            }
    }
}