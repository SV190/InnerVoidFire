package com.example.innervoid.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.innervoid.R
import com.example.innervoid.databinding.FragmentHomeBinding
import com.example.innervoid.model.Product
import com.example.innervoid.ui.adapters.ProductsAdapter
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var productsAdapter: ProductsAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "onViewCreated called")
        
        setupRecyclerView()
        loadProducts()
    }

    private fun setupRecyclerView() {
        Log.d("HomeFragment", "Setting up RecyclerView")
        productsAdapter = ProductsAdapter { product ->
            Log.d("HomeFragment", "Product clicked: ${product.id}")
            val action = HomeFragmentDirections.actionHomeToProductDetail(product.id)
            findNavController().navigate(action)
        }
        
        binding.productsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productsAdapter
        }
    }

    private fun loadProducts() {
        Log.d("HomeFragment", "Loading products")
        binding.progressBar.visibility = View.VISIBLE
        
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("HomeFragment", "Successfully loaded ${documents.size()} products")
                val products = documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val description = doc.getString("description") ?: ""
                        val price = doc.getDouble("price") ?: 0.0
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        val sizes = doc.get("sizes")
                        val sizesList = when (sizes) {
                            is List<*> -> sizes.filterIsInstance<String>()
                            else -> emptyList()
                        }

                        Product(
                            id = id,
                            name = name,
                            description = description,
                            price = price,
                            imageUrl = imageUrl,
                            sizes = sizesList
                        )
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error parsing product", e)
                        null
                    }
                }
                productsAdapter.submitList(products)
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error loading products", e)
                Toast.makeText(context, "Ошибка загрузки товаров", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 