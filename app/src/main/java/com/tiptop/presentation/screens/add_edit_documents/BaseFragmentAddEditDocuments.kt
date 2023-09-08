package com.tiptop.presentation.screens.add_edit_documents

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tiptop.R
import com.tiptop.app.common.Constants.MOTHER_ID
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.Utils
import com.tiptop.app.common.hideKeyBoard
import com.tiptop.app.common.hideKeyboard
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DocumentRemote
import com.tiptop.databinding.DialogAddEditNameBinding
import com.tiptop.databinding.DialogCreateFolderOrUploadBinding
import com.tiptop.databinding.DialogDocumentBinding
import com.tiptop.databinding.ScreenAddEditDocumentsBinding
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.UUID


@AndroidEntryPoint
open class BaseFragmentAddEditDocuments : BaseFragment(R.layout.screen_add_edit_documents) {
    lateinit var binding: ScreenAddEditDocumentsBinding
    private val vm by viewModels<AddEditDocumentViewModelImpl>()
    private var pickedFile: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        vm.observe()
        binding = ScreenAddEditDocumentsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        if (REPLACING_DOCUMENT != null) {
            replaceDocument(REPLACING_DOCUMENT!!)
        }
    }

    fun replaceDocument(document: DocumentLocal) {
        REPLACING_DOCUMENT = document
        binding.fabAdd.visibility = View.GONE
        binding.lBottom.visibility = View.VISIBLE
    }

    fun showEditNameDialog(
        title: String,
        name: String = "",
        function: (String) -> Unit
    ) {
        val v = DialogAddEditNameBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogStyle).apply {
            setView(v.root)
                .setCancelable(true)
                .create()
        }
        val alert = dialog.show()
        v.title.text = title
        if (name.isNotEmpty()) {
            v.etDocumentName.setText(name)
        }
        v.btnConfirm.setOnClickListener {
            function(v.etDocumentName.text.toString().trim())
            v.etDocumentName.hideKeyboard()
            alert.cancel()
        }
        v.btnCancel.setOnClickListener {
            v.etDocumentName.hideKeyboard()
            alert.cancel()
        }
    }

    fun showAddFolderOrFile(
        isLastFolder: Boolean = false,
        function: (View) -> Unit
    ) {
        val view =
            DialogCreateFolderOrUploadBinding.inflate(
                LayoutInflater.from(requireContext()),
                null,
                false
            )
        val dialogUpload = AlertDialog.Builder(requireContext(), R.style.MyDialogStyle)
            .setCancelable(true)
            .setView(view.root)
            .create()
        dialogUpload.show()
        if (isLastFolder) {
            view.lCreateFolder.visibility = View.GONE
        }
        view.tvCancel.setOnClickListener { dialogUpload.dismiss() }
        view.lCreateFolder.setOnClickListener {
            dialogUpload.dismiss()
            function(view.lCreateFolder)
        }
        view.lUploadPdf.setOnClickListener {
            dialogUpload.dismiss()
            function(view.lUploadPdf)
            // uploadFile(REQUEST_CODE_PDF)
        }
        view.lUploadTxt.setOnClickListener {
            dialogUpload.dismiss()
            //uploadFile(REQUEST_CODE_TXT)
        }
        view.lUploadWord.setOnClickListener {
            dialogUpload.dismiss()
            //  uploadFile(REQUEST_CODE_WORD)
        }
        view.lUploadImage.setOnClickListener {
            dialogUpload.dismiss()
            // uploadImage()
        }
    }

    fun showDialogDocument(
        document: DocumentLocal,
        function: (View) -> Unit
    ) {
        val vBinding = DialogDocumentBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogStyle).apply {
            setView(vBinding.root)
                .setCancelable(true)
                .create()
        }
        val alert = dialog.show()
        vBinding.tvDocumentName.text = document.name.substringBeforeLast(".")

        vBinding.tvEditDocument.setOnClickListener {
            function(vBinding.tvEditDocument)
            alert.cancel()
        }
        vBinding.tvReplaceDocument.setOnClickListener {
            function(vBinding.tvReplaceDocument)
            alert.cancel()
        }
        vBinding.tvDeleteDocument.setOnClickListener {
            function(vBinding.tvDeleteDocument)
            alert.cancel()
        }
        vBinding.tvCancel.setOnClickListener { alert.cancel() }
    }

    fun createPdfFile() {
        val permission = MutableLiveData(false)
        requestPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) {
            permission.postValue(it)
        }
        permission.observe(viewLifecycleOwner) {
            if (it) {
                isLoading = true
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
                resultLauncher.launch(intent)
            }
        }
    }

    fun editDocument(document: DocumentLocal) {
        val docName = if (document.type == TYPE_FOLDER) "Papka" else "Fayl"
        showEditNameDialog(
            title = "$docName nomini o'zgartirish",
            name = document.name.substringBeforeLast(".")
        ) { text ->
            if (text == document.name.substringBeforeLast(".")) {
                return@showEditNameDialog
            }
            if (text.isNotEmpty()) {
                val date = System.currentTimeMillis()
                val documentEdited = document.copy(name = text, date = date)
                if (isInternetAvailable(requireContext())) {
                    requireActivity().hideKeyBoard()
                    vm.saveDocument(documentEdited.toRemote())
                } else {
                    showSnackBarNoConnection()
                }
            }
        }
    }

    fun downloadFile(document: DocumentLocal) {
        showConfirmDialog(
            document.name.replaceAfter(".", ""),
            "Ushbu faylni tortib olishga ishonchingiz komilmi ?"
        ) {
            if (it) {
                vm.downloadDocument(document)
            }
        }
    }

    fun deleteDocument(document: DocumentLocal) {
        val docName = if (document.type == TYPE_FOLDER) "papka" else "fayl"
        showConfirmDialog(
            title = document.name.substringBeforeLast("."),
            message = "Ushbu ${docName}ni o'chirishga ishonchingiz komilmi?"
        ) {
            if (it) {
                if (isInternetAvailable(requireContext())) {
                    vm.deleteDocument(document)
                } else {
                    showSnackBarNoConnection()
                }
            }
        }
    }

    private fun requestPermissions(vararg permissions: String, function: (Boolean) -> Unit) {
        Dexter.withContext(requireContext())
            .withPermissions(
                *permissions
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            function(true)
                        } else {
                            showSettingsDialog(*permissions)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                showSnackBar("Xatolik")
            }
            .check()
    }

    private fun showSettingsDialog(vararg permissions: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Ruxsat kerak !")
        builder.setMessage("${getPermissionNames(*permissions)}larga ruxsat talab etiladi!")
        builder.setPositiveButton("Sozlamalar") { dialog, _ ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, 101)
        }
        builder.setNegativeButton("Bekor") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun getPermissionNames(vararg permissions: String): String {
        var names = ""
        permissions.forEach {
            when (it) {
                Manifest.permission.CAMERA -> names = "kamera, $names"
                Manifest.permission.REQUEST_INSTALL_PACKAGES -> names = "dastur o'rnatish, $names"
                Manifest.permission.READ_EXTERNAL_STORAGE -> names = "xotiradan o'qish, $names"
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> names = "xotiraga yozish, $names"
                Manifest.permission.ACCESS_COARSE_LOCATION -> names = "geo lokaciya, $names"
                Manifest.permission.ACCESS_FINE_LOCATION -> names = "geo lokaciya, $names"
            }
        }
        return if (names.length > 2) names.trim().substring(0, names.length - 2) else ""
    }

    private var resultLauncher =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                pickedFile = data?.data
                if (pickedFile != null) {
                    createRemoteFile(TYPE_PDF)
                }
            }
        }


    @SuppressLint("Recycle")
    private fun createRemoteFile(type: Int) {
        val file = File(pickedFile.toString())
        val date = System.currentTimeMillis()
        val documentFile =
            DocumentFile.fromSingleUri(requireContext(), pickedFile!!)
        val fileName = documentFile?.name ?: (date / 1000).toString()
        val headBytesCount = Utils().getHeadBytesCount().toInt()
        file.setReadable(true)
        val bytes =
            pickedFile?.let { requireContext().contentResolver.openInputStream(it)?.readBytes() }
        val fileSize = bytes?.size ?: 0
        val documentRemote = DocumentRemote(
            id = UUID.randomUUID().toString(),
            parentId = CURRENT_FOLDER_ID,
            name = fileName,
            headBytes = String(bytes!!.copyOfRange(0, headBytesCount)),
            type = type,
            size = fileSize.toLong(),
            date = date,
            dateAdded = date
        )
        vm.saveDocument(
            documentRemote, bytes.copyOfRange(headBytesCount, fileSize)
        )
    }

    companion object {
        var CURRENT_FOLDER_ID = MOTHER_ID
        var REPLACING_DOCUMENT: DocumentLocal? = null
    }
}