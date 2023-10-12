package com.tiptop.app.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tiptop.data.repository.local.MyRoom
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.tiptop.app.common.Utils
import com.tiptop.data.repository.local.DaoAruzBase
import com.tiptop.data.repository.local.DaoAruzUser
import com.tiptop.data.repository.local.DaoDeletedId
import com.tiptop.data.repository.local.DaoDevice
import com.tiptop.data.repository.local.DaoDocument
import com.tiptop.data.repository.local.DaoUser
import com.tiptop.data.repository.local.DaoUzarBase
import com.tiptop.data.repository.local.DaoUzarUser
import com.tiptop.domain.DocumentsRepository
import com.tiptop.domain.AuthRepository
import com.tiptop.domain.DictionaryRepository
import com.tiptop.domain.UserRepository
import com.tiptop.domain.impl.DocumentsRepositoryImpl
import com.tiptop.domain.impl.AuthRepositoryImpl
import com.tiptop.domain.impl.DictionaryRepositoryImpl
import com.tiptop.domain.impl.UsersRepositoryImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository =
        AuthRepositoryImpl(
            auth = Firebase.auth
        )

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: AuthRepository,
        daoUser: DaoUser,
        daoDevice: DaoDevice,
        daoDeletedId: DaoDeletedId,
        daoDocument: DaoDocument,
        ctx: Context
    ): UserRepository = UsersRepositoryImpl(
        remoteDatabase = Firebase,
        auth = auth,
        usersLocalDb = daoUser,
        devicesLocalDb = daoDevice,
        context = ctx,
        deletedIdsLocalDb = daoDeletedId,
        documentsLocalDb = daoDocument
    )

    @Provides
    @Singleton
    fun provideDictionaryRepository(
        auth: AuthRepository,
        daoUzarBase: DaoUzarBase,
        daoAruzBase: DaoAruzBase,
        daoUzarUser: DaoUzarUser,
        daoAruzUser: DaoAruzUser,
        ctx: Context,
        sharedPref: SharedPreferences,
        coroutineScope: CoroutineScope
    ): DictionaryRepository = DictionaryRepositoryImpl(
        auth = auth,
        remoteDatabase = Firebase,
        remoteStorage = FirebaseStorage.getInstance(),
        uzarBaseDb = daoUzarBase,
        aruzBaseDb = daoAruzBase,
        uzarUserDb = daoUzarUser,
        aruzUserDb = daoAruzUser,
        context = ctx,
        shared = sharedPref,
        coroutine = coroutineScope
    )

    @Provides
    @Singleton
    fun provideAddEditDocumentRepository(
        auth: AuthRepository,
        daoDocument: DaoDocument,
        ctx: Context,
        aruzUserDb: DaoAruzUser,
        coroutineScope: CoroutineScope
    ): DocumentsRepository = DocumentsRepositoryImpl(
        auth = auth,
        remoteDatabase = Firebase,
        remoteStorage = FirebaseStorage.getInstance(),
        documentsLocalDb = daoDocument,
        aruzUserDb = aruzUserDb,
        context = ctx,
        coroutine = coroutineScope
    )

    @Provides
    fun provideContext(
        app: Application
    ): Context {
        return app
    }

    @[Provides Singleton]
    fun getGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideLocalDatabase(app: Application): MyRoom = MyRoom.getInstance(app)

    @Provides
    @Singleton
    fun provideDaoAruzUser(db: MyRoom): DaoAruzUser = db.AruzUserDao()

    @Provides
    @Singleton
    fun provideDaoUzarUser(db: MyRoom): DaoUzarUser = db.UzarUserDao()

    @Provides
    @Singleton
    fun provideDaoAruzBase(db: MyRoom): DaoAruzBase = db.AruzBaseDao()

    @Provides
    @Singleton
    fun provideDaoUzarBase(db: MyRoom): DaoUzarBase = db.UzarBaseDao()

    @Provides
    @Singleton
    fun provideDaoUser(db: MyRoom): DaoUser = db.UserDao()

    @Provides
    @Singleton
    fun provideDaoDocument(db: MyRoom): DaoDocument = db.DocumentDao()

    @Provides
    @Singleton
    fun provideDaoDevice(db: MyRoom): DaoDevice = db.DeviceDao()

    @Provides
    @Singleton
    fun provideDaoDeletedId(db: MyRoom): DaoDeletedId = db.DeletedIdDao()

    @Singleton
    @Provides
    fun encryptedSharedPref(
        @ApplicationContext
        context: Context
    ): SharedPreferences {
        val masterKey =
            MasterKey
                .Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        return EncryptedSharedPreferences.create(
            context,
            Utils().getSharedPrefName(),
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Singleton
    @Provides
    fun providesCoroutineScope(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class AppScope
