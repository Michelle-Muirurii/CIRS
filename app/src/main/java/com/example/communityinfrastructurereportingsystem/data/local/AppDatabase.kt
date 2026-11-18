package com.example.communityinfrastructurereportingsystem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.User

@Database(entities = [Report::class, User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // One database object for the whole app (singleton)
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cirs_database"
                ).build().also { INSTANCE = it }
            }
    }
}