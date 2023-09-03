package com.tiptop.data.models.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.tiptop.data.models.remote.DeviceRemote

@Entity(tableName = "devices")
data class DeviceLocal(
    @PrimaryKey
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var searchText: String = "",
    var tablet: Boolean = false,
    var libVersion: String = "",
    var blocked: Boolean = false,
    var admin: Boolean = false,
    var date: Long = 0,
    var dateAdded: Long = 0
) {
    fun toRemote() = DeviceRemote(
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
