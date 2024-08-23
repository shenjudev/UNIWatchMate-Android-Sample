package com.sjbt.sdk.sample.ui.device.language

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmLanguage
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import timber.log.Timber

data class DialState(
    val requestLanguages: Async<MutableList<WmLanguage>> = Uninitialized,
)

sealed class DialEvent {
    class RequestFail(val throwable: Throwable) : DialEvent()

    class LanguageSet(val position: Int) : DialEvent()
}

class LanguagelInstalledViewModel : StateEventViewModel<DialState, DialEvent>(DialState()) {
    //  private val deviceManager = Injector.getDeviceManager()
    init {
        requestLanguages()
    }

    fun requestLanguages() {
        viewModelScope.launch {
            state.copy(requestLanguages = Loading()).newState()
            runCatchingWithLog {
                UNIWatchMate.wmApps.appLanguage.syncLanguageList.await()
            }.onSuccess {
                Timber.e( "language list: $it")
                if (it is MutableList) {
                    state.copy(requestLanguages = Success(it)).newState()
                } else {
                    state.copy(requestLanguages = Fail(Throwable("result is not a mutable list")))
                        .newState()
                }
            }.onFailure {
                state.copy(requestLanguages = Fail(it)).newState()
                DialEvent.RequestFail(it).newEvent()
            }
        }
    }

    /**
     * @param position Delete position
     */
    fun setLanguage(position: Int) {
        viewModelScope.launch {
            val wmLanguages = state.requestLanguages()
            if (wmLanguages != null && position < wmLanguages.size) {
              UNIWatchMate.wmApps.appLanguage.setLanguage(wmLanguages[position]).await()

                for ((index, language) in wmLanguages.withIndex()) {
                    language.curr_lang = index == position
                }
                DialEvent.LanguageSet(position).newEvent()
            }
        }
    }

}