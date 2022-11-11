package com.project24.animexapp.ui.profile

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project24.animexapp.databinding.FragmentNotificationsBinding
import com.project24.animexapp.databinding.FragmentProfileBinding
import kotlin.io.path.createTempDirectory

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val profileEmail = binding.profileEmail
        val currentUserEmail = firebaseAuth.currentUser?.email.toString()
        val currentUserID = firebaseAuth.currentUser?.uid.toString()

        profileEmail.text = currentUserEmail

        db.collection("Users").document(currentUserID).collection("Favourites").get().addOnSuccessListener { favourite ->
            for (document in favourite) {
                Log.d(TAG, "${document.data.getValue("favText")}")
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}