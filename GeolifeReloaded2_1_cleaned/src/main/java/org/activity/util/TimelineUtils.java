package org.activity.util;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.activity.constants.VerbosityConstants;
import org.activity.distances.HJEditDistance;
import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.CheckinEntry;
import org.activity.objects.Dimension;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.objects.Triple;
import org.activity.objects.UserGowalla;
import org.activity.sanityChecks.Sanity;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.json.JSONArray;

/**
 */
public class TimelineUtils
{

	/**
	 * Num of activities with distinct activity names
	 * 
	 * @return
	 */
	public static int countNumberOfDistinctActivities(ArrayList<ActivityObject> ActivityObjects)
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

	/**
	 * For sanity checking convertTimewiseMapToDatewiseMap()
	 * 
	 * @param dft
	 *            timezone to use
	 */
	public static void checkConvertTimewiseMapToDatewiseMap2(TimeZone tz)
	{
		LinkedHashMap<String, TreeMap<Timestamp, Integer>> timewiseMap = new LinkedHashMap<>();
		LinkedHashMap<String, TreeMap<Date, ArrayList<Integer>>> dateWiseMap = new LinkedHashMap<>();

		for (int i = 0; i <= 5; i++)
		{
			TreeMap<Timestamp, Integer> timeMap = new TreeMap<>();

			Instant instant = Instant.now();
			Timestamp ts = Timestamp.from(instant);

			for (int j = 0; j <= 6; j++)
			{
				timeMap.put(new Timestamp(ts.getTime() + (j * 60000000)), j);
			}
			timewiseMap.put("User" + i, timeMap);
		}

		for (Entry<String, TreeMap<Timestamp, Integer>> userW : timewiseMap.entrySet())
		{
			System.out.println("timewise:\nUser = " + userW.getKey());

			for (Entry<Timestamp, Integer> timeW : userW.getValue().entrySet())
			{
				System.out.println(" timew entry:" + timeW.getKey() + "-" + timeW.getValue());
			}
		}

		dateWiseMap = convertTimewiseMapToDatewiseMap2(timewiseMap, tz);

		for (Entry<String, TreeMap<Date, ArrayList<Integer>>> userW : dateWiseMap.entrySet())
		{
			System.out.println("Datewise:\nUser = " + userW.getKey());

			for (Entry<Date, ArrayList<Integer>> timeW : userW.getValue().entrySet())
			{
				System.out.println("Date = " + timeW.getKey().toString());
				// System.out.println("Date = " + timeW.getKey().toGMTString() + " " + timeW.getKey().toLocaleString());
				System.out.println(" datew entry:" + timeW.getKey() + "-" + timeW.getValue().toString());
			}
		}

	}

	/**
	 * For sanity checking convertTimewiseMapToDatewiseMap()
	 * 
	 * @param dft
	 *            timezone to use
	 */
	public static void checkConvertTimewiseMapToDatewiseMap(TimeZone dft)
	{
		LinkedHashMap<String, TreeMap<Timestamp, Integer>> timewiseMap = new LinkedHashMap<>();
		LinkedHashMap<String, TreeMap<LocalDate, ArrayList<Integer>>> dateWiseMap = new LinkedHashMap<>();

		for (int i = 0; i <= 5; i++)
		{
			TreeMap<Timestamp, Integer> timeMap = new TreeMap<>();

			Instant instant = Instant.now();
			Timestamp ts = Timestamp.from(instant);

			for (int j = 0; j <= 6; j++)
			{
				timeMap.put(new Timestamp(ts.getTime() + (j * 60000000)), j);
			}
			timewiseMap.put("User" + i, timeMap);
		}

		for (Entry<String, TreeMap<Timestamp, Integer>> userW : timewiseMap.entrySet())
		{
			System.out.println("timewise:\nUser = " + userW.getKey());

			for (Entry<Timestamp, Integer> timeW : userW.getValue().entrySet())
			{
				System.out.println(" timew entry:" + timeW.getKey() + "-" + timeW.getValue());
			}
		}

		dateWiseMap = convertTimewiseMapToDatewiseMap(timewiseMap, dft);

		for (Entry<String, TreeMap<LocalDate, ArrayList<Integer>>> userW : dateWiseMap.entrySet())
		{
			System.out.println("Datewise:\nUser = " + userW.getKey());

			for (Entry<LocalDate, ArrayList<Integer>> timeW : userW.getValue().entrySet())
			{
				System.out.println("Date = " + timeW.getKey().toString());
				// System.out.println("Date = " + timeW.getKey().toGMTString() + " " + timeW.getKey().toLocaleString());
				System.out.println(" datew entry:" + timeW.getKey() + "-" + timeW.getValue().toString());
			}
		}

	}

	/**
	 * For sanity checking convertTimewiseMapToDatewiseMap()
	 * 
	 * @param dft
	 *            timezone to use
	 */
	public static void checkConvertTimewiseMapToDatewiseMap()
	{
		LinkedHashMap<String, TreeMap<Timestamp, Integer>> timewiseMap = new LinkedHashMap<>();
		LinkedHashMap<String, TreeMap<Date, ArrayList<Integer>>> dateWiseMap = new LinkedHashMap<>();

		for (int i = 0; i <= 5; i++)
		{
			TreeMap<Timestamp, Integer> timeMap = new TreeMap<>();

			Instant instant = Instant.now();
			Timestamp ts = Timestamp.from(instant);

			for (int j = 0; j <= 6; j++)
			{
				timeMap.put(new Timestamp(ts.getTime() + (j * 60000000)), j);
			}
			timewiseMap.put("User" + i, timeMap);
		}

		for (Entry<String, TreeMap<Timestamp, Integer>> userW : timewiseMap.entrySet())
		{
			System.out.println("timewise:\nUser = " + userW.getKey());

			for (Entry<Timestamp, Integer> timeW : userW.getValue().entrySet())
			{
				System.out.println(" timew entry:" + timeW.getKey() + "-" + timeW.getValue());
			}
		}

		dateWiseMap = convertTimewiseMapToDatewiseMap(timewiseMap);

		for (Entry<String, TreeMap<Date, ArrayList<Integer>>> userW : dateWiseMap.entrySet())
		{
			System.out.println("Datewise:\nUser = " + userW.getKey());

			for (Entry<Date, ArrayList<Integer>> timeW : userW.getValue().entrySet())
			{
				System.out.println("Date = " + timeW.getKey().toString());
				// System.out.println("Date = " + timeW.getKey().toGMTString() + " " + timeW.getKey().toLocaleString());
				System.out.println(" datew entry:" + timeW.getKey() + "-" + timeW.getValue().toString());
			}
		}

	}

	// public static String traverserMapofMaps(Map<T,Map<>>)
	/**
	 * 
	 * 
	 * convert a timestamp wise map tp date wise map
	 * <p>
	 * <font color = red>make sure that the timezone is set appropriately</font>
	 * </p>
	 * 
	 * @param <T>
	 * 
	 * @param allActivityEvents
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 */
	public static <T> LinkedHashMap<String, TreeMap<LocalDate, ArrayList<T>>> convertTimewiseMapToDatewiseMap(
			LinkedHashMap<String, TreeMap<Timestamp, T>> timewiseMap, TimeZone dft)
	{
		LinkedHashMap<String, TreeMap<LocalDate, ArrayList<T>>> daywiseMap = new LinkedHashMap<>();
		System.out.println("starting convertTimewiseMapToDatewiseMap");

		for (Map.Entry<String, TreeMap<Timestamp, T>> perUserCheckinEntry : timewiseMap.entrySet()) // Iterate over
																									// Users
		{
			String userID = perUserCheckinEntry.getKey();
			TreeMap<Timestamp, T> checkinEntriesForThisUser = perUserCheckinEntry.getValue();

			TreeMap<LocalDate, ArrayList<T>> daywiseForThisUser = new TreeMap<>();

			for (Map.Entry<Timestamp, T> checkin : checkinEntriesForThisUser.entrySet()) // iterare over activity events
																							// for this user
			{
				// Date date = DateTimeUtils.getDate(checkin.getKey());// (Date)
				// activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension", "Date"); // start
				// date
				LocalDate ldate = DateTimeUtils.getLocalDate(checkin.getKey(), dft);

				if ((daywiseForThisUser.containsKey(ldate)) == false)
				{
					daywiseForThisUser.put(ldate, new ArrayList<T>());
				}
				daywiseForThisUser.get(ldate).add(checkin.getValue());
			}

			daywiseMap.put(userID, daywiseForThisUser);
		}
		return daywiseMap;
	}

	/**
	 * 
	 * <font color = red>Error: hidden time component in java.sql.Date causing duplicate date keys</font>
	 * 
	 * convert a timestamp wise map to date wise map
	 * 
	 * @param <T>
	 * 
	 * @param allActivityEvents
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 */
	public static <T> LinkedHashMap<String, TreeMap<Date, ArrayList<T>>> convertTimewiseMapToDatewiseMap(
			LinkedHashMap<String, TreeMap<Timestamp, T>> timewiseMap)
	{
		LinkedHashMap<String, TreeMap<Date, ArrayList<T>>> daywiseMap = new LinkedHashMap<>();
		System.out.println("starting convertTimewiseMapToDatewiseMap");

		for (Map.Entry<String, TreeMap<Timestamp, T>> perUserCheckinEntry : timewiseMap.entrySet()) // Iterate over
																									// Users
		{
			String userID = perUserCheckinEntry.getKey();
			TreeMap<Timestamp, T> checkinEntriesForThisUser = perUserCheckinEntry.getValue();

			TreeMap<Date, ArrayList<T>> daywiseForThisUser = new TreeMap<>();

			for (Map.Entry<Timestamp, T> checkin : checkinEntriesForThisUser.entrySet()) // iterare over activity events
																							// for this user
			{
				Date date = DateTimeUtils.getDate(checkin.getKey());
				// (Date) activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension", "Date"); //
				// start date
				// Date ldate = DateTimeUtils.getLocalDate(checkin.getKey(), dft);

				if ((daywiseForThisUser.containsKey(date)) == false)
				{
					daywiseForThisUser.put(date, new ArrayList<T>());
				}
				daywiseForThisUser.get(date).add(checkin.getValue());
			}

			daywiseMap.put(userID, daywiseForThisUser);
		}
		return daywiseMap;
	}

	/**
	 * 
	 * <font color = green>Issue Solved: Prevented hidden time component in java.sql.Date from causing duplicate date
	 * keys by timestamp->LocalDate->sql.Date</font>
	 * <p>
	 * convert a timestamp wise map to date wise map
	 * </p>
	 * 
	 * @param <T>
	 * 
	 * @param allActivityEvents
	 * @param tz
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 */
	public static <T> LinkedHashMap<String, TreeMap<Date, ArrayList<T>>> convertTimewiseMapToDatewiseMap2(
			LinkedHashMap<String, TreeMap<Timestamp, T>> timewiseMap, TimeZone tz)
	{
		LinkedHashMap<String, TreeMap<Date, ArrayList<T>>> daywiseMap = new LinkedHashMap<>();
		System.out.println("starting convertTimewiseMapToDatewiseMap");

		for (Map.Entry<String, TreeMap<Timestamp, T>> perUserCheckinEntry : timewiseMap.entrySet())
		{ // Iterate over Users

			String userID = perUserCheckinEntry.getKey();
			TreeMap<Timestamp, T> checkinEntriesForThisUser = perUserCheckinEntry.getValue();

			TreeMap<Date, ArrayList<T>> daywiseForThisUser = new TreeMap<>();

			for (Map.Entry<Timestamp, T> checkin : checkinEntriesForThisUser.entrySet())
			{
				// iterare over activity events for this user
				LocalDate ldate = DateTimeUtils.getLocalDate(checkin.getKey(), tz);// converting to ldate to removing
																					// hidden time.
				Date date = Date.valueOf(ldate);// ldate.get//DateTimeUtils.getDate(checkin.getKey());// (Date)
												// activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension",
												// "Date"); // start date
				// Date ldate = DateTimeUtils.getLocalDate(checkin.getKey(), dft);

				if ((daywiseForThisUser.containsKey(date)) == false)
				{
					daywiseForThisUser.put(date, new ArrayList<T>());
				}
				daywiseForThisUser.get(date).add(checkin.getValue());
			}
			daywiseMap.put(userID, daywiseForThisUser);
		}
		return daywiseMap;
	}

