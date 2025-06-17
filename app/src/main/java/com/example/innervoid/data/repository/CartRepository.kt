package com.example.innervoid.data.repository

import com.example.innervoid.data.model.CartItem
import com.example.innervoid.data.model.Product
import com.example.innervoid.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val db = FirebaseFirestore.getInstance()
    private val cartCollection = db.collection("carts")

    suspend fun addToCart(product: Product, size: String, userId: String): Result<Unit> {
        return try {
            // Получаем или создаем документ корзины пользователя
            val userCartRef = cartCollection.document(userId)
            
            // Проверяем, есть ли уже такой товар в корзине
            val existingItem = userCartRef.collection("items")
                .whereEqualTo("productId", product.id)
                .whereEqualTo("size", size)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (existingItem != null) {
                // Если товар уже есть, увеличиваем количество
                val currentQuantity = existingItem.getLong("quantity") ?: 1
                existingItem.reference.update("quantity", currentQuantity + 1).await()
            } else {
                // Если товара нет, создаем новый элемент корзины
                val cartItem = CartItem(
                    id = db.collection("carts").document().id,
                    productId = product.id,
                    userId = userId,
                    name = product.name,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    size = size,
                    quantity = 1
                )
                userCartRef.collection("items").document(cartItem.id).set(cartItem).await()
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при добавлении товара в корзину")
        }
    }

    suspend fun getCartItems(userId: String): Result<List<CartItem>> {
        return try {
            val items = cartCollection.document(userId)
                .collection("items")
                .get()
                .await()
                .toObjects(CartItem::class.java)
            
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при получении товаров из корзины")
        }
    }

    suspend fun updateQuantity(itemId: String, quantity: Int, userId: String): Result<Unit> {
        return try {
            if (quantity <= 0) {
                // Если количество 0 или меньше, удаляем товар из корзины
                cartCollection.document(userId)
                    .collection("items")
                    .document(itemId)
                    .delete()
                    .await()
            } else {
                // Иначе обновляем количество
                cartCollection.document(userId)
                    .collection("items")
                    .document(itemId)
                    .update("quantity", quantity)
                    .await()
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при обновлении количества товара")
        }
    }

    suspend fun removeFromCart(itemId: String, userId: String): Result<Unit> {
        return try {
            cartCollection.document(userId)
                .collection("items")
                .document(itemId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при удалении товара из корзины")
        }
    }
} 