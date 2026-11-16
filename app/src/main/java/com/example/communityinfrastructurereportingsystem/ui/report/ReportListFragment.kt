package com.example.communityinfrastructurereportingsystem.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.communityinfrastructurereportingsystem.R
import com.example.communityinfrastructurereportingsystem.databinding.FragmentReportListBinding
import com.example.communityinfrastructurereportingsystem.ui.adapter.ReportAdapter
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.AuthViewModel
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel

class ReportListFragment : Fragment() {

    private var _binding: FragmentReportListBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val reportViewModel: ReportViewModel by activityViewModels()

    private lateinit var adapter: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = authViewModel.currentUser.value
        if (currentUser == null) {
            findNavController().navigate(R.id.action_reportListFragment_to_loginFragment)
            return
        }

        binding.tvWelcome.text = "Welcome, ${currentUser.fullName}"

        adapter = ReportAdapter { report ->
            val bundle = Bundle().apply {
                putInt("reportId", report.id)
            }
            findNavController().navigate(R.id.action_reportListFragment_to_reportDetailFragment, bundle)
        }
        binding.rvReports.adapter = adapter

        reportViewModel.reportsForUser(currentUser.id).observe(viewLifecycleOwner) { reports ->
            binding.progressBar.visibility = View.GONE
            if (reports.isNullOrEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvReports.visibility = View.GONE
                binding.tvReportCount.text = "Reports submitted: 0"
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvReports.visibility = View.VISIBLE
                binding.tvReportCount.text = "Reports submitted: ${reports.size}"
                adapter.submitList(reports)
            }
        }

        binding.fabAddReport.setOnClickListener {
            findNavController().navigate(R.id.action_reportListFragment_to_createReportFragment)
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_reportListFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
