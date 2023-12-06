package com.sjbt.sdk

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.base.sdk.AbUniWatch
import com.base.sdk.entity.BindType
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.WmDevice
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.common.WmDiscoverDevice
import com.base.sdk.entity.common.WmTimeUnit
import com.base.sdk.entity.data.WmBatteryInfo
import com.base.sdk.entity.settings.*
import com.base.sdk.exception.WmTransferError
import com.google.gson.Gson
import com.polidea.rxandroidble3.LogConstants
import com.polidea.rxandroidble3.LogOptions
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.scan.ScanFilter
import com.polidea.rxandroidble3.scan.ScanResult
import com.polidea.rxandroidble3.scan.ScanSettings
import com.sjbt.sdk.app.*
import com.sjbt.sdk.dfu.SJTransferFile
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.entity.old.AppViewBean
import com.sjbt.sdk.entity.old.BasicInfo
import com.sjbt.sdk.entity.old.BatteryBean
import com.sjbt.sdk.log.SJLog
import com.sjbt.sdk.settings.*
import com.sjbt.sdk.spp.BtStateReceiver
import com.sjbt.sdk.spp.OnBtStateListener
import com.sjbt.sdk.spp.bt.BtEngineMsgQue
import com.sjbt.sdk.spp.bt.BtEngineMsgQue.SOCKET_STATE_NONE
import com.sjbt.sdk.spp.bt.BtStateListener
import com.sjbt.sdk.spp.bt.BtStateListener.*
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.sync.*
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.ClsUtils
import com.sjbt.sdk.utils.SharedPreferencesUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

