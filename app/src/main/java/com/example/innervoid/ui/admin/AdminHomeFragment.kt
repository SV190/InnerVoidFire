package com.example.innervoid.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innervoid.databinding.FragmentAdminHomeBinding

class AdminHomeFragment : Fragment() {
    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAddProductButton()
        loadProducts()
    }

    private fun setupRecyclerView() {
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(context)
        // TODO: Добавить адаптер для товаров
    }

    private fun setupAddProductButton() {
        binding.addProductButton.setOnClickListener {
            // TODO: Показать диалог добавления товара
        }
    }

    private fun loadProducts() {
        // TODO: Загрузить список товаров
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 