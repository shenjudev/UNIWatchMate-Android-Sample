package com.sjbt.sdk.sample.ui.device.sport

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmSport
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ResourceUtils
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.model.LocalSportLibrary
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.getSportLibrary
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await

data class SportState(
    val requestSports: Async<MutableList<WmSport>> = Uninitialized,
)

sealed class SportEvent {
    class RequestFail(val throwable: Throwable) : SportEvent()

    class SportRemoved(val position: Int) : SportEvent()

    class SportInstallSuccess(val position: Int) : SportEvent()
    class SportSortSuccess() : SportEvent()
    class SportInstallFail(val msg: String) : SportEvent()
    class SportUpdateFail(val msg: String?) : SportEvent()
}

class SportInstalledViewModel : StateEventViewModel<SportState, SportEvent>(SportState()) {
    var localSportLibrary: LocalSportLibrary? = null

    init {
        requestInstallSports()
    }

    fun requestInstallSports() {
        viewModelScope.launch {
            state.copy(requestSports = Loading()).newState()
            runCatchingWithLog {
                localSportLibrary = getSportLibrary()
                refreshInstallState(
                    UNIWatchMate.wmApps.appSport.getSportList.await(),
                    localSportLibrary
                )
            }.onSuccess {
                if (it is MutableList) {
                    state.copy(requestSports = Success(it)).newState()
                } else {
                    state.copy(requestSports = Fail(Throwable("result is not a mutable list")))
                        .newState()
                }
            }.onFailure {
                state.copy(requestSports = Fail(it)).newState()
                SportEvent.RequestFail(it).newEvent()
            }
        }
    }

    private fun refreshInstallState(
        wmSports: List<WmSport>?,
        localSportLibrary: LocalSportLibrary?,
    ): List<WmSport>? {
        wmSports?.let {
            for (bean in wmSports) {
                localSportLibrary?.let {
                    for (localSport in localSportLibrary!!.sports) {
                        if (bean.id == localSport.id) {
                            localSport.installed = true
                        }
                    }
                }
            }
        }

        return wmSports
    }

    /**
     * @param position Delete position
     */
    fun deleteSport(position: Int) {
        viewModelScope.launch {
            val sports = state.requestSports()
            if (sports != null && position + 8 < sports.size) {
                sports.removeAt(position + 8)
                runCatchingWithLog {
                    UNIWatchMate.wmApps.appSport.updateSportList(sports).await()
                }.onSuccess {
                    SportEvent.SportRemoved(position).newEvent()
                }.onFailure {
                    ToastUtil.showToast(it.message)
                    SportEvent.SportUpdateFail(it.message).newEvent()
                }
            }
        }
    }

    fun sortFixedSportList(fromOnStart: Int, to: Int) {
        viewModelScope.launch {
            val sports = state.requestSports()
            if (sports != null) {
                sports.add(to, sports.removeAt(fromOnStart))
                runCatchingWithLog {
                    UNIWatchMate.wmApps.appSport.updateSportList(sports).await()
                }.onSuccess {
                    SportEvent.SportSortSuccess().newEvent()
                }.onFailure {
                    ToastUtil.showToast(it.message)
                    SportEvent.SportUpdateFail(it.message).newEvent()

                }
            }
        }
    }

    fun installContactContain(position: Int, localSport: LocalSportLibrary.LocalSport) {
        viewModelScope.launch {
            val wmSports = state.requestSports()

            wmSports?.let {
                if (it.size >= 20) {
                    SportEvent.SportInstallFail(MyApplication.instance.resources.getString(R.string.ds_sport_at_most)).newEvent()
                    return@launch
                }
                val wmSport = WmSport(localSport.id, localSport.type, localSport.buildIn)
                wmSports.add(wmSport)
                runCatchingWithLog {
                    val result = UNIWatchMate.wmApps.appSport.updateSportList(wmSports).await()
                    result
                }.onSuccess {
                    localSport.installed = it
                    SportEvent.SportInstallSuccess(position).newEvent()
                }.onFailure {
                    localSport.installed = false
                    wmSports.removeAt(wmSports.size - 1)
                    SportEvent.SportInstallFail(it.toString()).newEvent()
                }
            }
        }
    }

    fun getNameById(id: Int): String {
        return localSportLibrary?.getNameById(id)?:""
    }
}