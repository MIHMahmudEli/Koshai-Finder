package com.example.koshailagbe.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koshailagbe.R

class ButcherRegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_butcher_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack      = view.findViewById<TextView>(R.id.btnBack)
        val name         = view.findViewById<EditText>(R.id.butcherName)
        val email        = view.findViewById<EditText>(R.id.butcherEmail)
        val password     = view.findViewById<EditText>(R.id.butcherPassword)
        val experience   = view.findViewById<EditText>(R.id.butcherExperience)
        val location     = view.findViewById<EditText>(R.id.butcherLocation)
        val rateCow      = view.findViewById<EditText>(R.id.butcherRateCow)
        val rateGoat     = view.findViewById<EditText>(R.id.butcherRateGoat)
        val createButton = view.findViewById<Button>(R.id.butcherCreateButton)
        val tvLoginLink  = view.findViewById<TextView>(R.id.tvLoginLink)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        tvLoginLink.setOnClickListener {
            findNavController().navigate(
                R.id.action_butcherRegister_to_login
            )
        }

        createButton.setOnClickListener {
            val nameVal     = name.text.toString().trim()
            val emailVal    = email.text.toString().trim()
            val passwordVal = password.text.toString().trim()
            val expVal      = experience.text.toString().trim()
            val locationVal = location.text.toString().trim()
            val rateCowVal  = rateCow.text.toString().trim()
            val rateGoatVal = rateGoat.text.toString().trim()

            when {
                nameVal.isEmpty() ->
                    toast("Please enter your full name")
                emailVal.isEmpty() ->
                    toast("Please enter your email")
                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(emailVal).matches() ->
                    toast("Enter a valid email address")
                passwordVal.length < 6 ->
                    toast("Password must be at least 6 characters")
                expVal.isEmpty() ->
                    toast("Please enter years of experience")
                locationVal.isEmpty() ->
                    toast("Please enter your location")
                rateCowVal.isEmpty() ->
                    toast("Please enter your rate per cow")
                rateGoatVal.isEmpty() ->
                    toast("Please enter your rate per goat")
                else -> {
                    toast("Butcher Account Created! Pending verification.")
                    findNavController().navigate(
                        R.id.action_butcherRegister_to_login
                    )
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}