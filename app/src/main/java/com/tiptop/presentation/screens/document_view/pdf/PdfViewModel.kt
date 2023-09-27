package com.tiptop.presentation.screens.document_view.pdf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.DocumentLocal
import kotlinx.coroutines.flow.Flow

interface PdfViewModel {
    val screenBlockState: LiveData<Boolean>
    val nightMode: LiveData<Boolean>
    val fullScreen: LiveData<Boolean>
    val currentPage: LiveData<Int>
    val currentDocument: LiveData<DocumentLocal>
    val lastDocuments: LiveData<List<DocumentLocal>>
    val words: LiveData<List<Dictionary>>

    fun setCurrentPage(page: Int)
    fun setDocument(id: String)
    fun changeFullScreen()
    fun changeNightMode()
    fun updateDocument()
    fun getWords(documentId: String)
    fun getSearchedBaseWords(searchText: String)
    fun updateBaseWord(word: Dictionary, pageNumber: Int, documentId: String)
}