package com.tiptop.presentation.screens.sign_up

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaredrummler.android.device.DeviceName
import com.tiptop.app.common.Constants
import com.tiptop.app.common.Resource
import com.tiptop.app.common.ResponseResult
import com.tiptop.app.common.encryption
import com.tiptop.data.models.remote.DeviceRemote
import com.tiptop.data.models.remote.UserRemote
import com.tiptop.domain.AuthRepository
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModelImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val remoteRepository: UserRepository,

    ) : ViewModel(), SignUpViewModel {
    private val deviceId = MutableLiveData<String>()
    private val _result = MutableLiveData(Resource.default(false))
    val result: LiveData<Resource<Boolean>> = _result

    private val isTablet = MutableLiveData<Boolean>()
    fun setDeviceIsTablet(tablet: Boolean) {
        this.isTablet.postValue(tablet)
    }

    fun setDeviceId(id: String) {
        deviceId.postValue(id)
    }

    override fun signUp(email: String, password: String, telegramUser: String) {
        _result.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = authRepository.signUp(email, password)
            if (result is ResponseResult.Success) {
                if (result.data?.isNotEmpty() == true) {
                    val date = System.currentTimeMillis()
                    var telegram = telegramUser
                    if (telegramUser.isNotEmpty()) {
                        telegram= telegramUser.encryption(date)
                    }

                    val userRemote = UserRemote(
                        id = result.data,
                        deviceId = deviceId.value ?: "",
                        email = email,
                        telegramUser = telegram,
                        permitted = false,
                        date = date,
                        dateAdded = date
                    )

                    val deviceName = try {
                        DeviceName.getDeviceName()
                    } catch (e: Exception) {
                        deviceId.value ?: ""
                    }
                    val deviceRemote = DeviceRemote(
                        id = deviceId.value ?: "",
                        name = deviceName,
                        tablet = isTablet.value ?: false,
                        blocked = false,
                        admin = false,
                        date = date,
                        libVersion = Constants.LIB_VERSION,
                        userId = userRemote.id,
                        dateAdded = date
                    )


                    val resultSaveRemoteUser = async {
                        remoteRepository.saveRemoteUser(userRemote)
                    }
                    val resultSaveRemoteDevice = async {
                        remoteRepository.saveRemoteDevice(deviceRemote)
                    }
                    val resultSaveRemoteUserPassword = async {
                        remoteRepository.saveRemoteUserPassword(password, userRemote.id)
                    }
                    val task1 = resultSaveRemoteUser.await()
                    val task2 = resultSaveRemoteUserPassword.await()
                    val task3 = resultSaveRemoteDevice.await()

                    if (task1 is ResponseResult.Success &&
                        task2 is ResponseResult.Success &&
                        task3 is ResponseResult.Success
                    ) {
                        if (task1.data && task2.data && task3.data) {
                            _result.postValue(Resource.success(null))
                        } else {
                            _result.postValue(Resource.error(null, null))
                        }
                    }
                } else {
                    _result.postValue(Resource.error(null, null))
                }
            } else {
                _result.postValue(Resource.error(null, null))
            }
        }
    }
}