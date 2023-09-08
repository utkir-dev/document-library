package com.tiptop.presentation.screens.pdf_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tiptop.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PdfViewModelIml @Inject constructor(
    private val auth: AuthRepository,
   // private val repository: DocumentRe
) : ViewModel() {
    private val _currentPage = MutableLiveData<Int>(1)
    val currentPage: LiveData<Int> = _currentPage

}