package com.soordinary.old.repository

import com.soordinary.old.OldApplication
import com.soordinary.old.room.database.ContactDatabase
import com.soordinary.old.room.entity.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Contact仓库
 */
class ContactRepository {
    private val contactDao = ContactDatabase.getDatabase(OldApplication.context).contactDao()

    suspend fun insertContact(contact: Contact) {
        withContext(Dispatchers.IO) {
            contactDao.insertContact(contact)
        }
    }

    suspend fun insertContacts(vararg contacts: Contact) {
        withContext(Dispatchers.IO) {
            // 展开为可变参数
            contactDao.insertContacts(*contacts)
        }
    }

    suspend fun deleteContact(contact: Contact) {
        withContext(Dispatchers.IO) {
            contactDao.deleteContact(contact)
        }
    }

    // 获取所有联系人的livedata
    fun getAllContactsLivedata() =
        contactDao.getAllContacts()
}