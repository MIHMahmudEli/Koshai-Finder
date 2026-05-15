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

        // Pending Koshais
        db.collection("koshais")
            .whereEqualTo("isVerified", false)
            .addSnapshotListener { snapshot, _ ->
                if (!isAdded) return@addSnapshotListener
                val count = snapshot?.size() ?: 0
                binding.tvPendingCount.text = "$count pending registrations"
            }

        // Total Users
        db.collection("users")
            .addSnapshotListener { snapshot, _ ->
                if (!isAdded) return@addSnapshotListener
                binding.tvTotalUsers.text = (snapshot?.size() ?: 0).toString()
            }

        // Total Koshais
        db.collection("koshais")
            .addSnapshotListener { snapshot, _ ->
                if (!isAdded) return@addSnapshotListener
                binding.tvTotalKoshais.text = (snapshot?.size() ?: 0).toString()
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
