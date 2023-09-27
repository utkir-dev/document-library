package com.tiptop.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiptop.presentation.screens.document_view.pdf.Dictionary

@Entity(tableName = "ar_uz_base")
data class ArabUzBase(
    var c0arab: String = "",
    var c1arabsearch: String = "",
    var c2uzbek: String = "",
    var c3rus: String = "",
    var saved: Boolean = false,
    @PrimaryKey
    override var docid: Int = 0
) : Dictionary {
    fun toUser()=ArabUzUser(
        docid = this.docid,
        c0arab = this.c0arab,
        c1arabsearch = this.c1arabsearch,
        c2uzbek = this.c2uzbek,
        c3rus = this.c3rus
    )
}
