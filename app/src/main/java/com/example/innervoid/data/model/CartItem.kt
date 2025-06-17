package com.example.innervoid.data.model

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val size: String = "",
    val quantity: Int = 1
) 