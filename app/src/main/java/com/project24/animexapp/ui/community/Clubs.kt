package com.project24.animexapp.ui.community

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.*
import com.project24.animexapp.databinding.FragmentClubsBinding
import java.text.SimpleDateFormat
import java.util.*

class Clubs : Fragment() {
    private var _binding: FragmentClubsBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var clubsList: List<ClubsData>
    private lateinit var clubsRV: RecyclerView
    private lateinit var clubsAdapter: ClubsAdapter
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClubsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        var createClubButton = binding.createClubButton
        createClubButton.setOnClickListener {
            openCreateClubDialog()
        }
        setClubsAdapter()

        return root
    }

    private fun setClubsAdapter() {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        db.collection("Clubs").addSnapshotListener { snapshot, error ->
            if(error != null) {
                Log.e(TAG, "onEvent: ", error)
                return@addSnapshotListener
            }

            clubsList = emptyList()
            clubsRV = binding.clubsRecycler
            clubsAdapter = ClubsAdapter(clubsList)
            clubsRV.layoutManager = LinearLayoutManager(mContext?.applicationContext,
                LinearLayoutManager.VERTICAL, false
            )

            clubsRV.adapter = clubsAdapter

            for(doc in snapshot!!) {
                val clubName = doc.getString("clubName")
                val clubShortDesc = doc.getString("clubShortDesc")
                val clubLongDesc = doc.getString("clubLongDesc")
                val clubAccess = doc.getString("clubAccess")
                val clubId = doc.getString("clubId")
                val clubMemberCount = doc.getLong("clubMemberCount")
                val clubDate = doc.getString("clubDate")
                val clubTime = doc.getString("clubTime")

                clubsList = clubsList + ClubsData(clubName, clubShortDesc, clubLongDesc, clubAccess, clubId, clubMemberCount?.toInt(), clubDate, clubTime)
                clubsAdapter.clubsList = clubsList
                clubsAdapter.run {
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun openCreateClubDialog() {
        val clubDialog = Dialog(this.requireContext())
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val currentUserID = firebaseAuth.currentUser?.uid
        val clubId = db.collection("Clubs").document().id
        clubDialog.setContentView(R.layout.dialog_club)
        clubDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialogCreateClubButton = clubDialog.findViewById<Button>(R.id.createClubDialogButton)
        val clubName = clubDialog.findViewById<EditText>(R.id.clubName)
        val clubShortDesc = clubDialog.findViewById<EditText>(R.id.clubShortDesc)
        val clubLongDesc = clubDialog.findViewById<EditText>(R.id.clubLongDesc)
        val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
        val currentTime = sdf.format(Date())
        val sdfDate = SimpleDateFormat("MM/dd/yyyy")
        val currentDate = sdfDate.format(Date())



        if(currentUserID == null) {
            Toast.makeText(this.requireContext(), "You must be logged in to create a club", Toast.LENGTH_SHORT).show()
        } else {
            clubDialog.show()

            dialogCreateClubButton.setOnClickListener {
                val clubAnyoneRadio = clubDialog.findViewById<RadioButton>(R.id.clubAnyoneRadio)
                val clubReqPermRadio = clubDialog.findViewById<RadioButton>(R.id.clubReqPermissionRadio)
                val clubNameText = clubName.text.toString()
                val clubShortDescText = clubShortDesc.text.toString()
                val clubLongDescText = clubLongDesc.text.toString()
                var clubAccess = ""

                var currentUsername: String


                if(clubAnyoneRadio.isChecked) {
                    clubAccess = "anyone"
                } else if (clubReqPermRadio.isChecked) {
                    clubAccess = "reqPerm"
                }

                if(clubNameText.isEmpty() && clubShortDescText.isEmpty() && clubLongDescText.isEmpty() && clubAccess == "") {
                    Toast.makeText(this.requireContext(), "You must fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    db.collection("Clubs").document(clubId).set(ClubsData(clubNameText, clubShortDescText, clubLongDescText, clubAccess, clubId, 1, currentDate, currentTime))
                    val docRef = db.collection("Users").document(currentUserID!!)
                    docRef.get().addOnSuccessListener { document ->
                        if(document!=null) {
                            currentUsername = document.data?.get("username").toString()
                            db.collection("Clubs").document(clubId).collection("Members").document(currentUserID.toString()).set(
                                ClubMembersData(currentUsername, currentUserID, "creator")
                            )
                            db.collection("Clubs").document(clubId).collection("Admins").document(currentUserID.toString()).set(
                                ClubMembersData(currentUsername, currentUserID, "creator")
                            )
                        }
                    }
                    clubDialog.dismiss()
                }
            }
        }
    }

}