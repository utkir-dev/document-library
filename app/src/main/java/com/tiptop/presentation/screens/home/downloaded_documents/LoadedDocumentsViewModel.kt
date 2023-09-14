package com.tiptop.presentation.screens.home.downloaded_documents

import androidx.lifecycle.LiveData
import com.tiptop.data.models.local.DocumentForRv

interface LoadedDocumentsViewModel {
    val isFolder: LiveData<Boolean>
    val documents: LiveData<List<DocumentForRv>>
    fun getDocumentsWithoutFolders()
    fun getDocumentsWithFolders()
}