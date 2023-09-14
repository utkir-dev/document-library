package com.tiptop.presentation.screens.home

import android.content.Context
import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.gson.Gson
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import com.tiptop.app.common.Constants
import com.tiptop.app.common.Constants.LIB_VERSION
import com.tiptop.app.common.Resource
import com.tiptop.app.common.SharedPrefSimple
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.domain.AuthRepository
import com.tiptop.domain.DocumentsRepository
import com.tiptop.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HomeViewModelImpl @Inject constructor(
    private val repository: DocumentsRepository
) : ViewModel(), HomeViewModel {

    private val _lastSeenDocument = MutableLiveData<DocumentLocal?>()
    override val lastSeenDocument: LiveData<DocumentLocal?> = _lastSeenDocument

    private val _countLoadedDocuments = MutableLiveData(0)
    override val countLoadedDocuments: LiveData<Int> = _countLoadedDocuments

    private val _countAllDocuments = MutableLiveData(0)
    override val countAllDocuments: LiveData<Int> = _countAllDocuments

    private val _countNewDocuments = MutableLiveData(0)
    override val countNewDocuments: LiveData<Int> = _countNewDocuments

    private val _hijriy = MutableLiveData("")
    override val hijriy: LiveData<String> = _hijriy

    init {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                repository.getAllDocumentsCount().collectLatest {
                    _countAllDocuments.postValue(it)
                }
            }
            async {
                repository.getLoadedDocumentsCount().collectLatest {
                    _countLoadedDocuments.postValue(it)
                }
            }
            async {
                repository.getNewDocumentsCount().collectLatest {
                    _countNewDocuments.postValue(it)
                }
            }
            async {
                repository.getLastSeenDocument().collectLatest {
                    it?.let {_lastSeenDocument.postValue(it)  }
                }
            }
            async {
                showHijri()
            }
        }
    }
    private fun showHijri() {
        try {
            val ar = Locale("ar");
            val uCal = UmmalquraCalendar(ar);

            val dateFormat = SimpleDateFormat("", ar);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateFormat.setCalendar(uCal);
            uCal.set(Calendar.YEAR, 1420);                  // Used to properly format 'yy' pattern
            dateFormat.set2DigitYearStart(uCal.getTime());  // Used to properly format 'yy' pattern

            val cal = UmmalquraCalendar(ar);

            // dateFormat.applyPattern("EEEE, MMMM d, yy");
            dateFormat.applyPattern("d MMMM yy, EEEE");
            cal.setTime(Date(System.currentTimeMillis()));
            // cal.setTime(dateFormat.parse("السبت, جمادى الأولى 23, 36 12:19:44"));
            val year = cal.get(Calendar.YEAR);                                      // 1436
            val month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, ar);       // جمادى الأولى
            val week = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, ar); // السبت
            val day = cal.get(Calendar.DAY_OF_MONTH);
            // val today="$day $month $year, $week"
            val today = "$year $month $day , $week"
            _hijriy.postValue(today)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}

