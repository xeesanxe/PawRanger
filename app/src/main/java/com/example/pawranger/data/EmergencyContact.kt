package com.example.pawranger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmergencyContact(

    val id: Long? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("contact_name")
    val contactName: String,

    @SerialName("phone_number")
    val phoneNumber: String,

    val relationship: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)