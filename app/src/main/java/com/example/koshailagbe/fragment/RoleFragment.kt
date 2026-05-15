package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentRoleBinding

class RoleFragment : Fragment() {

    private var _binding: FragmentRoleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleBinding.inflate(inflater, container, false)

        binding.btnUser.setOnClickListener {
            findNavController().navigate(R.id.action_roleFragment_to_registerUserFragment)
        }

        binding.btnKoshai.setOnClickListener {
            findNavController().navigate(R.id.action_roleFragment_to_registerKoshaiFragment)
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}