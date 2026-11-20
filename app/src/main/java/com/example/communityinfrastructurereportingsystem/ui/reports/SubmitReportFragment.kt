package com.example.communityinfrastructurereportingsystem.ui.reports

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.communityinfrastructurereportingsystem.data.local.SessionManager
import com.example.communityinfrastructurereportingsystem.data.model.NairobiConfig
import com.example.communityinfrastructurereportingsystem.data.model.ReportCategory
import com.example.communityinfrastructurereportingsystem.databinding.FragmentSubmitReportBinding
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.ReportViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File

class SubmitReportFragment : Fragment() {

    private var _binding: FragmentSubmitReportBinding? = null
    private val binding get() = _binding!!

    // Shared with the other screens (same ViewModel instance for the whole MainActivity)
    private val reportViewModel: ReportViewModel by activityViewModels()

    // What the user has added so far. Lost on rotation unless we save them (Lab 6, Part B)
    private var photoPath: String? = null     // photo that is confirmed
    private var pendingPath: String? = null   // file the camera is writing to right now
    private var latitude: Double? = null
    private var longitude: Double? = null

    // ── ACTIVITY RESULT LAUNCHERS ──────────────────────────────
    // Each one asks another app (or Android) to do something and gives us the answer.
    // They must be created here, not inside a click.

    // 1. Camera: the camera app saves a full-size photo into the Uri we give it
    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            if (saved) {
                photoPath = pendingPath
                showPhoto()
            } else {
                // The user cancelled: remove the empty file
                pendingPath?.let { File(it).delete() }
            }
            pendingPath = null
        }

    // 2. Gallery: Android's photo picker (needs no permission)
    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val path = copyToAppFolder(uri)
                if (path != null) {
                    photoPath = path
                    showPhoto()
                } else {
                    toast("Could not load that photo")
                }
            }
        }

    // 3. Location permission
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.values.any { it }) {
                fetchLocation()
            } else {
                toast("Location permission denied. You can still choose your sub-county.")
            }
        }

    // ── RESTORE after rotation ─────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            photoPath = savedInstanceState.getString("photo_path")
            pendingPath = savedInstanceState.getString("pending_path")
            if (savedInstanceState.containsKey("latitude")) {
                latitude = savedInstanceState.getDouble("latitude")
                longitude = savedInstanceState.getDouble("longitude")
            }
        }
    }

    // ── SAVE before rotation ───────────────────────────────────
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("photo_path", photoPath)
        outState.putString("pending_path", pendingPath)
        val lat = latitude
        val lng = longitude
        if (lat != null && lng != null) {
            outState.putDouble("latitude", lat)
            outState.putDouble("longitude", lng)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubmitReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Forget any old message left by another screen
        reportViewModel.clearMessage()

        // ── Dropdowns. The first item is a prompt: it is not a real choice,
        // so the ViewModel rejects it with "Choose the type of fault".
        val categories = listOf("Select fault type") + ReportCategory.all
        binding.categorySpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, categories
        )
        val areas = listOf("Select Nairobi sub-county") + NairobiConfig.subCounties
        binding.areaSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, areas
        )

        // Show what we restored after a rotation
        if (photoPath != null) showPhoto()
        if (latitude != null && longitude != null) showLocationText()

        // ── Buttons ────────────────────────────────────────────
        binding.takePhotoButton.setOnClickListener {
            val file = createImageFile()
            pendingPath = file.absolutePath
            // FileProvider turns the private file into a Uri the camera app is allowed to write to
            val uri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", file
            )
            takePhotoLauncher.launch(uri)
        }

        binding.galleryButton.setOnClickListener {
            pickPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.locationButton.setOnClickListener {
            val hasPermission =
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                fetchLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        binding.submitButton.setOnClickListener {
            val session = SessionManager(requireContext())

            // The ViewModel checks every rule (empty fields, Nairobi only...).
            // It returns an error text, or null when the report was accepted.
            val error = reportViewModel.addReport(
                title = binding.titleEditText.text.toString(),
                category = binding.categorySpinner.selectedItem.toString(),
                description = binding.descriptionEditText.text.toString(),
                area = binding.areaSpinner.selectedItem.toString(),
                latitude = latitude,
                longitude = longitude,
                photoPath = photoPath,
                reporterId = session.userId,
                reporterName = session.userName
            )
            if (error != null) {
                toast(error)
            }
        }

        // ── Watch the ViewModel ────────────────────────────────
        // Loading state: spinner on, button off while saving
        reportViewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.progressBar.visibility = if (saving) View.VISIBLE else View.GONE
            binding.submitButton.isEnabled = !saving
        }

        // Feedback: "Report submitted" or "Something went wrong..."
        reportViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                toast(message)
                reportViewModel.clearMessage()
                if (message == "Report submitted") {
                    // SAFE ARGS: generated from the <action> in nav_graph.xml
                    findNavController().navigate(
                        SubmitReportFragmentDirections.actionSubmitReportFragmentToMyReportsFragment()
                    )
                }
            }
        }
    }

    // ── HELPERS ────────────────────────────────────────────────

    // A new empty .jpg inside the app's private Pictures folder
    private fun createImageFile(): File {
        val folder = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: requireContext().filesDir
        return File(folder, "REPORT_${System.currentTimeMillis()}.jpg")
    }

    // The gallery gives a temporary Uri, so we COPY the photo into our own folder
    private fun copyToAppFolder(uri: Uri): String? {
        return try {
            val file = createImageFile()
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    // Glide loads the picture from its file path
    // Glide loads the picture from its file path
    private fun showPhoto() {
        val path = photoPath ?: return
        if (_binding == null) return
        binding.photoImageView.visibility = View.VISIBLE   // the picture box appears only once there is a photo
        Glide.with(this).load(File(path)).centerCrop().into(binding.photoImageView)
    }

    private fun showLocationText() {
        val lat = latitude ?: return
        val lng = longitude ?: return
        binding.locationTextView.text = String.format("Location added: %.5f, %.5f", lat, lng)
    }

    // We only get here after the permission was granted
    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        binding.locationTextView.text = "Getting your location..."
        val client = LocationServices.getFusedLocationProviderClient(requireActivity())
        client.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (_binding == null) return@addOnSuccessListener
            if (location == null) {
                binding.locationTextView.text = "Could not get location. Turn on GPS and try again."
            } else {
                latitude = location.latitude
                longitude = location.longitude
                showLocationText()
            }
        }.addOnFailureListener {
            if (_binding != null) {
                binding.locationTextView.text = "Could not get location. Try again."
            }
        }
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}