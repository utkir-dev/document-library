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
    val documents: MutableLiveData<List<DocumentLocal>>
    val documentsForRv: LiveData<List<DocumentForRv>>
    fun getDocumentsByParentId(parentId: String)
    fun searchDocument(seachText: String)
    fun saveDocument(document: DocumentRemote, byteArray: ByteArray?=null)
    fun downloadDocument(document: DocumentLocal)
    fun deleteDocument(document: DocumentLocal)
    fun getDocumentsForRv(parentId: String)

}