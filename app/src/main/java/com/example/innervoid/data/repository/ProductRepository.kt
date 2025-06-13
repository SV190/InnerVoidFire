package com.example.innervoid.data.repository

import com.example.innervoid.data.models.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepository {
    private val products = mutableListOf<Product>()

    fun getProducts(): Flow<List<Product>> = flow {
        emit(products)
    }

    suspend fun addProduct(product: Product) {
        products.add(product)
    }

    suspend fun updateProduct(product: Product) {
        val index = products.indexOfFirst { it.id == product.id }
        if (index != -1) {
            products[index] = product
        }
    }

    suspend fun deleteProduct(productId: String) {
        products.removeIf { it.id == productId }
    }

    suspend fun getProduct(productId: String): Product? {
        return products.find { it.id == productId }
    }
} 