package com.example.koshailagbe.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var _binding: com.example.koshailagbe.databinding.FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = com.example.koshailagbe.databinding.FragmentSplashBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        startBrandingAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            if (com.example.koshailagbe.utils.NetworkUtils.isInternetAvailable(requireContext())) {
                checkOnboardingAndUser()
            } else {
                findNavController().navigate(R.id.offlineFragment)
            }
        }, 2500) // Longer delay for animation

        return binding.root
    }

    private fun startBrandingAnimations() {
        // Logo entrance: Scale and Fade
        binding.cardLogo.alpha = 0f
        binding.cardLogo.scaleX = 0.5f
        binding.cardLogo.scaleY = 0.5f
        binding.cardLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        // Text entrance: Slide up and Fade
        binding.tvAppName.alpha = 0f
        binding.tvAppName.translationY = 50f
        binding.tvAppName.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(500)
            .start()

        binding.tvAppSlogan.alpha = 0f
        binding.tvAppSlogan.translationY = 30f
        binding.tvAppSlogan.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(800)
            .start()
    }

    private fun checkOnboardingAndUser() {
        if (!isAdded) return
        
        if (com.example.koshailagbe.utils.SharedPrefsHelper.isFirstRun(requireContext())) {
            findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
        } else {
            checkUser()
        }
    }

    private fun checkUser() {
        if (!isAdded) return

        val currentUser = auth.currentUser

        // No session — go to login
        if (currentUser == null) {
            goToLogin()
            return
        }

        // Session exists but email not verified — sign out and go to login
        if (!currentUser.isEmailVerified) {
            auth.signOut()
            goToLogin()
            return
        }

        val uid = currentUser.uid

        // Check local shared preferences first for fast routing
        val savedRole = com.example.koshailagbe.utils.SharedPrefsHelper.getUserRole(requireContext())
        if (savedRole == com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_USER) {
            navigate(R.id.action_splashFragment_to_userHomeFragment)
            return
        } else if (savedRole == com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI) {
            navigate(R.id.action_splashFragment_to_koshaiDashboardFragment)
            return
        } else if (savedRole == com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_ADMIN) {
            navigate(R.id.action_splashFragment_to_adminHomeFragment)
            return
        }

        // Check which role this user has
        db.collection("admin").document(uid).get()
            .addOnSuccessListener { adminDoc ->
                if (!isAdded) return@addOnSuccessListener
                if (adminDoc.exists()) {
                    com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_ADMIN)
                    navigate(R.id.action_splashFragment_to_adminHomeFragment)
                } else {
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            if (!isAdded) return@addOnSuccessListener
                            if (userDoc.exists()) {
                                if (userDoc.getBoolean("isBanned") == true) {
                                    auth.signOut()
                                    goToLogin()
                                    return@addOnSuccessListener
                                }
                                com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_USER)
                                navigate(R.id.action_splashFragment_to_userHomeFragment)
                            } else {
                                // Not a user — check koshais
                                db.collection("koshais").document(uid).get()
                                    .addOnSuccessListener { koshaiDoc ->
                                        if (!isAdded) return@addOnSuccessListener
                                        when {
                                        koshaiDoc.exists() -> {
                                            if (koshaiDoc.getBoolean("isBanned") == true) {
                                                auth.signOut()
                                                goToLogin()
                                                return@addOnSuccessListener
                                            }
                                            com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI)
                                            navigate(R.id.action_splashFragment_to_koshaiDashboardFragment)
                                        }
                                            else -> {
                                                // Logged in but no profile — go to role selection
                                                navigate(R.id.action_splashFragment_to_roleFragment)
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                        if (!isAdded) return@addOnFailureListener
                                        auth.signOut()
                                        goToLogin()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            if (!isAdded) return@addOnFailureListener
                            auth.signOut()
                            goToLogin()
                        }
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                auth.signOut()
                goToLogin()
            }
    }

    private fun navigate(actionId: Int) {
        if (!isAdded) return

        // Auto-subscribe to relevant topics for announcements
        val fcm = com.google.firebase.messaging.FirebaseMessaging.getInstance()
        fcm.subscribeToTopic("announcements")
        
        val role = com.example.koshailagbe.utils.SharedPrefsHelper.getUserRole(requireContext())
        if (role == com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_USER) {
            fcm.subscribeToTopic("all_users")
            fcm.unsubscribeFromTopic("all_koshais")
        } else if (role == com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI) {
            fcm.subscribeToTopic("all_koshais")
            fcm.unsubscribeFromTopic("all_users")
        }

        findNavController().navigate(
            actionId,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.splashFragment, true)
                .build()
        )
    }

    private fun goToLogin() {
        if (!isAdded) return
        findNavController().navigate(
            R.id.action_splashFragment_to_loginFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.splashFragment, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
