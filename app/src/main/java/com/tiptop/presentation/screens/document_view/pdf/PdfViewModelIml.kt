package com.tiptop.presentation.screens.document_view.pdf

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Constants.HEAD
import com.tiptop.app.common.Encryptor
import com.tiptop.app.common.Utils
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.domain.DictionaryRepository
import com.tiptop.domain.DocumentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class PdfViewModelIml @Inject constructor(
    private val repDocuments: DocumentsRepository,
    private val repDictionary: DictionaryRepository
) : ViewModel(), PdfViewModel {

    private val _currentPage = MutableLiveData(1)
    override val currentPage: LiveData<Int> = _currentPage

    private var currentTime = 15
    private var countDownTimer: CountDownTimer? = null

    private val _screenBlockState = MutableLiveData(false)
    override val screenBlockState: LiveData<Boolean> = _screenBlockState

    private val _nightMode = MutableLiveData(false)
    override val nightMode: LiveData<Boolean> = _nightMode

    private val _fullScreen = MutableLiveData(true)
    override val fullScreen: LiveData<Boolean> = _fullScreen

    private val _currentDocument = MutableLiveData(DocumentLocal())
    override val currentDocument: LiveData<DocumentLocal> = _currentDocument

    private val _lastDocuments = MutableLiveData<List<DocumentLocal>>(emptyList())
    override val lastDocuments: LiveData<List<DocumentLocal>> = _lastDocuments

    private val _documentBytes = MutableLiveData<ByteArray?>()
    val documentBytes: LiveData<ByteArray?> = _documentBytes

    private val _words = MutableLiveData<List<Dictionary>>()
    override val words: LiveData<List<Dictionary>> = _words

    private val _closestPage = MutableLiveData(0)
    val closestPage: LiveData<Int> = _closestPage
    override fun getWords(documentId: String) {
        viewModelScope.async(Dispatchers.IO) {
            repDictionary.getUserWords(documentId).collectLatest { userWords ->
                try {
                    val page = _currentPage.value ?: 1
                    val minDiff: Int = userWords.minOf { abs(it.pageNumber - page) }
                    val closeItem: ArabUzUser? =
                        userWords.find { abs(it.pageNumber - page) == minDiff }
                    closeItem?.let {
                        _closestPage.postValue(userWords.indexOf(it))
                    }
                } catch (_: Exception) {
                }

                if (userWords.isNotEmpty()) {
                    _words.postValue(userWords)
                }
            }
        }
    }

    override fun getSearchedBaseWords(searchText: String) {
        viewModelScope.async(Dispatchers.IO) {
            repDictionary.getSearchedBaseWords(searchText).collectLatest {
                _words.postValue(it)
            }
        }
    }

    override fun updateBaseWord(word: Dictionary, pageNumber: Int, documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (word is ArabUzBase) {
                async { repDictionary.updateBaseWord(word) }
                if (word.saved) {
                    async {
                        repDictionary.saveUserWord(
                            word.toUser().copy(
                                documentId = documentId,
                                pageNumber = pageNumber,
                                date = System.currentTimeMillis()
                            )
                        )
                    }
                } else {
                    async { repDictionary.deleteUserWord(word.docid) }
                }
            } else if (word is ArabUzUser) {
                async { repDictionary.deleteUserWord(word.docid) }
                async {
                    val baseWord = repDictionary.getBaseWordById(word.docid)
                    repDictionary.updateBaseWord(baseWord.copy(saved = false))
                }
            }
        }
    }

    override fun setCurrentPage(page: Int) {
        _currentPage.postValue(page)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun setDocument(id: String) {
        viewModelScope.async(Dispatchers.IO) {
            val document = repDocuments.getDocumentById(id)
            if (_currentDocument.value?.id != document.id) {
                _currentPage.postValue(document.lastSeenPage)
                _currentDocument.postValue(document)
                getFileBytes(document)
            }
        }
    }

    override fun changeFullScreen() {
        val value = _fullScreen.value ?: false
        _fullScreen.postValue(!value)
    }

    override fun changeNightMode() {
        val value = _nightMode.value ?: false
        _nightMode.postValue(!value)
    }

    fun getLastSeenDocuments() {
        viewModelScope.async(Dispatchers.IO) {
            repDocuments.getLastSeenDocuments().collectLatest {
                _lastDocuments.postValue(it)
            }
        }
    }

    override fun updateDocument() {
        viewModelScope.async(Dispatchers.IO) {
            _currentDocument.value?.let {
                it.lastSeenPage = _currentPage.value ?: 0
                it.lastSeenDate = System.currentTimeMillis()
                repDocuments.saveTempDocumentsToLocalDb(listOf(it))
            }
        }
    }

    fun updateTimer(time: Int? = null) {
        viewModelScope.launch {
            var isEnd = false
            _screenBlockState.postValue(false)
            countDownTimer?.cancel()
            countDownTimer = null
            time?.let { currentTime = it }
            val target = (currentTime * 60_000).toLong()
            countDownTimer = object : CountDownTimer(target, 1_000) {
                override fun onTick(p0: Long) {
                    if (p0 < 1000) {
                        isEnd = true
                    }
                }

                override fun onFinish() {
                    if (isEnd) {
                        _screenBlockState.postValue(true)
                    }
                }
            }
            countDownTimer?.start()
        }

    }

    fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun getFileBytes(document: DocumentLocal) {
        viewModelScope.async(Dispatchers.IO) {
            combine(
                repDocuments.getFileBytes(HEAD + document.id),
                repDocuments.getFileBytes(document.id)
            ) { headEncryptedBytes, bodyBytes ->
                bodyBytes?.let { body ->
                    headEncryptedBytes?.let { head ->
                        Encryptor().getDecryptedBytes(
                            Utils().getKeyStr(document.dateAdded),
                            Utils().getSpecStr(document.dateAdded),
                            head
                        ) {
                            it?.let {
                                _documentBytes.postValue(it.plus(body))
                            }
                        }
                    }
                }
            }.collect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        _documentBytes.postValue(null)
        cancelTimer()
    }
}