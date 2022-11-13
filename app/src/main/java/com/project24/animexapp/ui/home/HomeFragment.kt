package com.project24.animexapp.ui.home

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.LogInActivity
import com.project24.animexapp.R
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

    private lateinit var trendingList: List<KitsuAnimeData>



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
        
        val homeLogInBtn = binding.buttonHomeLogin
        val homeLogOutBtn = binding.buttonHomeLogout
        val textView: TextView = binding.textViewHomeLoginText
        // get current user's email
        val currentUser = firebaseAuth.currentUser?.email
        isLoggedIn = firebaseAuth.currentUser !== null //thanks

        ongoingList = emptyList()
        ongoingAnimeRV = binding.recyclerViewHomeOngoing
        ongoingAnimeAdapter = AnimeRVAdapter(ongoingList)

        ongoingAnimeRV.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        ongoingAnimeRV.adapter = ongoingAnimeAdapter


        //Removed functionality
        recommendationsList = emptyList()
        //recommendedAnimeRV = binding.RecForYouRV
        recommendedAnimeAdapter = AnimeRVAdapter(recommendationsList)
        //recommendedAnimeRV.adapter = recommendedAnimeAdapter


        setRecommendedAnime()
        setHeadAnime()




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
            getTrending()
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

    fun getTrending(){
        val client = KitsuApiClient.apiService.trendingAnime()

        client.enqueue(object: Callback<AnimeTrendingResponse> {
            override fun onResponse(
                call: Call<AnimeTrendingResponse>,
                response: Response<AnimeTrendingResponse>
            ){
                if(response.isSuccessful){
                    if(response.body() != null){
                        trendingList = response.body()!!.animeData
                        Log.d("TRENDING ANIME",""+trendingList.toString())
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

    fun setRecommendedAnime(){
        //TODO Ui interface to import single recommended anime here
        val recImage = binding.imageViewHomeRecommend
        val recTitle = binding.textViewHomeRecommendationsTitle
        val recScore = binding.textViewHomeRecommendationsScore
        val recBecTitle = binding.textViewHomeRecommendationsBecauseTitle
        var recSynopsis = binding.textViewHomeRecommendationsSynopsis

        //Inputted for Display Example
        recSynopsis.text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

    }

    fun setHeadAnime(){
        //TODO Implement anime header info here (only three can be displayed)
        val viewFlipper = binding.viewFlipperHome
        val radioButtons = arrayListOf(binding.radioButtonHomeFlipper1, binding.radioButtonHomeFlipper2, binding.radioButtonHomeFlipper3)

        viewFlipper.inAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {
                //Ignore, these were necessary to make radio buttons keep up with display
            }

            override fun onAnimationEnd(p0: Animation?) {
                radioButtons[viewFlipper.displayedChild].isChecked = true
            }

            override fun onAnimationRepeat(p0: Animation?) {
                //Ignore, these were necessary to make radio buttons keep up with display
            }
        })

        //The val you need
        //Format:
        //headerList[listed anime 0 to 2][image, title, synopsis]
        val headerList = mutableListOf<Any>()
        headerList.add(mutableListOf<Any>(binding.imageViewHomeHeader1, binding.textViewHomeTitleHeader1, binding.textViewHomeSynopsisHeader1))
        headerList.add(mutableListOf<Any>(binding.imageViewHomeHeader2, binding.textViewHomeTitleHeader2, binding.textViewHomeSynopsisHeader2))
        headerList.add(mutableListOf<Any>(binding.imageViewHomeHeader3, binding.textViewHomeTitleHeader3, binding.textViewHomeSynopsisHeader3))

    }
}