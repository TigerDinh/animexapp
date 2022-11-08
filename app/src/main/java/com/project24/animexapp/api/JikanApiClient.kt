package com.project24.animexapp.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

object JikanApiClient {
    private val BASE_URL = "https://api.jikan.moe/v4/"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    /* --- Don't currently need this level of logging.

    private val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BODY)

    val httpClient = OkHttpClient.Builder()
    httpClient.addInterceptor(logging)
     */

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            //.client(httpClient.build()) ---Don't currently need this level of logging
            .build()
    }

    val apiService: JikanApiService by lazy {
        retrofit.create(JikanApiService::class.java)
    }


}


interface JikanApiService{

    //https://api.jikan.moe/v4/top/anime

    @GET("top/anime")
    fun topAnime(@Query("limit") limit:Int): Call<AnimeSearchResponse>


    //https://api.jikan.moe/v4/anime

    @GET("anime")
    fun requestAnime(
        //Optional Params:
        @Query("q") query:String? = null, //search entry
        @Query("genre") genre:String? = null, //preferred genre
        @Query("status") status:String? = null, //Options: "airing", "complete", "upcoming"
        @Query("type") type:String? = null, //Options: "tv" "movie" "ova" "special" "ona" "music"
        @Query("min_score") minScore:Double? = null, //minimum score of returned anime
        @Query("rating") rating:Int? = null, //Options: "g" "pg" "pg13" "r17" "r" "rx"
        @Query("order_by") orderBy:String? = null, //Options: "mal_id", "title", "type", "rating", "start_date", "end_date", "episodes", "score", "scored_by", "rank", "popularity", "members", "favorites"
        //Defaulted Params:
        @Query("sfw") sfw:Boolean = true, //Don't want inappropriate stuff
        @Query("limit") limit:Int = 3 //switch to null when testing function calls in depth.
    ) : Call<AnimeSearchResponse>


    //https://api.jikan.moe/v4/users/{username}/favorites
    //INCOMPLETE FUNCTION DO NOT USE
    @GET("users/{username}/favorites")
    fun requestUserFavourites(
        @Path("username") username:String,
    ) : Call<UserFavouritesResponse>


}