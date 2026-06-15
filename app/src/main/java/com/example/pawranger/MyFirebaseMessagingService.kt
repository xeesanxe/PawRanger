package com.example.pawranger

import android.annotation.SuppressLint
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

    // Fungsi ini otomatis berjalan saat ada pesan SOS masuk dari Firebase
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Ambil title dan body (Bisa dari notification atau data payload Firebase)
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "PANGGILAN DARURAT SOS! \uD83D\uDEA8"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Seseorang butuh bantuanmu segera!"

        // Ambil koordinat mentah dari data payload
        val latitude = remoteMessage.data["latitude"]
        val longitude = remoteMessage.data["longitude"]

        sendNotification(title, body, latitude, longitude)
    }

    private fun sendNotification(title: String, messageBody: String, latitude: String?, longitude: String?) {
        val channelId = "sos_channel_id" // Harus sama persis dengan yang dikirim dari Edge Function

        // Atur aksi: Buka Gmaps kalau ada koordinat, kalau kosong buka aplikasi PawRanger
        val intent = if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
            val gmapsUri = Uri.parse("https://maps.google.com/?q=$latitude,$longitude")
            Intent(Intent.ACTION_VIEW, gmapsUri).apply {
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

        // Paksa pakai suara ALARM bawaan HP yang paling keras
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Khusus Android 8.0 (Oreo) ke atas, wajib pakai Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sinyal Darurat SOS PawRanger",
                NotificationManager.IMPORTANCE_HIGH // Wajib HIGH biar langsung muncul pop-up
            ).apply {
                description = "Channel khusus untuk alarm darurat PawRanger"

                // Atribut ini yang bikin HP ngeluarin suara teriak kaya alarm
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

                setSound(alarmSoundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000) // Getaran SOS agresif
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Buat tampilan Pop-up Notifikasinya menggunakan icon PawRanger
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

        // Tembakkan notifikasi ke layar HP
        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }
}