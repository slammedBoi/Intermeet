package com.intermeet.android

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Log the new token (optional)
        Log.d(TAG, "Refreshed token: $token")

        // Save the token to Firebase database
        sendRegistrationToServer(token)
    }
    private fun sendRegistrationToServer(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val tokenRef = FirebaseDatabase.getInstance().getReference("users/$userId/fcmToken")
            tokenRef.setValue(token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM Token updated successfully for user $userId")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to update FCM Token for user $userId", it)
                }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Notification received - Title: ${remoteMessage.notification?.title}, Body: ${remoteMessage.notification?.body}")
        Log.d(TAG, "Data payload: ${remoteMessage.data}")

        val userId = remoteMessage.data["userId"]
        if (userId == null) {
            Log.d(TAG, "No userId found in notification data")
        } else {
            Log.d(TAG, "UserId received with notification: $userId")
        }

        remoteMessage.notification?.let {
            val channel = if (it.title?.contains("like", true) == true) "LikeChannel" else "ChatChannel"
            if (channel == "ChatChannel" && AppState.isChatActivityVisible && AppState.currentChatUserId == userId) {
                Log.d(TAG, "ChatActivity is active with the user. No notification needed.")
            } else {
                showNotification(it.title ?: "New Notification", it.body ?: "You have a new message", channel, userId)
            }
        }
    }


    private fun showNotification(title: String, message: String, channelId: String, userId: String?) {
        Log.d(TAG, "Preparing to show notification. Channel: $channelId, UserId: $userId")

        if (channelId == "ChatChannel" && (AppState.isChatFragmentActive || AppState.isChatActivityVisible)) {
            Log.d(TAG, "Chat UI is active. Not showing chat notification.")
            return
        }


        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = System.currentTimeMillis().toInt()


        //val intent = Intent(this, ChatActivity::class.java).apply {
        //    putExtra("userId", userId)  // Pass the userId to ChatActivity
        //    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //}
        val intent = if (channelId == "LikeChannel") {
            Intent(this, MainActivity::class.java).apply {
                putExtra("openFragment", "like")  // Direct MainActivity to open LikePageFragment
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {  // Default to ChatActivity for other cases (e.g., ChatChannel)
            Intent(this, ChatActivity::class.java).apply {
                putExtra("userId", userId)  // Pass the userId to ChatActivity
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.intermeet_png_72ppi_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationID, notification)
        Log.d(TAG, "Notification posted: ID=$notificationID")
    }





    private fun handleNow(data: Map<String, String>) {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "LikeChannel"  // Ensure you have a notification channel.

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.intermeet_png_72ppi_icon)  // Ensure you have this drawable resource.
            .setContentTitle(title)
            .setContentText(body)
            .build()

        notificationManager.notify(0, notification)
    }
}
