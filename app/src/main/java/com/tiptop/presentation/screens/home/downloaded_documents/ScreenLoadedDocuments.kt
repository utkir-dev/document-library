package com.tiptop.presentation.screens.home.downloaded_documents

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gg.gapo.treeviewlib.GapoTreeView
import com.gg.gapo.treeviewlib.model.NodeViewData
import com.tiptop.R
import com.tiptop.app.common.Constants
import com.tiptop.app.common.Constants.MOTHER_ID
import com.tiptop.app.common.dp
import com.tiptop.app.common.validateFileSize
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.databinding.ScreenDocumentsBinding
import com.tiptop.presentation.screens.BaseFragment
import com.tiptop.presentation.screens.document_view.image.ARG_PARAM_IMAGE
import com.tiptop.presentation.screens.document_view.pdf.ScreenPdfView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class ScreenLoadedDocuments : BaseFragment(R.layout.screen_documents) {
    private val vm by viewModels<LoadedDocumentsViewModelImpl>()
    private var adapter: AdapterLoadedDocuments? = null
    private var searchText = ""
    private var _binding: ScreenDocumentsBinding? = null
    private var observer: Observer<List<DocumentForRv>>? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScreenDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        showDocumentsWithFolders()
        vm.isFolder.observe(viewLifecycleOwner) {
            if (it) {
                binding.ivFolder.setImageResource(R.drawable.ic_file)
                showDocumentsWithFolders()
            } else {
                binding.ivFolder.setImageResource(R.drawable.ic_folder)
                showDocumentsWithoutFolders()
            }
        }
    }

    private fun showDocumentsWithFolders() {
        adapter = null
        vm.getDocumentsWithFolders()
        observer?.let {
            vm.documents.removeObserver(it)
        }
        vm.documents.observe(viewLifecycleOwner) { list ->
            adapter = AdapterLoadedDocuments(object : AdapterLoadedDocuments.ClickListener {

                override fun onClickFile(document: DocumentForRv, v: View) {
                    showFile(document.toDocumentLocal())
                }

                override fun onClickFolder(
                    document: DocumentForRv,
                    rvChild1: RecyclerView
                ) {
                    val adapterChild1 =
                        AdapterLoadedDocuments(listener = object :
                            AdapterLoadedDocuments.ClickListener {

                            override fun onClickFile(
                                documentChild1: DocumentForRv,
                                v: View
                            ) {
                                showFile(documentChild1.toDocumentLocal())
                            }

                            override fun onClickFolder(
                                documentChild1: DocumentForRv,
                                rvChild2: RecyclerView
                            ) {
                                val adapterChild2 =
                                    AdapterLoadedDocuments(listener = object :
                                        AdapterLoadedDocuments.ClickListener {

                                        override fun onClickFile(
                                            documentChild2: DocumentForRv,
                                            v: View
                                        ) {
                                            showFile(documentChild2.toDocumentLocal())
                                        }

                                        override fun onClickFolder(
                                            documentChild2: DocumentForRv,
                                            rv: RecyclerView
                                        ) {
                                        }
                                    })
                                // child-2 recycler view
                                rvChild2.adapter = adapterChild2
                                adapterChild2.submitList(list.filter { it.parentId == documentChild1.parentId + documentChild1.id }
                                    .sortedWith(compareBy<DocumentForRv> { it.type }.thenBy { it.name }))
                            }
                        })

                    // child-1 recycler view
                    rvChild1.adapter = adapterChild1
                    adapterChild1.submitList(list.filter { it.parentId == MOTHER_ID + document.id }
                        .sortedWith(compareBy<DocumentForRv> { it.type }.thenBy { it.name }))
                }
            })

            binding.rvFolders.adapter = adapter
            adapter?.submitList(list.filter { it.parentId == MOTHER_ID }
                .sortedWith(compareBy<DocumentForRv> { it.type }.thenBy { it.name }))
        }
    }

    private fun showDocumentsWithoutFolders() {
        adapter = null
        vm.getDocumentsWithoutFolders()
        adapter = AdapterLoadedDocuments(object : AdapterLoadedDocuments.ClickListener {
            override fun onClickFile(document: DocumentForRv, v: View) {
                showFile(document.toDocumentLocal())
            }

            override fun onClickFolder(
                document: DocumentForRv,
                rv: RecyclerView
            ) {
            }
        })

        binding.rvFolders.adapter = adapter
        observer?.let {
            vm.documents.removeObserver(it)
        }
        observer = Observer { list ->
            adapter?.submitList(list.sortedBy { it.name })
        }
        vm.documents.observe(viewLifecycleOwner, observer!!)
    }

    private fun initClickListeners() {
        binding.ivFolder.visibility = View.VISIBLE
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.ivFolder.setOnClickListener {
            vm.initFolder()
        }
        binding.ivScreenOrientation.setOnClickListener {
            changeScreenOriantation()
        }
    }

    private fun showFile(document: DocumentLocal) {
        val selectedFile: File = requireContext().getFileStreamPath(document.id)
        if (selectedFile.exists()) {
            if (document.type == Constants.TYPE_PDF) {
                ScreenPdfView.currentId = document.id
                findNavController().navigate(
                    R.id.action_screenLoadedDocuments_to_screenDocument
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
}