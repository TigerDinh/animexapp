package com.project24.animexapp.ui.community

import android.content.ContentValues
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.NotificationsData
import com.project24.animexapp.R
import java.text.SimpleDateFormat
import java.util.*

class ClubDetails : AppCompatActivity() {
    private var clubTitle: String? = " "
    private var clubLongDesc: String? = " "
    private var clubDate: String? = " "
    private var clubMemberCount: Long? = 0
    private var clubId: String? = " "
    private var clubType: String? = " "
    private var clubAccess: String? = " "
    private val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
    private val currentTime: String = sdf.format(Date())
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var clubPostsList: List<ClubPosts>
    private lateinit var clubPostsRV: RecyclerView
    private lateinit var clubPostsAdapter: ClubPostsAdapter


    private lateinit var adminsList: List<Admins>
    private lateinit var adminsRV: RecyclerView
    private lateinit var adminsAdapter: ClubAdminAdapter

    private lateinit var membersList: List<Admins>
    private lateinit var membersRV: RecyclerView
    private lateinit var membersAdapter: MembersAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        val db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUserId = firebaseAuth.currentUser?.uid


        if(extras!=null) {
            // Extract clubId from extras
            clubTitle = extras.getString("clubName", "No title")
            clubId = extras.getString("clubId", "0")
            clubAccess = extras.getString("clubAccess", "No access")

        }


        setContentView(R.layout.activity_club_details)
        val postButton = findViewById<Button>(R.id.clubPostButton)
        setAdminsAdapter()
        // real time update club information
        updateClubInfo()
        updateClubButtons()
        postButton.setOnClickListener {
            createPost()
        }

