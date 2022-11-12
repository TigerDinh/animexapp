package com.project24.animexapp.ui.notifications

import com.project24.animexapp.R

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project24.animexapp.api.Anime
import com.project24.animexapp.api.AnimeSearchResponse
import com.project24.animexapp.api.JikanApiClient
import com.project24.animexapp.databinding.FragmentNotificationsBinding
import com.project24.animexapp.ui.home.AnimeRVAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private lateinit var animeList: List<Anime>
private lateinit var animeAnimeRV: RecyclerView
private lateinit var animeAnimeAdapter: AnimeRVAdapter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        animeList = emptyList()
        animeAnimeRV = binding.ExploreRV
        animeAnimeAdapter = AnimeRVAdapter(animeList)

        animeAnimeRV.layoutManager = GridLayoutManager(context, 2)
        //animeAnimeRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        animeAnimeRV.adapter = animeAnimeAdapter

        getAnime()



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getAnime(){
        val client = JikanApiClient.apiService.requestAnime()

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
}