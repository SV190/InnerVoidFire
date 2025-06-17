package com.example.innervoid.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innervoid.data.FirebaseManager
import com.example.innervoid.data.models.Message
import com.example.innervoid.data.models.User
import com.example.innervoid.databinding.FragmentAdminChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AdminChatFragment : Fragment() {
    private var _binding: FragmentAdminChatBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var messagesAdapter: AdminMessagesAdapter
    private val args: AdminChatFragmentArgs by navArgs()
    private var currentUser: User? = null

    companion object {
        private const val TAG = "AdminChatFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called for user ID: ${args.userId}")
        _binding = FragmentAdminChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        setupRecyclerView()
        setupMessageInput()
        setupBackButton()
        loadUserInfo()
        loadMessages()
        markMessagesAsRead()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        
        messagesAdapter = AdminMessagesAdapter()
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
        }
        Log.d(TAG, "Messages RecyclerView setup completed")
    }

    private fun setupMessageInput() {
        Log.d(TAG, "Setting up message input")
        binding.messageInputLayout.setEndIconOnClickListener {
            Log.d(TAG, "Send button clicked")
            sendMessage()
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadUserInfo() {
        Log.d(TAG, "Loading user info for ID: ${args.userId}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDoc = db.collection("users").document(args.userId).get().await()
                if (userDoc.exists()) {
                    val user = userDoc.toObject(User::class.java)
                    if (user != null) {
                        currentUser = user
                        launch(Dispatchers.Main) {
                            binding.userNameText.text = user.getFormattedName()
                            binding.userEmailText.text = user.email.ifEmpty { "Email не указан" }
                            
                            // Устанавливаем дефолтную аватарку
                            binding.userAvatar.setImageResource(com.example.innervoid.R.drawable.ic_default_avatar)
                        }
                        Log.d(TAG, "Successfully loaded user info: ${user.getFormattedName()} (${user.email})")
                    } else {
                        Log.w(TAG, "Failed to parse user data")
                        launch(Dispatchers.Main) {
                            binding.userNameText.text = "Пользователь ${args.userId}"
                            binding.userEmailText.text = "Email не указан"
                            binding.userAvatar.setImageResource(com.example.innervoid.R.drawable.ic_default_avatar)
                            Toast.makeText(context, "Ошибка загрузки информации о пользователе", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.w(TAG, "User document does not exist")
                    launch(Dispatchers.Main) {
                        binding.userNameText.text = "Пользователь ${args.userId}"
                        binding.userEmailText.text = "Email не указан"
                        binding.userAvatar.setImageResource(com.example.innervoid.R.drawable.ic_default_avatar)
                        Toast.makeText(context, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user info", e)
                launch(Dispatchers.Main) {
                    binding.userNameText.text = "Пользователь ${args.userId}"
                    binding.userEmailText.text = "Email не указан"
                    binding.userAvatar.setImageResource(com.example.innervoid.R.drawable.ic_default_avatar)
                    Toast.makeText(context, "Ошибка загрузки информации о пользователе: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isEmpty()) {
            Log.d(TAG, "Attempted to send empty message")
            return
        }

        Log.d(TAG, "Sending message to user ${args.userId}: $messageText")
        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = "admin",
            receiverId = args.userId,
            content = messageText,
            fromAdmin = true,
            createdAt = System.currentTimeMillis(),
            read = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Adding message to Firebase")
                firebaseManager.addMessage(args.userId, message)
                Log.d(TAG, "Message successfully added to Firebase")
                launch(Dispatchers.Main) {
                    binding.messageInput.text?.clear()
                    loadMessages()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка отправки сообщения: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadMessages() {
        Log.d(TAG, "Loading messages for user: ${args.userId}")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Fetching messages from FirebaseManager using dialogues")
                val messages = firebaseManager.getConversationMessages(args.userId)
                Log.d(TAG, "Retrieved ${messages.size} messages for user ${args.userId}")
                
                launch(Dispatchers.Main) {
                    Log.d(TAG, "Submitting ${messages.size} messages to adapter")
                    messagesAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        Log.d(TAG, "Scrolling to last message")
                        binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading messages for user ${args.userId}", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка загрузки сообщений: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun markMessagesAsRead() {
        Log.d(TAG, "Marking messages as read for user: ${args.userId}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Получаем все непрочитанные сообщения от пользователя
                val messages = firebaseManager.getConversationMessages(args.userId)
                val unreadMessages = messages.filter { !it.read && !it.fromAdmin }
                
                Log.d(TAG, "Found ${unreadMessages.size} unread messages from user")
                
                // Отмечаем все непрочитанные сообщения от пользователя как прочитанные
                for (message in unreadMessages) {
                    try {
                        firebaseManager.markMessageAsReadByUserId(args.userId, message.id)
                        Log.d(TAG, "Marked message ${message.id} as read")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error marking message ${message.id} as read", e)
                    }
                }
                
                Log.d(TAG, "Successfully marked ${unreadMessages.size} messages as read")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking messages as read", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }
} 