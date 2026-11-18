package com.example.communityinfrastructurereportingsystem.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.communityinfrastructurereportingsystem.R
import com.example.communityinfrastructurereportingsystem.data.model.UserRole
import com.example.communityinfrastructurereportingsystem.databinding.FragmentLoginBinding
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            val validationError = authViewModel.login(email, password)
            if (validationError != null) {
                Toast.makeText(requireContext(), validationError, Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id. action_loginFragment_to_registerFragment)
        }

        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                if (user.role == UserRole.STAFF) {
                    findNavController().navigate(R.id.action_loginFragment_to_staffDashboardFragment)
                } else {
                    findNavController().navigate(R.id.action_loginFragment_to_reportListFragment)
                }
            }
        }

        authViewModel.registeredEmail.observe(viewLifecycleOwner) { email ->
            if (email != null) {
                binding.etEmail.setText(email)
                Toast.makeText(requireContext(), "Registration successful! Please log in.", Toast.LENGTH_LONG).show()
                authViewModel.clearRegistered()
            }
        }

        authViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                authViewModel.clearMessage()
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
