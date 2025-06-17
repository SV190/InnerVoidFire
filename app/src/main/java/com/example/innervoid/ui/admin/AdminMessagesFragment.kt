package com.example.innervoid.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innervoid.data.FirebaseManager
import com.example.innervoid.data.models.Message
import com.example.innervoid.data.models.User
import com.example.innervoid.databinding.FragmentAdminMessagesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.example.innervoid.ui.admin.AdminMessagesAdapter
import com.example.innervoid.ui.messages.MessagesAdapter
import com.example.innervoid.ui.admin.UsersAdapter

class AdminMessagesFragment : Fragment() {
    private var _binding: FragmentAdminMessagesBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseManager()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var messagesAdapter: AdminMessagesAdapter
    private lateinit var usersAdapter: UsersAdapter
    private var selectedUserId: String? = null
    private var usersWithMessages = mutableListOf<User>()

    companion object {
        private const val TAG = "AdminMessagesFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentAdminMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        setupRecyclerViews()
        setupMessageInput()
        loadUsersWithMessages()
    }

    private fun setupRecyclerViews() {
        Log.d(TAG, "Setting up RecyclerViews")
        
        // Настройка адаптера для сообщений
        messagesAdapter = AdminMessagesAdapter()
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
        }
        Log.d(TAG, "Messages RecyclerView setup completed")

        // Настройка адаптера для пользователей
        usersAdapter = UsersAdapter { user ->
            Log.d(TAG, "User selected: ${user.name} (${user.id})")
            selectedUserId = user.id
            binding.selectedUserText.text = "Сообщения с ${user.name}"
            loadMessages()
        }
        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = usersAdapter
        }
        Log.d(TAG, "Users RecyclerView setup completed")
    }

    private fun setupMessageInput() {
        Log.d(TAG, "Setting up message input")
        binding.messageInputLayout.setEndIconOnClickListener {
            Log.d(TAG, "Send button clicked")
            sendMessage()
        }
    }

    private fun loadUsersWithMessages() {
        Log.d(TAG, "Starting to load users with messages")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Fetching dialogues from Firestore")
                // Получаем все диалоги, где есть пользователи (не админы)
                val dialoguesSnapshot = db.collection("dialogues")
                    .get()
                    .await()

                Log.d(TAG, "Found ${dialoguesSnapshot.size()} dialogues")
                val userIds = mutableSetOf<String>()
                
                // Извлекаем ID пользователей из всех диалогов
                for (dialogueDoc in dialoguesSnapshot.documents) {
                    val users = dialogueDoc.get("users") as? List<String> ?: emptyList()
                    // Добавляем всех пользователей кроме "admin"
                    users.filter { it != "admin" }.forEach { userId ->
                        userIds.add(userId)
                    }
                }

                Log.d(TAG, "Unique user IDs from dialogues: $userIds")

                // Если нет диалогов с пользователями, загружаем всех пользователей
                if (userIds.isEmpty()) {
                    Log.d(TAG, "No dialogues found, loading all non-admin users")
                    val allUsersSnapshot = db.collection("users")
                        .whereNotEqualTo("isAdmin", true)
                        .get()
                        .await()
                    
                    Log.d(TAG, "Found ${allUsersSnapshot.size()} non-admin users")
                    val users = allUsersSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)
                    }
                    
                    usersWithMessages.clear()
                    usersWithMessages.addAll(users)
                    
                    launch(Dispatchers.Main) {
                        Log.d(TAG, "Submitting ${users.size} users to adapter")
                        usersAdapter.submitList(usersWithMessages.toList())
                        if (users.isEmpty()) {
                            Log.w(TAG, "No users to display")
                            Toast.makeText(context, "Нет пользователей для отображения", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d(TAG, "Successfully loaded ${users.size} users")
                            Toast.makeText(context, "Загружено ${users.size} пользователей", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@launch
                }

                Log.d(TAG, "Loading user details for ${userIds.size} users")
                val users = mutableListOf<User>()
                for (userId in userIds) {
                    try {
                        Log.d(TAG, "Loading user details for ID: $userId")
                        val userDoc = db.collection("users").document(userId).get().await()
                        if (userDoc.exists()) {
                            val user = userDoc.toObject(User::class.java)
                            if (user != null) {
                                users.add(user)
                                Log.d(TAG, "Successfully loaded user: ${user.name}")
                            } else {
                                Log.w(TAG, "Failed to parse user data for ID: $userId")
                            }
                        } else {
                            Log.w(TAG, "User document does not exist for ID: $userId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading user $userId", e)
                        // Пропускаем пользователей с ошибками
                        continue
                    }
                }

                usersWithMessages.clear()
                usersWithMessages.addAll(users)

                launch(Dispatchers.Main) {
                    Log.d(TAG, "Submitting ${users.size} users with messages to adapter")
                    usersAdapter.submitList(usersWithMessages.toList())
                    if (users.isEmpty()) {
                        Log.w(TAG, "No users with messages found")
                        Toast.makeText(context, "Нет пользователей с сообщениями", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d(TAG, "Successfully loaded ${users.size} users with messages")
                        Toast.makeText(context, "Загружено ${users.size} пользователей с сообщениями", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading users with messages", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка загрузки пользователей: ${e.message}", Toast.LENGTH_LONG).show()
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

        val userId = selectedUserId
        if (userId == null) {
            Log.w(TAG, "Attempted to send message without selecting user")
            Toast.makeText(context, "Ошибка: пользователь не выбран", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Sending message to user $userId: $messageText")
        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = "admin",
            receiverId = userId,
            content = messageText,
            fromAdmin = true,
            createdAt = System.currentTimeMillis(),
            read = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Adding message to Firebase")
                firebaseManager.addMessage(userId, message)
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
        val userId = selectedUserId ?: return
        Log.d(TAG, "Loading messages for user: $userId")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Fetching messages from FirebaseManager using dialogues")
                val messages = firebaseManager.getConversationMessages(userId)
                Log.d(TAG, "Retrieved ${messages.size} messages for user $userId")
                
                launch(Dispatchers.Main) {
                    Log.d(TAG, "Submitting ${messages.size} messages to adapter")
                    messagesAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        Log.d(TAG, "Scrolling to last message")
                        binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading messages for user $userId", e)
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка загрузки сообщений: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }
} 