package com.soordinary.old.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.soordinary.old.room.dao.ContactDao
import com.soordinary.old.room.entity.Contact
import kotlin.concurrent.Volatile

/**
 * Contact的数据库
 */
@Database(version = 1, entities = [Contact::class], exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao // 获取 Dao

    companion object {
        @Volatile
        private var instance: ContactDatabase? = null // 单例实例

        // 获取 TodoDatabase 实例
        @Synchronized
        fun getDatabase(context: Context): ContactDatabase {
            instance?.let { return it }
            databaseBuilder(context.applicationContext, ContactDatabase::class.java, "task_database")
                .build()
                .apply { instance = this }
            return instance as ContactDatabase
        }
    }
}

