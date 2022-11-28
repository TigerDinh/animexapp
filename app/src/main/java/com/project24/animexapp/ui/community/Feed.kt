package com.project24.animexapp.ui.community

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.*
import com.project24.animexapp.databinding.FragmentFeedBinding
import java.text.SimpleDateFormat
import java.util.*

class Feed : Fragment() {
    private var _binding: FragmentFeedBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private val binding get() = _binding!!
    private lateinit var postsList: List<Posts>
    private lateinit var postsRV: RecyclerView
    private lateinit var postsAdapter: PostAdapter
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val communityPostButton = binding.communityPostButton
        val communityUserPost = binding.communityUserPost.text.toString()
        val setTagsButton = binding.setTagsButton
        val linkAnimeButton = binding.linkAnimeButton

        communityPostButton.setOnClickListener {
            createPost()
        }
        setPostsAdapter()
        return root
    }

    private fun setPostsAdapter() {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore



        db.collection("Posts").orderBy("time", Query.Direction.DESCENDING).addSnapshotListener { snapshot, error ->
            if( error != null) {
                Log.e(TAG, "onEvent: ", error)
                return@addSnapshotListener
            }


            postsList = emptyList()
            postsRV = binding.postsRecycler
            postsAdapter = PostAdapter(postsList)
            postsRV.layoutManager = LinearLayoutManager (mContext?.applicationContext,
                LinearLayoutManager.VERTICAL, false
            )

            postsRV.adapter = postsAdapter

            for(doc in snapshot!!) {
                val postUser = doc.getString("username")
                val postText = doc.getString("postText")
                val postTime = doc.getString("time")
                val postDate = doc.getString("date")
                val postLikesCount = doc.getLong("likes")
                val postCommentsCount = doc.getLong("commentsNum")
                val postId = doc.getString("postId")
                postsList = postsList + Posts(postText, "", "", "", postUser, postTime, postDate, postLikesCount?.toInt(), postCommentsCount?.toInt(), postId)
                postsAdapter.postList = postsList
                postsAdapter.run {
                    notifyDataSetChanged()
                }
            }
//            Log.d(TAG, "Current names: $posts")
        }
    }

    private fun createPost() {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        postsRV.adapter = postsAdapter

        val currentUserID = firebaseAuth.currentUser?.uid
        val communityUserPost = binding.communityUserPost.text.toString()
        val userPost = binding.communityUserPost
        var currentUsername: String
        val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
        val currentTime = sdf.format(Date())
        val sdfDate = SimpleDateFormat("MM/dd/yyyy")
        val currentDate = sdfDate.format(Date())
        var postId: Int

        if(userPost.text.isEmpty()) {
                Toast.makeText(activity, "You need to write something before posting", Toast.LENGTH_SHORT).show()
        }
        else {
            val docRef = db.collection("Users").document(currentUserID!!)
            docRef.get().addOnSuccessListener { document ->
                if(document!=null) {
                    currentUsername = document.data?.get("username").toString()
                    db.collection("Posts").document(postsList.size.toString()).set(Posts(communityUserPost, "", "", "", currentUsername, currentTime, currentDate,0, 0, postsList.size.toString()))

                }
            }

        }
        userPost.text.clear()


    }



}
