package com.example.app.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.gdl.R
import com.app.gdl.data.model.User
import com.app.gdl.presentation.ui.activity.MainActivity
import com.app.gdl.utils.SharedPref
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var pref: SharedPref
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received: ${remoteMessage.data}")
        pref= SharedPref(this)
        val data = remoteMessage.data

        remoteMessage.notification?.let {
            //for now hide
           // showNotification("Verification Successful",  "Your address has been verified. You can now place an order with us.")
            Log.d("FCM remoteMessage", "Message received: ${it.body}")
            //here save the data f user verified
           // Response : {"customer_id": 23, "customer_name": "Dipti Pandilwar", "email_id": "1234567890", "phone": "1234567890", "fcm_token": "ergivhtgR7OvRGevC_37g7:APA91bFsnILzU_e9r4cfvV4HHReu24bypudwv4Vd-7CNySaLdkfP9jGi9P0_0zWttuv04x_5L3tPKX6H9v6j_NFKHg-HglEyL-IPBPAl1lKicHq68tpjOE8", "address": [{"name": "Home", "text": "Sion, Mumbai, Maharashtra", "map_location": [19.0427021, 72.8736898], "default": 1, "verified": 1, "warehouse": "MALINDI", "price_class": "MLD CBD"}]}
            val customer = Gson().fromJson( "${it.body}", User::class.java)
            pref.saveCustomerToPrefs(this, customer) // save
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "default_channel"
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "FCM Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        manager.notify(0, notificationBuilder.build())
    }
}
