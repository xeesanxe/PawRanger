package com.example.pawranger.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class EmergencyAlertRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Kirim SOS — simpan ke Firestore
    suspend fun sendAlert(latitude: Double, longitude: Double): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        val data = hashMapOf(
            "userId" to userId,
            "latitude" to latitude,
            "longitude" to longitude,
            "message" to "SOS! Butuh bantuan segera!",
            "status" to "active",
            "createdAt" to System.currentTimeMillis().toString()
        )

        return try {
            db.collection("emergency_alerts")
                .add(data)
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
        val userId = auth.currentUser?.uid ?: return db.collection("emergency_alerts")
            .addSnapshotListener { _, _ -> }

        return db.collection("emergency_alerts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val alerts = snapshot.documents.map { doc ->
                    EmergencyAlert(
                        userId = doc.getString("userId"),
                        latitude = doc.getDouble("latitude"),
                        longitude = doc.getDouble("longitude"),
                        message = doc.getString("message"),
                        status = doc.getString("status"),
                        createdAt = doc.getString("createdAt")
                    )
                }
                onUpdate(alerts)
            }
    }
}