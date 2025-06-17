package com.example.innervoid.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.innervoid.data.models.User
import com.example.innervoid.data.repository.AuthRepository
import com.example.innervoid.utils.Result
import com.example.innervoid.utils.ToastUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val authRepository = AuthRepository()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.getCurrentUser()
                when (result) {
                    is Result.Success -> {
                        _currentUser.value = result.data
                        _error.value = null
                    }
                    is Result.Error -> {
                        _error.value = result.message
                        _currentUser.value = null
                    }
                    is Result.Loading -> {
                        // Обработка состояния загрузки
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Произошла ошибка при проверке авторизации"
                _currentUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.signIn(email, password)
                when (result) {
                    is Result.Success -> {
                        _currentUser.value = result.data
                        _error.value = null
                        ToastUtils.showToast(getApplication(), "Вход выполнен успешно")
                    }
                    is Result.Error -> {
                        _error.value = result.message
                        _currentUser.value = null
                        ToastUtils.showToast(getApplication(), result.message)
                    }
                    is Result.Loading -> {
                        // Обработка состояния загрузки
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Произошла ошибка при входе"
                _currentUser.value = null
                ToastUtils.showToast(getApplication(), e.message ?: "Произошла ошибка при входе")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.signUp(email, password, name)
                when (result) {
                    is Result.Success -> {
                        _currentUser.value = result.data
                        _error.value = null
                    }
                    is Result.Error -> {
                        _error.value = result.message
                        _currentUser.value = null
                    }
                    is Result.Loading -> {
                        // Обработка состояния загрузки
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Произошла ошибка при регистрации"
                _currentUser.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.signOut()
                _currentUser.value = null
                _error.value = null
                ToastUtils.showToast(getApplication(), "Выход выполнен")
            } catch (e: Exception) {
                _error.value = e.message ?: "Произошла ошибка при выходе"
                ToastUtils.showToast(getApplication(), e.message ?: "Произошла ошибка при выходе")
            } finally {
                _isLoading.value = false
            }
        }
    }
} 