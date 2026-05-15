package com.example.koshailagbe.utils

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.koshailagbe.R
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnackBar(
    message: String,
    isError: Boolean = false,
    duration: Int = Snackbar.LENGTH_LONG,
    actionText: String? = null,
    action: (() -> Unit)? = null
) {
    val view = view ?: return
    val snackbar = Snackbar.make(view, message, duration)
    
    // Customizing the Snackbar look
    val snackbarView = snackbar.view
    snackbarView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_snackbar)
    
    val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.setTextColor(Color.WHITE)
    textView.maxLines = 4 // Support longer error messages

    if (isError) {
        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
    } else {
        snackbar.setBackgroundTint(Color.parseColor("#2C3E50")) // Dark Blueish Grey
    }

    if (actionText != null && action != null) {
        snackbar.setAction(actionText) { action() }
        snackbar.setActionTextColor(Color.YELLOW)
    }

    snackbar.show()
}
