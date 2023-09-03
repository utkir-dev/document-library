package com.tiptop.presentation.screens.users.accounts

import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.UserLocal

interface AccountsViewModel {
    fun updateUser(user: UserLocal)
    fun deleteUser(user: UserLocal)
    fun updateDevice(device: DeviceLocal)
    fun deleteDevice(deviceId: String)
    fun getUserPassword(userId: String)
}