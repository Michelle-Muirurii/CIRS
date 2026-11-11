package com.example.communityinfrastructurereportingsystem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.User

// Which tables the database holds, and its version number
@Database(entities = [Report::class, User::class], version = 1, exportSchema = false)
abstract class CirsDatabase : RoomDatabase() {

    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao

    companion object {
        // Only ONE database object for the whole app
        @Volatile
        private var INSTANCE: CirsDatabase? = null

        fun getDatabase(context: Context): CirsDatabase {
            return INSTANCE ?: synchronized(this) {
                val database = Room.databaseBuilder(
                    context.applicationContext, // the whole app, not one screen
                    CirsDatabase::class.java,
                    "cirs_database"             // the file name on the phone
                ).build()
                INSTANCE = database
                database
            }
        }
    }
}