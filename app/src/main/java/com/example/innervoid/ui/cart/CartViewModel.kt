package com.example.innervoid.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innervoid.data.model.CartItem
import com.example.innervoid.data.model.Product
import com.example.innervoid.data.repository.CartRepository
import com.example.innervoid.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val repository = CartRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadCartItems()
    }

    fun addToCart(product: Product, size: String) {
        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            when (val result = repository.addToCart(product, size, userId)) {
                is Result.Success -> {
                    loadCartItems()
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

    fun loadCartItems() {
        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            when (val result = repository.getCartItems(userId)) {
                is Result.Success -> {
                    _cartItems.value = result.data
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

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            when (val result = repository.updateQuantity(cartItem.id, newQuantity, userId)) {
                is Result.Success -> {
                    loadCartItems()
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

    fun removeFromCart(cartItemId: String) {
        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            when (val result = repository.removeFromCart(cartItemId, userId)) {
                is Result.Success -> {
                    loadCartItems()
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