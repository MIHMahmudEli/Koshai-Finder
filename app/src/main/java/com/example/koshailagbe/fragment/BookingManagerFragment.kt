package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.R
import com.example.koshailagbe.adapter.BookingAdapter
import com.example.koshailagbe.databinding.FragmentBookingManagerBinding
import com.example.koshailagbe.model.Booking
import com.example.koshailagbe.utils.showSnackBar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class BookingManagerFragment : Fragment() {

    private var _binding: FragmentBookingManagerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: BookingAdapter
    private var allBookings = mutableListOf<Booking>()
    private var currentTab = 0 // 0: Pending, 1: Active, 2: History

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingManagerBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupTabs()
        setupToolbar()
        fetchBookings()

        return binding.root
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = BookingAdapter(
            emptyList(),
            onItemClick = { booking ->
                val bundle = Bundle().apply { putString("bookingId", booking.id) }
                findNavController().navigate(R.id.action_bookingManagerFragment_to_bookingDetailFragment, bundle)
            },
            onAccept = { booking -> updateBookingStatus(booking, "confirmed") },
            onDecline = { booking -> updateBookingStatus(booking, "cancelled") },
            onUpdateStatus = { booking -> handleStatusCycle(booking) }
        )
        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                filterAndDisplay()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun fetchBookings() {
        val uid = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE
        
        db.collection("bookings")
            .whereEqualTo("koshaiId", uid)
            .addSnapshotListener { snapshots, e ->
                if (!isAdded) return@addSnapshotListener
                binding.progressBar.visibility = View.GONE
                
                if (e != null) {
                    showSnackBar("Failed to load bookings: ${e.message}", isError = true)
                    return@addSnapshotListener
                }

                allBookings = snapshots?.documents?.mapNotNull { doc ->
                    val b = doc.toObject(Booking::class.java)
                    b?.id = doc.id
                    b
                }?.sortedByDescending { it.createdAt }?.toMutableList() ?: mutableListOf()
                
                filterAndDisplay()
            }
    }

    private fun filterAndDisplay() {
        val filteredList = when (currentTab) {
            0 -> allBookings.filter { it.status == "pending" }
            1 -> allBookings.filter { it.status in listOf("confirmed", "en_route", "arrived") }
            else -> allBookings.filter { it.status in listOf("completed", "cancelled") }
        }

        adapter.updateList(filteredList)
        binding.emptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateBookingStatus(booking: Booking, newStatus: String) {
        db.collection("bookings").document(booking.id).update("status", newStatus)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                showSnackBar("Booking $newStatus successfully")
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                showSnackBar("Failed to update booking: ${it.message}", isError = true)
            }
    }

    private fun handleStatusCycle(booking: Booking) {
        val nextStatus = when (booking.status) {
            "confirmed" -> "en_route"
            "en_route" -> "arrived"
            "arrived" -> "completed"
            else -> return
        }
        updateBookingStatus(booking, nextStatus)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
