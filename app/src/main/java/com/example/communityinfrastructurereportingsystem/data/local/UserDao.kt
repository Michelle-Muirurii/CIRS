package com.example.communityinfrastructurereportingsystem.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.communityinfrastructurereportingsystem.data.model.User

@Dao
interface UserDao {

    // IGNORE: if the email already exists, nothing is saved and -1 is returned.
    // The ViewModel uses this to show "Email already registered".
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User): Long

    // Used at login: find the user with this email (null if none)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?
}