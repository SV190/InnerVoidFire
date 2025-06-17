package com.example.innervoid.ui.catalog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.innervoid.databinding.FragmentCatalogBinding
import com.example.innervoid.model.Product
import com.example.innervoid.ui.adapters.ProductsAdapter
import com.google.firebase.firestore.FirebaseFirestore

class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var productsAdapter: ProductsAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CatalogFragment", "onViewCreated called")
        
        setupRecyclerView()
        loadProducts()
    }

    private fun setupRecyclerView() {
        Log.d("CatalogFragment", "Setting up RecyclerView")
        productsAdapter = ProductsAdapter { product ->
            Log.d("CatalogFragment", "Product clicked: ${product.id}")
            val action = CatalogFragmentDirections.actionCatalogToProductDetail(product.id)
            findNavController().navigate(action)
        }
        
        binding.productsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productsAdapter
        }
    }

    private fun loadProducts() {
        Log.d("CatalogFragment", "Loading products")
        binding.progressBar.visibility = View.VISIBLE
        
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("CatalogFragment", "Successfully loaded ${documents.size()} products")
                val products = documents.mapNotNull { doc ->
                    try {
                        val sizes = doc.get("sizes")
                        val sizesList = when (sizes) {
                            is List<*> -> sizes.filterIsInstance<String>()
                            else -> emptyList()
                        }
                        
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            sizes = sizesList
                        )
                    } catch (e: Exception) {
                        Log.e("CatalogFragment", "Error parsing product ${doc.id}", e)
                        null
                    }
                }
                productsAdapter.submitList(products)
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("CatalogFragment", "Error loading products", e)
                Toast.makeText(context, "Ошибка загрузки товаров", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 