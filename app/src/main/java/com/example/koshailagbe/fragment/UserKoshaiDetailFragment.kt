package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.adapter.ReviewAdapter
import com.example.koshailagbe.databinding.FragmentUserKoshaiDetailBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.example.koshailagbe.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserKoshaiDetailFragment : Fragment() {

    private var _binding: FragmentUserKoshaiDetailBinding? = null
    private val binding get() = _binding!!
    
    private var koshaiId: String? = null
    private lateinit var db: FirebaseFirestore
    private var koshaiProfile: KoshaiProfile? = null
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserKoshaiDetailBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        
        koshaiId = arguments?.getString("koshaiId")
        
        setupToolbar()
        setupReviewsRecyclerView()
        loadKoshaiData()
        loadReviews()
        setupActions()

        return binding.root
    }

    private fun setupReviewsRecyclerView() {
        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = reviewAdapter
    }

    private fun loadReviews() {
        val id = koshaiId ?: return
        db.collection("koshais").document(id).collection("reviews")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (!isAdded) return@addSnapshotListener
                if (e != null) return@addSnapshotListener
                
                val reviews = snapshot?.documents?.mapNotNull { it.toObject(Review::class.java) } ?: emptyList()
                reviewAdapter.updateList(reviews)
            }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun loadKoshaiData() {
        val id = koshaiId ?: return
        db.collection("koshais").document(id).addSnapshotListener { snapshot, e ->
            if (!isAdded || e != null) return@addSnapshotListener
            
            snapshot?.let { doc ->
                if (doc.exists()) {
                    koshaiProfile = doc.toObject(KoshaiProfile::class.java)?.apply { this.id = doc.id }
                    koshaiProfile?.let { updateUI(it, doc) }
                }
            }
        }
    }

    private fun updateUI(profile: KoshaiProfile, doc: com.google.firebase.firestore.DocumentSnapshot) {
        binding.tvName.text = doc.getString("name") ?: profile.name

        // Read numeric fields directly from snapshot to avoid Int→Long deserialization issues
        val rating = doc.getDouble("rating") ?: 0.0
        val totalJobs = doc.getLong("totalJobs") ?: 0L

        binding.tvRating.text = String.format("%.1f", rating)
        binding.tvJobCount.text = totalJobs.toString()

        val cowRate = doc.getDouble("ratePerCow") ?: 0.0
        val goatRate = doc.getDouble("ratePerGoat") ?: 0.0
        binding.tvCowRate.text = "৳${String.format("%.0f", cowRate)}"
        binding.tvGoatRate.text = "৳${String.format("%.0f", goatRate)}"

        val upazila = doc.getString("upazila") ?: ""
        val district = doc.getString("district") ?: ""
        binding.tvFullLocation.text = "📍 Available in $upazila, $district"
        binding.tvLocation.text = "$upazila, $district"

        // Show About card only if bio is available
        val bio = doc.getString("bio")?.trim()
        if (!bio.isNullOrEmpty()) {
            binding.cardAbout.visibility = View.VISIBLE
            binding.tvBio.text = bio
        } else {
            binding.cardAbout.visibility = View.GONE
        }
    }

    private var doc: com.google.firebase.firestore.DocumentSnapshot? = null

    private fun setupActions() {
        binding.btnBookNow.setOnClickListener {
            val bundle = Bundle().apply { 
                putString("koshaiId", koshaiId)
            }
            findNavController().navigate(R.id.action_userKoshaiDetailFragment_to_bookingRequestFragment, bundle)
        }

        binding.btnChat.setOnClickListener {
            val profile = koshaiProfile ?: return@setOnClickListener
            val bundle = Bundle().apply {
                putString("receiverId", profile.id)
                putString("receiverName", profile.name)
                putString("receiverPhoto", profile.photoUrl)
            }
            findNavController().navigate(R.id.action_userKoshaiDetailFragment_to_chatFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
