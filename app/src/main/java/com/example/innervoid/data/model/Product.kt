package com.example.innervoid.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val inStock: Boolean = true,
    val sizes: List<String> = listOf("S", "M", "L", "XL")
) 