package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.bumptech.glide.Glide
import com.example.koshailagbe.databinding.FragmentKoshaiDashboardBinding
import com.example.koshailagbe.model.KoshaiProfile
import com.example.koshailagbe.utils.SharedPrefsHelper
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KoshaiDashboardFragment : Fragment() {

    private var _binding: FragmentKoshaiDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var koshaiProfile: KoshaiProfile? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKoshaiDashboardBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClickListeners()
        loadKoshaiData()
        fetchBookingCounts()
        setupSwipeRefresh()

        return binding.root
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadKoshaiData()
            fetchBookingCounts()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupClickListeners() {
        binding.ibChatList.setOnClickListener {
            findNavController().navigate(R.id.chatListFragment)
        }

        binding.btnLogout.setOnClickListener {
            SharedPrefsHelper.clearUserRole(requireContext())
            auth.signOut()
            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.koshaiDashboardFragment, true)
                    .build()
            )
        }

        binding.statusToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val newStatus = when (checkedId) {
                    R.id.btnOnline -> "online"
                    R.id.btnBusy -> "busy"
                    else -> "offline"
                }
                
                // Only update Firestore and show SnackBar if the status actually changed
                // This prevents the trigger during initial data load in updateUI()
                if (koshaiProfile != null && newStatus != koshaiProfile?.status) {
                    updateStatus(newStatus)
                    koshaiProfile?.status = newStatus // Update local state immediately
                }
            }
        }

        binding.requestsCard.setOnClickListener {
            findNavController().navigate(R.id.action_koshaiDashboardFragment_to_bookingManagerFragment)
        }

        binding.scheduleCard.setOnClickListener {
            // Navigate to Booking Manager (Active tab) or a dedicated schedule view
            findNavController().navigate(R.id.action_koshaiDashboardFragment_to_bookingManagerFragment)
        }

        binding.cardAvailability.setOnClickListener {
            findNavController().navigate(R.id.action_koshaiDashboardFragment_to_availabilityFragment)
        }

        binding.cardEarnings.setOnClickListener {
            findNavController().navigate(R.id.action_koshaiDashboardFragment_to_earningsFragment)
        }

        binding.cardLeaderboard.setOnClickListener {
            findNavController().navigate(R.id.action_koshaiDashboardFragment_to_leaderboardFragment)
        }

        binding.cardProfile.setOnClickListener {
            findNavController().navigate(R.id.action_koshaiDashboardFragment_to_koshaiProfileFragment)
        }
    }

    private fun loadKoshaiData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("koshais").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (!isAdded) return@addSnapshotListener
                if (e != null) return@addSnapshotListener
                
                koshaiProfile = snapshot?.toObject(KoshaiProfile::class.java)
                koshaiProfile?.let { updateUI(it) }
            }
    }

    private fun updateUI(profile: KoshaiProfile) {
        binding.tvKoshaiName.text = "Welcome, ${profile.name}"
        // tvTotalJobs is updated by fetchBookingCounts() with live active job count
        binding.tvEarnings.text = "৳${formatShort(profile.earnings)}"
        binding.tvRating.text = String.format("%.1f", profile.rating)

        Glide.with(this)
            .load(profile.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .into(binding.ivProfile)

        // Set status toggle
        when (profile.status) {
            "online" -> {
                binding.statusToggleGroup.check(R.id.btnOnline)
                binding.tvKoshaiStatus.text = "Currently Online"
            }
            "busy" -> {
                binding.statusToggleGroup.check(R.id.btnBusy)
                binding.tvKoshaiStatus.text = "Currently Busy"
            }
            else -> {
                binding.statusToggleGroup.check(R.id.btnOffline)
                binding.tvKoshaiStatus.text = "Currently Offline"
            }
        }
    }

    /**
     * Formats a number into a compact shorthand:
     * 500 → ৳500, 1500 → ৳1.5k, 1200000 → ৳1.2M
     */
    private fun formatShort(value: Double): String = when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000.0)
        value >= 1_000     -> {
            val formatted = String.format("%.1fk", value / 1_000.0)
            if (formatted.endsWith(".0k")) formatted.replace(".0k", "k") else formatted
        }
        else -> value.toInt().toString()
    }

    private fun updateStatus(status: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("koshais").document(uid).update("status", status)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                binding.tvKoshaiStatus.text = "Currently ${status.replaceFirstChar { it.uppercase() }}"
                showSnackBar("Status updated to $status")
            }
    }

    private fun fetchBookingCounts() {
        val uid = auth.currentUser?.uid ?: return

        // Real-time listener for ALL active bookings (confirmed, en_route, arrived)
        // This drives both tvTotalJobs (active count) and the schedule info card
        db.collection("bookings")
            .whereEqualTo("koshaiId", uid)
            .addSnapshotListener { snapshots, e ->
                if (!isAdded) return@addSnapshotListener
                if (e != null || snapshots == null) return@addSnapshotListener

                val activeStatuses = setOf("confirmed", "en_route", "arrived")
                var activeCount = 0
                var pendingCount = 0

                for (doc in snapshots.documents) {
                    val status = doc.getString("status") ?: continue
                    when (status) {
                        in activeStatuses -> activeCount++
                        "pending"        -> pendingCount++
                    }
                }

                // Active jobs = accepted bookings currently in progress
                binding.tvTotalJobs.text = activeCount.toString()

                // Pending requests badge
                binding.tvRequestCount.text = "$pendingCount pending requests"

                // Schedule info
                binding.tvScheduleInfo.text = if (activeCount > 0)
                    "You have $activeCount active jobs"
                else
                    "No active jobs right now"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
