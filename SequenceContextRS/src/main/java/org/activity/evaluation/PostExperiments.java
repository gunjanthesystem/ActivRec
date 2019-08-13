package org.activity.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;

/**
 * To do some processing on results, evaluation, etc post experiments.
 * 
 * @author gunjan
 * @since 14 Jan
 */
public class PostExperiments
{
	public static void main(String args[])
	{
		// main14Jan2019();//disabled on Feb 6 2019
		mainFeb6_2019();
	}

	/////////////////////// START OF FEB 6
	public static void mainFeb6_2019()
	{
		addMoreFieldsToLogsFromRaw(
				"/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/dcu_data_2_FEB6H22M52ED0.5STimeDurAllActsFDStFilter0hrsFEDPerFS_20F_RTVPNN500NoTTFilterNCMyLevenshtein/All/MatchingUnit3.0/");
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/dcu_data_2_FEB6H16M6ED0.5STimeDurAllActsFDStFilter0hrsFEDPerFS_20F_RTVPNN100NoTTFilterNCMyLevenshtein/All/MatchingUnit3.0/");
	}

	public static void addMoreFieldsToLogsFromRaw(String commonPath)
	{
		String pathToRawFile = commonPath + "Raw0.csv";
		String pathToLogFileToAddFieldsTo = commonPath + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachCand.csv";
		int rawIndexForUser = 0;
		int rawIndexForDate = 1;
		int rawIndexForTime = 2;
		int rawIndexForNumOfCands = 8;
		int rawIndexForTargetAct = 10;
		int rawIndexForRecommList = 11;

		List<List<String>> res = ReadingFromFile.nColumnReaderStringLargeFileSelectedColumns(pathToRawFile, ",", true,
				false, new int[] { rawIndexForNumOfCands, rawIndexForTargetAct, rawIndexForRecommList, rawIndexForUser,
						rawIndexForDate, rawIndexForTime });
		res.remove(0);
		StringBuilder sb = new StringBuilder("TargetAct,RecommList,User,Date,Time\n");
		for (List<String> line : res)
		{
			System.out.println("line = " + line);
			int numOfCandsForThisRT = Integer.valueOf(line.get(0));
			String targetActForThisRT = line.get(1);
			String recommListForThisRT = line.get(2);

			for (int i = 0; i < numOfCandsForThisRT; i++)
			{
				sb.append(targetActForThisRT + "," + recommListForThisRT + "," + line.get(3) + "," + line.get(4) + ","
						+ line.get(5) + "\n");
			}
		}
		WToFile.writeToNewFile(sb.toString(), commonPath + "TargetRecommTemp.csv");

		ArrayList<String> filesToConcatenate = new ArrayList<>();
		filesToConcatenate.add(pathToLogFileToAddFieldsTo);
		filesToConcatenate.add(commonPath + "TargetRecommTemp.csv");
		CSVUtils.concatenateCSVFilesSideways(filesToConcatenate, false,
				commonPath + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachCandWithExtras.csv", "", true);
	}

	//////////////////////// END OF FEB 6

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * looking into distribution of target activity per RT
	 */
	public static void main14Jan2019()
	{
		String commonPath = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_JAN14H2M38ED1.0AllActsFDStFilter0hrsRTVPNN500NoTTFilterNC/";
		String pathToWrite = commonPath;
		LinkedHashMap<String, String> catIdNameDict = getActIDNameDictionary(
				commonPath + "All/CatIDNameDictionary.csv");
		countTargetActivity(
				"/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_JAN14H2M38ED1.0AllActsFDStFilter0hrsRTVPNN500NoTTFilterNC/All/MatchingUnit3.0/dataActual0.csv",
				catIdNameDict, pathToWrite);

		compareDistributionTrainTestTarget(commonPath, catIdNameDict, pathToWrite);
	}

	public static void compareDistributionTrainTestTarget(String commonPath,
			LinkedHashMap<String, String> catIdNameDict, String pathToWrite)
	{
		String trainFile = commonPath + "All/MatchingUnit3.0/FeatsOfTrainingTimelines.csv";
		String testFile = commonPath + "All/MatchingUnit3.0/FeatsOfTestTimelines.csv";
		String targetActFiles = commonPath + "targetActIDsUnrolled.csv";

		LinkedHashMap<String, Integer> trainActCount = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> testActCount = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> targetActCount = new LinkedHashMap<>();

		List<String> trainActs = ReadingFromFile.oneColumnReaderString(trainFile, ",", 3, true);
		Map<String, Long> trainActsCounts = trainActs.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		System.out.println("trainActsCounts = " + trainActsCounts);

		List<String> testActs = ReadingFromFile.oneColumnReaderString(testFile, ",", 3, true);
		Map<String, Long> testActsCounts = testActs.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		List<String> targetActs = ReadingFromFile.oneColumnReaderString(targetActFiles, ",", 0, false);
		Map<String, Long> targetActsCounts = targetActs.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		StringBuilder sb = new StringBuilder("ActName,ActID,trainCount,testCount,targetRTCount\n");
		for (Entry<String, String> e : catIdNameDict.entrySet())
		{
			String actID = e.getKey();
			sb.append(actID + "," + e.getValue() + "," + trainActsCounts.getOrDefault(actID, 0l) + ","
					+ testActsCounts.getOrDefault(actID, 0l) + "," + targetActsCounts.getOrDefault(actID, 0l) + "\n");
		}
		WToFile.writeToNewFile(sb.toString(), pathToWrite + "ActCountsTrainTestTarget.csv");

	}

	public static void countTargetActivity(String fileToRead, LinkedHashMap<String, String> catIdNameDict,
			String pathToWrite)
	{
		List<List<String>> allReadLines = ReadingFromFile.readLinesIntoListOfLists(fileToRead, ",");
		List<String> targetActIDs = allReadLines.stream().flatMap(e -> e.stream()).collect(Collectors.toList());
		List<String> targetNames = targetActIDs.stream().map(v -> catIdNameDict.get(v)).collect(Collectors.toList());
		WToFile.writeToNewFile(targetActIDs.stream().collect(Collectors.joining("\n")),
				pathToWrite + "targetActIDsUnrolled.csv");
		WToFile.writeToNewFile(targetNames.stream().collect(Collectors.joining("\n")),
				pathToWrite + "targetActNamesUnrolled.csv");
	}

	public static LinkedHashMap<String, String> getActIDNameDictionary(String fileToRead)
	{
		List<List<String>> allReadLines = ReadingFromFile.readLinesIntoListOfLists(fileToRead, ",");
		allReadLines.remove(0);
		LinkedHashMap<String, String> actIDNameDict = new LinkedHashMap<>();
		for (List<String> line : allReadLines)
		{
			actIDNameDict.put(line.get(0), line.get(1));
		}
		return actIDNameDict;
	}
}
