package com.example.koshailagbe.model

import com.google.firebase.Timestamp

data class ChatRoom(
    var id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Timestamp = Timestamp.now(),
    val userNames: Map<String, String> = emptyMap(),
    val userPhotos: Map<String, String> = emptyMap(),
    val unreadCounts: Map<String, Int> = emptyMap()
)

data class ChatMessage(
    var id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "text" // "text" or "image"
)
