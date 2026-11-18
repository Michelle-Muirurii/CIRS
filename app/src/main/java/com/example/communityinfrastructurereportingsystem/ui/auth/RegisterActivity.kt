package com.example.communityinfrastructurereportingsystem.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.communityinfrastructurereportingsystem.databinding.ActivityRegisterBinding
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val error = authViewModel.register(
                binding.fullNameEditText.text.toString(),
                binding.registerEmailEditText.text.toString(),
                binding.registerPasswordEditText.text.toString()
            )
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        // Register was opened from Login, so Login is still underneath: just close this one
        binding.loginLinkTextView.setOnClickListener {
            finish()
        }

        authViewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.registerButton.isEnabled = !loading
        }

        // "That email is already registered", etc.
        authViewModel.message.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearMessage()
            }
        }

        // Success: go back to Login and hand over the email (putExtra, as in Lab 5)
        authViewModel.registeredEmail.observe(this) { email ->
            if (email != null) {
                authViewModel.clearRegistered()
                Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("EMAIL", email)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
    }
}