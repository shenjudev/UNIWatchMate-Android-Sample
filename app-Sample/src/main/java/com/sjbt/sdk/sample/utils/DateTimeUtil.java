package com.sjbt.sdk.sample.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.LogUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * FileName: DateTimeUtil
 * Description: 时间控制类
 * Date: 2020/3/25 13:35
 * History:
 * <author> <time> <version> <desc>
 */
public class DateTimeUtil {

    /**
     * 一天的秒
     */
    public static final long DAY_IN_SECOND = 60 * 60 * 24;

    /**
     * 一天毫秒数
     */
    public static final long DAY_IN_MILLISECOND = 1000 * DAY_IN_SECOND;

    /**
     * 30天的毫秒数
     */
    public static final long MONTH_IN_MILLISECOND = DAY_IN_MILLISECOND * 30;


    public final static String DATEFORMAT_COMM = "yyyy-MM-dd HH:mm:ss";

    public final static String DATEFORMAT_MONTH = "yyyy-MM";

    public final static String DATEFORMAT_DAY = "yyyy-MM-dd";

    public final static String DATEFORMAT_MONTH_DAY = "MM/dd";

    final static String DATEFORMAT_HOUR_MIN_S = "HH:mm:ss";

    final static String DATEFORMAT_COM_HOUR = "yyyy-MM-dd HH";

    public final static String DATEFORMAT_HOUR_MIN = "HH:mm";

    final static String DATEFORMAT_DAY_INT = "yyyyMMdd";

    public final static String DATEFORMAT_COM_YYMMDD_HHMM = "yyyy-MM-dd HH:mm";

    /**
     * 将年月日时分秒格式转换成 年月日时分秒格式
     *
     * @param dateTime
     * @return
     */
    public static String strToTimeStampComm(String dateTime) {
        if (TextUtils.isEmpty(dateTime)) {
            return "";
        }

        //必须使用这个格式化
        SimpleDateFormat format = new SimpleDateFormat(DATEFORMAT_COMM);
        Date date;
        try {
            date = format.parse(dateTime);

            if (date == null) {
                return "";
            }
            return String.valueOf(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将年月日时数值格式转换成 年月日小时的时间格式
     *
     * @return
     */
    public static String intToTimeStampWithHour(int year, int month, int day, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hours, 0);
        Date time = cal.getTime();
        return date2Str(time, DATEFORMAT_COM_HOUR);
    }

    /**
     * 将年月日时分数值格式转换成 时间戳字符串的形式
     *
     * @return
     */
    public static String intToTimeStamp(int year, int month, int day, int hours, int min) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hours, min, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date time = cal.getTime();
        return String.valueOf(time.getTime());
    }

    /**
     * 将年月日时数值格式转换成 时间戳字符串的形式
     *
     * @return
     */
    public static String intToTimeStamp(int year, int month, int day, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hours, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date time = cal.getTime();
        return String.valueOf(time.getTime());
    }

    /**
     * 将年月日时分数值格式转换成 时间戳字符串的形式
     *
     * @return
     */
    public static String intToTimeStamp(int year, int month, int day, int hours, int min, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hours, min, seconds);
        cal.set(Calendar.MILLISECOND, 0);
        Date time = cal.getTime();
        return String.valueOf(time.getTime());
    }

    /**
     * 将年月日数值格式转换成 年月日的时间格式
     *
     * @return
     */
    public static String intToTimeStampWithDay(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date time = cal.getTime();
        return date2Str(time, DATEFORMAT_DAY);
    }

