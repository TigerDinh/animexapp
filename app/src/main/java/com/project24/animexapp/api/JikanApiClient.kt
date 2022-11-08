package com.project24.animexapp.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
    fun requestAnimeByStatus(@Query("status") status:String): Call<AnimeSearchResponse>

    //https://api.jikan.moe/v4/top/anime

    @GET("top/anime")
    fun topAnime(@Query("limit") limit:Int): Call<AnimeSearchResponse>


    @GET("anime")
    fun requestAnime(
        //Optional Params:
        @Query("q") query:String? = null,
        @Query("genre") genre:String? = null,
        @Query("status") status:String? = null,
        @Query("type") type:String? = null,
        @Query("min_score") minScore:Double? = null,
        @Query("limit") rating:Int? = null,
        @Query("order_by") orderBy:String? = null,
        //Defaulted Params:
        @Query("sfw") sfw:Boolean = true,
        @Query("limit") limit:Int = 10
    ) : Call<AnimeSearchResponse>


}