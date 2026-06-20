package com.example.pawranger.data

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyAlert(
    val id: Long? = null,
    val userId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val message: String? = null,
    val status: String? = null,
    val createdAt: String? = null
)