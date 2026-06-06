package com.example.pawranger.data

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

object MessageRepository {

    private val client get() = SupabaseConfig.client

    // Ambil semua pesan antara 2 user
    suspend fun getMessages(currentUserId: String, otherUserId: String): List<Chat> {
        return try {
            client!!.postgrest.from("messages")
                .select(Columns.ALL) {
                    filter {
                        or {
                            and {
                                eq("sender_id", currentUserId)
                                eq("receiver_id", otherUserId)
                            }
                            and {
                                eq("sender_id", otherUserId)
                                eq("receiver_id", currentUserId)
                            }
                        }
                    }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Chat>()
        } catch (e: Exception) {
            Log.e("MessageRepository", "Gagal ambil pesan: ${e.message}")
            emptyList()
        }
    }

    // Kirim pesan baru
    suspend fun sendMessage(senderId: String, receiverId: String, content: String): Boolean {
        return try {
            val pesan = mapOf(
                "sender_id" to senderId,
                "receiver_id" to receiverId,
                "content" to content
            )
            client!!.postgrest.from("messages").insert(pesan)
            true
        } catch (e: Exception) {
            Log.e("MessageRepository", "Gagal kirim pesan: ${e.message}")
            false
        }
    }

    // Ambil semua percakapan user (untuk list chat)
    suspend fun getConversations(currentUserId: String): List<Chat> {
        return try {
            client!!.postgrest.from("messages")
                .select(Columns.ALL) {
                    filter {
                        or {
                            eq("sender_id", currentUserId)
                            eq("receiver_id", currentUserId)
                        }
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Chat>()
        } catch (e: Exception) {
            Log.e("MessageRepository", "Gagal ambil conversation: ${e.message}")
            emptyList()
        }
    }
}