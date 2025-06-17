package com.example.innervoid.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.innervoid.MainActivity
import com.example.innervoid.R
import com.example.innervoid.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var navController: NavController
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверяем авторизацию
        if (auth.currentUser == null) {
            Log.d("AdminActivity", "User not authenticated, redirecting to auth")
            // Используем навигацию вместо прямого запуска активности
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController
            navController.navigate(R.id.authFragment)
            return
        }

        // Проверяем, является ли пользователь админом
        checkAdminStatus()
    }

    private fun checkAdminStatus() {
        val userId = auth.currentUser?.uid ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val isAdmin = document.getBoolean("admin") ?: false
                if (!isAdmin) {
                    Log.d("AdminActivity", "User is not admin, redirecting to main")
                    // Перенаправляем на главную активность
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    return@addOnSuccessListener
                }
                
                // Если пользователь админ, настраиваем навигацию
                setupNavigation()
            }
            .addOnFailureListener { e ->
                Log.e("AdminActivity", "Error checking admin status", e)
                // В случае ошибки, для безопасности перенаправляем на авторизацию
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                navController = navHostFragment.navController
                navController.navigate(R.id.authFragment)
            }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.adminBottomNavView.setupWithNavController(navController)
    }
} 