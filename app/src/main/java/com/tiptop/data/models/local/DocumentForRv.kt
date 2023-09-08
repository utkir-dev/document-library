package com.tiptop.data.models.local


data class DocumentForRv(
    var id: String = "",
    var parentId: String = "",
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
    var count: Int? = null
) {
    fun toDocumentLocal() = DocumentLocal(
        id = this.id,
        parentId = this.parentId,
        name = this.name,
        searchText = this.searchText,
        headBytes = this.headBytes,
        loaded = this.loaded,
        loading = this.loading,
        loadingBytes = this.loadingBytes,
        lastSeenPage = this.lastSeenPage,
        type = this.type,
        size = this.size,
        lastSeenDate = this.lastSeenDate,
        date = this.date,
        dateAdded = this.dateAdded
    )
}
