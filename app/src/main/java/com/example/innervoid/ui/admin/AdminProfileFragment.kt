package com.example.innervoid.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.innervoid.databinding.FragmentAdminProfileBinding

class AdminProfileFragment : Fragment() {
    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProfileImage()
        setupSaveButton()
        loadProfileData()
    }

    private fun setupProfileImage() {
        binding.changePhotoText.setOnClickListener {
            // TODO: Добавить выбор фото
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            // TODO: Сохранить изменения профиля
        }
    }

    private fun loadProfileData() {
        // Временные данные для демонстрации
        binding.nicknameInput.setText("Admin123")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 