package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentRegisterUserBinding
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class RegisterUserFragment : Fragment() {

    private var _binding: FragmentRegisterUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterUserBinding.inflate(inflater, container, false)
        
        // Modern Keyboard Animation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            binding.root.setWindowInsetsAnimationCallback(object : android.view.WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(insets: android.view.WindowInsets, animations: MutableList<android.view.WindowInsetsAnimation>): android.view.WindowInsets {
                    return insets
                }
            })
        }

        auth = FirebaseAuth.getInstance()
        binding.btnRegisterUser.setOnClickListener { attemptRegister() }
        setupFocusEffects()
        binding.tvBackToLogin.setOnClickListener { findNavController().popBackStack() }
        return binding.root
    }

    private fun attemptRegister() {
        val name     = binding.etName.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val phone    = binding.etPhone.text.toString().trim()
        val district = binding.etDistrict.text.toString().trim()
        val upazila  = binding.etUpazila.text.toString().trim()
        val referral = binding.etReferral.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Strict Validation
        if (name.length < 3) {
            binding.tilName.error = "Name must be at least 3 characters"; return
        } else binding.tilName.error = null

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"; return
        } else binding.tilEmail.error = null

        if (phone.length != 11) {
            binding.tilPhone.error = "Enter a valid 11-digit phone number"; return
        } else binding.tilPhone.error = null

        if (district.isEmpty()) {
            binding.tilDistrict.error = "District is required"; return
        } else binding.tilDistrict.error = null

        if (upazila.isEmpty()) {
            binding.tilUpazila.error = "Upazila is required"; return
        } else binding.tilUpazila.error = null

        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"; return
        } else binding.tilPassword.error = null

        binding.btnRegisterUser.isEnabled = false
        binding.btnRegisterUser.text = "Creating account..."

        // Store data to be saved AFTER email verification
        val referralCode = UUID.randomUUID().toString().take(8).uppercase()
        PendingRegistration.role = PendingRegistration.ROLE_USER
        PendingRegistration.data = hashMapOf(
            "name"         to name,
            "email"        to email,
            "phone"        to "+88$phone",
            "photoUrl"     to "",
            "district"     to district,
            "upazila"      to upazila,
            "fcmToken"     to "",
            "createdAt"    to Timestamp.now(),
            "referralCode" to referralCode,
            "referredBy"   to referral,
            "credits"      to 0
        )

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener
                // Persist to SharedPreferences BEFORE navigating away
                PendingRegistration.save(requireContext())
                // Send verification email — NO Firestore write yet
                result.user?.sendEmailVerification()?.addOnCompleteListener {
                    if (!isAdded) return@addOnCompleteListener
                    binding.btnRegisterUser.isEnabled = true
                    binding.btnRegisterUser.text = "Create Account"
                    showSnackBar("Verification email sent! Please check your inbox 📧")
                    findNavController().navigate(
                        R.id.action_registerUserFragment_to_emailVerificationFragment,
                        bundleOf(EmailVerificationFragment.ARG_DESTINATION to EmailVerificationFragment.DEST_USER)
                    )
                }
            }
            .addOnFailureListener { ex ->
                if (!isAdded) return@addOnFailureListener
                binding.btnRegisterUser.isEnabled = true
                binding.btnRegisterUser.text = "Create Account"

                // If account exists but unverified — resend link instead
                if (ex.message?.contains("already in use") == true) {
                    handleExistingUnverifiedAccount(email, password)
                } else {
                    PendingRegistration.clear()
                    showSnackBar("Registration failed: ${ex.message}", isError = true)
                }
            }
    }

    /** Account exists in Auth but no Firestore data (unverified). Try to sign in and resend the link. */
    private fun handleExistingUnverifiedAccount(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener
                val user = result.user ?: return@addOnSuccessListener
                if (user.isEmailVerified) {
                    // Already verified but no Firestore — rare edge case, just go to login
                    auth.signOut()
                    PendingRegistration.clear()
                    showSnackBar("Account already exists. Please login.", isError = true)
                    findNavController().popBackStack()
                } else {
                    // Still unverified — resend and go to verify screen
                    user.sendEmailVerification()?.addOnCompleteListener {
                        if (!isAdded) return@addOnCompleteListener
                        showSnackBar("Verification email resent 📧")
                        findNavController().navigate(
                            R.id.action_registerUserFragment_to_emailVerificationFragment,
                            bundleOf(EmailVerificationFragment.ARG_DESTINATION to EmailVerificationFragment.DEST_USER)
                        )
                    }
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                PendingRegistration.clear()
                showSnackBar("This email is already registered. Try logging in.", isError = true)
            }
    }

    private fun setupFocusEffects() {
        val focusListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.focusOverlay.visibility = View.VISIBLE
                binding.focusOverlay.animate().alpha(1f).setDuration(300).start()
                binding.registerUserCard.animate().scaleX(1.01f).scaleY(1.01f).setDuration(300).start()
            } else {
                binding.focusOverlay.animate().alpha(0f).setDuration(300).withEndAction {
                    binding.focusOverlay.visibility = View.GONE
                }.start()
                binding.registerUserCard.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            }
        }

        listOf(binding.etName, binding.etEmail, binding.etPhone, binding.etDistrict, 
               binding.etUpazila, binding.etReferral, binding.etPassword).forEach {
            it.onFocusChangeListener = focusListener
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
