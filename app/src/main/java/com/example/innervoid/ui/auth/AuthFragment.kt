package com.example.innervoid.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.innervoid.R
import com.example.innervoid.databinding.FragmentAuthBinding
import com.example.innervoid.ui.admin.AdminActivity
import com.example.innervoid.utils.ToastUtils

class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAuthModeSwitch()
        setupSubmitButton()
        observeAuthState()
    }

    private fun setupAuthModeSwitch() {
        // Устанавливаем начальное состояние
        updateUIForAuthMode(binding.authModeSwitch.isChecked)

        binding.authModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateUIForAuthMode(isChecked)
        }
    }

    private fun updateUIForAuthMode(isRegistrationMode: Boolean) {
        binding.apply {
            nameLayout.visibility = if (isRegistrationMode) View.VISIBLE else View.GONE
            submitButton.text = if (isRegistrationMode) "Зарегистрироваться" else "Войти"
            authModeSwitch.text = if (isRegistrationMode) "Регистрация" else "Вход"
        }
    }

    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                ToastUtils.showToast(requireContext(), "Пожалуйста, заполните все поля")
                return@setOnClickListener
            }

            if (binding.authModeSwitch.isChecked) {
                // Режим регистрации
                val name = binding.nameEditText.text.toString().trim()
                if (name.isEmpty()) {
                    ToastUtils.showToast(requireContext(), "Пожалуйста, введите никнейм")
                    return@setOnClickListener
                }
                authViewModel.signUp(email, password, name)
            } else {
                // Режим входа
                authViewModel.signIn(email, password)
            }
        }
    }

    private fun observeAuthState() {
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Log.d("AuthFragment", "User logged in: ${user.email}, isAdmin: ${user.isAdmin}")
                // Перенаправляем на соответствующую активность в зависимости от роли
                if (user.isAdmin) {
                    Log.d("AuthFragment", "Navigating to AdminActivity")
                    val intent = Intent(requireContext(), AdminActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Log.d("AuthFragment", "Navigating to regular home")
                    findNavController().navigate(R.id.navigation_home)
                }
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.submitButton.isEnabled = !isLoading
            binding.authModeSwitch.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 