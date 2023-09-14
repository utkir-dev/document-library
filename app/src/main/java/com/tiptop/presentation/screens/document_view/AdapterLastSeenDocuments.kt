package com.tiptop.presentation.screens.document_view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.databinding.ItemLastDocumentBinding


class AdapterLastSeenDocuments(val list: List<DocumentLocal> =ArrayList(), val listener: PageNumberClickListener) :
    RecyclerView.Adapter<AdapterLastSeenDocuments.Vh>() {
    inner class Vh(val v: ItemLastDocumentBinding) : RecyclerView.ViewHolder(v.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(document: DocumentLocal) {
            v.tvDocumentName.text = document.name.substringBeforeLast(".")
            v.root.setOnClickListener {
                listener.onClick(document)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemLastDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])

    }

    override fun getItemCount(): Int = list.size

    interface PageNumberClickListener {
        fun onClick(document: DocumentLocal)
    }

}