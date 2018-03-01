package org.activity.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.activity.evaluation.Evaluation;
import org.activity.ui.PopUps;

/**
 * 
 * Contains date time utility methods.
 * 
 * @author gunjan
 *
 */
public class DateTimeUtils
{

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		// the current date
		Instant i = Instant.now();

		Timestamp ts = Timestamp.from(i);
		System.out.println(i);
		System.out.println(ts);

		System.out.println(getDate(ts));
		System.out.println("getAbvDate= " + getShortDateLabel(LocalDateTime.now()));

		for (int j = 0; j < 100; j++)
		{
			System.out.println("\n-------------");
			long addition = j * 3600 + (j * 350000);
			boolean v = DateTimeUtils.isSameTimeInToleranceZoned(ts.getTime(), ts.getTime() + addition,
					ZoneId.of("Europe/Paris"), ZoneId.of("America/Chicago"), 3600);

			if (v)
			{
				System.out.println("===========--> j = " + j + " Same in tolerance=" + v + "addition = "
						+ (addition / 1000) + " secs tolerance in secs = " + 3600);
			}
			else
			{
				System.out.println("--> j = " + j + " Same in tolerance=" + v);
			}
		}
	}

	/**
	 * returns Month and day of month as 'JAN2'. Usually used to append to folder names,
	 * 
	 * @param currentDateTime
	 * @return
	 */
	public static String getShortDateLabel(LocalDateTime currentDateTime)
	{
		return currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth();
	}

	public static void checkJavaSqlDate()
	{

	}

	/**
	 * 
	 * @param ts1
	 * @param ts2
	 * @param toleranceInSeconds
	 * @return
	 */
	public static boolean isSameTimeInTolerance(Timestamp ts1, Timestamp ts2, long toleranceInSeconds)
	{
		long time1 = ts1.getHours() * 60 * 60 + ts1.getMinutes() * 60 + ts1.getSeconds();

		long time2 = ts2.getHours() * 60 * 60 + ts2.getMinutes() * 60 + ts2.getSeconds();

		if (Math.abs(time1 - time2) > toleranceInSeconds)
		{
			return false;
		}
		else
			return true;
	}

	/**
	 * 
	 * @param ts1
	 * @param ts2
	 * @param zoneId1
	 * @param zoneId2
	 * @param toleranceInSeconds
	 * @return
	 * @since 27 Feb 2018 SANITY CHECKED OK
	 */
	public static boolean isSameTimeInToleranceZoned(long tsInms1, long tsInms2, ZoneId zoneId1, ZoneId zoneId2,
			long toleranceInSeconds)
	{
		ZonedDateTime zonedDateTime1 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tsInms1), zoneId1);
		ZonedDateTime zonedDateTime2 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tsInms2), zoneId2);

		// System.out.println("zonedDateTime1 = " + zonedDateTime1);
		// System.out.println("zonedDateTime2 = " + zonedDateTime2);

		long time1 = zonedDateTime1.getHour() * 60 * 60 + zonedDateTime1.getMinute() * 60 + zonedDateTime1.getSecond();
		long time2 = zonedDateTime2.getHour() * 60 * 60 + zonedDateTime2.getMinute() * 60 + zonedDateTime2.getSecond();
		if (Math.abs(time1 - time2) > toleranceInSeconds)
		{
			return false;
		}
		else
			return true;
	}

	/**
	 * 
	 * @param ts1
	 * @param ts2
	 * @return
	 */
	public static long getTimeDiffInSecondsZoned(long tsInms1, long tsInms2, ZoneId zoneId1, ZoneId zoneId2)
	{
		ZonedDateTime zonedDateTime1 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tsInms1), zoneId1);
		ZonedDateTime zonedDateTime2 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tsInms2), zoneId2);

		// System.out.println("zonedDateTime1 = " + zonedDateTime1);
		// System.out.println("zonedDateTime2 = " + zonedDateTime2);

		long time1 = zonedDateTime1.getHour() * 60 * 60 + zonedDateTime1.getMinute() * 60 + zonedDateTime1.getSecond();
		long time2 = zonedDateTime2.getHour() * 60 * 60 + zonedDateTime2.getMinute() * 60 + zonedDateTime2.getSecond();

		return Math.abs(time1 - time2);
	}

	/**
	 * Returns the time in the day (as seconds past midnight) for the given timestamp.
	 * 
	 * @param tsInms1
	 * @param zoneId1
	 * @return
	 */
	public static long getTimeInDayInSecondsZoned(long tsInms1, ZoneId zoneId1)
	{
		if (zoneId1 == null)
		{
			PopUps.printTracedErrorMsg("Null zoneId1");
		}
		ZonedDateTime zonedDateTime1 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tsInms1), zoneId1);
		// return ts1.getHours() * 60 * 60 + ts1.getMinutes() * 60 + ts1.getSeconds();
		return zonedDateTime1.getHour() * 60 * 60 + zonedDateTime1.getMinute() * 60 + zonedDateTime1.getSecond();
	}

	/**
	 * 
	 * @param ts1
	 * @param ts2
	 * @return
	 */
	public static long getTimeDiffInSeconds(Timestamp ts1, Timestamp ts2)
	{
		long time1 = ts1.getHours() * 60 * 60 + ts1.getMinutes() * 60 + ts1.getSeconds();
		long time2 = ts2.getHours() * 60 * 60 + ts2.getMinutes() * 60 + ts2.getSeconds();

		return Math.abs(time1 - time2);
	}

	/**
	 * Returns the time in the day (as seconds past midnight) for the given timestamp.
	 * 
	 * @param ts1
	 * @return
	 */
	public static long getTimeInDayInSeconds(Timestamp ts1)
	{
		return ts1.getHours() * 60 * 60 + ts1.getMinutes() * 60 + ts1.getSeconds();
	}

	public static String getWeekDayFromDateString(String date) // dd-mm-yyyy
	{
		String weekDay = null;

		String[] splitted = date.split("-");
		Date date1 = new Date(Integer.valueOf(splitted[2]) - 1900, Integer.valueOf(splitted[1]) - 1,
				Integer.valueOf(splitted[0]));

		int weekDayInt = date1.getDay();
		//
		// switch(weekDayInt)
		// {
		// case 0: weekDay="Sunday"; break;
		// case 1: weekDay="Monday";break;
		// case 2: weekDay="Tuesday";break;
		// case 3: weekDay="Wednesday";break;
		// case 4: weekDay= "Thursday";break;
		// case 5: weekDay = "Friday";break;
		// case 6: weekDay ="Saturday";break;
		// default: weekDay = "not found"; break;
		//
		// }

		return getWeekDayFromWeekDayInt(weekDayInt);
	}

	public static String getWeekDayFromWeekDayInt(int weekDayInt)
	{
		String weekDay = null;

		switch (weekDayInt)
		{
			case 0:
				weekDay = "Sunday";
				break;
			case 1:
				weekDay = "Monday";
				break;
			case 2:
				weekDay = "Tuesday";
				break;
			case 3:
				weekDay = "Wednesday";
				break;
			case 4:
				weekDay = "Thursday";
				break;
			case 5:
				weekDay = "Friday";
				break;
			case 6:
				weekDay = "Saturday";
				break;
			default:
				weekDay = "not found";
				break;

		}

		return weekDay;
	}

	@SuppressWarnings("deprecation")
	public static Timestamp getTimestamp(String timeString, String dateString)
	{
		Timestamp timestamp;

		String[] splittedTime = RegexUtils.patternColon.split(timeString);// timeString.split(":");
		String[] splittedDate = RegexUtils.patternHyphen.split(dateString);// dateString.split("-");

		timestamp = new Timestamp(Integer.parseInt(splittedDate[0]) - 1900, // year
				Integer.parseInt(splittedDate[1]) - 1, // month
				Integer.parseInt(splittedDate[2]), // day
				Integer.parseInt(splittedTime[0]), // hours
				Integer.parseInt(splittedTime[1]), // minutes
				Integer.parseInt(splittedTime[2]), // seconds
				0); // nanoseconds

		return timestamp;
	}

	@SuppressWarnings("deprecation")
	public static long getTimestampAsLongms(String timeString, String dateString)
	{
		Timestamp timestamp;

		String[] splittedTime = RegexUtils.patternColon.split(timeString);// timeString.split(":");
		String[] splittedDate = RegexUtils.patternHyphen.split(dateString);// dateString.split("-");

		timestamp = new Timestamp(Integer.parseInt(splittedDate[0]) - 1900, // year
				Integer.parseInt(splittedDate[1]) - 1, // month
				Integer.parseInt(splittedDate[2]), // day
				Integer.parseInt(splittedTime[0]), // hours
				Integer.parseInt(splittedTime[1]), // minutes
				Integer.parseInt(splittedTime[2]), // seconds
				0); // nanoseconds

		return timestamp.getTime();
	}

	/**
	 * 
	 * @param timestampString
	 *            in ISO 8601 format
	 * @return
	 */
	public static Timestamp getTimestampFromISOString(String timestampString)// , String timeString)
	{
		Timestamp timeStamp = null;
		try
		{
			Instant instant = Instant.parse(timestampString);
			timeStamp = Timestamp.from(instant);

			// System.out.println("Hours= " + timeStamp.getHours() + "Mins= " + timeStamp.getMinutes() + "Sec=" +
			// timeStamp.getSeconds());
		}
		catch (Exception e)
		{
			System.out.println("Exception " + e + " thrown for getting timestamp from " + timestampString);
			e.printStackTrace();
		}
		return timeStamp;
	}

	public static Timestamp getIncrementedTimestamp(Timestamp initialTimestamp, int incrementInSeconds)
	{
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(initialTimestamp.getTime());
		cal.add(Calendar.SECOND, incrementInSeconds);

		Timestamp resultTimestamp = new Timestamp(cal.getTime().getTime());

		// System.out.println(" adding seconds to timestamp: "+initialTimestamp+"+"+incrementInSeconds+"seconds
		// ="+resultTimestamp);
		return resultTimestamp;
	}

	public static String getTimeString(Timestamp timestamp)
	{
		// System.out.println(" Inside getTimeString: timestamp="+timestamp+" extracted
		// time="+timestamp.getHours()+":"+timestamp.getMinutes()+":"+timestamp.getSeconds());
		return (timestamp.getHours() + ":" + timestamp.getMinutes() + ":" + timestamp.getSeconds());
	}

	public static String getDateString(Timestamp timestamp)
	{
		int year = timestamp.getYear() + 1900;
		int month = timestamp.getMonth() + 1;
		return (year + "-" + month + "-" + timestamp.getDate());
	}

	/**
	 * 
	 * @param timestamp
	 * @return
	 */
	public static Date getDate(Timestamp timestamp)
	{
		return new Date(timestamp.getTime());
	}

	/**
	 * <font color = green>Okay. java.sql.Date with no hidden time</font>
	 * 
	 * @param dateString
	 * @param separator
	 * @return
	 */
	public static Date getDateFromDDMMYYYY(String dateString, Pattern separator)
	{
		String[] splittedDate = separator.split(dateString);// dd/mm/yyyy
		return new Date(Integer.parseInt(splittedDate[2]) - 1900, Integer.parseInt(splittedDate[1]) - 1,
				Integer.parseInt(splittedDate[0]));
	}

	// /**
	// * INCORRECT
	// * @param timestamp
	// * @return
	// */
	// public static Date getDateSafely(Timestamp t)
	// {
	// Timestamp ts = new Timestamp(t.getYear(), t.getMonth(), t.getDay(), 0, 0, 0, 0);
	// return new Date(ts.getTime());
	// }

	/**
	 * 
	 * @param timestamp
	 * @param tz
	 * @return
	 */
	public static LocalDate getLocalDate(Timestamp timestamp, TimeZone tz)
	{
		TimeZone.setDefault(tz);
		return timestamp.toLocalDateTime().toLocalDate();
	}

	/**
	 * 
	 * @param ts1
	 * @param ts2
	 * @return
	 */
	public static boolean isSameDate(Timestamp ts1, Timestamp ts2)
	{
		if (ts1.getYear() != ts2.getYear()) return false;
		if (ts1.getMonth() != ts2.getMonth()) return false;
		if (ts1.getDate() != ts2.getDate())
			return false;
		else
			return true;

	}

	/**
	 * 
	 * difDays + difMonths * 30 + difYears * 365;
	 * 
	 * @param date1
	 *            "2009-03-15"
	 * @param date2
	 *            "2009-03-15"
	 * @return
	 */
	public static long getRoughDiffOfDates(String date1, String date2)
	{
		long roughNumOfDays = -99999;

		LocalDate ld = LocalDate.parse(date1);
		LocalDate ld2 = LocalDate.parse(date2);

		Period dif = Period.between(ld, ld2);

		long difDays = (dif.get(ChronoUnit.DAYS));
		long difMonths = (dif.get(ChronoUnit.MONTHS));
		long difYears = (dif.get(ChronoUnit.YEARS));

		roughNumOfDays = difDays + difMonths * 30 + difYears * 365;

		return roughNumOfDays;
	}

	public static int getTimeUnitInSeconds()
	{
		return 60;// 30 mins works 60*30;//1 hr works 60*60;
	}

	/**
	 * 
	 * 
	 * @param dataEntryForAnImage
	 *            must be of the form '<Timestamp in milliseconds as String>||ImageName||ActivityName'
	 * @return timestamp extracted
	 */
	public static Timestamp getTimestampFromDataEntry(String dataEntryForAnImage)
	{
		Timestamp timeStamp = null;
		// System.out.println("data entry="+dataEntryForAnImage);
		String[] splitted = dataEntryForAnImage.split(Pattern.quote("||"));

		// System.out.println("length of splitted is "+splitted.size());
		// System.out.println("splitted 0 is "+splitted[0]);
		timeStamp = new Timestamp(Long.valueOf(splitted[0]).longValue());

		return timeStamp;
	}

	/**
	 * Get Timestamp from image name
	 * 
	 * @param imageName
	 * @return
	 */
	/*
	 * public static Timestamp getTimestamp(String imageName) { Timestamp timeStamp=null; int year=0, month=0, day=0,
	 * hours=0, minutes=0, seconds=0;
	 * 
	 * //Pattern imageNamePattern= Pattern.compile("((.*)(_)(.*)(_)("); StringTokenizer tokenizer= new
	 * StringTokenizer(imageName,"_"); int count=0;
	 * 
	 * try { while(tokenizer.hasMoreTokens()) { String token=tokenizer.nextToken();
	 * //System.out.println("token ="+token+" count="+count);
	 * 
	 * if(count == 2) { year=Integer.parseInt(token.substring(0,4)); month=Integer.parseInt(token.substring(4,6));
	 * day=Integer.parseInt(token.substring(6,8)); }
	 * 
	 * if(count == 3) { hours=Integer.parseInt(token.substring(0,2)); minutes=Integer.parseInt(token.substring(2,4));
	 * seconds=Integer.parseInt(token.substring(4,6)); } count++;
	 * 
	 * }
	 * 
	 * //System.out.println(year+ " "+month+" "+day+" "+hours+" "+minutes+" "+seconds); timeStamp=new
	 * Timestamp(year-1900,month-1,day,hours,minutes, seconds,0); /// CHECK it out
	 * //System.out.println("Time stamp"+timeStamp); } catch(Exception e) {
	 * System.out.println("Exception "+e+" thrown for getting timestamo from "+ imageName); e.printStackTrace(); }
	 * return timeStamp; }
	 */

	public static Timestamp getTimestamp(String imageName)
	{
		Timestamp timeStamp = null;
		int year = 0, month = 0, day = 0, hours = 0, minutes = 0, seconds = 0;

		// Pattern imageNamePattern= Pattern.compile("((.*)(_)(.*)(_)(");
		String[] splitted = imageName.split("_");
		int count = 0;

		try
		{

			String dateString = splitted[splitted.length - 2];

			year = Integer.parseInt(dateString.substring(0, 4));
			month = Integer.parseInt(dateString.substring(4, 6));
			day = Integer.parseInt(dateString.substring(6, 8));

			String timeString = splitted[splitted.length - 1];

			hours = Integer.parseInt(timeString.substring(0, 2));
			minutes = Integer.parseInt(timeString.substring(2, 4));
			seconds = Integer.parseInt(timeString.substring(4, 6));

			// System.out.println(year+ " "+month+" "+day+" "+hours+" "+minutes+" "+seconds);
			timeStamp = new Timestamp(year - 1900, month - 1, day, hours, minutes, seconds, 0); // / CHECK it out
			// System.out.println("Time stamp"+timeStamp);
		}
		catch (Exception e)
		{
			System.out.println("Exception " + e + " thrown for getting timestamo from " + imageName);
			e.printStackTrace();
		}
		return timeStamp;
	}

	/**
	 * 
	 * @param timestampString
	 * @return
	 */
	public static Timestamp getTimestampLastFMData(String timestampString)// , String timeString)
	{
		Timestamp timeStamp = null;
		try
		{
			Instant instant = Instant.parse(timestampString);
			timeStamp = Timestamp.from(instant);

			// System.out.println("Hours= " + timeStamp.getHours() + "Mins= " + timeStamp.getMinutes() + "Sec=" +
			// timeStamp.getSeconds());
		}
		catch (Exception e)
		{
			System.out.println("Exception " + e + " thrown for getting timestamp from " + timestampString);
			e.printStackTrace();
		}
		return timeStamp;
	}

	// 2007-08-04,03:30:32
	// 0123456789 012345678
	public static Timestamp getTimestampGeoData(String dateString, String timeString)
	{
		Timestamp timeStamp = null;
		int year = 0, month = 0, day = 0, hours = 0, minutes = 0, seconds = 0;

		try
		{
			year = Integer.parseInt(dateString.substring(0, 4));
			month = Integer.parseInt(dateString.substring(5, 7));
			day = Integer.parseInt(dateString.substring(8, 10));

			hours = Integer.parseInt(timeString.substring(0, 2));
			minutes = Integer.parseInt(timeString.substring(3, 5));
			seconds = Integer.parseInt(timeString.substring(6, 8));

			// System.out.println(year+ " "+month+" "+day+" "+hours+" "+minutes+" "+seconds);
			timeStamp = new Timestamp(year - 1900, month - 1, day, hours, minutes, seconds, 0); // / CHECK it out

			if (hours != timeStamp.getHours())
			{
				System.err.println("Alert TS1 in getTimestampGeoData: hours not equal:\nReceived dateString= "
						+ dateString + " timeString= " + timeString + "\n\tParsed hour:" + hours
						+ "\n\tCreated timestamp (toGMTStrng()): " + timeStamp.toGMTString()
						+ "\n\tCreated timestamp (toStrng()): " + timeStamp.toString());
			}
			// System.out.println("Time stamp"+timeStamp);
		}
		catch (Exception e)
		{
			System.out
					.println("Exception " + e + " thrown for getting timestamp from " + dateString + " " + timeString);
			e.printStackTrace();
		}
		return timeStamp;
	}

	// 2007-08-04,03:30:32
	// 0123456789 012345678
	public static Timestamp getTimestampGeoDataBetter(String dateString, String timeString)
	{
		Timestamp timeStamp = null;
		int year = 0, month = 0, day = 0, hours = 0, minutes = 0, seconds = 0;

		try
		{
			year = Integer.parseInt(dateString.substring(0, 4));
			month = Integer.parseInt(dateString.substring(5, 7));
			day = Integer.parseInt(dateString.substring(8, 10));

			hours = Integer.parseInt(timeString.substring(0, 2));
			minutes = Integer.parseInt(timeString.substring(3, 5));
			seconds = Integer.parseInt(timeString.substring(6, 8));

			// System.out.println(year+ " "+month+" "+day+" "+hours+" "+minutes+" "+seconds);
			timeStamp = new Timestamp(year - 1900, month - 1, day, hours, minutes, seconds, 0); // / CHECK it out

			if (hours != timeStamp.getHours())
			{
				System.err.println("Alert");// TS1 in getTimestampGeoData: hours not equal:\nReceived dateString= " +
											// dateString
				// + " timeString= " + timeString + "\n\tParsed hour:" + hours + "\n\tCreated timestamp (toGMTStrng()):
				// "
				// + timeStamp.toGMTString() + "\n\tCreated timestamp (toStrng()): " + timeStamp.toString());
			}
			// System.out.println("Time stamp"+timeStamp);
		}
		catch (Exception e)
		{
			System.out
					.println("Exception " + e + " thrown for getting timestamp from " + dateString + " " + timeString);
			e.printStackTrace();
		}
		return timeStamp;
	}

	public static String getTimeCategoryOfDay(int hour)
	{
		String timeCategory = null;

		if (hour >= 0 && hour < 12)
		{
			timeCategory = Evaluation.timeCategories[1];
		}

		else if (hour >= 12 && hour < 16)
		{
			timeCategory = Evaluation.timeCategories[2];
		}

		else if (hour >= 16 && hour <= 23)
		{
			timeCategory = Evaluation.timeCategories[3];
		}

		return timeCategory;
	}

}
