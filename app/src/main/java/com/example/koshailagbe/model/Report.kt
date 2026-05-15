package com.example.koshailagbe.model

import com.google.firebase.Timestamp

data class Report(
    var id: String = "",
    val bookingId: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reportedEntityId: String = "", // Koshai or User ID
    val reportedEntityName: String = "",
    val reason: String = "",
    val details: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    var status: String = "pending", // pending, resolved
    var resolutionNote: String = ""
)
