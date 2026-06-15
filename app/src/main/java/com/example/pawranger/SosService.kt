package com.example.pawranger

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pawranger.data.EmergencyAlert
import com.example.pawranger.data.SOSRepository
import com.example.pawranger.utils.SessionManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*

class SosService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var sosJob: Job? = null
    private val sosRepository = SOSRepository()
    private lateinit var sessionManager: SessionManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "START") {
            startForegroundService()
            startLooping()
        } else if (action == "STOP") {
            stopLooping()
            stopSelf()
        }
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val channelId = "sos_service_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mode Darurat PawRanger",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("PawRanger SOS AKTIF")
            .setContentText("Aplikasi sedang mengirimkan lokasi Anda berkala...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1001, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1001, notification)
        }
    }

    private fun startLooping() {
        sosJob?.cancel()
        sosJob = serviceScope.launch {
            while (isActive) {
                fetchAndSendLocation()
                delay(120_000) // ubah 10_000 kalau mau testing
            }
        }
    }

    private fun stopLooping() {
        sosJob?.cancel()
    }

    @SuppressLint("MissingPermission")
    private fun fetchAndSendLocation() {
        // Ambil nomor pengirim dari sesi
        val rawPhone = sessionManager.getUserPhone() ?: ""

        // BERSILAT LIDAH 1: Bersihkan nomor pengirim pakai fungsi formatter biar pasti 08
        val myPhone = formatPhoneNumber(rawPhone.replace(Regex("[^0-9+]"), ""))

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                serviceScope.launch(Dispatchers.IO) {
                    try {
                        val contacts = sosRepository.getEmergencyContacts(myPhone)
                        contacts.forEach { contact ->

                            // BERSILAT LIDAH 2: Bersihkan nomor penerima sebelum masuk payload
                            val cleanReceiverPhone = formatPhoneNumber(contact.phoneNumber)

                            val alert = EmergencyAlert(
                                sender_phone = myPhone,
                                receiver_phone = cleanReceiverPhone, // Menggunakan nomor yang sudah bersih
                                latitude = location.latitude,
                                longitude = location.longitude
                            )

                            sosRepository.sendSOS(alert)
                        }
                        Log.d("SOS_SYSTEM", "Lokasi berhasil dikirim via Service!")
                    } catch (e: Exception) {
                        Log.e("SOS_SYSTEM", "Error di Service: ${e.message}")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

// Fungsi penyaring ditaruh di luar class agar bisa diakses mudah
fun formatPhoneNumber(phone: String?): String {
    if (phone.isNullOrEmpty()) return ""
    return when {
        phone.startsWith("+62") -> "0" + phone.substring(3)
        phone.startsWith("62") -> "0" + phone.substring(2)
        else -> phone
    }
}