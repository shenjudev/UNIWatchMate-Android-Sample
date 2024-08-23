package com.sjbt.sdk.sample.ui.muslim

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.RepeatRule
import com.base.sdk.entity.apps.RosaryFrequency
import com.base.sdk.entity.apps.WmRosaryReminder
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.data.device.SJDataConvertTools
import com.sjbt.sdk.sample.databinding.ActivityTasbihReminderBinding
import com.sjbt.sdk.sample.model.muslim.MuslimRepeatRule
import com.sjbt.sdk.sample.model.muslim.MuslimRepeatRule.FRI
import com.sjbt.sdk.sample.model.muslim.MuslimRepeatRule.MON
import com.sjbt.sdk.sample.model.muslim.MuslimRepeatRule.SAT
import com.sjbt.sdk.sample.model.muslim.MuslimRepeatRule.SUN
import com.sjbt.sdk.sample.model.muslim.MuslimRepeatRule.THU
import com.sjbt.sdk.sample.model.muslim.MuslimRepeatRule.TUE
import com.sjbt.sdk.sample.model.muslim.MuslimRepeatRule.WED
import com.sjbt.sdk.sample.model.muslim.MuslimTasbihReminder
import com.sjbt.sdk.sample.model.muslim.MuslimTasbihReminder.INTERVAL_120_MINS
import com.sjbt.sdk.sample.model.muslim.MuslimTasbihReminder.INTERVAL_180_MINS
import com.sjbt.sdk.sample.model.muslim.MuslimTasbihReminder.INTERVAL_30_MINS
import com.sjbt.sdk.sample.model.muslim.MuslimTasbihReminder.INTERVAL_60_MINS
import com.sjbt.sdk.sample.ui.dialog.CommWheelViewDialog
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.DateTimeUtil
import com.sjbt.sdk.sample.utils.TimeUtil
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.widget.CommItemView
import com.sjbt.sdk.sample.widget.TimeSelectionDialogMuslim
import com.sjbt.sdk.utils.log.GsonUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import java.util.Calendar
import java.util.Locale

class TasbihReminderActivity : BaseActivity() {
    private var muslimTasbihReminder: MuslimTasbihReminder? = null
    private lateinit var repeatAdapter: RepeatAdapter

    private val TAG = "TasbihReminderActivity"

    private val defaultStartHour = 8
    private val defaultEndHour = 20

    private val INTERVAL_TIME_30_MINUTES = 30
    private val INTERVAL_TIME_1_H = 1
    private val INTERVAL_TIME_2_H = 2
    private val INTERVAL_TIME_3_H = 3

    private lateinit var binding: ActivityTasbihReminderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasbihReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val tasbihReminderJson = CacheDataHelper.getTasbihJson()
        if (!TextUtils.isEmpty(tasbihReminderJson)) {
            muslimTasbihReminder =
                GsonUtil.fromJson(tasbihReminderJson, MuslimTasbihReminder::class.java)
        }

        if (muslimTasbihReminder == null) {

            val muslimRosaryRepeatRule = arrayListOf<MuslimRepeatRule>()
            muslimRosaryRepeatRule.add(MuslimRepeatRule(SUN, false))
            muslimRosaryRepeatRule.add(MuslimRepeatRule(MON, false))
            muslimRosaryRepeatRule.add(MuslimRepeatRule(TUE, false))
            muslimRosaryRepeatRule.add(MuslimRepeatRule(WED, false))
            muslimRosaryRepeatRule.add(MuslimRepeatRule(THU, false))
            muslimRosaryRepeatRule.add(MuslimRepeatRule(FRI, false))
            muslimRosaryRepeatRule.add(MuslimRepeatRule(SAT, false))

            muslimTasbihReminder =
                MuslimTasbihReminder(
                    1,
                    1,
                    defaultStartHour,
                    0,
                    defaultEndHour,
                    0,
                    INTERVAL_30_MINS,
                    muslimRosaryRepeatRule
                )
        }

        updateDataOnView()

