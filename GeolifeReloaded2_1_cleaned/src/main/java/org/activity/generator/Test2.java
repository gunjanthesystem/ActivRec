package org.activity.generator;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

public class Test2
{
	public static void main(String args[])
	{
		
		// System.out.println(TimeZone.getDefault());
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		// System.out.println(TimeZone.getDefault());
		// Test1.timestampErrorDiagnosis();
		
		Timestamp ts = org.activity.generator.DatabaseCreatorGowallaQuicker.getTimestampLastFMData("2010-05-19T03:35:19Z");
		System.out.println("kk");
		
		System.out.println(ts.toLocalDateTime().toLocalDate().toString());
		
		LocalDate ld = LocalDate.parse("2009-03-15");
		LocalDate ld2 = LocalDate.parse("2010-03-17");
		
		System.out.println(ld.toString());
		System.out.println(ld2.toString());
		
		Period dif = Period.between(ld, ld2);
		// Duration dif = Duration.between(ld, ld2);
		
		long difDays = (dif.get(ChronoUnit.DAYS));
		long difMonths = (dif.get(ChronoUnit.MONTHS));
		long difYears = (dif.get(ChronoUnit.YEARS));
		// System.out.println(dif);
		
		System.out.println(dif.get(ChronoUnit.DAYS));
		System.out.println(dif.get(ChronoUnit.MONTHS));
		System.out.println(dif.get(ChronoUnit.YEARS));
		
		long roughNumOfDays = difDays + difMonths * 30 + difYears * 365;
		
		System.out.println(roughNumOfDays);
		// System.out.println(dif.get(ChronoUnit.));
		
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
}
