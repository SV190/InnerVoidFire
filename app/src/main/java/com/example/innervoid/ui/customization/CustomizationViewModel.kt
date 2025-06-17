package com.example.innervoid.ui.customization

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.innervoid.model.CustomizationData
import com.example.innervoid.model.PrintPosition

class CustomizationViewModel : ViewModel() {
    private val _customizationData = MutableLiveData(CustomizationData())
    val customizationData: LiveData<CustomizationData> = _customizationData

    fun setPrintUri(uri: String) {
        Log.d("CustomizationViewModel", "Setting print URI: $uri")
        val currentData = _customizationData.value ?: CustomizationData()
        _customizationData.value = currentData.copy(printUri = uri)
    }

    fun getPrintUri(): String? {
        val uri = _customizationData.value?.printUri?.takeIf { it.isNotEmpty() }
        Log.d("CustomizationViewModel", "Getting print URI: $uri")
        return uri
    }

    fun setPrintPosition(position: PrintPosition) {
        Log.d("CustomizationViewModel", "Setting print position: $position")
        val currentData = _customizationData.value ?: CustomizationData()
        _customizationData.value = currentData.copy(printPosition = position)
    }

    fun getCustomizationData(): CustomizationData = _customizationData.value ?: CustomizationData()
} 