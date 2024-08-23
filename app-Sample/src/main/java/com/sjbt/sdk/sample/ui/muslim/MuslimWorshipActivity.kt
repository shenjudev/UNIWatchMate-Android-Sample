package com.sjbt.sdk.sample.ui.muslim

import android.content.Intent
import android.icu.util.Calendar
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PersistableBundle
import android.text.TextUtils
import android.view.View
import android.provider.Settings;
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatToggleButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.WmMuslimLocation
import com.base.sdk.entity.apps.PrayRemind
import com.base.sdk.entity.apps.WmMuslimCalcParam
import com.base.sdk.entity.apps.WmMuslimCalculateType
import com.base.sdk.entity.apps.WmPrayReminder
import com.blankj.utilcode.util.LogUtils
import com.kyleduo.switchbutton.SwitchButton
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.data.device.SJDataConvertTools
import com.sjbt.sdk.sample.databinding.ActivityMuslimWorshipBinding
import com.sjbt.sdk.sample.model.muslim.MuslimCalcParam
import com.sjbt.sdk.sample.model.muslim.MuslimPrayRemind
import com.sjbt.sdk.sample.model.muslim.MuslimPrayRemind.PRAY_ASR_ID
import com.sjbt.sdk.sample.model.muslim.MuslimPrayRemind.PRAY_DHUHR_ID
import com.sjbt.sdk.sample.model.muslim.MuslimPrayRemind.PRAY_FAJR_ID
import com.sjbt.sdk.sample.model.muslim.MuslimPrayRemind.PRAY_ISHA_ID
import com.sjbt.sdk.sample.model.muslim.MuslimPrayRemind.PRAY_MAGHRIB_ID
import com.sjbt.sdk.sample.model.muslim.MuslimPrayRemind.PRAY_SUNRISE_ID
import com.sjbt.sdk.sample.model.muslim.MuslimPrayReminder
import com.sjbt.sdk.sample.model.muslim.MuslimPrayTime
import com.sjbt.sdk.sample.model.muslim.MuslimTimeCalculateTypeParam
import com.sjbt.sdk.sample.model.muslim.MuslimTimeCalculateTypeUI
import com.sjbt.sdk.sample.ui.dialog.CommWheelViewDialog
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.DateUtil
import com.sjbt.sdk.sample.utils.LocationWrapper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.widget.CommItemView
import com.sjbt.sdk.utils.log.GsonUtil
import com.sjbt.sdk.utils.log.PermissionUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import java.util.Locale
import java.util.TimeZone


class MuslimWorshipActivity : BaseActivity(), View.OnClickListener {

    private val TAG = "MuslimWorshipActivity"

    private var mPrayReminder: MuslimPrayReminder? = null

    //设备端参数
    private var mDeviceTimeTypeParam = MuslimTimeCalculateTypeParam(0, 0)

    //选择弹窗用
    private val mTimeCalculateTypeUIS = arrayListOf<MuslimTimeCalculateTypeUI>()
    private val mTimeCalculateTypeNameList = ArrayList<String>()

    //ASR选择弹窗用
    private val mAsrTimeCalculateTypeUIS = arrayListOf<MuslimTimeCalculateTypeUI>()
    private val mAsrTypeNameList = ArrayList<String>()

    //JNI 计算时间参数
    private var mJNIGetTimeParam: MuslimCalcParam? = null

    //今天的时间计算结果列表
    private var mTodayMuslimPrayTimes: ArrayList<MuslimPrayTime>? = null

    private var locationWrapper: LocationWrapper? = null
    private var mCalendar: Calendar = Calendar.getInstance()

    private var currentMuslimPrayTime: MuslimPrayTime? = null
    private var nextMuslimPrayTime: MuslimPrayTime? = null

    private var countDownTimer: CountDownTimer? = null
    private var timeZooOffsetInHours: Int = 0
    private var lastHour = 0
    private var lastMinute = 0

    private var requestPermissionSetting = false
    private var mLocationPermission: Array<String>? = null

    private lateinit var binding: ActivityMuslimWorshipBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMuslimWorshipBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.switchReminder.setOnClickListener(this)
        binding.ivPrevDay.setOnClickListener(this)
        binding.ivNextDay.setOnClickListener(this)
        binding.imgFajrAlarm.setOnClickListener(this)
        binding.imgSunriseAlarm.setOnClickListener(this)
        binding.imgDhuhrAlarm.setOnClickListener(this)
        binding.imgAsrAlarm.setOnClickListener(this)
        binding.imgMaghribAlarm.setOnClickListener(this)
        binding.imgIshaAlarm.setOnClickListener(this)

        binding.llTasbihReminder.setOnClickListener(this)
        binding.llNameOfAllah.setOnClickListener(this)
        binding.llQiblaDirection.setOnClickListener(this)
        binding.llCalcMethod.setOnClickListener(this)
        binding.llAsrMethod.setOnClickListener(this)

