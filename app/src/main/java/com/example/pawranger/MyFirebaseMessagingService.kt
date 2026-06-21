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
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Ambil data payload dari Edge Function / Backend
        val title = remoteMessage.data["title"] ?: "🚨 PANGGILAN DARURAT SOS!"
        val body = remoteMessage.data["body"] ?: "Seseorang butuh bantuanmu segera!"
        val latitude = remoteMessage.data["latitude"]
        val longitude = remoteMessage.data["longitude"]

        // Ambil info pengirim biar bisa dikirim ke MainActivity
        val senderPhone = remoteMessage.data["senderPhone"] ?: "Tidak diketahui"
        val senderName = remoteMessage.data["senderName"] ?: "Seseorang"

        sendNotification(title, body, latitude, longitude, senderPhone, senderName)
    }

    private fun sendNotification(title: String, messageBody: String, latitude: String?, longitude: String?, senderPhone: String, senderName: String) {
        val channelId = "sos_channel_id"

        // 🔥 KUNCI FULL-SCREEN: Arahin langsung ke MainActivity bawa data darurat
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("isEmergency", true)
            putExtra("senderPhone", senderPhone)
            putExtra("senderName", senderName)
            putExtra("lat", latitude)
            putExtra("lng", longitude)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

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
                setBypassDnd(true) // 🔥 TEMBUS MODE JANGAN GANGGU (DND)!

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
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Kategori wajib buat nembus layar kunci
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // 🔥 SAKLAR WAKE UP SCREEN!

        val notification = notificationBuilder.build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT // Looping alarm tanpa ampun

        notificationManager.notify(Random.nextInt(), notification)
    }
}