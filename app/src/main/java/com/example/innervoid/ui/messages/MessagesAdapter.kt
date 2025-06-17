package com.example.innervoid.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.innervoid.data.models.Message
import com.example.innervoid.databinding.ItemMessageBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class MessagesAdapter : ListAdapter<Message, MessagesAdapter.MessageViewHolder>(MessageDiffCallback()) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: Message) {
            binding.messageText.text = message.content
            binding.messageTime.text = dateFormat.format(message.createdAt)
            
            // Определяем, является ли сообщение от текущего пользователя
            val isCurrentUserMessage = message.senderId == currentUserId
            
            if (isCurrentUserMessage) {
                // Сообщение от текущего пользователя - справа
                binding.messageText.setBackgroundResource(com.example.innervoid.R.drawable.message_background_user)
                binding.messageText.setTextColor(binding.root.context.getColor(com.example.innervoid.R.color.message_user_text))
                binding.messageTime.setTextColor(binding.root.context.getColor(com.example.innervoid.R.color.message_user_text))
                
                // Выравнивание справа
                binding.messageText.layoutParams = (binding.messageText.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                    startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                    endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                }
                binding.messageTime.layoutParams = (binding.messageTime.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                    startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                    endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                }
            } else {
                // Сообщение от другого пользователя - слева
                binding.messageText.setBackgroundResource(com.example.innervoid.R.drawable.message_background_admin)
                binding.messageText.setTextColor(binding.root.context.getColor(com.example.innervoid.R.color.message_admin_text))
                binding.messageTime.setTextColor(binding.root.context.getColor(com.example.innervoid.R.color.message_admin_text))
                
                // Выравнивание слева
                binding.messageText.layoutParams = (binding.messageText.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                    startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                }
                binding.messageTime.layoutParams = (binding.messageTime.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                    startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
                }
            }
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
} 