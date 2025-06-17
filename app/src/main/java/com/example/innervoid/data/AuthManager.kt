package com.example.innervoid.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.innervoid.data.models.User
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth: FirebaseAuth = Firebase.auth
    private val firebaseManager = FirebaseManager()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun signUp(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            
            if (user != null) {
                // Создаем запись пользователя в Firestore
                val userData = User(
                    id = user.uid,
                    name = name,
                    email = email,
                    photoUrl = "https://example.com/default-avatar.png",
                    deliveryAddress = ""
                )
                firebaseManager.addUser(userData)
                Result.success(user)
            } else {
                Result.failure(Exception("Ошибка создания пользователя"))
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Error signing up", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Ошибка входа"))
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Error signing in", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getCurrentUserData(): User? {
        val user = currentUser ?: return null
        return try {
            val doc = firebaseManager.getUser(user.uid)
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("AuthManager", "Error getting user data", e)
            null
        }
    }
} 