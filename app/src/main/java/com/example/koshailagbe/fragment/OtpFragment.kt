package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentOtpBinding
import com.example.koshailagbe.utils.SharedPrefsHelper
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        // Verification ID passed from phone login flow
        var verificationId: String = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupListeners()

        return binding.root
    }

    private fun setupListeners() {
        binding.btnVerifyContinue.setOnClickListener {
            val otp = binding.etVerificationCode.text.toString().trim()

            if (otp.length != 6) {
                showSnackBar(getString(R.string.error_invalid_otp), isError = true)
                return@setOnClickListener
            }

            if (verificationId.isEmpty()) {
                showSnackBar(getString(R.string.error_session_expired), isError = true)
                return@setOnClickListener
            }

            verifyCode(otp)
        }

        binding.tvResend.setOnClickListener {
            showSnackBar(getString(R.string.msg_resending_otp))
            // Logic for resending OTP would go here if implemented
        }
    }

    private fun verifyCode(otp: String) {
        setLoadingState(true)
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                val uid = it.user?.uid ?: return@addOnSuccessListener
                routeUserByRole(uid)
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                setLoadingState(false)
                showSnackBar(getString(R.string.error_update_failed, it.message), isError = true)
            }
    }

    private fun routeUserByRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (!isAdded) return@addOnSuccessListener
                if (userDoc.exists()) {
                    SharedPrefsHelper.saveUserRole(requireContext(), SharedPrefsHelper.ROLE_USER)
                    navigateTo(R.id.action_otpFragment_to_userHomeFragment)
                } else {
                    db.collection("koshais").document(uid).get()
                        .addOnSuccessListener { koshaiDoc ->
                            if (!isAdded) return@addOnSuccessListener
                            if (koshaiDoc.exists()) {
                                SharedPrefsHelper.saveUserRole(requireContext(), SharedPrefsHelper.ROLE_KOSHAI)
                                navigateTo(R.id.action_otpFragment_to_koshaiDashboardFragment)
                            } else {
                                navigateTo(R.id.action_otpFragment_to_roleFragment)
                            }
                        }
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                setLoadingState(false)
                showSnackBar(getString(R.string.error_load_failed, it.message), isError = true)
            }
    }

    private fun navigateTo(actionId: Int) {
        findNavController().navigate(
            actionId,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()
        )
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnVerifyContinue.isEnabled = !isLoading
        binding.btnVerifyContinue.text = if (isLoading) getString(R.string.msg_verifying) else getString(R.string.btn_verify_continue)
        // Add more visual feedback if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
