package com.example.innervoid.ui.cart

data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val size: String,
    val quantity: Int = 1
) 