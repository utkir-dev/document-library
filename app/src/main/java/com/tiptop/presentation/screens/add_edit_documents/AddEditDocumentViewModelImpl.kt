package com.tiptop.presentation.screens.add_edit_documents

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Constants
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.Resource
import com.tiptop.app.common.ResponseResult
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DocumentRemote
import com.tiptop.domain.DocumentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditDocumentViewModelImpl @Inject constructor(
    private val repository: DocumentsRepository
) : ViewModel(), AddEditDocumentViewModel {
    private val _resultUpdate = MutableLiveData(Resource.default(false))
    override val resultUpdate: LiveData<Resource<Boolean>> = _resultUpdate

    private val _resultDelete = MutableLiveData(Resource.default(false))
    override val resultDelete: LiveData<Resource<Boolean>> = _resultDelete

    private val _folderNames = MutableLiveData<List<String>>()
    val folderNames: LiveData<List<String>> = _folderNames

    private val _documentNames = MutableLiveData<List<String>>()
    val documentNames: LiveData<List<String>> = _documentNames

    private val _childDocument1 = MutableLiveData<DocumentLocal>()
    override val childDocument1: LiveData<DocumentLocal> = _childDocument1

    private val _childDocument2 = MutableLiveData<DocumentLocal>()
    override val childDocument2: LiveData<DocumentLocal> = _childDocument2

    private val _documentsForRv = MutableLiveData<List<DocumentForRv>>()
    override val documentsForRv: LiveData<List<DocumentForRv>> = _documentsForRv


    init {
        viewModelScope.async(Dispatchers.IO) {
            repository.getAllDocuments().collectLatest {
                _folderNames.postValue(it.filter { it.type == TYPE_FOLDER }
                    .map { it.nameDecrypted().lowercase() })
                _documentNames.postValue(it.filter { it.type != TYPE_FOLDER }
                    .map { it.nameDecrypted().substringBeforeLast(".").lowercase() })
            }
        }
    }

    fun setChildDocument1(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDocumentByIdFlow(id).collectLatest {
                _childDocument1.postValue(it)
            }
        }
    }

    fun setChildDocument2(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDocumentByIdFlow(id).collectLatest {
                _childDocument2.postValue(it)
            }
        }
    }

    override fun getDocumentsByParentId(parentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDocumentsByParentId(parentId).collectLatest {
                val list = it.map { it.toRvModel() }
                _documentsForRv.postValue(list)
                async {
                    list.forEach {
                        val count = repository.getChildsCountByParentId(it.parentId + it.id)
                        it.count = count
                    }
//----------------------------------------------------------------------------

//                    list.forEach { parent ->
//                        val listChild1 = repository.getChildDocuments(parent.parentId + parent.id).map { it.toRvModel() }
//
//                        listChild1.forEach { child1 ->
//                            val listChild2 =
//                                repository.getChildDocuments(child1.parentId + child1.id).map { it.toRvModel() }
//
//                            child1.count = listChild2.filter { it.type > 0 }.size
//                            child1.countNewDocuments =
//                                listChild2.filter { (System.currentTimeMillis() - it.dateAdded < Constants.NEW_DOCUMENTS_VISIBILITY_PERIOD) && it.type > 0 }.size
//                            child1.child = listChild2
//                        }
//
//                        parent.count = listChild1.filter { it.type > 0 }.size+listChild1.sumOf { it.count }
//                        parent.countNewDocuments =listChild1.sumOf { it.countNewDocuments } +
//                                listChild1.filter {(System.currentTimeMillis() - it.dateAdded < Constants.NEW_DOCUMENTS_VISIBILITY_PERIOD) && it.type > 0 }.size
//                        parent.child = listChild1
//                    }
                }.await()
                _documentsForRv.postValue(list)
            }
        }
    }

    override fun searchDocument(seachText: String) {
        viewModelScope.launch(Dispatchers.IO) {


        }
    }

    override fun saveDocument(
        document: DocumentRemote, headByteArray: ByteArray?, bodyByteArray: ByteArray?
    ) {
        _resultUpdate.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = repository.saveRemoteDocument(document)
            if (result is ResponseResult.Success) {
                _resultUpdate.postValue(Resource.success(true))
                async { headByteArray?.let { uploadHeadFile(it, document) } }
                async { bodyByteArray?.let { uploadFile(it, document) } }
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

    private fun uploadHeadFile(byteArray: ByteArray, document: DocumentRemote) {
        viewModelScope.launch(Dispatchers.IO) {
            async { repository.uploadHeadFile(byteArray, document) }
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