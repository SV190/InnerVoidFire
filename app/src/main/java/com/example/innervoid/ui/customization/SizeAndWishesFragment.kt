package com.example.innervoid.ui.customization

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.innervoid.databinding.FragmentSizeAndWishesBinding
import com.example.innervoid.data.FirebaseManager
import com.example.innervoid.data.models.Message
import com.example.innervoid.model.CustomizationData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SizeAndWishesFragment : Fragment() {
    private var _binding: FragmentSizeAndWishesBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager()
    private val auth = FirebaseAuth.getInstance()
    private val viewModel: CustomizationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSizeAndWishesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SizeAndWishesFragment", "onViewCreated called")
        setupSizeSpinner()
        setupSubmitButton()
        setupBackButton()
    }

    private fun setupSizeSpinner() {
        val sizes = arrayOf("S", "M", "L", "XL", "XXL")
        binding.sizeSpinner.adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sizes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            // Возвращаемся к предыдущему фрагменту
            findNavController().navigateUp()
        }
    }

    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            Log.d("SizeAndWishesFragment", "Submit button clicked")
            val size = binding.sizeSpinner.selectedItem.toString()
            val wishes = binding.wishesEditText.text.toString()
            
            Log.d("SizeAndWishesFragment", "Selected size: $size, wishes: $wishes")
            
            // Получаем данные кастомизации из ViewModel
            val customizationData = viewModel.getCustomizationData()
            
            if (customizationData.printUri.isNotEmpty()) {
                Log.d("SizeAndWishesFragment", "Customization data received: $customizationData")
                sendCustomizationRequest(size, wishes, customizationData)
            } else {
                Log.e("SizeAndWishesFragment", "Customization data is empty")
                Toast.makeText(context, "Ошибка: данные кастомизации не найдены", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendCustomizationRequest(size: String, wishes: String, customizationData: CustomizationData) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("SizeAndWishesFragment", "User is not authenticated")
            Toast.makeText(context, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }
        
        Log.d("SizeAndWishesFragment", "Sending customization request for user: $userId")
        
        val messageText = """
            Новая заявка на кастомизацию:
            Размер: $size
            Пожелания: $wishes
            Принт: ${customizationData.printUri}
            Позиция принта: x=${customizationData.printPosition.x}, y=${customizationData.printPosition.y}
            Масштаб: ${customizationData.printPosition.scale}
            Поворот: ${customizationData.printPosition.rotation}
        """.trimIndent()

        val message = Message(
            id = java.util.UUID.randomUUID().toString(),
            senderId = userId,
            receiverId = "admin",
            content = messageText,
            fromAdmin = false,
            createdAt = System.currentTimeMillis(),
            read = false
        )

        Log.d("SizeAndWishesFragment", "Created message object: $message")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SizeAndWishesFragment", "Attempting to add message to Firebase")
                firebaseManager.addMessage(userId, message)
                Log.d("SizeAndWishesFragment", "Message successfully added to Firebase")
                
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Заявка успешно отправлена", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                Log.e("SizeAndWishesFragment", "Error sending customization request", e)
                Log.e("SizeAndWishesFragment", "Error details: ${e.message}")
                Log.e("SizeAndWishesFragment", "Stack trace: ${e.stackTraceToString()}")
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка отправки заявки: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 