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

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText    = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val loginButton      = view.findViewById<Button>(R.id.loginButton)
        val registerButton   = view.findViewById<Button>(R.id.registerButton)
        val forgotPassword   = view.findViewById<TextView>(R.id.forgotPasswordText)

        loginButton.setOnClickListener {
            val email    = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            when {
                email.isEmpty()    -> toast("Please enter your email")
                password.isEmpty() -> toast("Please enter your password")
                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches() ->
                    toast("Enter a valid email address")
                password.length < 6 ->
                    toast("Password must be at least 6 characters")
                else -> {
                    toast("Login Successful!")
                    findNavController()
                        .navigate(R.id.action_login_to_dashboard)
                }
            }
        }

        registerButton.setOnClickListener {
            findNavController()
                .navigate(R.id.action_login_to_chooseRegister)
        }

        forgotPassword.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                toast("Enter your email first")
            } else {
                toast("Reset link sent to $email")
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}