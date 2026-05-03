package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class LiveStatusTrackingActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var etaMinutes = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_status_tracking)

        setupBackButton()
        setupTabs()
        setupCardClicks()
        setupContactButton()
        setupBottomNav()
        startEtaCountdown()
    }

    private fun setupBackButton() {
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
    }

    // ── Tabs ──────────────────────────────────────────────────────────────
    private fun setupTabs() {
        val tabAll    = findViewById<TextView>(R.id.tabAllBookings)
        val tabActive = findViewById<TextView>(R.id.tabActiveBookings)

        tabAll.setOnClickListener {
            tabAll.setBackgroundColor(0xFF1B5E35.toInt()); tabAll.setTextColor(0xFFFFFFFF.toInt())
            tabActive.setBackgroundColor(0xFFFFFFFF.toInt()); tabActive.setTextColor(0xFF888888.toInt())
            toast(getString(R.string.tab_all_bookings))
        }
        tabActive.setOnClickListener {
            tabActive.setBackgroundColor(0xFF1B5E35.toInt()); tabActive.setTextColor(0xFFFFFFFF.toInt())
            tabAll.setBackgroundColor(0xFFFFFFFF.toInt()); tabAll.setTextColor(0xFF888888.toInt())
            toast(getString(R.string.tab_active_bookings))
        }
    }

    // ── Card clicks ───────────────────────────────────────────────────────
    private fun setupCardClicks() {
        findViewById<CardView>(R.id.cardButcherStatus).setOnClickListener {
            toast(getString(R.string.msg_butcher_on_the_way, etaMinutes))
        }
        findViewById<CardView>(R.id.cardToolsMeatStatus).setOnClickListener {
            toast(getString(R.string.msg_tools_delivery_eta))
        }
    }

    // ── Countdown (simulated live update) ────────────────────────────────
    private fun startEtaCountdown() {
        val tvEtaSubtitle = findViewById<TextView>(R.id.tvButcherEtaSubtitle)
        val tvEtaMain = findViewById<TextView>(R.id.tvButcherEtaMain)

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (etaMinutes > 1) {
                    etaMinutes--
                    // Update UI with new ETA
                    tvEtaSubtitle.text = getString(R.string.label_arriving_dynamic, etaMinutes)
                    tvEtaMain.text = getString(R.string.value_dynamic_min, etaMinutes)
                    handler.postDelayed(this, 60_000)
                } else {
                    tvEtaSubtitle.text = getString(R.string.value_arrived)
                    tvEtaMain.text = getString(R.string.value_0_min)
                    toast(getString(R.string.msg_butcher_arrived))
                }
            }
        }, 60_000)
    }

    // ── Contact button ────────────────────────────────────────────────────
    private fun setupContactButton() {
        findViewById<Button>(R.id.btnContactButcher).setOnClickListener {
            toast(getString(R.string.msg_calling_butcher))
        }
    }

    // ── Bottom Navigation ─────────────────────────────────────────────────
    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navBookings).setOnClickListener {
            // Already on tracking/bookings screen
        }
        findViewById<LinearLayout>(R.id.navStore).setOnClickListener {
            startActivity(Intent(this, MeatShopActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navCharity).setOnClickListener {
            // Charity activity doesn't exist yet, avoiding crash by commenting out or using toast
            toast(getString(R.string.msg_coming_soon, getString(R.string.nav_charity_upper)))
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            toast(getString(R.string.msg_coming_soon, getString(R.string.nav_profile_upper)))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}