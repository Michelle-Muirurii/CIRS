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

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(CirsDatabase.getDatabase(application).userDao())

    private val _currentUser = MutableLiveData<User?>()
    private val _registeredEmail = MutableLiveData<String?>()
    private val _message = MutableLiveData<String?>()
    private val _isLoading = MutableLiveData(false)

    val currentUser: LiveData<User?> = _currentUser
    val registeredEmail: LiveData<String?> = _registeredEmail
    val message: LiveData<String?> = _message
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        viewModelScope.launch { repository.ensureStaffAccount() }
    }

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

    fun logout() {
        _currentUser.value = null
    }

    fun clearMessage() { _message.value = null }
    fun clearRegistered() { _registeredEmail.value = null }
}
