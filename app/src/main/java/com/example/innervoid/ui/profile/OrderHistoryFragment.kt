package com.example.innervoid.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innervoid.data.models.Order
import com.example.innervoid.databinding.FragmentOrderHistoryBinding
import com.example.innervoid.utils.Result
import com.example.innervoid.utils.ToastUtils
import java.text.NumberFormat
import java.util.Locale

class OrderHistoryFragment : Fragment() {
    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderHistoryViewModel by viewModels()
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    private lateinit var adapter: OrderHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeOrders()
        viewModel.loadOrders()
    }

    private fun setupRecyclerView() {
        adapter = OrderHistoryAdapter()
        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@OrderHistoryFragment.adapter
        }
    }

    private fun observeOrders() {
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            adapter.submitList(orders)
            binding.emptyOrdersText.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
            binding.ordersRecyclerView.visibility = if (orders.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                ToastUtils.showToast(requireContext(), it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 