        binding.reminderStartTime.setOnClickListener { v: View ->
            // 选择时间
            showPickerView(true, v as CommItemView)
        }
        binding.reminderEndTime.setOnClickListener { v: View ->
            // 选择时间
            showPickerView(false, v as CommItemView)
        }

        binding.reminderIntervalTime.setOnClickListener { v: View ->
            // 选择时间
            showIntervalPickView(v as CommItemView)
        }

        binding.switchReminder.setOnClickListener {
//            binding.switchReminder.isChecked = !binding.switchReminder.isChecked

            muslimTasbihReminder!!.switchState = if (binding.switchReminder.isChecked) {
                1
            } else {
                0
            }

        }

        binding.btnSave.setOnClickListener {

//            if (!isBleOpen || !isDeviceConnected) {
//                return@setOnClickListener
//            }

            muslimTasbihReminder?.let {

                LogUtils.i(TAG, "设置赞颂信息:" + GsonUtil.toJson(it))

                val frequency = when (it.interval) {
                    INTERVAL_30_MINS -> RosaryFrequency.Frequency30
                    INTERVAL_60_MINS -> RosaryFrequency.Frequency60
                    INTERVAL_120_MINS -> RosaryFrequency.Frequency120
                    INTERVAL_180_MINS -> RosaryFrequency.Frequency180
                    else -> RosaryFrequency.Frequency30
                }

                val repeatRules = arrayListOf<RepeatRule>()

                val wmRosaryReminder = WmRosaryReminder(
                    it.version,
                    it.switchState,
                    it.startHour,
                    it.startMinute,
                    it.endHour,
                    it.endMinute,
                    frequency,
                    repeatRules
                )

                UNIWatchMate.wmApps.appMuslim.setRosaryReminder(wmRosaryReminder)
                    .subscribe({ result ->
                        CacheDataHelper.saveTasbihJson(GsonUtil.toJson(muslimTasbihReminder))
                        LogUtils.i(TAG, "设置赞颂提醒结果：$result")
                        ToastUtil.showToast(getString(R.string.setup_success))
                        finish()
                    }, {
                        LogUtils.i(TAG, "设置赞颂提醒异常")
                    })
            }
        }

        MyApplication.instance.applicationScope.launch {

            launchWithLog {
                UNIWatchMate.wmApps.appMuslim.observeRosaryReminder.observeOn(AndroidSchedulers.mainThread())
                    .asFlow().collect {

                    LogUtils.i(TAG, "监听赞颂提醒变化:" + GsonUtil.toJson(it))

                    muslimTasbihReminder = SJDataConvertTools.instance.convertTasbihReminder(
                        it
                    )

                    CacheDataHelper.saveTasbihJson(
                        GsonUtil.toJson(
                            muslimTasbihReminder
                        )
                    )

                    updateDataOnView()

                }
            }
        }
    }

