package com.tiptop.domain.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.ResponseResult
import com.tiptop.app.common.Utils
import com.tiptop.app.di.AppScope
import com.tiptop.data.models.local.DocumentForRv
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.remote.DeletedIdRemote
import com.tiptop.data.models.remote.DocumentRemote
import com.tiptop.data.repository.local.DaoDocument
import com.tiptop.domain.AddEditDocumentRepository
import com.tiptop.domain.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddEditDocumentRepositoryImpl @Inject constructor(
    private val auth: AuthRepository,
    private val remoteDatabase: Firebase,
    private val remoteStorage: FirebaseStorage,
    private val documentsLocalDb: DaoDocument,
    private val context: Context,
    @AppScope private val coroutine: CoroutineScope
) : AddEditDocumentRepository {
    override suspend fun saveRemoteDocument(documentRemote: DocumentRemote): ResponseResult<Boolean> {
        val remote =
            remoteDatabase.firestore.collection(Utils().getDocumentsFolder())
                .document(documentRemote.id)
        return try {
            remote.set(documentRemote).await()
            ResponseResult.Success(true)
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }

    override suspend fun uploadFile(
        bytes: ByteArray,
        document: DocumentRemote
    ) {
        val documentLocal = document.toLocal()
        val reference = remoteStorage.getReference("${Utils().getDocumentsFolder()}/" + document.id)
        reference.putBytes(bytes)
            .addOnSuccessListener {
                coroutine.launch(Dispatchers.IO) {
                    async {
                        documentLocal.loading = false
                        documentLocal.loadingBytes = 0
                        documentsLocalDb.update(documentLocal)
                        ResponseResult.Success(true)
                    }
                }
            }
            .addOnFailureListener {
                ResponseResult.Failure("Xatolik")
            }
            .addOnCanceledListener {
                ResponseResult.Failure("Yuklash bekor bo'ldi")
            }
            .addOnPausedListener {
                ResponseResult.Failure("Yuklash to'xtab qoldi")
            }
            .addOnProgressListener {
                documentLocal.loading = true
                documentLocal.loadingBytes = it.bytesTransferred
                documentLocal.size = it.totalByteCount
                coroutine.launch(Dispatchers.IO) {
                    async { documentsLocalDb.update(documentLocal) }
                }
            }
    }

    override suspend fun downloadFile(document: DocumentLocal) {
        var file: File = context.getFileStreamPath(document.id)
        try {
            if (file.exists()) {
                file.delete()
                file = context.getFileStreamPath(document.id)
            }
        } catch (_: Exception) {
        }
        val reference = remoteStorage.getReference("${Utils().getDocumentsFolder()}/" + document.id)
        val uri: Uri = file.toUri()
        reference.getFile(uri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    coroutine.launch(Dispatchers.IO) {
                        async {
                            delay(1000)
                            val doc = document.copy(loaded = true, loading = false)
                            documentsLocalDb.update(doc)
                            Log.d("downloadFile", "OnComplete loaded: ${doc.loaded}")
                        }
                    }
                    //  runBlocking { documentsLocalDb.update(document) }
                }
            }.addOnProgressListener { taskSnapshot ->
                document.loading = true
                document.loadingBytes = taskSnapshot.bytesTransferred
                coroutine.launch(Dispatchers.IO) {
                    async {
                        documentsLocalDb.update(document)
                        Log.d("downloadFile", "OnProgress loaded: ${document.loaded}")

                    }
                }
                //  runBlocking { documentsLocalDb.update(document) }
            }
    }

    override suspend fun deleteDocument(document: DocumentLocal): ResponseResult<Boolean> {
        val remoteDatabase =
            remoteDatabase.firestore.collection(Utils().getDocumentsFolder())
                .document(document.id)
        val remoteStorage =
            remoteStorage.getReference("${Utils().getDocumentsFolder()}/" + document.id)
        val remoteDeletedFolder =
            remoteDatabase.firestore.collection(Utils().getDeletedIdsFolder())
                .document(document.id)
        return try {
            remoteDatabase.delete().await()
            remoteDeletedFolder.set(DeletedIdRemote(document.id, System.currentTimeMillis()))
                .await()
            if (document.type != TYPE_FOLDER) {
                remoteStorage.delete().await()
            }
            documentsLocalDb.delete(document.id)
            ResponseResult.Success(true)
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }

    override suspend fun observeDocuments(): Flow<Boolean> = callbackFlow {
        if (auth.currentUser != null) {
            val countDocuments = documentsLocalDb.getCount()
            val lastUpdatedDate =
                if (countDocuments == 1) 0 else documentsLocalDb.getLastUpdatedTime()
            remoteDatabase.firestore.collection(Utils().getDocumentsFolder())
                .whereGreaterThan("date", lastUpdatedDate)
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.let {
                        try {
                            val list = snapshot.map {
                                it.toObject(
                                    DocumentRemote::class.java
                                )
                            }
                            coroutine.launch(Dispatchers.IO) {
                                async {
                                    list.forEach {
                                        val doc = documentsLocalDb.getDocumentById(it.id)
                                        if (doc == null) {
                                            documentsLocalDb.add(it.toLocal())
                                        } else {
                                            documentsLocalDb.add(
                                                doc.copy(
                                                    parentId = it.parentId,
                                                    name = it.name,
                                                    date = it.date,
                                                )
                                            )
                                        }
                                    }

                                }.await()
                                trySend(true)
                            }
                        } catch (_: Exception) {
                            trySend(false)
                        }
                    }
                }
        }
        awaitClose { }
    }.flowOn(Dispatchers.IO)

    override fun getDocuments(searchText: String): Flow<List<DocumentLocal>> {
        return documentsLocalDb.getSearchedDocuments().flowOn(Dispatchers.IO)

    }

    override fun getChildsCountByParentId(parentId: String):Int{
        return documentsLocalDb.getChildsCountByParentId(parentId)
    }

    override fun getDocumentsByParentId(parentId: String): Flow<List<DocumentLocal>> {
        return documentsLocalDb.getDocumentsByParentId(parentId = parentId).flowOn(Dispatchers.IO)
    }

    override fun getDocumentsForRv(parentId: String): Flow<List<DocumentForRv>> {
        return documentsLocalDb.getDocumentsForRv(parentId).flowOn(Dispatchers.IO)
    }
}