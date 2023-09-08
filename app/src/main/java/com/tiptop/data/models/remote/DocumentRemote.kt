package com.tiptop.data.models.remote

import com.tiptop.data.models.local.DocumentLocal


data class DocumentRemote(
    var id: String = "",
    var parentId: String = "",
    var name: String = "",
    var headBytes: String = "",
    var type: Int = 0,
    var size: Long = 0,
    var date: Long = 0,
    var dateAdded: Long = 0,
) {
    fun toLocal() = DocumentLocal(
        id = this.id,
        parentId = this.parentId,
        name = this.name,
        headBytes = this.headBytes,
        type = this.type,
        size = this.size,
        date = this.date,
        dateAdded = this.dateAdded
    )
}
