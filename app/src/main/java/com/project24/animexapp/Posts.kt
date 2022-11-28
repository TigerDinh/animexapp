package com.project24.animexapp

data class Posts(
    val postText: String? = null,
    val postTag1: String? = null,
    val postTag2: String? = null,
    val postTag3: String? = null,
    val username: String? = null,
    val time: String? = null,
    val date: String? = null,
    val likes: Int? = 0,
    val commentsNum: Int? = 0,
    val postId: String? = null
) {}