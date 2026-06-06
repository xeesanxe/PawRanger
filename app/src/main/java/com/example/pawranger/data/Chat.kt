package com.example.pawranger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: Long = 0,
    @SerialName("sender_id")
    val senderId: String = "",
    @SerialName("receiver_id")
    val receiverId: String = "",
    val content: String = "",
    @SerialName("vn_url")
    val vnUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",

    // Untuk tampilan di list (tidak dari DB, diisi manual)
    val senderName: String = "",
    val lastMessage: String = ""
)