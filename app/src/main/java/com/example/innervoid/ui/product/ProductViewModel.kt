package com.example.innervoid.ui.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innervoid.data.model.Product
import com.example.innervoid.data.repository.ProductRepository
import com.example.innervoid.utils.Result
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _product = MutableLiveData<Result<Product>>()
    val product: LiveData<Result<Product>> = _product

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            when (val result = repository.getProducts()) {
                is Result.Success -> {
                    _products.value = result.data
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

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _product.value = Result.Loading
            _product.value = repository.getProduct(productId)
        }
    }
} 