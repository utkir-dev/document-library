package com.tiptop.presentation.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersPagerViewModelImpl @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    fun init() {
        viewModelScope.launch {
            async {
                repository.observeUsers().collect()
            }
            async {
                repository.observeDevices().collect()
            }
        }
    }
}