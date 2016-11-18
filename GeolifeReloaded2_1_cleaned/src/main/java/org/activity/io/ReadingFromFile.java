package org.activity.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.activity.ui.PopUps;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class ReadingFromFile
{
	
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
		contactenateCSVFiles(fileNamesToConcactenate, true,
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
	
	/**
	 * reads the given csv file into a list of CSV record
	 * 
	 * @param absoluteFileName
	 * @return
	 */
	/**
	 * 
	 * @param absoluteFileName
	 * @param rowNum
	 *            starts from 1
	 * @return
	 */
	public static CSVRecord getRowFromCSVFile(String absoluteFileName, int rowNum)
	{
		CSVRecord row = null;
		// try
		{
			row = getCSVRecords(absoluteFileName).get(rowNum - 1);
			
		}
		// catch (FileNotFoundException e)
		// {
		// e.printStackTrace();
		// }
		// catch (IOException e)
		// {
		// e.printStackTrace();
		// }
		
		// System.out.println(list.size() + " records read from " + absoluteFileName);
		return row;
	}
	
	/**
	 * reads the given csv file into a list of CSV record
	 * 
	 * @param absoluteFileName
	 * @return
	 */
	public static List<CSVRecord> getCSVRecords(String absoluteFileName)
	{
		List<CSVRecord> list = null;
		try
		{
			Reader in = new FileReader(absoluteFileName);
			CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT);
			
			list = parser.getRecords();
			
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		System.out.println(list.size() + " records read from " + absoluteFileName);
		return list;
	}
	
	/**
	 * reads the given csv file into a list of CSV record
	 * 
	 * @param absoluteFileName
	 * @return
	 */
	public static List<CSVRecord> getCSVRecords(String absoluteFileName, char delimiter)
	{
		List<CSVRecord> list = null;
		try
		{
			// CSVFormat csvFileFormat = CSVFormat.DEFAULT.withQuote(null).withDelimiter(delimiter);
			
			Reader in = new FileReader(absoluteFileName);
			CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withDelimiter(delimiter).withQuote(null));
			
			list = parser.getRecords();
			
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		System.out.println(list.size() + " records read from " + absoluteFileName);
		return list;
	}
	
	/**
	 * 
	 * @param listOfAbsFileNames
	 * @param hasColumnHeader
	 *            to make sure columnHeadersAreNotRepeated
	 */
	public static void contactenateCSVFiles(ArrayList<String> listOfAbsFileNames, boolean hasColumnHeader, String absfileToWrite)
	{
		int countOfFiles = 0, countOfTotalLines = 0;
		try
		{
			for (String fileToRead : listOfAbsFileNames)
			{
				countOfFiles += 1;
				List<CSVRecord> csvRecords = getCSVRecords(fileToRead);
				
				// System.out.println("read records from " + fileToRead + " are :");
				
				BufferedWriter bw = WritingToFile.getBufferedWriterForExistingFile(absfileToWrite);
				CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT);
				
				int countOfLines = 0;
				for (CSVRecord r : csvRecords)
				{
					countOfLines += 1;
					
					if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for non-first files
					{
						continue;
					}
					// System.out.println(r.toString());
					printer.printRecord(r);
					
				}
				System.out.println(countOfLines + " lines read for this user");
				countOfTotalLines += countOfLines;
				
				printer.close();
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param listOfAbsFileNames
	 * @param hasColumnHeader
	 *            to make sure columnHeadersAreNotRepeated
	 */
	public static void concatenateCSVFiles(ArrayList<String> listOfAbsFileNames, boolean hasColumnHeader, String absfileToWrite,
			char delimiter)
	{
		int countOfFiles = 0, countOfTotalLines = 0;
		try
		{
			for (String fileToRead : listOfAbsFileNames)
			{
				countOfFiles += 1;
				List<CSVRecord> csvRecords = getCSVRecords(fileToRead, delimiter);
				
				// System.out.println("read records from " + fileToRead + " are :");
				
				BufferedWriter bw = WritingToFile.getBufferedWriterForExistingFile(absfileToWrite);
				CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT.withDelimiter(delimiter).withQuote(null));
				
				int countOfLines = 0;
				for (CSVRecord r : csvRecords)
				{
					countOfLines += 1;
					
					if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for non-first files
					{
						continue;
					}
					// System.out.println(r.toString());
					printer.printRecord(r);
					
				}
				System.out.println(countOfLines + " lines read for this user");
				countOfTotalLines += countOfLines;
				
				printer.close();
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param absFileToSplit
	 * @param hasColumnHeader
	 * @param delimiter
	 * @param columnNumForSplit
	 * @return
	 */
	public static ArrayList<String> splitCSVFilesByColumnValueNoHeader(String absFileToSplit, char delimiter, int columnNumForSplit,
			String pathToWrite)
	{
		int countOfFiles = 0, countOfTotalLines = 0;
		int splitID = 1;
		int countOfLines = 0;
		BufferedWriter bw;
		CSVPrinter printer;
		StringBuffer stringBuffer = new StringBuffer();
		String currentVal = "", prevVal = "";
		String headerLine = "";
		
		try
		{
			List<CSVRecord> csvRecords = getCSVRecords(absFileToSplit, delimiter);
			countOfTotalLines = csvRecords.size();
			System.out.println("File to split has " + countOfTotalLines + " lines");
			printer = new CSVPrinter(stringBuffer, CSVFormat.DEFAULT.withDelimiter(delimiter).withQuote(null));
			
			for (CSVRecord rec : csvRecords)
			{
				countOfLines += 1;
				
				// System.out.println(stringBuffer.toString());
				currentVal = rec.get(columnNumForSplit);
				// System.out.println(stringBuffer.toString());
				
				System.out.println(countOfLines + " " + "currentVal= " + currentVal + " " + "prevVal= " + prevVal);
				
				if ((prevVal.length() == 0 || currentVal.equals(prevVal)) && countOfLines != countOfTotalLines)
				{
					System.out.println("if-->");
					printer.printRecord(rec); // gets apppended to the string buffer
					// System.out.println(stringBuffer.toString());
				}
				else if (countOfLines == countOfTotalLines)
				{
					System.out.println("else-->");
					bw = WritingToFile.getBufferedWriterForNewFile(pathToWrite + "Split" + splitID + ".csv");
					// System.out.println(" jjj ");
					// System.out.println(" kkk" + stringBuffer.toString());
					printer.printRecord(rec);
					bw.write(stringBuffer.toString());
					bw.close();
					stringBuffer.setLength(0);
					
					splitID++;
				}
				
				else
				{
					System.out.println("else-->");
					bw = WritingToFile.getBufferedWriterForNewFile(pathToWrite + "Split" + splitID + ".csv");
					// System.out.println(" jjj ");
					// System.out.println(" kkk" + stringBuffer.toString());
					bw.write(stringBuffer.toString());
					bw.close();
					stringBuffer.setLength(0);
					printer.printRecord(rec);
					splitID++;
				}
				prevVal = currentVal;
			}
			
			// // System.out.println("read records from " + fileToRead + " are :");
			//
			// // BufferedWriter bw = WritingToFile.getBufferedWriter(absfileToWrite);
			// CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT);
			//
			// int countOfLines = 0;
			// for (CSVRecord r : csvRecords)
			// {
			// countOfLines += 1;
			//
			// if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for non-first files
			// {
			// continue;
			// }
			// // System.out.println(r.toString());
			// printer.printRecord(r);
			//
			// }
			// System.out.println(countOfLines + " lines read for this user");
			// countOfTotalLines += countOfLines;
			//
			// printer.close();
			
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return new ArrayList<String>();
	}
	
	/**
	 * 
	 * @param absFileToSplit
	 * @param hasColumnHeader
	 * @param delimiter
	 * @param columnNumForSplit
	 * @return
	 */
	public static ArrayList<String> splitCSVFilesByColumnValueNoHeaderNoApache(String absFileToSplit, String delimiter,
			int columnNumForSplit, String pathToWrite, int numOfLines)
	{
		int countOfFiles = 0, countOfTotalLines = numOfLines;
		int splitID = 1;
		int countOfLines = 0;
		BufferedWriter bw;
		// CSVPrinter printer;
		StringBuffer stringBuffer = new StringBuffer();
		String currentVal = "", prevVal = "";
		String headerLine = "";
		
		try
		{
			// List<CSVRecord> csvRecords = getCSVRecords(absFileToSplit, delimiter);
			// countOfTotalLines = csvRecords.size();
			System.out.println("File to split has " + countOfTotalLines + " lines");
			BufferedReader br = new BufferedReader(new FileReader(absFileToSplit));// Constant.getCommonPath() +
			String lineRead = "";
			Pattern delimiterP = Pattern.compile(delimiter);
			
			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;
				
				String[] splittedString = delimiterP.split(lineRead);
				// System.out.println(stringBuffer.toString());
				currentVal = splittedString[columnNumForSplit];
				// System.out.println(stringBuffer.toString());
				
				// System.out.println(countOfLines + " " + "currentVal= " + currentVal + " " + "prevVal= " + prevVal);
				
				if ((prevVal.length() == 0 || currentVal.equals(prevVal)) && countOfLines != countOfTotalLines)
				{
					// System.out.println("if-->");
					// printer.printRecord(rec); // gets apppended to the string buffer
					stringBuffer.append(lineRead + "\n");
					// System.out.println(stringBuffer.toString());
				}
				else if (countOfLines == countOfTotalLines)
				{
					// System.out.println("else-->");
					bw = WritingToFile.getBufferedWriterForNewFile(pathToWrite + "Split" + splitID + ".csv");
					// System.out.println(" jjj ");
					// System.out.println(" kkk" + stringBuffer.toString());
					stringBuffer.append(lineRead + "\n");
					bw.write(stringBuffer.toString());
					bw.close();
					stringBuffer.setLength(0);
					
					splitID++;
				}
				
				else
				{
					// System.out.println("else-->");
					bw = WritingToFile.getBufferedWriterForNewFile(pathToWrite + "Split" + splitID + ".csv");
					// System.out.println(" jjj ");
					// System.out.println(" kkk" + stringBuffer.toString());
					bw.write(stringBuffer.toString());
					bw.close();
					stringBuffer.setLength(0);
					stringBuffer.append(lineRead + "\n");
					splitID++;
				}
				prevVal = currentVal;
			}
			
			// // System.out.println("read records from " + fileToRead + " are :");
			//
			// // BufferedWriter bw = WritingToFile.getBufferedWriter(absfileToWrite);
			// CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT);
			//
			// int countOfLines = 0;
			// for (CSVRecord r : csvRecords)
			// {
			// countOfLines += 1;
			//
			// if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for non-first files
			// {
			// continue;
			// }
			// // System.out.println(r.toString());
			// printer.printRecord(r);
			//
			// }
			// System.out.println(countOfLines + " lines read for this user");
			// countOfTotalLines += countOfLines;
			//
			// printer.close();
			
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return new ArrayList<String>();
	}
	
	/**
	 * 
	 * @param rowNum
	 *            starts from 1 and irrespective of header or no header
	 * @param colNum
	 *            starts from 1 and irrespective of header or no header
	 * @param filenameToRead
	 * @return
	 */
	public static String getCellValueFromCSVFile(int rowNum, int colNum, String filenameToRead)// , boolean hasColHeader, boolean hasRowHeader)
	{
		String tval = "";
		boolean found = false;
		
		List<CSVRecord> csvRecords = getCSVRecords(filenameToRead);
		
		for (int row = 0; row < csvRecords.size(); row++)
		{
			CSVRecord fetchedRow = csvRecords.get(row);
			// System.out.print(" row: " + row + " = " + fetchedRow.toString());
			
			for (int col = 0; col < fetchedRow.size(); col++)
			{
				// System.out.print(" col: " + col + " = " + fetchedRow.get(col));
				
				if (row == (rowNum - 1) && col == (colNum - 1))
				{
					// System.out.println("Found in row " + (row + 1) + " col " + (col + 1));
					return fetchedRow.get(col);
				}
			}
			// System.out.println("\n");
		}
		
		if (found == false)
		{
			// PopUps.showException(new Exception("Error in getCellValueFromCSVFile: rowNum=" + rowNum + " colNum=" + colNum
			// + " not found in file=" + filenameToRead), "ReadingFromFile.getCellValueFromCSVFile()");
		}
		
		return tval;
	}
	// /**
	// * reads a column from a csv file and returns it as an arraylist of String
	// *
	// * @param absolutePath
	// * path of fileNameToRead
	// * @param delimiter
	// * @param columnIndex
	// * starts from o
	// * @param hasHeader
	// * @return
	// */
	// public static List<String> twoColumnReaderString(String fileNameToRead, String delimiter, int columnIndex1, int columnIndex2, boolean hasHeader)
	// {
	//
	// List<String> raw = new ArrayList<String>();
	// String line = "";
	// try
	// {
	// BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
	//
	// int count;
	//
	// if (hasHeader)
	// {
	// raw = br.lines().skip(1).map((String s) -> (s.split(Pattern.quote(delimiter))[columnIndex])).collect(Collectors.toList());
	// }
	// else
	// {
	// raw = br.lines().map((String s) -> (s.split(Pattern.quote(delimiter))[columnIndex])).collect(Collectors.toList());
	// }
	// System.out.println("Size of raw =" + raw.size());
	// }
	//
	// catch (IOException e)
	// {
	// e.printStackTrace();
	// }
	// return raw;
	// }
}
