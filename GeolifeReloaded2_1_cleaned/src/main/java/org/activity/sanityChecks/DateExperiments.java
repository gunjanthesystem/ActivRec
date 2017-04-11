package org.activity.sanityChecks;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;

import org.activity.util.PerformanceAnalytics;

public class DateExperiments
{

	public static void main(String args[])
	{

		StringBuilder aLongStringsb = new StringBuilder();

		for (int i = 0; i < 500; i++)
		{
			aLongStringsb.append("aLongString__");
		}

		int size = 10000000;
		String aLongString = aLongStringsb.toString();

		// createSqlDateMap(aLongString, size);
		createLocalDateMap(aLongString, size);
		// createUtilDateMap(aLongString, size);

	}

	public static void createSqlDateMap(String aLongString, int numOfElements)
	{
		System.out.println("Inside createSqlDateMap");
		System.out.println("Before:" + PerformanceAnalytics.getHeapInformation());
		double d1 = PerformanceAnalytics.getUsedMemoryInMB();
		LinkedHashMap<Date, String> objSqlDate = new LinkedHashMap<>();
		long sd1 = 50000;
		for (int i = 0; i < numOfElements; i++)
		{
			objSqlDate.put(new Date(sd1 + 1000 * i), aLongString);
		}
		System.out.println("used memory  = " + (PerformanceAnalytics.getUsedMemoryInMB() - d1) + " MB");
		// System.out.println("Num of elements = " + objSqlDate.size());
		System.out.println("After" + PerformanceAnalytics.getHeapInformation());

	}

	public static void createUtilDateMap(String aLongString, int numOfElements)
	{
		System.out.println("Inside createUtilDateMap");
		System.out.println("Before:" + PerformanceAnalytics.getHeapInformation());
		double d1 = PerformanceAnalytics.getUsedMemoryInMB();
		LinkedHashMap<java.util.Date, String> objSqlDate = new LinkedHashMap<>();

		long sd1 = 50000;
		for (int i = 0; i < numOfElements; i++)
		{
			objSqlDate.put(new Date(sd1 + 1000 * i), aLongString);
		}
		System.out.println("used memory  = " + (PerformanceAnalytics.getUsedMemoryInMB() - d1) + " MB");
		// System.out.println("Num of elements = " + objSqlDate.size());
		System.out.println("After" + PerformanceAnalytics.getHeapInformation());
	}

	public static void createLocalDateMap(String aLongString, int numOfElements)
	{
		System.out.println("Inside createLocalDateMap");
		System.out.println("Before:" + PerformanceAnalytics.getHeapInformation());
		double d1 = PerformanceAnalytics.getUsedMemoryInMB();
		LinkedHashMap<java.time.LocalDate, String> objSqlDate = new LinkedHashMap<>();
		LocalDate ld1 = LocalDate.now();

		for (int i = 0; i < numOfElements; i++)
		{
			objSqlDate.put(ld1.plusDays(i), aLongString);
		}
		System.out.println("used memory  = " + (PerformanceAnalytics.getUsedMemoryInMB() - d1) + " MB");
		// System.out.println("Num of elements = " + objSqlDate.size());
		System.out.println("After" + PerformanceAnalytics.getHeapInformation());
	}
}
