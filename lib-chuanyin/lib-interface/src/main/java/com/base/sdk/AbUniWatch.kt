package com.base.sdk

import android.bluetooth.BluetoothDevice
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.WmDevice
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.common.WmDiscoverDevice
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.common.WmTimeUnit
import com.base.sdk.entity.data.WmBatteryInfo
import com.base.sdk.entity.settings.WmDeviceInfo
import com.base.sdk.port.log.AbWmLog
import com.base.sdk.port.AbWmTransferFile
import com.base.sdk.port.app.AbWmApps
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.port.sync.AbWmSyncs
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * sdk接口抽象类
 * 封装了几大功能模块
 * sdk需要实现此接口，并实现每一个功能模块下的功能
 * 拿到此接口实例即可操作sdk实现的所有功能
 *
 * sdk interface abstract class
 * Encapsulates several major functional modules
 * The sdk implementation class needs to implement this interface and implement the functions under each functional module
 * App can operate all functions implemented by sdk after getting an instance of this interface implementation class.
 */
abstract class AbUniWatch {

    /**
     * 设置模块
     */
    abstract val wmSettings: AbWmSettings

    /**
     * 应用模块
     */
    abstract val wmApps: AbWmApps

    /**
     * 同步模块
     */
    abstract val wmSync: AbWmSyncs

    /**
     * 文件传输
     */
    abstract val wmTransferFile: AbWmTransferFile

    /**
     * 日志
     */
    abstract val wmLog: AbWmLog

    /**
     * 是否支持日志打印
     */
    abstract fun setLogEnable(logEnable: Boolean)

    /**
     * 连接方法
     * @return 如果返回null，表示无法识别此[deviceMode]
     */
    abstract fun connect(
        address: String,
        bindInfo: WmBindInfo
    ): WmDevice?

    /**
     * 连接方法
     * @return 如果返回null，表示无法识别此[deviceMode]
     */
    abstract fun connect(
        device: BluetoothDevice,
        bindInfo: WmBindInfo
    ): WmDevice?

    /**
     * 连接方法
     * @return 如果返回null，表示无法识别此[qrString]
     */
    abstract fun connectScanQr(
        bindInfo: WmBindInfo,
    ): WmDevice?

    /**
     * 断开连接
     */
    abstract fun disconnect()

    /**
     * 恢复出厂设置
     */
    abstract fun reset(): Completable

    /**
     * 监听连接状态
     */
    abstract val observeConnectState: Observable<WmConnectState>

    /**
     * 获取连接状态
     */
    abstract fun getConnectState(): WmConnectState

    /**
     * 获取当前设备模式。
     * @return null表示尚未
     */
    abstract fun getDeviceModel(): WmDeviceModel?

    /**
     * 设置设备模式.
     * 如果一个SDK支持多个模式，需要保存当前模式，以便在 [getDeviceModel] 获取
     * @return 设置是否成功
     */
    abstract fun setDeviceModel(wmDeviceModel: WmDeviceModel): Boolean

    /**
     * 获取设备信息
     */
    abstract fun getDeviceInfo(): Single<WmDeviceInfo>

    /**
     * 获取电量信息
     */
    abstract fun getBatteryInfo(): Single<WmBatteryInfo>

    /**
     * 监听电量信息变化
     */
    abstract val observeBatteryChange: Observable<WmBatteryInfo>

    /**
     * 开始扫描设备
     * tag 过滤设备名字前/后缀 例如：oraimo Watch Neo
     */
    abstract fun startDiscovery(
        scanTime: Int,
        wmTimeUnit: WmTimeUnit,
        deviceModel: WmDeviceModel,
        tag: String
    ): Observable<WmDiscoverDevice>

    /**
     * 停止蓝牙扫描
     */
    abstract fun stopDiscovery()

    /**
     * 设备重启
     */
    abstract fun reboot(): Completable

    /**
     * 功能是否支持
     */
    abstract fun isFunctionAvailable(functionType: FunctionType): Boolean

}

enum class FunctionType {
    /**
     * 是否支持 表盘市场
     */
    SUPPORT_DIAL_MARKET,

    /**
     * 是否支持 通知
     */
    SUPPORT_NOTIFY,

    /**
     * 通知列表是否全部展开
     */
    NOTIFY_APPS_UNFOLD,

    /**
     * 是否支持 通话蓝牙
     */
    SUPPORT_BLE_PHONE,

    /**
     * 是否支持 显示关闭通话蓝牙
     */
    SUPPORT_CLOSE_BLE_PHONE,
    /**
     * 是否支持 同步联系人
     */
    SUPPORT_CONTACTS,

    /**
     * 是否支持紧急联系人
     *
     */
    SUPPORT_EMERGENCY_CONTACT,

    /**
     * 是否支持 同步收藏联系人
     */
    SUPPORT_FAVORITE_CONTACTS,

