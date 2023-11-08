package com.tiptop.presentation.screens.home.downloaded_documents

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Constants
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


    private val _isFolder = MutableLiveData(false)
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
}