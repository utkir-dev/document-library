package com.tiptop.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiptop.data.models.remote.FolderRemote

@Entity(tableName = "folders")
data class FolderLocal(
    @PrimaryKey
    var id: String = "",
    var parentIds: String = "",
    var name: String = "",
    var searchText: String = "",
    var date: Long = 0,
) {
    fun toRemote() = FolderRemote(
        id = this.id,
        parentIds = this.parentIds,
        name = this.name,
        date = this.date
    )
}
