package com.tiptop.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiptop.presentation.screens.document_view.pdf.Dictionary

@Entity(tableName = "ar_uz_user")
data class ArabUzUser(
    @PrimaryKey
    override var docid: Int = 0,
    var c0arab: String,
    var c1arabsearch: String,
    var c2uzbek: String,
    var c3rus: String,
    var documentId: String = "",
    var pageNumber: Int = 0,
    var date: Long = 0

) : Dictionary