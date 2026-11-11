package com.example.communityinfrastructurereportingsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.communityinfrastructurereportingsystem.data.model.Report

// @Dao: the things we can do with the "reports" table. Room writes the code inside
@Dao
interface ReportDao {

    // suspend: can pause while the database works, without freezing the screen
    @Insert
    suspend fun insertReport(report: Report): Long

    @Update
    suspend fun updateReport(report: Report)

    @Delete
    suspend fun deleteReport(report: Report)

    // LiveData: Room updates it automatically whenever the table changes
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): LiveData<List<Report>>

    @Query("SELECT * FROM reports WHERE reporterId = :userId ORDER BY createdAt DESC")
    fun getReportsByUser(userId: Int): LiveData<List<Report>>

    @Query("SELECT * FROM reports WHERE id = :id")
    fun getReportById(id: Int): LiveData<Report?>

    // "Surrounding issues": other reports in the same sub-county
    @Query("SELECT * FROM reports WHERE area = :area ORDER BY createdAt DESC")
    fun getReportsByArea(area: String): LiveData<List<Report>>

    // Staff filter: pass null for a filter you do not want to apply
    @Query("SELECT * FROM reports WHERE (:status IS NULL OR status = :status) AND (:category IS NULL OR category = :category) ORDER BY createdAt DESC")
    fun filterReports(status: String?, category: String?): LiveData<List<Report>>

    // For the staff dashboard counts
    @Query("SELECT COUNT(*) FROM reports WHERE status = :status")
    fun countByStatus(status: String): LiveData<Int>
}