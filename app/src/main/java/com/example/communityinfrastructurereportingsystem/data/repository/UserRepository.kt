package com.example.communityinfrastructurereportingsystem.data.repository

import com.example.communityinfrastructurereportingsystem.data.local.UserDao
import com.example.communityinfrastructurereportingsystem.data.model.User
import com.example.communityinfrastructurereportingsystem.data.model.UserRole
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {

    suspend fun register(fullName: String, email: String, password: String): Long {
        val user = User(fullName = fullName, email = email, passwordHash = hash(password))
        return userDao.insertUser(user)
    }

    suspend fun login(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email) ?: return null
        return if (user.passwordHash == hash(password)) user else null
    }

    suspend fun getUser(id: Int): User? = userDao.getUserById(id)

    suspend fun ensureStaffAccount() {
        userDao.insertUser(
            User(
                fullName = "County Staff",
                email = "staff@nairobi.go.ke",
                passwordHash = hash("Staff@123"),
                role = UserRole.STAFF
            )
        )
    }

    private fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
