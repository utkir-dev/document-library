package com.tiptop.presentation.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import com.tiptop.R
import com.tiptop.app.common.Constants.LIB_VERSION
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.DownloadController
import com.tiptop.app.common.Utils
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.local.LibVersion
import com.tiptop.databinding.ScreenHomeBinding
import com.tiptop.presentation.MainActivity
import com.tiptop.presentation.screens.BaseFragment
import com.tiptop.presentation.screens.document_view.pdf.ScreenPdfView
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ScreenHome : BaseFragment(R.layout.screen_home) {

    @Inject
    lateinit var shared: SharedPreferences
    private lateinit var binding: ScreenHomeBinding
    private val vm by viewModels<HomeViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScreenHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserves()
        initClickListeners()
    }

    private fun initClickListeners() {
        binding.ivScreenOrientation.setOnClickListener { changeScreenOriantation() }
        binding.cardLoadedDocuments.setOnClickListener {
            findNavController().navigate(R.id.action_screenHome_to_screenLoadedDocuments)
        }
        binding.cardAllDocuments.setOnClickListener {
            findNavController().navigate(R.id.action_screenHome_to_screenAllDocuments)
        }
        binding.ivToggle.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserves() {
        vm.countAllDocuments.observe(viewLifecycleOwner) { count: Int ->
            binding.tvAllDocuments.text = "Kutubxona ($count)"
        }
        vm.countLoadedDocuments.observe(viewLifecycleOwner) { count: Int ->
            binding.tvLoadedDocuments.text = "Yuklanganlar ($count)"
        }
        vm.countNewDocuments.observe(viewLifecycleOwner) { count: Int ->
            if (count > 0) {
                binding.tvNewDocumentsCount.visibility = View.VISIBLE
                binding.tvNewDocumentsCount.text = "+$count"
            } else {
                binding.tvNewDocumentsCount.visibility = View.GONE
            }
        }
        vm.lastSeenDocument.observe(viewLifecycleOwner) { document: DocumentLocal? ->
            document?.let {
                if (document.lastSeenDate > 0) {
                    binding.tvLastDocumentName.visibility = View.VISIBLE
                    binding.tvLastDocumentName.text =
                        document.nameDecrypted().substringBeforeLast(".")
                    binding.ivLastBook.setOnClickListener {
                        ScreenPdfView.currentId = document.id
                        findNavController().navigate(
                            R.id.action_screenHome_to_screenDocument
                        )
                    }
                    setPdfIcon(it)
                } else {
                    binding.tvLastDocumentName.visibility = View.GONE
                }
            }
        }

        vm.hijriy.observe(viewLifecycleOwner) { today ->
            if (today.isNotEmpty()) {
                binding.tvDateHijri.text = today
            }
        }
        observeNewVersion()

    }


    @SuppressLint("SetTextI18n")
    private fun observeNewVersion() {
        (activity as MainActivity).vm.libVersion.observe(viewLifecycleOwner) { libVersion ->
            try {
                val version = libVersion.apkName.substringBeforeLast(".")
                if (version != LIB_VERSION && version.isNotEmpty()) {
                    binding.tvLinkVersion.visibility = View.VISIBLE
                    binding.tvLinkVersion.text = "Yangi versiya: $version"
                    binding.tvLinkVersion.setOnClickListener {
                        showConfirmDialog(
                            "Yangi versiya: $version",
                            "Hajmi: ${libVersion.size}. Hozir tortib olishga ishonchingiz komilmi ?"
                        ) {
                            if (it) {
                                if (isInternetAvailable(requireContext())) {
                                    downloadApk(libVersion)
                                } else {
                                    showSnackBarNoConnection()
                                }
                            }
                        }
                    }
                } else {
                    binding.tvLinkVersion.visibility = View.GONE
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun downloadApk(libVersion: LibVersion) {
        val permission = MutableLiveData(false)
        requestPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            permission.postValue(it)
        }
        permission.observe(viewLifecycleOwner) {
            if (it) {
                DownloadController((activity as MainActivity), libVersion.apkName, libVersion.url).enqueueDownload()
            }
        }
    }


    private fun setPdfIcon(document: DocumentLocal, bitmapNew: Bitmap? = null) {
        if (document.type == TYPE_PDF) {
            if (bitmapNew != null) {
                binding.ivLastBook.setImageBitmap(bitmapNew)
            } else {
                val bitmap = getBitmapFromPref(document.id)
                if (bitmap != null) {
                    binding.ivLastBook.setImageBitmap(bitmap)
                } else {
                    createCoverPdf(document)
                }
            }
        }
    }

    private fun createCoverPdf(document: DocumentLocal) {
        try {
            vm.getFileBytes(document)
            vm.documentBytes.observe(viewLifecycleOwner) { bytes ->
                val pageNum = 0
                val pdfiumCore = PdfiumCore(requireContext())
                val pdfDocument: PdfDocument = pdfiumCore.newDocument(bytes)
                pdfiumCore.openPage(pdfDocument, pageNum)
                val width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum)
                val height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum)
                val bitmap = Bitmap.createBitmap(
                    width, height,
                    Bitmap.Config.RGB_565
                )
                pdfiumCore.renderPageBitmap(
                    pdfDocument, bitmap, pageNum, 0, 0,
                    width, height
                )
                pdfiumCore.closeDocument(pdfDocument)
                saveBitmapToPref(bitmap, document.id)
                setPdfIcon(document, bitmap)
            }
        } catch (_: Exception) {
        }
    }

    private fun saveBitmapToPref(bitmap: Bitmap, documentId: String) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 25, baos)
        val compressImage = baos.toByteArray()
        val sEncodedImage = Base64.encodeToString(compressImage, Base64.DEFAULT)
        shared.edit().putString(Utils().getUsersFolder() + documentId, sEncodedImage).apply()
    }

    private fun getBitmapFromPref(documentId: String): Bitmap? {
        val encodedImage = shared.getString(Utils().getUsersFolder() + documentId, "")
        val b = Base64.decode(encodedImage, Base64.DEFAULT)
        val bitmapImage = BitmapFactory.decodeByteArray(b, 0, b.size)
        return bitmapImage
    }
}