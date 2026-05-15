package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentUserHomeBinding
import com.example.koshailagbe.utils.SharedPrefsHelper
import com.google.firebase.auth.FirebaseAuth

class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
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
                    .setPopUpTo(R.id.userHomeFragment, true)
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