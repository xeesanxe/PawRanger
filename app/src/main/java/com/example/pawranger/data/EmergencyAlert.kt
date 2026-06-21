package com.example.pawranger.data

data class EmergencyAlert(
    val id: String? = null,
    val userId: String? = null,
    val senderName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val message: String? = null,
    val status: String? = null,
    val targetContacts: List<String>? = null,
    val createdAt: String? = null,
)