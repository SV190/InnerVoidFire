package com.example.innervoid.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innervoid.data.FirebaseManager
import com.example.innervoid.data.models.DialogueItem
import com.example.innervoid.data.models.User
import com.example.innervoid.databinding.FragmentAdminDialoguesBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminDialoguesFragment : Fragment() {
    private var _binding: FragmentAdminDialoguesBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val firebaseManager = FirebaseManager()
    private lateinit var dialoguesAdapter: AdminDialoguesAdapter
    
    // Кэш для хранения загруженных диалогов
    private var cachedDialogues = mutableListOf<DialogueItem>()
    private var isFirstLoad = true

    companion object {
        private const val TAG = "AdminDialoguesFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentAdminDialoguesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        setupRecyclerView()
        loadDialogues()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        
        // Если это не первая загрузка и у нас есть кэшированные данные, 
        // просто обновляем счетчики непрочитанных сообщений
        if (!isFirstLoad && cachedDialogues.isNotEmpty()) {
            updateUnreadCounts()
        } else {
            loadDialogues()
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        
        dialoguesAdapter = AdminDialoguesAdapter { dialogueItem ->
            Log.d(TAG, "Dialogue selected for user: ${dialogueItem.user.getFormattedName()} (${dialogueItem.user.id})")
            // Переходим к переписке с выбранным пользователем
            val action = AdminDialoguesFragmentDirections.actionNavigationAdminDialoguesToAdminChatFragment(dialogueItem.user.id)
            findNavController().navigate(action)
        }
        
        binding.dialoguesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dialoguesAdapter
        }
        Log.d(TAG, "Dialogues RecyclerView setup completed")
    }

    private fun loadDialogues() {
        Log.d(TAG, "Starting to load dialogues")
        binding.progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Fetching dialogues from Firestore")
                // Получаем все диалоги
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

                // Загружаем полную информацию о пользователях и их последних сообщениях
                val dialogueItems = mutableListOf<DialogueItem>()
                for (userId in userIds) {
                    try {
                        Log.d(TAG, "Loading user details and messages for ID: $userId")
                        
                        // Загружаем информацию о пользователе
                        val userDoc = db.collection("users").document(userId).get().await()
                        val user = if (userDoc.exists()) {
                            userDoc.toObject(User::class.java) ?: User(
                                id = userId,
                                name = "Пользователь $userId",
                                displayName = "Пользователь $userId",
                                email = "Email не указан",
                                photoUrl = "",
                                deliveryAddress = "",
                                isAdmin = false
                            )
                        } else {
                            User(
                                id = userId,
                                name = "Пользователь $userId",
                                displayName = "Пользователь $userId",
                                email = "Email не указан",
                                photoUrl = "",
                                deliveryAddress = "",
                                isAdmin = false
                            )
                        }

                        // Загружаем последние сообщения для этого пользователя
                        val messages = try {
                            firebaseManager.getConversationMessages(userId)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error loading messages for user $userId: ${e.message}")
                            emptyList()
                        }

                        // Получаем последнее сообщение и информацию о непрочитанных
                        val lastMessage = messages.lastOrNull()
                        val unreadMessages = messages.filter { !it.read && !it.fromAdmin }
                        
                        val dialogueItem = DialogueItem(
                            user = user,
                            lastMessage = lastMessage?.content ?: "",
                            lastMessageTime = lastMessage?.createdAt ?: 0L,
                            hasUnreadMessages = unreadMessages.isNotEmpty(),
                            unreadCount = unreadMessages.size
                        )
                        
                        dialogueItems.add(dialogueItem)
                        Log.d(TAG, "Successfully loaded dialogue for user: ${user.getFormattedName()} (${user.email})")
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading user $userId", e)
                        // Создаем пользователя с базовой информацией
                        val fallbackUser = User(
                            id = userId,
                            name = "Пользователь $userId",
                            displayName = "Пользователь $userId",
                            email = "Email не указан",
                            photoUrl = "",
                            deliveryAddress = "",
                            isAdmin = false
                        )
                        val fallbackDialogueItem = DialogueItem(
                            user = fallbackUser,
                            lastMessage = "",
                            lastMessageTime = 0L,
                            hasUnreadMessages = false,
                            unreadCount = 0
                        )
                        dialogueItems.add(fallbackDialogueItem)
                    }
                }

                // Сортируем по времени последнего сообщения (новые сверху)
                dialogueItems.sortByDescending { it.lastMessageTime }

                launch(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Log.d(TAG, "Submitting ${dialogueItems.size} dialogue items to adapter")
                    
                    // Сохраняем в кэш
                    cachedDialogues = dialogueItems.toMutableList()
                    isFirstLoad = false
                    
                    dialoguesAdapter.submitList(dialogueItems)
                    if (dialogueItems.isEmpty()) {
                        Log.w(TAG, "No dialogues found")
                        binding.emptyStateText.visibility = View.VISIBLE
                    } else {
                        Log.d(TAG, "Successfully loaded ${dialogueItems.size} dialogues")
                        binding.emptyStateText.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading dialogues", e)
                launch(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyStateText.visibility = View.VISIBLE
                    Toast.makeText(context, "Ошибка загрузки диалогов: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateUnreadCounts() {
        Log.d(TAG, "Updating unread counts for cached dialogues")
        binding.progressBar.visibility = View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedDialogues = mutableListOf<DialogueItem>()
                
                for (dialogueItem in cachedDialogues) {
                    try {
                        // Используем оптимизированные методы для быстрого обновления
                        val unreadCount = firebaseManager.getUnreadMessagesCountFromDialogues(dialogueItem.user.id)
                        val lastMessage = firebaseManager.getLastMessage(dialogueItem.user.id)
                        
                        val updatedDialogueItem = dialogueItem.copy(
                            lastMessage = lastMessage?.content ?: dialogueItem.lastMessage,
                            lastMessageTime = lastMessage?.createdAt ?: dialogueItem.lastMessageTime,
                            hasUnreadMessages = unreadCount > 0,
                            unreadCount = unreadCount
                        )
                        
                        updatedDialogues.add(updatedDialogueItem)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating unread count for user ${dialogueItem.user.id}", e)
                        updatedDialogues.add(dialogueItem)
                    }
                }
                
                // Сортируем по времени последнего сообщения
                updatedDialogues.sortByDescending { it.lastMessageTime }
                
                launch(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    cachedDialogues = updatedDialogues.toMutableList()
                    dialoguesAdapter.submitList(updatedDialogues)
                    Log.d(TAG, "Successfully updated unread counts for ${updatedDialogues.size} dialogues")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating unread counts", e)
                launch(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
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