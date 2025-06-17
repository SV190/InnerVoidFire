package com.example.innervoid.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innervoid.R
import com.example.innervoid.data.model.CartItem
import com.example.innervoid.databinding.FragmentCartBinding
import com.example.innervoid.ui.adapters.CartAdapter
import com.example.innervoid.viewmodel.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

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

        val userId = auth.currentUser?.uid
        if (userId == null) {
            binding.emptyCartText.text = "Пожалуйста, войдите в аккаунт"
            binding.cartRecyclerView.visibility = View.GONE
            binding.totalPriceLayout.visibility = View.GONE
            binding.buyButton.visibility = View.GONE
            return
        }

        setupRecyclerView()
        setupObservers()
        setupBuyButton()
        viewModel.loadCartItems()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onRemoveClick = { item ->
                viewModel.removeFromCart(item.id)
            },
            onQuantityChange = { item, newQuantity ->
                viewModel.updateQuantity(item, newQuantity)
            }
        )

        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun setupObservers() {
        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items)
            binding.emptyCartText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.cartRecyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            binding.totalPriceLayout.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            binding.buyButton.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.totalPrice.observe(viewLifecycleOwner) { total ->
            binding.totalPriceText.text = numberFormat.format(total)
        }
    }

    private fun setupBuyButton() {
        binding.buyButton.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            
            // Получаем адрес доставки пользователя
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val deliveryAddress = document.getString("deliveryAddress")
                    if (deliveryAddress.isNullOrEmpty()) {
                        Toast.makeText(context, "Пожалуйста, укажите адрес доставки в профиле", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.navigation_profile)
                    } else {
                        viewModel.createOrder(deliveryAddress)
                        Toast.makeText(context, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Ошибка при получении адреса доставки: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 