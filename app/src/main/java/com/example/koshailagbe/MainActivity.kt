package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Login Fields
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        // Buttons
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val googleLoginButton = findViewById<Button>(R.id.googleLoginButton)

        // Forgot Password
        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordText)

        // Login Button
        loginButton.setOnClickListener {

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Login Successful",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Register Button
        registerButton.setOnClickListener {

            startActivity(
                Intent(this, ChooseRegisterActivity::class.java)
            )

        }

        // Forgot Password
        forgotPasswordText.setOnClickListener {

            Toast.makeText(
                this,
                "Password reset link sent to your email",
                Toast.LENGTH_SHORT
            ).show()

        }

        // Google Login
        googleLoginButton.setOnClickListener {

            Toast.makeText(
                this,
                "Google Login Coming Soon",
                Toast.LENGTH_SHORT
            ).show()

        }
    }
}