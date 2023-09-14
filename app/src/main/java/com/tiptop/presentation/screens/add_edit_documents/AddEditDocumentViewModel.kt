package com.tiptop.presentation.screens.add_edit_documents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tiptop.app.common.Resource
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DocumentRemote

interface AddEditDocumentViewModel {
    val resultUpdate: LiveData<Resource<Boolean>>
    val resultDelete: LiveData<Resource<Boolean>>
    val documentsForRv: LiveData<List<DocumentForRv>>
    val childDocument1: LiveData<DocumentLocal>
    val childDocument2: LiveData<DocumentLocal>
    fun getDocumentsByParentId(parentId: String)
    fun searchDocument(seachText: String)
    fun saveDocument(
        document: DocumentRemote,
        headByteArray: ByteArray?=null,
        bodyByteArray: ByteArray?=null
    )

    fun downloadDocument(document: DocumentLocal)
    fun deleteDocument(document: DocumentLocal)
}