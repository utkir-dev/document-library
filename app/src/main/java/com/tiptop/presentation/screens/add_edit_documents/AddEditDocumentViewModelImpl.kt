package com.tiptop.presentation.screens.add_edit_documents

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.Resource
import com.tiptop.app.common.ResponseResult
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DocumentRemote
import com.tiptop.domain.AddEditDocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditDocumentViewModelImpl @Inject constructor(
    private val repository: AddEditDocumentRepository
) : ViewModel(), AddEditDocumentViewModel {
    private val _resultUpdate = MutableLiveData(Resource.default(false))
    override val resultUpdate: LiveData<Resource<Boolean>> = _resultUpdate

    private val _resultDelete = MutableLiveData(Resource.default(false))
    override val resultDelete: LiveData<Resource<Boolean>> = _resultDelete

    override val documents = MutableLiveData<List<DocumentLocal>>()
    private val _documentsForRv = MutableLiveData<List<DocumentForRv>>()
    override val documentsForRv: LiveData<List<DocumentForRv>> = _documentsForRv
    override fun getDocumentsByParentId(parentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDocumentsByParentId(parentId).collectLatest {
                val list = it.map { it.toRvModel() }
                //documents.postValue(it)
                _documentsForRv.postValue(list)
                async {
                    list.forEach {
                        val count = repository.getChildsCountByParentId(it.parentId + it.id)
                        it.count = count
                    }
                }.await()
                _documentsForRv.postValue(list)
            }
        }
    }

    override fun getDocumentsForRv(parentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDocumentsForRv(parentId).collectLatest {
                _documentsForRv.postValue(it)
            }
        }
    }

    fun observe() {
        viewModelScope.launch(Dispatchers.IO) {
            async { repository.observeDocuments().collect() }
        }
    }

    override fun searchDocument(seachText: String) {
        viewModelScope.launch(Dispatchers.IO) {


        }
    }

    override fun saveDocument(document: DocumentRemote, byteArray: ByteArray?) {
        _resultUpdate.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = repository.saveRemoteDocument(document)
            if (result is ResponseResult.Success) {
                _resultUpdate.postValue(Resource.success(true))
                byteArray?.let { uploadFile(it, document) }
            } else {
                _resultUpdate.postValue(Resource.error(false, "Xatolik"))
            }
        }
    }

    override fun downloadDocument(document: DocumentLocal) {
        viewModelScope.launch(Dispatchers.IO) {
            async { repository.downloadFile(document) }
        }
    }

    private fun uploadFile(byteArray: ByteArray, document: DocumentRemote) {
        viewModelScope.launch(Dispatchers.IO) {
            async { repository.uploadFile(byteArray, document) }
        }
    }

    override fun deleteDocument(document: DocumentLocal) {
        _resultDelete.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = repository.deleteDocument(document)
            if (result is ResponseResult.Success) {
                _resultDelete.postValue(Resource.success(true))
            } else {
                _resultDelete.postValue(Resource.error(false, "Xatolik"))
            }
        }
    }
}