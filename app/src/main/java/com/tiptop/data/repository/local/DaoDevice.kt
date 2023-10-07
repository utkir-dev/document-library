package com.tiptop.data.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tiptop.data.models.local.DeviceLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoDevice {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(device: DeviceLocal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMany(devices: List<DeviceLocal>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(device: DeviceLocal): Int

    @Query("SELECT COUNT(*) FROM devices")
    fun getCount(): Int

    @Query("DELETE FROM devices WHERE id=:id")
    suspend fun delete(id: String): Int

    @Query("delete from devices where id in (:idList)")
    fun deleteDevices(idList: List<String>)

    @Query("DELETE FROM devices WHERE id !=:id")
    fun clear(id: String)

    @Query("DELETE FROM devices")
    fun clear()

    @Query("SELECT * FROM devices WHERE id=:id limit 1")
    fun getCurrentDeviceFlowable(id: String): Flow<DeviceLocal>

    @Query("SELECT * FROM devices WHERE id=:id limit 1")
    fun getCurrentDevice(id: String): DeviceLocal

    @Query("SELECT * FROM devices ORDER BY date DESC")
    fun getAllDevices(): Flow<List<DeviceLocal>>

    @Query("SELECT * FROM devices WHERE name LIKE :searchText  ORDER BY date DESC")
    fun getSearchedDevices(searchText: String): Flow<List<DeviceLocal>>

    @Query("SELECT * FROM devices WHERE admin=:isAdmin and blocked=:notBlocked and name LIKE :searchText  ORDER BY date DESC")
    fun getSearchedAdminDevices(
        searchText: String,
        isAdmin: Boolean = true,
        notBlocked: Boolean = false
    ): Flow<List<DeviceLocal>>

    @Query("SELECT * FROM devices WHERE  blocked=:isBlocked and name LIKE :searchText  ORDER BY date DESC")
    fun getSearchedBlockedDevices(
        searchText: String,
        isBlocked: Boolean = true
    ): Flow<List<DeviceLocal>>

    @Query("SELECT * FROM devices WHERE admin=:isAdmin and blocked=:notBlocked ORDER BY date DESC")
    fun getAdminDevices(
        isAdmin: Boolean = true,
        notBlocked: Boolean = false
    ): Flow<List<DeviceLocal>>

    @Query("SELECT * FROM devices WHERE blocked=:isBlocked ORDER BY date DESC")
    fun getBlockedDevices(isBlocked: Boolean = true): Flow<List<DeviceLocal>>

    @Query("SELECT * FROM devices  WHERE userId=:userId order by date")
    fun getUserDevices(userId: String): Flow<List<DeviceLocal>>

    @Query("SELECT MAX(date) FROM devices  WHERE id!=:deviceId")
    fun getLastUpdatedTime(deviceId: String): Long
}