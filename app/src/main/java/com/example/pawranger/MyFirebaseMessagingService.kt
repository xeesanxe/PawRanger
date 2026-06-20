package com.example.pawranger

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Ambil data payload dari Supabase Edge Function
        val title = remoteMessage.data["title"] ?: "🚨 PANGGILAN DARURAT SOS!"
        val body = remoteMessage.data["body"] ?: "Seseorang butuh bantuanmu segera!"
        val latitude = remoteMessage.data["latitude"]
        val longitude = remoteMessage.data["longitude"]

        sendNotification(title, body, latitude, longitude)
    }

    private fun sendNotification(title: String, messageBody: String, latitude: String?, longitude: String?) {
        val channelId = "sos_channel_id"

        // Format Link Google Maps universal, langsung nembak aplikasi Maps / Browser HP
        val intent = if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
            val gmapsUrl = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
            Intent(Intent.ACTION_VIEW, Uri.parse(gmapsUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            } ?: Intent()
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Ambil suara tipe ALARM sistem HP yang paling keras
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sinyal Darurat SOS PawRanger",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel khusus untuk alarm darurat PawRanger"

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

                setSound(alarmSoundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setAutoCancel(true)
            .setSound(alarmSoundUri)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        // 🔥 TRIK PAMUNGKAS: Paksa sistem Android buat ngulang suara alarm (Looping) tanpa putus
        val notification = notificationBuilder.build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        // Tembak ke layar
        notificationManager.notify(Random.nextInt(), notification)
    }
}