package com.example.pawranger

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pawranger.utils.SessionManager
import com.example.pawranger.utils.PhoneUtils
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SosService : Service() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val NOTIFICATION_ID = 99
    private val CHANNEL_ID = "SOS_CHANNEL"

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val myPhone = PhoneUtils.formatPhoneNumber(sessionManager.getUserPhone())

        if (action == "START") {
            startForeground(NOTIFICATION_ID, createNotification("PawRanger SOS AKTIF"))
            triggerSos(myPhone)
        } else if (action == "STOP") {
            resolveSos(myPhone)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_STICKY
    }

    private fun triggerSos(myPhone: String) {
        serviceScope.launch {
            val contactList = mutableListOf<String>()
            try {
                val snapshot = db.collection("contacts")
                    .whereEqualTo("userId", myPhone)
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    val p = doc.getString("phoneNumber")
                    if (p != null) contactList.add(PhoneUtils.formatPhoneNumber(p))
                }
            } catch (e: Exception) {
                Log.e("SosService", "Gagal ambil kontak: ${e.message}")
            }

            while (isActive) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            updateFirestoreAlert(myPhone, it, "ACTIVE", contactList)
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("SosService", "Permission error: ${e.message}")
                }
                delay(120000)
            }
        }
    }

    private fun resolveSos(myPhone: String) {
        serviceScope.launch {
            try {
                db.collection("emergency_alerts").document(myPhone)
                    .update("status", "RESOLVED", "createdAt", System.currentTimeMillis().toString())
            } catch (e: Exception) {
                Log.e("SosService", "Error resolving: ${e.message}")
            }
            serviceJob.cancel()
        }
    }

    private fun updateFirestoreAlert(phone: String, loc: Location, status: String, contacts: List<String>) {
        val alertData = hashMapOf(
            "userId" to phone,
            "senderName" to (sessionManager.getUserName() ?: "Ranger"),
            "latitude" to loc.latitude,
            "longitude" to loc.longitude,
            "status" to status,
            "createdAt" to System.currentTimeMillis().toString(),
            "targetContacts" to contacts
        )

        db.collection("emergency_alerts").document(phone)
            .set(alertData)
            .addOnFailureListener { Log.e("Firebase", "Gagal update lokasi") }
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sinyal Bahaya Terdeteksi!")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_location_pin)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "SOS Service", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}