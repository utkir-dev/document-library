package com.tiptop.domain


import androidx.paging.PagingData
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.presentation.screens.document_view.pdf.Dictionary
import kotlinx.coroutines.flow.Flow

interface DictionaryRepository {
    fun getBaseWords(): Flow<List<ArabUzBase>>
    fun getBaseWordById(id:Int): ArabUzBase
    fun getSearchedBaseWords(searchText: String): Flow<List<ArabUzBase>>
    fun getUserWords(documentId:String): Flow<List<ArabUzUser>>
    suspend fun saveUserWord(word: ArabUzUser)
    suspend fun deleteUserWord(id: Int)
    suspend fun updateBaseWord(word: ArabUzBase)
    fun checkRemoteDictionary()
    fun getUserAllDictionary(): Flow<PagingData<Dictionary>>
    fun getUseDictionaryCount(): Flow<Int>
}