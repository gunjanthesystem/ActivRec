package org.activity.evaluation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.SFTPFile;
import org.activity.io.WToFile;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;

import com.jcraft.jsch.Session;

/**
 * To do signficance tests on results over users
 * 
 * @since 25 Jan 2018
 * @author gunjan
 *
 */
public class ResultsDistributionEvaluation
{
	static final int port = 22;
	// static final int firstToMax = 3;

	public ResultsDistributionEvaluation()
	{
	}

	public static void main(String args[])
	{
		String resultsLabelsPathFileToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsApril30ToRead_2.csv";// ResultsApril26ToRead_2.csv";
		String pathToRead = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY2ResultsDistributionFirstToMax3/FiveDays/";
		if (false)
		{
			fetchResultsFromServersInFormat(resultsLabelsPathFileToRead);
			processResultsFromDifferenceSetsOfUsers(pathToRead);
		}

		splitUsersMUZeroNonZeroGroup(pathToRead
				+ "/Concatenated/ConcatenatedED0.5STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrsRTV_AllPerDirectTopKAgreements_MinMUWithMaxFirst0Aware.csv",
				pathToRead + "Concatenated/");

	}

	public static void processResultsFromDifferenceSetsOfUsers(String commonPath)
	{
		// String commonPath =
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY2ResultsDistributionFirstToMax3/FiveDays/";
		String commonPathToWrite = commonPath + "Concatenated/";
		WToFile.createDirectoryIfNotExists(commonPathToWrite);

		String fileToSort = commonPath + "GTE100UserLabels.csv";
		String sortedFileToWrite = commonPathToWrite + "GTE100UserLabelsSorted.csv";
		String uniqueConfigsFileToWrite = commonPathToWrite + "GTE100UserLabelsUniqueConfigs.csv";

		sortIgnoringDates(fileToSort, sortedFileToWrite);
		Set<String> uniqueConfigs = findUniqueConfigs(fileToSort, uniqueConfigsFileToWrite);

		concatenateResultsFromAllSets(uniqueConfigs, commonPath, commonPathToWrite);
		// findGroupsOfUsersBasedOnBestMU(uniqueConfigs, commonPath);

	}

	public static void splitUsersMUZeroNonZeroGroup(String splitOnBasisOfThisFile, String pathToWrite)
	{
		List<List<String>> readData = ReadingFromFile.readLinesIntoListOfLists(splitOnBasisOfThisFile, ",");
		System.out.println("removing header: " + readData.remove(0));// remove header
		int indexOfMinMuWithMaxFirst3 = 1;

		Map<String, ArrayList<Pair<String, Integer>>> groupsOfUsers = new LinkedHashMap<>();
		groupsOfUsers.put("MinMUWithMaxFirst3_Zero", new ArrayList<>());
		groupsOfUsers.put("MinMUWithMaxFirst3_GTZero", new ArrayList<>());

		int rowIndex = -1;
		for (List<String> d : readData)
		{
			rowIndex += 1;
			String keyLabel = "";
			if (Integer.valueOf(d.get(indexOfMinMuWithMaxFirst3)) == 0)
			{
				keyLabel = "MinMUWithMaxFirst3_Zero";
			}
			else if (Integer.valueOf(d.get(indexOfMinMuWithMaxFirst3)) > 0)
			{
				keyLabel = "MinMUWithMaxFirst3_GTZero";
			}
			else
			{
				PopUps.showError("Error: d.get(indexOfMinMuWithMaxFirst3) = " + d.get(indexOfMinMuWithMaxFirst3));
			}
			groupsOfUsers.get(keyLabel).add(new Pair<String, Integer>(d.get(0), rowIndex));
		}

		System.out.println("MinMUWithMaxFirst3_Zero.size = " + groupsOfUsers.get("MinMUWithMaxFirst3_Zero").size());
		System.out.println("MinMUWithMaxFirst3_GTZero.size = " + groupsOfUsers.get("MinMUWithMaxFirst3_GTZero").size());

		WToFile.writeToNewFile(groupsOfUsers.get("MinMUWithMaxFirst3_Zero").stream()
				.map(e -> e.getFirst() + "," + e.getSecond()).collect(Collectors.joining("\n")),
				pathToWrite + "MinMUWithMaxFirst3_Zero.csv");
		WToFile.writeToNewFile(groupsOfUsers.get("MinMUWithMaxFirst3_GTZero").stream()
				.map(e -> e.getFirst() + "," + e.getSecond()).collect(Collectors.joining("\n")),
				pathToWrite + "MinMUWithMaxFirst3_GTZero.csv");
	}

