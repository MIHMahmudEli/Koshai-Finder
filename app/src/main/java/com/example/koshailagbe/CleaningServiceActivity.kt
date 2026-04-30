package com.example.koshailagbe

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CleaningServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cleaning_service)

        findViewById<Button>(R.id.btnOrderService).setOnClickListener {
            Toast.makeText(this, "Cleaning Service booked!", Toast.LENGTH_SHORT).show()
        }
    }
}