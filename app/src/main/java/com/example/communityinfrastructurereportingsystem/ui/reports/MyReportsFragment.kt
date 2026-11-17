package com.example.communityinfrastructurereportingsystem.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.communityinfrastructurereportingsystem.data.local.SessionManager
import com.example.communityinfrastructurereportingsystem.databinding.FragmentMyReportsBinding
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel

class MyReportsFragment : Fragment() {
    private var _binding: FragmentMyReportsBinding? = null
    private val binding get() = _binding!!
    private val reportViewModel: ReportViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = SessionManager(requireContext())

        // The Adapter starts with an empty list; the data arrives from Room below.
        // The lambda runs when a row is tapped: open that report's details (Safe Args)
        val adapter = ReportAdapter(emptyList()) { report ->
            findNavController().navigate(
                MyReportsFragmentDirections.actionMyReportsFragmentToReportDetailsFragment(report.id)
            )
        }

        // LayoutManager: arrange the rows in a vertical list
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reportsRecyclerView.adapter = adapter

        // Room's LiveData: runs once when the data first loads, and again on every change
        reportViewModel.reportsForUser(session.userId).observe(viewLifecycleOwner) { reports ->
            binding.progressBar.visibility = View.GONE                 // loading is over
            adapter.submitList(reports)
            binding.emptyTextView.visibility =                          // empty state
                if (reports.isEmpty()) View.VISIBLE else View.GONE
        }

        // "Report deleted", "Report updated"... show once, then clear
        reportViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                reportViewModel.clearMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
