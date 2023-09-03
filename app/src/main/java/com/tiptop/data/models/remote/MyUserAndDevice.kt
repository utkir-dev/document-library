package com.tiptop.data.models.remote

import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.UserLocal

data class MyUserAndDevice(
    var user: UserLocal?=null,
    var device: DeviceLocal?=null

)