    /**
     * 是否支持 快捷回复
     */
    SUPPORT_QUICK_REPLY,

    /**
     * 是否支持步数目标
     */
    SUPPORT_STEP_GOAL,

    /**
     * 是否支持卡路里目标
     *
     */
    SUPPORT_CALORIE_GOAL,

    /**
     * 是否支持活动时长目标
     *
     */
    SUPPORT_ACTIVITY_DURATION_GOAL,
    /**
     * 是否支持 久坐提醒
     */
    SUPPORT_REMINDER_LONG_SIT,

    /**
     * 是否支持 喝水提醒
     */
    SUPPORT_REMINDER_DRINK_WATER,

    /**
     * 是否支持 洗手提醒
     */
    SUPPORT_REMINDER_WASH_HAND,

    /**
     * 是否支持 心率自动检测
     */
    SUPPORT_HEART_RATE_MONITOR,

    /**
     * 是否支持 REM快速眼动
     */
    SUPPORT_REM,

    /**
     * 是否支持 运动分类
     */
    SUPPORT_SPORT_TYPE,

    /**
     * 是否显示固定运动类型
     */
    SPORT_SHOW_FIXED,

    /**
     * 是否支持 运动自识别开始
     */
    SUPPORT_SPORT_AUTO_START,

    /**
     * 是否支持 运动自识别结束
     */
    SUPPORT_SPORT_AUTO_PAUSE,

    /**
     * 是否支持 闹钟
     */
    SUPPORT_ALARM,

    /**
     * 是否支持闹钟标签
     *
     */
    SUPPORT_ALARM_LABEL,

    /**
     * 是否支持闹钟备注
     *
     */
    SUPPORT_ALARM_REMARK,

    /**
     * 是否支持 天气
     */
    SUPPORT_WEATHER,

    /**
     * 是否支持 查找手机
     */
    SUPPORT_FIND_PHONE,

    /**
     * 是否支持 查找手表
     */
    SUPPORT_FIND_WEAR,

    /**
     * 是否支持 世界时钟
     */
    SUPPORT_WORLD_CLOCK,

    /**
     * 是否支持 摇摇拍照
     */
    SUPPORT_HID_BLE,

    /**
     * 是否支持 遥控拍照
     * APP内置拍照功能
     *
     */
    SUPPORT_REMOTE_CAMERA,

    /**
     * 是否支持拍照预览
     */
    SUPPORT_CAMERA_PREVIEW,

    /**
     * 是否支持 设备语言
     */
    SUPPORT_LANGUAGE,
    /**
     * 是否支持 小部件
     */
    SUPPORT_SMALL_FUNCTION,
    /**
     * 是否支持音量调节
     *
     * @return
     */
    SUPPORT_VOLUME_CONTROL,

    /**
     * 是否支持安静心率过高提
     * 与日常心率过高提醒互斥，
     * 支持日常心率提醒，则不细分运动心率、安静心率过高提醒
     * {@link BaseWatchFunctions#isSupportDailyHeartRateWarning()}
     *
     * @return
     */
    SUPPORT_RESTING_HEART_RATE_WARNING,
    /**
     * 是否支持运动心率过高提醒
     * 与日常心率过高提醒互斥，
     * 支持日常心率提醒，则不细分运动心率、安静心率过高提醒
     * {@link BaseWatchFunctions#isSupportDailyHeartRateWarning()}
     *
     * @return
     */
    SUPPORT_EXERCISE_HEART_RATE_WARNING,

    /**
     * 是否支持日常心率过高提醒
     * 与运动心率、安静心率过高提醒互斥，
     * 支持日常心率提醒，则不细分运动心率、安静心率过高提醒
     * {@link BaseWatchFunctions#isSupportRestingHeartRateWarning()}
     * {@link BaseWatchFunctions#isSupportExerciseHeartRateWarning()}
     *
     * @return
     */
    SUPPORT_DAILY_HEART_RATE_WARNING,
    /**
     * 是否支持连续血氧
     *
     * @return
     */
    SUPPORT_CONTINUOUS_BLOOD_OXYGEN,

    /**
     * 是否支持蓝牙断连提醒设置
     *
     * @return
     */
    SUPPORT_BLUETOOTH_SETTINGS,
    /**
     * 是否支持导入本地音乐
     *
     * @return
     */
    SUPPORT_IMPORT_LOCAL_MUSIC,

    /**
     * 通话蓝牙与BLE是否同名
     *
     * @return
     */
    BT_BLE_SAME_NAME,

    /**
     * 是否支持事件提醒
     *
     * @return
     */
    SUPPORT_EVENT_REMINDER,

    /**
     * 是否支持亮屏提醒
     *
     * @return
     */
    SUPPORT_SCREEN_DURATION,

    /**
     * 是否支持 重启设备
     */
    SUPPORT_REBOOT
}