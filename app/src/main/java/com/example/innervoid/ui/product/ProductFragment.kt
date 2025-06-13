package com.example.innervoid.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.innervoid.R
import com.example.innervoid.databinding.FragmentProductBinding
import com.example.innervoid.model.Product

class ProductFragment : Fragment() {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private val args: ProductFragmentArgs by navArgs()
    private var selectedSize: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Если productId равен значению по умолчанию, возвращаемся назад
        if (args.productId == "default") {
            requireActivity().onBackPressed()
            return
        }

        // В реальном приложении здесь будет получение данных о продукте из базы данных
        val product = Product(
            id = args.productId,
            name = "Футболка с принтом",
            price = 1999.0,
            imageUrl = "https://example.com/tshirt.jpg",
            sizes = listOf("S", "M", "L", "XL"),
            description = "Классическая футболка из 100% хлопка с уникальным принтом"
        )

        setupUI(product)
    }

    private fun setupUI(product: Product) {
        binding.productName.text = product.name
        binding.productPrice.text = getString(R.string.price_format, product.price)
        binding.productDescription.text = product.description

        // Загрузка изображения
        Glide.with(this)
            .load(product.imageUrl)
            .centerCrop()
            .into(binding.productImage)

        // Настройка выбора размера
        binding.sizeGroup.removeAllViews()
        product.sizes.forEach { size ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = size
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSize = size
                        binding.sizeGroup.clearCheck()
                        this.isChecked = true
                    }
                }
            }
            binding.sizeGroup.addView(chip)
        }

        binding.addToCartButton.setOnClickListener {
            if (selectedSize == null) {
                Toast.makeText(context, "Выберите размер", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // TODO: Добавить в корзину
            Toast.makeText(context, "Добавлено в корзину", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 