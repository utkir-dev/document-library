package com.tiptop.data.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.UserLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoUser {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(user: UserLocal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMany(users: List<UserLocal>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(user: UserLocal): Int

    @Query("SELECT COUNT(*) FROM users")
    fun getCount(): Int

    @Query("DELETE FROM users WHERE id=:id")
    suspend fun delete(id: String): Int

    @Query("delete from users where id in (:idList)")
    fun deleteUsers(idList: List<String>)

    @Query("DELETE FROM users")
    fun clear()

    @Query("SELECT * FROM users WHERE id=:id limit 1")
    fun getCurrentUserFlowable(id: String): Flow<UserLocal>

    @Query("SELECT * FROM users WHERE id=:id limit 1")
    suspend fun getCurrentUser(id: String): UserLocal

    @Query("SELECT * FROM users ORDER BY date DESC")
    fun getAll(): Flow<List<UserLocal>>

    @Query("SELECT MAX(date) FROM users")
    fun getLastUpdatedTime(): Long

    @Query("SELECT * FROM users WHERE deviceId =:deviceId ORDER BY date DESC")
    fun getUsersByDeviceId(deviceId: String): Flow<List<UserLocal>>

    @Query("SELECT * FROM users LEFT JOIN  devices ON users.id = devices.userId ORDER BY users.date DESC")
    fun getUserAndDevices(): Flow<Map<UserLocal, List<DeviceLocal>>>

    @Query("SELECT * FROM users LEFT JOIN  devices ON users.id = devices.userId WHERE users.email LIKE :seachText  ORDER BY users.date DESC")
    fun getUserAndDevices(seachText: String): Flow<Map<UserLocal, List<DeviceLocal>>>

}