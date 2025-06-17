package com.example.innervoid.data.models

import com.google.firebase.firestore.DocumentId

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val size: String = "XS, S, M, L, XL",
    val createdAt: Long = System.currentTimeMillis(),
    val inStock: Boolean = true
)

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val quantity: Int = 1,
    val size: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val price: Double = 0.0
)

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val fromAdmin: Boolean = false
)

data class OrderItem(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val quantity: Int = 0,
    val size: String = "",
    val price: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val name: String = "",
    val imageUrl: String = ""
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val deliveryAddress: String = "",
    val status: String = "pending", // pending, processing, shipped, delivered
    val createdAt: Long = System.currentTimeMillis()
)

data class User(
    val id: String = "",
    val name: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val deliveryAddress: String = "",
    val isAdmin: Boolean = false
) {
    // Геттер для получения отображаемого имени
    fun getFormattedName(): String {
        return if (displayName.isNotEmpty()) displayName else name
    }
}

data class DialogueItem(
    val user: User,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val hasUnreadMessages: Boolean = false,
    val unreadCount: Int = 0
) 