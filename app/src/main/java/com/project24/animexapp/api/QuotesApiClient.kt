package com.project24.animexapp.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

object QuotesApiClient {
    private val BASE_URL = "https://animechan.vercel.app/api/"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            //.client(httpClient.build()) ---Don't currently need this level of logging
            .build()
    }

    val apiService: QuotesApiService by lazy {
        retrofit.create(QuotesApiService::class.java)
    }


}


interface QuotesApiService{

    // https://animechan.vercel.app/api/random
    @GET("random")
    fun randomQuote(): Call<QuotesResponse>

}