package com.example.innervoid.data.repository

import android.util.Log
import com.example.innervoid.data.models.User
import com.example.innervoid.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val firebaseUser = auth.currentUser ?: return Result.Error("Пользователь не авторизован")
            
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val isAdmin = userDoc.getBoolean("admin") ?: false
            Log.d("AuthRepository", "User ${firebaseUser.uid} isAdmin: $isAdmin")

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: userDoc.getString("displayName") ?: "Пользователь",
                photoUrl = userDoc.getString("photoUrl") ?: "https://example.com/default-avatar.png",
                deliveryAddress = userDoc.getString("deliveryAddress") ?: "",
                isAdmin = isAdmin
            )
            
            Result.Success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting user data", e)
            Result.Error(e.message ?: "Ошибка при получении данных пользователя")
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.Error("Ошибка входа")
            
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val isAdmin = userDoc.getBoolean("admin") ?: false
            Log.d("AuthRepository", "User ${firebaseUser.uid} isAdmin: $isAdmin")

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: userDoc.getString("displayName") ?: "Пользователь",
                photoUrl = userDoc.getString("photoUrl") ?: "https://example.com/default-avatar.png",
                deliveryAddress = userDoc.getString("deliveryAddress") ?: "",
                isAdmin = isAdmin
            )
            
            Result.Success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error signing in", e)
            Result.Error(e.message ?: "Ошибка при входе")
        }
    }

    suspend fun signUp(email: String, password: String, name: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.Error("Ошибка регистрации")
            
            // Обновляем displayName пользователя
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            firebaseUser.updateProfile(profileUpdates).await()
            
            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = name,
                photoUrl = "https://example.com/default-avatar.png",
                deliveryAddress = "",
                isAdmin = false // По умолчанию новый пользователь не является администратором
            )
            
            // Создаем запись пользователя в Firestore
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Ошибка при регистрации")
        }
    }

    suspend fun updateEmail(newEmail: String): Result<User> {
        return try {
            val firebaseUser = auth.currentUser ?: return Result.Error("Пользователь не авторизован")
            
            // Обновляем email только в Firestore (без Firebase Auth)
            firestore.collection("users")
                .document(firebaseUser.uid)
                .update("email", newEmail)
                .await()
            
            // Получаем обновленные данные пользователя
            getCurrentUser()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating email", e)
            Result.Error(e.message ?: "Ошибка при обновлении email")
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val firebaseUser = auth.currentUser ?: return Result.Error("Пользователь не авторизован")
            firebaseUser.sendEmailVerification().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error sending email verification", e)
            Result.Error(e.message ?: "Ошибка при отправке верификационного email")
        }
    }

    suspend fun isEmailVerified(): Boolean {
        return try {
            val firebaseUser = auth.currentUser
            firebaseUser?.isEmailVerified ?: false
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error checking email verification", e)
            false
        }
    }

    suspend fun updateUser(user: User): Result<User> {
        return try {
            val firebaseUser = auth.currentUser ?: return Result.Error("Пользователь не авторизован")
            
            // Обновляем данные в Firestore
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            
            // Обновляем displayName в Firebase Auth если он изменился
            if (user.name != firebaseUser.displayName) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = user.name
                }
                firebaseUser.updateProfile(profileUpdates).await()
            }
            
            Result.Success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating user", e)
            Result.Error(e.message ?: "Ошибка при обновлении пользователя")
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }
} 