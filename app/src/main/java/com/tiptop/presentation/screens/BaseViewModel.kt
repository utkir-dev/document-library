package com.tiptop.presentation.screens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Resource
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BaseViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {
    private val _result = MutableLiveData(Resource.default(false))
    val result: LiveData<Resource<Boolean>> = _result
    val currentUser = repository.getCurrentUserFlowable()
    val currentDevice = repository.getCurrentDeviceFlowable()

    fun initObservers() {
        viewModelScope.launch {
            Log.d("observeAuthState","observeAuthState start")
            repository.observeAuthState().collect {
                Log.d("observeAuthState","observeAuthState : $it")
                if (it){
                    async { repository.observeUser().collect() }
                    async { repository.observeDevice().collect() }
                    async { repository.observeDeletedIds().collect() }
                  //  async { repository.addFakeUsers()}
                }
            }
        }
    }

    fun clearDevices() {
        viewModelScope.launch {
            async { repository.clearDevices() }
        }
    }
}