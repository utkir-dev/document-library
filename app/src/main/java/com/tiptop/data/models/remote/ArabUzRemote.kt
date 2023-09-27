package com.tiptop.data.models.remote

import com.tiptop.data.models.local.ArabUzBase

data class ArabUzRemote(
    var docid: Int = 0,
    var c0arab: String = "",
    var c1arabsearch: String = "",
    var c2uzbek: String = "",
    var c3rus: String = ""
) {
    fun toLocal() = ArabUzBase(
        docid = this.docid,
        c0arab = this.c0arab,
        c1arabsearch = this.c1arabsearch,
        c2uzbek = this.c2uzbek,
        c3rus = this.c3rus,
        saved = false
    )
}
