package com.example.communityinfrastructurereportingsystem.ui.auth


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.communityinfrastructurereportingsystem.data.local.SessionManager
import com.example.communityinfrastructurereportingsystem.databinding.ActivityLoginBinding
import com.example.communityinfrastructurereportingsystem.ui.main.MainActivity
import com.example.communityinfrastructurereportingsystem.ui.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // The ViewModel does the checking and the database work; this screen only shows things
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Already logged in earlier? Skip this screen
        if (SessionManager(this).isLoggedIn()) {
            openMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If we came from Register, the email was attached with putExtra (Lab 5)
        val registeredEmail = intent.getStringExtra("EMAIL")
        if (registeredEmail != null) {
            binding.emailEditText.setText(registeredEmail)
        }

        binding.loginButton.setOnClickListener {
            val error = authViewModel.login(
                binding.emailEditText.text.toString(),
                binding.passwordEditText.text.toString()
            )
            // The ViewModel returns an error text if the input is not valid
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // ── Watch the ViewModel's LiveData ───────────────────────
        // Loading state: show the spinner and block double taps
        authViewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.loginButton.isEnabled = !loading
        }

        // Feedback: "Wrong email or password", etc.
        authViewModel.message.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearMessage()
            }
        }

        // Success: remember the user, then open the app
        authViewModel.currentUser.observe(this) { user ->
            if (user != null) {
                SessionManager(this).saveUser(user)
                openMain()
            }
        }
    }

    // finish() so that Back from the app does not return to Login
    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}