package com.tiptop.presentation.screens.users.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Resource
import com.tiptop.app.common.ResponseResult
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.UserLocal
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountsViewModelImpl @Inject constructor(
    private val repository: UserRepository
) : ViewModel(), AccountsViewModel {

    private val _resultUpdateDevice = MutableLiveData(Resource.default(false))
    val resultUpdateDevice: LiveData<Resource<Boolean>> = _resultUpdateDevice
    private val _resultUpdateUser = MutableLiveData(Resource.default(false))
    val resultUpdateUser: LiveData<Resource<Boolean>> = _resultUpdateUser
    private val _resultDelete = MutableLiveData(Resource.default(false))
    val resultDelete: LiveData<Resource<Boolean>> = _resultDelete

    private val _resultPassword = MutableLiveData(Resource.default(""))
    val resultPassword: LiveData<Resource<String>> = _resultPassword

    val userAndDevices =
        MutableLiveData<Map<UserLocal, List<DeviceLocal>>>()// repository.getUserAndDevices()

    fun observeDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserAndDevices().collectLatest {
                userAndDevices.postValue(it)
            }
        }
    }

    fun searchUser(seachText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserAndDevices(seachText).collectLatest {
                userAndDevices.postValue(it)
            }
        }
    }

    override fun updateUser(user: UserLocal) {
        _resultUpdateUser.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = repository.saveRemoteUser(user.toRemote())
            if (result is ResponseResult.Success) {
                _resultUpdateUser.postValue(Resource.success(true))
            } else {
                _resultUpdateUser.postValue(Resource.error(false, "Xatolik"))
            }
        }
    }

    override fun deleteUser(user: UserLocal) {
        _resultDelete.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = repository.deleteUser(user)
            if (result is ResponseResult.Success) {
                _resultDelete.postValue(Resource.success(true))
            } else {
                _resultDelete.postValue(Resource.error(false, "Xatolik"))
            }
        }
    }

    override fun updateDevice(device: DeviceLocal) {
        _resultUpdateDevice.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = repository.saveRemoteDevice(device.toRemote())
            if (result is ResponseResult.Success) {
                _resultUpdateDevice.postValue(Resource.success(true))
            } else {
                _resultUpdateDevice.postValue(Resource.error(false, "Xatolik"))
            }
        }
    }

    override fun deleteDevice(deviceId: String) {
        _resultDelete.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = repository.deleteDevice(deviceId)
            if (result is ResponseResult.Success) {
                _resultDelete.postValue(Resource.success(true))
            } else {
                _resultDelete.postValue(Resource.error(false, "Xatolik"))
            }
        }
    }

    override fun getUserPassword(userId: String) {
        _resultPassword.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = repository.getUserPassword(userId)
            if (result is ResponseResult.Success) {
                _resultPassword.postValue(Resource.success(result.data))
            } else {
                _resultPassword.postValue(Resource.error("", "Xatolik"))
            }
        }
    }
}