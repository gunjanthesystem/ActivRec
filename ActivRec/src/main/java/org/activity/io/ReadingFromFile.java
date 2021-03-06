package org.activity.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.ArrayUtils;

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
	public static List<String> oneColumnReaderString(String fileNameToRead, String delimiter, int columnIndex,
			boolean hasHeader)
	{

		List<String> raw = new ArrayList<String>();
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));
			if (hasHeader)
			{
				raw = br.lines().skip(1).map((String s) -> (s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> (s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			// System.out.println("Size of raw =" + raw.size());
			br.close();
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
		return raw;
	}

	/**
	 * reads a column from a csv file and returns it as an arraylist of String
	 * 
	 * @param inputStream
	 *            inputStream of fileNameToRead
	 * @param delimiter
	 * @param columnIndex
	 *            starts from 0
	 * @param hasHeader
	 * @return
	 */
	public static List<String> oneColumnReaderString(InputStream inputStream, String delimiter, int columnIndex,
			boolean hasHeader)
	{
		List<String> raw = new ArrayList<String>();
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(inputStream));

			if (hasHeader)
			{
				raw = br.lines().skip(1).map((String s) -> (s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> (s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			// System.out.println("Size of raw =" + raw.size());
			br.close();
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
	public static List<Long> oneColumnReaderLong(String fileNameToRead, String delimiter, int columnIndex,
			boolean hasHeader)
	{

		List<Long> raw = new ArrayList<Long>();
		String line = "";
		BufferedReader br;
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));

			int count;

			if (hasHeader)
			{
				raw = br.lines().skip(1)
						.map((String s) -> Long.parseLong(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> Long.parseLong(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			// System.out.println("Size of raw =" + raw.size());
			br.close();
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
	 *            starts from 0
	 * @param hasHeader
	 * @return
	 */
	public static List<Double> oneColumnReaderDouble(String fileNameToRead, String delimiter, int columnIndex,
			boolean hasHeader)
	{

		List<Double> raw = new ArrayList<Double>();
		String line = "";
		BufferedReader br;
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));// Constant.getCommonPath() +

			int count;

			if (hasHeader)
			{
				raw = br.lines().skip(1)
						.map((String s) -> Double.parseDouble(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> Double.parseDouble(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			// System.out.println("Size of raw =" + raw.size());
			br.close();
		}

		catch (IOException e)
		{
			System.err.println("Exception reading file: " + fileNameToRead);
			e.printStackTrace();
		}
		return raw;
	}

	public static List<Double> oneColumnReaderDouble(String fileNameToRead, String delimiter, int columnIndex,
			boolean hasHeader, long startRow, long endRow)
	{

		List<Double> raw = new ArrayList<Double>();
		String line = "";
		BufferedReader br;
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));// Constant.getCommonPath() +

			int count;

			if (hasHeader)
			{
				raw = br.lines().skip(1).skip(startRow).limit(endRow)
						.map((String s) -> Double.parseDouble(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().skip(startRow).limit(endRow)
						.map((String s) -> Double.parseDouble(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			// System.out.println("Size of raw =" + raw.size());
			br.close();
		}

		catch (IOException e)
		{
			System.err.println("Exception reading file: " + fileNameToRead);
			e.printStackTrace();
		}
		return raw;
	}

	public static List<Double> oneColumnReaderDouble(InputStream inputStream, String delimiter, int columnIndex,
			boolean hasHeader)
	{

		List<Double> raw = new ArrayList<Double>();
		String line = "";
		BufferedReader br;
		try
		{
			br = new BufferedReader(new InputStreamReader(inputStream));

			int count;

			if (hasHeader)
			{
				raw = br.lines().skip(1)
						.map((String s) -> Double.parseDouble(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> Double.parseDouble(s.split(Pattern.quote(delimiter))[columnIndex]))
						.collect(Collectors.toList());
			}
			// System.out.println("Size of raw =" + raw.size());
			br.close();
		}

		catch (IOException e)
		{
			System.err.println("Exception reading file from inputStream: " + inputStream.toString());
			e.printStackTrace();
		}
		return raw;
	}

	/**
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasHeader
	 * @return
	 */
	public static List<List<Double>> nColumnReaderDouble(String absFileNameToRead, String delimiter, boolean hasHeader)
	{
		List<List<Double>> res = null;
		try
		{
			res = ReadingFromFile.nColumnReaderDouble(new BufferedInputStream(new FileInputStream(absFileNameToRead)),
					delimiter, hasHeader);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 
	 * @param absFileNameToRead
	 * @param delimiter
	 * @param hasColHeader
	 * @param hasRowHeader
	 * @return
	 */
	public static List<List<Double>> nColumnReaderDouble(String absFileNameToRead, String delimiter,
			boolean hasColHeader, boolean hasRowHeader)
	{
		List<List<Double>> res = null;
		try
		{
			System.out.println("Reading file: " + absFileNameToRead);
			res = ReadingFromFile.nColumnReaderDouble(new BufferedInputStream(new FileInputStream(absFileNameToRead)),
					delimiter, hasColHeader, hasRowHeader);
		}
		catch (IOException e)
		{
			System.err.println("Error reading file: " + absFileNameToRead);
			e.printStackTrace();
			System.exit(-1);
		}
		return res;
	}

	/**
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasHeader
	 * @return
	 */
	public static List<List<Double>> nColumnReaderDouble(InputStream inputStream, String delimiter, boolean hasHeader)
	{

		List<List<Double>> raw = new ArrayList<>();
		BufferedReader br;

		try
		{
			br = new BufferedReader(new InputStreamReader(inputStream));
			if (hasHeader)
			{
				raw = br.lines().skip(1)
						.map((String s) -> Arrays.asList(s.split(Pattern.quote(delimiter))).stream()
								.map(n -> Double.parseDouble(n)).collect(Collectors.toList()))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines()
						.map((String s) -> Arrays.asList(s.split(Pattern.quote(delimiter))).stream()
								.map(n -> Double.parseDouble(n)).collect(Collectors.toList()))
						.collect(Collectors.toList());

			}
			// System.out.println("Size of raw =" + raw.size());
			br.close();
		}

		catch (Exception e)
		{
			System.err.println("Exception reading file from inputStream: " + inputStream.toString());
			e.printStackTrace();
		}
		return raw;
	}

	/**
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasColHeader
	 * @param hasRowHeader
	 * @return
	 * @throws IOException
	 * @since 19 July 2018
	 */
	public static List<List<Double>> nColumnReaderDouble(InputStream inputStream, String delimiter,
			boolean hasColHeader, boolean hasRowHeader)
	{

		List<List<Double>> raw = new ArrayList<>();
		BufferedReader br;

		int numOfColsToSkip = hasRowHeader ? 1 : 0;
		int numOfRowsToSkip = hasColHeader ? 1 : 0;
		try
		{
			br = new BufferedReader(new InputStreamReader(inputStream));
			raw = br.lines().skip(numOfRowsToSkip).map((String s) -> Arrays.asList(s.split(Pattern.quote(delimiter)))
					.stream().skip(numOfColsToSkip).map(n -> Double.parseDouble(n)).collect(Collectors.toList()))
					.collect(Collectors.toList());
			// System.out.println("Size of raw =" + raw.size());
			br.close();
		}

		catch (Exception e)
		{
			System.err.println("Exception reading file from inputStream: " + inputStream.toString());
			e.printStackTrace();
		}
		return raw;
	}

	/**
	 * 
	 * @param absFileName
	 * @param delimiter
	 * @param hasHeader
	 * @return
	 * @throws IOException
	 */
	public static List<List<String>> nColumnReaderString(String absFileName, String delimiter, boolean hasHeader)
			throws IOException
	{
		return nColumnReaderString(Files.newInputStream(Paths.get(absFileName), StandardOpenOption.READ), delimiter,
				hasHeader);

	}

	/**
	 * 
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasHeader
	 * @return
	 */
	public static List<List<String>> nColumnReaderString(InputStream inputStream, String delimiter, boolean hasHeader)
	{

		List<List<String>> raw = new ArrayList<>();
		BufferedReader br;

		try
		{
			br = new BufferedReader(new InputStreamReader(inputStream));
			if (hasHeader)
			{
				raw = br.lines().skip(1).map((String s) -> Arrays.asList(s.split(Pattern.quote(delimiter))).stream()
						.collect(Collectors.toList())).collect(Collectors.toList());
			}
			else
			{
				raw = br.lines().map((String s) -> Arrays.asList(s.split(Pattern.quote(delimiter))).stream()
						.collect(Collectors.toList())).collect(Collectors.toList());

			}
			// System.out.println("Size of raw =" + raw.size());
			br.close();
		}

		catch (IOException e)
		{
			System.err.println("Exception reading file from inputStream: " + inputStream.toString());
			e.printStackTrace();
		}
		return raw;
	}

	/**
	 * header is treated in same way as all other data
	 * <p>
	 * ref: http://www.baeldung.com/java-read-lines-large-file
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasHeader
	 * @param verboseReading
	 * @return
	 */
	public static List<List<String>> nColumnReaderStringLargeFile(InputStream inputStream, String delimiter,
			boolean hasHeader, boolean verboseReading)
	{

		List<List<String>> raw = new ArrayList<>();
		List<String> header = new ArrayList<>();
		String delimiterPattern = Pattern.quote(delimiter);

		if (verboseReading)
		{
			System.out.println("nColumnReaderStringLargeFile --");
		}
		try
		{
			LineIterator it = IOUtils.lineIterator(inputStream, "UTF-8");
			long countOfLinesRead = 0;

			while (it.hasNext())
			{
				countOfLinesRead += 1;
				String line = it.nextLine();
				// System.out.println("line=" + line);
				if (hasHeader && countOfLinesRead == 1)
				{
					header = Arrays.asList(line.split(delimiterPattern));
					raw.add(header);// header is treated in same way as all other data
					continue;
				}
				raw.add(Arrays.asList(line.split(delimiterPattern)));

				if (verboseReading && (countOfLinesRead % 25000 == 0))
				{
					System.out.println("Lines read: " + countOfLinesRead);
				}
				// if (countOfLinesRead > 100)
				// {
				// break;
				// }
			}

			System.out.println("Total Lines read: " + countOfLinesRead);
			it.close();
		}
		catch (IOException e)
		{
			System.err.println("Exception reading file from inputStream: " + inputStream.toString());
			e.printStackTrace();
		}

		// System.out.println("raw=\n" + raw);
		return raw;
	}

	/**
	 * Note: header is treated in same way as all other data
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasHeader
	 * @param verboseReading
	 * @param columnIndicesToSelect
	 * @return
	 */
	public static List<List<String>> nColumnReaderStringLargeFileSelectedColumns(String absFileName, String delimiter,
			boolean hasHeader, boolean verboseReading, int... columnIndicesToSelect)
	{
		BufferedInputStream inputStream = null;
		try
		{
			inputStream = new BufferedInputStream(new FileInputStream(absFileName));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return nColumnReaderStringLargeFileSelectedColumns(inputStream, delimiter, hasHeader, verboseReading,
				columnIndicesToSelect);
	}

	/**
	 * Note: header is treated in same way as all other data
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasHeader
	 * @param verboseReading
	 * @param columnIndicesToSelect
	 * @return
	 */
	public static List<List<String>> nColumnReaderStringLargeFileSelectedColumns(InputStream inputStream,
			String delimiter, boolean hasHeader, boolean verboseReading, int... columnIndicesToSelect)
	{

		long t1 = System.currentTimeMillis();
		List<List<String>> raw = new ArrayList<>();
		List<String> header = new ArrayList<>();
		String splitLiteral = Pattern.quote(delimiter);
		if (verboseReading)
		{
			System.out.println("nColumnReaderStringLargeFile --");
		}
		try
		{
			LineIterator it = IOUtils.lineIterator(inputStream, "UTF-8");
			long countOfLinesRead = 0;

			while (it.hasNext())
			{
				countOfLinesRead += 1;

				String line = it.nextLine();
				// System.out.println("line=" + line);
				String[] splittedString = (line.split(splitLiteral));

				if (hasHeader && countOfLinesRead == 1)
				{
					for (int indexToSelect : columnIndicesToSelect)
					{
						header.add(splittedString[indexToSelect]);
					}
					raw.add(header);// header is treated in same way as all other data
					continue;
				}

				List<String> temp = new ArrayList<>();

				try
				{
					for (int indexToSelect : columnIndicesToSelect)
					{
						temp.add(splittedString[indexToSelect]);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println(
							"line num:" + countOfLinesRead + " splittedString=" + Arrays.asList(splittedString));

				}

				raw.add(temp);// header is treated in same way as all other data

				if (verboseReading && (countOfLinesRead % 25000 == 0))
				{
					System.out.println("Lines read: " + countOfLinesRead);
				}

			}

			System.out.println(
					"Total Lines read: " + countOfLinesRead + " in " + (System.currentTimeMillis() - t1) + "ms");
			it.close();
		}
		catch (IOException e)
		{
			System.err.println("Exception reading file from inputStream: " + inputStream.toString());
			e.printStackTrace();
		}

		return raw;
	}

	/**
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasHeader
	 * @param verboseReading
	 * @param writeTheReadFile
	 * @param absFileToWrite
	 * @param columnIndicesToSelect
	 */
	public static List<List<String>> nColumnReaderStringLargeFileSelectedColumnsWithWrite(InputStream inputStream,
			String delimiter, boolean hasHeader, boolean verboseReading, boolean writeTheReadFile,
			String absFileToWrite, int... columnIndicesToSelect)
	{

		long t1 = System.currentTimeMillis();
		long countOfLinesRead = 0;
		List<List<String>> raw = new ArrayList<>();
		List<String> header = new ArrayList<>();
		String splitLiteral = Pattern.quote(delimiter);
		if (verboseReading)
		{
			System.out.println("nColumnReaderStringLargeFile --");
		}
		try
		{
			LineIterator it = IOUtils.lineIterator(inputStream, "UTF-8");

			while (it.hasNext())
			{
				countOfLinesRead += 1;

				String line = it.nextLine();
				// System.out.println("line=" + line);
				String[] splittedString = (line.split(splitLiteral));

				if (hasHeader && countOfLinesRead == 1)
				{
					for (int indexToSelect : columnIndicesToSelect)
					{
						header.add(splittedString[indexToSelect]);
					}
					raw.add(header);// header is treated in same way as all other data
					continue;
				}

				List<String> temp = new ArrayList<>();
				for (int indexToSelect : columnIndicesToSelect)
				{
					temp.add(splittedString[indexToSelect]);
				}
				raw.add(temp);// header is treated in same way as all other data

				if (countOfLinesRead % 25000 == 0)
				{
					StringBuilder sb = new StringBuilder();
					sb.append("\nLines read: " + countOfLinesRead);
					sb.append("\nraw.size()=" + raw.size());

					if (writeTheReadFile)
					{
						WToFile.writeListOfList(raw, absFileToWrite, "", ",", true);
					}
					raw.clear();

					if (verboseReading)
					{
						System.out.println(sb.toString() + "\nraw.size()=" + raw.size() + "\n\n");
					}
				}

			}

			if (writeTheReadFile)
			{
				WToFile.writeListOfList(raw, absFileToWrite, "", ",", true);
			}
			System.out.println(
					"Total Lines read: " + countOfLinesRead + " in " + (System.currentTimeMillis() - t1) + "ms");
			it.close();
		}
		catch (IOException e)
		{
			System.err.println("Exception reading file from inputStream: " + inputStream.toString());
			e.printStackTrace();
		}
		return raw;
	}

	/**
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasHeader
	 * @param verboseReading
	 * @param chunkSize
	 * @param columnIndicesToSelect
	 * @return
	 */
	public static List<List<String>> nColumnReaderStringLargeFileSelectedColumnsInChunks(InputStream inputStream,
			String delimiter, boolean hasHeader, boolean verboseReading, int chunkSize, int... columnIndicesToSelect)
	{
		List<List<String>> raw = new ArrayList<>();

		int lineNumber = 0;
		int chunkNumber = 1;
		List<List<String>> resForThisChunk = null;

		do
		{
			resForThisChunk = nColumnReaderStringLargeFileSelectedColumns(inputStream, delimiter, hasHeader,
					verboseReading, lineNumber, lineNumber + chunkSize, columnIndicesToSelect);
			raw.addAll(resForThisChunk);

			System.out.println("Read from linenumber:" + lineNumber + " to " + (lineNumber + chunkSize));
			System.out.println("resForThisChunk.size()=" + resForThisChunk.size());
			lineNumber += (chunkSize + 1);
		}
		while (resForThisChunk.size() != 0);

		return raw;
	}

	/**
	 * 
	 * @param inputStream
	 * @param delimiter
	 * @param hasHeader
	 * @param verboseReading
	 * @param startLineNum
	 * @param endLineNum
	 * @param columnIndicesToSelect
	 * @return
	 */
	public static List<List<String>> nColumnReaderStringLargeFileSelectedColumns(InputStream inputStream,
			String delimiter, boolean hasHeader, boolean verboseReading, long startLineNum, long endLineNum,
			int... columnIndicesToSelect)
	{

		List<List<String>> raw = new ArrayList<>();

		if (verboseReading)
		{
			System.out.println("\n\nnColumnReaderStringLargeFile --");
			System.out.println("Will read lines from " + startLineNum + " to " + endLineNum);

		}
		try
		{
			LineIterator it = IOUtils.lineIterator(inputStream, "UTF-8");
			long countOfLinesRead = 0;

			while (it.hasNext())
			{
				countOfLinesRead += 1;

				if (countOfLinesRead < startLineNum)
				{
					// System.out.println("continue");
					continue;
				}
				else if (countOfLinesRead > endLineNum)
				{
					System.out.println("break : countOfLinesRead=" + countOfLinesRead + " endLineNum" + endLineNum);
					break;
				}

				else
				{
					String line = it.nextLine();
					// System.out.println("line=" + line);
					String[] splittedString = (line.split(Pattern.quote(delimiter)));

					List<String> temp = new ArrayList<>();
					for (int indexToSelect : columnIndicesToSelect)
					{
						temp.add(splittedString[indexToSelect]);
					}
					raw.add(temp);

					if (verboseReading && (countOfLinesRead % 25000 == 0))
					{
						System.out.println("Lines read: " + countOfLinesRead);
					}
				}
			}

			System.out.println("Total Lines read: " + countOfLinesRead);
			it.close();
		}
		catch (IOException e)
		{
			System.err.println("Exception reading file from inputStream: " + inputStream.toString());
			e.printStackTrace();
		}

		return raw;
	}

	/**
	 * reads two columns from a csv file and returns it as an arraylist of String where each string is a contactenation
	 * of the strings (separated by a single space) from each row of the two columns
	 * 
	 * @param absolutePath
	 *            path of fileNameToRead
	 * @param delimiter
	 * @param columnIndex
	 *            starts from 0
	 * @param hasHeader
	 * @return
	 */
	public static List<String> twoColumnReaderString(String fileNameToRead, String delimiter, int column1Index,
			int column2Index, boolean hasHeader)
	{

		List<String> raw = new ArrayList<String>();
		String line = "";
		String dlimPatrn = Pattern.quote(delimiter);
		BufferedReader br;
		try
		{
			br = new BufferedReader(new FileReader(fileNameToRead));

			int count;

			if (hasHeader)
			{
				raw = br.lines().skip(1)
						.map((String s) -> (s.split(dlimPatrn)[column1Index] + " " + s.split(dlimPatrn)[column2Index]))
						.collect(Collectors.toList());
			}
			else
			{
				raw = br.lines()
						.map((String s) -> (s.split(dlimPatrn)[column1Index] + " " + s.split(dlimPatrn)[column2Index]))
						.collect(Collectors.toList());
			}
			// System.out.println("Size of raw =" + raw.size());
			br.close();
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
	public static void main1(String args[])
	{
		// String fileName =
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Jan27Daywise/recommPointsWithNoCandidates.csv";
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
		CSVUtils.concatenateCSVFiles(fileNamesToConcactenate, true,
				"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/TrajectorySpace/June10ContactednateStayPoints/AllStayPoints.csv");
	}

	public static void concat18Jan_1(String filename)
	{
		String[] files = { "MedianAllPerDirectTopKAgreementsL1_.csv", "MedianAllPerDirectTopKAgreements_.csv",
				"MeanAllPerDirectTopKAgreementsL1_.csv", "MeanAllPerDirectTopKAgreements_.csv" };

		for (String s : files)
		{
			ReadingFromFile.concat18Jan(s);
		}
	}

	public static void concat18Jan(String filename)
	{
		String commonPath = "./dataWritten/Jan3_Sampling_AKOM1DayOrder1/";
		ArrayList<String> fileNamesToConcactenate = new ArrayList<String>();
		int sampleIDStart = 0;
		int sampleIDEnd = 8;

		for (int i = sampleIDStart; i <= sampleIDEnd; i++)
		{
			// String userID = String.format("%03d", i);
			String fileName = commonPath + "Sample" + i + "/" + filename;
			fileNamesToConcactenate.add(fileName);
		}
		CSVUtils.concatenateCSVFiles(fileNamesToConcactenate, true, "./dataWritten/AllTogether" + filename + ".csv");
	}

	public static void main(String args[])
	{
		if (false)
		{
			ArrayList<ArrayList<String>> sublists = readRandomSamplesIntoListOfLists(
					"dataWritten/Jan16/randomlySampleUsers.txt", 13, 21, ",");

			for (ArrayList<String> l : sublists)
			{
				System.out.println(l.toString());
			}
		}

		String absFileNameToRead = "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/JUL19ED1.0STimeLocAllActsFDStFilter0hrs100RTV/AvgRecall_3.csv";
		List<List<Double>> res = nColumnReaderDouble(absFileNameToRead, ",", true, true);

		for (List<Double> v : res)
		{
			System.out.println(v);
		}

	}

	/**
	 * 
	 * @param absFileName
	 * @param startLineNum
	 * @param endLineNum
	 *            TODO Nov 2018: CHECK its correctness, seems endLineNum shoule be instea number of lines from start
	 * @param delimiter
	 * @return
	 */
	public static List<List<String>> readLinesIntoListOfLists(String absFileName, int startLineNum, int endLineNum,
			String delimiter)
	{
		try
		{
			return Files.lines(Paths.get(absFileName)).skip(startLineNum - 1).limit(endLineNum)
					.map(l -> new ArrayList<String>(Arrays.asList(l.split(delimiter))))
					.collect(Collectors.toCollection(ArrayList::new));
		}
		catch (Exception e)
		{
			PopUps.printTracedErrorMsgWithExit("Exception: " + e.toString());
			return null;
		}
	}

	/**
	 * 
	 * @param absFileName
	 * @param delimiter
	 * @return
	 */
	public static List<List<String>> readLinesIntoListOfLists(String absFileName, String delimiter)
	{
		try
		{
			return Files.lines(Paths.get(absFileName))
					.map(l -> new ArrayList<String>(Arrays.asList(l.split(delimiter))))
					.collect(Collectors.toCollection(ArrayList::new));
		}
		catch (Exception e)
		{
			PopUps.printTracedErrorMsgWithExit("Exception: " + e.toString());
			return null;
		}
	}

	/**
	 * 
	 * @param absFileName
	 * @param delimiter
	 * @return
	 */
	public static List<List<String>> readLinesIntoListOfListsPrecompiledSplit(String absFileName, String delimiter)
	{
		Pattern splitPattern = Pattern.compile("|");
		try
		{
			return Files.lines(Paths.get(absFileName))
					.map(l -> new ArrayList<String>(Arrays.asList(splitPattern.split(l))))
					.collect(Collectors.toCollection(ArrayList::new));
		}
		catch (Exception e)
		{
			PopUps.printTracedErrorMsgWithExit("Exception: " + e.toString());
			return null;
		}
	}

	/**
	 * 
	 * @param absFileName
	 * @param startLineNum
	 * @param endLineNum
	 * @param delimiter
	 * @return
	 */
	public static List<String> getColHeaders(String absFileName, String delimiter)
	{
		try
		{
			List<List<String>> v = Files.lines(Paths.get(absFileName)).limit(1)
					.map(l -> Arrays.asList(l.split(delimiter))).collect(Collectors.toList());
			return v.get(0);
		}
		catch (Exception e)
		{
			PopUps.printTracedErrorMsgWithExit("Exception: " + e.toString());
			return null;
		}
	}

	/**
	 * 
	 * @param absFileName
	 * @param startLineNum
	 * @param endLineNum
	 * @param delimiter
	 * @return
	 */
	public static ArrayList<ArrayList<String>> readRandomSamplesIntoListOfLists(String absFileName, int startLineNum,
			int endLineNum, String delimiter)
	{
		ArrayList<ArrayList<String>> newRes = new ArrayList<ArrayList<String>>();
		try
		{
			List<List<String>> oldRes = readLinesIntoListOfLists(absFileName, startLineNum, endLineNum, delimiter);

			for (List<String> l : oldRes)
			{// removing [ and ] from the strings
				newRes.add(l.stream().map(s -> s.replace("]", "")).map(s -> s.replace("[", "")).map(s -> s.trim())
						.collect(Collectors.toCollection(ArrayList::new)));
			}
		}
		catch (Exception e)
		{
			PopUps.printTracedErrorMsgWithExit("Exception: " + e.toString());
		}

		return newRes;
	}

	/**
	 * Return a list of black listed recommendation time with each entry of the form <rawUserID timestampAsString>.
	 * Currently the blacklisted RTs are the RTs not used in daywise matching in lieu of no candidate timelines for them
	 * during daywaise matching
	 * 
	 * @return
	 */
	public static List<String> getBlackListedRTs(String databaseName)
	{
		List<String> res = null;
		if (databaseName.equals("geolife1") == false)
		{
			PopUps.showException(new Exception(
					"Error in getBlackListedRTs(): this is mean only for geolife database while the current databse is "
							+ databaseName),
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
	 * 
	 * @param filePath
	 * @param rowIndex
	 *            starts from 0
	 * @param colIndex
	 *            starts from 0
	 * @param hasColHeader
	 * @return
	 */
	public static double getValByRowCol(String filePath, int rowIndex, int colIndex, boolean hasColHeader)
	{
		double res = Double.NaN;
		try
		{
			List<Double> valsInColumnAsList = oneColumnReaderDouble(filePath, ",", colIndex, hasColHeader);
			Double[] valsInColumnAsArray = valsInColumnAsList.toArray(new Double[valsInColumnAsList.size()]);
			double[] valsInColumnAsPrimitiveArray = ArrayUtils.toPrimitive(valsInColumnAsArray);

			res = valsInColumnAsPrimitiveArray[rowIndex];
		}
		catch (Exception e)
		{
			PopUps.printTracedErrorMsg("Error in getValByRowCol filePath =" + filePath + " rowIndex= " + rowIndex
					+ " colIndex=" + colIndex + " hasColHeader=" + hasColHeader);
		}
		return res;

	}

	/**
	 * 
	 * @param filesContainingMatrix
	 * @param nRows
	 * @param nCols
	 * @param hasColHeader
	 * @param delimiter
	 * @return
	 */
	public static String getMaxOverFilesForEachCell(ArrayList<String> filesContainingMatrix, int nRows, int nCols,
			boolean hasColHeader, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		try
		{
			// row,col, vals for that across mus
			LinkedHashMap<Pair<Integer, Integer>, List<Double>> valsL = new LinkedHashMap<>();
			// one list for each row col.
			// List<List<Double>> vals = new ArrayList<>(nRows * nCols);

			for (String fileName : filesContainingMatrix)
			{
				for (int row = 0; row < nRows; row++)
				{
					for (int col = 0; col < nCols; col++)
					{
						// vals.add(getValByRowCol(fileName, row, col, hasColHeader));
						valsL.get(new Pair<>(row, col)).add(getValByRowCol(fileName, row, col, hasColHeader));
						// valsL.put(new Pair<>(row, col), getValByRowCol(fileName, row, col, hasColHeader));
					}
				}
			}

			for (int row = 0; row < nRows; row++)
			{
				for (int col = 0; col < nCols; col++)
				{
					sb.append(Collections.max(valsL.get(new Pair<>(row, col))));
					if (col != nCols - 1)
					{
						sb.append(delimiter);
					}
				}
				sb.append("\n");
			}

		}
		catch (Exception e)
		{
			PopUps.printTracedErrorMsg("Error");
		}

		return sb.toString();
	}

	/**
	 * Closes multiple BufferedReaders
	 * 
	 * @param br1
	 * @param brs
	 * @throws IOException
	 */
	public static void closeBufferedReaders(BufferedReader br1, BufferedReader... brs) throws IOException
	{
		br1.close();
		for (BufferedReader br : brs)
		{
			br.close();
		}
	}

	/**
	 * Reads the file columnwise
	 * 
	 * 
	 * @param fileToRead
	 * @param columnIndicesToRead
	 * @param hasHeader
	 * @return arraylist(columns) of arraylist(values in the column)
	 */
	public static ArrayList<ArrayList<Double>> allColumnsReaderDouble(String fileToRead, String delimiter,
			int[] columnIndicesToRead, boolean hasHeader)
	{
		ArrayList<ArrayList<Double>> allVals = new ArrayList<>();

		int numOfColsRead = 0;
		for (int colIndex : columnIndicesToRead)
		{
			allVals.add(numOfColsRead++,
					(ArrayList<Double>) oneColumnReaderDouble(fileToRead, delimiter, colIndex, hasHeader));
		}

		if (columnIndicesToRead.length != allVals.size())
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: columnIndicesToRead.length" + columnIndicesToRead.length
					+ " != allVals.size()" + allVals.size()));
		}

		return allVals;
	}

	/**
	 * Reads the file columnwise
	 * 
	 * @param fileToRead
	 * @param delimiter
	 * @param columnIndicesToRead
	 * @param hasHeader
	 * @param startRow
	 * @param endRow
	 * @return arraylist(columns) of arraylist(values in the column)
	 */
	public static ArrayList<ArrayList<Double>> allColumnsReaderDouble(String fileToRead, String delimiter,
			int[] columnIndicesToRead, boolean hasHeader, long startRow, long endRow)
	{
		ArrayList<ArrayList<Double>> allVals = new ArrayList<>();

		int numOfColsRead = 0;
		for (int colIndex : columnIndicesToRead)
		{
			allVals.add(numOfColsRead++, (ArrayList<Double>) oneColumnReaderDouble(fileToRead, delimiter, colIndex,
					hasHeader, startRow, endRow));
		}

		if (columnIndicesToRead.length != allVals.size())
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: columnIndicesToRead.length" + columnIndicesToRead.length
					+ " != allVals.size()" + allVals.size()));
		}

		return allVals;
	}

	/**
	 * 
	 * @param absFileNameForLatLong
	 * @param delimiter
	 * @param latColIndex
	 * @param lonColIndex
	 * @param labelColIndex
	 */
	public static List<Triple<Double, Double, String>> readListOfLocationsV2(String absFileNameForLatLong,
			String delimiter, int latColIndex, int lonColIndex, int labelColIndex)
	{
		List<Triple<Double, Double, String>> listOfLocations = new ArrayList<>();
		try
		{
			List<List<String>> lines = nColumnReaderStringLargeFileSelectedColumns(
					new FileInputStream(new File(absFileNameForLatLong)), ",", true, false,
					new int[] { latColIndex, lonColIndex, labelColIndex });

			System.out.println("lines.size()=" + lines.size());

			int count = 0;

			for (List<String> line : lines)
			{
				count += 1;

				if (count == 1)
				{
					continue;
				}
				// if (count > 50000)
				// {
				// break;
				// }
				// System.out.println("line= " + line);
				// System.out.println("here 1");

				Triple<Double, Double, String> val = new Triple<>(Double.valueOf(line.get(0)),
						Double.valueOf(line.get(1)), line.get(2));

				// LatLong markerLatLong2 = new LatLong(-1.6073826, 67.9382483);// 47.606189, -122.335842);
				// // Double.valueOf(line.get(2).substring(0, 4)));
				// // LatLong markerLatLong2 = new LatLong(Double.valueOf(line.get(3).substring(0, 4)),
				// // Double.valueOf(line.get(2).substring(0, 4)));
				// System.out.println("LatLong= " + markerLatLong2.toString());
				// markerOptions2.position(markerLatLong2).title(line.get(1)).visible(true);
				// System.out.println("here2");
				// myMarker2 = new Marker(markerOptions2);
				// System.out.println("here3");
				listOfLocations.add(val);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// StringBuilder sb = new StringBuilder();
		// listOfMarkers.stream().forEachOrdered(e -> sb.append(e.toString() + "\n"));
		// System.out.println("List of markers= " + sb.toString());
		// System.out.println("listOfLocations.size()=" + listOfLocations.size());
		return listOfLocations;
	}

	/**
	 * 
	 * @param absFileNameForLatLong
	 * @param delimiter
	 * @param latColIndex
	 * @param lonColIndex
	 * @param labelColIndex
	 */
	public static Pair<List<Triple<Double, Double, String>>, Double> readListOfLocationsV3(String absFileNameForLatLong,
			String delimiter, int latColIndex, int lonColIndex, int labelColIndex, int fillValColIndex)
	{
		List<Triple<Double, Double, String>> listOfLocations = new ArrayList<>();
		List<Double> fillVal = new ArrayList();

		try
		{
			List<List<String>> lines = nColumnReaderStringLargeFileSelectedColumns(
					new FileInputStream(new File(absFileNameForLatLong)), ",", true, false,
					new int[] { latColIndex, lonColIndex, labelColIndex, fillValColIndex });

			System.out.println("lines.size()=" + lines.size());

			int count = 0;

			for (List<String> line : lines)
			{
				count += 1;

				if (count == 1)
				{
					continue;
				}
				if (++count > 20000)
				{
					break;
				}
				// System.out.println("line= " + line);
				// System.out.println("here 1");

				Triple<Double, Double, String> val = new Triple<>(Double.valueOf(line.get(0)),
						Double.valueOf(line.get(1)), "id=" + line.get(2));

				fillVal.add(Double.valueOf(line.get(3)));

				// LatLong markerLatLong2 = new LatLong(-1.6073826, 67.9382483);// 47.606189, -122.335842);
				// // Double.valueOf(line.get(2).substring(0, 4)));
				// // LatLong markerLatLong2 = new LatLong(Double.valueOf(line.get(3).substring(0, 4)),
				// // Double.valueOf(line.get(2).substring(0, 4)));
				// System.out.println("LatLong= " + markerLatLong2.toString());
				// markerOptions2.position(markerLatLong2).title(line.get(1)).visible(true);
				// System.out.println("here2");
				// myMarker2 = new Marker(markerOptions2);
				// System.out.println("here3");
				listOfLocations.add(val);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// StringBuilder sb = new StringBuilder();
		// listOfMarkers.stream().forEachOrdered(e -> sb.append(e.toString() + "\n"));
		// System.out.println("List of markers= " + sb.toString());
		// System.out.println("listOfLocations.size()=" + listOfLocations.size());
		return null;// new Pair<List<Triple<Double, Double, String>>, Double>(listOfLocations, fillVal);
	}
}
