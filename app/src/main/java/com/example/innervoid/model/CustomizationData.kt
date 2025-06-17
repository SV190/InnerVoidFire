package com.example.innervoid.model

data class CustomizationData(
    val printUri: String = "",
    val printPosition: PrintPosition = PrintPosition(),
    val size: String = "",
    val wishes: String = ""
)

data class PrintPosition(
    val x: Float = 0f,
    val y: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f
) 