	/**
	 * INCOMPLETE
	 * 
	 * @param uniqueConfigs
	 * @param commonPath
	 */
	private static void findGroupsOfUsersBasedOnBestMU(Set<String> uniqueConfigs, String commonPath)
	{
		// List<List<Integer>> groupsOfUsers = new ArrayList<>();
		LinkedHashMap<String, Map<String, List<Integer>>> groupsOfUsers = new LinkedHashMap<>();

		String[] resultLabelTails = { "_AllPerDirectTopKAgreements_.csv" };
		for (String config : uniqueConfigs)
		{
			String concatenatedFileToRead = commonPath + config + resultLabelTails;
			List<List<Double>> res = ReadingFromFile.nColumnReaderDouble(concatenatedFileToRead, ",", true);

			List<Integer> usersWithMUE0 = new ArrayList<>();
			List<Integer> usersWithMUGT0 = new ArrayList<>();

			int userIndex = -1;
			for (List<Double> r : res)
			{
				userIndex++;
				if (r.get(1) == 0)
				{
					usersWithMUE0.add(userIndex);
				}
				else if (r.get(1) > 0)
				{
					usersWithMUGT0.add(userIndex);
				}
				else
				{
					PopUps.showError("Error: unexpected best MU");
				}
			}

			Map<String, List<Integer>> mapOfUserGroups = new LinkedHashMap<>(2);
			mapOfUserGroups.put("usersWithMUE0", usersWithMUE0);
			mapOfUserGroups.put("usersWithMUGT0", usersWithMUGT0);

			// groupsOfUsers.put(config+resultLabelTails, value)
		}

	}

