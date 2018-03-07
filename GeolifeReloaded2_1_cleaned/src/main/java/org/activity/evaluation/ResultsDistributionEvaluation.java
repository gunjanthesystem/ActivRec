package org.activity.evaluation;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.io.ReadingFromFile;
import org.activity.io.SFTPFile;
import org.activity.io.WritingToFile;
import org.activity.objects.Pair;
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
	static final int firstToMax = 3;

	public ResultsDistributionEvaluation()
	{
		// org.apache.commons.lang3.tuple.Triple;
	}

	public static void main(String args[])
	{
		// runOneDay();
		// runFiveDays();
		// $runFeb2OneDayResults();//disabled on Feb 12 2018
		// $runFeb2FiveDaysResults(); //disabled on Feb 12 2018
		// $runFeb5FiveDaysResults();//disabled on Feb 12 2018
		// runFeb11FiveDaysResults();//disabled on feb 18 2018
		runFeb17FiveDaysResults();
		SFTPFile.closeAllChannels();
	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb17FiveDaysResults()
	{

		String pathToWrite = "./dataWritten/Mar5/FiveDays/";
		String resultsLabelsPathFile = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultMar5ToReadFormatted.csv";
		int numOfDay = 5;
		String statFileNames[] = { "AllPerDirectTopKAgreements_", "AllPerDirectTopKAgreementsL1_" };
		double muArray[] = Constant.matchingUnitAsPastCount;
		String pathToRead = "", resultsLabel = "", host = "";

		try
		{
			List<List<String>> resLabels = ReadingFromFile.nColumnReaderString(
					Files.newInputStream(Paths.get(resultsLabelsPathFile), StandardOpenOption.READ), ",", false);

			resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
			pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
			resLabels.add(Arrays.asList(new String[] { "mortar", resultsLabel, pathToRead }));

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
					int resSize = getResults2(pathToWrite, resultsLabel, pathToRead, muArray, statFileNames, host,
							firstToMax);
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
	public static int getResults2(String pathToWrite, String resultsLabel, String pathToRead, double[] muArray,
			String[] statFileNames, String host, int firstToMax) throws Exception
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
			WritingToFile.appendLineToFileAbsolute(resultsLabel + "\n", pathToWrite + "GTE100UserLabels.csv");
		}
		if (res.size() == 916)
		{
			WritingToFile.appendLineToFileAbsolute(resultsLabel + "\n", pathToWrite + "E916UserLabels.csv");
		}

		if (res.size() < 916)
		{
			WritingToFile.appendLineToFileAbsolute(resultsLabel + "\n", pathToWrite + "LT916UserLabels.csv");
		}
		return res.size();
	}

	public static String getHostFromString(String s)
	{
		if (s.trim().toLowerCase().contains("engine")) return Utils.engineHost;
		if (s.trim().toLowerCase().contains("howitzer")) return Utils.howitzerHost;
		if (s.trim().toLowerCase().contains("mortar")) return Utils.mortarHost;
		if (s.trim().toLowerCase().contains("claritytrec"))
			return Utils.clarityHost;
		else
		{
			PopUps.printTracedErrorMsgWithExit("Host not found for String:" + s);
			return "unknownHost";
		}
	}

	/**
	 * @since Feb 2 2018
	 * @param args
	 */
	public static void runFeb2OneDayResults()
	{

		String pathToWrite = "./dataWritten/Feb2/OneDay/";
		int numOfDay = 1;
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
	public static void main1(String[] args)
	{

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

		try
		{
			for (double muD : muArray)
			{
				int mu = (int) muD;

				String fileToRead = pathToRead + statFileName + mu + ".csv";

				Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port, fileToRead,
						user, passwd);

				List<List<Double>> res = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",", false);

				// System.out.println("mu= " + mu + " res=" + res);
				muKeyAllValsMap.put(mu, res);
				// inputAndSession.getSecond().disconnect();
			}

			// Convert to user wise result
			// userIndex , <MU, <list of first1,2,3 for that user and mu>>
			userMUKeyVals = transformToUserWise(muArray, muKeyAllValsMap);

			// user, min mu with highest first 3, first3 for this mu and user
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

			StringBuilder sb = new StringBuilder("userIndex,minMuWithMaxFirst3,First1,First2,First3\n");
			//// user, min mu with highest first 3, first3 for this mu and user
			for (Entry<Integer, Pair<Integer, List<Double>>> r : firstsWithHighF3ResultForEachUserOverMUs.entrySet())
			{
				sb.append(r.getKey() + "," + r.getValue().getFirst() + "," + r.getValue().getSecond().get(0) + ","
						+ r.getValue().getSecond().get(1) + "," + r.getValue().getSecond().get(2) + "\n");
			}
			WritingToFile.writeToNewFile(sb.toString(), pathToWrite + resultsLabel + "_" + statFileName + ".csv");

			WritingToFile.writeToNewFile(host + ":" + pathToRead,
					pathToWrite + "ReadMe/" + resultsLabel + "_" + statFileName + "ReadMe.txt");
		}
		catch (NullPointerException e)
		{
			WritingToFile.appendLineToFileAbsolute(
					PopUps.getCurrentStackTracedWarningMsg("\n\nException in getResult()\n"),
					pathToWrite + "ExceptionsEncountered.csv");
			return null;
		}
		return (userMUKeyVals);
	}

	/**
	 * convert ( MU , (list for each user, (list of first1,2,3 for that user and mu))) to (userIndex , (MU, (list of
	 * first1,2,3 for that user and mu))
	 * 
	 * @param muArray
	 * @param muKeyAllValsMap
	 * @return
	 */
	public static Map<Integer, Map<Integer, List<Double>>> transformToUserWise(double[] muArray,
			Map<Integer, List<List<Double>>> muKeyAllValsMap)
	{
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
				valsForThisUserAllMUs.put((int) muD, muResForAllUsersThisMU.get(u));
			}
			userMUKeyVals.put(u, valsForThisUserAllMUs);
		}
		return userMUKeyVals;
	}

}
