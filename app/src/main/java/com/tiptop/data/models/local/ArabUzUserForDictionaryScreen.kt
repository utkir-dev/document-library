package com.tiptop.data.models.local

import com.tiptop.presentation.screens.document_view.pdf.Dictionary

data class ArabUzUserForDictionaryScreen(
    override var docid: Int = 0,
    var c0arab: String,
    var c1arabsearch: String,
    var c2uzbek: String,
    var c3rus: String,
    var documentId: String = "",
    var pageNumber: Int = 0,
    var date: Long = 0,
    var documentName: String = "",
    var dateAdded: Long = 0

    ) : Dictionary