package com.project24.animexapp

import kotlin.random.Random


private val genreMap = hashMapOf<String, Int>("Action" to 1, "Adventure" to 2, "Avant Garde" to 3, "Award Winning" to 46, "Comedy" to 4, "Drama" to 8, "Fantasy" to 10, "Gourmet" to 47, "Horror" to 14, "Mystery" to 7, "Romance" to 22, "Sci-Fi" to 24, "Slice of Life" to 36, "Sports" to 30, "Supernatural" to 37, "Suspense" to 41, "Childcare" to 53, "Combat Sports" to 54, "Delinquents" to 55, "Detective" to 39, "Educational" to 56, "Gag Humor" to 57, "Gore" to 58, "High Stakes Game" to 59, "Historical" to 13, "Idols (Female)" to 60, "Idols (Male)" to 61, "Isekai" to 62, "Iyashikei" to 63, "Love Polygon" to 64, "Mahou Shoujo" to 66, "Martial Arts" to 17, "Mecha" to 18, "Medical" to 67, "Military" to 38, "Music" to 19, "Mythology" to 6, "Organized Crime" to 68, "Otaku Culture" to 69, "Parody" to 20, "Performing Arts" to 70, "Pets" to 71, "Psychological" to 40, "Racing" to 3, "Reincarnation" to 72, "Romantic Subtext" to 74, "Samurai" to 21, "School" to 23, "Showbiz" to 75, "Space" to 29, "Strategy Game" to 11, "Super Power" to 31, "Survival" to 76, "Team Sports" to 77, "Time Travel" to 78, "Vampire" to 32, "Video Game" to 79, "Visual Arts" to 80, "Workplace" to 48, "Joesei" to 43, "Kids" to 15, "Seinen" to 42, "Shoujo" to 25, "Shounen" to 27)

object Genre_Map {

    fun getItemGenreID(item: String): Int {
        return genreMap[item]!!
    }

    fun getRandomGenrePair(): Pair<String,Int> {
        val random = Random(System.currentTimeMillis())
        val randEntry = genreMap.entries.elementAt(random.nextInt(genreMap.size))
        return randEntry.toPair()
        //val keys = genreMap.keys
        //val randKey = keys.random()
        //return Pair(randKey,genreMap[randKey]!!)
    }

}