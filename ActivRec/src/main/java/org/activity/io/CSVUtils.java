package org.activity.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.StatUtils;

import com.github.mgunlogson.cuckoofilter4j.CuckooFilter;
import com.google.common.hash.Funnels;

import gnu.trove.set.hash.THashSet;

public class CSVUtils
{

	public static void temp()
	{
		String commonPath = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/gowalla1_MAY26NGramAnalysis_AllUsers/";

		List<String> userIDs = ReadingFromFile.oneColumnReaderString(
				"/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/gowalla1_MAY26NGramAnalysis_AllUsers/gowalla1NumOfDaysInCleanedTimelines.csv",
				",", 2, true);

		for (int n = 1; n <= 20; n++)
		{
			for (String u : userIDs)
			{
				String fileNameToRead = commonPath + n + "gram" + u + "FreqDist.csv";
				System.out.println(fileNameToRead);

				ArrayList<String> vals = (ArrayList<String>) ReadingFromFile.oneColumnReaderString(fileNameToRead, ",",
						1, true);

			}
		}

	}

	public static void splitCSVRowise(String originCSVFile, String delimiter, boolean hasHeader, int numOfSplittedFiles,
			String pathToWrite, String fileNamePhraseToWrite)
	{
		// List<CSVRecord> allLines = CSVUtils.getCSVRecords(originCSVFile, delimiter);
		// CSVRecord header = null; // = allLines.get(0);
		List<List<String>> allLines = null;
		List<String> header = null;
		try
		{
			// allLines = ReadingFromFile.nColumnReaderStringLargeFileSelectedColumns(new
			// FileInputStream(originCSVFile),
			// delimiter, hasHeader, false, new int[] { 0,1, 2, 3 });

			allLines = ReadingFromFile.nColumnReaderStringLargeFile(new FileInputStream(originCSVFile), delimiter,
					hasHeader, false);

			int numOfAdditionalLinesForNewHeaders = 0;
			if (hasHeader)
			{
				header = allLines.get(0);

				System.out.println("Header = " + header.toString());
				numOfAdditionalLinesForNewHeaders = numOfSplittedFiles - 1;
				allLines.remove(0);
			}

			int numOfLinesForEachFile = (int) (allLines.size() + numOfAdditionalLinesForNewHeaders)
					/ numOfSplittedFiles;

			int lineNum = 0, fileNum = 0;
			String absFileNameToWrite = "";
			StringBuilder sb = new StringBuilder();
			for (List<String> line : allLines)
			{

				lineNum += 1;
				// System.out.println("lineNum=" + lineNum + " fileNum=" + fileNum);
				// start of line
				if (lineNum == 1 || (lineNum % (numOfLinesForEachFile + 1) == 0))
				{
					fileNum += 1;
					absFileNameToWrite = pathToWrite + fileNamePhraseToWrite + fileNum + ".csv";

					if (hasHeader)
					{
						WToFile.writeToNewFile(String.join(",", header) + "\n", absFileNameToWrite);
					}
					// continue;
				}
				sb.append(String.join(",", line) + "\n");

				// end of line
				if (lineNum % (numOfLinesForEachFile) == 0)
				{
					WToFile.appendLineToFileAbs(sb.toString() + "\n", absFileNameToWrite);
					System.out.println("Wrote file: " + fileNum);
					sb.setLength(0);
				}
			}
			if (sb.length() != 0)
			{
				WToFile.appendLineToFileAbs(sb.toString() + "\n", absFileNameToWrite);
				System.out.println("Wrote file: " + fileNum);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		// ##removeDuplicateRowsFromRawFoursquareNY();
		// $$ concatenateFetchedTimezones();
		// splitCSVRowise("/home/gunjan/JupyterWorkspace/data/gowalla_spots_subset1_fromRaw28Feb2018.csv", ",", true,
		// 10, "/home/gunjan/JupyterWorkspace/data/", "gowalla_spots_subset1_fromRaw28Feb2018smallerFile");
		// temp();
		testSideConcat();
		// removeDuplicateRowsGowalla();
		// $$removeDuplicateRowsFromRawGowalla();// IMPORTANT
		// //$$removeDuplicationRowsUsingCuckoo(
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/ConsecutiveDiffAnalysis/RemoveDups/sbAllDistanceInM.csv",
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/ConsecutiveDiffAnalysis/RemoveDups/NoDupsbAllDistanceInM.csv",
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/ConsecutiveDiffAnalysis/RemoveDups/AllDupssbAllDistanceInM.csv");

		// $$removeDuplicationRows(
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/ConsecutiveDiffAnalysis/RemoveDups/sbAllDistanceInM.csv",
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/ConsecutiveDiffAnalysis/RemoveDups/SNoDupsbAllDistanceInM.csv",
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/ConsecutiveDiffAnalysis/RemoveDups/SAllDupssbAllDistanceInM.csv");

		// gowallaMain();// gowallaMain2();//
	}

	private static void concatenateFetchedTimezones()
	{
		ArrayList<String> fileNamesToConcatentate = new ArrayList<>();
		try
		{
			String commonPath = "/home/gunjan/JupyterWorkspace/data/d10/gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ";
			for (int i = 1; i <= 10; i++)
			{
				fileNamesToConcatentate.add(commonPath + i + ".csv");
			}
			CSVUtils.concatenateCSVFilesV2(fileNamesToConcatentate, true, commonPath + "AllConcatenated.csv", ',');
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void testSideConcat()
	{

		ArrayList<String> fileNamesToConcat = new ArrayList<String>();
		fileNamesToConcat.add("/home/gunjan/test/gunjan.csv");
		fileNamesToConcat.add("/home/gunjan/test/manali.csv");
		fileNamesToConcat.add("/home/gunjan/test/galadriel.csv");

		// fileNamesToConcat.add("/home/gunjan/tempConcat1.csv");
		// fileNamesToConcat.add("/home/gunjan/tempConcat2.csv");

		concatenateCSVFilesSideways(fileNamesToConcat, false, "/home/gunjan/test/concat.csv");
	}

	public static void removeDuplicateRowsGowalla()
	{
		// curtain 1 start
		String processedCheckInFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/processedCheckIns.csv";
		String noDupProcessedCheckinFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/NoDuplicateprocessedCheckIns.csv";
		String dupLinesCheckinFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/DupLinesFromProcessedCheckins.csv";

		// $$ check again to make sure this method is not doing anything more than the cuckoo method is doing
		// removeDuplicationRowsInPreVicinity(processedCheckInFileName, noDupProcessedCheckinFileName,
		// dupLinesCheckinFileName, 0);

		removeDuplicationRowsUsingCuckoo(processedCheckInFileName, noDupProcessedCheckinFileName,
				dupLinesCheckinFileName);
		// UsingCuckoo
		// curtain 1 end
	}

	public static void removeDuplicateRowsFromRawGowalla()
	{
		// curtain 1 start
		String checkInFileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_checkins.csv";
		// Disabled on Mar15_2018 String noDupCheckinFileName =
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/RemovingDuplicatesFromRawData/NoDup_gowalla_checkinsRaw.csv";
		// Disabled on Mar15_2018 String dupLinesCheckinFileName =
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/RemovingDuplicatesFromRawData/DupLines_gowalla_checkinsRaw.csv";

		String noDupCheckinFileName = "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/Mar15/RemovingDuplicatesFromRawData/NoDup_gowalla_checkinsRaw.csv";
		String dupLinesCheckinFileName = "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/Mar15/RemovingDuplicatesFromRawData/DupLines_gowalla_checkinsRaw.csv";

		// $$ check again to make sure this method is not doing anything more than the cuckoo method is doing
		// removeDuplicationRowsInPreVicinity(processedCheckInFileName, noDupProcessedCheckinFileName,
		// dupLinesCheckinFileName, 0);

		removeDuplicationRowsUsingCuckoo(checkInFileName, noDupCheckinFileName, dupLinesCheckinFileName);
		// UsingCuckoo
		// curtain 1 end
	}

	/**
	 * @since 14 Sep 2018
	 */
	public static void removeDuplicateRowsFromRawFoursquareNY()
	{
		// curtain 1 start
		String checkInFileName = "/run/media/gunjan/BackupVault/DatasetsInBackupVault/FoursquareDatasets/dataset_tsmc2014/dataset_TSMC2014_NYC_Processed1.txt";
		String noDupCheckinFileName = "/run/media/gunjan/BackupVault/DatasetsInBackupVault/FoursquareDatasets/dataset_tsmc2014/dataset_TSMC2014_NYC_Processed1_NoDup.txt";
		String dupLinesCheckinFileName = "/run/media/gunjan/BackupVault/DatasetsInBackupVault/FoursquareDatasets/dataset_tsmc2014/dataset_TSMC2014_NYC_DuplicateLines.txt";
		;

		// $$ check again to make sure this method is not doing anything more than the cuckoo method is doing
		// removeDuplicationRowsInPreVicinity(processedCheckInFileName, noDupProcessedCheckinFileName,
		// dupLinesCheckinFileName, 0);

		removeDuplicationRowsUsingCuckoo(checkInFileName, noDupCheckinFileName, dupLinesCheckinFileName);
		// UsingCuckoo
		// curtain 1 end
	}

	/**
	 * 
	 */
	public static void gowallaMain()
	{
		String[] fileNameHeadStrings = { "", "BO" };

		for (String fileNameHeadString : fileNameHeadStrings)
		{
			// String fileNameHeadString = "";// "BO";// for baseline occurrence file, empty for algo file

			String commonPathToRead = "./DD/Target/";
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_";
			String pathToWrite = "./DD/Target/";
			// + "///home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Analysis2/";
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/";

			String s[] = { "1", "101", "201", "301", "401", "501", "601", "701", "801", "901" };

			ArrayList<String> listOfAllMRRFiles = new ArrayList<String>();
			ArrayList<String> listOfAllMUsWithMaxMRRFiles = new ArrayList<String>();
			ArrayList<String> listOfAllCountsForClusterLabelAccToMinMUHavMaxMRRFiles = new ArrayList<String>();
			ArrayList<String> listOfAllCountsForClusterLabelAccToMajorityMUsHavMaxMRRFiles = new ArrayList<String>();
			ArrayList<String> listOfAllModeDistributionForClusterLabelAccToMinMUHavMaxMRRFiles = new ArrayList<String>();
			ArrayList<String> listOfAllModeDistributionForClusterLabelAccToMajorityMUsHavMaxMRRFiles = new ArrayList<String>();

			ArrayList<String> listOfrrCOlFiles = new ArrayList<String>();
			// ArrayList<String> listOfAllMRRFiles = new ArrayList<String>();

			for (int i = 0; i < s.length; i++)
			{
				String pathToRead = commonPathToRead + s[i] + "/CLUSTERING2/";

				listOfAllMRRFiles.add(pathToRead + fileNameHeadString + "AllMRR.csv");
				listOfrrCOlFiles.add(pathToRead + fileNameHeadString + "rrValsForBestMUCol.csv");
				listOfAllMUsWithMaxMRRFiles.add(pathToRead + fileNameHeadString + "MUsWithMaxMRR.csv");
				listOfAllCountsForClusterLabelAccToMinMUHavMaxMRRFiles
						.add(pathToRead + fileNameHeadString + "CountsForClusterLabelAccToMinMUHavMaxMRR.csv");
				listOfAllCountsForClusterLabelAccToMinMUHavMaxMRRFiles
						.add(pathToRead + fileNameHeadString + "CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
				listOfAllModeDistributionForClusterLabelAccToMinMUHavMaxMRRFiles.add(
						pathToRead + fileNameHeadString + "ModeDistributionForClusterLabelAccToMinMUHavMaxMRR.csv");
				listOfAllModeDistributionForClusterLabelAccToMajorityMUsHavMaxMRRFiles.add(pathToRead
						+ fileNameHeadString + "ModeDistributionForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
			}

			concatenateCSVFilesSideways(listOfAllMRRFiles, true, pathToWrite + fileNameHeadString + "AllMRR.csv");
			concatenateCSVFiles(listOfAllMUsWithMaxMRRFiles, true,
					pathToWrite + fileNameHeadString + "MUsWithMaxMRR.csv");
			concatenateCSVFiles(listOfAllCountsForClusterLabelAccToMinMUHavMaxMRRFiles, true,
					pathToWrite + fileNameHeadString + "CountsForClusterLabelAccToMinMUHavMaxMRR.csv");
			concatenateCSVFiles(listOfAllCountsForClusterLabelAccToMinMUHavMaxMRRFiles, true,
					pathToWrite + fileNameHeadString + "CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
			concatenateCSVFiles(listOfAllModeDistributionForClusterLabelAccToMinMUHavMaxMRRFiles, true,
					pathToWrite + fileNameHeadString + "ModeDistributionForClusterLabelAccToMinMUHavMaxMRR.csv");
			concatenateCSVFiles(listOfAllModeDistributionForClusterLabelAccToMajorityMUsHavMaxMRRFiles, true,
					pathToWrite + fileNameHeadString + "ModeDistributionForClusterLabelAccToMajorityMUsHavMaxMRR.csv");

			concatenateCSVFiles(listOfrrCOlFiles, true, pathToWrite + fileNameHeadString + "ListOfrrColFiles.csv");

			// concatenateCSVFiles(ArrayList<String>, boolean, String)
		}
	}

	public static void gowallaMainDaywise()
	{

		// String fileNameHeadString = "";// "BO";// for baseline occurrence file, empty for algo file

		String commonPathToRead = "./DD/Target/";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_";
		String pathToWrite = "./DD/Target/";
		// + "///home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Analysis2/";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/";

		String s[] = { "1", "101", "201", "301", "401", "501", "601", "701", "801", "901" };

		ArrayList<String> listOfAllMRRFiles = new ArrayList<String>();
		ArrayList<String> listOfrrCOlFiles = new ArrayList<String>();
		// ArrayList<String> listOfAllMRRFiles = new ArrayList<String>();

		for (int i = 0; i < s.length; i++)
		{
			String pathToRead = commonPathToRead + s[i];

			listOfAllMRRFiles.add(pathToRead + "/AlgoAllMeanReciprocalRank.csv");
			listOfrrCOlFiles.add(pathToRead + "rrValsForBestMUCol.csv");
		}

		concatenateCSVFilesSideways(listOfAllMRRFiles, true, pathToWrite + "AllMRR.csv");
		concatenateCSVFiles(listOfrrCOlFiles, true, pathToWrite + "ListOfrrColFiles.csv");

	}

	public static void gowallaMain2()
	{
		// String[] fileNameHeadStrings = { "", "BO" };

		// for (String fileNameHeadString : fileNameHeadStrings)
		{
			// String fileNameHeadString = "";// "BO";// for baseline occurrence file, empty for algo file

			String commonPathToRead = "./DD/Target/";
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_";
			String pathToWrite = "./DD/Target/";
			// + "///home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Analysis2/";
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/";

			String s[] = { "1", "101", "201", "301", "401", "501", "601", "701", "801", "901" };

			ArrayList<String> listOfAllMRRFiles = new ArrayList<String>();

			for (int i = 0; i < s.length; i++)
			{
				String pathToRead = commonPathToRead + s[i] + "/";

				listOfAllMRRFiles.add(pathToRead + "CountTimeCategoryOfRecommPoitns.csv");
			}

			concatenateCSVFiles(listOfAllMRRFiles, true, pathToWrite + "AllCountTimeCategoryOfRecommPoitns.csv");

		}
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
		BufferedWriter bwDup = WToFile.getBWForNewFile(duplicateLinesFileName);
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
					WToFile.appendLineToFileAbs(uniqueLines.toString(), outputFileName);
					// bw.write(uniqueLines.toString());
					uniqueLines.setLength(0);
				}

				if (allLinesCount % 200000 == 0)
				{
					System.out.println("lines read: " + allLinesCount);
				}
			}

			WToFile.appendLineToFileAbs(uniqueLines.toString(), outputFileName);
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
	public static void removeDuplicationRowsTrove(String inputFileName, String outputFileName,
			String duplicateLinesFileName)
	{
		System.out.println("Entering removeDuplicationRows");

		THashSet<String> allUniqueEntries = new THashSet<String>();
		BufferedReader br = null;
		BufferedWriter bw = WToFile.getBWForNewFile(outputFileName);
		BufferedWriter bwDup = WToFile.getBWForNewFile(duplicateLinesFileName);
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
	public static void removeDuplicationRowsInPreVicinity(String inputFileName, String outputFileName,
			String duplicateLinesFileName, int previcinity)
	{
		System.out.println("Entering removeDuplicationRowsInPreVicinity");

		ArrayList<String> allUniqueEntries = new ArrayList<String>();
		BufferedReader br = null;
		// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(outputFileName);
		BufferedWriter bwDup = WToFile.getBWForNewFile(duplicateLinesFileName);
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
					WToFile.appendLineToFileAbs(uniqueLines.toString(), outputFileName);
					// bw.write(uniqueLines.toString());
					uniqueLines.setLength(0);
				}

				if (allLinesCount % 100000 == 0)
				{
					System.out.println("lines read: " + allLinesCount);
				}
			}

			WToFile.appendLineToFileAbs(uniqueLines.toString(), outputFileName);
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
		System.out.println("Exiting removeDuplicationRowsInPreVicinity");
	}

	/**
	 * 
	 * @param listOfAbsFileNames
	 * @param hasColumnHeader
	 *            to make sure columnHeadersAreNotRepeated
	 */
	public static void concatenateCSVFilesOriginal(ArrayList<String> listOfAbsFileNames, boolean hasColumnHeader,
			String absfileToWrite)
	{
		int countOfFiles = 0, countOfTotalLines = 0;
		try
		{
			for (String fileToRead : listOfAbsFileNames)
			{
				countOfFiles += 1;
				List<CSVRecord> csvRecords = CSVUtils.getCSVRecords(fileToRead);

				// System.out.println("read records from " + fileToRead + " are :");

				BufferedWriter bw = WToFile.getBufferedWriterForExistingFile(absfileToWrite);
				CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT);

				int countOfLines = 0;
				for (CSVRecord r : csvRecords)
				{
					countOfLines += 1;

					// Changed on 25 June
					// if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for
					if (hasColumnHeader && countOfLines == 1) // dont write the header for
																// non-first files
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
			System.out.println("Concatenated file: " + absfileToWrite);
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
	 * @param absfileToWrite
	 */
	public static void concatenateCSVFiles(ArrayList<String> listOfAbsFileNames, boolean hasColumnHeader,
			String absfileToWrite)
	{
		int countOfFiles = 0, countOfTotalLines = 0;

		try
		{
			Files.deleteIfExists(Paths.get(absfileToWrite));

			for (String fileToRead : listOfAbsFileNames)
			{
				countOfFiles += 1;
				List<CSVRecord> csvRecords = CSVUtils.getCSVRecords(fileToRead);

				// System.out.println("read records from " + fileToRead + " are :");

				BufferedWriter bw = WToFile.getBufferedWriterForExistingFile(absfileToWrite);
				CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT);

				int countOfLines = 0;
				for (CSVRecord r : csvRecords)
				{
					countOfLines += 1;

					// added back again on 19 July 2018
					if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1)// TODO check for correct behaviour
					{// dont write the header for non-first files
						continue;
					}
					// Changed on 25 June
					// if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for
					// if (hasColumnHeader && countOfLines == 1) // dont write the header for
					// // non-first files
					// {
					// continue;
					// }
					// System.out.println(r.toString());
					printer.printRecord(r);

				}
				System.out.println(countOfLines + " lines read for this user");
				countOfTotalLines += countOfLines;

				printer.close();
			}
			System.out.println("Concatenated file: " + absfileToWrite);
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
	public static void concatenateCSVFilesSideways(ArrayList<String> listOfAbsFileNames, boolean hasRowHeader,
			String absfileToWrite)
	{
		concatenateCSVFilesSideways(listOfAbsFileNames, hasRowHeader, absfileToWrite, "", false);
	}

	/**
	 * 
	 * @param listOfAbsFileNames
	 * @param hasRowHeader
	 * @param absfileToWrite
	 * @param interFileDelimiter
	 * @param writeNewFile
	 */
	public static void concatenateCSVFilesSideways(ArrayList<String> listOfAbsFileNames, boolean hasRowHeader,
			String absfileToWrite, String interFileDelimiter, boolean writeNewFile)
	{
		// read each file
		// store all lines in a list, size of list = num of rows
		// read new file. add string from corresponding lines
		int countOfFiles = 0, countOfTotalLines = 0;

		// if (hasRowHeader)
		// {
		// PopUps.showException(new Exception("row header not allowed"),
		// "org.activity.util.CSVUtils.concatenateCSVFilesSideways(ArrayList<String>, boolean, String)");
		// }
		LinkedHashMap<Integer, String> finalLines = new LinkedHashMap<Integer, String>();
		try
		{
			for (String fileToRead : listOfAbsFileNames)
			{
				BufferedReader br = new BufferedReader(new FileReader(fileToRead));

				int lineIndex = 0;
				String currentLine;
				while ((currentLine = br.readLine()) != null)
				{
					String res = "";

					if (hasRowHeader)
					{
						String[] splitted = currentLine.split(",");

						StringBuilder trimmedCurrentLine = new StringBuilder();
						for (int i = 1; i < splitted.length; i++)
						{
							trimmedCurrentLine.append(splitted[i] + ",");
						}
						currentLine = trimmedCurrentLine.toString();
					}

					if (finalLines.containsKey(lineIndex))
					{
						String prev = finalLines.get(lineIndex);
						res = prev + interFileDelimiter + currentLine;
					}
					else
					{
						res = currentLine;
					}

					finalLines.put(lineIndex, res);
					lineIndex += 1;
				}
				br.close();
			}

			BufferedWriter bw = writeNewFile ? WToFile.getBWForNewFile(absfileToWrite)
					: WToFile.getBufferedWriterForExistingFile(absfileToWrite);
			for (Entry<Integer, String> l : finalLines.entrySet())
			{
				bw.write(l.getValue().toString() + "\n");
			}
			bw.close();
		}
		// try
		// {
		// for (String fileToRead : listOfAbsFileNames)
		// {
		// countOfFiles += 1;
		//
		// List<CSVRecord> csvRecords = CSVUtils.getCSVRecords(fileToRead);
		// int numOfRow = csvRecords.size();
		//
		// // System.out.println("read records from " + fileToRead + " are :");
		//
		// BufferedWriter bw = WritingToFile.getBufferedWriterForExistingFile(absfileToWrite);
		// CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT);
		//
		// int countOfLines = 0;
		// int maxNumOfColumns = 0;
		//
		// // find the max number of columns in this file
		// for (CSVRecord r : csvRecords)
		// {
		// countOfLines += 1;
		//
		// if (r.size() > maxNumOfColumns)
		// maxNumOfColumns = r.size();
		//
		// // if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for non-first
		// files
		// // {
		// // continue;
		// // }
		// // // System.out.println(r.toString());
		// // printer.printRecord(r);
		//
		// }
		// System.out.println(countOfLines + " lines read for this user" + " maxNumOfCoulumns = " + maxNumOfColumns);
		// countOfTotalLines += countOfLines;
		//
		// printer.close();
		// }
		// }

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
	public static void concatenateCSVFilesV2(ArrayList<String> listOfAbsFileNames, boolean hasColumnHeader,
			String absfileToWrite, char delimiter)
	{
		int countOfFiles = 0, countOfTotalLines = 0;
		try
		{
			for (String fileToRead : listOfAbsFileNames)
			{
				countOfFiles += 1;
				List<CSVRecord> csvRecords = CSVUtils.getCSVRecords(fileToRead, delimiter);

				// System.out.println("read records from " + fileToRead + " are :");

				BufferedWriter bw = WToFile.getBufferedWriterForExistingFile(absfileToWrite);
				CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT.withDelimiter(delimiter).withQuote(null));

				int countOfLines = 0;
				for (CSVRecord r : csvRecords)
				{
					countOfLines += 1;

					if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for
																					// non-first files
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
	 * 
	 * @param absfileToWrite
	 * @param delimiter
	 * @param rowHeaderPerFile
	 * @param headerForRowHeader
	 * @since May 18 2018
	 */
	public static void concatCSVFilesWithRowHeaderPerFile(ArrayList<String> listOfAbsFileNames, boolean hasColumnHeader,
			String absfileToWrite, char delimiter, ArrayList<String> rowHeaderPerFile, String headerForRowHeader)
	{
		int countOfFiles = 0, countOfTotalLines = 0;
		try
		{
			for (String fileToRead : listOfAbsFileNames)
			{
				countOfFiles += 1;
				List<CSVRecord> csvRecords = CSVUtils.getCSVRecords(fileToRead, delimiter);

				// System.out.println("read records from " + fileToRead + " are :");

				BufferedWriter bw = WToFile.getBufferedWriterForExistingFile(absfileToWrite);
				CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT.withDelimiter(delimiter).withQuote(null));

				int countOfLines = 0;
				for (CSVRecord r : csvRecords)
				{
					countOfLines += 1;

					// dont write the header for non-first files
					if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1)
					{
						continue;
					}
					// System.out.println(r.toString());

					ArrayList<String> valsToWriteForThisRow = new ArrayList<>();

					if (hasColumnHeader && countOfFiles == 1 && countOfLines == 1)
					{
						valsToWriteForThisRow.add(headerForRowHeader);
					}
					else// for non header lines only
					{
						valsToWriteForThisRow.add(rowHeaderPerFile.get(countOfFiles - 1));
					}

					for (int i = 0; i < r.size(); i++)
					{
						valsToWriteForThisRow.add(r.get(i));
					}
					printer.printRecord(valsToWriteForThisRow);

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
	public static ArrayList<String> splitCSVFilesByColumnValueNoHeader(String absFileToSplit, char delimiter,
			int columnNumForSplit, String pathToWrite)
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
					bw = WToFile.getBWForNewFile(pathToWrite + "Split" + splitID + ".csv");
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
					bw = WToFile.getBWForNewFile(pathToWrite + "Split" + splitID + ".csv");
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
			// if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for non-first
			// files
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
					bw = WToFile.getBWForNewFile(pathToWrite + "Split" + splitID + ".csv");
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
					bw = WToFile.getBWForNewFile(pathToWrite + "Split" + splitID + ".csv");
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
			// if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1) // dont write the header for non-first
			// files
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
	public static String getCellValueFromCSVFile(int rowNum, int colNum, String filenameToRead)// , boolean
																								// hasColHeader, boolean
																								// hasRowHeader)
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
			// PopUps.showException(new Exception("Error in getCellValueFromCSVFile: rowNum=" + rowNum + " colNum=" +
			// colNum
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
	// public static List<String> twoColumnReaderString(String fileNameToRead, String delimiter, int columnIndex1, int
	// columnIndex2, boolean hasHeader)
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
	// raw = br.lines().skip(1).map((String s) ->
	// (s.split(Pattern.quote(delimiter))[columnIndex])).collect(Collectors.toList());
	// }
	// else
	// {
	// raw = br.lines().map((String s) ->
	// (s.split(Pattern.quote(delimiter))[columnIndex])).collect(Collectors.toList());
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
	public static void removeDuplicationRowsUsingCuckoo(String inputFileName, String outputFileName,
			String duplicateLinesFileName)
	{
		System.out.println("Entering removeDuplicationRowsUsingCuckoo");

		// create
		CuckooFilter<CharSequence> allUniqueEntries = new CuckooFilter.Builder<>(
				Funnels.stringFunnel(Charset.defaultCharset()), 37000000).withFalsePositiveRate(0.0000000000001)
						.build();// 0.0000000000001

		// HashSet<String> allUniqueEntries = new HashSet<String>();
		BufferedReader br = null;
		BufferedWriter bw = WToFile.getBWForNewFile(outputFileName);
		BufferedWriter bwDup = WToFile.getBWForNewFile(duplicateLinesFileName);
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
						String msg = "Insert failed on cuckoo filter. allLinesCount = " + allLinesCount;
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
		System.out.println("Exiting removeDuplicationRowsUsingCuckoo");
	}

	/**
	 * Write the max cell value over the csv files reads
	 * 
	 * @param absCSVFileNamesToRead
	 * @param absFileNameToWrite
	 * @param beginRow
	 *            start from 1
	 * @param endRow
	 * @param beginCol
	 *            start from 1
	 * @param endCol
	 */
	public static void writeMaxCellOverCSVFiles(String[] absCSVFileNamesToRead, String absFileNameToWrite, int beginRow,
			int endRow, int beginCol, int endCol, String fileToReadForNullifyingZeros)
	{
		int numOfFiles = absCSVFileNamesToRead.length;
		BufferedWriter bw = WToFile.getBWForNewFile(absFileNameToWrite);
		BufferedWriter bwMU = WToFile
				.getBWForNewFile(absFileNameToWrite.substring(0, absFileNameToWrite.length() - 4) + "MU.csv");// stores
																												// the
																												// value
																												// of
																												// max
																												// mu
		try
		{
			for (int row = beginRow; row <= endRow; row++)
			{
				for (int col = beginCol; col <= endCol; col++)
				{
					double valuesForThisCellPosition[] = new double[numOfFiles];
					int fileReadCounter = 0;
					for (String fileToRead : absCSVFileNamesToRead)
					{
						String val = getCellValueFromCSVFile(row, col, fileToRead);

						if (val.length() == 0 || val == null)
						{
							valuesForThisCellPosition[fileReadCounter] = -999;
						}
						else
						{
							valuesForThisCellPosition[fileReadCounter] = Double.valueOf(val);
						}
						System.out.println("reading file: " + fileToRead + " value at (" + row + "," + col + ") = "
								+ valuesForThisCellPosition[fileReadCounter]);
						fileReadCounter++;
					}

					double maxOfCellsAtThisPosition = StatUtils.max(valuesForThisCellPosition);
					String maxOfCellsAtThisPositionString = String.valueOf(maxOfCellsAtThisPosition);

					if (maxOfCellsAtThisPosition == 0)
					{ // check if there were no RTs
						// int numOfRTs = 0;

						String whetherThisCellPosIsValid = getCellValueFromCSVFile(row, col,
								fileToReadForNullifyingZeros);

						// numOfRTs = Integer.valueOf(ReadingFromFile.getCellValueFromCSVFile(row, col,
						// fileToReadForNullifyingZeros));

						if (whetherThisCellPosIsValid.equals("0") || whetherThisCellPosIsValid.equals("")
								|| whetherThisCellPosIsValid == null)// (numOfRTs == 0)
						{
							maxOfCellsAtThisPositionString = "NA";
						}
					}

					bw.write(maxOfCellsAtThisPositionString);
					// bwMU.write(Arrays.toString(UtilityBelt.findLargeNumberIndices(valuesForThisCellPosition)));

					// bwMU.write(Arrays.toString(UtilityBelt.findLargeNumberIndices(valuesForThisCellPosition)));
					// String.join("_", UtilityBelt.findLargeNumberIndices(valuesForThisCellPosition));
					if (maxOfCellsAtThisPositionString.equals("NA"))// (maxOfCellsAtThisPosition == 0)
					{
						bwMU.write("NA");
					}
					else
						bwMU.write(StringUtils.getArrayAsStringDelimited(
								ComparatorUtils.findLargeNumberIndices(valuesForThisCellPosition), "_"));
					if (col != endCol)
					{
						bw.write(",");
						bwMU.write(",");
					}
					else
					{
						bw.newLine();
						bwMU.newLine();
					}
				}
			}
			bw.close();
			bwMU.close();
		}
		catch (Exception e)
		{
			PopUps.showException(e, "writeMaxCellOverCSVFiles");
			e.printStackTrace();
		}

		// BufferedReader brRR = new BufferedReader(new FileReader(commonPath + fileNamePhrase + timeCategory +
		// "ReciprocalRank.csv"));
	}

	/**
	 * 
	 * @param s
	 * @param delimiter
	 * @return
	 */
	public static String CSVRecordToString(CSVRecord s, String delimiter)
	{
		String res = "";
		for (int i = 0; i < s.size(); i++)
		{
			res += s.get(i) + delimiter;
		}
		return res.substring(0, res.length() - delimiter.length());// a,b,c,d, // len = 8 (0,7)
	}

	// notes: ll threes csv files have same number of rows and columns
	public static void CompareCSV(String[] args)
	{
		BufferedReader br1 = null, br2 = null, brMeta = null;

		int rowCount = 0;
		int columnCount = 0;

		String fileName1 = "/home/gunjan/MATLAB/bin/DCU data works/July20/Copy 1 aug global 550/dataRankedRecommendationWithScores.csv";
		String fileName2 = "/home/gunjan/MATLAB/bin/DCU data works/July20/Copy 1 aug global 500/dataRankedRecommendationWithScores.csv";
		String fileNameMeta = "/home/gunjan/MATLAB/bin/DCU data works/July20/Copy 1 aug global 500/meta.csv";

		System.out.println("File 1 is:" + fileName1);
		System.out.println("File 2 is:" + fileName2 + "\n");

		try
		{
			br1 = new BufferedReader(new FileReader(fileName1));
			br2 = new BufferedReader(new FileReader(fileName2));
			brMeta = new BufferedReader(new FileReader(fileNameMeta));

			String currentLine1, currentLine2, currentLineMeta;

			while ((currentLineMeta = brMeta.readLine()) != null)
			{
				rowCount++;

				currentLine1 = br1.readLine();
				currentLine2 = br2.readLine();

				if (currentLine1.equals(currentLine2))
				{
					System.out.println("User " + rowCount + " is same");
				}
				else
				{
					System.out.println("User " + rowCount + " is different");

					String columns1[] = currentLine1.split(Pattern.quote(","));
					String columns2[] = currentLine2.split(Pattern.quote(","));
					String columnsMeta[] = currentLineMeta.split(Pattern.quote(","));

					System.out.println("count colums1=" + columns1.length + " count columns2=" + columns2.length
							+ " count meta columns=" + columnsMeta.length);
					for (int j = 0; j < columns2.length; j++)
					{
						if (columns1[j].equals(columns2[j]))
						{

						}
						else
						{
							System.out.println("\tcolumns " + (j + 1) + " is different.");// Timestamp="+columnsMeta[j]);
							System.out.println("\t\tfile 1 is:" + columns1[j]);
							System.out.println("\t\tfile 2 is:" + columns2[j]);
						}
					}

				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br1 != null) br1.close();
				if (br2 != null) br2.close();
				if (brMeta != null) brMeta.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}

	}
}
