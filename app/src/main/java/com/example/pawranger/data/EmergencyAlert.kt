package com.example.pawranger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmergencyAlert(

    val id: Long? = null,

    @SerialName("user_id")
    val userId: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    val status: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)