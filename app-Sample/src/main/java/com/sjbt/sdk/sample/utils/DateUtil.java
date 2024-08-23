package com.sjbt.sdk.sample.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * author : sj
 * package_name : com.sparkpro.business.utils
 * class_name : DateUtil
 * description : 日期工具类
 * time : 2021-10-26 17:46
 */
public class DateUtil {

    /**
     * 日期格式：yyyy/MM/dd
     */
    public static final String FORMAT_YMD = "yyyy/MM/dd";

    /**
     * 日期格式 yyyy.MM.dd
     */
    public static final String FORMAT_Y_M_D = "yyyy.MM.dd";

    /**
     * 日期格式：yyyy-MM-dd
     */
    public static final String FORMAT_DATA_YMD = "yyyy-MM-dd";

    /**
     * 日期格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String FORMAT_DATA_YMD_HMS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式：MM/dd
     */
    public static final String FORMAT_MD = "MM/dd";

    /**
     * 日期格式：MM/dd HH:mm
     */
    public static final String FORMAT_MD_HM = "MM/dd HH:mm";

    /**
     * 日期格式：星期，月 日
     */
    public static final String FORMAT_EMD = "E, MMM d";

    /**
     *  zh日期格式：星期，月 日
     */
    public static final String FORMAT_ZH_EMD = "MMMd日 E";

    /**
     * 日期格式：月.日
     */
    public static final String FORMAT_MMD = "MMM d";

    /**
     * 日期格式：星期,月 日 年
     */
    public static final String FORMAT_EMDY = "EEEE, MMM dd, yyyy";

    /**
     * 日期格式：月 日, 年
     */
    public static final String FORMAT_MDY = "MMM dd, yyyy";

    /**
     * 日期格式：年/月
     */
    public static final String FORMAT_YM = "yyyy/MM";
    /**
     * 日期格式：年
     */
    public static final String FORMAT_Y = "yyyy";

    /**
     * 生日格式
     */
    public static final String FORMAT_BIRTH = "MMM dd, yyyy";

    /**
     * 时间格式
     */
    public static final String FORMAT_HM = "HH:mm";

    /**
     * 获取当前星期几
     *
     * @return
     */
    public static int getCurrentWeekday() {
        return getWeekdayFromTimeMillis(System.currentTimeMillis());
    }

    /**
     * 根据时间毫秒值获取星期几
     * Calendar中默认周日为一周的第一天
     *
     * @param timeMillis
     * @return
     */
    public static int getWeekdayFromTimeMillis(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        int index = calendar.get(Calendar.DAY_OF_WEEK);
        return (index - 1) == 0 ? 7 : (index - 1);
    }

    /**
     * 获取星期几
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static int getWeekday(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        int index = calendar.get(Calendar.DAY_OF_WEEK);
        return (index - 1) == 0 ? 7 : (index - 1);
    }

    /**
     * 获取当前日期
     *
     * @param format 格式
     * @return
     */
    public static String getCurrentDate(String format) {
        return formatDateFromTimeMillis(System.currentTimeMillis(), format);
    }

    /**
     * 获取当前日期
     *
     * @param format 格式
     * @return
     */
    public static String getCurrentDate(String format, Locale locale) {
        return formatDateFromTimeMillis(System.currentTimeMillis(), format, locale);
    }

    /**
     * 获取当前日（忽略时、分、秒）
     *
     * @return
     */
    public static Calendar getCurrentDataIgnoreTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 获取当前日期
     *
     * @return
     */
    public static Calendar getCurrentData() {
        return Calendar.getInstance();
    }

