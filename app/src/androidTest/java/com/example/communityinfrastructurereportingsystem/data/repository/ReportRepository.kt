package com.example.communityinfrastructurereportingsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.communityinfrastructurereportingsystem.data.local.ReportDao
import com.example.communityinfrastructurereportingsystem.data.model.Report

// The repository is the single door into the report data.
// The ViewModel talks to it and never touches the DAO directly.
class ReportRepository(private val reportDao: ReportDao) {

    // LiveData from Room: updates by itself whenever the table changes
    val allReports: LiveData<List<Report>> = reportDao.getAllReports()

    fun reportsByUser(userId: Int): LiveData<List<Report>> = reportDao.getReportsByUser(userId)

    fun reportById(id: Int): LiveData<Report?> = reportDao.getReportById(id)

    fun reportsByArea(area: String): LiveData<List<Report>> = reportDao.getReportsByArea(area)

    fun filterReports(status: String?, category: String?): LiveData<List<Report>> =
        reportDao.filterReports(status, category)

    fun countByStatus(status: String): LiveData<Int> = reportDao.countByStatus(status)

    // suspend: must be called from a coroutine (the ViewModel does this)
    suspend fun addReport(report: Report): Long = reportDao.insertReport(report)

    suspend fun updateReport(report: Report) = reportDao.updateReport(report)

    suspend fun deleteReport(report: Report) = reportDao.deleteReport(report)
}