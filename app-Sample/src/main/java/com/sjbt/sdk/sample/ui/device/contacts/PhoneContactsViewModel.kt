package com.sjbt.sdk.sample.ui.device.contacts

import android.annotation.SuppressLint
import android.provider.ContactsContract
import androidx.lifecycle.viewModelScope
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.model.device.PhoneContact
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

data class PhoneContactsState(
    val requestPhoneContacts: Async<ArrayList<PhoneContact>> = Uninitialized,
)

sealed class PhoneContactsEvent {
    class RequestFail(val throwable: Throwable) : PhoneContactsEvent()
    object NavigateUp : PhoneContactsEvent()
}

class PhoneContactsViewModel() :
    StateEventViewModel<PhoneContactsState, PhoneContactsEvent>(PhoneContactsState()) {

    private val applicationScope = Injector.getApplicationScope()

    init {
        requestContacts()
    }

    fun requestContacts() {
        viewModelScope.launch {
            Timber.i("requestContacts: start")
            state.copy(requestPhoneContacts = Loading()).newState()
            applicationScope.launch {
                runCatchingWithLog {
                    getAllContacts()
                }.onSuccess {
                    state.copy(requestPhoneContacts = Success(ArrayList(it))).newState()
                }.onFailure {
                    state.copy(requestPhoneContacts = Fail(it)).newState()
                    PhoneContactsEvent.RequestFail(it).newEvent()
                }
                Timber.i("requestContacts: end")
            }
        }
    }

    fun sendNavigateUpEvent() {
        viewModelScope.launch {
            delay(1000)
            PhoneContactsEvent.NavigateUp.newEvent()
        }
    }

    @SuppressLint("Range")
    fun getAllContacts(): ArrayList<PhoneContact> {
        val context = MyApplication.instance.applicationContext
        val contacts: ArrayList<PhoneContact> = ArrayList()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null
        )
        while (cursor!!.moveToNext()) {
            //新建一个联系人实例
            val contactId = cursor!!.getString(
                cursor
                    .getColumnIndex(ContactsContract.Contacts._ID)
            )
            //获取联系人姓名
            val name = cursor!!.getString(
                cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            )

            //获取联系人电话号码
            val phoneCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                null,
                null
            )
            var phoneNum = ""
            while (phoneCursor!!.moveToNext()) {
                var phone =
                    phoneCursor!!.getString(phoneCursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phone = phone.replace("-", "")
                phone = phone.replace(" ", "")
                phoneNum = phone
            }

            val temp = PhoneContact(name, phoneNum,false)

            //获取联系人备注信息
            val noteCursor = context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data._ID,
                    ContactsContract.CommonDataKinds.Nickname.NAME
                ),
                ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'",
                arrayOf(contactId),
                null
            )
            temp?.let { contacts.add(it) }
            //记得要把cursor给close掉
            phoneCursor!!.close()
            noteCursor!!.close()
        }
        cursor!!.close()
        return contacts
    }
}