	/**
	 * 
	 * @param fileWithContentsToSort
	 * @param sortedFileToWrite
	 */
	private static void sortIgnoringDates(String fileWithContentsToSort, String sortedFileToWrite)
	{
		List<String> res = ReadingFromFile.oneColumnReaderString(fileWithContentsToSort, ",", 0, false);

		TreeMap<String, String> resSorted = new TreeMap<>(Collections.reverseOrder());
		// since all labels have first 5 characters as date such as APR29
		for (String r : res)
		{
			resSorted.put(r.substring(5), r);
		}

		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> e : resSorted.entrySet())
		{
			System.out.println(e.getKey() + " - " + e.getValue());
			sb.append(e.getValue() + "\n");
		}
		WToFile.writeToNewFile(sb.toString(), sortedFileToWrite);

	}

	private static Set<String> findUniqueConfigs(String fileToSort, String sortedFileToWrite)
	{
		List<String> res = ReadingFromFile.oneColumnReaderString(fileToSort, ",", 0, false);
		String sets[] = { "B", "C", "D", "E" };
		Set<String> uniqueConfigs = new TreeSet<>();
		// since all labels have first 5 characters as date such as APR29
		for (String r : res)
		{
			r = r.replace("LikeRecSys", "");// remove "LikeRecSys" comment in label
			r = r.substring(5);

			for (String set : sets)
			{
				r = r.replace("Set" + set, "");
			}
			uniqueConfigs.add(r);
		}

		StringBuilder sb = new StringBuilder();
		for (String e : uniqueConfigs)
		{
			System.out.println(e);
			sb.append(e + "\n");
		}
		WToFile.writeToNewFile(sb.toString(), sortedFileToWrite);
		return uniqueConfigs;
	}

	/**
	 * 
	 * @param uniqueConfigs
	 * @param commonPathToRead
	 * @param commonPathToWrite
	 */
	private static void concatenateResultsFromAllSets(Set<String> uniqueConfigs, String commonPathToRead,
			String commonPathToWrite)
	{
		String[] resultLabelTails1 = { "_AllPerDirectTopKAgreementsL1_", "_AllPerDirectTopKAgreements_" };
		String[] resultLabelTails2 = { "userMUKeyVals.csv", "MinMUWithMaxFirst3.csv", "MinMUWithMaxFirst0Aware.csv" };

		String sets[] = { "", "B", "C", "D", "E" };

		// get list of files in the current directory
		try
		{
			List<String> pathToAllFiles = Files.list(Paths.get(commonPathToRead)).filter(Files::isRegularFile)
					.map(p -> p.toString()).collect(Collectors.toList());

			for (String config : uniqueConfigs)
			{
				for (String labelTail1 : resultLabelTails1)
				{
					for (String labelTail2 : resultLabelTails2)
					{
						ArrayList<String> filesToConcatenateInOrder = new ArrayList<>();

						for (String set : sets)
						{
							String setLabel = set.length() > 0 ? "Set" + set : "";
							String specialTail = (set.length() == 0 && config.contains("ED-1")) ? "LikeRecSys" : "";

							String resultsLabel = setLabel + config + specialTail + labelTail1 + labelTail2;
							List<String> filenamesContainingResultsLabel = null;
							if (set.length() > 0)
							{
								filenamesContainingResultsLabel = pathToAllFiles.stream()
										.filter(p -> p.contains(resultsLabel)).collect(Collectors.toList());
							}
							else
							{
								filenamesContainingResultsLabel = pathToAllFiles.stream()
										.filter(p -> p.contains(resultsLabel)).filter(p -> p.contains("Set") == false)
										.collect(Collectors.toList());
							}

							System.out.println("resultsLabel= " + resultsLabel + "  num of matching file = "
									+ filenamesContainingResultsLabel.size() + " \n\t"
									+ String.join("\n\t", filenamesContainingResultsLabel));

							if (filenamesContainingResultsLabel.size() != 1)
							{
								PopUps.showError("Error filenamesContainingResultsLabel.size()="
										+ filenamesContainingResultsLabel.size());
							}
							else
							{
								filesToConcatenateInOrder.add(filenamesContainingResultsLabel.get(0));
							}
						} // end of loop over set
						System.out.println("-----------------\nfilesToConcatenateInOrder= \n\t"
								+ String.join("\n\t", filesToConcatenateInOrder) + "\n");
						CSVUtils.concatenateCSVFiles(filesToConcatenateInOrder, true,
								commonPathToWrite + "Concatenated" + config + labelTail1 + labelTail2, ',');
					}
				} // end of loop of resultsLabelTail
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void fetchResultsFromServersInFormat(String resultsLabelsPathFileToRead)
	{
		// runOneDay();
		// runFiveDays();
		// $runFeb2OneDayResults();//disabled on Feb 12 2018
		// $runFeb2FiveDaysResults(); //disabled on Feb 12 2018
		// $runFeb5FiveDaysResults();//disabled on Feb 12 2018
		// runFeb11FiveDaysResults();//disabled on feb 18 2018
		// runFeb17FiveDaysResults();
		// $$runMar9FiveDaysResults();
		runApril10Results(resultsLabelsPathFileToRead, 3);
		SFTPFile.closeAllChannels();
	}

	/**
	 * @param pathToRead2
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runApril10Results(String resultsLabelsPathFileToRead, int firstToMax)
	{

		String pathToWrite = "./dataWritten/" + LocalDateTime.now().getMonth().toString().substring(0, 3)
				+ LocalDateTime.now().getDayOfMonth() + "ResultsDistributionFirstToMax" + firstToMax + "/FiveDays/";// "./dataWritten/Mar9/FiveDays/";
		WToFile.createDirectoryIfNotExists(pathToWrite);
		WToFile.createDirectoryIfNotExists(pathToWrite + "ReadMe/");

		// String resultsLabelsPathFile =
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsApril10ToRead.csv";
		// String resultsLabelsPathFile =
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsApril30ToRead_2.csv";//
		// ResultsApril26ToRead_2.csv";
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCountFixed;
		String pathToRead = "", resultsLabel = "", host = "";

		try
		{
			List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
					Files.newInputStream(Paths.get(resultsLabelsPathFileToRead), StandardOpenOption.READ), ",", false);

			for (List<String> resEntry : resLabels)
			{
				if (resEntry.size() == 3)
				{
					pathToRead = resEntry.get(2).trim();
					String splitted[] = pathToRead.split("/");
					resultsLabel = splitted[splitted.length - 1];

					host = getHostFromString(resEntry.get(1)).trim();

					WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + resultsLabel + "UserLabels.csv");

					System.out.println(
							"pathToRead= " + pathToRead + " \nresultsLabel:" + resultsLabel + "\nhost:" + host + "\n");
					int resSize = getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray,
							statFileNames, host, firstToMax);
					if (resSize < 0)
					{
						continue;
					}
				}
			}

		}
		catch (

		Exception e)
		{
			e.printStackTrace();
		}
		// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500_EDα0.5";
		// pathToRead =
		// "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/";
		// getResults2(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host, firstToMax);

	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runMar9FiveDaysResults()
	{

		String pathToWrite = "./dataWritten/Mar26ResultsDistribution/FiveDays/";// "./dataWritten/Mar9/FiveDays/";
		String resultsLabelsPathFile = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultMar26ToReadFormatted.csv";// ResultMar9ToReadFormatted.csv";
		int numOfDay = 5;
		int firstToMax = 3;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "", host = "";

		try
		{
			List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
					Files.newInputStream(Paths.get(resultsLabelsPathFile), StandardOpenOption.READ), ",", false);

			resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
			pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
			// resLabels.add(Arrays.asList(new String[] { "mortar", resultsLabel, pathToRead }));

			// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-600";
			// pathToRead =
			// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Feb2NCount_5Day_ThresholdNN600/";
			// resLabels.add(Arrays.asList(new String[] { "howitzer", resultsLabel, pathToRead }));

			for (List<String> resEntry : resLabels)
			{
				if (resEntry.size() > 1)
				{
					pathToRead = resEntry.get(2).trim();
					String splitted[] = pathToRead.split("/");
					resultsLabel = splitted[splitted.length - 1];
					host = getHostFromString(resEntry.get(1)).trim();

					if (resEntry.get(0).equals("100R"))
					{
						WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "100RUserLabels.csv");
					}
					if (resEntry.get(0).equals("916"))
					{
						WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "916UserLabels.csv");
					}

					System.out.println(
							"pathToRead= " + pathToRead + " \nresultsLabel:" + resultsLabel + "\nhost:" + host + "\n");
					int resSize = getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray,
							statFileNames, host, firstToMax);
					if (resSize < 0)
					{
						continue;
					}
				}
			}

		}
		catch (

		Exception e)
		{
			e.printStackTrace();
		}
		// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500_EDα0.5";
		// pathToRead =
		// "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/";
		// getResults2(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host, firstToMax);

	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb17FiveDaysResults()
	{

		String pathToWrite = "./dataWritten/Mar5/FiveDays/";
		String resultsLabelsPathFile = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultMar5ToReadFormatted2.csv";
		int numOfDay = 5;
		int firstToMax = 3;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "", host = "";

		try
		{
			List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
					Files.newInputStream(Paths.get(resultsLabelsPathFile), StandardOpenOption.READ), ",", false);

			resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
			pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
			// resLabels.add(Arrays.asList(new String[] { "mortar", resultsLabel, pathToRead }));

			// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-600";
			// pathToRead =
			// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Feb2NCount_5Day_ThresholdNN600/";
			// resLabels.add(Arrays.asList(new String[] { "howitzer", resultsLabel, pathToRead }));

			for (List<String> resEntry : resLabels)
			{
				if (resEntry.size() > 1)
				{
					pathToRead = resEntry.get(2).trim();
					resultsLabel = resEntry.get(1).trim();
					host = getHostFromString(resEntry.get(0)).trim();

					System.out.println(
							"pathToRead= " + pathToRead + " \nresultsLabel:" + resultsLabel + "\nhost:" + host + "\n");
					int resSize = getResultsForEachStatFile(pathToWrite, resultsLabel, pathToRead, muArray,
							statFileNames, host, firstToMax);
					if (resSize < 0)
					{
						continue;
					}
				}
			}

		}
		catch (

		Exception e)
		{
			e.printStackTrace();
		}
		// resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500_EDα0.5";
		// pathToRead =
		// "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/";
		// getResults2(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host, firstToMax);

	}

	/**
	 * Call getResult() for each stat file and accumulates results in a map
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileNames
	 * @param host
	 * @param firstToMax
	 * @return
	 * @throws Exception
	 */
	public static int getResultsForEachStatFile(String pathToWrite, String resultsLabel, String pathToRead,
			double[] muArray, String[] statFileNames, String host, int firstToMax) throws Exception
	{
		Map<Integer, Map<Integer, List<Double>>> res = null;

		for (String statFileName : statFileNames)
		{
			res = getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, host, firstToMax);
		}

		if (res == null)
		{
			return -1;
		}

		if (res.size() >= 100)
		{
			WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "GTE100UserLabels.csv");
		}
		if (res.size() == 916)
		{
			WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "E916UserLabels.csv");
		}

		if (res.size() < 916)
		{
			WToFile.appendLineToFileAbs(resultsLabel + "\n", pathToWrite + "LT916UserLabels.csv");
		}
		return res.size();
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String getHostFromString(String s)
	{
		if (s.trim().toLowerCase().contains("engine")) return Utils.engineHost;
		if (s.trim().toLowerCase().contains("howitzer")) return Utils.howitzerHost;
		if (s.trim().toLowerCase().contains("mortar")) return Utils.mortarHost;
		if (s.trim().toLowerCase().contains("claritytrec")) return Utils.clarityHost;
		if (s.trim().toLowerCase().contains("local")) return Utils.localHost;

		PopUps.printTracedErrorMsgWithExit("Host not found for String:" + s);
		return "unknownHost";

	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb2OneDayResults()
	{

		String pathToWrite = "./dataWritten/Feb2/OneDay/";
		int numOfDay = 1;
		int firstToMax = 3;
		int[] orders = { 5, 3, 4, 2, 1 };
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "";

		// prepare AKOM results
		for (int order : orders)
		{
			resultsLabel = "AKOM_916U_915N_" + numOfDay + "dayC_Order-" + order;
			pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb1_AKOM_"
					+ numOfDay + "DayFilter_Order" + order + "/";

			for (String statFileName : statFileNames)
			{
				getResult(pathToWrite, resultsLabel, pathToRead, new double[] { order - 1 }, statFileName,
						Utils.engineHost, firstToMax);
			}
		}
		resultsLabel = "Ncount_916U_915N_1dayC_ThreshNN-500";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_Ncount_AllCand1DayFilter_part1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.howitzerHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshPer-50";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec26_NCount_AllCand1DayFilter_percentile50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.mortarHost, firstToMax);
		}

	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb2FiveDaysResults()
	{

		String pathToWrite = "./dataWritten/Feb2/FiveDays/";
		int numOfDay = 5;
		int firstToMax = 3;
		int[] orders = { 5, 3, 4, 2, 1 };
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "";

		// prepare AKOM results
		for (int order : orders)
		{
			resultsLabel = "AKOM_916U_915N_" + numOfDay + "dayC_Order-" + order;
			pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb1_AKOM_"
					+ numOfDay + "DayFilter_Order" + order + "/";

			for (String statFileName : statFileNames)
			{
				getResult(pathToWrite, resultsLabel, pathToRead, new double[] { order - 1 }, statFileName,
						Utils.engineHost, firstToMax);
			}
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.mortarHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-50";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.engineHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-100";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold100/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.engineHost, firstToMax);
		}

	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb5FiveDaysResults()
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Feb5/FiveDays/";
		int numOfDay = 5;
		int[] orders = { 5, 3, 4, 2, 1 };
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "";

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-600";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Feb2NCount_5Day_ThresholdNN600/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.howitzerHost, firstToMax);
		}
	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb11FiveDaysResults()
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Feb11/FiveDays/";
		int numOfDay = 5;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "";

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500_EDα0.5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.engineHost, firstToMax);
		}
	}

	//

	/**
	 * @since Jan 31 2018
	 * @param args
	 */
	public static void runOneDay()
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Jan31/OneDay/";

		// String rootPath0 =
		// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";

		// "AllPerDirectTopKAgreements_0.csv", "AllPerDirectTopKAgreementsL1_0.csv"

		double muArray[] = Constant.matchingUnitAsPastCount;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };

		// // user index (row index), mu, list of vals
		// LinkedHashMap<Integer, LinkedHashMap<Integer, List<List<Double>>>> userIndexMUKeysValuesMap = new
		// LinkedHashMap<>();

		String pathToRead = "", resultsLabel = "";

		resultsLabel = "AKOM_916U_915N_1dayC_Order-1";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, Utils.howitzerHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-1_run2";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, Utils.engineHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-3";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order3/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 2 }, statFileName, Utils.engineHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 4 }, statFileName, Utils.engineHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-5_run2";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 4 }, statFileName, Utils.howitzerHost,
					firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshNN-500";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_Ncount_AllCand1DayFilter_part1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.howitzerHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshPer-50";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec26_NCount_AllCand1DayFilter_percentile50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.mortarHost, firstToMax);
		}

	}

	/**
	 * @since Jan 31 2018
	 * @param args
	 */
	public static void runFiveDays()
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Jan31/FiveDays/";

		// String rootPath0 =
		// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";

		// "AllPerDirectTopKAgreements_0.csv", "AllPerDirectTopKAgreementsL1_0.csv"

		double muArray[] = Constant.matchingUnitAsPastCount;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };

		// // user index (row index), mu, list of vals
		// LinkedHashMap<Integer, LinkedHashMap<Integer, List<List<Double>>>> userIndexMUKeysValuesMap = new
		// LinkedHashMap<>();

		String pathToRead = "", resultsLabel = "";

		resultsLabel = "AKOM_916U_915N_5dayC_Order-1";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter5Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, Utils.howitzerHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_5dayC_Order-1_run2";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_5DayFilter_Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, Utils.engineHost,
					firstToMax);
		}

		////////////////////////
		resultsLabel = "AKOM_916U_915N_5dayC_Order-3";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_5DayFilter_Order3/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 2 }, statFileName, Utils.engineHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_5dayC_Order-5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_5DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 4 }, statFileName, Utils.engineHost,
					firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.mortarHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-50";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.engineHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-100";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold100/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.engineHost, firstToMax);
		}

	}

	/**
	 * used before 31 Jan: old results
	 * 
	 * @param args
	 */
	public static void mainBefore31Jan2018(String[] args)
	{
		int firstToMax = 3;
		String pathToWrite = "./dataWritten/Jan26/";

		// String rootPath0 =
		// "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";

		// "AllPerDirectTopKAgreements_0.csv", "AllPerDirectTopKAgreementsL1_0.csv"

		double muArray[] = Constant.matchingUnitAsPastCount;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };

		// // user index (row index), mu, list of vals
		// LinkedHashMap<Integer, LinkedHashMap<Integer, List<List<Double>>>> userIndexMUKeysValuesMap = new
		// LinkedHashMap<>();

		String pathToRead = "", resultsLabel = "";

		resultsLabel = "AKOM_916U_915N_1dayC_Order-1";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec11AKOMDayFilter1Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, Utils.howitzerHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-3";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_AKOM_1DayFilter_Order3/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, Utils.engineHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_AKOM_1DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, Utils.engineHost,
					firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshNN-500";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_Ncount_AllCand1DayFilter_part1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, Utils.howitzerHost, firstToMax);
		}

	}

	/**
	 * 
	 * @param pathToWrite
	 * @param resultsLabel
	 * @param pathToRead
	 * @param muArray
	 * @param statFileName
	 * @param host
	 * @param firstToMax
	 * @return
	 */
	private static Map<Integer, Map<Integer, List<Double>>> getResult(String pathToWrite, String resultsLabel,
			String pathToRead, double[] muArray, String statFileName, String host, int firstToMax)
	{
		String passwd = Utils.getPassWordForHost(host);
		String user = Utils.getUserForHost(host);

		// MU , <list for each user, <list of first1,2,3 for that user and mu>>
		Map<Integer, List<List<Double>>> muKeyAllValsMap = new LinkedHashMap<>();

		// Convert to user wise result
		// userIndex , <MU, <list of first1,2,3 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = null;

		String userSetLabel = "";
		try
		{// if PureAKOM, then mu is order of AKOM -1
			if (pathToRead.contains("PureAKOMOrder"))
			{
				String[] splitted = pathToRead.split("Order");
				String[] splitted2 = splitted[1].split("/");
				double muForOrder = Double.valueOf(splitted2[0]);
				muArray = new double[] { muForOrder - 1 };
			}
			if (pathToRead.contains("Set"))
			{
				int i = pathToRead.indexOf("Set");
				userSetLabel = pathToRead.substring(i, i + 4);
			}

			for (double muD : muArray)
			{
				int mu = (int) muD;
				List<List<Double>> readResFromFile = null;

				String statFileForThisMU = pathToRead + statFileName + mu + ".csv";

				if (host.contains("local"))
				{// each row corresponds to a user
					readResFromFile = ReadingFromFile.nColumnReaderDouble(statFileForThisMU, ",", false);
				}
				else
				{
					Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port,
							statFileForThisMU, user, passwd);
					readResFromFile = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",", false);
				}
				// System.out.println("mu= " + mu + " res=" + res);
				muKeyAllValsMap.put(mu, readResFromFile);
				// inputAndSession.getSecond().disconnect();
			}

			// Convert to user wise result
			// userIndex , <MU, <list of first1,2,3 for that user and mu>>
			userMUKeyVals = transformToUserWise(muArray, muKeyAllValsMap);

			writeUserMuKeyVals(userMUKeyVals, pathToWrite + resultsLabel + "_" + statFileName + "userMUKeyVals.csv",
					userSetLabel);
			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Pair<Integer, List<Double>>> firstsForMinMUWithMaxFxForEachUser = getValsForMinMUHavingMaxF3(
					firstToMax, userMUKeyVals);
			StringBuilder sb = new StringBuilder("userIndex,minMuWithMaxFirst3,First1,First2,First3\n");
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsForMinMUWithMaxFxForEachUser.entrySet())
			{
				sb.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
						+ r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
						+ r.getValue().getSecond().get(2) + "\n");
			}
			WToFile.writeToNewFile(sb.toString(),
					pathToWrite + resultsLabel + "_" + statFileName + "MinMUWithMaxFirst" + firstToMax + ".csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////////////////////////////
			// userIndex, min mu with highest first 3, first3 for this mu and user
			Map<Integer, Triple<Integer, List<Double>, Integer>> firstsForMinMUWithMaxFx0AwareForEachUser = getValsForMinMUHavingMaxF3ZeroAware(
					new int[] { 3, 2, 1 }, userMUKeyVals);
			StringBuilder sb2 = new StringBuilder("userIndex,minMuWithMaxFirst3,First1,First2,First3,ChosenFirst\n");
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Triple<Integer, List<Double>, Integer>> r : firstsForMinMUWithMaxFx0AwareForEachUser
					.entrySet())
			{
				sb2.append(userSetLabel + r.getKey() + "," + r.getValue().getFirst() + ","
						+ r.getValue().getSecond().get(0) + "," + r.getValue().getSecond().get(1) + ","
						+ r.getValue().getSecond().get(2) + "," + r.getValue().getThird() + "\n");
			}
			WToFile.writeToNewFile(sb2.toString(),
					pathToWrite + resultsLabel + "_" + statFileName + "MinMUWithMaxFirst0Aware.csv");
			/////////////////////////////////////////////////////////////////////////////////////////////////

			WToFile.writeToNewFile(host + ":" + pathToRead,
					pathToWrite + "ReadMe/" + resultsLabel + "_" + statFileName + "ReadMe.txt");
		}
		catch (NullPointerException e)
		{
			WToFile.appendLineToFileAbs(PopUps.getCurrentStackTracedWarningMsg("\n\nException in getResult()\n"),
					pathToWrite + "ExceptionsEncountered.csv");
			return null;
		}
		return (userMUKeyVals);
	}

	/**
	 * Write rows of for all MUs "UserIndex,MU,First1,First2,First3"
	 * 
	 * @param userMUKeyVals
	 * @param fileNameToWrite
	 * @param userSetLabel
	 */
	private static void writeUserMuKeyVals(Map<Integer, Map<Integer, List<Double>>> userMUKeyVals,
			String fileNameToWrite, String userSetLabel)
	{
		StringBuilder sb = new StringBuilder("UserIndex,MU,First1,First2,First3\n");
		for (Entry<Integer, Map<Integer, List<Double>>> e : userMUKeyVals.entrySet())
		{
			Integer userIndex = e.getKey();
			for (Entry<Integer, List<Double>> e2 : e.getValue().entrySet())
			{
				sb.append(userSetLabel + userIndex + "," + e2.getKey() + ","
						+ e2.getValue().stream().map(i -> i.toString()).collect(Collectors.joining(",")) + "\n");
			}
		}
		WToFile.writeToNewFile(sb.toString(), fileNameToWrite);
	}

	/**
	 * 
	 * @param firstToMax
	 *            this will determine which MU is chose, min mu with max firstToMax (say First3) is chosen
	 * @param userMUKeyVals
	 *            { userIndex , {MU, {list of first1,2,3 for that user and mu}}}
	 * @return { userIndex,{ min mu with highest first 3, first3 for this mu and user}}
	 */
	private static Map<Integer, Pair<Integer, List<Double>>> getValsForMinMUHavingMaxF3(int firstToMax,
			Map<Integer, Map<Integer, List<Double>>> userMUKeyVals)
	{
		// userIndex, min mu with highest first 3, first3 for this mu and user
		Map<Integer, Pair<Integer, List<Double>>> firstsWithHighF3ResultForEachUserOverMUs = new LinkedHashMap<>();

		for (Entry<Integer, Map<Integer, List<Double>>> userVals : userMUKeyVals.entrySet())
		{
			int userIndex = userVals.getKey();
			// MU, list of first 1,2,3 for that mu for current user
			Map<Integer, List<Double>> valsForAllMUs = userVals.getValue();

			// find max first 3 over all MUs
			double maxFirst3OverAllMUS = valsForAllMUs.entrySet().stream()
					.mapToDouble(e -> e.getValue().get(firstToMax - 1)).max().getAsDouble();

			// find MUs having Max first 3 found just above.
			Set<Integer> musHavingTheMax = valsForAllMUs.entrySet().stream()
					.filter(e -> e.getValue().get(firstToMax - 1).equals(maxFirst3OverAllMUS)).map(e -> e.getKey())
					.collect(Collectors.toSet());

			// find min MU having max first 3
			int minMUHavingMaxFirst3ForThisUser = musHavingTheMax.stream().mapToInt(e -> e).min().getAsInt();

			firstsWithHighF3ResultForEachUserOverMUs.put(userIndex, new Pair<Integer, List<Double>>(
					minMUHavingMaxFirst3ForThisUser, valsForAllMUs.get(minMUHavingMaxFirst3ForThisUser)));

		}
		return firstsWithHighF3ResultForEachUserOverMUs;
	}

	/**
	 * instead of choosing minMUHavingMaxFirst3, if maxfirst3 is 0 then choose the min mu having max first 2 and if that
	 * is 0 then choose min mu having max first 1
	 * 
	 * @param firstsToMaxInOrder
	 *            this will determine which MU is chose, min mu with max firstToMax (say First3) is chosen, {a,b,c} If
	 *            max First_a is 0 then choose max First_b, if that is 0 then choose max First_c.
	 * @param userMUKeyVals
	 *            { userIndex , {MU, {list of first1,2,3 for that user and mu}}}
	 * @return { userIndex,{ min mu with highest first 3, corresponding first1 2 & 3 vals for this mu &
	 *         user,chosenFirstForThisUser}}
	 */
	private static Map<Integer, Triple<Integer, List<Double>, Integer>> getValsForMinMUHavingMaxF3ZeroAware(
			int[] firstsToMaxInOrder, Map<Integer, Map<Integer, List<Double>>> userMUKeyVals)
	{
		// {userIndex,Triple {
		// minMU with highest chosen first (either 3 2 or 1),
		// corresponding first1 2 & 3 vals for this mu & user,
		// chosenFirstForThisUser
		// }
		Map<Integer, Triple<Integer, List<Double>, Integer>> firstsForHighChosenFirstResultForEachUserOverMUs = new LinkedHashMap<>();

		for (Entry<Integer, Map<Integer, List<Double>>> userVals : userMUKeyVals.entrySet())
		{
			int userIndex = userVals.getKey();
			// MU, list of first 1,2,3 for that mu for current user
			Map<Integer, List<Double>> valsForAllMUs = userVals.getValue();

			int chosenFirstToMax = -99999;// whether first 3, 2, or 1
			Double maxOfChosenFirstOverAllMUS = Double.valueOf(-99999);

			for (int firstToMax : firstsToMaxInOrder)
			{
				// find max first 3 over all MUs
				maxOfChosenFirstOverAllMUS = valsForAllMUs.entrySet().stream()
						.mapToDouble(e -> e.getValue().get(firstToMax - 1)).max().getAsDouble();

				// if (maxOfChosenFirstOverAllMUS.compareTo(Double.valueOf(0)) > 0)
				if (Constant.equalsForFloat(maxOfChosenFirstOverAllMUS, Double.valueOf(0)))
				{
					continue;
				}
				else
				{
					chosenFirstToMax = firstToMax;
					break;
				}
			}

			// if all First3, 2, 1 are all zeros for all MU, then set chosenFirstToMax = first preference (say 3)
			if (chosenFirstToMax < -1)
			{
				chosenFirstToMax = firstsToMaxInOrder[0];
				// maxOfChosenFirstOverAllMUS = 0;
				System.out.println("Warning: all First3, 2, 1 are all zeros for all MU, chosenFirstToMax="
						+ chosenFirstToMax + " maxOfChosenFirstOverAllMUS=" + maxOfChosenFirstOverAllMUS);
			}

			// find MUs having Max first 3 found just above.
			Set<Integer> musHavingTheMax = new LinkedHashSet<>();
			// valsForAllMUs.entrySet().stream()
			// .filter(e -> e.getValue().get(chosenFirstToMax - 1).equals(maxOfChosenFirstOverAllMUS))
			// .map(e -> e.getKey()).collect(Collectors.toSet());

			for (Entry<Integer, List<Double>> muEntry : valsForAllMUs.entrySet())
			{
				Double valueForChosenFirstForThisMU = muEntry.getValue().get(chosenFirstToMax - 1);
				// (valueForChosenFirstForThisMU - maxOfChosenFirstOverAllMUS) < Constant.epsilonForFloatZero)
				if (Constant.equalsForFloat(valueForChosenFirstForThisMU, maxOfChosenFirstOverAllMUS))
				{
					musHavingTheMax.add(muEntry.getKey());
				}
			}

			// find min MU having max first 3
			int minMUHavingMaxChosenFirstForThisUser = musHavingTheMax.stream().mapToInt(e -> e).min().getAsInt();

			firstsForHighChosenFirstResultForEachUserOverMUs.put(userIndex,
					new Triple<Integer, List<Double>, Integer>(minMUHavingMaxChosenFirstForThisUser,
							valsForAllMUs.get(minMUHavingMaxChosenFirstForThisUser), chosenFirstToMax));
			// the corresponding first1, first2 and first3 for minMuHavingMaxFirstChosen
		}
		return firstsForHighChosenFirstResultForEachUserOverMUs;
	}

	/**
	 * convert ( MU , (list for each user, (list of first1,2,3 for that user and mu))) to (userIndex , (MU, (list of
	 * first1,2,3 for that user and mu))
	 * 
	 * @param muArray
	 * @param muKeyAllValsMap
	 * @return {userIndex ,{MU, {list of first1,2,3 for that user and mu}}}
	 */
	public static Map<Integer, Map<Integer, List<Double>>> transformToUserWise(double[] muArray,
			Map<Integer, List<List<Double>>> muKeyAllValsMap)
	{
		System.out.println("Inside transformToUserWise: muArray.length= " + muArray.length + " muKeyAllValsMap.size()="
				+ muKeyAllValsMap.size());

		// userIndex , <MU, <list of first1,2,3 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = new LinkedHashMap<>();

		// assuming all MU results have same number of users which should be true;
		int numOfUsers = muKeyAllValsMap.get((int) muArray[0]).size();
		System.out.println("Num of users = " + numOfUsers);

		for (int u = 0; u < numOfUsers; u++)// for each user
		{
			Map<Integer, List<Double>> valsForThisUserAllMUs = new LinkedHashMap<>();
			for (double muD : muArray)
			{
				List<List<Double>> muResForAllUsersThisMU = muKeyAllValsMap.get((int) muD);
				System.out.println("Num of users for muD=" + muD + " =" + muResForAllUsersThisMU.size());

				valsForThisUserAllMUs.put((int) muD, muResForAllUsersThisMU.get(u));
			}
			userMUKeyVals.put(u, valsForThisUserAllMUs);
		}
		return userMUKeyVals;
	}

}
