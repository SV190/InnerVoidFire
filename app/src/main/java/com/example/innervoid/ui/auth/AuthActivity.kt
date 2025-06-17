package com.example.innervoid.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.innervoid.MainActivity
import com.example.innervoid.databinding.ActivityAuthBinding
import com.example.innervoid.ui.admin.AdminActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверяем, авторизован ли пользователь
        if (auth.currentUser != null) {
            checkUserType()
        }

        setupAuthUI()
    }

    private fun setupAuthUI() {
        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            signIn(email, password)
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            register(email, password)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d("AuthActivity", "Sign in successful, checking user type")
                checkUserType()
            }
            .addOnFailureListener { e ->
                Log.e("AuthActivity", "Sign in failed", e)
                Toast.makeText(this, "Ошибка входа: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                
                // Создаем запись пользователя в Firestore
                val userData = hashMapOf(
                    "email" to email,
                    "admin" to false
                )
                
                db.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        Log.d("AuthActivity", "User registered successfully")
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("AuthActivity", "Error creating user document", e)
                        Toast.makeText(this, "Ошибка регистрации: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AuthActivity", "Registration failed", e)
                Toast.makeText(this, "Ошибка регистрации: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserType() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val isAdmin = document.getBoolean("admin") ?: false
                if (isAdmin) {
                    Log.d("AuthActivity", "User is admin, redirecting to admin panel")
                    startActivity(Intent(this, AdminActivity::class.java))
                } else {
                    Log.d("AuthActivity", "User is regular, redirecting to main screen")
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("AuthActivity", "Error checking user type", e)
                // В случае ошибки, для безопасности перенаправляем на главный экран
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
} 