package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.adapter.EarningAdapter
import com.example.koshailagbe.databinding.FragmentEarningsBinding
import com.example.koshailagbe.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EarningsFragment : Fragment() {

    private var _binding: FragmentEarningsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: EarningAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEarningsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        fetchEarnings()

        return binding.root
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupRecyclerView() {
        adapter = EarningAdapter(emptyList())
        binding.rvEarnings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEarnings.adapter = adapter
    }

    private fun fetchEarnings() {
        val uid = auth.currentUser?.uid ?: return
        
        // Fetch without orderBy to avoid index requirement for now
        db.collection("bookings")
            .whereEqualTo("koshaiId", uid)
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { snapshots ->
                if (!isAdded) return@addOnSuccessListener
                
                val bookings = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.apply { id = doc.id }
                }.sortedByDescending { it.date }

                adapter.updateList(bookings)
                
                val total = bookings.sumOf { it.rateBreakdown["total"] ?: 0.0 }
                binding.tvTotalEarnings.text = "৳${String.format("%.2f", total)}"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
