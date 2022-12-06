package com.project24.animexapp.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object KitsuApiClient {
    private val BASE_URL = "https://kitsu.io/api/edge/"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            //.client(httpClient.build()) ---Don't currently need this level of logging
            .build()
    }

    val apiService: KitsuApiService by lazy {
        retrofit.create(KitsuApiService::class.java)
    }


}


interface KitsuApiService{

    // https://kitsu.io/api/edge/trending/anime
    @GET("trending/anime")
    fun trendingAnime(): Call<KitsuAnimeResponse>

    //https://kitsu.io/api/edge/anime
    //https://kitsu.io/api/edge/anime?filter[seasonYear]=2022&filter[season]=fall&sort=-averageRating
    @GET("anime")
    fun requestAnime(
        @Query("filter[seasonYear]") year:String? = null, //Year of the Anime. If multiple go for eg: 2015..2018
        @Query("filter[season]") season:String? = null, //Season of the Anime
        @Query("sort") sort:String? = null, //Sort preference as a comma seperated list if multiple. eg sort=-averageRating. The minus makes it desc.
        @Query("categories") categories:String? = null, //Sort preference as a comma seperated list if multiple
    )    : Call<KitsuAnimeResponse>

}