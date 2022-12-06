package com.project24.animexapp.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.Genre_Map
import com.project24.animexapp.LogInActivity
import com.project24.animexapp.R
import com.project24.animexapp.api.*
import com.project24.animexapp.databinding.FragmentHomeBinding
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import dev.failsafe.RetryPolicy
import dev.failsafe.retrofit.FailsafeCall
import retrofit2.Response
import java.security.SecureRandom
import java.time.Duration
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private var isLoggedIn: Boolean = false //Integrate with firebase value

    private lateinit var recommendationsList: List<Anime>
    private lateinit var recommendedAnimeRV: RecyclerView
    private lateinit var recommendedAnimeAdapter: AnimeRVAdapter

    private lateinit var trendingList: List<KitsuAnimeData>
    private lateinit var trendingAnimeSV: SliderView
    private lateinit var trendingAdapter: SliderAdapter

    private lateinit var newAnimeList: List<KitsuAnimeData>
    private lateinit var newAnimeRV: RecyclerView
    private lateinit var newAnimeAdapter: AnimeCardRVAdapter

    private lateinit var discoverAnimeList: List<KitsuAnimeData>
    private lateinit var discoverAnimeRV: RecyclerView
    private lateinit var discoverAnimeAdapter: AnimeCardRVAdapter

    private lateinit var animeByGenreList1: List<Anime>
    private lateinit var animeByGenreRV1: RecyclerView
    private lateinit var animeByGenreAdapter1: AnimeRVAdapter

    private lateinit var animeByGenreList2: List<Anime>
    private lateinit var animeByGenreRV2: RecyclerView
    private lateinit var animeByGenreAdapter2: AnimeRVAdapter


    companion object{
        private val rand = SecureRandom()
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        Firebase.firestore

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
        isLoggedIn = firebaseAuth.currentUser !== null //thanks

        newAnimeList = emptyList()
        newAnimeRV = binding.recyclerViewHomeNewThisSeason
        newAnimeAdapter = AnimeCardRVAdapter(newAnimeList)

        newAnimeRV.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        newAnimeRV.adapter = newAnimeAdapter


        discoverAnimeList = emptyList()
        discoverAnimeRV = binding.recyclerViewHomeDiscoverModernClassics
        discoverAnimeAdapter = AnimeCardRVAdapter(discoverAnimeList)

        discoverAnimeRV.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        discoverAnimeRV.adapter = discoverAnimeAdapter


        animeByGenreList1 = emptyList()
        animeByGenreRV1 = binding.recyclerViewHomeGenre1
        animeByGenreAdapter1 = AnimeRVAdapter(animeByGenreList1, 0)

        animeByGenreRV1.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        animeByGenreRV1.adapter = animeByGenreAdapter1

        animeByGenreList2 = emptyList()
        animeByGenreRV2 = binding.recyclerViewHomeGenre2
        animeByGenreAdapter2 = AnimeRVAdapter(animeByGenreList2, 0)

        animeByGenreRV2.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        animeByGenreRV2.adapter = animeByGenreAdapter2



        trendingAnimeSV = binding.sliderViewHomeHeader

        recommendationsList = emptyList()
        recommendedAnimeRV = binding.recyclerViewHomeRecommendedForYou
        recommendedAnimeAdapter = AnimeRVAdapter(recommendationsList, 0)

        /*ongoingAnimeRV.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)*/
        recommendedAnimeRV.adapter = recommendedAnimeAdapter

        getTrending()

        binding.buttonHomeLogin.setOnClickListener {
            val intent = Intent(activity, LogInActivity::class.java)
            startActivity(intent)
        }

        val user = firebaseAuth.currentUser?.email.toString()
        if(user!="null")
            Toast.makeText(activity, "Logged in as $user", Toast.LENGTH_SHORT).show()

        getNewThisSeason()
        getDiscoverAnime()

        //set genre 1
        val genrePair1 = Genre_Map.getRandomGenrePair()
        binding.titleGenre1.text = genrePair1.first
        getGenreAnime(genrePair1.second, 1)

        //set genre 2
        var genrePair2 = Genre_Map.getRandomGenrePair()
        while(genrePair1.toString() == genrePair2.toString()) {
            genrePair2 = Genre_Map.getRandomGenrePair()
        }
        binding.titleGenre2.text = genrePair2.first
        getGenreAnime(genrePair2.second, 2)

        if(isLoggedIn){
            //Logged In View
            setRecommendedForYou()
            binding.linerLayoutHomePleaseLogin.visibility = View.GONE
            binding.linearLayoutHomeRecommendedForYou.visibility = View.VISIBLE
        }
        else{
            //Not Logged In View
            binding.linearLayoutHomeRecommendedForYou.visibility = View.GONE
            binding.linerLayoutHomePleaseLogin.visibility = View.VISIBLE
        }
        return root
    }

    private fun getNewThisSeason(){
        //Use Kitsu api call
        //To get top this season, use : https://kitsu.io/api/edge/anime?filter[seasonYear]=2022&filter[season]=fall&sort=-averageRating
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val season: String

        if(month in 1..3){
            season = "winter"
        }
        else if(month in 4..6){
            season = "spring"
        }
        else if(month in 7..9){
            season = "summer"
        }
        else{
            season = "fall"
        }

        val client = KitsuApiClient.apiService.requestAnime(
            year = year.toString(),
            season = season,
            sort = "-averageRating"
        )

        val retryPolicy = RetryPolicy.builder<Response<KitsuAnimeResponse>>()
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            if(it.isSuccessful){
                if(it.body() != null){

                    val tmpList = mutableListOf<KitsuAnimeData>()
                    for(anime in it.body()!!.animeData)
                    {
                        if(anime.attributes.coverImageData != null){
                            tmpList.add(anime)
                        }
                    }
                    newAnimeList = tmpList //it.body()!!.animeData
                    newAnimeAdapter.animeList = newAnimeList
                    newAnimeRV.adapter = newAnimeAdapter
                    newAnimeAdapter.notifyDataSetChanged()
                }
            }
        }
    }


    private fun getRecommendedForYou() {
        /*
        Changes:
            Get from kitsu now, but how:
                Get a random faovurite
                Use jikan call too get reccs for that anime
                Use kitsu to get image lol
         */


        val db = Firebase.firestore
        val currentUserID = firebaseAuth.currentUser?.uid.toString()

        // Asynchronous method to get random anime from favourites
        db.collection("Users").document(currentUserID).collection("Favourites").get()
            .addOnSuccessListener { favourite ->
                val favouriteList = ArrayList<QueryDocumentSnapshot>()
                for (document in favourite) {
                    favouriteList.add(document)
                }

                if (favouriteList.size > 0) {
                    var randomFavouriteAnimeIndex = 0
                    if (favouriteList.size != 1){
                        val randomFavouriteAnimeIndexList = List(1) { rand.nextInt((favouriteList.size - 1)) }
                        randomFavouriteAnimeIndex = randomFavouriteAnimeIndexList[0]
                    }

                    val randomFavouriteAnimeID =
                        favouriteList[randomFavouriteAnimeIndex].data.getValue("mal_id") as Long

                    val chosenLanguagePreferences = requireActivity().getSharedPreferences(getString(
                        R.string.shared_preference_language_key), Context.MODE_PRIVATE)
                    val chosenLanguage =
                        chosenLanguagePreferences.getString(getString(R.string.chosen_language_key), getString(R.string.english))!!

                    if (chosenLanguage == getString(R.string.english)
                        && favouriteList[randomFavouriteAnimeIndex].data.getValue("anime_english_title") != null
                    ) {
                        binding.textViewHomeRecommendedTitle.text = favouriteList[randomFavouriteAnimeIndex].data.getValue("anime_english_title") as String
                    }
                    else{
                        binding.textViewHomeRecommendedTitle.text = favouriteList[randomFavouriteAnimeIndex].data.getValue("anime_title") as String
                    }

                    setRecommendedForYouDetails(randomFavouriteAnimeID)
                }
            }
    }

    private fun setRecommendedForYouDetails(givenAnimeID: Long) {
        val client = JikanApiClient.apiService.getRecommendationsByID(givenAnimeID)

        val retryPolicy = RetryPolicy.builder<Response<RecommendationsByIDResponse>>()
            .withDelay(Duration.ofSeconds(3))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            val response = it
            // Grab full information of "recommended for you" anime
            if(response.isSuccessful){
                if(response.body() != null){
                    val recommendedAnimeDataList = response.body()!!.result
                    if (recommendedAnimeDataList.isNotEmpty()){

                        /*
                        Display rv of anime?
                         */
                        val tmpList = mutableListOf<Anime>()
                        for(a in recommendedAnimeDataList){
                            tmpList.add(a.animeData)
                        }
                        recommendationsList = tmpList
                        recommendedAnimeAdapter.animeList = recommendationsList
                        recommendedAnimeRV.adapter = recommendedAnimeAdapter
                        recommendedAnimeAdapter.notifyDataSetChanged()

                    }
                }
            }
        }
    }


    private fun getDiscoverAnime(){
        //Discover anime Specs:
        /*
            Anime from 5-10 years ago
            Should be all highly rated (min_score)
            random order
            From kitsu
         */
        val c = Calendar.getInstance()
        val currentYear = c.get(Calendar.YEAR)

        val yearRangeStart = currentYear - 10
        val yearRangeEnd = currentYear - 5

        val yearRange = "$yearRangeStart..$yearRangeEnd"

        val client = KitsuApiClient.apiService.requestAnime(
            year = yearRange,
            sort = "-averageRating"
        )

        val retryPolicy = RetryPolicy.builder<Response<KitsuAnimeResponse>>()
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            if(it.isSuccessful){
                if(it.body() != null){
                    val tmpList = mutableListOf<KitsuAnimeData>()
                    for(anime in it.body()!!.animeData)
                    {
                        if(anime.attributes.coverImageData != null){
                            tmpList.add(anime)
                        }
                    }
                    discoverAnimeList = tmpList //it.body()!!.animeData
                    discoverAnimeAdapter.animeList = discoverAnimeList
                    discoverAnimeRV.adapter = discoverAnimeAdapter
                    discoverAnimeAdapter.notifyDataSetChanged()

                }
            }
        }
    }

    private fun getGenreAnime(genreID: Int,rvNum: Int){
        /*
        Get a list of anime for a genre/category. Genre is Random for now.
         */

        val client = JikanApiClient.apiService.requestAnime(genres = genreID.toString(), minScore = 7.0)

        val retryPolicy = RetryPolicy.builder<Response<AnimeSearchResponse>>()
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            if(it.isSuccessful){
                if(it.body() != null){
                    val tmpList = mutableListOf<Anime>()
                    for(anime in it.body()!!.result)
                    {
                        if(anime.imageData != null){
                            tmpList.add(anime)
                        }
                    }
                    if(rvNum == 1){
                        animeByGenreList1 = tmpList //it.body()!!.animeData
                        animeByGenreAdapter1.animeList = animeByGenreList1
                        animeByGenreRV1.adapter = animeByGenreAdapter1
                        animeByGenreAdapter1.notifyDataSetChanged()
                    }
                    else if(rvNum == 2){
                        animeByGenreList2 = tmpList //it.body()!!.animeData
                        animeByGenreAdapter2.animeList = animeByGenreList2
                        animeByGenreRV2.adapter = animeByGenreAdapter2
                        animeByGenreAdapter2.notifyDataSetChanged()
                    }


                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setRecommendedForYou() {
        val db = Firebase.firestore
        val currentUserID = firebaseAuth.currentUser?.uid.toString()
        val favDocRef = db.collection("Users").document(currentUserID).collection("Favourites")

        // Checking if favourite list is empty
        favDocRef.get().addOnSuccessListener() {
            if(!it.isEmpty) {
                getRecommendedForYou()
            }
        }
    }

    fun getTrending(){
        val client = KitsuApiClient.apiService.trendingAnime()

        val retryPolicy = RetryPolicy.builder<Response<KitsuAnimeResponse>>()
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
                    trendingAnimeSV.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
                    trendingAnimeSV.setIndicatorAnimation(IndicatorAnimationType.SLIDE)
                    trendingAnimeSV.startAutoCycle()
                }
            }
        }
    }

}