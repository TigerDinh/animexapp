package com.project24.animexapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class AnimeDetails : AppCompatActivity() {

    private val MAL_ID = 5114

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        if(extras!=null)
            Log.d("MAL_ID", extras.getLong("ANIME_ID",-1).toString())
        setContentView(R.layout.activity_anime_details)
    }
}