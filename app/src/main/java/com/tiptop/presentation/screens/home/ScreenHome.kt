package com.tiptop.presentation.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tiptop.R
import com.tiptop.databinding.ScreenHomeBinding
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreenHome : BaseFragment(R.layout.screen_home) {

    private lateinit var binding: ScreenHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScreenHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        Log.d("pdf", "fileBytes = ${fileBytes?.size}")
//
//
//        binding.pdfView
//            // .fromAsset("testPdf_100mb.pdf")
//            .fromBytes(fileBytes)
//            .defaultPage(0)
//            .enableSwipe(true)
//            .swipeHorizontal(false)
//            .enableDoubletap(true)
//            .enableAntialiasing(true)
//            .enableAnnotationRendering(false)
//            .spacing(1)
//            .nightMode(false)
//            .pageFitPolicy(FitPolicy.WIDTH)
//            .scrollHandle(DefaultScrollHandle(requireContext()))
//            .load()
    }

    companion object {
        var fileBytes: ByteArray? = null
    }
}