package com.example.innervoid.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innervoid.databinding.FragmentCartBinding

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadCartItems()
        setupBuyButton()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter { item ->
            // TODO: Удалить товар из корзины
        }
        binding.cartItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun loadCartItems() {
        // Временные данные для демонстрации
        val cartItems = listOf(
            CartItem(
                id = "1",
                name = "Футболка базовая",
                price = 1999.0,
                imageUrl = "https://example.com/tshirt.jpg",
                size = "M"
            ),
            CartItem(
                id = "2",
                name = "Худи оверсайз",
                price = 3999.0,
                imageUrl = "https://example.com/hoodie.jpg",
                size = "L"
            )
        )
        cartAdapter.submitList(cartItems)
        updateTotalPrice(cartItems)
    }

    private fun updateTotalPrice(items: List<CartItem>) {
        val total = items.sumOf { it.price }
        binding.totalPrice.text = "$total ₽"
    }

    private fun setupBuyButton() {
        binding.buyButton.setOnClickListener {
            // TODO: Отправить заказ админу
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 