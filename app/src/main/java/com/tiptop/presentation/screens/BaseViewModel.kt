package com.tiptop.presentation.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Resource
import com.tiptop.domain.DocumentsRepository
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BaseViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentsRepository
) : ViewModel() {
    private val _result = MutableLiveData(Resource.default(false))
    val result: LiveData<Resource<Boolean>> = _result
    val currentUser = userRepository.getCurrentUserFlowable()
    val currentDevice = userRepository.getCurrentDeviceFlowable()

    fun initObservers() {
        viewModelScope.launch {
            userRepository.observeAuthState().collect {
                if (it) {
                    async { userRepository.observeUser().collect() }
                    async { userRepository.observeDevice().collect() }
                    async { userRepository.observeDeletedIds().collect() }
                    async { documentRepository.observeDocuments().collect() }
                    async { userRepository.checkLibVersion() }
                    //  async { repository.addFakeUsers()}
                }
            }
        }
    }

    fun clearDevices() {
        viewModelScope.launch {
            async { userRepository.clearDevices() }
        }
    }
}