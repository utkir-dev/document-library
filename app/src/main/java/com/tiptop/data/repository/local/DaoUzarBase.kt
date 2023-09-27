package com.tiptop.data.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tiptop.data.models.local.UzArabBase
import kotlinx.coroutines.flow.Flow


@Dao
interface DaoUzarBase {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUzar(word: UzArabBase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUzars(words: List<UzArabBase>)

    @Query("select * from uz_ar_base order by c0uzbek limit 200")
    fun getUzarList(): Flow<List<UzArabBase>>

    @Query("select count(*) from uz_ar_base")
    fun getUzarCount(): Int

    @Query("SELECT * FROM uz_ar_base  WHERE c0uzbek  LIKE :search order by c0uzbek")
    fun searchWords(search: String = ""): Flow<List<UzArabBase>>
}

