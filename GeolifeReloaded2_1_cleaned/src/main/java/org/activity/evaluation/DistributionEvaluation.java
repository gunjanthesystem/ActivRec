package org.activity.evaluation;

import java.io.InputStream;
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

import com.jcraft.jsch.Session;

/**
 * To do signficance tests on results over users
 * 
 * @since 25 Jan 2018
 * @author gunjan
 *
 */
public class DistributionEvaluation
{
	static final String howitzerUsr = "gunjan";
	static final String howitzerHost = "howitzer.ucd.ie";
	static final String engineHost = "theengine2.ucd.ie";
	static final String mortarHost = "mortar.ucd.ie";
	static final int port = 22;
	static final int firstToMax = 3;

	public DistributionEvaluation()
	{
		// org.apache.commons.lang3.tuple.Triple;
	}

	public static void main(String args[])
	{
		runOneDay();
		runFiveDays();
	}

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
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, howitzerHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-1_run2";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, engineHost, firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-3";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order3/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 2 }, statFileName, engineHost, firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 4 }, statFileName, engineHost, firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-5_run2";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_1DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 4 }, statFileName, howitzerHost,
					firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshNN-500";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_Ncount_AllCand1DayFilter_part1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, howitzerHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshPer-50";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec26_NCount_AllCand1DayFilter_percentile50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, mortarHost, firstToMax);
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
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, howitzerHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_5dayC_Order-1_run2";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_5DayFilter_Order1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, engineHost, firstToMax);
		}

		////////////////////////
		resultsLabel = "AKOM_916U_915N_5dayC_Order-3";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_5DayFilter_Order3/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 2 }, statFileName, engineHost, firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_5dayC_Order-5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan31_AKOM_5DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 4 }, statFileName, engineHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-500";
		pathToRead = "/Users/gunjankumar/SyncedWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_NCount_AllCand5DayFilter/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, mortarHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-50";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold50/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, engineHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_5dayC_ThreshNN-100";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Jan20Ncount5DayThreshold100/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, engineHost, firstToMax);
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
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, howitzerHost,
					firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-3";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_AKOM_1DayFilter_Order3/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, engineHost, firstToMax);
		}

		resultsLabel = "AKOM_916U_915N_1dayC_Order-5";
		pathToRead = "/home/gunjan/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_AKOM_1DayFilter_Order5/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, new double[] { 0 }, statFileName, engineHost, firstToMax);
		}

		resultsLabel = "Ncount_916U_915N_1dayC_ThreshNN-500";
		pathToRead = "/Users/admin/SyncedWorkspace/JavaWorkspace/Mar2Merged/GeolifeReloaded2_1_cleaned/dataWritten/Dec20_Ncount_AllCand1DayFilter_part1/";
		for (String statFileName : statFileNames)
		{
			getResult(pathToWrite, resultsLabel, pathToRead, muArray, statFileName, howitzerHost, firstToMax);
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
	 */
	private static void getResult(String pathToWrite, String resultsLabel, String pathToRead, double[] muArray,
			String statFileName, String host, int firstToMax)
	{
		String passwd = null;
		switch (host)
		{
			case howitzerHost:
				passwd = ReadingFromFile.oneColumnReaderString("./dataToRead/Jan26/serverInfo.kry", ",", 0, false)
						.get(0);
				break;
			case mortarHost:
				passwd = ReadingFromFile.oneColumnReaderString("./dataToRead/Jan26/serverInfo.kry", ",", 1, false)
						.get(0);
				break;
			case engineHost:
				passwd = ReadingFromFile.oneColumnReaderString("./dataToRead/Jan26/serverInfo.kry", ",", 2, false)
						.get(0);
				break;

		}

		// MU , <list for each user, <list of first1,2,3 for that user and mu>>
		Map<Integer, List<List<Double>>> muKeyAllValsMap = new LinkedHashMap<>();

		for (double muD : muArray)
		{
			int mu = (int) muD;

			String fileToRead = pathToRead + statFileName + mu + ".csv";

			Pair<InputStream, Session> inputAndSession = SFTPFile.getInputStreamForSFTPFile(host, port, fileToRead,
					howitzerUsr, passwd);

			List<List<Double>> res = ReadingFromFile.nColumnReaderDouble(inputAndSession.getFirst(), ",", false);

			// System.out.println("mu= " + mu + " res=" + res);
			muKeyAllValsMap.put(mu, res);
			inputAndSession.getSecond().disconnect();
		}

		// Convert to user wise result
		// userIndex , <MU, <list of first1,2,3 for that user and mu>>
		Map<Integer, Map<Integer, List<Double>>> userMUKeyVals = transformToUserWise(muArray, muKeyAllValsMap);

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
		WritingToFile.writeToNewFile(pathToRead,
				pathToWrite + "ReadMe/" + resultsLabel + "_" + statFileName + "ReadMe.txt");
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
