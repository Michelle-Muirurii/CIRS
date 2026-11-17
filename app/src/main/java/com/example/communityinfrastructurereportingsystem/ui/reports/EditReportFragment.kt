package com.example.communityinfrastructurereportingsystem.ui.reports

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.communityinfrastructurereportingsystem.data.model.NairobiConfig
import com.example.communityinfrastructurereportingsystem.data.model.Report
import com.example.communityinfrastructurereportingsystem.data.model.ReportCategory
import com.example.communityinfrastructurereportingsystem.databinding.FragmentEditReportBinding
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel

class EditReportFragment : Fragment() {
    private var _binding: FragmentEditReportBinding? = null
    private val binding get() = _binding!!
    private val args: EditReportFragmentArgs by navArgs()
    private val reportViewModel: ReportViewModel by activityViewModels()

    // The report being edited (loaded once from Room)
    private var currentReport: Report? = null

    // Same lists as the submit form: the first item is a prompt, not a real choice
    private val categories = listOf("Select fault type") + ReportCategory.all
    private val areas = listOf("Select Nairobi sub-county") + NairobiConfig.subCounties

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reportViewModel.clearMessage()   // forget any old message

        binding.categorySpinner.adapter = ArrayAdapter(
            requireContext(), R.layout.simple_spinner_dropdown_item, categories
        )
        binding.areaSpinner.adapter = ArrayAdapter(
            requireContext(), R.layout.simple_spinner_dropdown_item, areas
        )

        // Load the report ONCE and fill the form. After a rotation (savedInstanceState != null)
        // Android restores what the user typed, so we must not overwrite it.
        reportViewModel.reportById(args.reportId).observe(viewLifecycleOwner) { report ->
            if (report != null && currentReport == null) {
                currentReport = report
                if (savedInstanceState == null) fillForm(report)
            }
        }

        binding.saveButton.setOnClickListener {
            val report = currentReport ?: return@setOnClickListener
            // copy() makes a new Report with the same values, except the ones we change
            val updated = report.copy(
                title = binding.titleEditText.text.toString(),
                category = binding.categorySpinner.selectedItem.toString(),
                description = binding.descriptionEditText.text.toString(),
                area = binding.areaSpinner.selectedItem.toString()
            )
            // The ViewModel checks the rules and returns an error text, or null if accepted
            val error = reportViewModel.updateReport(updated)
            if (error != null) toast(error)
        }

        reportViewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.progressBar.visibility = if (saving) View.VISIBLE else View.GONE
            binding.saveButton.isEnabled = !saving
        }

        reportViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                toast(message)
                reportViewModel.clearMessage()
                if (message == "Report updated") {
                    findNavController().navigateUp()   // back to the details screen
                }
            }
        }
    }

    private fun fillForm(report: Report) {
        binding.titleEditText.setText(report.title)
        binding.descriptionEditText.setText(report.description)
        binding.categorySpinner.setSelection(categories.indexOf(report.category).coerceAtLeast(0))
        binding.areaSpinner.setSelection(areas.indexOf(report.area).coerceAtLeast(0))
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
