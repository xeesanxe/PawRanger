package com.example.pawranger.data

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// Data class kecil untuk menangkap fcm_token dari Supabase
@Serializable
data class TokenResponse(val fcm_token: String? = null)

class SOSRepository {
    private val client = SupabaseConfig.client

    // Mengirim sinyal SOS ke tabel emergency_alerts
    suspend fun sendSOS(alert: EmergencyAlert) = withContext(Dispatchers.IO) {
        client.postgrest.from("emergency_alerts").insert(alert)
    }

    // Mengambil daftar kontak darurat milik user
    suspend fun getEmergencyContacts(myPhone: String): List<Contact> = withContext(Dispatchers.IO) {
        client.postgrest.from("contacts")
            .select {
                filter {
                    eq("owner_phone", myPhone) // Sesuai kolom di tabel contacts Anda
                }
            }
            .decodeList<Contact>()
    }

    // --- TAMBAHAN TAHAP 1: MENGAMBIL FCM TOKEN TARGET ---
    suspend fun getFcmTokenByPhone(phoneNumber: String): String? = withContext(Dispatchers.IO) {
        try {
            // Query ke tabel profiles untuk mencari fcm_token berdasarkan nomor telepon target
            val result = client.postgrest.from("profiles")
                .select {
                    filter {
                        eq("no_telp", phoneNumber) // Pastikan nama kolom "no_telp" sesuai dengan yang ada di Supabase
                    }
                }
                .decodeSingleOrNull<TokenResponse>()

            result?.fcm_token
        } catch (e: Exception) {
            Log.e("SOS_REPO", "Gagal ambil token target: ${e.message}")
            null
        }
    }
    // --- AKHIR TAMBAHAN TAHAP 1 ---

    // Mendengarkan sinyal SOS baru secara Realtime
    fun listenForAlerts(myPhone: String): Flow<EmergencyAlert> {
        val channel = client.realtime.channel("sos_alerts")

        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(
            schema = "public"
        ) {
            table = "emergency_alerts"
        }

        return flow.map { insertAction ->
            insertAction.decodeRecord<EmergencyAlert>()
        }.filter { alert ->
            alert.receiver_phone == myPhone
        }
    }

    suspend fun connect() {
        client.realtime.connect()
    }
}