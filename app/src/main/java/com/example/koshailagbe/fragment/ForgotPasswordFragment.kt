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
                    
                    // Keep the dim overlay's hole synced with the card's new position
                    val card = binding.cardContainer
                    binding.focusDimOverlay.holeRect = android.graphics.RectF(
                        card.x, card.y, card.x + card.width, card.y + card.height
                    )
                    
                    return insets
                }
            })
        }

        auth = FirebaseAuth.getInstance()

        setupFocusDimming()

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Reset link sent! Check your email.", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        binding.tvBackToLogin.setOnClickListener { findNavController().popBackStack() }

        return binding.root
    }

    private fun setupFocusDimming() {
        binding.etEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Calculate the hole for the glass card
                val card = binding.cardContainer
                val rect = android.graphics.RectF(
                    card.x,
                    card.y,
                    card.x + card.width,
                    card.y + card.height
                )
                binding.focusDimOverlay.holeRect = rect
                binding.focusDimOverlay.holeRadius = 28f * resources.displayMetrics.density

                binding.focusDimOverlay.visibility = View.VISIBLE
                binding.focusDimOverlay.animate().alpha(1f).setDuration(300).start()
            } else {
                binding.focusDimOverlay.animate().alpha(0f).setDuration(300).withEndAction {
                    binding.focusDimOverlay.visibility = View.GONE
                }.start()
            }
        }
        
        binding.focusDimOverlay.setOnClickListener {
            binding.etEmail.clearFocus()
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
