package com.tiptop.presentation.screens.add_edit_documents

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.tiptop.R
import com.tiptop.app.common.Constants.MOTHER_ID
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.Constants.TYPE_IMAGE
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.Encryptor
import com.tiptop.app.common.Resource
import com.tiptop.app.common.Utils
import com.tiptop.app.common.encryption
import com.tiptop.app.common.hideKeyBoard
import com.tiptop.app.common.hideKeyboard
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DocumentRemote
import com.tiptop.databinding.DialogAddEditNameBinding
import com.tiptop.databinding.DialogCreateFolderOrUploadBinding
import com.tiptop.databinding.DialogDocumentBinding
import com.tiptop.databinding.ScreenAddEditDocumentsBinding
import com.tiptop.presentation.MainActivity.Companion.TEMPORARY_OUT
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

import java.util.UUID


@AndroidEntryPoint
open class BaseFragmentAddEditDocuments : BaseFragment(R.layout.screen_add_edit_documents) {
    lateinit var binding: ScreenAddEditDocumentsBinding
    private val vm by viewModels<AddEditDocumentViewModelImpl>()
    private var pickedFile: Uri? = null
    private var documentNames = emptyList<String>()
    var folderNames = emptyList<String>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScreenAddEditDocumentsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        vm.documentNames.observe(viewLifecycleOwner) {
            documentNames = it
        }
        vm.folderNames.observe(viewLifecycleOwner) {
            folderNames = it
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
        }
        view.lUploadTxt.setOnClickListener {
            dialogUpload.dismiss()
        }
        view.lUploadWord.setOnClickListener {
            dialogUpload.dismiss()
        }
        view.lUploadImage.setOnClickListener {
            dialogUpload.dismiss()
            function(view.lUploadImage)
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

        val fileName = document.nameDecrypted()

        vBinding.tvDocumentName.text = fileName.substringBeforeLast(".")

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

    fun createFile(typeFile: Int) {
        TYPE = typeFile
        val permission = MutableLiveData(false)
        requestPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) {
            permission.postValue(it)
        }
        permission.observe(viewLifecycleOwner) {
            if (it) {
                TEMPORARY_OUT = true
                // Intent.ACTION_OPEN_DOCUMENT
                // Intent.ACTION_GET_CONTENT
                //addCategory(Intent.CATEGORY_OPENABLE)
                if (TYPE == TYPE_PDF) {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "application/pdf"
                    resultLauncher.launch(intent)
                } else if (TYPE == TYPE_IMAGE) {
                    val intent =// Intent(Intent.ACTION_GET_CONTENT)
                        Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    resultLauncher.launch(intent)
                }
            }
        }
    }

    fun editDocument(document: DocumentForRv) {
        if (document.type == TYPE_FOLDER) {
            val firstName = document.name
            showEditNameDialog(
                title = "Papka nomini o'zgartirish",
                name = document.name
            ) { text ->
                if (text == firstName) {
                    return@showEditNameDialog
                }
                if (folderNames.contains(text.lowercase())) {
                    showSnackBar("Bu nomli papka allaqachon mavjud")
                } else if (text.isNotEmpty()) {
                    val documentEdited = document.copy(
                        name = text,
                        date = System.currentTimeMillis()
                    )
                    if (isInternetAvailable(requireContext())) {
                        requireActivity().hideKeyBoard()
                        vm.saveDocument(documentEdited.toRemote())
                    } else {
                        showSnackBarNoConnection()
                    }
                }
            }
        } else {
            val firstName = document.name.substringBeforeLast(".")
            // val extention = document.name.substringAfterLast(".")
            showEditNameDialog(
                title = "Fayl nomini o'zgartirish",
                name = firstName
            ) { text ->
                if (text == firstName) {
                    return@showEditNameDialog
                }
                if (documentNames.contains(text.lowercase())) {
                    showSnackBar("Bu nomli fayl allaqachon mavjud")
                } else if (text.isNotEmpty()) {
                    val documentEdited = document.copy(
                        name = text,
                        date = System.currentTimeMillis()
                    )
                    if (isInternetAvailable(requireContext())) {
                        requireActivity().hideKeyBoard()
                        vm.saveDocument(documentEdited.toRemote())
                    } else {
                        showSnackBarNoConnection()
                    }
                }
            }
        }
    }

