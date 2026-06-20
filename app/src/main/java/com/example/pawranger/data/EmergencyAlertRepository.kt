package com.example.pawranger.data

import android.content.Context
import com.example.pawranger.utils.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class EmergencyAlertRepository(context: Context) { // Tambah context biar bisa buka SessionManager

    private val db = FirebaseFirestore.getInstance()
    private val sessionManager = SessionManager(context)

    // Kirim SOS — simpan ke Firestore
    suspend fun sendAlert(latitude: Double, longitude: Double): Boolean {
        // Tarik KTP Nomor HP lu, BUKAN dari FirebaseAuth
        val rawPhone = sessionManager.getUserPhone() ?: return false
        val userId = rawPhone.replace(Regex("[^0-9+]"), "")

        if (userId.isEmpty()) return false

        val alert = EmergencyAlert(
            userId = userId,
            latitude = latitude,
            longitude = longitude,
            message = "SOS! Butuh bantuan segera!",
            status = "ACTIVE",
            createdAt = System.currentTimeMillis().toString()
        )

        return try {
            db.collection("emergency_alerts")
                .add(alert)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Real-time listener — pantau alert aktif milik user ini
    fun listenToMyAlerts(
        onUpdate: (List<EmergencyAlert>) -> Unit
    ): ListenerRegistration {
        val rawPhone = sessionManager.getUserPhone() ?: return db.collection("emergency_alerts").addSnapshotListener { _, _ -> }
        val userId = rawPhone.replace(Regex("[^0-9+]"), "")

        return db.collection("emergency_alerts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                // Pakai toObject otomatis biar nggak usah mapping manual yang bikin error
                val alerts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(EmergencyAlert::class.java)
                }
                onUpdate(alerts)
            }
    }
}