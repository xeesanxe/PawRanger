package com.example.pawranger.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PawRangerSession", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val USER_NAME = "userName" // This will store Username
        private const val FULL_NAME = "fullName" // This will store Full Name
        private const val USER_EMAIL = "userEmail"
        private const val USER_PHONE = "userPhone"
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

    fun saveFullName(name: String) {
        prefs.edit().putString(FULL_NAME, name).apply()
    }

    fun getFullName(): String? {
        return prefs.getString(FULL_NAME, "John Doe")
    }

    fun saveEmail(email: String) {
        prefs.edit().putString(USER_EMAIL, email).apply()
    }

    fun getEmail(): String? {
        return prefs.getString(USER_EMAIL, "aliya.nur@gmail.com")
    }

    fun savePhone(phone: String) {
        prefs.edit().putString(USER_PHONE, phone).apply()
    }

    fun getPhone(): String? {
        return prefs.getString(USER_PHONE, "0812-3456-7890")
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}