        binding.tvDate.setText(
            DateUtil.formatDateFromTimeMillis(
                System.currentTimeMillis(), DateUtil.FORMAT_MDY,
                Locale.ENGLISH
            )
        )

        mCalendar = Calendar.getInstance()
        mLocationPermission = PermissionUtil.getLocationPermission()

        locationWrapper = LocationWrapper.getInstance(this)

        mTimeCalculateTypeNameList.clear()
        mTimeCalculateTypeNameList.add(resources.getString(R.string.shia_ithna_ashari))
        mTimeCalculateTypeNameList.add(resources.getString(R.string.university_of_islamic_science))
        mTimeCalculateTypeNameList.add(resources.getString(R.string.muslim_world_league))
        mTimeCalculateTypeNameList.add(resources.getString(R.string.islamic_society_of_north_america))
        mTimeCalculateTypeNameList.add(resources.getString(R.string.umm_al_qura))

        mTimeCalculateTypeUIS.clear()
        mTimeCalculateTypeNameList.forEachIndexed { index, s ->
            val typeItem = MuslimTimeCalculateTypeUI(index, s)
            mTimeCalculateTypeUIS.add(typeItem)
        }

        mAsrTypeNameList.clear()
        mAsrTypeNameList.add(resources.getString(R.string.asr_shafli))
        mAsrTypeNameList.add(resources.getString(R.string.asr_hanafi))

        mAsrTimeCalculateTypeUIS.clear()
        mAsrTypeNameList.forEachIndexed { index, s ->
            val typeItem =
                MuslimTimeCalculateTypeUI(index, s)
            mAsrTimeCalculateTypeUIS.add(typeItem)
        }

        initPrayRemindData()

        checkLocation()

        MyApplication.instance.applicationScope.launch {

            launchWithLog {
                UNIWatchMate.wmApps.appMuslim.observePrayReminder.asFlow().collect {
                    LogUtils.i("监听祈祷提醒变化: ${GsonUtil.toJson(it)}")
                    val prayReminder = SJDataConvertTools.instance.convertPrayReminder(
                        it
                    )

                    CacheDataHelper.savePrayReminderJson(GsonUtil.toJson(prayReminder))

                    mPrayReminder = prayReminder

                    updatePrayReminder()

                }
            }
        }
    }

    private fun hourMinuteToStr(it: MuslimPrayTime) = "${
        if (it.hour < 10) {
            "0${it.hour}"
        } else {
            it.hour
        }
    }:${
        if (it.minute < 10) {
            "0${it.minute}"
        } else {
            it.minute
        }
    }"


