package com.tiptop.data.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tiptop.data.models.local.ArabUzBase
import kotlinx.coroutines.flow.Flow


@Dao
interface DaoAruzBase {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAruz(word: ArabUzBase)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAruz(word: ArabUzBase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAruzs(words: List<ArabUzBase>)

    @Query("SELECT * FROM ar_uz_base WHERE docid=:id  LIMIT 1")
    fun getAruzById(id: Int): ArabUzBase

    @Query("SELECT * FROM ar_uz_base LIMIT 100")
    fun getAruzList(): Flow<List<ArabUzBase>>

    @Query("SELECT COUNT(*) FROM ar_uz_base")
    fun getAruzCount(): Int

    @Query("SELECT * FROM ar_uz_base  WHERE c1arabsearch  LIKE :search")
    fun getSearchedBaseWords(search: String = ""): Flow<List<ArabUzBase>>
}

