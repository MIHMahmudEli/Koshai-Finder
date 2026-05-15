package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.databinding.FragmentOtpBinding
import com.example.koshailagbe.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    companion object {
        // Verification ID passed from phone login flow (if used)
        var verificationId: String = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()

            if (otp.length != 6) {
                Toast.makeText(requireContext(), "Enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (verificationId.isEmpty()) {
                Toast.makeText(requireContext(), "Session expired. Please go back and try again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val credential = PhoneAuthProvider.getCredential(verificationId, otp)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    if (!isAdded) return@addOnSuccessListener
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                    val db = FirebaseFirestore.getInstance()

                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            if (!isAdded) return@addOnSuccessListener
                            if (userDoc.exists()) {
                                findNavController().navigate(R.id.action_otpFragment_to_userHomeFragment)
                            } else {
                                db.collection("koshais").document(uid).get()
                                    .addOnSuccessListener { koshaiDoc ->
                                        if (!isAdded) return@addOnSuccessListener
                                        if (koshaiDoc.exists()) {
                                            findNavController().navigate(R.id.action_otpFragment_to_koshaiDashboardFragment)
                                        } else {
                                            findNavController().navigate(R.id.action_otpFragment_to_roleFragment)
                                        }
                                    }
                            }
                        }
                }
                .addOnFailureListener {
                    if (!isAdded) return@addOnFailureListener
                    Toast.makeText(requireContext(), "Invalid OTP: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        binding.tvResend.setOnClickListener {
            Toast.makeText(requireContext(), "Resending code...", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}