package com.tiptop.presentation.screens.users.devices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tiptop.app.common.Resource
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.UserLocal

interface DevicesViewModel {
    val resultUpdate: LiveData<Resource<Boolean>>
    val resultDelete: LiveData<Resource<Boolean>>
    val users: MutableLiveData<List<UserLocal>>

    val devices: MutableLiveData<List<DeviceLocal>>
    fun observeDevices()
    fun searchDevice(seachText: String)
    fun searchUsersByDeviceId(deviceId: String)
    fun updateDevice(device: DeviceLocal)
    fun deleteDevice(deviceId: String)
}