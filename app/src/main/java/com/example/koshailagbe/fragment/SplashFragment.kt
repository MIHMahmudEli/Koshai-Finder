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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUser()
        }, 1500)

        return view
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
            navigate(R.id.action_splashFragment_to_koshaiHomeFragment)
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
                                com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_USER)
                                navigate(R.id.action_splashFragment_to_userHomeFragment)
                            } else {
                                // Not a user — check koshais
                                db.collection("koshais").document(uid).get()
                                    .addOnSuccessListener { koshaiDoc ->
                                        if (!isAdded) return@addOnSuccessListener
                                        when {
                                            koshaiDoc.exists() -> {
                                                com.example.koshailagbe.utils.SharedPrefsHelper.saveUserRole(requireContext(), com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI)
                                                navigate(R.id.action_splashFragment_to_koshaiHomeFragment)
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
}
