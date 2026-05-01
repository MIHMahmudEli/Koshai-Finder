package com.example.koshailagbe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailEditText    = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton      = findViewById<Button>(R.id.loginButton)
        val registerButton   = findViewById<Button>(R.id.registerButton)
        val forgotPassword   = findViewById<TextView>(R.id.forgotPasswordText)
        val googleButton     = findViewById<Button>(R.id.googleLoginButton)

        // ── Login ──────────────────────────────────────────────────────────────
        loginButton.setOnClickListener {
            val email    = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            when {
                email.isEmpty()    -> toast("Please enter your email")
                password.isEmpty() -> toast("Please enter your password")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    toast("Enter a valid email address")
                password.length < 6 ->
                    toast("Password must be at least 6 characters")
                else -> {
                    toast("Login Successful!")
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            }
        }

        // ── Register ───────────────────────────────────────────────────────────
        registerButton.setOnClickListener {
            startActivity(Intent(this, ChooseRegisterActivity::class.java))
        }

        // ── Forgot Password ────────────────────────────────────────────────────
        forgotPassword.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                toast("Enter your email first to reset password")
            } else {
                toast("Password reset link sent to $email")
            }
        }

        // ── Google Login ───────────────────────────────────────────────────────
        googleButton.setOnClickListener {
            toast("Google Login — connect Firebase Auth to enable")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}