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
    val type: String?,

    @Json(name="title_english")
    val englishTitle: String?,

    @Json(name="title_japanese")
    val japaneseTitle: String?,

    @Json(name = "images")
    val imageData: AnimeImage?,

    @Json(name = "synopsis")
    val synopsis: String?,

    @Json(name = "score")
    val score: Double?,

    @Json(name = "trailer")
    val trailerData: TrailerData?,
    )

data class TrailerData(
    @Json(name = "youtube_id")
    val youtubeID: String?,

    @Json(name = "url")
    val youtubeURL: String?,

    @Json(name = "embed_url")
    val youtubeEmbedURL: String?,
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

data class Character(
    @Json(name = "character")
    val characterData : CharacterData
)

data class CharacterData(
    @Json(name = "images")
    val imageData : AnimeImage,

    @Json(name = "name")
    val characterName : String,
)

data class AnimeSearchResponse(

    @Json(name = "data")
    val result: List<Anime>
)

data class UserFavouriteAnime(
    @Json(name = "anime")
    val result: List<Anime>
)

data class UserFavouritesResponse(
    @Json(name = "data")
    val result: UserFavouriteAnime
)
data class AnimeEntry(
    @Json(name = "entry")
    val animeData: Anime
)

data class RecommendationsByIDResponse(
    @Json(name = "data")
    val result: List<AnimeEntry>,
)

data class AnimeSearchByIDResponse(
    @Json(name = "data")
    val animeData : Anime,
)

data class AnimeCharacterSearchResponse(
    @Json(name = "data")
    val animeData : List<Character>,
)
