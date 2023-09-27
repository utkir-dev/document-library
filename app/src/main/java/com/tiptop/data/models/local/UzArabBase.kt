package com.tiptop.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiptop.presentation.screens.document_view.pdf.Dictionary

@Entity(tableName = "uz_ar_base")
data class UzArabBase(
    var c0uzbek: String = "",
    var c1arab: String = "",
    @PrimaryKey
    override var docid: Int = 0
): Dictionary