//    override fun needEventBus(): Boolean {
//        return true
//    }
//
//    override fun distributeEvent(event: BaseEvent<*>) {
//        super.distributeEvent(event)
//        when (event.type) {
//            MUSLIM_TASBIH_REMINDER -> {
//                val tasbihReminderJson = DeviceCache.getTasbihJson()
//                if (!TextUtils.isEmpty(tasbihReminderJson)) {
//                    muslimTasbihReminder =
//                        GsonUtil.fromJson(tasbihReminderJson, MuslimTasbihReminder::class.java)
//                    updateDataOnView()
//                }
//            }
//        }
//    }

    private fun showPickerView(isStart: Boolean, timeView: CommItemView) {

        val startDialog = TimeSelectionDialogMuslim.getInstance(
            if (isStart) {
                muslimTasbihReminder!!.startHour
            } else {
                muslimTasbihReminder!!.endHour
            }, if (isStart) {
                muslimTasbihReminder!!.startMinute
            } else {
                muslimTasbihReminder!!.endMinute
            }, DateTimeUtil.is24(this), false
        )

        startDialog.setOnItemSelectedListener { hour, minuter ->
            if (isStart) {
                if (!checkLegal(
                        hour,
                        minuter,
                        muslimTasbihReminder!!.endHour,
                        muslimTasbihReminder!!.endMinute,
                        muslimTasbihReminder!!.interval,
                    )
                ) return@setOnItemSelectedListener
            } else {
                if (!checkLegal(
                        muslimTasbihReminder!!.startHour,
                        muslimTasbihReminder!!.startMinute,
                        hour,
                        minuter,
                        muslimTasbihReminder!!.interval,
                    )
                ) return@setOnItemSelectedListener
            }

            if (isStart) {
                muslimTasbihReminder!!.startHour = hour
                muslimTasbihReminder!!.startMinute = minuter
            } else {
                muslimTasbihReminder!!.endHour = hour
                muslimTasbihReminder!!.endMinute = minuter
            }

            showTimeView(isStart, timeView)
//            }
        }

        startDialog.show(supportFragmentManager)
    }

    private fun checkLegal(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        interval: Int
    ): Boolean {

        val startTime = startHour * 60 * 60 + startMinute * 60
        val endTime = endHour * 60 * 60 + endMinute * 60

        if (startTime >= endTime) {
            ToastUtil.showToast(
                getString(R.string.noon_disturb_time_not_support_over_day2),
            )
            return false
        }
        return true
    }

    private fun showTimeView(start: Boolean, timeView: CommItemView) {
        val timeStr = TimeUtil.formatTimeBySystem(
            this, if (start) {
                muslimTasbihReminder!!.startHour
            } else {
                muslimTasbihReminder!!.endHour
            }, if (start) {
                muslimTasbihReminder!!.startMinute
            } else {
                muslimTasbihReminder!!.endMinute
            }
        )

        if (timeStr.contains(" ")) {
            try {
                timeView.setData(timeStr.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0])
                timeView.setUnit(timeStr.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1])
                if (TextUtils.equals(Locale.getDefault().language, Locale.CHINESE.language)) {
                    timeView.setDataColor(
                        ContextCompat.getColor(
                            this, R.color.primary_gray_3
                        )
                    )
                    timeView.setUnitColor(ContextCompat.getColor(this, R.color.black))
                }
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        LogUtils.i(TAG, "选中的时间" + timeStr)

        timeView.setData(timeStr)
        timeView.setUnit("")
    }

    private fun showIntervalPickView(intervalView: CommItemView) {
        val data = java.util.ArrayList<String>()

        val time_30 = INTERVAL_TIME_30_MINUTES.toString() + getString(R.string.unit_min)
        val time_1h = INTERVAL_TIME_1_H.toString() + getString(R.string.unit_hour)
        val time_2h =
            INTERVAL_TIME_2_H.toString() + getString(R.string.unit_hour)
        val time_3h = INTERVAL_TIME_3_H.toString() + getString(R.string.unit_hour)

        data.add(time_30)
        data.add(time_1h)
        data.add(time_2h)
        data.add(time_3h)
        val defaultValue: String = binding.reminderIntervalTime.data

        CommWheelViewDialog.getInstance(data, defaultValue, true)
            .setOnItemSelectListener { value: String? ->
                intervalView.setData(value)

                when (value) {
                    time_30 -> {
                        muslimTasbihReminder!!.interval = INTERVAL_TIME_30_MINUTES
                    }

                    time_1h -> {
                        muslimTasbihReminder!!.interval = 60
                    }

                    time_2h -> {
                        muslimTasbihReminder!!.interval = INTERVAL_TIME_2_H * 60
                    }

                    time_3h -> {
                        muslimTasbihReminder!!.interval = INTERVAL_TIME_3_H * 60
                    }
                }

                intervalView.setUnit("")

            }.show(supportFragmentManager)
    }

    private fun updateDataOnView() {

        runOnUiThread {

            //默认星期开始日为周一
            val firstDayOfWeekP: Int = Calendar.SUNDAY

            val weekDays = DateTimeUtil.getWeekDay(firstDayOfWeekP)

            for (i in weekDays.indices) {
                val weekDay = weekDays[i]

                convertRepeatToWeekDays(weekDay)

            }
            repeatAdapter =
                RepeatAdapter(this, weekDays)
            binding.reminderRepeatWeek.layoutManager = GridLayoutManager(this, 7)
            binding.reminderRepeatWeek.adapter = repeatAdapter
            binding.switchReminder.isChecked = muslimTasbihReminder!!.switchState == 1


            val startTimeStr = TimeUtil.formatTimeBySystem(
                this,
                muslimTasbihReminder!!.startHour,
                muslimTasbihReminder!!.startMinute
            )

            val endTimeStr = TimeUtil.formatTimeBySystem(
                this,
                muslimTasbihReminder!!.endHour,
                muslimTasbihReminder!!.endMinute

            )

            binding.reminderStartTime.setData(startTimeStr)
            binding.reminderEndTime.setData(endTimeStr)

            var timeStr = when (muslimTasbihReminder!!.interval) {
                INTERVAL_30_MINS -> {
                    INTERVAL_TIME_30_MINUTES.toString() + getString(R.string.unit_min)
                }

                INTERVAL_60_MINS -> {
                    INTERVAL_TIME_1_H.toString() + getString(R.string.unit_hour)
                }

                INTERVAL_120_MINS -> {
                    INTERVAL_TIME_2_H.toString() + getString(R.string.unit_hour)
                }

                INTERVAL_180_MINS -> {
                    INTERVAL_TIME_3_H.toString() + getString(R.string.unit_hour)
                }

                else -> {
                    INTERVAL_TIME_30_MINUTES.toString() + getString(R.string.unit_min)
                }

            }

            binding.reminderIntervalTime.setData(timeStr)

            repeatAdapter.setOnItemClickListener { adapter, view, position ->
                val weekDay = repeatAdapter.getItem(position)
                weekDay!!.isSelected = !weekDay.isSelected
                repeatAdapter.notifyItemChanged(position)
                LogUtils.i(TAG, "设置重复周期:" + GsonUtil.toJson(muslimTasbihReminder))
                convertWeekDaysToRepeat(weekDay)
            }

        }

    }

    private fun convertRepeatToWeekDays(weekDay: DateTimeUtil.WeekDay) {
        when (weekDay.index) {
            Calendar.SUNDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == SUN) {
                        weekDay.isSelected = repeatRule.isRepeat
                    }
                }
            }

            Calendar.MONDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == MON) {
                        weekDay.isSelected = repeatRule.isRepeat
                    }
                }
            }

            Calendar.TUESDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == TUE) {
                        weekDay.isSelected = repeatRule.isRepeat
                    }
                }
            }

            Calendar.WEDNESDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == WED) {
                        weekDay.isSelected = repeatRule.isRepeat
                    }
                }
            }

            Calendar.THURSDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == THU) {
                        weekDay.isSelected = repeatRule.isRepeat
                    }
                }
            }

            Calendar.FRIDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == FRI) {
                        weekDay.isSelected = repeatRule.isRepeat
                    }
                }

            }

            Calendar.SATURDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->

                    if (repeatRule.weekId == SAT) {
                        weekDay.isSelected = repeatRule.isRepeat
                    }

                }
            }
        }
    }

    private fun convertWeekDaysToRepeat(weekDay: DateTimeUtil.WeekDay) {
        when (weekDay.index) {
            Calendar.SUNDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == SUN) {
                        repeatRule.isRepeat = weekDay.isSelected
                    }
                }
            }

            Calendar.MONDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == MON) {
                        repeatRule.isRepeat = weekDay.isSelected
                    }
                }
            }

            Calendar.TUESDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == TUE) {
                        repeatRule.isRepeat = weekDay.isSelected
                    }
                }
            }

            Calendar.WEDNESDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == WED) {
                        repeatRule.isRepeat = weekDay.isSelected
                    }
                }
            }

            Calendar.THURSDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == THU) {
                        repeatRule.isRepeat = weekDay.isSelected
                    }
                }
            }

            Calendar.FRIDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->
                    if (repeatRule.weekId == FRI) {
                        repeatRule.isRepeat = weekDay.isSelected
                    }
                }

            }

            Calendar.SATURDAY -> {
                muslimTasbihReminder!!.repeatRules.forEach { repeatRule ->

                    if (repeatRule.weekId == SAT) {
                        repeatRule.isRepeat = weekDay.isSelected
                    }

                }
            }
        }
    }

}