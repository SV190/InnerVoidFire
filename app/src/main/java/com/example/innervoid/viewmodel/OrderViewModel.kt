package com.example.innervoid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innervoid.data.models.Order
import com.example.innervoid.data.models.OrderItem
import com.example.innervoid.data.repository.OrderRepository
import com.example.innervoid.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _currentOrder = MutableLiveData<Order?>()
    val currentOrder: LiveData<Order?> = _currentOrder

    private val _userOrders = MutableLiveData<List<Order>>()
    val userOrders: LiveData<List<Order>> = _userOrders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun createOrder(userId: String, cartItems: List<OrderItem>, deliveryAddress: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = orderRepository.createOrder(userId, cartItems, deliveryAddress)) {
                is Result.Success -> {
                    _currentOrder.value = result.data
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                is Result.Loading -> {
                    // Обработка состояния загрузки
                }
            }

            _isLoading.value = false
        }
    }

    fun getOrder(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = orderRepository.getOrder(orderId)) {
                is Result.Success -> {
                    _currentOrder.value = result.data
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                is Result.Loading -> {
                    // Обработка состояния загрузки
                }
            }

            _isLoading.value = false
        }
    }

    fun loadUserOrders() {
        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = orderRepository.getUserOrders(userId)) {
                is Result.Success -> {
                    _userOrders.value = result.data
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                is Result.Loading -> {
                    // Обработка состояния загрузки
                }
            }

            _isLoading.value = false
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = orderRepository.updateOrderStatus(orderId, status)) {
                is Result.Success -> {
                    getOrder(orderId)
                }
                is Result.Error -> {
                    _error.value = result.message
                }
                is Result.Loading -> {
                    // Обработка состояния загрузки
                }
            }

            _isLoading.value = false
        }
    }
} 