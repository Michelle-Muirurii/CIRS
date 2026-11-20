package com.example.communityinfrastructurereportingsystem.ui.reports

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.communityinfrastructurereportingsystem.R
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.ReportStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Shared helpers (Person 6 uses them too)
fun statusColor(status: String): Int = when (status) {
    ReportStatus.PENDING -> Color.parseColor("#FF9A4D")      // orange
    ReportStatus.IN_PROGRESS -> Color.parseColor("#7DB3FF")  // blue
    ReportStatus.RESOLVED -> Color.parseColor("#5ED68F")     // green
    else -> Color.LTGRAY
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

// The Adapter tells the RecyclerView how to show each report (Lesson 10)
class ReportAdapter(
    private var reports: List<Report>,
    private val onItemClick: (Report) -> Unit    // what to do when a row is tapped
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    // The ViewHolder keeps the row's views, so we look them up only once per row
    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo = itemView.findViewById<ImageView>(R.id.ivPhoto)
        val title = itemView.findViewById<TextView>(R.id.tvTitle)
        val details = itemView.findViewById<TextView>(R.id.tvDetails)
        val status = itemView.findViewById<TextView>(R.id.tvStatus)
        val date = itemView.findViewById<TextView>(R.id.tvDate)
    }

    // Called only a few times: it creates a new row from item_report.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun getItemCount(): Int = reports.size

    // Called every time a row scrolls into view: fill the (recycled) row with this report
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]

        holder.title.text = report.title
        holder.details.text = "${report.category} - ${report.area}"
        holder.status.text = report.status
        holder.status.setTextColor(statusColor(report.status))
        holder.date.text = "By ${report.reporterName} - ${formatDate(report.createdAt)}"

        // Rows are RECYCLED, so every row must set OR clear its photo,
        // or a scrolled-away row would keep showing the old picture
        val path = report.photoPath
        if (path != null && File(path).exists()) {
            Glide.with(holder.itemView).load(File(path)).centerCrop().into(holder.photo)
        } else {
            Glide.with(holder.itemView).clear(holder.photo)
            holder.photo.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener { onItemClick(report) }
    }

    // Give the adapter a new list and redraw
    fun submitList(newList: List<Report>) {
        reports = newList
        notifyDataSetChanged()
    }
}