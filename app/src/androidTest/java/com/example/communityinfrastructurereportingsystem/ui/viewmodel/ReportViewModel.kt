package com.example.communityinfrastructurereportingsystem.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.communityinfrastructurereportingsystem.data.local.CirsDatabase
import com.example.communityinfrastructurereportingsystem.data.model.NairobiConfig
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.ReportCategory
import com.example.communityinfrastructurereportingsystem.data.model.ReportStatus
import com.example.communityinfrastructurereportingsystem.data.repository.ReportRepository
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    // Build the data layer once: database -> DAO -> repository
    private val repository = ReportRepository(CirsDatabase.getDatabase(application).reportDao())

    // ── 1. THE DATA ─────────────────────────────────────────
    // Before the first load this has no value yet: screens can show a ProgressBar
    // while allReports.value == null (the "loading" state)
    val allReports: LiveData<List<Report>> = repository.allReports

    // "Empty" state: true when there are no reports, so the screen shows a message
    val isEmpty: LiveData<Boolean> = allReports.map { it.isEmpty() }

    // ── 2. TEXT READY FOR THE SCREENS (map makes a new LiveData) ──────
    val reportCountText: LiveData<String> = allReports.map { "Reports\n${it.size}" }

    val pendingCount: LiveData<Int> = repository.countByStatus(ReportStatus.PENDING)
    val inProgressCount: LiveData<Int> = repository.countByStatus(ReportStatus.IN_PROGRESS)
    val resolvedCount: LiveData<Int> = repository.countByStatus(ReportStatus.RESOLVED)

    // ── 3. STAFF FILTER ─────────────────────────────────────
    // null means "do not filter on this"
    private val filter = MutableLiveData(Pair<String?, String?>(null, null))

    // switchMap: when the filter changes, ask Room for a new list
    val filteredReports: LiveData<List<Report>> = filter.switchMap { (status, category) ->
        repository.filterReports(status, category)
    }

    fun setFilter(status: String?, category: String?) {
        filter.value = Pair(status, category)
    }

    // ── 4. UI STATE: saving and feedback ────────────────────
    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving         // show a ProgressBar while true

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message           // "Report submitted", errors...

    fun clearMessage() { _message.value = null }

    // ── 5. READ: lists for particular screens ───────────────
    fun reportsForUser(userId: Int): LiveData<List<Report>> = repository.reportsByUser(userId)
    fun reportById(id: Int): LiveData<Report?> = repository.reportById(id)
    fun reportsInArea(area: String): LiveData<List<Report>> = repository.reportsByArea(area)

    // ── 6. CREATE ───────────────────────────────────────────
    // Returns an error message, or null if the input was valid
    fun addReport(
        title: String,
        category: String,
        description: String,
        area: String,
        latitude: Double?,
        longitude: Double?,
        photoPath: String?,
        reporterId: Int,
        reporterName: String
    ): String? {
        val error = validate(title, category, description, area, latitude, longitude)
        if (error != null) return error

        val report = Report(
            title = title.trim(),
            category = category,
            description = description.trim(),
            area = area,
            latitude = latitude,
            longitude = longitude,
            photoPath = photoPath,
            reporterId = reporterId,
            reporterName = reporterName
        )
        save("Report submitted") { repository.addReport(report) }
        return null
    }

    // ── 7. UPDATE ───────────────────────────────────────────
    // Edit a report's details (the resident's edit screen)
    fun updateReport(report: Report): String? {
        val error = validate(
            report.title, report.category, report.description,
            report.area, report.latitude, report.longitude
        )
        if (error != null) return error

        val updated = report.copy(updatedAt = System.currentTimeMillis())
        save("Report updated") { repository.updateReport(updated) }
        return null
    }

    // Change only the status (the staff screen)
    fun updateStatus(report: Report, newStatus: String): String? {
        if (newStatus !in ReportStatus.all) return "Choose a valid status"

        val updated = report.copy(status = newStatus, updatedAt = System.currentTimeMillis())
        save("Status changed to $newStatus") { repository.updateReport(updated) }
        return null
    }

    // ── 8. DELETE ───────────────────────────────────────────
    fun deleteReport(report: Report) {
        save("Report deleted") { repository.deleteReport(report) }
    }

    // ── Helpers ─────────────────────────────────────────────
    // Runs a database action in a coroutine and reports success or failure
    private fun save(successMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                action()
                _message.value = successMessage
            } catch (e: Exception) {
                _message.value = "Something went wrong. Please try again."
            }
            _isSaving.value = false
        }
    }

    // Business rules: the app is for Nairobi only
    private fun validate(
        title: String,
        category: String,
        description: String,
        area: String,
        latitude: Double?,
        longitude: Double?
    ): String? {
        if (title.isBlank() || description.isBlank()) return "Please fill in the title and description"
        if (title.trim().length < 5) return "Title must be at least 5 characters"
        if (description.trim().length < 10) return "Please describe the fault in more detail"
        if (category !in ReportCategory.all) return "Choose the type of fault"
        if (area !in NairobiConfig.subCounties) return "Choose a Nairobi sub-county"
        if ((latitude == null) != (longitude == null)) return "The location is incomplete"
        if (latitude != null && longitude != null &&
            !NairobiConfig.isInsideNairobi(latitude, longitude)
        ) {
            return "This app only accepts reports from inside Nairobi County"
        }
        return null
    }
}