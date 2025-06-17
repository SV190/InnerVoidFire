package com.example.innervoid.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innervoid.data.AuthManager
import com.example.innervoid.data.models.User
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authManager = AuthManager()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userData = authManager.getCurrentUserData()
                _currentUser.value = userData
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error checking auth state", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = authManager.signUp(email, password, name)
                result.fold(
                    onSuccess = {
                        val userData = authManager.getCurrentUserData()
                        _currentUser.value = userData
                    },
                    onFailure = {
                        _error.value = it.message
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error signing up", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = authManager.signIn(email, password)
                result.fold(
                    onSuccess = {
                        val userData = authManager.getCurrentUserData()
                        _currentUser.value = userData
                    },
                    onFailure = {
                        _error.value = it.message
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error signing in", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _currentUser.value = null
    }
} 