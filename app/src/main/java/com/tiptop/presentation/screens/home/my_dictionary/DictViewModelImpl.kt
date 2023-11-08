package com.tiptop.presentation.screens.home.my_dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.ArabUzUserForDictionaryScreen
import com.tiptop.domain.DictionaryRepository
import com.tiptop.presentation.screens.document_view.pdf.Dictionary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictViewModelImpl @Inject constructor(
    private val repository: DictionaryRepository
) : ViewModel() {
    val dictionaryUser = repository.getUserAllDictionary().cachedIn(viewModelScope)
    fun deleteWord(word: Dictionary) {
        viewModelScope.launch(Dispatchers.IO) {
            if (word is ArabUzUserForDictionaryScreen) {
                async { repository.deleteUserWord(word.docid) }
                async {
                    val baseWord = repository.getBaseWordById(word.docid)
                    repository.updateBaseWord(baseWord.copy(saved = false))
                }
            }
        }
    }
}