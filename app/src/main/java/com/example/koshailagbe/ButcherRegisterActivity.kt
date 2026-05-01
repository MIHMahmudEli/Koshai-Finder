package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ButcherRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_butcher_register)

        val btnBack      = findViewById<TextView>(R.id.btnBack)
        val name         = findViewById<EditText>(R.id.butcherName)
        val email        = findViewById<EditText>(R.id.butcherEmail)
        val password     = findViewById<EditText>(R.id.butcherPassword)
        val experience   = findViewById<EditText>(R.id.butcherExperience)
        val location     = findViewById<EditText>(R.id.butcherLocation)
        val rateCow      = findViewById<EditText>(R.id.butcherRateCow)
        val rateGoat     = findViewById<EditText>(R.id.butcherRateGoat)
        val createButton = findViewById<Button>(R.id.butcherCreateButton)
        val tvLoginLink  = findViewById<TextView>(R.id.tvLoginLink)

        btnBack.setOnClickListener { finish() }

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        createButton.setOnClickListener {
            val nameVal       = name.text.toString().trim()
            val emailVal      = email.text.toString().trim()
            val passwordVal   = password.text.toString().trim()
            val expVal        = experience.text.toString().trim()
            val locationVal   = location.text.toString().trim()
            val rateCowVal    = rateCow.text.toString().trim()
            val rateGoatVal   = rateGoat.text.toString().trim()

            when {
                nameVal.isEmpty()     -> toast("Please enter your full name")
                emailVal.isEmpty()    -> toast("Please enter your email")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(emailVal).matches() ->
                    toast("Enter a valid email address")
                passwordVal.length < 6 ->
                    toast("Password must be at least 6 characters")
                expVal.isEmpty()      -> toast("Please enter years of experience")
                locationVal.isEmpty() -> toast("Please enter your district/location")
                rateCowVal.isEmpty()  -> toast("Please enter your rate per cow")
                rateGoatVal.isEmpty() -> toast("Please enter your rate per goat")
                else -> {
                    toast("Butcher Account Created! Pending admin verification.")
                    // Clear fields
                    name.text.clear()
                    email.text.clear()
                    password.text.clear()
                    experience.text.clear()
                    location.text.clear()
                    rateCow.text.clear()
                    rateGoat.text.clear()
                    // Go to login
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}