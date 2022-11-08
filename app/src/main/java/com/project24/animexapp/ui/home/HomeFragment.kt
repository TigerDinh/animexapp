package com.project24.animexapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.project24.animexapp.api.AnimeSearchResponse
import com.project24.animexapp.api.JikanApiClient
import com.project24.animexapp.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private var isLoggedIn: Boolean = false //Integrate with firebase value

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        if(isLoggedIn){
            //Logged In View
        }
        else{
            //Not Logged In View

            /*
            The client val is a placeholder right now to showcase the working of the api.
            The client will send a request for ongoing anime.
            Play around with the params to get different kinds of anime results.
            See the JikanApiService interface, at the JikanApiClient file for the options
            See the api docs to see the possible values for the params.
             */
            val client = JikanApiClient.apiService.requestAnime(status = "airing")

            client.enqueue(object: Callback<AnimeSearchResponse> {
                override fun onResponse(
                    call: Call<AnimeSearchResponse>,
                    response: Response<AnimeSearchResponse>
                ){
                    if(response.isSuccessful){
                        if(response.body() != null){
                            val ongoingList = response.body()!!.result
                            Log.d("anime",""+ongoingList.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<AnimeSearchResponse>, t: Throwable) {
                    Log.e("API FAIL",""+t.message)
                }
            })
        }




        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}