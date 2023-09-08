package com.tiptop.presentation.screens.settings.sign_in

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaredrummler.android.device.DeviceName
import com.tiptop.app.common.Constants.LIB_VERSION
import com.tiptop.app.common.Resource
import com.tiptop.app.common.ResponseResult
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.UserLocal
import com.tiptop.data.models.remote.DeviceRemote
import com.tiptop.domain.AuthRepository
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModelImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val remoteRepository: UserRepository
) : ViewModel(), SignInViewModel {
    private val _result = MutableLiveData(Resource.default(false))
    val result: LiveData<Resource<Boolean>> = _result
    private val deviceId = MutableLiveData<String>()
    private val isTablet = MutableLiveData<Boolean>()
    fun setDeviceId(id: String) {
        deviceId.postValue(id)
    }

    fun setDeviceIsTablet(tablet: Boolean) {
        this.isTablet.postValue(tablet)
    }

    override fun signIn(email: String, password: String) {
        _result.postValue(Resource.loading(null))
        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            if (result is ResponseResult.Success) {
                if (result.data?.isNotEmpty() == true) {
                    val deviceName = try {
                        DeviceName.getDeviceName()
                    } catch (e: Exception) {
                        deviceId.value ?: ""
                    }
                    val date = System.currentTimeMillis()
                    var device: DeviceLocal? = null
                    var user: UserLocal? = null

                    val taskUser = async {
                        launch(Dispatchers.Default) {
                            repeat(10) {
                                user = remoteRepository.getCurrentUser()
                                if (user != null) {
                                    cancel()
                                }
                                delay(500)
                            }
                        }
                    }
                    val taskDevice = async {
                        launch(Dispatchers.Default) {
                            repeat(10) {
                                device = remoteRepository.getCurrentDevice()
                                if (device != null) {
                                    cancel()
                                }
                                delay(500)
                            }
                        }
                    }
                    taskUser.await()
                    taskDevice.await()
                    if (device == null) {
                        val deviceRemote =
                            DeviceRemote(
                                id = deviceId.value ?: "",
                                name = deviceName,
                                tablet = isTablet.value ?: false,
                                blocked = false,
                                admin = false,
                                date = date,
                                libVersion = LIB_VERSION,
                                userId = authRepository.currentUser?.uid ?: "",
                                dateAdded = date
                            )
                        val resultSaveRemoteDevice = async {
                            remoteRepository.saveRemoteDevice(deviceRemote)
                        }.await()
                        if (resultSaveRemoteDevice is ResponseResult.Success) {
                            if (resultSaveRemoteDevice.data) {
                                _result.postValue(Resource.success(null))
                            } else {
                                _result.postValue(Resource.error(null, null))
                            }
                        }
                    } else {
                        _result.postValue(Resource.success(null))
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