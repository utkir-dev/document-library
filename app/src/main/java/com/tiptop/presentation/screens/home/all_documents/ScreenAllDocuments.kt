package com.tiptop.presentation.screens.home.all_documents

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
import androidx.navigation.fragment.findNavController
import com.gg.gapo.treeviewlib.GapoTreeView
import com.gg.gapo.treeviewlib.model.NodeViewData
import com.tiptop.R
import com.tiptop.app.common.Constants
import com.tiptop.app.common.Constants.MOTHER_ID
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.Constants.TYPE_IMAGE
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.Constants.TYPE_TXT
import com.tiptop.app.common.Encryptor
import com.tiptop.app.common.Utils
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
import java.nio.charset.StandardCharsets


@AndroidEntryPoint
class ScreenAllDocuments : BaseFragment(R.layout.screen_documents),
    GapoTreeView.Listener<DocumentForRv> {
    private var treeView: GapoTreeView<DocumentForRv>? = null
    private var listTempDocuments = ArrayList<DocumentLocal>()
    private val vm by viewModels<AllDocumentsViewModelImpl>()
    private var _binding: ScreenDocumentsBinding? = null
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
        vm.initDocumentsHome()
        initClickListeners()

        vm.documents.observe(viewLifecycleOwner) {
            treeView = GapoTreeView.Builder.plant<DocumentForRv>(requireContext())
                .withRecyclerView(binding.rvFolders)
                .withLayoutRes(R.layout.item_document)
                .setListener(this)
                .setData(it.filter { it.parentId == MOTHER_ID }
                    .sortedWith(compareBy<DocumentForRv> { it.type }.thenBy { it.name }))
                .itemMargin(16.dp) //optional: margin by node's level. default = 24dp
                //.showAllNodes(false) //optional: show all nodes or just show parent node. default = false
                //.addAdapters(config = Config.DEFAULT, adapters = arrayOf(adapter))
                .build()
        }
    }

    private fun initClickListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.ivFolder.setOnClickListener {

        }
        binding.ivScreenOrientation.setOnClickListener {
            changeScreenOriantation()
        }
    }

    private fun downloadFile(document: DocumentLocal) {
        showConfirmDialog(
            document.nameDecrypted().replaceAfter(".", ""),
            "Ushbu faylni tortib olishga ishonchingiz komilmi ?"
        ) {
            if (it) {
                vm.downloadDocument(document)
            }
        }
    }

    private fun showFile(document: DocumentLocal) {
        val selectedFile: File = requireContext().getFileStreamPath(document.id)
        if (selectedFile.exists()) {
            if (document.type == TYPE_PDF) {
                ScreenPdfView.currentId = document.id
                findNavController().navigate(
                    R.id.action_screenAllDocuments_to_screenDocument
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

    @SuppressLint("SetTextI18n")
    override fun onBind(
        holder: View,
        position: Int,
        item: NodeViewData<DocumentForRv>,
        bundle: Bundle?
    ) {
        val folderName = holder.findViewById<TextView>(R.id.tv_folder_name)
        val fileSize = holder.findViewById<TextView>(R.id.tv_file_size)
        val ivFolder = holder.findViewById<ImageView>(R.id.iv_folder)
        val ivFileState = holder.findViewById<ImageView>(R.id.iv_file_state)
        val lItemTop = holder.findViewById<LinearLayout>(R.id.l_item_top)
        val vLine = holder.findViewById<View>(R.id.v_line)

        val l_progress = holder.findViewById<LinearLayout>(R.id.l_progress)
        val tv_progress_text = holder.findViewById<TextView>(R.id.tv_progress_text)
        val progressbar = holder.findViewById<ProgressBar>(R.id.progressbar)

        val data = item.getData()
        var name = data.nameDecrypted()
        fileSize.text = data.size.validateFileSize()

        if (data.parentId == MOTHER_ID) {
            vLine.visibility = View.VISIBLE
        } else {
            vLine.visibility = View.GONE
        }
        if (item.isLeaf) {

        } else {

        }
        vm.resultDownload.observe(viewLifecycleOwner) { document ->
            if (document.id == data.id) {
                if (document.loading) {
                    l_progress.visibility = View.VISIBLE
                    val total = if (document.size == 0L) 1 else document.size
                    val percent = (document.loadingBytes * 100 / total).toInt()
                    tv_progress_text.text =
                        "$percent %  ( ${document.loadingBytes.validateFileSize()} / ${document.size.validateFileSize()} )"
                    progressbar.setProgress(percent, true)
                } else {
                    l_progress.visibility = View.GONE
                    if (document.loaded) {
                        ivFileState.setImageResource(R.drawable.ic_checked)
                        listTempDocuments.add(document)
                    }
                }
            }

        }
        if (data.type == TYPE_FOLDER) {
            val count = if ((data.count ?: 0) > 0) "(${data.count})" else ""
            folderName.text = "${name}$count"
            ivFileState.visibility = View.GONE
            fileSize.visibility = View.GONE
            if (item.isExpanded) {
                ivFolder.setImageResource(R.drawable.ic_open_folder)
            } else {
                ivFolder.setImageResource(R.drawable.ic_folder)
            }
        } else {
            folderName.text = name.substringBeforeLast(".")
            ivFileState.visibility = View.VISIBLE
            if (data.loaded) {
                ivFileState.setImageResource(R.drawable.ic_checked)
            } else {
                ivFileState.setImageResource(R.drawable.ic_file_download)
                ivFileState.setOnClickListener {
                    downloadFile(data.toDocumentLocal())
                }
            }
            if (data.type == TYPE_PDF) {
                ivFolder.setImageResource(R.drawable.ic_pdf)
            } else if (data.type == TYPE_TXT) {
                ivFolder.setImageResource(R.drawable.ic_txt)
            } else if (data.type == TYPE_IMAGE) {
                ivFolder.setImageResource(R.drawable.ic_image)
            }
            fileSize.visibility = View.VISIBLE
            fileSize.text = data.size.validateFileSize()
        }
        val tempDocument = listTempDocuments.find { it.id == data.id }
        tempDocument?.let {
            ivFileState.setImageResource(R.drawable.ic_checked)
        }

        //select node
        lItemTop.setOnClickListener {
            treeView?.selectNode(item.nodeId, !item.isSelected) // will trigger onNodeSelected
            if (listTempDocuments.map { it.id }.contains(data.id)) {
                showFile(data.toDocumentLocal())
            }
            if (data.type != TYPE_FOLDER && data.loaded) {
                showFile(data.toDocumentLocal())
            }

            if (item.isExpanded) {
                treeView?.collapseNode(item.nodeId)
            } else {
                treeView?.expandNode(item.nodeId)
            }
        }


        //toggle node
        holder.setOnClickListener {
            if (item.isExpanded) {
                treeView?.collapseNode(item.nodeId)
            } else {
                treeView?.expandNode(item.nodeId)
            }
        }
    }

    override fun onNodeSelected(
        node: NodeViewData<DocumentForRv>,
        child: List<NodeViewData<DocumentForRv>>,
        isSelected: Boolean
    ) {
        if (!isSelected) return // prevent unselect node

        treeView?.clearNodesSelected()
        treeView?.setSelectedNode(listOf(node), isSelected)
        treeView?.requestUpdateTree()
    }

    override fun onPause() {
        super.onPause()
        if (listTempDocuments.isNotEmpty()) {
            vm.saveTempDocumentsToLocalDb(listTempDocuments)
        }
    }
}