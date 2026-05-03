package com.example.koshailagbe

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class FullServicePackageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_service_package)

        setupBackButton()
        setupServiceCards()
        setupWeightSeekBar()
        setupBookButtons()
    }

    private fun setupBackButton() {
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupServiceCards() {
        val services = mapOf(
            R.id.cardSlaughter     to "Slaughter service included ✓",
            R.id.cardSkinning      to "Skinning service included ✓",
            R.id.cardCutting       to "Cutting service included ✓",
            R.id.cardCleaningService to "Cleaning service included ✓",
            R.id.cardDisposal      to "Disposal service included ✓"
        )
        services.forEach { (id, msg) ->
            findViewById<CardView>(id).setOnClickListener { toast(msg) }
        }
    }

    private fun setupWeightSeekBar() {
        val seekBar     = findViewById<SeekBar>(R.id.seekBarWeight)
        val tvWeight    = findViewById<TextView>(R.id.tvWeightValue)
        val tvTotal     = findViewById<TextView>(R.id.tvTotalPrice)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                // Map 0–100 → 100–500 kg
                val kg = 100 + (progress * 4)
                tvWeight.text = "Selected: ~$kg kg"

                // Recalculate price based on weight
                val base  = 1900
                val extra = (progress / 10) * 150
                val total = base + extra
                tvTotal.text = "৳${String.format("%,d", total)}"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun setupBookButtons() {
        findViewById<Button>(R.id.btnBookFullService).setOnClickListener {
            toast("Full Service Package booked! We'll confirm shortly.")
        }
        findViewById<Button>(R.id.btnBookNowYellow).setOnClickListener {
            toast("Booking request sent! Our team will contact you.")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}