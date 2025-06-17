package com.example.innervoid.data.repository

import com.example.innervoid.data.models.User
import com.example.innervoid.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val firebaseUser = auth.currentUser ?: return Result.Error("Пользователь не авторизован")
            
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: userDoc.getString("name") ?: "Пользователь",
                photoUrl = userDoc.getString("photoUrl") ?: "https://example.com/default-avatar.png",
                deliveryAddress = userDoc.getString("deliveryAddress") ?: "",
                isAdmin = userDoc.getBoolean("isAdmin") ?: false
            )
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при получении данных пользователя")
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun updateUser(user: User) {
        firestore.collection("users")
            .document(user.id)
            .set(user)
            .await()
    }
} 