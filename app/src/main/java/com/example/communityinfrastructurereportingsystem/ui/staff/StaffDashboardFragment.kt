package com.example.communityinfrastructurereportingsystem.ui.staff

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.communityinfrastructurereportingsystem.R
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.ReportCategory
import com.example.communityinfrastructurereportingsystem.data.model.ReportStatus
import com.example.communityinfrastructurereportingsystem.databinding.FragmentStaffDashboardBinding
import com.example.communityinfrastructurereportingsystem.ui.adapter.ReportAdapter
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.AuthViewModel
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel

class StaffDashboardFragment : Fragment() {

    private var _binding: FragmentStaffDashboardBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val reportViewModel: ReportViewModel by activityViewModels()

    private lateinit var adapter: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReportAdapter { report ->
            showStatusUpdateDialog(report)
        }
        binding.rvStaffReports.adapter = adapter

        reportViewModel.pendingCount.observe(viewLifecycleOwner) { count ->
            binding.tvPendingCount.text = (count ?: 0).toString()
        }
        reportViewModel.inProgressCount.observe(viewLifecycleOwner) { count ->
            binding.tvInProgressCount.text = (count ?: 0).toString()
        }
        reportViewModel.resolvedCount.observe(viewLifecycleOwner) { count ->
            binding.tvResolvedCount.text = (count ?: 0).toString()
        }

        setupFilters()

        reportViewModel.filteredReports.observe(viewLifecycleOwner) { reports ->
            if (reports.isNullOrEmpty()) {
                binding.tvStaffEmpty.visibility = View.VISIBLE
                binding.rvStaffReports.visibility = View.GONE
            } else {
                binding.tvStaffEmpty.visibility = View.GONE
                binding.rvStaffReports.visibility = View.VISIBLE
                adapter.submitList(reports)
            }
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_staffDashboardFragment_to_loginFragment)
        }

        reportViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                reportViewModel.clearMessage()
            }
        }
    }

    private fun setupFilters() {
        val statusList = listOf("All Statuses") + ReportStatus.all
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spFilterStatus.adapter = statusAdapter

        val categoryList = listOf("All Categories") + ReportCategory.all
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spFilterCategory.adapter = categoryAdapter

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = binding.spFilterStatus.selectedItem?.toString()
                val selectedCategory = binding.spFilterCategory.selectedItem?.toString()

                val filterStatus = if (selectedStatus == "All Statuses") null else selectedStatus
                val filterCategory = if (selectedCategory == "All Categories") null else selectedCategory

                reportViewModel.setFilter(filterStatus, filterCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spFilterStatus.onItemSelectedListener = listener
        binding.spFilterCategory.onItemSelectedListener = listener
    }

    private fun showStatusUpdateDialog(report: Report) {
        val options = arrayOf("View Details", "Mark Pending", "Mark In Progress", "Mark Resolved")
        AlertDialog.Builder(requireContext())
            .setTitle("Manage Report #${report.id}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val bundle = Bundle().apply { putInt("reportId", report.id) }
                        findNavController().navigate(R.id.action_staffDashboardFragment_to_reportDetailFragment, bundle)
                    }
                    1 -> reportViewModel.updateStatus(report, ReportStatus.PENDING)
                    2 -> reportViewModel.updateStatus(report, ReportStatus.IN_PROGRESS)
                    3 -> reportViewModel.updateStatus(report, ReportStatus.RESOLVED)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