        setClubPostsAdapter()
        val askAdmin = findViewById<TextView>(R.id.clubDetailsAskAdminButton)
        db.collection("Clubs").document(clubId!!).collection("AdminRequest").document(currentUserId.toString()).addSnapshotListener {snapshot, error ->
            if(error != null) {
                Log.e(ContentValues.TAG, "onEvent: ", error)
                return@addSnapshotListener
            }

            if(snapshot != null) {
                if(snapshot.exists()) {
                    askAdmin.setBackgroundResource(R.drawable.button_requested)
                    askAdmin.text = "Requested to be admin"
                } else {
                    askAdmin.setBackgroundResource(R.drawable.button_unclicked)
                    askAdmin.text = "Ask to be admin"
                }
            }
        }
    }

    private fun updateClubButtons() {
        val db = Firebase.firestore
        val currentUserId = firebaseAuth.currentUser?.uid
        val clubJoinButton = findViewById<TextView>(R.id.clubDetailsJoinButton)
        val askAdmin = findViewById<TextView>(R.id.clubDetailsAskAdminButton)
        val docRef = db.collection("Users").document(currentUserId!!)
        var currentUsername: String
        val currentUserEmail = firebaseAuth.currentUser?.email
        val notifId = db.collection("Users").document(currentUserId.toString()).collection("Notifications").document().id

        db.collection("Clubs").document(clubId!!).collection("Members").document(currentUserId.toString()).addSnapshotListener { snapshot, error ->
            if(error != null) {
                Log.e(ContentValues.TAG, "onEvent: ", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // if user is a member
                if(snapshot.exists()) {
                    clubJoinButton.text = "Joined"
                    clubJoinButton.setTextColor(Color.parseColor("#FFFFFF"))
                    clubJoinButton.setBackgroundResource(R.drawable.button_joined)

                    db.collection("Clubs").document(clubId!!).collection("Admins").document(currentUserId.toString()).get().addOnSuccessListener { it ->
                        // if user is an admin
                        if(it.exists()) {
                            askAdmin.text = "You are an admin"
                            askAdmin.isClickable = false
                            askAdmin.setBackgroundResource(R.drawable.button_requested)
                        } else {
                        // send admin request notification to admins
                            askAdmin.setOnClickListener {
                                db.collection("Clubs").document(clubId!!).collection("AdminRequest").document(currentUserId).get().addOnSuccessListener {
                                    if(it.exists()) {
                                        askAdmin.text = "Ask to be admin"
                                        askAdmin.setBackgroundResource(R.drawable.button_unclicked)

                                        // delete user from admin request collection
                                        db.collection("Clubs").document(clubId!!).collection("AdminRequest").document(currentUserId).delete()

                                        // delete notification
                                        db.collection("Clubs").document(clubId!!).collection("Admins").get().addOnSuccessListener {
                                            for(doc in it) {
                                                db.collection("Users").document(doc.id).collection("Notifications").document(notifId).delete()
                                            }
                                        }
                                    } else {
                                        askAdmin.text = "Requested to be admin"
                                        askAdmin.setBackgroundResource(R.drawable.button_requested)
                                        db.collection("Clubs").document(clubId!!).collection("AdminRequest").document(currentUserId).set(ClubMembersData(" ", currentUserId, "member"))

                                        docRef.get().addOnSuccessListener { document ->
                                            if(document!=null) {
                                                currentUsername = document.data?.get("username").toString()
                                                db.collection("Clubs").document(clubId!!).collection("Admins").get().addOnSuccessListener {
                                                    for(doc in it) {
                                                        db.collection("Users").document(doc.id).collection("Notifications").document(notifId).set(
                                                            NotificationsData("adminRequest", currentUserEmail, currentUserId,"$currentUsername asked to be an admin of $clubTitle", notifId, currentUsername, clubId!!, currentTime)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    askAdmin.visibility = View.VISIBLE

                    // clicking on join button when already a member
                    clubJoinButton.setOnClickListener {
                        // delete user from members collection & decrement memberCount
                        db.collection("Clubs").document(clubId!!).collection("Members").document(currentUserId.toString()).get().addOnSuccessListener {
                            if (it["memberPrivileges"]?.equals("creator") == true) {
                                Toast.makeText(
                                    this,
                                    "You are the creator of this club.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                clubJoinButton.isClickable = false
                            } else {
                                db.collection("Clubs").document(clubId!!).collection("Members")
                                    .document(currentUserId.toString()).delete()
                                db.collection("Clubs").document(clubId!!)
                                    .update("clubMemberCount", FieldValue.increment(-1))
                            }
                        }
                    }
                } else {
                    // if user is in request collection
                    db.collection("Clubs").document(clubId!!).collection("Requests").document(currentUserId.toString()).addSnapshotListener { snapshot, error ->
                        if(error != null) {
                            Log.e(ContentValues.TAG, "onEvent: ", error)
                            return@addSnapshotListener
                        }
                        if(snapshot!=null) {
                            if(snapshot.exists()) {
                                clubJoinButton.text = "Requested"
                                clubJoinButton.setBackgroundResource(R.drawable.button_requested)
                                askAdmin.visibility = View.GONE
                            } else {

                                // if club is public
                                if(clubAccess == "anyone") {
                                    clubJoinButton.text = "Join"
                                    clubJoinButton.setTextColor(Color.parseColor("#FFFFFF"))
                                    clubJoinButton.setBackgroundResource(R.drawable.button_unclicked)
                                    askAdmin.visibility = View.GONE

                                    // on join button click on public
                                    clubJoinButton.setOnClickListener {
                                        docRef.get().addOnSuccessListener { document ->
                                            if(document!=null) {
                                                currentUsername = document.data?.get("username").toString()
                                                db.collection("Clubs").document(clubId!!).collection("Members").document(currentUserId.toString()).set(
                                                    ClubMembersData(currentUsername, currentUserId, "member")
                                                )
                                                db.collection("Clubs").document(clubId!!).update("clubMemberCount", FieldValue.increment(1))
                                            }
                                        }
                                    }
                                }

                                // if club is private
                                else if (clubAccess == "reqPerm") {
                                    clubJoinButton.text = "Request to join"
                                    clubJoinButton.setTextColor(Color.parseColor("#FFFFFF"))
                                    clubJoinButton.setBackgroundResource(R.drawable.button_unclicked)
                                    askAdmin.visibility = View.GONE
                                    // on button click on private
                                    clubJoinButton.setOnClickListener {
                                        // if user is in request collection
                                        db.collection("Clubs").document(clubId!!).collection("Requests").document(currentUserId.toString()).get().addOnSuccessListener {
                                            if(it.exists()) {
                                                clubJoinButton.text = "Request to join"
                                                clubJoinButton.setTextColor(Color.parseColor("#FFFFFF"))
                                                clubJoinButton.setBackgroundResource(R.drawable.button_unclicked)
                                                askAdmin.visibility = View.GONE
                                                db.collection("Clubs").document(clubId!!).collection("Requests").document(currentUserId.toString()).delete()

                                                // delete notification
                                                db.collection("Clubs").document(clubId!!).collection("Admins").get().addOnSuccessListener {
                                                    for(doc in it) {
                                                        db.collection("Users").document(doc.id).collection("Notifications").document(notifId).delete()
                                                    }
                                                }

                                            } else {
                                                clubJoinButton.text = "Requested"
                                                clubJoinButton.setBackgroundResource(R.drawable.button_requested)
                                                docRef.get().addOnSuccessListener { document ->
                                                    if(document!=null) {
                                                        currentUsername = document.data?.get("username").toString()
                                                        db.collection("Clubs").document(clubId!!).collection("Requests").document(currentUserId.toString()).set(
                                                            ClubMembersData(currentUsername, currentUserId, "member")
                                                        )
                                                    }
                                                }
                                                docRef.get().addOnSuccessListener { document ->
                                                    if(document!=null) {
                                                        currentUsername = document.data?.get("username").toString()
                                                        db.collection("Clubs").document(clubId!!).collection("Admins").get().addOnSuccessListener {
                                                            for(doc in it) {
                                                                db.collection("Users").document(doc.id).collection("Notifications").document(notifId).set(
                                                                    NotificationsData("joinRequest", currentUserEmail, currentUserId,"$currentUserEmail has requested to join $clubTitle", notifId, currentUsername, clubId, currentTime)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }




    private fun setAdminsAdapter() {
        val db = Firebase.firestore
        db.collection("Clubs").document(clubId!!).collection("Admins").addSnapshotListener { snapshot, error ->
            if(error != null) {
                Log.e(ContentValues.TAG, "onEvent: ", error)
                return@addSnapshotListener
            }

            adminsList = emptyList()
            adminsRV = findViewById(R.id.adminRecyclerView)
            adminsAdapter = ClubAdminAdapter(adminsList)
            adminsRV.layoutManager = GridLayoutManager(this.applicationContext, 4)
            adminsRV.adapter = adminsAdapter



            for(doc in snapshot!!) {
                val adminUsername = doc.getString("memberUsername")
                adminsList = adminsList + Admins(adminUsername)
                adminsAdapter.adminsList = adminsList
                adminsAdapter.run {
                    notifyDataSetChanged()
                }
            }
        }

        db.collection("Clubs").document(clubId!!).collection("Members").addSnapshotListener { snapshot, error ->
            if(error != null) {
                Log.e(ContentValues.TAG, "onEvent: ", error)
                return@addSnapshotListener
            }

            membersList = emptyList()
            membersRV = findViewById(R.id.allMembersRecyclerView)
            membersAdapter = MembersAdapter(membersList)
            membersRV.layoutManager = GridLayoutManager(this.applicationContext, 4)
            membersRV.adapter = membersAdapter

            for(doc in snapshot!!) {
                val adminUsername = doc.getString("memberUsername")
                membersList = membersList + Admins(adminUsername)
                membersAdapter.membersList = membersList
                membersAdapter.run {
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun updateClubInfo() {
        val db = Firebase.firestore
        db.collection("Clubs").document(clubId!!).addSnapshotListener { snapshot, error ->
            if(error != null) {
                Log.e(ContentValues.TAG, "onEvent: ", error)
                return@addSnapshotListener
            }


            val titleOfClub = findViewById<TextView>(R.id.clubDetailsTitle)
            val membersOfClub = findViewById<TextView>(R.id.clubDetailsCount)
            val dateOfClub = findViewById<TextView>(R.id.clubDetailsDate)
            val longDescOfClub = findViewById<TextView>(R.id.clubDetailsDesc)
            val typeOfClub = findViewById<TextView>(R.id.clubType)
            val membersText = findViewById<TextView>(R.id.membersText)
            clubTitle = snapshot?.getString("clubName")
            clubMemberCount = snapshot?.getLong("clubMemberCount")
            clubDate = snapshot?.getString("clubDate")
            clubLongDesc = snapshot?.getString("clubLongDesc")
            clubType = snapshot?.getString("clubAccess")
            titleOfClub.text = clubTitle
            membersOfClub.text = clubMemberCount.toString()
            dateOfClub.text = clubDate
            longDescOfClub.text = clubLongDesc
            if(clubMemberCount?.toInt()!! > 1) {
                membersText.text = "members"
            } else {
                membersText.text = "member"
            }

            if(clubType == "anyone") {
                typeOfClub.text = "public"
            } else if (clubType == "reqPerm") {
                typeOfClub.text = "private"
            }
        }

    }

    private fun setClubPostsAdapter() {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        db.collection("Clubs").document(clubId!!).collection("Posts").addSnapshotListener { snapshot, error ->
            if(error != null) {
                Log.e(ContentValues.TAG, "onEvent: ", error)
                return@addSnapshotListener
            }

        clubPostsList = emptyList()
        clubPostsRV = findViewById(R.id.clubPostsRecycler)
        clubPostsAdapter = ClubPostsAdapter(clubPostsList)
        clubPostsRV.layoutManager = LinearLayoutManager(this.applicationContext,
            LinearLayoutManager.VERTICAL, false
        )

        clubPostsRV.adapter = clubPostsAdapter


            for(doc in snapshot!!) {
                val clubPostsUser = doc.getString("username")
                val clubPostsText = doc.getString("postText")
                val clubPostsTime = doc.getString("time")
                val clubPostsDate = doc.getString("date")
                val clubPostsLikesCount = doc.getLong("likes")
                val clubPostsCommentsCount = doc.getLong("commentsNum")
                val clubPostsId = doc.getString("postId")
                val clubId = doc.getString("clubId")

                clubPostsList = clubPostsList + ClubPosts(clubPostsText, clubPostsUser, clubPostsTime, clubPostsDate, clubPostsLikesCount?.toInt(), clubPostsCommentsCount?.toInt(), clubPostsId, clubId)
                clubPostsAdapter.postList = clubPostsList
                clubPostsAdapter.run {
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun createPost() {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        clubPostsRV.adapter = clubPostsAdapter

        val currentUserID = firebaseAuth.currentUser?.uid
        val clubsUserPost = findViewById<EditText>(R.id.clubUserPost).text.toString()
        val userPost = findViewById<EditText>(R.id.clubUserPost)
        var currentUsername: String
        val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
        val currentTime = sdf.format(Date())
        val sdfDate = SimpleDateFormat("MM/dd/yyyy")
        val currentDate = sdfDate.format(Date())
        val postId = db.collection("Clubs").document(clubId!!).collection("Posts").document().id


        if(userPost.text.isEmpty()) {
            Toast.makeText(this, "You need to write something before posting", Toast.LENGTH_SHORT).show()
        }
        else {
            val docRef = db.collection("Users").document(currentUserID!!)
            docRef.get().addOnSuccessListener { document ->
                if(document!=null) {
                    currentUsername = document.data?.get("username").toString()
                    db.collection("Clubs").document(clubId!!).collection("Posts").document(postId).set(
                        ClubPosts(clubsUserPost, currentUsername, currentTime, currentDate,0, 0, postId, clubId)
                    )

                }
            }

        }
        userPost.text.clear()
    }
}