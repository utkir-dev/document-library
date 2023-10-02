package com.tiptop.presentation.screens.home.downloaded_documents

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.domain.DocumentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadedDocumentsViewModelImpl @Inject constructor(
    private val repository: DocumentsRepository
) : ViewModel(), LoadedDocumentsViewModel {


    private val _isFolder = MutableLiveData(true)
    override val isFolder: LiveData<Boolean> = _isFolder

    private val _documents = MutableLiveData<List<DocumentForRv>>()
    override val documents: LiveData<List<DocumentForRv>> = _documents


    override fun getDocumentsWithoutFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getLoadedDocuments().collectLatest {
                val list = it.filter { it.type > 0 }.map { it.toRvModel() }
                _documents.postValue(list)
            }
        }
    }

    override fun getDocumentsWithFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getLoadedDocuments().collectLatest {
                val list = it.map { it.toRvModel() }
                _documents.postValue(list)
                async {
                    list.forEach {
                        val count = repository.getLoadedChildsCountByParentId(it.parentId + it.id)
                        it.count = count
                    }
                }.await()
                _documents.postValue(list)
            }
        }
    }

    fun initFolder() {
        viewModelScope.launch {
            if (_isFolder.value == true) {
                _isFolder.postValue(false)
            } else {
                _isFolder.postValue(true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("viewmodelOnCleared","LoadedDocumentsViewmodel cleared")

    }
}