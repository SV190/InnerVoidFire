package com.example.innervoid.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.innervoid.data.models.DialogueItem
import com.example.innervoid.databinding.ItemDialogueBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminDialoguesAdapter(
    private val onDialogueClick: (DialogueItem) -> Unit
) : ListAdapter<DialogueItem, AdminDialoguesAdapter.DialogueViewHolder>(DialogueDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogueViewHolder {
        val binding = ItemDialogueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DialogueViewHolder(binding, onDialogueClick)
    }

    override fun onBindViewHolder(holder: DialogueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DialogueViewHolder(
        private val binding: ItemDialogueBinding,
        private val onDialogueClick: (DialogueItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

        fun bind(dialogueItem: DialogueItem) {
            val user = dialogueItem.user
            
            binding.userName.text = user.getFormattedName()
            binding.userEmail.text = user.email
            
            // Устанавливаем аватар пользователя (если есть)
            if (user.photoUrl != null && user.photoUrl.isNotEmpty()) {
                // Здесь можно загрузить изображение с помощью Glide или Picasso
                // Glide.with(binding.userAvatar).load(user.photoUrl).into(binding.userAvatar)
            } else {
                // Устанавливаем дефолтную аватарку
                binding.userAvatar.setImageResource(com.example.innervoid.R.drawable.ic_default_avatar)
            }
            
            // Отображаем последнее сообщение
            if (dialogueItem.lastMessage.isNotEmpty()) {
                binding.lastMessage.text = dialogueItem.lastMessage
                binding.lastMessage.visibility = android.view.View.VISIBLE
            } else {
                binding.lastMessage.visibility = android.view.View.GONE
            }
            
            // Отображаем время последнего сообщения
            if (dialogueItem.lastMessageTime > 0) {
                val messageDate = Date(dialogueItem.lastMessageTime)
                val currentDate = Date()
                
                // Если сообщение сегодня - показываем время, иначе дату
                val isToday = android.text.format.DateUtils.isToday(dialogueItem.lastMessageTime)
                val timeText = if (isToday) {
                    timeFormat.format(messageDate)
                } else {
                    dateFormat.format(messageDate)
                }
                
                binding.lastMessageTime.text = timeText
                binding.lastMessageTime.visibility = android.view.View.VISIBLE
            } else {
                binding.lastMessageTime.visibility = android.view.View.GONE
            }
            
            // Отображаем индикатор непрочитанных сообщений
            if (dialogueItem.hasUnreadMessages && dialogueItem.unreadCount > 0) {
                binding.unreadCount.text = if (dialogueItem.unreadCount > 99) "99+" else dialogueItem.unreadCount.toString()
                binding.unreadCount.visibility = android.view.View.VISIBLE
                
                // Делаем имя пользователя жирным для непрочитанных диалогов
                binding.userName.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                binding.unreadCount.visibility = android.view.View.GONE
                binding.userName.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            
            binding.root.setOnClickListener {
                onDialogueClick(dialogueItem)
            }
        }
    }

    private class DialogueDiffCallback : DiffUtil.ItemCallback<DialogueItem>() {
        override fun areItemsTheSame(oldItem: DialogueItem, newItem: DialogueItem): Boolean {
            return oldItem.user.id == newItem.user.id
        }

        override fun areContentsTheSame(oldItem: DialogueItem, newItem: DialogueItem): Boolean {
            return oldItem == newItem
        }
    }
} 