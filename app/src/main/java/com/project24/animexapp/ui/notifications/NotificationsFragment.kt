package com.project24.animexapp.ui.notifications

import android.app.DownloadManager
import android.content.ContentValues
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
import com.project24.animexapp.NotificationsAdapter
import com.project24.animexapp.NotificationsData
import com.project24.animexapp.PostAdapter
import com.project24.animexapp.Posts
import com.project24.animexapp.databinding.FragmentNotificationsBinding
import com.project24.animexapp.databinding.FragmentPlaylistsBinding

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var notifsList: List<NotificationsData>
    private lateinit var notifsRV: RecyclerView
    private lateinit var notifsAdapter: NotificationsAdapter
    private val binding get() = _binding!!
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setNotifsAdapter()

        return root
    }

    private fun setNotifsAdapter() {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val currentUserId = firebaseAuth.currentUser?.uid
        val noNotifications = binding.noNotifications

        db.collection("Users").document(currentUserId.toString()).collection("Notifications").orderBy("time", Query.Direction.DESCENDING).addSnapshotListener {snapshot, error ->
            if( error != null) {
                Log.e(ContentValues.TAG, "onEvent: ", error)
                return@addSnapshotListener
            }

            notifsList = emptyList()
            notifsRV = binding.notifsRecycler
            notifsAdapter = NotificationsAdapter(notifsList)
            notifsRV.layoutManager = LinearLayoutManager (mContext?.applicationContext,
                LinearLayoutManager.VERTICAL, false
            )

            notifsRV.adapter = notifsAdapter

            if (snapshot != null) {
                if(snapshot.isEmpty) {
                    noNotifications.visibility = View.VISIBLE
                } else {
                    noNotifications.visibility = View.GONE
                }
            }
            for (doc in snapshot!!) {

                val notifsTitle = doc.getString("notificationTitle")
                val requesterId = doc.getString("requesterId")
                val requester = doc.getString("requester")
                val time = doc.getString("time")
                val clubId = doc.getString("clubId")
                val notifId = doc.getString("notificationId")
                val requesterUsername = doc.getString("requesterUsername")
                val notificationType = doc.getString("notificationType")

                notifsList = notifsList + NotificationsData(notificationType, requester, requesterId, notifsTitle, notifId, requesterUsername, clubId, time)
                notifsAdapter.notifsList = notifsList
                notifsAdapter.run {
                    notifyDataSetChanged()
                }
            }
        }
    }
}