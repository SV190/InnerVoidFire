package com.example.innervoid.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innervoid.data.models.User
import com.example.innervoid.data.repository.AuthRepository
import com.example.innervoid.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableLiveData<Result<User>>()
    val currentUser: LiveData<Result<User>> = _currentUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isEmailVerified = MutableLiveData<Boolean>()
    val isEmailVerified: LiveData<Boolean> = _isEmailVerified

    init {
        loadUserData()
        checkEmailVerificationStatus()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.getCurrentUser()
                _currentUser.value = result
            } catch (e: Exception) {
                _currentUser.value = Result.Error(e.message ?: "Ошибка при загрузке данных пользователя")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkEmailVerificationStatus() {
        viewModelScope.launch {
            _isEmailVerified.value = authRepository.isEmailVerified()
        }
    }

    fun updateProfile(name: String, email: String, address: String, photoUrl: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUserData = (_currentUser.value as? Result.Success<User>)?.data
                    ?: throw Exception("Данные пользователя не найдены")

                // Если email изменился, обновляем его
                if (email != currentUserData.email) {
                    val emailUpdateResult = authRepository.updateEmail(email)
                    if (emailUpdateResult is Result.Error) {
                        _error.value = emailUpdateResult.message
                        return@launch
                    }
                }

                // Обновляем остальные данные пользователя
                val updatedUser = currentUserData.copy(
                    name = name,
                    email = email,
                    deliveryAddress = address,
                    photoUrl = photoUrl ?: currentUserData.photoUrl
                )
                
                val updateResult = authRepository.updateUser(updatedUser)
                if (updateResult is Result.Success) {
                    _currentUser.value = updateResult
                    _error.value = null
                } else {
                    _error.value = (updateResult as Result.Error).message
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка при обновлении профиля"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.sendEmailVerification()
                if (result is Result.Error) {
                    _error.value = result.message
                } else {
                    _error.value = "Верификационный email отправлен. Проверьте почту."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка при отправке верификационного email"
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
                _currentUser.value = Result.Error("Пользователь не авторизован")
            } catch (e: Exception) {
                _currentUser.value = Result.Error(e.message ?: "Ошибка при выходе из аккаунта")
            } finally {
                _isLoading.value = false
            }
        }
    }
} 