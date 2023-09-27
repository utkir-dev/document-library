package com.tiptop.data.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tiptop.data.models.local.UzArabUser
import kotlinx.coroutines.flow.Flow


@Dao
interface DaoUzarUser {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUzar(word: UzArabUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUzars(words: List<UzArabUser>)

    @Query("select * from uz_ar_user order by uzbek limit 200")
    fun getUzarList(): Flow<List<UzArabUser>>

    @Query("select count(*) from uz_ar_user")
    fun getUzarCount(): Int

    @Query("SELECT * FROM uz_ar_user  WHERE uzbek  LIKE :search order by uzbek")
    fun searchWords(search: String = ""): Flow<List<UzArabUser>>
}

