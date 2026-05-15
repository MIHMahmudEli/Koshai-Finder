package com.example.koshailagbe.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentEmailVerificationBinding
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmailVerificationFragment : Fragment() {

    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var destinationRole: String = DEST_USER

    // Auto-poll every 4 seconds to detect email verification
    private val pollHandler = Handler(Looper.getMainLooper())
    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!isAdded) return
            auth.currentUser?.reload()?.addOnCompleteListener { task ->
                if (!isAdded) return@addOnCompleteListener
                if (task.isSuccessful && auth.currentUser?.isEmailVerified == true) {
                    // Auto-detected! Save to Firestore and proceed
                    saveToFirestore(auto = true)
                } else {
                    // Not yet — check again in 4 seconds
                    pollHandler.postDelayed(this, 4_000)
                }
            }
        }
    }

    companion object {
        const val ARG_DESTINATION = "destination"
        const val DEST_USER   = "user"
        const val DEST_KOSHAI = "koshai"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        destinationRole = arguments?.getString(ARG_DESTINATION) ?: DEST_USER

        // Try to restore pending data if process was killed
        PendingRegistration.loadIfEmpty(requireContext())

        binding.tvEmail.text = auth.currentUser?.email ?: ""

        // Start auto-polling
        pollHandler.postDelayed(pollRunnable, 4_000)

        binding.btnIveVerified.setOnClickListener { checkVerificationAndSave() }

        binding.btnResendEmail.setOnClickListener {
            auth.currentUser?.sendEmailVerification()
                ?.addOnSuccessListener {
                    if (!isAdded) return@addOnSuccessListener
                    showSnackBar("Verification email resent! ✉️")
                    binding.btnResendEmail.isEnabled = false
                    binding.btnResendEmail.text = "Resent! Wait 30s..."
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (isAdded) {
                            binding.btnResendEmail.isEnabled = true
                            binding.btnResendEmail.text = "Resend Verification Email"
                        }
                    }, 30_000)
                }
                ?.addOnFailureListener {
                    if (!isAdded) return@addOnFailureListener
                    Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        binding.tvBackToLogin.setOnClickListener {
            pollHandler.removeCallbacks(pollRunnable)
            PendingRegistration.clear(requireContext())
            auth.signOut()
            findNavController().navigate(
                R.id.action_emailVerificationFragment_to_loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true).build()
            )
        }

        return binding.root
    }

    private fun checkVerificationAndSave() {
        binding.btnIveVerified.isEnabled = false
        binding.btnIveVerified.text = "Checking..."

        auth.currentUser?.reload()?.addOnCompleteListener { task ->
            if (!isAdded) return@addOnCompleteListener
            if (!task.isSuccessful || auth.currentUser?.isEmailVerified != true) {
                resetButton()
                showSnackBar("Email not verified yet. Please click the link in your inbox.", isError = true)
                return@addOnCompleteListener
            }
            saveToFirestore(auto = false)
        }
    }

    private fun saveToFirestore(auto: Boolean) {
        pollHandler.removeCallbacks(pollRunnable) // Stop polling

        val uid = auth.currentUser?.uid
        if (uid == null) {
            resetButton()
            showSnackBar("Session expired. Please login again.", isError = true)
            return
        }

        // Try to restore data from SharedPreferences if in-memory object was cleared
        val dataReady = PendingRegistration.loadIfEmpty(requireContext())

        if (!dataReady || !PendingRegistration.isReady()) {
            // Data lost and can't be recovered — check if already saved (edge case)
            routeExistingUser(uid)
            return
        }

        val collection = if (destinationRole == DEST_KOSHAI) "koshais" else "users"
        val dataToSave = HashMap(PendingRegistration.data)
        dataToSave["uid"] = uid

        if (!auto) {
            binding.btnIveVerified.text = "Saving..."
        }

        db.collection(collection).document(uid).set(dataToSave)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                PendingRegistration.clear(requireContext())
                val roleToSave = if (destinationRole == DEST_KOSHAI) com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI else com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_USER
                com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), roleToSave)
                showSnackBar("Welcome! Your account is ready 🎉")
                navigateHome()
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                resetButton()
                val errMsg = when {
                    it.message?.contains("PERMISSION_DENIED") == true ->
                        "Firestore rules not configured. Please update rules in Firebase Console to: allow read, write: if request.auth != null;"
                    it.message?.contains("NOT_FOUND") == true ->
                        "Firestore database not created yet. Please create it in Firebase Console."
                    else -> "Failed to save profile: ${it.message}"
                }
                showSnackBar(errMsg, isError = true)
                // Restart polling so auto-detection still works
                pollHandler.postDelayed(pollRunnable, 4_000)
            }
    }

    private fun routeExistingUser(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                if (doc.exists()) { 
                    com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_USER)
                    navigateHome(); return@addOnSuccessListener 
                }
                db.collection("koshais").document(uid).get()
                    .addOnSuccessListener { kDoc ->
                        if (!isAdded) return@addOnSuccessListener
                        if (kDoc.exists()) { 
                            destinationRole = DEST_KOSHAI; 
                            com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI)
                            navigateHome() 
                        }
                        else {
                            resetButton()
                            Toast.makeText(
                                requireContext(),
                                "Registration data was lost. Please register again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .addOnFailureListener { resetButton() }
            }
            .addOnFailureListener { resetButton() }
    }

    private fun navigateHome() {
        if (!isAdded) return
        val action = if (destinationRole == DEST_KOSHAI)
            R.id.action_emailVerificationFragment_to_koshaiDashboardFragment
        else
            R.id.action_emailVerificationFragment_to_userHomeFragment

        findNavController().navigate(
            action, null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true).build()
        )
    }

    private fun resetButton() {
        if (isAdded) {
            binding.btnIveVerified.isEnabled = true
            binding.btnIveVerified.text = "I've Verified — Continue"
        }
    }

    override fun onDestroyView() {
        pollHandler.removeCallbacks(pollRunnable) // Stop polling when leaving
        super.onDestroyView()
        _binding = null
    }
}
