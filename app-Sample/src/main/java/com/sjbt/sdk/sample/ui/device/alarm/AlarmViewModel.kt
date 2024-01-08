package com.sjbt.sdk.sample.ui.device.alarm

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmAlarm
import com.blankj.utilcode.util.GsonUtils
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import timber.log.Timber

data class AlarmState(
    val requestAlarms: Async<ArrayList<WmAlarm>> = Uninitialized,
)

sealed class AlarmEvent {
    class RequestFail(val throwable: Throwable) : AlarmEvent()
    class AlarmInserted(val position: Int) : AlarmEvent()
    class AlarmRemoved(val position: Int) : AlarmEvent()
    class AlarmMoved(val fromPosition: Int, val toPosition: Int) : AlarmEvent()
    class AlarmFial(val throwable: Throwable) : AlarmEvent()
    object NavigateUp : AlarmEvent()
}


class AlarmViewModel : StateEventViewModel<AlarmState, AlarmEvent>(AlarmState()) {
    //    private val deviceManager = Injector.getDeviceManager()
    init {
        requestAlarms()
        observeAlarms()
    }

    fun requestAlarms() {
        viewModelScope.launch {
            state.copy(requestAlarms = Loading()).newState()
            UNIWatchMate.wmApps.appAlarm.getAlarmList.toFlowable().asFlow().catch {
                state.copy(requestAlarms = Fail(it)).newState()
                AlarmEvent.RequestFail(it).newEvent()
            }.collect{
                state.copy(requestAlarms = Success(ArrayList(AlarmHelper.sort(it)))).newState()
            }
        }
    }
    fun observeAlarms() {
        viewModelScope.launch {
            state.copy(requestAlarms = Loading()).newState()
            UNIWatchMate.wmApps.appAlarm.observeAlarmList.asFlow().catch {
//                state.copy(requestAlarms = Fail(it)).newState()
//                AlarmEvent.RequestFail(it).newEvent()
                ToastUtil.showToast(it.message)
                Timber.e(it)
            }.collect{
                Timber.d(GsonUtils.toJson(it))
                state.copy(requestAlarms = Success(ArrayList(AlarmHelper.sort(it)))).newState()
            }
        }
    }
    private fun findAlarmAddPosition(alarm: WmAlarm, list: List<WmAlarm>): Int {
        var addPosition: Int? = null
        for (i in list.indices) {
            if (AlarmHelper.comparator.compare(alarm, list[i]) < 0) {
                addPosition = i
                break
            }
        }
        if (addPosition == null) {
            addPosition = list.size
        }
        return addPosition
    }

    fun addAlarm(alarm: WmAlarm) {
        viewModelScope.launch {

            val alarms = state.requestAlarms()
            if (alarms != null) {
                val addPosition = findAlarmAddPosition(alarm, alarms)
                alarms.add(addPosition, alarm)
                action()
                AlarmEvent.AlarmInserted(addPosition).newEvent()
            }
        }
    }

    /**
     * @param position Delete position
     */
    fun deleteAlarm(position: Int) {
        viewModelScope.launch {
            val alarms = state.requestAlarms()
            if (alarms != null && position < alarms.size) {
                val alarm = alarms.removeAt(position)
                action()
                AlarmEvent.AlarmRemoved(position).newEvent()
            }
        }
    }

    /**
     * @param position Modify position
     * @param alarmModified Modified data
     */
    fun modifyAlarm(position: Int, alarmModified: WmAlarm) {
        viewModelScope.launch {
            val alarms = state.requestAlarms()
            if (alarms != null && position < alarms.size) {
                if (alarms.contains(alarmModified)) {
                    AlarmEvent.AlarmFial(Throwable()).newEvent()
                    throw IllegalStateException()//不能直接改list里的数据
                }
                alarms.removeAt(position)
                val addPosition = findAlarmAddPosition(alarmModified, alarms)
                alarms.add(addPosition, alarmModified)
                action()
                Timber.i(  "modifyAlarm")
                AlarmEvent.AlarmMoved(position, addPosition).newEvent()
            }
        }
    }

    fun sendNavigateUpEvent() {
        viewModelScope.launch {
            delay(1000)
            AlarmEvent.NavigateUp.newEvent()
        }
    }

    suspend fun action() {
        val alarms = state.requestAlarms()
        if (alarms != null) {
           val actionResult = UNIWatchMate.wmApps.appAlarm.updateAlarmList(alarms).await()
            Timber.d("actionResult=$actionResult")
        }
    }

}