    /**
     * 将年月日时分秒格式转换成 小时的时间戳
     *
     * @param dateTime
     * @return
     */
    public static String strToTimeStampWithHour(String dateTime) {
        if (TextUtils.isEmpty(dateTime)) {
            return "";
        }

        //必须使用这个格式化
        SimpleDateFormat format = new SimpleDateFormat(DATEFORMAT_COM_HOUR);
        Date date;
        try {
            date = format.parse(dateTime);

            if (date == null) {
                return "";
            }
            return String.valueOf(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 将年月日时分秒格式转换成 日期的时间戳
     *
     * @param dateTime
     * @return
     */
    public static String strToTimeStampWithDay(String dateTime) {

        if (TextUtils.isEmpty(dateTime)) {
            return "";
        }

        //必须使用这个格式化
        SimpleDateFormat format = new SimpleDateFormat(DATEFORMAT_DAY);
        Date date;
        try {
            date = format.parse(dateTime);

            if (date == null) {
                return "";
            }
            return String.valueOf(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将yymmdd格式转为时间戳
     *
     * @param dateTime
     * @return
     */
    public static String strYyMmDdToTimeStampWithDay(String dateTime) {

        if (TextUtils.isEmpty(dateTime)) {
            return "";
        }

        //必须使用这个格式化
        SimpleDateFormat format = new SimpleDateFormat(DATEFORMAT_DAY_INT);
        Date date;
        try {
            date = format.parse(dateTime);

            if (date == null) {
                return "";
            }
            return String.valueOf(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将时间戳转换成日期格式
     *
     * @param timeStamp
     * @return
     */
    public static String timeStampToStr(String timeStamp) {
        if (TextUtils.isEmpty(timeStamp)) {
            return "";
        }

        //必须使用这个格式化
        SimpleDateFormat format = new SimpleDateFormat(DATEFORMAT_DAY);
        Date date;
        try {
            date = format.parse(timeStamp);

            if (date == null) {
                return "";
            }
            return String.valueOf(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 时间戳转换成Date
     *
     * @param timeStamp
     * @return
     */
    public static Date timeStamp2Date(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return null;
        }
        Date date = new Date();
        long time = string2Long(timeStamp);
        //时间戳如果只到秒，需要乘以1000
        date.setTime(time);
        if (date.getYear() == 70) {
            date.setTime(time * 1000);
        }

        return date;
    }

    /**
     * 字符串转long
     *
     * @param string
     * @return
     */
    public static long string2Long(String string) {
        try {
            if (TextUtils.isEmpty(string)) {
                return 0;
            }

            return Long.parseLong(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param timeStamp
     * @return
     */
    public static String timeStamp2DateStr(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_COMM);
        return sdf.format(timeStamp2Date(timeStamp));
    }

    /**
     * 时间戳转为：年月日时分
     *
     * @param timeStamp
     * @return
     */
    public static String timeStamp2YmdHm(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_COM_YYMMDD_HHMM);
        return sdf.format(timeStamp2Date(timeStamp));
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param timeStamp
     * @return
     */
    public static Integer timeStamp2DateInt(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return 0;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_DAY_INT);
        try {
            return Integer.valueOf(sdf.format(timeStamp2Date(timeStamp)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * 将时间戳转为时分秒
     *
     * @param timeStamp
     * @return
     */
    public static String timeStamp2HnmStr(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_HOUR_MIN_S);
        return sdf.format(timeStamp2Date(timeStamp));
    }

    /**
     * 将时间戳转为时分
     *
     * @param timeStamp
     * @return
     */
    public static String timeStamp2HmStr(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_HOUR_MIN);
        return sdf.format(timeStamp2Date(timeStamp));
    }

    /**
     * 将时间戳转为年月日时
     *
     * @param timeStamp
     * @return
     */
    public static String timeStamp2Hour(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_COM_HOUR);
        return sdf.format(timeStamp2Date(timeStamp));
    }

    /**
     * 将时间戳转为年月日
     *
     * @param timeStamp
     * @return
     */
    public static String timeStamp2Day(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_DAY);
        return sdf.format(timeStamp2Date(timeStamp));
    }

    /**
     * 将时间戳转为年月
     *
     * @param timeStamp
     * @return
     */
    public static String timeStamp2Month(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_MONTH);
        return sdf.format(timeStamp2Date(timeStamp));
    }


    /**
     * 将年月字符串转换成对应语言的年月，如2020-06，转换成中文 2020年6月，英文2020 June
     *
     * @param timeStamp
     * @return
     */
    /*public static String timeStampFormatLanguage(String timeStamp) {
        if (TextUtils.isEmpty(timeStamp)) {
            return "";
        }
        DateFormat format = new SimpleDateFormat(DATEFORMAT_MONTH);
        Date date = null;
        try {
            date = format.parse(timeStamp);
            return date2StrByCountry(date, false);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStamp;
    }*/

    /**
     * 根据时间戳 获取日期里的小时
     *
     * @param timeStamp
     * @return
     */
    public static int getHourFromTimeStamp(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return -1;
        }
        Date date = timeStamp2Date(timeStamp);
        return date == null ? -1 : date.getHours();
    }

    /**
     * 获取日期里的小时
     *
     * @param date
     * @return
     */
    public static int getHourFromDate(Date date) {
        if (date == null) {
            return -1;
        }

        return date.getHours();
    }


    /**
     * 得到日期的结束日期，例如2004-1-1 15:12，转换后为2004-1-1 23:59:59，
     *
     * @param date 所要转换的日期
     * @return 为第二天的零点整
     */
    public static Date getTodayEnd(Date date) {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(date);
        gc.set(Calendar.HOUR_OF_DAY, 23);
        gc.set(Calendar.MINUTE, 59);
        gc.set(Calendar.SECOND, 59);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTime();
    }

    /**
     * 得到日期的起始日期，例如2004-1-1 15:12，转换后为 2004-1-1 00:00
     *
     * @param date 需要转换的日期
     * @return 该日期的零点
     */
    public static Date getTodayStart(Date date) {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(date);
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTime();
    }

    /**
     * 获取小时的开始时间
     *
     * @param date
     * @return
     */
    public static Date getHourStart(Date date) {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(date);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc.getTime();
    }

    /**
     * 获取上个月的此天日期
     *
     * @return
     */
    public static Date getLastMonth() {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(new Date());
        gc.add(Calendar.MONTH, -1);
        return gc.getTime();
    }

    /**
     * 重载:
     * 获取上个月的此天日期
     *
     * @return
     */
    public static Date getLastMonth(Date date) {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(date);
        gc.add(Calendar.MONTH, -1);
        return gc.getTime();
    }

    /**
     * 获取昨天
     *
     * @return
     */
    public static Date getLastDay() {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(new Date());
        gc.add(Calendar.DATE, -1);
        return gc.getTime();
    }

    /**
     * 获取指定天之前
     *
     * @param count
     * @return
     */
    public static Date getLastDay(int count) {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(new Date());
        gc.add(Calendar.DATE, -count);
        return gc.getTime();
    }

    /**
     * 获取指定天之前
     *
     * @param date  指定日期
     * @param count
     * @return
     */
    public static Date getLastDay(Date date, int count) {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(date);
        gc.add(Calendar.DATE, -count);
        return gc.getTime();
    }

    /**
     * 获取上一个小时
     *
     * @param date
     * @return
     */
    public static Date getLastHour(Date date) {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(date);
        gc.add(Calendar.HOUR_OF_DAY, -1);
        return gc.getTime();
    }

    /**
     * 获取 上一年 的此天日期
     *
     * @return
     */
    public static Date getLastYear() {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(new Date());
        gc.add(Calendar.YEAR, -1);
        return gc.getTime();
    }

    /**
     * 在指定的日期基础上，增加或是减少天数
     *
     * @param date 指定的日期
     * @param days 需要增加或是减少的天数，正数为增加，负数为减少
     * @return 增加或是减少后的日期
     */
    public static Date dateDayAdd(Date date, int days) {
        final long now = date.getTime() + (days * DAY_IN_MILLISECOND);
        return new Date(now);
    }


    /**
     * 得到日期所在月份的开始日期（第一天的开始日期），例如2004-1-15 15:10，转换后为2004-1-1 00:00
     *
     * @param date 需要转换的日期
     * @return 日期所在月份的开始日期
     */
    public static Date getMonthBegin(Date date) {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(date);
        final int year = gc.get(Calendar.YEAR);
        final int mon = gc.get(Calendar.MONTH);
        final Calendar gCal = new GregorianCalendar(year, mon, 1);
        return gCal.getTime();
    }

    /**
     * 根据年、月返回由年、月构成的日期的月份开始日期
     *
     * @param year  所在的年
     * @param month 所在的月份，从1月到12月
     * @return 由年、月构成的日期的月份开始日期
     */
    public static Date getMonthBegin(int year, int month) {
        if ((month <= 0) || (month > 12)) {
            throw new IllegalArgumentException("month must from 1 to 12");
        }
        final Calendar gc = new GregorianCalendar(year, month - 1, 1);
        return gc.getTime();
    }

    /**
     * 根据日期所在的月份，得到下个月的第一天零点整
     *
     * @param date 需要转换的日期
     * @return 这个月的最后一天 23:59:59
     */
    public static Date getMonthEnd(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        return getTodayStart(cal.getTime());
    }

    /**
     * 这个月的最后一天 23:59:59
     *
     * @param date
     * @return
     */
    public static Date getMonthEnd2(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 根据日期所在的星期，得到这个星期的开始日期，注意，每周从星期日开始计算
     *
     * @param date 需要转换的日期
     * @return 传入日期所在周的第一天的零点整
     */
    /*public static Date getWeekBegin(Date date) {
        final Calendar gCal = Calendar.getInstance();
        int firstDayOfWeek = getFirstDayOfWeek();
        gCal.setFirstDayOfWeek(firstDayOfWeek);
        gCal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        gCal.setTime(date);
        gCal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);

        Date time = gCal.getTime();
        return time;
    }

    *//**
     * 根据日期所在的星期，得到下周开始第一天的零点整
     *
     * @param date 需要转换的日期
     * @return 传入日期的下周开始第一天的零点整
     *//*
    public static Date getWeekEnd(Date date) {
        final Calendar gCal = Calendar.getInstance();
        int firstDayOfWeek = getFirstDayOfWeek();
        gCal.setFirstDayOfWeek(firstDayOfWeek);
        gCal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek + 6);
        gCal.setTime(date);
        gCal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek + 6);
        Date time = gCal.getTime();
        return getTodayEnd(time);
    }*/

    /**
     * 根据年、月返回由年、月构成的日期的下一个月第一天零点整
     *
     * @param year  所在的年
     * @param month 所在的月份，从1月到12月
     * @return 这个月的最后一天 23:59:59
     */
    public static Date getMonthEnd(int year, int month) {
        final Date start = getMonthBegin(year, month);
        return getMonthEnd(start);
    }

    /**
     * 根据年、月返回由年、月构成的日期的下一个月第一天零点整
     *
     * @param year  所在的年
     * @param month 所在的月份，从1月到12月
     * @return 这个月的最后一天 23:59:59
     */
    public static Date getMonthEnd2(int year, int month) {
        final Date start = getMonthBegin(year, month);
        return getMonthEnd2(start);
    }

    /**
     * 在指定日期的基础上，增加或是减少月份信息，如1月31日，增加一个月后，则为2月28日（非闰年）
     *
     * @param date   指定的日期
     * @param months 增加或是减少的月份数，正数为增加，负数为减少
     * @return 增加或是减少后的日期
     */
    public static Date dateMonthAdd(Date date, int months) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int m = cal.get(Calendar.MONTH) + months;
        if (m < 0) {
            m += -12;
        }
        cal.roll(Calendar.YEAR, m / 12);
        cal.roll(Calendar.MONTH, months);
        return cal.getTime();
    }


    /**
     * 得到指定日期在当前星期中的天数，例如2004-5-20日，返回5，
     * <p>
     * 每周以周日为开始按1计算，所以星期四为5
     *
     * @param date 指定的日期
     * @return 返回天数
     */
    public static int getDateWeekDay(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * 得到指定日期在当前周内是第几天 (周一开始)
     *
     * @param date 指定日期
     * @return 周内天书
     */
    public static int getWeek(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return ((cal.get(Calendar.DAY_OF_WEEK) - 1) + 7) % 7;
    }


    /**
     * 计算两个时间之间的时间差
     *
     * @param from 开始
     * @param to   结束
     * @return 时间差
     */
    public static long calculateTimeInMillis(Date from, Date to) {
        final Calendar fromCal = getCalendar(from);
        final Calendar toCal = getCalendar(to);
        if (fromCal.after(toCal)) {
            fromCal.setTime(to);
            toCal.setTime(from);
        }
        return toCal.getTimeInMillis() - fromCal.getTimeInMillis();
    }

    /**
     * 获取Calendar实例
     *
     * @param date 日期类型
     * @return
     */
    public static Calendar getCalendar(Date date) {
        final Calendar gc = Calendar.getInstance();
        gc.setTime(date);
        return gc;
    }


    /**
     * 字符串转换成日期
     */
    public static Date str2Date(String str) {
        if (TextUtils.isEmpty(str))
            return null;
        //先用时间戳转换，失败之后再用日期转换
        DateFormat format = new SimpleDateFormat(DATEFORMAT_COMM, Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
//            e.printStackTrace();
            format = new SimpleDateFormat(DATEFORMAT_DAY, Locale.ENGLISH);
            try {
                date = format.parse(str);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        return date;
    }

    /*
    public static String str2OtherStr(String inStrTime, String inPattern, String outPattern) {

        DateFormat intFormat = new SimpleDateFormat(inPattern);
        DateFormat outFormat = new SimpleDateFormat(outPattern);
        Date date = null;
        try {
            date = intFormat.parse(inStrTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String outStrTime=null;
        if (date!=null){
            outStrTime = outFormat.format(date);
        }

        return outStrTime;
    }*/


    /**
     * 日期转换成字符串:年月日时分秒
     */
    public static String date2Str(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_COMM);
        return sdf.format(date);
    }

    /**
     * 日期转换成字符串:指定的日期格式
     */
    public static String date2Str(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 日期转换成字符串:指定的日期格式
     */
    public static String date2Str(Date date, String pattern, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
        return sdf.format(date);
    }

    /**
     * 日期转换成字符串：年月日
     */
    public static String date2StrOnDay(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_DAY);
        return sdf.format(date);
    }

    /**
     * 设置每周的起始日，可传Calendar.MONDAY,Calendar.SATURDAY,Calendar.SUNDAY对应周一，周六，周日
     *
     * @param firstDayOfWeek Calendar.MONDAY,Calendar.SATURDAY,Calendar.SUNDAY
     * @author
     * created at 2020/4/21 16:32
     */
    /*public static void setFirstDayOfWeek(int firstDayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(firstDayOfWeek);
        MMKV.defaultMMKV().putInt(MMKVConstant.MMKVCommon.COMMON_FIRST_DAY_OF_WEEK, firstDayOfWeek);
        EventBus.getDefault().post(new UnitChangedEvent().firstWeekDay(true));
    }*/

    /**
     * 获取每周的起始日，默认周日
     *
     * @author
     * created at 2020/4/21 16:38
     */
    /*public static int getFirstDayOfWeek() {
        return MMKV.defaultMMKV().getInt(MMKVConstant.MMKVCommon.COMMON_FIRST_DAY_OF_WEEK, getDefaultWeekStart());
    }

    public static int getDefaultWeekStart() {
        return UnitTools.getAppUnitByLanguage(CountryUtil.getAppLanguage()).weekStart;
    }

    public static void clearFirstDayOfWeek() {
        MMKV.defaultMMKV().remove(MMKVConstant.MMKVCommon.COMMON_FIRST_DAY_OF_WEEK);
    }*/

    /**
     * 判断当天在本周内的索引
     * @return
     */
    /*public static int getCurrentDayOfWeekIndex(){
        Date date = new Date();
        List<WeekDay> weekDays = getWeekDay();
        for (int i = 0; i < weekDays.size(); i++) {
            WeekDay weekDay = weekDays.get(i);
            if (DateTimeUtil.isSameDay(date.getTime(),weekDay.time))return i;
        }
        return -1;
    }*/

    /**
     * 获取一周的每一天(默认星期开始日为周一)
     *
     * @author created at 2020/4/21 16:20
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<WeekDay> getWeekDay() {
        return getWeekDay(Calendar.MONDAY);
    }

    /**
     * 根据当前日期，偏移offset个周的那周每一天数据
     *
     * @param firstDayOfWeekP 星期开始日
     * @author created at 2020/4/21 16:20
     */
//    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<WeekDay> getWeekDay(int firstDayOfWeekP) {
        LogUtils.i("getWeekDay firstDayOfWeekP:" + firstDayOfWeekP);
        LogUtils.i();
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(firstDayOfWeekP);
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        // 获取本周的第一天
        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        List<WeekDay> list = new ArrayList<>();

        Locale currentLocale = Locale.getDefault();
        String dayPattern = "MM-dd-yyyy";
        SimpleDateFormat format = new SimpleDateFormat(dayPattern);
        for (int i = 0; i < 7; i++) {
            WeekDay weekDay = new WeekDay();
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek + i);
            // 获取星期的显示名称，例如：周一、星期一、Monday等等
            String displayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, currentLocale);
//            if (isLocaleLanguageEqual(currentLocale, Locale.CHINESE)) {
//                String substring = displayName.substring(displayName.length() - 1);
//                weekDay.week = substring;
//            } else { 周一 --> 一
            weekDay.week = displayName;
//            }

            weekDay.day = format.format(calendar.getTime());
            weekDay.index = calendar.get(Calendar.DAY_OF_WEEK);
            weekDay.time = calendar.getTimeInMillis();

            list.add(weekDay);
        }

        return list;
    }

    /**
     * 获取一个月天数
     *
     * @param date
     * @return
     */
    public static int getDaysOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    /**
     * 得到两个日期之间的天数列表，返回是 年月日
     *
     * @param startTime 如：2021-08-04 15:00:00
     * @param endTime   如：2021-08-04 16:00:00
     * @return
     */
    public static List<String> getDays(String startTime, String endTime) {

        // 返回的日期集合
        List<String> days = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat(DATEFORMAT_DAY);
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }

    /**
     * 获取两个时间中间的小时
     *
     * @param startTime 2021-08-04 15:10:00
     * @param endTime   2021-08-04 18:30:00
     * @return
     */
    public static List<String> getHours(String startTime, String endTime) {

        // 返回的日期集合
        List<String> hours = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat(DATEFORMAT_COMM);
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);
            tempStart.set(Calendar.MINUTE, 0);
            tempStart.set(Calendar.SECOND, 0);
            tempStart.set(Calendar.MILLISECOND, 0);
            tempStart.add(Calendar.HOUR_OF_DAY, 1);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.HOUR_OF_DAY, 1);
            tempEnd.set(Calendar.MINUTE, 0);
            tempEnd.set(Calendar.SECOND, 0);
            tempEnd.set(Calendar.MILLISECOND, 0);

            while (tempStart.before(tempEnd)) {
                hours.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.HOUR_OF_DAY, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        LogUtils.i("两个日期之间的小时为---> ", hours);

        return hours;
    }

    /**
     * 得到两个日期之间的天数列表，返回年月日的 时间戳
     *
     * @param startTime 如：2020-04-25
     * @param endTime   如：2020-04-25
     * @return
     */
    public static List<String> getDaysForTimeStamp(String startTime, String endTime) {

        // 返回的日期集合
        List<String> days = getDays(startTime, endTime);
        List<String> returnList = new LinkedList<>();
        for (String date : days) {
            returnList.add(DateTimeUtil.strToTimeStampWithDay(date));
        }

        return returnList;
    }

    /**
     * 判断：开始时间 是否在 结束时间 之前
     *
     * @param startTimeStr
     * @param endTimeStr
     * @return
     */
    public static boolean isStartBeforeEnd(String startTimeStr, String endTimeStr) {

        Date startDate = str2Date(startTimeStr);
        Date endDate = str2Date(endTimeStr);

        if (startDate == null || endDate == null) {
            return false;
        }

        return startDate.getTime() < endDate.getTime();
    }

    /**
     * 得到下一天的时间字符串
     *
     * @param dateStr
     * @return
     */
    public static String getNextDayStr(String dateStr) {

        if (TextUtils.isEmpty(dateStr)) {
            return "";
        }

        DateFormat dft = new SimpleDateFormat(DATEFORMAT_DAY, Locale.ENGLISH);
        try {
            Date temp = dft.parse(dateStr);
            Calendar cld = Calendar.getInstance();
            cld.setTime(temp);
            cld.add(Calendar.DATE, 1);
            temp = cld.getTime();
            //获得下一天日期字符串
            String nextDay = dft.format(temp);

            return nextDay;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 得到前一天的时间字符串
     *
     * @param dateStr
     * @return
     */
    public static String getBeforeDayStr(String dateStr) {

        if (TextUtils.isEmpty(dateStr)) {
            return "";
        }

        DateFormat dft = new SimpleDateFormat(DATEFORMAT_DAY);
        try {
            Date temp = dft.parse(dateStr);
            Calendar cld = Calendar.getInstance();
            cld.setTime(temp);
            cld.add(Calendar.DATE, -1);
            temp = cld.getTime();
            //获得下一天日期字符串
            String nextDay = dft.format(temp);

            return nextDay;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 判断两个时间小时分钟是否一致
     *
     * @param hour
     * @param minute
     * @param endHour
     * @param endMinute
     * @return
     */
    public static boolean isSameHourAndMinute(int hour, int minute, int endHour, int endMinute) {
        return hour == endHour && minute == endMinute;
    }

    /**
     * 求两个小时分钟的时间段间隔多少分钟
     *
     * @param sHour
     * @param sMinute
     * @param endHour
     * @param endMinute
     * @return
     */
    public static int getGapMinutesByHourAndMinute(int sHour, int sMinute, int endHour, int endMinute, boolean isOverDay) {
        int startMCount = sHour * 60 + sMinute;
        int endMCount = endHour * 60 + endMinute;

        if (startMCount >= endMCount) {
            //跨天
            if (isOverDay) {
                return 24 * 60 + endMCount - startMCount;
            } else {
                return endMCount - startMCount;
            }
        } else {
            return endMCount - startMCount;
        }
    }

    /**
     * 求两个小时分钟的时间是否跨天
     *
     * @param sHour
     * @param sMinute
     * @param endHour
     * @param endMinute
     * @return
     */
    public static boolean isOverDayByHourAndMinute(int sHour, int sMinute, int endHour, int endMinute) {
        int startMCount = sHour * 60 + sMinute;
        int endMCount = endHour * 60 + endMinute;

        if (startMCount >= endMCount) {
            //跨天
            return true;
        } else {
            return false;
        }
    }

    public static class WeekDay {
        /**
         * 星期的显示名称
         */
        public String week;
        /**
         * 对应的日期
         */
        public String day;

        /**
         * 对应的在一周内的索引
         */
        public int index;

        public long time;

        public boolean isSelected;
        public int id;

        @Override
        public String toString() {
            return "WeekDay{" +
                    "week='" + week + '\'' +
                    ", day='" + day + '\'' +
                    ", index=" + index +
                    ", time=" + time +
                    ", isSelected=" + isSelected +
                    '}';
        }
    }

    /**
     * 将输入的日期字符串格式转成Date对象
     */
    public static synchronized Date str2Date(String time, String pattern) {
        SimpleDateFormat mFormatter = new SimpleDateFormat(DATEFORMAT_DAY);
        mFormatter.applyPattern(pattern);
        try {
            return mFormatter.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过日期对象得年份的整数值
     *
     * @param date
     * @return
     */
    public static int getYearFromDate(Date date) {
        int result = -1;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            result = cal.get(Calendar.YEAR);
        }
        return result;
    }

    /**
     * 通过日期对象得月份的整数值
     *
     * @param date
     * @return
     */
    public static int getMonthFromDate(Date date) {
        int result = -1;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            result = cal.get(Calendar.MONTH) + 1;
        }
        return result;
    }

    /**
     * 通过日期对象得小时的整数值(自动判断24小时制还是12小时制)
     *
     * @param date
     * @return
     */
    public static int getHoursFromDate(Date date, Context ctx) {
        int result = -1;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if (is24(ctx)) {
                result = cal.get(Calendar.HOUR_OF_DAY);
            } else {
                result = cal.get(Calendar.HOUR);
            }
        }
        LogUtils.i("getHoursFromDate", " result " + result);

        return result;
    }

    /**
     * 通过日期对象得小时的整数值(24小时制)
     *
     * @param date
     * @return
     */
    public static int getHoursFromDateFor24(Date date) {
        int result = -1;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            result = cal.get(Calendar.HOUR_OF_DAY);
        }

//        LogUtils.i("getHoursFromDateFor24", " result " + result);

        return result;
    }

    /**
     * 设置小时和时间，返回日期对象(24小时制)
     *
     * @return
     */
    public static Date setHoursAndMinutesForDate(int hours, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 通过日期对象得分钟的整数值(自动判断24小时制还是12小时制)
     *
     * @param date
     * @return
     */
    public static int getMinutesFromDate(Date date) {
        int result = -1;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            result = cal.get(Calendar.MINUTE);
        }
//        LogUtils.i("getMinutesFromDate", " result " + result);

        return result;
    }

    /**
     * 将秒置为0
     *
     * @param date
     * @return
     */
    public static long getMinutesTimeStampFromDate(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        }
        return 0;
    }

    /**
     * 是否是24小时制
     *
     * @param ctx
     * @return
     */
    public static boolean is24(Context ctx) {
       /* ContentResolver cv = ctx.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
        //strTimeFormat某些rom12小时制时会返回null
        return strTimeFormat != null && strTimeFormat.equals("24");*/

        return android.text.format.DateFormat.is24HourFormat(ctx);

    }

    /**
     * 求年龄
     *
     * @param birthday
     * @return
     */
    public static int getAgeByBirth(Date birthday) {
        if (birthday == null) {
            return 0;
        }


        //Calendar：日历
        /*从Calendar对象中或得一个Date对象*/
        Calendar cal = Calendar.getInstance();
        /*把出生日期放入Calendar类型的bir对象中，进行Calendar和Date类型进行转换*/
        Calendar bir = Calendar.getInstance();
        bir.setTime(birthday);
        /*如果生日大于当前日期，则抛出异常：出生日期不能大于当前日期*/
        if (cal.before(birthday)) {
            throw new IllegalArgumentException("The birthday is before Now,It's unbelievable");
        }
        /*取出当前年月日*/
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayNow = cal.get(Calendar.DAY_OF_MONTH);
        /*取出出生年月日*/
        int yearBirth = bir.get(Calendar.YEAR);
        int monthBirth = bir.get(Calendar.MONTH);
        int dayBirth = bir.get(Calendar.DAY_OF_MONTH);
        /*大概年龄是当前年减去出生年*/
        int age = yearNow - yearBirth;
        /*如果出当前月小与出生月，或者当前月等于出生月但是当前日小于出生日，那么年龄age就减一岁*/
        if (monthNow < monthBirth || (monthNow == monthBirth && dayNow < dayBirth)) {
            age--;
        }
        return age;
    }

    /**
     * 获取两个时间段的分钟差
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getGapMinutes(String startDate, String endDate) {
        return getGapMinutes(startDate, endDate, null, false);
    }

    /**
     * 获取两个时间段的分钟差(重载方法)
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getGapMinutes(String startDate, String endDate, String pattern, boolean isRecycle) {

        if (TextUtils.isEmpty(pattern)) {
            pattern = "yyyy-MM-dd HH:mm";
        }

        long start = 0;
        long end = 0;
        try {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            start = df.parse(startDate).getTime();
            end = df.parse(endDate).getTime();
        } catch (Exception e) {
        }

        int minutes = (int) ((end - start) / (1000 * 60));
        if (isRecycle) {
            minutes = (minutes + 1400) % 1400;
        }
        return minutes;
    }

    /**
     * 获取两个时间段的分钟差(重载方法)
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getGapMinutes(Date startDate, Date endDate) {

        long start = startDate.getTime();
        long end = endDate.getTime();
        int minutes = (int) ((end - start) / (1000 * 60));
        return minutes;
    }

    /**
     * 判断时间是否是整点(速度更快)
     *
     * @return
     */
    public static boolean isWholeHourOther(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);
        return minutes == 0 && seconds == 0;
    }

    /**
     * 根据语言将日期格式转换对应的日期显示
     *
     * @author
     * created at 2020/5/28 15:40
     */
    /*public static String strFormatDateByLanguage(String dateStr) {
        return strFormatDateByLanguage(dateStr, false);
    }

    *//**
     * 根据语言将日期格式转换对应的日期显示
     *
     * @param toMonth 是否只显示到月份，即年月
     * @author
     * created at 2020/5/28 15:40
     *//*
    public static String strFormatDateByLanguage(String dateStr, boolean toMonth) {
        Date date = str2Date(dateStr);
        if (date != null) {
            return date2StrByCountry(date, !toMonth);
        }
        return dateStr;
    }

    *//**
     * 根据语言将日期格式转换对应的日期显示
     *
     * @param toMonth 是否只显示到月份，即年月
     * @author
     * created at 2020/5/28 15:40
     *//*
    public static String formatDateByLanguage(Date date, boolean toMonth) {
        if (date != null) {
            LanguageType languageType = CountryUtil.getAppLanguage();
            if (languageType == LanguageType.de) {
                return date2Str(date, toMonth ? "MMMM yyyy" : "d. MMMM yyyy", Locale.GERMANY);
            } else if (languageType == LanguageType.en) {
                return date2Str(date, toMonth ? "MMMM yyyy" : "MMMM d, yyyy", Locale.ENGLISH);
            } else if (languageType == LanguageType.zh) {
                return date2Str(date, toMonth ? "yyyy年M月" : "yyyy年M月d日");
            }
        }
        return "";
    }*/

    /**
     * 判断时间是否是0点整
     *
     * @return
     */
    public static boolean isZeroTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);
        return hours == 0 && minutes == 0 && seconds == 0;
    }

    /**
     * 判断是不是同一天
     *
     * @param day1
     * @param day2
     * @return
     */
    public static boolean isSameDay(long day1, long day2) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(day1);
        int d1 = instance.get(Calendar.DAY_OF_YEAR);
        instance.setTimeInMillis(day2);
        int d2 = instance.get(Calendar.DAY_OF_YEAR);
        return d1 == d2;
    }

    /**
     * 判断是不是同一个小时
     *
     * @param day1
     * @param day2
     * @return
     */
    public static boolean isSameHour(long day1, long day2) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(day1);
        int d1 = instance.get(Calendar.HOUR_OF_DAY);
        instance.setTimeInMillis(day2);
        int d2 = instance.get(Calendar.HOUR_OF_DAY);
        return d1 == d2;
    }

    /**
     * 获取年月日数组
     *
     * @return
     */
    public static int[] getYearMonthDay(Date date) {
        int[] ints = new int[3];
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        ints[0] = cal.get(Calendar.YEAR);
        ints[1] = cal.get(Calendar.MONTH) + 1;
        ints[2] = cal.get(Calendar.DAY_OF_MONTH);
        return ints;
    }

    /**
     * 获取一段时间内处于中央位置的Date(整点)
     *
     * @return
     */
    /*public static Date getCenterDateByTime(Date startDate, Date endDate) {

        int startHoursFromDateFor24 = getHoursFromDateFor24(startDate);
        int endHoursFromDateFor24 = getHoursFromDateFor24(endDate);
        if (endHoursFromDateFor24 <= startHoursFromDateFor24) {
            endHoursFromDateFor24 = endHoursFromDateFor24 + 24;
        }
        int delDa = (endHoursFromDateFor24 - startHoursFromDateFor24) / 2;
        int delDa1 = (endHoursFromDateFor24 - startHoursFromDateFor24) % 2;
        if (delDa1 != 0) {
            delDa++;
        }
        //如果中间整点时间跟结束时间在同一个小时，则取开始时间跟结束时间的中间时间点
        if (startHoursFromDateFor24 + delDa == endHoursFromDateFor24) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date date = new DateTime(cal.getTime()).plusMinutes(((endHoursFromDateFor24 - startHoursFromDateFor24) * 60 + endDate.getMinutes() - startDate.getMinutes()) / 2).toDate();
            return date;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date newDate = cal.getTime();
        Date date = new DateTime(newDate.getTime()).plusHours(delDa).toDate();
        return date;
    }*/

    /**
     * 获取一段时间内处于中央位置的Date(平均值)
     *
     * @return
     */
    /*public static Date getAverageDateByTime(Date startDate, Date endDate) {

        int startHoursFromDateFor24 = getHoursFromDateFor24(startDate);
        int endHoursFromDateFor24 = getHoursFromDateFor24(endDate);
        if (endHoursFromDateFor24 <= startHoursFromDateFor24) {
            endHoursFromDateFor24 = endHoursFromDateFor24 + 24;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = new DateTime(cal.getTime()).plusMinutes(((endHoursFromDateFor24 - startHoursFromDateFor24) * 60 + endDate.getMinutes() - startDate.getMinutes()) / 2).toDate();
        return date;
    }*/

    /**
     * 设置Date 秒数和毫秒数为0
     *
     * @return
     */
    public static Date setDateSecondsAndMilliSecondsForZero(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date newDate = cal.getTime();
        return newDate;
    }

    /**
     * 日期转换成字符串:根据不同的语言输出不同的格式
     */
    /*public static String date2StrByCountry(Date date) {
        return date2StrByCountry(date, true);
    }*/

    /**
     * 日期转换成字符串:根据不同的语言输出不同的格式
     */
    public static String date2StrByCountry(Date date, boolean showDay, boolean showYear) {
        String name;
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        int year = instance.get(Calendar.YEAR);
//        int month = instance.get(Calendar.MONTH) + 1;
        int day = instance.get(Calendar.DAY_OF_MONTH);

        Locale locale;
        String yearTip = showYear ? "yyyy年" : "";

        if (DateTimeUtil.isLocaleLanguageEqual(Locale.getDefault(), Locale.CHINESE)) {
            locale = Locale.CHINESE;
            SimpleDateFormat sdf = new SimpleDateFormat(!showDay ? yearTip + "M月" : yearTip + "M月d日", locale);
            name = sdf.format(date);
        } else {
            locale = Locale.ENGLISH;
            String monthStr2 = instance.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
            name = monthStr2 + " " + yearTip;
        }

        return name;
    }

    public static String date2StrByCountry(Date date, boolean showDay) {

        return date2StrByCountry(date, showDay, true);
    }

    /*public static String date2StrByCountryOnlyYear(Date date) {
        String name;
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        int year = instance.get(Calendar.YEAR);

        Locale locale;
        LanguageType appLanguage = CountryUtil.getAppLanguage();
        switch (appLanguage) {
            case ja:
            case zh:
                locale = Locale.CHINESE;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年", locale);
                name = sdf.format(date);
                break;
            case de:
                locale = Locale.GERMAN;
                name = String.valueOf(year);
                break;
            default:
                locale = Locale.ENGLISH;
                name = String.valueOf(year);
                break;
        }

        return name;
    }*/

    public static String getCurrentDay() {
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        return date2Str(date, DATEFORMAT_DAY);
    }

    /**
     * 通过生日返回岁数，参数格式为yyyy-MM-dd，如为空，返回0岁
     *
     * @author created at 2020/7/11 9:04
     */
    public static int getAge(String birthday) {
        if (TextUtils.isEmpty(birthday)) {
            return 0;
        }
        Date dateBirthday = str2Date(birthday);
        if (dateBirthday == null) {
            return 0;
        }
        return getAge(dateBirthday);
    }

    /**
     * 通过生日返回岁数，参数格式为yyyy-MM-dd，如为空，返回0岁
     *
     * @author created at 2020/7/11 9:04
     */
    public static int getAge(Date dateBirthday) {

        Date date = new Date();
        date.setTime(System.currentTimeMillis());

        if (dateBirthday == null) {
            return 0;
        }

        int age = date.getYear() - dateBirthday.getYear();
        if (dateBirthday.getMonth() > date.getMonth()) {
            return age - 1;
        } else if (dateBirthday.getMonth() == date.getMonth() && dateBirthday.getDate() > date.getDate()) {
            return age - 1;
        }
        return age;
    }

    /**
     * 判断两个日期是否是同一周
     *
     * @param date1
     * @param date2
     * @return
     */
    /*public static boolean isSameWeek(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        long weekBegin = getWeekBegin(getTodayStart(date1)).getTime();
        long weekWeekEnd = getWeekEnd(getTodayEnd(date1)).getTime();

        long time = date2.getTime();
        if (time >= weekBegin && time <= weekWeekEnd) {
            return true;
        } else {
            return false;
        }

    }*/

    /**
     * 判断两个日期是否是同一月
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameMonth(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        String value1 = getYearFromDate(date1) + "-" + getMonthFromDate(date1);
        String value2 = getYearFromDate(date2) + "-" + getMonthFromDate(date2);

        if (value1.equals(value2)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 判断两个日期是否是同一年
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameYear(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        String value1 = String.valueOf(getYearFromDate(date1));
        String value2 = String.valueOf(getYearFromDate(date2));

        if (value1.equals(value2)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 上午 下午 是否在时间前面
     *
     * @author created at 2020/11/5 14:46
     */
    /*public static boolean isBeforeTime() {
        return CountryUtil.getAppLanguage() == LanguageType.zh || CountryUtil.getAppLanguage() == LanguageType.ja;
    }*/
    public static String getMidSleepTime(int startHour, int endHour) {
        return DateTimeUtil.date2Str(DateTimeUtil.setHoursAndMinutesForDate(((startHour + ((endHour > startHour ? 0 : 24) + endHour - startHour) / 2) % 24), 0), "HH:mm");
    }

    /**
     * 和当天的时间做比较
     *
     * @param dateStr 比如：2020-04-25
     * @return
     */
    public static String getDateStrWithNowTimeStamp(String dateStr) {
        long nowTimeStamp = DateTimeUtil.getTodayStart(new Date()).getTime();
        //如果查询的时间大于当前时间，则用当前时间
        if (string2Long(DateTimeUtil.strToTimeStampWithDay(dateStr)) > nowTimeStamp) {
            String day = DateTimeUtil.timeStamp2Day(String.valueOf(nowTimeStamp));
            LogUtils.i("【注意】此时间超过当天时间，改为当天时间---> " + dateStr + " -> " + day);
            return day;
        }
        return dateStr;
    }

    /**
     * 跟当前时间戳对比，获取较小的时间戳
     *
     * @param date
     * @return
     */
    public static long getSmallerWithNow(long date) {
        long nowTimeStamp = System.currentTimeMillis();
        return Math.min(date, nowTimeStamp);
    }

    /**
     * 根据 年月日 求时间戳
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static long getTimeStampByNumber(int year, int month, int day) {
        return getTimeStampByNumber(year, month, day, 0, 0, 0);
    }

    /**
     * 根据 年月日时分秒 求时间戳
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param min
     * @param second
     * @return
     */
    public static long getTimeStampByNumber(int year, int month, int day, int hour, int min, int second) {
        return getDateByNumber(year, month, day, hour, min, second).getTime();
    }

    /**
     * 根据年月日时分秒 求 Date
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param min
     * @param second
     * @return
     */
    public static Date getDateByNumber(int year, int month, int day, int hour, int min, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 根据年月日时分秒 求 Date
     *
     * @param hour
     * @param min
     * @return
     */
    public static Date getDateByNumber(int hour, int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 根据毫秒时间戳 求 时长为多少分钟多少秒
     *
     * @return
     */
    public static long[] getTimeForMinuteAndSeconds(long time) {
        long[] longs = new long[2];
        long l = time / 1000;
        long minute = l / 60;
        longs[0] = minute;
        long seconds = l % 60;
        longs[1] = seconds;
        return longs;
    }

    /**
     * 求时间是几号
     *
     * @return
     */
    public static int getDayForTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static String getWeekDayForTime(Date date, boolean needChinese) {
        return getWeekDayForTime(date, needChinese, false);
    }

    /**
     * 求时间是星期几
     *
     * @return
     */
    public static String getWeekDayForTime(Date date, boolean needChinese, boolean enUpperCase) {
        String week = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
//        String language = CountryUtil.getAppLanguage().name();
        Locale locale = Locale.getDefault();
        if (!needChinese && isLocaleLanguageEqual(locale, Locale.CHINESE)) {
            locale = Locale.ENGLISH;
        }
        // 获取星期的显示名称，例如：周一、星期一、Monday等等
        String displayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale);
        if (TextUtils.isEmpty(displayName)) {
            return "";
        }
        assert displayName != null;
        if (isLocaleLanguageEqual(locale, Locale.CHINESE) && needChinese) {
            week = displayName.substring(displayName.length() - 1);
        } else {
            if (locale == Locale.ENGLISH && enUpperCase) {
                week = displayName.toUpperCase(locale);
            } else {
                week = displayName;
            }
        }

        return week;
    }

    public static boolean isLocaleLanguageEqual(Locale locale1, Locale locale2) {
        return locale1.getLanguage().equals(locale2.getLanguage());
    }

    /**
     * 获取时分秒格式的时长
     *
     * @author
     * created at 2021/6/28 16:53
     */
    /*public static String getDuration(int duration) {
        StringBuilder sb = new StringBuilder();
        int hour = IntUtils.div(duration, 3600);
        int min = IntUtils.div(duration - hour * 3600, 60);
        int second = (int) IntUtils.remainder(duration - hour * 3600 - min * 60, 60);
        if (hour >= 10) {
            sb.append(hour).append(":");
        } else if (hour >= 0) {
            sb.append("0").append(hour).append(":");
        }

        if (min >= 10) {
            sb.append(min).append(":");
        } else if (min >= 0) {
            sb.append("0").append(min).append(":");
        }

        if (second >= 10) {
            sb.append(second);
        } else if (second >= 0) {
            sb.append("0").append(second);
        }

        return sb.toString();
    }

    public static String getMonthDayWeekStyle(Date date) {
        String dateStr = DateTimeUtil.date2StrByCountry(date, true, false) +
                " " + DateTimeUtil.getWeekDayForTime(date, false, false);
        LanguageType appLanguage = CountryUtil.getAppLanguage();
        switch (appLanguage) {
            case zh:
                dateStr = DateTimeUtil.date2StrByCountry(date, true, false) +
                        " " + DateTimeUtil.getWeekDayForTime(date, false, false);
                break;
            case en:
                dateStr = DateTimeUtil.getWeekDayForTime(date, false, false) + ", " + DateTimeUtil.date2StrByCountry(date, true, false);
                break;
            case de:
            case es:
            case fr:
            case it:
                dateStr = DateTimeUtil.getWeekDayForTime(date, false, false) + " " + DateTimeUtil.date2StrByCountry(date, true, false);
                break;
            case ja:
                dateStr = DateTimeUtil.date2StrByCountry(date, true, false) + " (" + DateTimeUtil.getWeekDayForTime(date, false, false) + ")";
                break;
        }

        return dateStr;
    }

    public static boolean isNight(Date date) {
        Date todayStart = getTodayStart(date);
        long todayStartTime = todayStart.getTime();
        long time18Clock = new DateTime(todayStartTime).plusHours(18).toDate().getTime();
        long time6Clock = new DateTime(todayStartTime).plusHours(6).toDate().getTime();
        long dateTime = date.getTime();
        boolean isDayTime = dateTime >= time6Clock && dateTime < time18Clock;
        return !isDayTime;
    }*/

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