    /**
     * 根据时间毫秒值转换成指定格式
     *
     * @param timeMillis
     * @param format
     * @return
     */
    public static String formatDateFromTimeMillis(long timeMillis, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    /**
     * 根据时间毫秒值转换成指定格式
     *
     * @param timeMillis
     * @param format
     * @param locale
     * @return
     */
    public static String formatDateFromTimeMillis(long timeMillis, String format, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        return sdf.format(new Date(timeMillis));
    }

    /**
     * 获取Data对象
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static Calendar getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 获取月的天数
     *
     * @param year
     * @param month 对应的是Calendar.MONTH，
     *              该数据从0开始，比自然月小1，所以，参数应该是月份-1
     * @return
     */
    public static int getMonthDayCount(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month + 1, 1);
        calendar.add(Calendar.DATE, -1);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取月的天数
     *
     * @param date
     * @return
     */
    public static int getMonthDayCount(Calendar date) {
        return getMonthDayCount(date.get(Calendar.YEAR), date.get(Calendar.MONTH));
    }

    /**
     * 根据毫秒值获取年月日
     *
     * @param timeMillis
     * @return
     */
    public static int[] getYearMonthDay(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        return new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE)};
    }

    /**
     * 是否是相同月份
     *
     * @param calendar1
     * @param calendar2
     * @return
     */
    public static boolean isSameMonth(Calendar calendar1, Calendar calendar2) {
        if (calendar1 == null || calendar2 == null) {
            return false;
        }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }

    /**
     * 判断是否为当前时间之前的月份
     *
     * @param calendar
     * @return
     */
    public static boolean isPriorMonth(Calendar calendar) {
        if (calendar == null) {
            return true;
        }
        Calendar current = Calendar.getInstance();
        return (calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH)) < (current.get(Calendar.YEAR) * 12 + current.get(Calendar.MONTH));
    }

    /**
     * 判断是否为当前时间之前的日期（精确到多少号，忽略时、分、秒）
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static boolean isPriorDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        return calendar.before(getCurrentData());
    }

    /**
     * 判断是否为当前时间之前的日期
     *
     * @param timestamp
     * @return
     */
    public static boolean isPriorDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.before(getCurrentData());
    }

    /**
     * 获取指定月数前的日期
     *
     * @param calendar
     * @param before
     * @return
     */
    public static Calendar getDateBeforeMonth(Calendar calendar, int before) {
        Calendar cal = (Calendar) calendar.clone();
        cal.add(Calendar.MONTH, -before);
        return cal;
    }

    /**
     * 获取指定天数前的日期
     *
     * @param calendar
     * @param before
     * @return
     */
    public static Calendar getDateBeforeDay(Calendar calendar, int before) {
        Calendar cal = (Calendar) calendar.clone();
        cal.add(Calendar.DATE, -before);
        return cal;
    }

    /**
     * 获取指定日期所在周的周一的日期
     *
     * @param date
     * @return
     */
    public static Calendar getMondayOfWeek(@NonNull Calendar date) {
        Calendar calendar = (Calendar) date.clone();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekday == 0) {
            weekday = 7;
        }
        calendar.add(Calendar.DATE, 1 - weekday);
        return calendar;
    }

    /**
     * 获取日期所在周的周日
     *
     * @param date
     * @return
     */
    public static Calendar getSundayOfWeek(@NonNull Calendar date) {
        Calendar calendar = (Calendar) date.clone();
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DATE, 1 - day_of_week);
        return calendar;
    }

    /**
     * 获取日期所在周的周六
     *
     * @param date
     * @return
     */
    public static Calendar getSaturdayOfWeek(@NonNull Calendar date) {
        Calendar calendar = (Calendar) date.clone();
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
        if (day_of_week == 7) {
            day_of_week = 0;
        }
        calendar.add(Calendar.DATE, -day_of_week);
        return calendar;
    }

    /**
     * 获取xx月1号
     *
     * @param date
     * @return
     */
    public static Calendar getFirstDayOfMonth(@NonNull Calendar date) {
        Calendar calendar = (Calendar) date.clone();
        calendar.set(Calendar.DATE, 1);
        return calendar;
    }

    /**
     * 将日期字符串转年、月、日数组
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static int[] parseDateStr2YearMonthDay(String dateStr, String format) {
        if (TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(format)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr);
            if (date == null) {
                return null;
            } else {
                return getYearMonthDay(date.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析日期
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static Calendar parseDate(String dateStr, String format) {
        if (TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(format)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr);
            if (date == null) {
                return null;
            } else {
                return getDate(date.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取日期字符串
     *
     * @param year
     * @param month
     * @param day
     * @param format
     * @return
     */
    public static String getDateStr(int year, int month, int day, String format) {
        return formatDateFromTimeMillis(getDate(year, month, day).getTimeInMillis(), format);
    }

    /**
     * 获取日期字符串
     *
     * @param year
     * @param month
     * @param day
     * @param format
     * @return
     */
    public static String getDateStr(int year, int month, int day, String format, Locale locale) {
        return formatDateFromTimeMillis(getDate(year, month, day).getTimeInMillis(), format, locale);
    }

    /**
     * 获取日期
     *
     * @param timeStamp
     * @return
     */
    public static Calendar getDate(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        return calendar;
    }

    /**
     * 获取指定日期间所有日期的集合，包含开始、结束日期
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ParseException
     */
    public static List<String> getDateList(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATA_YMD, Locale.CHINA);
        //保存日期的集合 
        List<String> list = new ArrayList<String>();
        try {
            Date date_end = sdf.parse(endDate);
            Date date_start = sdf.parse(startDate);
            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (date_start.getTime() <= date_end.getTime()) {
                list.add(sdf.format(date_start));
                calendar.setTime(date_start);
                //增加一天 放入集合
                calendar.add(Calendar.DATE, 1);
                date_start = calendar.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 忽略时分秒
     *
     * @param calendar
     * @return
     */
    public static Calendar ignoreTime(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 判断是否为同一天
     *
     * @param c1
     * @param c2
     * @return
     */
    public static boolean isSameDay(Calendar c1, Calendar c2) {
        if (c1 == null || c2 == null) {
            return false;
        }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 获取毫秒值
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minter
     * @param second
     * @return
     */
    public static long getTimeMillis(int year, int month, int day, int hour, int minter, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minter, second);
        return calendar.getTimeInMillis();
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

    /**
     * 获取当前时间偏移UTC时间的分钟数
     *
     * @return
     */
    public static int getCurrentTimeZone() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeZone().getRawOffset() / (60 * 1000);
    }

    /**
     * 毫秒转HH:mm:ss格式字符串
     *
     * @param millis The milliseconds.
     */
    public static String millis2String(long millis) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
        Date curDate = new Date(millis);
        String hh = String.valueOf((int) millis / 60 / 60 / 1000);
        if (hh.length() < 2) {
            hh = "0" + hh;
        }
        return hh + ":" + formatter.format(curDate);
    }

    /**
     * 通过时分获取毫秒数
     *
     * @param hour
     * @param minute
     * @return
     */
    public static long getMillisecondByHour(int hour, int minute) {
        return hour * 60 * 60 + minute * 60;
    }

    /**
     * 判断给定的日期是否是今天
     *
     * @param year  年份
     * @param month 月份（1-12）
     * @param day   日期
     * @return 如果是昨天返回true，否则返回false
     */
    public static boolean isToday(int year, int month, int day) {
        // 获取当前时间的Calendar实例
        Calendar now = Calendar.getInstance();
        // 获取昨天的时间，设置年份、月份和日期
        Calendar date = (Calendar) now.clone();
        // 设置昨天的年份、月份和日期
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month - 1);//外部传进来的时+1的
        date.set(Calendar.DAY_OF_MONTH, day);

        // 检查是否是今天
        return now.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == date.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 判断给定的日期是否是昨天
     *
     * @param year  年份
     * @param month 月份（1-12）
     * @param day   日期
     * @return 如果是昨天返回true，否则返回false
     */
    public static boolean isYesterday(int year, int month, int day) {
        // 获取当前时间的Calendar实例
        Calendar now = Calendar.getInstance();
        // 获取昨天的时间，设置年份、月份和日期
        Calendar yesterday = (Calendar) now.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        // 设置昨天的年份、月份和日期
        yesterday.set(Calendar.YEAR, year);
        yesterday.set(Calendar.MONTH, month - 1);//外部传进来的时+1的
        yesterday.set(Calendar.DAY_OF_MONTH, day);

        // 检查是否是昨天
        return now.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) + 1;
    }

    /**
     * 获取明天的年月日
     *
     * @return 如果是昨天返回true，否则返回false
     */
    public static Calendar getTomorrow() {
        // 获取当前时间的Calendar实例
        Calendar tomorrow = Calendar.getInstance();
        // 获取昨天的时间，设置年份、月份和日期
        tomorrow.add(Calendar.DAY_OF_MONTH, +1);
        // 检查是否是昨天
        return tomorrow;
    }
}
