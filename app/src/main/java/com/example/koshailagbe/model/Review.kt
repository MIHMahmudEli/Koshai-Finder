package com.example.koshailagbe.model

import com.google.firebase.Timestamp

data class Review(
    var id: String = "",
    val userId: String = "",
    val userName: String = "User",
    val rating: Float = 5f,
    val comment: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    @get:com.google.firebase.firestore.PropertyName("isHidden")
    @set:com.google.firebase.firestore.PropertyName("isHidden")
    var isHidden: Boolean = false,
    var koshaiId: String = ""
)
