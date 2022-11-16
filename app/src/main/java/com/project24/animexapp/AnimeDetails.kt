package com.project24.animexapp

//import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
//import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
//import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
//import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AnimeDetails : YouTubeBaseActivity() {
    private var animeID : Long = -1
    private var YOUTUBE_API_KEY: String? = ""

    private lateinit var firebaseAuth: FirebaseAuth

    private var youTubePlayerView : YouTubePlayerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        if(extras!=null) {
            animeID = extras.getLong(getString(R.string.anime_id_key), -1)
        }

        setContentView(R.layout.activity_anime_details)

        YOUTUBE_API_KEY = this.packageManager.getApplicationInfo(
            this.packageName,
            PackageManager.GET_META_DATA
        ).metaData.getString("com.project24.animexapp.YoutubeKey")


        grabAnimeInfo()
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
                    val animeData = response.body()!!.animeData

                    setAnimeDetails(animeData)
                    SetUpStarsRating(animeData)
                    setButtons(animeData)
                    setReviewDialog(animeData)
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
                    setAnimeCharacterDetails(response.body()!!.animeData)
                }
            }

            override fun onFailure(call: Call<AnimeCharacterSearchResponse>, t: Throwable) {
                Log.e("API FAIL",""+t.message)
            }
        })
    }

    private fun SetUpStarsRating(animeData: Anime) {
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
                for (j in 0..i) {
                    starButtons[j].setColorFilter(resources.getColor(R.color.main_color))
                }

                for (k in i + 1..9) {
                    starButtons[k].setColorFilter(resources.getColor(R.color.placehold_gray))
                }
            }
        }
    }

    private fun setReviewDialog(animeData: Anime) {
        val submitAReview = findViewById<Button>(R.id.submitAReview)
        val reviewDialog = Dialog(this)

        reviewDialog.setContentView(R.layout.dialog_review)
        reviewDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val reviewRatingSpinner = reviewDialog.findViewById(R.id.reviewRatingSpinner) as Spinner
        val reviewArrow = reviewDialog.findViewById(R.id.spinnerArrow) as ImageView
        val reviewAnimeTitle = reviewDialog.findViewById(R.id.reviewAnimeTitle) as TextView
        val submitReviewDialog = reviewDialog.findViewById(R.id.reviewSubmitButton) as Button
        val reviewComment = reviewDialog.findViewById(R.id.reviewAnimeComment) as EditText

        reviewAnimeTitle.text = animeData.title

            reviewArrow.setOnClickListener {
                reviewRatingSpinner.performClick()
            }

        ArrayAdapter.createFromResource(
            this,
            R.array.reviewrating,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            reviewRatingSpinner.adapter = adapter
        }
        submitAReview.setOnClickListener {
            reviewDialog.show()
        }
    }

    private fun setButtons(animeData: Anime) {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val currentUserID = firebaseAuth.currentUser?.uid

        var favourite = 0; var watchlater = 0; var watching = 0;
        var favouriteButton = findViewById<ImageButton>(R.id.imageButtonAnimeDetailsFavourite)
        var watchLaterButton = findViewById<ImageButton>(R.id.imageButtonAnimeDetailsWatchLater)
        var watcthingButton = findViewById<ImageButton>(R.id.imageButtonAnimeDetailsWatching)

        var favDocRef = db.collection("Users").document(currentUserID.toString()).collection("Favourites").document(animeID.toString())
        var watchLaterDocRef = db.collection("Users").document(currentUserID.toString()).collection("WatchLater").document(animeID.toString())
        var watchingDocRef = db.collection("Users").document(currentUserID.toString()).collection("Watching").document(animeID.toString())


        // keep buttons selected if clicked, else gray
        favDocRef.get().addOnSuccessListener {
            if(it.exists()) {
                favouriteButton.setColorFilter(resources.getColor(R.color.main_color))
                favouriteButton.setOnClickListener() {
                    favouriteButton.setColorFilter(resources.getColor(R.color.placehold_gray))
                    favDocRef.delete()
                }
            }
        }

        watchLaterDocRef.get().addOnSuccessListener {
            if(it.exists()) {
                watchLaterButton.setColorFilter(resources.getColor(R.color.main_color))
                watchLaterButton.setOnClickListener() {
                    watchLaterButton.setColorFilter(resources.getColor(R.color.placehold_gray))
                    watchLaterDocRef.delete()
                }
            }
        }

        watchingDocRef.get().addOnSuccessListener {
            if(it.exists()) {
                watcthingButton.setColorFilter(resources.getColor(R.color.main_color))
                watcthingButton.setOnClickListener() {
                    watcthingButton.setColorFilter(resources.getColor(R.color.placehold_gray))
                    watchingDocRef.delete()
                }
            }
        }

        favouriteButton.setOnClickListener() {
            when(favourite++ % 2 ) {
                0 ->
                {
                    favouriteButton.setColorFilter(resources.getColor(R.color.main_color))
                    if(db.collection("Users").document(currentUserID.toString()).collection("Favourites").document(animeID.toString()).equals(animeID.toString()))
                        Toast.makeText(this, "Already favourite", Toast.LENGTH_LONG).show()
                    else if(db.collection("AnimeData").document(animeData.mal_id.toString()).equals(animeData.mal_id.toString()))
                        Toast.makeText(this, "Already in AnimeData DB", Toast.LENGTH_LONG).show()
                    else {
                        db.collection("Users").document(currentUserID.toString()).collection("Favourites").document(animeID.toString()).set(Favourite(animeData.mal_id, animeData.imageData!!.jpg!!.URL, animeData.title))
                        db.collection("AnimeData").document(animeData.mal_id.toString()).set(LocalAnime(animeData.mal_id, animeData.title, animeData.imageData!!.jpg!!.URL, animeData.synopsis, animeData.score, animeData.trailerData))
                    }
                }

                1 ->
                {
                    favouriteButton.setColorFilter(resources.getColor(R.color.placehold_gray))
                    favDocRef.delete()
                }
            }
        }

        watchLaterButton.setOnClickListener() {
            when(watchlater++ % 2 ) {
                0 ->
                {
                    watchLaterButton.setColorFilter(resources.getColor(R.color.main_color))
                    if(db.collection("Users").document(currentUserID.toString()).collection("WatchLater").document(animeID.toString()).equals(animeID.toString()))
                        Toast.makeText(this, "Already in watch later", Toast.LENGTH_LONG).show()
                    else if(db.collection("AnimeData").document(animeData.mal_id.toString()).equals(animeData.mal_id.toString()))
                        Toast.makeText(this, "Already in AnimeData DB", Toast.LENGTH_LONG).show()
                    else {
                        db.collection("Users").document(currentUserID.toString()).collection("WatchLater").document(animeID.toString()).set(Favourite(animeData.mal_id, animeData.imageData!!.jpg!!.URL, animeData.title))
                        db.collection("AnimeData").document(animeData.mal_id.toString()).set(LocalAnime(animeData.mal_id, animeData.title, animeData.imageData!!.jpg!!.URL, animeData.synopsis, animeData.score, animeData.trailerData))
                    }
                }
                1 ->
                {
                    watchLaterButton.setColorFilter(resources.getColor(R.color.placehold_gray))
                    watchLaterDocRef.delete()
                }
            }
        }

        watcthingButton.setOnClickListener() {
            when(watching++ % 2 ) {
                0 ->
                {
                    watcthingButton.setColorFilter(resources.getColor(R.color.main_color))
                    if(db.collection("Users").document(currentUserID.toString()).collection("Watching").document(animeID.toString()).equals(animeID.toString()))
                        Toast.makeText(this, "Already in watching", Toast.LENGTH_LONG).show()
                    else if(db.collection("AnimeData").document(animeData.mal_id.toString()).equals(animeData.mal_id.toString()))
                        Toast.makeText(this, "Already in AnimeData DB", Toast.LENGTH_LONG).show()
                    else {
                        db.collection("Users").document(currentUserID.toString()).collection("Watching").document(animeID.toString()).set(Favourite(animeData.mal_id, animeData.imageData!!.jpg!!.URL, animeData.title))
                        db.collection("AnimeData").document(animeData.mal_id.toString()).set(LocalAnime(animeData.mal_id, animeData.title, animeData.imageData!!.jpg!!.URL, animeData.synopsis, animeData.score, animeData.trailerData))
                    }
                }


                1 ->
                {
                    watcthingButton.setColorFilter(resources.getColor(R.color.placehold_gray))
                    watchingDocRef.delete()
                }
            }
        }
    }

    private fun setAnimeCharacterDetails(characterList: List<Character>) {
        val characterRV = findViewById<RecyclerView>(R.id.recyclerViewAnimeDetailsCharacters)

        var characterAdapter = CharacterRVAdapter(characterList)

        characterRV.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,false)
        characterRV.adapter = characterAdapter
    }

    private fun setAnimeDetails(animeData: Anime) {
        setAnimeTitle(animeData.title)
        setAnimeTrailer(animeData.trailerData?.youtubeID)
        setAnimeSynopsis(animeData.synopsis)
        setAnimeScore(animeData.score.toString())
    }

    private fun setAnimeTitle(givenTitle: String) {
        val txt = findViewById<TextView>(R.id.textViewAnimeDetailsTitle)
        txt.text = givenTitle
    }

    private fun setAnimeScore(givenScore: String) {
        val score = findViewById<TextView>(R.id.textViewAnimeDetailsScore)
        score.text = givenScore
    }

    private fun setAnimeTrailer(youtubeID: String?) {
        if (youtubeID == null){
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

        var syn = findViewById<TextView>(R.id.textViewAnimeDetailsSynopsis)
        syn.text = synopsis.dropLast(25)
    }
}