package org.activity.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.evaluation.RecommendationTestsMar2017Gen;
import org.activity.io.SerializableJSONArray;
import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.CheckinEntry;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.UserGowalla;
import org.activity.ui.PopUps;
import org.activity.util.ConnectDatabase;
import org.activity.util.PerformanceAnalytics;
import org.activity.util.RegexUtils;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

/**
 * This class is used for recommending without the web interface (no web server) Note: to run these experiments proper
 * parameters must be set in class org.activity.util.Constant and this class
 * 
 * @author gunjan
 */
public class ControllerWithoutServer
{
	String pathToLatestSerialisedJSONArray = "", pathForLatestSerialisedJSONArray = "",
			pathToLatestSerialisedTimelines = "", pathForLatestSerialisedTimelines = "", commonPath = "";

	public ControllerWithoutServer()
	{
		try
		{
			System.out.println("Starting ControllerWithoutServer>>>>");
			LocalDateTime currentDateTime = LocalDateTime.now();
			TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 21, 2016
			Constant.setDefaultTimeZone("UTC");
			// String pathToLatestSerialisedJSONArray = "", pathForLatestSerialisedJSONArray = "",
			// pathToLatestSerialisedTimelines = "", pathForLatestSerialisedTimelines = "", commonPath = "";
			System.out.println("Running experiments for database: " + Constant.getDatabaseName());// .DATABASE_NAME);

			setPaths(Constant.getDatabaseName());
			// new ConnectDatabase(Constant.getDatabaseName()); // all method and variable in this class are static
			// new Constant(commonPath, Constant.getDatabaseName());

			System.out.println("Just before Constant.initialise:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
					+ PerformanceAnalytics.getHeapPercentageFree());
			/*
			 * $ Disabled for Gowalla dataset for now// ConnectDatabase.initialise(Constant.getDatabaseName()); // all
			 * method and variable in this class are static
			 */
			Constant.initialise(commonPath, Constant.getDatabaseName(), "./dataToRead/April7/mapCatIDsHierDist.kryo");
			// ,// 550);
			System.out.println("Just after Constant.initialise:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
					+ PerformanceAnalytics.getHeapPercentageFree());

			long bt = System.currentTimeMillis();

			if (Constant.toSerializeJSONArray)
			{
				fetchAndSerializeJSONArray(Constant.getDatabaseName());
			}
			// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			SerializableJSONArray jsonArrayD = null;
			long dt = System.currentTimeMillis();
			if (Constant.toDeSerializeJSONArray)
			{
				jsonArrayD = (SerializableJSONArray) Serializer.deSerializeThis(pathToLatestSerialisedJSONArray);// GeolifeJSONArrayDec24_2.obj");
				System.out.println("Deserialized JSONArray");
			}
			// // System.out.println(jsonArray.getJSONArray().toString() == jsonArrayD.getJSONArray().toString());
			// // System.out.println(jsonArray.getJSONArray().toString().length());
			// // System.out.println("---------");
			// // System.out.println(jsonArray.getJSONArray().toString().length());
			//
			// // System.out.println(StringUtils.difference(jsonArray.getJSONArray().toString(),
			// jsonArrayD.getJSONArray().toString()));
			// System.out.println("ajooba");1
			// //System.out.println(jsonArray.toString());
			//
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal = null;
			// new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
			if (Constant.toCreateTimelines)
			{
				String gowallaDataFolder = "./dataToRead/Mar30/DatabaseCreatedMerged/";// Feb23
				usersDayTimelinesOriginal = createTimelines(Constant.getDatabaseName(), jsonArrayD, gowallaDataFolder);
			}
			// //to improve repeat execution performance...serialising
			if (Constant.toSerializeTimelines)
			{
				Serializer.serializeThis(usersDayTimelinesOriginal, pathForLatestSerialisedTimelines);
				pathToLatestSerialisedTimelines = pathForLatestSerialisedTimelines;
				System.out.println("Serialized Timelines");
			}
			//
			// // /////////////////////////////////////
			if (Constant.toDeSerializeTimelines)
			{
				if (Constant.getDatabaseName().equals("gowalla1"))
				{
					usersDayTimelinesOriginal = (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
							.kryoDeSerializeThis(pathToLatestSerialisedTimelines);
				}
				else
				{
					usersDayTimelinesOriginal = (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
							.deSerializeThis(pathToLatestSerialisedTimelines);
				}
				System.out.println("deserialised userTimelines.size()=" + usersDayTimelinesOriginal.size());
				System.out.println("Deserialized Timelines");
			}
			long ct = System.currentTimeMillis();

			if (!Constant.checkAllParametersSet())
			{
				System.err.println("All essential paramaters in Constant not set. Exiting");
				PopUps.showError("All essential paramaters in Constant not set. Exiting");
				System.exit(-162);
			}

			Pair<Boolean, String> hasDuplicateDates = TimelineUtils.hasDuplicateDates(usersDayTimelinesOriginal);
			if (hasDuplicateDates.getFirst())
			{
				System.out.println(
						"Alert!Alert! hasDuplicateDates.  for users:" + hasDuplicateDates.getSecond().toString());
			}
			else
			{
				System.out.println("Thank God, no duplicate dates.");
			}
			//////////// for Gowalla start
			// WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
			//////////// false, false, false, "GowallaUserDayTimelines.csv");

			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = reduceAndCleanTimelines(
					Constant.getDatabaseName(), usersDayTimelinesOriginal, true);

			/////////// start of temp
			long numOfActWithMultipleWorkingLevelCatID = 0, numOfAOs = 0;
			HashSet<String> multipleWorkingLevelCatIds = new HashSet<>();
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
				{
					for (ActivityObject ao : dateEntry.getValue().getActivityObjectsInTimeline())
					{
						numOfAOs += 1;
						if (RegexUtils.patternDoubleUnderScore.split(ao.getWorkingLevelCatIDs()).length > 1)
						{
							multipleWorkingLevelCatIds.add(ao.getWorkingLevelCatIDs());
							numOfActWithMultipleWorkingLevelCatID += 1;
						}
					}
				}
			}
			System.out.println("num of AOs = " + numOfAOs);
			System.out.println("numOfActWithMultipleWorkingLevelCatID = " + numOfActWithMultipleWorkingLevelCatID);
			System.out.println("% ActWithMultipleWorkingLevelCatID = "
					+ ((numOfActWithMultipleWorkingLevelCatID / numOfAOs) * 100));
			WritingToFile.writeToNewFile(
					multipleWorkingLevelCatIds.stream().map(s -> s.toString()).collect(Collectors.joining("\n")),
					Constant.outputCoreResultsPath + "SetmultipleWorkingLevelCatIds.csv");
			// System.exit(0);
			/////////// end of temp
			// int countOfSampleUsers = 0;
			// $$$
			// Constant.outputCoreResultsPath =
			// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_";

			// $$TimelineStats.timelineStatsController(usersCleanedDayTimelines);
			// $$TimelineWEKAClusteringController clustering = new
			// TimelineWEKAClusteringController(usersCleanedDayTimelines, null);

			// // start of for gowalla weather data generation //commented out 22 Jan 2017
			// GowallaWeatherPreprocessing.GowallaWeatherPreprocessingController(usersCleanedDayTimelines,
			// Constant.outputCoreResultsPath);
			// // end of for gowalla weather data generation //commented out 22 Jan 2017

			// start of consective counts

			// good curtain 7 Feb 2017 start
			// LinkedHashMap<String, ArrayList<Integer>> consecutiveCounts = TimelineUtils
			// .countConsecutiveSimilarActivities2(usersCleanedDayTimelines,
			// /* $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Jan22/" */
			// // "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/ConsecutiveAnalysis/",
			// "./dataWritten/ConsecutiveDiffAnalysis3/",
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/UI/CatIDNameDictionary.kryo");
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/CatIDNameDictionary.kryo");
			// good curtain 7 Feb 2017 end

			// end of consecutive counts

			// for (int i = 0; i < s.length; i++)
			// {
			// int startUserIndex = Integer.valueOf(s[i]) - 1;// 100
			// int endUserIndex = startUserIndex + 99; // 199
			//
			// int countOfSampleUsers = 0;
			// System.out.println("startUserIndex=" + startUserIndex + " endUserIndex" + endUserIndex);
			// }
			String groupsOf100UsersLabels[] = { "1", "101", "201", "301", "401", "501", "601", "701", "801", "901" };
			// ,// "1001" };
			System.out.println("List of all users:\n" + usersCleanedDayTimelines.keySet().toString() + "\n");

			WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
					Constant.getCommonPath() + "NumOfActsPerUserPerDate.csv");
			String commonBasePath = Constant.getCommonPath();

			// // important curtain 1 start 10 Feb 2017
			for (String groupsOf100UsersLabel : groupsOf100UsersLabels)
			{
				// important so as to wipe the previously assigned user ids
				Constant.initialise(commonPath, Constant.getDatabaseName());

				int startUserIndex = Integer.valueOf(groupsOf100UsersLabel) - 1;// 100
				int endUserIndex = startUserIndex + 99;// 199;// 140; // 199
				Constant.outputCoreResultsPath = commonBasePath + groupsOf100UsersLabel + "/";
				Files.createDirectories(Paths.get(Constant.outputCoreResultsPath)); // added on 9th Feb 2017
				int indexOfSampleUsers = 0;

				/// sample users
				LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsers = new LinkedHashMap<>();

				for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
				{
					// countOfSampleUsers += 1;
					if (indexOfSampleUsers < startUserIndex)
					{
						indexOfSampleUsers += 1;
						continue;
					}
					if (indexOfSampleUsers > endUserIndex)
					{
						indexOfSampleUsers += 1;
						break;
					}
					sampledUsers.put(userEntry.getKey(), userEntry.getValue());
					// $$System.out.println("putting in user= " + userEntry.getKey());
					indexOfSampleUsers += 1;
				}
				System.out.println("num of sampled users for this iteration = " + sampledUsers.size());
				System.out.println(" -- Users = " + sampledUsers.keySet().toString());

				// $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
				// $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
				// $$RecommendationTestsBaseClosestTime recommendationsTest = new RecommendationTestsBaseClosestTime(
				// $$ sampledUsers);
				RecommendationTestsMar2017Gen recommendationsTest = new RecommendationTestsMar2017Gen(sampledUsers,
						Constant.lookPastType, Constant.caseType, Constant.typeOfThresholds, Constant.getUserIDs(),
						Constant.percentageInTraining);

				// RecommendationTestsDayWise2FasterJan2016 recommendationsTest = new
				// RecommendationTestsDayWise2FasterJan2016(
				// sampledUsers);
			}
			// // important curtain 1 end 10 Feb 2017
			// $$$
			// String, LinkedHashMap<Date, UserDayTimeline>
			//////////// for Gowalla end

			// WritingToFile.writeUsersDayTimelines(usersDayTimelinesOriginal, "usersDayTimelinesOriginal", true, true,
			// true);// users
			// $$ WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
			// true, true, true, "AllInSameFileApr21UncorrrectedTZ.csv");// users

			// //Start of disabled on 15 Sep 2016 _1
			// String serialisedTimelines1 = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesAPR21.lmap";
			// String serialisedTimelines2 = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesJUN18.lmap";
			// TimelineUtilities.CompareTimelines(serialisedTimelines1, serialisedTimelines2);
			// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersCleanedRearrangedDayTimelines =
			// new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
			// usersCleanedRearrangedDayTimelines = TimelineUtilities.cleanDayTimelines(usersDayTimelinesOriginal);
			// usersCleanedRearrangedDayTimelines =
			// TimelineUtilities.rearrangeDayTimelinesOrderForDataset(usersCleanedRearrangedDayTimelines);
			// //end of disabled on 15 Sep 2016 _1

			// $$ WritingToFile.writeUsersDayTimelines(usersDayTimelinesOriginal, "usersCleanedRearrangedDayTimelines",
			// true, true, true);// users

			// UtilityBelt.traverseUserTimelines(userTimelines); // Debugging Check: OK Cheked again with timestamp: OK
			// UtilityBelt.traverseActivityEvents(allActivityEvents); // Debugging Check: OK

			// for actual recommendation
			// RecommendationMaster recommendationMaster=new
			// RecommendationMaster(userTimelines,dateAtRecomm,timeAtRecomm,weekDayAtRecomm,userAtRecomm);

			// for testing
			// RecommendationTestsBaseClosestTime recommendationsTest=new
			// RecommendationTestsBaseClosestTime(userTimelines);//,userAtRecomm);
			// RecommendationTestsDayWise recommendationsTest=new
			// RecommendationTestsDayWise(userTimelines);//,userAtRecomm);

			/** CURRENT: To run the recommendation experiments **/
			// $$ RecommendationTestsDayWise2FasterJan2016 recommendationsTest =
			// $$ new RecommendationTestsDayWise2FasterJan2016(usersDayTimelinesOriginal);
			// $$ disabled on 15 Sep 2016RecommendationTestsMU recommendationsTest = new
			// RecommendationTestsMU(usersDayTimelinesOriginal);
			// $$RecommendationTestsMU recommendationsTest = new RecommendationTestsMU(sampledUsers);//
			// usersCleanedDayTimelines);
			// $$$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);//
			// usersCleanedDayTimelines);
			// $$RecommendationTestsBaseClosestTime recommendationsTest = new
			// RecommendationTestsBaseClosestTime(sampledUsers);// usersCleanedDayTimelines);

			/**** CURRENT END ****/

			// repeat start
			// 201-300 users
			// countOfSampleUsers = 0;
			// Constant.outputCoreResultsPath =
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_201/";
			// for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userEntry : usersCleanedDayTimelines.entrySet())
			// {
			// countOfSampleUsers += 1;
			// if (countOfSampleUsers <= 200)
			// {
			// continue;
			// }
			// if (countOfSampleUsers > 300)
			// {
			// break;
			// }
			// sampledUsers.put(userEntry.getKey(), userEntry.getValue());
			// }
			// for (String userS : sampledUsers.keySet())
			// {
			// System.out.println("userS= " + userS);
			// }
			// recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);// usersCleanedDayTimelines);

			// 301-400
			// countOfSampleUsers = 0;
			// Constant.outputCoreResultsPath =
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_301/";
			//
			// for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userEntry : usersCleanedDayTimelines.entrySet())
			// {
			// countOfSampleUsers += 1;
			// if (countOfSampleUsers <= 300)
			// {
			// continue;
			// }
			// if (countOfSampleUsers > 400)
			// {
			// break;
			// }
			// sampledUsers.put(userEntry.getKey(), userEntry.getValue());
			// }
			// for (String userS : sampledUsers.keySet())
			// {
			// System.out.println("userS= " + userS);
			// }
			// recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);// usersCleanedDayTimelines);
			//
			// 401-500
			// countOfSampleUsers = 0;
			// Constant.outputCoreResultsPath =
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_401/";
			//
			// for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userEntry : usersCleanedDayTimelines.entrySet())
			// {
			// countOfSampleUsers += 1;
			// if (countOfSampleUsers <= 400)
			// {
			// continue;
			// }
			// if (countOfSampleUsers > 500)
			// {
			// break;
			// }
			// sampledUsers.put(userEntry.getKey(), userEntry.getValue());
			// }
			// for (String userS : sampledUsers.keySet())
			// {
			// System.out.println("userS= " + userS);
			// }
			// recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);// usersCleanedDayTimelines);

			// 501-600
			// countOfSampleUsers = 0;
			// Constant.outputCoreResultsPath =
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_501/";
			//
			// for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userEntry : usersCleanedDayTimelines.entrySet())
			// {
			// countOfSampleUsers += 1;
			// if (countOfSampleUsers <= 500)
			// {
			// continue;
			// }
			// if (countOfSampleUsers > 600)
			// {
			// break;
			// }
			// sampledUsers.put(userEntry.getKey(), userEntry.getValue());
			// }
			// for (String userS : sampledUsers.keySet())
			// {
			// System.out.println("userS= " + userS);
			// }
			// recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);// usersCleanedDayTimelines);

			// // 601-700
			// countOfSampleUsers = 0;
			// Constant.outputCoreResultsPath =
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_601/";
			//
			// for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userEntry : usersCleanedDayTimelines.entrySet())
			// {
			// countOfSampleUsers += 1;
			// if (countOfSampleUsers <= 600)
			// {
			// continue;
			// }
			// if (countOfSampleUsers > 700)
			// {
			// break;
			// }
			// sampledUsers.put(userEntry.getKey(), userEntry.getValue());
			// }
			// for (String userS : sampledUsers.keySet())
			// {
			// System.out.println("userS= " + userS);
			// }
			// recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);// usersCleanedDayTimelines);

			// repeat end

			/** To get some stats on the generated timelines **/
			// TimelineStats.timelineStatsController(usersTimelines);
			// TimelineStats.timelineStatsController(usersDayTimelines);
			// $$ TimelineStats.timelineStatsController(usersDayTimelinesOriginal);

			/** Clustering timelines using weka and after feature extraction **/
			// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersCleanedRearrangedDayTimelines = new
			// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
			// usersCleanedRearrangedDayTimelines = UtilityBelt.cleanDayTimelines(usersDayTimelinesOriginal);
			// usersCleanedRearrangedDayTimelines =
			// UtilityBelt.rearrangeDayTimelinesOrderForDataset(usersCleanedRearrangedDayTimelines);

			/** CURRENT **/
			// $$ TimelineWEKAClusteringController clustering = new
			// TimelineWEKAClusteringController(usersCleanedRearrangedDayTimelines, null);

			/** END OF CURRENT **/

			/** CURRENT **/
			// $$TimelineStats.timelineStatsController(usersCleanedRearrangedDayTimelines);
			/** END OF CURRENT **/

			/** To experiment with distance **/
			// ExperimentDistances ed = new ExperimentDistances(usersTimelines);

			// RecommendationMasterDayWise2Faster*/

			// Start of not needed any more as we are doing leave one out cross validation
			// Pair<ArrayList<String>, ArrayList<String>> trainingTestUsers = trainingTestSplit.getTrainingTestUsers();
			// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> onlyTrainingUsersTimelines =
			// UtilityBelt.expungeUserDayTimelinesByUser(usersDayTimelines,
			// trainingTestUsers.getSecond());
			// TimelineWEKAClusteringController clustering = new
			// TimelineWEKAClusteringController(onlyTrainingUsersTimelines);
			// TimelineWEKAClusteringController clustering = new TimelineWEKAClusteringController(usersDayTimelines,
			// trainingTestUsers);
			// End of not needed any more as we are doing leave one out cross validation

			long et = System.currentTimeMillis();

			if (Constant.USEPOOLED)
			{
				ConnectDatabase.destroyPooledConnection();
			}

			System.out.println("This experiment took " + ((et - ct) / 1000) + " seconds");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * For Gowalla data:
	 * <ol type="1">
	 * <li>removes days with less than 10 acts per day</li>
	 * <li>removes users with less than 50 days</li>
	 * </ol>
	 * For all data, removes day timelines:
	 * <ol type="1">
	 * <li>with no valid activity,</li>
	 * <li>with <=1 distinct valid activity, and</li>
	 * <li>the weekend day timelines.</li>
	 * </ol>
	 * For Gowalla data:
	 * <ol type="1">
	 * <li>again removes users with less than 50 days (this is after cleaning)</li>
	 * 
	 * @param databaseName
	 * @param usersDayTimelinesOriginal
	 * @param writeToFile
	 * @return
	 */
	private LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceAndCleanTimelines(String databaseName,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile)
	{
		if (databaseName.equals("gowalla1"))
		{
			usersDayTimelinesOriginal = reduceGowallaTimelines(Constant.getDatabaseName(), usersDayTimelinesOriginal,
					true);
		}

		///// clean timelines
		System.out.println(
				"\n-- Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day timelines.");
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = TimelineUtils
				.cleanDayTimelines(usersDayTimelinesOriginal);
		if (writeToFile)
		{// WritingToFile.writeUsersDayTimelinesSameFile(usersCleanedDayTimelines, "usersCleanedDayTimelines", false,
			// false, false,"GowallaUserDayTimelinesCleaned.csv");// users
			WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
					"usersCleanedDayTimelines", "GowallaPerUserDayNumOfActsCleaned.csv");// us
			WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
					Constant.getCommonPath() + "NumOfDaysPerUserCleaned.csv");
		}
		System.out.println("Num of users cleaned = " + usersCleanedDayTimelines.size());
		///

		if (databaseName.equals("gowalla1"))
		{
			///// again remove users with less than 50 days (these are the clean days)
			System.out.println("\n--again remove users with less than 50 days (these are the clean days)");
			usersCleanedDayTimelines = TimelineUtils.removeUsersWithLessDays(usersCleanedDayTimelines, 50,
					Constant.getCommonPath() + "removeCleanedDayTimelinesWithLessThan50DaysLog.csv");
		}

		if (writeToFile)
		{// Writing user day timelines. big file ~ 17.3GB
			// WritingToFile.writeUsersDayTimelinesSameFile(usersCleanedDayTimelines,
			// "usersCleanedDayTimelinesReduced3",false, false, false, "GowallaUserDayTimelinesCleanedReduced3.csv");//

			// write a subset of timelines
			Map<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelinesSampled = usersCleanedDayTimelines
					.entrySet().stream().limit(2).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

			WritingToFile.writeUsersDayTimelinesSameFile(
					new LinkedHashMap<String, LinkedHashMap<Date, Timeline>>(usersCleanedDayTimelinesSampled),
					"usersCleanedDayTimelinesReduced3First2UsersOnly", false, false, false,
					"GowallaUserDayTimelinesCleanedReduced3First2UsersOnly.csv");// users

			WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
					"usersCleanedDayTimelinesReduced3", "GowallaPerUserDayNumOfActsCleanedReduced3.csv");// us
			WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
					Constant.getCommonPath() + "NumOfDaysPerUserCleanedReduced3.csv");
			System.out.println("Num of users cleaned reduced3  = " + usersCleanedDayTimelines.size());
		}
		return usersCleanedDayTimelines;
	}

	/**
	 * removed days with less than 10 acts per day
	 * <p>
	 * removed users with less than 50 days
	 * 
	 * @param databaseName
	 * @param usersDayTimelinesOriginal
	 * @param writeToFile
	 * @return
	 */
	private LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceGowallaTimelines(String databaseName,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile)
	{
		System.out.println("Inside reduceGowallaTimelines");

		if (databaseName.equals("gowalla1") == false)
		{
			String msg = PopUps.getCurrentStackTracedErrorMsg(
					"Error in reduceTimelines(): should not be called for databases other than gowalla1. Called for database: "
							+ databaseName);
			System.err.println(msg);
			return null;
		}

		else
		{
			// Originally received timelines
			if (writeToFile)
			{
				WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
						"usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActs.csv");// us
				WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
						Constant.getCommonPath() + "NumOfDaysPerUser.csv");
			}
			System.out.println("Num of users = " + usersDayTimelinesOriginal.size());

			////////// removed days with less than 10 acts per day
			System.out.println("\n-- removing days with less than 10 acts per day");
			usersDayTimelinesOriginal = TimelineUtils.removeDayTimelinesWithLessAct(usersDayTimelinesOriginal, 10,
					Constant.getCommonPath() + "removeDayTimelinesWithLessThan10ActLog.csv");
			if (writeToFile)
			{
				WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
						"usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced1.csv");// us
				WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
						Constant.getCommonPath() + "NumOfDaysPerUserReduced1.csv");
				// WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
				// false, false, false, "GowallaUserDayTimelinesReduced1.csv");// users
			}
			System.out.println("Num of users reduced1 = " + usersDayTimelinesOriginal.size());
			//////////

			////////// removed users with less than 50 days
			System.out.println("\n-- removing users with less than 50 days");
			usersDayTimelinesOriginal = TimelineUtils.removeUsersWithLessDays(usersDayTimelinesOriginal, 50,
					Constant.getCommonPath() + "removeDayTimelinesWithLessThan50DaysLog.csv");
			if (writeToFile)
			{
				WritingToFile.writeNumOfActsPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
						"usersDayTimelinesOriginal", "GowallaPerUserDayNumOfActsReduced2.csv");// us
				WritingToFile.writeNumOfDaysPerUsersDayTimelinesSameFile(usersDayTimelinesOriginal,
						Constant.getCommonPath() + "NumOfDaysPerUserReduced2.csv");
				// WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
				// false, false, false,"GowallaUserDayTimelinesReduced2.csv");// users
			}
			System.out.println("Num of users reduced2 = " + usersDayTimelinesOriginal.size());
			//////////

			return usersDayTimelinesOriginal;
		}
	}

	/**
	 * Sets commonPath, pathToLatestSerialisedJSONArray, pathForLatestSerialisedJSONArray,
	 * pathToLatestSerialisedTimelines, pathForLatestSerialisedTimelines
	 * 
	 * @param databaseName
	 */
	public void setPaths(String databaseName)
	{
		LocalDateTime currentDateTime = LocalDateTime.now();

		switch (databaseName)
		{
		case "gowalla1":
			pathToLatestSerialisedJSONArray = null;// "";
			pathForLatestSerialisedJSONArray = "" + currentDateTime.getMonth().toString().substring(0, 3)
					+ currentDateTime.getDayOfMonth() + "obj";
			pathToLatestSerialisedTimelines = null;
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30/UserTimelinesNOV30.kryo";
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30/UserTimelines.kryo";
			// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/GowallaUserDayTimelines13Sep2016.kryo";//
			// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep15DatabaseGenerationJava/GowallaUserDayTimelines13Sep2016.kryo";
			pathForLatestSerialisedTimelines = null;
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30/UserTimelines"
			// + currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth()
			// + ".kryo";
			commonPath = Constant.outputCoreResultsPath;
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/";
			// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Jan22/";// $$Nov30/";//
			// run/media/gunjan/BoX1/GowallaSpaceSpaceSpace/GowallaDataWorksSep19/";//
			/// "/run/media/gunjan/BoX2/GowallaSpaceSpace/GowallaDataWorksSep16/";
			break;

		case "geolife1":
			pathToLatestSerialisedJSONArray = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifeJSONArrayAPR21obj";
			// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifeJSONArrayMAY27obj";
			// GeolifeJSONArrayFeb13.obj";
			pathForLatestSerialisedJSONArray = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifeJSONArray"
					+ currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + "obj";

			// $$UMAP submission
			// $$pathToLatestSerialisedTimelines = "/run/media/gunjan/HOME/gunjan/Geolife Data
			// Works/UserTimelinesJUN18.lmap";//
			// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesJUN15.lmap";//
			// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data
			// Works/UserTimelinesAPR15.lmap";//
			// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesAPR10.lmap";//
			// UserTimelinesFeb13.lmap";

			// After UMAP submission 19th April 2016
			pathToLatestSerialisedTimelines = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelinesAPR21.lmap";

			pathForLatestSerialisedTimelines = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/UserTimelines"
					+ currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + ".lmap";
			commonPath = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/";// version 3 based on rank score
																				// function
			break;

		case "dcu_data_2":
			pathToLatestSerialisedJSONArray = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/JSONArrayOct29.obj";
			pathForLatestSerialisedJSONArray = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/JSONArray"
					+ currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + "obj";

			pathToLatestSerialisedTimelines = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/DCUUserTimelinesJUN19.lmap";
			// "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
			// Works/WorkingSet7July/DCUUserTimelinesJUN15.lmap";
			// "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
			// Works/WorkingSet7July/DCUUserTimelinesMAY7.lmap"; DCUUserTimelinesOct29.lmap";
			pathForLatestSerialisedTimelines = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/DCUUserTimelines"
					+ currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth() + ".lmap";

			commonPath = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/";
			break;

		default:
			System.err.println("Error: unrecognised database name");
			break;
		}

	}

	/**
	 * Fetch fresh data from database and Serialized JSONArray
	 * 
	 * @param databaseName
	 * @throws SQLException
	 */
	public void fetchAndSerializeJSONArray(String databaseName) throws SQLException
	{
		String selectedAttributes = "activity_fact_table.User_ID," +
		// date_dimension_table.Date,time_dimension_table.Start_Time," +
				" activity_fact_table.Activity_ID, activity_fact_table.Time_ID, activity_fact_table.Location_ID, activity_fact_table.Date_ID";

		String orderByString = "activity_fact_table.User_ID, date_dimension_table.Date, time_dimension_table.Start_Time";
		// String whereQueryString ="where activity_dimension_table.Activity_Name!='Not Available' &&
		// activity_dimension_table.Activity_Name!='Unknown'";

		String whereQueryString = "";

		if (databaseName.equals("geolife1"))
		{
			whereQueryString = "where "
					+ /*
						 * * * "activity_fact_table.User_ID in ( 62, 84, 52, 68, 167, 179, 153, 85, 128, 10 ) && " +
						 */"activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY1
					+ "' && activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY2 + "'";// for
																											// faster
		} // "";// "where activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY1 +
			// "' && activity_dimension_table.Activity_Name!='" + Constant.INVALID_ACTIVITY2 + "'";

		SerializableJSONArray jsonArray = new SerializableJSONArray(
				ConnectDatabase.getJSONArrayOfDataTable(selectedAttributes, whereQueryString, orderByString));

		WritingToFile.writeToNewFile(jsonArray.toString(), Constant.getCommonPath() + "JSONArray.csv");
		// System.out.println(jsonArray.toString());
		Serializer.serializeThis(jsonArray, pathForLatestSerialisedJSONArray);
		pathToLatestSerialisedJSONArray = pathForLatestSerialisedJSONArray;
		System.out.println("Fetched fresh data from database and Serialized JSONArray");
	}

	/**
	 * 
	 * @param databaseName
	 * @param jsonArrayD
	 * @param gowallaDataFolder
	 * @return
	 */
	public LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createTimelines(String databaseName,
			SerializableJSONArray jsonArrayD, String gowallaDataFolder)
	{
		long dt = System.currentTimeMillis();
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal = null;

		if (databaseName.equals("gowalla1"))
		{
			// $$String gowallaDataFolder = "./dataToRead/Feb23/DatabaseCreatedMerged/";// DatabaseCreatedMerged/";//
			// Feb2/DatabaseCreated/";
			System.out.println("gowallaDataFolder = " + gowallaDataFolder);
			// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/DatabaseCreated/";
			// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov29/DatabaseCreation/";
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinData = (LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>) Serializer
					.kryoDeSerializeThis(gowallaDataFolder + "mapForAllCheckinData.kryo");
			// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllCheckinData.kryo");
			LinkedHashMap<String, UserGowalla> mapForAllUserData = (LinkedHashMap<String, UserGowalla>) Serializer
					.kryoDeSerializeThis(gowallaDataFolder + "mapForAllUserData.kryo");
			// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllUserData.kryo");
			LinkedHashMap<Integer, LocationGowalla> mapForAllLocationData = (LinkedHashMap<Integer, LocationGowalla>) Serializer
					.kryoDeSerializeThis(gowallaDataFolder + "mapForAllLocationData.kryo");
			// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllLocationData.kryo");

			System.out.println("before creating timelines\n" + PerformanceAnalytics.getHeapInformation());

			usersDayTimelinesOriginal = TimelineUtils.createUserTimelinesFromCheckinEntriesGowalla(mapForAllCheckinData,
					mapForAllLocationData);

		}
		else
		{
			ArrayList<ActivityObject> allActivityEvents = UtilityBelt
					.createActivityObjectsFromJsonArray(jsonArrayD.getJSONArray());
			// UtilityBelt.traverseActivityEvents(allActivityEvents); // Debugging Check: OK
			usersDayTimelinesOriginal = TimelineUtils.createUserTimelinesFromActivityObjects(allActivityEvents);
		}

		System.out
				.println("userTimelines.size()=" + usersDayTimelinesOriginal.size() + " for database: " + databaseName);
		long lt = System.currentTimeMillis();
		System.out.println("timelines creation takes " + (lt - dt) / 1000 + " secs");

		return usersDayTimelinesOriginal;
	}

}