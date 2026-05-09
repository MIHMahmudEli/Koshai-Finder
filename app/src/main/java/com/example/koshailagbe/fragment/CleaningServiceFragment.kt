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

class CleaningServiceFragment : Fragment() {

    private val selectedServices = mutableSetOf<String>()
    private var selectedSlot = "7:00 AM"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_cleaning_service, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        setupTabs(view)
        setupServiceCards(view)
        setupTimeSlots(view)
        setupBundleButton(view)
        setupBottomNav(view)
    }

    private fun setupTabs(view: View) {
        val tab1 = view.findViewById<TextView>(R.id.tabCleaningOption)
        val tab2 = view.findViewById<TextView>(R.id.tabCleaningOptions)

        tab1.setOnClickListener {
            tab1.setBackgroundColor(0xFF1B5E35.toInt())
            tab1.setTextColor(0xFFFFFFFF.toInt())
            tab2.setBackgroundColor(0xFFFFFFFF.toInt())
            tab2.setTextColor(0xFF888888.toInt())
        }
        tab2.setOnClickListener {
            tab2.setBackgroundColor(0xFF1B5E35.toInt())
            tab2.setTextColor(0xFFFFFFFF.toInt())
            tab1.setBackgroundColor(0xFFFFFFFF.toInt())
            tab1.setTextColor(0xFF888888.toInt())
            toast("Cleaning Options — Coming soon")
        }
    }

    private fun setupServiceCards(view: View) {
        toggleCard(view, R.id.cardSlaughterArea, R.id.checkSlaughter, "Slaughter Area")
        toggleCard(view, R.id.cardUtensils, R.id.checkUtensils, "Utensils")
        toggleStarCard(view, R.id.cardBloodRemoval, "Blood Removal")
        toggleStarCard(view, R.id.cardWasteDisposal, "Waste Disposal")
    }

    private fun toggleCard(view: View, cardId: Int, checkId: Int, name: String) {
        val card  = view.findViewById<CardView>(cardId)
        val check = view.findViewById<TextView>(checkId)
        var selected = false

        card.setOnClickListener {
            selected = !selected
            if (selected) {
                selectedServices.add(name)
                check.text = getString(R.string.selected_checkbox)
                card.setCardBackgroundColor(0xFFE8F5E9.toInt())
            } else {
                selectedServices.remove(name)
                check.text = getString(R.string.unselected_checkbox)
                card.setCardBackgroundColor(0xFFFFFFFF.toInt())
            }
        }
    }

    private fun toggleStarCard(view: View, cardId: Int, name: String) {
        val card = view.findViewById<CardView>(cardId)
        var selected = false
        card.setOnClickListener {
            selected = !selected
            if (selected) {
                selectedServices.add(name)
                card.setCardBackgroundColor(0xFFE8F5E9.toInt())
                toast("$name added ✓")
            } else {
                selectedServices.remove(name)
                card.setCardBackgroundColor(0xFFFFFFFF.toInt())
                toast("$name removed")
            }
        }
    }

    private fun setupTimeSlots(view: View) {
        val slots = listOf(
            Pair(R.id.slot7am,  "7:00 AM"),
            Pair(R.id.slot12pm, "12:00 PM"),
            Pair(R.id.slot9pm,  "9:00 PM")
        )

        slots.forEach { (id, time) ->
            view.findViewById<TextView>(id).setOnClickListener {
                selectedSlot = time
                slots.forEach { (sid, _) ->
                    val tv = view.findViewById<TextView>(sid)
                    tv.setTextColor(0xFF888888.toInt())
                    tv.setBackgroundResource(R.drawable.slot_bg_unselected)
                }
                val tv = view.findViewById<TextView>(id)
                tv.setTextColor(0xFF1B5E35.toInt())
                tv.setBackgroundResource(R.drawable.slot_bg_selected)
                toast("Time slot: $time selected")
            }
        }
    }

    private fun setupBundleButton(view: View) {
        view.findViewById<Button>(R.id.btnGetBundle).setOnClickListener {
            if (selectedServices.isEmpty()) {
                toast("Please select at least one service")
            } else {
                toast("Bundle booked: ${selectedServices.joinToString(", ")} at $selectedSlot ✓")
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
            findNavController().navigate(R.id.charityDistributionFragment)
        }
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            toast("Profile — Coming soon")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}