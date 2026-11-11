package com.example.communityinfrastructurereportingsystem.data.repository

import com.example.communityinfrastructurereportingsystem.data.local.UserDao
import com.example.communityinfrastructurereportingsystem.data.model.User
import com.example.communityinfrastructurereportingsystem.data.model.UserRole
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {

    // Returns the new user's id, or -1 if that email is already registered
    suspend fun register(fullName: String, email: String, password: String): Long {
        val user = User(fullName = fullName, email = email, passwordHash = hash(password))
        return userDao.insertUser(user)
    }

    // Returns the user if email and password match, otherwise null
    suspend fun login(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email) ?: return null
        return if (user.passwordHash == hash(password)) user else null
    }

    suspend fun getUser(id: Int): User? = userDao.getUserById(id)

    // Creates the county staff account once. If it exists already, IGNORE does nothing.
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

    // Turns a password into a fixed-length code so the real password is never stored.
    // (Fine for this course project; real apps also add a random "salt".)
    private fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}