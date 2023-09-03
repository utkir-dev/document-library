package com.tiptop.presentation.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tiptop.app.common.Resource
import com.tiptop.domain.AuthRepository
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val remoteRepository: UserRepository

) : ViewModel(), HomeViewModel {
    private val _result = MutableLiveData(Resource.default(false))
    val result: LiveData<Resource<Boolean>> = _result


}