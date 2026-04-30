package com.example.koshailagbe

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ButcherBookingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_butcher_booking)

        val btnBook = findViewById<Button>(R.id.btnBook)
        btnBook.setOnClickListener {
            Toast.makeText(this, "Butcher Booked Successfully", Toast.LENGTH_SHORT).show()
        }
    }
}