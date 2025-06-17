package com.example.innervoid.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innervoid.databinding.FragmentMessagesBinding
import com.example.innervoid.data.FirebaseManager
import com.example.innervoid.data.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MessagesFragment : Fragment() {
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var messagesAdapter: MessagesAdapter
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseManager = FirebaseManager()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MessagesFragment", "onCreate called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("MessagesFragment", "onCreateView called")
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MessagesFragment", "onViewCreated called")
        setupRecyclerView()
        setupMessageInput()
        loadMessages()
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter()
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
        }
    }

    private fun setupMessageInput() {
        binding.messageInputLayout.setEndIconOnClickListener {
            sendMessage()
        }
    }

    private fun loadMessages() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("MessagesFragment", "Начало загрузки сообщений для пользователя: $userId")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("MessagesFragment", "Запрос сообщений из Firebase")
                val messages = firebaseManager.getConversationMessages(userId)
                Log.d("MessagesFragment", "Получено ${messages.size} сообщений")
                
                launch(Dispatchers.Main) {
                    messagesAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        Log.d("MessagesFragment", "Прокрутка к последнему сообщению")
                        binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
                    }
                }
            } catch (e: Exception) {
                Log.e("MessagesFragment", "Ошибка загрузки сообщений", e)
                Log.e("MessagesFragment", "Детали ошибки: ${e.message}")
                Log.e("MessagesFragment", "Stack trace: ${e.stackTraceToString()}")
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка загрузки сообщений: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isEmpty()) return

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            return
        }

        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = userId,
            receiverId = "admin",
            content = messageText,
            fromAdmin = false,
            createdAt = System.currentTimeMillis(),
            read = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("MessagesFragment", "Попытка сохранения сообщения в Firebase")
                firebaseManager.addMessage(userId, message)
                Log.d("MessagesFragment", "Сообщение успешно сохранено в Firebase")
                
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Сообщение отправлено", Toast.LENGTH_SHORT).show()
                    Log.d("MessagesFragment", "Обновление списка сообщений после отправки")
                    loadMessages()
                }
            } catch (e: Exception) {
                Log.e("MessagesFragment", "Ошибка отправки сообщения", e)
                Log.e("MessagesFragment", "Детали ошибки: ${e.message}")
                Log.e("MessagesFragment", "Stack trace: ${e.stackTraceToString()}")
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка отправки сообщения: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("MessagesFragment", "onDestroyView called")
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MessagesFragment", "onDestroy called")
    }
} 