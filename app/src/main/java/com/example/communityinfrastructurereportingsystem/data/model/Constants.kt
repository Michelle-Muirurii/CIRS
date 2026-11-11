package com.example.communityinfrastructurereportingsystem.data.model

object ReportStatus {
    const val PENDING = "Pending"
    const val IN_PROGRESS = "In Progress"
    const val RESOLVED = "Resolved"
    val all = listOf(PENDING, IN_PROGRESS, RESOLVED)
}

object ReportCategory {
    const val POTHOLE = "Pothole"
    const val STREETLIGHT = "Broken Streetlight"
    const val PAVEMENT = "Damaged Pavement"
    const val DRAINAGE = "Blocked Drainage"
    val all = listOf(POTHOLE, STREETLIGHT, PAVEMENT, DRAINAGE)
}

object UserRole {
    const val RESIDENT = "RESIDENT"
    const val STAFF = "STAFF"
}