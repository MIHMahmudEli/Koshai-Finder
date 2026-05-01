package com.example.koshailagbe

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ButcherBookingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_butcher_booking)

        // Find views
        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnBookNow = findViewById<Button>(R.id.btnBookNow)
        val btnBookNowSecondary = findViewById<Button>(R.id.btnBookNowSecondary)
        val btnBookNowAlt = findViewById<Button>(R.id.btnBookNowAlt)
        val btnAddToOutlier = findViewById<Button>(R.id.btnAddToOutlier)

        // Back button click
        btnBack.setOnClickListener {
            finish()
        }

        // Booking buttons click
        val bookingClickListener = {
            Toast.makeText(this, "Booking Successful!", Toast.LENGTH_SHORT).show()
        }

        btnBookNow.setOnClickListener { bookingClickListener() }
        btnBookNowSecondary.setOnClickListener { bookingClickListener() }
        btnBookNowAlt.setOnClickListener { bookingClickListener() }

        // Other buttons
        btnAddToOutlier.setOnClickListener {
            Toast.makeText(this, "Added to Outlier list", Toast.LENGTH_SHORT).show()
        }
    }
}