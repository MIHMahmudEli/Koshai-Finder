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
import com.example.koshailagbe.databinding.FragmentRegisterKoshaiBinding
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

class RegisterKoshaiFragment : Fragment() {

    private var _binding: FragmentRegisterKoshaiBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterKoshaiBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        binding.btnRegisterKoshai.setOnClickListener { attemptRegister() }
        binding.tvBackToLogin.setOnClickListener { findNavController().popBackStack() }
        return binding.root
    }

    private fun attemptRegister() {
        val name      = binding.etName.text.toString().trim()
        val email     = binding.etEmail.text.toString().trim()
        val phone     = binding.etPhone.text.toString().trim()
        val district  = binding.etDistrict.text.toString().trim()
        val upazila   = binding.etUpazila.text.toString().trim()
        val rateCow   = binding.etRateCow.text.toString().trim()
        val rateGoat  = binding.etRateGoat.text.toString().trim()
        val rateSheep = binding.etRateSheep.text.toString().trim()
        val password  = binding.etPassword.text.toString().trim()

        // Validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || district.isEmpty() ||
            upazila.isEmpty() || rateCow.isEmpty() || rateGoat.isEmpty() || rateSheep.isEmpty() || password.isEmpty()
        ) {
            showSnackBar("Please fill all required fields", isError = true)
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"; return
        } else binding.tilEmail.error = null

        if (phone.length < 10) {
            binding.tilPhone.error = "Enter a valid phone number"; return
        } else binding.tilPhone.error = null

        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"; return
        } else binding.tilPassword.error = null

        binding.btnRegisterKoshai.isEnabled = false
        binding.btnRegisterKoshai.text = "Submitting..."

        // Store data to be saved AFTER email verification
        PendingRegistration.role = PendingRegistration.ROLE_KOSHAI
        PendingRegistration.data = hashMapOf(
            "name"               to name,
            "email"              to email,
            "phone"              to "+88$phone",
            "photoUrl"           to "",
            "nidUrl"             to "",
            "tradeCertUrl"       to "",
            "district"           to district,
            "upazila"            to upazila,
            "coverageAreas"      to listOf<String>(),
            "ratePerCow"         to (rateCow.toDoubleOrNull() ?: 0.0),
            "ratePerGoat"        to (rateGoat.toDoubleOrNull() ?: 0.0),
            "ratePerSheep"       to (rateSheep.toDoubleOrNull() ?: 0.0),
            "surgeMultiplier"    to 1.0,
            "status"             to "offline",
            "isVerified"         to false,
            "isEidMode"          to false,
            "isFlagged"          to false,
            "isBanned"           to false,
            "rating"             to 0.0,
            "totalRatings"       to 0,
            "totalJobs"          to 0,
            "lat"                to 0.0,
            "lng"                to 0.0,
            "locationUpdatedAt"  to Timestamp.now(),
            "earnings"           to 0.0,
            "fcmToken"           to "",
            "createdAt"          to Timestamp.now()
        )

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener
                // Persist to SharedPreferences BEFORE navigating away
                PendingRegistration.save(requireContext())
                // Send verification email — NO Firestore write yet
                result.user?.sendEmailVerification()?.addOnCompleteListener {
                    if (!isAdded) return@addOnCompleteListener
                    binding.btnRegisterKoshai.isEnabled = true
                    binding.btnRegisterKoshai.text = "Submit Registration"
                    showSnackBar("Verification email sent! Please check your inbox 📧")
                    findNavController().navigate(
                        R.id.action_registerKoshaiFragment_to_emailVerificationFragment,
                        bundleOf(EmailVerificationFragment.ARG_DESTINATION to EmailVerificationFragment.DEST_KOSHAI)
                    )
                }
            }
            .addOnFailureListener { ex ->
                if (!isAdded) return@addOnFailureListener
                binding.btnRegisterKoshai.isEnabled = true
                binding.btnRegisterKoshai.text = "Submit Registration"

                if (ex.message?.contains("already in use") == true) {
                    handleExistingUnverifiedAccount(email, password)
                } else {
                    PendingRegistration.clear()
                    showSnackBar("Registration failed: ${ex.message}", isError = true)
                }
            }
    }

    private fun handleExistingUnverifiedAccount(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                if (!isAdded) return@addOnSuccessListener
                val user = result.user ?: return@addOnSuccessListener
                if (user.isEmailVerified) {
                    auth.signOut()
                    PendingRegistration.clear()
                    showSnackBar("Account already exists. Please login.", isError = true)
                    findNavController().popBackStack()
                } else {
                    user.sendEmailVerification()?.addOnCompleteListener {
                        if (!isAdded) return@addOnCompleteListener
                        showSnackBar("Verification email resent 📧")
                        findNavController().navigate(
                            R.id.action_registerKoshaiFragment_to_emailVerificationFragment,
                            bundleOf(EmailVerificationFragment.ARG_DESTINATION to EmailVerificationFragment.DEST_KOSHAI)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
