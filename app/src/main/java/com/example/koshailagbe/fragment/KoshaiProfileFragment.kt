package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.R
import com.example.koshailagbe.adapter.ReviewAdapter
import com.example.koshailagbe.databinding.FragmentKoshaiProfileBinding
import com.example.koshailagbe.model.Review
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class KoshaiProfileFragment : Fragment() {

    private var _binding: FragmentKoshaiProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKoshaiProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        loadProfileData()
        fetchReviews()

        return binding.root
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_koshaiProfileFragment_to_editProfileFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(emptyList())
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter
    }

    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("koshais").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                binding.tvName.text = doc.getString("name") ?: "N/A"
                binding.tvRating.text = String.format("%.1f", doc.getDouble("rating") ?: 0.0)
                binding.tvJobs.text = (doc.getLong("totalJobs") ?: 0).toString()
                
                val bio = doc.getString("bio")
                if (!bio.isNullOrEmpty()) {
                    binding.tvBio.text = bio
                }

                val photoUrl = doc.getString("photoUrl")
                com.bumptech.glide.Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivProfile)
            }
    }

    private fun fetchReviews() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("koshais").document(uid).collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshots ->
                if (!isAdded) return@addOnSuccessListener
                val reviews = snapshots.toObjects(Review::class.java)
                adapter.updateList(reviews)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
