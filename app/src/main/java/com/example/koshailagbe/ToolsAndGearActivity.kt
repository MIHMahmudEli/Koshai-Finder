package com.example.koshailagbe

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ToolsAndGearActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tools_and_gear)

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnBuyKnife = findViewById<Button>(R.id.btnBuyKnife)
        val btnBuyBlock = findViewById<Button>(R.id.btnBuyBlock)
        val btnBuyBlote = findViewById<Button>(R.id.btnBuyBlote)
        val btnBuyRope = findViewById<Button>(R.id.btnBuyRope)
        val btnGetBundle = findViewById<Button>(R.id.btnGetBundle)

        btnBack.setOnClickListener {
            finish()
        }

        btnBuyKnife.setOnClickListener {
            showToast(getString(R.string.msg_item_added, getString(R.string.product_knives_kulves)))
        }

        btnBuyBlock.setOnClickListener {
            showToast(getString(R.string.msg_item_added, getString(R.string.product_chopping_blocks)))
        }

        btnBuyBlote.setOnClickListener {
            showToast(getString(R.string.msg_item_added, getString(R.string.product_chopping_blotes)))
        }

        btnBuyRope.setOnClickListener {
            showToast(getString(R.string.msg_item_added, getString(R.string.product_ropes_ropes)))
        }

        btnGetBundle.setOnClickListener {
            showToast(getString(R.string.msg_bundle_added))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}