package com.example.innervoid.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.innervoid.data.models.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
import java.util.*
import com.google.firebase.auth.FirebaseAuth

class FirebaseManager {
    private val db: FirebaseFirestore = Firebase.firestore

    // Коллекции
    private val productsCollection = db.collection("products")
    private val cartItemsCollection = db.collection("cart_items")
    private val messagesCollection = db.collection("messages")
    private val ordersCollection = db.collection("orders")
    private val usersCollection = db.collection("users")
    private val dialoguesCollection = db.collection("dialogues")

    // Products
    suspend fun addProduct(product: Product) = productsCollection.document(product.id).set(product).await()
    
    suspend fun getProduct(id: String): DocumentSnapshot {
        val result = productsCollection.document(id).get().await()
        Log.d("FirebaseManager", "Getting product with ID: $id, exists: ${result.exists()}")
        return result
    }
    
    suspend fun updateProduct(product: Product) = productsCollection.document(product.id).set(product).await()
    suspend fun deleteProduct(id: String) = productsCollection.document(id).delete().await()
    
    suspend fun getAllProducts(): QuerySnapshot {
        val result = productsCollection.get().await()
        Log.d("FirebaseManager", "Getting all products, count: ${result.documents.size}")
        return result
    }

    // Cart Items
    suspend fun addCartItem(cartItem: CartItem) = cartItemsCollection.document(cartItem.id).set(cartItem).await()
    suspend fun getCartItem(id: String) = cartItemsCollection.document(id).get().await()
    suspend fun updateCartItem(cartItem: CartItem) = cartItemsCollection.document(cartItem.id).set(cartItem).await()
    suspend fun deleteCartItem(id: String) = cartItemsCollection.document(id).delete().await()
    suspend fun getUserCartItems(userId: String) = cartItemsCollection.whereEqualTo("userId", userId).get().await()

    // Функция для получения всех диалогов
    suspend fun getAllDialogues(): List<Map<String, Any>> {
        val dialogues = dialoguesCollection
            .get()
            .await()
            .documents
            .map { doc ->
                mapOf(
                    "id" to doc.id,
                    "users" to (doc.get("users") as? List<String> ?: emptyList()),
                    "createdAt" to (doc.get("createdAt") as? Long ?: 0L)
                )
            }
        Log.d("FirebaseManager", "Получено ${dialogues.size} диалогов")
        return dialogues
    }

    // Функция для получения ID диалога между пользователем и админом
    private suspend fun getDialogueId(userId: String): String {
        Log.d("FirebaseManager", "Поиск диалога для пользователя: $userId")
        
        // Получаем все диалоги
        val allDialogues = getAllDialogues()
        
        // Ищем диалог, где пользователь является участником
        val existingDialogue = allDialogues.find { dialogue ->
            val users = dialogue["users"] as? List<String> ?: emptyList()
            users.contains(userId)
        }

        if (existingDialogue == null) {
            // Создаем новый диалог
            val newDialogueId = dialoguesCollection.document().id
            val dialogue = hashMapOf(
                "users" to listOf(userId, "admin"),
                "createdAt" to System.currentTimeMillis()
            )
            Log.d("FirebaseManager", "Создание нового диалога с ID: $newDialogueId")
            dialoguesCollection.document(newDialogueId).set(dialogue).await()
            return newDialogueId
        } else {
            val dialogueId = existingDialogue["id"] as String
            Log.d("FirebaseManager", "Найден существующий диалог с ID: $dialogueId")
            return dialogueId
        }
    }

