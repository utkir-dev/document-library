package com.tiptop.domain

import com.tiptop.app.common.Resource
import com.tiptop.app.common.ResponseResult
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DocumentRemote
import kotlinx.coroutines.flow.Flow

interface DocumentsRepository {
    suspend fun saveRemoteDocument(documentRemote: DocumentRemote): ResponseResult<Boolean>
    suspend fun saveTempDocumentsToLocalDb(tempDocuments:List<DocumentLocal>)
    suspend fun uploadFile(bytes: ByteArray, document: DocumentRemote)
    suspend fun uploadHeadFile(bytes: ByteArray, document: DocumentRemote)

    suspend fun downloadFile(document: DocumentLocal)
    suspend fun downloadFileLive(document: DocumentLocal): Flow<Resource<DocumentLocal>>
    suspend fun deleteDocument(document: DocumentLocal): ResponseResult<Boolean>
    suspend fun observeDocuments(): Flow<Boolean>
    fun getFileBytes(id:String): Flow<ByteArray?>
    fun getLoadedDocumentsCount(): Flow<Int>
    fun getAllDocumentsCount(): Flow<Int>
    fun getNewDocumentsCount(): Flow<Int>
    fun getLastSeenDocument(): Flow<DocumentLocal?>
    fun getDocuments(searchText: String): Flow<List<DocumentLocal>>
    fun getAllDocuments(): Flow<List<DocumentLocal>>
    fun getLoadedDocuments(): Flow<List<DocumentLocal>>
    fun getChildsCountByParentId(parentId: String): Int
    fun getDocumentsByParentId(parentId: String): Flow<List<DocumentLocal>>
    fun getChildDocuments(parentId: String): List<DocumentLocal>
    fun getDocumentByIdFlow(id: String): Flow<DocumentLocal>
    fun getLastSeenDocuments(): Flow<List<DocumentLocal>>
    suspend fun downloadHeadFile(document: DocumentLocal)
}