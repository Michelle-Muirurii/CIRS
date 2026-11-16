package com.example.communityinfrastructurereportingsystem.ui.report

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
import com.example.communityinfrastructurereportingsystem.data.model.NairobiConfig
import com.example.communityinfrastructurereportingsystem.data.model.ReportCategory
import com.example.communityinfrastructurereportingsystem.databinding.FragmentCreateReportBinding
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.AuthViewModel
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel

class CreateReportFragment : Fragment() {

    private var _binding: FragmentCreateReportBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val reportViewModel: ReportViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = authViewModel.currentUser.value
        if (currentUser == null) {
            findNavController().navigateUp()
            return
        }

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            ReportCategory.all
        ).apply {
            setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        }
        binding.spCategory.adapter = categoryAdapter

        val areaAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            NairobiConfig.subCounties
        ).apply {
            setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        }
        binding.spArea.adapter = areaAdapter

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val category = binding.spCategory.selectedItem?.toString() ?: ""
            val area = binding.spArea.selectedItem?.toString() ?: ""
            val description = binding.etDescription.text.toString()
            val latText = binding.etLatitude.text.toString().trim()
            val lngText = binding.etLongitude.text.toString().trim()

            val lat = if (latText.isNotEmpty()) latText.toDoubleOrNull() else null
            val lng = if (lngText.isNotEmpty()) lngText.toDoubleOrNull() else null

            val error = reportViewModel.addReport(
                title = title,
                category = category,
                description = description,
                area = area,
                latitude = lat,
                longitude = lng,
                photoPath = null,
                reporterId = currentUser.id,
                reporterName = currentUser.fullName
            )

            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        reportViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                reportViewModel.clearMessage()
                if (message == "Report submitted") {
                    findNavController().navigateUp()
                }
            }
        }

        reportViewModel.isSaving.observe(viewLifecycleOwner) { isSaving ->
            binding.progressBar.visibility = if (isSaving) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !isSaving
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
