package com.example.innervoid.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innervoid.data.FirebaseManager
import com.example.innervoid.data.models.Product
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val firebaseManager = FirebaseManager()
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _currentProduct = MutableLiveData<Product>()
    val currentProduct: LiveData<Product> = _currentProduct

    fun loadProducts() {
        Log.d("ProductViewModel", "Starting to load products")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("ProductViewModel", "Fetching products from Firestore")
                val result = firebaseManager.getAllProducts()
                Log.d("ProductViewModel", "Received ${result.documents.size} products from Firestore")
                val productList = result.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)
                }
                Log.d("ProductViewModel", "Mapped ${productList.size} products")
                _products.value = productList
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading products", e)
                _error.value = e.message ?: "Ошибка загрузки продуктов"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getProduct(productId: String): LiveData<Product> {
        Log.d("ProductViewModel", "Getting product with ID: $productId")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("ProductViewModel", "Fetching product from Firestore")
                val result = firebaseManager.getProduct(productId)
                Log.d("ProductViewModel", "Received product document: ${result.exists()}")
                val product = result.toObject(Product::class.java)
                Log.d("ProductViewModel", "Mapped product: $product")
                _currentProduct.value = product
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading product", e)
                _error.value = e.message ?: "Ошибка загрузки продукта"
            } finally {
                _isLoading.value = false
            }
        }
        return currentProduct
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                firebaseManager.addProduct(product)
                loadProducts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка добавления продукта"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                firebaseManager.updateProduct(product)
                loadProducts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка обновления продукта"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                firebaseManager.deleteProduct(productId)
                loadProducts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка удаления продукта"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 