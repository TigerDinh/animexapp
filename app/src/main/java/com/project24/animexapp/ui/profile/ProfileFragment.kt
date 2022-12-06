package com.project24.animexapp.ui.profile

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.LogInActivity
import com.project24.animexapp.R
import com.project24.animexapp.api.JikanApiClient
import com.project24.animexapp.api.LocalAnime
import com.project24.animexapp.api.UserFavouritesResponse
import com.project24.animexapp.databinding.FragmentProfileBinding
import dev.failsafe.RetryPolicy
import dev.failsafe.retrofit.FailsafeCall
import retrofit2.Response
import java.time.Duration


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    private lateinit var favoritesList: List<LocalAnime>
    private lateinit var favoritesAnimeRV: RecyclerView
    private lateinit var favoritesAnimeAdapter: LocalAnimeRVAdapter

    private lateinit var watchLaterList: List<LocalAnime>
    private lateinit var watchLaterAnimeRV: RecyclerView
    private lateinit var watchLaterAnimeAdapter: LocalAnimeRVAdapter

    private lateinit var watchingList: List<LocalAnime>
    private lateinit var watchingAnimeRV: RecyclerView
    private lateinit var watchingAnimeAdapter: LocalAnimeRVAdapter

    private lateinit var profileViewModel : ProfileViewModel

    private lateinit var currentUserID : String
    private lateinit var chosenLanguage : String

    private lateinit var favEmptyText : TextView
    private lateinit var watchingEmptyText : TextView
    private lateinit var watchLaterEmptyText : TextView
    private lateinit var noAccountContent : LinearLayout
    private lateinit var profileContent : LinearLayout
    private lateinit var profileLoginButton : Button
    private lateinit var profileLogoutButton : Button

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firebaseAuth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val profileEmail = binding.profileEmail
        val currentUserEmail = firebaseAuth.currentUser?.email.toString()
        currentUserID = firebaseAuth.currentUser?.uid.toString()
        favEmptyText = binding.emptyFavText
        watchingEmptyText = binding.emptyWatchingText
        watchLaterEmptyText = binding.emptyWatchLaterText
        profileContent = binding.profileContent
        profileLogoutButton = binding.profileLogoutButton
        noAccountContent = binding.noAccountContent
        profileLoginButton = binding.profileLoginButton
        val englishBtn = binding.englishBtn
        val japaneseBtn = binding.japaneseBtn
        val chosenLanguagePreferences = requireActivity().getSharedPreferences(getString(R.string.shared_preference_language_key), MODE_PRIVATE)
        chosenLanguage =
            chosenLanguagePreferences.getString(getString(R.string.chosen_language_key), getString(R.string.english))!!

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

        // Set language for entire app
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

        return root
    }

    override fun onStart() {
        if (currentUserID !=="null") {
            val favDocRef = db.collection("Users").document(currentUserID).collection("Favourites")
            val watchLaterDocRef = db.collection("Users").document(currentUserID).collection("WatchLater")
            val watchingDocRef = db.collection("Users").document(currentUserID).collection("Watching")

            noAccountContent.visibility = View.GONE
            profileLogoutButton.visibility = View.VISIBLE

            favDocRef.get().addOnSuccessListener() {
                if(it.isEmpty) {
                    favEmptyText.visibility = View.VISIBLE
                    favEmptyText.text = "You have no items on your favourites yet."
                } else {
                    favEmptyText.visibility = View.GONE
                }
            }

            watchingDocRef.get().addOnSuccessListener() {
                if(it.isEmpty) {
                    watchingEmptyText.visibility = View.VISIBLE
                    watchingEmptyText.text = "You have no items on your watching yet."
                } else {
                    watchingEmptyText.visibility = View.GONE
                }
            }

            watchLaterDocRef.get().addOnSuccessListener() {
                if(it.isEmpty) {
                    watchLaterEmptyText.visibility = View.VISIBLE
                    watchLaterEmptyText.text = "You have no items on your watch later yet."
                } else {
                    watchLaterEmptyText.visibility = View.GONE
                }
            }

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
            favoritesAnimeAdapter.notifyDataSetChanged()
            watchingAnimeAdapter.notifyDataSetChanged()
            watchLaterAnimeAdapter.notifyDataSetChanged()
        }


        else{
            profileContent.visibility = View.GONE
            profileLogoutButton.visibility = View.GONE

            profileLoginButton.setOnClickListener {
                val intent = Intent(activity, LogInActivity::class.java)
                startActivity(intent)

            }
        }
        super.onStart()
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
                if (favourite.size() == 0){
                    favoritesAnimeAdapter.animeList = emptyList()
                    favoritesAnimeAdapter.notifyDataSetChanged()
                }

                else{
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
    }

    private fun updateWatchingLater(currentUserID: String, chosenLanguage: String?) {
        val db = Firebase.firestore
        watchLaterList = emptyList()

        db.collection("Users").document(currentUserID).collection("WatchLater").get()
            .addOnSuccessListener { watchLater ->
                if (watchLater.size() == 0){
                    watchLaterAnimeAdapter.animeList = emptyList()
                    watchLaterAnimeAdapter.notifyDataSetChanged()
                }

                else{
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
    }

    private fun updateCurrentlyWatching(currentUserID: String, chosenLanguage: String?) {
        val db = Firebase.firestore
        watchingList = emptyList()
        db.collection("Users").document(currentUserID).collection("Watching").get()
            .addOnSuccessListener { watching ->
                if (watching.size() == 0){
                    watchingAnimeAdapter.animeList = emptyList()
                    watchingAnimeAdapter.notifyDataSetChanged()
                }

                else{
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
}
