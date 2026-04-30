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

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        val forgotPassword = findViewById<TextView>(R.id.forgotPasswordText)
        val googleButton = findViewById<Button>(R.id.googleLoginButton)

        // Login Button
        loginButton.setOnClickListener {

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Login Successful",
                    Toast.LENGTH_SHORT
                ).show()

                // Dashboard Activity Open
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)

            }
        }

        // Register Button
        registerButton.setOnClickListener {
            startActivity(Intent(this, ChooseRegisterActivity::class.java))
        }

        // Forgot Password
        forgotPassword.setOnClickListener {

            Toast.makeText(
                this,
                "Password reset link sent",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Continue with Google
        googleButton.setOnClickListener {

            Toast.makeText(
                this,
                "Google Login Clicked",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}