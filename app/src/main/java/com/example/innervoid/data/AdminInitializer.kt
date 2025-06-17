package com.example.innervoid.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminInitializer {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun createAdminUser(email: String, password: String) {
        try {
            // Создаем пользователя в Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Не удалось создать пользователя")

            // Создаем запись в Firestore с правами админа
            val userData = hashMapOf(
                "email" to email,
                "admin" to true
            )

            db.collection("users").document(user.uid)
                .set(userData)
                .await()

            Log.d("AdminInitializer", "Админ успешно создан: ${user.uid}")
        } catch (e: Exception) {
            Log.e("AdminInitializer", "Ошибка создания админа", e)
            throw e
        }
    }
} 