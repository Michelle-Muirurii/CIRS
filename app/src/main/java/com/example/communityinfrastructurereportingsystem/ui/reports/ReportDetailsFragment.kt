package com.example.communityinfrastructurereportingsystem.ui.reports


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.communityinfrastructurereportingsystem.data.local.SessionManager
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.ReportStatus
import com.example.communityinfrastructurereportingsystem.databinding.FragmentReportDetailsBinding
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel
import java.io.File
import android.widget.ArrayAdapter
import com.example.communityinfrastructurereportingsystem.data.model.UserRole


class ReportDetailsFragment : Fragment() {

    private var _binding: FragmentReportDetailsBinding? = null
    private val binding get() = _binding!!

    // SAFE ARGS: the reportId that arrived with the navigation
    private val args: ReportDetailsFragmentArgs by navArgs()

    private val reportViewModel: ReportViewModel by activityViewModels()

    // The report currently on screen (used by the buttons)
    private var currentReport: Report? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportViewModel.clearMessage()   // forget any old message
        val session = SessionManager(requireContext())

        // STAFF: the status dropdown and its button
        binding.statusSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, ReportStatus.all
        )
        binding.updateStatusButton.setOnClickListener {
            val report = currentReport ?: return@setOnClickListener
            // The ViewModel checks the status is valid and saves it
            val error = reportViewModel.updateStatus(
                report, binding.statusSpinner.selectedItem.toString()
            )
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        // Load the report from Room by its id. Because it is LiveData,
        // the screen refreshes by itself after an edit or a status change.
        reportViewModel.reportById(args.reportId).observe(viewLifecycleOwner) { report ->
            // null = the report was deleted: nothing to show
            if (report == null) return@observe
            currentReport = report
            showReport(report, session)
        }

        binding.editButton.setOnClickListener {
            currentReport?.let {
                findNavController().navigate(
                    ReportDetailsFragmentDirections
                        .actionReportDetailsFragmentToEditReportFragment(it.id)
                )
            }
        }

        binding.deleteButton.setOnClickListener {
            // Always ask before deleting
            AlertDialog.Builder(requireContext())
                .setTitle("Delete report?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    currentReport?.let { report ->
                        reportViewModel.deleteReport(report)
                        report.photoPath?.let { File(it).delete() }   // remove the photo file too
                        findNavController().navigateUp()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Implicit Intent (Lab 5): ask any maps app to show the location
        binding.mapButton.setOnClickListener {
            val report = currentReport ?: return@setOnClickListener
            val lat = report.latitude ?: return@setOnClickListener
            val lng = report.longitude ?: return@setOnClickListener
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(report.title)})")
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No maps app found", Toast.LENGTH_SHORT).show()
            }
        }

        reportViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                reportViewModel.clearMessage()
            }
        }
    }

    private fun showReport(report: Report, session: SessionManager) {
        binding.titleTextView.text = report.title
        binding.statusTextView.text = report.status
        binding.statusTextView.setTextColor(statusColor(report.status))
        binding.categoryTextView.text = "Type: ${report.category}"
        binding.areaTextView.text = "Sub-county: ${report.area}"
        binding.descriptionTextView.text = report.description
        binding.reporterTextView.text = "Reported by ${report.reporterName}"
        binding.datesTextView.text =
            "Reported: ${formatDate(report.createdAt)}  |  Updated: ${formatDate(report.updatedAt)}"

        // Photo: show it only if the file exists
        val path = report.photoPath
        if (path != null && File(path).exists()) {
            binding.photoImageView.visibility = View.VISIBLE
            Glide.with(this).load(File(path)).centerCrop().into(binding.photoImageView)
        } else {
            binding.photoImageView.visibility = View.GONE
        }

        // Location: show the coordinates and the maps button only if we have them
        val lat = report.latitude
        val lng = report.longitude
        if (lat != null && lng != null) {
            binding.locationTextView.visibility = View.VISIBLE
            binding.locationTextView.text = String.format("Location: %.5f, %.5f", lat, lng)
            binding.mapButton.visibility = View.VISIBLE
        } else {
            binding.locationTextView.visibility = View.GONE
            binding.mapButton.visibility = View.GONE
        }

        // Only the OWNER can edit or delete, and only while the report is still Pending
        // (once staff start work, the report is locked)
        val isOwner = report.reporterId == session.userId
        val canChange = isOwner && report.status == ReportStatus.PENDING
        binding.editButton.visibility = if (canChange) View.VISIBLE else View.GONE
        binding.deleteButton.visibility = if (canChange) View.VISIBLE else View.GONE

        // STAFF: show the status controls and select the report's current status
        val isStaff = session.role == UserRole.STAFF
        binding.staffControlsLayout.visibility = if (isStaff) View.VISIBLE else View.GONE
        if (isStaff) {
            binding.statusSpinner.setSelection(
                ReportStatus.all.indexOf(report.status).coerceAtLeast(0)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}