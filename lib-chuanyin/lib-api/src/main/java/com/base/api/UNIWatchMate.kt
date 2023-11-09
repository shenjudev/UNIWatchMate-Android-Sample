package com.base.api

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import com.base.sdk.AbUniWatch
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.WmDevice
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.common.WmDiscoverDevice
import com.base.sdk.entity.common.WmTimeUnit
import com.base.sdk.entity.data.WmBatteryInfo
import com.base.sdk.entity.settings.WmDeviceInfo
import com.base.sdk.port.log.AbWmLog
import com.base.sdk.port.AbWmTransferFile
import com.base.sdk.port.app.AbWmApps
import com.base.sdk.port.sync.AbWmSyncs
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

object UNIWatchMate : AbUniWatch() {
    private val TAG = "UNIWatchMate"

    private lateinit var application: Application
    private val uniWatches: MutableList<AbUniWatch> = ArrayList()

    private val uniWatchSubject = BehaviorSubject.create<AbUniWatch>()
    private val uniWatchObservable = BehaviorObservable<AbUniWatch>(uniWatchSubject)

    override val wmSettings: AbWmSettings = AbWmSettingsDelegate(uniWatchObservable)
    override val wmApps: AbWmApps = AbWmAppDelegate(uniWatchObservable)
    override val wmSync: AbWmSyncs = AbWmSyncDelegate(uniWatchObservable)
    override val wmLog: AbWmLog = AbWmLogDelegate(uniWatchObservable)
    override val wmTransferFile: AbWmTransferFile = AbWmTransferDelegate(uniWatchObservable)

    fun init(application: Application, uniWatches: List<AbUniWatch>) {
//        if (this::application.isInitialized) {
//            return
//        }
        this.uniWatches.clear()
        this.application = application
        this.uniWatches.addAll(uniWatches)

        if (uniWatches.isEmpty()) {
            throw RuntimeException("No Sdk Register Exception!")
        }

        this.uniWatches.forEach {
            uniWatchSubject.onNext(it)
        }

    }

    fun observeUniWatchChange(): Observable<AbUniWatch> {
        return uniWatchObservable
    }

    override fun connect(address: String, bindInfo: WmBindInfo): WmDevice? {
        return uniWatchSubject.value.connect(address, bindInfo)
    }

    override fun connect(device: BluetoothDevice, bindInfo: WmBindInfo): WmDevice? {
        return uniWatchSubject.value.connect(device, bindInfo)
    }

    override fun connectScanQr(qrString: String, bindInfo: WmBindInfo): WmDevice? {
        uniWatches.forEach {
            val result = it.connectScanQr(qrString, bindInfo)
            if (result != null) {
                uniWatchSubject.onNext(it)
                return result
            }
        }
        return null
    }

    override fun disconnect() {
        uniWatchSubject.value.disconnect()
    }

    override fun reset(): Completable {
        return uniWatchSubject.value.reset()
    }

    override fun reboot(): Completable {
        return uniWatchSubject.value.reboot()
    }

    override val observeConnectState: Observable<WmConnectState> = uniWatchSubject.switchMap {
        it.observeConnectState
    }.distinctUntilChanged()

    override fun getConnectState(): WmConnectState {
        val watch = uniWatchSubject.value ?: return WmConnectState.DISCONNECTED
        return watch.getConnectState()
    }

    override fun getDeviceModel(): WmDeviceModel? {
        return uniWatchSubject.value?.getDeviceModel()
    }

     override fun setDeviceModel(wmDeviceModel: WmDeviceModel): Boolean {
        if (uniWatchSubject.value?.getDeviceModel() == wmDeviceModel) {
            //deviceMode不变
            return false
        }
        for (i in uniWatches.indices) {
            val uniWatch = uniWatches[i]
            if (uniWatch.setDeviceModel(wmDeviceModel)) {
                uniWatchSubject.onNext(uniWatch)
                return true
            }
        }
        //出现此情况，说明调用者没有正确调用 init
        throw RuntimeException("No Sdk Match Exception!")
    }

    override fun getDeviceInfo(): Single<WmDeviceInfo> {
        return uniWatchSubject.value.getDeviceInfo()
    }

    override fun getBatteryInfo(): Single<WmBatteryInfo> {
        return uniWatchSubject.value.getBatteryInfo()
    }

    override val observeBatteryChange: Observable<WmBatteryInfo>
        get() = uniWatchSubject.value.observeBatteryChange

    override fun startDiscovery(
        scanTime: Int,
        wmTimeUnit: WmTimeUnit,
        deviceModel: WmDeviceModel,
        tag: String
    ): Observable<WmDiscoverDevice> {

        uniWatches.forEach {
            val selected = it.setDeviceModel(deviceModel)
            if (selected) {
                uniWatchSubject.onNext(it)
                return uniWatchSubject.value.startDiscovery(scanTime, wmTimeUnit, deviceModel, tag)
            }
        }

        return Observable.create {
            it.onError(RuntimeException("no sdk support!"))
        }

    }

    override fun setLogEnable(logEnable: Boolean) {
        uniWatchSubject.value?.setLogEnable(logEnable)
    }

    override fun stopDiscovery() {
        uniWatchSubject.value?.stopDiscovery()
    }


}