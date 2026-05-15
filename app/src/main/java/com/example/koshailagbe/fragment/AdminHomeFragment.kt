package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentAdminHomeBinding
import com.example.koshailagbe.utils.SharedPrefsHelper
import com.google.firebase.auth.FirebaseAuth

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        setupStats()
        setupLogout()
        
        binding.cardVerification.setOnClickListener {
            findNavController().navigate(R.id.action_adminHomeFragment_to_adminKoshaiVerificationFragment)
        }

        return binding.root
    }

    private fun setupStats() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // 1. Koshai Stats (Verified count & Total Jobs & System Earnings)
        db.collection("koshais")
            .addSnapshotListener { snapshot, _ ->
                if (!isAdded) return@addSnapshotListener
                val docs = snapshot?.documents ?: emptyList()
                
                val verifiedCount = docs.count { it.getBoolean("isVerified") == true }
                val totalJobs = docs.sumOf { it.getLong("totalJobs") ?: 0L }
                val totalEarnings = docs.sumOf { it.getDouble("earnings") ?: 0.0 }
                
                binding.tvTotalVerified.text = verifiedCount.toString()
                binding.tvCompletedJobs.text = totalJobs.toString()
                binding.tvTotalEarnings.text = "৳${String.format("%.0f", totalEarnings)}"
                
                // Also update the pending count for the verification card
                val pendingCount = docs.count { it.getBoolean("isVerified") == false }
                binding.tvPendingCount.text = "$pendingCount pending registrations"
            }

        // 2. User Stats
        db.collection("users")
            .addSnapshotListener { snapshot, _ ->
                if (!isAdded) return@addSnapshotListener
                binding.tvTotalUsers.text = (snapshot?.size() ?: 0).toString()
            }

        // 3. Booking Activity (Pending vs Active)
        db.collection("bookings")
            .addSnapshotListener { snapshot, _ ->
                if (!isAdded) return@addSnapshotListener
                val docs = snapshot?.documents ?: emptyList()
                
                val pending = docs.count { it.getString("status") == "pending" }
                val active = docs.count { it.getString("status") == "accepted" }
                
                binding.tvPendingBookings.text = pending.toString()
                binding.tvActiveBookings.text = active.toString()
            }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            SharedPrefsHelper.clearUserRole(requireContext())
            auth.signOut()
            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.adminHomeFragment, true)
                    .build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
