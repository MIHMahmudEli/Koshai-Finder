package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R

class ToolsAndGearFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tools_and_gear, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        // Tabs
        val tabHardware = view.findViewById<TextView>(R.id.tabHardware)
        val tabKnives   = view.findViewById<TextView>(R.id.tabKnives)

        tabHardware.setOnClickListener {
            tabHardware.setBackgroundColor(0xFF1B5E35.toInt())
            tabHardware.setTextColor(0xFFFFFFFF.toInt())
            tabKnives.setBackgroundColor(0xFFFFFFFF.toInt())
            tabKnives.setTextColor(0xFF888888.toInt())
            toast("Hardware tools")
        }

        tabKnives.setOnClickListener {
            tabKnives.setBackgroundColor(0xFF1B5E35.toInt())
            tabKnives.setTextColor(0xFFFFFFFF.toInt())
            tabHardware.setBackgroundColor(0xFFFFFFFF.toInt())
            tabHardware.setTextColor(0xFF888888.toInt())
            toast("Knives")
        }

        // BUY buttons
        view.findViewById<Button>(R.id.btnBuyKnife).setOnClickListener {
            toast("Knives Kulves added — ৳450")
        }
        view.findViewById<Button>(R.id.btnBuyBlock).setOnClickListener {
            toast("Chopping Blocks added — ৳350")
        }
        view.findViewById<Button>(R.id.btnBuyBlote).setOnClickListener {
            toast("Chopping Blotes added — ৳280")
        }
        view.findViewById<Button>(R.id.btnBuyRope).setOnClickListener {
            toast("Ropes added — ৳150")
        }

        // GET BUNDLE
        view.findViewById<Button>(R.id.btnGetBundle).setOnClickListener {
            toast("Bundle added to cart! Total: ৳1,230")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}