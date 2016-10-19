package com.astrofizzbizz.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtilties 
{

	public static Date makeDate(int day, int month, int year, int hour, int min, int sec) throws ParseException
	{
		DateFormat df1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String sdate = day + "-" + month + "-" + year + " " + hour + ":" + min + ":" + sec;
		Date date = df1.parse(sdate);
//		System.out.println(sdate);
//		System.out.println(date.toString());
		return date;
	}
	public static Date makeDate(int[] dayMonthYearHourMinuteSecond) throws ParseException
	{
		return makeDate(
				dayMonthYearHourMinuteSecond[0], 
				dayMonthYearHourMinuteSecond[1], 
				dayMonthYearHourMinuteSecond[2], 
				dayMonthYearHourMinuteSecond[3], 
				dayMonthYearHourMinuteSecond[4], 
				dayMonthYearHourMinuteSecond[5]);
	}
	public static int[] dayMonthYearHourMinuteSecond(Date date)
	{
		int[] vec = new int[6];
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		vec[0] = calendar.get(Calendar.DAY_OF_MONTH);
		vec[1] = calendar.get(Calendar.MONTH) + 1;;
		vec[2] = calendar.get(Calendar.YEAR);;
		vec[3] = calendar.get(Calendar.HOUR_OF_DAY);
		vec[4] = calendar.get(Calendar.MINUTE);
		vec[5] = calendar.get(Calendar.SECOND);
		return vec;
	}
	public static int getDay(Date date) 
	{
		int[] dayMonthYearHourMinuteSecond = DateUtilties.dayMonthYearHourMinuteSecond(date);
		return dayMonthYearHourMinuteSecond[0];
	}
	public static int getMonth(Date date) 
	{
		int[] dayMonthYearHourMinuteSecond = DateUtilties.dayMonthYearHourMinuteSecond(date);
		return dayMonthYearHourMinuteSecond[1];
	}
	public static int getYear(Date date) 
	{
		int[] dayMonthYearHourMinuteSecond = DateUtilties.dayMonthYearHourMinuteSecond(date);
		return dayMonthYearHourMinuteSecond[2];
	}
	public static int getHours(Date date) 
	{
		int[] dayMonthYearHourMinuteSecond = DateUtilties.dayMonthYearHourMinuteSecond(date);
		return dayMonthYearHourMinuteSecond[3];
	}
	public static int getMins(Date date) 
	{
		int[] dayMonthYearHourMinuteSecond = DateUtilties.dayMonthYearHourMinuteSecond(date);
		return dayMonthYearHourMinuteSecond[4];
	}
	public static int getSecss(Date date) 
	{
		int[] dayMonthYearHourMinuteSecond = DateUtilties.dayMonthYearHourMinuteSecond(date);
		return dayMonthYearHourMinuteSecond[5];
	}
	public static void main(String[] args) 
	{
	}

}
