package com.tiptop.presentation.screens.add_edit_documents

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tiptop.R
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.Constants.TYPE_IMAGE
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.Constants.TYPE_TXT
import com.tiptop.app.common.validateFileSize
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.databinding.ItemDocumentBinding


open class AdapterDocument(val listener: ClickListener) :
    ListAdapter<DocumentForRv, AdapterDocument.Vh>(MyDiffUtil()) {
    private var selectedItemPosition = -1

    inner class Vh(val v: ItemDocumentBinding) : RecyclerView.ViewHolder(v.root) {

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun onBind(document: DocumentForRv, position: Int) {
            var name = document.name
            when (document.type) {
                TYPE_FOLDER -> {
                    v.tvFileSize.visibility = View.GONE
                    v.ivFolder.setImageResource(R.drawable.ic_folder)
                    if ((document.count?:0) > 0) {
                        name = "$name(${document.count})"
                    }
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
            v.tvFolderName.text = name.substringBeforeLast(".")
            v.tvFileSize.text = document.size.validateFileSize()
            if (document.loading) {
                v.lProgress.visibility = View.VISIBLE
                val total = if (document.size == 0L) 1 else document.size
                val percent = (document.loadingBytes * 100 / total).toInt()
                v.tvProgressText.text =
                    "$percent %  ( ${document.loadingBytes.validateFileSize()} / ${document.size.validateFileSize()} )"
                v.progressbar.progress = percent
            } else {
                v.lProgress.visibility = View.GONE
            }
            Log.d("downloadFile","AdapterDocument loaded: ${document.loaded}")

            if (document.type == TYPE_FOLDER) {
                v.ivFileState.visibility = View.GONE
                // listener.onClick(book, position, v.rvSubFolder)
            } else {
                v.ivFileState.visibility = View.VISIBLE
                if (document.loaded) {
                    v.ivFileState.setImageResource(R.drawable.ic_checked)
                    Log.d("downloadFile","AdapterDocument if loaded: ${document.loaded}")

                } else {
                    Log.d("downloadFile","AdapterDocument else loaded: ${document.loaded}")

                    v.ivFileState.setImageResource(R.drawable.ic_file_download)
                    v.ivFileState.setOnClickListener {
                        listener.onClick(document, position, v.ivFileState)
                    }
                }

            }
            v.lItemTop.setOnClickListener {
                selectedItemPosition = position
                notifyDataSetChanged()
                listener.onClick(document, position, v.lItemTop)
            }

            v.lItemTop.setOnLongClickListener {
                listener.onLongClick(document, position, v.lItemTop)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position), position)

    }

    interface ClickListener {
        fun onClick(document: DocumentForRv, position: Int, v: View)
        fun onLongClick(document: DocumentForRv, position: Int, v: View)
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