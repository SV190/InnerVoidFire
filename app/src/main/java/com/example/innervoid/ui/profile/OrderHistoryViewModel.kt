package com.example.innervoid.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innervoid.data.models.Order
import com.example.innervoid.data.repository.OrderRepository
import com.example.innervoid.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class OrderHistoryViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = orderRepository.getUserOrders(userId)
                when (result) {
                    is Result.Success -> {
                        _orders.value = result.data
                    }
                    is Result.Error -> {
                        _error.value = result.message
                    }
                    is Result.Loading -> {
                        // Обработка состояния загрузки
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка при загрузке заказов"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 