    // Обновленная функция добавления сообщения
    suspend fun addMessage(userId: String, message: Message) {
        Log.d("FirebaseManager", "Начало добавления сообщения. UserId: $userId, MessageId: ${message.id}")
        try {
            val dialogueId = getDialogueId(userId)
            Log.d("FirebaseManager", "Получен ID диалога: $dialogueId")
            
            // Сохраняем сообщение в подколлекции messages диалога
            dialoguesCollection.document(dialogueId)
                .collection("messages")
                .document(message.id)
                .set(message)
                .await()
            
            Log.d("FirebaseManager", "Сообщение успешно сохранено")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Ошибка при добавлении сообщения", e)
            Log.e("FirebaseManager", "Детали ошибки: ${e.message}")
            Log.e("FirebaseManager", "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    // Обновленная функция получения сообщений
    suspend fun getConversationMessages(userId: String): List<Message> {
        Log.d("FirebaseManager", "Запрос сообщений для пользователя: $userId")
        try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId == null) {
                Log.e("FirebaseManager", "Текущий пользователь не авторизован")
                throw Exception("Пользователь не авторизован")
            }

            Log.d("FirebaseManager", "Текущий пользователь: $currentUserId")

            // Получаем все диалоги
            val dialogues = dialoguesCollection
                .whereArrayContains("users", userId)
                .get()
                .await()
                .documents

            Log.d("FirebaseManager", "Найдено ${dialogues.size} диалогов для пользователя $userId")
            dialogues.forEach { doc ->
                Log.d("FirebaseManager", "Диалог ID: ${doc.id}, данные: ${doc.data}")
            }

            if (dialogues.isEmpty()) {
                Log.d("FirebaseManager", "Диалоги не найдены, возвращаем пустой список")
                return emptyList()
            }

            // Получаем сообщения из первого найденного диалога
            val dialogueId = dialogues[0].id
            Log.d("FirebaseManager", "Используем диалог с ID: $dialogueId")

            val messages = dialoguesCollection.document(dialogueId)
                .collection("messages")
                .orderBy("createdAt")
                .get()
                .await()
                .toObjects(Message::class.java)

            Log.d("FirebaseManager", "Получено ${messages.size} сообщений из диалога $dialogueId")
            messages.forEach { message ->
                Log.d("FirebaseManager", "Сообщение: id=${message.id}, senderId=${message.senderId}, receiverId=${message.receiverId}, content=${message.content}, createdAt=${message.createdAt}")
            }

            return messages
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Ошибка при получении сообщений", e)
            Log.e("FirebaseManager", "Детали ошибки: ${e.message}")
            Log.e("FirebaseManager", "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    suspend fun getMessage(messageId: String) = 
        messagesCollection.document(messageId).get().await()

    suspend fun getUserMessages(userId: String) = 
        messagesCollection
            .whereEqualTo("receiverId", userId)
            .orderBy("createdAt")
            .get()
            .await()
        
    suspend fun getAdminMessages() = 
        messagesCollection
            .whereEqualTo("receiverId", FirebaseAuth.getInstance().currentUser?.uid)
            .get()
            .await()
        
    suspend fun markMessageAsRead(dialogueId: String, messageId: String) {
        Log.d("FirebaseManager", "Отметка сообщения как прочитанного. DialogueId: $dialogueId, MessageId: $messageId")
        try {
            dialoguesCollection.document(dialogueId)
                .collection("messages")
                .document(messageId)
                .update("read", true)
                .await()
            Log.d("FirebaseManager", "Сообщение успешно отмечено как прочитанное")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Ошибка при отметке сообщения как прочитанного", e)
            Log.e("FirebaseManager", "Детали ошибки: ${e.message}")
            Log.e("FirebaseManager", "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    suspend fun markMessageAsReadByUserId(userId: String, messageId: String) {
        Log.d("FirebaseManager", "Отметка сообщения как прочитанного. UserId: $userId, MessageId: $messageId")
        try {
            val dialogueId = getDialogueId(userId)
            Log.d("FirebaseManager", "Получен ID диалога: $dialogueId")
            
            dialoguesCollection.document(dialogueId)
                .collection("messages")
                .document(messageId)
                .update("read", true)
                .await()
            Log.d("FirebaseManager", "Сообщение успешно отмечено как прочитанное")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Ошибка при отметке сообщения как прочитанного", e)
            Log.e("FirebaseManager", "Детали ошибки: ${e.message}")
            Log.e("FirebaseManager", "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }
        
    suspend fun getUnreadMessagesCount(userId: String): Int {
        Log.d("FirebaseManager", "Подсчет непрочитанных сообщений для пользователя: $userId")
        try {
            val count = messagesCollection
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("read", false)
                .get()
                .await()
                .size()
            
            Log.d("FirebaseManager", "Найдено $count непрочитанных сообщений")
            return count
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Ошибка при подсчете непрочитанных сообщений", e)
            Log.e("FirebaseManager", "Детали ошибки: ${e.message}")
            Log.e("FirebaseManager", "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    // Оптимизированный метод для быстрого получения непрочитанных сообщений из диалогов
    suspend fun getUnreadMessagesCountFromDialogues(userId: String): Int {
        Log.d("FirebaseManager", "Быстрый подсчет непрочитанных сообщений для пользователя: $userId")
        try {
            val dialogues = dialoguesCollection
                .whereArrayContains("users", userId)
                .get()
                .await()
                .documents

            if (dialogues.isEmpty()) {
                return 0
            }

            val dialogueId = dialogues[0].id
            val unreadMessages = dialoguesCollection.document(dialogueId)
                .collection("messages")
                .whereEqualTo("read", false)
                .whereNotEqualTo("fromAdmin", true)
                .get()
                .await()

            val count = unreadMessages.size()
            Log.d("FirebaseManager", "Быстро найдено $count непрочитанных сообщений")
            return count
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Ошибка при быстром подсчете непрочитанных сообщений", e)
            return 0
        }
    }

    // Оптимизированный метод для получения последнего сообщения
    suspend fun getLastMessage(userId: String): Message? {
        Log.d("FirebaseManager", "Получение последнего сообщения для пользователя: $userId")
        try {
            val dialogues = dialoguesCollection
                .whereArrayContains("users", userId)
                .get()
                .await()
                .documents

            if (dialogues.isEmpty()) {
                return null
            }

            val dialogueId = dialogues[0].id
            val lastMessage = dialoguesCollection.document(dialogueId)
                .collection("messages")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()

            return lastMessage?.toObject(Message::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Ошибка при получении последнего сообщения", e)
            return null
        }
    }

    // Orders
    suspend fun addOrder(orderItem: OrderItem) = ordersCollection.document(orderItem.id).set(orderItem).await()
    suspend fun getOrder(id: String) = ordersCollection.document(id).get().await()
    suspend fun getUserOrders(userId: String) = ordersCollection
        .whereEqualTo("userId", userId)
        .get()
        .await()

    // Users
    suspend fun addUser(user: User) = usersCollection.document(user.id).set(user).await()
    suspend fun getUser(id: String) = usersCollection.document(id).get().await()
    suspend fun updateUser(user: User) = usersCollection.document(user.id).set(user).await()
    suspend fun deleteUser(id: String) = usersCollection.document(id).delete().await()

    suspend fun saveCustomizationData(data: CustomizationData) {
        db.collection("customizations")
            .document(data.id)
            .set(data)
            .await()
    }

    suspend fun getCustomizationData(userId: String): CustomizationData? {
        return try {
            val snapshot = db.collection("customizations")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.toObject(CustomizationData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMessages(userId: String): List<Message> {
        return try {
            val snapshot = db.collection("messages")
                .whereEqualTo("receiverId", userId)
                .get()
                .await()
            
            snapshot.toObjects(Message::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
} 