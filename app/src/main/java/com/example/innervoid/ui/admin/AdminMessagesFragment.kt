package com.example.innervoid.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innervoid.databinding.FragmentAdminMessagesBinding
import com.example.innervoid.ui.messages.Message
import com.example.innervoid.ui.messages.MessagesAdapter

class AdminMessagesFragment : Fragment() {
    private var _binding: FragmentAdminMessagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var messagesAdapter: MessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadMessages()
        setupSendButton()
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter()
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messagesAdapter
        }
    }

    private fun loadMessages() {
        // Временные данные для демонстрации
        val messages = listOf(
            Message(
                id = "1",
                text = "Здравствуйте! У меня вопрос по заказу",
                isFromAdmin = false
            ),
            Message(
                id = "2",
                text = "Добрый день! Чем могу помочь?",
                isFromAdmin = true
            )
        )
        messagesAdapter.submitList(messages)
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // TODO: Отправить сообщение
                binding.messageInput.text.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 