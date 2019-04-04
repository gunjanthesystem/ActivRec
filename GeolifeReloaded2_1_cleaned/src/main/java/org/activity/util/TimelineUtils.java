package org.activity.util;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.activity.constants.Enums;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.distances.DistanceUtils;
import org.activity.distances.FeatureWiseEditDistance;
import org.activity.distances.FeatureWiseWeightedEditDistance;
import org.activity.distances.HJEditDistance;
import org.activity.distances.OTMDSAMEditDistance;
import org.activity.io.EditDistanceMemorizer;
import org.activity.io.Serializer;
import org.activity.io.TimelineWriters;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.sanityChecks.Sanity;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;

//import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
/**
 */
public class TimelineUtils
{

	/**
	 * Num of activities with distinct activity names
	 * 
	 * @return
	 */
	public static int countNumberOfDistinctActivities(ArrayList<ActivityObject2018> ActivityObjects)
	{
		Set<String> set = ActivityObjects.stream().map(ao -> ao.getActivityName().trim()).collect(Collectors.toSet());
		return set.size();
	}
	// public TimelineUtilities()
	// {
	//
	// }

	public static void main(String args[])
	{
		TimeZone dft = TimeZone.getTimeZone("UTC");
		TimeZone.setDefault(dft);

		// $$ checkConvertTimewiseMapToDatewiseMap2(dft);

		ArrayList<Integer> l = new ArrayList<Integer>();
		System.out.println("max num of elemens in arraylist = " + Integer.MAX_VALUE);
	}

	// /**
	// * Creates user day timelines from the given list of Activity Objects.
	// *
	// * Activity events ---> day timelines (later, not here)---> user timelines
	// * <p>
	// * <font color = red>make sure that the timezone is set appropriately</font>
	// * </p>
	// *
	// * @param allActivityEvents
	// * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	// */
	// public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createUserTimelinesFromCheckinEntriesGowalla(
	// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> checkinEntries,
	// LinkedHashMap<Integer, LocationGowalla> locationObjects)
	// {
	// long ct1 = System.currentTimeMillis();
	//
	// System.out.println("starting createUserTimelinesFromCheckinEntriesGowalla");
	// LinkedHashMap<String, TreeMap<Date, ArrayList<CheckinEntry>>> checkinEntriesDatewise =
	// convertTimewiseMapToDatewiseMap2(
	// checkinEntries, Constant.getTimeZone());
	//
	// // Start of added on Aug 10 2017
	// // // Filter out days which have more than 500 checkins
	// // StringBuilder sb = new StringBuilder();
	// // checkinEntriesDatewise.entrySet().stream().forEachOrdered(e->sb.append(str));
	// // End of added on Aug 10 2017
	//
	// LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> activityObjectsDatewise =
	// convertCheckinEntriesToActivityObjectsGowalla(
	// checkinEntriesDatewise, locationObjects);
	//
	// int optimalSizeWrtUsers = (int) (Math.ceil(activityObjectsDatewise.size() / 0.75));
	// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDaytimelines = new LinkedHashMap<>(
	// optimalSizeWrtUsers);
	//
	// // StringBuilder sbNumOfActsPerDayPerUser;
	// // WritingToFile.appendLineToFileAbsolute("User,Date,NumOfActsInDay\n", "NumOfActsPerUserPerDay.csv");
	//
	// for (Entry<String, TreeMap<Date, ArrayList<ActivityObject>>> userEntry : activityObjectsDatewise.entrySet())
	// {
	// String userID = userEntry.getKey();
	// LinkedHashMap<Date, Timeline> dayTimelines = new LinkedHashMap<>(
	// (int) (Math.ceil(userEntry.getValue().size() / 0.75)));
	//
	// // sbNumOfActsPerDayPerUser = new StringBuilder();// "User,Day,NumOfActs\n");
	// for (Entry<Date, ArrayList<ActivityObject>> dateEntry : userEntry.getValue().entrySet())
	// {
	// Date date = dateEntry.getKey();
	// ArrayList<ActivityObject> activityObjectsInDay = dateEntry.getValue();
	// // String dateID = date.toString();
	// // String dayName = DateTimeUtils.getWeekDayFromWeekDayInt(date.getDay());//
	// // DateTimeUtils.getWeekDayFromWeekDayInt(date.getDayOfWeek().getValue());
	// dayTimelines.put(date, new Timeline(activityObjectsInDay, true, true));
	// // sbNumOfActsPerDayPerUser.append(userID).append(',').append(date.toString()).append(',')
	// // .append(activityObjectsInDay.size()).append("\n");
	// }
	// // WritingToFile.appendLineToFileAbsolute(sbNumOfActsPerDayPerUser.toString(),
	// // "NumOfActsPerUserPerDay.csv");
	// userDaytimelines.put(userID, dayTimelines);
	// }
	//
	// long ct4 = System.currentTimeMillis();
	// System.out.println(
	// "created timelines for" + userDaytimelines.size() + " users in " + ((ct4 - ct1) / 1000) + " seconds");
	//
	// System.out.println("exiting createUserTimelinesFromCheckinEntriesGowalla");
	// // System.exit(0);
	// return userDaytimelines;
	// }

	// mm
	/**
	 * INCOMPLETE
	 * 
	 * @deprecated INCOMPLETE
	 * @param usersDayTimelines
	 */
	public static void countConsecutiveSimilarActivities(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines)
	{
		// <User__Date, time difference between then in secs>
		LinkedHashMap<String, Double> mapForConsecutive2s = new LinkedHashMap<>();

		// <User__Date, time difference between then in secs>
		LinkedHashMap<String, Double> mapForConsecutive3s = new LinkedHashMap<>();

		// <User__Date, time difference between then in secs>
		LinkedHashMap<String, Double> mapForConsecutive4s = new LinkedHashMap<>();

		// <User__Date, time difference between then in secs>
		LinkedHashMap<String, Double> mapForConsecutive5s = new LinkedHashMap<>();

		// <User__Date, time difference between then in secs>
		LinkedHashMap<String, Double> mapForConsecutive6OrMores = new LinkedHashMap<>();

		for (Entry<String, LinkedHashMap<Date, Timeline>> userE : usersDayTimelines.entrySet())
		{
			String user = userE.getKey();

			for (Entry<Date, Timeline> dateE : userE.getValue().entrySet())
			{
				String date = dateE.getKey().toString();

				String prevActivityName = "";
				Timestamp prevActivityStartTimestamp = null;
				int numOfConsecutives = 0;
				long timeDiff = 0;
				for (ActivityObject2018 aos : dateE.getValue().getActivityObjectsInTimeline())
				{
					String activityName = aos.getActivityName();
					if (activityName.equals(prevActivityName))
					{
						numOfConsecutives += 1;
						timeDiff += aos.getStartTimestamp().getTime() - prevActivityStartTimestamp.getTime();
					}
				}
			}
		}
	}

	/**
	 * Deserialises the cat id name dictionary, created a map with catids as key and empty array list as values.
	 * 
	 * @param absPathToCatIDDictionary
	 * @return Pair(catIDLengthConsecutives, catIDNameDictionary)
	 */
	public static Pair<LinkedHashMap<String, ArrayList<Integer>>, TreeMap<Integer, String>> getEmptyMapOfCatIDs(
			String absPathToCatIDDictionary)
	{
		System.out.println("Entering org.activity.util.TimelineUtils.getEmptyMapOfCatIDs(String)");
		LinkedHashMap<String, ArrayList<Integer>> catIDLengthConsecutives = new LinkedHashMap<>();

		TreeMap<Integer, String> catIDNameDictionary = (TreeMap<Integer, String>) Serializer
				.kryoDeSerializeThis(absPathToCatIDDictionary);
		System.out.println("Num of catids in dictionary = " + catIDNameDictionary.size() + "\n");

		for (Integer catID : catIDNameDictionary.keySet())
		{
			catIDLengthConsecutives.put(String.valueOf(catID), new ArrayList<Integer>());
			// System.out.print(catID + ",");
		}

		System.out.println("\nExiting org.activity.util.TimelineUtils.getEmptyMapOfCatIDs(String)");
		return new Pair(catIDLengthConsecutives, catIDNameDictionary);
	}

