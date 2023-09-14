package com.tiptop.presentation.screens.add_edit_documents

import android.annotation.SuppressLint
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
import com.tiptop.app.common.Constants.MOTHER_ID
import com.tiptop.app.common.Constants.TYPE_IMAGE
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.presentation.screens.document_view.pdf.ARG_PARAM_DOCUMENT
import com.tiptop.presentation.screens.document_view.image.ARG_PARAM_IMAGE
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class ScreenAddEditDocumentsChild2 : BaseFragmentAddEditDocuments() {
    private val vm by viewModels<AddEditDocumentViewModelImpl>()
    private var adapter: AdapterAddEditDocument? = null
    private var searchText = ""
    private var folders = ArrayList<String>()
    private var parentId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            parentId = it.getString(ARG_PARAM_CHILD2) ?: ""
            val idChild2 = it.getString(ARG_PARAM_CURRENT_DOCUMENT) ?: ""
            val idChild1 = parentId.replace(MOTHER_ID, "").replace(idChild2, "")
            if (parentId.isNotEmpty()) {
                vm.setChildDocument1(idChild1)
                vm.setChildDocument2(idChild2)
                vm.getDocumentsByParentId(parentId)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        adapter = AdapterAddEditDocument(object : AdapterAddEditDocument.ClickListener {
            override fun onClick(document: DocumentForRv, position: Int, v: View) {
                when (v.id) {
                    R.id.iv_file_state -> {
                        downloadFile(document.toDocumentLocal())
                    }

                    R.id.l_item_top -> {
                        if (document.loaded) {
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
                findNavController().navigate(
                    R.id.action_screenAddEditDocumentsChild2_to_screenDocument,
                    bundleOf(ARG_PARAM_DOCUMENT to document.id)
                )
            }
            if (document.type == Constants.TYPE_IMAGE) {
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

    @SuppressLint("SetTextI18n")
    private fun initVisibilities() {
        binding.lTopRecycler.visibility = View.VISIBLE
        binding.tvChild1.visibility = View.VISIBLE
        binding.tvChild2.visibility = View.VISIBLE
        binding.tvChildMain.setTextColor(Color.BLACK)

        binding.tvChild1.setTextColor(Color.BLACK)
        vm.childDocument1.observe(viewLifecycleOwner) {
            if (it != null) {
                val child1 = it.name
                binding.tvChild1.text = "$child1 / "
            }
        }
        vm.childDocument2.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.tvChild2.text = it.name
            }
        }
    }

    private fun initClickListeners() {
        binding.fabAdd.setOnClickListener {
            CURRENT_FOLDER_ID = parentId
            showAddFolderOrFile(true) { v ->
                when (v.id) {
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

    companion object {
        var ARG_PARAM_CHILD2 = "arg_params_child2"
        var ARG_PARAM_CURRENT_DOCUMENT = "arg_param_current_document"
    }
}