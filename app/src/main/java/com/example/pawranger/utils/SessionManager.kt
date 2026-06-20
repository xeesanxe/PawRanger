package com.example.pawranger.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PawRangerSession", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val USER_NAME = "userName"
        private const val USER_EMAIL = "userEmail"
        private const val USER_PHONE = "userPhone"
        private const val PROFILE_IMAGE = "profileImage"
        private const val IS_DARK_MODE = "isDarkMode"
        private const val EMERGENCY_MESSAGE = "emergencyMessage"
        private const val IS_SOS_ACTIVE = "isSosActive"
    }

    fun setSosActive(isActive: Boolean) {
        prefs.edit().putBoolean(IS_SOS_ACTIVE, isActive).apply()
    }

    fun isSosActive(): Boolean {
        return prefs.getBoolean(IS_SOS_ACTIVE, false)
    }

    fun setDarkMode(isDarkMode: Boolean) {
        prefs.edit().putBoolean(IS_DARK_MODE, isDarkMode).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(IS_DARK_MODE, false)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun saveUserName(name: String) {
        prefs.edit().putString(USER_NAME, name).apply()
    }

    fun getUserName(): String? {
        return prefs.getString(USER_NAME, "Aliya")
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(USER_EMAIL, "aliya.nur@gmail.com")
    }
    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }
    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun saveUserPhone(phone: String) {
        prefs.edit().putString(USER_PHONE, phone).apply()
    }

    fun getUserPhone(): String? {
        return prefs.getString(USER_PHONE, "+62 812-3456-7890")
    }

    fun saveProfileImage(uri: String) {
        prefs.edit().putString(PROFILE_IMAGE, uri).apply()
    }

    fun getProfileImage(): String? {
        return prefs.getString(PROFILE_IMAGE, null)
    }

    fun saveEmergencyMessage(message: String) {
        prefs.edit().putString(EMERGENCY_MESSAGE, message).apply()
    }

    fun getEmergencyMessage(): String {
        return prefs.getString(EMERGENCY_MESSAGE, "Halo, saya butuh bantuan segera. Lokasi saya dikirimkan otomatis melalui aplikasi ini.") 
            ?: "Halo, saya butuh bantuan segera. Lokasi saya dikirimkan otomatis melalui aplikasi ini."
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
