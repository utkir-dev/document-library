package com.tiptop.domain

import com.tiptop.app.common.ResponseResult
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DocumentRemote
import kotlinx.coroutines.flow.Flow

interface AddEditDocumentRepository {
    suspend fun saveRemoteDocument(documentRemote: DocumentRemote): ResponseResult<Boolean>
    suspend fun uploadFile(bytes: ByteArray, document: DocumentRemote)
    suspend fun downloadFile(document: DocumentLocal)
    suspend fun deleteDocument(document: DocumentLocal): ResponseResult<Boolean>
    suspend fun observeDocuments(): Flow<Boolean>
    fun getDocuments(searchText: String): Flow<List<DocumentLocal>>
    fun getChildsCountByParentId(parentId: String): Int
    fun getDocumentsByParentId(parentId: String): Flow<List<DocumentLocal>>
    fun getDocumentsForRv(parentId: String): Flow<List<DocumentForRv>>
}