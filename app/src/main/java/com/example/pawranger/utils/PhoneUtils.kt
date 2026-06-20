package com.example.pawranger.utils

object PhoneUtils {
    fun formatPhoneNumber(phone: String?): String {
        if (phone.isNullOrEmpty()) return ""
        // Hilangkan semua karakter kecuali angka
        val numOnly = phone.replace(Regex("[^0-9]"), "")
        
        return when {
            numOnly.startsWith("62") -> "0" + numOnly.substring(2)
            numOnly.startsWith("0") -> numOnly
            else -> numOnly
        }
    }
}
