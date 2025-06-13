package com.example.innervoid.data.models

data class CartItem(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val quantity: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) 