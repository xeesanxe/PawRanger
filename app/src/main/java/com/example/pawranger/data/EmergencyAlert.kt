package com.example.pawranger.data

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyAlert(
    val id: String? = null,
    val sender_phone: String,
    val receiver_phone: String,
    val latitude: Double,
    val longitude: Double,
    val status: String = "PENDING",
    val created_at: String? = null
)