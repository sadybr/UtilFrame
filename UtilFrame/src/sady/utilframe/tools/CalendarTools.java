package sady.utilframe.tools;

import java.util.Calendar;

public class CalendarTools {
	
	private CalendarTools() {}
	
	public static String getCalendarDifAsString(long calendarDiff) {
		long time = calendarDiff;
		long day = time / (24 * 60 *60 * 1000);
		time = time % (24 * 60 *60 * 1000);
		long hour = time / (60 *60 * 1000);
		time = time % (60 *60 * 1000);
		long minute = time / (60 * 1000);
		time = time % (60 * 1000);
		long second = time / (1000);
		time = time % (1000);
		
		return StringTools.lPad(hour + (day * 24), 2, '0') + ":" +
		StringTools.lPad(minute, 2, '0') + ":" + StringTools.lPad(second, 2, '0');
	}
	public static String getCalendarDifAsStringMilli(long calendarDiff) {
		long time = calendarDiff;
		long day = time / (24 * 60 *60 * 1000);
		time = time % (24 * 60 *60 * 1000);
		long hour = time / (60 *60 * 1000);
		time = time % (60 *60 * 1000);
		long minute = time / (60 * 1000);
		time = time % (60 * 1000);
		long second = time / (1000);
		time = time % (1000);
		
		return StringTools.lPad(hour + (day * 24), 2, '0') + ":" +
		StringTools.lPad(minute, 2, '0') + ":" + StringTools.lPad(second, 2, '0')
		+ ":" + time;
	}
	
	public static String getCalendarHours(long calendarDiff) {
		String time = getCalendarDifAsString(calendarDiff);
		String times[] = time.split(":");
		return times[0] + "," + StringTools.lPad((Integer.parseInt(times[1]) * 10) / 6, 2, '0'); 
	}
	
	public static long getCalendarDif(Calendar begin, Calendar end) {
		return end.getTimeInMillis() - begin.getTimeInMillis();
	}
}
