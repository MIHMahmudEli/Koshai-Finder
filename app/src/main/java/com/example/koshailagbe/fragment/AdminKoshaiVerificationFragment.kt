package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.adapter.KoshaiVerificationAdapter
import com.example.koshailagbe.databinding.FragmentAdminKoshaiVerificationBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.google.firebase.firestore.FirebaseFirestore

class AdminKoshaiVerificationFragment : Fragment() {

    private var _binding: FragmentAdminKoshaiVerificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: KoshaiVerificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminKoshaiVerificationBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadPendingKoshais()
        
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = KoshaiVerificationAdapter(emptyList(), 
            onApprove = { koshai -> approveKoshai(koshai) },
            onReject = { koshai -> rejectKoshai(koshai) }
        )
        binding.rvVerification.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVerification.adapter = adapter
    }

    private fun loadPendingKoshais() {
        binding.shimmerLoading.visibility = View.VISIBLE
        binding.shimmerLoading.startShimmer()
        
        db.collection("koshais")
            .whereEqualTo("isVerified", false)
            .addSnapshotListener { snapshot, e ->
                if (!isAdded) return@addSnapshotListener
                binding.shimmerLoading.stopShimmer()
                binding.shimmerLoading.visibility = View.GONE
                
                if (e != null) return@addSnapshotListener
                
                val koshais = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(KoshaiProfile::class.java)?.apply { id = doc.id }
                } ?: emptyList()

                adapter.updateList(koshais)
                
                if (koshais.isEmpty()) {
                    binding.layoutEmpty.root.visibility = View.VISIBLE
                    binding.layoutEmpty.tvEmptyTitle.text = "All Clear!"
                    binding.layoutEmpty.tvEmptySubtitle.text = "No pending Koshai registrations at the moment."
                    binding.rvVerification.visibility = View.GONE
                } else {
                    binding.layoutEmpty.root.visibility = View.GONE
                    binding.rvVerification.visibility = View.VISIBLE
                }
            }
    }

    private fun approveKoshai(koshai: KoshaiProfile) {
        db.collection("koshais").document(koshai.id)
            .update("isVerified", true)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "${koshai.name} Approved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to approve.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectKoshai(koshai: KoshaiProfile) {
        // For now, rejection just stays unverified or we could delete the profile
        // Let's just show a toast for now as business logic for rejection varies
        Toast.makeText(requireContext(), "Rejection logic pending.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
