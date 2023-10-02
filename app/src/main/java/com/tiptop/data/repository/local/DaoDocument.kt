package com.tiptop.data.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tiptop.data.models.local.DocumentLocal
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

    @Query("SELECT COUNT(*) FROM documents where type>0 AND loaded=:loaded")
    fun getLoadedDocumentsCount(loaded: Boolean = true): Flow<Int>

    @Query("SELECT COUNT(*) FROM documents WHERE type>0")
    fun getAllDocumentsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM documents WHERE type>0 AND dateAdded>:date")
    fun getNewDocumentsCount(date: Long): Flow<Int>

    @Query("DELETE FROM documents WHERE id=:id")
    suspend fun delete(id: String): Int

    @Query("DELETE FROM documents WHERE id IN (:idList)")
    fun deleteDocuments(idList: List<String>)

    @Query("DELETE FROM documents")
    fun clear()

    @Query("SELECT MAX(date) FROM documents")
    fun getLastUpdatedTime(): Long

    @Query("SELECT * FROM documents WHERE id=:id LIMIT 1")
    fun getDocumentById(id: String): DocumentLocal

    @Query("SELECT * FROM documents WHERE id=:id LIMIT 1")
    fun getDocumentByIdFlow(id: String): Flow<DocumentLocal>

    // EXPERIMENTAL
    @Query("SELECT * FROM documents  WHERE type=1 AND loaded=:loaded ORDER BY lastSeenDate DESC")
    fun getLastSeenDocuments(loaded: Boolean = true): Flow<List<DocumentLocal>>

    @Query("SELECT * FROM documents WHERE type=1  AND loaded=:loaded ORDER BY lastSeenDate DESC LIMIT 1")
    fun getLastSeenDocument(loaded: Boolean = true): Flow<DocumentLocal?>

    @Query("SELECT * FROM documents WHERE parentId=:parentId ORDER BY date DESC")
    fun getDocumentsByParentId(parentId: String): Flow<List<DocumentLocal>>

    @Query("SELECT * FROM documents WHERE parentId=:parentId ORDER BY date DESC")
    fun getChildDocuments(parentId: String): List<DocumentLocal>

    @Query("SELECT COUNT(*) FROM documents WHERE parentId=:parentId")
    fun getChildsCountByParentId(parentId: String): Int

    @Query("SELECT COUNT(*) FROM documents WHERE parentId=:parentId and loaded=:loaded")
    fun getLoadedChildsCountByParentId(parentId: String, loaded: Boolean = true): Int

    @Query("SELECT *  FROM documents   ORDER BY date DESC")
    fun getSearchedDocuments(): Flow<List<DocumentLocal>>

    @Query("SELECT *  FROM documents  ORDER BY date DESC")
    fun getAllDocuments(): Flow<List<DocumentLocal>>

    @Query("SELECT *  FROM documents WHERE loaded=:loaded OR type=0  ORDER BY dateAdded DESC")
    fun getLoadedDocuments(loaded: Boolean = true): Flow<List<DocumentLocal>>

//    @Query("SELECT " +
//            "parent.id as id, " +
//            "parent.parentId as parentId, " +
//            "parent.name as name, " +
//            "parent.searchText as searchText, " +
//            "parent.headBytes as headBytes, " +
//            "parent.loaded as loaded, " +
//            "parent.loading as loading, " +
//            "parent.loadingBytes as loadingBytes, " +
//            "parent.lastSeenPage as lastSeenPage, " +
//            "parent.type as type, " +
//            "parent.size as size, " +
//            "parent.lastSeenDate as lastSeenDate, " +
//            "parent.date as date, " +
//            "parent.dateAdded as dateAdded, " +
//            "(select count(*) from documents child where parent.parentId+parent.id=child.parentId) as count  from documents parent left join documents child " +
//            "on parent.parentId+parent.id=child.parentId where parent.parentId=:parentId  ORDER BY date DESC")
//    fun getDocumentsForRv(parentId: String): Flow<List<DocumentForRv>>

}
