package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CustomerRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_register)

        val btnBack      = findViewById<TextView>(R.id.btnBack)
        val name         = findViewById<EditText>(R.id.customerName)
        val email        = findViewById<EditText>(R.id.customerEmail)
        val password     = findViewById<EditText>(R.id.customerPassword)
        val phone        = findViewById<EditText>(R.id.customerPhone)
        val createButton = findViewById<Button>(R.id.customerCreateButton)
        val tvLoginLink  = findViewById<TextView>(R.id.tvLoginLink)

        btnBack.setOnClickListener { finish() }

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        createButton.setOnClickListener {
            val nameVal     = name.text.toString().trim()
            val emailVal    = email.text.toString().trim()
            val passwordVal = password.text.toString().trim()
            val phoneVal    = phone.text.toString().trim()

            when {
                nameVal.isEmpty()     -> toast("Please enter your full name")
                emailVal.isEmpty()    -> toast("Please enter your email")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(emailVal).matches() ->
                    toast("Enter a valid email address")
                passwordVal.length < 6 ->
                    toast("Password must be at least 6 characters")
                phoneVal.isEmpty()    -> toast("Please enter your phone number")
                phoneVal.length < 11  -> toast("Enter a valid phone number")
                else -> {
                    toast("Customer Account Created Successfully!")
                    // Clear fields
                    name.text.clear()
                    email.text.clear()
                    password.text.clear()
                    phone.text.clear()
                    // Go to Dashboard
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}