package com.tiptop.data.models.remote

import com.tiptop.data.models.local.UserLocal

data class UserRemote(
    var id: String = "",
    var deviceId: String = "",
    var email: String = "",
    var telegramUser: String = "",
    var permitted: Boolean = false,
    var date: Long = 0,
    var dateAdded: Long = 0
) {
    fun toLocal() = UserLocal(
        id = this.id,
        deviceId = this.deviceId,
        email = this.email,
        telegramUser = this.telegramUser,
        permitted = this.permitted,
        date = this.date,
        dateAdded = this.dateAdded
    )
}
