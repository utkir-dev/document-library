package com.tiptop.data.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tiptop.data.models.local.DeletedIdLocal
@Dao
interface DaoDeletedId {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(deletedId: DeletedIdLocal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMany(deletedIds: List<DeletedIdLocal>): List<Long>

    @Query("DELETE FROM deletedIds")
    fun clear()

    @Query("SELECT MAX(date) FROM deletedIds")
    fun getLastUpdatedTime(): Long
}