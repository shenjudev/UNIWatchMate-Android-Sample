package com.sjbt.sdk.sample.ui.device.contacts

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.settings.WmEmergencyCall
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import timber.log.Timber

data class EmergencyContactsState(
    val requestEmergencyCall: Async<WmEmergencyCall> = Uninitialized,
)

sealed class EmergencyCallEvent {
    class RequestFail(val throwable: Throwable) : EmergencyCallEvent()
    class setEmergencyContactSuccess(val wmEmergencyCall: WmEmergencyCall) : EmergencyCallEvent()
    class setEmergencyContactFail(val throwable: Throwable) : EmergencyCallEvent()
    object NavigateUp : EmergencyCallEvent()
}


class EmergencyContactViewModel :
    StateEventViewModel<EmergencyContactsState, EmergencyCallEvent>(EmergencyContactsState()) {

    init {
        requestEmegencyCall()
    }

    fun requestEmegencyCall() {
        viewModelScope.launch {
            state.copy(requestEmergencyCall = Loading()).newState()
            runCatchingWithLog {
                Timber.i( "observableEmergencyContacts")
                UNIWatchMate.wmApps.appContact.observableEmergencyContacts().awaitFirst()
//                WmEmergencyCall(false, mutableListOf<WmContact>())
            }.onSuccess {
                UNIWatchMate.wmLog.logI(
                    "EmergencyContactViewModel",
                    "observableEmergencyContacts result$it"
                )
                state.copy(requestEmergencyCall = Success(it)).newState()
            }.onFailure {
                state.copy(requestEmergencyCall = Fail(it)).newState()
                EmergencyCallEvent.RequestFail(it).newEvent()
            }
        }
    }

    fun setEmergencyEnbalbe(enable: Boolean) {
        viewModelScope.launch {
            val call = state.requestEmergencyCall()
            call?.let {
                it.isEnabled = enable
                Timber.d("setEmergencyEnbalbe $it")
                setEmergencyCall(it)
            }
        }
    }

    fun setEmergencyContact(contact: WmContact) {
        viewModelScope.launch {
            val call = state.requestEmergencyCall()
            call?.let {
                it.emergencyContacts.clear()
                it.emergencyContacts.add(contact)
                Timber.d("setEmergencyContact $contact")
                setEmergencyCall(it)

            }
        }
    }

    suspend fun setEmergencyCall(call: WmEmergencyCall) {
        runCatchingWithLog {
            val result = UNIWatchMate.wmApps.appContact.updateEmergencyContact(call).await()
            Timber.d(  "result=$result")
        }.onSuccess {
            EmergencyCallEvent.setEmergencyContactSuccess(call).newEvent()
        }.onFailure {
            EmergencyCallEvent.setEmergencyContactFail(it).newEvent()

        }

    }

}