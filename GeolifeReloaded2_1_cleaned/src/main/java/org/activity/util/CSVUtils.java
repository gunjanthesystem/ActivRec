package org.activity.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.activity.io.WritingToFile;
import org.activity.ui.PopUps;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.github.mgunlogson.cuckoofilter4j.CuckooFilter;
import com.google.common.hash.Funnels;

import gnu.trove.set.hash.THashSet;

public class CSVUtils
{
	
	public static void main(String[] args)
	{
		String processedCheckInFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/processedCheckIns.csv";
		String noDupProcessedCheckinFileName =
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/NoDuplicateprocessedCheckIns.csv";
		String dupLinesCheckinFileName =
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/DupLinesFromProcessedCheckins.csv";
		
		// removeDuplicationRowsInPreVicinity(processedCheckInFileName, noDupProcessedCheckinFileName, dupLinesCheckinFileName, 0);
		removeDuplicationRowsUsingCuckoo(processedCheckInFileName, noDupProcessedCheckinFileName, dupLinesCheckinFileName);
		// UsingCuckoo
	}
	
	/**
	 * Removes duplicate rows from a given csv file.</br>
	 * preserves order of unique rows</br>
	 * if possible, make it well performant
	 * 
	 * @param inputFileName
	 * @param logPath
	 */
	public static void removeDuplicationRows(String inputFileName, String outputFileName, String duplicateLinesFileName)
	{
		System.out.println("Entering removeDuplicationRows");
		
		HashSet<CharSequence> allUniqueEntries = new HashSet<CharSequence>();
		BufferedReader br = null;
		// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(outputFileName);
		BufferedWriter bwDup = WritingToFile.getBufferedWriterForNewFile(duplicateLinesFileName);
		StringBuilder uniqueLines = new StringBuilder();
		
		long t1 = System.currentTimeMillis();
		int allLinesCount = 0, uniqueLinesCount = 0, duplicateLinesCount = 0;
		try
		{
			br = new BufferedReader(new FileReader(inputFileName));
			
			CharSequence currentLineRead;
			
			while ((currentLineRead = br.readLine()) != null)
			{
				allLinesCount += 1;
				
				// if (allLinesCount > 2000000)
				// {
				// allLinesCount -= 1;
				// break;
				// }
				
				if (allUniqueEntries.contains(currentLineRead))
				{
					duplicateLinesCount++;
					bwDup.append(currentLineRead.toString() + "\n");
				}
				else
				{
					uniqueLinesCount += 1;
					allUniqueEntries.add(currentLineRead);
					uniqueLines.append(currentLineRead + "\n");
				}
				
				if (allLinesCount % 20000 == 0)
				{
					// truncate the set to save space
					// if(allUniqueEntries.size() >2000)
					// {
					// allUniqueEntries.val
					// }
					
					// System.out.println("read- " + allLinesCount);
					// System.out.println("free memory: " + Runtime.getRuntime().freeMemory() + " bytes");
					WritingToFile.appendLineToFileAbsolute(uniqueLines.toString(), outputFileName);
					// bw.write(uniqueLines.toString());
					uniqueLines.setLength(0);
				}
				
				if (allLinesCount % 200000 == 0)
				{
					System.out.println("lines read: " + allLinesCount);
				}
			}
			
			WritingToFile.appendLineToFileAbsolute(uniqueLines.toString(), outputFileName);
			// bw.write(uniqueLines.toString());
			uniqueLines.setLength(0);
			
			br.close();
			// bw.close();
			bwDup.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Num of lines read = " + allLinesCount);
		System.out.println("Num of unique lines = " + allUniqueEntries.size() + " = " + uniqueLinesCount);
		
		System.out.println("Num of duplicate lines = " + duplicateLinesCount);
		
		long sum = uniqueLinesCount + duplicateLinesCount;
		
		if (sum != allLinesCount)
		{
			System.out.println(" sum = " + sum + "allLinesCount = " + allLinesCount);
		}
		System.out.println("time taken = " + (t2 - t1) / 1000 + " secs ");
		System.out.println("Exiting removeDuplicationRows");
	}
	
	/**
	 * Removes duplicate rows from a given csv file.</br>
	 * preserves order of unique rows</br>
	 * if possible, make it well performant
	 * 
	 * @param inputFileName
	 * @param logPath
	 */
	public static void removeDuplicationRowsTrove(String inputFileName, String outputFileName, String duplicateLinesFileName)
	{
		System.out.println("Entering removeDuplicationRows");
		
		THashSet<String> allUniqueEntries = new THashSet<String>();
		BufferedReader br = null;
		BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(outputFileName);
		BufferedWriter bwDup = WritingToFile.getBufferedWriterForNewFile(duplicateLinesFileName);
		StringBuilder uniqueLines = new StringBuilder();
		
		long t1 = System.currentTimeMillis();
		int allLinesCount = 0, uniqueLinesCount = 0, duplicateLinesCount = 0;
		try
		{
			br = new BufferedReader(new FileReader(inputFileName));
			
			String currentLineRead;
			
			while ((currentLineRead = br.readLine()) != null)
			{
				allLinesCount += 1;
				
				// if (allLinesCount > 2000000)
				// {
				// allLinesCount -= 1;
				// break;
				// }
				
				if (allUniqueEntries.contains(currentLineRead))
				{
					duplicateLinesCount++;
					bwDup.append(currentLineRead.toString() + "\n");
				}
				else
				{
					uniqueLinesCount += 1;
					allUniqueEntries.add(currentLineRead);
					uniqueLines.append(currentLineRead + "\n");
				}
				
				if (allLinesCount % 25000 == 0)
				{
					// truncate the set to save space
					// if(allUniqueEntries.size() >2000)
					// {
					// allUniqueEntries.val
					// }
					
					System.out.println("lines read: " + allLinesCount);
					System.out.println("free memory: " + Runtime.getRuntime().freeMemory() + " bytes");
					bw.write(uniqueLines.toString());
					uniqueLines.setLength(0);
				}
				
			}
			
			bw.write(uniqueLines.toString());
			uniqueLines.setLength(0);
			
			br.close();
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Num of lines read = " + allLinesCount);
		System.out.println("Num of unique lines = " + allUniqueEntries.size() + " = " + uniqueLinesCount);
		
		System.out.println("Num of duplicate lines = " + duplicateLinesCount);
		
		long sum = uniqueLinesCount + duplicateLinesCount;
		
		if (sum != allLinesCount)
		{
			System.out.println(" sum = " + sum + "allLinesCount = " + allLinesCount);
		}
		System.out.println("time taken = " + (t2 - t1) / 1000 + " secs ");
		System.out.println("Exiting removeDuplicationRows");
	}
	
	/**
	 * Removes duplicate rows from a given csv file.</br>
	 * preserves order of unique rows</br>
	 * if possible, make it well performant INCOMPLETE
	 * 
	 * @param inputFileName
	 * @param outputFileName
	 * @param duplicateLinesFileName
	 * @param previcinity
	 */
	public static void removeDuplicationRowsInPreVicinity(String inputFileName, String outputFileName, String duplicateLinesFileName,
			int previcinity)
	{
		System.out.println("Entering removeDuplicationRows");
		
		ArrayList<String> allUniqueEntries = new ArrayList<String>();
		BufferedReader br = null;
		// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(outputFileName);
		BufferedWriter bwDup = WritingToFile.getBufferedWriterForNewFile(duplicateLinesFileName);
		StringBuilder uniqueLines = new StringBuilder();
		
		long t1 = System.currentTimeMillis();
		int allLinesCount = 0, uniqueLinesCount = 0, duplicateLinesCount = 0;
		try
		{
			br = new BufferedReader(new FileReader(inputFileName));
			
			String currentLineRead;
			
			while ((currentLineRead = br.readLine()) != null)
			{
				allLinesCount += 1;
				
				// if (allLinesCount > 2000000)
				// {
				// allLinesCount -= 1;
				// break;
				// }
				
				if (allUniqueEntries.contains(currentLineRead))
				{
					duplicateLinesCount++;
					bwDup.append(currentLineRead.toString() + "\n");
				}
				else
				{
					uniqueLinesCount += 1;
					allUniqueEntries.add(currentLineRead);
					uniqueLines.append(currentLineRead + "\n");
				}
				
				if (allLinesCount % 40000 == 0)
				{
					// truncate the set to save space
					// if(allUniqueEntries.size() >2000)
					// {
					// allUniqueEntries.val
					// }
					
					// System.out.println("read- " + allLinesCount);
					// System.out.println("free memory: " + Runtime.getRuntime().freeMemory() + " bytes");
					WritingToFile.appendLineToFileAbsolute(uniqueLines.toString(), outputFileName);
					// bw.write(uniqueLines.toString());
					uniqueLines.setLength(0);
				}
				
				if (allLinesCount % 100000 == 0)
				{
					System.out.println("lines read: " + allLinesCount);
				}
			}
			
			WritingToFile.appendLineToFileAbsolute(uniqueLines.toString(), outputFileName);
			// bw.write(uniqueLines.toString());
			uniqueLines.setLength(0);
			
			br.close();
			// bw.close();
			bwDup.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Num of lines read = " + allLinesCount);
		System.out.println("Num of unique lines = " + allUniqueEntries.size() + " = " + uniqueLinesCount);
		
		System.out.println("Num of duplicate lines = " + duplicateLinesCount);
		
		long sum = uniqueLinesCount + duplicateLinesCount;
		
		if (sum != allLinesCount)
		{
			System.out.println(" sum = " + sum + "allLinesCount = " + allLinesCount);
		}
		System.out.println("time taken = " + (t2 - t1) / 1000 + " secs ");
		System.out.println("Exiting removeDuplicationRows");
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
				List<CSVRecord> csvRecords = CSVUtils.getCSVRecords(fileToRead);
				
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
				List<CSVRecord> csvRecords = CSVUtils.getCSVRecords(fileToRead, delimiter);
				
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
			List<CSVRecord> csvRecords = CSVUtils.getCSVRecords(absFileToSplit, delimiter);
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
	
	/**
	 * Removes duplicate rows from a given csv file.</br>
	 * preserves order of unique rows</br>
	 * if possible, make it well performant
	 * 
	 * Using Cuckoo filter
	 * 
	 * @param inputFileName
	 * @param logPath
	 */
	public static void removeDuplicationRowsUsingCuckoo(String inputFileName, String outputFileName, String duplicateLinesFileName)
	{
		System.out.println("Entering removeDuplicationRows");
		
		// create
		CuckooFilter<CharSequence> allUniqueEntries = new CuckooFilter.Builder<>(Funnels.stringFunnel(Charset.defaultCharset()), 37000000)
				.withFalsePositiveRate(0.0000000000001).build();// 0.0000000000001
		
		// HashSet<String> allUniqueEntries = new HashSet<String>();
		BufferedReader br = null;
		BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(outputFileName);
		BufferedWriter bwDup = WritingToFile.getBufferedWriterForNewFile(duplicateLinesFileName);
		StringBuilder uniqueLines = new StringBuilder();
		
		long t1 = System.currentTimeMillis();
		int allLinesCount = 0, uniqueLinesCount = 0, duplicateLinesCount = 0;
		try
		{
			br = new BufferedReader(new FileReader(inputFileName));
			
			String currentLineRead;
			
			while ((currentLineRead = br.readLine()) != null)
			{
				allLinesCount += 1;
				
				// if (allLinesCount > 2000000)
				// {
				// allLinesCount -= 1;
				// break;
				// }
				
				if (allUniqueEntries.mightContain(currentLineRead) == false) // definitely does not contain
				// if (allUniqueEntries.contains(currentLineRead))
				// allUniqueEntries
				{
					uniqueLinesCount += 1;
					boolean insert = allUniqueEntries.put(currentLineRead);
					if (!insert)
					{
						String msg = "Insert failed on cuckoo filter. allLiinesCount = " + allLinesCount;
						PopUps.showError(msg);
					}
					uniqueLines.append(currentLineRead + "\n");
				}
				else // probably duplicate
				{
					duplicateLinesCount++;
					bwDup.append(currentLineRead.toString() + "\n");
				}
				
				if (allLinesCount % 200000 == 0)
				{
					// truncate the set to save space
					// if(allUniqueEntries.size() >2000)
					// {
					// allUniqueEntries.val
					// }
					
					System.out.println("lines read: " + allLinesCount);
					// System.out.println("free memory: " + Runtime.getRuntime().freeMemory() + " bytes");
					bw.write(uniqueLines.toString());
					uniqueLines.setLength(0);
				}
				
			}
			
			bw.write(uniqueLines.toString());
			uniqueLines.setLength(0);
			
			br.close();
			bw.close();
			bwDup.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Num of lines read = " + allLinesCount);
		System.out.println("Num of unique lines = " + allUniqueEntries.getCount() + " = " + uniqueLinesCount);
		
		System.out.println("Num of duplicate lines = " + duplicateLinesCount);
		
		long sum = uniqueLinesCount + duplicateLinesCount;
		
		if (sum != allLinesCount)
		{
			System.out.println(" sum = " + sum + "allLinesCount = " + allLinesCount);
		}
		System.out.println("time taken = " + (t2 - t1) / 1000 + " secs ");
		System.out.println("Exiting removeDuplicationRows");
	}
}