package cn.emagsoftware.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * <p>关于的时间的通用类
 * @author Wendell
 * @date 2011.04.13
 */
public final class DateUtilities {
	
	public static int getYear() {
		GregorianCalendar calendar = new GregorianCalendar();
		int year = calendar.get(Calendar.YEAR);
		return year;
	}
	
	/**
	 * 是否是闰年
	 * @return
	 */
	public static boolean isLeapYear(){
		int year = getYear();
		if ((year % 4 == 0) && ((year % 100 != 0) | (year % 400 == 0))) return true;
		return false;
	}
	
	public static int getMonth() {
		GregorianCalendar calendar = new GregorianCalendar();
		int month = calendar.get(Calendar.MONTH) + 1;
		return month;
	}

	public static int getDate() {
		GregorianCalendar calendar = new GregorianCalendar();
		int date = calendar.get(Calendar.DATE);
		return date;
	}

	public static int getDay() {
		GregorianCalendar calendar = new GregorianCalendar();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		return day;
	}
	
	public static int getMaxDayThisMonth(){
		GregorianCalendar calendar = new GregorianCalendar();
		int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		return maxDay;
	}
	
	public static int getWeek(){
		GregorianCalendar calendar = new GregorianCalendar();
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		return day;
	}
	
	public static int[] getMondayThisWeek(boolean sundayIsFirst){
		int amount = 0;
		int week = getWeek();
		if(week == Calendar.SUNDAY){
			if(sundayIsFirst) amount = 1;
			else amount = -6;
		}else if(week == Calendar.MONDAY) amount = 0;
		else if(week == Calendar.TUESDAY) amount = -1;
		else if(week == Calendar.WEDNESDAY) amount = -2;
		else if(week == Calendar.THURSDAY) amount = -3;
		else if(week == Calendar.FRIDAY) amount = -4;
		else if(week == Calendar.SATURDAY) amount = -5;
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add(Calendar.DAY_OF_MONTH, amount);
		return new int[]{
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH)
		};
	}
	
	public static int[] getSundayThisWeek(boolean sundayIsFirst){
		int amount = 0;
		int week = getWeek();
		if(week == Calendar.SUNDAY) amount = 0;
		else if(week == Calendar.MONDAY) {
			if(sundayIsFirst) amount = -1;
			else amount = 6;
		}else if(week == Calendar.TUESDAY) {
			if(sundayIsFirst) amount = -2;
			else amount = 5;
		}else if(week == Calendar.WEDNESDAY) {
			if(sundayIsFirst) amount = -3;
			else amount = 4;
		}else if(week == Calendar.THURSDAY) {
			if(sundayIsFirst) amount = -4;
			else amount = 3;
		}else if(week == Calendar.FRIDAY){
			if(sundayIsFirst) amount = -5;
			else amount = 2;
		}else if(week == Calendar.SATURDAY){
			if(sundayIsFirst) amount = -6;
			else amount = 1;
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add(Calendar.DAY_OF_MONTH, amount);
		return new int[]{
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH)
		};
	}
	
	public static int getHours() {
		GregorianCalendar calendar = new GregorianCalendar();
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		return hours;
	}

	public static int getMinutes() {
		GregorianCalendar calendar = new GregorianCalendar();
		int min = calendar.get(Calendar.MINUTE);
		return min;
	}

	public static int getSeconds() {
		GregorianCalendar calendar = new GregorianCalendar();
		int sec = calendar.get(Calendar.SECOND);
		return sec;
	}

	public static int getMilliSeconds() {
		GregorianCalendar calendar = new GregorianCalendar();
		int millisec = calendar.get(Calendar.MILLISECOND);
		return millisec;
	}
	
	/**
	 * <p>dateformat:yyyy-MM-dd,yyyy-M-d,yyyy-MM-dd HH:mm:ss,yyyy-MM-dd H:m:s
	 * @param date
	 * @param dateformat
	 * @return
	 */
	public static String getFormatDate(Date date,String dateformat) {
		SimpleDateFormat format = new SimpleDateFormat(dateformat);
		String time = format.format(date);
		return time;
	}
	
	public static String getFormatDate(Date date) {
		return getFormatDate(date,"yyyy-MM-dd");
	}
	
	/**
	 * <p>dateformat:yyyy-MM-dd,yyyy-M-d,yyyy-MM-dd HH:mm:ss,yyyy-MM-dd H:m:s
	 * @param dateformat
	 * @return
	 */
	public static String getFormatDate(String dateformat) {
		Date now = new Date();
		return getFormatDate(now,dateformat);
	}
	
	public static String getFormatDate() {
		return getFormatDate("yyyy-MM-dd");
	}
	
	/**
	 * <p>dateformat:yyyy-MM-dd,yyyy-M-d,yyyy-MM-dd HH:mm:ss,yyyy-MM-dd H:m:s
	 * @param dateStr
	 * @param dateformat
	 * @return
	 */
	public static Date getParseDate(String dateStr,String dateformat){
		try{
			SimpleDateFormat format = new SimpleDateFormat(dateformat);
			Date date = format.parse(dateStr);
			return date;
		}catch(ParseException e){
			throw new RuntimeException(e);
		}
	}
	
	public static Date getParseDate(String dateStr){
		return getParseDate(dateStr,"yyyy-MM-dd");
	}
	
}

