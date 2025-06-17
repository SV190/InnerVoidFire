package com.example.innervoid.ui.customization

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.innervoid.databinding.FragmentSelectPrintBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class SelectPrintFragment : Fragment() {
    private var _binding: FragmentSelectPrintBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomizationViewModel by activityViewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d("SelectPrintFragment", "Selected image URI: $uri")
                // Загружаем изображение с помощью Glide
                Glide.with(this)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.selectedImage)
                
                // Сохраняем URI в ViewModel
                viewModel.setPrintUri(uri.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectPrintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectPrintButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        binding.nextButton.setOnClickListener {
            findNavController().navigate(SelectPrintFragmentDirections.actionSelectPrintToPositionPrint())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 