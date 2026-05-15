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

        return binding.root
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
        db.collection("koshais").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                koshaiProfile = doc.toObject(KoshaiProfile::class.java)
                koshaiProfile?.let { updateUI(it) }
            }
    }

    private fun updateUI(profile: KoshaiProfile) {
        binding.tvKoshaiName.text = "Welcome, ${profile.name}"
        binding.tvTotalJobs.text = profile.totalJobs.toString()
        binding.tvEarnings.text = "৳${profile.earnings}"
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
        
        // Fetch Pending Requests
        db.collection("bookings")
            .whereEqualTo("koshaiId", uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { docs ->
                if (!isAdded) return@addOnSuccessListener
                val count = docs.size()
                binding.tvRequestCount.text = "$count pending requests"
            }

        // Fetch Today's Confirmed Bookings (Simplified)
        db.collection("bookings")
            .whereEqualTo("koshaiId", uid)
            .whereEqualTo("status", "confirmed")
            .get()
            .addOnSuccessListener { docs ->
                if (!isAdded) return@addOnSuccessListener
                val count = docs.size()
                if (count > 0) {
                    binding.tvScheduleInfo.text = "You have $count jobs today"
                } else {
                    binding.tvScheduleInfo.text = "No bookings for today"
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
