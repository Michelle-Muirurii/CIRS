package com.example.communityinfrastructurereportingsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// @Entity: every Report is stored as one ROW in the "reports" table
@Entity(tableName = "reports")
data class Report(
    // Room fills in the id (1, 2, 3...). "= 0" means "not saved yet"
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val description: String,
    val area: String,                    // Nairobi sub-county
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoPath: String? = null,       // path of the photo file, not the photo itself
    val status: String = ReportStatus.PENDING,
    val reporterId: Int,
    val reporterName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable   // lets Safe Args pass a Report between screens, as Tenant does in the labs