package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAccountCards(view)
        setupAppControlCards(view)
        setupSecurityCards(view)
        setupActionButtons(view)
        setupBottomNav(view)
    }

    // ── Account & Personal Info cards ─────────────────────────────────────
    private fun setupAccountCards(view: View) {
        view.findViewById<CardView>(R.id.cardPersonalDetails).setOnClickListener {
            toast("Personal Details — Coming soon")
        }
        view.findViewById<CardView>(R.id.cardAddressBook).setOnClickListener {
            toast("Address Book — Coming soon")
        }
        view.findViewById<CardView>(R.id.cardPaymentMethods).setOnClickListener {
            toast("Payment Methods — Coming soon")
        }
        view.findViewById<TextView>(R.id.btnEdit).setOnClickListener {
            toast("Edit Profile — Coming soon")
        }
    }

    // ── App Controls cards ────────────────────────────────────────────────
    private fun setupAppControlCards(view: View) {
        view.findViewById<CardView>(R.id.cardMyBookings).setOnClickListener {
            toast("My Bookings — Coming soon")
        }
        view.findViewById<CardView>(R.id.cardEidToolsOrders).setOnClickListener {
            toast("Eid Tools Orders — Coming soon")
        }
        view.findViewById<CardView>(R.id.cardServicePreferences).setOnClickListener {
            toast("Service Preferences — Coming soon")
        }
    }

    // ── Security & Support cards ──────────────────────────────────────────
    private fun setupSecurityCards(view: View) {
        view.findViewById<CardView>(R.id.cardChangePassword).setOnClickListener {
            toast("Change Password — Coming soon")
        }
        view.findViewById<CardView>(R.id.cardNotificationSettings).setOnClickListener {
            toast("Notification Settings — Coming soon")
        }
        view.findViewById<CardView>(R.id.cardSupportHelp).setOnClickListener {
            toast("Support & Help — Coming soon")
        }
    }

    // ── Action Buttons ────────────────────────────────────────────────────
    private fun setupActionButtons(view: View) {

        // Switch to Koshai Mode
        view.findViewById<Button>(R.id.btnSwitchKoshai).setOnClickListener {
            // Hardcoded koshai login check
            // যখন real auth আসবে তখন এটা replace হবে
            findNavController().navigate(R.id.action_profile_to_koshaiLogin)
        }

        // Log Out
        view.findViewById<Button>(R.id.btnLogOut).setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_login)
        }

        // Delete Account
        view.findViewById<Button>(R.id.btnDeleteAccount).setOnClickListener {
            toast("Delete Account — Are you sure?")
        }
    }

    // ── Bottom Navigation ─────────────────────────────────────────────────
    private fun setupBottomNav(view: View) {
        view.findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            findNavController().navigate(R.id.dashboardFragment)
        }
        view.findViewById<LinearLayout>(R.id.navBookings).setOnClickListener {
            toast("My Bookings — Coming soon")
        }
        view.findViewById<LinearLayout>(R.id.navStore).setOnClickListener {
            findNavController().navigate(R.id.meatShopFragment)
        }
        view.findViewById<LinearLayout>(R.id.navCharity).setOnClickListener {
            findNavController().navigate(R.id.charityDistributionFragment)
        }
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            // Already here
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}