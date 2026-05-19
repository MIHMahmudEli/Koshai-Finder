package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.example.koshailagbe.databinding.FragmentRegisterUserBinding
import com.example.koshailagbe.utils.showSnackBar
import com.example.koshailagbe.utils.SharedPrefsHelper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import android.graphics.RectF
import android.view.inputmethod.InputMethodManager
import android.content.Context
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
                    val imeInsets = insets.getInsets(android.view.WindowInsets.Type.ime())
                    binding.registrationCard.translationY = -(imeInsets.bottom / 3f)
                    
                    val card = binding.registrationCard
                    binding.focusDimOverlay.holeRect = RectF(
                        card.x, card.y + card.translationY, card.x + card.width, card.y + card.height + card.translationY
                    )
                    
                    return insets
                }
            })
        }

        auth = FirebaseAuth.getInstance()
        setupFocusDimming()
        binding.btnRegisterUser.setOnClickListener { attemptRegister() }
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

        // Validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || district.isEmpty() ||
            upazila.isEmpty() || password.isEmpty()
        ) {
            showSnackBar(getString(R.string.error_fill_all_fields), isError = true)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.msg_valid_email); return
        } else binding.etEmail.error = null

        if (phone.length < 11) {
            binding.etPhone.error = getString(R.string.error_invalid_phone); return
        } else binding.etPhone.error = null

        if (password.length < 6) {
            binding.etPassword.error = getString(R.string.msg_password_length); return
        } else binding.etPassword.error = null

        binding.btnRegisterUser.isEnabled = false
        binding.btnRegisterUser.text = getString(R.string.msg_submitting)

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
            "credits"      to 0,
            "isBanned"     to false
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
                    binding.btnRegisterUser.text = getString(R.string.create_account_button)
                    showSnackBar(getString(R.string.msg_verification_email_sent))
                    findNavController().navigate(
                        R.id.action_registerUserFragment_to_emailVerificationFragment,
                        bundleOf(EmailVerificationFragment.ARG_DESTINATION to EmailVerificationFragment.DEST_USER)
                    )
                }
            }
            .addOnFailureListener { ex ->
                if (!isAdded) return@addOnFailureListener
                binding.btnRegisterUser.isEnabled = true
                binding.btnRegisterUser.text = getString(R.string.create_account_button)

                if (ex.message?.contains("already in use") == true) {
                    handleExistingUnverifiedAccount(email, password)
                } else {
                    PendingRegistration.clear(requireContext())
                    showSnackBar(getString(R.string.error_update_failed, ex.message), isError = true)
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
                    PendingRegistration.clear(requireContext())
                    showSnackBar(getString(R.string.msg_account_exists_login), isError = true)
                    findNavController().popBackStack()
                } else {
                    user.sendEmailVerification()?.addOnCompleteListener {
                        if (!isAdded) return@addOnCompleteListener
                        showSnackBar(getString(R.string.msg_verification_email_resent))
                        findNavController().navigate(
                            R.id.action_registerUserFragment_to_emailVerificationFragment,
                            bundleOf(EmailVerificationFragment.ARG_DESTINATION to EmailVerificationFragment.DEST_USER)
                        )
                    }
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                PendingRegistration.clear(requireContext())
                showSnackBar(getString(R.string.msg_already_registered_login), isError = true)
            }
    }

    private fun setupFocusDimming() {
        val focusListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val card = binding.registrationCard
                val rect = RectF(
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
                val anyFocused = binding.etName.hasFocus() || binding.etEmail.hasFocus() || 
                                 binding.etPhone.hasFocus() || binding.etDistrict.hasFocus() || 
                                 binding.etUpazila.hasFocus() || binding.etPassword.hasFocus() ||
                                 binding.etReferral.hasFocus()
                
                if (!anyFocused) {
                    binding.focusDimOverlay.animate().alpha(0f).setDuration(300).withEndAction {
                        binding.focusDimOverlay.visibility = View.GONE
                    }.start()
                }
            }
        }
        
        val editTexts = listOf(
            binding.etName, binding.etEmail, binding.etPhone,
            binding.etDistrict, binding.etUpazila, binding.etPassword, binding.etReferral
        )
        editTexts.forEach { it.onFocusChangeListener = focusListener }
        
        binding.focusDimOverlay.setOnClickListener {
            editTexts.forEach { it.clearFocus() }
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
