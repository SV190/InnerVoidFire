package com.example.innervoid.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.innervoid.R
import com.example.innervoid.databinding.FragmentProfileBinding
import com.example.innervoid.ui.auth.AuthActivity
import com.example.innervoid.utils.Result
import com.example.innervoid.utils.ToastUtils
import com.example.innervoid.data.models.User

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Сразу показываем выбранное изображение
                Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(binding.userPhotoImageView)

                // Обновляем профиль с новым URI фотографии
                viewModel.updateProfile(
                    name = binding.nameEditText.text.toString(),
                    email = binding.emailEditText.text.toString(),
                    address = binding.addressEditText.text.toString(),
                    photoUrl = uri.toString()
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLogoutButton()
        setupSaveButton()
        setupChangePhotoButton()
        setupOrderHistoryButton()
        setupEmailVerificationButton()
        observeUserData()
        observeEmailVerificationStatus()
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            // Завершаем текущую активность и запускаем AuthActivity
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val address = binding.addressEditText.text.toString()

            if (name.isBlank() || email.isBlank()) {
                ToastUtils.showToast(requireContext(), "Имя и email не могут быть пустыми")
                return@setOnClickListener
            }

            // Проверяем формат email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ToastUtils.showToast(requireContext(), "Введите корректный email")
                return@setOnClickListener
            }

            viewModel.updateProfile(name, email, address)
        }
    }

    private fun setupEmailVerificationButton() {
        binding.emailVerificationButton.setOnClickListener {
            viewModel.sendEmailVerification()
        }
    }

    private fun setupChangePhotoButton() {
        binding.changePhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }
    }

    private fun setupOrderHistoryButton() {
        binding.orderHistoryButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_order_history)
        }
    }

    private fun observeUserData() {
        viewModel.currentUser.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success<User> -> {
                    val user = result.data
                    binding.nameEditText.setText(user.name)
                    binding.emailEditText.setText(user.email)
                    binding.addressEditText.setText(user.deliveryAddress)
                    
                    // Загрузка фотографии пользователя
                    if (user.photoUrl.isNotEmpty()) {
                        try {
                            val photoUri = Uri.parse(user.photoUrl)
                            Glide.with(requireContext())
                                .load(photoUri)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .into(binding.userPhotoImageView)
                        } catch (e: Exception) {
                            // Если не удалось загрузить фото, показываем заглушку
                            binding.userPhotoImageView.setImageResource(R.drawable.ic_default_avatar)
                        }
                    } else {
                        binding.userPhotoImageView.setImageResource(R.drawable.ic_default_avatar)
                    }
                    
                    binding.progressBar.visibility = View.GONE
                }
                is Result.Error -> {
                    ToastUtils.showToast(requireContext(), result.message)
                    binding.progressBar.visibility = View.GONE
                }
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                ToastUtils.showToast(requireContext(), it)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.saveButton.isEnabled = !isLoading
            binding.changePhotoButton.isEnabled = !isLoading
        }
    }

    private fun observeEmailVerificationStatus() {
        viewModel.isEmailVerified.observe(viewLifecycleOwner) { isVerified ->
            if (isVerified) {
                binding.emailVerificationStatus.text = "Email подтвержден"
                binding.emailVerificationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                binding.emailVerificationButton.visibility = View.GONE
            } else {
                binding.emailVerificationStatus.text = "Email не подтвержден"
                binding.emailVerificationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                binding.emailVerificationButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 