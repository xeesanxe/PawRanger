package com.example.pawranger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val name: String,
    @SerialName("phoneNumber") // Disamakan dengan nama kolom di Screenshot Supabase Anda
    val phoneNumber: String
)
