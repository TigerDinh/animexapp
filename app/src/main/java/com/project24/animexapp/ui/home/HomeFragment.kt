package com.project24.animexapp.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.AnimeDetails
import com.project24.animexapp.ui.LoadingScreens.LoadingBarActivity
import com.project24.animexapp.R
import com.project24.animexapp.api.*
import com.project24.animexapp.databinding.FragmentHomeBinding
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import dev.failsafe.RetryPolicy
import dev.failsafe.retrofit.FailsafeCall
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Thread.sleep
import java.time.Duration
import java.util.concurrent.TimeUnit

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
    private lateinit var trendingAnimeSV: SliderView
    private lateinit var trendingAdapter: SliderAdapter

    private lateinit var nologinLayout: LinearLayout
    private lateinit var loginLayout: LinearLayout
    private lateinit var mainFlipper: ViewFlipper



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // get current user's email
        val currentUserEmail = firebaseAuth.currentUser?.email
        val currentUserID = firebaseAuth.currentUser?.uid

        // get current user's email
        val currentUser = firebaseAuth.currentUser?.email
        isLoggedIn = firebaseAuth.currentUser !== null //thanks


        ongoingList = emptyList()
        ongoingAnimeRV = binding.recyclerViewHomeOngoing
        ongoingAnimeAdapter = AnimeRVAdapter(ongoingList, 0)

        ongoingAnimeRV.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        ongoingAnimeRV.adapter = ongoingAnimeAdapter


        /*
        //Observe LiveData
        viewModel.ongoingList.observe(viewLifecycleOwner, Observer { animeList ->
            //Update the RV with the data here.
            ongoingAnimeAdapter.animeList = animeList
            ongoingAnimeAdapter.notifyDataSetChanged()
        })

         */

        trendingAnimeSV = binding.sliderViewHomeHeader

        recommendationsList = emptyList()
        //recommendedAnimeRV = binding.RecForYouRV
        recommendedAnimeAdapter = AnimeRVAdapter(recommendationsList, 0)
        //recommendedAnimeRV.adapter = recommendedAnimeAdapter

        getTrending()
        setRecommendedAnime()
        //setHeadAnime()

        nologinLayout = binding.layoutHomeNoLogin
        loginLayout = binding.layoutHomeLogin

        val user = firebaseAuth.currentUser?.email.toString()
        if(user!="null")
            Toast.makeText(activity, "Logged in as $user", Toast.LENGTH_SHORT).show()

        if(isLoggedIn){
            //Logged In View
            getOngoingAnime()
            setRecommendedForYou()
            setupRefreshButtonForRecommendedForYou()
            nologinLayout.visibility = View.GONE
            loginLayout.visibility  = View.VISIBLE
        }
        else{
            //Not Logged In View
            getOngoingAnime()
            loginLayout.visibility = View.GONE
            nologinLayout.visibility  = View.VISIBLE
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        if(isLoggedIn) {
            setRecommendedForYou()
        }
    }

    private fun setupRefreshButtonForRecommendedForYou() {
        val refreshBtn = binding.refreshRecommendedAnimeForYouBtn
        refreshBtn.setOnClickListener(){
            sleep(500) // Prevents api request overload
            setRecommendedForYou()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getMyRecommendations(id: Long){
        val client = JikanApiClient.apiService.getRecommendationsByID(id = id)

        val retryPolicy = RetryPolicy.builder<Response<RecommendationsByIDResponse>>()
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            if(it.isSuccessful){
                if(it.body() != null){
                    val animeEntries = it.body()!!.result
                    for (animeEntry in animeEntries){
                        recommendationsList = recommendationsList.plus(animeEntry.animeData)
                    }

                    //PASS THE LIST TO THE ADAPTER AND REFRESH IT
                    recommendedAnimeAdapter.animeList = recommendationsList
                    recommendedAnimeAdapter.notifyDataSetChanged()

                }
            }
        }
        /*
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

                    }else{
                        Log.e("huh?","HUH")
                    }
                }
            }
            override fun onFailure(call: Call<RecommendationsByIDResponse>, t: Throwable) {
                Log.e("RECOMMENDED API FAIL",""+t.message)
            }
        })

         */
    }

    private fun setRecommendedForYou() {
        val db = Firebase.firestore
        val currentUserID = firebaseAuth.currentUser?.uid.toString()
        val favDocRef = db.collection("Users").document(currentUserID).collection("Favourites")

        // Checking if favourite list is empty
        favDocRef.get().addOnSuccessListener() {
            if(it.isEmpty) {
                // TODO Matthew, implement what the user sees when he has no favourites

            }
            else {
                getRecommendedForYou()
            }
        }
    }

    private fun getRecommendedForYou() {
        val db = Firebase.firestore
        val currentUserID = firebaseAuth.currentUser?.uid.toString()

        // Asynchronous method to get random anime from favourites
        db.collection("Users").document(currentUserID).collection("Favourites").get()
            .addOnSuccessListener { favourite ->
                val favouriteList = ArrayList<QueryDocumentSnapshot>()
                for (document in favourite) {
                    favouriteList.add(document)
                }
                val randomFavouriteAnimeIndex = (0..(favouriteList.size - 1)).random()

                val favouriteAnimeImage = favouriteList[randomFavouriteAnimeIndex].data.getValue("image_url") as String
                // TODO Matthew, set favouriteAnimeImage (which is a string url) to an imageview for the "recommended for you" section

                val favouriteAnimeTitle = favouriteList[randomFavouriteAnimeIndex].data.getValue("anime_title") as String
                setBecauseYouLike(favouriteAnimeTitle)

                val randomFavouriteAnimeID = favouriteList[randomFavouriteAnimeIndex].data.getValue(("mal_id")) as Long
                setRecommendedForYouDetails(randomFavouriteAnimeID)
            }
    }

    private fun setBecauseYouLike(favouriteAnimeTitle: String) {
        binding.textViewHomeRecommendationsBecauseTitle.text = favouriteAnimeTitle
    }

    private fun setRecommendedForYouDetails(givenAnimeID: Long) {
        val client = JikanApiClient.apiService.getRecommendationsByID(givenAnimeID)

        val retryPolicy = RetryPolicy.builder<Response<RecommendationsByIDResponse>>()
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            if(it.isSuccessful){
                if(it.body() != null){
                    val recommendedAnimeDataList = it.body()!!.result
                    val recommendedAnime = recommendedAnimeDataList.get(0).animeData

                    binding.textViewHomeRecommendationsTitle.text = recommendedAnime.title
                    binding.textViewHomeRecommendationsScore.text = recommendedAnime.score.toString()
                    Glide.with(requireView()).load(recommendedAnime.imageData!!.jpg!!.URL).centerCrop().into(binding.imageViewHomeRecommend)

                    // When recommended for you image is clicked, open anime detail page for that anime
                    binding.imageViewHomeRecommend.setOnClickListener {
                        val showAnimeIntent = Intent(requireActivity(), AnimeDetails::class.java)
                        showAnimeIntent.putExtra(getString(R.string.anime_id_key), recommendedAnime.mal_id)
                        requireActivity().startActivity(showAnimeIntent)
                        startLoadingActivity(requireActivity()) // Activities are placed in "First In Last Out" stack
                    }
                }
            }
        }

        /*
        client.enqueue(object: Callback<RecommendationsByIDResponse> {
            override fun onResponse(
                call: Call<RecommendationsByIDResponse>,
                response: Response<RecommendationsByIDResponse>
            ) {
                if(response.isSuccessful) {

                    // Get random recommended anime
                    val recommendedAnimeDataList = response.body()!!.result
                    val randomIndex = (0..(recommendedAnimeDataList.size - 1)).random()
                    val randomRecommendedAnime = recommendedAnimeDataList.get(randomIndex).animeData

                    // Get the full information of the random recommended anime
                    val client2 = JikanApiClient.apiService.getAnimeByID(randomRecommendedAnime.mal_id)
                    client2.enqueue(object: Callback<AnimeSearchByIDResponse> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<AnimeSearchByIDResponse>,
                            response: Response<AnimeSearchByIDResponse>
                        ) {
                            if(response.isSuccessful){

                                // Set the full information of the random recommended anime
                                val recommendedAnime = response.body()!!.animeData
                                // TODO title can be too long sometimes
                                binding.textViewHomeRecommendationsTitle.text = recommendedAnime.title
                                binding.textViewHomeRecommendationsSynopsis.text = recommendedAnime.synopsis
                                if (recommendedAnime.score == null){
                                    binding.textViewHomeRecommendationsScore.text = "Unrated"
                                }
                                else{
                                    binding.textViewHomeRecommendationsScore.text = recommendedAnime.score.toString()
                                }
                                Glide.with(view!!).load(recommendedAnime.imageData!!.jpg!!.URL).centerCrop().into(binding.imageViewHomeRecommend)

                                // When recommended for you image is clicked, open anime detail page for that anime
                                binding.imageViewHomeRecommend.setOnClickListener {
                                    val showAnimeIntent = Intent(requireActivity(), AnimeDetails::class.java)
                                    showAnimeIntent.putExtra(getString(R.string.anime_id_key), recommendedAnime.mal_id)
                                    requireActivity().startActivity(showAnimeIntent)
                                    startLoadingActivity(requireActivity()) // Activities are placed in "First In Last Out" stack
                                }
                            }
                        }
                        override fun onFailure(call: Call<AnimeSearchByIDResponse>, t: Throwable) {
                            Log.e("API FAIL",""+t.message)
                        }
                    })
                }
            }

            override fun onFailure(call: Call<RecommendationsByIDResponse>, t: Throwable) {
                Log.e("API FAIL",""+t.message)
            }
        })

         */
    }

    private fun startLoadingActivity(requireActivity: FragmentActivity) {
        val intent = Intent(requireActivity, LoadingBarActivity::class.java)
        requireActivity.startActivity(intent)
    }

    fun getOngoingAnime(){
        val client = JikanApiClient.apiService.requestAnime(status = "airing")

        val retryPolicy = RetryPolicy.builder<Response<AnimeSearchResponse>>()
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            if(it.isSuccessful){
                if(it.body() != null){
                    ongoingList = it.body()!!.result
                    ongoingAnimeAdapter.animeList = ongoingList
                    ongoingAnimeAdapter.notifyDataSetChanged()
                }
            }
        }
        /*
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

         */
    }

    fun getTrending(){
        val client = KitsuApiClient.apiService.trendingAnime()

        val retryPolicy = RetryPolicy.builder<Response<AnimeTrendingResponse>>()
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            if(it.isSuccessful){
                if(it.body() != null){

                    trendingList = it.body()!!.animeData

                    trendingAdapter = SliderAdapter(trendingList)
                    trendingAnimeSV.setSliderAdapter(trendingAdapter)
                    trendingAdapter.notifyDataSetChanged()
                    trendingAnimeSV.scrollTimeInMillis = 5000
                    trendingAnimeSV.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                    trendingAnimeSV.setIndicatorAnimation(IndicatorAnimationType.SLIDE);
                    trendingAnimeSV.startAutoCycle();
                }
            }
        }

        /*
        client.enqueue(object: Callback<AnimeTrendingResponse> {
            override fun onResponse(
                call: Call<AnimeTrendingResponse>,
                response: Response<AnimeTrendingResponse>
            ){
                if(response.isSuccessful){
                    if(response.body() != null){
                        trendingList = response.body()!!.animeData

                        trendingAdapter = SliderAdapter(trendingList)
                        trendingAnimeSV.setSliderAdapter(trendingAdapter)
                        trendingAdapter.notifyDataSetChanged()
                        trendingAnimeSV.scrollTimeInMillis = 5000
                        trendingAnimeSV.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                        trendingAnimeSV.setIndicatorAnimation(IndicatorAnimationType.SLIDE);
                        trendingAnimeSV.startAutoCycle();

                        //Log.d("TRENDING ANIME",""+trendingList.toString())
                    }
                }else{
                    Log.e("TRENDING ANIME", response.message()+" "+call.request().url)
                }
            }
            override fun onFailure(call: Call<AnimeTrendingResponse>, t: Throwable) {
                Log.e("TRENDING ANIME API FAIL",""+t.message)
            }
        })*/

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
        val sliderView = binding.sliderViewHomeHeader
    }
}

