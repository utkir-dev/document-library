package com.tiptop.domain.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.gson.Gson
import com.tiptop.app.common.Constants
import com.tiptop.app.common.Constants.HEAD
import com.tiptop.app.common.Constants.NEW_DOCUMENTS_VISIBILITY_PERIOD
import com.tiptop.app.common.Constants.NEW_VERSION
import com.tiptop.app.common.Constants.TYPE_FOLDER
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.common.DownloadController
import com.tiptop.app.common.Resource
import com.tiptop.app.common.ResponseResult
import com.tiptop.app.common.Utils
import com.tiptop.app.common.getBytes
import com.tiptop.app.common.validateFileSize
import com.tiptop.app.di.AppScope
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.local.LibVersion
import com.tiptop.data.models.remote.DeletedIdRemote
import com.tiptop.data.models.remote.DocumentRemote
import com.tiptop.data.repository.local.DaoDocument
import com.tiptop.domain.DocumentsRepository
import com.tiptop.domain.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DocumentsRepositoryImpl @Inject constructor(
    private val auth: AuthRepository,
    private val remoteDatabase: Firebase,
    private val remoteStorage: FirebaseStorage,
    private val documentsLocalDb: DaoDocument,
    private val context: Context,
    private val shared: SharedPreferences,
    @AppScope private val coroutine: CoroutineScope
) : DocumentsRepository {
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

    override suspend fun saveTempDocumentsToLocalDb(tempDocuments: List<DocumentLocal>) {
        documentsLocalDb.addMany(tempDocuments)
    }

    override suspend fun uploadFile(
        bytes: ByteArray,
        document: DocumentRemote
    ) {
        val documentLocal = document.toLocal()
        val reference =
            remoteStorage.getReference("${Utils().getDocumentsFolder()}/" + document.id)
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

    override suspend fun uploadHeadFile(
        bytes: ByteArray,
        document: DocumentRemote
    ) {
        val reference =
            remoteStorage.getReference("${Utils().getDocumentsFolder()}/" + HEAD + document.id)
        reference.putBytes(bytes)
    }

    override suspend fun downloadFile(document: DocumentLocal) {
        if (document.type == TYPE_PDF) {
            coroutine.launch(Dispatchers.IO) {
                async { downloadHeadFile(document) }
            }
        }
        var file: File = context.getFileStreamPath(document.id)
        try {
            if (file.exists()) {
                file.delete()
                file = context.getFileStreamPath(document.id)
            }
        } catch (_: Exception) {
        }
        val reference =
            remoteStorage.getReference("${Utils().getDocumentsFolder()}/" + document.id)
        val uri: Uri = file.toUri()
        reference.getFile(uri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    coroutine.launch(Dispatchers.IO) {
                        async {
                            delay(1000)
                            val doc = document.copy(loaded = true, loading = false)
                            documentsLocalDb.update(doc)
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
                    }
                }
                //  runBlocking { documentsLocalDb.update(document) }
            }
    }

    override suspend fun downloadHeadFile(document: DocumentLocal) {
        var file: File = context.getFileStreamPath(HEAD + document.id)
        try {
            if (file.exists()) {
                file.delete()
                file = context.getFileStreamPath(HEAD + document.id)
            }
        } catch (_: Exception) {
        }
        val reference =
            remoteStorage.getReference("${Utils().getDocumentsFolder()}/" + HEAD + document.id)
        reference.getFile(file.toUri())
    }

    override fun checkLibVersion() = flow {
        if (auth.currentUser != null) {
            if (LIB_VERSION != null) {
                emit(LIB_VERSION!!)
            } else {
                var fileName = ""
                var fileUrl = ""
                var fileSize = 0L
                var task: Task<StorageMetadata>? = null
                remoteStorage.getReference(NEW_VERSION).listAll().addOnSuccessListener { list ->
                    list.items.forEach { item ->
                        fileName = item.name
                        task = item.metadata.addOnSuccessListener {
                            fileSize = it.sizeBytes
                        }
                    }
                }.await()

                remoteStorage.getReference("$NEW_VERSION/$fileName").downloadUrl.addOnSuccessListener { uri ->
                    fileUrl = uri.toString()
                }.await()
                task?.await()
                val lib = LibVersion(
                    apkName = fileName,
                    url = fileUrl,
                    size = fileSize.validateFileSize()
                )
                LIB_VERSION = lib
                emit(lib)
            }
        }
    }


    override suspend fun downloadFileLive(document: DocumentLocal): Flow<Resource<DocumentLocal>> =
        callbackFlow {
            if (document.type == TYPE_PDF) {
                async { downloadHeadFile(document) }
            }
            var file: File = context.getFileStreamPath(document.id)
            try {
                if (file.exists()) {
                    file.delete()
                    file = context.getFileStreamPath(document.id)
                }
            } catch (_: Exception) {
            }
            val reference =
                remoteStorage.getReference("${Utils().getDocumentsFolder()}/" + document.id)
            val uri: Uri = file.toUri()
            reference.getFile(uri)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        coroutine.launch(Dispatchers.IO) {
                            async {
                                val doc = document.copy(loaded = true, loading = false)
                                trySend(Resource.success(doc))
                            }
                        }
                        //  runBlocking { documentsLocalDb.update(document) }
                    } else {
                        coroutine.async(Dispatchers.IO) {
                            trySend(Resource.error(null, "Xatolik"))
                        }
                    }
                }.addOnProgressListener { taskSnapshot ->
                    document.loading = true
                    document.loadingBytes = taskSnapshot.bytesTransferred
                    coroutine.launch(Dispatchers.IO) {
                        async {
                            trySend(Resource.loading(document))
                        }
                    }
                }

            awaitClose { }
        }.flowOn(Dispatchers.IO)

    override suspend fun deleteDocument(document: DocumentLocal): ResponseResult<Boolean> {
        val remoteDatabase =
            remoteDatabase.firestore.collection(Utils().getDocumentsFolder())
                .document(document.id)
        val remoteStorage =
            remoteStorage.getReference("${Utils().getDocumentsFolder()}/" + document.id)
        val remoteDeletedFolder =
            remoteDatabase.firestore.collection(Utils().getDeletedIdsFolder())
                .document(document.id)
        val tempDeletedDocument = document
        return try {
            documentsLocalDb.delete(document.id)
            if (document.type > 0) {
                deleteFile(document.id)
            }
            val task1 = remoteDatabase.delete()
            val task2 =
                remoteDeletedFolder.set(DeletedIdRemote(document.id, System.currentTimeMillis()))

            var task3: Task<Void>? = null
            if (document.type != TYPE_FOLDER) {
                task3 = remoteStorage.delete()
            }
            task1.await()
            task2.await()
            task3?.await()

            ResponseResult.Success(true)
        } catch (e: Exception) {
            documentsLocalDb.add(tempDeletedDocument)
            ResponseResult.Failure(e.message)
        }
    }

    private fun deleteFile(id: String) {
        val file: File =
            context.getFileStreamPath(id)
        val headFile: File =
            context.getFileStreamPath(HEAD + id)
        try {
            file.delete()
        } catch (_: Exception) {
        }
        try {
            headFile.delete()
        } catch (_: Exception) {
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

    @SuppressLint("Recycle")
    override fun getFileBytes(id: String): Flow<ByteArray?> = flow {
        emit(getBytes(id, context))
    }

    override fun getLoadedDocumentsCount(): Flow<Int> {
        return documentsLocalDb.getLoadedDocumentsCount(true).flowOn(Dispatchers.IO)
    }

    override fun getAllDocumentsCount(): Flow<Int> {
        return documentsLocalDb.getAllDocumentsCount().flowOn(Dispatchers.IO)
    }

    override fun getNewDocumentsCount(): Flow<Int> {
        val date = System.currentTimeMillis() - NEW_DOCUMENTS_VISIBILITY_PERIOD
        return documentsLocalDb.getNewDocumentsCount(date).flowOn(Dispatchers.IO)
    }

    override fun getLastSeenDocument(): Flow<DocumentLocal?> {
        return documentsLocalDb.getLastSeenDocument().flowOn(Dispatchers.IO)
    }

    override fun getDocuments(searchText: String): Flow<List<DocumentLocal>> {
        return documentsLocalDb.getSearchedDocuments().flowOn(Dispatchers.IO)

    }

    override fun getAllDocuments(): Flow<List<DocumentLocal>> {
        return documentsLocalDb.getAllDocuments().flowOn(Dispatchers.IO)
    }
    override fun getLoadedDocuments(): Flow<List<DocumentLocal>> {
        return documentsLocalDb.getLoadedDocuments().flowOn(Dispatchers.IO)
    }

    override fun getChildsCountByParentId(parentId: String): Int {
        return documentsLocalDb.getChildsCountByParentId(parentId)
    }

    override fun getLoadedChildsCountByParentId(parentId: String): Int {
        return documentsLocalDb.getLoadedChildsCountByParentId(parentId)
    }

    override fun getDocumentsByParentId(parentId: String): Flow<List<DocumentLocal>> {
        return documentsLocalDb.getDocumentsByParentId(parentId = parentId)
            .flowOn(Dispatchers.IO)
    }

    override fun getChildDocuments(parentId: String): List<DocumentLocal> {
        return documentsLocalDb.getChildDocuments(parentId)
    }

    override fun getDocumentByIdFlow(id: String): Flow<DocumentLocal> {
        return documentsLocalDb.getDocumentByIdFlow(id).flowOn(Dispatchers.IO)
    }

    override fun getDocumentById(id: String): DocumentLocal {
        return documentsLocalDb.getDocumentById(id)
    }

    override fun getLastSeenDocuments(): Flow<List<DocumentLocal>> {
        return documentsLocalDb.getLastSeenDocuments().flowOn(Dispatchers.IO)
    }

    companion object {
        private var LIB_VERSION: LibVersion? = null

    }
}