	/**
	 * Creates user day timelines from the given list of Activity Objects.
	 * 
	 * Activity events ---> day timelines (later, not here)---> user timelines
	 * <p>
	 * <font color = red>make sure that the timezone is set appropriately</font>
	 * </p>
	 * 
	 * @param allActivityEvents
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createUserTimelinesFromCheckinEntriesGowalla(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> checkinEntries,
			LinkedHashMap<Integer, LocationGowalla> locationObjects)
	{
		long ct1 = System.currentTimeMillis();

		System.out.println("starting createUserTimelinesFromCheckinEntriesGowalla");
		LinkedHashMap<String, TreeMap<Date, ArrayList<CheckinEntry>>> checkinEntriesDatewise = convertTimewiseMapToDatewiseMap2(
				checkinEntries, Constant.getTimeZone());

		// Start of added on Aug 10 2017
		// // Filter out days which have more than 500 checkins
		// StringBuilder sb = new StringBuilder();
		// checkinEntriesDatewise.entrySet().stream().forEachOrdered(e->sb.append(str));
		// End of added on Aug 10 2017

		LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> activityObjectsDatewise = convertCheckinEntriesToActivityObjectsGowalla(
				checkinEntriesDatewise, locationObjects);

		int optimalSizeWrtUsers = (int) (Math.ceil(activityObjectsDatewise.size() / 0.75));
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDaytimelines = new LinkedHashMap<>(
				optimalSizeWrtUsers);

		// StringBuilder sbNumOfActsPerDayPerUser;
		// WritingToFile.appendLineToFileAbsolute("User,Date,NumOfActsInDay\n", "NumOfActsPerUserPerDay.csv");

		for (Entry<String, TreeMap<Date, ArrayList<ActivityObject>>> userEntry : activityObjectsDatewise.entrySet())
		{
			String userID = userEntry.getKey();
			LinkedHashMap<Date, Timeline> dayTimelines = new LinkedHashMap<>(
					(int) (Math.ceil(userEntry.getValue().size() / 0.75)));

			// sbNumOfActsPerDayPerUser = new StringBuilder();// "User,Day,NumOfActs\n");
			for (Entry<Date, ArrayList<ActivityObject>> dateEntry : userEntry.getValue().entrySet())
			{
				Date date = dateEntry.getKey();
				ArrayList<ActivityObject> activityObjectsInDay = dateEntry.getValue();
				// String dateID = date.toString();
				// String dayName = DateTimeUtils.getWeekDayFromWeekDayInt(date.getDay());//
				// DateTimeUtils.getWeekDayFromWeekDayInt(date.getDayOfWeek().getValue());
				dayTimelines.put(date, new Timeline(activityObjectsInDay, true, true));
				// sbNumOfActsPerDayPerUser.append(userID).append(',').append(date.toString()).append(',')
				// .append(activityObjectsInDay.size()).append("\n");
			}
			// WritingToFile.appendLineToFileAbsolute(sbNumOfActsPerDayPerUser.toString(),
			// "NumOfActsPerUserPerDay.csv");
			userDaytimelines.put(userID, dayTimelines);
		}

		long ct4 = System.currentTimeMillis();
		System.out.println(
				"created timelines for" + userDaytimelines.size() + " users in " + ((ct4 - ct1) / 1000) + " seconds");

		System.out.println("exiting createUserTimelinesFromCheckinEntriesGowalla");
		// System.exit(0);
		return userDaytimelines;
	}

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
				for (ActivityObject aos : dateE.getValue().getActivityObjectsInTimeline())
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
		WritingToFile.appendLineToFileAbsolute("User,Timestamp,CatID,CatName,DistDiff,DurationDiff\n",
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
					for (ActivityObject aos : dateE.getValue().getActivityObjectsInTimeline())
					{
						aoCount += 1;

						String activityName = aos.getActivityName();
						double distNext = aos.getDistanceInMFromNext();
						long durationNext = aos.getDurationInSecondsFromNext();
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
							WritingToFile.appendLineToFileAbsolute(sbEnumerateAllCats.toString(),
									commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv");
							sbEnumerateAllCats.setLength(0);

							/////////////////
							WritingToFile.appendLineToFileAbsolute(sbAllDistanceInMDurationInSec.toString(),
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
				WritingToFile.appendLineToFileAbsolute(sbEnumerateAllCats.toString(),
						commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv");
				sbEnumerateAllCats.setLength(0);

				/////////////////
				WritingToFile.appendLineToFileAbsolute(sbAllDistanceInMDurationInSec.toString(),
						commonPathToWrite + "DistDurDiffBetweenConsecSimilars.csv");
				sbAllDistanceInMDurationInSec.setLength(0);

				// WritingToFile.appendLineToFileAbsolute(sbAllDurationFromNext.toString(),
				// commonPathToWrite + "sbAllDurationFromNext.csv");
				// sbAllDurationFromNext.setLength(0);
				/////////////////

			}

			System.out.println("Num of aos read = " + aoCount);
			WritingToFile.writeConsectiveCountsEqualLength(catIDLengthConsecutives, catIDNameDictionary,
					commonPathToWrite + "CatwiseConsecCountsEqualLength.csv", true, true);
			WritingToFile.writeConsectiveCountsEqualLength(userLengthConsecutives, catIDNameDictionary,
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
	 * convert checkinentries to activity objects
	 * 
	 * @param checkinEntriesDatewise
	 * @param locationObjects
	 * @return
	 */
	private static LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> convertCheckinEntriesToActivityObjectsGowalla(
			LinkedHashMap<String, TreeMap<Date, ArrayList<CheckinEntry>>> checkinEntriesDatewise,
			LinkedHashMap<Integer, LocationGowalla> locationObjects)
	{
		LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> activityObjectsDatewise = new LinkedHashMap<>(
				(int) (Math.ceil(checkinEntriesDatewise.size() / 0.75)));

		System.out.println("starting convertCheckinEntriesToActivityObjectsGowalla");
		System.out.println("Num of locationObjects received = " + locationObjects.size() + " with keys as follows");

		if (VerbosityConstants.WriteLocationMap)
		{// locationObjects.keySet().stream().forEach(e -> System.out.print(String.valueOf(e) + "||"));
			StringBuilder locInfo = new StringBuilder();
			locationObjects.entrySet().forEach(e -> locInfo.append(e.getValue().toString() + "\n"));
			WritingToFile.writeToNewFile(locInfo.toString(), Constant.getOutputCoreResultsPath() + "LocationMap.csv");
		} // System.out.println("Num of locationObjects received = " + locationObjects.size());

		int numOfCInsWithMultipleLocIDs = 0, numOfCInsWithMultipleDistinctLocIDs = 0,
				numOfCInsWithMultipleWorkingLevelCatIDs = 0, numOfCIns = 0;
		HashMap<String, String> actsOfCinsWithMultipleWorkLevelCatIDs = new HashMap<>();
		// Set<String> setOfCatIDsofAOs = new TreeSet<String>();

		// convert checkinentries to activity objects
		for (Entry<String, TreeMap<Date, ArrayList<CheckinEntry>>> userEntry : checkinEntriesDatewise.entrySet())
		{// over users
			String userID = userEntry.getKey();
			TreeMap<Date, ArrayList<ActivityObject>> dayWiseForThisUser = new TreeMap<>();
			// System.out.print("\nuser: " + userID);
			for (Entry<Date, ArrayList<CheckinEntry>> dateEntry : userEntry.getValue().entrySet()) // over dates
			{
				// Date date = dateEntry.getKey();
				ArrayList<ActivityObject> activityObjectsForThisUserThisDate = new ArrayList<>(
						dateEntry.getValue().size());

				// System.out.println(
				// "--* user:" + userID + " date:" + date.toString() + " has " + dateEntry.getValue().size() + "
				// checkinentries");

				for (CheckinEntry cin : dateEntry.getValue())// over checkins
				{
					numOfCIns += 1;
					int activityID = Integer.valueOf(cin.getActivityID());
					String activityName = String.valueOf(cin.getActivityID());// "";

					Timestamp startTimestamp = cin.getTimestamp(); // timestamp of first cin with its a merged one
					String startLatitude = cin.getStartLatitude(); // of first cin if its a merged one
					String startLongitude = cin.getStartLongitude();// of first cin if its a merged one
					String startAltitude = "";//
					String userIDInside = cin.getUserID();
					double distaneInMFromPrev = cin.getDistanceInMetersFromPrev();// of first cin if its a merged one
					long durationInSecFromPrev = cin.getDurationInSecsFromPrev();// of first cin if its a merged one

					String[] levelWiseCatIDs = cin.getLevelWiseCatIDs();
					// int locationID = Integer.valueOf(e.getLocationID());
					ArrayList<Integer> locIDs = cin.getLocationIDs();// e.getLocationIDs());
					String locationName = "";//
					if (RegexUtils.patternDoubleUnderScore.split(cin.getWorkingLevelCatIDs()).length > 1)
					{
						numOfCInsWithMultipleWorkingLevelCatIDs += 1;
						actsOfCinsWithMultipleWorkLevelCatIDs.put(activityName, cin.getWorkingLevelCatIDs());
					}
					// sanity check start
					if (userIDInside.equals(userID) == false)
					{
						System.err.println(
								"Error: sanity check failed in createUserTimelinesFromCheckinEntriesGowalla()");
					}
					// sanity check end

					int numOfLocIDs = locIDs.size();
					int photos_count = 0, checkins_count = 0, users_count = 0, radius_meters = 0, highlights_count = 0,
							items_count = 0, max_items_count = 0;
					// we need to compute the average of these atributes in case there are more than one place id for a
					// (merged) checkin entry
					for (Integer locationID : locIDs)
					{
						LocationGowalla loc = locationObjects.get((locationID));

						if (loc == null)
						{
							System.err.println(
									"Error in convertCheckinEntriesToActivityObjectsGowalla: No LocationGowalla object found for locationID="
											+ String.valueOf(locationID));
						}

						photos_count += loc.getPhotos_count();
						checkins_count += loc.getCheckins_count();
						users_count += loc.getUsers_count();
						radius_meters += loc.getRadius_meters();
						highlights_count = loc.getHighlights_count();
						items_count += loc.getItems_count();
						max_items_count += loc.getMax_items_count();
					}

					if (numOfLocIDs > 1)
					{
						numOfCInsWithMultipleLocIDs += 1;
						photos_count = photos_count / numOfLocIDs;
						checkins_count = checkins_count / numOfLocIDs;
						users_count = users_count / numOfLocIDs;
						radius_meters = radius_meters / numOfLocIDs;
						highlights_count = highlights_count / numOfLocIDs;
						items_count = items_count / numOfLocIDs;
						max_items_count = max_items_count / numOfLocIDs;
					}

					if (new HashSet<Integer>(locIDs).size() > 1) // if more than 1 distinct locations
					{
						numOfCInsWithMultipleDistinctLocIDs += 1;
					}
					// LocationGowalla loc = locationObjects.get(String.valueOf(locationID));
					// int photos_count = loc.getPhotos_count();
					// int checkins_count = loc.getCheckins_count();
					// int users_count = loc.getUsers_count();
					// int radius_meters = loc.getRadius_meters();
					// int highlights_count = loc.getHighlights_count();
					// int items_count = loc.getItems_count();
					// int max_items_count = loc.getMax_items_count();

					ActivityObject ao = new ActivityObject(activityID, locIDs, activityName, locationName,
							startTimestamp, startLatitude, startLongitude, startAltitude, userID, photos_count,
							checkins_count, users_count, radius_meters, highlights_count, items_count, max_items_count,
							cin.getWorkingLevelCatIDs(), distaneInMFromPrev, durationInSecFromPrev, levelWiseCatIDs);
					// setOfCatIDsofAOs.add(ao.getActivityID());
					activityObjectsForThisUserThisDate.add(ao);
				}
				dayWiseForThisUser.put(dateEntry.getKey(), activityObjectsForThisUserThisDate);
			}
			activityObjectsDatewise.put(userID, dayWiseForThisUser);
		}
		System.out.println(" numOfCIns = " + numOfCIns);
		System.out.println(" numOfCInsWithMultipleLocIDs = " + numOfCInsWithMultipleLocIDs);
		System.out.println(" numOfCInsWithMultipleDistinctLocIDs = " + numOfCInsWithMultipleDistinctLocIDs);
		System.out.println(" numOfCInsWithMultipleWorkingLevelCatIDs = " + numOfCInsWithMultipleWorkingLevelCatIDs
				+ " for working level = " + DomainConstants.gowallaWorkingCatLevel);

		WritingToFile.writeToNewFile(
				actsOfCinsWithMultipleWorkLevelCatIDs.entrySet().stream().map(e -> e.getKey() + "-" + e.getValue())
						.collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "MapActsOfCinsWithMultipleWorkLevelCatIDs.csv");
		System.out.println("exiting convertCheckinEntriesToActivityObjectsGowalla");
		return activityObjectsDatewise;

	}

