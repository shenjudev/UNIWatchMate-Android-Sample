package com.sjbt.sdk.sample.ui.device.sport

import android.util.SparseArray
import androidx.core.util.size
import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmSport
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
    val requestSports: Async<SparseArray<MutableList<WmSport>>> = Uninitialized,
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
                val fixedList =    refreshInstallState(
                    UNIWatchMate.wmApps.appSport.getFixedSportList.await(),
                    localSportLibrary
                )
                val dynamicList =   refreshInstallState(
                    UNIWatchMate.wmApps.appSport.getDynamicSportList.await(),
                    localSportLibrary
                )
                val sparseArray = SparseArray<MutableList<WmSport>>()
                sparseArray[0]= fixedList as MutableList<WmSport>?
                sparseArray[1]= dynamicList as MutableList<WmSport>?
                sparseArray
            }.onSuccess {
                state.copy(requestSports = Success(it)).newState()
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
            val sportsMap = state.requestSports()
            if (sportsMap != null && sportsMap.size>1&&sportsMap[1]!=null&&sportsMap[1].size>position) {
               val removedSport = sportsMap[1].removeAt(position )
                runCatchingWithLog {
                    UNIWatchMate.wmApps.appSport.updateDynamicSportList(sportsMap[1]).await()
                }.onSuccess {
                    if (it) {
                        SportEvent.SportRemoved(position).newEvent()
                    }else{
                        sportsMap[1].add(position,removedSport)
                        SportEvent.SportUpdateFail("删除失败").newEvent()
                    }
                }.onFailure {
                    ToastUtil.showToast(it.message)
                    sportsMap[1].add(position,removedSport)
                    SportEvent.SportUpdateFail(it.message).newEvent()
                }
            }
        }
    }

    fun sortFixedSportList(fromOnStart: Int, to: Int) {
        viewModelScope.launch {
            val sportsMap = state.requestSports()
            if (sportsMap != null&&sportsMap[0]!=null) {
                sportsMap[0].add(to, sportsMap[0].removeAt(fromOnStart))
                runCatchingWithLog {
                    UNIWatchMate.wmApps.appSport.updateFixedSportList(sportsMap[0]).await()
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
            val wmSports = state.requestSports()?.get(1)

            wmSports?.let {
                if (it.size >= 12) {
                    SportEvent.SportInstallFail(MyApplication.instance.resources.getString(R.string.ds_sport_at_most)).newEvent()
                    return@launch
                }
                val wmSport = WmSport(localSport.id, localSport.type, localSport.buildIn)
                wmSports.add(wmSport)
                runCatchingWithLog {
                    val result = UNIWatchMate.wmApps.appSport.updateDynamicSportList(wmSports).await()
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