package com.example.koshailagbe

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CleaningServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cleaning_service)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnOrderService).setOnClickListener {
            Toast.makeText(this, getString(R.string.cleaning_service_booked), Toast.LENGTH_SHORT).show()
        }
    }
}