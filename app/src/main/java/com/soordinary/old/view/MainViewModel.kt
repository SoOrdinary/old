package com.soordinary.old.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soordinary.old.repository.ContactRepository
import com.soordinary.old.room.entity.Contact
import kotlinx.coroutines.launch

class MainViewModel:ViewModel() {

    private var contactRepository = ContactRepository()
    private var _allContactsLiveData = contactRepository.getAllContactsLivedata()

    var contactList = ArrayList<Contact>()
    val allContactsLiveData get() = _allContactsLiveData

    fun insertContact(contact: Contact) = viewModelScope.launch {
        contactRepository.insertContacts(contact)
    }

    fun insertContacts(vararg contacts: Contact) = viewModelScope.launch {
        contactRepository.insertContacts(*contacts)
    }

    fun deleteContact(contact: Contact) = viewModelScope.launch {
        contactRepository.deleteContact(contact)
    }
}