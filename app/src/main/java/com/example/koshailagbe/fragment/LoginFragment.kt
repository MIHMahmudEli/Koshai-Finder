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
import com.example.koshailagbe.databinding.FragmentLoginBinding
import com.example.koshailagbe.utils.showSnackBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        
        // Modern Keyboard Animation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            binding.root.setWindowInsetsAnimationCallback(object : android.view.WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(insets: android.view.WindowInsets, animations: MutableList<android.view.WindowInsetsAnimation>): android.view.WindowInsets {
                    val imeInsets = insets.getInsets(android.view.WindowInsets.Type.ime())
                    binding.loginCard.translationY = -(imeInsets.bottom / 2f)
                    
                    // Keep the dim overlay's hole synced with the card's new position
                    val card = binding.loginCard
                    binding.focusDimOverlay.holeRect = android.graphics.RectF(
                        card.x, card.y, card.x + card.width, card.y + card.height
                    )
                    
                    return insets
                }
            })
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnLogin.setOnClickListener { attemptLogin() }

        setupFocusDimming()

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_roleFragment)
        }

        return binding.root
    }

    private fun attemptLogin() {
        val emailPhone = binding.etEmailPhone.text.toString().trim()
        val password   = binding.etPassword.text.toString().trim()

        if (emailPhone.isEmpty() || password.isEmpty()) {
            showSnackBar("Please fill in all fields", isError = true)
            return
        }

        // Accept both email or phone number
        val email = if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailPhone).matches()) {
            emailPhone
        } else {
            // phone number provided — not supported for email login
            showSnackBar("Please enter your registered email address", isError = true)
            return
        }

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                val user = auth.currentUser

                // Check email verification
                if (user?.isEmailVerified == false) {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Login"
                    showSnackBar("Please verify your email first. Check your inbox.", isError = true)
                    // Offer to go to verification screen
                    routeToVerification(user.uid)
                    return@addOnSuccessListener
                }

                routeUserByRole(user?.uid ?: "")
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Login"
                val msg = when {
                    it.message?.contains("no user") == true ||
                    it.message?.contains("identifier") == true -> "No account found with this email."
                    it.message?.contains("password") == true   -> "Incorrect password."
                    else -> "Login failed: ${it.message}"
                }
                showSnackBar(msg, isError = true)
            }
    }

    private fun routeToVerification(uid: String) {
        // Check which role to route to after verification
        db.collection("koshais").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                val dest = if (doc.exists())
                    EmailVerificationFragment.DEST_KOSHAI
                else
                    EmailVerificationFragment.DEST_USER
                findNavController().navigate(
                    R.id.action_loginFragment_to_emailVerificationFragment,
                    bundleOf(EmailVerificationFragment.ARG_DESTINATION to dest)
                )
            }
    }

    private fun routeUserByRole(uid: String) {
        if (uid.isEmpty()) {
            resetLoginButton()
            return
        }
        db.collection("admin").document(uid).get()
            .addOnSuccessListener { adminDoc ->
                if (!isAdded) return@addOnSuccessListener
                if (adminDoc.exists()) {
                    com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_ADMIN)
                    findNavController().navigate(
                        R.id.action_loginFragment_to_adminHomeFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.loginFragment, true).build()
                    )
                } else {
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            if (!isAdded) return@addOnSuccessListener
                            if (userDoc.exists()) {
                                if (userDoc.getBoolean("isBanned") == true) {
                                    resetLoginButton()
                                    showSnackBar("Your account is banned. Contact support.", isError = true)
                                    auth.signOut()
                                    return@addOnSuccessListener
                                }
                                com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_USER)
                                findNavController().navigate(
                                    R.id.action_loginFragment_to_userHomeFragment,
                                    null,
                                    androidx.navigation.NavOptions.Builder()
                                        .setPopUpTo(R.id.loginFragment, true).build()
                                )
                            } else {
                                db.collection("koshais").document(uid).get()
                                    .addOnSuccessListener { koshaiDoc ->
                                        if (!isAdded) return@addOnSuccessListener
                                        if (koshaiDoc.exists()) {
                                            if (koshaiDoc.getBoolean("isBanned") == true) {
                                                resetLoginButton()
                                                showSnackBar("Your account is banned. Contact support.", isError = true)
                                                auth.signOut()
                                                return@addOnSuccessListener
                                            }
                                            com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI)
                                            findNavController().navigate(
                                                R.id.action_loginFragment_to_koshaiDashboardFragment,
                                                null,
                                                androidx.navigation.NavOptions.Builder()
                                                    .setPopUpTo(R.id.loginFragment, true).build()
                                            )
                                        } else {
                                            resetLoginButton()
                                            showSnackBar("Account not found.", isError = true)
                                        }
                                    }
                                    .addOnFailureListener { resetLoginButton() }
                            }
                        }
                        .addOnFailureListener { resetLoginButton() }
                }
            }
            .addOnFailureListener { resetLoginButton() }
    }

    private fun setupFocusDimming() {
        val focusListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Calculate the hole for the glass card
                val card = binding.loginCard
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
                if (!binding.etEmailPhone.hasFocus() && !binding.etPassword.hasFocus()) {
                    binding.focusDimOverlay.animate().alpha(0f).setDuration(300).withEndAction {
                        binding.focusDimOverlay.visibility = View.GONE
                    }.start()
                }
            }
        }
        binding.etEmailPhone.onFocusChangeListener = focusListener
        binding.etPassword.onFocusChangeListener = focusListener
        
        // Hide dim when clicking the overlay itself
        binding.focusDimOverlay.setOnClickListener {
            binding.etEmailPhone.clearFocus()
            binding.etPassword.clearFocus()
            // Hide keyboard
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun resetLoginButton() {
        if (isAdded) {
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "Login"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}