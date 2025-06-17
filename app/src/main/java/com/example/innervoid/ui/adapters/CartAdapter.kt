package com.example.innervoid.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.innervoid.R
import com.example.innervoid.data.model.CartItem
import com.example.innervoid.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val onRemoveClick: (CartItem) -> Unit,
    private val onQuantityChange: (CartItem, Int) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val numberFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

        fun bind(cartItem: CartItem) {
            binding.apply {
                productName.text = cartItem.name
                productPrice.text = numberFormat.format(cartItem.price)
                productSize.text = cartItem.size
                quantityText.text = cartItem.quantity.toString()

                Glide.with(productImage)
                    .load(cartItem.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(productImage)

                removeButton.setOnClickListener {
                    onRemoveClick(cartItem)
                }

                decreaseButton.setOnClickListener {
                    if (cartItem.quantity > 1) {
                        onQuantityChange(cartItem, cartItem.quantity - 1)
                    }
                }

                increaseButton.setOnClickListener {
                    onQuantityChange(cartItem, cartItem.quantity + 1)
                }
            }
        }
    }

    private class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
} 