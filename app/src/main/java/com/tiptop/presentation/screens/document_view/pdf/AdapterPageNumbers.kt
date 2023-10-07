package com.tiptop.presentation.screens.document_view.pdf

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tiptop.databinding.ItemPageNumberBinding


class AdapterPageNumbers(val list: List<String>, val listener: PageNumberClickListener) :
    RecyclerView.Adapter<AdapterPageNumbers.Vh>() {
    inner class Vh(val v: ItemPageNumberBinding) : RecyclerView.ViewHolder(v.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(value: String, position: Int) {
            v.tvMark.text = value
            v.root.setOnClickListener {
                listener.onClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemPageNumberBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position], position)

    }

    override fun getItemCount(): Int = list.size

    interface PageNumberClickListener {
        fun onClick(value: Int)
    }
}