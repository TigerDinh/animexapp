package com.project24.animexapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.ui.community.ClubMembersData


class NotificationsAdapter(var notifsList : List<NotificationsData>): RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent:ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notifsList.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val notifsItem = notifsList[position]
        holder.bindNotifs(notifsItem)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private var view: View = itemView
        private lateinit var notifs: NotificationsData
        var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        var allowButton: TextView
        var clearButton: ImageView
        var denyButton: TextView

        init {
            var currentUserId = firebaseAuth.currentUser?.uid
            allowButton = itemView.findViewById(R.id.allowButton)
            clearButton = itemView.findViewById(R.id.clearNotification)
            denyButton = itemView.findViewById(R.id.denyButton)
            var currentUsername: String
            val docRef = db.collection("Users").document(currentUserId!!)

            // When allow button is clicked in notification:
            // remove requesting user from request collection
            // add user to member collection for club
            // Increment member count for club
            // Delete notification


            allowButton.setOnClickListener {

                if(notifs.notificationType == "joinRequest") {
                    // delete requesting user from request collection
                    db.collection("Clubs").document(notifs.clubId.toString()).collection("Requests").document(notifs.requesterId.toString()).delete()

                    // add user to member collection
                    db.collection("Clubs").document(notifs.clubId.toString()).collection("Members").document(notifs.requesterId.toString()).set(
                        ClubMembersData(notifs.requesterUsername, notifs.requester, "member")
                    )

                    // increment club count
                    db.collection("Clubs").document(notifs.clubId.toString()).update("clubMemberCount", FieldValue.increment(1))

                    // delete notification
                    db.collection("Users").document(currentUserId.toString()).collection("Notifications").document(notifs.notificationId.toString()).delete()

                } else if (notifs.notificationType == "adminRequest") {
                    db.collection("Clubs").document(notifs.clubId.toString()).collection("AdminRequest").document(notifs.requesterId.toString()).delete()
                    db.collection("Clubs").document(notifs.clubId.toString()).collection("Members").document(notifs.requesterId.toString()).set(
                        ClubMembersData(notifs.requesterUsername, notifs.requester, "member")
                    )
                    db.collection("Clubs").document(notifs.clubId.toString()).collection("Admins").document(notifs.requesterId.toString()).set(
                        ClubMembersData(notifs.requesterUsername, notifs.requester, "member")
                    )
                    db.collection("Users").document(currentUserId.toString()).collection("Notifications").document(notifs.notificationId.toString()).delete()
                }

            }

            clearButton.setOnClickListener {
                db.collection("Users").document(currentUserId.toString()).collection("Notifications").document(notifs.notificationId.toString()).delete()
            }

            denyButton.setOnClickListener {
                if(notifs.notificationType == "joinRequest") {
                    db.collection("Clubs").document(notifs.clubId.toString()).collection("Requests").document(notifs.requesterId.toString()).delete()
                    db.collection("Users").document(currentUserId.toString()).collection("Notifications").document(notifs.notificationId.toString()).delete()
                } else if (notifs.notificationType == "adminRequest") {
                    db.collection("Clubs").document(notifs.clubId.toString()).collection("AdminRequest").document(notifs.requesterId.toString()).delete()
                    db.collection("Users").document(currentUserId.toString()).collection("Notifications").document(notifs.notificationId.toString()).delete()
                }
            }
        }

        fun bindNotifs(notifs: NotificationsData) {
            this.notifs = notifs
            view.findViewById<TextView>(R.id.notificationText).text = notifs.notificationTitle
        }




    }

}