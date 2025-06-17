package com.example.innervoid.ui.admin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.innervoid.R
import com.example.innervoid.databinding.FragmentAdminProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class AdminProfileFragment : Fragment() {
    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.profileImage.setImageURI(uri)
                uploadImage(uri)
            }
        }
    }

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
        setupUI()
        loadUserData()
    }

    private fun setupUI() {
        binding.profileImage.setOnClickListener {
            openImagePicker()
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    private fun uploadImage(imageUri: Uri) {
        try {
            binding.progressBar.visibility = View.VISIBLE
            
            // Получаем Bitmap из Uri
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            // Сжимаем изображение
            val compressedBitmap = compressBitmap(bitmap)
            
            // Конвертируем в Base64
            val base64Image = bitmapToBase64(compressedBitmap)
            
            // Сохраняем в Firestore
            updateProfileImage(base64Image)
            
        } catch (e: Exception) {
            Log.e("AdminProfile", "Error processing image", e)
            Toast.makeText(context, "Ошибка обработки изображения: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxSize = 800 // максимальный размер изображения
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        
        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun updateProfileImage(base64Image: String) {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(userId)
            .update("profileImage", base64Image)
            .addOnSuccessListener {
                Toast.makeText(context, "Фото профиля обновлено", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("AdminProfile", "Error updating profile image", e)
                Toast.makeText(context, "Ошибка обновления фото: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: ""
                    val profileImage = document.getString("profileImage")

                    binding.nameText.text = name
                    binding.emailText.text = email

                    profileImage?.let { base64Image ->
                        try {
                            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.profileImage.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            Log.e("AdminProfile", "Error loading profile image", e)
                            binding.profileImage.setImageResource(R.drawable.placeholder_image)
                        }
                    } ?: run {
                        binding.profileImage.setImageResource(R.drawable.placeholder_image)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminProfile", "Error loading user data", e)
                Toast.makeText(context, "Ошибка загрузки данных: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(requireContext(), com.example.innervoid.ui.auth.AuthActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 