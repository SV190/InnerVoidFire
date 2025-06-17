package com.example.innervoid.data.models

data class CustomizationData(
    val id: String = "",
    val userId: String = "",
    val modelName: String = "",
    val description: String = "",
    val imageBytes: ByteArray = ByteArray(0),
    val createdAt: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomizationData

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (modelName != other.modelName) return false
        if (description != other.description) return false
        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + modelName.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + imageBytes.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
} 