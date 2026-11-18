package com.example.communityinfrastructurereportingsystem.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.communityinfrastructurereportingsystem.data.local.SessionManager
import com.example.communityinfrastructurereportingsystem.data.model.ReportCategory
import com.example.communityinfrastructurereportingsystem.data.model.ReportStatus
import com.example.communityinfrastructurereportingsystem.databinding.FragmentStaffDashboardBinding
import com.example.communityinfrastructurereportingsystem.ui.reports.ReportAdapter
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel

class StaffDashboardFragment : Fragment() {

    private var _binding: FragmentStaffDashboardBinding? = null
    private val binding get() = _binding!!

    private val reportViewModel: ReportViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.greetingTextView.text = "Staff: ${SessionManager(requireContext()).userName}"

        // ── The list. Person 5's adapter is reused; a tap opens the details screen ──
        val adapter = ReportAdapter(emptyList()) { report ->
            findNavController().navigate(
                StaffDashboardFragmentDirections
                    .actionStaffDashboardFragmentToReportDetailsFragment(report.id)
            )
        }
        binding.staffRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.staffRecyclerView.adapter = adapter

        // ── Filter dropdowns. Position 0 means "All" ──
        binding.statusFilterSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("All statuses") + ReportStatus.all
        )
        binding.categoryFilterSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("All types") + ReportCategory.all
        )

        // Runs when the screen opens and every time a dropdown choice changes
        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.statusFilterSpinner.onItemSelectedListener = filterListener
        binding.categoryFilterSpinner.onItemSelectedListener = filterListener

        // ── The filtered list from Room (LiveData) ──
        reportViewModel.filteredReports.observe(viewLifecycleOwner) { reports ->
            binding.progressBar.visibility = View.GONE           // loading is over
            adapter.submitList(reports)
            binding.emptyTextView.visibility =                   // empty state
                if (reports.isEmpty()) View.VISIBLE else View.GONE
        }

        // ── Counts for every report ──
        reportViewModel.allReports.observe(viewLifecycleOwner) {
            binding.totalTextView.text = "Total\n${it.size}"
        }
        reportViewModel.pendingCount.observe(viewLifecycleOwner) {
            binding.pendingTextView.text = "Pending\n$it"
        }
        reportViewModel.inProgressCount.observe(viewLifecycleOwner) {
            binding.inProgressTextView.text = "In progress\n$it"
        }
        reportViewModel.resolvedCount.observe(viewLifecycleOwner) {
            binding.resolvedTextView.text = "Resolved\n$it"
        }
    }

    // null means "do not filter on this" (see ReportDao.filterReports)
    private fun applyFilter() {
        val status = if (binding.statusFilterSpinner.selectedItemPosition == 0) null
        else binding.statusFilterSpinner.selectedItem.toString()

        val category = if (binding.categoryFilterSpinner.selectedItemPosition == 0) null
        else binding.categoryFilterSpinner.selectedItem.toString()

        reportViewModel.setFilter(status, category)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}