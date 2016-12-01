package org.activity.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.activity.ui.PopUps;
import org.activity.util.CSVUtils;

public class ReadingFromFile
{
	
	/**
	 * 
	 * @param absFileName
	 * @return
	 */
	public static long getNumOfLines(String absFileName)
	{
		long numLines = 0;
		long ct1 = System.currentTimeMillis();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(absFileName));
			
			while (br.readLine() != null)
			{
				// if ((lines % 1000) == 0)
				// {
				// System.out.println(".");
				// }
				numLines++;
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long ct2 = System.currentTimeMillis();
		System.out.println("getNumOfLines count " + numLines + " lines in :" + (ct2 - ct1) / 1000 + " secs");
		return numLines;
	}
	
	/**
	 * reads a column from a csv file and returns it as an arraylist of String
	 * 
	 * @param absolutePath
	 *            path of fileNameToRead
	 * @param delimiter
	 * @param columnIndex
	 *            starts from 0
	 * @param hasHeader
	 * @return
	 */
	public static List<String> oneColumnReaderString(String fileNameToRead, String delimiter, int columnIndex, boolean hasHeader)
	{
		
		List<String> raw = new ArrayList<String>();
		String line = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			
			int count;
			
			if (hasHeader)
			{
				raw = br.lines().skip(1).map((String s) -> (s.split(Pattern.quote(delimiter))[columnIndex])).collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> (s.split(Pattern.quote(delimiter))[columnIndex])).collect(Collectors.toList());
			}
			System.out.println("Size of raw =" + raw.size());
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return raw;
	}
	
	/**
	 * reads a column from a csv file and returns it as an arraylist of long
	 * 
	 * @param absolutePath
	 *            path of fileNameToRead
	 * @param delimiter
	 * @param columnIndex
	 *            starts from o
	 * @param hasHeader
	 * @return
	 */
	public static List<Long> oneColumnReaderLong(String fileNameToRead, String delimiter, int columnIndex, boolean hasHeader)
	{
		
		List<Long> raw = new ArrayList<Long>();
		String line = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			
			int count;
			
			if (hasHeader)
			{
				raw = br.lines().skip(1).map((String s) -> Long.parseLong(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> Long.parseLong(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			System.out.println("Size of raw =" + raw.size());
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return raw;
	}
	
	/**
	 * reads a column from a csv file and returns it as an arraylist of Double
	 * 
	 * @param absolutePath
	 *            path of fileNameToRead
	 * @param delimiter
	 * @param columnIndex
	 *            starts from o
	 * @param hasHeader
	 * @return
	 */
	public static List<Double> oneColumnReaderDouble(String fileNameToRead, String delimiter, int columnIndex, boolean hasHeader)
	{
		
		List<Double> raw = new ArrayList<Double>();
		String line = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));// Constant.getCommonPath() +
			
			int count;
			
			if (hasHeader)
			{
				raw = br.lines().skip(1).map((String s) -> Double.parseDouble(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> Double.parseDouble(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			System.out.println("Size of raw =" + raw.size());
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return raw;
	}
	
	/**
	 * reads two columns from a csv file and returns it as an arraylist of String where each string is a contactenation of the strings (separated by a single space) from each row
	 * of the two columns
	 * 
	 * @param absolutePath
	 *            path of fileNameToRead
	 * @param delimiter
	 * @param columnIndex
	 *            starts from 0
	 * @param hasHeader
	 * @return
	 */
	public static List<String> twoColumnReaderString(String fileNameToRead, String delimiter, int column1Index, int column2Index,
			boolean hasHeader)
	{
		
		List<String> raw = new ArrayList<String>();
		String line = "";
		String dlimPatrn = Pattern.quote(delimiter);
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			
			int count;
			
			if (hasHeader)
			{
				raw = br.lines().skip(1).map((String s) -> (s.split(dlimPatrn)[column1Index] + " " + s.split(dlimPatrn)[column2Index]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> (s.split(dlimPatrn)[column1Index] + " " + s.split(dlimPatrn)[column2Index]))
						.collect(Collectors.toList());
			}
			// System.out.println("Size of raw =" + raw.size());
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return raw;
	}
	
	// public static String fun1(String s, int column1Index, int column2Index)
	// {
	// String res = null;
	// res = s.split(",")[column1Index] + s.split(",")[column2Index];
	// return res;
	// }
	/**
	 * For Test Purposes
	 * 
	 * @param args
	 */
	public static void main(String args[])
	{
		// String fileName = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Jan27Daywise/recommPointsWithNoCandidates.csv";
		// List<String> list1 = twoColumnReaderString(fileName, ",", 0, 3, true);
		//
		// list1.stream().forEach(System.out::println);
		
		// String fileName =
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb19/Clustering0MUTil30/Iteration1AllMRR.csv";
		//
		// String val = getCellValueFromCSVFile(5, 3, fileName);
		// System.out.println("Read value = " + val + " as double = " + Double.valueOf(val));
		
		String commonPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/TrajectorySpace/June8AllJavaSer/";
		ArrayList<String> fileNamesToConcactenate = new ArrayList<String>();
		int userIDStart = 0;
		int userIDEnd = 181;
		
		for (int i = userIDStart; i <= userIDEnd; i++)
		{
			String userID = String.format("%03d", i);
			String fileName = commonPath + userID + "StayPoints.csv";
			fileNamesToConcactenate.add(fileName);
		}
		CSVUtils.contactenateCSVFiles(fileNamesToConcactenate, true,
				"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/TrajectorySpace/June10ContactednateStayPoints/AllStayPoints.csv");
	}
	
	/**
	 * Return a list of black listed recommendation time with each entry of the form <rawUserID timestampAsString>. Currently the blacklisted RTs are the RTs not used in daywise
	 * matching in lieu of no candidate timelines for them during daywaise matching
	 * 
	 * @return
	 */
	public static List<String> getBlackListedRTs(String databaseName)
	{
		List<String> res = null;
		if (databaseName.equals("geolife1") == false)
		{
			PopUps.showException(new Exception(
					"Error in getBlackListedRTs(): this is mean only for geolife database while the current databse is " + databaseName),
					"org.activity.util.ReadingFromFile.getBlackListedRTs(String)");
		}
		
		else
		{
			String fileName = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Jan27Daywise/recommPointsWithNoCandidates.csv";
			res = twoColumnReaderString(fileName, ",", 0, 3, true);
			
			System.out.println("The blacklisted RTs are: ");
			res.stream().forEach(System.out::println);
		}
		
		return res;
	}
}
