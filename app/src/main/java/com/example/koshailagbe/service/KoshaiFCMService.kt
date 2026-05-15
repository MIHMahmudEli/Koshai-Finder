package com.example.koshailagbe.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.koshailagbe.MainActivity
import com.example.koshailagbe.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class KoshaiFCMService : FirebaseMessagingService() {

    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateTokenInFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle data-only messages for stat updates (sent from server/Cloud Function)
        val data = remoteMessage.data
        if (data["type"] == "booking_completed") {
            handleBookingCompleted(
                koshaiId = data["koshaiId"] ?: return,
                bookingId = data["bookingId"] ?: return,
                earnings = data["earnings"]?.toDoubleOrNull() ?: 0.0
            )
        }

        // Show notification
        remoteMessage.notification?.let {
            showNotification(it.title ?: "Koshai Finder", it.body ?: "")
        } ?: run {
            val title = data["title"] ?: "New Update"
            val body = data["body"] ?: "Check your app for details"
            if (title.isNotEmpty()) showNotification(title, body)
        }
    }

    /**
     * Atomically updates the Koshai's totalJobs and earnings when a booking is completed.
     * This is triggered from the FCM data payload so it works even when the app is in
     * the background or killed.
     *
     * NOTE: If you don't have a backend sending FCM messages, the Fragments
     * (BookingManagerFragment & BookingDetailFragment) already handle this via WriteBatch.
     * This service acts as a safety net.
     */
    private fun handleBookingCompleted(koshaiId: String, bookingId: String, earnings: Double) {
        Log.d("KoshaiFCMService", "Updating stats for koshai=$koshaiId earnings=$earnings")

        // Use a transaction to avoid double-counting if both the Fragment and FCM fire
        val koshaiRef = db.collection("koshais").document(koshaiId)
        val bookingRef = db.collection("bookings").document(bookingId)

        db.runTransaction { transaction ->
            val bookingSnap = transaction.get(bookingRef)
            val alreadyCounted = bookingSnap.getBoolean("statsCounted") ?: false

            if (!alreadyCounted) {
                // Increment stats atomically
                transaction.update(koshaiRef, "totalJobs", FieldValue.increment(1))
                transaction.update(koshaiRef, "earnings", FieldValue.increment(earnings.toLong()))
                // Mark booking so we never count it twice
                transaction.update(bookingRef, "statsCounted", true)
            }
        }.addOnSuccessListener {
            Log.d("KoshaiFCMService", "Stats updated successfully")
        }.addOnFailureListener { e ->
            Log.e("KoshaiFCMService", "Failed to update stats: ${e.message}")
        }
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "koshai_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Koshai Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun updateTokenInFirestore(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).update("fcmToken", token)
        db.collection("koshais").document(uid).update("fcmToken", token)
    }
}
