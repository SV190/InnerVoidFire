package com.example.innervoid.ui.customization

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.innervoid.databinding.FragmentCustomizationContainerBinding
import com.example.innervoid.model.CustomizationData
import com.example.innervoid.model.PrintPosition

class CustomizationContainerFragment : Fragment() {
    private var _binding: FragmentCustomizationContainerBinding? = null
    private val binding get() = _binding!!
    
    private var customizationData = CustomizationData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomizationContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CustomizationContainer", "Fragment created with data: $customizationData")
    }

    fun setPrintUri(uri: String) {
        Log.d("CustomizationContainer", "Setting print URI: $uri")
        customizationData = customizationData.copy(printUri = uri)
    }

    fun getPrintUri(): String? {
        val uri = customizationData.printUri.takeIf { it.isNotEmpty() }
        Log.d("CustomizationContainer", "Getting print URI: $uri")
        return uri
    }

    fun setPrintPosition(position: PrintPosition) {
        Log.d("CustomizationContainer", "Setting print position: $position")
        customizationData = customizationData.copy(printPosition = position)
    }

    fun getCustomizationData(): CustomizationData = customizationData

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 