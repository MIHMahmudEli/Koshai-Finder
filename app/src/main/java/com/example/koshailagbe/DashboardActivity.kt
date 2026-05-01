package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setupQuickBooking()
        setupMarketplace()
        setupBottomNav()
        setupProductButtons()
    }

    // ── Quick Booking buttons ─────────────────────────────────────────────
    private fun setupQuickBooking() {
        findViewById<CardView>(R.id.btnBookButcher).setOnClickListener {
            startActivity(Intent(this, ButcherBookingActivity::class.java))
        }

        findViewById<CardView>(R.id.btnFullService).setOnClickListener {
            toast(getString(R.string.msg_coming_soon, getString(R.string.full_service_label)))
        }

        findViewById<CardView>(R.id.btnUrgentButcher).setOnClickListener {
            toast(getString(R.string.msg_urgent_butcher))
        }
    }

    // ── Marketplace buttons ───────────────────────────────────────────────
    private fun setupMarketplace() {
        findViewById<CardView>(R.id.btnMeatShop).setOnClickListener {
            startActivity(Intent(this, MeatShopActivity::class.java))
        }

        findViewById<CardView>(R.id.btnToolsGear).setOnClickListener {
            startActivity(Intent(this, ToolsAndGearActivity::class.java))
        }

        findViewById<CardView>(R.id.btnCleaningService).setOnClickListener {
            startActivity(Intent(this, CleaningServiceActivity::class.java))
        }

        findViewById<CardView>(R.id.btnExclusiveOffers).setOnClickListener {
            toast(getString(R.string.msg_exclusive_offers))
        }

        findViewById<CardView>(R.id.btnLiveStatus).setOnClickListener {
            toast(getString(R.string.msg_live_status))
        }

        findViewById<TextView>(R.id.tvViewAll).setOnClickListener {
            startActivity(Intent(this, MeatShopActivity::class.java))
        }
    }

    // ── Product buttons ──────────────────────────────────────────────────
    private fun setupProductButtons() {
        findViewById<Button>(R.id.btnAddBeef).setOnClickListener {
            toast(getString(R.string.msg_added_to_cart_price, getString(R.string.beef_per_kg), "৳390"))
        }

        findViewById<Button>(R.id.btnAddMutton).setOnClickListener {
            toast(getString(R.string.msg_added_to_cart_price, getString(R.string.mutton_per_kg), "৳370"))
        }

        findViewById<Button>(R.id.btnAddPremium).setOnClickListener {
            toast(getString(R.string.msg_added_to_cart_price, getString(R.string.premium_cuts), "৳360"))
        }
    }

    // ── Bottom Navigation ────────────────────────────────────────────────
    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            // Already here
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

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}