package org.activity.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.PathConstants;
import org.activity.evaluation.EvaluationSeq;
import org.activity.evaluation.RecommendationTestsMar2017GenSeqCleaned3Nov2017;
import org.activity.generator.ToyTimelineUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.SerializableJSONArray;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.CheckinEntry;
import org.activity.objects.CheckinEntryV2;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.UserGowalla;
import org.activity.probability.ProbabilityUtilityBelt;
import org.activity.sanityChecks.ResultsSanityChecks;
import org.activity.stats.TimelineStats;
import org.activity.ui.PopUps;
import org.activity.util.ConnectDatabase;
import org.activity.util.DateTimeUtils;
import org.activity.util.PerformanceAnalytics;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

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

	/**
	 * 
	 * @param databaseName
	 */
	public ControllerWithoutServer(String databaseName)
	{
		try
		{
			System.out.println("Starting ControllerWithoutServer>>>>\n" + PerformanceAnalytics.getHeapInformation()
					+ "\n" + "currentDateTime: " + LocalDateTime.now() + "\nRunning experiments for database: "
					+ databaseName);

			TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 21, 2016
			Constant.setDefaultTimeZone("UTC");

			// String pathToLatestSerialisedJSONArray = "", pathForLatestSerialisedJSONArray = "",
			// pathToLatestSerialisedTimelines = "", pathForLatestSerialisedTimelines = "", commonPath = "";

			setPathsSerialisedJSONTimelines(databaseName);

			// new ConnectDatabase(Constant.getDatabaseName()); // all method and variable in this class are static
			// new Constant(commonPath, Constant.getDatabaseName());

			System.out.println("Just before Constant.initialise:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
					+ PerformanceAnalytics.getHeapPercentageFree());
			/*
			 * $ Disabled for Gowalla dataset for now// ConnectDatabase.initialise(Constant.getDatabaseName()); // all
			 * method and variable in this class are static
			 */
			// specific for Gowalla dataset
			PathConstants.intialise(Constant.For9kUsers);
			Constant.initialise(commonPath, databaseName, PathConstants.pathToSerialisedCatIDsHierDist,
					PathConstants.pathToSerialisedCatIDNameDictionary, PathConstants.pathToSerialisedLocationObjects,
					PathConstants.pathToSerialisedUserObjects, PathConstants.pathToSerialisedGowallaLocZoneIdMap);
			String commonBasePath = Constant.getCommonPath();
			System.out.println("Just after Constant.initialise:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
					+ PerformanceAnalytics.getHeapPercentageFree());

			////////// ~~~~~~~~~~~~~~~~~`
			long dt1 = System.currentTimeMillis();
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal = createAllTimelines(
					databaseName, Constant.toSerializeJSONArray, Constant.toDeSerializeJSONArray,
					Constant.toCreateTimelines, Constant.toSerializeTimelines, Constant.toDeSerializeTimelines);
			// PopUps.showMessage("here0");
			////////// ~~~~~~~~~~~~~~~~~`
			//////////// for Gowalla start
			// WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
			// false, false, false, "GowallaUserDayTimelines.csv");
			System.out.println("Before reduceAndCleanTimelines\n" + PerformanceAnalytics.getHeapInformation());

			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = null;
			if (Constant.reduceAndCleanTimelinesBeforeRecomm)
			{
				if (Constant.For9kUsers)// For 9k users
				{
					usersCleanedDayTimelines = reduceAndCleanTimelines2(databaseName, usersDayTimelinesOriginal, true,
							commonBasePath, 10, 7, 500);
				}
				else // For 916 users
				{
					usersCleanedDayTimelines = reduceAndCleanTimelines(databaseName, usersDayTimelinesOriginal, true,
							commonBasePath);
				}
			}
			else// in this case, we are expecting the data is already subsetting and cleaned
			{
				System.out.println("Alert! Not reducing and cleaning data !!");
				usersCleanedDayTimelines = usersDayTimelinesOriginal;
				writeTimelineStats(usersCleanedDayTimelines, false, true, true, true, "UsersCleanedDayTimelines",
						commonBasePath);
			}

			usersDayTimelinesOriginal = null; // null this out so as to be ready for garbage collection.
			System.out.println("After reduceAndCleanTimelines\n" + PerformanceAnalytics.getHeapInformation());
			long dt3 = System.currentTimeMillis();
			System.out.println("Time taken = " + (dt3 - dt1) + " ms");
			// System.exit(0);
			/////////// start of temp
			// $$WritingToFile.writeNumberOfActsWithMultipleWorkingLevelCatID(usersCleanedDayTimelines, true,
			// $$ Constant.outputCoreResultsPath + "MultipleWorkingLevelCatIds.csv");
			/////////// end of temp

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

			// String groupsOf100UsersLabels[] = { "1", "101", "201", "301", "401", "501", "601", "701", "801", "901" };
			// ,// "1001" };
			// System.out.println("List of all users:\n" + usersCleanedDayTimelines.keySet().toString() + "\n");
			// String commonBasePath = Constant.getCommonPath();
			// PopUps.showMessage("here01");
			TimelineStats.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
					commonBasePath + "NumOfDaysPerUsersDayTimelines.csv");

			System.out.println("Before sampleUsersExec\n" + PerformanceAnalytics.getHeapInformation());
			// PopUps.showMessage("here02");
			///

			///////////////////
			// TimelineUtils.writeNumOfNullTZCinsPerUserPerLocID(usersCleanedDayTimelines,
			// "NOTZForCleanedSubsettedData");
			// Disaled on 23 April 2018 as we do not have nullTZ checkins in new dataset
			// $TimelineUtils.writeNumOfNullTZCinsPerUserPerLocIDTrainTestDataOnly(usersCleanedDayTimelines,
			// $ "NOTZForCleanedSubsettedTraintestData");

			TimelineUtils.countNumOfMultipleLocationIDs(usersCleanedDayTimelines);

			setDataVarietyConstants(usersCleanedDayTimelines, true, "UsersCleanedDTs_", true);

			writeActIDNamesInFixedOrder(Constant.getCommonPath() + "CatIDNameMap.csv");
			// System.exit(0);
			if (false)// temporary
			{
				TimelineUtils.writeAllActObjs(usersCleanedDayTimelines, Constant.getCommonPath() + "AllActObjs.csv");
				TimelineUtils.writeLocationObjects(Constant.getUniqueLocIDs(),
						DomainConstants.getLocIDLocationObjectDictionary(),
						commonBasePath + "UniqueLocationObjects.csv");
				// SpatialUtils.createLocationDistanceDatabase(DomainConstants.getLocIDLocationObjectDictionary());
				TimelineUtils.writeUserObjects(usersCleanedDayTimelines.keySet(),
						DomainConstants.getUserIDUserObjectDictionary(), commonBasePath + "UniqueUserObjects.csv");

			}

			if (false)// temporary for 22 feb 2018,
			{
				findUniqueLocationsInTrainTest(usersCleanedDayTimelines, true);
				System.exit(0);
			}

			// Curtain 8 Feb 2018 start
			// $$TimelineUtils.writeAllActObjs(usersCleanedDayTimelines, Constant.getCommonPath() + "AllActObjs.csv");

			// Curtain 8 Feb 2018 end

			///////////////////
			/*
			 * TreeSet<Integer> uniqueLocIDs = getUniqueLocIDs(usersCleanedDayTimelines);
			 * Serializer.serializeThis(uniqueLocIDs, "./dataWritten/UniqueLocIDsInCleanedTimeines.ser");
			 * TreeSet<Integer> uniqueLocIDs1 = (TreeSet<Integer>) Serializer
			 * .deSerializeThis("./dataWritten/UniqueLocIDsInCleanedTimeines.ser");
			 * Serializer.kryoSerializeThis(uniqueLocIDs, "./dataWritten/UniqueLocIDsInCleanedTimeines.kryo");
			 * TreeSet<Integer> uniqueLocIDs2 = (TreeSet<Integer>) Serializer
			 * .kryoDeSerializeThis("./dataWritten/UniqueLocIDsInCleanedTimeines.kryo");
			 */

			// $$TimelineStats.writeAllCitiesCounts(usersCleanedDayTimelines,
			// $$ Constant.outputCoreResultsPath + "AllCitiesCount");
			// // important curtain 1 start 21 Dec 2017 10 Feb 2017
			DomainConstants.clearGowallaLocZoneIdMap();// to save memory

			if (Constant.useToyTimelines)
			{
				boolean createToyTimelines = false, serialiseToyTimelines = false, deserialiseToyTimelines = true;// strue;
				LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersToyDayTimelines = null;
				if (createToyTimelines)
				{
					// Create Toy timelines by sampling
					// $$ ToyTimelineUtils.createToyTimelinesSamplingCinEntriesGowalla(usersCleanedDayTimelines);
					// creating toy timelines manually
					usersToyDayTimelines = ToyTimelineUtils.createToyTimelinesManuallyGowalla(
							usersCleanedDayTimelines.keySet(), Constant.getUniqueActivityIDs(),
							Constant.getUniqueLocationIDsPerActID(), Constant.getUniqueLocIDs(),
							PathConstants.commonPathToGowallaPreProcessedData);
				}
				if (serialiseToyTimelines)
				{
					Serializer.kryoSerializeThis(usersToyDayTimelines,
							Constant.getCommonPath() + "ToyTimelinesManually6June.kryo");
				}
				if (deserialiseToyTimelines)
				{
					usersToyDayTimelines = (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
							.kryoDeSerializeThis(PathConstants.pathToToyTimelines);
					// System.exit(0);
				}

				ToyTimelineUtils.writeOnlyActIDs(usersToyDayTimelines,
						Constant.getCommonPath() + "ToyTimelinesOnlyActIDs.csv");
				ToyTimelineUtils.writeOnlyActIDs2(usersToyDayTimelines,
						Constant.getCommonPath() + "ToyTimelinesOnlyActIDs2.csv");
				ToyTimelineUtils.writeActIDTS(usersToyDayTimelines,
						Constant.getCommonPath() + "ToyTimelinesActIDTS.csv");

				// do it again using the toy timelines
				setDataVarietyConstants(usersToyDayTimelines, true, "ToyTs_", true);

				// done especially for toy timelines to avoid writing all activitie in timeline activity stats.
				Constant.setActivityNames(
						Constant.getUniqueActivityIDs().stream().map(i -> String.valueOf(i)).toArray(String[]::new));

				writeActIDNamesInFixedOrder(Constant.getCommonPath() + "ToyCatIDNameMap.csv");

				// PopUps.showMessage("After toy timelines creation!!");
				// $$Disabled on May29 2018 TimelineStats.timelineStatsController(usersCleanedDayToyTimelines);
				// PopUps.showMessage("here");
				WToFile.writeUsersDayTimelinesSameFile(usersToyDayTimelines, "usersToyDayTimelines", false, false,
						false, "GowallaUserDayToyTimelines.csv", commonBasePath);
				// PopUps.showMessage("here2");
				TimelineStats.timelineStatsController(usersToyDayTimelines);
				// System.exit(0);
				// End of Moved here on 18 May 2018

				// make the usersCleanedDayTimelines point to the toy timelines
				usersCleanedDayTimelines = usersToyDayTimelines;
			}

			if (Constant.For9kUsers)
			{
				System.out.println("For9kUsers :");
				sampleUsersExecuteExperimentsFor9kUsers(commonBasePath, usersCleanedDayTimelines,
						Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor));
				// System.exit(0);
			}
			else
			{
				if (Constant.useRandomlySampled100Users)
				{
					System.out.println("useRandomlySampled100Users :");
					List<String> sampledUserIndicesStr = ReadingFromFile
							// .oneColumnReaderString("./dataToRead/RandomlySample100Users/Mar1_2018.csv", ",", 0,
							// .oneColumnReaderString("./dataToRead/RandomlySample100UsersApril24_2018.csv", ",", 0,
							.oneColumnReaderString(Constant.pathToRandomlySampledUserIndices, ",", 0, false);
					System.out.println("pathToRandomLySampleUserIndices=" + Constant.pathToRandomlySampledUserIndices);
					List<Integer> sampledUserIndices = sampledUserIndicesStr.stream().map(i -> Integer.valueOf(i))
							.collect(Collectors.toList());

					sampleUsersByIndicesExecuteRecommendationTests(usersCleanedDayTimelines,
							DomainConstants.gowalla100RandomUsersLabel, sampledUserIndices, commonBasePath);
				}
				else if (Constant.runForAllUsersAtOnce)
				{
					System.out.println("runForAllUsersAtOnce :");
					if (true)
					{
						int numOfUsers = usersCleanedDayTimelines.size();
						List<Integer> allUserIndices = IntStream.range(0, numOfUsers).boxed()
								.collect(Collectors.toList());
						sampleUsersByIndicesExecuteRecommendationTests(usersCleanedDayTimelines, "All", allUserIndices,
								commonBasePath);
						ResultsSanityChecks.assertSameNumOfRTsAcrossAllMUsForUsers(commonBasePath, false);
					}
					if (false)// temporary sanity check
					{
						List<String> sampledUserIDsStr = ReadingFromFile.oneColumnReaderString(
								"/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/APR11ED1.0AllActsFDStFilter0hrs_debugging/All/MatchingUnit3.0/UsersWithNoValidRTs.csv",
								",", 0, false);

						sampleUsersByIDsExecuteRecommendationTests(usersCleanedDayTimelines, "All", sampledUserIDsStr,
								commonBasePath);
					}
				}
				else
				{ // Start of curtain Aug 11 2017
					sampleUsersExecuteRecommendationTests(usersCleanedDayTimelines,
							DomainConstants.gowallaUserGroupsLabels, commonBasePath);
				}
				// End of curtain Aug 11 2017
			}
			// $$// important curtain 1 end 21 Dec 2017 10 Feb 2017

			// // important curtain 2 start 2 June 2017
			// TimelineStats.timelineStatsController(usersCleanedDayTimelines);
			// // important curtain 2 start 2 June 2017
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

			/** To get some stats on the generated timelines **/
			// $$TimelineStats.timelineStatsController(usersCleanedDayTimelines);// usersTimelines);
			// TimelineStats.timelineStatsController(usersDayTimelines);
			// $$ TimelineStats.timelineStatsController(usersDayTimelinesOriginal);

			/** Clustering timelines using weka and after feature extraction **/
			// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersCleanedRearrangedDayTimelines = new
			// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
			// usersCleanedRearrangedDayTimelines = UtilityBelt.cleanDayTimelines(usersDayTimelinesOriginal);
			// usersCleanedRearrangedDayTimelines =
			// UtilityBelt.rearrangeDayTimelinesOrderForDataset(usersCleanedRearrangedDayTimelines);

			/** CURRENT **/
			// $$TimelineWEKAClusteringController clustering = new
			// TimelineWEKAClusteringController(usersCleanedDayTimelines,
			// $$ null);
			// usersCleanedRearrangedDayTimelines, null);

			/** END OF CURRENT **/

			/** CURRENT **/
			// $$TimelineStats.timelineStatsController(usersCleanedRearrangedDayTimelines);
			/** END OF CURRENT **/

			/** To experiment with distance **/
			// ExperimentDistances ed = new ExperimentDistances(usersTimelines);

			long et = System.currentTimeMillis();

			if (Constant.USEPOOLED)
			{
				ConnectDatabase.destroyPooledConnection();
			}
			// $System.out.println("This experiment took " + ((et - ct) / 1000) + " seconds");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Set and/or write UniqueLocIDs, UniqueLocIDsPerActID, UserIDActIDLocIDMap, UniqueActivityIDs, UniquePDValPerUser
	 * in the given timelines
	 * 
	 * @param givenDayTimelines
	 * @param write
	 * @param labelPhrase
	 * @param setConstantVariables
	 */
	public static void setDataVarietyConstants(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> givenDayTimelines,
			boolean write, String labelPhrase, boolean setConstantVariables)
	{
		if (setConstantVariables)
		{
			Constant.setUniqueLocIDs(TimelineUtils.getUniqueLocIDs(givenDayTimelines, write, labelPhrase));
			Constant.setUniqueLocationIDsPerActID(
					TimelineUtils.getUniqueLocIDsPerActID(givenDayTimelines, write, labelPhrase));
			Constant.setUserIDActIDLocIDsMap(
					TimelineUtils.getUserIDActIDLocIDMap(givenDayTimelines, write, labelPhrase));
			Constant.setUniqueActivityIDs(TimelineUtils.getUniqueActivityIDs(givenDayTimelines, write, labelPhrase));
			Constant.setUniquePDValsPerUser(TimelineUtils.getUniquePDValPerUser(givenDayTimelines, write, labelPhrase));
		}
		else
		{
			TimelineUtils.getUniqueLocIDs(givenDayTimelines, write, labelPhrase);
			TimelineUtils.getUniqueLocIDsPerActID(givenDayTimelines, write, labelPhrase);
			TimelineUtils.getUserIDActIDLocIDMap(givenDayTimelines, write, labelPhrase);
			TimelineUtils.getUniqueActivityIDs(givenDayTimelines, write, labelPhrase);
			TimelineUtils.getUniquePDValPerUser(givenDayTimelines, write, labelPhrase);
		}
	}

	/**
	 * Writes actID, Act names in the order fixed in Constant.activityNames
	 * 
	 * @param absFileNameToWrite
	 */
	private void writeActIDNamesInFixedOrder(String absFileNameToWrite)
	{
		String[] activityNames = Constant.getActivityNames();
		StringBuilder sb = new StringBuilder("ActID,ActName\n");
		for (String a : activityNames)
		{
			sb.append(a + "," + DomainConstants.catIDNameDictionary.get(Integer.valueOf(a)) + "\n");
		}
		WToFile.writeToNewFile(sb.toString(), absFileNameToWrite);// Constant.getCommonPath() + "CatIDNameMap.csv");
	}

	/**
	 * <p>
	 * logic extracted to a method on 18 May 2018, before it was part of ControllerWithoutServer constructor. Not sure
	 * whether it should be static or non-static. However, keeping it static at the moment to avoid chances of unwanted
	 * state changes.
	 * 
	 * @param commonBasePath
	 * @param usersCleanedDayTimelines
	 * @param muArray
	 * @throws IOException
	 */
	private static void sampleUsersExecuteExperimentsFor9kUsers(String commonBasePath,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, double[] muArray)
			throws IOException
	{
		// Start of curtain Aug 14 2017
		// $$selectGivenUsersExecuteRecommendationTests(usersCleanedDayTimelines, IntStream
		// $$ .of(DomainConstants.gowallaUserIDInUserGroup1Users).boxed().collect(Collectors.toList()),
		// $$commonBasePath, "1");
		// End of curtain Aug 14 2017
		boolean useSampledUsersFromFile = true;
		ArrayList<ArrayList<String>> listOfSampledUserIDs = null;
		if (useSampledUsersFromFile)
		{
			String sampledUsersListFile = "./dataToRead/Jan16/randomlySampleUsers.txt";
			System.out.println("Reading Sampled users from " + sampledUsersListFile);
			listOfSampledUserIDs = ReadingFromFile.readRandomSamplesIntoListOfLists(sampledUsersListFile, 13, 21, ",");

			int listNum = 0;
			for (ArrayList<String> l : listOfSampledUserIDs)
			{
				System.out.println("List num:" + (++listNum));
				System.out.println(l.toString());
			}
		}
		else
		{
			System.out.println("New Randomly Sampling users");
			listOfSampledUserIDs = randomlySampleUsersIDs(usersCleanedDayTimelines, 9, 1000);
		}

		for (int sampleID = 0; sampleID < listOfSampledUserIDs.size(); sampleID++)
		{
			System.out.println(" listOfSampledUserIDs.get(sampleID)= " + listOfSampledUserIDs.get(sampleID));
			System.out
					.println(" listOfSampledUserIDs.get(sampleID).size= " + listOfSampledUserIDs.get(sampleID).size());

			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUserCleanedDayTimelines = getDayTimelinesForUserIDsV2(
					usersCleanedDayTimelines, listOfSampledUserIDs.get(sampleID));

			System.out.println("sampledUserCleanedDayTimelines.size()=" + sampledUserCleanedDayTimelines.size());

			sampleUsersExecuteRecommendationTests(sampledUserCleanedDayTimelines,
					DomainConstants.gowallaUserGroupsLabels, commonBasePath + "Sample" + sampleID + "/");

			new EvaluationSeq(3, commonBasePath + "Sample" + sampleID + "/", muArray);
		}

	}

	/**
	 * // temporary for 22 feb 2018, to find the unique locations in the training timelines (most recent five // days)
	 * and test timelines, this chunk of code has been borrowed from // RecommendationtestsMar2017GenSeq3Nov2017.java
	 * <p>
	 * Writes the following files:-
	 * <ol>
	 * <li>UniqueLocIDs5DaysTrainTest.csv</li>
	 * <li>UniqueLocationObjects5DaysTrainTest.csv</li>
	 * <li>AllActObjs5DaysTrain.csv</li>
	 * <li>AllActObjsTest.csv</li>
	 * </ol>
	 * 
	 * @param usersCleanedDayTimelines
	 * @param exit
	 * @since 22 feb 2018
	 */
	private void findUniqueLocationsInTrainTest(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean exit)
	{
		// temporary for 22 feb 2018, to find the unique locations in the training timelines (most recent five
		// days) and test timelines, this chunk of code has been borrowed from
		// RecommendationtestsMar2017GenSeq3Nov2017.java
		LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW = null;
		// training test timelines for all users continuous
		LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd = null;

		long tt1 = System.currentTimeMillis();
		if (Constant.collaborativeCandidates)
		{
			trainTestTimelinesForAllUsersDW = TimelineUtils.splitAllUsersTestTrainingTimelines(usersCleanedDayTimelines,
					Constant.percentageInTraining, Constant.cleanTimelinesAgainInsideTrainTestSplit);

			if (Constant.filterTrainingTimelinesByRecentDays)
			{
				trainTimelinesAllUsersContinuousFiltrd = RecommendationTestsMar2017GenSeqCleaned3Nov2017
						.getContinousTrainingTimelinesWithFilterByRecentDaysV2(trainTestTimelinesForAllUsersDW,
								Constant.getRecentDaysInTrainingTimelines());
			}
			else
			{
				// sampledUsersTimelines
				trainTimelinesAllUsersContinuousFiltrd = RecommendationTestsMar2017GenSeqCleaned3Nov2017
						.getContinousTrainingTimelines(trainTestTimelinesForAllUsersDW);
			}
		}
		System.out.println("time take for timeline train test splitting which might be save in experiment ="
				+ ((System.currentTimeMillis() - tt1) * 1.0) / 1000 + " secs");

		Set<Integer> uniqueLocTrains = TimelineUtils.getUniqueLocIDs(trainTimelinesAllUsersContinuousFiltrd, true,
				Constant.getCommonPath() + "UniqueLocIDs5DaysTrain.csv");
		Set<Integer> uniqueLocTests = TimelineUtils.getUniqueLocIDsFromTestOnly(trainTestTimelinesForAllUsersDW, true,
				Constant.getCommonPath() + "UniqueLocIDsTest.csv");

		Set<Integer> uniqueLocTrainsTests = new TreeSet<>();
		uniqueLocTrainsTests.addAll(uniqueLocTrains);
		uniqueLocTrainsTests.addAll(uniqueLocTests);

		WToFile.writeToNewFile(
				uniqueLocTrainsTests.stream().map(e -> e.toString()).collect(Collectors.joining("\n")).toString(),
				Constant.getCommonPath() + "UniqueLocIDs5DaysTrainTest.csv");

		TimelineUtils.writeLocationObjects(uniqueLocTrainsTests, DomainConstants.getLocIDLocationObjectDictionary(),
				Constant.getCommonPath() + "UniqueLocationObjects5DaysTrainTest.csv");

		TimelineUtils.writeAllActObjs(trainTimelinesAllUsersContinuousFiltrd,
				Constant.getCommonPath() + "AllActObjs5DaysTrain.csv");
		TimelineUtils.writeAllActObjsFromTestOnly(trainTestTimelinesForAllUsersDW,
				Constant.getCommonPath() + "AllActObjsTest.csv");
		// TimelineUtils.countNumOfMultipleLocationIDs(usersCleanedDayTimelines);
		if (exit)
		{
			System.exit(0);
		}
	}

	/**
	 * 
	 * @param usersCleanedDayTimelines
	 * @param numOfSublists
	 * @param sizeOfEachSublist
	 * @return
	 */
	private static ArrayList<ArrayList<String>> randomlySampleUsersIDs(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, int numOfSublists,
			int sizeOfEachSublist)
	{
		ArrayList<String> all9kUserIDs = UtilityBelt.getKeysAsOrderedList(usersCleanedDayTimelines);
		ArrayList<ArrayList<String>> sublists = ProbabilityUtilityBelt.randomlySampleIntoSublists(all9kUserIDs,
				numOfSublists, sizeOfEachSublist);
		// System.out.println("sublists= \n" + sublists);

		Pair<Integer, ArrayList<Double>> res = ProbabilityUtilityBelt.getIntersectionSizes(sublists);
		int numOfUniqueElements = res.getFirst();
		ArrayList<Double> intersectionSizes = res.getSecond();

		Pair<Double, String> stats = ProbabilityUtilityBelt.getSamplingIntersectionStats(all9kUserIDs.size(),
				numOfSublists, sizeOfEachSublist, numOfUniqueElements, intersectionSizes);
		// coverage = stats.getFirst();

		StringBuilder sampledUserIDsAsString = new StringBuilder("\nsampledUserIDsAsString:\n");
		sublists.stream().forEachOrdered(e -> sampledUserIDsAsString.append(e.toString() + "\n"));
		System.out.println(stats.getSecond());

		WToFile.writeToNewFile(stats.getSecond() + sampledUserIDsAsString.toString(),
				Constant.getCommonPath() + "randomlySampleUsers.txt");

		return sublists;
	}

	// Constant.getDatabaseName()
	// Constant.toSerializeJSONArray
	// Constant.toDeSerializeJSONArray
	// Constant.toCreateTimelines
	// Constant.toSerializeTimelines
	// Constant.toDeSerializeTimelines
	/**
	 * 
	 * @param databaseName
	 * @param toSerializeJSONArray
	 * @param toDeSerializeJSONArray
	 * @param toCreateTimelines
	 * @param toSerializeTimelines
	 * @param toDeSerializeTimelines
	 * @return
	 * @throws SQLException
	 */
	private LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createAllTimelines(String databaseName,
			boolean toSerializeJSONArray, boolean toDeSerializeJSONArray, boolean toCreateTimelines,
			boolean toSerializeTimelines, boolean toDeSerializeTimelines) throws SQLException
	{
		long dt1 = System.currentTimeMillis();
		if (toSerializeJSONArray)
		{
			fetchAndSerializeJSONArray(databaseName);
		}

		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		SerializableJSONArray jsonArrayD = null;
		long dt2 = System.currentTimeMillis();
		if (toDeSerializeJSONArray)
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
		System.out.println("Before createTimelines\n" + PerformanceAnalytics.getHeapInformation());
		if (toCreateTimelines)
		{
			usersDayTimelinesOriginal = createTimelines(databaseName, jsonArrayD,
					PathConstants.commonPathToGowallaPreProcessedData);
		}
		System.out.println("After createTimelines\n" + PerformanceAnalytics.getHeapInformation());

		// //to improve repeat execution performance...serialising
		if (Constant.toSerializeTimelines)
		{
			Serializer.serializeThis(usersDayTimelinesOriginal, pathForLatestSerialisedTimelines);
			pathToLatestSerialisedTimelines = pathForLatestSerialisedTimelines;
			System.out.println("Serialized Timelines");
		}
		//
		// // /////////////////////////////////////
		if (toDeSerializeTimelines)
		{
			if (databaseName.equals("gowalla1"))
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
			System.out
					.println("Alert!Alert! hasDuplicateDates.  for users:" + hasDuplicateDates.getSecond().toString());
		}
		else
		{
			System.out.println("Thank God, no duplicate dates.");
		}
		System.out.println("createTimelines() took " + (ct - dt1) + " ms");
		return usersDayTimelinesOriginal;
	}

	/**
	 * 
	 * @param usersCleanedDayTimelines
	 * @param groupsOf100UsersLabels
	 * @param commonBasePath
	 * @throws IOException
	 */
	private static void sampleUsersExecuteRecommendationTests(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
			String[] groupsOf100UsersLabels, String commonBasePath) throws IOException
	{
		// LinkedHashMap<Integer, String> indexOfBlackListedUsers = new LinkedHashMap<>();
		System.out.println("Inside sampleUsersExecuteRecommendationTests: usersCleanedDayTimelines received size="
				+ usersCleanedDayTimelines.size());

		for (String groupsOf100UsersLabel : groupsOf100UsersLabels)
		{
			System.out.println("-- iteration start for groupsOf100UsersLabel = " + groupsOf100UsersLabel);
			// important so as to wipe the previously assigned user ids
			Constant.initialise(commonBasePath, Constant.getDatabaseName());

			int startUserIndex = Integer.valueOf(groupsOf100UsersLabel) - 1;// 100
			int endUserIndex = startUserIndex + 99;// $$ should be 99;// 199;// 140; // 199
			Constant.setOutputCoreResultsPath(commonBasePath + groupsOf100UsersLabel + "/");

			Files.createDirectories(Paths.get(Constant.getOutputCoreResultsPath())); // added on 9th Feb 2017

			/// sample users
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsers = new LinkedHashMap<>(134);
			// (ceil) 100/0.75

			int indexOfSampleUser = 0;
			int numOfUsersSkippedGT553MaxActsPerDay = 0;
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				System.out.print(" indexOfSampleUser = " + indexOfSampleUser + "\t");
				// countOfSampleUsers += 1;
				if (indexOfSampleUser < startUserIndex)
				{
					System.out.println(" " + indexOfSampleUser + "<" + startUserIndex + " hence skipping");
					indexOfSampleUser += 1;
					continue;
				}
				if (indexOfSampleUser > endUserIndex)
				{
					System.out.println(" " + indexOfSampleUser + ">" + startUserIndex + " hence breaking");
					indexOfSampleUser += 1;
					break;
				}

				if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
				{
					System.out.println(" " + indexOfSampleUser + " Skipping user: " + userEntry.getKey()
							+ " as in gowallaUserIDsWithGT553MaxActsPerDay");
					WToFile.appendLineToFileAbs(indexOfSampleUser + "," + userEntry.getKey() + "\n",
							"IndexOfBlacklistedUsers.csv");
					numOfUsersSkippedGT553MaxActsPerDay += 1;
					indexOfSampleUser += 1;
					continue;
				}
				else
				{
					System.out.println(" choosing this ");
					sampledUsers.put(userEntry.getKey(), userEntry.getValue());
					indexOfSampleUser += 1;
				}
				// $$System.out.println("putting in user= " + userEntry.getKey());
			}

			// TODO likely the code segment below is not needed anymore as blacklisted users have already been removed.
			// start of get timelines for all users for collaborative approach
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsers = new LinkedHashMap<>(1000);
			int numOfAllUsersSkippedGT553MaxActsPerDay = 0;
			if (Constant.collaborativeCandidates)
			{
				for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
				{
					if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
					{
						numOfAllUsersSkippedGT553MaxActsPerDay += 1;
						continue;
					}
					else
					{
						allUsers.put(userEntry.getKey(), userEntry.getValue());
					}
				}
				System.out.println("got timelines for all users for coll cand: allUsers.size()= " + allUsers.size());
				// Sanity.eq(numOfUsersSkippedGT553MaxActsPerDay, numOfAllUsersSkippedGT553MaxActsPerDay,
				System.out.println("numOfUsersSkippedGT553MaxActsPerDay=" + numOfUsersSkippedGT553MaxActsPerDay
						+ " numOfAllUsersSkippedGT553MaxActsPerDay=" + numOfAllUsersSkippedGT553MaxActsPerDay);

			}
			// end of get timelines for all users for collaborative approach

			System.out.println("num of sampled users for this iteration = " + sampledUsers.size());
			System.out.println(" -- Users = " + sampledUsers.keySet().toString());

			// $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
			// $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
			// $$RecommendationTestsBaseClosestTime recommendationsTest = new RecommendationTestsBaseClosestTime(
			// $$ sampledUsers);

			System.out.println("Just Before recommendationsTest\n" + PerformanceAnalytics.getHeapInformation());

			// // start of curtain may 4 2017
			// RecommendationTestsMar2017Gen recommendationsTest = new RecommendationTestsMar2017Gen(sampledUsers,
			// Constant.lookPastType, Constant.caseType, Constant.typeOfThresholds, Constant.getUserIDs(),
			// Constant.percentageInTraining);
			// // end of curtain may 4 2017
			// System.exit(0);
			RecommendationTestsMar2017GenSeqCleaned3Nov2017 recommendationsTest = new RecommendationTestsMar2017GenSeqCleaned3Nov2017(
					sampledUsers, Constant.lookPastType, Constant.caseType, Constant.typeOfiiWASThresholds,
					Constant.getUserIDs(), Constant.percentageInTraining, 3, allUsers);

			/// /// RecommendationTestsMar2017GenDummyOnlyRTCount

			// RecommendationTestsDayWise2FasterJan2016 recommendationsTest = new
			// RecommendationTestsDayWise2FasterJan2016(sampledUsers);

			System.out.println("-- iteration end for groupsOf100UsersLabel = " + groupsOf100UsersLabel);
		}
	}

	/**
	 * 
	 * @param usersCleanedDayTimelines
	 * @param groupsOf100UsersLabel
	 * @param userIndicesToSelect
	 * @param commonBasePath
	 * @throws IOException
	 * @since Mar 2 2018
	 */
	private void sampleUsersByIndicesExecuteRecommendationTests(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String groupOf100UsersLabel,
			List<Integer> userIndicesToSelect, String commonBasePath) throws IOException
	{
		// LinkedHashMap<Integer, String> indexOfBlackListedUsers = new LinkedHashMap<>();
		System.out.println(
				"Inside sampleUsersByIndicesExecuteRecommendationTests: usersCleanedDayTimelines received size="
						+ usersCleanedDayTimelines.size());

		System.out.println("-- iteration start for groupOf100UsersLabel = " + groupOf100UsersLabel);
		// important so as to wipe the previously assigned user ids
		Constant.initialise(commonPath, Constant.getDatabaseName());
		Constant.setOutputCoreResultsPath(commonBasePath + groupOf100UsersLabel + "/");
		Files.createDirectories(Paths.get(Constant.getOutputCoreResultsPath())); // added on 9th Feb 2017

		/// sample users
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsers = new LinkedHashMap<>(134);
		// (ceil) 100/0.75

		int indexOfSampleUser = 0;
		int numOfUsersSkippedGT553MaxActsPerDay = 0;
		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
		{
			System.out.print(" indexOfSampleUser = " + indexOfSampleUser + "\t");
			// countOfSampleUsers += 1;
			if (userIndicesToSelect.contains(indexOfSampleUser) == false)
			{
				System.out.println(" " + indexOfSampleUser + " hence skipping");
				indexOfSampleUser += 1;
				continue;
			}
			if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
			{
				System.out.println(" ALERT ALERT ALERT !! NOT EXPECTED" + indexOfSampleUser + " Skipping user: "
						+ userEntry.getKey() + " as in gowallaUserIDsWithGT553MaxActsPerDay");
				WToFile.appendLineToFileAbs(indexOfSampleUser + "," + userEntry.getKey() + "\n",
						"IndexOfBlacklistedUsers.csv");
				numOfUsersSkippedGT553MaxActsPerDay += 1;
				indexOfSampleUser += 1;
				continue;
			}
			else
			{
				System.out.println(" choosing this ");
				sampledUsers.put(userEntry.getKey(), userEntry.getValue());
				indexOfSampleUser += 1;
			}
			// $$System.out.println("putting in user= " + userEntry.getKey());
		}

		// TODO likely the code segment below is not needed anymore as blacklisted users have already been removed.
		// start of get timelines for all users for collaborative approach
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsers = new LinkedHashMap<>(1000);
		int numOfAllUsersSkippedGT553MaxActsPerDay = 0;
		if (Constant.collaborativeCandidates)
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
				{
					numOfAllUsersSkippedGT553MaxActsPerDay += 1;
					continue;
				}
				else
				{
					allUsers.put(userEntry.getKey(), userEntry.getValue());
				}
			}
			System.out.println("got timelines for all users for coll cand: allUsers.size()= " + allUsers.size());
			// Sanity.eq(numOfUsersSkippedGT553MaxActsPerDay, numOfAllUsersSkippedGT553MaxActsPerDay,
			System.out.println("numOfUsersSkippedGT553MaxActsPerDay=" + numOfUsersSkippedGT553MaxActsPerDay
					+ " numOfAllUsersSkippedGT553MaxActsPerDay=" + numOfAllUsersSkippedGT553MaxActsPerDay);
		}
		// end of get timelines for all users for collaborative approach

		System.out.println("num of sampled users for this iteration = " + sampledUsers.size());
		System.out.println("num of allUsers users for this iteration = " + allUsers.size());
		System.out.println(" -- Users = " + sampledUsers.keySet().toString());
		System.out.println(" -- All Users for collaboration = " + allUsers.keySet().toString());

		WToFile.writeToNewFile(String.join("\n", sampledUsers.keySet()), Constant.getCommonPath() + "sampledUsers.csv");
		WToFile.writeToNewFile(String.join("\n", allUsers.keySet()), Constant.getCommonPath() + "allUsers.csv");

		// $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
		// $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
		// $$RecommendationTestsBaseClosestTime recommendationsTest = new RecommendationTestsBaseClosestTime(
		// $$ sampledUsers);

		System.out.println("Just Before recommendationsTest\n" + PerformanceAnalytics.getHeapInformation());

		// // start of curtain may 4 2017
		// RecommendationTestsMar2017Gen recommendationsTest = new RecommendationTestsMar2017Gen(sampledUsers,
		// Constant.lookPastType, Constant.caseType, Constant.typeOfThresholds, Constant.getUserIDs(),
		// Constant.percentageInTraining);
		// // end of curtain may 4 2017
		// System.exit(0);
		RecommendationTestsMar2017GenSeqCleaned3Nov2017 recommendationsTest = new RecommendationTestsMar2017GenSeqCleaned3Nov2017(
				sampledUsers, Constant.lookPastType, Constant.caseType, Constant.typeOfiiWASThresholds,
				Constant.getUserIDs(), Constant.percentageInTraining, 3, allUsers);

		/// /// RecommendationTestsMar2017GenDummyOnlyRTCount

		// RecommendationTestsDayWise2FasterJan2016 recommendationsTest = new
		// RecommendationTestsDayWise2FasterJan2016(sampledUsers);

		System.out.println("-- iteration end for groupOf100UsersLabel = " + groupOf100UsersLabel);

	}

	/**
	 * Fork of sampleUsersByIndicesExecuteRecommendationTests to sample by userID.
	 * 
	 * @param usersCleanedDayTimelines
	 * @param groupsOf100UsersLabel
	 * @param userIndicesToSelect
	 * @param commonBasePath
	 * @throws IOException
	 * @since April 11 2018
	 */
	private void sampleUsersByIDsExecuteRecommendationTests(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String groupOf100UsersLabel,
			List<String> userIndicesToSelect, String commonBasePath) throws IOException
	{
		// LinkedHashMap<Integer, String> indexOfBlackListedUsers = new LinkedHashMap<>();
		System.out.println("Inside sampleUsersByIDsExecuteRecommendationTests: usersCleanedDayTimelines received size="
				+ usersCleanedDayTimelines.size());

		System.out.println("-- iteration start for groupOf100UsersLabel = " + groupOf100UsersLabel);
		// important so as to wipe the previously assigned user ids
		Constant.initialise(commonPath, Constant.getDatabaseName());
		Constant.setOutputCoreResultsPath(commonBasePath + groupOf100UsersLabel + "/");
		Files.createDirectories(Paths.get(Constant.getOutputCoreResultsPath())); // added on 9th Feb 2017

		/// sample users
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsers = new LinkedHashMap<>(134);
		// (ceil) 100/0.75

		int numOfUsersSkippedGT553MaxActsPerDay = 0;
		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
		{
			String userID = userEntry.getKey();
			System.out.print(" userID = " + userID + "\t");
			// countOfSampleUsers += 1;
			if (userIndicesToSelect.contains(userID) == false)
			{
				System.out.println(userID + "is not in selection hence skipping");
				continue;
			}
			if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
			{
				System.out.println(" ALERT ALERT ALERT !! NOT EXPECTED userID" + userID + " Skipping user: "
						+ userEntry.getKey() + " as in gowallaUserIDsWithGT553MaxActsPerDay");
				WToFile.appendLineToFileAbs(userEntry.getKey() + "\n",
						Constant.getCommonPath() + "BlacklistedUsersSkipped.csv");
				numOfUsersSkippedGT553MaxActsPerDay += 1;
				continue;
			}
			else
			{
				System.out.println(" choosing this ");
				sampledUsers.put(userEntry.getKey(), userEntry.getValue());
			}
			// $$System.out.println("putting in user= " + userEntry.getKey());
		}

		// TODO likely the code segment below is not needed anymore as blacklisted users have already been removed.
		// start of get timelines for all users for collaborative approach
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsers = new LinkedHashMap<>(1000);
		int numOfAllUsersSkippedGT553MaxActsPerDay = 0;
		if (Constant.collaborativeCandidates)
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
				{
					numOfAllUsersSkippedGT553MaxActsPerDay += 1;
					continue;
				}
				else
				{
					allUsers.put(userEntry.getKey(), userEntry.getValue());
				}
			}
			System.out.println("got timelines for all users for coll cand: allUsers.size()= " + allUsers.size());
			// Sanity.eq(numOfUsersSkippedGT553MaxActsPerDay, numOfAllUsersSkippedGT553MaxActsPerDay,
			System.out.println("numOfUsersSkippedGT553MaxActsPerDay=" + numOfUsersSkippedGT553MaxActsPerDay
					+ " numOfAllUsersSkippedGT553MaxActsPerDay=" + numOfAllUsersSkippedGT553MaxActsPerDay);

		}
		// end of get timelines for all users for collaborative approach

		System.out.println("num of sampled users for this iteration = " + sampledUsers.size());
		System.out.println("num of allUsers users for this iteration = " + allUsers.size());
		System.out.println(" -- Users = " + sampledUsers.keySet().toString());
		System.out.println(" -- All Users for collaboration = " + allUsers.keySet().toString());

		// $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
		// $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
		// $$RecommendationTestsBaseClosestTime recommendationsTest = new RecommendationTestsBaseClosestTime(
		// $$ sampledUsers);

		System.out.println("Just Before recommendationsTest\n" + PerformanceAnalytics.getHeapInformation());

		// // start of curtain may 4 2017
		// RecommendationTestsMar2017Gen recommendationsTest = new RecommendationTestsMar2017Gen(sampledUsers,
		// Constant.lookPastType, Constant.caseType, Constant.typeOfThresholds, Constant.getUserIDs(),
		// Constant.percentageInTraining);
		// // end of curtain may 4 2017
		// System.exit(0);
		RecommendationTestsMar2017GenSeqCleaned3Nov2017 recommendationsTest = new RecommendationTestsMar2017GenSeqCleaned3Nov2017(
				sampledUsers, Constant.lookPastType, Constant.caseType, Constant.typeOfiiWASThresholds,
				Constant.getUserIDs(), Constant.percentageInTraining, 3, allUsers);

		/// /// RecommendationTestsMar2017GenDummyOnlyRTCount

		// RecommendationTestsDayWise2FasterJan2016 recommendationsTest = new
		// RecommendationTestsDayWise2FasterJan2016(sampledUsers);

		System.out.println("-- iteration end for groupOf100UsersLabel = " + groupOf100UsersLabel);

	}

	/**
	 * 
	 * @param usersCleanedDayTimelines
	 * @param userIDsToSelect
	 * @param commonBasePath
	 * @param groupLabel
	 * @throws IOException
	 */
	private void selectGivenUsersExecuteRecommendationTests(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
			List<Integer> userIDsToSelect, String commonBasePath, String groupLabel) throws IOException
	{
		System.out.println("-- iteration start for groupLabel = " + groupLabel);
		System.out.println("userIDsToSelect.size()= " + userIDsToSelect.size());
		// important so as to wipe the previously assigned user ids
		Constant.initialise(commonPath, Constant.getDatabaseName());

		Constant.setOutputCoreResultsPath(commonBasePath + groupLabel + "/");
		Files.createDirectories(Paths.get(Constant.getOutputCoreResultsPath())); // added on 9th Feb 2017

		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsersTimelines = getDayTimelinesForUserIDs(
				usersCleanedDayTimelines, userIDsToSelect);

		System.out.println("num of sampled users for this iteration = " + sampledUsersTimelines.size());
		System.out.println(" -- Users = " + sampledUsersTimelines.keySet().toString());

		// $$RecommendationTestsMasterMU2 recommendationsTest = new RecommendationTestsMasterMU2(sampledUsers);
		// $$RecommendationTestsBaseClosestTime recommendationsTest = new
		// RecommendationTestsBaseClosestTime(sampledUsers);
		System.out.println("Just Before recommendationsTest\n" + PerformanceAnalytics.getHeapInformation());

		// // start of curtain may 4 2017
		// RecommendationTestsMar2017Gen recommendationsTest = new RecommendationTestsMar2017Gen(sampledUsers,
		// Constant.lookPastType, Constant.caseType, Constant.typeOfThresholds, Constant.getUserIDs(),
		// Constant.percentageInTraining);
		// // end of curtain may 4 2017

		RecommendationTestsMar2017GenSeqCleaned3Nov2017 recommendationsTest = new RecommendationTestsMar2017GenSeqCleaned3Nov2017(
				sampledUsersTimelines, Constant.lookPastType, Constant.caseType, Constant.typeOfiiWASThresholds,
				Constant.getUserIDs(), Constant.percentageInTraining, 3, usersCleanedDayTimelines);

		System.out.println("-- iteration end for groupLabel = " + groupLabel);
	}

	/**
	 * Extract day timelines for given user ids
	 * 
	 * @param usersCleanedDayTimelines
	 * @param userIDsToSelect
	 * @return
	 */
	public LinkedHashMap<String, LinkedHashMap<Date, Timeline>> getDayTimelinesForUserIDs(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
			List<Integer> userIDsToSelect)
	{
		/// sample users
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsersTimelines = new LinkedHashMap<>(
				userIDsToSelect.size());// (ceil) 100/0.75

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
		{
			if (userIDsToSelect.contains(Integer.valueOf(userEntry.getKey())))
			{
				sampledUsersTimelines.put(userEntry.getKey(), userEntry.getValue());
			}
			// $$System.out.println("putting in user= " + userEntry.getKey());
		}
		return sampledUsersTimelines;
	}

	/**
	 * 
	 * @param usersCleanedDayTimelines
	 * @param userIDsToSelect
	 * @return
	 */
	public LinkedHashMap<String, LinkedHashMap<Date, Timeline>> getDayTimelinesForUserIndices(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
			List<Integer> userIDsToSelect)
	{
		/// sample users
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsersTimelines = new LinkedHashMap<>(
				userIDsToSelect.size());// (ceil) 100/0.75

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
		{
			if (userIDsToSelect.contains(Integer.valueOf(userEntry.getKey())))
			{
				sampledUsersTimelines.put(userEntry.getKey(), userEntry.getValue());
			}
			// $$System.out.println("putting in user= " + userEntry.getKey());
		}
		return sampledUsersTimelines;
	}

	/**
	 * Extract day timelines for given user ids
	 * 
	 * @param usersCleanedDayTimelines
	 * @param userIDsToSelect
	 * @return day timelines for given user ids
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> getDayTimelinesForUserIDsV2(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
			ArrayList<String> userIDsToSelect)
	{
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUsersTimelines = new LinkedHashMap<>(
				userIDsToSelect.size());// (ceil) 100/0.75
		System.out.println("Inside getDayTimelinesForUserIDsV2: userIDsToSelect.size()= " + userIDsToSelect.size());
		System.out.println("userIDsToSelect= " + userIDsToSelect);
		System.out.println("usersCleanedDayTimelines.size()=" + usersCleanedDayTimelines.size());

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
		{
			if (userIDsToSelect.contains(userEntry.getKey()))
			{
				sampledUsersTimelines.put(userEntry.getKey(), userEntry.getValue());
				// System.out.println("putting in user= " + userEntry.getKey());
			}
			else
			{
				// System.out.println("not putting in user= " + userEntry.getKey());
			}

		}

		System.out.println("sampledUsersTimelines.size()=" + sampledUsersTimelines.size());
		return sampledUsersTimelines;
	}

	/**
	 * Sets the following paths:
	 * <p>
	 * <ul>
	 * <li>pathToLatestSerialisedJSONArray</li>
	 * <li>pathForLatestSerialisedJSONArray</li>
	 * <li>pathToLatestSerialisedTimelines</li>
	 * <li>commonPath</li>
	 * </ul>
	 * 
	 * @param databaseName
	 */
	public void setPathsSerialisedJSONTimelines(String databaseName)
	{
		LocalDateTime currentDateTime = LocalDateTime.now();

		switch (databaseName)
		{
			case "gowalla1":
				pathToLatestSerialisedJSONArray = null;// "";
				pathForLatestSerialisedJSONArray = "" + DateTimeUtils.getShortDateLabel(currentDateTime) + "obj";
				pathToLatestSerialisedTimelines = null;
				// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30/UserTimelinesNOV30.kryo";
				// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30/UserTimelines.kryo";
				// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/GowallaUserDayTimelines13Sep2016.kryo";//
				// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep15DatabaseGenerationJava/GowallaUserDayTimelines13Sep2016.kryo";
				pathForLatestSerialisedTimelines = null;
				// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30/UserTimelines"
				// + currentDateTime.getMonth().toString().substring(0, 3) + currentDateTime.getDayOfMonth()
				// + ".kryo";
				commonPath = Constant.getOutputCoreResultsPath();
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
						+ DateTimeUtils.getShortDateLabel(currentDateTime) + "obj";

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
						+ DateTimeUtils.getShortDateLabel(currentDateTime) + ".lmap";
				commonPath = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/";// version 3 based on rank score
																					// function
				break;

			case "dcu_data_2":
				pathToLatestSerialisedJSONArray = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/JSONArrayOct29.obj";
				pathForLatestSerialisedJSONArray = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/JSONArray"
						+ DateTimeUtils.getShortDateLabel(currentDateTime) + "obj";

				pathToLatestSerialisedTimelines = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/DCUUserTimelinesJUN19.lmap";
				// "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
				// Works/WorkingSet7July/DCUUserTimelinesJUN15.lmap";
				// "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data
				// Works/WorkingSet7July/DCUUserTimelinesMAY7.lmap"; DCUUserTimelinesOct29.lmap";
				pathForLatestSerialisedTimelines = "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/DCUUserTimelines"
						+ DateTimeUtils.getShortDateLabel(currentDateTime) + ".lmap";

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

		WToFile.writeToNewFile(jsonArray.toString(), Constant.getCommonPath() + "JSONArray.csv");
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

			LinkedHashMap<String, UserGowalla> mapForAllUserData = (LinkedHashMap<String, UserGowalla>) Serializer
					.kryoDeSerializeThis(gowallaDataFolder + "mapForAllUserData.kryo");
			// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllUserData.kryo");
			// LinkedHashMap<Integer, LocationGowalla> mapForAllLocationData = (LinkedHashMap<Integer,
			// LocationGowalla>)//Serializer.kryoDeSerializeThis(gowallaDataFolder + "mapForAllLocationData.kryo");
			Int2ObjectOpenHashMap<LocationGowalla> mapForAllLocationData = UtilityBelt
					.toFasterIntObjectOpenHashMap((LinkedHashMap<Integer, LocationGowalla>) Serializer
							.kryoDeSerializeThis(gowallaDataFolder + "mapForAllLocationData.kryo"));
			// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllLocationData.kryo");

			if (Constant.useCheckinEntryV2)
			{
				LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>> mapForAllCheckinData = (LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>>) Serializer
						.kryoDeSerializeThis(gowallaDataFolder + "mapForAllCheckinData.kryo");

				System.out.println("before creating timelines while having deserialised objects in memory\n"
						+ PerformanceAnalytics.getHeapInformation());

				usersDayTimelinesOriginal = TimelineUtils.createUserTimelinesFromCheckinEntriesGowallaFaster1_V2(
						mapForAllCheckinData, mapForAllLocationData);
			}
			else
			{
				// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/DatabaseCreated/";
				// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov29/DatabaseCreation/";
				LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllCheckinData = (LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>) Serializer
						.kryoDeSerializeThis(gowallaDataFolder + "mapForAllCheckinData.kryo");
				// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/mapForAllCheckinData.kryo");

				System.out.println("before creating timelines while having deserialised objects in memory\n"
						+ PerformanceAnalytics.getHeapInformation());

				usersDayTimelinesOriginal = TimelineUtils.createUserTimelinesFromCheckinEntriesGowallaFaster1(
						mapForAllCheckinData, mapForAllLocationData);
			}
		}
		else // When databaseName is not gowalla1
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

	/**
	 * For Gowalla data:
	 * <ol type="1">
	 * <li>removes days with less than 10 acts per day</li>
	 * <li>removes users with less than 7 days</li>
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
	 * 
	 * @param databaseName
	 * @param usersDayTimelinesOriginal
	 * @param writeToFile
	 * @param commonPath
	 * @param actsPerDayLowerLimit
	 * @param numOfSuchDaysLowerLimit
	 * @param actsPerDayUpperLimit
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceAndCleanTimelines2(String databaseName,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile,
			String commonPath, int actsPerDayLowerLimit, int numOfSuchDaysLowerLimit, int actsPerDayUpperLimit)
	{
		// int actsPerDayLowerLimit = 10;
		// int numOfSuchDaysLowerLimit = 7;
		// int actsPerDayUpperLimit = 500;
		// Originally received timelines
		if (writeToFile)
		{
			writeTimelineStats(usersDayTimelinesOriginal, false, true, true, true, "OriginalBeforeReduceClean",
					commonPath);
		}

		if (databaseName.equals("gowalla1"))
		{
			usersDayTimelinesOriginal = reduceTimelinesByActDensity(databaseName, usersDayTimelinesOriginal, true,
					false, actsPerDayLowerLimit, numOfSuchDaysLowerLimit, actsPerDayUpperLimit, commonPath);
		}

		///// clean timelines
		System.out.println(
				"\n-- Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day timelines.");
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = TimelineUtils
				.cleanUsersDayTimelines(usersDayTimelinesOriginal);
		if (writeToFile)
		{
			writeTimelineStats(usersCleanedDayTimelines, false, true, true, false, "Cleaned", commonPath);
		}
		///

		///// again remove users with less than 2 days (these are the clean days)
		// if (false){// disabled
		System.out.println("\n--again remove users with less than 2 day (these are the clean days)");
		usersCleanedDayTimelines = TimelineUtils.removeUsersWithLessDays(usersCleanedDayTimelines, 2,
				Constant.getCommonPath() + "removeCleanedDayTimelinesWithLessThan2DaysLog.csv");

		if (writeToFile)
		{
			writeTimelineStats(usersDayTimelinesOriginal, true, true, true, true, "cleaned reduced3", commonPath);
		}

		return usersCleanedDayTimelines;
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
	 * @param commonPathToWrite
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceAndCleanTimelines(String databaseName,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile,
			String commonPathToWrite)
	{
		// Originally received timelines
		if (writeToFile)
		{
			writeTimelineStats(usersDayTimelinesOriginal, false, true, true, true, "OriginalBeforeReduceClean",
					commonPathToWrite);
		}

		if (databaseName.equals("gowalla1"))
		{
			usersDayTimelinesOriginal = reduceGowallaTimelinesByActDensity(databaseName, usersDayTimelinesOriginal,
					true, 10, 50, commonPathToWrite);
		}

		///// clean timelines
		System.out.println(
				"\n-- Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day timelines.");
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = TimelineUtils
				.cleanUsersDayTimelines(usersDayTimelinesOriginal);
		if (writeToFile)
		{
			writeTimelineStats(usersCleanedDayTimelines, false, true, true, false,
					"RemovedLT10ActPerDayLT50DaysCleaned", commonPathToWrite);
		}

		if (databaseName.equals("gowalla1"))
		{
			///// again remove users with less than 50 days (these are the clean days)
			System.out.println("\n--again remove users with less than 50 days (these are the clean days)");
			usersCleanedDayTimelines = TimelineUtils.removeUsersWithLessDays(usersCleanedDayTimelines, 50,
					Constant.getCommonPath() + "removeCleanedDayTimelinesWithLessThan50DaysLog.csv");
			if (writeToFile)
			{
				writeTimelineStats(usersCleanedDayTimelines, false, true, true, false,
						"RemovedLT10ActPerDayLT50DaysCleanedLT50Days", commonPathToWrite);
			}

			/////////

			///// again remove blacklisted user
			System.out.println("\n--again remove blacklisted gowalla users");
			usersCleanedDayTimelines = TimelineUtils.removeBlackListedUsers(databaseName, usersCleanedDayTimelines,
					Constant.getCommonPath() + "BlackListedUsers.csv");
			if (writeToFile)
			{
				writeTimelineStats(usersCleanedDayTimelines, true, true, true, true,
						"RemovedLT10ActPerDayLT50DaysCleanedLT50DaysBlUsers", commonPathToWrite);
			}
		}
		return usersCleanedDayTimelines;
	}

	/**
	 * 
	 * @param timelines
	 * @param writeSubsetOfTimelines
	 * @param writeNumOfActsPerUsersDayTimelines
	 * @param writeNumOfDaysPerUsersDayTimelines
	 * @param writeNumOfDistinctValidActsPerUsersDayTimelines
	 * @param labelEnd
	 * @param commonPathToWrite
	 */
	public static void writeTimelineStats(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> timelines,
			boolean writeSubsetOfTimelines, boolean writeNumOfActsPerUsersDayTimelines,
			boolean writeNumOfDaysPerUsersDayTimelines, boolean writeNumOfDistinctValidActsPerUsersDayTimelines,
			String labelEnd, String commonPathToWrite)
	{
		// Writing user day timelines. big file ~ 17.3GB
		// WritingToFile.writeUsersDayTimelinesSameFile(timelines,"usersCleanedDayTimelinesReduced"+labelEnd,false,
		// false, false,"GowallaUserDayTimelinesCleanedReduced"+labelEnd+".csv");//

		if (writeSubsetOfTimelines)
		{
			Map<String, LinkedHashMap<Date, Timeline>> timelinesSampled = timelines.entrySet().stream().limit(2)
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

			WToFile.writeUsersDayTimelinesSameFile(
					new LinkedHashMap<String, LinkedHashMap<Date, Timeline>>(timelinesSampled),
					"usersDayTimelines" + labelEnd + "First2UsersOnly", false, false, false,
					"GowallaUserDayTimelines" + labelEnd + "First2UsersOnly.csv", commonPathToWrite);// users
		}
		if (writeNumOfActsPerUsersDayTimelines)
		{
			TimelineStats.writeNumOfActsPerUsersDayTimelinesSameFile(timelines, "usersDayTimelines" + labelEnd,
					"GowallaPerUserDayNumOfActs" + labelEnd + ".csv", commonPathToWrite);
		}
		if (writeNumOfDaysPerUsersDayTimelines)
		{
			TimelineStats.writeNumOfDaysPerUsersDayTimelinesSameFile(timelines,
					commonPathToWrite + "NumOfDaysPerUser" + labelEnd + ".csv");

		}
		if (writeNumOfDistinctValidActsPerUsersDayTimelines)
		{
			TimelineStats.writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile(timelines,
					"usersDayTimelines" + labelEnd, "GowallaPerUserDayNumOfDistinctValidActs" + labelEnd + ".csv",
					commonPathToWrite);
		}
		System.out.println(" Num of users" + labelEnd + "= " + timelines.size());
	}

	//
	/**
	 * USED
	 * <p>
	 * Removes days with less than actsPerDayLowerLimit acts per day
	 * <p>
	 * Removes days with greater than actsPerDayUpperLimit acts per day
	 * <p>
	 * Removes users with less than numOfSuchDaysLowerLimit days
	 * 
	 * @param databaseName
	 * @param usersDayTimelinesOriginal
	 * @param writeToFile
	 * @param writeLogs
	 * @param actsPerDayLowerLimit
	 * @param numOfSuchDaysLowerLimit
	 * @param actsPerDayUpperLimit
	 * @param commonPath
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceTimelinesByActDensity(String databaseName,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile,
			boolean writeLogs, int actsPerDayLowerLimit, int numOfSuchDaysLowerLimit, int actsPerDayUpperLimit,
			String commonPath) // formerly reduceTimelines3
	{// making it generic and not restricting to gowalla dataset on 2 Jan 2018
		System.out.println("Inside reduceTimelinesByActivityDensity");
		String labelEnd = "";

		////////// remove days with less than actsPerDayLowerLimit acts per day

		if (actsPerDayLowerLimit > -1)
		{
			labelEnd = "RemovedDaysWithLT" + actsPerDayLowerLimit + "ActPerDay";
			System.out.println("\n-- removing days with less than " + actsPerDayLowerLimit + " acts per day");
			usersDayTimelinesOriginal = TimelineUtils.removeDayTimelinesWithLessAct(usersDayTimelinesOriginal,
					actsPerDayLowerLimit,
					commonPath + "removeDayTimelinesWithLessThan" + actsPerDayLowerLimit + "ActLog.csv", writeLogs);
			if (writeToFile)
			{
				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false, labelEnd, commonPath);
			}
		}
		//////////

		////////// removed days with greater than actsPerDayUpperLimit acts per day
		if (actsPerDayUpperLimit > -1)
		{
			labelEnd += "GT" + actsPerDayUpperLimit + "ActsPerDay";
			System.out.println("\n-- removing days with greater than " + actsPerDayUpperLimit + " acts per day");
			usersDayTimelinesOriginal = TimelineUtils.removeDayTimelinesWithGreaterAct(usersDayTimelinesOriginal,
					actsPerDayUpperLimit,
					commonPath + "removeDayTimelinesWithGreaterThan" + actsPerDayUpperLimit + "ActLog.csv", writeLogs);

			if (writeToFile)
			{
				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false, labelEnd, commonPath);
			}
		}
		//////////

		////////// removed users with less than numOfSuchDaysLowerLimit days
		if (numOfSuchDaysLowerLimit > -1)
		{
			labelEnd += "UsersWithLT" + numOfSuchDaysLowerLimit + "Days";
			System.out.println("\n-- removing users with less than " + numOfSuchDaysLowerLimit + " days");
			usersDayTimelinesOriginal = TimelineUtils.removeUsersWithLessDays(usersDayTimelinesOriginal,
					numOfSuchDaysLowerLimit,
					commonPath + "removeDayTimelinesWithLessThan" + numOfSuchDaysLowerLimit + "DaysLog.csv");
			if (writeToFile)
			{
				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false, labelEnd, commonPath);

			}
		}
		//////////

		return usersDayTimelinesOriginal;

	}

	//
	/**
	 * removed days with less than actsPerDayThreshold(10) acts per day
	 * <p>
	 * removed users with less than suchDaysThreshold(50) days
	 * 
	 * @param databaseName
	 * @param usersDayTimelinesOriginal
	 * @param writeToFile
	 * @param actsPerDayThreshold
	 * @param suchDaysThreshold
	 * @param commonPath
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceGowallaTimelinesByActDensity(
			String databaseName, LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal,
			boolean writeToFile, int actsPerDayThreshold, int suchDaysThreshold, String commonPath)
	{// formerly reduceGowallaTimelines
		System.out.println("Inside reduceGowallaTimelinesByActDensity");

		if (databaseName.equals("gowalla1") == false)
		{
			String msg = PopUps.getTracedErrorMsg(
					"Error in reduceTimelines(): should not be called for databases other than gowalla1. Called for database: "
							+ databaseName);
			System.err.println(msg);
			return null;
		}

		else
		{
			////////// removed days with less than 10 acts per day
			System.out.println("\n-- removing days with less than " + actsPerDayThreshold + " acts per day");
			usersDayTimelinesOriginal = TimelineUtils.removeDayTimelinesWithLessAct(usersDayTimelinesOriginal,
					actsPerDayThreshold,
					Constant.getCommonPath() + "removeDayTimelinesWithLessThan" + actsPerDayThreshold + "ActLog.csv",
					true);
			if (writeToFile)
			{
				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false,
						"RemovedLT" + actsPerDayThreshold + "ActPerDay", commonPath);
			}

			//////////

			////////// removed users with less than 50 days
			System.out.println("\n-- removing users with less than " + suchDaysThreshold + " days");
			usersDayTimelinesOriginal = TimelineUtils.removeUsersWithLessDays(usersDayTimelinesOriginal,
					suchDaysThreshold,
					Constant.getCommonPath() + "removeDayTimelinesWithLessThan" + suchDaysThreshold + "DaysLog.csv");
			if (writeToFile)
			{
				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false,
						"RemovedLT" + actsPerDayThreshold + "ActPerDayLT" + suchDaysThreshold + "Days", commonPath);
			}

			return usersDayTimelinesOriginal;
		}
	}

}