package com.example.innervoid.ui.customization

import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.innervoid.databinding.FragmentPositionPrintBinding
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.example.innervoid.model.PrintPosition
import android.view.MotionEvent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class PositionPrintFragment : Fragment() {
    private var _binding: FragmentPositionPrintBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomizationViewModel by activityViewModels()
    private var currentScale = 1f
    private var currentRotation = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var lastX = 0f
    private var lastY = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPositionPrintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем URI принта из ViewModel
        val printUri = viewModel.getPrintUri()
        Log.d("PositionPrintFragment", "Received print URI: $printUri")
        
        printUri?.let { uri ->
            loadPrintImage(uri)
        }

        setupGestures()
        setupNavigation()
        setupBackButton()
    }

    private fun loadPrintImage(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            Log.d("PositionPrintFragment", "Loading image from URI: $uri")

            Glide.with(this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        Log.d("PositionPrintFragment", "Image loaded successfully")
                        binding.printImage.setImageDrawable(resource)
                        // Устанавливаем начальные размеры принта
                        binding.printImage.layoutParams.width = 200
                        binding.printImage.layoutParams.height = 200
                        binding.printImage.requestLayout()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.d("PositionPrintFragment", "Image load cleared")
                    }
                })
        } catch (e: Exception) {
            Log.e("PositionPrintFragment", "Error loading image", e)
        }
    }

    private fun setupGestures() {
        binding.printImage.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - lastX
                    val deltaY = event.y - lastY
                    currentX += deltaX
                    currentY += deltaY
                    lastX = event.x
                    lastY = event.y
                    updatePrintPosition()
                    true
                }
                else -> false
            }
        }

        // Устанавливаем максимальное значение масштаба 3
        binding.scaleSlider.valueFrom = 0.1f
        binding.scaleSlider.valueTo = 3f
        binding.scaleSlider.value = 1f

        binding.scaleSlider.addOnChangeListener { _, value, _ ->
            currentScale = value
            updatePrintPosition()
        }

        binding.rotateSlider.addOnChangeListener { _, value, _ ->
            currentRotation = value
            updatePrintPosition()
        }
    }

    private fun updatePrintPosition() {
        binding.printImage.apply {
            scaleX = currentScale
            scaleY = currentScale
            rotation = currentRotation
            translationX = currentX
            translationY = currentY
        }
    }

    private fun setupNavigation() {
        binding.nextButton.setOnClickListener {
            // Сохраняем текущую позицию принта
            val printPosition = PrintPosition(
                x = currentX,
                y = currentY,
                scale = currentScale,
                rotation = currentRotation
            )
            viewModel.setPrintPosition(printPosition)
            
            // Переходим к следующему шагу
            findNavController().navigate(PositionPrintFragmentDirections.actionPositionPrintToSizeAndWishes())
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 