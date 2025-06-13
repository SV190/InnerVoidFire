package com.example.innervoid.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val sizes: List<String> = listOf("S", "M", "L", "XL"),
    val description: String = ""
) 