package com.tiptop.data.models.local

import android.os.Bundle
import com.gg.gapo.treeviewlib.model.NodeData
import com.tiptop.app.common.decryption
import com.tiptop.app.common.encryption
import com.tiptop.data.models.remote.DocumentRemote


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
    var count: Int = 0,
    var countNewDocuments: Int = 0,
    var child: List<DocumentForRv> = ArrayList()
) : NodeData<DocumentForRv> {
    fun toRemote() = DocumentRemote(
        id = this.id,
        parentId = this.parentId,
        name = this.name.encryption(this.dateAdded),
        headBytes = this.headBytes,
        type = this.type,
        size = this.size,
        date = this.date,
        dateAdded = this.dateAdded,
    )

    fun toDocumentLocal() = DocumentLocal(
        id = this.id,
        parentId = this.parentId,
        name = this.name.encryption(this.dateAdded),
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

    fun nameDecrypted(): String {
        return if (name.isEmpty()) "" else name.decryption(dateAdded)
    }

    override fun areContentsTheSame(item: NodeData<DocumentForRv>): Boolean {
        return this.equals(item)
    }

    override fun areItemsTheSame(item: NodeData<DocumentForRv>): Boolean {
        return nodeViewId == item.nodeViewId
    }

    override fun getChangePayload(item: NodeData<DocumentForRv>): Bundle {
        return Bundle()
    }

    override val nodeViewId: String
        get() = id

    override fun getNodeChild(): List<NodeData<DocumentForRv>> {
        return child.sortedWith(compareBy<DocumentForRv> { it.type }.thenBy { it.name })
    }
}

