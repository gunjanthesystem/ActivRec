package org.activity.io;

import java.sql.Date;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.evaluation.RecommendationTestsMar2017GenSeqCleaned3Nov2017;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Dimension;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.objects.UserGowalla;
import org.activity.util.RegexUtils;
import org.activity.util.TimelineTransformers;
import org.activity.util.TimelineUtils;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

/**
 * 
 * @author gunjan
 *
 */
public class TimelineWriters
{

	/**
	 * 
	 * @param res
	 */
	public static void writeTrainTestTimlinesAOsPerUser(LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> res)
	{
		StringBuilder sb = new StringBuilder("User,#DaysInTrain,#AOsInTrain,#DaysInTest,#AOsInTest\n");

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> e : res.entrySet())
		{
			LinkedHashMap<Date, Timeline> trainDayTimelines = e.getValue().get(0);
			LinkedHashMap<Date, Timeline> testDayTimelines = e.getValue().get(1);
			long numOfAOsInTrainDayTimelines = trainDayTimelines.entrySet().stream().mapToLong(t -> t.getValue().size())
					.sum();
			long numOfAOsInTestDayTimelines = testDayTimelines.entrySet().stream().mapToLong(t -> t.getValue().size())
					.sum();
			sb.append(e.getKey() + "," + trainDayTimelines.size() + "," + numOfAOsInTrainDayTimelines + ","
					+ testDayTimelines.size() + "," + numOfAOsInTestDayTimelines + "\n");
		}
		WToFile.writeToNewFile(sb.toString(), Constant.getCommonPath() + "TrainTestSplitAOsDaysPerUser.csv");
	}

	/**
	 * Write all act objs with their features.
	 * <p>
	 * This method was created to investigate issue with feature level edit distance>100 because of issue with checkin
	 * counts.
	 * 
	 * @param usersCleanedDayTimelines
	 * @param abFileNameToWrite
	 * @since 7 Feb 2018
	 */
	public static void writeAllActObjs(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
			String abFileNameToWrite)
	{
		String delimiter = ",";
		WToFile.appendLineToFileAbs(
				"User" + delimiter + ActivityObject2018.getHeaderForStringAllGowallaTSWithNameForHeaded(delimiter) + "\n",
				abFileNameToWrite);

		for (Entry<String, LinkedHashMap<Date, Timeline>> userData : usersCleanedDayTimelines.entrySet())
		{
			String user = userData.getKey();

			for (Entry<Date, Timeline> timeline : userData.getValue().entrySet())
			{
				StringBuilder sbForThisTimeline = new StringBuilder();
				for (ActivityObject2018 ao : timeline.getValue().getActivityObjectsInTimeline())
				{
					sbForThisTimeline
							.append(user + delimiter + ao.toStringAllGowallaTSWithNameForHeaded(delimiter) + "\n");
				}
				WToFile.appendLineToFileAbs(sbForThisTimeline.toString(), abFileNameToWrite);
			}
		}
	}

	/**
	 * Write all act objs with their features.
	 * <p>
	 * This method was created to investigate issue with feature level edit distance>100 because of issue with checkin
	 * counts.
	 * 
	 * @param usersCleanedDayTimelines
	 * @param abFileNameToWrite
	 * @since 22 Feb 2018
	 */
	public static void writeAllActObjs(Map<String, Timeline> usersTimelines, String abFileNameToWrite)
	{
		String delimiter = ",";
		WToFile.appendLineToFileAbs(
				"User" + delimiter + ActivityObject2018.getHeaderForStringAllGowallaTSWithNameForHeaded(delimiter) + "\n",
				abFileNameToWrite);

		for (Entry<String, Timeline> userData : usersTimelines.entrySet())
		{
			String user = userData.getKey();

			StringBuilder sbForThisTimeline = new StringBuilder();
			for (ActivityObject2018 ao : userData.getValue().getActivityObjectsInTimeline())
			{
				sbForThisTimeline.append(user + delimiter + ao.toStringAllGowallaTSWithNameForHeaded(delimiter) + "\n");
			}
			WToFile.appendLineToFileAbs(sbForThisTimeline.toString(), abFileNameToWrite);
		}
	}

	/**
	 * Write all act objs with their features from test timelines (second in list)
	 * <p>
	 * 
	 * 
	 * @param usersCleanedDayTimelines
	 * @param abFileNameToWrite
	 * @since 22 Feb 2018
	 */
	public static void writeAllActObjsFromTestOnly(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW,
			String abFileNameToWrite)
	{
		String delimiter = ",";
		WToFile.appendLineToFileAbs(
				"User" + delimiter + ActivityObject2018.getHeaderForStringAllGowallaTSWithNameForHeaded(delimiter) + "\n",
				abFileNameToWrite);

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> userData : trainTestTimelinesForAllUsersDW.entrySet())
		{
			String user = userData.getKey();

			for (Entry<Date, Timeline> timeline : userData.getValue().get(1).entrySet())
			{
				StringBuilder sbForThisTimeline = new StringBuilder();
				for (ActivityObject2018 ao : timeline.getValue().getActivityObjectsInTimeline())
				{
					sbForThisTimeline
							.append(user + delimiter + ao.toStringAllGowallaTSWithNameForHeaded(delimiter) + "\n");
				}
				WToFile.appendLineToFileAbs(sbForThisTimeline.toString(), abFileNameToWrite);
			}
		}
	}

	/**
	 * 
	 * @param uniqueLocIDs
	 * @param map
	 * @param absFileNameToUse
	 */
	public static void writeLocationObjects(Set<Integer> uniqueLocIDs, LinkedHashMap<Integer, LocationGowalla> map,
			String absFileNameToUse)
	{
		StringBuilder sb = new StringBuilder(LocationGowalla.toStringHeader() + "\n");
		for (Integer id : uniqueLocIDs)
		{
			sb.append(map.get(id).toString() + "\n");
		}

		WToFile.writeToNewFile(sb.toString(), absFileNameToUse);
	}

	/**
	 * 
	 * @param uniqueUserIDs
	 * @param map
	 * @param absFileNameToUse
	 */
	public static void writeUserObjects(Set<String> uniqueUserIDs, LinkedHashMap<String, UserGowalla> map,
			String absFileNameToUse)
	{
		StringBuilder sb = new StringBuilder(UserGowalla.toStringHeader() + "\n");
		for (String id : uniqueUserIDs)
		{
			sb.append(map.get(id).toString() + "\n");
		}
		WToFile.writeToNewFile(sb.toString(), absFileNameToUse);
	}

	/**
	 * For getting not timezone occurrence in cleaned susbsetted timelines
	 * <p>
	 * Not assigning fallback zoneid but just logging the count
	 * 
	 * @since Mar 1 2018
	 * 
	 * 
	 * 
	 * @param usersCleanedDayTimelines
	 * @param fileNamePhrase
	 */
	public static void writeNumOfNullTZCinsPerUserPerLocIDContinuousTimelines(
			LinkedHashMap<String, Timeline> usersCleanedDayTimelines, String fileNamePhrase)
	{
		System.out.println("Inside writeNumOfNullTZCinsPerUserPerLocID():");
		// {userid, locids, num of null zone ids for this user id locid}
		Map<String, Map<List<Integer>, Long>> userIdLocIdNumOfNullZoneAOsMap = new LinkedHashMap<>();
		IntSortedSet locIDsWithNoTZ = new IntRBTreeSet();
		IntSortedSet locIDsWithNoFallbackTZ = new IntRBTreeSet();
		HashMap<String, String> AOsWithMultipleWorkLevelCatIDs = new HashMap<>();

		long numOfAOsWithNullZoneIDs = 0, numOfAOsWithEvenFallbackNullZoneIds = 0, numOfAOs = 0,
				numOfAOsWithMultipleLocIDs = 0, numOfAOsWithMultipleDistinctLocIDs = 0,
				numOfAOsWithMultipleWorkingLevelCatIDs = 0;

		try
		{
			for (Entry<String, Timeline> userEntry : usersCleanedDayTimelines.entrySet())
			{
				String userID = userEntry.getKey();
				ZoneId fallbackZoneId = null;

				for (ActivityObject2018 ao : userEntry.getValue().getActivityObjectsInTimeline())
				{
					numOfAOs += 1;

					ArrayList<Integer> locIDs = ao.getLocationIDs();

					if (locIDs.size() > 1)
					{
						numOfAOsWithMultipleLocIDs += 1;
					}
					if (new HashSet<Integer>(locIDs).size() > 1) // if more than 1 distinct locations
					{
						numOfAOsWithMultipleDistinctLocIDs += 1;
					}
					if (RegexUtils.patternDoubleUnderScore.split(ao.getWorkingLevelCatIDs()).length > 1)
					{
						numOfAOsWithMultipleWorkingLevelCatIDs += 1;
						AOsWithMultipleWorkLevelCatIDs.put(ao.getActivityName(), ao.getWorkingLevelCatIDs());
					}

					ZoneId currentZoneId = ao.getTimeZoneId();

					if (currentZoneId == null)
					{
						numOfAOsWithNullZoneIDs += 1;
						locIDsWithNoTZ.addAll(locIDs);

						Map<List<Integer>, Long> mapForUserNTZ = userIdLocIdNumOfNullZoneAOsMap.get(userID);
						if (mapForUserNTZ != null)
						{
							Long numOfNZCinsForThisUserLocID = mapForUserNTZ.get(locIDs);
							if (numOfNZCinsForThisUserLocID != null)
							{
								mapForUserNTZ.put(locIDs, numOfNZCinsForThisUserLocID + 1);
							}
							else
							{
								mapForUserNTZ.put(locIDs, (long) 1);
							}
						}
						else
						{
							mapForUserNTZ = new LinkedHashMap<List<Integer>, Long>();
							mapForUserNTZ.put(locIDs, (long) 1);
							userIdLocIdNumOfNullZoneAOsMap.put(userID, mapForUserNTZ);
						}

						currentZoneId = fallbackZoneId;
						// WritingToFile.appendLineToFileAbsolute(userID + "," + locIDs + "," + startTimestamp +
						// "\n",
						// Constant.getOutputCoreResultsPath() + "NullZoneIDs.csv");
						if (fallbackZoneId == null)
						{
							locIDsWithNoFallbackTZ.addAll(locIDs);
							numOfAOsWithEvenFallbackNullZoneIds += 1;
							// WritingToFile.appendLineToFileAbsolute(userID + "," + locIDs + "," + startTimestamp +
							// ","
							// + numOfCinsForThisUser + "\n",Constant.getOutputCoreResultsPath() +
							// "FallbackNullZoneIDs.csv");
						}
					}
					fallbackZoneId = currentZoneId;
				}

			}

			System.out.println("locIDsWithNoTZ.size()= " + locIDsWithNoTZ.size());
			System.out.println("locIDsWithNoFallbackTZ.size()= " + locIDsWithNoFallbackTZ.size());
			WToFile.writeToNewFile(locIDsWithNoTZ.stream().map(String::valueOf).collect(Collectors.joining("\n")),
					Constant.getOutputCoreResultsPath() + "UniqueLocIDsWithNoTZIn" + fileNamePhrase + ".csv");
			WToFile.writeToNewFile(
					locIDsWithNoFallbackTZ.stream().map(String::valueOf).collect(Collectors.joining("\n")),
					Constant.getOutputCoreResultsPath() + "locIDsWithNoFallbackTZ" + fileNamePhrase + ".csv");

			System.out.println(" numOfAOsWithNullZoneIDs= " + numOfAOsWithNullZoneIDs + "  "
					+ ((numOfAOsWithNullZoneIDs * 100.0) / numOfAOs) + "% of total AOs");
			System.out.println(" numOfAOsWithEvenFallbackNullZoneIds= " + numOfAOsWithEvenFallbackNullZoneIds + "  "
					+ ((numOfAOsWithEvenFallbackNullZoneIds * 1.0) / numOfAOs) + "% of total AOs");
			System.out.println(" numOfAOs = " + numOfAOs);
			System.out.println(" numOfAOsWithMultipleLocIDs = " + numOfAOsWithMultipleLocIDs);
			System.out.println(" numOfAOsWithMultipleDistinctLocIDs = " + numOfAOsWithMultipleDistinctLocIDs);
			System.out.println(" numOfCInsWithMultipleWorkingLevelCatIDs = " + numOfAOsWithMultipleWorkingLevelCatIDs
					+ " for working level = " + DomainConstants.gowallaWorkingCatLevel);

			WToFile.writeToNewFile(
					AOsWithMultipleWorkLevelCatIDs.entrySet().stream().map(e -> e.getKey() + "-" + e.getValue())
							.collect(Collectors.joining("\n")),
					Constant.getOutputCoreResultsPath() + "AOsWithMultipleWorkLevelCatIDs" + fileNamePhrase + ".csv");

			WToFile.writeMapOfMap(userIdLocIdNumOfNullZoneAOsMap, "UserID;LocID;NumOfNullTZCins\n", ";",
					Constant.getOutputCoreResultsPath() + "userIdLocIdNumOfNullZoneAOsMap" + fileNamePhrase + ".csv");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * For getting not timezone occurrence in cleaned susbsetted timelines
	 * <p>
	 * Not assigning fallback zoneid but just logging the count
	 * 
	 * @since Mar 1 2018
	 * 
	 * 
	 * 
	 * @param usersCleanedDayTimelines
	 * @param fileNamePhrase
	 */
	public static void writeNumOfNullTZCinsPerUserPerLocIDTrainTestDataOnly(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String fileNamePhrase)
	{
		if (Constant.collaborativeCandidates)
		{
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW = TimelineUtils
					.splitAllUsersTestTrainingTimelines(usersCleanedDayTimelines, Constant.percentageInTraining, false);

			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous;
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> testTimelinesAllUsers = new LinkedHashMap<>();
			if (Constant.filterTrainingTimelinesByRecentDays)
			{
				trainTimelinesAllUsersContinuous = RecommendationTestsMar2017GenSeqCleaned3Nov2017
						.getContinousTrainingTimelinesWithFilterByRecentDaysV2(trainTestTimelinesForAllUsersDW,
								Constant.getRecentDaysInTrainingTimelines());
			}
			else
			{
				// sampledUsersTimelines
				trainTimelinesAllUsersContinuous = RecommendationTestsMar2017GenSeqCleaned3Nov2017
						.getContinousTrainingTimelines(trainTestTimelinesForAllUsersDW);
			}

			for (Entry<String, List<LinkedHashMap<Date, Timeline>>> userEntry : trainTestTimelinesForAllUsersDW
					.entrySet())
			{
				testTimelinesAllUsers.put(userEntry.getKey(), userEntry.getValue().get(1));
			}

			writeNumOfNullTZCinsPerUserPerLocIDContinuousTimelines(trainTimelinesAllUsersContinuous,
					fileNamePhrase + "TrainRecentDaysOnly");
			writeNumOfNullTZCinsPerUserPerLocID(testTimelinesAllUsers, fileNamePhrase + "TestOnly");
		}
	}

	/**
	 * 
	 * @param usersCleanedDayTimelines
	 * @param fileNamePhrase
	 * @since March 1 2018
	 */
	public static void writeNumOfNullTZCinsPerUserPerLocID(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String fileNamePhrase)
	{

		System.out.println("Inside writeNumOfNullTZCinsPerUserPerLocID():");
		// {userid, locids, num of null zone ids for this user id locid}
		Map<String, Map<List<Integer>, Long>> userIdLocIdNumOfNullZoneAOsMap = new LinkedHashMap<>();
		IntSortedSet locIDsWithNoTZ = new IntRBTreeSet();
		IntSortedSet locIDsWithNoFallbackTZ = new IntRBTreeSet();
		HashMap<String, String> AOsWithMultipleWorkLevelCatIDs = new HashMap<>();

		long numOfAOsWithNullZoneIDs = 0, numOfAOsWithEvenFallbackNullZoneIds = 0, numOfAOs = 0,
				numOfAOsWithMultipleLocIDs = 0, numOfAOsWithMultipleDistinctLocIDs = 0,
				numOfAOsWithMultipleWorkingLevelCatIDs = 0;

		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				String userID = userEntry.getKey();
				ZoneId fallbackZoneId = null;
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
				{
					for (ActivityObject2018 ao : dateEntry.getValue().getActivityObjectsInTimeline())
					{
						numOfAOs += 1;

						ArrayList<Integer> locIDs = ao.getLocationIDs();

						if (locIDs.size() > 1)
						{
							numOfAOsWithMultipleLocIDs += 1;
						}
						if (new HashSet<Integer>(locIDs).size() > 1) // if more than 1 distinct locations
						{
							numOfAOsWithMultipleDistinctLocIDs += 1;
						}
						if (RegexUtils.patternDoubleUnderScore.split(ao.getWorkingLevelCatIDs()).length > 1)
						{
							numOfAOsWithMultipleWorkingLevelCatIDs += 1;
							AOsWithMultipleWorkLevelCatIDs.put(ao.getActivityName(), ao.getWorkingLevelCatIDs());
						}

						ZoneId currentZoneId = ao.getTimeZoneId();

						if (currentZoneId == null)
						{
							numOfAOsWithNullZoneIDs += 1;
							locIDsWithNoTZ.addAll(locIDs);

							Map<List<Integer>, Long> mapForUserNTZ = userIdLocIdNumOfNullZoneAOsMap.get(userID);
							if (mapForUserNTZ != null)
							{
								Long numOfNZCinsForThisUserLocID = mapForUserNTZ.get(locIDs);
								if (numOfNZCinsForThisUserLocID != null)
								{
									mapForUserNTZ.put(locIDs, numOfNZCinsForThisUserLocID + 1);
								}
								else
								{
									mapForUserNTZ.put(locIDs, (long) 1);
								}
							}
							else
							{
								mapForUserNTZ = new LinkedHashMap<List<Integer>, Long>();
								mapForUserNTZ.put(locIDs, (long) 1);
								userIdLocIdNumOfNullZoneAOsMap.put(userID, mapForUserNTZ);
							}

							currentZoneId = fallbackZoneId;
							// WritingToFile.appendLineToFileAbsolute(userID + "," + locIDs + "," + startTimestamp +
							// "\n",
							// Constant.getOutputCoreResultsPath() + "NullZoneIDs.csv");
							if (fallbackZoneId == null)
							{
								locIDsWithNoFallbackTZ.addAll(locIDs);
								numOfAOsWithEvenFallbackNullZoneIds += 1;
								// WritingToFile.appendLineToFileAbsolute(userID + "," + locIDs + "," + startTimestamp +
								// ","
								// + numOfCinsForThisUser + "\n",Constant.getOutputCoreResultsPath() +
								// "FallbackNullZoneIDs.csv");
							}
						}
						fallbackZoneId = currentZoneId;
					}

				}
			}

			System.out.println("locIDsWithNoTZ.size()= " + locIDsWithNoTZ.size());
			System.out.println("locIDsWithNoFallbackTZ.size()= " + locIDsWithNoFallbackTZ.size());
			WToFile.writeToNewFile(locIDsWithNoTZ.stream().map(String::valueOf).collect(Collectors.joining("\n")),
					Constant.getOutputCoreResultsPath() + "UniqueLocIDsWithNoTZIn" + fileNamePhrase + ".csv");
			WToFile.writeToNewFile(
					locIDsWithNoFallbackTZ.stream().map(String::valueOf).collect(Collectors.joining("\n")),
					Constant.getOutputCoreResultsPath() + "locIDsWithNoFallbackTZ" + fileNamePhrase + ".csv");

			System.out.println(" numOfAOsWithNullZoneIDs= " + numOfAOsWithNullZoneIDs + "  "
					+ ((numOfAOsWithNullZoneIDs * 100.0) / numOfAOs) + "% of total AOs");
			System.out.println(" numOfAOsWithEvenFallbackNullZoneIds= " + numOfAOsWithEvenFallbackNullZoneIds + "  "
					+ ((numOfAOsWithEvenFallbackNullZoneIds * 1.0) / numOfAOs) + "% of total AOs");
			System.out.println(" numOfAOs = " + numOfAOs);
			System.out.println(" numOfAOsWithMultipleLocIDs = " + numOfAOsWithMultipleLocIDs);
			System.out.println(" numOfAOsWithMultipleDistinctLocIDs = " + numOfAOsWithMultipleDistinctLocIDs);
			System.out.println(" numOfCInsWithMultipleWorkingLevelCatIDs = " + numOfAOsWithMultipleWorkingLevelCatIDs
					+ " for working level = " + DomainConstants.gowallaWorkingCatLevel);

			WToFile.writeToNewFile(
					AOsWithMultipleWorkLevelCatIDs.entrySet().stream().map(e -> e.getKey() + "-" + e.getValue())
							.collect(Collectors.joining("\n")),
					Constant.getOutputCoreResultsPath() + "AOsWithMultipleWorkLevelCatIDs" + fileNamePhrase + ".csv");

			WToFile.writeMapOfMap(userIdLocIdNumOfNullZoneAOsMap, "UserID;LocID;NumOfNullTZCins\n", ";",
					Constant.getOutputCoreResultsPath() + "userIdLocIdNumOfNullZoneAOsMap" + fileNamePhrase + ".csv");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 * @param userAllDatesTimeslines
	 */
	public static void writeUserActNamesSeqAsCode(int userID, LinkedHashMap<Date, Timeline> userTrainingTimelines,
			LinkedHashMap<Date, Timeline> userTestTimelines, LinkedHashMap<Date, Timeline> userAllDatesTimeslines)
	{
		StringBuilder sbTraining = new StringBuilder(String.valueOf(userID) + ",");
		userTrainingTimelines.entrySet().stream()
				.forEachOrdered(tl -> sbTraining.append(tl.getValue().getActivityObjectsAsStringCode(",")));
		sbTraining.append("\n");
		WToFile.appendLineToFileAbs(sbTraining.toString(),
				Constant.getCommonPath() + "TrainingTimelinesAsSeqOfCodes.csv");
	}

	/**
	 * 
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 * @param userAllDatesTimeslines
	 */
	public static void writeUserActNamesSeqAsNames(int userID, LinkedHashMap<Date, Timeline> userTrainingTimelines,
			LinkedHashMap<Date, Timeline> userTestTimelines, LinkedHashMap<Date, Timeline> userAllDatesTimeslines)
	{
		StringBuilder sbTraining = new StringBuilder(String.valueOf(userID) + ",");
	
		userTrainingTimelines.entrySet().stream()
				.forEachOrdered(tl -> sbTraining.append(TimelineTransformers.timelineToSeqOfActNames(tl.getValue(), ",")));
	
		sbTraining.append("\n");
		WToFile.appendLineToFileAbs(sbTraining.toString(),
				Constant.getCommonPath() + "TrainingTimelinesAsSeqOfCodes.csv");
	}

	/**
	 * @deprecated
	 * @param userTimelines
	 */
	public static void traverseUserTimelines(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userTimelines)
	{
		// Iterate over Users
		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> userEntry : userTimelines.entrySet())
		{
			System.out.println("User ID =" + userEntry.getKey());
			System.out.println("Number of day timelines for this user=" + userEntry.getValue().size());
	
			for (Map.Entry<Date, Timeline> dayTimelineEntry : userEntry.getValue().entrySet())
			{
				System.out.println("Date: " + dayTimelineEntry.getKey());
				// dayTimelineEntry.getValue().printActivityEventNamesInSequence();
				// 21Oct dayTimelineEntry.getValue().printActivityEventNamesWithTimestampsInSequence();
				// System.out.println();
			}
		}
	
	}

	public static void traverseTimelines(HashMap<String, ArrayList<ActivityObject2018>> timeLines)
	{
		System.out.println("----------------Traversing Timelines----------");
	
		Iterator it = timeLines.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pairs = (Map.Entry) it.next();
	
			String id = pairs.getKey().toString();
			ArrayList<ActivityObject2018> activityEvents = (ArrayList<ActivityObject2018>) pairs.getValue();
	
			System.out.println("Timeline for id = " + id);
	
			System.out.println("Number of Activity Events =" + activityEvents.size());
	
			for (ActivityObject2018 ae : activityEvents)
			{
				System.out.print("Activity Name= " + ae.getActivityName());
				System.out.print("Start Time= " + ae.getStartTimestamp());
				System.out.print("End Time= " + ae.getEndTimestamp());
	
			}
			System.out.println();
			// it.remove(); // avoids a ConcurrentModificationException
		}
	
	}

	public static void traverseSplittedTimelines(HashMap<String, ArrayList<String>> timeLines)
	{
		System.out.println("----------------Traversing Splitted Timelines----------");
	
		Iterator it = timeLines.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pairs = (Map.Entry) it.next();
	
			String id = pairs.getKey().toString();
			ArrayList<String> activityNames = (ArrayList<String>) pairs.getValue();
	
			System.out.println("Splitted Timeline id = " + id);
	
			System.out.println("zz Number of Activity Names =" + activityNames.size());
	
			System.out.println("b");
			for (int i = 0; i < activityNames.size(); i++)
			{
				System.out.print("b");
				System.out.print("Activity name index" + i);
				System.out.print("Activity Name= " + activityNames.get(i).toString() + " ");
			}
			System.out.println("-uuuuuuuuuuuuuuuuuuuuuuuuu---");
			System.out.println("c");
			// it.remove(); // avoids a ConcurrentModificationException
		}
	
	}

	public static void traverseSingleSplittedTimeline(String timelineId, ArrayList<String> splittedTimeline)
	{
		// $$System.out.println("--Traversing splitted timeline:"+timelineId);
		for (String name : splittedTimeline)
		{
			System.out.print(name + "<");
		}
		System.out.println();
	}

	/**
	 * 
	 * @param map
	 */
	public static void traverseMapOfDayTimelines(LinkedHashMap<Date, Timeline> map)
	{
		System.out.println("traversing map of day timelines");
		for (Map.Entry<Date, Timeline> entry : map.entrySet())
		{
			System.out.print("Date: " + entry.getKey());
			entry.getValue().printActivityObjectNamesInSequence();
			System.out.println();
		}
		System.out.println("-----------");
	}

	public static void traverseMapOfTimelines(LinkedHashMap<String, Timeline> map)
	{
		System.out.println("traversing map of day timelines");
	
		for (Map.Entry<String, Timeline> entry : map.entrySet())
		{
			System.out.print("ID: " + entry.getKey());
			entry.getValue().printActivityObjectNamesInSequence();
			System.out.println();
		}
		System.out.println("-----------");
	}

	public static void traverseMapOfTimelinesWithNext(LinkedHashMap<Integer, TimelineWithNext> map)
	{
		System.out.println("traversing map of timelines");
	
		for (Map.Entry<Integer, TimelineWithNext> entry : map.entrySet())
		{
			System.out.print("ID: " + entry.getKey());
			entry.getValue().printActivityObjectNamesInSequence();
			System.out.println("\t ** Next Activity Object (name=): "
					+ entry.getValue().getNextActivityObject().getActivityName());
		}
		System.out.println("-----------");
	}

	/**
	 * 
	 */
	public static void traverseDimensionIDNameValues(HashMap<String, String> dimensionIDNameValues)
	{
		for (Map.Entry<String, String> entry : dimensionIDNameValues.entrySet())
		{
			System.out.print(" " + entry.getKey() + " getDimensionAttributeValue: " + entry.getValue() + " ");
		}
		System.out.println("");
	}

	/**
	 * 
	 */
	public static void traverseActivityObject(HashMap<String, String> dimensionIDNameValues,
			ArrayList<Dimension> dimensions)
	{
		System.out.println("\n---Traversing Activity Event:--");
		System.out.print("----Dimensions ID are: ");
		traverseDimensionIDNameValues(dimensionIDNameValues);
		System.out.println("----Dimension attributes are: ");
		dimensions.stream().forEach(d -> d.traverseDimensionAttributeNameValuepairs());
	}

}
