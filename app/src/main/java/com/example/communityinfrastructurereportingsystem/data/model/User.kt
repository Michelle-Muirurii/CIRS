package com.example.communityinfrastructurereportingsystem.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// The unique index on email stops two accounts using the same email
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,            // never store a plain password
    val role: String = UserRole.RESIDENT
)