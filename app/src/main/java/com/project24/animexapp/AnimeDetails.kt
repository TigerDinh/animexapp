package com.project24.animexapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.project24.animexapp.api.Anime
import com.project24.animexapp.api.AnimeSearchByIDResponse
import com.project24.animexapp.api.JikanApiClient
import com.project24.animexapp.api.TrailerData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnimeDetails : AppCompatActivity() {

    private val MAL_ID = 5114 // DELETE THIS
    private var animeID : Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        if(extras!=null) {
            animeID = extras.getLong("ANIME_ID", -1)
        }
        setContentView(R.layout.activity_anime_details)

        grabAnimeInfo()
    }

    private fun grabAnimeInfo() {
        if (animeID.toInt() == -1){
            return
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
    }

    private fun setAnimeDetails(animeData: Anime) {
        val txt = findViewById<TextView>(R.id.textViewAnimeDetailsTitle)
        txt.text = animeData.title

        if (animeData.trailerData?.youtubeID == null){
            Log.d("Failed to load video", "Sadge") // DELETE THIS
            return
        }

        // Load Video
        val youTubePlayerView: YouTubePlayerView = findViewById(R.id.videoPlayer)
        youTubePlayerView.enterFullScreen()
        youTubePlayerView.toggleFullScreen()
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                // loading the selected video into the YouTube Player
                youTubePlayer.cueVideo(animeData.trailerData?.youtubeID, 0F)
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                // this method is called if video has ended,
                super.onStateChange(youTubePlayer, state)
            }
        })


    }
}