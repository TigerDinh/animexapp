package com.project24.animexapp.ui.profile

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.api.Anime
import com.project24.animexapp.api.AnimeSearchByIDResponse
import com.project24.animexapp.api.JikanApiClient
import com.project24.animexapp.databinding.FragmentNotificationsBinding
import com.project24.animexapp.databinding.FragmentProfileBinding
import com.project24.animexapp.ui.home.AnimeRVAdapter
import okhttp3.internal.wait
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.io.path.createTempDirectory

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var favoritesList: List<Anime>
    private lateinit var favoritesAnimeRV: RecyclerView
    private lateinit var favoritesAnimeAdapter: AnimeRVAdapter

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
        val currentUserID = firebaseAuth.currentUser?.uid.toString()

        profileEmail.text = currentUserEmail

        favoritesList = emptyList()
        favoritesAnimeRV = binding.favoritesRecyclerView
        favoritesAnimeAdapter = AnimeRVAdapter(favoritesList)

        favoritesAnimeRV.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.HORIZONTAL,false)
        favoritesAnimeRV.adapter = favoritesAnimeAdapter

        db.collection("Users").document(currentUserID).collection("Favourites").get().addOnSuccessListener { favourite ->
            //Log.d("favorite",favourite.documents.)
            //var idList = emptyList<Long>()
            for (document in favourite) {
                var malID : Long = document.data.getValue("mal_id") as Long
                grabAnimeInfo(malID)
                //idList = idList.plus(malID)
                //Log.d("MAL_IDFAV", malID.toString())



                /*
                val favButton = Button(this.requireContext())
                favButton.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                favButton.text = document.data.getValue("mal_id").toString()
                binding.root.addView(favButton)

                 */
            }
            //Log.d("MAL_IDFAV", idList.toString())
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun grabAnimeInfo(animeID: Long) {
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
                    //setAnimeDetails(response.body()!!.animeData)
                    favoritesList = favoritesList + response.body()!!.animeData
                    favoritesAnimeAdapter.animeList = favoritesList
                    favoritesAnimeAdapter.notifyDataSetChanged()
                }else{      
                    Log.e("animeerr","ANIMEERR: "+response.message())
                }
            }

            override fun onFailure(call: Call<AnimeSearchByIDResponse>, t: Throwable) {
                Log.e("API FAIL",""+t.message)
            }
        })
    }



}