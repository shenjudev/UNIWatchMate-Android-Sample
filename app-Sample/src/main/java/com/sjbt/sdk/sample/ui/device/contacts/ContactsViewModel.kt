package com.sjbt.sdk.sample.ui.device.contacts

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmContact
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import timber.log.Timber

data class ContactsState(
    val requestContacts: Async<ArrayList<WmContact>> = Uninitialized,
)


sealed class ContactsEvent {
    class RequestFail(val throwable: Throwable) : ContactsEvent()
    class RequestEmergencyFail(val throwable: Throwable) : ContactsEvent()
    class Inserted(val pos: Int) : ContactsEvent()
    class Update100Success : ContactsEvent()
    class Removed(val position: Int) : ContactsEvent()
    class UpdateFail : ContactsEvent()
    object NavigateUp : ContactsEvent()
}


class ContactsViewModel() : StateEventViewModel<ContactsState, ContactsEvent>(ContactsState()) {

    private val deviceManager = Injector.getDeviceManager()

    init {
        requestContacts()
    }

    fun requestContacts() {
        viewModelScope.launch {
            Timber.i("requestContacts: start")

            state.copy(requestContacts = Loading()).newState()

            runCatchingWithLog {
                Timber.i("requestContacts: awaitFirst")
                UNIWatchMate.wmApps.appContact.getContactList.await()
            }.onSuccess {
                state.copy(requestContacts = Success(ArrayList(it))).newState()
            }.onFailure {
                state.copy(requestContacts = Fail(it)).newState()
                ContactsEvent.RequestFail(it).newEvent()
            }
            Timber.i("requestContacts: end")

        }

    }

    fun addContacts(contact: WmContact) {
        viewModelScope.launch {
            Timber.i("addContacts: start")
            val list = state.requestContacts()
            if (list != null) {
                var exist = false
                for (item in list) {
                    if (item.number == contact.number && item.name == contact.name) {
                        exist = true
                        break
                    }
                }
                if (!exist) {
                    list.add(contact)
                    runCatchingWithLog {
                        Timber.i("addContacts: action")
                        action(list)
                    }.onSuccess {
                        ContactsEvent.Inserted(list.size).newEvent()
                    }.onFailure {
                        ContactsEvent.UpdateFail().newEvent()
                    }
                } else {
                    ContactsEvent.UpdateFail().newEvent()
                }
                Timber.i("addContacts: end")

            }
        }
    }

    fun addContacts(contacts: MutableList<WmContact>) {
        viewModelScope.launch {
            val list = state.requestContacts()
            if (list != null) {
                for (contact in contacts) {
                    var exist = false
                    var removedItem: WmContact? = null
                    for (item in list) {
                        if (item.number == contact.number && item.name == contact.name) {
                            exist = true
                            list.remove(item)
                            removedItem = item
                            break
                        }
                    }
                    if (!exist) {
                        list.add(contact)
                    } else {
                        if (removedItem != null) {
                            list.add(removedItem)
                        }
                    }
                }
                runCatchingWithLog {
                    action(list)
                }.onSuccess {
                    ContactsEvent.Update100Success().newEvent()
                }.onFailure {
                    ContactsEvent.UpdateFail().newEvent()
                }

            }
        }
    }

    fun add100Contacts(contacts: MutableList<WmContact>) {
        viewModelScope.launch {
            val list = state.requestContacts()
            if (list != null) {
                list.clear()
                list.addAll(contacts)
                runCatchingWithLog {
                    action(list)
                }.onSuccess {
                    ContactsEvent.Update100Success().newEvent()
                }.onFailure {
                    ContactsEvent.UpdateFail().newEvent()
                }
            }
        }
    }

    /**
     * @param position 要删除的位置(The location to delete)
     */
    fun deleteContacts(position: Int) {
        viewModelScope.launch {
            val list = state.requestContacts()
            if (list != null && position < list.size) {
                list.removeAt(position)
                runCatchingWithLog {
                    action(list)
                }.onSuccess {
                    ContactsEvent.Removed(position).newEvent()
                }.onFailure {
                    ContactsEvent.UpdateFail().newEvent()
                }
            }
        }
    }

    fun sendNavigateUpEvent() {
        viewModelScope.launch {
            delay(1000)
            ContactsEvent.NavigateUp.newEvent()
        }
    }

    suspend fun action(list: ArrayList<WmContact>): Boolean {
        val result = UNIWatchMate.wmApps.appContact.updateContactList(list).await()
        Timber.i("setContactsAction result=$result ")
        return result
    }
}
