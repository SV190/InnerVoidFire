package com.example.innervoid.data.models

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 