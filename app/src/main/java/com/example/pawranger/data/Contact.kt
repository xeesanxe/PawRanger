package com.example.pawranger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: Int = 0,
    val name: String,
    @SerialName("phoneNumber")
    val phoneNumber: String,
    @SerialName("user_id")
    val userId: String? = null
)
