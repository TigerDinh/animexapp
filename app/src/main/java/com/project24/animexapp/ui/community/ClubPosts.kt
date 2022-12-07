package com.project24.animexapp.ui.community

data class ClubPosts(
    val postText: String? = null,
    val username: String? = null,
    val time: String? = null,
    val date: String? = null,
    val likes: Int? = 0,
    val commentsNum: Int? = 0,
    val postId: String? = null,
    val clubId: String? = null
) {}