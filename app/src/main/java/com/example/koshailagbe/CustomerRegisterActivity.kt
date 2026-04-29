package com.example.koshailagbe

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CustomerRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_register)

        val name = findViewById<EditText>(R.id.customerName)
        val email = findViewById<EditText>(R.id.customerEmail)
        val password = findViewById<EditText>(R.id.customerPassword)
        val phone = findViewById<EditText>(R.id.customerPhone)
        val createButton = findViewById<Button>(R.id.customerCreateButton)

        createButton.setOnClickListener {

            if (name.text.isEmpty() ||
                email.text.isEmpty() ||
                password.text.isEmpty() ||
                phone.text.isEmpty()) {

                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()

            } else {

                Toast.makeText(this, "Customer Account Created", Toast.LENGTH_SHORT).show()

                name.text.clear()
                email.text.clear()
                password.text.clear()
                phone.text.clear()
            }
        }
    }
}