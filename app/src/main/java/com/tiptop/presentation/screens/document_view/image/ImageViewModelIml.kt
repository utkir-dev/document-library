package com.tiptop.presentation.screens.document_view.image

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Constants
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
class ImageViewModelIml @Inject constructor(
    private val repository: DocumentsRepository
) : ViewModel(), ImageViewModel {


    private val _currentDocument = MutableLiveData(DocumentLocal())
    override val currentDocument: LiveData<DocumentLocal> = _currentDocument


    private val _documentBytes = MutableLiveData<ByteArray?>()
    val documentBytes: LiveData<ByteArray?> = _documentBytes
    override fun setDocument(id: String) {
        viewModelScope.async(Dispatchers.IO) {
            repository.getDocumentByIdFlow(id).collectLatest {
                _currentDocument.postValue(it)
                getFileBytes(it)

            }
        }
    }

    private fun getFileBytes(document: DocumentLocal) {
        viewModelScope.async(Dispatchers.IO) {
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