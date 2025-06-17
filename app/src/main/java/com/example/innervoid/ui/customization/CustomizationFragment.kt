package com.example.innervoid.ui.customization

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.innervoid.R
import com.example.innervoid.data.FirebaseManager
import com.example.innervoid.data.models.CustomizationData
import com.example.innervoid.databinding.FragmentCustomizationBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

class CustomizationFragment : Fragment() {
    private var _binding: FragmentCustomizationBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadCustomizationData()
    }

    private fun setupUI() {
        binding.nextButton.setOnClickListener {
            if (validateInput()) {
                saveCustomizationData()
                findNavController().navigate(R.id.action_customizationFragment_to_sizeAndWishesFragment)
            }
        }
    }

    private fun validateInput(): Boolean {
        val modelName = binding.modelNameInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()
        val imageDrawable = binding.modelImage.drawable

        if (modelName.isEmpty()) {
            binding.modelNameInput.error = "Введите название модели"
            return false
        }

        if (description.isEmpty()) {
            binding.descriptionInput.error = "Введите описание"
            return false
        }

        if (imageDrawable == null) {
            Toast.makeText(context, "Выберите изображение модели", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveCustomizationData() {
        val userId = auth.currentUser?.uid ?: return
        val modelName = binding.modelNameInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()
        val imageDrawable = binding.modelImage.drawable as? BitmapDrawable
        val imageBitmap = imageDrawable?.bitmap

        if (imageBitmap == null) {
            Toast.makeText(context, "Ошибка: изображение не выбрано", Toast.LENGTH_SHORT).show()
            return
        }

        val imageBytes = ByteArrayOutputStream().apply {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, this)
        }.toByteArray()

        val customizationData = CustomizationData(
            id = UUID.randomUUID().toString(),
            userId = userId,
            modelName = modelName,
            description = description,
            imageBytes = imageBytes,
            createdAt = System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                firebaseManager.saveCustomizationData(customizationData)
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Данные сохранены", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка сохранения: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadCustomizationData() {
        val userId = auth.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = firebaseManager.getCustomizationData(userId)
                launch(Dispatchers.Main) {
                    data?.let { updateUI(it) }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка загрузки данных: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(data: CustomizationData) {
        binding.modelNameInput.setText(data.modelName)
        binding.descriptionInput.setText(data.description)
        // Здесь можно добавить загрузку изображения из байтов
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 