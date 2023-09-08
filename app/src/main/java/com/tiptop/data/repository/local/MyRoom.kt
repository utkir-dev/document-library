package com.tiptop.data.repository.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tiptop.app.common.Utils
import com.tiptop.data.models.local.DeletedIdLocal
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.local.FolderLocal
import com.tiptop.data.models.local.UserLocal


@Database(
    entities = [
        DeviceLocal::class,
        DocumentLocal::class,
        UserLocal::class,
        DeletedIdLocal::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MyRoom : RoomDatabase() {

    abstract fun DocumentDao(): DaoDocument
    abstract fun UserDao(): DaoUser
    abstract fun DeviceDao(): DaoDevice
    abstract fun DeletedIdDao(): DaoDeletedId

    companion object {
        @Volatile
        private var INSTANSE: MyRoom? = null
        val DATABASE_NAME = Utils().getDatabaseName()
        fun getInstance(ctx: Context): MyRoom {
            return INSTANSE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext,
                    MyRoom::class.java,
                    DATABASE_NAME
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANSE = instance
                instance
            }
        }
    }
}
