package com.tiptop.presentation.screens.users.devices

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
class AdminDevicesViewModelImpl @Inject constructor(
    private val repository: UserRepository
) : ViewModel(), DevicesViewModel {

    private val _resultUpdate = MutableLiveData(Resource.default(false))
    override val resultUpdate: LiveData<Resource<Boolean>> = _resultUpdate

    private val _resultDelete = MutableLiveData(Resource.default(false))
    override val resultDelete: LiveData<Resource<Boolean>> = _resultDelete

    override val users = MutableLiveData<List<UserLocal>>()
    override val devices = MutableLiveData<List<DeviceLocal>>()

    override fun observeDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAdminDevices().collectLatest {
                devices.postValue(it)
            }
        }
    }

    override fun searchDevice(seachText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSearchedAdminDevices(seachText).collectLatest {
                devices.postValue(it)
            }
        }
    }

    override fun searchUsersByDeviceId(deviceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUsersByDeviceid(deviceId).collectLatest {
                users.postValue(it)
            }
        }
    }

    override fun updateDevice(device: DeviceLocal) {
        _resultUpdate.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = repository.saveRemoteDevice(device.toRemote())
            if (result is ResponseResult.Success) {
                _resultUpdate.postValue(Resource.success(true))
            } else {
                _resultUpdate.postValue(Resource.error(false, "Xatolik"))
            }
        }
    }

    override fun deleteDevice(deviceId: String) {

    }
}