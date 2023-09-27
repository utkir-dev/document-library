package com.tiptop.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiptop.presentation.screens.document_view.pdf.Dictionary

@Entity(tableName = "uz_ar_user")
data class UzArabUser(
    @PrimaryKey
    override var docid: Int = 0,
    var uzbek: String = "",
    var arab: String = "",
    var documentId: String = "",
    var pageNumber: Int = 0,
    var date: Long = 0,
): Dictionary