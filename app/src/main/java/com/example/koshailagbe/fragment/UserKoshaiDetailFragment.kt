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
        db.collection("koshais").document(id).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                koshaiProfile = doc.toObject(KoshaiProfile::class.java)?.apply { this.id = doc.id }
                koshaiProfile?.let { updateUI(it, doc) }
            }
    }

    private fun updateUI(profile: KoshaiProfile, doc: com.google.firebase.firestore.DocumentSnapshot) {
        binding.tvName.text = profile.name
        binding.tvCowRate.text = "৳${String.format("%.0f", profile.ratePerCow)}"
        binding.tvGoatRate.text = "৳${String.format("%.0f", profile.ratePerGoat)}"
        binding.tvFullLocation.text = "📍 Available in ${profile.upazila}, ${profile.district}"
        binding.tvLocation.text = "${profile.upazila}, ${profile.district}"

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
