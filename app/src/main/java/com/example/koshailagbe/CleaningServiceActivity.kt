package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class CleaningServiceActivity : AppCompatActivity() {

    // Track selections
    private val selectedServices = mutableSetOf<String>()
    private var selectedSlot     = "7:00 AM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cleaning_service)

        setupBackButton()
        setupTabs()
        setupServiceCards()
        setupTimeSlots()
        setupBundle()
        setupBottomNav()
    }

    private fun setupBackButton() {
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
    }

    // ── Tabs ──────────────────────────────────────────────────────────────
    private fun setupTabs() {
        val tab1 = findViewById<TextView>(R.id.tabCleaningOption)
        val tab2 = findViewById<TextView>(R.id.tabCleaningOptions)

        tab1.setOnClickListener {
            tab1.setBackgroundColor(0xFF1B5E35.toInt()); tab1.setTextColor(0xFFFFFFFF.toInt())
            tab2.setBackgroundColor(0xFFFFFFFF.toInt()); tab2.setTextColor(0xFF888888.toInt())
        }
        tab2.setOnClickListener {
            tab2.setBackgroundColor(0xFF1B5E35.toInt()); tab2.setTextColor(0xFFFFFFFF.toInt())
            tab1.setBackgroundColor(0xFFFFFFFF.toInt()); tab1.setTextColor(0xFF888888.toInt())
            toast(getString(R.string.msg_cleaning_options_coming_soon))
        }
    }

    // ── Service card toggles ──────────────────────────────────────────────
    private fun setupServiceCards() {
        toggleCard(R.id.cardSlaughterArea,  R.id.checkSlaughter,  getString(R.string.slaughter_area))
        toggleCard(R.id.cardUtensils,       R.id.checkUtensils,   getString(R.string.utensils))
        setupStarCard(R.id.cardBloodRemoval,  getString(R.string.blood_removal))
        setupStarCard(R.id.cardWasteDisposal, getString(R.string.waste_disposal))
    }

    private fun toggleCard(cardId: Int, checkId: Int, name: String) {
        val card  = findViewById<CardView>(cardId)
        val check = findViewById<TextView>(checkId)
        var selected = false

        card.setOnClickListener {
            selected = !selected
            if (selected) {
                selectedServices.add(name)
                check.text = getString(R.string.select_selected)
                card.setCardBackgroundColor(0xFFE8F5E9.toInt())
            } else {
                selectedServices.remove(name)
                check.text = getString(R.string.select_unselected)
                card.setCardBackgroundColor(0xFFFFFFFF.toInt())
            }
        }
    }

    private fun setupStarCard(cardId: Int, name: String) {
        val card = findViewById<CardView>(cardId)
        var selected = false
        card.setOnClickListener {
            selected = !selected
            if (selected) {
                selectedServices.add(name)
                card.setCardBackgroundColor(0xFFE8F5E9.toInt())
                toast(getString(R.string.msg_service_added, name))
            } else {
                selectedServices.remove(name)
                card.setCardBackgroundColor(0xFFFFFFFF.toInt())
                toast(getString(R.string.msg_service_removed, name))
            }
        }
    }

    // ── Time slot selection ───────────────────────────────────────────────
    private fun setupTimeSlots() {
        val slots = listOf(
            Pair(R.id.slot7am,  getString(R.string.slot_7am)),
            Pair(R.id.slot12pm, getString(R.string.slot_12pm)),
            Pair(R.id.slot9pm,  getString(R.string.slot_9pm))
        )

        slots.forEach { (id, time) ->
            findViewById<TextView>(id).setOnClickListener {
                selectedSlot = time
                // Reset all to unselected look
                slots.forEach { (sid, _) ->
                    val tv = findViewById<TextView>(sid)
                    tv.setTextColor(0xFF888888.toInt())
                    tv.setBackgroundResource(R.drawable.slot_bg_unselected)
                }
                // Mark selected
                val tv = findViewById<TextView>(id)
                tv.setTextColor(0xFF1B5E35.toInt())
                tv.setBackgroundResource(R.drawable.slot_bg_selected)
                toast(getString(R.string.msg_time_slot_selected, time))
            }
        }
    }

    // ── Get Bundle button ─────────────────────────────────────────────────
    private fun setupBundle() {
        findViewById<Button>(R.id.btnGetBundle).setOnClickListener {
            if (selectedServices.isEmpty()) {
                toast(getString(R.string.msg_select_service))
            } else {
                toast(getString(R.string.msg_bundle_booked, selectedServices.joinToString(", "), selectedSlot))
            }
        }
    }

    // ── Bottom Navigation ─────────────────────────────────────────────────
    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navBookings).setOnClickListener {
            toast(getString(R.string.msg_coming_soon, getString(R.string.nav_bookings)))
        }
        findViewById<LinearLayout>(R.id.navStore).setOnClickListener {
            startActivity(Intent(this, MeatShopActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navCharity).setOnClickListener {
            toast(getString(R.string.msg_coming_soon, getString(R.string.nav_charity)))
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            toast(getString(R.string.msg_coming_soon, getString(R.string.nav_profile)))
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}