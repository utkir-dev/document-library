package com.tiptop.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiptop.data.models.remote.DeletedIdRemote

@Entity(tableName = "deletedIds")
data class DeletedIdLocal(
    @PrimaryKey
    var id: String = "",
    var date: Long = 0,
){
    fun toRemote()= DeletedIdRemote(
        id=this.id,
        date=this.date
    )
}
