package com.example.pawranger.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PawRangerSession", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val USER_NAME = "userName"
        private const val USER_EMAIL = "userEmail"
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

    fun saveEmail(email: String) {
        prefs.edit().putString(USER_EMAIL, email).apply()
    }

    fun getEmail(): String? {
        return prefs.getString(USER_EMAIL, "aliya.nur@gmail.com")
    }
    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }
    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
