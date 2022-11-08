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
    val english_title: String?,

    @Json(name = "images")
    val imageData: AnimeImage?,

    @Json(name = "synopsis")
    val synopsis: String?,

    @Json(name = "score")
    val score: Double?,
    )

data class AnimeImage(
    @Json(name = "jpg")
    val jpg: Jpg?,
    @Json(name = "webp")
    val webp: Webp?
)

data class Jpg(
    @Json(name = "image_url")
    val URL: String,
    @Json(name = "small_image_url")
    val smallURL: String?,
    @Json(name = "large_image_url")
    val largeURL: String?
)

data class Webp(
    @Json(name = "image_url")
    val URL: String,
    @Json(name = "small_image_url")
    val smallURL: String?,
    @Json(name = "large_image_url")
    val largeURL: String?
)


data class AnimeSearchResponse(

    @Json(name = "data")
    val result: List<Anime>
)