// TODO, use or clean this dead code
//trendingList = emptyList()
//trendingAdapter = SliderAdapter(trendingList)
//trendingAnimeSV.setSliderAdapter(trendingAdapter)
/*mainFlipper = binding.viewFlipperHome
mainFlipper.flipInterval = 2000 //2 seconds before flip
mainFlipper.isAutoStart = true //Autostart*/

//Logged In View

/*
The function is a placeholder right now to showcase the working of the api.
The client will send a request for ongoing anime.
Play around with the params to get different kinds of anime results.
See the JikanApiService interface, at the JikanApiClient file for the options
See the api docs to see the possible values for the params.
*/

//Log.d("ONGOING ANIME OUTSIDE",""+ongoingList.toString())
//getTrending()
/*
This function will be useful as a starting point for importing user favourites.
It takes in a userID (set to some random guy for now) and logs the favourite anime
of that user.
After implementing login, we can search for a user and add their favs to our acct.
*/
//val username = "B_root" //Some random guy I found and decided to make our testing username lol
//logUserFavourites(username)
/*
The function is a placeholder right now to showcase the working of the api.
The client will send a request for ongoing anime.
Play around with the params to get different kinds of anime results.
See the JikanApiService interface, at the JikanApiClient file for the options
See the api docs to see the possible values for the params.
*/
/*
This function will be useful as a starting point for importing user favourites.
It takes in a userID (set to some random guy for now) and logs the favourite anime
of that user.
After implementing login, we can search for a user and add their favs to our acct.
*/
//val username = "B_root" //Some random guy I found and decided to make our testing username lol
//logUserFavourites(username)