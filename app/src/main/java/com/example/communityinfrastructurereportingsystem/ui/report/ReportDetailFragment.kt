package com.example.communityinfrastructurereportingsystem.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.communityinfrastructurereportingsystem.R
import com.example.communityinfrastructurereportingsystem.data.model.ReportStatus
import com.example.communityinfrastructurereportingsystem.databinding.FragmentReportDetailBinding
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!

    private val reportViewModel: ReportViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reportId = arguments?.getInt("reportId") ?: -1
        if (reportId == -1) return

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        reportViewModel.reportById(reportId).observe(viewLifecycleOwner) { report ->
            if (report != null) {
                binding.tvTitle.text = report.title
                binding.tvCategory.text = report.category
                binding.tvArea.text = "Sub-County: ${report.area}"
                binding.tvReporter.text = "Reported by: ${report.reporterName}"
                binding.tvStatus.text = report.status
                binding.tvDate.text = "Submitted: ${dateFormat.format(Date(report.createdAt))}"
                binding.tvDescription.text = report.description

                if (report.latitude != null && report.longitude != null) {
                    binding.tvCoordinates.text = "Location: ${report.latitude}, ${report.longitude} (Nairobi County)"
                } else {
                    binding.tvCoordinates.text = "Location: Sub-County area (${report.area})"
                }

                val bgDrawable = when (report.status) {
                    ReportStatus.IN_PROGRESS -> R.drawable.bg_status_in_progress
                    ReportStatus.RESOLVED -> R.drawable.bg_status_resolved
                    else -> R.drawable.bg_status_pending
                }
                binding.tvStatus.setBackgroundResource(bgDrawable)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
