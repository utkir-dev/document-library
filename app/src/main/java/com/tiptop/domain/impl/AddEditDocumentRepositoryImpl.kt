package com.tiptop.domain.impl

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.tiptop.domain.AddEditDocumentRepository

import com.tiptop.domain.UserRepository
import javax.inject.Inject

class AddEditDocumentRepositoryImpl @Inject constructor(
    private val remoteDatabase: Firebase,
    private val context: Context
) : AddEditDocumentRepository{
}