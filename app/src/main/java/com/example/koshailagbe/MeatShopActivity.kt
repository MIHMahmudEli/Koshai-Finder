package com.example.koshailagbe

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MeatShopActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meat_shop)

        val gridView = findViewById<GridView>(R.id.gridMeatShop)
        val items = arrayOf("Beef 1kg - 750 BDT", "Mutton 1kg - 1000 BDT", "Spices Combo - 350 BDT", "Mustard Oil - 250 BDT")

        gridView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        gridView.setOnItemClickListener { parent, view, position, id ->
            Toast.makeText(this, "Added to Cart: " + items[position], Toast.LENGTH_SHORT).show()
        }
    }
}