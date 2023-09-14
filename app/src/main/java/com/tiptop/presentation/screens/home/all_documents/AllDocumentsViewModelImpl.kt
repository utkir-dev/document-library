package com.tiptop.presentation.screens.home.all_documents

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Resource
import com.tiptop.app.common.Status
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.local.UserLocal
import com.tiptop.domain.DocumentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllDocumentsViewModelImpl @Inject constructor(
    private val repository: DocumentsRepository
) : ViewModel(), AllDocumentsViewModel {


    private val _resultDownload = MutableLiveData(DocumentLocal())
    override val resultDownload: MutableLiveData<DocumentLocal> = _resultDownload

    override val documents = MutableLiveData<List<DocumentForRv>>()
    fun initDocumentsHome() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllDocuments().collectLatest {
                val list = it.map { it.toRvModel() }
                documents.postValue(list)
                async {
                    list.forEach { parent ->
                        val listChild1 = repository.getChildDocuments(parent.parentId + parent.id)
                            .map { it.toRvModel() }
                        listChild1.forEach { child1 ->
                            val listChild2 =
                                repository.getChildDocuments(child1.parentId + child1.id)
                                    .map { it.toRvModel() }
                            child1.count = listChild2.size
                            child1.child = listChild2
                        }
                        parent.count = listChild1.size
                        parent.child = listChild1
                    }
                }.await()
                documents.postValue(list)
            }

        }
    }

    override fun searchDocument(seachText: String) {
        viewModelScope.launch(Dispatchers.IO) {


        }
    }

    override fun downloadDocument(document: DocumentLocal) {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                repository.downloadFileLive(document)
                    .collect{ resource: Resource<DocumentLocal> ->
                        if (resource.data is DocumentLocal) {
                            _resultDownload.postValue(resource.data)
                        }
                    }
            }
        }
    }

    fun saveTempDocumentsToLocalDb(tempDocuments: List<DocumentLocal>) {
        viewModelScope.async(Dispatchers.IO) {
            repository.saveTempDocumentsToLocalDb(tempDocuments)
        }
    }
}