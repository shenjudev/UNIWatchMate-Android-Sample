package com.sjbt.sdk.sample

import android.app.Application
import android.util.Log
import com.base.api.BuildConfig
import com.base.api.UNIWatchMate
import com.base.sdk.entity.WmSupportTypeBean
import com.sjbt.sdk.SJUniWatch
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import timber.log.Timber

fun uniWatchInit(application: Application) {
    //1.设置log输出(Set log output)
    //"UNIWatchMate"使用Timber作为日志输出，所以需要配置Timber ("UNIWatchMate" uses Timber as log output, so you need to configure Timber)
    if (BuildConfig.DEBUG) {
        if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }
    } else {
        Timber.plant(object : Timber.DebugTree() {
            override fun isLoggable(tag: String?, priority: Int): Boolean {
                return priority > Log.DEBUG
            }
        })
    }
    RxJavaPlugins.setErrorHandler { throwable: Throwable ->
        Timber.e("RxJavaPlugins throwable=" + throwable.message)
    }

    //2.配置支持不同厂商的手表(Configurations Support watches from different manufacturers)
    val sjUniWatch = SJUniWatchImpl(application, 10000,"");//绅聚设备操作对象(shenju device operation objects)

    sjUniWatch.setLogEnable(true)
    val supportDeviceList = arrayListOf<WmSupportTypeBean>()

    supportDeviceList.add(
        WmSupportTypeBean("T09",
            "T09",
            "https://cdniotpub.aiframe.net/media/spark/device/784a30834a783866bfe34a19be29a2c0/9eb573c72e183dfb932a7872230daa42.png",
            "https://cdniotpub.aiframe.net/media/spark/device/784a30834a783866bfe34a19be29a2c0/58f5ef91d2cb3026b8909447b30966b1.png")
    )

    supportDeviceList.add(
        WmSupportTypeBean("802N",
        "oraimo Watch Nova V",
        "https://cdniotpub.aiframe.net/media/spark/device/1279142e7dd9331bb8259d34acb81885/bc328ac1036a3ad48b8d0d68becc0aea.png",
        "https://cdniotpub.aiframe.net/media/spark/device/1279142e7dd9331bb8259d34acb81885/d404e8fb4c9333ab9352bb8743099b5f.png")
    )

    supportDeviceList.add(
        WmSupportTypeBean("Spark Watch",
            "Spark Watch",
            "https://cdniotpub.aiframe.net/media/spark/device/784a30834a783866bfe34a19be29a2c0/b34a75927b93366b9c7d41d9e4e3a67c.png",
            "https://cdniotpub.aiframe.net/media/spark/device/784a30834a783866bfe34a19be29a2c0/4a2ec1313e593ffabc3917f787927638.png")
    )

    supportDeviceList.add(
        WmSupportTypeBean("TY42_ZYT01",
            "TY42_ZYT01",
            "https://cdniotpub.aiframe.net/media/spark/device/784a30834a783866bfe34a19be29a2c0/b34a75927b93366b9c7d41d9e4e3a67c.png",
            "https://cdniotpub.aiframe.net/media/spark/device/784a30834a783866bfe34a19be29a2c0/4a2ec1313e593ffabc3917f787927638.png")
    )
    sjUniWatch.setSupportDeviceTypeList(supportDeviceList)

    UNIWatchMate.init(
        application, listOf(
            sjUniWatch
        )
    )
}

class SJUniWatchImpl(
    mContext: Application,
    mMsgTimeOut: Int,
    mLogPath:String?
) : SJUniWatch(mContext,mMsgTimeOut,mLogPath) {

}

//class FcUniWatchImpl(application: Application) : FcUniWatch(application) {
//
//    init {
//        //1. FitCloud-SDK 需要知道当前APP的前后台状态
//        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
//            var startCount = 0
//            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//            }
//
//            override fun onActivityStarted(activity: Activity) {
//                if (startCount == 0) {
//                    //At this time, the APP enters the foreground
//                    isForeground = true
//                }
//                startCount++
//            }
//
//            override fun onActivityResumed(activity: Activity) {
//            }
//
//            override fun onActivityPaused(activity: Activity) {
//            }
//
//            override fun onActivityStopped(activity: Activity) {
//                startCount--
//                if (startCount == 0) {
//                    //At this time, the APP enters the background
//                    isForeground = false
//                }
//            }
//
//            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//            }
//
//            override fun onActivityDestroyed(activity: Activity) {
//            }
//        })
//
//        //2.FitCloud-SDK 内部使用 "RxAndroidBLE"，所以需要配置RxAndroidBLE的日志输出
//        RxBleClient.updateLogOptions(
//            LogOptions.Builder()
//                .setShouldLogAttributeValues(BuildConfig.DEBUG)
//                .setShouldLogScannedPeripherals(false)
//                .setMacAddressLogSetting(if (BuildConfig.DEBUG) LogConstants.MAC_ADDRESS_FULL else LogConstants.NONE)
//                .setUuidsLogSetting(if (BuildConfig.DEBUG) LogConstants.UUIDS_FULL else LogConstants.NONE)
//                .setLogLevel(LogConstants.WARN)
//                .setLogger { level, tag, msg ->
//                    Timber.tag(tag).log(level, msg)
//                }
//                .build()
//        )
//    }
//
//    /**
//     * 延迟创建具体的SDK示例，提高性能
//     */
//    override fun create(application: Application): FcSDK {
//        val sdk = FcSDK
//            .Builder(application)
//            // 因为FitCloud-SDK 内部使用 "RxAndroidBLE"，需要创建RxBleClient对象。
//            // 如果项目里有其他SDK也使用了RxBleClient，那么需要自己维持一个单例，然后设置到这里。
////          .setRxBleClient(RxBleClient.create(application))
//            .build()
//        sdk.setReConnectFrequent(false)//在后台时减少回连的次数，节省电量
//        return sdk
//    }
//}