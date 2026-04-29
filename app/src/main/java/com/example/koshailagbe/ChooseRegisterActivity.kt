package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ChooseRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_register)

        val customerButton = findViewById<Button>(R.id.customerButton)
        val butcherButton = findViewById<Button>(R.id.butcherButton)

        customerButton.setOnClickListener {
            startActivity(Intent(this, CustomerRegisterActivity::class.java))
        }

        butcherButton.setOnClickListener {
            startActivity(Intent(this, ButcherRegisterActivity::class.java))
        }
    }
}