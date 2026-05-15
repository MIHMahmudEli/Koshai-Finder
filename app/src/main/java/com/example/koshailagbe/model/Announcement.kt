package com.example.koshailagbe.model

import com.google.firebase.Timestamp

data class Announcement(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    val target: String = "all", // all, users, koshais
    val timestamp: Timestamp = Timestamp.now(),
    val author: String = "Admin"
)
