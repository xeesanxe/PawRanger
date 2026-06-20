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

// Data class untuk menangkap fcm_token dari Supabase
@Serializable
data class TokenResponse(val fcm_token: String? = null)

// Data class untuk menangkap nama pengguna saat login
@Serializable
data class ProfileResponse(val nama: String? = null)

// Data class untuk proses insert/upsert profil baru
@Serializable
data class ProfileInsert(val nama: String, val no_telp: String, val fcm_token: String)

// Data class untuk memperbarui token Firebase saat login ulang
@Serializable
data class TokenUpdate(val fcm_token: String)

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
                    eq("owner_phone", myPhone) // Sesuai kolom di tabel contacts kamu
                }
            }
            .decodeList<Contact>()
    }

    // MENGAMBIL FCM TOKEN TARGET
    suspend fun getFcmTokenByPhone(phoneNumber: String): String? = withContext(Dispatchers.IO) {
        try {
            // Query ke tabel profiles untuk mencari fcm_token berdasarkan nomor telepon target
            val result = client.postgrest.from("profiles")
                .select {
                    filter {
                        eq("no_telp", phoneNumber) // Pastikan nama kolom "no_telp" sesuai
                    }
                }
                .decodeSingleOrNull<TokenResponse>()

            result?.fcm_token
        } catch (e: Exception) {
            Log.e("SOS_REPO", "Gagal ambil token target: ${e.message}")
            null
        }
    }

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

    // FUNGSI BARU: Menyimpan Profil dan Token ke Supabase saat Register
    suspend fun saveUserProfile(nama: String, noTelp: String, fcmToken: String) = withContext(Dispatchers.IO) {
        try {
            val profileData = ProfileInsert(nama, noTelp, fcmToken)
            // Menggunakan upsert agar memperbarui data jika nomor sudah pernah terdaftar
            client.postgrest.from("profiles").upsert(profileData)
        } catch (e: Exception) {
            Log.e("SOS_REPO", "Gagal simpan profil: ${e.message}")
            throw e
        }
    }

    // FUNGSI BARU: Memverifikasi nomor saat Login dan menyinkronkan Token terbaru
    suspend fun loginUserAndSyncToken(noTelp: String, newToken: String): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Cek apakah nomor tersebut ada di tabel profiles
            val result = client.postgrest.from("profiles")
                .select {
                    filter {
                        eq("no_telp", noTelp)
                    }
                }
                .decodeSingleOrNull<ProfileResponse>()

            if (result != null) {
                // 2. Jika ada, update fcm_token nya ke token HP yang sekarang
                val updateData = TokenUpdate(newToken)
                client.postgrest.from("profiles").update(updateData) {
                    filter {
                        eq("no_telp", noTelp)
                    }
                }
                return@withContext result.nama ?: "Pengguna PawRanger"
            }

            // Jika null berarti nomor belum terdaftar
            return@withContext null

        } catch (e: Exception) {
            Log.e("SOS_REPO", "Gagal verifikasi login: ${e.message}")
            return@withContext null
        }
    }

    suspend fun connect() {
        client.realtime.connect()
    }
}