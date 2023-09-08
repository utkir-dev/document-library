package com.tiptop.data.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.local.UserLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoDocument {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(document: DocumentLocal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMany(documents: List<DocumentLocal>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(document: DocumentLocal): Int

    @Query("SELECT COUNT(*) FROM documents")
    fun getCount(): Int

    @Query("DELETE FROM documents WHERE id=:id")
    suspend fun delete(id: String): Int

    @Query("delete from documents where id in (:idList)")
    fun deleteDocuments(idList: List<String>)

    @Query("DELETE FROM documents")
    fun clear()

    @Query("SELECT MAX(date) FROM documents")
    fun getLastUpdatedTime(): Long

    @Query("SELECT * from documents where id=:id limit 1")
    fun getDocumentById(id: String): DocumentLocal
    @Query("SELECT * from documents where parentId=:parentId ORDER BY date DESC")
    fun getDocumentsByParentId(parentId: String): Flow<List<DocumentLocal>>
   @Query("SELECT COUNT(*) from documents where parentId=:parentId")
    fun getChildsCountByParentId(parentId: String):Int

    @Query("SELECT *  from documents   ORDER BY date DESC")
    fun getSearchedDocuments(): Flow<List<DocumentLocal>>

    @Query("SELECT " +
            "parent.id as id, " +
            "parent.parentId as parentId, " +
            "parent.name as name, " +
            "parent.searchText as searchText, " +
            "parent.headBytes as headBytes, " +
            "parent.loaded as loaded, " +
            "parent.loading as loading, " +
            "parent.loadingBytes as loadingBytes, " +
            "parent.lastSeenPage as lastSeenPage, " +
            "parent.type as type, " +
            "parent.size as size, " +
            "parent.lastSeenDate as lastSeenDate, " +
            "parent.date as date, " +
            "parent.dateAdded as dateAdded, " +
            "(select count(*) from documents child where parent.parentId+parent.id=child.parentId) as count  from documents parent left join documents child " +
            "on parent.parentId+parent.id=child.parentId where parent.parentId=:parentId  ORDER BY date DESC")
    fun getDocumentsForRv(parentId: String): Flow<List<DocumentForRv>>

}
