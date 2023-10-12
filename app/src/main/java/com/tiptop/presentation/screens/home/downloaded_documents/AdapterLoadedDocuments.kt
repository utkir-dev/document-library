package com.tiptop.presentation.screens.home.downloaded_documents

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tiptop.R
import com.tiptop.app.common.Constants
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.Constants.TYPE_IMAGE
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.Constants.TYPE_TXT
import com.tiptop.app.common.Encryptor
import com.tiptop.app.common.Utils
import com.tiptop.app.common.collapse
import com.tiptop.app.common.expand
import com.tiptop.app.common.validateFileSize
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.databinding.ItemDocumentTreeBinding
import java.nio.charset.StandardCharsets


open class AdapterLoadedDocuments(val listener: ClickListener) :
    ListAdapter<DocumentForRv, AdapterLoadedDocuments.Vh>(MyDiffUtil()) {
    inner class Vh(val v: ItemDocumentTreeBinding) : RecyclerView.ViewHolder(v.root) {

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun onBind(document: DocumentForRv, position: Int) {
            var name = if (document.name.contains("."))
                document.name.substringBeforeLast(".")
            else document.name
            when (document.type) {
                TYPE_FOLDER -> {
                    v.tvFileSize.visibility = View.GONE
                    v.ivFolder.setImageResource(R.drawable.ic_folder)
                    name = "$name(${document.count})"
                }

                TYPE_PDF -> {
                    v.ivFolder.setImageResource(R.drawable.ic_pdf)
                    v.tvFileSize.visibility = View.VISIBLE
                    v.tvFileSize.text = document.size.validateFileSize()
                }

                TYPE_TXT -> {
                    v.ivFolder.setImageResource(R.drawable.ic_txt)
                    v.tvFileSize.visibility = View.VISIBLE
                    v.tvFileSize.text = document.size.validateFileSize()
                }

                TYPE_IMAGE -> {
                    v.ivFolder.setImageResource(R.drawable.ic_image)
                    v.tvFileSize.visibility = View.VISIBLE
                    v.tvFileSize.text = document.size.validateFileSize()
                }
            }

            v.tvFolderName.text = name
            v.tvFileSize.text = document.size.validateFileSize()
            v.ivFileState.visibility = View.GONE

            v.lItemTop.setOnClickListener {
                if (document.type == 0) {
                    if (v.rvDocuments.visibility == View.GONE) {
                        listener.onClickFolder(document, v.rvDocuments)
                        v.ivFolder.setImageResource(R.drawable.ic_open_folder)
                        expand(v.rvDocuments)
                    } else {
                        v.ivFolder.setImageResource(R.drawable.ic_folder)
                        collapse(v.rvDocuments)
                    }
                } else {
                    listener.onClickFile(document, v.lItemTop)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(
            ItemDocumentTreeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position), position)

    }

    interface ClickListener {
        fun onClickFile(document: DocumentForRv, v: View)
        fun onClickFolder(document: DocumentForRv, rv: RecyclerView)
    }

    class MyDiffUtil : DiffUtil.ItemCallback<DocumentForRv>() {
        override fun areItemsTheSame(oldItem: DocumentForRv, newItem: DocumentForRv): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DocumentForRv, newItem: DocumentForRv): Boolean {
            return oldItem.equals(newItem)
        }
    }

}