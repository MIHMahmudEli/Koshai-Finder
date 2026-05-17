package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                koshaiProfile?.let { 
                    updateUI(it)
                    checkProfileCompletion(it)
                }
            }
    }

    private fun checkProfileCompletion(profile: KoshaiProfile) {
        // If bio is empty or they haven't set their upazila, prompt them
        if ((profile.bio.isEmpty() || profile.upazila.isEmpty()) && isAdded) {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Complete Your Profile")
                .setMessage("A complete profile helps you get more bookings. Add a bio and specify your exact service area now.")
                .setPositiveButton("Complete Now") { _, _ ->
                    findNavController().navigate(R.id.action_koshaiDashboardFragment_to_koshaiProfileFragment)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun updateUI(profile: KoshaiProfile) {
        binding.tvKoshaiName.text = "Welcome, ${profile.name}"
        // tvTotalJobs is updated by fetchBookingCounts() with live active job count
        binding.tvRating.text = String.format(java.util.Locale.getDefault(), "%.1f", profile.rating)

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


    private fun updateStatus(status: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("koshais").document(uid).update("status", status)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                binding.tvKoshaiStatus.text = "Currently ${status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}"
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
