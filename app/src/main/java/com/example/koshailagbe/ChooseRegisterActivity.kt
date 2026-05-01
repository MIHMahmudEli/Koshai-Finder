package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ChooseRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_register)

        val btnBack      = findViewById<TextView>(R.id.btnBack)
        val customerCard = findViewById<CardView>(R.id.customerButton)
        val butcherCard  = findViewById<CardView>(R.id.butcherButton)
        val tvLoginLink  = findViewById<TextView>(R.id.tvLoginLink)

        btnBack.setOnClickListener { finish() }

        customerCard.setOnClickListener {
            startActivity(Intent(this, CustomerRegisterActivity::class.java))
        }

        butcherCard.setOnClickListener {
            startActivity(Intent(this, ButcherRegisterActivity::class.java))
        }

        tvLoginLink.setOnClickListener {
            // Go back to login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}