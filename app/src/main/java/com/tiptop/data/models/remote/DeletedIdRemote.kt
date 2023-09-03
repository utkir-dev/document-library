package com.tiptop.data.models.remote

import com.tiptop.data.models.local.DeletedIdLocal

data class DeletedIdRemote(
    var id: String = "",
    var date: Long = 0,
){
    fun toLocal()=DeletedIdLocal(
        id=this.id,
        date=this.date
    )
}