abstract class SJUniWatch(context: Application, timeout: Int) : AbUniWatch(),
    BtStateListener {

    private val TAG = "SJUniWatch"
    private val mDeviceType = "OSW-802N"

    var mContext: Application
    var mMsgTimeOut: Int

    var mBtStateReceiver: BtStateReceiver? = null

    private val mBtAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var discoveryObservableEmitter: ObservableEmitter<WmDiscoverDevice>

    private var deviceInfoEmitter: SingleEmitter<WmDeviceInfo>? = null
    private var batteryEmitter: SingleEmitter<WmBatteryInfo>? = null
    private var observeBatteryEmitter: ObservableEmitter<WmBatteryInfo>? = null

    private var mBindInfo: WmBindInfo? = null
    private var mCurrDevice: BluetoothDevice? = null
    private var mCurrAddress: String? = null
    private var mDeviceName: String = ""
    private var mConnectTryCount = 0
    private var mConnectState: WmConnectState = WmConnectState.CONNECTING

    override val wmSettings = SJSettings(this)
    override val wmApps = SJApps(this)
    override val wmSync = SJSyncData(this)
    override val wmTransferFile = SJTransferFile(this)
    override val wmLog = SJLog(this)

    private val mBtEngine: BtEngineMsgQue = BtEngineMsgQue(this)
    private val mBindStateMap = HashMap<String, Boolean>()

    //同步数据
    private val syncActivityDuration = wmSync.syncActivityDurationData as SyncActivityDurationData
    private val syncCaloriesData = wmSync.syncCaloriesData as SyncCaloriesData
    private val syncDistanceData = wmSync.syncDistanceData as SyncDistanceData
    private val syncHeartRateData = wmSync.syncHeartRateData as SyncHeartRateData
    private val syncOxygenData = wmSync.syncOxygenData as SyncOxygenData
    private val syncRealtimeRateData = wmSync.syncRealtimeRateData as SyncRealtimeRateData
    private val syncSleepData = wmSync.syncSleepData as SyncSleepData
    private val syncSportSummaryData = wmSync.syncSportSummaryData as SyncSportSummaryData
    private val syncStepData = wmSync.syncStepData as SyncStepData
    private val syncDailyActivityDurationData =
        wmSync.syncDailyActivityDuration as SyncDailyActivityDurationData
    private val syncAllData = wmSync.syncAllData as SyncAllData

    //应用
    private val appDateTime = wmApps.appDateTime as AppDateTime
    private val appCamera = wmApps.appCamera as AppCamera
    private val appAlarm = wmApps.appAlarm as AppAlarm
    private val appContact = wmApps.appContact as AppContact
    private val appDial = wmApps.appDial as AppDial
    private val appFind = wmApps.appFind as AppFind
    private val appLanguage = wmApps.appLanguage as AppLanguage
    private val appNotification = wmApps.appNotification as AppNotification
    private val appSport = wmApps.appSport as AppSport
    private val appWeather = wmApps.appWeather as AppWeather
    private val appMusicControl = wmApps.appMusicControl as AppMusicControl

    //设置
    private val settingAppView = wmSettings.settingAppView as SettingAppView
    private val settingHeartRateAlerts = wmSettings.settingHeartRate as SettingHeartRateAlerts
    private val settingPersonalInfo = wmSettings.settingPersonalInfo as SettingPersonalInfo
    private val settingSedentaryReminder =
        wmSettings.settingSedentaryReminder as SettingSedentaryReminder
    private val settingSoundAndHaptic = wmSettings.settingSoundAndHaptic as SettingSoundAndHaptic
    private val settingSportGoal = wmSettings.settingSportGoal as SettingSportGoal
    private val settingUnitInfo = wmSettings.settingUnitInfo as SettingUnitInfo
    private val settingWistRaise = wmSettings.settingWistRaise as SettingWistRaise
    private val settingSleepSet = wmSettings.settingSleepSettings as SettingSleepSet
    private val settingDrinkWaterReminder =
        wmSettings.settingDrinkWater as SettingDrinkWaterReminder

    private var unbindEmitter: CompletableEmitter? = null
    private var mWmFunctionSupport: WmFunctionSupport = WmFunctionSupport()

    private val gson = Gson()
    private var sharedPreferencesUtils: SharedPreferencesUtils
    var sdkLogEnable = false
    private var isAppFront = true
    private val mHandler = Handler(Looper.getMainLooper())
    var MTU: Int = 600
    private var mtuEmitter: SingleEmitter<Int>? = null
    private var node04Emitter: SingleEmitter<Int>? = null
    private val mPayloadMap = PayloadMap()

    private var discoveryTag: String = ""
    val observableMtu: Single<Int> = Single.create { emitter ->
        mtuEmitter = emitter
        sendSyncSafeMsg(CmdHelper.getMTUCmd)
    }

    private var readSubPkMsg: ReadSubPkMsg? = null
    private var subPkObservableEmitter: ObservableEmitter<MsgBean>? = null

    private val subPkEmitterMap = ConcurrentHashMap<Short, ObservableEmitter<MsgBean>>()
    private val readSubPkMsgMap = ConcurrentHashMap<Short, ReadSubPkMsg>()

    fun sendReadSubPkObserveNode(
        readSubPkMsg: ReadSubPkMsg,
        payloadPackage: PayloadPackage
    ): Observable<MsgBean> {
        return Observable.create { emitter ->
            subPkEmitterMap.put(payloadPackage._id, emitter)
            readSubPkMsgMap.put(payloadPackage._id, readSubPkMsg)

            sendReadNodeCmdList(payloadPackage)
        }
    }

    /**
     * App给设备分包传输的时候要监听04回复
     */
    fun sendAndObserveNode04(msg: ByteArray): Single<Int> {
        return Single.create { emitter ->
            node04Emitter = emitter
            sendNoTimeOutMsg(msg)
        }
    }

    override fun setAppFront(front: Boolean) {
        isAppFront = front
    }

    override fun setLogEnable(logEnable: Boolean) {
        this.sdkLogEnable = logEnable
    }

    //当前消息的节点信息
    var mPayloadPackage: PayloadPackage? = null
    lateinit var rxBleClient: RxBleClient

    init {
        mContext = context
        mMsgTimeOut = timeout

        mBtEngine.setListener(this)
        sharedPreferencesUtils = SharedPreferencesUtils.getInstance(mContext)

        mBtStateReceiver = BtStateReceiver(mContext!!, wmLog, object : OnBtStateListener {

            override fun onClassicBtDisConnect(device: BluetoothDevice) {
                wmLog.logE(TAG, "onClassicBtDisConnect：" + device.address)

                if (device == mCurrDevice) {
                    btStateChange(WmConnectState.DISCONNECTED)
                    //                        removeCallBackRunner(mConnectTimeoutRunner)
                }
            }

            override fun onClassicBtConnect(device: BluetoothDevice) {
                wmLog.logE(TAG, "onClassicBtConnect：" + device.address)

                if (TextUtils.isEmpty(mCurrAddress)) {
                    mCurrAddress = sharedPreferencesUtils.getString(BT_ADDRESS, "")
                }

                if (device.address == mCurrAddress) {
                    mBindInfo?.let {
                        if (mCurrAddress?.let { it1 -> mBtEngine.getSocketState(it1) } == SOCKET_STATE_NONE) {
                            connect(device, it)
                        }
                    }
                }
            }

            override fun onClassicBtDisabled() {
                wmLog.logD(TAG, "onClassicBtDisabled")
                btStateChange(WmConnectState.DISCONNECTED)
//                removeCallBackRunner(mConnectTimeoutRunner)
            }

            override fun onClassicBtOpen() {
                wmLog.logD(TAG, "onClassicBtOpen")
//                removeCallBackRunner(mConnectTimeoutRunner)
            }

            override fun onBindState(device: BluetoothDevice, bondState: Int) {
                if (bondState == BluetoothDevice.BOND_NONE) {
                    if (device == mCurrDevice) {
                        mConnectTryCount = MAX_RETRY_COUNT
                        mBtEngine.clearStateMap()
                        btStateChange(WmConnectState.DISCONNECTED)
//                        removeCallBackRunner(mConnectTimeoutRunner)
                        wmLog.logD(TAG, "cancel pair：" + device.address)
                    }
                }
            }

            override fun onDiscoveryDevice(device: WmDiscoverDevice) {
                if (device.device.name.contains(discoveryTag)) {
                    discoveryObservableEmitter?.onNext(device)
                }
            }

            override fun onStartDiscovery() {

            }

            override fun onStopDiscovery() {
//                if (!discoveryObservableEmitter.isDisposed) {
//                    discoveryObservableEmitter?.onComplete()
//                    wmLog.logD(TAG, "onComplete")
//                }
            }
        })

        rxBleClient = RxBleClient.create(mContext)
        RxBleClient.updateLogOptions(
            LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        )

        appCamera.startCameraThread()
    }

    private fun btDisconnectSet() {
        wmTransferFile.transferError(WmTransferError.ERROR_DISCONNECT, "bt disconnect")
        mBtEngine.clearMsgQueue()
        mBtEngine.clearStateMap()
        appCamera.stopCameraPreview()

        syncActivityDuration.observeDisconnectState()
        syncCaloriesData.observeDisconnectState()
        syncDistanceData.observeDisconnectState()
        syncHeartRateData.observeDisconnectState()
        syncOxygenData.observeDisconnectState()
        syncRealtimeRateData.observeDisconnectState()
        syncSleepData.observeDisconnectState()
        syncSportSummaryData.observeDisconnectState()
        syncStepData.observeDisconnectState()
        syncDailyActivityDurationData.observeDisconnectState()
        syncAllData.observeDisconnectState()

        appDateTime.observeDisconnectState()
        appCamera.observeDisconnectState()
        appAlarm.observeDisconnectState()
        appContact.observeDisconnectState()
        appDial.observeDisconnectState()
        appFind.observeDisconnectState()
        appLanguage.observeDisconnectState()
        appSport.observeDisconnectState()

        settingAppView.observeDisconnectState()
        settingHeartRateAlerts.observeDisconnectState()
        settingPersonalInfo.observeDisconnectState()
        settingSedentaryReminder.observeDisconnectState()
        settingSoundAndHaptic.observeDisconnectState()
        settingSportGoal.observeDisconnectState()
        settingUnitInfo.observeDisconnectState()
        settingWistRaise.observeDisconnectState()
        settingSleepSet.observeDisconnectState()
        settingDrinkWaterReminder.observeDisconnectState()

        disconnect()
    }

    override fun getDeviceInfo(): Single<WmDeviceInfo> {
        return Single.create {
            deviceInfoEmitter = it
            getBasicInfo()
        }
    }


    override fun getBatteryInfo(): Single<WmBatteryInfo> {
        return Single.create {
            batteryEmitter = it
            sendSyncSafeMsg(CmdHelper.batteryInfo)
        }
    }

    override val observeBatteryChange: Observable<WmBatteryInfo> =
        Observable.create { emitter -> observeBatteryEmitter = emitter }

    /**
     * 获取基本信息
     * @param
     */
    private fun getBasicInfo() {
        sendSyncSafeMsg(CmdHelper.baseInfoCmd)
    }

    /**
     * 获取当前支持的功能
     *
     * @return
     */
    open fun getActionSupportCmd() {
        sendReadNodeCmdList(CmdHelper.getRequestActionListCmd())
    }

    override fun socketNotify(state: Int, obj: Any?) {
        try {
            when (state) {
                MSG -> {
                    val msgBean: MsgBean = obj as MsgBean

                    when (msgBean.head) {
                        HEAD_VERIFY -> {

                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_8001 -> {
                                    sendSyncSafeMsg(CmdHelper.biuVerifyCmd)
                                }

                                CMD_ID_8002 -> {
                                    mConnectTryCount = 0
                                    getActionSupportCmd()
                                }
                            }
                        }

                        HEAD_COMMON -> {

                            when (msgBean.cmdId.toShort()) {

                                CMD_ID_8001 -> {//基本信息
                                    val basicInfo: BasicInfo = gson.fromJson(
                                        msgBean.payloadJson, BasicInfo::class.java
                                    )

                                    mDeviceName = basicInfo.dev_name

                                    basicInfo?.let {
                                        val wm = WmDeviceInfo(
                                            it.prod_mode,
                                            it.mac_addr,
                                            it.soft_ver,
                                            it.dev_id,
                                            it.dev_name,
                                            it.dev_name,
                                            it.dial_ability,
                                            it.screen,
                                            it.lang,
                                            it.cw,
                                            it.ch
                                        )

                                        deviceInfoEmitter?.onSuccess(wm)
                                    }
                                }

                                CMD_ID_8002 -> {

                                }

                                CMD_ID_8003 -> {//电量消息
                                    val batteryBean = gson.fromJson(
                                        msgBean.payloadJson, BatteryBean::class.java
                                    )

                                    batteryBean?.let {
                                        val batteryInfo = WmBatteryInfo(
                                            it.isIs_charging == 1, it.battery_main
                                        )

                                        batteryEmitter?.let {
                                            if (!it.isDisposed) {
                                                it?.onSuccess(batteryInfo)
                                            }
                                        }

                                        observeBatteryEmitter?.onNext(
                                            batteryInfo
                                        )
                                    }
                                }

                                CMD_ID_8004 -> {
                                    appNotification.sendNotificationEmitter?.onSuccess(msgBean.payload[0].toInt() == 1)
                                }

                                CMD_ID_8007 -> {//同步时间
                                    appDateTime.setEmitter?.onSuccess(true)
                                }

                                CMD_ID_8008 -> {//获取AppView List

                                    val appViewBean = gson.fromJson(
                                        msgBean.payloadJson, AppViewBean::class.java
                                    )

                                    val appViews = mutableListOf<AppView>()
                                    appViewBean?.let {
                                        it.list.forEach {
                                            appViews.add(AppView(it.using, it.id))
                                        }
                                    }

                                    val wmAppView = WmAppView(appViews)

                                    settingAppView.appViewsBack(wmAppView)
                                }

                                CMD_ID_8009 -> {//APP 视图设置
                                    settingAppView.setAppViewResult(msgBean.payload[0].toInt() == 1)
                                }

                                CMD_ID_8010 -> {//设置/删除表盘返回
                                    val type = msgBean.payload[0].toInt() // 1设定 2删除
                                    val actResult = msgBean.payload[1].toInt()  //是否操作成功
                                    val reason = msgBean.payload[2].toInt()  //是否操作成功

                                    appDial.deleteDialResult(actResult == 1)
                                }

                                CMD_ID_800F -> {
                                    if (msgBean.divideType === DIVIDE_N_2) {
                                        appDial.mMyDialList.clear()
                                        appDial.addDialList(msgBean)
                                    } else {
                                        if (msgBean.divideType === DIVIDE_Y_F_2) {
                                            appDial.mMyDialList.clear()
                                            appDial.addDialList(msgBean)
                                            return
                                        } else if (msgBean.divideType === DIVIDE_Y_M_2) {
                                            appDial.addDialList(msgBean)
                                            return
                                        } else if (msgBean.divideType === DIVIDE_Y_E_2) {
                                            appDial.addDialList(msgBean)
                                        }
                                    }

                                    appDial.syncDialListEmitter?.onNext(appDial.mMyDialList)
                                    appDial.syncDialListEmitter?.onComplete()
                                }

                                CMD_ID_8014 -> {//查询表盘当前信息

                                }

                                CMD_ID_8017 -> {//获取触感
                                    var ringState = 0
                                    var msgShake = 0
                                    var crowShake = 0
                                    var sysShake = 0
                                    var armScreen = 0
                                    var keepNoVoice = 0

                                    for (i in 0 until msgBean.payload.size) {

                                        when (i) {
                                            SettingSoundAndHaptic.SoundAndHapticType.RING.type -> {
                                                ringState = msgBean.payload[i].toInt()
                                            }

                                            SettingSoundAndHaptic.SoundAndHapticType.NOTIFY.type -> {
                                                msgShake = msgBean.payload[i].toInt()
                                            }

                                            SettingSoundAndHaptic.SoundAndHapticType.CROWN.type -> {
                                                crowShake = msgBean.payload[i].toInt()
                                            }

                                            SettingSoundAndHaptic.SoundAndHapticType.SYSTEM.type -> {
                                                sysShake = msgBean.payload[i].toInt()
                                            }

                                            4 -> {
                                                armScreen = msgBean.payload[i].toInt()
                                            }

                                            SettingSoundAndHaptic.SoundAndHapticType.MUTED.type -> {
                                                keepNoVoice = msgBean.payload[i].toInt()
                                            }
                                        }
                                    }

                                    val wmWristRaise = WmWristRaise(armScreen == 1)

                                    settingWistRaise.backWistRaiseSettings(wmWristRaise)

                                    val wmSoundAndHaptic = WmSoundAndHaptic(
                                        ringState == 1,
                                        msgShake == 1,
                                        crowShake == 1,
                                        sysShake == 1,
                                        keepNoVoice == 1
                                    )

                                    settingSoundAndHaptic.backSoundAndHapticSettings(
                                        wmSoundAndHaptic
                                    )

                                }

                                CMD_ID_8018 -> {//设置触感

                                    val setSuccess = msgBean.payload[0].toInt() == 1

                                    if (setSuccess) {
                                        settingSoundAndHaptic.setSuccess()
                                        settingWistRaise.setSuccess()
                                    }

                                }

                                CMD_ID_8019 -> {//监听触感
                                    sendSyncSafeMsg(CmdHelper.deviceRingStateRespondCmd)
                                    val ctype = msgBean.payload[0].toInt()
                                    val vValue = msgBean.payload[1].toInt()

                                    when (ctype) {

                                        4 -> {
                                            settingWistRaise.observeWmWistRaiseChange(
                                                ctype, vValue
                                            )
                                        }

                                        else -> {
                                            settingSoundAndHaptic.observeWmWistRaiseChange(
                                                ctype, vValue
                                            )
                                        }
                                    }
                                }

                                CMD_ID_8028 -> {//监听 设备拍照命令
                                    appCamera.cameraObserveTakePhotoEmitter?.onNext(true)
                                    sendNoTimeOutMsg(
                                        CmdHelper.getCameraRespondCmd(
                                            CMD_ID_8028, 1.toByte()
                                        )
                                    )
                                }

                                CMD_ID_8029 -> {//监听 设备拉起或者关闭相机
                                    appCamera.observeDeviceCamera(msgBean.payload[0].toInt() == 1)

                                    sendNoTimeOutMsg(
                                        CmdHelper.getCameraRespondCmd(
                                            CMD_ID_8029, if (isAppFront) {
                                                1.toByte()
                                            } else {
                                                0.toByte()
                                            }
                                        )
                                    )
                                }

                                CMD_ID_802A -> {//监听App打开设备相机
                                    appCamera.openCameraResult(msgBean.payload[0].toInt() == 1)
                                }

                                CMD_ID_802B -> {//监听相机闪光灯和前后摄像头
                                    val action = msgBean.payload[0]
                                    val stateOn = msgBean.payload[1]

                                    appCamera.observeCameraAction(action, stateOn)

                                    sendNoTimeOutMsg(
                                        CmdHelper.getCameraRespondCmd(
                                            CMD_ID_802B, 1.toByte()
                                        )
                                    )
                                }

                                CMD_ID_802C -> {//发送 前后摄切换、闪光灯切换

                                }

//                                CMD_ID_802D -> {//旧协议 不用
//                                    mBindInfo?.let {
//                                        wmLog.logD(TAG, "bindinfo:$it")
//                                        sendNormalMsg(CmdHelper.getBindCmd(it))
//                                    }
//
//                                    mWmFunctionSupportBean =
//                                        WmFunctionSupport.toActionSupportBean(msgBean.payload)
//                                }

                                CMD_ID_802E -> {//绑定
                                    val result = msgBean.payload[0].toInt()
                                    wmLog.logD(TAG, "bind result:$result")

                                    if (result == 1) {

                                        btStateChange(WmConnectState.VERIFIED)
                                        mCurrAddress?.let {
                                            mBindStateMap.put(it, true)
                                        }

                                    } else {
                                        removeDevice()
                                        mCurrAddress?.let {
                                            mBindStateMap.put(it, false)
                                        }

                                        disconnect()
                                    }
                                }

                                CMD_ID_802F -> {//解绑
                                    val result = msgBean.payload[0].toInt()

                                    mCurrAddress?.let {
                                        mBindStateMap.put(it, false)
                                    }

                                    wmLog.logD(TAG, "unbind success:$result")

                                    if (result == 1) {
                                        unbindEmitter?.onComplete()
                                        removeDevice()
                                    } else {
                                        unbindEmitter?.onError(RuntimeException("unbind failed"))
                                    }
                                }
                            }
                        }

                        HEAD_SPORT_HEALTH -> {
                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_800C, CMD_ID_800D, CMD_ID_800E -> {
                                    settingSleepSet.sleepSetBusiness(msgBean)
                                }
                            }
                        }

                        HEAD_CAMERA_PREVIEW -> {

                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_8001 -> {
                                    appCamera.cameraPreviewBuz(msgBean)
                                }

                                CMD_ID_8003 -> {
                                    val frameSuccess = msgBean.payload[0]

                                    wmLog.logD(TAG, "send success：$frameSuccess")

                                    wmLog.logD(
                                        TAG,
                                        "continueUpdateFrame 03:${appCamera.continueUpdateFrame}"
                                    )

                                    appCamera.sendFrameData03(frameSuccess)

                                }
                            }
                        }

                        HEAD_FILE_SPP_A_2_D -> {
                            wmTransferFile.transferFileBuz(msgBean)
                        }

                        HEAD_NODE_TYPE -> {
                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_8001 -> {//请求
                                    if (msgBean.payload.size > 10) {//设备请求的消息
                                        sendNoTimeOutMsg(CmdHelper.communityMsg)

                                        var payloadPackage: PayloadPackage =
                                            PayloadPackage.fromByteArray(msgBean.payload)

                                        parseResponseNodePayload(msgBean, payloadPackage)
                                    } else {
                                        wmLog.logD(
                                            TAG,
                                            "No Node Msg：" + msgBean.payload.size
                                        )
                                    }
                                }

                                CMD_ID_8002 -> {//响应
                                    sendNoTimeOutMsg(CmdHelper.communityMsg)

                                    if (msgBean.payloadLen >= 10 || msgBean.divideType != DIVIDE_N_2) {//设备应用层回复 包含带应用层payload的数据和分包数据的处理逻辑
                                        wmLog.logD(
                                            TAG,
                                            "Node Message DIVIDE TYPE：" + msgBean.divideType
                                        )

                                        if (msgBean.divideType == DIVIDE_N_2) {//不分包消息

                                            var payloadPackage: PayloadPackage =
                                                msgBean.payloadPackage!!

                                            wmLog.logE(
                                                TAG,
                                                "one package msg Complete packageSeq：" + payloadPackage._id
                                            )

                                            subPkEmitterMap[payloadPackage._id]?.let {

                                                subPkObservableEmitter = it

                                                if (!it.isDisposed) {

                                                    readSubPkMsg =
                                                        readSubPkMsgMap[payloadPackage._id]
                                                    readSubPkMsg?.setHasNext(payloadPackage.hasNext())
                                                    it.onNext(msgBean)

                                                    if (!payloadPackage.hasNext()) {
                                                        it.onComplete()
                                                    }
                                                } else {
                                                    parseResponseNodePayload(
                                                        msgBean,
                                                        payloadPackage
                                                    )
                                                }

                                            } ?: run {
                                                parseResponseNodePayload(msgBean, payloadPackage)
                                            }

                                        } else {//分包消息

                                            if (msgBean.divideType == DIVIDE_Y_F_2) {//多个业务分包
                                                val payloadPackage =
                                                    PayloadPackage.fromByteArray(msgBean.payload)

                                                subPkObservableEmitter =
                                                    subPkEmitterMap[payloadPackage._id]

                                                readSubPkMsg =
                                                    readSubPkMsgMap[payloadPackage._id]

                                                wmLog.logE(
                                                    TAG,
                                                    "sub package msg first hasNext:" + payloadPackage.hasNext()
                                                )

                                                readSubPkMsg?.setHasNext(
                                                    payloadPackage.hasNext()
                                                )
                                            }

                                            subPkObservableEmitter?.onNext(msgBean)

                                            if (msgBean.divideType == DIVIDE_Y_E_2) {

                                                readSubPkMsg?.let {

                                                    wmLog.logE(
                                                        TAG,
                                                        "sub package msg Complete hasNext：${it.getHasNext()}"
                                                    )

                                                    if (!it.getHasNext()) {
                                                        subPkObservableEmitter?.onComplete()
                                                    }
                                                }
                                            }
                                        }

                                    } else {//不带节点的消息
                                        wmLog.logD(TAG, "No Node MSg：" + msgBean.payloadLen)
                                    }
                                }

                                CMD_ID_8003 -> {
                                    MTU =
                                        BtUtils.byte2short(msgBean.payload.reversedArray()).toInt()
                                    mtuEmitter?.onSuccess(MTU)
                                    wmLog.logD(TAG, "MTU:$MTU")
                                }

                                CMD_ID_8004 -> {
                                    node04Emitter?.onSuccess(msgBean.cmdOrder)
                                }
                            }
                        }
                    }
                }

                TIME_OUT -> {
                    msgTimeOut(obj as MsgBean)
                }

                BUSY -> {

                }

                ON_SOCKET_CLOSE -> {
                    wmLog.logD(TAG, "onSocketClose")
                    btStateChange(WmConnectState.DISCONNECTED)
                }

                CONNECTED -> {
                    mCurrAddress?.let {
                        sharedPreferencesUtils.putString(BT_ADDRESS, it)
                    }

                    btStateChange(WmConnectState.CONNECTED)
                    sendSyncSafeMsg(CmdHelper.biuShakeHandsCmd)
                    mBtStateReceiver?.let {
                        it.setmSocket(mBtEngine.getmSocket())
                        it.setmCurrDevice(mCurrAddress)
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun msgTimeOut(msgBean: MsgBean) {

        wmLog.logE(TAG, "msg time out：$msgBean")

        mBtAdapter?.takeIf { !it.isEnabled }?.let {
            mBtEngine.clearMsgQueue()
            mBtEngine.clearStateMap()
            disconnect()
        }

        when (msgBean.head) {
            HEAD_VERIFY -> {
                when (msgBean.cmdId.toShort()) {
                    CMD_ID_8001, CMD_ID_8002 -> {
                        mBtEngine.clearStateMap()
                        mBtEngine.clearMsgQueue()

                        removeDevice()
                    }
                }
            }

            HEAD_COMMON -> {
                when (msgBean.cmdId.toShort()) {
                    CMD_ID_8001 -> {
                        deviceInfoEmitter?.onError(RuntimeException("get deviceInfo time out!"))
                    }

                    CMD_ID_8002 -> {
                    }

                    CMD_ID_8003 -> {
                        batteryEmitter?.let {
                            if (!it.isDisposed) {
                                it.onError(RuntimeException("get battery time out"))
                            }
                        }
                    }

                    CMD_ID_8004 -> {
//                        if (sendNotifyEmitter != null) {
//                            sendNotifyEmitter.onError(RuntimeException("send notify timeout!"))
//                        }

                    }
                    CMD_ID_8005 -> {}
                    CMD_ID_8006 -> {}
                    CMD_ID_8006 -> {
                        appDateTime.setEmitter?.onSuccess(false)
                    }

                    CMD_ID_8008 -> {
                        settingAppView.appViewsBackTimeOut()
                    }
                    CMD_ID_8009 -> {
                        settingAppView.appViewsSetTimeOut()
                    }

                    CMD_ID_800A -> {}
                    CMD_ID_800B -> {}
                    CMD_ID_800C -> {}
                    CMD_ID_800D -> {}
                    CMD_ID_800E -> {}
                    CMD_ID_800F -> {
                        appDial.syncDialListEmitter?.onError(RuntimeException("get dials timeout!"))
                    }

                    CMD_ID_8010 -> {
                        appDial.deleteDialEmitter?.onError(RuntimeException("delete dial timeout!"))
                    }

                    CMD_ID_8011 -> {}
                    CMD_ID_8012 -> {}
                    CMD_ID_8013 -> {}
                    CMD_ID_8014 -> {

//                        if (getDialEmitter != null) {
//                            getDialEmitter.onError(RuntimeException("get dial timeout!"))
//                        }

                    }

                    CMD_ID_8017 -> {
                        settingSoundAndHaptic.setSoundAndHapticTimeOut()
                    }

                    CMD_ID_8018 -> {
                        settingSoundAndHaptic.getSoundAndHapticTimeOut()
                    }

                    CMD_ID_801C -> {
//                        if (setAlarmEmitter != null) {
//                            setAlarmEmitter.onError(RuntimeException("set alarm timeout!"))
//                        }
                    }

                    CMD_ID_801E -> {
//                        if (getAlarmEmitter != null) {
//                            getAlarmEmitter.onError(RuntimeException("get alarm timeout!"))
//                        }
                    }
                    CMD_ID_8021 -> {
//                        if (searchDeviceEmitter != null) {
//                            searchDeviceEmitter.onError(RuntimeException("search device timeout!"))
//                        }
                    }
                    CMD_ID_8022 -> {
//                        if (contactListEmitter != null) {
//                            contactListEmitter.onError(RuntimeException("get contact list timeout!"))
//                        }
                    }
                    CMD_ID_8023 -> {
//                        if (appAddContactEmitter != null) {
//                            appAddContactEmitter.onError(RuntimeException("app add contact time out"))
//                        }
                    }
                    CMD_ID_8025 -> {
//                        if (appDelContactEmitter != null) {
//                            appDelContactEmitter.onError(RuntimeException("app delete contact timeout"))
//                        }
                    }
                    CMD_ID_8026 -> {}
                    CMD_ID_8027 -> {
//                        if (contactActionType == CONTACT_ACTION_LIST) {
//                            contactListEmitter.onError(RuntimeException("get contact list timeout!"))
//                        } else if (contactActionType == CONTACT_ACTION_ADD) {
//                            appAddContactEmitter.onError(RuntimeException("app add contact time out"))
//                        } else if (contactActionType == CONTACT_ACTION_DELETE) {
//                            appDelContactEmitter.onError(RuntimeException("app delete contact timeout"))
//                        }
                    }
                    CMD_ID_8029 -> {
//                        appCamera.cameraSingleOpenEmitter?.onError(RuntimeException("call device camera time out"))
                    }

                    CMD_ID_802A -> {
                        appCamera.cameraSingleOpenEmitter?.onError(RuntimeException("call device camera time out"))
                    }

                    CMD_ID_802D -> {
//                        if (actionSupportEmitter != null) {
//                            actionSupportEmitter.onError(RuntimeException("action bean error!"))
//                        }
                    }

                    CMD_ID_802E -> {
                        mObservableConnectState
                    }

                    CMD_ID_802F -> {
                    }
                }
            }

            HEAD_SPORT_HEALTH -> {
                when (msgBean.cmdId.toShort()) {
                    CMD_ID_8001 -> {

//                        if (getSportInfoEmitter != null) {
//                            getSportInfoEmitter.onError(RuntimeException("get sport info timeout"))
//                        }

                        wmApps as SJApps

                    }

                    CMD_ID_8002 -> {
//                        if (stepEmitter != null) {
//                            stepEmitter.onError(RuntimeException("get step timeout"))
//                        }
                    }

                    CMD_ID_8003 -> {
//                        if (rateEmitter != null) {
//                            rateEmitter.onError(RuntimeException("get rate timeout"))
//                        }
                    }
                    CMD_ID_8008 -> {
//                        if (sleepRecordEmitter != null) {
//                            sleepRecordEmitter.onError(RuntimeException("get sleep record timeout"))
//                        }
                    }
                    CMD_ID_8009 -> {
//                        if (getBloodOxEmitter != null) {
//                            getBloodOxEmitter.onError(RuntimeException("get blood ox timeout"))
//                        }
                    }
                    CMD_ID_800A -> {
//                        if (getBloodSugarEmitter != null) {
//                            getBloodSugarEmitter.onError(RuntimeException("get blood sugar timeout"))
//                        }
                    }
                    CMD_ID_800B -> {
//                        if (getBloodPressEmitter != null) {
//                            getBloodPressEmitter.onError(RuntimeException("get blood press timeout"))
//                        }
                    }
                    CMD_ID_800C -> {
//                        if (sleepSetEmitter != null) {
//                        sleepSetEmitter.onError(RuntimeException("sleep set timeout"))
//                        }
                    }
                    CMD_ID_800D -> {
//                        if (setSleepEmitter != null) {
//                        setSleepEmitter.onError(RuntimeException("set sleep timeout"))
//                    }
                    }
                }
            }

            HEAD_FILE_SPP_A_2_D -> {
                wmTransferFile.timeOut(msgBean)
            }

            HEAD_CAMERA_PREVIEW -> {
                wmTransferFile.mTransferring = false
                when (msgBean.cmdId.toShort()) {
                    CMD_ID_8001 -> {
//                    if (cameraPreviewEmitter != null) {
//                        cameraPreviewEmitter.onError(RuntimeException("camera preview timeout"))
//                    }
                    }
                }
            }

            HEAD_NODE_TYPE -> {
                when (msgBean.cmdId.toShort()) {
                    CMD_ID_8001 -> {//请求
                        if (msgBean.payload.size > 10) {//设备应用层回复

                            if (msgBean.head == HEAD_NODE_TYPE && msgBean.cmdId.toShort() != CMD_ID_8004) {

                                wmLog.logE(
                                    TAG,
                                    "time out node msg：" + BtUtils.bytesToHexString(msgBean.originData)
                                )

                                var payloadPackage: PayloadPackage =
                                    PayloadPackage.fromByteArray(msgBean.payload)

                                parseTimeOutNode(payloadPackage, msgBean)
                            }
                        }
                    }
                }
            }
        }
    }

    fun logD(TAG: String, msg: String) {
        wmLog.logD(TAG, msg)
    }

    private fun removeDevice() {
        mCurrDevice?.let {
            ClsUtils.removeBond(BluetoothDevice::class.java, it)
        }
    }

    fun clearMsg() {
        mBtEngine.clearMsgQueue()
    }

    fun sendNoTimeOutMsg(bytes: ByteArray) {
        mBtEngine.sendNoTimeOutMsg(bytes)
    }

    fun sendSyncSafeMsg(msg: ByteArray) {
        if (wmTransferFile.mTransferring) {
            val byteBuffer = ByteBuffer.wrap(msg)
            val head = byteBuffer.get()
            val cmdId: Short = byteBuffer[2].toShort()

            if (isMsgStopped(head, cmdId)) {
                wmLog.logD(TAG, "sending...:" + BtUtils.bytesToHexString(msg))
                return
            }
        }

        mBtEngine.sendSyncSafeMsgOnWorkThread(msg)
    }

    /**
     * 发送写入类型Node节点消息
     */
    fun sendWriteNodeCmdList(payloadPackage: PayloadPackage) {
        mPayloadMap.putPayload(payloadPackage)

        payloadPackage.toByteArray(requestType = RequestType.REQ_TYPE_WRITE).forEach {
            var payload: ByteArray = it

            val cmdArray = CmdHelper.constructCmd(
                HEAD_NODE_TYPE,
                CMD_ID_8001,
                DIVIDE_N_2,
                0,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )

            sendSyncSafeMsg(cmdArray)
        }

        mPayloadPackage = payloadPackage

    }

    /**
     * 发送读取类型Node节点消息
     */
    fun sendReadNodeCmdList(payloadPackage: PayloadPackage) {
        mPayloadMap.putPayload(payloadPackage)

        payloadPackage.toByteArray(requestType = RequestType.REQ_TYPE_READ).forEach {
            val cmdArray = CmdHelper.constructCmd(
                HEAD_NODE_TYPE,
                CMD_ID_8001,
                DIVIDE_N_2,
                0,
                0,
                BtUtils.getCrc(HEX_FFFF, it, it.size),
                it
            )

            sendSyncSafeMsg(cmdArray)
        }

        mPayloadPackage = payloadPackage

    }

    /**
     * 发送操作类型Node节点消息
     */
    fun sendExecuteNodeCmdList(payloadPackage: PayloadPackage) {
        payloadPackage.toByteArray(requestType = RequestType.REQ_TYPE_EXECUTE).forEach {
            val cmdArray = CmdHelper.constructCmd(
                HEAD_NODE_TYPE,
                CMD_ID_8001,
                DIVIDE_N_2,
                0,
                0,
                BtUtils.getCrc(HEX_FFFF, it, it.size),
                it
            )

            sendSyncSafeMsg(cmdArray)
        }

        mPayloadPackage = payloadPackage

    }

    private fun parseResponseNodePayload(
        msgBean: MsgBean,
        payloadPackage: PayloadPackage
    ) {
        if (payloadPackage.actionType == ResponseResultType.RESPONSE_ALL_OK.type) {
            wmLog.logD(TAG, "All OK:" + mPayloadMap.getPayload(payloadPackage._id).packageSeq)

        } else if (payloadPackage.actionType == ResponseResultType.RESPONSE_ALL_FAIL.type) {
            wmLog.logD(TAG, "All Fail" + mPayloadMap.getPayload(payloadPackage._id).packageSeq)

        } else if (payloadPackage.actionType == RequestType.REQ_TYPE_NOTIFY.type) {

            parseNotifyNode(payloadPackage, msgBean)

        } else if (payloadPackage.actionType == ResponseResultType.RESPONSE_EACH.type
            || payloadPackage.actionType == RequestType.REQ_TYPE_WRITE.type
            || payloadPackage.actionType == RequestType.REQ_TYPE_READ.type
            || payloadPackage.actionType == RequestType.REQ_TYPE_EXECUTE.type
        ) {
            wmLog.logD(TAG, "Each node msg")
            parseResponseEachNode(payloadPackage, msgBean)
        }

    }

    /**
     * 解析超时的节点消息
     */
    private fun parseTimeOutNode(
        payloadPackage: PayloadPackage, msgBean: MsgBean
    ) {
        payloadPackage.itemList.forEach {
            when (it.urn[0]) {
                URN_CONNECT -> {//蓝牙连接 暂用旧协议格式

                }

                URN_SETTING -> {//设置同步
                    when (it.urn[1]) {
                        URN_SETTING_SPORT -> {//运动目标
                            settingSportGoal.onTimeOut(msgBean, it)
                        }

                        URN_SETTING_PERSONAL -> {//健康信息
                            settingPersonalInfo.onTimeOut(msgBean, it)
                        }

                        URN_SETTING_UNIT -> {//单位同步
                            settingUnitInfo.onTimeOut(msgBean, it)
                        }

                        URN_SETTING_LANGUAGE -> {//语言设置
                            appLanguage.onTimeOut(msgBean, it)
                        }

                        URN_SETTING_SEDENTARY -> {//久坐提醒
                            settingSedentaryReminder.onTimeOut(msgBean, it)
                        }

                        URN_SETTING_DRINK -> {//喝水提醒
                            settingDrinkWaterReminder.onTimeOut(msgBean, it)
                        }

                        URN_SETTING_DATE_TIME -> {//时间同步

                        }

                        URN_SETTING_SOUND -> {//声音和触感

                        }

                        URN_SETTING_ARM -> {//抬腕亮屏

                        }

                        URN_SETTING_APP_VIEW -> {//AppView

                        }

                        URN_SETTING_DEVICE_INFO -> {//DeviceInfo

                        }

                    }
                }

                URN_APP_SETTING -> {//应用

                    when (it.urn[1]) {
                        URN_APP_ALARM -> {
                            appAlarm.onTimeOut(msgBean, it)
                        }

                        URN_APP_SPORT -> {
                            appSport.onTimeOut(msgBean, it)
                        }

                        URN_APP_CONTACT -> {
                            appContact.onTimeOut(msgBean, it)
                        }

                        URN_APP_WEATHER -> {
                            appWeather.onTimeOut(msgBean, it)
                        }

                        URN_APP_RATE -> {
                            settingHeartRateAlerts.onTimeOut(msgBean, it)
                        }

                        URN_APP_FIND_DEVICE, URN_APP_FIND_PHONE -> {
                            appFind.onTimeOut(msgBean, it)
                        }
                    }
                }

                URN_APP_CONTROL -> {
                    when (it.urn[1]) {
                        URN_APP_FIND_PHONE, URN_APP_FIND_DEVICE -> {
                            appFind.onTimeOut(msgBean, it)
                        }

                    }
                }

                URN_SPORT_DATA -> {//运动同步
                    when (it.urn[1]) {
                        URN_SPORT_STEP -> {
                            syncStepData.onTimeOut(msgBean, it)
                        }

                        URN_SPORT_CALORIES -> {
                            syncCaloriesData.onTimeOut(msgBean, it)
                        }

                        URN_SPORT_OXYGEN -> {
                            syncOxygenData.onTimeOut(msgBean, it)
                        }

                        URN_SPORT_ACTIVITY_LEN -> {
                            syncActivityDuration.onTimeOut(msgBean, it)
                        }

                        URN_SPORT_DISTANCE -> {
                            syncDistanceData.onTimeOut(msgBean, it)
                        }

                        URN_SPORT_RATE -> {

                            when (it.urn[2]) {
                                URN_SPORT_RATE_REALTIME -> {
                                    syncRealtimeRateData.onTimeOut(msgBean, it)
                                }

                                URN_SPORT_RATE_RECORD -> {
                                    syncHeartRateData.onTimeOut(msgBean, it)
                                }
                            }
                        }

                        URN_SPORT_SLEEP -> {
                            syncSleepData.onTimeOut(msgBean, it)
                        }

                        URN_SPORT_SUMMARY, URN_SPORT_10S_DISTANCE, URN_SPORT_10S_RATE, URN_SPORT_10S_CALORIES, URN_SPORT_10S_STEP_FREQUENCY -> {
                            syncSportSummaryData.onTimeOut(msgBean, it)
                        }
                    }
                }
            }
        }
    }

    private fun parseNotifyNode(payloadPackage: PayloadPackage, msgBean: MsgBean) {
        payloadPackage.itemList.forEach {
            when (it.urn[0]) {
                URN_APP_SETTING -> {
                    when (it.urn[1]) {
                        URN_APP_ALARM -> {
                            appAlarm.alarmUpdate()
                        }
                    }
                }
            }
        }
    }

    private fun parseResponseEachNode(
        payloadPackage: PayloadPackage, msgBean: MsgBean
    ) {
        payloadPackage.itemList.forEach {
            when (it.urn[0]) {
                URN_CONNECT -> {//蓝牙连接 暂用旧协议格式
                }

                URN_FUN_LIST -> {
                    mBindInfo?.let {
                        wmLog.logD(TAG, "bindinfo:$it")

                        if (mBindStateMap[it.macAddress] == true) {
                            it.bindType = BindType.CONNECT_BACK
                        }

                        sendSyncSafeMsg(CmdHelper.getBindCmd(it))
                    } ?: run {
                        btStateChange(WmConnectState.DISCONNECTED)
                    }

                    mWmFunctionSupport =
                        SJFunctionSupport.toActionSupportBean(msgBean.payloadPackage!!.itemList[0].data)

                    wmLog.logD(TAG, mWmFunctionSupport.toString())
                }

                URN_SETTING -> {//设置同步
                    when (it.urn[1]) {
                        URN_SETTING_SPORT -> {//运动目标
                            settingSportGoal.sportInfoBusiness(it)
                        }

                        URN_SETTING_PERSONAL -> {//健康信息
                            settingPersonalInfo.personalInfoBusiness(it)
                        }

                        URN_SETTING_UNIT -> {//单位同步
                            settingUnitInfo.unitInfoBusiness(it)
                        }

                        URN_SETTING_LANGUAGE -> {//语言设置
                            appLanguage.languageBusiness(it, msgBean)
                        }

                        URN_SETTING_SEDENTARY -> {//久坐提醒
                            settingSedentaryReminder.sedentaryReminderBusiness(it)
                        }

                        URN_SETTING_DRINK -> {//喝水提醒
                            settingDrinkWaterReminder.drinkWaterBusiness(it)
                        }

                        URN_SETTING_DATE_TIME -> {//时间同步

                        }

                        URN_SETTING_SOUND -> {//声音和触感

                        }

                        URN_SETTING_ARM -> {//抬腕亮屏

                        }

                        URN_SETTING_APP_VIEW -> {//AppView

                        }

                        URN_SETTING_DEVICE_INFO -> {//DeviceInfo

                        }

                    }
                }

                URN_APP_SETTING -> {//应用

                    when (it.urn[1]) {
                        URN_APP_ALARM -> {
                            appAlarm.alarmBusiness(it)
                        }

                        URN_APP_SPORT -> {
                            appSport.appSportBusiness(payloadPackage, it)
                        }

                        URN_APP_CONTACT -> {
                            appContact.contactBusiness(payloadPackage, it, msgBean!!)
                        }

                        URN_APP_WEATHER -> {
                            appWeather.weatherBusiness(payloadPackage, it)
                        }

                        URN_APP_RATE -> {
                            settingHeartRateAlerts.settingHeartRateBusiness(it)
                        }
                    }
                }

                URN_APP_CONTROL -> {
                    when (it.urn[1]) {
                        URN_APP_FIND_PHONE, URN_APP_FIND_DEVICE -> {
                            appFind.appFindBusiness(it)
                        }

                        URN_APP_MUSIC_CONTROL -> {
                            appMusicControl.musicControlBusiness(it)
                        }
                    }
                }

                URN_SPORT_DATA -> {//运动同步
                    when (it.urn[1]) {
//
//                        URN_SPORT_STEP -> {
//                            syncStepData.syncStepBusiness(it)
//                        }
//
//                        URN_SPORT_DISTANCE -> {
//
//                        }
//
//                        URN_SPORT_CALORIES -> {
//                            syncCaloriesData.syncCaloriesBusiness(it)
//                        }
//
//                        URN_SPORT_ACTIVITY_LEN -> {
//
//                            syncActivityDuration.syncActivityDurationDataBusiness(it)
//                        }
//
//                        URN_SPORT_DAILY_ACTIVITY_LEN -> {
//
//                        }
//
//                        URN_SPORT_DISTANCE -> {
//                            syncDistanceData.syncDistanceBusiness(it)
//                        }
//
//                        URN_SPORT_OXYGEN -> {
//                            syncOxygenData.syncOxygenDataBusiness(it)
//                        }
//
//                        URN_SPORT_RATE -> {
//                            when (it.urn[2]) {
//                                URN_SPORT_RATE_RECORD -> {
//                                    syncHeartRateData.syncHeartRateBusiness(it)
//                                }
//
//                                URN_SPORT_RATE_REALTIME -> {
//                                    syncRealtimeRateData.syncRealHeartRateBusiness(it)
//                                }
//                            }
//                        }
//
//                        URN_SPORT_SLEEP -> {
//                            syncSleepData.syncRealHeartRateBusiness(it)
//                        }
//
//                        URN_SPORT_SUMMARY -> {
//                            syncSportSummaryData.syncSportSummaryDataBusiness(it)
//                        }

                        URN_SPORT_DAILY_ACTIVITY_LEN -> {
                            syncDailyActivityDurationData.syncDailyActivityDurationDataBusiness(it)
                        }
//
//                        URN_SPORT_10S_RATE -> {
//                            syncSportSummaryData.syncTenSecondsRateBusiness(it)
//                        }
//
//                        URN_SPORT_10S_STEP_FREQUENCY -> {
//                            syncSportSummaryData.syncTenSecondsStepFrequencyBusiness(it)
//                        }
//
//                        URN_SPORT_10S_DISTANCE -> {
//                            syncSportSummaryData.syncTenSecondsDistanceBusiness(it)
//                        }
//
//                        URN_SPORT_10S_CALORIES -> {
//                            syncSportSummaryData.syncTenSecondsCaloriesBusiness(it)
//                        }
//
                    }
                }
            }
        }
    }


    private fun isMsgStopped(head: Byte, cmdId: Short): Boolean {
        return head != HEAD_FILE_SPP_A_2_D && head != HEAD_CAMERA_PREVIEW && !isCameraCmd(
            head, cmdId
        )
    }

    private fun isCameraCmd(head: Byte, cmdId: Short): Boolean {
        return head == HEAD_COMMON && (cmdId == CMD_ID_8028 || cmdId == CMD_ID_8029 || cmdId == CMD_ID_802A || cmdId == CMD_ID_802B || cmdId == CMD_ID_802C)
    }

    override fun socketNotifyError(obj: MsgBean?) {

    }

    override fun onConnectFailed(device: BluetoothDevice, msg: String?) {

        wmLog.logE(TAG, "onConnectFailed:$msg")

        if (device == mCurrDevice) {

            if (msg!!.contains("socket might closed or timeout") || msg.contains("Connection reset by peer") || msg.contains(
                    "Connect refused"
                ) && mBindStateMap.get(device.address) == true
            ) {
                mConnectTryCount++

                wmLog.logE(TAG, "reconnect times:$mConnectTryCount")

                if (mConnectTryCount < MAX_RETRY_COUNT) {
                    reConnect(device)
                } else {
                    mConnectTryCount = 0
                    btStateChange(WmConnectState.DISCONNECTED)
                }
            } else {
                mConnectTryCount = 0
                btStateChange(WmConnectState.DISCONNECTED)
            }
        }
    }

    override fun connectScanQr(bindInfo: WmBindInfo): WmDevice? {
        mBindInfo = bindInfo

        mBindInfo?.let {
            bindInfo.model = if (bindInfo.deviceType == mDeviceType) {
                WmDeviceModel.SJ_WATCH
            } else {
                WmDeviceModel.NOT_REG
            }

            return bindInfo.macAddress?.let {
                connect(it, bindInfo)
            }

        } ?: run {
            return WmDevice(WmDeviceModel.NOT_REG)
        }

    }

    /**
     * 通过address 连接
     */
    override fun connect(
        address: String, bindInfo: WmBindInfo
    ): WmDevice {
        synchronized(this) {
            mBindInfo = bindInfo

            mBindInfo?.let {

                val wmDevice = WmDevice(bindInfo.model)
                mCurrAddress = address
                mBtStateReceiver?.let {
                    it.setmCurrDevice(mCurrAddress)
                }

                if (TextUtils.isEmpty(bindInfo.userId)) {
                    btStateChange(WmConnectState.DISCONNECTED)
                    return wmDevice
                }

                wmDevice.address = address
                wmDevice.mode = bindInfo.model
                wmDevice.randomCode = bindInfo.randomCode

                wmDevice.isRecognized = bindInfo.model == WmDeviceModel.SJ_WATCH

                if (wmDevice.isRecognized) {
                    try {
                        btStateChange(WmConnectState.CONNECTING)
                        mCurrDevice = mBtAdapter.getRemoteDevice(address)
                        mCurrDevice!!.name?.let {
                            mDeviceName = it
                            wmDevice.name = it
                        }
                        mBtEngine.connect(mCurrDevice)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        btStateChange(WmConnectState.DISCONNECTED)
                    }
                } else {
                    observeConnectState.onNext(WmConnectState.DISCONNECTED)
                }

                wmLog.logD(TAG, " connect:${wmDevice}")

                return wmDevice
            } ?: run {
                return WmDevice(WmDeviceModel.NOT_REG)
            }
        }
    }

    /**
     * 通过BluetoothDevice 连接
     */
    override fun connect(
        bluetoothDevice: BluetoothDevice, bindInfo: WmBindInfo
    ): WmDevice {
        synchronized(this) {
            mBindInfo = bindInfo
            mBindInfo?.let {
                mCurrDevice = bluetoothDevice
                val wmDevice = WmDevice(bindInfo.model)

                if (TextUtils.isEmpty(bindInfo.userId)) {
                    btStateChange(WmConnectState.DISCONNECTED)
                    return wmDevice
                }

                mCurrAddress = bluetoothDevice.address
                wmDevice.address = bluetoothDevice.address
                wmDevice.isRecognized = bindInfo.model == WmDeviceModel.SJ_WATCH

                bluetoothDevice.name?.let {
                    mDeviceName = it
                    wmDevice.name = it
                }

                mCurrAddress?.let {
                    if (mBtEngine.getSocketState(it) == SOCKET_STATE_NONE) {
                        mBtStateReceiver?.let {
                            it.setmCurrDevice(mCurrAddress)
                        }

                        if (wmDevice.isRecognized) {
                            wmLog.logE(TAG, "sdk pre connect:${wmDevice}")
                            btStateChange(WmConnectState.CONNECTING)
                            mBtEngine.connect(bluetoothDevice)
                        } else {
                            btStateChange(WmConnectState.DISCONNECTED)
                        }
                    }

                    wmLog.logD(TAG, " connect:${wmDevice}")
                }

                return wmDevice
            } ?: run {
                return WmDevice(WmDeviceModel.NOT_REG)
            }
        }
    }

    /**
     * 重连
     */
    private fun reConnect(device: BluetoothDevice) {
        mBindInfo?.let {
            connect(device, it)
        }
    }

    fun btStateChange(state: WmConnectState) {
        wmTransferFile.mTransferring = false
        observeConnectState.onNext(state)
        mConnectState = state

        if (state == WmConnectState.DISCONNECTED) {
            btDisconnectSet()
        }
    }

    override fun disconnect() {
        mBtEngine.clearMsgQueue()
        mBtEngine.closeSocket("app", true)
    }

    override fun reset(): Completable {

        mCurrAddress?.let {
            mBindStateMap.put(it, false)
        }
        sharedPreferencesUtils.putString(BT_ADDRESS, "")

        return Completable.create { emitter ->
            unbindEmitter = emitter

            if (mConnectState == WmConnectState.VERIFIED) {
                sendSyncSafeMsg(CmdHelper.getUnBindCmd())
            } else {
                emitter.onError(RuntimeException("NOT VERIFIED"))
            }
        }
    }

    override fun reboot(): Completable {
        return Completable.create { emitter ->
            unbindEmitter = emitter

            if (mConnectState == WmConnectState.VERIFIED) {
                sendSyncSafeMsg(CmdHelper.getRebootCmd())

                mHandler.postDelayed({

                    unbindEmitter?.let {
                        if (!it.isDisposed) {
                            it.onComplete()
                        }
                    }

                }, 5 * 1000)
            } else {
                emitter.onError(RuntimeException("not VERIFIED"))
            }
        }
    }

    private val mObservableConnectState: PublishSubject<WmConnectState> =
        PublishSubject.create()

    override val observeConnectState: PublishSubject<WmConnectState> = mObservableConnectState

    override fun getConnectState(): WmConnectState {
        return mConnectState
    }

    override fun setDeviceModel(wmDeviceModel: WmDeviceModel): Boolean {
        return wmDeviceModel == WmDeviceModel.SJ_WATCH
    }

    override fun startDiscovery(
        scanTime: Int, wmTimeUnit: WmTimeUnit, deviceModel: WmDeviceModel, tag: String
    ): Observable<WmDiscoverDevice> {
        discoveryTag = tag

        return Observable.create { emitter ->
            discoveryObservableEmitter = emitter

            wmLog.logD(TAG, "discoveryObservableEmitter:$discoveryObservableEmitter")

            if (rxBleClient.isScanRuntimePermissionGranted) {
                scanBleDevices()
            }

            val stopAfter: Long = when (wmTimeUnit) {
                WmTimeUnit.SECONDS -> {
                    scanTime * 1000L
                }
                WmTimeUnit.MILLISECONDS -> {
                    scanTime.toLong()
                }
                WmTimeUnit.MINUTES -> {
                    scanTime * 1000 * 60L
                }
                else -> {
                    scanTime.toLong()
                }
            }

            wmLog.logE(TAG, "stopAfter:$stopAfter")

            mHandler.postDelayed({
                if (!discoveryObservableEmitter.isDisposed) {
                    discoveryObservableEmitter?.onComplete()
                    wmLog.logD(TAG, "stop discovery onComplete")
                }
                dispose()
            }, stopAfter)

        }
    }

    override fun stopDiscovery() {
        if (!discoveryObservableEmitter.isDisposed) {
            discoveryObservableEmitter?.onComplete()
            wmLog.logD(TAG, "stop discovery onComplete")
        }
        dispose()
    }

    private var scanDisposable: Disposable? = null

    private val isScanning: Boolean
        get() = scanDisposable != null

    private fun scanBleDevices() {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        val scanFilter = ScanFilter.Builder()
//            .setDeviceAddress("B4:99:4C:34:DC:8B")
            // add custom filters if needed
            .build()

        rxBleClient.scanBleDevices(scanSettings, scanFilter)
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally { dispose() }
            .subscribe({
                addScanResult(it)
            }, {
                onScanFailure(it)
            })
            .let { scanDisposable = it }
    }

    private fun addScanResult(bleScanResult: ScanResult) {
        val bleDevice = bleScanResult.bleDevice
        if (!TextUtils.isEmpty(bleDevice.name)) {
            val byteBuffer = ByteBuffer.wrap(bleScanResult.scanRecord.bytes)

            val id = byteBuffer.get(5).toInt().and(0xFF)
            val productType = byteBuffer.get(6).toInt().and(0xFF)
            val deviceType = byteBuffer.get(7).toInt().and(0xFF)

            if (id + productType == deviceType && deviceType == DEVICE_MANUFACTURER_CODE) {
                bleDevice.name?.let {
                    if (!TextUtils.isEmpty(discoveryTag) && it.contains(discoveryTag)) {
                        discoveryObservableEmitter?.onNext(
                            WmDiscoverDevice(
                                bleDevice.bluetoothDevice,
                                bleScanResult.rssi
                            )
                        )
                    } else {
                        discoveryObservableEmitter?.onNext(
                            WmDiscoverDevice(
                                bleDevice.bluetoothDevice,
                                bleScanResult.rssi
                            )
                        )
                    }
                }
            }
        }
    }

    private fun onScanFailure(throwable: Throwable) {
        discoveryObservableEmitter?.onError(throwable)
    }

    private fun dispose() {
        scanDisposable?.dispose()
        scanDisposable = null
    }

    override fun getDeviceModel(): WmDeviceModel {
        return WmDeviceModel.SJ_WATCH
    }

    override fun getFunctionSupportState(): WmFunctionSupport {
        return mWmFunctionSupport
    }
}