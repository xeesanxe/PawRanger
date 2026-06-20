package com.example.pawranger.data

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SOSRepository {
    private val db = FirebaseFirestore.getInstance()

    // Mengirim sinyal SOS ke koleksi emergency_alerts di Firebase
    suspend fun sendSOS(alert: EmergencyAlert) = withContext(Dispatchers.IO) {
        try {
            db.collection("emergency_alerts").add(alert).await()
        } catch (e: Exception) {
            Log.e("SOS_REPO", "Gagal kirim SOS: ${e.message}")
        }
    }

    // Mengambil daftar kontak darurat milik user
    suspend fun getEmergencyContacts(myPhone: String): List<Contact> = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("contacts")
                .whereEqualTo("owner_phone", myPhone)
                .get()
                .await()
            snapshot.toObjects(Contact::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // MENGAMBIL FCM TOKEN TARGET
    suspend fun getFcmTokenByPhone(phoneNumber: String): String? = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("profiles")
                .whereEqualTo("no_telp", phoneNumber)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                snapshot.documents[0].getString("fcm_token")
            } else null
        } catch (e: Exception) {
            Log.e("SOS_REPO", "Gagal ambil token target: ${e.message}")
            null
        }
    }

    // Mendengarkan sinyal SOS baru secara Realtime pakai Firestore
    fun listenForAlerts(myPhone: String): Flow<EmergencyAlert> = callbackFlow {
        val listener = db.collection("emergency_alerts")
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val alert = change.document.toObject(EmergencyAlert::class.java)
                        // Cek apakah pesan daruratnya ditujukan ke nomor HP user ini
                        if (alert.message?.contains(myPhone) == true) {
                            trySend(alert).isSuccess
                        }
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    // FUNGSI BARU: Menyimpan Profil dan Token ke Firestore saat Register
    suspend fun saveUserProfile(nama: String, noTelp: String, fcmToken: String) = withContext(Dispatchers.IO) {
        try {
            val profileData = hashMapOf(
                "nama" to nama,
                "no_telp" to noTelp,
                "fcm_token" to fcmToken
            )
            // Jadikan nomor telepon sebagai Document ID biar gampang di-update (upsert)
            db.collection("profiles").document(noTelp).set(profileData).await()
        } catch (e: Exception) {
            Log.e("SOS_REPO", "Gagal simpan profil: ${e.message}")
            throw e
        }
    }

    // FUNGSI BARU: Memverifikasi nomor saat Login dan menyinkronkan Token terbaru
    suspend fun loginUserAndSyncToken(noTelp: String, newToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val docRef = db.collection("profiles").document(noTelp)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                // Update token
                docRef.update("fcm_token", newToken).await()
                return@withContext snapshot.getString("nama") ?: "Pengguna PawRanger"
            }
            return@withContext null
        } catch (e: Exception) {
            Log.e("SOS_REPO", "Gagal verifikasi login: ${e.message}")
            return@withContext null
        }
    }

    suspend fun connect() {
        // Firestore udah otomatis konek, jadi function ini dikosongin aja biar kodingan di MainActivity nggak error
    }
}