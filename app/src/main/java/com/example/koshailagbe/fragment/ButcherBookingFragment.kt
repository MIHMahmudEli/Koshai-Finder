package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R

class ButcherBookingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_butcher_booking, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<Button>(R.id.btnBookNow1).setOnClickListener {
            toast("Booking confirmed with Top-Rated Butcher!")
        }

        view.findViewById<Button>(R.id.btnBookNow2).setOnClickListener {
            toast("Butcher #2 — Booking confirmed!")
        }

        view.findViewById<CardView>(R.id.cardButcher1).setOnClickListener {
            toast("Butcher profile selected")
        }

        view.findViewById<Button>(R.id.btnSelectButcher1).setOnClickListener {
            toast("Added to your booking list ✓")
        }

        view.findViewById<Button>(R.id.btnBookNowFinal).setOnClickListener {
            toast("Booking request sent!")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}