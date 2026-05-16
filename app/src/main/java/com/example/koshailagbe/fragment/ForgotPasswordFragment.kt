package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        
        // Modern Keyboard Animation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            binding.root.setWindowInsetsAnimationCallback(object : android.view.WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(insets: android.view.WindowInsets, animations: MutableList<android.view.WindowInsetsAnimation>): android.view.WindowInsets {
                    val imeInsets = insets.getInsets(android.view.WindowInsets.Type.ime())
                    binding.cardContainer.translationY = -(imeInsets.bottom / 2f)
                    return insets
                }
            })
        }

        auth = FirebaseAuth.getInstance()

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please enter your email", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    android.widget.Toast.makeText(requireContext(), "Reset link sent! Check your email.", android.widget.Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    android.widget.Toast.makeText(requireContext(), "Error: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
                }
        }
        
        setupFocusEffects()

        binding.tvBackToLogin.setOnClickListener { findNavController().popBackStack() }

        return binding.root
    }

    private fun setupFocusEffects() {
        val focusListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.focusOverlay.visibility = View.VISIBLE
                binding.focusOverlay.animate().alpha(1f).setDuration(300).start()
                binding.cardContainer.animate().scaleX(1.02f).scaleY(1.02f).setDuration(300).start()
            } else {
                binding.focusOverlay.animate().alpha(0f).setDuration(300).withEndAction {
                    binding.focusOverlay.visibility = View.GONE
                }.start()
                binding.cardContainer.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            }
        }
        binding.etEmail.onFocusChangeListener = focusListener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
