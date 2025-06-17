package com.example.innervoid.data.repository

import com.example.innervoid.data.models.Order
import com.example.innervoid.data.models.OrderItem
import com.example.innervoid.utils.Result
import com.example.innervoid.data.FirebaseManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")
    private val firebaseManager = FirebaseManager()

    suspend fun createOrder(userId: String, cartItems: List<OrderItem>, deliveryAddress: String): Result<Order> {
        return try {
            val orderId = ordersCollection.document().id
            val totalPrice = cartItems.sumOf { it.price * it.quantity }
            
            val order = Order(
                id = orderId,
                userId = userId,
                items = cartItems,
                totalPrice = totalPrice,
                deliveryAddress = deliveryAddress,
                status = "pending",
                createdAt = System.currentTimeMillis()
            )
            
            ordersCollection.document(orderId).set(order).await()

            // Отправляем сообщение админу о новом заказе
            val messageText = buildString {
                append("Новый заказ #$orderId\n")
                append("От пользователя: $userId\n")
                append("Адрес доставки: $deliveryAddress\n")
                append("Сумма заказа: $totalPrice\n")
                append("Товары:\n")
                cartItems.forEach { item ->
                    append("- ${item.name} (${item.size}): ${item.quantity} шт. x ${item.price} = ${item.price * item.quantity}\n")
                }
            }

            val message = com.example.innervoid.data.models.Message(
                id = db.collection("messages").document().id,
                senderId = userId,
                receiverId = "admin",
                content = messageText,
                fromAdmin = false,
                createdAt = System.currentTimeMillis(),
                read = false
            )

            firebaseManager.addMessage(userId, message)
            
            Result.Success(order)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при создании заказа")
        }
    }

    suspend fun getOrder(orderId: String): Result<Order> {
        return try {
            val document = ordersCollection.document(orderId).get().await()
            if (document.exists()) {
                val order = document.toObject(Order::class.java)
                if (order != null) {
                    Result.Success(order)
                } else {
                    Result.Error("Не удалось преобразовать данные заказа")
                }
            } else {
                Result.Error("Заказ не найден")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при получении заказа")
        }
    }

    suspend fun getUserOrders(userId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val orders = snapshot.toObjects(Order::class.java)
            Result.Success(orders)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при получении заказов пользователя")
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId)
                .update("status", status)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при обновлении статуса заказа")
        }
    }
} 