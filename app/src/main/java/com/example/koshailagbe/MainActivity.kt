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
                email.isEmpty()    -> toast(getString(R.string.msg_please_enter_email))
                password.isEmpty() -> toast(getString(R.string.msg_please_enter_password))
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    toast(getString(R.string.msg_valid_email))
                password.length < 6 ->
                    toast(getString(R.string.msg_password_length))
                else -> {
                    toast(getString(R.string.msg_login_success))
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
                toast(getString(R.string.msg_reset_email_first))
            } else {
                toast(getString(R.string.msg_reset_sent, email))
            }
        }

        // ── Google Login ───────────────────────────────────────────────────────
        googleButton.setOnClickListener {
            toast(getString(R.string.msg_google_login_firebase))
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}