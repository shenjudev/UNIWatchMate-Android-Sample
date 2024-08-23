package com.sjbt.sdk.sample.utils;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;


import com.sjbt.sdk.sample.R;

import java.util.Calendar;
import java.util.Locale;

/**
 * author : sj
 * package_name : com.transsion.data.util
 * class_name : TimeUtil
 * description : 时间格式工具类
 * time : 2022-05-27 18:23
 */
public class TimeUtil {

    /**
     * 中文12小时制
     */
    public static final String TIME_FORMAT_12_CN = "a hh:mm";

    /**
     * 英文12小时制
     */
    public static final String TIME_FORMAT_12_EN = "hh:mm a";

    /**
     * 24小时制
     */
    public static final String TIME_FORMAT_24 = "HH:mm";

    /**
     * 是否为24小时制
     *
     * @param context
     * @return
     */
    public static boolean is24HourFormat(Context context) {
        return DateFormat.is24HourFormat(context);
    }

    /**
     * 根据系统时间制式转换小时
     *
     * @param hour 传入24小时制
     * @return
     */
    public static String formatTimeBySystem(@NonNull Context context,
                                            @IntRange(from = 0, to = 23) int hour,
                                            @IntRange(from = 0, to = 59) int min) {
        if (DateFormat.is24HourFormat(context)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
            return DateUtil.formatDateFromTimeMillis(calendar.getTimeInMillis(), TIME_FORMAT_24);
        }
        return format24to12(hour, min);
    }

    /**
     * 根据系统时间制式转换小时
     *
     * @return
     */
    public static String formatTimeBySystem(@NonNull Context context, long timestamp) {
        if (DateFormat.is24HourFormat(context)) {
            return DateUtil.formatDateFromTimeMillis(timestamp, TIME_FORMAT_24);
        }
        if (TextUtils.equals(Locale.getDefault().getLanguage(), Locale.CHINESE.getLanguage())) {
            return DateUtil.formatDateFromTimeMillis(timestamp, TIME_FORMAT_12_CN);
        } else {
            return DateUtil.formatDateFromTimeMillis(timestamp, TIME_FORMAT_12_EN);
        }
    }

    /**
     * 根据系统时间制式转换小时
     *
     * @param hour 传入24小时制
     * @return
     */
    public static String formatTimeBySystemForMuslim(@NonNull Context context,
                                                     @IntRange(from = 0, to = 23) int hour,
                                                     @IntRange(from = 0, to = 59) int min) {
        if (DateFormat.is24HourFormat(context)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
            return DateUtil.formatDateFromTimeMillis(calendar.getTimeInMillis(), TIME_FORMAT_24);
        }
        return format24to12ForMuslim(hour, min);
    }

    /**
     * 24小时制转12小时制
     *
     * @param hour
     * @return
     */
    public static String format24to12ForMuslim(@IntRange(from = 0, to = 23) int hour, @IntRange(from = 0, to = 59) int min) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, hour);
        instance.set(Calendar.MINUTE, min);
        return DateUtil.formatDateFromTimeMillis(instance.getTimeInMillis(), "hh:mm");
    }

    /**
     * 24小时制转12小时制
     *
     * @param hour
     * @return
     */
    public static String format24to12(@IntRange(from = 0, to = 23) int hour, @IntRange(from = 0, to = 59) int min) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, hour);
        instance.set(Calendar.MINUTE, min);
        if (TextUtils.equals(Locale.getDefault().getLanguage(), Locale.CHINESE.getLanguage())) {
            //中文
            return DateUtil.formatDateFromTimeMillis(instance.getTimeInMillis(), TIME_FORMAT_12_CN);
        }
        return DateUtil.formatDateFromTimeMillis(instance.getTimeInMillis(), TIME_FORMAT_12_EN);
    }

    /**
     * 12小时转24小时
     *
     * @param hour
     * @param isAm true表示上午
     * @return
     */
    public static String format12to24(@IntRange(from = 1, to = 12) int hour,
                                      @IntRange(from = 0, to = 59) int min,
                                      boolean isAm) {
        int hour24 = hour;
        if (isAm) {
            if (hour == 12) {
                hour24 = 0;
            }
        } else {
            hour24 = hour + 12;
        }
        hour24 = hour24 % 24;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour24);
        calendar.set(Calendar.MINUTE, min);
        return DateUtil.formatDateFromTimeMillis(calendar.getTimeInMillis(), TIME_FORMAT_24);
    }

    /**
     * 24小时转12小时
     *
     * @param hour
     * @return
     */
    public static String formatHour24to12(@NonNull Context context, @IntRange(from = 0, to = 23) int hour) {
        String am_pm = hour < 12 ? context.getString(R.string.muslim_time_am) : context.getString(R.string.muslim_time_pm);
        hour = hour % 12;
        if (hour == 0) {
            hour = 12;
        }
        if (TextUtils.equals(Locale.getDefault().getLanguage(), Locale.CHINESE.getLanguage())) {
            //中文
            return am_pm + " " + hour;
        }
        return hour + " " + am_pm;
    }

    public static String millisToHMS(Long millis) {
        Long seconds = (millis / 1000) % 60;
        Long minutes = (millis / (1000 * 60)) % 60;
        Long hours = millis / (1000 * 60 * 60);
        if (hours > 0) {
            return String.format(
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }
        return String.format(
                "%02d:%02d",
                minutes,
                seconds
        );
    }
}
