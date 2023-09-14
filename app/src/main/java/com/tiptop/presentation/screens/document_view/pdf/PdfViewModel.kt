package com.tiptop.presentation.screens.document_view.pdf

import androidx.lifecycle.LiveData
import com.tiptop.data.models.local.DocumentLocal

interface PdfViewModel {
    val screenBlockState: LiveData<Boolean>
    val nightMode: LiveData<Boolean>
    val fullScreen: LiveData<Boolean>
    val currentPage: LiveData<Int>
    val currentDocument: LiveData<DocumentLocal>


    fun setCurrentPage(page: Int)
    fun setDocument(id: String)
    fun changeFullScreen()
    fun changeNightMode()
    fun updateDocument()
    val lastDocuments: LiveData<List<DocumentLocal>>
}