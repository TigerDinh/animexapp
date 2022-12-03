package com.project24.animexapp.ui.dashboard

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project24.animexapp.Genre_Map.getItemGenreID
import com.project24.animexapp.R
import com.project24.animexapp.api.Anime
import com.project24.animexapp.api.AnimeSearchResponse
import com.project24.animexapp.api.JikanApiClient
import com.project24.animexapp.databinding.FragmentDashboardBinding
import com.project24.animexapp.ui.home.AnimeRVAdapter
import dev.failsafe.RetryPolicy
import dev.failsafe.retrofit.FailsafeCall
import retrofit2.Response
import java.time.Duration

private lateinit var exploreAnimeList: List<Anime>
private lateinit var exploreAnimeRV: RecyclerView
private lateinit var exploreAnimeAdapter: AnimeRVAdapter
private var filterSettings = mutableListOf("", "", "", "", "", "desc")

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)*/

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        exploreAnimeList = emptyList()
        exploreAnimeRV = binding.ExploreRV
        exploreAnimeAdapter = AnimeRVAdapter(exploreAnimeList, 1)

        exploreAnimeRV.layoutManager = GridLayoutManager(context, 2)
        //animeAnimeRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        exploreAnimeRV.adapter = exploreAnimeAdapter

        getExploreAnime()
        getFilter()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getExploreAnime(
        query:String? = null, //search entry
        genres:String? = null, //preferred genre. PLEASE NOTE. NEEDS TO BE INPUTTED AS COMMA SEPARATED LIST OF GENRE_ID eg [1,2]
        genresExcluded:String? = null, //excluded genre. PLEASE NOTE. NEEDS TO BE INPUTTED AS COMMA SEPARATED LIST OF GENRE_ID eg [1,2]
        status:String? = null, //Options: "airing", "complete", "upcoming"
        type:String? = null, //Options: "tv" "movie" "ova" "special" "ona" "music"
        minScore:Double? = null, //minimum score of returned anime
        rating:Int? = null, //Options: "g" "pg" "pg13" "r17" "r" "rx"
        orderBy:String? = null, //Options: "mal_id", "title", "type", "rating", "start_date", "end_date", "episodes", "score", "scored_by", "rank", "popularity", "members", "favorites"
        sort:String? = null
    ){
        val client = JikanApiClient.apiService.requestAnime(
            query = query,
            genres = genres,
            genresExcluded = genresExcluded,
            status = status,
            type = type,
            minScore = minScore,
            rating = rating,
            orderBy = orderBy,
            sort = sort,
            limit = 24 //Custom for explore.
        )

        val retryPolicy = RetryPolicy.builder<Response<AnimeSearchResponse>>()
            .withDelay(Duration.ofSeconds(1))
            .withMaxRetries(3)
            .build()

        val failsafeCall = FailsafeCall.with(retryPolicy).compose(client)

        val cFuture = failsafeCall.executeAsync()
        cFuture.thenApply {
            if(it.isSuccessful){
                if(it.body() != null){
                    exploreAnimeList = it.body()!!.result

                    //PASS THE LIST TO THE ADAPTER AND REFRESH IT

                    exploreAnimeAdapter.animeList = exploreAnimeList
                    exploreAnimeAdapter.notifyDataSetChanged()


                    //Log.d("ONGOING ANIME",""+ongoingList.toString())
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
                        exploreAnimeList = response.body()!!.result

                        //PASS THE LIST TO THE ADAPTER AND REFRESH IT

                        exploreAnimeAdapter.animeList = exploreAnimeList
                        exploreAnimeAdapter.notifyDataSetChanged()


                        //Log.d("ONGOING ANIME",""+ongoingList.toString())
                    }
                }
            }
            override fun onFailure(call: Call<AnimeSearchResponse>, t: Throwable) {
                Log.e("EXPLORE ANIME API FAIL",""+t.message)
            }
        })

         */
    }

    fun getFilter(){
        val searchFilter = binding.buttonExploreFilter
        val stringArrays = arrayOf(resources.getStringArray(R.array.status),
            resources.getStringArray(R.array.type), resources.getStringArray(R.array.sortby))
        val stringGenreArray = resources.getStringArray(R.array.genres)

        //Filter Dialog
        val dialog = Dialog(requireContext())
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_search_filter)

        //Get Resources
        val spinnerArray = arrayListOf<Spinner>(dialog.findViewById(R.id.spinnerSearchFilterStatus), dialog.findViewById(R.id.spinnerSearchFilterType),
            dialog.findViewById(R.id.spinnerSearchFilterSort))
        val radioGroupOrder = dialog.findViewById<RadioGroup>(R.id.radioGroupSearchFilterOrder)
        val buttonFilterAccept = dialog.findViewById<Button>(R.id.buttonFilterAccept)
        val filterGenreButton = dialog.findViewById<Button>(R.id.buttonFilterGenreButton)

        //Genre Dialog
        val genreDialog = Dialog(requireContext())
        genreDialog.setCanceledOnTouchOutside(false)
        genreDialog.setContentView(R.layout.dialog_genre_filter)

        //Get Resources Genre
        val genreList = genreDialog.findViewById<ListView>(R.id.listViewFilterGenre)
        var filterGenre = Array(stringGenreArray.size) {i -> 0}
        var buttonGenreAccept = genreDialog.findViewById<Button>(R.id.buttonFilterGenreAccept)

        //Open filter dialog
        searchFilter.setOnClickListener() {
            dialog.show()
        }

        //Open genre dialog
        filterGenreButton.setOnClickListener() {
            genreDialog.show()
        }


        //When genre item is clicked
        genreList.setOnItemClickListener() {
            parent, view, position, id ->

            //filterGenre is a int array with each element representing a genre
            //if the value is 0, the item is neutral
            //if the value is 1, the item should be filterd in and color the view green
            //if the value is 2, the item should be filtered out and color the view red
            //BUGGED: Bc the listview recycles and im setting the view color, it is reusing colored views for non selected items
            when(filterGenre[position]) {
                //Set genre to filter in, filter out, or neutral
                //change color accordingly
                0 -> {
                    view.setBackgroundColor(resources.getColor(R.color.filter_ok))
                    filterGenre[position] = 1
                }
                1 -> {
                    view.setBackgroundColor(resources.getColor(R.color.filter_no))
                    filterGenre[position] = 2
                }
                2 -> {
                    view.setBackgroundColor(resources.getColor(R.color.transparent))
                    filterGenre[position] = 0
                }
            }
        }

        buttonGenreAccept.setOnClickListener() {

            //Check the status of each genre filter
            //if 1 or 2, filter in or our respectfully
            for (i in 0..filterGenre.size-1) {
                when(filterGenre[i]) {
                    //Function to map genre string to mal_id is called for each
                    1-> filterSettings[0] = filterSettings[0]+getItemGenreID(stringGenreArray[i])+","
                    2-> filterSettings[1] = filterSettings[1]+getItemGenreID(stringGenreArray[i])+","
                }
            }

            //Dropping last comma
            filterSettings[0] = filterSettings[0].dropLast(1)
            filterSettings[1] = filterSettings[1].dropLast(1)
            genreDialog.dismiss()
        }

        val searchBar = binding.inputEditTextExploreSearch


        buttonFilterAccept.setOnClickListener() {

            //Return the string selected for status, type, and sort by
            for (i in 2..4) {
                filterSettings[i] = stringArrays[i-2][spinnerArray[i-2].selectedItemPosition].lowercase()
            }


            //Return order choice
            when(radioGroupOrder.indexOfChild(dialog.findViewById<RadioButton>(radioGroupOrder.checkedRadioButtonId))) {
                0-> filterSettings[5] = "desc"
                1-> filterSettings[5] = "asc"
            }

            Log.d("debug",": $filterSettings")
            dialog.dismiss()

            //TODO Hassan, this is the called function when the user confirms all their filters
            //There is a bug where other genres are being colored bc the listview is recyling the view
            //im still trying to fix it, but if you wanna take a crack go ahead
            /*Filters are stored in filterSettings (string array[6])
            index of:
            0 = genre filter in (ex: "1,2,6,4")
            1 = genre filter out (ex: "1,2,6,4")
            2 = status (ex: complete)
            3 = type (ex: tv)
            4 = sortby (ex: score)
            5 = order (either desc or asc)
            each element contains unique int index which corresponds with index of string.xml array
            note that genres is not mapped properly/does not contain all genres, will fix later*/
            getExploreAnime(
                query = if(searchBar.text.toString()!="") searchBar.text.toString() else null,
                status = if(filterSettings[2]!="status") filterSettings[2] else null,
                type = if(filterSettings[3]!="type") filterSettings[3] else null,
                orderBy = filterSettings[4],
                sort = if(filterSettings[5]!="default") filterSettings[5] else null,
            )
        }

        searchBar.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if(p1 == EditorInfo.IME_ACTION_SEARCH){
                    getExploreAnime(
                        query = if(searchBar.text.toString()!="") searchBar.text.toString() else null,
                        status = if(filterSettings[2]!="status") filterSettings[2] else null,
                        type = if(filterSettings[3]!="type") filterSettings[3] else null,
                        orderBy = filterSettings[4],
                        sort = if(filterSettings[5]!="default") filterSettings[5] else null,
                    )
                    return true
                }
                return false
            }

        })

    }
}

