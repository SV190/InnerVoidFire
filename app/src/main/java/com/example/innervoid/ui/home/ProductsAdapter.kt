package com.example.innervoid.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.innervoid.R
import com.example.innervoid.data.models.Product

class ProductsAdapter(
    private val onProductClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {
    private val TAG = "ProductsAdapter"
    private var products: List<Product> = emptyList()

    fun updateProducts(newProducts: List<Product>) {
        Log.d(TAG, "Updating products: ${newProducts.size} items")
        products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        Log.d(TAG, "Creating new ViewHolder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        Log.d(TAG, "Binding product at position $position: ${product.name}")
        holder.bind(product)
        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "Current item count: ${products.size}")
        return products.size
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.productImage)
        private val nameTextView: TextView = itemView.findViewById(R.id.productName)
        private val priceTextView: TextView = itemView.findViewById(R.id.productPrice)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.productDescription)

        fun bind(product: Product) {
            Log.d(TAG, "Binding product: ${product.name}, imageUrl: ${product.imageUrl}")
            nameTextView.text = product.name
            priceTextView.text = "₽${product.price}"
            descriptionTextView.text = product.description

            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<android.graphics.drawable.Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, "Failed to load image for product ${product.name}: ${e?.message}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        model: Any,
                        target: Target<android.graphics.drawable.Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "Successfully loaded image for product ${product.name}")
                        return false
                    }
                })
                .into(imageView)
        }
    }
} 