package com.anubhav.chatapp

import com.anubhav.chatapp.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.anubhav.chatapp.activities.MainActivity
import com.google.firebase.database.annotations.NotNull
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseService: FirebaseMessagingService() {
        override fun onMessageReceived(@NotNull remoteMessage: RemoteMessage) {
            super.onMessageReceived(remoteMessage)
            val notification = remoteMessage.notification
            sendNotification(notification!!.title, notification.body)
        }

        private fun sendNotification(title: String?, messageBody: String?) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT
            )
            val channelId = "1"
            val defaultSoundUri: Uri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_send)
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }
    }
