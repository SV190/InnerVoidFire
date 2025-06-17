package com.example.innervoid.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.innervoid.data.models.Order
import com.example.innervoid.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter : ListAdapter<Order, OrderHistoryAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
        private val numberFormat = java.text.NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

        fun bind(order: Order) {
            binding.apply {
                orderNumberText.text = "Заказ #${order.id.take(8)}"
                orderDateText.text = dateFormat.format(order.createdAt)
                orderStatusText.text = when (order.status) {
                    "pending" -> "В обработке"
                    "processing" -> "Готовится"
                    "shipped" -> "Отправлен"
                    "delivered" -> "Доставлен"
                    else -> order.status
                }
                orderTotalText.text = numberFormat.format(order.totalPrice)
                orderAddressText.text = order.deliveryAddress
                orderItemsCountText.text = "${order.items.size} товаров"
            }
        }
    }

    private class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
} 