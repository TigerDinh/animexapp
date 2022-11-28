package com.project24.animexapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.api.LocalAnime
import com.project24.animexapp.ui.community.Feed
import com.project24.animexapp.ui.home.AnimeRVAdapter


class PostAdapter(var postList : List<Posts>): RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateViewHolder(parent:ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }
    override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int) {

        val postItem = postList[position]
        holder.bindAnime(postItem)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private var view: View = itemView
        private lateinit var postItem: Posts
        var button: ImageView
        var context: Context = itemView.context
        val db = Firebase.firestore
        var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

        init {
            val thumbsClicked: Drawable? = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_baseline_thumb_up_alt_24,
                null
            )
            val thumbsUnClicked: Drawable? = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_baseline_thumb_up_off_alt_24,
                null
            )
            val currentUserId = firebaseAuth.currentUser?.uid
            button = itemView.findViewById(R.id.thumbsUpButton)



            button.setOnClickListener {
                db.collection("Posts").document(postItem.postId.toString()).collection("LikedBy").document(currentUserId.toString()).get().addOnSuccessListener {
                    if(it.exists()) {
                        button.setImageDrawable(thumbsUnClicked)
                        db.collection("Posts").document(postItem.postId.toString())
                            .update("likes", FieldValue.increment(-1))
                        db.collection("Posts").document(postItem.postId.toString()).collection("LikedBy").document(currentUserId.toString()).delete()
                    } else {
                        button.setImageDrawable(thumbsClicked)
                        db.collection("Posts").document(postItem.postId.toString()).collection("LikedBy")
                            .document(currentUserId.toString()).set(
                                Posts(
                                    postItem.postText,
                                    "",
                                    "",
                                    "",
                                    postItem.username,
                                    postItem.time,
                                    postItem.date,
                                    postItem.likes,
                                    postItem.commentsNum,
                                    postItem.postId.toString()
                                )
                            )
                        db.collection("Posts").document(postItem.postId.toString())
                            .update("likes", FieldValue.increment(1))
                    }
                }
            }
        }

        fun bindAnime(postItem: Posts) {
            val currentUserId = firebaseAuth.currentUser?.uid
            button = itemView.findViewById(R.id.thumbsUpButton)
            this.postItem = postItem
            val thumbsClicked: Drawable? = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_baseline_thumb_up_alt_24,
                null
            )
            val thumbsUnClicked: Drawable? = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_baseline_thumb_up_off_alt_24,
                null
            )

            db.collection("Posts").document(postItem.postId.toString()).collection("LikedBy").document(currentUserId.toString()).get().addOnSuccessListener {
                if (it.exists()) {
                    button.setImageDrawable(thumbsClicked)
                } else {
                    button.setImageDrawable(thumbsUnClicked)
                }
            }
            view.findViewById<TextView>(R.id.postText).text = postItem.postText
            view.findViewById<TextView>(R.id.postUsername).text = postItem.username
            view.findViewById<TextView>(R.id.postLikesCount).text = postItem.likes.toString()
            view.findViewById<TextView>(R.id.postCommentsCount).text = postItem.commentsNum.toString()
            view.findViewById<TextView>(R.id.postDate).text = postItem.date.toString()
        }

    }

}