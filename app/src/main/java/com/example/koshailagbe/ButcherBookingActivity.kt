package com.example.koshailagbe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class ButcherBookingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, ButcherBookingFragment())
                .commit()
        }
    }
}

class ButcherBookingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_butcher_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        val btnBack = view.findViewById<TextView>(R.id.btnBack)
        val btnBookNow = view.findViewById<Button>(R.id.btnBookNow)
        val btnBookNowSecondary = view.findViewById<Button>(R.id.btnBookNowSecondary)
        val btnBookNowAlt = view.findViewById<Button>(R.id.btnBookNowAlt)
        val btnAddToOutlier = view.findViewById<Button>(R.id.btnAddToOutlier)

        // Back button click
        btnBack.setOnClickListener {
            if (isAdded) {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    requireActivity().finish()
                }
            }
        }

        // Booking buttons click
        val bookingClickListener = {
            Toast.makeText(requireContext(), "Booking Successful!", Toast.LENGTH_SHORT).show()
        }

        btnBookNow.setOnClickListener { bookingClickListener() }
        btnBookNowSecondary.setOnClickListener { bookingClickListener() }
        btnBookNowAlt.setOnClickListener { bookingClickListener() }

        // Other button
        btnAddToOutlier.setOnClickListener {
            Toast.makeText(requireContext(), "Added to Outlier list", Toast.LENGTH_SHORT).show()
        }
    }
}