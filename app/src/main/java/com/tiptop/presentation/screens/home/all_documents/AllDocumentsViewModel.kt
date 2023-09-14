package com.tiptop.presentation.screens.home.all_documents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal

interface AllDocumentsViewModel {
    val resultDownload: LiveData<DocumentLocal>
    val documents: MutableLiveData<List<DocumentForRv>>
    fun searchDocument(seachText: String)
    fun downloadDocument(document: DocumentLocal)

}