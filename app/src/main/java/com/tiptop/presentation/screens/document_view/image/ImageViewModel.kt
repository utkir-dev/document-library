package com.tiptop.presentation.screens.document_view.image

import androidx.lifecycle.LiveData
import com.tiptop.data.models.local.DocumentLocal

interface ImageViewModel {
    val currentDocument: LiveData<DocumentLocal>
    fun setDocument(id: String)
}