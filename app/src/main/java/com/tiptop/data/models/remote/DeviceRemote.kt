package com.tiptop.data.models.remote

import com.tiptop.data.models.local.DeviceLocal

data class DeviceRemote(
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var tablet: Boolean = false,
    var libVersion: String = "",
    var blocked: Boolean = false,
    var admin: Boolean = false,
    var date: Long = 0,
    var dateAdded: Long = 0
) {
    fun toLocal() = DeviceLocal(
        id = this.id,
        userId = this.userId,
        name = this.name,
        tablet = this.tablet,
        libVersion = this.libVersion,
        blocked = this.blocked,
        admin = this.admin,
        date = this.date,
        dateAdded = this.dateAdded,
    )
}
