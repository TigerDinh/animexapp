package com.project24.animexapp.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object QuotesApiClient {
    private val BASE_URL = "https://animechan.vercel.app/api/"

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

    val apiService: QuotesApiService by lazy {
        retrofit.create(QuotesApiService::class.java)
    }


}


interface QuotesApiService{

    // https://animechan.vercel.app/api/random
    @GET("random")
    fun randomQuote(): Call<QuotesResponse>

}