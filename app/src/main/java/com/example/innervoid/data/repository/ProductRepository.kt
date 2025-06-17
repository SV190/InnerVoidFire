package com.example.innervoid.data.repository

import com.example.innervoid.data.model.Product
import com.example.innervoid.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")
    private val products = mutableListOf<Product>()

    fun getProductsFlow(): Flow<List<Product>> = flow {
        try {
            val snapshot = productsCollection.get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            emit(products)
        } catch (e: Exception) {
            emit(emptyList())
        }
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

    suspend fun getProduct(productId: String): Result<Product> {
        return try {
            val document = productsCollection.document(productId).get().await()
            if (document.exists()) {
                val product = document.toObject(Product::class.java)
                if (product != null) {
                    Result.Success(product)
                } else {
                    Result.Error("Не удалось преобразовать данные товара")
                }
            } else {
                Result.Error("Товар не найден")
            }
        } catch (e: Exception) {
            Result.Error("Ошибка при загрузке товара: ${e.message}")
        }
    }

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val snapshot = productsCollection.get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)
            }
            Result.Success(products)
        } catch (e: Exception) {
            Result.Error("Ошибка при загрузке товаров: ${e.message}")
        }
    }

    suspend fun getAllProducts(): List<Product> {
        val snapshot = productsCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
    }

    suspend fun getProductsByCategory(category: String): List<Product> {
        val snapshot = productsCollection
            .whereEqualTo("category", category)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
    }
} 