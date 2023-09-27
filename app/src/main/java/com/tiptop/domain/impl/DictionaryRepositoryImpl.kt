package com.tiptop.domain.impl

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tiptop.app.common.Constants.ARUZ
import com.tiptop.app.common.Constants.COUNT_ARUZ
import com.tiptop.app.common.Constants.COUNT_URAZ
import com.tiptop.app.common.Constants.DICTIONARY
import com.tiptop.app.common.Constants.DICTIONARY_VERSION
import com.tiptop.app.common.Constants.KEY_VERSION_ARUZ
import com.tiptop.app.common.Constants.KEY_VERSION_UZAR
import com.tiptop.app.common.Constants.UZAR
import com.tiptop.app.common.UnzipUtils
import com.tiptop.app.di.AppScope
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.UzArabBase
import com.tiptop.data.models.remote.ArabUzRemote
import com.tiptop.data.models.remote.DictVersion
import com.tiptop.data.repository.local.DaoAruzBase
import com.tiptop.data.repository.local.DaoAruzUser
import com.tiptop.data.repository.local.DaoUzarBase
import com.tiptop.data.repository.local.DaoUzarUser
import com.tiptop.domain.AuthRepository
import com.tiptop.domain.DictionaryRepository
import com.tiptop.presentation.screens.document_view.pdf.Dictionary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DictionaryRepositoryImpl @Inject constructor(
    private val auth: AuthRepository,
    private val remoteDatabase: Firebase,
    private val remoteStorage: FirebaseStorage,
    private val uzarBaseDb: DaoUzarBase,
    private val aruzBaseDb: DaoAruzBase,
    private val uzarUserDb: DaoUzarUser,
    private val aruzUserDb: DaoAruzUser,
    private val context: Context,
    private val shared: SharedPreferences,
    @AppScope private val coroutine: CoroutineScope
) : DictionaryRepository {
    override fun getBaseWords(): Flow<List<ArabUzBase>> {
        return aruzBaseDb.getAruzList().flowOn(Dispatchers.IO)
    }

    override fun getBaseWordById(id: Int): ArabUzBase {
        return  aruzBaseDb.getAruzById(id)
    }

    override fun getSearchedBaseWords(searchText: String): Flow<List<ArabUzBase>> {
        return aruzBaseDb.getSearchedBaseWords("$searchText%").flowOn(Dispatchers.IO)
    }

    override fun getUserWords(documentId: String): Flow<List<ArabUzUser>> {
        return aruzUserDb.getAruzList(documentId).flowOn(Dispatchers.IO)
    }

    override suspend fun saveUserWord(word: ArabUzUser) {
        aruzUserDb.addAruz(word)
    }

    override suspend fun deleteUserWord(id: Int) {
        aruzUserDb.deleteAruzUser(id)
    }

    override suspend fun updateBaseWord(word: ArabUzBase) {
        aruzBaseDb.updateAruz(word)
    }

    override fun checkRemoteDictionary() {
        if (auth.currentUser != null) {
            Log.d("aruz", "checkRemoteDictionary")
            remoteDatabase.firestore.collection(DICTIONARY_VERSION).document(DICTIONARY_VERSION)
                .addSnapshotListener { value, error ->
                    Log.d("aruz", "value :$value")
                    Log.d("aruz", "error :${error?.message}")

                    value?.let {
                        try {
                            val countAruz = aruzBaseDb.getAruzCount()
                            val countUraz = uzarBaseDb.getUzarCount()

                            val aruzVersion = shared.getString(KEY_VERSION_ARUZ, "") ?: ""
                            val uzarVersion = shared.getString(KEY_VERSION_UZAR, "") ?: ""
                            val dictVersion = it.toObject(DictVersion::class.java)
                            if (dictVersion?.aruzVersion != aruzVersion || countAruz < COUNT_ARUZ) {
                                initAruz(dictVersion?.aruzVersion ?: "")
                            }
                            if (dictVersion?.uzarVersion != uzarVersion || countUraz < COUNT_URAZ) {
                                initUzar(dictVersion?.uzarVersion ?: "")
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
        }
    }

    private fun initAruz(version: String) {
        val file = context.getFileStreamPath("$ARUZ.zip")
        remoteStorage.getReference("/$DICTIONARY/${"$ARUZ.zip"}")
            .getFile(file.toUri())
            .addOnSuccessListener {
                if (it.task.isSuccessful) {
                    UnzipUtils.unzip(context, file, DICTIONARY) {
                        val fileJson = context.getFileStreamPath("$ARUZ.json")

                        val type = object : TypeToken<ArrayList<ArabUzRemote>>() {}.type
                        val arUz = Gson().fromJson<ArrayList<ArabUzRemote>>(
                            fileJson.readText(Charsets.UTF_8),
                            type
                        )
                        coroutine.launch(Dispatchers.IO) {
                            async { aruzBaseDb.addAruzs(arUz.map { it.toLocal() }) }.await()
                            shared.edit().putString(KEY_VERSION_ARUZ, version).apply()
                            val count = aruzBaseDb.getAruzCount()
                        }
                    }
                }
            }
    }

    private fun initUzar(version: String) {
        val file = context.getFileStreamPath("$UZAR.zip")
        remoteStorage.getReference("/$DICTIONARY/${"$UZAR.zip"}")
            .getFile(file.toUri())
            .addOnSuccessListener {
                if (it.task.isSuccessful) {
                    UnzipUtils.unzip(context, file, DICTIONARY) {
                        val fileJson = context.getFileStreamPath("$UZAR.json")
                        val type = object : TypeToken<ArrayList<UzArabBase>>() {}.type
                        val uzar = Gson().fromJson<ArrayList<UzArabBase>>(
                            fileJson.readText(Charsets.UTF_8),
                            type
                        )
                        coroutine.launch(Dispatchers.IO) {
                            async { uzarBaseDb.addUzars(uzar) }.await()
                            shared.edit().putString(KEY_VERSION_UZAR, version).apply()
                            val count = uzarBaseDb.getUzarCount()
//                                arUz.forEach {
//                                    DB_DIC.daoUzar().addUzar(it)
//                                    Log.d("aruz","added: ${it.c0uzbek}")
//                                }
                        }
                    }
                }
            }
    }

}