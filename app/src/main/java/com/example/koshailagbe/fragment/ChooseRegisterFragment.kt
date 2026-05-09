package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R

class ChooseRegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_choose_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack      = view.findViewById<TextView>(R.id.btnBack)
        val customerCard = view.findViewById<CardView>(R.id.customerButton)
        val butcherCard  = view.findViewById<CardView>(R.id.butcherButton)
        val tvLoginLink  = view.findViewById<TextView>(R.id.tvLoginLink)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        customerCard.setOnClickListener {
            findNavController().navigate(
                R.id.action_chooseRegister_to_customerRegister
            )
        }

        butcherCard.setOnClickListener {
            findNavController().navigate(
                R.id.action_chooseRegister_to_butcherRegister
            )
        }

        tvLoginLink.setOnClickListener {
            findNavController().navigate(
                R.id.action_chooseRegister_to_login
            )
        }
    }
}