	/**
	 * Count the length of consecutive occurrence of same activity names
	 * <p>
	 * TODO: (note: April 9 2018) may need to check if distNext whcih has now become actually distFromPrev because of
	 * change in Class ActivityObject is still okay for this method or does it needs to be change
	 * 
	 * @param usersDayTimelines
	 * @param commonPathToWrite
	 * @param absPathToCatIDDictionary
	 * @return
	 */
	public static LinkedHashMap<String, ArrayList<Integer>> countConsecutiveSimilarActivities2(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String commonPathToWrite,
			String absPathToCatIDDictionary)
	{
		// LinkedHashMap<String, ArrayList<Long>> catIDTimeDifferencesOfConsecutives = new LinkedHashMap<>();
		Pair<LinkedHashMap<String, ArrayList<Integer>>, TreeMap<Integer, String>> r1 = getEmptyMapOfCatIDs(
				absPathToCatIDDictionary);
		// <catid, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> catIDLengthConsecutives = r1.getFirst();
		// <catid,catname>
		TreeMap<Integer, String> catIDNameDictionary = r1.getSecond();

		// <userIDt, [1,1,2,4,1,1,1,6]>
		LinkedHashMap<String, ArrayList<Integer>> userLengthConsecutives = new LinkedHashMap<>();

		StringBuilder sbAllDistanceInMDurationInSec = new StringBuilder();
		// changed to write dist and duration diff in same lin so in R analysis i can filter by both at the same time.
		// StringBuilder sbAllDurationFromNext = new StringBuilder();
		WToFile.appendLineToFileAbs("User,Timestamp,CatID,CatName,DistDiff,DurationDiff\n",
				commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv"); // writing header

		StringBuilder sbEnumerateAllCats = new StringBuilder();// write all catid sequentially userwise

		long aoCount = 0;
		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userE : usersDayTimelines.entrySet())
			{
				String user = userE.getKey();

				ArrayList<Integer> userLengthConsecutivesValues = new ArrayList<Integer>();

				String prevActivityName = "";// Timestamp prevActivityStartTimestamp = null;

				int numOfConsecutives = 1;// long timeDiff = 0;

				StringBuilder distanceDurationFromNextSeq = new StringBuilder(); // only write >1 consecs
				// StringBuilder durationFromNextSeq = new StringBuilder();// only write >1 consecs

				for (Entry<Date, Timeline> dateE : userE.getValue().entrySet())
				{
					String date = dateE.getKey().toString();
					for (ActivityObject2018 aos : dateE.getValue().getActivityObjectsInTimeline())
					{
						aoCount += 1;

						String activityName = aos.getActivityName();
						double distNext = aos.getDistanceInMFromPrev();
						long durationNext = aos.getDurationInSecondsFromPrev();
						String ts = aos.getStartTimestamp().toString();
						String actCatName = catIDNameDictionary.get(Integer.valueOf(activityName));

						// System.out.println("aoCount=" + aoCount + " activityName=" + activityName);

						sbEnumerateAllCats.append(user + "," + ts + "," + activityName + "," + actCatName + "\n");
						// $$System.out.println("\nReading: " + user + "," + ts + "," + activityName + "," +
						// actCatName);

						if (activityName.equals(prevActivityName))
						{
							// $$ System.out.println(" act name:" + activityName + " = prevActName = " +
							// prevActivityName
							// $$ + " \n Hence append");
							numOfConsecutives += 1;
							distanceDurationFromNextSeq.append(user + "," + ts + "," + activityName + "," + actCatName
									+ "," + String.valueOf(distNext) + "," + String.valueOf(durationNext) + "\n");
							// durationFromNextSeq.append(user + "," + ts + "," + activityName + "," + actCatName + ","
							// + String.valueOf(durationNext) + "\n");
							// timeDiff += aos.getStartTimestamp().getTime() - prevActivityStartTimestamp.getTime();
							// System.out.println(" Current Prev act Same, numOfConsecutives =" + numOfConsecutives);
							continue;
						}

						else // not equals then
						{
							// $$System.out.println(" act name:" + activityName + " != prevActName = " +
							// prevActivityName);
							ArrayList<Integer> consecVals = catIDLengthConsecutives.get(prevActivityName);
							if (consecVals == null)
							{
								if (prevActivityName.length() > 0)
								{
									System.out.println(
											"Error in org.activity.util.TimelineUtils.countConsecutiveSimilarActivities2(): consecVals = null, i,e., array list for activityName="
													+ prevActivityName
													+ " hasn't been initialised in catIDLengthConsecutives");
								}
								else
								{
									// encountered the first activity for that user.
									// System.out.println(" first activity for this user.");
								}
							}
							else
							{
								// $$System.out.println(" currently numOfConsecutives= " + numOfConsecutives);
								consecVals.add(numOfConsecutives);
								catIDLengthConsecutives.put(prevActivityName, consecVals);
								userLengthConsecutivesValues.add(numOfConsecutives);

								if (numOfConsecutives > 1)
								{
									sbAllDistanceInMDurationInSec.append(distanceDurationFromNextSeq.toString());
									// sbAllDurationFromNext.append(durationFromNextSeq.toString());// + "\n");
									// $$System.out.println("appending to dista, duration");
								}
								// else
								// {
								distanceDurationFromNextSeq.setLength(0);
								// durationFromNextSeq.setLength(0);
								// }

								// System.out.println(" Current Prev act diff, numOfConsecutives =" +
								// numOfConsecutives);
								// System.out.println(" (prev) activity name=" + prevActivityName + " consecVals="
								// + catIDLengthConsecutives.get(prevActivityName).toString());
								numOfConsecutives = 1;// resetting
							}

						}
						prevActivityName = activityName;

						if (aoCount % 20000 == 0)
						{
							WToFile.appendLineToFileAbs(sbEnumerateAllCats.toString(),
									commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv");
							sbEnumerateAllCats.setLength(0);

							/////////////////
							WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
									commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
							sbAllDistanceInMDurationInSec.setLength(0);

							// WritingToFile.appendLineToFileAbsolute(sbAllDurationFromNext.toString(),
							// commonPathToWrite + "sbAllDurationFromNext.csv");
							// sbAllDurationFromNext.setLength(0);
							/////////////////

						}
					} // end of loop over aos over this day for this user
						// break;
				} // end of loop over days
					// break;
				userLengthConsecutives.put(user, userLengthConsecutivesValues);
			} // end of loop over users

			// write remaining in buffer
			if (sbEnumerateAllCats.length() != 0)
			{
				WToFile.appendLineToFileAbs(sbEnumerateAllCats.toString(),
						commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv");
				sbEnumerateAllCats.setLength(0);

				/////////////////
				WToFile.appendLineToFileAbs(sbAllDistanceInMDurationInSec.toString(),
						commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
				sbAllDistanceInMDurationInSec.setLength(0);

				// WritingToFile.appendLineToFileAbsolute(sbAllDurationFromNext.toString(),
				// commonPathToWrite + "sbAllDurationFromNext.csv");
				// sbAllDurationFromNext.setLength(0);
				/////////////////

			}

			System.out.println("Num of aos read = " + aoCount);
			WToFile.writeConsectiveCountsEqualLength(catIDLengthConsecutives, catIDNameDictionary,
					commonPathToWrite + "CatwiseConsecCountsEqualLength.csv", true, true);
			WToFile.writeConsectiveCountsEqualLength(userLengthConsecutives, catIDNameDictionary,
					commonPathToWrite + "UserwiseConsecCountsEqualLength.csv", false, false);

			// WritingToFile.appendLineToFileAbsolute(sbEnumerateAllCats.toString(),
			// commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv"); // probably not needed

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return catIDLengthConsecutives;

	}

	// LinkedHashMap<Date, UserDayTimeline> dayTimelines = new LinkedHashMap<Date, UserDayTimeline>();
	//
	// for (Map.Entry<Date, ArrayList<ActivityObject>> perDateActivityEventsForThisUserEntry :
	// perDateActivityEventsForThisUser.entrySet())
	// {
	// Date date = perDateActivityEventsForThisUserEntry.getKey();
	//
	// dayTimelines.put(date, new UserDayTimeline(perDateActivityEventsForThisUserEntry.getValue(), date));
	//
	// }
	//
	// userTimelines.put(userID, dayTimelines);
	//
	// }
	//
	// System.out.println("exiting createUserTimelinesFromActivityEvents");return userTimelines;

	/**
	 * 
	 * @param locIDs
	 * @param locIDGridIndexMap
	 * @param userForLog
	 * @param tsForLog
	 * @return
	 */
	public final static int getGridIndex(ArrayList<Integer> locIDs, Map<Long, Integer> locIDGridIndexMap,
			String userForLog, Timestamp tsForLog)
	{
		StringBuilder sbLog = new StringBuilder();
		Set<Integer> uniqueLocIDs = new TreeSet<>();
		Set<Integer> uniqueGridIndices = new TreeSet<>();
		Map<Integer, Integer> gridIndicesCount = new LinkedHashMap<>();
		List<Integer> gridIndices = new ArrayList<>();

		for (int locID : locIDs)
		{
			Integer gridIndex = locIDGridIndexMap.get(new Long(locID));
			sbLog.append("locID," + locID + ",gridIndex," + gridIndex + "\n");

			uniqueLocIDs.add(locID);
			uniqueGridIndices.add(gridIndex);
			gridIndices.add(gridIndex);

			Integer oldCount = gridIndicesCount.get(gridIndex);
			if (oldCount == null)
			{
				gridIndicesCount.put(gridIndex, 1);
			}
			else
			{
				gridIndicesCount.put(gridIndex, oldCount + 1);
			}
		}

		List<Integer> gridIndicesWithMaxCount = (List<Integer>) ComparatorUtils.getKeysWithMaxValues(gridIndicesCount);

		if (VerbosityConstants.writeGridIndicesPerCheckin)
		{
			WToFile.appendLineToFileAbs(sbLog.toString(),
					Constant.getCommonPath() + "LocIDGridIndexWhileCreatingTimelines.csv");
		}

		WToFile.appendLineToFileAbs(
				gridIndices.size() + "," + uniqueGridIndices.size() + ","
						+ gridIndices.stream().map(l -> String.valueOf(l)).collect(Collectors.joining("|")) + "\n",
				Constant.getCommonPath() + "LocIDGridIndexWhileCreatingTimelinesSize.csv");

		// #LocationIDs,#UniqueLocationIDs,#GridIDs,#UniqueGridIDs,#GridIDsWithMaxCount,LocIDs,GridIDs,GridIDsWithMaxCount

		if (uniqueGridIndices.size() > 1)
		{
			String fileNameToWrite = Constant.getCommonPath() + "LocIDGridIndexWhileCreatingTimelinesStats.csv";
			File f = new File(fileNameToWrite);
			if (!f.exists())
			{
				WToFile.appendLineToFileAbs(
						"#LocationIDs,#UniqueLocationIDs,#GridIDs,#UniqueGridIDs,#GridIDsWithMaxCount,LocIDs,GridIDs,GridIDsWithMaxCount\n",
						fileNameToWrite);
			}

			WToFile.appendLineToFileAbs(locIDs.size() + "," + uniqueLocIDs.size() + "," + gridIndices.size() + ","
					+ uniqueGridIndices.size() + "," + gridIndicesWithMaxCount.size() + ","
					+ StringUtils.collectionToString(locIDs, "|") + ","
					+ StringUtils.collectionToString(gridIndices, "|") + ","
					+ StringUtils.collectionToString(gridIndicesWithMaxCount, "|") + "\n", fileNameToWrite);
		}

		// List<Long> gridIDsList = new ArrayList<>();
		// gridIDsList.addAll(uniqueGridIDs);
		int gridIndexToReturn = gridIndicesWithMaxCount.get(0);

		if (VerbosityConstants.writeGridIndicesPerCheckin)
		{
			WToFile.appendLineToFileAbs("locIDs\t"
					+ locIDs.stream().map(v -> v.toString()).collect(Collectors.joining("|")) + "\tgridIndexToReturn\t"
					+ gridIndexToReturn + "\tu\t" + userForLog + "\tts\t" + tsForLog + "\n",
					Constant.getCommonPath() + "GridIndexPerCheckin.csv");
		}

		return gridIndexToReturn;
	}

	/**
	 * Created to assign locGridIndex to already created (TOY) timelines
	 * 
	 * @param usersToyDayTimelines
	 * @return
	 * @since 6 Aug 2018
	 */
	public static final LinkedHashMap<String, LinkedHashMap<Date, Timeline>> assignLocGridIndices(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersToyDayTimelines)
	{

		for (Entry<String, LinkedHashMap<Date, Timeline>> e : usersToyDayTimelines.entrySet())
		{
			for (Entry<Date, Timeline> dateEntry : e.getValue().entrySet())
			{
				for (ActivityObject2018 ao : dateEntry.getValue().getActivityObjectsInTimeline())
				{
					Map<Long, Integer> locIDGridIndexMap = DomainConstants.getLocIDGridIndexGowallaMap();
					int gridIndex = getGridIndex(ao.getLocationIDs(), locIDGridIndexMap, e.getKey(),
							ao.getStartTimestamp());
					ao.setGridIndex(gridIndex);
				}
			}
		}
		return usersToyDayTimelines;
	}

	/**
	 * training test split for all users simulatneously.
	 * <p>
	 * Earlier we are doing train test split for each user’s timeline iteratively, for collaborative approach we need
	 * training timelines for all users simulatneously, hence need to do training test split simulataneously.
	 * 
	 * @param allUsersAllDatesTimeslines
	 * @param percentageInTraining
	 * @param cleanHereUserDayTimelines
	 * @return
	 * @since 25 July 2017
	 */
	public static LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> splitAllUsersTestTrainingTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsersAllDatesTimeslines,
			double percentageInTraining, boolean cleanHereUserDayTimelines)
	{
		LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> res = new LinkedHashMap<>();

		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> oneUserAllDatesTimelinesEntry : allUsersAllDatesTimeslines
					.entrySet())
			{
				String userID = oneUserAllDatesTimelinesEntry.getKey();
				System.out.println("splitAllUsersTestTrainingTimelines: userID = " + userID);
				LinkedHashMap<Date, Timeline> oneUserAllDatesTimelines = oneUserAllDatesTimelinesEntry.getValue();

				// //////////////////REMOVING SELECTED TIMELINES FROM DATASET////////////////////
				if (cleanHereUserDayTimelines)
				{
					oneUserAllDatesTimelines = TimelineTrimmers.cleanUserDayTimelines(oneUserAllDatesTimelines,
							Constant.getCommonPath() + "InsideSplitAllUsersTestTrainingTimelines", userID);
				}
				// ////////////////////////////////////////////////////////////////////////////////

				// Splitting the set of timelines into training set and test set.
				List<LinkedHashMap<Date, Timeline>> trainTestTimelines = TimelineUtils
						.splitTestTrainingTimelines(oneUserAllDatesTimelines, percentageInTraining);
				// LinkedHashMap<Date, Timeline> userTrainingTimelines = trainTestTimelines.get(0);
				// LinkedHashMap<Date, Timeline> userTestTimelines = trainTestTimelines.get(1);
				res.put(userID, trainTestTimelines);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (VerbosityConstants.writeTrainTestTimelinesAOsPerUser)
		{
			TimelineWriters.writeTrainTestTimlinesAOsPerUser(res);
		}
		return res;
	}

	/**
	 * 
	 * @param userAllDatesTimeslines
	 * @param percentageInTraining
	 * @return List, first element is training timelines (daywise) and second element is test timelines(daywise)
	 */
	public static List<LinkedHashMap<Date, Timeline>> splitTestTrainingTimelines(
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines, double percentageInTraining)
	{
		ArrayList<LinkedHashMap<Date, Timeline>> trainTestTimelines = new ArrayList<>();

		int numberOfValidDays = 0;

		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().containsAtLeastOneValidActivity() == false)
			{ // if the day timelines contains no valid activity, then don't consider it for training or test
				System.err.println(PopUps.getTracedErrorMsg(
						"Error in splitTestTrainingTimelines: 45: userAllDatesTimeslines contains a day timeline with no valid activity, but we already tried to remove it"));
				continue;
			}
			numberOfValidDays++;
		}
		// int numberOfDays = userAllDatesTimeslines.size();
		int numberOfDaysForTraining = (int) Math.round(numberOfValidDays * percentageInTraining);// floor

		int numberOfDaysForTest = numberOfValidDays - numberOfDaysForTraining;

		if (numberOfDaysForTest < 1)
		{
			numberOfDaysForTest = 1;
			numberOfDaysForTraining = numberOfValidDays - numberOfDaysForTest;
		}

		LinkedHashMap<Date, Timeline> userTrainingTimelines = new LinkedHashMap<>();
		LinkedHashMap<Date, Timeline> userTestTimelines = new LinkedHashMap<>();

		int count = 1;
		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().containsAtLeastOneValidActivity() == false) // not essential anymore
			{ // if the day timelines contains no valid activity, then don't consider it for training or test
				continue;
			}
			if (count <= numberOfDaysForTraining)
			{
				userTrainingTimelines.put(entry.getKey(), entry.getValue());
				count++;
			}
			else
			{
				userTestTimelines.put(entry.getKey(), entry.getValue());
				count++;
			}
		}

		trainTestTimelines.add(userTrainingTimelines);
		trainTestTimelines.add(userTestTimelines);

		System.out.println("Number of Training days = " + trainTestTimelines.get(0).size());
		System.out.println("Number of Test days = " + trainTestTimelines.get(1).size());

		if (trainTestTimelines.size() > 2)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in splitTestTrainingTimelines: there are more than two (train+test) timelines in returned result, there are "
							+ trainTestTimelines.size() + " timelines."));
			System.exit(-43);
		}

		return trainTestTimelines;
	}

	/**
	 * To check if the inner linkedhashmap has multiple dates. This can happen is this java.sql.date was not directly
	 * created from sql fetch query and instead was created from a timestamp object. If the java.sql.Date is created
	 * from timestamp object then the time component still remains in the Date object thus can cause multiple Date
	 * objects with same dates (but different times).
	 * 
	 * @param usersDayTimelinesOriginal
	 * @return
	 */
	public static Pair<Boolean, String> hasDuplicateDates(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal)
	{
		boolean hasDuplicateDates = false;
		String usersWithDuplicateDates = new String();

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersDayTimelinesOriginal.entrySet())
		{
			String user = userEntry.getKey();

			ArrayList<String> dateAsStringList = new ArrayList<>();
			Set<String> dateAsStringSet = new HashSet();

			for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
			{
				dateAsStringList.add(dateEntry.getKey().toString());
				dateAsStringSet.add(dateEntry.getKey().toString());
			}

			if (dateAsStringList.size() != dateAsStringSet.size())
			{
				usersWithDuplicateDates += "__" + user;
				hasDuplicateDates = true;
			}
		}

		return new Pair<Boolean, String>(hasDuplicateDates, usersWithDuplicateDates);
	}

	/**
	 * TODO this method can be improved for performance
	 * 
	 * @param dayTimelinesForUser
	 * @param dateA
	 * @return
	 */
	public static Timeline getUserDayTimelineByDateFromMap(LinkedHashMap<Date, Timeline> dayTimelinesForUser,
			Date dateA)
	{
		for (Map.Entry<Date, Timeline> entry : dayTimelinesForUser.entrySet())
		{
			// System.out.println("Date ="+entry.getKey());
			// if(entry.getKey().toString().equals((new Date(2014-1900,4-1,10)).toString()))
			// System.out.println("!!!!!!!!E U R E K A !!!!!!!");
			if (entry.getKey().toString().equals(dateA.toString()))
			{
				// System.out.println("!!!!!!!FOUND THE O N E!!!!!!");
				return entry.getValue();
			}
		}
		return null;
	}

	////
	/**
	 * Checks whether the given list Activity Objects are in chronological sequence.
	 * 
	 * @param listToCheck
	 * @return
	 */
	public static boolean isChronological(ArrayList<ActivityObject2018> listToCheck)
	{
		boolean chronologyPreserved = true;

		for (int i = 0; i < listToCheck.size() - 1; i++)
		{
			// one activity object's starttimestamp is after the next activity object's starttimestamp, implying the
			// breaking of chronoligcal order
			if (listToCheck.get(i).getStartTimestamp().after(listToCheck.get(i + 1).getStartTimestamp()))
			{
				chronologyPreserved = false;
			}
		}
		return chronologyPreserved;
	}

	/**
	 * 
	 * @param activityObjectIndex
	 * @param givenTimeline
	 * @return
	 */
	public static boolean isNoValidActivityAfterIt(int activityObjectIndex, Timeline givenTimeline)
	{
		boolean isNoValidAfter = true;
		ArrayList<ActivityObject2018> objectsInGivenTimeline = givenTimeline.getActivityObjectsInTimeline();

		System.out.println("inside isNoValidActivityAfterIt");
		System.out.println("activityIndexAfterWhichToCheck=" + activityObjectIndex);

		givenTimeline.printActivityObjectNamesInSequence();
		System.out.println("Number of activities in timeline=" + objectsInGivenTimeline.size());

		if (activityObjectIndex == objectsInGivenTimeline.size() - 1)
		{
			return true;
		}

		for (int i = activityObjectIndex + 1; i < objectsInGivenTimeline.size(); i++)
		{
			if (UtilityBelt.isValidActivityName(objectsInGivenTimeline.get(i).getActivityName()))
			{
				System.out.println("Activity making it false=" + objectsInGivenTimeline.get(i).getActivityName());
				isNoValidAfter = false;
				break;
			}
		}

		System.out.println("No valid after is:" + isNoValidAfter);
		return isNoValidAfter;
	}

	/**
	 * 
	 * @param givenActivityObjectIndex
	 * @param givenTimelineToCheckIn
	 * @return
	 */
	public static boolean isNoValidActivityAfterItInTheDay(int givenActivityObjectIndex,
			Timeline givenTimelineToCheckIn)
	{
		boolean isNoValidAfter = true;
		ArrayList<ActivityObject2018> aosInGivenTimelineToCheckIn = givenTimelineToCheckIn
				.getActivityObjectsInTimeline();

		// Date dateOfAOAtGivenIndex = DateTimeUtils.getDate(
		// givenTimelineToCheckIn.getActivityObjectAtPosition(givenActivityObjectIndex).getEndTimestamp());

		LocalDate dateOfAOAtGivenIndex = givenTimelineToCheckIn.getActivityObjectAtPosition(givenActivityObjectIndex)
				.getEndTimestamp().toLocalDateTime().toLocalDate();

		if (givenActivityObjectIndex == aosInGivenTimelineToCheckIn.size() - 1)
		{
			return true;
		}

		int i = -1;
		for (i = givenActivityObjectIndex + 1; i < aosInGivenTimelineToCheckIn.size(); i++)
		{
			// System.out.println("for index " + i);
			LocalDate dateOfThisAO = aosInGivenTimelineToCheckIn.get(i).getEndTimestamp().toLocalDateTime()
					.toLocalDate();

			// System.out.println("dateOfAOAtGivenIndex = " + dateOfAOAtGivenIndex + " dateOfThisAO = " + dateOfThisAO);
			// System.out
			// .println("dateOfThisAO.equals(dateOfAOAtGivenIndex = " + dateOfThisAO.equals(dateOfAOAtGivenIndex));

			if (dateOfThisAO.equals(dateOfAOAtGivenIndex)) // only look at aos in same day
			{
				// System.out.println("found same date");
				if (UtilityBelt.isValidActivityName(aosInGivenTimelineToCheckIn.get(i).getActivityName()))
				{
					// System.out.println("found valid act");
					isNoValidAfter = false;
					break;
				}
			}
			else
			{
				break;
			}
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("-------\n\tinside isNoValidActivityAfterItInTheDay\n\tactivityIndexAfterWhichToCheck="
					+ givenActivityObjectIndex);
			givenTimelineToCheckIn.printActivityObjectNamesInSequence();
			System.out.println("\tNumber of activities in timeline=" + aosInGivenTimelineToCheckIn.size());
			if (!isNoValidAfter)
				System.out.println("\tAO making it false=" + aosInGivenTimelineToCheckIn.get(i).getActivityName());
			System.out.println("\tNo valid after is:" + isNoValidAfter + "\n-------");
		}

		return isNoValidAfter;
	}

	/**
	 * 
	 * @param timestamp
	 * @param givenTimelineToCheckIn
	 * @return
	 * @since 12 Aug 2018
	 */
	public static List<ActivityObject2018> getValidAOsAfterThisTimeInTheDay(Timestamp timestamp,
			Timeline givenTimelineToCheckIn)
	{
		int indexOfAOAtThisTime = givenTimelineToCheckIn.getIndexOfActivityObjectAtTime(timestamp);
		if (VerbosityConstants.verbose)
		{
			System.out.println(
					"\n\rInside getValidAOsAfterThisTimeInTheDay(): indexOfAOAtThisTime = " + indexOfAOAtThisTime
							+ " timestamp = " + timestamp + " will call now  getValidAOsAfterItInTheDay()\n");
		}
		return getValidAOsAfterItInTheDay(indexOfAOAtThisTime, givenTimelineToCheckIn);
	}

	///
	/**
	 * 
	 * @param givenActivityObjectIndex
	 * @param givenTimelineToCheckIn
	 * @return
	 * @since 12 Aug 2018
	 */
	public static List<ActivityObject2018> getValidAOsAfterItInTheDay(int givenActivityObjectIndex,
			Timeline givenTimelineToCheckIn)
	{
		List<ActivityObject2018> validAOsAfter = new ArrayList<>();

		ArrayList<ActivityObject2018> aosInGivenTimelineToCheckIn = givenTimelineToCheckIn
				.getActivityObjectsInTimeline();

		LocalDate dateOfAOAtGivenIndex = givenTimelineToCheckIn.getActivityObjectAtPosition(givenActivityObjectIndex)
				.getEndTimestamp().toLocalDateTime().toLocalDate();

		int i = -1;
		for (i = givenActivityObjectIndex + 1; i < aosInGivenTimelineToCheckIn.size(); i++)
		{
			// System.out.println("for index " + i);
			ActivityObject2018 aoToCheck = aosInGivenTimelineToCheckIn.get(i);
			LocalDate dateOfThisAO = aoToCheck.getEndTimestamp().toLocalDateTime().toLocalDate();
			// System.out.println("dateOfAOAtGivenIndex = " + dateOfAOAtGivenIndex + " dateOfThisAO = " + dateOfThisAO);
			// System.out.println("dateOfThisAO.equals(dateOfAOAtGivenIndex = " +
			// dateOfThisAO.equals(dateOfAOAtGivenIndex));

			if (dateOfThisAO.equals(dateOfAOAtGivenIndex)) // only look at aos in same day
			{
				// System.out.println("found same date");
				if (UtilityBelt.isValidActivityName(aoToCheck.getActivityName()))
				{
					// System.out.println("found valid act");
					validAOsAfter.add(aoToCheck);
				}
			}
			else
			{
				break;
			}
		}

		if (VerbosityConstants.verbose)
		{
			StringBuilder sb = new StringBuilder(
					"\tinside getValidAOsAfterItInTheDay\n\tactivityIndexAfterWhichToCheck=" + givenActivityObjectIndex
							+ "\n givenTimelineToCheckIn = ");
			givenTimelineToCheckIn.getActivityObjectsInTimeline().stream()
					.forEachOrdered(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("|")));
			// givenTimelineToCheckIn.printActivityObjectNamesInSequence();
			sb.append("\n\tNumber of activities in timeline=" + aosInGivenTimelineToCheckIn.size()
					+ " validAOsAfter.size() = " + validAOsAfter.size() + "  nvalidAOsAfter=\n\t");
			validAOsAfter.stream().forEachOrdered(ao -> sb.append(">>" + ao.getPrimaryDimensionVal("|")));
			System.out.println(sb.toString() + "\n\tExiting getValidAOsAfterItInTheDay()\n");
		}

		return validAOsAfter;
	}

	///

	/**
	 * Checks if there is atleast N valid AOs after the given index in the given timeline on the same day as the AO at
	 * given index
	 * 
	 * @param givenActivityObjectIndex
	 * @param givenTimelineToCheckIn
	 * @param N
	 * @return
	 */
	public static boolean hasAtleastNValidAOsAfterItInTheDay(int givenActivityObjectIndex,
			Timeline givenTimelineToCheckIn, int N)
	{
		ArrayList<ActivityObject2018> aosInGivenTimelineToCheckIn = givenTimelineToCheckIn
				.getActivityObjectsInTimeline();

		// abcde: len=5, if N = 3, valid indices = 0,1 invalid indices = 2,3,4 >(5-1-3) > 1
		// trivial case
		if (givenActivityObjectIndex > (aosInGivenTimelineToCheckIn.size() - 1 - N))
		{
			return false;
		}

		boolean hasAtleastNValidAOsAfterItInTheDay = false;

		LocalDate dateOfAOAtGivenIndex = givenTimelineToCheckIn.getActivityObjectAtPosition(givenActivityObjectIndex)
				.getEndTimestamp().toLocalDateTime().toLocalDate();

		int i = -1;
		int numOfValidNextAOs = 0;
		for (i = givenActivityObjectIndex + 1; i < aosInGivenTimelineToCheckIn.size(); i++)
		{
			// System.out.println("for index " + i);
			LocalDate dateOfThisAO = aosInGivenTimelineToCheckIn.get(i).getEndTimestamp().toLocalDateTime()
					.toLocalDate();
			// System.out.println("dateOfAOAtGivenIndex = " + dateOfAOAtGivenIndex + " dateOfThisAO = " + dateOfThisAO);
			// System.out
			// .println("dateOfThisAO.equals(dateOfAOAtGivenIndex = " + dateOfThisAO.equals(dateOfAOAtGivenIndex));

			if (dateOfThisAO.equals(dateOfAOAtGivenIndex)) // only look at aos in same day
			{
				// System.out.println("found same date");
				if (UtilityBelt.isValidActivityName(aosInGivenTimelineToCheckIn.get(i).getActivityName()))
				{
					// System.out.println("found valid act");
					numOfValidNextAOs += 1;
					if (numOfValidNextAOs >= N)
					{
						hasAtleastNValidAOsAfterItInTheDay = true;
						break;
					}
				}
			}
			else
			{
				break;
			}
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("-------\n\tinside hasAtleastNValidAOsAfterItInTheDay\n\tactivityIndexAfterWhichToCheck="
					+ givenActivityObjectIndex + " N = " + N + " hasAtleastNValidAOsAfterItInTheDay = "
					+ hasAtleastNValidAOsAfterItInTheDay);
			// givenTimelineToCheckIn.printActivityObjectNamesInSequence();
			System.out.println(givenTimelineToCheckIn.getPrimaryDimensionValsInSequence());
			System.out.println("\tNumber of activities in timeline=" + aosInGivenTimelineToCheckIn.size());
		}

		return hasAtleastNValidAOsAfterItInTheDay;
	}

	/**
	 * 
	 * @param t
	 * @param timestamp
	 * @param N
	 * @return
	 */
	public static ArrayList<ActivityObject2018> getNextNValidAOsAfterActivityAtThisTimeSameDay(Timeline t,
			Timestamp timestamp, int N)
	{

		LocalDate dateOfGivenTS = timestamp.toLocalDateTime().toLocalDate();
		System.out.println("Inside getNextNValidAOsAfterActivityAtThisTimeSameDay\n given ts=" + timestamp.toString()
				+ " localdate extracted=" + dateOfGivenTS.toString());

		ArrayList<ActivityObject2018> result = new ArrayList<>(N);

		int indexOfActivityObjectAtGivenTimestamp = t.getIndexOfActivityObjectAtTime(timestamp);

		for (int i = 0; i < N; i++)
		{
			ActivityObject2018 ao = t
					.getNextValidActivityAfterActivityAtThisPositionPD(indexOfActivityObjectAtGivenTimestamp + i);

			// System.out.println("Debug:\nTimestamp of ao = " + ao.getEndTimestamp());FOUND OKAY in RUN
			LocalDate dateOfAO = ao.getEndTimestamp().toLocalDateTime().toLocalDate();
			// System.out.println("LocalDate of ao = " + dateOfAO);
			// System.out.println("dateOfAO.equals(dateOfGivenTS)=" + dateOfAO.equals(dateOfGivenTS));

			if (dateOfAO.equals(dateOfGivenTS))
			{// DateTimeUtils.isSameDate(ao.getStartTimestamp(), // timestamp))
				result.add(ao);
			}
			else
			{
				break;
			}
		}

		if (result.size() != N)
		{
			System.err.println(
					PopUps.getTracedErrorMsg("Error in getNextNValidAOsAfterActivityAtThisTimeSameDay result.size(): "
							+ result.size() + "!= N:" + N));
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("Next valid acts: ");
			result.stream().forEach((ActivityObject2018 ao) -> System.out.print(ao.toStringAllGowallaTS() + ">>"));
			System.out.println("Exiting getNextNValidAOsAfterActivityAtThisTimeSameDay");
		}
		return result;
	}

	public static int getNumOfWeekendsInGivenDayTimelines(LinkedHashMap<Date, Timeline> userTimelines)
	{
		int numberOfWeekends = 0;
		for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
		{
			int weekDayInt = entry.getKey().getDay();
			if (weekDayInt == 0 || weekDayInt == 6)
			{
				numberOfWeekends++;
			}
		}
		return numberOfWeekends;
	}

	/**
	 * Checks whether all the ActivityObjects in the Timeline are of the same day or not. Compared the start timestamps
	 * of activity objects
	 * 
	 * @return
	 */
	public static boolean isSameDay(ArrayList<ActivityObject2018> activityObjectsInDay)
	{
		if (activityObjectsInDay.size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in isSameDay activityObjectsInDay.size()= " + activityObjectsInDay.size()));
		}
		Timestamp firstTimestamp = activityObjectsInDay.get(0).getStartTimestamp();
		if (activityObjectsInDay.stream().skip(1)
				.anyMatch(ao -> DateTimeUtils.isSameDate(firstTimestamp, ao.getStartTimestamp()) == false))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	//

	/**
	 * Useful for mu breaking of timelines to find if a particular rt will have recommendation for daywise approach so
	 * that they can be compared
	 * 
	 * @param trainingTimelines
	 * @param dateAtRecomm
	 * @param activityAtRecommPoint
	 * @param collaborativeCandidates
	 * @param userIDAtRecomm
	 * @param trainTestTimelinesForAllUsers
	 * @return
	 */
	public static boolean hasDaywiseCandidateTimelines(LinkedHashMap<Date, Timeline> trainingTimelines,
			Date dateAtRecomm, ActivityObject2018 activityAtRecommPoint, boolean collaborativeCandidates,
			String userIDAtRecomm,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers)
	{
		boolean hasDayWiseCands = false;

		if (collaborativeCandidates)
		{
			LinkedHashMap<String, Timeline> candidateTimelines = TimelineExtractors
					.extractDaywiseCandidateTimelinesCollV2(dateAtRecomm, userIDAtRecomm, activityAtRecommPoint,
							trainTestTimelinesForAllUsers, Constant.only1CandFromEachCollUser,
							Constant.onlyPastFromRecommDateInCandInColl);
			if (candidateTimelines.size() > 0)
			{
				hasDayWiseCands = true;
			}
		}
		else
		{
			LinkedHashMap<Date, Timeline> candidateTimelines = TimelineExtractors
					.extractDaywiseCandidateTimelines(trainingTimelines, dateAtRecomm, activityAtRecommPoint);
			if (candidateTimelines.size() > 0)
			{
				hasDayWiseCands = true;
			}
		}

		return hasDayWiseCands;
	}

	/**
	 * 
	 * @param candidateTimelines2
	 * @param activitiesGuidingRecomm2
	 * @param userIDAtRecomm
	 * @param dateAtRecomm
	 *            only used for writing to file
	 * @param timeAtRecomm
	 *            only used for writing to file
	 * @param hasInvalidActivityNames
	 * @param invalidActName1
	 * @param invalidActName2
	 * @param distanceUsed
	 * @param HJEditDistance
	 * @return
	 */
	public static Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> getEditDistancesForDaywiseCandidateTimelines(
			LinkedHashMap<String, Timeline> candidateTimelines2, ArrayList<ActivityObject2018> activitiesGuidingRecomm2,
			String userIDAtRecomm, String dateAtRecomm, String timeAtRecomm, boolean hasInvalidActivityNames,
			String invalidActName1, String invalidActName2, String distanceUsed, HJEditDistance hjEditDistance)
	{
		// <Date of CandidateTimeline, (End point index of least distant subsequence, String containing the trace of
		// edit operations performed, edit distance of
		// least distant subsequence)>
		/**
		 * {Date of CandidateTimeline as string, Pair {trace of edit operations performed, edit distance of least
		 * distant subsequence}}
		 */
		LinkedHashMap<String, Pair<String, Double>> distancesRes = new LinkedHashMap<>();

		/**
		 * {Date of CandidateTimeline as string, End point index of least distant subsequence}}
		 */
		LinkedHashMap<String, Integer> endIndicesOfLeastDistantSubcand = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> candidate : candidateTimelines2.entrySet())
		{
			Timeline candidateTimeline = candidate.getValue();
			// similarityScores.put(entry.getKey(), getSimilarityScore(entry.getValue(),activitiesGuidingRecomm));
			// (Activity Events in Candidate Day, activity events on or before recomm on recomm day)
			// Long candTimelineID = candidate.getKey().getTime();// used as dummy, not exactly useful right now
			Triple<Integer, String, Double> distance = getEditDistanceLeastDistantSubcand(candidateTimeline,
					activitiesGuidingRecomm2, userIDAtRecomm, dateAtRecomm, timeAtRecomm, candidate.getKey(),
					hasInvalidActivityNames, invalidActName1, invalidActName2, distanceUsed, hjEditDistance);

			distancesRes.put(candidate.getKey(), new Pair<String, Double>(distance.getSecond(), distance.getThird()));
			endIndicesOfLeastDistantSubcand.put(candidate.getKey(), distance.getFirst());
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}

		return new Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>>(distancesRes,
				endIndicesOfLeastDistantSubcand);
	}

	/**
	 * Fork of getEditDistancesForDaywiseCandidateTimelines
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param userIDAtRecomm
	 * @param dateAtRecomm
	 *            only used for writing to file
	 * @param timeAtRecomm
	 *            only used for writing to file
	 * @param distanceUsed
	 * @param hjEditDistance
	 * @param featureWiseEditDistance
	 * @param featureWiseWeightedEditDistance
	 * @param OTMDSAMEditDistance
	 * @param editDistancesMemorizer
	 * @param lookPastType
	 * @return
	 * @since 17 Dec 2018
	 */
	public static Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> getEditDistancesForDaywiseCandidateTimelines17Dec2018(
			// LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018>
			// activitiesGuidingRecomm,String userIDAtRecomm, String dateAtRecomm, String timeAtRecomm, boolean
			// hasInvalidActivityNames, String invalidActName1, String invalidActName2, String distanceUsed,
			// HJEditDistance hjEditDistance)
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userIDAtRecomm, String dateAtRecomm, String timeAtRecomm,
			String distanceUsed, HJEditDistance hjEditDistance, FeatureWiseEditDistance featureWiseEditDistance,
			FeatureWiseWeightedEditDistance featureWiseWeightedEditDistance, OTMDSAMEditDistance OTMDSAMEditDistance,
			EditDistanceMemorizer editDistancesMemorizer, LookPastType lookPastType)
	{
		// <Date of CandidateTimeline, (End point index of least distant subsequence, String containing the trace of
		// edit operations performed, edit distance of
		// least distant subsequence)>
		/**
		 * {Date of CandidateTimeline as string, Pair {trace of edit operations performed, edit distance of least
		 * distant subsequence}}
		 */
		LinkedHashMap<String, Pair<String, Double>> distancesRes = new LinkedHashMap<>();

		/**
		 * {Date of CandidateTimeline as string, End point index of least distant subsequence}}
		 */
		LinkedHashMap<String, Integer> endIndicesOfLeastDistantSubcand = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> candidate : candidateTimelines.entrySet())
		{
			Timeline candidateTimeline = candidate.getValue();
			// similarityScores.put(entry.getKey(), getSimilarityScore(entry.getValue(),activitiesGuidingRecomm));
			// (Activity Events in Candidate Day, activity events on or before recomm on recomm day)
			// Long candTimelineID = candidate.getKey().getTime();// used as dummy, not exactly useful right now
			Triple<Integer, String, Double> distance = getEditDistanceLeastDistantSubcand17Dec2018(candidateTimeline,
					// activitiesGuidingRecomm, userIDAtRecomm, dateAtRecomm, timeAtRecomm, candidate.getKey(),
					// hasInvalidActivityNames, invalidActName1, invalidActName2, distanceUsed, hjEditDistance);
					activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
					distanceUsed, hjEditDistance, featureWiseEditDistance, featureWiseWeightedEditDistance,
					OTMDSAMEditDistance, editDistancesMemorizer, lookPastType);

			distancesRes.put(candidate.getKey(), new Pair<String, Double>(distance.getSecond(), distance.getThird()));
			endIndicesOfLeastDistantSubcand.put(candidate.getKey(), distance.getFirst());
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}

		return new Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>>(distancesRes,
				endIndicesOfLeastDistantSubcand);
	}

	/**
	 * Get distance scores using modified edit distance.
	 * 
	 * <end point index, edit operations trace, edit distance> The distance score is the distance between the activities
	 * guiding recommendation and the least distant subcandidate (which has a valid activity after it) from the
	 * candidate timeline. (subcandidate is a subsequence from candidate timeline, from the start of the candidate
	 * timeline to any occurrence of the ActivityGuiding Recomm or current activity).
	 * 
	 * 
	 * @param candidateDayTimeline
	 *            currently a day timeline. It should not be TimelineWithNext, because instead of timeline 'with next'
	 *            here the subsequence must have a valid act after it or else not considered. This valid act after the
	 *            subsequence essentially served as "the next" activity to recommend.
	 * @param activitiesGuidingRecomm
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 *            only used for writing to file
	 * @param timeAtRecomm
	 *            only used for writing to file
	 * @param candidateID
	 * @param hasInvalidActivityNames
	 * @param invalidActName1
	 * @param invalidActName2
	 * @param distanceUsed
	 * @param HJEditDistance
	 * @return Triple{EndIndexOfLestDistantSubCand,TraceOfEditOperation,EditDistance}
	 */
	public static Triple<Integer, String, Double> getEditDistanceLeastDistantSubcand(Timeline candidateDayTimeline,
			ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candidateID, boolean hasInvalidActivityNames, String invalidActName1,
			String invalidActName2, String distanceUsed, HJEditDistance hjEditDistance)
	{

		// find the end points in the userDayTimeline
		char activityAtRecommPointAsStringCode = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1)
				.getCharCodeFromActID();
		String activitiesGuidingAsStringCode = StringCode
				.getStringCodeForActivityObjectsFromActID(activitiesGuidingRecomm);
		String userDayTimelineAsStringCode = candidateDayTimeline.getActivityObjectsAsStringCode();

		ArrayList<Integer> indicesOfEndPointActivityInDayButNotLastValid = getIndicesOfEndPointActivityInDayButNotLastValid(
				userDayTimelineAsStringCode, activityAtRecommPointAsStringCode, hasInvalidActivityNames,
				Constant.INVALID_ACTIVITY1, Constant.INVALID_ACTIVITY2);

		// System.out.println(
		// "Inside getEditDistancesLeastDistantSubcands: indicesOfEndPointActivityInDayButNotLastValid.size() ="
		// + indicesOfEndPointActivityInDayButNotLastValid.size());

		// $$WritingToFile.writeEndPoinIndexCheck24Oct(activityAtRecommPointAsStringCode,userDayTimelineAsStringCode,indicesOfEndPointActivityInDay1,indicesOfEndPointActivityInDay);

		/** index of end point, edit operations trace, edit distance **/
		LinkedHashMap<Integer, Pair<String, Double>> distanceScoresForEachSubsequence = new LinkedHashMap<>();

		// getting distance scores for each subcandidate
		switch (distanceUsed)
		{
		case "HJEditDistance":
		{
			// HJEditDistance editSimilarity = new HJEditDistance();
			for (Integer indexOfEndPointWithValidAfterIt : indicesOfEndPointActivityInDayButNotLastValid)
			{
				// long t1 = System.currentTimeMillis();
				Pair<String, Double> distance = hjEditDistance.getHJEditDistanceWithTrace(
						candidateDayTimeline.getActivityObjectsInTimelineFromToIndex(0,
								indexOfEndPointWithValidAfterIt + 1),
						activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm, candidateID);
				// System.out.println(
				// "getHJEditDistanceWithTrace computed in: " + (System.currentTimeMillis() - t1) + " ms");
				distanceScoresForEachSubsequence.put(indexOfEndPointWithValidAfterIt, distance);
				// System.out.println("Distance between:\n
				// activitiesGuidingRecomm:"+UtilityBelt.getActivityNamesFromArrayList(activitiesGuidingRecomm)+
				// "\n and subsequence of
				// Cand:"+UtilityBelt.getActivityNamesFromArrayList(userDayTimeline.getActivityObjectsInDayFromToIndex(0,indicesOfEndPointActivityInDay1.get(i)+1)));
			}
			break;
		}
		default:
			System.err.println(PopUps.getTracedErrorMsg("Error unknown distance:" + distanceUsed));
			System.exit(-1);
		}

		// sort by each subcand by edit distance
		distanceScoresForEachSubsequence = (LinkedHashMap<Integer, Pair<String, Double>>) ComparatorUtils
				.sortByValueAscendingIntStrDoub(distanceScoresForEachSubsequence);

		if (distanceScoresForEachSubsequence.size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error no subsequence to be considered for distance,distanceScoresForEachSubsequence.size() = "
							+ distanceScoresForEachSubsequence.size()));
		}

		// we only consider the most similar subsequence . i.e. the first entry in this Map
		List<Entry<Integer, Pair<String, Double>>> subseqWithLeastEditDist = distanceScoresForEachSubsequence.entrySet()
				.stream().limit(1).collect(Collectors.toList());

		int endPointIndexForSubsequenceWithHighestSimilarity = subseqWithLeastEditDist.get(0).getKey();// -1;
		String traceEditOperationsForSubsequenceWithHighestSimilarity = subseqWithLeastEditDist.get(0).getValue()
				.getFirst();
		double distanceScoreForSubsequenceWithHighestSimilarity = subseqWithLeastEditDist.get(0).getValue().getSecond();// -9999;
		distanceScoreForSubsequenceWithHighestSimilarity = StatsUtils
				.round(distanceScoreForSubsequenceWithHighestSimilarity, Constant.RoundingPrecision);

		// finding the end point index with highest similarity WHICH HAS A VALID ACTIVITY AFTER IT
		// removed some legacy code from here since we have already checked if there exists valid act after every of the
		// considered index of subcand. the legacy code can be found:
		// org.activity.recomm.RecommendationMasterDayWise2FasterMar2017.getDistanceScoreModifiedEdit()

		// /////

		if (VerbosityConstants.verbose)
		{
			System.out.println("---Debug: getEditDistancesLeastDistantSubcand---- \nactivitiesGuidingAsStringCode="
					+ activitiesGuidingAsStringCode + "\nactivityAtRecommPointAsStringCode="
					+ activityAtRecommPointAsStringCode + "\nuserDayTimelineAsStringCode="
					+ userDayTimelineAsStringCode);
			for (Map.Entry<Integer, Pair<String, Double>> entry1 : distanceScoresForEachSubsequence.entrySet())
			{
				System.out.println("End point= " + entry1.getKey() + " distance=" + entry1.getValue().getSecond()
						+ "trace=" + entry1.getValue().getFirst());
			}
			System.out.println("endPointIndexForSubsequenceWithHighestSimilarity="
					+ endPointIndexForSubsequenceWithHighestSimilarity);
			System.out.println("distanceScoreForSubsequenceWithHighestSimilarity="
					+ distanceScoreForSubsequenceWithHighestSimilarity);
		}
		if (VerbosityConstants.WriteEditDistancesOfAllEndPoints)
		{
			WToFile.writeEditDistancesOfAllEndPoints(activitiesGuidingRecomm, candidateDayTimeline,
					distanceScoresForEachSubsequence);

		}

		// Triple{EndIndexOfLestDistantSubCand,TraceOfEditOperation,EditDistance}
		return new Triple<Integer, String, Double>(endPointIndexForSubsequenceWithHighestSimilarity,
				traceEditOperationsForSubsequenceWithHighestSimilarity,
				distanceScoreForSubsequenceWithHighestSimilarity);
	}

	//// start of added on 17 Dec 2018
	/**
	 * Fork of getEditDistanceLeastDistantSubcand Get distance scores using modified edit distance.
	 * 
	 * <end point index, edit operations trace, edit distance> The distance score is the distance between the activities
	 * guiding recommendation and the least distant subcandidate (which has a valid activity after it) from the
	 * candidate timeline. (subcandidate is a subsequence from candidate timeline, from the start of the candidate
	 * timeline to any occurrence of the ActivityGuiding Recomm or current activity).
	 * 
	 * 
	 * @param candidateDayTimeline
	 *            currently a day timeline. It should not be TimelineWithNext, because instead of timeline 'with next'
	 *            here the subsequence must have a valid act after it or else not considered. This valid act after the
	 *            subsequence essentially served as "the next" activity to recommend.
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 *            only used for writing to file
	 * @param timeAtRecomm
	 *            only used for writing to file
	 * @param distanceUsed
	 * @param hjEditDistance
	 * @param featureWiseEditDistance
	 * @param featureWiseWeightedEditDistance
	 * @param OTMDSAMEditDistance
	 * @param editDistancesMemorizer
	 * @param lookPastType
	 * @return Triple{EndIndexOfLeastDistantSubCand,TraceOfEditOperation,EditDistance}
	 * @since 17 Dec 2018
	 */
	public static Triple<Integer, String, Double> getEditDistanceLeastDistantSubcand17Dec2018(
			Timeline candidateDayTimeline,
			// ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm,
			// String dateAtRecomm, String timeAtRecomm, String candidateID, boolean hasInvalidActivityNames,
			// String invalidActName1, String invalidActName2, String distanceUsed, HJEditDistance hjEditDistance)
			ArrayList<ActivityObject2018> activitiesGuidingRecomm, Enums.CaseType caseType, String userIDAtRecomm,
			String dateAtRecomm, String timeAtRecomm, String distanceUsed, HJEditDistance hjEditDistance,
			FeatureWiseEditDistance featureWiseEditDistance,
			FeatureWiseWeightedEditDistance featureWiseWeightedEditDistance, OTMDSAMEditDistance OTMDSAMEditDistance,
			EditDistanceMemorizer editDistancesMemorizer, LookPastType lookPastType)
	{
		String separatorForCandIDAndEndPointForThisSubCand = "~~";
		// String invalidActName1 = Constant.getInvalidActivity1();
		// String invalidActName2 = Constant.getInvalidActivity2();
		// System.out.println(
		// "Inside getEditDistancesLeastDistantSubcands: indicesOfEndPointActivityInDayButNotLastValid.size() ="
		// + indicesOfEndPointActivityInDayButNotLastValid.size());
		// $$WritingToFile.writeEndPoinIndexCheck24Oct(activityAtRecommPointAsStringCode,userDayTimelineAsStringCode,indicesOfEndPointActivityInDay1,indicesOfEndPointActivityInDay);

		// find the end points in the userDayTimeline
		char activityAtRecommPointAsStringCode = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1)
				.getCharCodeFromActID();
		String activitiesGuidingAsStringCode = StringCode
				.getStringCodeForActivityObjectsFromActID(activitiesGuidingRecomm);
		String userDayTimelineAsStringCode = candidateDayTimeline.getActivityObjectsAsStringCode();

		ArrayList<Integer> indicesOfEndPointActivityInDayButNotLastValid = getIndicesOfEndPointActivityInDayButNotLastValid(
				userDayTimelineAsStringCode, activityAtRecommPointAsStringCode, Constant.hasInvalidActivityNames,
				Constant.INVALID_ACTIVITY1, Constant.INVALID_ACTIVITY2);

		// start of added on 17 Dec 2018
		// Consider the set of subcandidates as a set of candidate timelines
		// Extract set of subcandidate timelines
		LinkedHashMap<String, Timeline> subCandTimelinesForGivenTimelines = new LinkedHashMap(
				indicesOfEndPointActivityInDayButNotLastValid.size());
		for (Integer indexOfEndPointWithValidAfterIt : indicesOfEndPointActivityInDayButNotLastValid)
		{// subcandID = <timelineID>$$<indexOfEndPointWithValidAfterIt>;//SCAND for Sub Candidate
			String subCandTimelineID = candidateDayTimeline.getTimelineID()
					+ separatorForCandIDAndEndPointForThisSubCand + indexOfEndPointWithValidAfterIt;
			Timeline subCandTimelines = new Timeline(candidateDayTimeline.getActivityObjectsInTimelineFromToIndex(0,
					indexOfEndPointWithValidAfterIt + 1), true, true);
			subCandTimelines.setTimelineID(subCandTimelineID);
			subCandTimelinesForGivenTimelines.put(subCandTimelineID, subCandTimelines);
		}
		// finished creation of subcanndidates

		// get edit distances for each subcandidate
		/** index of end point, edit operations trace, edit distance **/
		// LinkedHashMap<Integer, Pair<String, Double>> distanceScoresForEachSubcandidate = new LinkedHashMap<>();
		// {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
		LinkedHashMap<String, Pair<String, Double>> normalisedDistanceForSubcandTimelines = DistanceUtils
				.getNormalisedDistancesForCandidateTimelinesFullCand(subCandTimelinesForGivenTimelines,
						activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(),
						timeAtRecomm.toString(), distanceUsed, hjEditDistance, featureWiseEditDistance,
						featureWiseWeightedEditDistance, OTMDSAMEditDistance, editDistancesMemorizer, lookPastType);
		// end of added on 17 Dec 2018

		// getting distance scores for each subcandidate
		// switch (distanceUsed)
		// {
		// case "HJEditDistance":
		// {
		// // HJEditDistance editSimilarity = new HJEditDistance();
		// for (Integer indexOfEndPointWithValidAfterIt : indicesOfEndPointActivityInDayButNotLastValid)
		// {
		// // long t1 = System.currentTimeMillis();
		// Pair<String, Double> distance = hjEditDistance.getHJEditDistanceWithTrace(
		// candidateDayTimeline.getActivityObjectsInTimelineFromToIndex(0,
		// indexOfEndPointWithValidAfterIt + 1),
		// activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm, candidateID);
		// // System.out.println(
		// // "getHJEditDistanceWithTrace computed in: " + (System.currentTimeMillis() - t1) + " ms");
		// distanceScoresForEachSubsequence.put(indexOfEndPointWithValidAfterIt, distance);
		// // System.out.println("Distance between:\n
		// // activitiesGuidingRecomm:"+UtilityBelt.getActivityNamesFromArrayList(activitiesGuidingRecomm)+
		// // "\n and subsequence of
		// //
		// Cand:"+UtilityBelt.getActivityNamesFromArrayList(userDayTimeline.getActivityObjectsInDayFromToIndex(0,indicesOfEndPointActivityInDay1.get(i)+1)));
		// }
		// break;
		// }
		// default:
		// System.err.println(PopUps.getTracedErrorMsg("Error unknown distance:" + distanceUsed));
		// System.exit(-1);
		// }

		// sort by each subcand by edit distance
		// distanceScoresForEachSubcandidate = (LinkedHashMap<Integer, Pair<String, Double>>) ComparatorUtils
		// .sortByValueAscendingIntStrDoub(distanceScoresForEachSubcandidate);
		normalisedDistanceForSubcandTimelines = (LinkedHashMap<String, Pair<String, Double>>) ComparatorUtils
				.sortByValueAscendingStrStrDoub(normalisedDistanceForSubcandTimelines);

		if (normalisedDistanceForSubcandTimelines.size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error no subsequence to be considered for distance,normalisedDistanceForSubcandTimelines.size() = "
							+ normalisedDistanceForSubcandTimelines.size()));
		}

		// we only consider the most similar subsequence . i.e. the first entry in this Map
		// List<Entry<Integer, Pair<String, Double>>> subseqWithLeastEditDist = distanceScoresForEachSubcandidate
		// .entrySet().stream().limit(1).collect(Collectors.toList());
		List<Entry<String, Pair<String, Double>>> subseqWithLeastEditDist = normalisedDistanceForSubcandTimelines
				.entrySet().stream().limit(1).collect(Collectors.toList());

		// from subcandID = <timelineID>$$<indexOfEndPointWithValidAfterIt>;
		int endPointIndexForSubsequenceWithHighestSimilarity = Integer
				.valueOf(subseqWithLeastEditDist.get(0).getKey().split(separatorForCandIDAndEndPointForThisSubCand)[1]);// -1;
		String traceEditOperationsForSubsequenceWithHighestSimilarity = subseqWithLeastEditDist.get(0).getValue()
				.getFirst();
		double distanceScoreForSubsequenceWithHighestSimilarity = subseqWithLeastEditDist.get(0).getValue().getSecond();// -9999;
		distanceScoreForSubsequenceWithHighestSimilarity = StatsUtils
				.round(distanceScoreForSubsequenceWithHighestSimilarity, Constant.RoundingPrecision);

		// finding the end point index with highest similarity WHICH HAS A VALID ACTIVITY AFTER IT
		// removed some legacy code from here since we have already checked if there exists valid act after every of the
		// considered index of subcand. the legacy code can be found:
		// org.activity.recomm.RecommendationMasterDayWise2FasterMar2017.getDistanceScoreModifiedEdit()

		// /////
		//
		if (VerbosityConstants.verbose)
		{
			// PopUps.showMessage("Here1");
			StringBuilder sb = new StringBuilder(
					"---Debug17Dec2018: getEditDistancesLeastDistantSubcand---- \nactivitiesGuidingAsStringCode="
							+ activitiesGuidingAsStringCode + "\nactivityAtRecommPointAsStringCode="
							+ activityAtRecommPointAsStringCode + "\nuserDayTimelineAsStringCode="
							+ userDayTimelineAsStringCode + "\n");

			// for sanity check that subcand timelines are correctly created from timelines
			// checked OKAY on 17 Dec 2018
			sb.append("candidateDayTimeline (id:" + candidateDayTimeline.getTimelineID() + ")= --\n"
					+ candidateDayTimeline.getActivityObjectNamesInSequence() + "\nSubCands = \n");
			subCandTimelinesForGivenTimelines.entrySet().stream().forEachOrdered(e -> sb.append(
					e.getValue().getTimelineID() + " -- " + e.getValue().getActivityObjectNamesInSequence() + "\n"));
			//////////////////////////////////////

			for (Map.Entry<String, Pair<String, Double>> entry1 : normalisedDistanceForSubcandTimelines.entrySet())
			{
				sb.append("SubCandID= " + entry1.getKey() + " distance=" + entry1.getValue().getSecond() + "trace="
						+ entry1.getValue().getFirst() + "\n");
			}
			sb.append("endPointIndexForSubsequenceWithHighestSimilarity="
					+ endPointIndexForSubsequenceWithHighestSimilarity
					+ "\ndistanceScoreForSubsequenceWithHighestSimilarity="
					+ distanceScoreForSubsequenceWithHighestSimilarity);
			System.out.println(sb.toString());
		}
		if (VerbosityConstants.WriteEditDistancesOfAllEndPoints)
		{
			WToFile.writeEditDistancesOfAllEndPoints2(activitiesGuidingRecomm, candidateDayTimeline,
					normalisedDistanceForSubcandTimelines);
		}

		// Triple{EndIndexOfLestDistantSubCand,TraceOfEditOperation,EditDistance}
		return new Triple<Integer, String, Double>(endPointIndexForSubsequenceWithHighestSimilarity,
				traceEditOperationsForSubsequenceWithHighestSimilarity,
				distanceScoreForSubsequenceWithHighestSimilarity);
	}
	//// end of added on 17 Dec 2018

	/**
	 * 
	 * @param userDayActivitiesAsStringCode
	 * @param codeOfEndPointActivity
	 * @return
	 */
	public static ArrayList<Integer> getIndicesOfSubstringInStringButNotAsLast(String userDayActivitiesAsStringCode,
			String codeOfEndPointActivity)
	{
		// System.out.println("\nDebug getIndicesOfEndPointActivityInDayButNotAsLast:
		// userDayActivitiesAsStringCode="+userDayActivitiesAsStringCode+" and
		// codeOfEndPointActivity="+codeOfEndPointActivity);
		ArrayList<Integer> indicesOfEndPointActivityInDay = new ArrayList<>();

		int index = userDayActivitiesAsStringCode.indexOf(codeOfEndPointActivity); // get index of first occurrence

		while (index >= 0)
		{
			// System.out.println(index);
			if (index != (userDayActivitiesAsStringCode.length() - 1)) // not last index
			{
				indicesOfEndPointActivityInDay.add(index);
			}
			index = userDayActivitiesAsStringCode.indexOf(codeOfEndPointActivity, index + 1);
		}
		return indicesOfEndPointActivityInDay;
	}

	/**
	 * Indices of current activity occurrence with atleast one valid activity after it.
	 * 
	 * @param userDayActivitiesAsStringCode
	 *            each activity is represented by single char in this string
	 * @param codeOfEndPointActivity
	 * @param hasInvalidActivityNames
	 * @param invalidAct1
	 * @param invalidAct2
	 * @return
	 */
	public static ArrayList<Integer> getIndicesOfEndPointActivityInDayButNotLastValid(
			String userDayActivitiesAsStringCode, char codeOfEndPointActivity, boolean hasInvalidActivityNames,
			String invalidAct1, String invalidAct2)
	{

		// get indices of valid activity ActivityNames
		ArrayList<Integer> indicesOfValids = new ArrayList<>();

		if (hasInvalidActivityNames)
		{
			char codeOfInvalidAct1 = StringCode.getCharCodeFromInvalidActivityName(invalidAct1);
			char codeOfInvalidAct2 = StringCode.getCharCodeFromInvalidActivityName(invalidAct2);
			for (int i = 0; i < userDayActivitiesAsStringCode.length(); i++)
			{
				char charToCheck = userDayActivitiesAsStringCode.charAt(i);
				// userDayActivitiesAsStringCode.substring(i, i + 1); // only one character
				// here codeToCheck is of length 1, hence, using endsWith or equals below shouldn't make difference
				// sanity checked in org.activity.sanityChecks.TestDummy2.checkString1()
				// if (codeToCheck.equals((codeUn) || codeToCheck.equals(codeO))
				if (charToCheck != codeOfInvalidAct1 && charToCheck != codeOfInvalidAct2)
				{
					indicesOfValids.add(i);
				}
			}
		}
		else// all indices are valid acts
		{
			indicesOfValids = (ArrayList<Integer>) IntStream.rangeClosed(0, userDayActivitiesAsStringCode.length() - 1)
					.boxed().collect(Collectors.toList());
		}

		ArrayList<Integer> endPoints = new ArrayList<>();

		for (int indexOfValid = 0; indexOfValid < indicesOfValids.size() - 1; indexOfValid++)
		{ // skip the last valid because there is no valid activity to recommend after that.
			// if (userDayActivitiesAsStringCode.substring(indexOfValid, indexOfValid +
			// 1).equals(codeOfEndPointActivity))
			if (userDayActivitiesAsStringCode.charAt(indexOfValid) == codeOfEndPointActivity)
			{
				endPoints.add(indexOfValid);
			}
		}

		if (VerbosityConstants.verbose)
		{
			System.out
					.println("\nDebug getIndicesOfEndPointActivityInDayButNotLastValid: userDayActivitiesAsStringCode="
							+ userDayActivitiesAsStringCode + "  and codeOfEndPointActivity=" + codeOfEndPointActivity);
			System.out.println("indices of valids=" + indicesOfValids);
			System.out.println("end points considered" + endPoints);
		}

		return endPoints;
	}

	/**
	 * Gets the start time distances of the (valid) Activity Object in each candidate timeline which is nearest to the
	 * start time of the current Activity Object
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param userIDAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param hasinvalidactivitynames
	 * @param iNVALID_ACTIVITY1
	 * @param iNVALID_ACTIVITY2
	 * @param distanceUsed
	 * @return {candID, Pair{ActName with nearest ST to current AO,abs time diff in secs}}
	 */
	public static Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> getClosestTimeDistancesForDaywiseCandidateTimelines(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			String userIDAtRecomm, String dateAtRecomm, String timeAtRecomm, boolean hasinvalidactivitynames,
			String iNVALID_ACTIVITY1, String iNVALID_ACTIVITY2, String distanceUsed)
	{

		// timelineID, <Index for the nearest Activity Object, Diff of Start time of nearest Activity
		// Object with start time of current Activity Object>
		// {Date of CandidateTimeline as string, Pair{ActName with nearest ST to current AO,abs time diff in secs}}
		LinkedHashMap<String, Pair<String, Double>> distances = new LinkedHashMap<>();

		/**
		 * {Date of CandidateTimeline as string, End point index of least distant subsequence}}
		 */
		LinkedHashMap<String, Integer> indicesOfActObjsWithNearestST = new LinkedHashMap<>();

		ActivityObject2018 activityObjectAtRecommPoint = activitiesGuidingRecomm
				.get(activitiesGuidingRecomm.size() - 1);
		Timestamp startTimestampOfActObjAtRecommPoint = activityObjectAtRecommPoint.getStartTimestamp();

		for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
		{
			/*
			 * For this cand timeline, find the Activity Object with start timestamp nearest to the start timestamp of
			 * current Activity Object and the distance is diff of their start times
			 */
			Triple<Integer, ActivityObject2018, Double> score = (entry.getValue()
					.getTimeDiffValidAOInDayWithStartTimeNearestTo(startTimestampOfActObjAtRecommPoint));

			distances.put(entry.getKey(),
					new Pair<String, Double>(score.getSecond().getActivityName(), score.getThird()));

			indicesOfActObjsWithNearestST.put(entry.getKey(), score.getFirst());
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}
		return new Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>>(distances,
				indicesOfActObjsWithNearestST);
	}

	///////
	/**
	 * This is not restricted to daywise view of candidate timelines. The candidate timelines are considered as one
	 * single timelines.
	 * <p>
	 * Gets the start time distances of the (valid) Activity Object in each candidate timeline which is nearest to the
	 * start time of the current Activity Object
	 * <p>
	 * <font color = blue> can be optimized. No need to extract unique dates as the candidate timelines are already
	 * unique dates. However, need to check this and refactor carefully</font>
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param userIDAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param hasinvalidactivitynames
	 * @param iNVALID_ACTIVITY1
	 * @param iNVALID_ACTIVITY2
	 * @param distanceUsed
	 * @return {candID, Pair{ActName with nearest ST to current AO,abs time diff in secs}}
	 * @since 12 June 2017
	 */
	public static Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> getClosestTimeDistancesForCandidateTimelines(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			String userIDAtRecomm, String dateAtRecomm, String timeAtRecomm, boolean hasinvalidactivitynames,
			String iNVALID_ACTIVITY1, String iNVALID_ACTIVITY2, String distanceUsed, boolean verbose)
	{
		// timelineID, <Index for the nearest Activity Object, Diff of Start time of nearest Activity
		// Object with start time of current Activity Object>
		// {Date of CandidateTimeline as string, Pair{ActName with nearest ST to current AO,abs time diff in secs}}
		LinkedHashMap<String, Pair<String, Double>> distances = new LinkedHashMap<>();

		/**
		 * {Date of CandidateTimeline as string, End point index of least distant subsequence}}
		 */
		LinkedHashMap<String, Integer> indicesOfActObjsWithNearestST = new LinkedHashMap<>();

		ActivityObject2018 activityObjectAtRecommPoint = activitiesGuidingRecomm
				.get(activitiesGuidingRecomm.size() - 1);
		Timestamp startTimestampOfActObjAtRecommPoint = activityObjectAtRecommPoint.getStartTimestamp();

		Timeline candidateTimelinesAsOne = TimelineTransformers.dayTimelinesToATimeline2(candidateTimelines, false,
				true);

		// find how many times this time occurs in the candidate timelines.
		LinkedHashSet<LocalDate> uniqueDatesInCands = TimelineUtils.getUniqueDates(candidateTimelinesAsOne, verbose);

		Sanity.eq(uniqueDatesInCands.size(), candidateTimelines.size(),
				"Error: we were expecting uniqueDatesInCands.size() = " + uniqueDatesInCands.size()
						+ " candidateTimelines.size() =" + candidateTimelines.size() + " to be equal");
		// since the candidate timelines were indeed day timelines here.
		if (verbose)
		{
			System.out.println("\nuniqueDatesInCands.size() = " + uniqueDatesInCands.size()
					+ " candidateTimelines.size() =" + candidateTimelines.size());
		}

		ArrayList<Timestamp> timestampsToLookInto = TimelineCreators.createTimestampsToLookAt(uniqueDatesInCands,
				startTimestampOfActObjAtRecommPoint, verbose);

		for (Timestamp tsToLookInto : timestampsToLookInto)
		// for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
		{

			// Actually these dates as string should be same as keys candidateTimelines
			Date d = new Date(tsToLookInto.getYear(), tsToLookInto.getMonth(), tsToLookInto.getDate());
			/*
			 * For this cand timeline, find the Activity Object with start timestamp nearest to the start timestamp of
			 * current Activity Object and the distance is diff of their start times
			 */
			Triple<Integer, ActivityObject2018, Double> score = candidateTimelinesAsOne
					.getTimeDiffValidAOWithStartTimeNearestTo(tsToLookInto, verbose);

			distances.put(d.toString(),
					new Pair<String, Double>(score.getSecond().getActivityName(), score.getThird()));

			indicesOfActObjsWithNearestST.put(d.toString(), score.getFirst());
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}

		// these two should be identical
		candidateTimelines.keySet();
		distances.keySet();

		if (!candidateTimelines.keySet().equals(distances.keySet()))
		{
			String candKeys = candidateTimelines.keySet().stream().collect(Collectors.joining("\n"));
			String distKeys = distances.keySet().stream().collect(Collectors.joining("\n"));
			System.out.println("candidateTimelines.keySet()== distances.keySet(): "
					+ (candidateTimelines.keySet().equals(distances.keySet())));
			System.out.println("distKeys==candKeys : " + (distKeys.equals(candKeys)));
			System.out.println("candKeys = " + candKeys);
			System.out.println("distKeys = " + distKeys);

		}

		return new Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>>(distances,
				indicesOfActObjsWithNearestST);
	}

	///////

	/**
	 * Fork of getClosestTimeDistancesForCandidateTimelines
	 * <p>
	 * This is not restricted to daywise view of candidate timelines. The candidate timelines are considered as one
	 * single timelines.
	 * <p>
	 * Gets the start time distances of the (valid) Activity Object in each candidate timeline which is nearest to the
	 * start time of the current Activity Object
	 * <p>
	 * <font color = blue> can be optimized. No need to extract unique dates as the candidate timelines are already
	 * unique dates. However, need to check this and refactor carefully</font>
	 * 
	 * @param candidateTimelines
	 *            from other multiple users {UserID__DateAsString,Timeline}
	 * @param activitiesGuidingRecomm
	 * @param userIDAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param hasinvalidactivitynames
	 * @param iNVALID_ACTIVITY1
	 * @param iNVALID_ACTIVITY2
	 * @param distanceUsed
	 * @return {candID, Pair{ActName with nearest ST to current AO,abs time diff in secs}}
	 * @since 21 July 2017
	 */
	public static Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> getClosestTimeDistancesForCandidateTimelinesColl(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			String userIDAtRecomm, String dateAtRecomm, String timeAtRecomm, boolean hasinvalidactivitynames,
			String iNVALID_ACTIVITY1, String iNVALID_ACTIVITY2, String distanceUsed, boolean verbose)
	{
		// timelineID, <Index for the nearest Activity Object, Diff of Start time of nearest Activity
		// Object with start time of current Activity Object>
		// {UserID__DateAsStringCandidateTimeline, Pair{ActName with nearest ST to current AO,abs time diff in secs}}
		LinkedHashMap<String, Pair<String, Double>> distances = new LinkedHashMap<>();

		/**
		 * {UserID__DateAsStringCandidateTimeline, End point index of least distant subsequence}}
		 */
		LinkedHashMap<String, Integer> indicesOfActObjsWithNearestST = new LinkedHashMap<>();

		ActivityObject2018 activityObjectAtRecommPoint = activitiesGuidingRecomm
				.get(activitiesGuidingRecomm.size() - 1);
		Timestamp startTimestampOfActObjAtRecommPoint = activityObjectAtRecommPoint.getStartTimestamp();

		LinkedHashMap<String, LinkedHashMap<String, Timeline>> userWiseCandidateTimelines = toUserwiseTimelines(
				candidateTimelines);

		for (Entry<String, LinkedHashMap<String, Timeline>> cand : userWiseCandidateTimelines.entrySet())
		{
			String anotherUserID = cand.getKey();
			LinkedHashMap<String, Timeline> candTimelinesFromAnotherUser = cand.getValue();
			// Was there any essential need for this in the first place, i.e., even in non-collaborative approach? Yes,
			// there was since we want to allow closest activity from previous day as well in cases when the act is near
			// midnight
			Timeline candTimelinesFromAnotherUserAsOne = TimelineTransformers
					.dayTimelinesToATimeline2(candTimelinesFromAnotherUser, false, true);

			// find how many times this time occurs in the candidate timelines.
			LinkedHashSet<LocalDate> uniqueDatesInCands = TimelineUtils
					.getUniqueDates(candTimelinesFromAnotherUserAsOne, verbose);

			// @@@@@
			Sanity.eq(uniqueDatesInCands.size(), candTimelinesFromAnotherUser.size(),
					"Error: we were expecting uniqueDatesInCands.size() = " + uniqueDatesInCands.size()
							+ " candTimelinesFromAnotherUser.size() =" + candTimelinesFromAnotherUser.size()
							+ " to be equal");
			// since the candTimelinesFromAnotherUser were indeed day timelines here.
			if (verbose)
			{
				System.out.println("\nanotherUserID= " + anotherUserID + "  uniqueDatesInCands.size() = "
						+ uniqueDatesInCands.size() + " candTimelinesFromAnotherUser.size() ="
						+ candTimelinesFromAnotherUser.size());
			}

			ArrayList<Timestamp> timestampsToLookInto = TimelineCreators.createTimestampsToLookAt(uniqueDatesInCands,
					startTimestampOfActObjAtRecommPoint, verbose);

			// for (Entry<String, Timeline> cand : candidateTimelines.entrySet())
			// { for (ActivityObject ao : cand.getValue().getActivityObjectsInTimeline())
			// { uniqueDates.add(Instant.ofEpochMilli(ao.getStartTimestampInms())
			// .atZone(Constant.getTimeZone().toZoneId()).toLocalDate()); } }

			for (Timestamp tsToLookInto : timestampsToLookInto)
			// for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
			{
				// Actually these dates as string should be same as keys candidateTimelines
				Date d = new Date(tsToLookInto.getYear(), tsToLookInto.getMonth(), tsToLookInto.getDate());
				/*
				 * For this candTimelinesFromAnotherUserAsOne, find the Activity Object with start timestamp nearest to
				 * the start timestamp of current Activity Object and the distance is diff of their start times
				 */
				Triple<Integer, ActivityObject2018, Double> score = candTimelinesFromAnotherUserAsOne
						.getTimeDiffValidAOWithStartTimeNearestTo(tsToLookInto, verbose);

				distances.put(anotherUserID + "__" + d.toString(),
						new Pair<String, Double>(score.getSecond().getActivityName(), score.getThird()));

				indicesOfActObjsWithNearestST.put(anotherUserID + "__" + d.toString(), score.getFirst());
				// System.out.println("now we put "+entry.getKey()+" and score="+score);
			}
		}

		// these two should be identical
		String candKeys = candidateTimelines.keySet().stream().collect(Collectors.joining("\n"));
		String distKeys = distances.keySet().stream().collect(Collectors.joining("\n"));

		StringBuilder msg = new StringBuilder();
		msg.append("candidateTimelines.keySet()== distances.keySet() ?: "
				+ (candidateTimelines.keySet().equals(distances.keySet())) + "\n");
		msg.append("distKeys==candKeys : " + (distKeys.equals(candKeys)) + "\n");
		// msg.append("candKeys = " + candKeys + "\n");
		// msg.append("distKeys = " + distKeys + "\n");
		System.out.println(msg);

		if (!candidateTimelines.keySet().equals(distances.keySet()))
		{

			// String candKeyse = candidateTimelines.keySet().stream().collect(Collectors.joining("\n"));
			// String distKeyse = distances.keySet().stream().collect(Collectors.joining("\n"));

			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("candidateTimelines.keySet()== distances.keySet() ?: "
					+ (candidateTimelines.keySet().equals(distances.keySet())) + "\n");
			errorMsg.append("distKeys==candKeys : " + (distKeys.equals(candKeys)) + "\n");
			errorMsg.append("candKeys = " + candKeys + "\n");
			errorMsg.append("distKeys = " + distKeys + "\n");
			PopUps.printTracedErrorMsgWithExit(errorMsg.toString());
		}

		return new Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>>(distances,
				indicesOfActObjsWithNearestST);
	}

	///////

	/**
	 * <p>
	 * This is not restricted to daywise view of candidate timelines. The candidate timelines are considered as one
	 * single timelines.
	 * <p>
	 * Gets the start time distances of the (valid) Activity Object in each candidate timeline which is nearest to the
	 * start time of the current Activity Object
	 * <p>
	 * <font color = blue> can be optimized. No need to extract unique dates as the candidate timelines are already
	 * unique dates. However, need to check this and refactor carefully</font>
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param userIDAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param hasinvalidactivitynames
	 * @param iNVALID_ACTIVITY1
	 * @param iNVALID_ACTIVITY2
	 * @param distanceUsed
	 * @param timeDiffThresholdInMilliSecs
	 * @param verbose
	 * @return {candID, Pair{ActName with nearest ST to current AO,abs time diff in secs}}
	 * @since 15 Aug 2017
	 */
	public static Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> getClosestTimeDistsForCandTimelinesColl1CandPerNeighbour(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			String userIDAtRecomm, String dateAtRecomm, String timeAtRecomm, boolean hasinvalidactivitynames,
			String iNVALID_ACTIVITY1, String iNVALID_ACTIVITY2, String distanceUsed,
			/* double timeDiffThresholdInMilliSecs, */boolean verbose)
	{
		// timelineID, <Index for the nearest Activity Object, Diff of Start time of nearest Activity
		// Object with start time of current Activity Object>
		// {UserID__DateAsStringCandidateTimeline, Pair{ActName with nearest ST to current AO,abs time diff in secs}}
		LinkedHashMap<String, Pair<String, Double>> distances = new LinkedHashMap<>();

		int numOfUsersWithNoRecomms = 0;
		/**
		 * {UserID__DateAsStringCandidateTimeline, End point index of least distant subsequence}}
		 */
		LinkedHashMap<String, Integer> indicesOfActObjsWithNearestST = new LinkedHashMap<>();

		ActivityObject2018 activityObjectAtRecommPoint = activitiesGuidingRecomm
				.get(activitiesGuidingRecomm.size() - 1);
		Timestamp startTimestampOfActObjAtRecommPoint = activityObjectAtRecommPoint.getStartTimestamp();

		LinkedHashMap<String, LinkedHashMap<String, Timeline>> userWiseCandidateTimelines = toUserwiseTimelines(
				candidateTimelines);

		for (Entry<String, LinkedHashMap<String, Timeline>> cand : userWiseCandidateTimelines.entrySet())
		{
			String anotherUserID = cand.getKey();
			LinkedHashMap<String, Timeline> candTimelinesFromAnotherUser = cand.getValue();
			// Was there any essential need for this in the first place, i.e., even in non-collaborative approach? Yes,
			// there was since we want to allow closest activity from previous day as well in cases when the act is near
			// midnight
			Timeline candTimelinesFromAnotherUserAsOne = TimelineTransformers
					.dayTimelinesToATimeline2(candTimelinesFromAnotherUser, false, true);

			// find how many times this time occurs in the candidate timelines.
			LinkedHashSet<LocalDate> uniqueDatesInCands = TimelineUtils
					.getUniqueDates(candTimelinesFromAnotherUserAsOne, verbose);

			// @@@@@
			// Sanity.eq(uniqueDatesInCands.size(), candTimelinesFromAnotherUser.size(),
			// "Error: we were expecting uniqueDatesInCands.size() = " + uniqueDatesInCands.size()
			// + " candTimelinesFromAnotherUser.size() =" + candTimelinesFromAnotherUser.size()
			// + " to be equal");
			// since the candTimelinesFromAnotherUser were indeed day timelines here.

			ArrayList<Timestamp> timestampsToLookInto = TimelineCreators.createTimestampsToLookAt(uniqueDatesInCands,
					startTimestampOfActObjAtRecommPoint, verbose);

			// for (Entry<String, Timeline> cand : candidateTimelines.entrySet())
			// { for (ActivityObject ao : cand.getValue().getActivityObjectsInTimeline())
			// { uniqueDates.add(Instant.ofEpochMilli(ao.getStartTimestampInms())
			// .atZone(Constant.getTimeZone().toZoneId()).toLocalDate()); } }

			TreeMap<Double, Triple<Integer, ActivityObject2018, Double>> timeDiffOfClosest = new TreeMap<>();
			for (Timestamp tsToLookInto : timestampsToLookInto)
			// for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
			{
				// Actually these dates as string should be same as keys candidateTimelines
				// Date d = new Date(tsToLookInto.getYear(), tsToLookInto.getMonth(), tsToLookInto.getDate());

				/*
				 * For this candTimelinesFromAnotherUserAsOne, find the Activity Object with start timestamp nearest to
				 * the start timestamp of current Activity Object and the distance is diff of their start times
				 */
				Triple<Integer, ActivityObject2018, Double> score = candTimelinesFromAnotherUserAsOne
						.getTimeDiffValidAOWithStartTimeNearestTo(tsToLookInto, verbose);
				// System.out.println("Score= " + score.toString());
				timeDiffOfClosest.put(score.getThird(), score);
			}

			Triple<Integer, ActivityObject2018, Double> closestScore = timeDiffOfClosest.firstEntry().getValue();

			if (closestScore != null)// closestScore.getThird() > timeDiffThresholdInMilliSecs)
			{
				distances.put(anotherUserID /* + "__" + d.toString() */,
						new Pair<String, Double>(closestScore.getSecond().getActivityName(), closestScore.getThird()));
				indicesOfActObjsWithNearestST.put(anotherUserID /* + "__" + d.toString() */, closestScore.getFirst());
			}
			else
			{
				numOfUsersWithNoRecomms += 1;
				System.out.println("No recomm from this cand as no one");// is as close as " +
																			// timeDiffThresholdInMilliSecs
				// + " to their respective current times.");
			}
			if (verbose)
			{
				System.out.println("\nanotherUserID= " + anotherUserID + "  uniqueDatesInCands.size() = "
						+ uniqueDatesInCands.size() + " candTimelinesFromAnotherUser.size() ="
						+ candTimelinesFromAnotherUser.size());
				System.out.println("timeDiffOfClosest = " + timeDiffOfClosest);
				System.out.println("closestScore= " + closestScore);
			}
			// System.out.println("now we put "+entry.getKey()+" and score="+score);

		}

		System.out.println("numOfUsersWithNoRecomms = " + numOfUsersWithNoRecomms);

		// these two should be identical: NOT HERE SINCE NOT DAYWISE
		// //Start of curtain Aug 15
		// String candKeys = candidateTimelines.keySet().stream().collect(Collectors.joining("\n"));
		// String distKeys = distances.keySet().stream().collect(Collectors.joining("\n"));
		//
		// StringBuilder msg = new StringBuilder();
		// msg.append("candidateTimelines.keySet()== distances.keySet() ?: "
		// + (candidateTimelines.keySet().equals(distances.keySet())) + "\n");
		// msg.append("distKeys==candKeys : " + (distKeys.equals(candKeys)) + "\n");
		// // msg.append("candKeys = " + candKeys + "\n");
		// // msg.append("distKeys = " + distKeys + "\n");
		// System.out.println(msg);
		//
		// if (!candidateTimelines.keySet().equals(distances.keySet()))
		// {
		//
		// // String candKeyse = candidateTimelines.keySet().stream().collect(Collectors.joining("\n"));
		// // String distKeyse = distances.keySet().stream().collect(Collectors.joining("\n"));
		//
		// StringBuilder errorMsg = new StringBuilder();
		// errorMsg.append("candidateTimelines.keySet()== distances.keySet() ?: "
		// + (candidateTimelines.keySet().equals(distances.keySet())) + "\n");
		// errorMsg.append("distKeys==candKeys : " + (distKeys.equals(candKeys)) + "\n");
		// errorMsg.append("candKeys = " + candKeys + "\n");
		// errorMsg.append("distKeys = " + distKeys + "\n");
		// PopUps.printTracedErrorMsgWithExit(errorMsg.toString());
		// }
		// //End of curtain Aug 15
		return new Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>>(distances,
				indicesOfActObjsWithNearestST);
	}

	///////

	/**
	 * 
	 * @param givenTimelinesWithUserIDInKey
	 *            {UserID__DateAsString, Timeline}
	 * 
	 * @return {UserID,{DateAsString, Timeline}}
	 */
	private static LinkedHashMap<String, LinkedHashMap<String, Timeline>> toUserwiseTimelines(
			LinkedHashMap<String, Timeline> givenTimelinesWithUserIDInKey)
	{
		LinkedHashMap<String, LinkedHashMap<String, Timeline>> userWiseTimelines = new LinkedHashMap<>();
		try
		{
			for (Entry<String, Timeline> t : givenTimelinesWithUserIDInKey.entrySet())
			{
				String[] keySplitted = RegexUtils.patternDoubleUnderScore.split(t.getKey());
				if (keySplitted.length != 2)
				{
					PopUps.printTracedErrorMsgWithExit("Error: expected key with UserID__DateAsString but found "
							+ t.getKey() + "keySplitted.length= " + keySplitted.length
							+ " != 2. see org.activity.recomm.RecommendationMasterMar2017GenSeq.extractCandClosestTimeColl1()");
				}
				else
				{
					// converting to integer intermediately to ensure that it is user id as number
					String userID = String.valueOf(Integer.valueOf(keySplitted[0]));
					String dateAsString = keySplitted[1];

					if (userWiseTimelines.containsKey(userID))
					{
						userWiseTimelines.get(userID).put(dateAsString, t.getValue());
						// HERE
					}
					else
					{
						LinkedHashMap<String, Timeline> aTimelineForThisUser = new LinkedHashMap<>();
						aTimelineForThisUser.put(dateAsString, t.getValue());
						userWiseTimelines.put(userID, aTimelineForThisUser);
					}
				}
			}

			// start of sanity check
			int numOfGivenTimelines = givenTimelinesWithUserIDInKey.size();
			int numOfReturnedTimeslines = 0;// userWiseTimelines.entrySet().stream().map(e->e.getValue().size()).
			for (Entry<String, LinkedHashMap<String, Timeline>> temp : userWiseTimelines.entrySet())
			{
				numOfReturnedTimeslines += temp.getValue().size();
			}
			System.out.println("In toUserwiseTimelines(): numOfGivenTimelines:" + numOfGivenTimelines
					+ "\t numOfReturnedTimeslines:" + numOfReturnedTimeslines);
			Sanity.eq(numOfGivenTimelines, numOfReturnedTimeslines, "numOfGivenTimelines:" + numOfGivenTimelines
					+ " != numOfReturnedTimeslines:" + numOfReturnedTimeslines);
			// end of sanity check
			if (numOfGivenTimelines != numOfReturnedTimeslines)
			{
				PopUps.printTracedErrorMsgWithExit("numOfGivenTimelines:" + numOfGivenTimelines
						+ " != numOfReturnedTimeslines:" + numOfReturnedTimeslines);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return userWiseTimelines;
	}

	/**
	 * Returns list of unqiue dates in the timeline (from start timestamps of activity objects in timeline)
	 * 
	 * @param candidateTimelinesAsOne
	 * @return
	 */
	public static LinkedHashSet<LocalDate> getUniqueDates(Timeline givenTimeline, boolean verbose)
	{
		LinkedHashSet<LocalDate> uniqueDates = new LinkedHashSet<>();
		ArrayList<ActivityObject2018> aosInTimeline = givenTimeline.getActivityObjectsInTimeline();

		for (ActivityObject2018 ao : aosInTimeline)
		{
			uniqueDates.add(Instant.ofEpochMilli(ao.getStartTimestampInms()).atZone(Constant.getTimeZone().toZoneId())
					.toLocalDate());
		}

		if (verbose)
		{
			System.out.println("\nInside getUniqueDates.\n givenTimeline=\n");
			givenTimeline.printActivityObjectNamesWithTimestampsInSequence("\n");
			System.out.println("\nuniqueDates:\n");
			uniqueDates.stream().forEachOrdered(d -> System.out.print(d.toString() + "\t"));
		}
		return uniqueDates;
	}

	/**
	 * Returns list of unqiue dates in the timeline (from start timestamps of activity objects in timeline)
	 * 
	 * @param candidateTimelinesAsOne
	 * @return
	 */
	public static LinkedHashSet<LocalDate> getUniqueDates2(LinkedHashMap<String, Timeline> candidateTimelines,
			boolean verbose)
	{
		LinkedHashSet<LocalDate> uniqueDates = new LinkedHashSet<>();

		for (Entry<String, Timeline> cand : candidateTimelines.entrySet())
		{
			for (ActivityObject2018 ao : cand.getValue().getActivityObjectsInTimeline())
			{
				uniqueDates.add(Instant.ofEpochMilli(ao.getStartTimestampInms())
						.atZone(Constant.getTimeZone().toZoneId()).toLocalDate());
			}
		}
		if (verbose)
		{
			System.out.println("\nInside getUniqueDates.\n givenTimeline=\n");
			for (Entry<String, Timeline> cand : candidateTimelines.entrySet())
			{
				cand.getValue().printActivityObjectNamesWithTimestampsInSequence("\n");
			}

			System.out.println("\nuniqueDates:\n");
			uniqueDates.stream().forEachOrdered(d -> System.out.print(d.toString() + "\t"));
		}
		return uniqueDates;
	}

	/**
	 * Extract unique location IDs from the given timelines
	 * <p>
	 * For daywise timelines as input
	 * 
	 * @param usersCleanedDayTimelines
	 * @param labelPhrase
	 * @return
	 */
	public static TreeSet<Integer> getUniqueLocIDs(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean write,
			String labelPhrase)
	{
		TreeSet<Integer> uniqueLocIDs = new TreeSet<>();
		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
				{
					dateEntry.getValue().getActivityObjectsInTimeline().stream()
							.forEach(ao -> uniqueLocIDs.addAll(ao.getLocationIDs()));
				}
			}
			System.out.println("Inside getUniqueLocIDs: uniqueLocIDs.size()=" + uniqueLocIDs.size());
			if (write)
			{
				// WritingToFile.writeToNewFile(uniqueLocIDs.toString(), );
				WToFile.writeToNewFile(
						uniqueLocIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")).toString(),
						Constant.getCommonPath() + labelPhrase + "UniqueLocIDs.csv");// );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniqueLocIDs;
	}

	/**
	 * Extract unique given dimension from the given timelines
	 * <p>
	 * For daywise timelines as input
	 * 
	 * @param usersCleanedDayTimelines
	 * @param labelPhrase
	 * @return
	 */
	public static TreeSet<Integer> getUniqueGivenDimensionVals(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean write,
			String labelPhrase, PrimaryDimension givenDimension)
	{
		TreeSet<Integer> uniqueGivenDimensionVals = new TreeSet<>();
		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
				{
					dateEntry.getValue().getActivityObjectsInTimeline().stream()
							.forEach(ao -> uniqueGivenDimensionVals.addAll(ao.getGivenDimensionVal(givenDimension)));
				}
			}
			System.out.println("Inside givenDimension = " + givenDimension
					+ ", getUniqueGivenDimensionVals: uniqueGivenDimensionVals.size()="
					+ uniqueGivenDimensionVals.size());
			if (write)
			{
				// WritingToFile.writeToNewFile(uniqueLocIDs.toString(), );
				WToFile.writeToNewFile(
						uniqueGivenDimensionVals.stream().map(e -> e.toString()).collect(Collectors.joining("\n"))
								.toString(),
						Constant.getCommonPath() + labelPhrase + "Unique" + givenDimension + "Vals.csv");// );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniqueGivenDimensionVals;
	}

	/**
	 * Extract unique location IDs per actID from the given timelines
	 * 
	 * @param usersCleanedDayTimelines
	 * @param labelPhrase
	 * @return
	 */
	public static TreeMap<Integer, TreeSet<Integer>> getUniqueLocIDsPerActID(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean write,
			String labelPhrase)
	{
		// map of <act id, <list of locations>>
		TreeMap<Integer, TreeSet<Integer>> actIDLocIDsMap = new TreeMap<>();

		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
				{
					Timeline timelineForThisDay = dateEntry.getValue();

					for (ActivityObject2018 ao : timelineForThisDay.getActivityObjectsInTimeline())
					{
						TreeSet<Integer> locationIDsForThisActID = actIDLocIDsMap.get(ao.getActivityID());
						if (locationIDsForThisActID == null)
						{
							locationIDsForThisActID = new TreeSet<>();
						}
						locationIDsForThisActID.addAll(ao.getLocationIDs());
						actIDLocIDsMap.put(ao.getActivityID(), locationIDsForThisActID);
					}
				}
			}

			if (write)
			{
				StringBuilder sb = new StringBuilder("actID,NumOfLocIDs,listOfLocIDs\n");
				for (Entry<Integer, TreeSet<Integer>> actEntry : actIDLocIDsMap.entrySet())
				{
					sb.append(actEntry.getKey() + "," + actEntry.getValue().size());
					actEntry.getValue().stream().forEachOrdered(l -> sb.append("," + l));
					sb.append("\n");
					// actEntry.getValue().stream().collect(Collectors.joining(","));
					// String.join(",", actEntry.getValue());
					// sb.append(actEntry.getValue().stream().collect(Collectors.joining(",")) + "\n");
				}
				WToFile.writeToNewFile(sb.toString(),
						Constant.getCommonPath() + labelPhrase + "UniqueLocIDsPerActID.csv");// );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return actIDLocIDsMap;
	}

	/**
	 * Extract unique location IDs per actID from the given timelines
	 * 
	 * @param usersCleanedDayTimelines
	 * @param labelPhrase
	 * @return
	 */
	public static TreeMap<Integer, TreeSet<Integer>> getUniqueLocGridIDsPerActID(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean write,
			String labelPhrase)
	{
		// map of <act id, <list of locations>>
		TreeMap<Integer, TreeSet<Integer>> actIDLocIDsMap = new TreeMap<>();

		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
				{
					Timeline timelineForThisDay = dateEntry.getValue();

					for (ActivityObject2018 ao : timelineForThisDay.getActivityObjectsInTimeline())
					{
						TreeSet<Integer> locationIDsForThisActID = actIDLocIDsMap.get(ao.getActivityID());
						if (locationIDsForThisActID == null)
						{
							locationIDsForThisActID = new TreeSet<>();
						}
						locationIDsForThisActID.addAll(ao.getGivenDimensionVal(PrimaryDimension.LocationGridID));// .getLocationIDs());
						actIDLocIDsMap.put(ao.getActivityID(), locationIDsForThisActID);
					}
				}
			}

			if (write)
			{
				StringBuilder sb = new StringBuilder("actID,NumOfLocGridIDs,listOfLocGridIDs\n");
				for (Entry<Integer, TreeSet<Integer>> actEntry : actIDLocIDsMap.entrySet())
				{
					sb.append(actEntry.getKey() + "," + actEntry.getValue().size());
					actEntry.getValue().stream().forEachOrdered(l -> sb.append("," + l));
					sb.append("\n");
					// actEntry.getValue().stream().collect(Collectors.joining(","));
					// String.join(",", actEntry.getValue());
					// sb.append(actEntry.getValue().stream().collect(Collectors.joining(",")) + "\n");
				}
				WToFile.writeToNewFile(sb.toString(),
						Constant.getCommonPath() + labelPhrase + "actIDLocGridIDsMap.csv");// );
			}
			Serializer.kryoSerializeThis(actIDLocIDsMap, Constant.getCommonPath() + "actIDLocGridIDsMap.kryo");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return actIDLocIDsMap;
	}

	// /**
	// * Extract unique location IDs per actID from the given timelines
	// *
	// * @param usersCleanedDayTimelines
	// * @param labelPhrase
	// * @return
	// */
	// public static TreeMap<Integer, TreeSet<Integer>> getUniqueActIDsPerLocGridID(
	// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean write,
	// String labelPhrase)
	// {
	// PrimaryDimension perGivenDimension = PrimaryDimension.LocationGridID;
	// // PrimaryDimension targetDimension = PrimaryDimension.ActivityID;
	// // map of <act id, <list of locations>>
	// TreeMap<Integer, TreeSet<Integer>> actIDLocGridIDsMap = new TreeMap<>();
	//
	// try
	// {
	// for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
	// {
	// for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
	// {
	// Timeline timelineForThisDay = dateEntry.getValue();
	//
	// for (ActivityObject ao : timelineForThisDay.getActivityObjectsInTimeline())
	// {
	// int actID = ao.getActivityID();
	//
	// for (Integer locGridID : ao.getGivenDimensionVal(perGivenDimension))
	// {
	// // TreeSet<Integer> locationIDsForThisActID = actIDLocGridIDsMap.get(ao.getActivityID());
	// TreeSet<Integer> actIDsForThisLocationID = actIDLocGridIDsMap.get(ao.getActivityID());
	//
	// if (locationIDsForThisActID == null)
	// {
	// locationIDsForThisActID = new TreeSet<>();
	// }
	// locationIDsForThisActID.addAll(ao.getLocationIDs());
	// actIDLocGridIDsMap.put(ao.getActivityID(), locationIDsForThisActID);
	// }
	// }
	// }
	// }
	//
	// if (write)
	// {
	// StringBuilder sb = new StringBuilder("actID,NumOfLocIDs,listOfLocIDs\n");
	// for (Entry<Integer, TreeSet<Integer>> actEntry : actIDLocGridIDsMap.entrySet())
	// {
	// sb.append(actEntry.getKey() + "," + actEntry.getValue().size());
	// actEntry.getValue().stream().forEachOrdered(l -> sb.append("," + l));
	// sb.append("\n");
	// // actEntry.getValue().stream().collect(Collectors.joining(","));
	// // String.join(",", actEntry.getValue());
	// // sb.append(actEntry.getValue().stream().collect(Collectors.joining(",")) + "\n");
	// }
	// WToFile.writeToNewFile(sb.toString(),
	// Constant.getCommonPath() + labelPhrase + "UniqueLocIDsPerActID.csv");// );
	// }
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// return actIDLocGridIDsMap;
	// }

	//
	/**
	 * Extract {userID,{unique actIDs for this user, {unique locIDs for this actID for this userID}}}
	 * 
	 * @param usersCleanedDayTimelines
	 * @param labelPhrase
	 * @return
	 * @since May 24 2018
	 */
	public static TreeMap<String, TreeMap<Integer, LinkedHashSet<Integer>>> getUserIDActIDLocIDMap(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean write,
			String labelPhrase)
	{
		// map of {userID,{unique actIDs for this user, {unique locIDs for this actID for this userID}}}
		TreeMap<String, TreeMap<Integer, LinkedHashSet<Integer>>> userIDActIDLocIDsMap = new TreeMap<>();

		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				String userID = userEntry.getKey();

				TreeMap<Integer, LinkedHashSet<Integer>> actIDsLocIDsForThisUser = new TreeMap<>();

				for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
				{
					Timeline timelineForThisDay = dateEntry.getValue();

					for (ActivityObject2018 ao : timelineForThisDay.getActivityObjectsInTimeline())
					{
						Integer actID = ao.getActivityID();
						LinkedHashSet<Integer> locIDsForThisActIDForThisUser = actIDsLocIDsForThisUser.get(actID);

						if (locIDsForThisActIDForThisUser == null)
						{
							locIDsForThisActIDForThisUser = new LinkedHashSet<>();
						}

						locIDsForThisActIDForThisUser.addAll(ao.getLocationIDs());

						actIDsLocIDsForThisUser.put(actID, locIDsForThisActIDForThisUser);
					}
				} // end of loop over date entries for this user
				userIDActIDLocIDsMap.put(userID, actIDsLocIDsForThisUser);
			}

			if (write)
			{
				StringBuilder sb = new StringBuilder("UserID,ActID,NumOfLocIDs,ListOfLocIDs\n");
				for (Entry<String, TreeMap<Integer, LinkedHashSet<Integer>>> userEntry : userIDActIDLocIDsMap
						.entrySet())
				{
					String userID = userEntry.getKey();

					for (Entry<Integer, LinkedHashSet<Integer>> actEntry : userEntry.getValue().entrySet())
					{
						sb.append(userID + "," + actEntry.getKey() + "," + actEntry.getValue().size() + "," + actEntry
								.getValue().stream().map(i -> String.valueOf(i)).collect(Collectors.joining(","))
								+ "\n");
					}
				}
				WToFile.writeToNewFile(sb.toString(),
						Constant.getCommonPath() + labelPhrase + "UserIDActIDLocIDsMap.csv");// );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return userIDActIDLocIDsMap;
	}

	//
	/**
	 * Extract unique location IDs from the given timelines.
	 * <p>
	 * For continous timelines as input
	 * 
	 * @param usersCleanedDayTimelines
	 * @param write
	 * @param filenamePhrase
	 * @return
	 * @since 22 Feb 2018
	 */
	public static Set<Integer> getUniqueLocIDs(Map<String, Timeline> usersCleanedDayTimelines, boolean write,
			String absFileNameToWrite)
	{
		TreeSet<Integer> uniqueLocIDs = new TreeSet<>();
		try
		{
			for (Entry<String, Timeline> e : usersCleanedDayTimelines.entrySet())
			{

				e.getValue().getActivityObjectsInTimeline().stream()
						.forEach(ao -> uniqueLocIDs.addAll(ao.getLocationIDs()));
			}
			System.out.println("Inside getUniqueLocIDs: uniqueLocIDs.size()=" + uniqueLocIDs.size());
			if (write)
			{
				// WritingToFile.writeToNewFile(uniqueLocIDs.toString(), absFileNameToWrite);// );
				WToFile.writeToNewFile(
						uniqueLocIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")).toString(),
						absFileNameToWrite);// );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniqueLocIDs;
	}

	/**
	 * 
	 * @param trainTestTimelinesForAllUsersDW
	 * @param write
	 * @param absFileNameToWrite
	 * @return
	 * @since 22 Feb 2018
	 */
	public static Set<Integer> getUniqueLocIDsFromTestOnly(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW, boolean write,
			String absFileNameToWrite)
	{
		TreeSet<Integer> uniqueLocIDs = new TreeSet<>();
		try
		{
			for (Entry<String, List<LinkedHashMap<Date, Timeline>>> e : trainTestTimelinesForAllUsersDW.entrySet())
			{
				for (Entry<Date, Timeline> e2 : e.getValue().get(1).entrySet())
				{
					e2.getValue().getActivityObjectsInTimeline().stream()
							.forEach(ao -> uniqueLocIDs.addAll(ao.getLocationIDs()));
				}
			}
			System.out.println("Inside getUniqueLocIDs: uniqueLocIDs.size()=" + uniqueLocIDs.size());
			if (write)
			{
				// /WritingToFile.writeToNewFile(uniqueLocIDs.toString(), absFileNameToWrite);
				WToFile.writeToNewFile(
						uniqueLocIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")).toString(),
						absFileNameToWrite);// );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniqueLocIDs;
	}

	/**
	 * Extract unique location IDs from the given timelines
	 * 
	 * @param usersCleanedDayTimelines
	 * @return
	 */
	public static void countNumOfMultipleLocationIDs(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines)
	{

		int numOfSingleLocID = 0, numOfMultipleLocIDs = 0, numOfAOs = 0;
		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> e : usersCleanedDayTimelines.entrySet())
			{
				for (Entry<Date, Timeline> e2 : e.getValue().entrySet())
				{
					for (ActivityObject2018 ao : e2.getValue().getActivityObjectsInTimeline())
					{
						numOfAOs += 1;
						if (ao.getLocationIDs().size() > 1)
						{
							numOfMultipleLocIDs += 1;
						}
						else if (ao.getLocationIDs().size() == 1)
						{
							numOfSingleLocID += 1;
						}
						else
						{
							PopUps.printTracedErrorMsg("(ao.getLocationIDs().size() = " + ao.getLocationIDs().size());
						}
					}

				}
			}
			System.out.println("Inside countNumOfMultipleLocationIDs: \nnumOfAOs\t\t=" + numOfAOs
					+ "\nnumOfSingleLocID\t=" + numOfSingleLocID + "\nnumOfMultipleLocIDs\t=" + numOfMultipleLocIDs);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// return uniqueLocIDs;
	}

	/**
	 * Extract unique activity IDs from the given timelines
	 * 
	 * @param usersCleanedDayTimelines
	 * @param write
	 * @param labelPhrase
	 * @return
	 */
	public static TreeSet<Integer> getUniqueActivityIDs(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean write,
			String labelPhrase)
	{
		TreeSet<Integer> uniqueActIDs = new TreeSet<>();
		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayTimelines.entrySet())
			{
				for (Entry<Date, Timeline> dayTimelineEntry : userEntry.getValue().entrySet())
				{
					dayTimelineEntry.getValue().getActivityObjectsInTimeline().stream()
							.forEach(ao -> uniqueActIDs.add(ao.getActivityID()));
				}
			}
			System.out.println("Inside getUniqueActivityIDs: uniqueActIDs.size()=" + uniqueActIDs.size());

			if (write)
			{
				WToFile.writeToNewFile(
						uniqueActIDs.stream().map(i -> String.valueOf(i)).collect(Collectors.joining("\n")),
						Constant.getCommonPath() + labelPhrase + "UniqueActIDs.csv");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniqueActIDs;
	}

	/**
	 * Extract unique activity IDs from the given timeline
	 * 
	 * @param givenTimeline
	 * @return
	 */
	public static Set<Integer> getUniqueActIDsInTimeline(Timeline givenTimeline)
	{
		return givenTimeline.getActivityObjectsInTimeline().stream().map(ao -> ao.getActivityID())
				.collect(Collectors.toSet());
	}

	/**
	 * Extract unique PD Vals per user
	 * 
	 * @param usersCleanedDayTimelines
	 * @param writeToFile
	 * @param labelPhrase
	 * @return
	 */
	public static LinkedHashMap<String, TreeSet<Integer>> getUniquePDValPerUser(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean writeToFile,
			String labelPhrase)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("User,NumOfUniquePDVals,UniquePDVals\n");
		LinkedHashMap<String, TreeSet<Integer>> uniquePDValsPerUser = new LinkedHashMap<>();
		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> e : usersCleanedDayTimelines.entrySet())
			{
				String user = e.getKey();
				TreeSet<Integer> uniquePDValsForThisUser = new TreeSet<>();
				for (Entry<Date, Timeline> e2 : e.getValue().entrySet())
				{
					e2.getValue().getActivityObjectsInTimeline().stream()
							.forEach(ao -> uniquePDValsForThisUser.addAll(ao.getPrimaryDimensionVal()));
				}
				uniquePDValsPerUser.put(user, uniquePDValsForThisUser);

				sb.append(user + "," + uniquePDValsForThisUser.size() + ","
						+ uniquePDValsForThisUser.stream().map(i -> String.valueOf(i)).collect(Collectors.joining(","))
						+ "\n");
			}
			// System.out.println("Inside getUniqueActivityIDs: uniqueActIDs.size()=" + uniqueActIDs.size());

			if (writeToFile)
			{
				WToFile.writeToNewFile(sb.toString(),
						Constant.getCommonPath() + labelPhrase + "UniquePDValsPerUser.csv");// "NumOfUniquePDValPerUser.csv");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniquePDValsPerUser;
	}

	/**
	 * Extract unique given dimension Vals per user
	 * 
	 * @param usersCleanedDayTimelines
	 * @param writeToFile
	 * @param labelPhrase
	 * @param givenDimension
	 * @return
	 * @since 18 July 2018
	 */
	public static LinkedHashMap<String, TreeSet<Integer>> getUniqueGivenDimensionValPerUser(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean writeToFile,
			String labelPhrase, PrimaryDimension givenDimension)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("User,NumOfUnique" + givenDimension + "Vals,UniqueGDVals\n");
		LinkedHashMap<String, TreeSet<Integer>> uniqueGDValsPerUser = new LinkedHashMap<>();

		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> e : usersCleanedDayTimelines.entrySet())
			{
				String user = e.getKey();
				TreeSet<Integer> uniqueGDValsForThisUser = new TreeSet<>();
				for (Entry<Date, Timeline> e2 : e.getValue().entrySet())
				{
					e2.getValue().getActivityObjectsInTimeline().stream()
							.forEach(ao -> uniqueGDValsForThisUser.addAll(ao.getGivenDimensionVal(givenDimension)));
				}
				uniqueGDValsPerUser.put(user, uniqueGDValsForThisUser);

				sb.append(user + "," + uniqueGDValsForThisUser.size() + ","
						+ uniqueGDValsForThisUser.stream().map(i -> String.valueOf(i)).collect(Collectors.joining(","))
						+ "\n");
			}
			// System.out.println("Inside getUniqueActivityIDs: uniqueActIDs.size()=" + uniqueActIDs.size());

			if (writeToFile)
			{
				WToFile.writeToNewFile(sb.toString(),
						Constant.getCommonPath() + labelPhrase + "Unique" + givenDimension + "ValsPerUser.csv");// "NumOfUniquePDValPerUser.csv");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniqueGDValsPerUser;
	}

	/**
	 * Get count for each unique given dimension val per user
	 * <p>
	 * This can be extended to get the count per user per day
	 * 
	 * @param usersCleanedDayTimelines
	 * @param writeToFile
	 * @param labelPhrase
	 * @param givenDimension
	 * @param uniqueGivenDimenalVals
	 * @return
	 * @since 18 July 2018
	 */
	public static LinkedHashMap<String, TreeMap<Integer, Integer>> getGivenDimensionValCountPerUser(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean writeToFile,
			String labelPhrase, PrimaryDimension givenDimension, TreeSet<Integer> uniqueGivenDimenalVals)
	{
		// StringBuilder sb = new StringBuilder();
		// sb.append("User,NumOfUnique" + givenDimension + "Vals,UniqueGDVals\n");
		// {user, {GDVal,count}}
		LinkedHashMap<String, TreeMap<Integer, Integer>> GDValCountPerUser = new LinkedHashMap<>();
		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> e : usersCleanedDayTimelines.entrySet())
			{
				String user = e.getKey();

				// if new user
				if (GDValCountPerUser.containsKey(user) == false)
				{// intialise with empty count for each unique value of given dimension
					// Map<Integer, Integer> GDValsCountForThisUser = uniqueGivenDimenalVals.stream()
					// .collect(Collectors.toMap(k -> Integer.valueOf(k), k -> Integer.valueOf(0)));
					TreeMap<Integer, Integer> GDValsCountForThisUser = uniqueGivenDimenalVals.stream()
							.collect(Collectors.toMap(k -> Integer.valueOf(k), k -> Integer.valueOf(0),
									(oldVal, newVal) -> newVal, TreeMap::new));

					GDValCountPerUser.put(user, GDValsCountForThisUser);
				}

				for (Entry<Date, Timeline> dayEntry : e.getValue().entrySet())
				{
					for (ActivityObject2018 ao : dayEntry.getValue().getActivityObjectsInTimeline())
					{
						ArrayList<Integer> gdVals = ao.getGivenDimensionVal(givenDimension);

						for (Integer gdVal : gdVals)
						{
							Integer oldVal = GDValCountPerUser.get(user).get(gdVal);
							GDValCountPerUser.get(user).put(gdVal, oldVal + 1);
						}
					}
				}
			}

			if (writeToFile)
			{
				String absFileNameToWrite = Constant.getCommonPath() + labelPhrase + givenDimension
						+ "ValsCountPerUser.csv";// "NumOfUniquePDValPerUser.csv");
				StringBuilder sb = new StringBuilder("user");
				uniqueGivenDimenalVals.stream().forEachOrdered(v -> sb.append("," + String.valueOf(v)));
				sb.append("\n");

				for (Entry<String, TreeMap<Integer, Integer>> entry : GDValCountPerUser.entrySet())
				{
					sb.append(entry.getKey());
					uniqueGivenDimenalVals.stream()
							.forEachOrdered(v -> sb.append("," + String.valueOf(entry.getValue().get(v))));
					sb.append("\n");
				}
				WToFile.writeToNewFile(sb.toString(), absFileNameToWrite);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return GDValCountPerUser;
	}

	/**
	 * Convert Date keys to String keys with userID appended i.e. new key = userID_(DateAsString)
	 * 
	 * @param trainTimelinesDWForAllExceptCurrUser
	 * @return
	 * @since 27 Dec 2018
	 */
	public static LinkedHashMap<String, Timeline> toTogetherWithUserIDStringKeys(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> mapOfMapWithDateKeys)
	{
		LinkedHashMap<String, Timeline> res = new LinkedHashMap<>();
		int expectedSize = 0;

		for (Entry<String, LinkedHashMap<Date, Timeline>> outerEntry : mapOfMapWithDateKeys.entrySet())
		{
			String userID = outerEntry.getKey();
			expectedSize += outerEntry.getValue().size();

			for (Entry<Date, Timeline> innerE : outerEntry.getValue().entrySet())
			{
				res.put(userID + "_" + innerE.getKey().toString(), innerE.getValue());
			}
		}
		Sanity.eq(expectedSize, res.size(), "Error in toTogetherWithUserIDStringKeys(): expectedSize = " + expectedSize
				+ " while res.size() = " + res.size());
		return res;
	}

	/**
	 * Convert Date keys to String keys with userID appended i.e. new key = userID_(DateAsString)
	 * 
	 * @param trainTimelinesDWForAllExceptCurrUser
	 * @return
	 * @since 27 Dec 2018
	 */
	public static LinkedHashMap<String, Timeline> toStringKeys(LinkedHashMap<Date, Timeline> mapWithDateKeys)
	{
		LinkedHashMap<String, Timeline> res = new LinkedHashMap<>();
		for (Entry<Date, Timeline> innerE : mapWithDateKeys.entrySet())
		{
			res.put(innerE.getKey().toString(), innerE.getValue());
		}
		Sanity.eq(mapWithDateKeys.size(), res.size(), "Error in toStringKeys(): mapWithDateKeys.size() = "
				+ mapWithDateKeys.size() + " while res.size() = " + res.size());
		return res;
	}

}
/////////////// UNUSED CODE

// DISABLED BECAUSE AFTER THE OCT 2016 CRASH, I COULD NOT FIND BACK THE CLASS CheckinActivityObject
// /**
// *
// * @param allActivityObjects
// * @return
// */
// public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>
// createUserTimelinesFromCheckinActivityObjects(
// LinkedHashMap<String, TreeMap<Timestamp, CheckinActivityObject>> allActivityObjects)
// {
// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines =
// new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
// // userid, usertimeline
// LinkedHashMap<String, ArrayList<CheckinActivityObject>> perUserActivityEvents =
// new LinkedHashMap<String, ArrayList<CheckinActivityObject>>();
//
// System.out.println("inside createUserTimelinesFromCheckinActivityObjects");
//
// for (Entry<String, TreeMap<Timestamp, CheckinActivityObject>> perUserActObjEntry : allActivityObjects.entrySet())
// // Iterate over Users
// {
// String userID = perUserActObjEntry.getKey();
//
// System.out.println("for user:" + userID + " number of activity-objects =" +
// perUserActObjEntry.getValue().size());
//
// LinkedHashMap<Date, ArrayList<CheckinActivityObject>> perDateActObjsForThisUser =
// new LinkedHashMap<Date, ArrayList<CheckinActivityObject>>();
//
// TreeMap<Timestamp, CheckinActivityObject> actObjsForThisUserByTS = perUserActObjEntry.getValue();
//
// for (Entry<Timestamp, CheckinActivityObject> perTSActObjEntry : actObjsForThisUserByTS.entrySet()) // Iterate
// over Users
// {
// Date date = new Date(perTSActObjEntry.getKey().getTime());// (Date)
// actObjsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension", "Date"); // start date
//
// if (!(perDateActObjsForThisUser.containsKey(date)))
// {
// perDateActObjsForThisUser.put(date, new ArrayList<CheckinActivityObject>());
// }
//
// perDateActObjsForThisUser.get(date).add(perTSActObjEntry.getValue());
// }
//
// // perDateActivityEventsForThisUser has been created now.
//
// LinkedHashMap<Date, UserDayTimeline> dayTimelines = new LinkedHashMap<Date, UserDayTimeline>();
//
// for (Map.Entry<Date, ArrayList<CheckinActivityObject>> perDateActivityEventsForThisUserEntry :
// perDateActObjsForThisUser
// .entrySet())
// {
// Date date = perDateActivityEventsForThisUserEntry.getKey();
//
// /// todo dayTimelines.put(date, new UserDayTimeline(perDateActivityEventsForThisUserEntry.getValue(), date));
//
// }
//
// userTimelines.put(userID, dayTimelines);
//
// }
//
// System.out.println("exiting createUserTimelinesFromActivityEvents");
// return userTimelines;
// }

/// **
// *
// * @param usersTimelines
// * @return LinkedHashMap<User ID as String, Timeline of the user with user id as integer as timeline id>
// */
// public static LinkedHashMap<String, Timeline> dayTimelinesToTimelines(
// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
// {
// LinkedHashMap<String, Timeline> timelines = new LinkedHashMap<String, Timeline>();
//
// if (usersTimelines.size() == 0 || usersTimelines == null)
// {
// System.err.println(PopUps.getCurrentStackTracedErrorMsg(
// "Error in org.activity.util.UtilityBelt.dayTimelinesToTimelines(LinkedHashMap<String, LinkedHashMap<Date,
/// UserDayTimeline>>): userTimeline.size = "
// + usersTimelines.size()));
// }
//
// for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : usersTimelines.entrySet())
// {
//
// timelines.put(entry.getKey(), new Timeline(entry.getValue(), Integer.valueOf(entry.getKey())));
//
// timelines.put(entry.getKey(), new Timeline(entry.getValue(), Integer.valueOf(entry.getKey())));
// }
// return timelines;
// }

// private void compareTimelines(String serialisedTimelines1, String serialisedTimelines2)
// {
// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelinesOriginal1 =
// (LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>) Serializer.deSerializeThis(serialisedTimelines1);
// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelinesOriginal2 =
// (LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>) Serializer.deSerializeThis(serialisedTimelines2);
//
// StringBuffer s = new StringBuffer("Comparing " + serialisedTimelines1 + " and " + serialisedTimelines2 + "\n");
//
// if (usersDayTimelinesOriginal1.size() == usersDayTimelinesOriginal2.size())
// {
// s.append("Num of users: same " + usersDayTimelinesOriginal1.size() + " = " + usersDayTimelinesOriginal2.size());
//
// for
// }
// else
// {
// s.append("Num of users: different " + usersDayTimelinesOriginal1.size() + " = " +
// usersDayTimelinesOriginal2.size());
// }
//
//
// WritingToFile.appendLineToFileAbsolute(s.toString(), Constant.getCommonPath() + "ComparingTimelines.txt");// ,
// fullPathfileNameToUse);
// }
