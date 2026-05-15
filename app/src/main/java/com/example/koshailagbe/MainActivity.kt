package com.example.koshailagbe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.koshailagbe.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure status bar and navigation bar are visible
        WindowCompat.setDecorFitsSystemWindows(window, true)
        
        setContentView(R.layout.activity_main)

        askNotificationPermission()
        updateFcmToken()
        setupAnnouncementListener()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener
            
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).update("fcmToken", token)
            db.collection("koshais").document(uid).update("fcmToken", token)
        }
    }

    private var bookingListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var chatListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var announcementListener: com.google.firebase.firestore.ListenerRegistration? = null

    private fun setupAnnouncementListener() {
        val db = FirebaseFirestore.getInstance()
        
        // Listen for new announcements added in the last 10 seconds to avoid spamming old ones on start
        val startTime = com.google.firebase.Timestamp(java.util.Date(System.currentTimeMillis() - 10000))
        
        announcementListener = db.collection("announcements")
            .whereGreaterThan("timestamp", startTime)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                val role = com.example.koshailagbe.utils.SharedPrefsHelper.getUserRole(this)
                
                for (change in snapshot.documentChanges) {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val announcement = change.document.toObject(com.example.koshailagbe.model.Announcement::class.java)
                        
                        // Check if announcement targets this user
                        if (announcement.target == "all" || 
                            (announcement.target == "users" && role == com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_USER) ||
                            (announcement.target == "koshais" && role == com.example.koshailagbe.utils.SharedPrefsHelper.ROLE_KOSHAI)) {
                            
                            showLocalNotification(announcement.title, announcement.message)
                        }
                    }
                }
            }
    }

    private fun showLocalNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "system_announcements"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Announcements", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun listenForBookingUpdates() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        bookingListener = db.collection("bookings")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                snapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                        val booking = dc.document.toObject(com.example.koshailagbe.model.Booking::class.java)
                        com.example.koshailagbe.utils.NotificationHelper.showNotification(
                            this,
                            "Booking Update",
                            "Your booking status has been updated to ${booking.status}"
                        )
                    }
                }
            }
    }

    private fun listenForNewMessages() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        chatListener = db.collection("chatRooms")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                snapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                        val room = dc.document.toObject(com.example.koshailagbe.model.ChatRoom::class.java)
                        val lastMessage = room.lastMessage
                        if (lastMessage.isNotEmpty()) {
                            com.example.koshailagbe.utils.NotificationHelper.showNotification(
                                this,
                                "New Message",
                                lastMessage
                            )
                        }
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
        listenForBookingUpdates()
        listenForNewMessages()
    }

    override fun onStop() {
        super.onStop()
        bookingListener?.remove()
        chatListener?.remove()
        announcementListener?.remove()
    }
}