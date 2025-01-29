package com.soordinary.old.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.soordinary.old.room.entity.Contact

/**
 * 联系人的CRUD接口
 */
@Dao
interface ContactDao {

    // 插入单个联系人
    @Insert
    fun insertContact(contact: Contact): Unit

    // 插入多个联系人
    @Insert
    fun insertContacts(vararg contacts: Contact): Unit

    // 删除某个联系人
    @Delete
    fun deleteContact(contact: Contact): Unit

    // 查询所有联系人
    @Query("SELECT * FROM contacts")
    fun getAllContacts(): LiveData<List<Contact>>
}