package com.tiptop.data.models.remote

import com.tiptop.data.models.local.FolderLocal


data class FolderRemote(
    var id: String = "",
    var parentIds: String = "",
    var name: String = "",
    var date: Long = 0,
) {
    fun toLocal() = FolderLocal(
        id = this.id,
        parentIds = this.parentIds,
        name = this.name,
        date = this.date
    )
}
