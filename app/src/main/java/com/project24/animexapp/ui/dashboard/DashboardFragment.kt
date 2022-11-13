package com.project24.animexapp.ui.dashboard

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project24.animexapp.R
import com.project24.animexapp.api.Anime
import com.project24.animexapp.api.AnimeSearchResponse
import com.project24.animexapp.api.JikanApiClient
import com.project24.animexapp.databinding.FragmentDashboardBinding
import com.project24.animexapp.ui.home.AnimeRVAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var animeList: List<Anime>
private lateinit var animeAnimeRV: RecyclerView
private lateinit var animeAnimeAdapter: AnimeRVAdapter
private var filterSettings = arrayListOf(0, 0, 0, 0, 0)

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


        animeList = emptyList()
        animeAnimeRV = binding.ExploreRV
        animeAnimeAdapter = AnimeRVAdapter(animeList)

        animeAnimeRV.layoutManager = GridLayoutManager(context, 4)
        //animeAnimeRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        animeAnimeRV.adapter = animeAnimeAdapter

        getAnime()

        getFilter()



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getAnime(){
        val client = JikanApiClient.apiService.requestAnime(limit = 50)

        client.enqueue(object: Callback<AnimeSearchResponse> {
            override fun onResponse(
                call: Call<AnimeSearchResponse>,
                response: Response<AnimeSearchResponse>
            ){
                if(response.isSuccessful){
                    if(response.body() != null){
                        animeList = response.body()!!.result

                        //PASS THE LIST TO THE ADAPTER AND REFRESH IT

                        animeAnimeAdapter.animeList = animeList
                        animeAnimeAdapter.notifyDataSetChanged()


                        //Log.d("ONGOING ANIME",""+ongoingList.toString())
                    }
                }
            }
            override fun onFailure(call: Call<AnimeSearchResponse>, t: Throwable) {
                Log.e("EXPLORE ANIME API FAIL",""+t.message)
            }
        })
    }

    fun getFilter(){
        val searchFilter = binding.buttonExploreFilter
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_search_filter)
        val spinnerArray = arrayListOf<Spinner>(dialog.findViewById(R.id.spinnerSearchFilterGenre),
            dialog.findViewById(R.id.spinnerSearchFilterStatus), dialog.findViewById(R.id.spinnerSearchFilterType),
            dialog.findViewById(R.id.spinnerSearchFilterSort))

        val radioGroupOrder = dialog.findViewById<RadioGroup>(R.id.radioGroupSearchFilterOrder)

        val buttonFilterAccept = dialog.findViewById<Button>(R.id.buttonFilterAccept)

        searchFilter.setOnClickListener() {
            dialog.show()
        }


        buttonFilterAccept.setOnClickListener() {
            for (i in 0..3) {
                filterSettings[i] = spinnerArray[i].selectedItemPosition
            }

            filterSettings[4] = radioGroupOrder.indexOfChild(dialog.findViewById<RadioButton>(radioGroupOrder.checkedRadioButtonId))

            //println("debug: $filterSettings")
            dialog.dismiss()
            //TODO this space is the moment after the filter parameters are set
            /*Filters are stored in filterSettings (int array[5])
            index of:
            0 = genre
            1 = status
            2 = type
            3 = sort by
            4 = 0 is list in descending, 1 is list in ascending
            each element contains unique int index which coresponds with index of string.xml array
            note that genres is not mapped properly/does not contain all genres, will fix later*/
        }
    }
}
