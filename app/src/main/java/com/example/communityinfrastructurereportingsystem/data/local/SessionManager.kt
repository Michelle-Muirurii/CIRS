package com.example.communityinfrastructurereportingsystem.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cirs_session", Context.MODE_PRIVATE)

    var userId: Int
        get() = prefs.getInt("user_id", -1)
        set(value) = prefs.edit().putInt("user_id", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "Resident") ?: "Resident"
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userEmail: String
        get() = prefs.getString("user_email", "") ?: ""
        set(value) = prefs.edit().putString("user_email", value).apply()

    var role: String
        get() = prefs.getString("user_role", "RESIDENT") ?: "RESIDENT"
        set(value) = prefs.edit().putString("user_role", value).apply()

    fun saveSession(id: Int, name: String, email: String, userRole: String) {
        prefs.edit()
            .putInt("user_id", id)
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_role", userRole)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = userId != -1
}
