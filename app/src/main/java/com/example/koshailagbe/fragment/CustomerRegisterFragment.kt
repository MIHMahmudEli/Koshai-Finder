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

class CustomerRegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_customer_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack      = view.findViewById<TextView>(R.id.btnBack)
        val name         = view.findViewById<EditText>(R.id.customerName)
        val phone        = view.findViewById<EditText>(R.id.customerPhone)
        val email        = view.findViewById<EditText>(R.id.customerEmail)
        val password     = view.findViewById<EditText>(R.id.customerPassword)
        val createButton = view.findViewById<Button>(R.id.customerCreateButton)
        val tvLoginLink  = view.findViewById<TextView>(R.id.tvLoginLink)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        tvLoginLink.setOnClickListener {
            findNavController().navigate(
                R.id.action_customerRegister_to_login
            )
        }

        createButton.setOnClickListener {
            val nameVal     = name.text.toString().trim()
            val phoneVal    = phone.text.toString().trim()
            val emailVal    = email.text.toString().trim()
            val passwordVal = password.text.toString().trim()

            when {
                nameVal.isEmpty() ->
                    toast("Please enter your full name")
                phoneVal.isEmpty() || phoneVal.length < 11 ->
                    toast("Enter a valid 11-digit phone number")
                emailVal.isEmpty() ->
                    toast("Please enter your email")
                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(emailVal).matches() ->
                    toast("Enter a valid email address")
                passwordVal.length < 6 ->
                    toast("Password must be at least 6 characters")
                else -> {
                    toast("Account Created! Welcome 🎉")
                    findNavController().navigate(
                        R.id.action_customerRegister_to_dashboard
                    )
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}