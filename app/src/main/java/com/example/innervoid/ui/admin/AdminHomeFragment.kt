package com.example.innervoid.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.innervoid.R
import com.example.innervoid.data.model.Product
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class AdminHomeFragment : Fragment() {
    private lateinit var productsAdapter: AdminProductsAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupFab(view)
        loadProducts()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.productsRecyclerView)
        productsAdapter = AdminProductsAdapter(
            onEditClick = { product -> showEditProductDialog(product) },
            onDeleteClick = { product -> showDeleteConfirmationDialog(product) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productsAdapter
        }
    }

    private fun setupFab(view: View) {
        view.findViewById<FloatingActionButton>(R.id.addProductFab).setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun loadProducts() {
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                val products = documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java).copy(id = doc.id)
                }
                productsAdapter.submitList(products)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка загрузки товаров: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddProductDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.nameInput)
        val priceInput = dialogView.findViewById<TextInputEditText>(R.id.priceInput)
        val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.descriptionInput)
        val categoryInput = dialogView.findViewById<TextInputEditText>(R.id.categoryInput)
        val imageUrlInput = dialogView.findViewById<TextInputEditText>(R.id.imageUrlInput)
        val sizesChipGroup = dialogView.findViewById<ChipGroup>(R.id.sizesChipGroup)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Добавить товар")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val name = nameInput.text.toString()
                val price = priceInput.text.toString().toDoubleOrNull() ?: 0.0
                val description = descriptionInput.text.toString()
                val category = categoryInput.text.toString()
                val imageUrl = imageUrlInput.text.toString()
                val selectedSizes = getSelectedSizes(sizesChipGroup)

                if (name.isNotBlank() && category.isNotBlank() && selectedSizes.isNotEmpty()) {
                    addProduct(name, price, description, category, imageUrl, selectedSizes)
                } else {
                    Toast.makeText(context, "Заполните все обязательные поля и выберите хотя бы один размер", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditProductDialog(product: Product) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.nameInput)
        val priceInput = dialogView.findViewById<TextInputEditText>(R.id.priceInput)
        val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.descriptionInput)
        val categoryInput = dialogView.findViewById<TextInputEditText>(R.id.categoryInput)
        val imageUrlInput = dialogView.findViewById<TextInputEditText>(R.id.imageUrlInput)
        val sizesChipGroup = dialogView.findViewById<ChipGroup>(R.id.sizesChipGroup)

        nameInput.setText(product.name)
        priceInput.setText(product.price.toString())
        descriptionInput.setText(product.description)
        categoryInput.setText(product.category)
        imageUrlInput.setText(product.imageUrl)

        // Устанавливаем выбранные размеры
        product.sizes.forEach { size ->
            when (size) {
                "S" -> dialogView.findViewById<Chip>(R.id.sizeSChip).isChecked = true
                "M" -> dialogView.findViewById<Chip>(R.id.sizeMChip).isChecked = true
                "L" -> dialogView.findViewById<Chip>(R.id.sizeLChip).isChecked = true
                "XL" -> dialogView.findViewById<Chip>(R.id.sizeXLChip).isChecked = true
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Редактировать товар")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = nameInput.text.toString()
                val price = priceInput.text.toString().toDoubleOrNull() ?: 0.0
                val description = descriptionInput.text.toString()
                val category = categoryInput.text.toString()
                val imageUrl = imageUrlInput.text.toString()
                val selectedSizes = getSelectedSizes(sizesChipGroup)

                if (name.isNotBlank() && category.isNotBlank() && selectedSizes.isNotEmpty()) {
                    updateProduct(product.id, name, price, description, category, imageUrl, selectedSizes)
                } else {
                    Toast.makeText(context, "Заполните все обязательные поля и выберите хотя бы один размер", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun getSelectedSizes(chipGroup: ChipGroup): List<String> {
        val selectedSizes = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                selectedSizes.add(chip.text.toString())
            }
        }
        return selectedSizes
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить товар")
            .setMessage("Вы уверены, что хотите удалить товар \"${product.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteProduct(product.id)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addProduct(name: String, price: Double, description: String, category: String, imageUrl: String, sizes: List<String>) {
        val productId = UUID.randomUUID().toString()
        val product = Product(
            id = productId,
            name = name,
            price = price,
            description = description,
            category = category,
            imageUrl = imageUrl,
            sizes = sizes
        )

        db.collection("products")
            .document(productId)
            .set(product)
            .addOnSuccessListener {
                Toast.makeText(context, "Товар успешно добавлен", Toast.LENGTH_SHORT).show()
                loadProducts()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка добавления товара: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProduct(id: String, name: String, price: Double, description: String, category: String, imageUrl: String, sizes: List<String>) {
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "price" to price,
            "description" to description,
            "category" to category,
            "imageUrl" to imageUrl,
            "sizes" to sizes
        )

        db.collection("products")
            .document(id)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Товар успешно обновлен", Toast.LENGTH_SHORT).show()
                loadProducts()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка обновления товара: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteProduct(id: String) {
        db.collection("products")
            .document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Товар успешно удален", Toast.LENGTH_SHORT).show()
                loadProducts()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка удаления товара: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 