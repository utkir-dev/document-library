package com.tiptop.presentation.screens.document_view.image

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tiptop.R
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.app.common.share
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.databinding.ScreenImageViewBinding
import com.tiptop.presentation.MainActivity.Companion.TEMPORARY_OUT
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


const val ARG_PARAM_IMAGE = "arg_param_image"

@AndroidEntryPoint
class ScreenImageView : BaseFragment(R.layout.screen_image_view) {
    private val vm by viewModels<ImageViewModelIml>()
    private var _binding: ScreenImageViewBinding? = null
    private var currentDocument: DocumentLocal? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val id = it.getString(ARG_PARAM_IMAGE) ?: ""
            if (id.isNotEmpty()) {
                vm.setDocument(id)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ScreenImageViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showImage()
        initClickListeners()
        initObservers()
    }

    private fun showImage() {
        vm.documentBytes.observe(viewLifecycleOwner) { byteArray ->
            if (byteArray != null) {
                currentBytes = byteArray
                val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imageView.setImageBitmap(bmp)
            }
        }
    }

    private fun initObservers() {
        vm.currentDocument.observe(viewLifecycleOwner) {
            currentDocument = it
        }
    }

    private fun initClickListeners() {
        binding.ivShare.setOnClickListener {
            currentBytes?.let { bytes ->
                currentDocument?.let { doc ->
                    TEMPORARY_OUT = true
                    if (isInternetAvailable(requireContext())) {
                        share(doc, bytes, requireActivity())
                    } else {
                        showSnackBarNoConnection()
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        var currentBytes: ByteArray? = null
    }
}