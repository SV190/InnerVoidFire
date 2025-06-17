package com.example.innervoid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.innervoid.databinding.ActivityMainBinding
import com.example.innervoid.ui.admin.AdminActivity
import com.example.innervoid.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupNavigation()
        checkUserStatus()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_catalog,
                R.id.navigation_cart,
                R.id.navigation_profile,
                R.id.navigation_customization,
                R.id.navigation_messages
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("MainActivity", "Navigation to: ${destination.label}")
            binding.bottomNavView.visibility = when (destination.id) {
                R.id.navigation_home,
                R.id.navigation_catalog,
                R.id.navigation_cart,
                R.id.navigation_profile,
                R.id.navigation_customization,
                R.id.navigation_messages -> android.view.View.VISIBLE
                else -> android.view.View.GONE
            }

            // Устанавливаем заголовок для фрагмента деталей продукта
            if (destination.id == R.id.productDetailFragment) {
                supportActionBar?.title = "Детали товара"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("MainActivity", "User not authenticated")
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val isAdmin = document.getBoolean("admin") ?: false
                if (isAdmin) {
                    Log.d("MainActivity", "User is admin")
                    startActivity(Intent(this, AdminActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error checking admin status", e)
            }
    }
} 