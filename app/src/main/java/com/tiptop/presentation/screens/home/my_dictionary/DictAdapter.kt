package com.tiptop.presentation.screens.home.my_dictionary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tiptop.app.common.decryption
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.ArabUzUserForDictionaryScreen
import com.tiptop.databinding.ItemDictBinding
import com.tiptop.presentation.screens.document_view.pdf.Dictionary

class DictAdapter(private var listener: PageNumberClickListener) :
    PagingDataAdapter<Dictionary, DictAdapter.ViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Dictionary>() {
            override fun areItemsTheSame(oldItem: Dictionary, newItem: Dictionary): Boolean =
                oldItem.docid == newItem.docid

            override fun areContentsTheSame(oldItem: Dictionary, newItem: Dictionary): Boolean {
                var response = false
                if (oldItem is ArabUzBase && newItem is ArabUzBase) {
                    response = oldItem == newItem
                } else if (oldItem is ArabUzUserForDictionaryScreen && newItem is ArabUzUserForDictionaryScreen) {
                    response = oldItem == newItem
                }
                return response
            }
        }
    }

    inner class ViewHolder(val binding: ItemDictBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDictBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = getItem(position) as ArabUzUserForDictionaryScreen
        holder.binding.apply {
            tvSearchedWord.text = word?.c0arab
            tvDictWord.text = word?.c2uzbek
            tvDictWord.setOnClickListener {
                listener.onClick(word)
            }
            tvPageNumber.visibility = View.VISIBLE
            tvPageNumber.text =
                "${word?.pageNumber ?: 1}-bet.${word.documentName.decryption(word.dateAdded)}"
        }
    }

    interface PageNumberClickListener {
        fun onClick(word: Dictionary)
    }
}