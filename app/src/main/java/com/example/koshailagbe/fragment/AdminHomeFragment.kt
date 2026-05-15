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

        binding.btnLogout.setOnClickListener {
            // Clear user role
            SharedPrefsHelper.clearUserRole(requireContext())
            
            // Sign out from Firebase
            auth.signOut()

            // Navigate back to login screen
            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.adminHomeFragment, true)
                    .build()
            )
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
