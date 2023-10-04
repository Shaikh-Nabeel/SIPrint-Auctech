package com.auctech.siprint.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.auctech.siprint.Constants
import com.auctech.siprint.PreferenceManager
import com.auctech.siprint.R
import com.auctech.siprint.home.activity.MainActivity
import com.auctech.siprint.initials.response.ResponseSignup
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title
            val body = remoteMessage.notification?.body

            // Create and show a notification
            showNotification(this, title, body)
        }

        // Handle the data payload if there is one
        if (remoteMessage.data.isNotEmpty()) {
            // Handle the data payload here
        }
    }

    override fun onNewToken(token: String) {
        try {
            val apiService: ApiClient = RetrofitClient.instance.create(ApiClient::class.java)
            val body = JsonObject()
            body.addProperty("user_id", PreferenceManager.getStringValue(Constants.USER_ID))
            body.addProperty("fcmToken", token)
            apiService.updateFcmToken(body)
                .enqueue(object : Callback<ResponseSignup> {
                    override fun onResponse(
                        call: Call<ResponseSignup>,
                        response: Response<ResponseSignup>
                    ) {
//                        Toast.makeText(
//                            this@MainActivity,
//                            response.body().toString(),
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }

                    override fun onFailure(call: Call<ResponseSignup>, t: Throwable) {
                        t.printStackTrace()
                    }

                })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(context: Context, title: String?, body: String?) {
        val channelId = "siprint.general.notification"

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("from", "notification")
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.app_logo) // Set your notification icon here
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(Color.BLUE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Create a notification channel (required for Android 8.0 and higher)
        createNotificationChannel(context, channelId)

        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "siprint notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val notificationId = 837 // Unique ID for the notification
    }
}
