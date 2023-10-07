package com.tiptop.domain

import androidx.lifecycle.LiveData
import com.tiptop.app.common.ResponseResult
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.UserLocal
import com.tiptop.data.models.remote.DeviceRemote
import com.tiptop.data.models.remote.UserRemote
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun updateDateRemoteUser()
    suspend fun updateDateRemoteDevice()
    suspend fun saveRemoteUser(userRemote: UserRemote): ResponseResult<Boolean>
    suspend fun saveRemoteUserPassword(password: String, id: String): ResponseResult<Boolean>
    suspend fun saveRemoteDevice(remoteDevice: DeviceRemote): ResponseResult<Boolean>
    suspend fun deleteUser(user: UserLocal): ResponseResult<Boolean>
    suspend fun deleteDevice(deviceId: String): ResponseResult<Boolean>
    fun clearDevices()
    suspend fun getUserPassword(userId: String): ResponseResult<String>

    suspend fun observeAuthState():Flow<Boolean>
    suspend fun observeDevices(): Flow<Boolean>
    suspend fun observeDevice(): Flow<Boolean>
    suspend fun observeUsers(): Flow<Boolean>
    suspend fun observeUser(): Flow<Boolean>
    suspend fun observeDeletedIds(): Flow<Boolean>
    suspend fun addFakeUsers()



    fun getUsers(): Flow<List<UserLocal>>
    fun getUserAndDevices(): Flow<Map<UserLocal,List<DeviceLocal>>>
    fun getUserAndDevices(seachText:String): Flow<Map<UserLocal,List<DeviceLocal>>>
    fun getUserDevices(userId: String): Flow<List<DeviceLocal>>
    fun getUsersByDeviceid(deviceId: String): Flow<List<UserLocal>>
    fun getSearchedDevices(searchText: String): Flow<List<DeviceLocal>>
    fun getSearchedAdminDevices(searchText: String): Flow<List<DeviceLocal>>
    fun getSearchedBlockedDevices(searchText: String): Flow<List<DeviceLocal>>
    fun getAllDevices(): Flow<List<DeviceLocal>>
    fun getAdminDevices(): Flow<List<DeviceLocal>>
    fun getBlockedDevices(): Flow<List<DeviceLocal>>
    fun getCurrentDeviceFlowable(): Flow<DeviceLocal>
    fun getCurrentDevice(): DeviceLocal
    suspend fun checkDeviceExist(): LiveData<DeviceRemote?>
    fun getCurrentUserFlowable(): Flow<UserLocal>
    suspend fun getCurrentUser(): UserLocal
}