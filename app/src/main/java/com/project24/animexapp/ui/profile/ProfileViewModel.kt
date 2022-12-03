package com.project24.animexapp.ui.profile

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project24.animexapp.api.LocalAnime

class ProfileViewModel : ViewModel() {

    var items: List<LocalAnime> = emptyList()
    var nameToShow = MutableLiveData<MutableList<LocalAnime>>()
    private var arrayList: MutableList<LocalAnime> = mutableListOf<LocalAnime>()

    private val _text = MutableLiveData<String>().apply {
        value = "This is profile Fragment"
    }
    val text: LiveData<String> = _text

    init {
//        nameToShow.value = arrayList
        val thread = Thread(){
            val handler = Handler(Looper.getMainLooper())
            for (item in items){
                val myRunnable = Runnable {
                    arrayList.add(item)
                    nameToShow.value = arrayList // note that value() needs to be called or the observer in the fragment won't be notified about the change. see https://stackoverflow.com/questions/47941537/notify-observer-when-item-is-added-to-list-of-livedata?rq=1
                } // code crashes if accessing nameToShow in a thread
                handler.post(myRunnable)
            }
        }
        thread.start()
    }
}