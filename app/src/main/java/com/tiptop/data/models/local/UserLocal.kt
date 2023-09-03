package com.tiptop.data.models.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiptop.data.models.remote.UserRemote

@Entity(tableName = "users")
data class UserLocal(
    @PrimaryKey
    var id: String = "",
    var deviceId: String = "",
    var email: String = "",
    var searchText: String = "",
    var telegramUser: String = "",
    var permitted: Boolean = false,
    var date: Long = 0,
    var dateAdded: Long = 0
) {
    fun toRemote() = UserRemote(
        id = this.id,
        deviceId = this.deviceId,
        email = this.email,
        telegramUser = this.telegramUser,
        permitted = this.permitted,
        date = this.date,
        dateAdded = this.dateAdded
    )
}
