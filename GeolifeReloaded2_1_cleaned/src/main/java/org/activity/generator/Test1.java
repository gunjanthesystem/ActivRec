package org.activity.generator;

import java.sql.Timestamp;
import java.util.TimeZone;

public class Test1
{
	
	public static void main(String[] args)
	{
		System.out.println("running");
		
		timestampErrorDiagnosis();
	}
	
	/**
	 * To check the timestamp error which is leading to merge issue in Geolife data
	 */
	public static void timestampErrorDiagnosis()
	{
		// Timestamp ts;
		// // Calendar cal = new GregorianCalendar();
		// System.out.println(TimeZone.getDefault());
		//
		// // String timeZones[] = TimeZone.getAvailableIDs();
		// // for (String tzs : timeZones)
		// // {
		// // System.out.println(tzs);
		// // }
		// // cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		// // System.out.println(cal.getTimeZone());
		// //
		// TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		System.out.println("Inside test1" + TimeZone.getDefault());
		try
		{
			// System.out.println(Calendar.)
			// timestampDiagnosis2("2009-03-28", "01:32:13");
			// timestampDiagnosis2("2009-03-29", "01:32:13");
			// timestampDiagnosis2("2009-03-31", "01:32:13");
			// timestampDiagnosis2("2009-04-01", "01:32:13");
			//
			timestampDiagnosis2("2009-01-01", "01:00:00");
			timestampDiagnosis2("2009-07-01", "01:00:00");
			timestampDiagnosis2("2009-03-28", "01:00:00");
			timestampDiagnosis2("2009-03-29", "01:00:00");
			timestampDiagnosis2("2009-03-31", "01:00:00");
			timestampDiagnosis2("2009-04-01", "01:00:00");
			//
			// timestampDiagnosis2("2009-01-01", "00:00:00");
			// timestampDiagnosis2("2009-07-01", "00:00:00");
			// timestampDiagnosis2("2009-03-28", "01:00:00");
			// timestampDiagnosis2("2009-03-29", "01:00:00");
			// timestampDiagnosis2("2009-03-31", "01:00:00");
			// timestampDiagnosis2("2009-04-01", "01:00:00");
			//
			// timestampDiagnosis2("2009-01-01", "01:00:00");
			// timestampDiagnosis2("2009-07-01", "01:00:00");
			//
			// timestampDiagnosis2("2014-03-28", "01:00:00");
			// timestampDiagnosis2("2014-03-30", "01:00:00");
			// timestampDiagnosis2("2014-03-31", "01:00:00");
			// timestampDiagnosis2("2014-04-01", "01:00:00");
			//
			// timestampDiagnosis2("2014-01-01", "01:00:00");
			// timestampDiagnosis2("2014-07-01", "01:00:00");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void timestampDiagnosis2(String tsStringDate, String tsStringTime)
	{
		Timestamp ts = DatabaseCreatorGeolifeQuicker.getTimestampGeoData(tsStringDate, tsStringTime);
		System.out.println("\n================================");
		System.out.println("Timestamp string as input: = " + tsStringDate + "  " + tsStringTime);
		System.out.println("Timestamp generated (Timestamp.toGMTString()) = " + ts.toGMTString());
		System.out.println("Timestamp generated (Timestamp.toString()) = " + ts.toString());
		System.out.println("Timestamp generated (Timestamp.toHours()) = " + ts.getHours());
		System.out.println("Timestamp generated (Timestamp.getTimezoneOffset()) = " + ts.getTimezoneOffset());
		System.out.println("Timestamp generated (Timestamp.getTime()) = " + ts.getTime());
		System.out.println("Converted to Instant = " + ts.toInstant().toString());
		
		System.out.println("================================ \n");// + ts.getTimezoneOffset());
	}
}

// System.out.println("================================ \n");// + ts.getTimezoneOffset());
// ts = DatabaseCreatorGeolifeQuicker.getTimestampGeoData("2009-03-28", "01:32:13");
// System.out.println("Timestamp generated (Timestamp.toGMTString()) = " + ts.toGMTString());
// System.out.println("Timestamp generated (Timestamp.toString()) = " + ts.toString());
// System.out.println("Timestamp generated (Timestamp.toHours()) = " + ts.getHours());
// System.out.println("Timestamp generated (Timestamp.getTimezoneOffset()) = " + ts.getTimezoneOffset());
// System.out.println("Timestamp generated (Timestamp.getTime()) = " + ts.getTime());
// System.out.println("================================ \n");// + ts.getTimezoneOffset());
// ts = DatabaseCreatorGeolifeQuicker.getTimestampGeoData("2009-03-29", "01:32:13");
// System.out.println("Timestamp generated (Timestamp.toGMTString()) = " + ts.toGMTString());
// System.out.println("Timestamp generated (Timestamp.toString()) = " + ts.toString());
// System.out.println("Timestamp generated (Timestamp.toHours()) = " + ts.getHours());
// System.out.println("Timestamp generated (Timestamp.getTimezoneOffset()) = " + ts.getTimezoneOffset());
// System.out.println("Timestamp generated (Timestamp.getTime()) = " + ts.getTime());
// System.out.println("================================ \n");// + ts.getTimezoneOffset());
// ts = DatabaseCreatorGeolifeQuicker.getTimestampGeoData("2009-03-31", "01:32:13");
// System.out.println("Timestamp generated (Timestamp.toGMTString()) = " + ts.toGMTString());
// System.out.println("Timestamp generated (Timestamp.toString()) = " + ts.toString());
// System.out.println("Timestamp generated (Timestamp.toHours()) = " + ts.getHours());
// System.out.println("Timestamp generated (Timestamp.getTimezoneOffset()) = " + ts.getTimezoneOffset());
// System.out.println("Timestamp generated (Timestamp.getTime()) = " + ts.getTime());
// System.out.println("================================ \n");// + ts.getTimezoneOffset());
// ts = DatabaseCreatorGeolifeQuicker.getTimestampGeoData("2009-04-01", "01:32:13");
// System.out.println("Timestamp generated (Timestamp.toGMTString()) = " + ts.toGMTString());
// System.out.println("Timestamp generated (Timestamp.toString()) = " + ts.toString());
// System.out.println("Timestamp generated (Timestamp.toHours()) = " + ts.getHours());
// System.out.println("Timestamp generated (Timestamp.getTimezoneOffset()) = " + ts.getTimezoneOffset());
// System.out.println("Timestamp generated (Timestamp.getTime()) = " + ts.getTime());
// System.out.println("================================ \n");// + ts.getTimezoneOffset());

