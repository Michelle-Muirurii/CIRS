package com.example.communityinfrastructurereportingsystem.data.local

import android.content.Context
import com.example.communityinfrastructurereportingsystem.data.model.User
import com.example.communityinfrastructurereportingsystem.data.model.UserRole

// Remembers who is logged in, even after the app is closed (SharedPreferences = a tiny key-value file)
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("cirs_session", Context.MODE_PRIVATE)

    // Called once, right after a successful login
    fun saveUser(user: User) {
        prefs.edit()
            .putInt("user_id", user.id)
            .putString("user_name", user.fullName)
            .putString("user_email", user.email)
            .putString("user_role", user.role)
            .apply()
    }

    val userId: Int get() = prefs.getInt("user_id", -1)
    val userName: String get() = prefs.getString("user_name", "") ?: ""
    val userEmail: String get() = prefs.getString("user_email", "") ?: ""
    val role: String get() = prefs.getString("user_role", UserRole.RESIDENT) ?: UserRole.RESIDENT

    fun isLoggedIn(): Boolean = userId != -1

    // Called on logout
    fun clear() {
        prefs.edit().clear().apply()
    }
}