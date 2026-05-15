package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentUserKoshaiDetailBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.google.firebase.firestore.FirebaseFirestore

class UserKoshaiDetailFragment : Fragment() {

    private var _binding: FragmentUserKoshaiDetailBinding? = null
    private val binding get() = _binding!!
    
    private var koshaiId: String? = null
    private lateinit var db: FirebaseFirestore
    private var koshaiProfile: KoshaiProfile? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserKoshaiDetailBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        
        koshaiId = arguments?.getString("koshaiId")
        
        setupToolbar()
        loadKoshaiData()
        setupBookingButton()

        return binding.root
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
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
        binding.collapsingToolbar.title = profile.name
        binding.tvCowRate.text = "৳${String.format("%.0f", profile.ratePerCow)}"
        binding.tvGoatRate.text = "৳${String.format("%.0f", profile.ratePerGoat)}"
        binding.tvFullLocation.text = "Available in ${profile.upazila}, ${profile.district}"
        
        // Use bio if available, otherwise a helpful placeholder
        val bio = doc.getString("bio") ?: "Experienced Koshai available for professional slaughtering and cleaning services."
        binding.tvBio.text = bio
    }

    private var doc: com.google.firebase.firestore.DocumentSnapshot? = null

    private fun setupBookingButton() {
        binding.btnBookNow.setOnClickListener {
            val bundle = Bundle().apply { 
                putString("koshaiId", koshaiId)
            }
            findNavController().navigate(R.id.action_userKoshaiDetailFragment_to_bookingRequestFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
