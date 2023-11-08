package com.tiptop.presentation.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Resource
import com.tiptop.data.models.local.LibVersion
import com.tiptop.domain.DictionaryRepository
import com.tiptop.domain.DocumentsRepository
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BaseViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentsRepository,
    private val dictionaryRepository: DictionaryRepository,

    ) : ViewModel() {
    private val _result = MutableLiveData(Resource.default(false))
    val result: LiveData<Resource<Boolean>> = _result

    private val _libVersion = MutableLiveData(LibVersion())
    val libVersion: LiveData<LibVersion> = _libVersion


    val currentUser = userRepository.getCurrentUserFlowable()
    val currentDevice = userRepository.getCurrentDeviceFlowable()

    fun initObservers() {
        viewModelScope.launch {
            userRepository.observeAuthState().collect {
                if (it && !initialized) {
                    val task1 = async { userRepository.observeUser().collect() }
                    val task2 = async { userRepository.observeDevice().collect() }
                    val task3 = async { documentRepository.observeDocuments().collect() }
                    val task4 = async {
                        documentRepository.checkLibVersion().collectLatest {
                            _libVersion.postValue(it)
                        }
                    }
                    val task5 = async { dictionaryRepository.checkRemoteDictionary() }
                    //  async { repository.addFakeUsers()}
                    task1.await()
                    task2.await()
                    task3.await()
                    task4.await()
                    task5.await()
                    initialized = true
                }
            }
        }
    }

    fun observeDeletedIds() {
        viewModelScope.async {
            if (!initializedDeletedIds)
                async { userRepository.observeDeletedIds().collect() }.await()
            initializedDeletedIds = true
        }
    }

    fun clearDevices() {
        viewModelScope.launch {
            async { userRepository.clearDevices() }
        }
    }

    companion object {
        private var initialized = false
        private var initializedDeletedIds = false
    }
}