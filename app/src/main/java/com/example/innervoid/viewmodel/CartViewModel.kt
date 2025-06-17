package com.example.innervoid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innervoid.data.model.CartItem
import com.example.innervoid.data.models.OrderItem
import com.example.innervoid.data.model.Product
import com.example.innervoid.data.repository.CartRepository
import com.example.innervoid.data.repository.OrderRepository
import com.example.innervoid.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val repository = CartRepository()
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice

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
                    calculateTotalPrice(result.data)
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

    fun createOrder(deliveryAddress: String) {
        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Пользователь не авторизован"
            return
        }

        val currentCartItems = _cartItems.value ?: run {
            _error.value = "Корзина пуста"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Преобразуем CartItem в OrderItem
                val orderItems = currentCartItems.map { cartItem ->
                    OrderItem(
                        id = cartItem.id,
                        productId = cartItem.productId,
                        userId = userId,
                        quantity = cartItem.quantity,
                        size = cartItem.size,
                        price = cartItem.price
                    )
                }

                when (val result = orderRepository.createOrder(userId, orderItems, deliveryAddress)) {
                    is Result.Success -> {
                        // Очищаем корзину после успешного создания заказа
                        currentCartItems.forEach { cartItem ->
                            repository.removeFromCart(cartItem.id, userId)
                        }
                        loadCartItems()
                    }
                    is Result.Error -> {
                        _error.value = result.message
                    }
                    is Result.Loading -> {
                        // Обработка состояния загрузки
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка при создании заказа"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateTotalPrice(items: List<CartItem>) {
        val total = items.sumOf { it.price * it.quantity }
        _totalPrice.value = total
    }
} 