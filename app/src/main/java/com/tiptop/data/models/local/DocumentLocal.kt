package com.tiptop.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiptop.data.models.remote.DocumentRemote

@Entity(tableName = "documents")
data class DocumentLocal(
    @PrimaryKey
    var id: String = "",
    var folderIds: String = "",
    var name: String = "",
    var searchText: String = "",
    var headBytes: String = "",
    var loaded: Boolean = false,
    var loading: Boolean = false,
    var loadingBytes: Long = 0,
    var lastSeenPage: Int = 0,
    var type: Int = 0,
    var size: Long = 0,
    var lastSeenDate: Long = 0,
    var date: Long = 0,
    var dateAdded: Long = 0,
) {
    fun toRemote() = DocumentRemote(
        id = this.id,
        folderIds = this.folderIds,
        name = this.name,
        headBytes = this.headBytes,
        type = this.type,
        size = this.size,
        date = this.date,
        dateAdded = this.dateAdded,
    )
}
