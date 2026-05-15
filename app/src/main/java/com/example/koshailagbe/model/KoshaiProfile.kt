package com.example.koshailagbe.model

import com.google.firebase.Timestamp

data class KoshaiProfile(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var photoUrl: String = "",
    var district: String = "",
    var upazila: String = "",
    var status: String = "offline", // online, busy, offline
    var isVerified: Boolean = false,
    var isEidMode: Boolean = false,
    var rating: Double = 0.0,
    var totalRatings: Long = 0,
    var totalJobs: Long = 0,
    var earnings: Double = 0.0,  // Double to match Firestore FieldValue.increment()
    var ratePerCow: Double = 0.0,
    var ratePerGoat: Double = 0.0,
    var ratePerSheep: Double = 0.0
)
