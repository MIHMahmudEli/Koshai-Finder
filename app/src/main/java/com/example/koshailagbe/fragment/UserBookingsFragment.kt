package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.koshailagbe.R
import com.example.koshailagbe.adapter.UserBookingsAdapter
import com.example.koshailagbe.databinding.FragmentUserBookingsBinding
import com.example.koshailagbe.model.Booking
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserBookingsFragment : Fragment() {

    private var _binding: FragmentUserBookingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserBookingsAdapter
    private var allBookings = listOf<Booking>()
    private var currentTab = 0 // 0: Pending, 1: Upcoming, 2: Past

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBookingsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupTabs()
        fetchBookings()

        binding.btnBrowse?.setOnClickListener {
            findNavController().popBackStack(R.id.userHomeFragment, false)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        adapter = UserBookingsAdapter(
            emptyList(),
            onItemClick = { booking ->
                // Optionally navigate to a booking detail screen
            },
            onReviewClick = { booking ->
                val bundle = Bundle().apply {
                    putString("bookingId", booking.id)
                    putString("koshaiId", booking.koshaiId)
                    putString("koshaiName", booking.koshaiName)
                }
                findNavController().navigate(R.id.action_userBookingsFragment_to_reviewFragment, bundle)
            }
        )
        binding.rvUserBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserBookings.adapter = adapter
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
        val userId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (!isAdded) return@addSnapshotListener
                binding.progressBar.visibility = View.GONE

                if (e != null) return@addSnapshotListener

                allBookings = snapshots?.documents?.mapNotNull { doc ->
                    val b = doc.toObject(Booking::class.java)
                    b?.id = doc.id
                    b
                }?.sortedByDescending { it.createdAt } ?: emptyList()

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
        
        if (filteredList.isEmpty()) {
            val title = when (currentTab) {
                0 -> "No Pending Requests"
                1 -> "No Upcoming Services"
                else -> "No Booking History"
            }
            val subtitle = when (currentTab) {
                0 -> "Your new service requests will appear here."
                1 -> "Once a Koshai confirms, your active jobs will show up here."
                else -> "Your completed or cancelled jobs will be archived here."
            }
            
            binding.emptyState.findViewById<android.widget.TextView>(R.id.tvEmptyTitle).text = title
            binding.emptyState.findViewById<android.widget.TextView>(R.id.tvEmptySubtitle).text = subtitle
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
