package com.project24.animexapp.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.LogInActivity
import com.project24.animexapp.api.*
import com.project24.animexapp.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private var isLoggedIn: Boolean = false //Integrate with firebase value

    private lateinit var ongoingList: List<Anime>
    private lateinit var ongoingAnimeRV: RecyclerView
    private lateinit var ongoingAnimeAdapter: AnimeRVAdapter

    private lateinit var recommendationsList: List<Anime>
    private lateinit var recommendedAnimeRV: RecyclerView
    private lateinit var recommendedAnimeAdapter: AnimeRVAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // get current user's email
        val currentUserEmail = firebaseAuth.currentUser?.email
        val currentUserID = firebaseAuth.currentUser?.uid
        
        val homeLogInBtn = binding.buttonHomeGuestLogin
        val homeLogOutBtn = binding.buttonHomeGuestLogout
        val textView: TextView = binding.textViewHomeGuestLoginText
        // get current user's email
        val currentUser = firebaseAuth.currentUser?.email
        isLoggedIn = firebaseAuth.currentUser !== null //thanks

        ongoingList = emptyList()
        ongoingAnimeRV = binding.recyclerViewHomeGuestOngoing
        ongoingAnimeAdapter = AnimeRVAdapter(ongoingList)

        ongoingAnimeRV.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        ongoingAnimeRV.adapter = ongoingAnimeAdapter

        recommendationsList = emptyList()
        recommendedAnimeRV = binding.RecForYouRV
        recommendedAnimeAdapter = AnimeRVAdapter(recommendationsList)
        recommendedAnimeRV.adapter = recommendedAnimeAdapter

        //Added by matthew
        var guestRecommendedAnimeRV = binding.recyclerViewHomeGuestRecommendations

        guestRecommendedAnimeRV.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        guestRecommendedAnimeRV.adapter = ongoingAnimeAdapter
        //////////



        // when login button is clicked, go to login activity
        homeLogInBtn.setOnClickListener {
            val intent = Intent(activity, LogInActivity::class.java)
            startActivity(intent)
        }

        // when logged out button is clicked, logout user and hide logout button, show login button
        homeLogOutBtn.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(activity, "Logged out", Toast.LENGTH_SHORT).show()
            homeLogOutBtn.visibility = View.GONE
            homeLogInBtn.visibility = View.VISIBLE
            textView.text = "Welcome to AnimeXApp"
        }


        // if logged in, hide login button, show logout button, change text to Welcome, User...
        if(firebaseAuth.currentUser !== null) {
            homeLogOutBtn.visibility = View.VISIBLE
            homeLogInBtn.visibility = View.GONE
            textView.text = "Welcome, $currentUserEmail"
        // if logged out, hide logout button, show login button
        } else {
            homeLogOutBtn.visibility = View.GONE
            homeLogInBtn.visibility = View.VISIBLE
            textView.text = "Welcome to AnimeXApp"
        }

        val user = firebaseAuth.currentUser?.email.toString()
        Toast.makeText(activity, "Logged in as $user", Toast.LENGTH_SHORT).show()

        if(isLoggedIn){
            //Logged In View

            /*
            The function is a placeholder right now to showcase the working of the api.
            The client will send a request for ongoing anime.
            Play around with the params to get different kinds of anime results.
            See the JikanApiService interface, at the JikanApiClient file for the options
            See the api docs to see the possible values for the params.
            */
            getOngoingAnime()
            getMyRecommendations(5114)
            //Log.d("ONGOING ANIME OUTSIDE",""+ongoingList.toString())
            logTrending()
            /*
            This function will be useful as a starting point for importing user favourites.
            It takes in a userID (set to some random guy for now) and logs the favourite anime
            of that user.
            After implementing login, we can search for a user and add their favs to our acct.
            */
            //val username = "B_root" //Some random guy I found and decided to make our testing username lol
            //logUserFavourites(username)
        }
        else{
            //Not Logged In View

            /*
            The function is a placeholder right now to showcase the working of the api.
            The client will send a request for ongoing anime.
            Play around with the params to get different kinds of anime results.
            See the JikanApiService interface, at the JikanApiClient file for the options
            See the api docs to see the possible values for the params.
            */
            getOngoingAnime()
            /*
            This function will be useful as a starting point for importing user favourites.
            It takes in a userID (set to some random guy for now) and logs the favourite anime
            of that user.
            After implementing login, we can search for a user and add their favs to our acct.
            */
            //val username = "B_root" //Some random guy I found and decided to make our testing username lol
            //logUserFavourites(username)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getMyRecommendations(id: Long){
        val client = JikanApiClient.apiService.getRecommendationsByID(id = id)

        client.enqueue(object: Callback<RecommendationsByIDResponse> {
            override fun onResponse(
                call: Call<RecommendationsByIDResponse>,
                response: Response<RecommendationsByIDResponse>
            ){
                if(response.isSuccessful){
                    if(response.body() != null){
                        val animeEntries = response.body()!!.result
                        for (animeEntry in animeEntries){
                            recommendationsList = recommendationsList.plus(animeEntry.animeData)
                        }

                        //PASS THE LIST TO THE ADAPTER AND REFRESH IT
                        recommendedAnimeAdapter.animeList = recommendationsList
                        recommendedAnimeAdapter.notifyDataSetChanged()

                        //ongoingAnimeAdapter.animeList = ongoingList
                        //ongoingAnimeAdapter.notifyDataSetChanged()


                        Log.d("RECOMMENDED FOR YOU",""+recommendationsList[0].toString())
                    }else{
                        Log.e("huh?","HUH")
                    }
                }
            }
            override fun onFailure(call: Call<RecommendationsByIDResponse>, t: Throwable) {
                Log.e("RECOMMENDED API FAIL",""+t.message)
            }
        })
    }

    fun getOngoingAnime(){
        val client = JikanApiClient.apiService.requestAnime(status = "airing")

        client.enqueue(object: Callback<AnimeSearchResponse> {
            override fun onResponse(
                call: Call<AnimeSearchResponse>,
                response: Response<AnimeSearchResponse>
            ){
                if(response.isSuccessful){
                    if(response.body() != null){
                        ongoingList = response.body()!!.result

                        //PASS THE LIST TO THE ADAPTER AND REFRESH IT

                        ongoingAnimeAdapter.animeList = ongoingList
                        ongoingAnimeAdapter.notifyDataSetChanged()


                        //Log.d("ONGOING ANIME",""+ongoingList.toString())
                    }
                }
            }
            override fun onFailure(call: Call<AnimeSearchResponse>, t: Throwable) {
                Log.e("ONGOING ANIME API FAIL",""+t.message)
            }
        })
    }

    fun logTrending(){
        val client = KitsuApiClient.apiService.trendingAnime()

        client.enqueue(object: Callback<AnimeTrendingResponse> {
            override fun onResponse(
                call: Call<AnimeTrendingResponse>,
                response: Response<AnimeTrendingResponse>
            ){
                if(response.isSuccessful){
                    if(response.body() != null){
                        val trendingResponse = response.body()!!.animeData
                        Log.d("TRENDING ANIME",""+trendingResponse.toString())
                    }
                }else{
                    Log.e("TRENDING ANIME", response.message()+" "+call.request().url)
                }
            }
            override fun onFailure(call: Call<AnimeTrendingResponse>, t: Throwable) {
                Log.e("TRENDING ANIME API FAIL",""+t.message)
            }
        })
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
}