package com.project24.animexapp.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

object KitsuApiClient {
    private val BASE_URL = "https://kitsu.io/api/edge/"

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

    val apiService: KitsuApiService by lazy {
        retrofit.create(KitsuApiService::class.java)
    }


}


interface KitsuApiService{

    // https://kitsu.io/api/edge/trending/anime
    @GET("trending/anime")
    fun trendingAnime(): Call<AnimeTrendingResponse>

}