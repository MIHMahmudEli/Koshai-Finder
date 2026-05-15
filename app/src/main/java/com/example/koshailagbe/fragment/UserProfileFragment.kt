package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        loadUserProfile()

        return binding.root
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_editProfileFragment)
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                binding.tvName.text = doc.getString("name") ?: "User Name"
                binding.tvEmail.text = doc.getString("email") ?: auth.currentUser?.email
                binding.tvPhone.text = doc.getString("phone") ?: "No phone"
                
                val district = doc.getString("district") ?: ""
                val upazila = doc.getString("upazila") ?: ""
                binding.tvLocation.text = if (district.isNotEmpty()) "$upazila, $district" else "Location not set"
                
                val photoUrl = doc.getString("photoUrl")
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivProfile)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
