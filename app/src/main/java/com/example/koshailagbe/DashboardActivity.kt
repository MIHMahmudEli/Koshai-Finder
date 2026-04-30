package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initializing UI Views
        val cardButcher = findViewById<CardView>(R.id.cardButcherBooking)
        val cardFullService = findViewById<CardView>(R.id.cardFullService)
        val cardUrgent = findViewById<CardView>(R.id.cardUrgent)
        val cardMeatShop = findViewById<CardView>(R.id.cardMeatShop)
        val cardTools = findViewById<CardView>(R.id.cardTools)
        val cardCleaning = findViewById<CardView>(R.id.cardCleaning)

        // Set Listeners to open respective activity
        cardButcher.setOnClickListener {
            startActivity(Intent(this, ButcherBookingActivity::class.java))
        }

        cardFullService.setOnClickListener {
            // Can be redirected to Full Service activity
            Toast.makeText(this, "Full Service Feature Opened", Toast.LENGTH_SHORT).show()
        }

        cardUrgent.setOnClickListener {
            Toast.makeText(this, "Emergency Booking Activated!", Toast.LENGTH_SHORT).show()
        }

        cardMeatShop.setOnClickListener {
            startActivity(Intent(this, MeatShopActivity::class.java))
        }

        cardTools.setOnClickListener {
            startActivity(Intent(this, ToolsAndGearActivity::class.java))
        }

        cardCleaning.setOnClickListener {
            startActivity(Intent(this, CleaningServiceActivity::class.java))
        }
    }
}