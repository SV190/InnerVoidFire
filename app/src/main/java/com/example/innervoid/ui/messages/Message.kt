package com.example.innervoid.ui.messages

data class Message(
    val id: String,
    val text: String,
    val isFromAdmin: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) 