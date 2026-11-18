package com.example.communityinfrastructurereportingsystem.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.communityinfrastructurereportingsystem.R
import com.example.communityinfrastructurereportingsystem.data.local.SessionManager
import com.example.communityinfrastructurereportingsystem.data.model.NairobiConfig
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.ReportStatus
import com.example.communityinfrastructurereportingsystem.databinding.FragmentDashboardBinding
import com.example.communityinfrastructurereportingsystem.ui.reports.ReportAdapter
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.WeatherViewModel
import com.example.communityinfrastructurereportingsystem.worker.ReportScheduler


class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val reportViewModel: ReportViewModel by activityViewModels()


    private val weatherViewModel: WeatherViewModel by activityViewModels()

    // Asks the user to allow notifications (Android 13+)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // The list of the sub-county currently chosen in the spinner
    private var areaReports: LiveData<List<Report>>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = SessionManager(requireContext())
        binding.greetingTextView.text = "Hello, ${session.userName}"

        // PERSON 6: observe the weather here and show it in binding.weatherTextView

        // ── WEATHER (API): rain / flood warning for Nairobi ─────
        binding.weatherTextView.visibility = View.VISIBLE
        weatherViewModel.weatherText.observe(viewLifecycleOwner) { text ->
            binding.weatherTextView.text = text
        }
        binding.weatherTextView.setOnClickListener { weatherViewModel.loadWeather() }  // tap to retry
        if (savedInstanceState == null) {
            weatherViewModel.loadWeather()
        }

        // ── BACKGROUND WORK: notifications about status changes ──
        if (savedInstanceState == null) {
            askNotificationPermission()
            ReportScheduler.start(requireContext())
        }

        // -- My report counts (worked out from my own list) -----
        reportViewModel.reportsForUser(session.userId).observe(viewLifecycleOwner) { reports ->
            binding.totalTextView.text = "Total\n${reports.size}"
            binding.pendingTextView.text =
                "Pending\n${reports.count { it.status == ReportStatus.PENDING }}"
            binding.inProgressTextView.text =
                "In progress\n${reports.count { it.status == ReportStatus.IN_PROGRESS }}"
            binding.resolvedTextView.text =
                "Resolved\n${reports.count { it.status == ReportStatus.RESOLVED }}"
        }

        // -- Buttons (Safe Args actions from nav_graph.xml) -----
        binding.reportFaultButton.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToSubmitReportFragment()
            )
        }

        binding.myReportsButton.setOnClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToMyReportsFragment()
            )
        }

        // -- Surrounding issues: reports from ALL residents in one sub-county --
        // Other residents' reports open in the same details screen. There is no
        // action for this in nav_graph.xml, so we navigate by destination id
        // and pass the report id in a Bundle.
        val adapter = ReportAdapter(emptyList()) { report ->
            findNavController().navigate(
                R.id.reportDetailsFragment,
                bundleOf("reportId" to report.id)
            )
        }
        binding.areaRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.areaRecyclerView.adapter = adapter

        binding.areaSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            NairobiConfig.subCounties
        )

        binding.areaSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                // Stop watching the previous sub-county, then watch the new one
                areaReports?.removeObservers(viewLifecycleOwner)
                val live = reportViewModel.reportsInArea(NairobiConfig.subCounties[position])
                areaReports = live
                live.observe(viewLifecycleOwner) { reports ->
                    adapter.submitList(reports)
                    binding.areaEmptyTextView.visibility =
                        if (reports.isEmpty()) View.VISIBLE else View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
