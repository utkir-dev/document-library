package com.tiptop.presentation.screens.home

import androidx.lifecycle.LiveData
import com.tiptop.data.models.local.DocumentLocal

interface HomeViewModel {
    val lastSeenDocument: LiveData<DocumentLocal?>
    val countLoadedDocuments: LiveData<Int>
    val countAllDocuments: LiveData<Int>
    val countNewDocuments: LiveData<Int>
    val hijriy: LiveData<String>
}