	/**
	 * Creates user day timelines from the given list of Activity Objects.
	 * 
	 * Activity events ---> day timelines (later, not here)---> user timelines
	 * 
	 * @deprecated needs to be checked again
	 * @param allActivityEvents
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createUserTimelinesFromActivityObjects(
			ArrayList<ActivityObject> allActivityEvents)
	{
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userTimelines = new LinkedHashMap<>();
		// userid, usertimeline
		LinkedHashMap<String, ArrayList<ActivityObject>> perUserActivityEvents = new LinkedHashMap<>();

		System.out.println("inside createUserTimelinesFromActivityObjects");

		for (int i = 0; i < allActivityEvents.size(); i++)
		{
			// UserDayTimeline userTimeline= new UserDayTimeline();
			// System.out.println(allActivityEvents.size());
			// System.out.println(allActivityEvents.get(i));
			// System.out.println(allActivityEvents.get(i).getDimensionIDValue("User_ID"));
			String userID = allActivityEvents.get(i).getDimensionIDValue("User_ID");
			if (!(perUserActivityEvents.containsKey(userID)))
			{
				perUserActivityEvents.put(userID, new ArrayList<ActivityObject>());
			}
			perUserActivityEvents.get(userID).add(allActivityEvents.get(i));
			/*
			 * if(!(userTimelines.containsKey(userID)) ) { userTimelines.put(userID, new HashMap<Date,UserDayTimeline>
			 * ()); }
			 */
			// Date date = (Date)allActivityEvents.get(i).getDimensionAttributeValue("Date_Dimension", "Date");
		}

		// perUserActivityEventCreated

		// Iterate over Users
		for (Map.Entry<String, ArrayList<ActivityObject>> perUserActivityEventsEntry : perUserActivityEvents.entrySet())
		{
			System.out.println("for user:" + perUserActivityEventsEntry.getKey() + " number of activity-objects ="
					+ perUserActivityEventsEntry.getValue().size());
			String userID = perUserActivityEventsEntry.getKey();
			ArrayList<ActivityObject> activityEventsForThisUser = perUserActivityEventsEntry.getValue();

			LinkedHashMap<Date, ArrayList<ActivityObject>> perDateActivityEventsForThisUser = new LinkedHashMap<Date, ArrayList<ActivityObject>>();

			for (int i = 0; i < activityEventsForThisUser.size(); i++) // iterare over activity events for this user
			{
				Date date = (Date) activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension",
						"Date"); // start
									// date
				if (!(perDateActivityEventsForThisUser.containsKey(date)))
				{
					perDateActivityEventsForThisUser.put(date, new ArrayList<ActivityObject>());
				}
				perDateActivityEventsForThisUser.get(date).add(activityEventsForThisUser.get(i));
			}

			// perDateActivityEventsForThisUser has been created now.

			LinkedHashMap<Date, Timeline> dayTimelines = new LinkedHashMap<Date, Timeline>();

			for (Map.Entry<Date, ArrayList<ActivityObject>> perDateActivityEventsForThisUserEntry : perDateActivityEventsForThisUser
					.entrySet())
			{
				Date date = perDateActivityEventsForThisUserEntry.getKey();
				dayTimelines.put(date, new Timeline(perDateActivityEventsForThisUserEntry.getValue(), true, true));
			}
			userTimelines.put(userID, dayTimelines);
		}
		System.out.println("exiting createUserTimelinesFromActivityEvents");
		return userTimelines;
	}

	/**
	 * Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day timelines.
	 * (removeDayTimelinesWithNoValidAct(),removeDayTimelinesWithOneOrLessDistinctValidAct(),removeWeekendDayTimelines()
	 * <b><font color="red">Note: this does not expunges all invalid activity objects from the timeline</font></b>
	 * 
	 * @param usersTimelines
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> cleanDayTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersTimelines)
	{
		System.out.println("Inside cleanDayTimelines(): total num of users before cleaning = " + usersTimelines.size());
		long ct = System.currentTimeMillis();
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> cleanedUserDayTimelines = new LinkedHashMap<>();

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{

			LinkedHashMap<Date, Timeline> cleanedDayTimelines = TimelineUtils.cleanUserDayTimelines(
					usersTimelinesEntry.getValue(), Constant.getCommonPath() + "LogCleanedDayTimelines_",
					usersTimelinesEntry.getKey());

			if (VerbosityConstants.verboseTimelineCleaning)
			{
				System.out.println("for user: " + usersTimelinesEntry.getKey() + "#cleanedDayTimeline = "
						+ cleanedDayTimelines.size() + "\n");
			}

			if (cleanedDayTimelines.size() > 0)
			{
				cleanedUserDayTimelines.put(usersTimelinesEntry.getKey(), cleanedDayTimelines);
			}
		}
		System.out.println("\ttotal num of users after cleaning = " + cleanedUserDayTimelines.size());

		long ct2 = System.currentTimeMillis();
		System.out.println("Exiting cleanDayTimelines(): total num of users after cleaning = "
				+ cleanedUserDayTimelines.size() + " time taken: " + ((ct2 - ct) / 1000) + " secs");
		return cleanedUserDayTimelines;
	}

	/**
	 * Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day timelines.
	 * (removeDayTimelinesWithNoValidAct(),removeDayTimelinesWithOneOrLessDistinctValidAct(),removeWeekendDayTimelines()
	 * <b><font color="red">Note: this does not expunges all invalid activity objects from the timeline</font></b>
	 * 
	 * @param userDayTimelines
	 * @return
	 */
	public static LinkedHashMap<Date, Timeline> cleanUserDayTimelines(LinkedHashMap<Date, Timeline> userDayTimelines)
	{
		userDayTimelines = TimelineUtils.removeDayTimelinesWithNoValidAct(userDayTimelines);
		userDayTimelines = TimelineUtils.removeDayTimelinesWithOneOrLessDistinctValidAct(userDayTimelines);
		userDayTimelines = TimelineUtils.removeWeekendDayTimelines(userDayTimelines);

		return userDayTimelines;
	}

	/**
	 * Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day timelines.
	 * (removeDayTimelinesWithNoValidAct(),removeDayTimelinesWithOneOrLessDistinctValidAct(),removeWeekendDayTimelines()
	 * <b><font color="red">Note: this does not expunges all invalid activity objects from the timeline</font></b>
	 * <p>
	 * Same as previous cleanUserDayTimelines but with logging output to csv file instead of printing.
	 * </p>
	 * 
	 * 
	 * @param userDayTimelines
	 * @param logFileNamePhrase
	 *            should include the common (root) path
	 * @param user
	 * @return
	 */
	public static LinkedHashMap<Date, Timeline> cleanUserDayTimelines(LinkedHashMap<Date, Timeline> userDayTimelines,
			String logFileNamePhrase, String user)
	{
		String removeDayTimelinesWithNoValidActLog = logFileNamePhrase + "RemoveDayTimelinesWithNoValidAct.csv";

		userDayTimelines = TimelineUtils.removeDayTimelinesWithNoValidAct(userDayTimelines,
				removeDayTimelinesWithNoValidActLog, user);

		userDayTimelines = TimelineUtils.removeDayTimelinesWithOneOrLessDistinctValidAct(userDayTimelines,
				logFileNamePhrase + "RemoveDayTimelinesWithOneOrLessDistinctValidAct.csv", user);
		userDayTimelines = TimelineUtils.removeWeekendDayTimelines(userDayTimelines,
				logFileNamePhrase + "RemoveWeekendDayTimelines.csv", user);

		return userDayTimelines;
	}

	/**
	 * training test split for all users simulatneously.
	 * <p>
	 * Earlier we are doing train test split for each user’s timeline iteratively, for collaborative approach we need
	 * training timelines for all users simulatneously, hence need to do training test split simulatneously.
	 * 
	 * @param allUsersAllDatesTimeslines
	 * @param percentageInTraining
	 * @return
	 * @since 25 July 2017
	 */
	public static LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> splitAllUsersTestTrainingTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsersAllDatesTimeslines,
			double percentageInTraining)
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
				oneUserAllDatesTimelines = TimelineUtils.cleanUserDayTimelines(oneUserAllDatesTimelines,
						Constant.getCommonPath() + "InsideSplitAllUsersTestTrainingTimelines", userID);
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

	public static void traverseTimelines(HashMap<String, ArrayList<ActivityObject>> timeLines)
	{
		System.out.println("----------------Traversing Timelines----------");

		Iterator it = timeLines.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pairs = (Map.Entry) it.next();

			String id = pairs.getKey().toString();
			ArrayList<ActivityObject> activityEvents = (ArrayList<ActivityObject>) pairs.getValue();

			System.out.println("Timeline for id = " + id);

			System.out.println("Number of Activity Events =" + activityEvents.size());

			for (ActivityObject ae : activityEvents)
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
	 * Converts a JSON Array to HashMap<String, ArrayList<ActivityObject>>
	 * 
	 * @param jsonArray
	 * @return
	 */
	public static HashMap<String, ArrayList<ActivityObject>> createActivityObjectsFromJsonArray(JSONArray jsonArray)
	{

		HashMap<String, ArrayList<ActivityObject>> timeLines = new HashMap<String, ArrayList<ActivityObject>>();
		// Key: Identifier for timeline (can be UserID in some case)
		// Value: An ArrayList of ActivityEvents

		System.out.println("inside createTimelinesFromJsonArray: checking parsing json array");
		System.out.println("number of elements in json array = " + jsonArray.length());

		try
		{
			for (int i = 0; i < jsonArray.length(); i++)
			{
				String userID = jsonArray.getJSONObject(i).get("User_ID").toString();
				String activityName = jsonArray.getJSONObject(i).getString("Activity_Name");
				String startTimeString = jsonArray.getJSONObject(i).get("Start_Time").toString();
				String endTimeString = jsonArray.getJSONObject(i).get("End_Time").toString();
				String dateString = jsonArray.getJSONObject(i).get("Date").toString();
				Timestamp startTime = DateTimeUtils.getTimestamp(startTimeString, dateString);
				Timestamp endTime = DateTimeUtils.getTimestamp(endTimeString, dateString);
				;

				if (!(timeLines.containsKey(userID))) timeLines.put(userID, new ArrayList<ActivityObject>());

				// get corresponding timeline (ArrayList of ActivityEvents) and add the Activity Event to that ArrayList
				ActivityObject activityEvent = new ActivityObject(activityName, startTime, endTime);
				timeLines.get(userID).add(activityEvent);
			} // note: the ActivityEvents obtained from the database are not in chronological order, thus the ArrayList
				// as of now does not contain Activity Events in chronological
				// order. Also its
				// a
				// good assumption for
				// results obtained using any general database.
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		return timeLines;
	}

	// MANALI
	/**
	 * 
	 * @param userAllDatesTimeslines
	 * @return
	 */
	public static LinkedHashMap<Date, Timeline> removeWeekendDayTimelines(
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines)
	{
		LinkedHashMap<Date, Timeline> datesTimelinesPruned = userAllDatesTimeslines;
		LinkedHashSet<Date> datesToRemove = new LinkedHashSet<>(); // changed from ArrayList to LinkedHashSet on 17
																	// Mar 2017

		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getKey().getDay() == 0 || entry.getKey().getDay() == 6)
			{
				datesToRemove.add(entry.getKey());
			}
		}

		for (Date dateToRemove : datesToRemove)
		{
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (VerbosityConstants.verboseTimelineCleaning)
		{
			System.out.print("Weekends to remove:");
			datesToRemove.stream().forEach(d -> System.out.println("," + d.toString()));
			System.out.println("Num of days removed for removeWeekendDayTimelines =" + datesToRemove.size());
		}

		// System.out.println("Count of weekends removed ="+datesToRemove.size()); //THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE ENTRIES, UNCOMMENT TO SEE, HOWEVER IT DOES NOT
		// AFFECT OUR PURPORSE // probably this has been resolved by using set but not verified
		return datesTimelinesPruned;
	}

	/**
	 * <p>
	 * Same as previous removeWeekendDayTimelines but with logging output to csv file instead of printing.
	 * </p>
	 * 
	 * @param userAllDatesTimeslines
	 * @param logFileName
	 * @param user
	 * @return
	 */
	public static LinkedHashMap<Date, Timeline> removeWeekendDayTimelines(
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines, String logFileName, String user)
	{
		LinkedHashMap<Date, Timeline> datesTimelinesPruned = userAllDatesTimeslines;
		LinkedHashSet<Date> datesToRemove = new LinkedHashSet<Date>();
		StringBuilder log = new StringBuilder();

		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getKey().getDay() == 0 || entry.getKey().getDay() == 6)
			{
				datesToRemove.add(entry.getKey());
				log.append(user + "," + entry.getKey() + "," + (entry.getKey().getDay()) + "\n");
			}
		}

		for (Date dateToRemove : datesToRemove)
		{
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (VerbosityConstants.verboseTimelineCleaning)
		{
			System.out.println("Num of days removed for removeWeekendDayTimelines =" + datesToRemove.size());
		}
		// System.out.println("Count of weekends removed ="+datesToRemove.size()); //THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE ENTRIES, UNCOMMENT TO SEE, HOWEVER IT DOES NOT
		// AFFECT OUR PURPORSE // probably this has been resolved by using set but not verified
		WritingToFile.appendLineToFileAbsolute(log.toString(), logFileName);
		return datesTimelinesPruned;
	}

	/**
	 * 
	 * @param userAllDatesTimeslines
	 * @return
	 */
	public static LinkedHashMap<Date, Timeline> removeWeekdaysDayTimelines(
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines)
	{
		LinkedHashMap<Date, Timeline> datesTimelinesPruned = userAllDatesTimeslines;
		LinkedHashSet<Date> datesToRemove = new LinkedHashSet<Date>();

		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (!(entry.getKey().getDay() == 0 || entry.getKey().getDay() == 6))
			{
				datesToRemove.add(entry.getKey());
			}
		}

		for (Date dateToRemove : datesToRemove)
		{
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (VerbosityConstants.verboseTimelineCleaning)
		{
			System.out.println("Weekdays to remove=");
			datesToRemove.stream().forEach(d -> System.out.println("," + d.toString()));
		}

		return datesTimelinesPruned;
	}

	/**
	 * Removes day timelines with no valid activities in the day
	 * 
	 * @param userAllDatesTimeslines
	 * @return
	 */
	public static LinkedHashMap<Date, Timeline> removeDayTimelinesWithNoValidAct(
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines)
	{
		LinkedHashMap<Date, Timeline> datesTimelinesPruned = userAllDatesTimeslines;
		LinkedHashSet<Date> datesToRemove = new LinkedHashSet<Date>();

		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().containsAtLeastOneValidActivity() == false)
			{
				datesToRemove.add(entry.getKey());
			}
		}

		for (Date dateToRemove : datesToRemove)
		{
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (VerbosityConstants.verboseTimelineCleaning)
		{
			System.out.print("Invalid days to remove for no valid activities in the day:");
			datesToRemove.stream().forEach(d -> System.out.println("," + d.toString()));
			System.out.println("Num of days removed for removeDayTimelinesWithNoValidAct =" + datesToRemove.size());
		}

		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());
		return datesTimelinesPruned;
	}

	/**
	 * Removes day timelines with no valid activities in the day
	 * <p>
	 * Same as previous removeDayTimelinesWithNoValidAct but with logging output to csv file instead of printing.
	 * </p>
	 * 
	 * @param userAllDatesTimeslines
	 * @return
	 */
	public static LinkedHashMap<Date, Timeline> removeDayTimelinesWithNoValidAct(
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines, String logFileName, String user)
	{
		LinkedHashMap<Date, Timeline> datesTimelinesPruned = userAllDatesTimeslines;
		LinkedHashSet<Date> datesToRemove = new LinkedHashSet<Date>();

		StringBuilder log = new StringBuilder("User,DateToRemove\n");
		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().containsAtLeastOneValidActivity() == false)
			{
				datesToRemove.add(entry.getKey());
				log.append(user + "," + entry.getKey() + "\n");
			}
		}

		for (Date dateToRemove : datesToRemove)
		{
			// System.out.print(datesToRemove.toString()+",");
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (VerbosityConstants.verboseTimelineCleaning)
		{
			System.out.println("Num of days removed for removeDayTimelinesWithNoValidAct =" + datesToRemove.size());
		}
		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());//THERE IS SOME BUG, THE ARRAY HAS

		WritingToFile.appendLineToFileAbsolute(log.toString(), logFileName);
		return datesTimelinesPruned;
	}

	/**
	 * Removes day timelines which have <=1 distinct valid activities in a day.
	 * 
	 * @param userAllDatesTimeslines
	 * @return HashMap of day timelines <Date, UserDayTimeline>
	 */
	public static LinkedHashMap<Date, Timeline> removeDayTimelinesWithOneOrLessDistinctValidAct(
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines)
	{
		// boolean verbose = VerbosityConstants.verboseTimelineCleaning;
		LinkedHashMap<Date, Timeline> datesTimelinesPruned = userAllDatesTimeslines;
		LinkedHashSet<Date> datesToRemove = new LinkedHashSet<Date>();

		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().countNumberOfValidDistinctActivities() <= 1)
			{
				datesToRemove.add(entry.getKey());
			}
		}

		for (Date dateToRemove : datesToRemove)
		{
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (VerbosityConstants.verboseTimelineCleaning)
		{
			System.out.print("Invalid days to remove for TimelinesWithLessThanOneDistinctValidAct:");
			datesToRemove.stream().forEach(d -> System.out.print("," + d.toString()));
			System.out.println(
					"Num of days removed for removeDayTimelinesWithOneOrLessDistinctValidAct =" + datesToRemove.size());
		}
		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());
		return datesTimelinesPruned;
	}

	/**
	 * Removes day timelines which have <=1 distinct valid activities in a day.
	 * 
	 * <p>
	 * Same as previous removeDayTimelinesWithOneOrLessDistinctValidAct but with logging output to csv file instead of
	 * printing.
	 * </p>
	 * 
	 * @param userAllDatesTimeslines
	 * @return HashMap of day timelines <Date, UserDayTimeline>
	 */
	public static LinkedHashMap<Date, Timeline> removeDayTimelinesWithOneOrLessDistinctValidAct(
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines, String logFileName, String user)
	{
		LinkedHashMap<Date, Timeline> datesTimelinesPruned = userAllDatesTimeslines;
		LinkedHashSet<Date> datesToRemove = new LinkedHashSet<Date>();

		StringBuilder log = new StringBuilder("User,date\n");

		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().countNumberOfValidDistinctActivities() <= 1)
			{
				datesToRemove.add(entry.getKey());
				log.append(user + "," + entry.getKey() + "," + entry.getValue().countNumberOfValidDistinctActivities()
						+ "\n");
			}
		}

		for (Date dateToRemove : datesToRemove)
		{
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (VerbosityConstants.verboseTimelineCleaning)
		{
			System.out.println(
					"Num of days removed for removeDayTimelinesWithOneOrLessDistinctValidAct =" + datesToRemove.size());
		}
		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());
		WritingToFile.appendLineToFileAbsolute(log.toString(), logFileName);
		return datesTimelinesPruned;
	}

	/**
	 * Removes day timelines which have <=1 distinct valid activities in a day.
	 * 
	 * @param userAllDatesTimeslines
	 * @return HashMap of day timelines <Date, UserDayTimeline>
	 */
	public static LinkedHashMap<Date, Timeline> removeDayTimelinesLessDistinctValidAct(
			LinkedHashMap<Date, Timeline> userAllDatesTimeslines, int lowerLimit)
	{
		LinkedHashMap<Date, Timeline> datesTimelinesPruned = userAllDatesTimeslines;
		LinkedHashSet<Date> datesToRemove = new LinkedHashSet<Date>();

		for (Map.Entry<Date, Timeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().countNumberOfValidDistinctActivities() < lowerLimit)
			{
				datesToRemove.add(entry.getKey());
			}
		}

		for (Date dateToRemove : datesToRemove)
		{
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (VerbosityConstants.verboseTimelineCleaning)
		{
			System.out.print("Invalid days to remove for removeDayTimelinesLessDistinctValidAct:");
			datesToRemove.stream().forEach(d -> System.out.print("," + d.toString()));
			System.out.println("\nNum of days removed for removeDayTimelinesLessDistinctValidAct (lowerlimit ="
					+ lowerLimit + ") =" + datesToRemove.size());
		}
		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());
		return datesTimelinesPruned;
	}

	/**
	 * @since Feb 13 2018
	 * @param databaseName
	 * @param usersDayTimelinesOriginal
	 * @param absFileNameForLog
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> removeBlackListedUsers(String databaseName,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, String absFileNameForLog)
	{
		if (!databaseName.equals("gowalla1"))
		{
			PopUps.printTracedErrorMsgWithExit("removeBlackListedUsers(): Only implemented for Gowalla dataset");
			return null;
		}

		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> timelinesForSelectedUser = new LinkedHashMap<>();
		WritingToFile.writeToNewFile("IndexOfUser,userID\n", absFileNameForLog);
		int indexOfUser = -1, numOfUsersSkippedGT553MaxActsPerDay = 0;
		///// Remove timelines for blacklisted users
		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersDayTimelinesOriginal.entrySet())
		{
			indexOfUser += 1;
			if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
			{
				System.out.println(" " + indexOfUser + " Removing user: " + userEntry.getKey()
						+ " as in gowallaUserIDsWithGT553MaxActsPerDay");
				WritingToFile.appendLineToFileAbsolute(indexOfUser + "," + userEntry.getKey() + "\n",
						absFileNameForLog);
				numOfUsersSkippedGT553MaxActsPerDay += 1;
				continue;
			}
			else
			{
				timelinesForSelectedUser.put(userEntry.getKey(), userEntry.getValue());
			}
		}
		System.out.println("numOfUsersSkippedGT553MaxActsPerDay= " + numOfUsersSkippedGT553MaxActsPerDay);

		return timelinesForSelectedUser;
	}

	public static LinkedHashMap<String, Timeline> rearrangeTimelinesByGivenOrder(LinkedHashMap<String, Timeline> map,
			int[] orderKeys)
	{
		LinkedHashMap<String, Timeline> rearranged = new LinkedHashMap<String, Timeline>();

		for (int key : orderKeys)
		{
			String keyString = Integer.toString(key);
			rearranged.put(keyString, map.get(keyString));
		}

		return rearranged;
	}

	/**
	 * 
	 * @param map
	 * @param orderKeys
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> rearrangeDayTimelinesByGivenOrder(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> map, int[] orderKeys)
	{
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> rearranged = new LinkedHashMap<>();

		for (int key : orderKeys)
		{
			String keyString = Integer.toString(key);
			if (map.containsKey(keyString) == false)
			{
				continue;
			}
			else
			{
				rearranged.put(keyString, map.get(keyString));
			}
		}
		return rearranged;
	}

	/**
	 * Rearranges the map where key is user id , according to the user id order prescribed in Constant.userIDs which is
	 * determined by the dataset used
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, Integer> rearrangeOrderForDataset(LinkedHashMap<String, Integer> userMaps)
	{
		LinkedHashMap<String, Integer> rearranged = new LinkedHashMap<String, Integer>();
		rearranged = UtilityBelt.rearrangeByGivenOrder(userMaps, Constant.getUserIDs());
		return rearranged;
	}

	/**
	 * Rearranges the map where key is user id , according to the user id order prescribed in Constant.userIDs which is
	 * determined by the dataset used
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, Timeline> rearrangeTimelinesOrderForDataset(
			LinkedHashMap<String, Timeline> userMaps)
	{
		LinkedHashMap<String, Timeline> rearranged = new LinkedHashMap<String, Timeline>();
		rearranged = rearrangeTimelinesByGivenOrder(userMaps, Constant.getUserIDs());
		return rearranged;
	}

	/**
	 * Rearranges the map where key is user id , according to the user id order prescribed in Constant.userIDs which is
	 * determined by the dataset used
	 * 
	 * @param map
	 * @return
	 */
	// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines = new LinkedHashMap<String,
	// LinkedHashMap<Date, UserDayTimeline>>();
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> rearrangeDayTimelinesOrderForDataset(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userMaps)
	{
		System.out.println("rearrangeDayTimelinesOrderForDataset received: " + userMaps.size() + " users");
		long ct1 = System.currentTimeMillis();
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> rearranged = new LinkedHashMap<>();
		rearranged = rearrangeDayTimelinesByGivenOrder(userMaps, Constant.getUserIDs());
		long ct2 = System.currentTimeMillis();
		System.out.println("rearrangeDayTimelinesOrderForDataset returned: " + rearranged.size() + " users"
				+ ". time taken: " + ((ct2 - ct1) / 1000) + " secs");
		return rearranged;
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
	 * Removes timelines with greater than upperLimit activity objects in a day
	 * 
	 * 
	 * @param usersDayTimelinesOriginal
	 * @param upperLimit
	 * @param fileNameForLog
	 * @param writeLog
	 *            can be disabled to save heap space otherwise consumed by StringBuilder
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> removeDayTimelinesWithGreaterAct(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, int upperLimit,
			String fileNameForLog, boolean writeLog)
	{
		System.out.println("Inside removeDayTimelinesWithGreaterAct");
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> result = new LinkedHashMap<>();
		StringBuilder log = new StringBuilder();
		ArrayList datesRemoved = new ArrayList<>();
		log.append("User, #days(daytimelines) removed\n");

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersDayTimelinesOriginal.entrySet())
		{
			String user = userEntry.getKey();
			LinkedHashMap<Date, Timeline> toKeepTimelines = new LinkedHashMap<>();

			int countOfDatesRemovedForThisUser = 0;
			for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
			{
				if (dateEntry.getValue().getActivityObjectsInTimeline().size() <= upperLimit)
				{
					toKeepTimelines.put(dateEntry.getKey(), dateEntry.getValue());
				}
				else
				{
					countOfDatesRemovedForThisUser += 1;
					datesRemoved.add(dateEntry.getKey());
					// datesRemoved.append(dateEntry.getKey().toString() + "_");
				}
			}

			if (countOfDatesRemovedForThisUser > 0)
			{
				if (writeLog)
				{
					log.append(user + "," + countOfDatesRemovedForThisUser + "," + datesRemoved.toString() + "\n");
					// System.out.println("For user:" + user + " #days(/daytimelines) removed = " +
					// countOfDatesRemovedForThisUser + " (having <"
					// + lowerLimit + " aos per day)");
				}
			}

			if (toKeepTimelines.size() > 0)
			{
				result.put(user, toKeepTimelines);
			}
		}

		WritingToFile.writeToNewFile(log.toString(), fileNameForLog);
		System.out.println("Exiting removeDayTimelinesWithGreaterAct");
		return result;
	}

	/**
	 * Removes timelines with less than lowerLimit activity objects in a day
	 * 
	 * 
	 * @param usersDayTimelinesOriginal
	 * @param lowerLimit
	 * @param fileNameForLog
	 * @param writeLog
	 *            can be disabled to save heap space otherwise consumed by StringBuilder
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> removeDayTimelinesWithLessAct(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, int lowerLimit,
			String fileNameForLog, boolean writeLog)
	{
		System.out.println("Inside removeDayTimelinesWithLessValidAct");
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> result = new LinkedHashMap<>();
		StringBuilder log = new StringBuilder();
		ArrayList datesRemoved = new ArrayList<>();
		log.append("User, #days(daytimelines) removed\n");

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersDayTimelinesOriginal.entrySet())
		{
			String user = userEntry.getKey();
			LinkedHashMap<Date, Timeline> toKeepTimelines = new LinkedHashMap<>();

			int countOfDatesRemovedForThisUser = 0;
			for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
			{
				if (dateEntry.getValue().getActivityObjectsInTimeline().size() >= lowerLimit)
				{
					toKeepTimelines.put(dateEntry.getKey(), dateEntry.getValue());
				}
				else
				{
					countOfDatesRemovedForThisUser += 1;
					datesRemoved.add(dateEntry.getKey());
					// datesRemoved.append(dateEntry.getKey().toString() + "_");
				}
			}

			if (countOfDatesRemovedForThisUser > 0)
			{
				if (writeLog)
				{
					log.append(user + "," + countOfDatesRemovedForThisUser + "," + datesRemoved.toString() + "\n");
					// System.out.println("For user:" + user + " #days(/daytimelines) removed = " +
					// countOfDatesRemovedForThisUser + " (having <"
					// + lowerLimit + " aos per day)");
				}
			}

			if (toKeepTimelines.size() > 0)
			{
				result.put(user, toKeepTimelines);
			}
		}

		WritingToFile.writeToNewFile(log.toString(), fileNameForLog);
		System.out.println("Exiting removeDayTimelinesWithLessValidAct");
		return result;
	}

	/**
	 * Removes user with less than lowerLimitDays
	 * 
	 * @param usersDayTimelinesOriginal
	 * @param lowerLimit
	 * @param fileNameForLog
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> removeUsersWithLessDays(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, int lowerLimit,
			String fileNameForLog)
	{
		System.out.println("Inside removeUsersWithLessDays");
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> result = new LinkedHashMap<>();
		StringBuffer log = new StringBuffer();
		log.append("UsersRemoved, NumOfDays\n");

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersDayTimelinesOriginal.entrySet())
		{
			String user = userEntry.getKey();
			if (userEntry.getValue().size() >= lowerLimit)
			{
				result.put(user, userEntry.getValue());
			}
			else
			{
				log.append(user + "," + userEntry.getValue().size() + "\n");
			}
		}

		WritingToFile.writeToNewFile(log.toString(), fileNameForLog);
		System.out.println("Exiting removeUsersWithLessDays");
		return result;
	}

	/**
	 * Removes timelines which whose edit distance are above the given threshold
	 * 
	 * @param distanceScoresSorted
	 * @param threshold
	 * @return a Map of Date (Key) of candidate timelines and their corresponding edit distance(value)
	 */
	public static LinkedHashMap<Date, Double> removeAboveThreshold(LinkedHashMap<Date, Double> distanceScoresSorted,
			double threshold)
	{
		LinkedHashMap<Date, Double> pruned = new LinkedHashMap<Date, Double>();

		System.out.println("Inside removeAboveThreshold");
		for (Map.Entry<Date, Double> entry : distanceScoresSorted.entrySet()) // Iterate over Users
		{
			System.out.print(" __reading:" + entry.getValue());
			if (entry.getValue() <= threshold)
			{
				pruned.put(entry.getKey(), entry.getValue());
				System.out.print(" keeping entry for:" + entry.getValue());
			}
			else
			{
				System.out.print(" removing entry for:" + entry.getValue());
			}
			System.out.print("~~~~~~~~");
		}
		return pruned;
	}

	/**
	 * Removes timelines which whose edit distance are above the given threshold
	 * 
	 * @param distanceScoresSorted
	 * @param threshold
	 * @return a Map of Date (Key) of candidate timelines and their corresponding edit distance(value)
	 */
	public static LinkedHashMap<Date, Triple<Integer, String, Double>> removeAboveThresholdDISD(
			LinkedHashMap<Date, Triple<Integer, String, Double>> distanceScoresSorted, double threshold)
	{
		LinkedHashMap<Date, Triple<Integer, String, Double>> pruned = new LinkedHashMap<Date, Triple<Integer, String, Double>>();

		System.out.println("Inside removeAboveThreshold");
		for (Map.Entry<Date, Triple<Integer, String, Double>> entry : distanceScoresSorted.entrySet()) // Iterate over
																										// Users
		{
			System.out.print(" __reading:" + entry.getValue());

			if (entry.getValue().getThird() <= threshold)
			{
				pruned.put(entry.getKey(), entry.getValue());
				System.out.print(" keeping entry for:" + entry.getValue().toString());
			}

			else
			{
				System.out.print(" removing entry for:" + entry.getValue().toString());
			}

			System.out.print("~~~~~~~~");
		}
		return pruned;
	}

	/**
	 * Removes timelines which whose edit distance are above the given threshold
	 * 
	 * @param distanceScoresSorted
	 * @param threshold
	 * @return a Map of String ID (Key) of candidate timelines and their corresponding edit distance(value)
	 */
	public static LinkedHashMap<String, Double> removeAboveThreshold2(
			LinkedHashMap<String, Double> distanceScoresSorted, double threshold)
	{
		LinkedHashMap<String, Double> pruned = new LinkedHashMap<String, Double>();
		int numberOfTimelinesRemoved = 0;
		System.out.println("Inside removeAboveThreshold2");
		for (Map.Entry<String, Double> entry : distanceScoresSorted.entrySet())
		{
			System.out.print(" __reading:" + entry.getValue());
			if (entry.getValue() <= threshold)
			{
				pruned.put(entry.getKey(), entry.getValue());
				System.out.print(" keeping entry for:" + entry.getValue());
			}
			else
			{
				System.out.print(" removing entry for:" + entry.getValue());
				numberOfTimelinesRemoved += 1;
			}
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("Number of timelines removeAboveThreshold2=" + numberOfTimelinesRemoved);
		}
		return pruned;
	}

	/**
	 * Removes timelines which whose edit distance are above the given threshold
	 * 
	 * @param distanceScoresSorted
	 * @param threshold
	 * @return a Map of String ID (Key) of candidate timelines and their corresponding edit distance(value)
	 */
	public static LinkedHashMap<String, Pair<Integer, Double>> removeAboveThreshold3(
			LinkedHashMap<String, Pair<Integer, Double>> distanceScoresSorted, double threshold)
	{
		LinkedHashMap<String, Pair<Integer, Double>> pruned = new LinkedHashMap<String, Pair<Integer, Double>>();

		int numberOfTimelinesRemoved = 0;

		System.out.println("Inside removeAboveThreshold3");

		for (Map.Entry<String, Pair<Integer, Double>> entry : distanceScoresSorted.entrySet())
		{
			String timelineID = entry.getKey();
			Pair<Integer, Double> editDistancePair = entry.getValue();
			double editDistanceEntry = editDistancePair.getSecond();

			System.out.print(" __reading:" + editDistanceEntry);

			if (editDistanceEntry <= threshold)
			{
				pruned.put(timelineID, editDistancePair);
				System.out.print(" keeping entry for:" + entry.getValue());
			}
			else
			{
				System.out.print(" removing entry for:" + entry.getValue());
				numberOfTimelinesRemoved += 1;
			}
		}
		if (VerbosityConstants.verbose)
		{
			System.out.println("Number of timelines removeAboveThreshold3=" + numberOfTimelinesRemoved);
		}
		return pruned;
	}

	// /////
	// TODO: MAKE IT GENERIC <T> for ID of timeline
	/**
	 * Removes timelines which whose edit distance are above the given threshold
	 * 
	 * @param distanceScoresSorted
	 * @param threshold
	 * @return a Map of TimelineID (Key) and their corresponding edit distance(value)
	 */
	public static LinkedHashMap<Integer, Double> removeAboveThreshold4FullCand(
			LinkedHashMap<Integer, Double> distanceScoresSortedFullCand, double threshold)
	{
		LinkedHashMap<Integer, Double> pruned = new LinkedHashMap<Integer, Double>();

		int numberOfTimelinesRemoved = 0;

		System.out.println("Inside removeAboveThreshold3");

		for (Map.Entry<Integer, Double> entry : distanceScoresSortedFullCand.entrySet())
		{
			Integer timelineID = entry.getKey();
			double editDistanceEntry = entry.getValue();

			System.out.print(" __reading:" + editDistanceEntry);

			if (editDistanceEntry <= threshold)
			{
				pruned.put(timelineID, editDistanceEntry);
				System.out.print(" keeping entry for:" + editDistanceEntry);
			}
			else
			{
				System.out.print(" removing entry for:" + editDistanceEntry);
				numberOfTimelinesRemoved += 1;
			}
		}
		if (VerbosityConstants.verbose)
		{
			System.out.println("Number of timelines removed=" + numberOfTimelinesRemoved);
		}
		return pruned;
	}

	// /////
	/**
	 * Is function
	 * 
	 * @param distanceScoresSortedFullCand
	 * @param threshold
	 * @return
	 */
	public static LinkedHashMap<Integer, Pair<String, Double>> removeAboveThreshold4FullCandISD(
			LinkedHashMap<Integer, Pair<String, Double>> distanceScoresSortedFullCand, double threshold)
	{
		LinkedHashMap<Integer, Pair<String, Double>> pruned = new LinkedHashMap<>();

		int numberOfTimelinesRemoved = 0;

		System.out.println("Inside removeAboveThreshold4FullCandISD");

		for (Map.Entry<Integer, Pair<String, Double>> entry : distanceScoresSortedFullCand.entrySet())
		{
			Integer timelineID = entry.getKey();
			double editDistanceEntry = entry.getValue().getSecond();

			// $$System.out.print(" __reading:" + editDistanceEntry);

			if (editDistanceEntry <= threshold)
			{
				pruned.put(timelineID, entry.getValue());
				// $$System.out.print(" keeping entry for:" + editDistanceEntry);
			}
			else
			{
				// $$System.out.print(" removing entry for:" + editDistanceEntry);
				numberOfTimelinesRemoved += 1;
			}
		}
		if (VerbosityConstants.verbose)
		{
			System.out.println("Number of timelines removed=" + numberOfTimelinesRemoved);
		}
		return pruned;
	}

	/**
	 * 
	 * @param distanceScoresSortedFullCand
	 * @param threshold
	 * @return
	 */
	public static LinkedHashMap<String, Pair<String, Double>> removeAboveThreshold4SSD(
			LinkedHashMap<String, Pair<String, Double>> distanceScoresSortedFullCand, double threshold)
	{
		LinkedHashMap<String, Pair<String, Double>> pruned = distanceScoresSortedFullCand.entrySet().stream()
				.filter(e -> e.getValue().getSecond() <= threshold).collect(Collectors.toMap(e -> e.getKey(),
						e -> e.getValue(), (v1, v2) -> v1, LinkedHashMap<String, Pair<String, Double>>::new));
		if (VerbosityConstants.verbose)
		{
			System.out.println("Number of timelines removed=" + (distanceScoresSortedFullCand.size() - pruned.size()));
		}
		return pruned;
	}

	// TODO: MAKE IT GENERIC <T> for ID of timeline
	/**
	 * Removes timelines which whose edit distance are above the given threshold
	 * 
	 * @param distanceScoresSorted
	 * @param threshold
	 * @return a Map of TimelineID (Key) and their corresponding edit distance(value)
	 */
	public static LinkedHashMap<Integer, Pair<Integer, Double>> removeAboveThreshold4(
			LinkedHashMap<Integer, Pair<Integer, Double>> distanceScoresSorted, double threshold)
	{
		LinkedHashMap<Integer, Pair<Integer, Double>> pruned = new LinkedHashMap<Integer, Pair<Integer, Double>>();

		int numberOfTimelinesRemoved = 0;

		System.out.println("Inside removeAboveThreshold3");

		for (Map.Entry<Integer, Pair<Integer, Double>> entry : distanceScoresSorted.entrySet())
		{
			Integer timelineID = entry.getKey();
			Pair<Integer, Double> editDistancePair = entry.getValue();
			double editDistanceEntry = editDistancePair.getSecond();

			System.out.print(" __reading:" + editDistanceEntry);

			if (editDistanceEntry <= threshold)
			{
				pruned.put(timelineID, editDistancePair);
				System.out.print(" keeping entry for:" + entry.getValue());
			}
			else
			{
				System.out.print(" removing entry for:" + entry.getValue());
				numberOfTimelinesRemoved += 1;
			}
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("Number of timelines removed=" + numberOfTimelinesRemoved);
		}
		return pruned;
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

	/**
	 * Cleaned , expunge all invalid activity objects and rearrange according to user id order in Constant.userID for
	 * the current dataset </br>
	 * <font color="red">NOT NEED ANYMORE AS IT HAS BEEN BROKEN DOWN INTO SMALLER SINGLE PURPOSE FUNCTIONS</font>
	 * 
	 * @param usersTimelines
	 * @return
	 */
	public static LinkedHashMap<String, Timeline> dayTimelinesToCleanedExpungedRearrangedTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersTimelines)
	{
		LinkedHashMap<String, Timeline> cleanedERTimelines = new LinkedHashMap<>();
		System.out.println("inside dayTimelinesToCleanedExpungedRearrangedTimelines()");

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			String userID = usersTimelinesEntry.getKey();
			LinkedHashMap<Date, Timeline> userDayTimelines = usersTimelinesEntry.getValue();

			userDayTimelines = cleanUserDayTimelines(userDayTimelines);

			// converts the day time to continuous dayless //// new Timeline(userDayTimelines);
			Timeline timelineForUser = dayTimelinesToATimeline(userDayTimelines, false, true);

			timelineForUser = TimelineUtils.expungeInvalids(timelineForUser); // expunges invalid activity objects

			cleanedERTimelines.put(userID, timelineForUser);
		}
		System.out.println("\t" + cleanedERTimelines.size() + " timelines created");
		cleanedERTimelines = rearrangeTimelinesOrderForDataset(cleanedERTimelines);
		System.out.println("\t" + cleanedERTimelines.size() + " timelines created");
		return cleanedERTimelines;
	}

	////
	/**
	 * 
	 * @param usersTimelines
	 * @return LinkedHashMap<User ID as String, Timeline of the user with user id as integer as timeline id>
	 */
	public static LinkedHashMap<String, Timeline> dayTimelinesToTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersTimelines)
	{
		LinkedHashMap<String, Timeline> timelines = new LinkedHashMap<>();
		if (usersTimelines.size() == 0 || usersTimelines == null)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in dayTimelinesToTimelines(): userTimeline.size = " + usersTimelines.size()));
		}
		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersTimelines.entrySet())
		{
			Timeline timelinesOfAllDatesCombined = dayTimelinesToATimeline(entry.getValue(), false, true);
			timelines.put(entry.getKey(), timelinesOfAllDatesCombined);
			// timelines.put(entry.getKey(), new Timeline(entry.getValue(), Integer.valueOf(entry.getKey())));
		}
		return timelines;
	}

	////
	/**
	 * Checks whether the given list Activity Objects are in chronological sequence.
	 * 
	 * @param listToCheck
	 * @return
	 */
	public static boolean isChronological(ArrayList<ActivityObject> listToCheck)
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
		ArrayList<ActivityObject> objectsInGivenTimeline = givenTimeline.getActivityObjectsInTimeline();

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
		ArrayList<ActivityObject> aosInGivenTimelineToCheckIn = givenTimelineToCheckIn.getActivityObjectsInTimeline();

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
		ArrayList<ActivityObject> aosInGivenTimelineToCheckIn = givenTimelineToCheckIn.getActivityObjectsInTimeline();

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
	public static ArrayList<ActivityObject> getNextNValidAOsAfterActivityAtThisTimeSameDay(Timeline t,
			Timestamp timestamp, int N)
	{

		LocalDate dateOfGivenTS = timestamp.toLocalDateTime().toLocalDate();
		System.out.println("Inside getNextNValidAOsAfterActivityAtThisTimeSameDay\n given ts=" + timestamp.toString()
				+ " localdate extracted=" + dateOfGivenTS.toString());

		ArrayList<ActivityObject> result = new ArrayList<>(N);

		int indexOfActivityObjectAtGivenTimestamp = t.getIndexOfActivityObjectAtTime(timestamp);

		for (int i = 0; i < N; i++)
		{
			ActivityObject ao = t
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
			result.stream().forEach((ActivityObject ao) -> System.out.print(ao.toStringAllGowallaTS() + ">>"));
			System.out.println("Exiting getNextNValidAOsAfterActivityAtThisTimeSameDay");
		}
		return result;
	}

	/**
	 * 
	 * @param usersTimelines
	 * @param userIDsToExpunge
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> expungeUserDayTimelinesByUser(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersTimelines, ArrayList<String> userIDsToExpunge)
	{
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reducedTimelines = new LinkedHashMap<>();
		System.out.println("number of users before reducing users timeline = " + usersTimelines.size());
		System.out.println("Users to remove = " + userIDsToExpunge.toString());

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			String userID = usersTimelinesEntry.getKey();
			System.out.println("Reading " + userID);
			if (userIDsToExpunge.contains(userID))
			{
				System.out.println("removing");
				continue;
			}
			else
			{
				System.out.println("keeping");
				reducedTimelines.put(userID, usersTimelinesEntry.getValue());
			}
		}
		System.out.println("number of users in reduced users timeline = " + reducedTimelines.size());
		return reducedTimelines;
	}

	/**
	 * 
	 * @param usersTimelines
	 * @return
	 */
	public static LinkedHashMap<String, Timeline> expungeInvalids(LinkedHashMap<String, Timeline> usersTimelines)
	{
		LinkedHashMap<String, Timeline> expungedTimelines = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> entry : usersTimelines.entrySet())
		{
			expungedTimelines.put(entry.getKey(), TimelineUtils.expungeInvalids(entry.getValue()));
		}
		return expungedTimelines;
	}

	/**
	 * 
	 * @param usersDayTimelines
	 * @return
	 */
	public static LinkedHashMap<Date, Timeline> expungeInvalidsDayTimelines(
			LinkedHashMap<Date, Timeline> usersDayTimelines)
	{
		return usersDayTimelines.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
				e -> TimelineUtils.expungeInvalids(e.getValue()), (v1, v2) -> v1, LinkedHashMap<Date, Timeline>::new));
	}

	/**
	 * Expunge invalids from training and test timelines for all users.
	 * 
	 * @param trainTestTimelinesForAllUsersOrig
	 * @return
	 */
	public static LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> expungeInvalidsDayTimelinesAllUsers(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersOrig)
	{
		LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers = null;

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> entry : trainTestTimelinesForAllUsersOrig.entrySet())
		{
			List<LinkedHashMap<Date, Timeline>> list = new ArrayList<>();
			list.add(expungeInvalidsDayTimelines(entry.getValue().get(0)));// trainingTimelinesForThisUser
			list.add(expungeInvalidsDayTimelines(entry.getValue().get(1)));// testTimelinesForThisUser

			trainTestTimelinesForAllUsers.put(entry.getKey(), list);
		}
		return trainTestTimelinesForAllUsers;
	}

	/**
	 * Removes the invalid activity objects from the given Timeline. Invalid Activity Objects are Activity Objects with
	 * Activity Name as 'Others' or 'Unknown'
	 * 
	 * @param timelineToPrune
	 * @return the Timeline without all its invalid activity objects
	 */
	public static Timeline expungeInvalids(Timeline timelineToPrune)
	{
		if (timelineToPrune == null)
		{
			System.err.println(
					PopUps.getTracedErrorMsg("Error inside UtulityBelt.expungeInvalids: timelineToPrune is null"));
		}

		ArrayList<ActivityObject> arrayToPrune = timelineToPrune.getActivityObjectsInTimeline();
		ArrayList<ActivityObject> arrPruned = (ArrayList<ActivityObject>) arrayToPrune.stream()
				.filter(ao -> ao.isInvalidActivityName() == false).collect(Collectors.toList());

		Timeline prunedTimeline = new Timeline(arrPruned, timelineToPrune.isShouldBelongToSingleDay(),
				timelineToPrune.isShouldBelongToSingleUser());
		prunedTimeline.setTimelineID(timelineToPrune.getTimelineID());
		return prunedTimeline;
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
	 * Fetches the current timeline from the given longer timeline from the recommendation point back until the matching
	 * unit count Activity Objects.
	 * 
	 * @param longerTimeline
	 *            the timelines (test timeline) from which the current timeline is to be extracted
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCounts
	 * @return Pair(current_TimelineWithNext, reductionInMU)
	 */
	public static Pair<TimelineWithNext, Double> getCurrentTimelineFromLongerTimelineMUCount(Timeline longerTimeline,
			Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm, double matchingUnitInCountsD)
	{
		// $$System.out.println("------Inside getCurrentTimelineFromLongerTimelineMUCount");
		double reductionInMU = 0;
		int matchingUnitInCounts = (int) matchingUnitInCountsD;

		Timestamp currentEndTimestamp = new Timestamp(dateAtRecomm.getYear(), dateAtRecomm.getMonth(),
				dateAtRecomm.getDate(), timeAtRecomm.getHours(), timeAtRecomm.getMinutes(), timeAtRecomm.getSeconds(),
				0);
		// long currentEndTime=currentEndTimestamp.getTime();

		int indexOfCurrentEnd = longerTimeline.getIndexOfActivityObjectAtTime(currentEndTimestamp);

		if (indexOfCurrentEnd - matchingUnitInCounts < 0)
		{
			reductionInMU = matchingUnitInCounts - indexOfCurrentEnd;
			System.out.println("Warning: reducing mu since not enough past,indexOfCurrentEnd=" + indexOfCurrentEnd
					+ ", muInCounts=" + matchingUnitInCounts + "), new MU=" + indexOfCurrentEnd);
			matchingUnitInCounts = indexOfCurrentEnd;
		}

		// this is a safe cast in this case
		// long matchingUnitInMilliSeconds= (long)(matchingUnitInHours*60*60*1000);//multiply(new
		// BigDecimal(60*60*1000))).longValue();
		// Timestamp currentStartTimestamp = new Timestamp(currentEndTime- matchingUnitInMilliSeconds);

		int indexOfCurrentStart = indexOfCurrentEnd - matchingUnitInCounts;

		if (VerbosityConstants.verbose)
		{
			System.out.println("longer timeline=" + longerTimeline.getActivityObjectNamesWithTimestampsInSequence());// getActivityObjectNamesInSequence());
			// currentTimeline.getActivityObjectNamesWithTimestampsInSequence());
			System.out.println("Start index of current timeline=" + indexOfCurrentStart
					+ "\nEnd index of current timeline=" + indexOfCurrentEnd + "\nAdjusted MU:" + matchingUnitInCounts);
		}

		// identify the recommendation point in longer timeline
		ArrayList<ActivityObject> activityObjectsInCurrentTimeline = longerTimeline
				.getActivityObjectsInTimelineFromToIndex(indexOfCurrentStart, indexOfCurrentEnd + 1);

		System.out.println("AOsInCurrTimeline.size()=" + activityObjectsInCurrentTimeline.size());

		ActivityObject nextValidActivityObject = longerTimeline
				.getNextValidActivityAfterActivityAtThisPositionPD(indexOfCurrentEnd);
		ActivityObject nextActivityObject = longerTimeline
				.getNextActivityAfterActivityAtThisPosition(indexOfCurrentEnd);

		int isInvalid = nextActivityObject.isInvalidActivityName() ? 1 : -1;
		TimelineWithNext currentTimeline = new TimelineWithNext(activityObjectsInCurrentTimeline,
				nextValidActivityObject, false, true);
		currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);

		// System.out.println("Current timeline="+currentTimeline.getActivityObjectNamesInSequence());
		if (currentTimeline.getActivityObjectsInTimeline().size() != (matchingUnitInCounts + 1))
		// note: this is matching unit in counts reduced
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error: the current timeline does not have #activity objs = adjusted matching unit"));
		}

		// $$System.out.println("------Exiting getCurrentTimelineFromLongerTimelineMUCount");
		return new Pair<>(currentTimeline, reductionInMU);
	}

	/**
	 * Fetches the current timeline from the given longer timeline from the recommendation point back until the matching
	 * unit length.
	 * 
	 * @param longerTimeline
	 *            the timelines (test timeline) from which the current timeline is to be extracted
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInHours
	 * @return
	 */
	public static TimelineWithNext getCurrentTimelineFromLongerTimelineMUHours(Timeline longerTimeline,
			Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm, double matchingUnitInHours)
	{
		System.out.println("------- Inside getCurrentTimelineFromLongerTimelineMUHours");

		Timestamp currentEndTimestamp = new Timestamp(dateAtRecomm.getYear(), dateAtRecomm.getMonth(),
				dateAtRecomm.getDate(), timeAtRecomm.getHours(), timeAtRecomm.getMinutes(), timeAtRecomm.getSeconds(),
				0);
		long currentEndTime = currentEndTimestamp.getTime();

		// this is a safe cast in this case
		long matchingUnitInMilliSeconds = (long) (matchingUnitInHours * 60 * 60 * 1000);// multiply(new
																						// BigDecimal(60*60*1000))).longValue();

		Timestamp currentStartTimestamp = new Timestamp(currentEndTime - matchingUnitInMilliSeconds);

		System.out.println("Starttime of current timeline=" + currentStartTimestamp + "\nEndtime of current timeline="
				+ currentEndTimestamp);

		// identify the recommendation point in longer timeline
		ArrayList<ActivityObject> activityObjectsInCurrentTimeline = longerTimeline
				.getActivityObjectsBetweenTime(currentStartTimestamp, currentEndTimestamp);

		ActivityObject nextValidActivityObject = longerTimeline
				.getNextValidActivityAfterActivityAtThisTime(currentEndTimestamp);
		ActivityObject nextActivityObject = longerTimeline.getNextActivityAfterActivityAtThisTime(currentEndTimestamp);

		int isInvalid = nextActivityObject.isInvalidActivityName() ? 1 : -1;
		TimelineWithNext currentTimeline = new TimelineWithNext(activityObjectsInCurrentTimeline,
				nextValidActivityObject, false, true);
		currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);
		System.out.println("------- Exiting getCurrentTimelineFromLongerTimelineMUHours");
		return currentTimeline;
	}

	/**
	 * Fetches the current timeline from the given longer timeline from the recommendation point back until the matching
	 * unit count Activity Objects.
	 * 
	 * @param longerTimeline
	 *            the timelines (test timeline) from which the current timeline is to be extracted
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCounts
	 * @return
	 */
	public static TimelineWithNext getCurrentTimelineFromLongerTimelineDaywise(
			LinkedHashMap<Date, Timeline> testDayTimelines, Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm)
	{
		// $$System.out.println("------Inside getCurrentTimelineFromLongerTimelineDaywise");

		///////////////////////////////////////////////////////////////////
		Timestamp currentEndTimestamp = new Timestamp(dateAtRecomm.getYear(), dateAtRecomm.getMonth(),
				dateAtRecomm.getDate(), timeAtRecomm.getHours(), timeAtRecomm.getMinutes(), timeAtRecomm.getSeconds(),
				0);
		// Timestamp currentEndTimestamp2 = new Timestamp(timeAtRecomm.getTime()); INCORRECT 1970 year
		// check if timestamps are actually equally, if yes, prefer the cleaner method INCORRECT 1970 year
		// System.out.println("Debug sanity check Note: currentEndTimestamp2.equals(currentEndTimestamp) ="
		// + currentEndTimestamp.equals(currentEndTimestamp));
		// System.out.println("Debug sanity check Note: currentEndTimestamp2 =" + currentEndTimestamp
		// + "currentEndTimestamp = " + currentEndTimestamp);
		///////////////////////////////////////////////////////////////////
		Timeline currentDayTimeline = testDayTimelines.get(dateAtRecomm);

		if (VerbosityConstants.verbose)
		{
			LocalDate dateOfFetchedDayTimeline = currentDayTimeline.getActivityObjectsInTimeline().get(0).getEndDate();
			System.out.println("Debug sanity check Note: dateOfFetchedDayTimeline = " + dateOfFetchedDayTimeline
					+ " dateAtRecomm = " + dateAtRecomm + "equals = " + dateAtRecomm.equals(dateOfFetchedDayTimeline));
		}
		if (currentDayTimeline == null || !(currentDayTimeline.size() > 0))
		{
			System.err.println(
					PopUps.getTracedErrorMsg("Error: currentDayTimeline.size() =" + currentDayTimeline.size()));
		}
		///////////////////////////////////////////////////////////////////

		int indexOfCurrentEnd = currentDayTimeline.getIndexOfActivityObjectAtTime(currentEndTimestamp);

		// identify the recommendation point in longer timeline
		ArrayList<ActivityObject> activityObjectsInCurrentTimeline = currentDayTimeline
				.getActivityObjectsInTimelineFromToIndex(0, indexOfCurrentEnd + 1);

		ActivityObject nextValidActivityObject = currentDayTimeline
				.getNextValidActivityAfterActivityAtThisPositionPD(indexOfCurrentEnd);
		ActivityObject nextActivityObject = currentDayTimeline
				.getNextActivityAfterActivityAtThisPosition(indexOfCurrentEnd);

		int isInvalid = nextActivityObject.isInvalidActivityName() ? 1 : -1;

		TimelineWithNext currentTimeline = new TimelineWithNext(activityObjectsInCurrentTimeline,
				nextValidActivityObject, true, true);
		currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);

		// if (VerbosityConstants.verbose)
		// System.out.println("Current timeline=" + currentTimeline.getActivityObjectNamesInSequence());

		if (!(currentTimeline.size() > 0))
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: currentTimeline.size() =" + currentTimeline.size()));
		}

		// $$System.out.println("------Exiting getCurrentTimelineFromLongerTimelineDaywise");
		return currentTimeline;
	}

	/**
	 * Checks whether all the ActivityObjects in the Timeline are of the same day or not. Compared the start timestamps
	 * of activity objects
	 * 
	 * @return
	 */
	public static boolean isSameDay(ArrayList<ActivityObject> activityObjectsInDay)
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

	/**
	 * Creates a Timeline consisting of the Activity Objects from the given LinkedHashMap of UserDay Timelines thus
	 * essentially converts mulitple day timelines into a single continuous single timeline
	 * 
	 * @param dayTimelines
	 *            LinkedHashMap of UserDayTimelines
	 * 
	 * @param shouldBelongToSingleDay
	 *            should the Timeline to be returned belong to a single day
	 * @param shouldBelongToSingleUser
	 *            should the Timeline to be returned belong to a single user
	 */
	public static Timeline dayTimelinesToATimeline(LinkedHashMap<Date, Timeline> dayTimelines,
			boolean shouldBelongToSingleDay, boolean shouldBelongToSingleUser)
	{
		long dt = System.currentTimeMillis();
		Timeline concatenatedTimeline = null;

		ArrayList<ActivityObject> allActivityObjects = new ArrayList<>();
		// ArrayList<ActivityObject> allActivityObjectsDummy = new ArrayList<>();

		// long t1 = System.nanoTime();
		// TODO: this method is a signficant performance eater, find way to optimise it.
		for (Map.Entry<Date, Timeline> entryOneDayTimeline : dayTimelines.entrySet())
		{
			allActivityObjects.addAll(entryOneDayTimeline.getValue().getActivityObjectsInTimeline());
		}
		// long t2 = System.nanoTime();

		// Start of dummy: was trying Collections.addAll if it gave better performance but it wasnt better
		// for (Map.Entry<Date, Timeline> entryOneDayTimeline : dayTimelines.entrySet())
		// {
		// ArrayList<ActivityObject> aosInThisDayTimeline = entryOneDayTimeline.getValue()
		// .getActivityObjectsInTimeline();
		// Collections.addAll(allActivityObjectsDummy,
		// aosInThisDayTimeline.toArray(new ActivityObject[aosInThisDayTimeline.size()]));
		// }
		// long t3 = System.nanoTime();

		// System.out.println("Approach original: " + (t2 - t1) + " ns");
		// System.out.println("Approach new : " + (t3 - t2) + " ns");
		// System.out.println("% change : " + 100.0 * ((t3 - t2) - (t2 - t1)) / (t2 - t1));
		// System.out.println("allActivityObjectsDummy.equals(allActivityObjects) = "
		// + allActivityObjectsDummy.equals(allActivityObjects));
		// end of dummy

		if (allActivityObjects.size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in 'dayTimelinesToATimeline' creating Timeline: Empty Activity Objects provided"));
			System.exit(-1);
		}

		else
		{
			concatenatedTimeline = new Timeline(allActivityObjects, shouldBelongToSingleDay, shouldBelongToSingleUser);
			if (VerbosityConstants.verbose)
			{
				System.out.println("Creating timelines for " + dayTimelines.size() + " daytimelines  takes "
						+ (System.currentTimeMillis() - dt) + " ms");
			}
		}
		return concatenatedTimeline;
	}

	//
	/**
	 * Same as dayTimelinesToATimeline but with String keys instead of Date
	 * <p>
	 * Creates a Timeline consisting of the Activity Objects from the given LinkedHashMap of UserDay Timelines thus
	 * essentially converts mulitple day timelines into a single continuous single timeline
	 * 
	 * @param dayTimelines
	 *            LinkedHashMap of UserDayTimelines
	 * 
	 * @param shouldBelongToSingleDay
	 *            should the Timeline to be returned belong to a single day
	 * @param shouldBelongToSingleUser
	 *            should the Timeline to be returned belong to a single user
	 * @since 12 June 2017
	 */
	public static Timeline dayTimelinesToATimeline2(LinkedHashMap<String, Timeline> dayTimelines,
			boolean shouldBelongToSingleDay, boolean shouldBelongToSingleUser)
	{
		long dt = System.currentTimeMillis();
		Timeline concatenatedTimeline = null;

		ArrayList<ActivityObject> allActivityObjects = new ArrayList<>();

		for (Map.Entry<String, Timeline> entry : dayTimelines.entrySet())
		{
			allActivityObjects.addAll(entry.getValue().getActivityObjectsInTimeline());
		}

		if (allActivityObjects.size() == 0)
		{
			PopUps.printTracedErrorMsgWithExit("creating Timeline: Empty Activity Objects provided");
		}

		else
		{
			concatenatedTimeline = new Timeline(allActivityObjects, shouldBelongToSingleDay, shouldBelongToSingleUser);
			if (VerbosityConstants.verbose)
			{
				System.out.println("Creating timelines for " + dayTimelines.size() + " daytimelines  takes "
						+ (System.currentTimeMillis() - dt) / 1000 + " secs");
			}
		}
		return concatenatedTimeline;
	}

	//

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
	 * Useful for mu breaking of timelines to find if a particular rt will have recommendation for daywise approach so
	 * that they can be compared
	 * 
	 * @param trainingTimelines
	 * @param dateAtRecomm
	 * @param activityAtRecommPoint
	 * @return
	 */
	public static boolean hasDaywiseCandidateTimelines(LinkedHashMap<Date, Timeline> trainingTimelines,
			Date dateAtRecomm, ActivityObject activityAtRecommPoint)
	{// ArrayList<ActivityObject> activitiesGuidingRecomm,*/
		LinkedHashMap<Date, Timeline> candidateTimelines = TimelineExtractors
				.extractDaywiseCandidateTimelines(trainingTimelines, dateAtRecomm, activityAtRecommPoint);
		if (candidateTimelines.size() > 0)
			return true;
		else
			return false;
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
			LinkedHashMap<String, Timeline> candidateTimelines2, ArrayList<ActivityObject> activitiesGuidingRecomm2,
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
			ArrayList<ActivityObject> activitiesGuidingRecomm, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candidateID, boolean hasInvalidActivityNames, String invalidActName1,
			String invalidActName2, String distanceUsed, HJEditDistance hjEditDistance)
	{

		// find the end points in the userDayTimeline
		char activityAtRecommPointAsStringCode = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1)
				.getCharCode();
		String activitiesGuidingAsStringCode = StringCode.getStringCodeForActivityObjects(activitiesGuidingRecomm);
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
			WritingToFile.writeEditDistancesOfAllEndPoints(activitiesGuidingRecomm, candidateDayTimeline,
					distanceScoresForEachSubsequence);

		}

		// Triple{EndIndexOfLestDistantSubCand,TraceOfEditOperation,EditDistance}
		return new Triple<Integer, String, Double>(endPointIndexForSubsequenceWithHighestSimilarity,
				traceEditOperationsForSubsequenceWithHighestSimilarity,
				distanceScoreForSubsequenceWithHighestSimilarity);
	}

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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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

		ActivityObject activityObjectAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1);
		Timestamp startTimestampOfActObjAtRecommPoint = activityObjectAtRecommPoint.getStartTimestamp();

		for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
		{
			/*
			 * For this cand timeline, find the Activity Object with start timestamp nearest to the start timestamp of
			 * current Activity Object and the distance is diff of their start times
			 */
			Triple<Integer, ActivityObject, Double> score = (entry.getValue()
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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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

		ActivityObject activityObjectAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1);
		Timestamp startTimestampOfActObjAtRecommPoint = activityObjectAtRecommPoint.getStartTimestamp();

		Timeline candidateTimelinesAsOne = TimelineUtils.dayTimelinesToATimeline2(candidateTimelines, false, true);

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

		ArrayList<Timestamp> timestampsToLookInto = createTimestampsToLookAt(uniqueDatesInCands,
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
			Triple<Integer, ActivityObject, Double> score = candidateTimelinesAsOne
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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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

		ActivityObject activityObjectAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1);
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
			Timeline candTimelinesFromAnotherUserAsOne = TimelineUtils
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

			ArrayList<Timestamp> timestampsToLookInto = createTimestampsToLookAt(uniqueDatesInCands,
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
				Triple<Integer, ActivityObject, Double> score = candTimelinesFromAnotherUserAsOne
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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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

		ActivityObject activityObjectAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1);
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
			Timeline candTimelinesFromAnotherUserAsOne = TimelineUtils
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

			ArrayList<Timestamp> timestampsToLookInto = createTimestampsToLookAt(uniqueDatesInCands,
					startTimestampOfActObjAtRecommPoint, verbose);

			// for (Entry<String, Timeline> cand : candidateTimelines.entrySet())
			// { for (ActivityObject ao : cand.getValue().getActivityObjectsInTimeline())
			// { uniqueDates.add(Instant.ofEpochMilli(ao.getStartTimestampInms())
			// .atZone(Constant.getTimeZone().toZoneId()).toLocalDate()); } }

			TreeMap<Double, Triple<Integer, ActivityObject, Double>> timeDiffOfClosest = new TreeMap<>();
			for (Timestamp tsToLookInto : timestampsToLookInto)
			// for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
			{
				// Actually these dates as string should be same as keys candidateTimelines
				// Date d = new Date(tsToLookInto.getYear(), tsToLookInto.getMonth(), tsToLookInto.getDate());

				/*
				 * For this candTimelinesFromAnotherUserAsOne, find the Activity Object with start timestamp nearest to
				 * the start timestamp of current Activity Object and the distance is diff of their start times
				 */
				Triple<Integer, ActivityObject, Double> score = candTimelinesFromAnotherUserAsOne
						.getTimeDiffValidAOWithStartTimeNearestTo(tsToLookInto, verbose);
				// System.out.println("Score= " + score.toString());
				timeDiffOfClosest.put(score.getThird(), score);
			}

			Triple<Integer, ActivityObject, Double> closestScore = timeDiffOfClosest.firstEntry().getValue();

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
	 * For each given date in givenDates, create a timestamp for the given time in givenTime
	 * 
	 * @param uniqueDatesInCands
	 * @param startTimestampOfActObjAtRecommPoint
	 * @return
	 */
	public static ArrayList<Timestamp> createTimestampsToLookAt(LinkedHashSet<LocalDate> givenDates,
			Timestamp givenTime, boolean verbose)
	{
		ArrayList<Timestamp> createdTimestamp = new ArrayList<>();
		int hrs = givenTime.getHours();
		int mins = givenTime.getMinutes();
		int secs = givenTime.getSeconds();

		for (LocalDate d : givenDates)
		{
			createdTimestamp.add(
					new Timestamp(d.getYear() - 1900, d.getMonthValue() - 1, d.getDayOfMonth(), hrs, mins, secs, 0));
		}

		// sanity check
		if (verbose)
		{
			System.out.println("\nInside createTimestampsToLookAt:\ngiven dates=");
			givenDates.stream().forEachOrdered(d -> System.out.print(d.toString() + "\t"));
			System.out.println("\ncreateTimestampsToLookAt=");
			createdTimestamp.stream().forEachOrdered(d -> System.out.print(d.toString() + "\t"));
		}

		return createdTimestamp;
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
		ArrayList<ActivityObject> aosInTimeline = givenTimeline.getActivityObjectsInTimeline();

		for (ActivityObject ao : aosInTimeline)
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
			for (ActivityObject ao : cand.getValue().getActivityObjectsInTimeline())
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

	/**
	 * Extract unique location IDs from the given timelines
	 * 
	 * @param usersCleanedDayTimelines
	 * @return
	 */
	public static TreeSet<Integer> getUniqueLocIDs(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean write)
	{
		TreeSet<Integer> uniqueLocIDs = new TreeSet<>();
		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> e : usersCleanedDayTimelines.entrySet())
			{
				for (Entry<Date, Timeline> e2 : e.getValue().entrySet())
				{
					e2.getValue().getActivityObjectsInTimeline().stream()
							.forEach(ao -> uniqueLocIDs.addAll(ao.getLocationIDs()));
				}
			}
			System.out.println("Inside getUniqueLocIDs: uniqueLocIDs.size()=" + uniqueLocIDs.size());
			if (write)
			{
				// WritingToFile.writeToNewFile(uniqueLocIDs.toString(), );
				WritingToFile.writeToNewFile(
						uniqueLocIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")).toString(),
						Constant.getCommonPath() + "UniqueLocIDs.csv");// );
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
				WritingToFile.writeToNewFile(
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
				WritingToFile.writeToNewFile(
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
					for (ActivityObject ao : e2.getValue().getActivityObjectsInTimeline())
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
	 * @return
	 */
	public static TreeSet<Integer> getUniqueActivityIDs(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean write)
	{
		TreeSet<Integer> uniqueActIDs = new TreeSet<>();
		try
		{
			for (Entry<String, LinkedHashMap<Date, Timeline>> e : usersCleanedDayTimelines.entrySet())
			{
				for (Entry<Date, Timeline> e2 : e.getValue().entrySet())
				{
					e2.getValue().getActivityObjectsInTimeline().stream()
							.forEach(ao -> uniqueActIDs.add(ao.getActivityID()));
				}
			}
			System.out.println("Inside getUniqueActivityIDs: uniqueActIDs.size()=" + uniqueActIDs.size());
			if (write)
			{
				WritingToFile.writeToNewFile(uniqueActIDs.toString(), Constant.getCommonPath() + "uniqueActIDs.csv");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniqueActIDs;
	}

	/**
	 * Extract unique PD Vals per user
	 * 
	 * @param usersCleanedDayTimelines
	 * @param writeToFile
	 * @param fileName
	 * @return
	 */
	public static LinkedHashMap<String, TreeSet<Integer>> getUniquePDValPerUser(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean writeToFile,
			String fileName)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("User,#UniquePDVals\n");
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
				sb.append(user + "," + uniquePDValsForThisUser.size() + "\n");
			}
			// System.out.println("Inside getUniqueActivityIDs: uniqueActIDs.size()=" + uniqueActIDs.size());

			if (writeToFile)
			{
				WritingToFile.writeToNewFile(sb.toString(), Constant.getOutputCoreResultsPath() + fileName);// "NumOfUniquePDValPerUser.csv");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniquePDValsPerUser;
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
		WritingToFile.appendLineToFileAbsolute(
				"User" + delimiter + ActivityObject.getHeaderForStringAllGowallaTSWithNameForHeaded(delimiter) + "\n",
				abFileNameToWrite);

		for (Entry<String, LinkedHashMap<Date, Timeline>> userData : usersCleanedDayTimelines.entrySet())
		{
			String user = userData.getKey();

			for (Entry<Date, Timeline> timeline : userData.getValue().entrySet())
			{
				StringBuilder sbForThisTimeline = new StringBuilder();
				for (ActivityObject ao : timeline.getValue().getActivityObjectsInTimeline())
				{
					sbForThisTimeline
							.append(user + delimiter + ao.toStringAllGowallaTSWithNameForHeaded(delimiter) + "\n");
				}
				WritingToFile.appendLineToFileAbsolute(sbForThisTimeline.toString(), abFileNameToWrite);
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
		WritingToFile.appendLineToFileAbsolute(
				"User" + delimiter + ActivityObject.getHeaderForStringAllGowallaTSWithNameForHeaded(delimiter) + "\n",
				abFileNameToWrite);

		for (Entry<String, Timeline> userData : usersTimelines.entrySet())
		{
			String user = userData.getKey();

			StringBuilder sbForThisTimeline = new StringBuilder();
			for (ActivityObject ao : userData.getValue().getActivityObjectsInTimeline())
			{
				sbForThisTimeline.append(user + delimiter + ao.toStringAllGowallaTSWithNameForHeaded(delimiter) + "\n");
			}
			WritingToFile.appendLineToFileAbsolute(sbForThisTimeline.toString(), abFileNameToWrite);
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
		WritingToFile.appendLineToFileAbsolute(
				"User" + delimiter + ActivityObject.getHeaderForStringAllGowallaTSWithNameForHeaded(delimiter) + "\n",
				abFileNameToWrite);

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> userData : trainTestTimelinesForAllUsersDW.entrySet())
		{
			String user = userData.getKey();

			for (Entry<Date, Timeline> timeline : userData.getValue().get(1).entrySet())
			{
				StringBuilder sbForThisTimeline = new StringBuilder();
				for (ActivityObject ao : timeline.getValue().getActivityObjectsInTimeline())
				{
					sbForThisTimeline
							.append(user + delimiter + ao.toStringAllGowallaTSWithNameForHeaded(delimiter) + "\n");
				}
				WritingToFile.appendLineToFileAbsolute(sbForThisTimeline.toString(), abFileNameToWrite);
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

		WritingToFile.writeToNewFile(sb.toString(), absFileNameToUse);
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
		WritingToFile.writeToNewFile(sb.toString(), absFileNameToUse);
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
