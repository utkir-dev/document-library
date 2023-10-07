package com.tiptop.presentation.screens.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Constants
import com.tiptop.domain.AuthRepository
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModelIml @Inject constructor(
    private val auth: AuthRepository,
    private val remoteRepository: UserRepository
) : ViewModel() {
    private val _state = MutableLiveData<Int>(-1)
    val state: LiveData<Int> = _state

    init {
        viewModelScope.launch {
            val isAllowed = auth.currentUser != null
            delay(1)
            if (isAllowed) {
                init()
                _state.postValue(1)
            } else {
                _state.postValue(0)
            }
        }
    }

    private fun init() {
        viewModelScope.async {
            async { remoteRepository.updateDateRemoteUser() }
            async { remoteRepository.updateDateRemoteDevice() }
        }
    }
}