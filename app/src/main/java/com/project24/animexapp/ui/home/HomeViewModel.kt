package com.project24.animexapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel : ViewModel() {
    private var firebaseAuth: FirebaseAuth


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