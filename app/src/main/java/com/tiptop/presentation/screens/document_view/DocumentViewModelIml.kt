package com.tiptop.presentation.screens.document_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Constants.HEAD
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.Encryptor
import com.tiptop.app.common.Utils
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.domain.DocumentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class DocumentViewModelIml @Inject constructor(
    private val repository: DocumentsRepository
) : ViewModel() {
    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _currentDocument = MutableLiveData(DocumentLocal())
    val currentDocument: LiveData<DocumentLocal> = _currentDocument

    private val _documentBytes = MutableLiveData<ByteArray?>()
    val documentBytes: LiveData<ByteArray?> = _documentBytes
    fun setDocument(id: String) {
        viewModelScope.async(Dispatchers.IO) {
            repository.getDocumentByIdFlow(id).collectLatest {

                _currentPage.postValue(it.lastSeenPage)
                _currentDocument.postValue(it)
                getFileBytes(it)

            }
        }
    }

    fun getLastSeenDocuments() {
        viewModelScope.async(Dispatchers.IO) {
            repository.getLastSeenDocuments().collectLatest {

            }
        }
    }

    fun updateDocument(document: DocumentLocal) {
        viewModelScope.async(Dispatchers.IO) {
            repository.saveTempDocumentsToLocalDb(listOf(document))
        }
    }

    private fun getFileBytes(document: DocumentLocal) {
        viewModelScope.async(Dispatchers.IO) {
            if (document.type == TYPE_PDF) {
                combine(
                    repository.getFileBytes(HEAD + document.id),
                    repository.getFileBytes(document.id)
                ) { headEncryptedBytes, bodyBytes ->
                    bodyBytes?.let { body ->
                        headEncryptedBytes?.let { head ->
                            Encryptor().getDecryptedBytes(
                                Utils().getKeyStr(document.dateAdded),
                                Utils().getSpecStr(document.dateAdded),
                                head
                            ) {
                                it?.let { _documentBytes.postValue(it.plus(body)) }
                            }
                        }
                    }
                }.collect()

            } else {
                repository.getFileBytes(document.id).collectLatest { encryptedBytes ->
                    encryptedBytes?.let { data ->
                        Encryptor().getDecryptedBytes(
                            Utils().getKeyStr(document.dateAdded),
                            Utils().getSpecStr(document.dateAdded),
                            data
                        ) { decryptedData ->
                            decryptedData?.let { _documentBytes.postValue(it) }
                        }
                    }
                }
            }
        }
    }
}