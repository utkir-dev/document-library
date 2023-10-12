package com.tiptop.data.repository.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.ArabUzUserForDictionaryScreen
import kotlinx.coroutines.flow.Flow


@Dao
interface DaoAruzUser {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAruz(word: ArabUzUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAruzs(words: List<ArabUzUser>)

    @Query("DELETE FROM ar_uz_user WHERE docid=:id")
    suspend fun deleteAruzUser(id: Int): Int

    @Query("SELECT * FROM ar_uz_user WHERE docid=:id  LIMIT 1")
    fun getAruzById(id: Int): ArabUzUser

    @Query("SELECT * FROM ar_uz_user WHERE documentId=:documentId ORDER BY pageNumber")
    fun getAruzList(documentId: String): Flow<List<ArabUzUser>>

//    @Query("SELECT * FROM ar_uz_user ORDER BY date")
//    fun getAruz(): PagingSource<Int, ArabUzUser>

//    @Query("SELECT * FROM ar_uz_user ORDER BY date ASC LIMIT :limit OFFSET :offset")
//    suspend fun getPagedList(limit: Int, offset: Int): List<ArabUzUser>

    @Query("SELECT *, (SELECT name FROM documents WHERE documents.id = ar_uz_user.documentId limit 1) AS documentName, (SELECT dateAdded FROM documents WHERE documents.id = ar_uz_user.documentId limit 1) AS dateAdded  FROM ar_uz_user  ORDER BY ar_uz_user.date ASC LIMIT :limit OFFSET :offset")
    suspend fun getPagedList(limit: Int, offset: Int): List<ArabUzUserForDictionaryScreen>

    @Query("SELECT COUNT(*) FROM ar_uz_user")
    fun getAruzUserCount(): Flow<Int>

    @Query("SELECT * FROM ar_uz_user  WHERE c1arabsearch  LIKE :search ORDER BY c0arab")
    fun searchWords(search: String = ""): Flow<List<ArabUzUser>>

    @Query("delete  FROM ar_uz_user  WHERE documentId=:id")
    fun clearAruzUser(id: String)
}

