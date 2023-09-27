package com.tiptop.presentation.screens.document_view.pdf

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tiptop.R
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.UzArabBase
import com.tiptop.databinding.ItemDictBinding


class AdapterDictionary(
    var searchingText: String = "",
    val listener: PageNumberClickListener
) : ListAdapter<Dictionary, AdapterDictionary.Vh>(MyDiffUtil()) {
    inner class Vh(val v: ItemDictBinding) : RecyclerView.ViewHolder(v.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(word: Dictionary, position: Int) {
            if (word is ArabUzBase) {
                v.tvPageNumber.visibility = View.GONE
                v.tvSearchedWord.text = word.c0arab
                v.tvDictWord.text = word.c2uzbek
                if (word.saved) {
                    v.ivSavedWord.visibility = View.VISIBLE
                    v.ivSavedWord.setImageResource(R.drawable.star1)
                } else {
                    v.ivSavedWord.visibility = View.GONE
                }
            } else if (word is ArabUzUser) {
                v.tvSearchedWord.text = word.c0arab
                v.tvDictWord.text = word.c2uzbek
                v.tvPageNumber.setOnClickListener {
                    listener.onClickPage(word.pageNumber)
                }
                if (position < 1) {
                    v.tvPageNumber.visibility = View.VISIBLE
                    v.tvPageNumber.text = "${word.pageNumber + 1}-bet"
                } else {
                    if (word.pageNumber > (getItem(position - 1) as ArabUzUser).pageNumber) {
                        v.tvPageNumber.visibility = View.VISIBLE
                        v.tvPageNumber.text = "${word.pageNumber + 1}-bet"
                    } else {
                        v.tvPageNumber.visibility = View.GONE
                    }
                }

            } else if (word is UzArabBase) {
                v.tvPageNumber.visibility = View.GONE
                if (searchingText.isNotEmpty() && word.c0uzbek.contains(
                        searchingText,
                        ignoreCase = true
                    )
                ) {
                    val startIndex = 0// word.c0uzbek.indexOf(searchingText, ignoreCase = true)
                    val spannableText = SpannableString(word.c0uzbek)
                    spannableText.setSpan(
                        ForegroundColorSpan(Color.RED),
                        startIndex, startIndex + searchingText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    v.tvSearchedWord.text = spannableText
                } else {
                    v.tvSearchedWord.text = word.c0uzbek
                }
                v.tvDictWord.text = word.c1arab
            }
            v.root.setOnClickListener {
                listener.onClick(word)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemDictBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position), position)

    }

    interface PageNumberClickListener {
        fun onClick(word: Dictionary)
        fun onClickPage(page: Int)
    }

    class MyDiffUtil : DiffUtil.ItemCallback<Dictionary>() {
        override fun areItemsTheSame(oldItem: Dictionary, newItem: Dictionary): Boolean {
            var response = false
            if (oldItem is ArabUzBase && newItem is ArabUzBase) {
                response = oldItem.docid == newItem.docid
            } else if (oldItem is ArabUzUser && newItem is ArabUzUser) {
                response = oldItem.docid == newItem.docid
            }
            return response
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Dictionary, newItem: Dictionary): Boolean {
            var response = false
            if (oldItem is ArabUzBase && newItem is ArabUzBase) {
                response = oldItem == newItem
            } else if (oldItem is ArabUzUser && newItem is ArabUzUser) {
                response = oldItem == newItem
            }
            return response
        }
    }
}