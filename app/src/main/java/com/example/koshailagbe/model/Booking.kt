package com.example.koshailagbe.model

import com.google.firebase.Timestamp

data class Booking(
    var id: String = "",
    var userId: String = "",
    var koshaiId: String = "",
    var address: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var date: Timestamp = Timestamp.now(),
    var slot: String = "07:00-09:00",
    var animalTypes: Map<String, Int> = mapOf("cow" to 0, "goat" to 0, "sheep" to 0),
    var rateBreakdown: Map<String, Double> = mapOf("total" to 0.0, "surgeMultiplier" to 1.0),
    var depositPaid: Double = 0.0,
    var status: String = "pending", // pending, confirmed, en_route, arrived, completed, cancelled
    var isGroupBooking: Boolean = false,
    var userName: String = "",
    var koshaiName: String = "",
    var createdAt: Timestamp = Timestamp.now(),
    var isReviewed: Boolean = false
)
