package com.example.pawranger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    @SerialName("name") // Sesuai kolom di Supabase Anda
    val name: String,
    
    @SerialName("phoneNumber") // Sesuai kolom di Supabase Anda
    val phoneNumber: String,
    
    @SerialName("owner_phone") // Kolom baru yang Anda tambahkan
    val ownerPhone: String = ""
)