//    override fun distributeEvent(event: BaseEvent<*>) {
//        super.distributeEvent(event)
//        when (event.type) {
//            MUSLIM_PRAY_REMINDER_CHANGE -> {
//                mPrayReminder =
//                    GsonUtil.fromJson(DeviceCache.getPrayReminderJson(), MuslimPrayReminder::class.java)
//                updatePrayReminder()
//            }
//        }
//    }


    private fun startCountDownTimer(timeLeftInMillis: Long) {

        if (countDownTimer == null) {
            createCountDownTimer(timeLeftInMillis)
        } else {
            countDownTimer!!.cancel()
            createCountDownTimer(timeLeftInMillis)
        }
    }

    private fun createCountDownTimer(timeLeftInMillis: Long) {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvPrayTimeLeft.setText(
                    String.format(
                        getString(R.string.starts_in),
                        DateUtil.millisToHMS(millisUntilFinished)
                    )
                )
            }

            override fun onFinish() {

                mJNIGetTimeParam?.let {
                    getPrayTimes(it)
                }
            }
        }.start()
    }

    private fun resetPrayTimeUi(muslimCalcParam: MuslimCalcParam) {
        binding.layoutFajr.setBackgroundColor(getColor(R.color.transparent))
        binding.layoutSunrise.setBackgroundColor(getColor(R.color.transparent))
        binding.layoutDhuhr.setBackgroundColor(getColor(R.color.transparent))
        binding.layoutAsr.setBackgroundColor(getColor(R.color.transparent))
        binding.layoutMaghrib.setBackgroundColor(getColor(R.color.transparent))

        if (isYesTodayEvent && DateUtil.isYesterday(
                muslimCalcParam.year,
                muslimCalcParam.month,
                muslimCalcParam.day
            )
        ) {
            binding.layoutIsha.setBackgroundColor(getColor(R.color.color_21E6E0))
        } else {
            binding.layoutIsha.setBackgroundColor(getColor(R.color.transparent))
        }
    }

    private var isYesTodayEvent = false
    private fun setPrayTime(hour: Int, minute: Int) {
        currentMuslimPrayTime = null
        mTodayMuslimPrayTimes?.forEach {
            if (DateUtil.getMillisecondByHour(
                    hour,
                    minute
                ) >= DateUtil.getMillisecondByHour(it.hour, it.minute)
            ) {
                currentMuslimPrayTime = it
            }
        }

        if (currentMuslimPrayTime != null) {
            isYesTodayEvent = false
            when (currentMuslimPrayTime!!.id) {
                PRAY_FAJR_ID -> {
                    binding.layoutFajr.setBackgroundColor(getColor(R.color.color_05BEB5))
                    binding.layoutSunrise.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutDhuhr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutAsr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutMaghrib.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutIsha.setBackgroundColor(getColor(R.color.transparent))
                }

                PRAY_SUNRISE_ID -> {
                    binding.layoutFajr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutSunrise.setBackgroundColor(getColor(R.color.color_05BEB5))
                    binding.layoutDhuhr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutAsr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutMaghrib.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutIsha.setBackgroundColor(getColor(R.color.transparent))
                }

                PRAY_DHUHR_ID -> {
                    binding.layoutFajr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutSunrise.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutDhuhr.setBackgroundColor(getColor(R.color.color_05BEB5))
                    binding.layoutAsr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutMaghrib.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutIsha.setBackgroundColor(getColor(R.color.transparent))
                }

                PRAY_ASR_ID -> {
                    binding.layoutFajr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutSunrise.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutDhuhr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutAsr.setBackgroundColor(getColor(R.color.color_05BEB5))
                    binding.layoutMaghrib.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutIsha.setBackgroundColor(getColor(R.color.transparent))
                }

                PRAY_MAGHRIB_ID -> {
                    binding.layoutFajr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutSunrise.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutDhuhr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutAsr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutMaghrib.setBackgroundColor(getColor(R.color.color_05BEB5))
                    binding.layoutIsha.setBackgroundColor(getColor(R.color.transparent))
                }

                PRAY_ISHA_ID -> {
                    binding.layoutFajr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutSunrise.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutDhuhr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutAsr.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutMaghrib.setBackgroundColor(getColor(R.color.transparent))
                    binding.layoutIsha.setBackgroundColor(getColor(R.color.color_05BEB5))
                }
            }
        } else {
            isYesTodayEvent = true
            binding.layoutFajr.setBackgroundColor(getColor(R.color.transparent))
            binding.layoutSunrise.setBackgroundColor(getColor(R.color.transparent))
            binding.layoutDhuhr.setBackgroundColor(getColor(R.color.transparent))
            binding.layoutAsr.setBackgroundColor(getColor(R.color.transparent))
            binding.layoutMaghrib.setBackgroundColor(getColor(R.color.transparent))
            binding.layoutIsha.setBackgroundColor(getColor(R.color.transparent))
        }

    }

    private fun getPrayTimes(muslimCalcParam: MuslimCalcParam) {

        LogUtils.i(TAG, "获取时间的参数列表 praytime:" + GsonUtil.toJson(muslimCalcParam))

        val wmMuslimCalcParam = convertWmMuslimCalcParam(muslimCalcParam)

        val result = UNIWatchMate.wmApps.appMuslim.getPrayTimes(wmMuslimCalcParam)

        if (result.isNotEmpty()) {


            LogUtils.i(TAG, "时间返回结果：" + GsonUtil.toJson(result))

            val today = Calendar.getInstance()

            // 获取小时（24小时制）
            val hour: Int = today.get(Calendar.HOUR_OF_DAY)

            // 获取分钟
            val minute: Int = today.get(Calendar.MINUTE)

            // 获取秒
            val second: Int = today.get(Calendar.SECOND)

            LogUtils.i(TAG, "当前 " + hour + "时" + minute + "分" + second + "秒")

            var findNext = false

            mTodayMuslimPrayTimes = arrayListOf()

            result?.forEach {
                val muslimPrayTime = MuslimPrayTime()
                muslimPrayTime.id = it.id
                muslimPrayTime.hour = it.hour
                muslimPrayTime.minute = it.minute

                if (it.id == PRAY_FAJR_ID) {
                    muslimPrayTime.name = getString(R.string.fajr)
                    binding.tvFajrTime.setText(hourMinuteToStr(muslimPrayTime))

                } else if (it.id == PRAY_SUNRISE_ID) {
                    muslimPrayTime.name = getString(R.string.sunrise)
                    binding.tvSunriseTime.setText(hourMinuteToStr(muslimPrayTime))
                } else if (it.id == PRAY_DHUHR_ID) {
                    muslimPrayTime.name = getString(R.string.dhuhr)
                    binding.tvDhuhrTime.setText(hourMinuteToStr(muslimPrayTime))

                } else if (it.id == PRAY_ASR_ID) {
                    muslimPrayTime.name = getString(R.string.asr)
                    binding.tvAsrTime.setText(hourMinuteToStr(muslimPrayTime))

                } else if (it.id == PRAY_MAGHRIB_ID) {
                    muslimPrayTime.name = getString(R.string.maghrib)
                    binding.tvMaghribTime.setText(hourMinuteToStr(muslimPrayTime))

                } else if (it.id == PRAY_ISHA_ID) {
                    muslimPrayTime.name = getString(R.string.isha)
                    binding.tvIshaTime.setText(hourMinuteToStr(muslimPrayTime))

                    if (DateUtil.isToday(
                            muslimCalcParam.year,
                            muslimCalcParam.month,
                            muslimCalcParam.day
                        )
                    ) {
                        lastHour = it.hour
                        lastMinute = it.minute
                    }
                }

                if (DateUtil.isToday(
                        muslimCalcParam.year,
                        muslimCalcParam.month,
                        muslimCalcParam.day
                    )
                ) {
                    if (DateUtil.getMillisecondByHour(hour, minute)
                        < DateUtil.getMillisecondByHour(it.hour, it.minute)
                        &&
                        !findNext
                    ) {
                        nextMuslimPrayTime = muslimPrayTime
                        findNext = true

                        var leftHour = it.hour - hour
                        var leftMinute = it.minute - minute

                        val totalLeftSeconds =
                            ((leftHour * 60 * 60 + leftMinute * 60 - second) * 1000).toLong()

                        LogUtils.i(TAG, "剩余总毫秒数：$totalLeftSeconds")

                        setNextPrayEvent(totalLeftSeconds, muslimPrayTime)
                    }

                }

                mTodayMuslimPrayTimes!!.add(muslimPrayTime)
            }

            if (DateUtil.isToday(
                    muslimCalcParam.year,
                    muslimCalcParam.month,
                    muslimCalcParam.day
                )
            ) {

                if (DateUtil.getMillisecondByHour(
                        hour,
                        minute
                    ) >= DateUtil.getMillisecondByHour(lastHour, lastMinute)
                ) {//Next是明天的第一个

                    val tomorrowMuslimCalcParam = MuslimCalcParam()
                    val tomorrow = DateUtil.getTomorrow()

                    tomorrowMuslimCalcParam.calcType = muslimCalcParam.calcType
                    tomorrowMuslimCalcParam.juristicMethod = muslimCalcParam.juristicMethod

                    tomorrowMuslimCalcParam.latitude = muslimCalcParam.latitude
                    tomorrowMuslimCalcParam.longitude = muslimCalcParam.longitude
                    tomorrowMuslimCalcParam.timeZone = muslimCalcParam.timeZone

                    tomorrowMuslimCalcParam.year = tomorrow.get(Calendar.YEAR)
                    tomorrowMuslimCalcParam.month = tomorrow.get(Calendar.MONTH) + 1
                    tomorrowMuslimCalcParam.day = tomorrow.get(Calendar.DAY_OF_MONTH)

                    getNextDatePrayTimes(tomorrowMuslimCalcParam, hour, minute, second)
                }

                setPrayTime(hour, minute)

            } else {//如果不是今天
                resetPrayTimeUi(muslimCalcParam)
            }
        }
    }

    private fun convertWmMuslimCalcParam(muslimCalcParam: MuslimCalcParam): WmMuslimCalcParam {
        val wmMuslimCalcParam = WmMuslimCalcParam()
        wmMuslimCalcParam.timeZone = muslimCalcParam.timeZone
        wmMuslimCalcParam.calcType = muslimCalcParam.calcType
        wmMuslimCalcParam.juristicMethod = muslimCalcParam.juristicMethod
        wmMuslimCalcParam.year = muslimCalcParam.year
        wmMuslimCalcParam.month = muslimCalcParam.month
        wmMuslimCalcParam.day = muslimCalcParam.day
        wmMuslimCalcParam.latitude = muslimCalcParam.latitude
        wmMuslimCalcParam.longitude = muslimCalcParam.longitude
        wmMuslimCalcParam.timeZone = muslimCalcParam.timeZone
        return wmMuslimCalcParam
    }

    private fun getNextDatePrayTimes(
        muslimCalcParam: MuslimCalcParam,
        currentHour: Int,
        currentMinute: Int,
        currentSecond: Int
    ) {

        LogUtils.i(
            TAG,
            "获取明天时间的参数列表:" + GsonUtil.toJson(muslimCalcParam)
        )
        val wmMuslimCalcParam = convertWmMuslimCalcParam(muslimCalcParam)
        val result = UNIWatchMate.wmApps.appMuslim.getPrayTimes(wmMuslimCalcParam)
        LogUtils.i(TAG, "明天时间列表返回结果：" + GsonUtil.toJson(result))

        result?.forEach {
            if (it.id == PRAY_FAJR_ID) {

                val muslimPrayTime = MuslimPrayTime()
                muslimPrayTime.name = getString(R.string.fajr)
                muslimPrayTime.id = it.id
                muslimPrayTime.hour = it.hour
                muslimPrayTime.minute = it.minute
                nextMuslimPrayTime = muslimPrayTime//默认是第一个
            }
        }

        nextMuslimPrayTime?.let {
            val totalLeftSeconds =
                ((it.hour + 24) * 60 * 60 + it.minute * 60 - currentHour * 60 * 60 - currentMinute * 60 - currentSecond) * 1000.toLong()

            LogUtils.i(TAG, "到明天fajr剩余时间：$totalLeftSeconds")

            setNextPrayEvent(totalLeftSeconds, it)
        }

    }


    private fun setNextPrayEvent(
        totalLeftSeconds: Long,
        it: MuslimPrayTime
    ) {

        startCountDownTimer(totalLeftSeconds)

        binding.tvEventName.text = it.name
        val prayTimeStr = hourMinuteToStr(it)
        binding.tvPrayTime.text = prayTimeStr

        when (it.id) {

            PRAY_FAJR_ID -> {
                binding.layoutNextEvent.setBackgroundResource(R.mipmap.img_muslim_bg_1)
            }

            PRAY_SUNRISE_ID -> {
                binding.layoutNextEvent.setBackgroundResource(R.mipmap.img_muslim_bg_2)
            }

            PRAY_DHUHR_ID -> {
                binding.layoutNextEvent.setBackgroundResource(R.mipmap.img_muslim_bg_3)
            }

            PRAY_ASR_ID -> {
                binding.layoutNextEvent.setBackgroundResource(R.mipmap.img_muslim_bg_4)
            }

            PRAY_MAGHRIB_ID -> {
                binding.layoutNextEvent.setBackgroundResource(R.mipmap.img_muslim_bg_5)
            }

            PRAY_ISHA_ID -> {
                binding.layoutNextEvent.setBackgroundResource(R.mipmap.img_muslim_bg_6)
            }
        }
    }

    private fun checkLocation() {
        mDeviceTimeTypeParam.calculateType = CacheDataHelper.getPrayTimeType()
        mDeviceTimeTypeParam.juristicMethodType = CacheDataHelper.getPrayTimeAsrType()

        if (PermissionUtil.checkPermission(this, *mLocationPermission!!)) {
            locationWrapper!!.setLocationChangeListener(object :
                LocationWrapper.LocationChangeListener {
                override fun onProviderEnabled(provider: String?) {
                    LogUtils.i(TAG, "定位权限打开 onProviderEnabled:$provider")
                }

                override fun onProviderDisabled(provider: String?) {
                    LogUtils.i(TAG, "定位权限关闭 onProviderDisabled:$provider")
                }

                override fun onLocationChanged(location: Location) {
                    LogUtils.i(
                        TAG,
                        "定位发生变化 onLocationChanged longitude: ${location.longitude} latitude: ${location.latitude}"
                    )
                    if (location != null) {
                        updateLocation(location)
                    }
                }
            })

            locationWrapper!!.requestLocation()

            val location = locationWrapper!!.location

            if (location != null) {
                updateLocation(location)
            } else {
                LogUtils.i(TAG, "获取不到定位")

                binding.tvPrayTimeLeft.setText(
                    String.format(
                        getString(R.string.starts_in),
                        "-"
                    )
                )
                binding.layoutLocationFail.visibility = View.VISIBLE

                binding.layoutLocationFail.setOnClickListener {
                    LogUtils.i(TAG, "跳转到开启定位")
                    requestPermissionSetting = true
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }

        } else {

            LogUtils.i(TAG, "没有位置权限")
            binding.tvPrayTimeLeft.setText(
                String.format(
                    getString(R.string.starts_in),
                    "-"
                )
            )
            binding.layoutLocationFail.visibility = View.VISIBLE

            binding.layoutLocationFail.setOnClickListener {
                requestPermissionSetting = true
                ToastUtil.showToast("没有定位权限")
                return@setOnClickListener
            }
        }

        updatePrayReminder()

        mAsrTimeCalculateTypeUIS.forEach {
            if (mDeviceTimeTypeParam.juristicMethodType == it.type) {
                binding.llAsrMethod.setData(it.name)
            }
        }

        mTimeCalculateTypeUIS.forEach {
            if (mDeviceTimeTypeParam.calculateType == it.type) {
                binding.llCalcMethod.setData(it.name)
            }
        }

    }

    private fun updatePrayReminder() {
        runOnUiThread {
            mPrayReminder?.let {
                binding.switchReminder.isChecked = it.switchState == 1
                binding.layoutPrayerTimings.visibility =
                    if (binding.switchReminder.isChecked) View.VISIBLE else View.GONE

                if (it.switchState == 1) {
                    it.prayReminds.forEach {
                        if (it.id == PRAY_FAJR_ID) {
                            binding.imgFajrAlarm.isChecked = it.isOpen
                        } else if (it.id == PRAY_SUNRISE_ID) {
                            binding.imgSunriseAlarm.isChecked = it.isOpen
                        } else if (it.id == PRAY_DHUHR_ID) {
                            binding.imgDhuhrAlarm.isChecked = it.isOpen
                        } else if (it.id == PRAY_ASR_ID) {
                            binding.imgAsrAlarm.isChecked = it.isOpen
                        } else if (it.id == PRAY_MAGHRIB_ID) {
                            binding.imgMaghribAlarm.isChecked = it.isOpen
                        } else if (it.id == PRAY_ISHA_ID) {
                            binding.imgIshaAlarm.isChecked = it.isOpen
                        }
                    }
                }
            }
        }
    }

    private fun updateLocation(location: Location) {
        requestPermissionSetting = false
        binding.layoutLocationFail.visibility = View.GONE
        val timeZone = TimeZone.getDefault()
        // 获取时区的ID，例如："America/New_York"
//        LogUtils.i(TAG, "时区ID：" + timeZone.id)

        val currentTimeMillis = System.currentTimeMillis()
        // 获取当前时区与UTC的偏移量（以毫秒为单位），然后转换为分钟
        timeZooOffsetInHours = timeZone.getOffset(currentTimeMillis) / (60 * 60 * 1000)
//        LogUtils.i(TAG, "时区码：" + timeZooOffsetInHours)

        val muslimLocation = WmMuslimLocation(
            location.longitude, location.latitude, timeZooOffsetInHours
        )

        val result = UNIWatchMate.setLocation(muslimLocation)
        LogUtils.i(TAG, "经纬度/时区设置结果：" + result)

        val year = mCalendar.get(Calendar.YEAR)
        val month = mCalendar.get(Calendar.MONTH) + 1
        val day = mCalendar.get(Calendar.DAY_OF_MONTH)

        val longitude = location.longitude
        val latitude = location.latitude

        mJNIGetTimeParam = MuslimCalcParam()

        mJNIGetTimeParam?.let {
            it.calcType = mDeviceTimeTypeParam.calculateType
            it.juristicMethod = mDeviceTimeTypeParam.juristicMethodType
            it.longitude = longitude
            it.latitude = latitude
            it.year = year
            it.month = month
            it.day = day
            it.timeZone = timeZooOffsetInHours

            getPrayTimes(it)
        }

    }

    private fun initPrayRemindData() {

        if (TextUtils.isEmpty(CacheDataHelper.getPrayReminderJson())) {
            //默认祈祷提醒
            val listPrayReminder = arrayListOf<MuslimPrayRemind>()
            listPrayReminder.add(MuslimPrayRemind(PRAY_FAJR_ID, false))
            listPrayReminder.add(MuslimPrayRemind(PRAY_SUNRISE_ID, false))
            listPrayReminder.add(MuslimPrayRemind(PRAY_DHUHR_ID, false))
            listPrayReminder.add(MuslimPrayRemind(PRAY_ASR_ID, false))
            listPrayReminder.add(MuslimPrayRemind(PRAY_MAGHRIB_ID, false))
            listPrayReminder.add(MuslimPrayRemind(PRAY_ISHA_ID, false))
            mPrayReminder = MuslimPrayReminder(1, 0, listPrayReminder)

            mPrayReminder?.let {
                binding.switchReminder.isChecked = it.switchState == 1

                it.prayReminds.forEach {
                    if (it.id == PRAY_FAJR_ID) {
                        binding.imgFajrAlarm.isChecked = it.isOpen
                    } else if (it.id == PRAY_SUNRISE_ID) {
                        binding.imgSunriseAlarm.isChecked = it.isOpen
                    } else if (it.id == PRAY_DHUHR_ID) {
                        binding.imgDhuhrAlarm.isChecked = it.isOpen
                    } else if (it.id == PRAY_ASR_ID) {
                        binding.imgAsrAlarm.isChecked = it.isOpen
                    } else if (it.id == PRAY_MAGHRIB_ID) {
                        binding.imgMaghribAlarm.isChecked = it.isOpen
                    } else if (it.id == PRAY_ISHA_ID) {
                        binding.imgIshaAlarm.isChecked = it.isOpen
                    }
                }
            }

            LogUtils.i(TAG, "默认提醒数据:" + GsonUtil.toJson(mPrayReminder))
        } else {
            mPrayReminder =
                GsonUtil.fromJson(
                    CacheDataHelper.getPrayReminderJson(),
                    MuslimPrayReminder::class.java
                )
        }

    }

    override fun onResume() {
        super.onResume()

//        binding?.tvDate?.setText(
//            DateUtil.formatDateFromTimeMillis(
//                System.currentTimeMillis(), DateUtil.FORMAT_MDY,
//                Locale.ENGLISH
//            )
//        )
//
//        if (requestPermissionSetting) {
//            checkLocation()
//        }

    }

    private fun setMuslimPrayReminder(view: View) {
        LogUtils.i(TAG, "设置祈祷提醒:" + GsonUtil.toJson(mPrayReminder!!))

        val list = ArrayList<PrayRemind>()
        mPrayReminder!!.prayReminds.forEach {
            list.add(PrayRemind(it.id, it.isOpen))
        }

        val wmPrayReminder = WmPrayReminder(1, mPrayReminder!!.switchState, list)

        UNIWatchMate.wmApps.appMuslim.setPrayReminder(wmPrayReminder).subscribe({ result ->
            LogUtils.i(TAG, "设置祈祷提醒成功:" + result)

            CacheDataHelper.savePrayReminderJson(GsonUtil.toJson(mPrayReminder))

            if (!result) {
                ToastUtil.showToast(getString(R.string.sync_failed))
                if (view is SwitchButton) {
                    view.isChecked = !view.isChecked
                } else if (view is AppCompatToggleButton) {
                    view.isChecked = !view.isChecked
                }
            }
            updatePrayReminder()
        }, {

        })
    }

    override fun onClick(v: View) {
//
//        if (!isBleOpen || !isDeviceConnected) {
//            setMuslimPrayReminder(v)
//            return
//        }

        when (v.id) {
            R.id.switch_reminder -> {
                binding.layoutPrayerTimings.visibility =
                    if (binding.switchReminder.isChecked) View.VISIBLE else View.GONE

                mPrayReminder!!.switchState = if (binding.switchReminder.isChecked) 1 else 0

                setMuslimPrayReminder(v)
            }

            R.id.iv_prev_day -> {

                mCalendar.add(Calendar.DAY_OF_MONTH, -1); // 减一天
                binding.tvDate.setText(
                    DateUtil.formatDateFromTimeMillis(
                        mCalendar.timeInMillis, DateUtil.FORMAT_MDY,
                        Locale.ENGLISH
                    )
                )

                mJNIGetTimeParam?.let {

                    it.year = mCalendar.get(Calendar.YEAR)
                    it.month = mCalendar.get(Calendar.MONTH) + 1
                    it.day = mCalendar.get(Calendar.DAY_OF_MONTH)

                    getPrayTimes(it)
                }
            }

            R.id.iv_next_day -> {

                mCalendar.add(Calendar.DAY_OF_MONTH, 1); // 加一天

                binding.tvDate.setText(
                    DateUtil.formatDateFromTimeMillis(
                        mCalendar.timeInMillis, DateUtil.FORMAT_MDY,
                        Locale.ENGLISH
                    )
                )

                mJNIGetTimeParam?.let {
                    it.year = mCalendar.get(Calendar.YEAR)
                    it.month = mCalendar.get(Calendar.MONTH) + 1
                    it.day = mCalendar.get(Calendar.DAY_OF_MONTH)

                    getPrayTimes(it)
                }
            }

            R.id.img_fajr_alarm -> {
                mPrayReminder!!.prayReminds.forEach {
                    if (it.id == PRAY_FAJR_ID) {
                        it.isOpen = !it.isOpen
                    }
                }

                setMuslimPrayReminder(v)
            }

            R.id.img_sunrise_alarm -> {
                mPrayReminder!!.prayReminds.forEach {
                    if (it.id == PRAY_SUNRISE_ID) {
                        it.isOpen = !it.isOpen
                    }
                }

                setMuslimPrayReminder(v)
            }

            R.id.img_dhuhr_alarm -> {
                mPrayReminder!!.prayReminds.forEach {
                    if (it.id == PRAY_DHUHR_ID) {
                        it.isOpen = !it.isOpen
                    }
                }

                setMuslimPrayReminder(v)
            }

            R.id.img_asr_alarm -> {
                mPrayReminder!!.prayReminds.forEach {
                    if (it.id == PRAY_ASR_ID) {
                        it.isOpen = !it.isOpen
                    }
                }

                setMuslimPrayReminder(v)
            }

            R.id.img_maghrib_alarm -> {
                mPrayReminder!!.prayReminds.forEach {
                    if (it.id == PRAY_MAGHRIB_ID) {
                        it.isOpen = !it.isOpen
                    }
                }

                setMuslimPrayReminder(v)
            }

            R.id.img_isha_alarm -> {
                mPrayReminder!!.prayReminds.forEach {
                    if (it.id == PRAY_ISHA_ID) {
                        it.isOpen = !it.isOpen
                    }
                }

                setMuslimPrayReminder(v)
            }

            R.id.ll_tasbih_reminder -> {
                val intent = Intent(this, TasbihReminderActivity::class.java)
                startActivity(intent)
            }

            R.id.ll_name_of_allah -> {
                val intent = Intent(this, NameOfAllahActivity::class.java)
                startActivity(intent)
            }

            R.id.ll_qibla_direction -> {
                val intent = Intent(this, MuslimCompassActivity::class.java)
                startActivity(intent)
            }

            R.id.ll_calc_method -> {
                showCalcMethodPickView(v as CommItemView)
            }

            R.id.ll_asr_method -> {
                showAsrPickView(v as CommItemView)
            }
        }
    }

    private fun showCalcMethodPickView(calcView: CommItemView) {

        var defaultValue: String =
            mTimeCalculateTypeNameList.get(mDeviceTimeTypeParam.calculateType)

        val customDialView =
            CommWheelViewDialog.getInstance(mTimeCalculateTypeNameList, defaultValue, true);

        customDialView.setOnItemSelectListener { value: String? ->
            LogUtils.i(TAG, "选择计算方式:$value")
            mTimeCalculateTypeUIS.forEachIndexed { index, muslimTimeCalculateTypeUi ->

                if (value == muslimTimeCalculateTypeUi.name) {

                    mDeviceTimeTypeParam.calculateType =
                        muslimTimeCalculateTypeUi.type

                    val wmMuslimCalculateType = WmMuslimCalculateType(
                        mDeviceTimeTypeParam.calculateType,
                        mDeviceTimeTypeParam.juristicMethodType
                    )

                    UNIWatchMate.wmApps.appMuslim.setTimeCalculateType(wmMuslimCalculateType)
                        .subscribe({ result ->
                            LogUtils.i(TAG, "时间选择结果:$result")
                            if (result) {
                                calcView.setData(value)
                                calcView.setUnit("")

                                mJNIGetTimeParam?.let {
                                    it.calcType = mDeviceTimeTypeParam.calculateType
                                    CacheDataHelper.savePrayTimeType(mDeviceTimeTypeParam.calculateType)
                                    getPrayTimes(it)
                                }
                            } else {
                                ToastUtil.showToast(getString(R.string.sync_failed))
                            }
                        }, {
                            LogUtils.i(TAG, "时间选择异常:${it.message}")
                        })

                    return@forEachIndexed
                }
            }
        }.show(supportFragmentManager)

        customDialView.setTvLeftTxt(getString(R.string.muslim_cancel))
        customDialView.setTvRightTxt(getString(R.string.muslim_save))
    }

    private fun showAsrPickView(calcView: CommItemView) {

        var defaultValue: String =
            mAsrTypeNameList.get(mDeviceTimeTypeParam.juristicMethodType)

        val customDialView = CommWheelViewDialog.getInstance(mAsrTypeNameList, defaultValue, true);
        customDialView.setOnItemSelectListener { value: String? ->
            LogUtils.i(TAG, "Asr 选择计算方式:$value")

            mAsrTimeCalculateTypeUIS.forEachIndexed { index, muslimTimeCalculateTypeUi ->
                if (value == muslimTimeCalculateTypeUi.name) {

                    mDeviceTimeTypeParam.juristicMethodType =
                        muslimTimeCalculateTypeUi.type

                    val wmMuslimCalculateType = WmMuslimCalculateType(
                        mDeviceTimeTypeParam.calculateType,
                        mDeviceTimeTypeParam.juristicMethodType
                    )

                    UNIWatchMate.wmApps.appMuslim.setTimeCalculateType(wmMuslimCalculateType)
                        .subscribe({ result ->
                            LogUtils.i(TAG, "Asr 时间选择结果:$result")
                            if (result) {
                                calcView.setData(value)
                                calcView.setUnit("")

                                CacheDataHelper.savePrayTimeAsrType(mDeviceTimeTypeParam.juristicMethodType)

                                mJNIGetTimeParam?.let {
                                    it.juristicMethod = mDeviceTimeTypeParam.juristicMethodType
                                    getPrayTimes(it)
                                }

                            } else {
                                ToastUtil.showToast(getString(R.string.sync_failed))
                            }
                        }, {
                            LogUtils.i(TAG, "Asr 时间选择异常:${it.message}")
                        })

                    return@forEachIndexed
                }
            }
        }.show(supportFragmentManager)

        customDialView.setTvLeftTxt(getString(R.string.muslim_cancel))
        customDialView.setTvRightTxt(getString(R.string.muslim_save))
    }

}