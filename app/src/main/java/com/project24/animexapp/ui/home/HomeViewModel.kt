package com.project24.animexapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.project24.animexapp.api.Anime
import com.project24.animexapp.api.AnimeEntry

class HomeViewModel : ViewModel() {
    private var firebaseAuth: FirebaseAuth

    //var ongoingList: MutableLiveData<List<Anime>> = MutableLiveData()
    //var recommendedAnimeDataList: MutableLiveData<List<AnimeEntry>> = MutableLiveData()


    private val _text = MutableLiveData<String>().apply {
        firebaseAuth = FirebaseAuth.getInstance()
        var currentUser = firebaseAuth.currentUser?.email

        value = if (currentUser !== null) {
            "Welcome, $currentUser"
        } else {
            "This is home fragment"
        }

    }
    val text: LiveData<String> = _text
}