package com.tiptop.presentation.screens.add_edit_documents

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tiptop.R
import com.tiptop.app.common.Constants.MOTHER_ID
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.Constants.TYPE_IMAGE
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.encryption
import com.tiptop.app.common.hideKeyBoard
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DocumentRemote
import com.tiptop.presentation.screens.document_view.image.ARG_PARAM_IMAGE
import com.tiptop.presentation.screens.document_view.pdf.ScreenPdfView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.UUID


@AndroidEntryPoint
class ScreenAddEditDocuments : BaseFragmentAddEditDocuments() {
    private val vm by viewModels<AddEditDocumentViewModelImpl>()
    private var adapter: AdapterAddEditDocument? = null
    private var searchText = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.getDocumentsByParentId(MOTHER_ID)
        initClickListeners()
        adapter = AdapterAddEditDocument(object : AdapterAddEditDocument.ClickListener {
            override fun onClick(document: DocumentForRv, position: Int, v: View) {
                when (v.id) {
                    R.id.iv_file_state -> {
                        downloadFile(document.toDocumentLocal())
                    }

                    R.id.l_item_top -> {
                        if (document.type == TYPE_FOLDER) {
                            findNavController().navigate(
                                R.id.action_screenAddEditDocuments_to_screenAddEditDocumentsChild1,
                                bundleOf(ScreenAddEditDocumentsChild1.ARG_PARAM_CHILD1 to MOTHER_ID + document.id)
                            )
                        } else if (document.loaded) {
                            showFile(document.toDocumentLocal())
                        }
                    }
                }

            }

            override fun onLongClick(document: DocumentForRv, position: Int, v: View) {
                showDialogDocument(document.toDocumentLocal()) { view ->
                    when (view.id) {
                        R.id.tv_edit_document -> editDocument(document)
                        R.id.tv_replace_document -> replaceDocument(document.toDocumentLocal())
                        R.id.tv_delete_document -> deleteDocument(document)
                    }
                }
            }
        })

        binding.rvFolders.adapter = adapter
        binding.rvFolders.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        binding.lTopRecycler.visibility = View.GONE
        vm.documentsForRv.observe(viewLifecycleOwner) {
            val documents = it.sortedWith(compareBy<DocumentForRv> { it.type }.thenBy { it.name })
            adapter?.submitList(documents)
        }
    }

    private fun showFile(document: DocumentLocal) {
        val selectedFile: File = requireContext().getFileStreamPath(document.id)
        if (selectedFile.exists()) {
            if (document.type == TYPE_PDF) {
                ScreenPdfView.currentId = document.id
                findNavController().navigate(
                    R.id.action_screenAddEditDocuments_to_screenDocument
                )
            }
            if (document.type == TYPE_IMAGE) {
                findNavController().navigate(
                    R.id.screenImageView,
                    bundleOf(ARG_PARAM_IMAGE to document.id)
                )
            }
//            else if (document.type == TYPE_TXT || document.name.substringAfterLast(".", "") == "txt") {
//                findNavController().navigate(R.id.fragment_txt)
//            }
        }
    }

    override fun onResume() {
        super.onResume()
        initVisibilities()
    }

    private fun initVisibilities() {
        binding.lTopRecycler.visibility = View.GONE
        binding.ivScreenOrientation.visibility = View.GONE
    }

    private fun initClickListeners() {
        binding.fabAdd.setOnClickListener {
            CURRENT_FOLDER_ID = MOTHER_ID
            showAddFolderOrFile { v ->
                when (v.id) {
                    R.id.l_create_folder -> {
                        createFolder()
                    }

                    R.id.l_upload_pdf -> {
                        createFile(TYPE_PDF)
                    }

                    R.id.l_upload_image -> {
                        createFile(TYPE_IMAGE)
                    }
                }
            }
        }

        binding.btnReplace.setOnClickListener {
            binding.fabAdd.visibility = View.VISIBLE
            binding.lBottom.visibility = View.GONE
            REPLACING_DOCUMENT?.let {
                it.parentId = MOTHER_ID
                it.date = System.currentTimeMillis()
                if (isInternetAvailable(requireContext())) {
                    vm.saveDocument(it.toRemote())
                } else {
                    showSnackBarNoConnection()
                }

            }
            REPLACING_DOCUMENT = null
        }
        binding.btnCancelReplace.setOnClickListener {
            binding.fabAdd.visibility = View.VISIBLE
            binding.lBottom.visibility = View.GONE
            REPLACING_DOCUMENT = null
        }
    }

    private fun createFolder() {
        showEditNameDialog("Papka yaratish") { text ->
            if (folderNames.contains(text.lowercase())) {
                showSnackBar("Bu papka allaqachon kiritilgan !")
                return@showEditNameDialog
            }
            if (text.isNotEmpty()) {
                val date = System.currentTimeMillis()
                val name = text.encryption(date)
                val document = DocumentRemote(
                    id = UUID.randomUUID().toString(),
                    parentId = MOTHER_ID,
                    name = name,
                    headBytes = "",
                    type = TYPE_FOLDER,
                    size = 0,
                    date = date,
                    dateAdded = date
                )
                if (isInternetAvailable(requireContext())) {
                    requireActivity().hideKeyBoard()
                    vm.saveDocument(document)
                } else {
                    showSnackBarNoConnection()
                }
            }
        }
    }
}