package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R

class FullServicePackageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_full_service_package, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        setupServiceCards(view)
        setupWeightSeekBar(view)
        setupBookButtons(view)
    }

    private fun setupServiceCards(view: View) {
        val services = mapOf(
            R.id.cardSlaughter       to R.string.slaughter_msg,
            R.id.cardSkinning        to R.string.skinning_msg,
            R.id.cardCutting         to R.string.cutting_msg,
            R.id.cardCleaningService to R.string.cleaning_msg,
            R.id.cardDisposal        to R.string.disposal_msg
        )
        services.forEach { (id, resId) ->
            view.findViewById<CardView>(id).setOnClickListener {
                toast(getString(resId))
            }
        }
    }

    private fun setupWeightSeekBar(view: View) {
        val seekBar  = view.findViewById<SeekBar>(R.id.seekBarWeight)
        val tvWeight = view.findViewById<TextView>(R.id.tvWeightValue)
        val tvTotal  = view.findViewById<TextView>(R.id.tvTotalPrice)

        // Set initial values based on current progress
        updateWeightAndPrice(seekBar.progress, tvWeight, tvTotal)

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    sb: SeekBar?, progress: Int, fromUser: Boolean
                ) {
                    updateWeightAndPrice(progress, tvWeight, tvTotal)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            }
        )
    }

    private fun updateWeightAndPrice(progress: Int, tvWeight: TextView, tvTotal: TextView) {
        val kg = 100 + (progress * 5) // Matching the 100-600 range (500/100 = 5)
        tvWeight.text = getString(R.string.weight_format, kg)

        // Example pricing logic: Base 1500 + 10 BDT per kg
        val total = 1500 + (kg * 10)
        tvTotal.text = getString(R.string.price_format, total)
    }

    private fun setupBookButtons(view: View) {
        view.findViewById<Button>(R.id.btnBookFullService).setOnClickListener {
            toast(getString(R.string.booked_msg))
        }
        view.findViewById<Button>(R.id.btnBookNowYellow).setOnClickListener {
            toast(getString(R.string.request_sent_msg))
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}