package com.tiptop.domain.impl

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tiptop.app.common.MyDevice
import com.tiptop.app.common.ResponseResult
import com.tiptop.app.common.Utils
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.UserLocal
import com.tiptop.data.models.remote.DeletedIdRemote
import com.tiptop.data.models.remote.DeviceRemote
import com.tiptop.data.models.remote.UserRemote
import com.tiptop.data.repository.local.DaoDeletedId
import com.tiptop.data.repository.local.DaoDevice
import com.tiptop.data.repository.local.DaoUser
import com.tiptop.domain.AuthRepository
import com.tiptop.domain.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val remoteDatabase: Firebase,
    private val auth: AuthRepository,
    private val usersLocalDb: DaoUser,
    private val devicesLocalDb: DaoDevice,
    private val deletedIdsLocalDb: DaoDeletedId,
    private val context: Context
) : UserRepository {

    override suspend fun saveRemoteUser(userRemote: UserRemote): ResponseResult<Boolean> {
        val remote =
            remoteDatabase.firestore.collection(Utils().getUsersFolder())
                .document(userRemote.id)
        return try {
            remote.set(userRemote).await()
            ResponseResult.Success(true)
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }

    override suspend fun saveRemoteUserPassword(
        password: String,
        id: String
    ): ResponseResult<Boolean> {
        val remote =
            remoteDatabase.firestore.collection(Utils().getUsersPasswordFolder())
                .document(id)

        return try {
            remote.set(mapOf(id to password)).await()
            ResponseResult.Success(true)
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }

    override suspend fun saveRemoteDevice(remoteDevice: DeviceRemote): ResponseResult<Boolean> {
        val remote =
            remoteDatabase.firestore.collection(Utils().getDevicesFolder())
                .document(remoteDevice.id)
        return try {
            remote.set(remoteDevice).await()
            ResponseResult.Success(true)
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }


    override suspend fun observeDevices(): Flow<Boolean> = callbackFlow {
        if (auth.currentUser != null) {
            val countDevices = devicesLocalDb.getCount()
            val lastUpdatedDate = if (countDevices == 1) 0 else devicesLocalDb.getLastUpdatedTime()
            remoteDatabase.firestore.collection(Utils().getDevicesFolder())
                .whereGreaterThanOrEqualTo("date", lastUpdatedDate)
                .addSnapshotListener { snapshot, error ->
                    snapshot?.let {
                        try {
                            val list = snapshot.map {
                                it.toObject(
                                    DeviceRemote::class.java
                                )
                            }

                            runBlocking {
                                devicesLocalDb.addMany(list.map {
                                    it.toLocal()
                                })
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

    override suspend fun observeDevice(): Flow<Boolean> = callbackFlow {
        if (auth.currentUser != null) {
            val deviceId = MyDevice(context).getUniquieId()
            remoteDatabase.firestore.collection(Utils().getDevicesFolder()).document(deviceId)
                .addSnapshotListener { snapshot, error ->
                    snapshot?.let {
                        try {
                            val device = snapshot.toObject(DeviceRemote::class.java)
                            runBlocking {
                                devicesLocalDb.add(device!!.toLocal())
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

    override suspend fun observeUsers(): Flow<Boolean> = callbackFlow {
        if (auth.currentUser != null) {
            val countUsers = usersLocalDb.getCount()
            val lastUpdatedDate = if (countUsers == 1) 0 else usersLocalDb.getLastUpdatedTime() ?: 0
            remoteDatabase.firestore.collection(Utils().getUsersFolder())
                .whereGreaterThanOrEqualTo("date", lastUpdatedDate)
                .addSnapshotListener { snapshot, error ->
                    snapshot?.let {
                        try {
                            val list = snapshot.map {
                                it.toObject(
                                    UserRemote::class.java
                                )
                            }
                            runBlocking {
                                usersLocalDb.addMany(list.map {
                                    it.toLocal()
                                })
                                trySend(true)
                            }
                        } catch (e: Exception) {
                            trySend(false)
                        }
                    }
                }
        }
        awaitClose { }

    }.flowOn(Dispatchers.IO)

    override suspend fun observeAuthState(): Flow<Boolean> = callbackFlow {
        auth.authState?.addAuthStateListener {
            if (it.currentUser != null) {
                trySend(true)
            } else {
                trySend(false)
            }
        }
        awaitClose { }
    }.flowOn(Dispatchers.IO)

    override suspend fun observeUser(): Flow<Boolean> = callbackFlow {
        if (auth.currentUser != null) {
            remoteDatabase.firestore.collection(Utils().getUsersFolder())
                .document(auth.currentUser?.uid ?: "")
                .addSnapshotListener { snapshot, error ->
                    snapshot?.let {
                        try {
                            val user = snapshot.toObject(UserRemote::class.java)
                            runBlocking {
                                usersLocalDb.add(user!!.toLocal())
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

    override suspend fun observeDeletedIds(): Flow<Boolean> = callbackFlow {
        if (auth.currentUser != null) {
            val lastUpdatedDate = deletedIdsLocalDb.getLastUpdatedTime()
            deletedIdsLocalDb.clear()
            remoteDatabase.firestore.collection(Utils().getDeletedIdsFolder())
                .whereGreaterThanOrEqualTo("date", lastUpdatedDate)
                .addSnapshotListener { snapshot, error ->
                    snapshot?.let {
                        try {
                            val list = snapshot.map {
                                it.toObject(
                                    DeletedIdRemote::class.java
                                )
                            }
                            val idList = list.map { it.id }
                            runBlocking {
                                val task1 =
                                    async { deletedIdsLocalDb.addMany(list.map { it.toLocal() }) }
                                val task2 = async { usersLocalDb.deleteUsers(idList) }
                                val task3 = async { devicesLocalDb.deleteDevices(idList) }
                                task1.await()
                                task2.await()
                                task3.await()
                                trySend(true)
                            }
                        } catch (e: Exception) {
                            trySend(false)
                        }
                    }
                }
        }
        awaitClose { }
    }.flowOn(Dispatchers.IO)

    override suspend fun addFakeUsers() {
        val randomSimbols = "abcdefghjkqrtyooupbmrlvdccsdegrmolp"
        val fakeUsers = ArrayList<UserLocal>()
        for (i in 1..200) {
            fakeUsers.add(
                UserLocal(
                    id = UUID.randomUUID().toString(),
                    deviceId = i.toString(),
                    email = "${randomSimbols.random()}${randomSimbols.random()}${randomSimbols.random()}@mail.com",
                    telegramUser = "user-$i",
                    permitted = true,
                    date = System.currentTimeMillis(),
                    dateAdded = System.currentTimeMillis(),
                )
            )
        }
        usersLocalDb.addMany(fakeUsers)
    }





    override fun getUsers(): Flow<List<UserLocal>> {
        return usersLocalDb.getAll().flowOn(Dispatchers.IO)
    }

    override fun getUserAndDevices(): Flow<Map<UserLocal, List<DeviceLocal>>> {
        return usersLocalDb.getUserAndDevices().flowOn(Dispatchers.IO)
    }

    override fun getUserAndDevices(seachText: String): Flow<Map<UserLocal, List<DeviceLocal>>> {
        return usersLocalDb.getUserAndDevices("%${seachText}%").flowOn(Dispatchers.IO)
    }

    override fun getUserDevices(userId: String): Flow<List<DeviceLocal>> {
        return devicesLocalDb.getUserDevices(userId).flowOn(Dispatchers.IO)
    }

    override fun getUsersByDeviceid(deviceId: String): Flow<List<UserLocal>> {
        return usersLocalDb.getUsersByDeviceId(deviceId).flowOn(Dispatchers.IO)
    }

    override fun getSearchedDevices(searchText: String): Flow<List<DeviceLocal>> {
        return devicesLocalDb.getSearchedDevices("%${searchText}%").flowOn(Dispatchers.IO)
    }

    override fun getSearchedAdminDevices(searchText: String): Flow<List<DeviceLocal>> {
        return devicesLocalDb.getSearchedAdminDevices("%${searchText}%").flowOn(Dispatchers.IO)
    }

    override fun getSearchedBlockedDevices(searchText: String): Flow<List<DeviceLocal>> {
        return devicesLocalDb.getSearchedBlockedDevices("%${searchText}%").flowOn(Dispatchers.IO)
    }

    override fun getAllDevices(): Flow<List<DeviceLocal>> {
        return devicesLocalDb.getAllDevices().flowOn(Dispatchers.IO)
    }

    override fun getAdminDevices(): Flow<List<DeviceLocal>> {
        return devicesLocalDb.getAdminDevices().flowOn(Dispatchers.IO)
    }

    override fun getBlockedDevices(): Flow<List<DeviceLocal>> {
        return devicesLocalDb.getBlockedDevices().flowOn(Dispatchers.IO)
    }

    override fun getCurrentDeviceFlowable(): Flow<DeviceLocal> {
        return devicesLocalDb.getCurrentDeviceFlowable(MyDevice(context).getUniquieId())
            .flowOn(Dispatchers.IO)
    }

    override fun getCurrentDevice(): DeviceLocal {
        return devicesLocalDb.getCurrentDevice(MyDevice(context).getUniquieId())
    }

    override suspend fun checkDeviceExist(): LiveData<DeviceRemote?> {
        if (auth.currentUser != null) {
            val deviceId = MyDevice(context).getUniquieId()
            val result =
                remoteDatabase.firestore.collection(Utils().getDevicesFolder()).document(deviceId)
                    .get()
                    .await()
            if (result?.exists() == true) {
                return try {
                    MutableLiveData(result.toObject<DeviceRemote>())
                } catch (e: Exception) {
                    MutableLiveData(null)
                }
            } else {
                return MutableLiveData(null)
            }
        } else {
            return MutableLiveData(null)
        }
    }

    override fun getCurrentUserFlowable(): Flow<UserLocal> {
        return usersLocalDb.getCurrentUserFlowable(auth.currentUser?.uid ?: "")
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getCurrentUser(): UserLocal {
        return usersLocalDb.getCurrentUser(auth.currentUser?.uid ?: "")
    }

    override suspend fun deleteUser(user: UserLocal): ResponseResult<Boolean> {
        val remoteUser =
            remoteDatabase.firestore.collection(Utils().getUsersFolder())
                .document(user.id)
        val remoteDeletedFolder =
            remoteDatabase.firestore.collection(Utils().getDeletedIdsFolder())
                .document(user.id)
        return try {
            val emailCurrent = auth.currentUser?.email
            val passwordCurrent =
                remoteDatabase.firestore.collection(Utils().getUsersPasswordFolder())
                    .document(auth.currentUser?.uid ?: "").get().await()[auth.currentUser?.uid
                    ?: ""].toString()

            val passwordUser = remoteDatabase.firestore.collection(Utils().getUsersPasswordFolder())
                .document(user.id).get().await()[user.id].toString()
            auth.signOut()
            auth.signIn(user.email, passwordUser)
            auth.currentUser?.delete()?.await()
            auth.signOut()
            auth.signIn(emailCurrent!!, passwordCurrent)
            remoteUser.delete().await()
            remoteDeletedFolder.set(DeletedIdRemote(user.id, System.currentTimeMillis())).await()
            usersLocalDb.delete(user.id)

            ResponseResult.Success(true)
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }

    override suspend fun deleteDevice(deviceId: String): ResponseResult<Boolean> {
        val remote =
            remoteDatabase.firestore.collection(Utils().getDevicesFolder())
                .document(deviceId)
        val remoteDeletedFolder =
            remoteDatabase.firestore.collection(Utils().getDeletedIdsFolder())
                .document(deviceId)
        return try {
            remote.delete().await()
            remoteDeletedFolder.set(DeletedIdRemote(deviceId, System.currentTimeMillis())).await()
            usersLocalDb.delete(deviceId)
            ResponseResult.Success(true)
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }

    override fun clearDevices() {
        val deviceId = MyDevice(context).getUniquieId()
        if (devicesLocalDb.getCount() > 1) {
            devicesLocalDb.clear(deviceId)
        }
    }

    override suspend fun getUserPassword(userId: String): ResponseResult<String> {
        val remote =
            remoteDatabase.firestore.collection(Utils().getUsersPasswordFolder())
                .document(userId)
        var password = ""
        return try {
            remote.get().addOnSuccessListener {
                password = it[userId].toString()
            }.await()
            ResponseResult.Success(password)
        } catch (e: Exception) {
            ResponseResult.Failure(e.message)
        }
    }


}

