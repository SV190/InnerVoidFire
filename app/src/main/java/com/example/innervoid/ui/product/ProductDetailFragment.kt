package com.example.innervoid.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.innervoid.data.model.Product
import com.example.innervoid.data.repository.CartRepository
import com.example.innervoid.databinding.FragmentProductDetailBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val cartRepository = CartRepository()
    private val auth = FirebaseAuth.getInstance()
    private var selectedSize: String? = null
    private var currentProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        loadProductDetails()
    }

    private fun setupListeners() {
        binding.sizesChipGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedSize = group.findViewById<Chip>(checkedId)?.text?.toString()
            binding.addToCartButton.isEnabled = selectedSize != null
        }

        binding.addToCartButton.setOnClickListener {
            addToCart()
        }
    }

    private fun addToCart() {
        val product = currentProduct
        val size = selectedSize
        val userId = auth.currentUser?.uid

        if (product == null || size == null) {
            Toast.makeText(context, "Выберите размер", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null) {
            Toast.makeText(context, "Необходимо войти в аккаунт", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                when (cartRepository.addToCart(product, size, userId)) {
                    is com.example.innervoid.utils.Result.Success -> {
                        Toast.makeText(context, "Товар добавлен в корзину", Toast.LENGTH_SHORT).show()
                    }
                    is com.example.innervoid.utils.Result.Error -> {
                        Toast.makeText(context, "Ошибка добавления в корзину", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProductDetails() {
        val productId = arguments?.getString("productId")
        if (productId == null) {
            findNavController().navigateUp()
            return
        }

        db.collection("products")
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                currentProduct = Product(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    description = document.getString("description") ?: "",
                    price = document.getDouble("price") ?: 0.0,
                    imageUrl = document.getString("imageUrl") ?: "",
                    category = document.getString("category") ?: "",
                    inStock = document.getBoolean("inStock") ?: true
                )

                binding.productName.text = currentProduct?.name
                binding.productPrice.text = "${currentProduct?.price} ₽"
                binding.productDescription.text = currentProduct?.description
                binding.productCategory.text = currentProduct?.category
                
                // Загрузка изображения
                currentProduct?.imageUrl?.let { imageUrl ->
                    Glide.with(this)
                        .load(imageUrl)
                        .centerCrop()
                        .into(binding.productImage)
                }
                
                // Загрузка и отображение размеров
                val sizeString = document.getString("size") ?: "S, M, L, XL"
                val sizes = sizeString.split(", ").map { it.trim() }
                
                binding.sizesChipGroup.removeAllViews()
                sizes.forEach { size ->
                    val chip = Chip(requireContext()).apply {
                        text = size
                        isCheckable = true
                        isClickable = true
                    }
                    binding.sizesChipGroup.addView(chip)
                }
            }
            .addOnFailureListener {
                findNavController().navigateUp()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 