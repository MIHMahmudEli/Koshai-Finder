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

    // ── Quick Booking cards ────────────────────────────────────────────────────
    private fun setupQuickBooking() {
        findViewById<CardView>(R.id.cardBookButcher).setOnClickListener {
            startActivity(Intent(this, ButcherBookingActivity::class.java))
        }
        findViewById<CardView>(R.id.cardFullService).setOnClickListener {
            toast("Full Service Package — Coming Soon")
        }
        findViewById<CardView>(R.id.cardUrgent).setOnClickListener {
            toast("Urgent Butcher — Finding nearest available koshai...")
        }
    }

    // ── Marketplace cards ──────────────────────────────────────────────────────
    private fun setupMarketplace() {
        findViewById<CardView>(R.id.cardMeatShop).setOnClickListener {
            startActivity(Intent(this, MeatShopActivity::class.java))
        }
        findViewById<CardView>(R.id.cardTools).setOnClickListener {
            startActivity(Intent(this, ToolsAndGearActivity::class.java))
        }
        findViewById<CardView>(R.id.cardCleaning).setOnClickListener {
            startActivity(Intent(this, CleaningServiceActivity::class.java))
        }
        findViewById<CardView>(R.id.cardOffers).setOnClickListener {
            toast("Exclusive Offers — Eid special deals loading...")
        }
        findViewById<CardView>(R.id.cardLiveStatus).setOnClickListener {
            toast("Live Status — Tracking butcher location...")
        }
        findViewById<TextView>(R.id.tvViewAll).setOnClickListener {
            startActivity(Intent(this, MeatShopActivity::class.java))
        }
    }

    // ── Product Add to Cart buttons ────────────────────────────────────────────
    private fun setupProductButtons() {
        findViewById<Button>(R.id.btnAddBeef).setOnClickListener {
            toast("Beef (Per Kg) added to cart — ৳390")
        }
        findViewById<Button>(R.id.btnAddMutton).setOnClickListener {
            toast("Mutton (Per Kg) added to cart — ৳370")
        }
        findViewById<Button>(R.id.btnAddPremium).setOnClickListener {
            toast("Premium Cuts added to cart — ৳360")
        }
    }

    // ── Bottom Navigation ──────────────────────────────────────────────────────
    private fun setupBottomNav() {
        // Home — already here, do nothing
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { /* already here */ }

        findViewById<LinearLayout>(R.id.navBookings).setOnClickListener {
            toast("My Bookings — Coming soon")
        }
        findViewById<LinearLayout>(R.id.navStore).setOnClickListener {
            startActivity(Intent(this, MeatShopActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navCharity).setOnClickListener {
            toast("Charity Distribution — Coming soon")
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            toast("Profile — Coming soon")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}