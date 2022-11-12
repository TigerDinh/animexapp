package com.project24.animexapp

//import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
//import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
//import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
//import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.api.Anime
import com.project24.animexapp.api.AnimeCharacterSearchResponse
import com.project24.animexapp.api.AnimeSearchByIDResponse
import com.project24.animexapp.api.Character
import com.project24.animexapp.api.JikanApiClient
import okhttp3.internal.notifyAll
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AnimeDetails : YouTubeBaseActivity() {

    private var animeID : Long = -1
    private var YOUTUBE_API_KEY: String? = ""

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val currentUserID = firebaseAuth.currentUser?.uid

        val extras = intent.extras
        if(extras!=null) {
            animeID = extras.getLong("ANIME_ID", -1)
        }
        setContentView(R.layout.activity_anime_details)

        YOUTUBE_API_KEY = this.packageManager.getApplicationInfo(
            this.packageName,
            PackageManager.GET_META_DATA
        ).metaData.getString("com.project24.animexapp.YoutubeKey")

        grabAnimeInfo()


        // Added by Matthew
        var favourite = 0; var watchlater = 0; var watching = 0;
        var favouriteButton = findViewById<ImageButton>(R.id.imageButtonAnimeDetailsFavourite)
        var watchLaterButton = findViewById<ImageButton>(R.id.imageButtonAnimeDetailsWatchLater)
        var watcthingButton = findViewById<ImageButton>(R.id.imageButtonAnimeDetailsWatching)

        var favDocRef = db.collection("Users").document(currentUserID.toString()).collection("Favourites").document(animeID.toString())


        // keep favourite button green if user already favourited anime, else gray
        favDocRef.get().addOnSuccessListener {
           if(it.exists()) {
               favouriteButton.setColorFilter(resources.getColor(R.color.main_color))
               favouriteButton.setOnClickListener() {
                   favouriteButton.setColorFilter(resources.getColor(R.color.placehold_gray))
                   favDocRef.delete()
               }
           }
        }

        favouriteButton.setOnClickListener() {
            when(favourite++ % 2 ) {
                0 -> {
                    favouriteButton.setColorFilter(resources.getColor(R.color.main_color))
                    if(db.collection("Users").document(currentUserID.toString()).collection("Favourites").document(animeID.toString()).equals(animeID.toString()))
                        Toast.makeText(this, "Already favourite", Toast.LENGTH_LONG).show()
                    else
                        db.collection("Users").document(currentUserID.toString()).collection("Favourites").document(animeID.toString()).set(Favourite(animeID))
                }

                1 -> {
                    favouriteButton.setColorFilter(resources.getColor(R.color.placehold_gray))
                    favDocRef.delete()
                }
            }
        }

        watchLaterButton.setOnClickListener() {
            when(watchlater++ % 2 ) {
                0 -> watchLaterButton.setColorFilter(resources.getColor(R.color.main_color))
                1 -> watchLaterButton.setColorFilter(resources.getColor(R.color.placehold_gray))
            }
        }

        watcthingButton.setOnClickListener() {
            when(watching++ % 2 ) {
                0 -> watcthingButton.setColorFilter(resources.getColor(R.color.main_color))
                1 -> watcthingButton.setColorFilter(resources.getColor(R.color.placehold_gray))
            }
        }

        var starButtons = ArrayList<ImageButton>()
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar1))
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar2))
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar3))
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar4))
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar5))
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar6))
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar7))
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar8))
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar9))
        starButtons.add(findViewById(R.id.imageButtonAnimeDetailsStar10))

        for (i in 0..9) {
            starButtons[i].setOnClickListener() {
                println("debug: clicked! $i")
                for (j in 0 .. i) {
                    println("debug: color $j")
                    starButtons[j].setColorFilter(resources.getColor(R.color.main_color))
                }

                for (k in i+1 .. 9) {
                    println("debug: decolor $k")
                    starButtons[k].setColorFilter(resources.getColor(R.color.placehold_gray))
                }
            }
        }
    }

    private fun grabAnimeInfo() {
        if (animeID.toInt() == -1){
            return //Indicates the previous activity did not correctly pass the animeID
        }

        val client = JikanApiClient.apiService.getAnimeByID(animeID)
        client.enqueue(object: Callback<AnimeSearchByIDResponse> {
            override fun onResponse(
                call: Call<AnimeSearchByIDResponse>,
                response: Response<AnimeSearchByIDResponse>
            ) {
                if(response.isSuccessful){
                    Log.d("anime",""+ response.body()!!.animeData)
                    setAnimeDetails(response.body()!!.animeData)
                }
            }

            override fun onFailure(call: Call<AnimeSearchByIDResponse>, t: Throwable) {
                Log.e("API FAIL",""+t.message)
            }
        })

        val client2 = JikanApiClient.apiService.getAnimeCharacterById(animeID)
        client2.enqueue(object: Callback<AnimeCharacterSearchResponse> {
            override fun onResponse(
                call: Call<AnimeCharacterSearchResponse>,
                response: Response<AnimeCharacterSearchResponse>
            ) {
                if(response.isSuccessful){
                    Log.d("Anime Characters",""+ response.body()!!.animeData)
                    setAnimeCharacterDetails(response.body()!!.animeData)
                }
            }

            override fun onFailure(call: Call<AnimeCharacterSearchResponse>, t: Throwable) {
                Log.e("API FAIL",""+t.message)
            }
        })
    }

    private fun setAnimeCharacterDetails(characterList: List<Character>) {
        // TODO Matthew, use this list to set data about anime characters onto the UI
        /*
        Here's how you can access each character and their data within
        for (character in characterList){
            character.characterData.characterName
            character.characterData.imageData.jpg
            character.characterData.imageData.webp
        }
         */
    }

    private fun setAnimeDetails(animeData: Anime) {
        setAnimeTitle(animeData.title)
        setAnimeTrailer(animeData.trailerData?.youtubeID)
        setAnimeSynopsis(animeData.synopsis)
    }

    private fun setAnimeTitle(givenTitle: String) {
        val txt = findViewById<TextView>(R.id.textViewAnimeDetailsTitle)
        txt.text = givenTitle
    }

    private fun setAnimeTrailer(youtubeID: String?) {
        if (youtubeID == null){
            Log.d("Failed to load video", "Sadge") // DELETE THIS
            return
        }

        val youtubeTrailerID = youtubeID
        val youTubePlayerView : YouTubePlayerView = findViewById(R.id.youtubePlayerView)
        youTubePlayerView.initialize(YOUTUBE_API_KEY, object:YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider?,
                player: YouTubePlayer?,
                bln: Boolean
            ) {
                player?.cueVideo(youtubeTrailerID)
                player?.play()
            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider?,
                result: YouTubeInitializationResult?
            ) {
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setAnimeSynopsis(synopsis: String?) {
        if (synopsis == null){
            return
        }

        // TODO For Matthew, set this synopsis onto the UI
    }
}