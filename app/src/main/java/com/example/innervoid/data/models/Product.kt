package com.example.innervoid.data.models

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val inStock: Boolean = true
) 