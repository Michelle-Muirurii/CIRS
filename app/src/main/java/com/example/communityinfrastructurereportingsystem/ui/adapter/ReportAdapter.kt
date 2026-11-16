package com.example.communityinfrastructurereportingsystem.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.communityinfrastructurereportingsystem.R
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.ReportStatus
import com.example.communityinfrastructurereportingsystem.databinding.ItemReportBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAdapter(
    private val onItemClick: (Report) -> Unit
) : ListAdapter<Report, ReportAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReportViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReportViewHolder(
        private val binding: ItemReportBinding,
        private val onItemClick: (Report) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(report: Report) {
            binding.tvTitle.text = report.title
            binding.tvCategory.text = report.category
            binding.tvArea.text = "Area: ${report.area}"
            binding.tvStatus.text = report.status
            binding.tvDate.text = "Submitted: ${dateFormat.format(Date(report.createdAt))}"

            val bgDrawable = when (report.status) {
                ReportStatus.IN_PROGRESS -> R.drawable.bg_status_in_progress
                ReportStatus.RESOLVED -> R.drawable.bg_status_resolved
                else -> R.drawable.bg_status_pending
            }
            binding.tvStatus.setBackgroundResource(bgDrawable)

            binding.root.setOnClickListener { onItemClick(report) }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean =
            oldItem == newItem
    }
}
