package com.example.innervoid

import android.app.Application
import com.example.innervoid.data.TestDataInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InnerVoidApp : Application() {
    companion object {
        lateinit var instance: InnerVoidApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Инициализация тестовых данных
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TestDataInitializer().initializeTestData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 