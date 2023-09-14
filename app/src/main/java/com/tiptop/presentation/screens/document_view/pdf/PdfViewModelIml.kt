package com.tiptop.presentation.screens.document_view.pdf

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiptop.app.common.Constants.HEAD
import com.tiptop.app.common.Encryptor
import com.tiptop.app.common.Utils
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.domain.DocumentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfViewModelIml @Inject constructor(
    private val repository: DocumentsRepository
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
    override fun setCurrentPage(page: Int) {
        _currentPage.postValue(page)
    }

    private val _documentBytes = MutableLiveData<ByteArray?>()
    val documentBytes: LiveData<ByteArray?> = _documentBytes
    override fun setDocument(id: String) {
        viewModelScope.async(Dispatchers.IO) {
            repository.getDocumentByIdFlow(id).collectLatest {
                _currentPage.postValue(it.lastSeenPage)
                _currentDocument.postValue(it)
                getFileBytes(it)

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
            repository.getLastSeenDocuments().collectLatest {
                _lastDocuments.postValue(it)
            }
        }
    }

    override fun updateDocument() {
        viewModelScope.async(Dispatchers.IO) {
            _currentDocument.value?.let {
                it.lastSeenPage = _currentPage.value ?: 0
                it.lastSeenDate = System.currentTimeMillis()
                repository.saveTempDocumentsToLocalDb(listOf(it))
            }
        }
    }


    fun updateTimer(time: Int? = null) {
        viewModelScope.launch {
            Log.d("timer", "updateTimer time: ${time}")

            var isEnd = false
            _screenBlockState.postValue(false)
            countDownTimer?.cancel()
            countDownTimer = null
            time?.let { currentTime = it }
            val target = (currentTime * 60_000).toLong()
            countDownTimer = object : CountDownTimer(target, 1_000) {
                override fun onTick(p0: Long) {
                    Log.d("timer", "timer: ${p0 / 1000}")
                    if (p0 < 1000) {
                        isEnd = true
                    }
                }

                override fun onFinish() {
                    Log.d("timer", "timer finished")
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
                repository.getFileBytes(HEAD + document.id),
                repository.getFileBytes(document.id)
            ) { headEncryptedBytes, bodyBytes ->
                bodyBytes?.let { body ->
                    headEncryptedBytes?.let { head ->
                        Encryptor().getDecryptedBytes(
                            Utils().getKeyStr(document.dateAdded),
                            Utils().getSpecStr(document.dateAdded),
                            head
                        ) {
                            it?.let { _documentBytes.postValue(it.plus(body)) }
                        }
                    }
                }
            }.collect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimer()
    }
}