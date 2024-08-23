package com.sjbt.sdk.sample.ui.device.dial.diyDial.editVideo;

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    fun now(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    /**
     * 时间转换器
     */
    fun dateStr2timeStamp(pattern: String = format, dateStr: String): Long {
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date = simpleDateFormat.parse(dateStr)
        return date.time
    }

    fun dateStr2timeStamp(dateStr: String): Long {
        val simpleDateFormat = SimpleDateFormat(format)
        val date = simpleDateFormat.parse(dateStr)
        return date.time
    }

    val format: String = "yyyy-MM-dd HH:mm:ss"

    fun getUploadDataTime(): Long {
        return getTodayStartTime().plus(12 * 60 * 60 * 1000)
    }


    fun getCurrDataTime(): String {
        val df =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss") //设置日期格式精确到毫秒 SSS代表毫秒
        return df.format(Date())
    }

    fun formatMilliseconds(milliseconds: Long): String? {
        val hours = (milliseconds / (1000 * 60 * 60)).toInt() % 24
        val minutes = (milliseconds / (1000 * 60) % 60).toInt()
        val seconds = (milliseconds / 1000).toInt() % 60
        val millis = (milliseconds % 1000).toInt()

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }



    /**
     * 计算两个时间的时间差
     */
    fun getTimeDiff(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Long {
        val startTime = Date()
        startTime.hours = startHour
        startTime.minutes = startMinute

        val endTime = Date()
        endTime.hours = endHour
        endTime.minutes = endMinute

        val diffInMilliseconds = Math.abs(endTime.time - startTime.time)
        var diffInMinutes: Long = diffInMilliseconds / 1000 / 60;

        if (diffInMinutes > 10 * 60) {
            diffInMinutes = 24 * 60 - diffInMinutes
        }

        return diffInMinutes
    }

    fun getTodayStartTime(): Long {

        val todayStart = Calendar.getInstance()
        todayStart[Calendar.HOUR_OF_DAY] = 0
        //        todayEnd[Calendar.HOUR] = 23  //12小时制
        todayStart[Calendar.MINUTE] = 0
        todayStart[Calendar.SECOND] = 0
        todayStart[Calendar.MILLISECOND] = 1

        return todayStart.time.time
    }

    fun getTodayEndTime(): Long? {
        val todayEnd = Calendar.getInstance()
        todayEnd[Calendar.HOUR_OF_DAY] = 23
//        todayEnd[Calendar.HOUR] = 23  //12小时制
        todayEnd[Calendar.MINUTE] = 59
        todayEnd[Calendar.SECOND] = 59
        todayEnd[Calendar.MILLISECOND] = 999

        return todayEnd.time.time
    }

    /**
     * 获取从现在往前6个月的起始时间
     */
    fun getLastSixMonthStartTime(): Long? {

        val todayStart = Calendar.getInstance()
        todayStart[Calendar.MONTH] = todayStart[Calendar.MONTH] - 5

        todayStart[Calendar.DAY_OF_MONTH] = 1
        todayStart[Calendar.HOUR_OF_DAY] = 0
        //        todayEnd[Calendar.HOUR] = 23  //12小时制
        todayStart[Calendar.MINUTE] = 0
        todayStart[Calendar.SECOND] = 0
        todayStart[Calendar.MILLISECOND] = 0

        return todayStart.time.time
    }

    fun getYestDayStartTime(): Long {

        val todayStart = Calendar.getInstance()
        todayStart[Calendar.DAY_OF_YEAR] = todayStart[Calendar.DAY_OF_YEAR] - 1

        todayStart[Calendar.HOUR_OF_DAY] = 0
        //        todayEnd[Calendar.HOUR] = 23  //12小时制
        todayStart[Calendar.MINUTE] = 0
        todayStart[Calendar.SECOND] = 0
        todayStart[Calendar.MILLISECOND] = 0

        return todayStart.time.time
    }

    fun getYestDayEndTime(): Long? {
        val todayEnd = Calendar.getInstance()

        todayEnd[Calendar.DAY_OF_YEAR] = todayEnd[Calendar.DAY_OF_YEAR] - 1

        todayEnd[Calendar.HOUR_OF_DAY] = 23
//        todayEnd[Calendar.HOUR] = 23  //12小时制
        todayEnd[Calendar.MINUTE] = 59
        todayEnd[Calendar.SECOND] = 59
        todayEnd[Calendar.MILLISECOND] = 999

        return todayEnd.time.time
    }

    /**
     * 本周开始时间
     */
    fun getWeekStartTime(): Long? {
        val weekStart = Calendar.getInstance()
        weekStart.firstDayOfWeek = Calendar.MONDAY
        weekStart[Calendar.DAY_OF_WEEK] = weekStart.firstDayOfWeek
        weekStart[Calendar.HOUR_OF_DAY] = 0
        weekStart[Calendar.MINUTE] = 0
        weekStart[Calendar.SECOND] = 0
        weekStart[Calendar.MILLISECOND] = 0

        return weekStart.time.time
    }

    /**
     * 本周结束时间
     */
    fun getWeekEndTime(): Long? {
        val weekEnd = Calendar.getInstance()
        weekEnd.firstDayOfWeek = Calendar.MONDAY
        weekEnd[Calendar.DAY_OF_WEEK] = weekEnd.firstDayOfWeek + 6
        weekEnd[Calendar.HOUR_OF_DAY] = 23
        weekEnd[Calendar.MINUTE] = 59
        weekEnd[Calendar.SECOND] = 59
        weekEnd[Calendar.MILLISECOND] = 999

        return weekEnd.time.time
    }


    //获取目标年份中目标月的第一天的开始时间
    fun getFirstDayOfMonth(): Long? {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 0);
        cal[Calendar.DAY_OF_MONTH] = 1
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        //获取目标月和目标年份的当月第一天时间
        return cal.time.time
    }

    //获取目标年份中目标月的最后一天
    fun getLastDayOfMonth(): Long? {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 23
        cal[Calendar.MINUTE] = 59
        cal[Calendar.SECOND] = 59
        //获取目标月和目标年份的当月第一天时间
        val last = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal[Calendar.DAY_OF_MONTH] = last
        return cal.time.time
    }

    //获取目标年份中目标月的第一天的开始时间
    fun getFirstDayOfYear(): Long? {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 0);
        cal[Calendar.DAY_OF_YEAR] = 1
        cal[Calendar.MONTH] = 0
        cal[Calendar.DAY_OF_MONTH] = 1
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        //获取目标月和目标年份的当月第一天时间
        return cal.time.time
    }

    //获取目标年份中目标月的最后一天
    fun getLastDayOfHalfYear(): Long? {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 23
        cal[Calendar.MINUTE] = 59
        cal[Calendar.SECOND] = 59
        //获取目标月和目标年份的当月第一天时间
        val last = cal.getActualMaximum(Calendar.DAY_OF_YEAR) / 2 - 1
        cal[Calendar.DAY_OF_YEAR] = last
        return cal.time.time
    }

    //获取目标年份中目标月的最后一天
    fun getLastDayOfYear(): Long? {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 23
        cal[Calendar.MINUTE] = 59
        cal[Calendar.SECOND] = 59
        //获取目标月和目标年份的当月第一天时间
        val last = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
        cal[Calendar.DAY_OF_YEAR] = last
        return cal.time.time
    }

    /**
     * 获取过去7天内的日期数组
     * @param intervals      intervals天内
     * @return              日期数组
     */
    fun getDays(intervals: Int): LinkedList<String>? {
        val pastDaysList: LinkedList<String> = LinkedList()
        for (i in intervals - 1 downTo 0) {
            pastDaysList.add(getPastDate(i))
        }

        return pastDaysList
    }

    /**
     * 获取过去第几天的日期
     * @param past
     * @return
     */
    private fun getPastDate(past: Int): String {
        val calendar = Calendar.getInstance()
        calendar[Calendar.DAY_OF_YEAR] = calendar[Calendar.DAY_OF_YEAR] - past
        val today = calendar.time
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(today)
    }
}