    fun downloadFile(document: DocumentLocal) {
        val fileName = document.nameDecrypted()
        showConfirmDialog(
            fileName.replaceAfter(".", ""),
            "Ushbu faylni tortib olishga ishonchingiz komilmi ?"
        ) {
            if (it) {
                vm.downloadDocument(document)
            }
        }
    }

    fun deleteDocument(document: DocumentForRv) {
        if (document.type == TYPE_FOLDER) {
            if (document.count>0) {
                showSnackBar("Ushbu papkada fayllar bor. Avval ularni o'chiring")
            } else {
                showConfirmDialog(
                    title = document.name,
                    message = "Ushbu papkani o'chirishga ishonchingiz komilmi?"
                ) {
                    if (it) {
                        if (isInternetAvailable(requireContext())) {
                            vm.deleteDocument(document.toDocumentLocal())
                        } else {
                            showSnackBarNoConnection()
                        }
                    }
                }
            }

        } else {
            showConfirmDialog(
                title = document.name,
                message = "Ushbu faylni o'chirishga ishonchingiz komilmi?"
            ) {
                if (it) {
                    if (isInternetAvailable(requireContext())) {
                        vm.deleteDocument(document.toDocumentLocal())
                    } else {
                        showSnackBarNoConnection()
                    }
                }
            }

        }


    }


    private var resultLauncher =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                pickedFile = data?.data
                if (pickedFile != null) {
                    createRemoteFile()
                }
            }
        }

    @SuppressLint("Recycle")
    private fun createRemoteFile() {
        pickedFile?.let { pickedUri ->
            val file = File(pickedUri.toString())
            val date = System.currentTimeMillis()
            val documentFile =
                DocumentFile.fromSingleUri(requireContext(), pickedUri)
            val fileName = (documentFile?.name ?: (date / 1000).toString()).encryption(date)
            if (documentNames.contains(fileName.lowercase())) {
                showSnackBar("Bu nomli fayl allaqachon mavjud")
                return@let
            }
            val headBytesCount = Utils().getHeadBytesCount().toInt()
            file.setReadable(true)
            val bytes = requireContext().contentResolver.openInputStream(pickedUri)?.readBytes()
                ?: byteArrayOf()

            val fileSize = bytes.size
            val document = DocumentRemote(
                id = UUID.randomUUID().toString(),
                parentId = CURRENT_FOLDER_ID,
                name = fileName,
                headBytes = "",// String(bytes!!.copyOfRange(0, headBytesCount)),
                type = TYPE,
                size = fileSize.toLong(),
                date = date,
                dateAdded = date
            )
            if (TYPE == TYPE_PDF) {
                Encryptor().getEncryptedBytes(
                    Utils().getKeyStr(document.dateAdded),
                    Utils().getSpecStr(document.dateAdded),
                    bytes.copyOfRange(0, headBytesCount)
                ) { encryptedHeadBytes ->
                    vm.saveDocument(
                        document = document,
                        headByteArray = encryptedHeadBytes,
                        bodyByteArray = bytes.copyOfRange(headBytesCount, fileSize)
                    )
                }
            } else {
                Encryptor().getEncryptedBytes(
                    Utils().getKeyStr(document.dateAdded),
                    Utils().getSpecStr(document.dateAdded),
                    bytes
                ) { encryptedBytes ->
                    vm.saveDocument(
                        document = document,
                        headByteArray = null,
                        bodyByteArray = encryptedBytes
                    )
                }
            }
        }
    }

    companion object {
        var CURRENT_FOLDER_ID = MOTHER_ID
        var REPLACING_DOCUMENT: DocumentLocal? = null
        private var TYPE: Int = TYPE_PDF
    }
}