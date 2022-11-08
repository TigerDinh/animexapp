package com.project24.animexapp.api

import com.squareup.moshi.Json

data class Anime(

    @Json(name = "mal_id")
    val mal_id: Long,

    @Json(name = "url")
    val url: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "type")
    val type: String,

    @Json(name="title_english")
    val english_title: String?

    )

data class AnimeSearchResponse(

    @Json(name = "data")
    val result: List<Anime>
)