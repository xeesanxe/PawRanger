package com.example.pawranger

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Fungsi ini otomatis berjalan saat ada pesan SOS masuk dari Firebase
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Ambil data pesan teks dan koordinat yang dikirim
        val title = remoteMessage.data["title"] ?: "PANGGILAN DARURAT SOS!"
        val body = remoteMessage.data["body"] ?: "Seseorang membutuhkan bantuan Anda."
        val latitude = remoteMessage.data["latitude"]
        val longitude = remoteMessage.data["longitude"]

        // Racik link Google Maps jika koordinatnya dikirimkan
        val gmapsUrl = if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
            "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
        } else { null }

        sendNotification(title, body, gmapsUrl)
    }

    private fun sendNotification(title: String, messageBody: String, gmapsUrl: String?) {
        val channelId = "sos_notification_channel"

        // Atur aksi: jika notifikasi diklik, langsung buka Link Gmaps / browser bawaan HP
        val intent = if (!gmapsUrl.isNullOrEmpty()) {
            Intent(Intent.ACTION_VIEW, Uri.parse(gmapsUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            // Jika link kosong, buka aplikasi PawRanger seperti biasa
            packageManager.getLaunchIntentForPackage(packageName)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Atur suara alarm default HP
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Buat tampilan Pop-up Notifikasinya (PERBAIKAN IKON DI SINI)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Biar langsung muncul di atas layar
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Khusus Android 8.0 (Oreo) ke atas, wajib pakai Notification Channel (PERBAIKAN TITIK DI SINI)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sinyal Darurat SOS PawRanger",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel khusus untuk mengirimkan notifikasi bahaya berulang."
                setSound(defaultSoundUri, null)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Jalankan notifikasi ke layar HP
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}