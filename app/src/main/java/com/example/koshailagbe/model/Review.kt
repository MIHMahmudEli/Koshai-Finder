package com.example.koshailagbe.model

import com.google.firebase.Timestamp

data class Review(
    val userId: String = "",
    val userName: String = "User",
    val rating: Float = 5f,
    val comment: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
