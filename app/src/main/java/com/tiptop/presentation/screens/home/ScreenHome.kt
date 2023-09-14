package com.tiptop.presentation.screens.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.tiptop.R
import com.tiptop.app.common.Constants.KEY_LIB_VERSION
import com.tiptop.app.common.Constants.LIB_VERSION
import com.tiptop.app.common.SharedPrefSimple
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.LibVersion
import com.tiptop.databinding.ScreenHomeBinding
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileNotFoundException

@AndroidEntryPoint
class ScreenHome : BaseFragment(R.layout.screen_home) {

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
        binding.tvLinkVersion.setOnClickListener { }
        binding.cardLastBook.setOnClickListener { }
        binding.cardLoadedDocuments.setOnClickListener {findNavController().navigate(R.id.action_screenHome_to_screenLoadedDocuments) }
        binding.cardAllDocuments.setOnClickListener {
            findNavController().navigate(R.id.action_screenHome_to_screenAllDocuments)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserves() {
        vm.countAllDocuments.observe(viewLifecycleOwner) { count: Int ->
            binding.tvAllDocuments.text = "Kutubxona ($count)"
        }
        vm.countLoadedDocuments.observe(viewLifecycleOwner) { count: Int ->
            binding.tvLoadedDocuments.text = "Yuklab olinganlar ($count)"
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
                    binding.tvLastDocumentName.text = document.name.substringBeforeLast(".")
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
        val jsonString = SharedPrefSimple(requireActivity()).getString(KEY_LIB_VERSION)
        if (jsonString.isNotEmpty()) {
            try {
                val libVersion = Gson().fromJson(jsonString, LibVersion::class.java)
                if (libVersion.version != LIB_VERSION && libVersion.version.isNotEmpty()) {
                    binding.tvLinkVersion.visibility = View.VISIBLE
                    binding.tvLinkVersion.text = "Yangi versiya: ${libVersion.version}"
                } else {
                    binding.tvLinkVersion.visibility = View.GONE
                }
            } catch (_: Exception) {
            }

        } else {
            binding.tvLinkVersion.visibility = View.GONE
        }

    }

    //    private fun getBitmap(document: DocumentLocal): Bitmap? {
//        if (document.type == TYPE_PDF) {
//            getBitmapFromPref(document.id)?.let { return it }
//            val file: File? = requireContext().getFileStreamPath(document.id)
//            val outputFile =
//            val pageNum = 0
//            val pdfiumCore = PdfiumCore(requireContext())
//            try {
//                val pdfDocument: PdfDocument = pdfiumCore.newDocument(openFile(outputFile))
//                pdfiumCore.openPage(pdfDocument, pageNum)
//                val width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum)
//                val height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum)
//                // ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
//                // RGB_565 - little worse quality, twice less memory usage
//                val bitmap = Bitmap.createBitmap(
//                    width, height,
//                    Bitmap.Config.RGB_565
//                )
//                pdfiumCore.renderPageBitmap(
//                    pdfDocument, bitmap, pageNum, 0, 0,
//                    width, height
//                )
//                //if you need to render annotations and form fields, you can use
//                //the same method above adding 'true' as last param
//                pdfiumCore.closeDocument(pdfDocument) // important!
//// save to share
//                saveBitmapToPref(bitmap, document.id)
//                return bitmap
//            } catch (ex: IOException) {
//                ex.printStackTrace()
//            }
//        }
//        return null
//    }
//    private fun saveBitmapToPref(bitmap: Bitmap,documentId: String) {
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 25, baos)
//        val compressImage = baos.toByteArray()
//        val sEncodedImage = Base64.encodeToString(compressImage, Base64.DEFAULT)
//        PREF.setString(KEY_LAST_BOOK_BITMAP + documentId, sEncodedImage)
//    }
//    private fun getBitmapFromPref(bookId: String): Bitmap? {
//        val encodedImage =  PREF.getString(KEY_LAST_DOCUMENT_BITMAP + bookId) ?: ""
//        val b = Base64.decode(encodedImage, Base64.DEFAULT)
//        val bitmapImage = BitmapFactory.decodeByteArray(b, 0, b.size)
//        return bitmapImage
//    }
    private fun openFile(file: File?): ParcelFileDescriptor? {
        val descriptor: ParcelFileDescriptor = try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
        return descriptor
    }

    companion object {
        var fileBytes: ByteArray? = null
    }
}