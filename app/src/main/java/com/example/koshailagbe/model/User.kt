package com.example.koshailagbe.model

import com.google.firebase.Timestamp

data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var photoUrl: String = "",
    var district: String = "",
    var upazila: String = "",
    var referralCode: String = "",
    var referredBy: String = "",
    var credits: Int = 0,
    var createdAt: Timestamp? = null,
    @get:com.google.firebase.firestore.PropertyName("isBanned")
    @set:com.google.firebase.firestore.PropertyName("isBanned")
    var isBanned: Boolean = false,
    var bannedReason: String = ""
)
