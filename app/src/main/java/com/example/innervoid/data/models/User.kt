package com.example.innervoid.data.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val deliveryAddress: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) 