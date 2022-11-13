package com.project24.animexapp.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.api.*
import com.project24.animexapp.databinding.FragmentProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var favoritesList: List<LocalAnime>
    private lateinit var favoritesAnimeRV: RecyclerView
    private lateinit var favoritesAnimeAdapter: LocalAnimeRVAdapter

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
        favoritesAnimeAdapter = LocalAnimeRVAdapter(favoritesList)

        favoritesAnimeRV.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL, false
        )
        favoritesAnimeRV.adapter = favoritesAnimeAdapter

        db.collection("Users").document(currentUserID).collection("Favourites").get()
            .addOnSuccessListener { favourite ->
                //Log.d("favorite",favourite.documents.)
                //var idList = emptyList<Long>()
                for (document in favourite) {
                    var malID: Long = document.data.getValue("mal_id") as Long
                    var imgURL: String = document.data.getValue("image_url") as String
                    var animeTitle: String = document.data.getValue("anime_title") as String
                    favoritesList = favoritesList + LocalAnime(malID, animeTitle, imgURL)
                    favoritesAnimeAdapter.animeList = favoritesList
                    favoritesAnimeAdapter.notifyDataSetChanged()
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
    fun logUserFavourites(username: String){
        val client = JikanApiClient.apiService.requestUserFavourites(username = username)

        client.enqueue(object: Callback<UserFavouritesResponse> {
            override fun onResponse(
                call: Call<UserFavouritesResponse>,
                response: Response<UserFavouritesResponse>
            ){
                if(response.isSuccessful){
                    if(response.body() != null){
                        val userFavs = response.body()!!.result
                        Log.d("USER FAVS ANIME",""+userFavs.toString())
                    }
                }else{
                    Log.e("USER FAVS ANIME", response.message()+" "+call.request().url)
                }
            }
            override fun onFailure(call: Call<UserFavouritesResponse>, t: Throwable) {
                Log.e("USER FAVS API FAIL",""+t.message)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
