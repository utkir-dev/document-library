package com.tiptop.presentation.screens.add_edit_documents

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tiptop.R
import com.tiptop.app.common.Constants
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.hideKeyBoard
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DocumentRemote
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.UUID


@AndroidEntryPoint
class ScreenAddEditDocumentsChild1 : BaseFragmentAddEditDocuments() {
    private val vm by viewModels<AddEditDocumentViewModelImpl>()
    private var adapter: AdapterDocument? = null
    private var searchText = ""
    private var folders = ArrayList<String>()
    private var parentId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            parentId = it.getString(ARG_PARAM_CHILD1) ?: ""
            if (parentId.isNotEmpty()) {
                vm.getDocumentsByParentId(parentId)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        adapter = AdapterDocument(object : AdapterDocument.ClickListener {
            override fun onClick(document: DocumentForRv, position: Int, v: View) {
                when (v.id) {
                    R.id.iv_file_state -> {
                        downloadFile(document.toDocumentLocal())
                    }

                    R.id.l_item_top -> {
                        if (document.type == TYPE_FOLDER) {
                            findNavController().navigate(
                                R.id.action_screenAddEditDocumentsChild1_to_screenAddEditDocumentsChild2,
                                bundleOf(ScreenAddEditDocumentsChild2.ARG_PARAM_CHILD2 to parentId + document.id)
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
                        R.id.tv_edit_document -> editDocument(document.toDocumentLocal())
                        R.id.tv_replace_document -> replaceDocument(document.toDocumentLocal())
                        R.id.tv_delete_document -> deleteDocument(document.toDocumentLocal())
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
            folders = it.filter { it.type == 0 }.map { it.name } as ArrayList<String>
            adapter?.submitList(documents)
        }
    }



    private fun showFile(document: DocumentLocal) {
        val selectedFile: File = requireContext().getFileStreamPath(document.id)
        if (selectedFile.exists()) {
            if (document.type == Constants.TYPE_PDF) {
                //  findNavController().navigate(R.id.fragment_pdf)
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
        binding.lTopRecycler.visibility=View.VISIBLE
        binding.tvChild1.visibility=View.VISIBLE
        binding.tvChild2.visibility = View.GONE
        binding.tvChildMain.text="Asosiy / "
        binding.tvChildMain.setTextColor(Color.BLACK)
        binding.tvChild1.text= "Child1"
    }

    private fun initClickListeners() {
        binding.fabAdd.setOnClickListener {
            CURRENT_FOLDER_ID = parentId
            showAddFolderOrFile { v ->
                when (v.id) {
                    R.id.l_create_folder -> createFolder()
                    R.id.l_upload_pdf -> createPdfFile()

                }
            }
        }
        binding.btnReplace.setOnClickListener {
            binding.fabAdd.visibility = View.VISIBLE
            binding.lBottom.visibility = View.GONE
            REPLACING_DOCUMENT?.let {
                it.parentId = parentId
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
            if (folders.contains(text)) {
                showSnackBar("Bu papka allaqachon kiritilgan !")
                return@showEditNameDialog
            }
            if (text.isNotEmpty()) {
                val date = System.currentTimeMillis()
                val document = DocumentRemote(
                    id = UUID.randomUUID().toString(),
                    parentId = parentId,
                    name = text,
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

    companion object {
        var ARG_PARAM_CHILD1 = "arg_params_child1"
    }
}