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
    private var allCompletedBookings: List<Booking> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEarningsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        fetchEarnings()

        return binding.root
    }

    private fun setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            filterBookings(checkedIds.firstOrNull())
        }
    }

    private fun filterBookings(checkedId: Int?) {
        val calendar = java.util.Calendar.getInstance()
        val currentMonth = calendar.get(java.util.Calendar.MONTH)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)

        val filtered = when (checkedId) {
            binding.chipMonth.id -> {
                allCompletedBookings.filter {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.date.seconds * 1000 }
                    cal.get(java.util.Calendar.MONTH) == currentMonth && cal.get(java.util.Calendar.YEAR) == currentYear
                }
            }
            binding.chipWeek.id -> {
                allCompletedBookings.filter {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.date.seconds * 1000 }
                    cal.get(java.util.Calendar.WEEK_OF_YEAR) == currentWeek && cal.get(java.util.Calendar.YEAR) == currentYear
                }
            }
            else -> allCompletedBookings
        }
        adapter.updateList(filtered)
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
        
        db.collection("bookings")
            .whereEqualTo("koshaiId", uid)
            .get()
            .addOnSuccessListener { snapshots ->
                if (!isAdded) return@addOnSuccessListener
                
                val allBookings = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.apply { id = doc.id }
                }

                val completedBookings = allBookings.filter { it.status == "completed" }
                    .sortedByDescending { it.date }

                allCompletedBookings = completedBookings
                filterBookings(binding.chipGroupFilter.checkedChipId)
                
                // Total Earnings
                val total = completedBookings.sumOf { it.rateBreakdown["total"] ?: 0.0 }
                binding.tvTotalEarnings.text = "৳${String.format("%.2f", total)}"

                // Monthly Earnings
                val calendar = java.util.Calendar.getInstance()
                val currentMonth = calendar.get(java.util.Calendar.MONTH)
                val currentYear = calendar.get(java.util.Calendar.YEAR)

                val monthlyTotal = completedBookings.filter {
                    val bookingCal = java.util.Calendar.getInstance().apply { timeInMillis = it.date.seconds * 1000 }
                    bookingCal.get(java.util.Calendar.MONTH) == currentMonth &&
                    bookingCal.get(java.util.Calendar.YEAR) == currentYear
                }.sumOf { it.rateBreakdown["total"] ?: 0.0 }
                
                binding.tvMonthlyEarnings.text = "৳${String.format("%.2f", monthlyTotal)}"

                // Pending (Ongoing/Requested) Potential Earnings
                val pendingTotal = allBookings.filter { it.status != "completed" && it.status != "cancelled" }
                    .sumOf { it.rateBreakdown["total"] ?: 0.0 }
                
                binding.tvPendingEarnings.text = "৳${String.format("%.2f", pendingTotal)}"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
