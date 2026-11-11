package com.example.communityinfrastructurereportingsystem.ui.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.communityinfrastructurereportingsystem.data.local.CirsDatabase
import com.example.communityinfrastructurereportingsystem.data.model.User
import com.example.communityinfrastructurereportingsystem.data.repository.UserRepository
import kotlinx.coroutines.launch

// AndroidViewModel: a ViewModel that receives the Application (safe to keep).
// No Toast, no View, no Activity in here: screens show the messages.
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Build the data layer once: database -> DAO -> repository
    private val repository = UserRepository(CirsDatabase.getDatabase(application).userDao())

    // PRIVATE and MUTABLE: only this ViewModel changes them
    private val _currentUser = MutableLiveData<User?>()
    private val _registeredEmail = MutableLiveData<String?>()
    private val _message = MutableLiveData<String?>()
    private val _isLoading = MutableLiveData(false)

    // PUBLIC and READ-ONLY: screens can watch them, not change them
    val currentUser: LiveData<User?> = _currentUser          // set after a successful login
    val registeredEmail: LiveData<String?> = _registeredEmail // set after a successful register
    val message: LiveData<String?> = _message                // error/feedback text for the screen
    val isLoading: LiveData<Boolean> = _isLoading            // show a ProgressBar while true

    init {
        // Make sure the county staff account exists
        viewModelScope.launch { repository.ensureStaffAccount() }
    }

    // Returns an error message, or null if the input is valid (same idea as Lab 8)
    fun register(fullName: String, email: String, password: String): String? {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()

        if (cleanName.isEmpty() || cleanEmail.isEmpty() || password.isEmpty()) {
            return "Please fill in all fields"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return "Enter a valid email address"
        }
        if (password.length < 6) {
            return "Password must be at least 6 characters"
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val id = repository.register(cleanName, cleanEmail, password)
                if (id == -1L) {
                    _message.value = "That email is already registered"
                } else {
                    _registeredEmail.value = cleanEmail
                }
            } catch (e: Exception) {
                _message.value = "Could not create the account. Try again."
            }
            _isLoading.value = false
        }
        return null
    }

    fun login(email: String, password: String): String? {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            return "Please enter your email and password"
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = repository.login(cleanEmail, password)
                if (user == null) {
                    _message.value = "Wrong email or password"
                } else {
                    _currentUser.value = user
                }
            } catch (e: Exception) {
                _message.value = "Could not log in. Try again."
            }
            _isLoading.value = false
        }
        return null
    }

    // Call after the screen has shown the message, so it does not ever show again
    fun clearMessage() { _message.value = null }
    fun clearRegistered() { _registeredEmail.value = null }
}