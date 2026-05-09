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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R

class CharityDistributionFragment : Fragment() {

    private var selectedPortion = 300
    private var selectedOrg     = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_charity_distribution, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        setupPortionButtons(view)
        setupOrgCards(view)
        setupDonateButton(view)
        setupBottomNav(view)
    }

    private fun setupPortionButtons(view: View) {
        val portions = mapOf(
            R.id.btn300Portion  to 300,
            R.id.btn1000Portion to 1000,
            R.id.btn2000Portion to 2000
        )

        portions.forEach { (id, amount) ->
            view.findViewById<TextView>(id).setOnClickListener {
                selectedPortion = amount
                portions.keys.forEach { btnId ->
                    view.findViewById<TextView>(btnId).apply {
                        setBackgroundColor(0xFFF5F5F5.toInt())
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
                    }
                }
                view.findViewById<TextView>(id).apply {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_green))
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
                toast(getString(R.string.msg_portions_selected, amount))
            }
        }
    }

    private fun setupOrgCards(view: View) {
        view.findViewById<CardView>(R.id.cardOrphanage).setOnClickListener {
            selectedOrg = getString(R.string.org_orphanage)
            highlightOrg(view, R.id.cardOrphanage, R.id.cardFloodRelief)
            toast(getString(R.string.msg_org_selected, selectedOrg))
        }
        view.findViewById<CardView>(R.id.cardFloodRelief).setOnClickListener {
            selectedOrg = getString(R.string.org_flood_relief)
            highlightOrg(view, R.id.cardFloodRelief, R.id.cardOrphanage)
            toast(getString(R.string.msg_org_selected, selectedOrg))
        }
    }

    private fun highlightOrg(view: View, selected: Int, other: Int) {
        view.findViewById<CardView>(selected)
            .setCardBackgroundColor(0xFFE8F5E9.toInt()) // Light green highlight
        view.findViewById<CardView>(other)
            .setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun setupDonateButton(view: View) {
        view.findViewById<Button>(R.id.btnDonate).setOnClickListener {
            when {
                selectedOrg.isEmpty() ->
                    toast(getString(R.string.msg_please_select_org))
                else ->
                    toast(getString(R.string.msg_donation_success, selectedPortion, selectedOrg))
            }
        }
    }

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
            // Already here
        }
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            toast("Profile — Coming soon")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
