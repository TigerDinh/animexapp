package com.project24.animexapp.ui.community

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.NotificationsData
import com.project24.animexapp.R
import java.text.SimpleDateFormat
import java.util.*

class ClubsAdapter(var clubsList: List<ClubsData>): RecyclerView.Adapter<ClubsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.club_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return clubsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clubItem = clubsList[position]
        holder.bindClub(clubItem)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var view: View = itemView
        private lateinit var clubs: ClubsData
        var joinButton: LinearLayout
        var context: Context = itemView.context
        val db = Firebase.firestore
        var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        private val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
        private val currentTime: String = sdf.format(Date())
        val currentUserId = firebaseAuth.currentUser?.uid

        init {
            view.setOnClickListener(this)
            val currentUserId = firebaseAuth.currentUser?.uid
            val currentUserEmail = firebaseAuth.currentUser?.email
            joinButton = itemView.findViewById(R.id.joinLL)
            val notifId = db.collection("Users").document(currentUserId.toString()).collection("Notifications").document().id
            var currentUsername: String

            joinButton.setOnClickListener {
                if(currentUserId == null ) {
                    Toast.makeText(context, "You need to be logged in to do that", Toast.LENGTH_SHORT).show()
                } else {
                    if(clubs.clubAccess == "anyone") {
                        // check if user already a member of club
                        db.collection("Clubs").document(clubs.clubId.toString()).collection("Members").document(currentUserId.toString()).get().addOnSuccessListener { it ->
                            // if user is already a member
                            if(it.exists()) {
                                // check if user is the creator. if they are, then toast "you are creator". else, decrement club member count, delete user from member collection
                                db.collection("Clubs").document(clubs.clubId.toString()).collection("Members").document(currentUserId.toString()).get().addOnSuccessListener {
                                    if(it["memberPrivileges"]?.equals("creator") == true) {
                                        Toast.makeText(context, "You are the creator of this club.", Toast.LENGTH_SHORT).show()
                                        view.findViewById<TextView>(R.id.joinButton).isClickable = false
                                    } else {
                                        joinButton.setBackgroundColor(Color.parseColor("#673AB7"))
                                        view.findViewById<TextView>(R.id.joinButton).text = "Join"
                                        db.collection("Clubs").document(clubs.clubId.toString())
                                            .update("clubMemberCount", FieldValue.increment(-1))
                                        db.collection("Clubs").document(clubs.clubId.toString()).collection("Members").document(currentUserId.toString()).delete()
                                        db.collection("Users").document(currentUserId.toString()).collection("Clubs").document(clubs.clubId.toString()).delete()
                                    }
                                }

                                // if user is not member of club, add to member collection
                            } else {
                                joinButton.setBackgroundColor(Color.parseColor("#32cd32"))

                                db.collection("Users").document(currentUserId!!).get().addOnSuccessListener { document ->
                                    if(document!=null) {
                                        currentUsername = document.data?.get("username").toString()
                                        db.collection("Clubs").document(clubs.clubId.toString()).collection("Members").document(currentUserId.toString()).set(
                                            ClubMembersData(currentUsername, currentUserId, "member")
                                        )
                                    }
                                }

                                db.collection("Users").document(currentUserId.toString()).collection("Clubs").document(clubs.clubId.toString()).set(
                                    ClubsData(clubs.clubName, clubs.clubShortDesc, clubs.clubLongDesc, clubs.clubAccess, clubs.clubId, clubs.clubMemberCount)
                                )
                                db.collection("Clubs").document(clubs.clubId.toString()).update("clubMemberCount", FieldValue.increment(1))
                            }
                        }
                    }
                    // if club access requires permission
                    else if(clubs.clubAccess == "reqPerm") {
                        db.collection("Clubs").document(clubs.clubId.toString()).collection("Requests").document(currentUserId.toString()).get().addOnSuccessListener { it ->

                            // if user is in the request collection
                            if(it.exists()) {
                                joinButton.setBackgroundColor(Color.parseColor("#673AB7"))
                                view.findViewById<TextView>(R.id.joinButton).text = "Request to join"
                                db.collection("Clubs").document(clubs.clubId.toString()).collection("Requests").document(currentUserId.toString()).delete()

                                // delete notification
                                db.collection("Clubs").document(clubs.clubId.toString()).collection("Admins").get().addOnSuccessListener {
                                    for(doc in it) {
                                        db.collection("Users").document(doc.id).collection("Notifications").document(notifId).delete()
                                    }
                                }

                            } else {

                                // if user is a member of club and is not in request collection
                                db.collection("Clubs").document(clubs.clubId.toString()).collection("Members").document(currentUserId.toString()).get().addOnSuccessListener { it ->
                                    if (it.exists()) {
                                        db.collection("Clubs").document(clubs.clubId.toString()).collection("Members").document(currentUserId.toString()).get().addOnSuccessListener {
                                            if(it["memberPrivileges"]?.equals("creator") == true) {
                                                Toast.makeText(context, "You are the creator of this club.", Toast.LENGTH_SHORT).show()
                                                view.findViewById<TextView>(R.id.joinButton).isClickable = false
                                            } else {
                                                joinButton.setBackgroundColor(Color.parseColor("#673AB7"))
                                                view.findViewById<TextView>(R.id.joinButton).text = "Request to join"
                                                db.collection("Clubs").document(clubs.clubId.toString()).collection("Members").document(currentUserId.toString()).delete()
                                                db.collection("Clubs").document(clubs.clubId.toString()).update("clubMemberCount", FieldValue.increment(-1))
                                            }
                                        }

                                    } else {

                                        // For each admin of the club, add to request notifications to notification collection in the user database
                                        db.collection("Users").document(currentUserId!!).get().addOnSuccessListener { document ->
                                            if(document!=null) {
                                                currentUsername = document.data?.get("username").toString()
                                                db.collection("Clubs").document(clubs.clubId.toString()).collection("Admins").get().addOnSuccessListener {
                                                    for(doc in it) {
                                                        db.collection("Users").document(doc.id).collection("Notifications").document(notifId).set(
                                                            NotificationsData("joinRequest", currentUserEmail, currentUserId,"$currentUsername has requested to join ${clubs.clubName}", notifId, currentUsername, clubs.clubId, currentTime)
                                                        )
                                                    }
                                                }
                                            }
                                        }


                                        view.findViewById<TextView>(R.id.joinButton).text = "Requested"
                                        joinButton.setBackgroundColor(Color.parseColor("#c9c9c9"))

                                        // Add user to clubs request collection
                                        db.collection("Users").document(currentUserId!!).get().addOnSuccessListener { document ->
                                            if(document!=null) {
                                                currentUsername = document.data?.get("username").toString()
                                                db.collection("Clubs").document(clubs.clubId.toString()).collection("Requests").document(currentUserId.toString()).set(
                                                    ClubMembersData(currentUsername, currentUserId, "member")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // if club can be joined by anyone
            }
        }

        fun bindClub(clubs: ClubsData) {
            this.clubs = clubs
            joinButton = itemView.findViewById(R.id.joinLL)
            val currentUserId = firebaseAuth.currentUser?.uid

            view.findViewById<TextView>(R.id.clubNameAdapter).text = clubs.clubName
            view.findViewById<TextView>(R.id.clubShortDescAdapter).text = clubs.clubShortDesc
            view.findViewById<TextView>(R.id.clubMemberCountAdapter).text = clubs.clubMemberCount.toString() + " "

            if(clubs.clubAccess == "anyone") {
                db.collection("Clubs").document(clubs.clubId.toString()).collection("Members").document(currentUserId.toString()).get().addOnSuccessListener {
                    if (it.exists()) {
                        view.findViewById<TextView>(R.id.joinButton).text = "Joined"
                        joinButton.setBackgroundColor(Color.parseColor("#32cd32"))
                    } else {
                        view.findViewById<TextView>(R.id.joinButton).text = "Join"
                    }
                }
            }
            else if(clubs.clubAccess == "reqPerm") {
                db.collection("Clubs").document(clubs.clubId.toString()).collection("Requests").document(currentUserId.toString()).get().addOnSuccessListener { it ->
                    if (it.exists()) {
                        view.findViewById<TextView>(R.id.joinButton).text = "Requested"
                        joinButton.setBackgroundColor(Color.parseColor("#c9c9c9"))
                    } else {
                        db.collection("Clubs").document(clubs.clubId.toString()).collection("Members").document(currentUserId.toString()).get().addOnSuccessListener {
                            if (it.exists()) {
                                view.findViewById<TextView>(R.id.joinButton).text = "Joined"
                                joinButton.setBackgroundColor(Color.parseColor("#32cd32"))
                            } else {
                                view.findViewById<TextView>(R.id.joinButton).text = "Request to join"
                            }
                        }
                    }
                }
            }
        }

        override fun onClick(v: View) {
            val context = itemView.context
            val showClubDetailsIntent = Intent(context, ClubDetails::class.java)
            showClubDetailsIntent.putExtra("clubName", clubs.clubName.toString())
            showClubDetailsIntent.putExtra("clubMemberCount", clubs.clubMemberCount.toString())
            showClubDetailsIntent.putExtra("clubLongDesc", clubs.clubLongDesc.toString())
            showClubDetailsIntent.putExtra("clubDate", clubs.clubDate.toString())
            showClubDetailsIntent.putExtra("clubId", clubs.clubId.toString())
            showClubDetailsIntent.putExtra("clubAccess", clubs.clubAccess.toString())
            context.startActivity(showClubDetailsIntent)
//            startLoadingActivity(itemView.context) // Activities are placed in "First In Last Out" stack
        }
    }
}