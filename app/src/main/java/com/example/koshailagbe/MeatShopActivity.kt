package com.example.koshailagbe

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MeatShopActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meat_shop)

        // Find views
        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnAddBeef = findViewById<Button>(R.id.btnAddBeef)
        val btnAddMutton = findViewById<Button>(R.id.btnAddMutton)
        val btnAddRibs = findViewById<Button>(R.id.btnAddRibs)
        val btnAddKeema = findViewById<Button>(R.id.btnAddKeema)
        val btnAddToCart = findViewById<Button>(R.id.btnAddToCart)

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Add buttons
        val addClickListener = { itemName: String ->
            Toast.makeText(this, getString(R.string.added_to_cart_msg, itemName), Toast.LENGTH_SHORT).show()
        }

        btnAddBeef.setOnClickListener { addClickListener("Beef") }
        btnAddMutton.setOnClickListener { addClickListener("Mutton") }
        btnAddRibs.setOnClickListener { addClickListener("Ribs") }
        btnAddKeema.setOnClickListener { addClickListener("Keema") }

        btnAddToCart.setOnClickListener {
            Toast.makeText(this, "Items added to your cart!", Toast.LENGTH_SHORT).show()
        }
    }
}