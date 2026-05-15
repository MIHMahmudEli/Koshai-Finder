package com.example.koshailagbe.model

import com.google.firebase.Timestamp

data class ChatMessage(
    var id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val bookingId: String = ""
)
