package com.example.koshailagbe

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ButcherRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_butcher_register)

        val name = findViewById<EditText>(R.id.butcherName)
        val email = findViewById<EditText>(R.id.butcherEmail)
        val password = findViewById<EditText>(R.id.butcherPassword)
        val experience = findViewById<EditText>(R.id.butcherExperience)
        val location = findViewById<EditText>(R.id.butcherLocation)

        val createButton = findViewById<Button>(R.id.butcherCreateButton)

        createButton.setOnClickListener {

            if (name.text.isEmpty() ||
                email.text.isEmpty() ||
                password.text.isEmpty() ||
                experience.text.isEmpty() ||
                location.text.isEmpty()) {

                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()

            } else {

                Toast.makeText(this, "Butcher Account Created", Toast.LENGTH_SHORT).show()

                name.text.clear()
                email.text.clear()
                password.text.clear()
                experience.text.clear()
                location.text.clear()
            }
        }
    }
}