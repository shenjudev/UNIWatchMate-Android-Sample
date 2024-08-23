package com.sjbt.sdk.sample.utils

import android.text.TextUtils
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmDeviceInfo
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.sjbt.sdk.sample.model.MuslimAllahInfo
import com.sjbt.sdk.sample.model.user.UserInfo
import com.sjbt.sdk.utils.log.GsonUtil

object CacheDataHelper {

    private var transferringFile = false
    private var synchronizingData = false
    var cameraLaunchedByDevice = false
    var cameraLaunchedBySelf = false
    private var currDeviceBean: WmDeviceInfo? = null
    private var globalUserInfo: UserInfo? = null

    var deviceConnectState:WmConnectState = WmConnectState.DISCONNECTED

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    val allahInfoMap = LinkedHashMap<Int, MuslimAllahInfo>();

    fun setGlobalUserInfo(userInfo: UserInfo) {
        globalUserInfo = userInfo
    }

    fun getUserInfo(): UserInfo? {
        return globalUserInfo
    }

    fun clearCachedData() {
//        setCurrentDeviceBean(null)
        currDeviceBean = null
    }

    fun clearDataWithOutAccount() {
//        setCurrentDeviceBean(null)
        currDeviceBean = null
    }


    fun setLongitude(longitude: Double) {
        this.longitude = longitude
    }

    fun getLongitude(): Double {
        return longitude
    }

    fun setLatitude(latitude: Double) {
        this.latitude = latitude
    }

    fun getLatitude(): Double {
        return latitude
    }


    fun setCurrentDeviceInfo(deviceBean: WmDeviceInfo?) {
        currDeviceBean = deviceBean
    }

    fun getCurrentDeiceBean(): WmDeviceInfo? {
        return currDeviceBean
    }

    fun getTransferring(): Boolean {
        return transferringFile
    }

    fun setTransferring(boolean: Boolean) {
        transferringFile = boolean
    }

    fun getSynchronizingData(): Boolean {
        return synchronizingData
    }

    fun setSynchronizingData(boolean: Boolean) {
        synchronizingData = boolean
    }

    /**
     * ALLAH 列表json
     */
    const val NAME_OF_ALLAH_STR = "name_of_allah"

    /**
     * 祈祷提醒
     */
    const val PRAY_REMINDER_JSON = "pray_reminder_json";

    /**
     * 赞颂祈祷const val
     */
    const val TASBIH_REMINDER_JSON = "tasbih_reminder_json";

    /**
     * 祈祷时间
     */
    const val PRAY_TIME_JSON = "pray_time_json";

    /**
     * prayconst val type
     */
    const val PRAY_TIME_TYPE = "pray_time_type";

    /**
     * prayconst val ime type
     */
    const val PRAY_TIME_ASR_TYPE = "pray_time_asr_type";


    fun getNameOfAllahList(): List<MuslimAllahInfo> {

        val nameOfAllahJson = SPUtils.getInstance().getString(NAME_OF_ALLAH_STR)

        if (TextUtils.isEmpty(nameOfAllahJson)) {
            return arrayListOf()
        }

        val nameOfAllahList = GsonUtil.formatJson2List(nameOfAllahJson, MuslimAllahInfo::class.java)

        return nameOfAllahList
    }

    fun setNameOfAllahList(nameOfAllahList: List<MuslimAllahInfo>) {
        if (nameOfAllahList.isEmpty()) {
            SPUtils.getInstance().put(NAME_OF_ALLAH_STR, "")
            return
        }

        SPUtils.getInstance().put(NAME_OF_ALLAH_STR, GsonUtil.toJson(nameOfAllahList))
    }


    fun savePrayReminderJson(str: String) {
        LogUtils.d(TAG, "存入PrayReminder json: $str")
        SPUtils.getInstance().put(PRAY_REMINDER_JSON, str)
    }

    fun getPrayReminderJson(): String {
        val str = SPUtils.getInstance().getString(PRAY_REMINDER_JSON, "") as String
        LogUtils.d(TAG, "获取PrayReminder json: $str")
        return str
    }

    fun saveTasbihJson(str: String) {
        LogUtils.d(TAG, "存入Tasbih json: $str")
        SPUtils.getInstance().put(TASBIH_REMINDER_JSON, str)
    }

    fun getTasbihJson(): String {
        val str = SPUtils.getInstance().getString(TASBIH_REMINDER_JSON, "") as String
        LogUtils.d(TAG, "获取Tasbih json: $str")
        return str
    }

    fun savePrayTimeType(type: Int) {
        LogUtils.d(TAG, "存入prayTime type: $type")
        SPUtils.getInstance().put(PRAY_TIME_TYPE, type)
    }

    fun getPrayTimeType(): Int {
        val str = SPUtils.getInstance().getInt(PRAY_TIME_TYPE, 0) as Int
        LogUtils.d(TAG, "获取prayTime json: $str")
        return str
    }

    fun savePrayTimeAsrType(type: Int) {
        LogUtils.d(TAG, "存入prayTime asr type: $type")
        SPUtils.getInstance().put(PRAY_TIME_ASR_TYPE, type)
    }

    fun getPrayTimeAsrType(): Int {
        val str = SPUtils.getInstance().getInt(PRAY_TIME_ASR_TYPE, 0) as Int
        LogUtils.d(TAG, "获取prayTime asr type: $str")
        return str
    }

}