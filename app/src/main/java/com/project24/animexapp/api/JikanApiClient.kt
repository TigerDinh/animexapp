package com.project24.animexapp.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

object JikanApiClient {
    private val BASE_URL = "https://api.jikan.moe/v4/"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apiService: JikanApiService by lazy {
        retrofit.create(JikanApiService::class.java)
    }


}


interface JikanApiService{

    @GET("anime")
    fun requestAnime(@Query("status") status:String): Call<AnimeSearchResponse>

    //https://api.jikan.moe/v4/top/anime

    @GET("top/anime")
    fun topAnime(@Query("limit") limit:Int): Call<AnimeSearchResponse>

}