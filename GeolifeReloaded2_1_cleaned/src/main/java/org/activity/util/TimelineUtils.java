package org.activity.util;

import java.io.BufferedWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.CheckinEntry;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.objects.UserDayTimeline;
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
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < ActivityObjects.size(); i++)
		{
			set.add(ActivityObjects.get(i).getActivityName().trim());
		}
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
				Date date = DateTimeUtils.getDate(checkin.getKey());// (Date)
																	// activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension",
																	// "Date"); // start
				// date
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

		for (Map.Entry<String, TreeMap<Timestamp, T>> perUserCheckinEntry : timewiseMap.entrySet()) // Iterate over
																									// Users
		{
			String userID = perUserCheckinEntry.getKey();
			TreeMap<Timestamp, T> checkinEntriesForThisUser = perUserCheckinEntry.getValue();

			TreeMap<Date, ArrayList<T>> daywiseForThisUser = new TreeMap<>();

			for (Map.Entry<Timestamp, T> checkin : checkinEntriesForThisUser.entrySet()) // iterare over activity events
																							// for this user
			{
				LocalDate ldate = DateTimeUtils.getLocalDate(checkin.getKey(), tz);// converting to ldate to removing
																					// hidden time.

				Date date = Date.valueOf(ldate);// ldate.get//DateTimeUtils.getDate(checkin.getKey());// (Date)
												// activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension",
												// "Date"); // start
				// date
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
	public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> createUserTimelinesFromCheckinEntriesGowalla(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> checkinEntries,
			LinkedHashMap<String, LocationGowalla> locationObjects)
	{
		long ct1 = System.currentTimeMillis();

		System.out.println("starting createUserTimelinesFromCheckinEntriesGowalla");
		LinkedHashMap<String, TreeMap<Date, ArrayList<CheckinEntry>>> checkinEntriesDatewise = convertTimewiseMapToDatewiseMap2(
				checkinEntries, Constant.getTimeZone());

		LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> activityObjectsDatewise = convertCheckinEntriesToActivityObjectsGowalla(
				checkinEntriesDatewise, locationObjects);

		LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userDaytimelines = new LinkedHashMap<>();

		for (Entry<String, TreeMap<Date, ArrayList<ActivityObject>>> userEntry : activityObjectsDatewise.entrySet())
		{
			String userID = userEntry.getKey();
			LinkedHashMap<Date, UserDayTimeline> dayTimelines = new LinkedHashMap<>();

			for (Entry<Date, ArrayList<ActivityObject>> dateEntry : userEntry.getValue().entrySet())

			{
				Date date = dateEntry.getKey();
				ArrayList<ActivityObject> activityObjectsInDay = dateEntry.getValue();
				String dateID = date.toString();
				String dayName = DateTimeUtils.getWeekDayFromWeekDayInt(date.getDay());// DateTimeUtils.getWeekDayFromWeekDayInt(date.getDayOfWeek().getValue());
				dayTimelines.put(date, new UserDayTimeline(activityObjectsInDay, dateID, userID, dayName, date));
			}

			userDaytimelines.put(userID, dayTimelines);
		}

		long ct4 = System.currentTimeMillis();
		System.out.println(
				"created timelines for" + userDaytimelines.size() + " users in " + ((ct4 - ct1) / 1000) + " seconds");

		System.out.println("exiting createUserTimelinesFromCheckinEntriesGowalla");
		return userDaytimelines;
	}

	/**
	 * INCOMPLETE
	 * 
	 * @param usersDayTimelines
	 */
	public static void countConsecutiveSimilarActivities(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelines)
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

		for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userE : usersDayTimelines.entrySet())
		{
			String user = userE.getKey();

			for (Entry<Date, UserDayTimeline> dateE : userE.getValue().entrySet())
			{
				String date = dateE.getKey().toString();

				String prevActivityName = "";
				Timestamp prevActivityStartTimestamp = null;
				int numOfConsecutives = 0;
				long timeDiff = 0;
				for (ActivityObject aos : dateE.getValue().getActivityObjectsInDay())
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
			System.out.print(catID + ",");
		}

		System.out.println("Exiting org.activity.util.TimelineUtils.getEmptyMapOfCatIDs(String)");
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
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelines, String commonPathToWrite,
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

		StringBuilder sbAllDistanceInM = new StringBuilder();
		StringBuilder sbAllDurationFromNext = new StringBuilder();

		StringBuilder sbEnumerateAllCats = new StringBuilder();// write all catid sequentially userwise

		long aoCount = 0;
		try
		{
			for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userE : usersDayTimelines.entrySet())
			{
				String user = userE.getKey();

				ArrayList<Integer> userLengthConsecutivesValues = new ArrayList<Integer>();

				String prevActivityName = "";// Timestamp prevActivityStartTimestamp = null;

				int numOfConsecutives = 1;// long timeDiff = 0;

				StringBuilder distanceFromNextSeq = new StringBuilder(); // only write >1 consecs
				StringBuilder durationFromNextSeq = new StringBuilder();// only write >1 consecs

				for (Entry<Date, UserDayTimeline> dateE : userE.getValue().entrySet())
				{
					String date = dateE.getKey().toString();
					for (ActivityObject aos : dateE.getValue().getActivityObjectsInDay())
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
							distanceFromNextSeq.append(user + "," + ts + "," + activityName + "," + actCatName + ","
									+ String.valueOf(distNext) + "\n");
							durationFromNextSeq.append(user + "," + ts + "," + activityName + "," + actCatName + ","
									+ String.valueOf(durationNext) + "\n");
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
									sbAllDistanceInM.append(distanceFromNextSeq.toString());// + "\n");
									sbAllDurationFromNext.append(durationFromNextSeq.toString());// + "\n");
									// $$System.out.println("appending to dista, duration");
								}
								// else
								// {
								distanceFromNextSeq.setLength(0);
								durationFromNextSeq.setLength(0);
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
							WritingToFile.appendLineToFileAbsolute(sbAllDistanceInM.toString(),
									commonPathToWrite + "sbAllDistanceInM.csv");
							sbAllDistanceInM.setLength(0);

							WritingToFile.appendLineToFileAbsolute(sbAllDurationFromNext.toString(),
									commonPathToWrite + "sbAllDurationFromNext.csv");
							sbAllDurationFromNext.setLength(0);
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
				WritingToFile.appendLineToFileAbsolute(sbAllDistanceInM.toString(),
						commonPathToWrite + "sbAllDistanceInM.csv");
				sbAllDistanceInM.setLength(0);

				WritingToFile.appendLineToFileAbsolute(sbAllDurationFromNext.toString(),
						commonPathToWrite + "sbAllDurationFromNext.csv");
				sbAllDurationFromNext.setLength(0);
				/////////////////

			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Num of aos read = " + aoCount);
		writeConsectiveCountsEqualLength(catIDLengthConsecutives, catIDNameDictionary,
				commonPathToWrite + "CatwiseConsecCountsEqualLength.csv", true, true);
		writeConsectiveCountsEqualLength(userLengthConsecutives, catIDNameDictionary,
				commonPathToWrite + "UserwiseConsecCountsEqualLength.csv", false, false);

		WritingToFile.appendLineToFileAbsolute(sbEnumerateAllCats.toString(),
				commonPathToWrite + "ActualOccurrenceOfCatsSeq.csv");
		return catIDLengthConsecutives;

	}

	/**
	 * 
	 * @param map
	 * @param catIDNameDictionary
	 * @param absfileNameToUse
	 */
	public static void writeConsectiveCounts(LinkedHashMap<String, ArrayList<Integer>> map,
			TreeMap<Integer, String> catIDNameDictionary, String absfileNameToUse)
	{
		int numOfCatIDsInMap = 0;
		try
		{
			BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(absfileNameToUse);
			for (Entry<String, ArrayList<Integer>> entry : map.entrySet())
			{
				String catID = entry.getKey();
				ArrayList<Integer> consecCounts = entry.getValue();

				if (consecCounts.size() != 0)
				{
					StringBuilder sb = new StringBuilder();
					sb.append(catID + ":" + catIDNameDictionary.get(Integer.valueOf(catID)));

					numOfCatIDsInMap += 1;

					for (Integer t : consecCounts)
					{
						sb.append("," + t);
					}

					bw.write(sb.toString() + "\n");
				}
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Num of catIDs in dictionary = " + catIDNameDictionary.size());
		System.out.println("Num of catIDs in working dataset = " + numOfCatIDsInMap);
	}

	/**
	 * Make each row of equal length (i.e., same num of columns by filling in with "NA"s. This is to facilitate reading
	 * data in R.
	 * 
	 * @param map
	 * @param catIDNameDictionary
	 * @param absfileNameToUse
	 * @param insertNAs
	 * @param writeCatName
	 */
	public static void writeConsectiveCountsEqualLength(LinkedHashMap<String, ArrayList<Integer>> map,
			TreeMap<Integer, String> catIDNameDictionary, String absfileNameToUse, boolean insertNAs,
			boolean writeCatName)
	{
		int maxNumOfVals = -1; // max consec counts over all cat ids

		// find the max size
		maxNumOfVals = (map.entrySet().stream().mapToInt(e -> e.getValue().size()).max()).getAsInt();
		// maxNumOfVals += 1; // 1 additional column for header
		// for (Entry<String, ArrayList<Integer>> e : map.entrySet())
		// {
		// // if(e.getValue().size()>maxSizeOfArrayList)
		// }

		int numOfCatIDsInMap = 0;
		try
		{
			BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(absfileNameToUse);
			for (Entry<String, ArrayList<Integer>> entry : map.entrySet())
			{
				String catID = entry.getKey();
				ArrayList<Integer> consecCounts = entry.getValue();

				if (consecCounts.size() != 0)
				{
					int numOfNAsToBeInserted = maxNumOfVals - consecCounts.size();
					StringBuilder sb = new StringBuilder();

					if (writeCatName)
					{
						sb.append(catID + ":" + catIDNameDictionary.get(Integer.valueOf(catID)));
					}
					else
					{
						sb.append(catID + ":");
					}
					numOfCatIDsInMap += 1;

					for (Integer t : consecCounts)
					{
						sb.append("," + t);
					}

					if (insertNAs)
					{
						for (int i = 0; i < numOfNAsToBeInserted; i++)
						{
							sb.append(",NA");
						}
					}

					bw.write(sb.toString() + "\n");
				}
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Num of catIDs in dictionary = " + catIDNameDictionary.size());
		System.out.println("Num of catIDs in working dataset = " + numOfCatIDsInMap);
		System.out.println("maxNumOfVals = " + maxNumOfVals);
	}
	// LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> activityObjectsDatewise = new LinkedHashMap<>();
	//
	// // convert checkinentries to activity objects
	// for (Entry<String, TreeMap<Date, ArrayList<CheckinEntry>>> userEntry : checkinEntriesDatewise.entrySet()) // over
	// users
	// {
	// String userID = userEntry.getKey();
	// TreeMap<Date, ArrayList<ActivityObject>> dayWiseForThisUser = new TreeMap<>();
	//
	// for (Entry<Date, ArrayList<CheckinEntry>> dateEntry : userEntry.getValue().entrySet()) // over dates
	// {
	// Date date = dateEntry.getKey();
	// ArrayList<ActivityObject> activityObjectsForThisUserThisDate = new ArrayList<>();
	//
	// for (CheckinEntry e : dateEntry.getValue())// over checkins
	// {
	// int activityID = e.getActivityID();
	// int locationID = e.getLocationID();
	//
	// String activityName = "";//
	// String locationName = "";//
	// Timestamp startTimestamp = e.getTimestamp();
	// String startLatitude = e.getStartLatitude();
	// String startLongitude = e.getStartLongitude();
	// String startAltitude = "";//
	// String userIDInside = e.getUserID();
	//
	// // sanity check start
	// if (userIDInside.equals(userID) == false)
	// {
	// System.err.println("Sanity check failed in createUserTimelinesFromCheckinEntriesGowalla()");
	// }
	// // sanity check end
	//
	// LocationGowalla loc = locationObjects.get(String.valueOf(locationID));
	// int photos_count = loc.getPhotos_count();
	// int checkins_count = loc.getCheckins_count();
	// int users_count = loc.getUsers_count();
	// int radius_meters = loc.getRadius_meters();
	// int highlights_count = loc.getHighlights_count();
	// int items_count = loc.getItems_count();
	// int max_items_count = loc.getMax_items_count();
	//
	// ActivityObject ao = new ActivityObject(activityID, locationID, activityName, locationName, startTimestamp,
	// startLatitude, startLongitude, startAltitude, userID, photos_count, checkins_count, users_count, radius_meters,
	// highlights_count, items_count, max_items_count);
	//
	// activityObjectsForThisUserThisDate.add(ao);
	// }
	// dayWiseForThisUser.put(date, activityObjectsForThisUserThisDate);
	// }
	// activityObjectsDatewise.put(userID, dayWiseForThisUser);
	// }
	//
	// System.out.println("inside createUserTimelinesFromCheckinEntriesGowalla");
	//
	// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines =
	// new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
	//
	// // userid, usertimeline
	// LinkedHashMap<String, ArrayList<ActivityObject>> perUserActivityEvents = new LinkedHashMap<String,
	// ArrayList<ActivityObject>>();
	//
	// for (Map.Entry<String, TreeMap<Timestamp, CheckinEntry>> perUserCheckinEntry : checkinEntries.entrySet()) //
	// Iterate over Users
	// {
	//
	// String userID = perUserCheckinEntry.getKey();
	// TreeMap<Timestamp, CheckinEntry> checkinEntriesForThisUser = perUserCheckinEntry.getValue();
	//
	// System.out.println("user:" + userID + " #CheckinEntries =" + checkinEntriesForThisUser.size());
	//
	// LinkedHashMap<Date, ArrayList<ActivityObject>> perDateActivityObjectsForThisUser =
	// new LinkedHashMap<Date, ArrayList<ActivityObject>>();
	//
	// for (Map.Entry<Timestamp, CheckinEntry> checkin : checkinEntriesForThisUser.entrySet()) // iterare over activity
	// events for this user
	// {
	// Timestamp ts = checkin.getKey();
	// CheckinEntry checkinEntry = checkin.getValue();
	// Date date = DateTimeUtils.getDate(ts);// (Date)
	// activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension", "Date"); // start date
	//
	// if (!(perDateActivityObjectsForThisUser.containsKey(date)))
	// {
	// perDateActivityObjectsForThisUser.put(date, new ArrayList<ActivityObject>());
	// }
	//
	// perDateActivityObjectsForThisUser.get(date).add(activityEventsForThisUser.get(i));
	// }
	//
	// perDateActivityEventsForThisUser has been created now.

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
			LinkedHashMap<String, LocationGowalla> locationObjects)
	{
		LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> activityObjectsDatewise = new LinkedHashMap<>();
		System.out.println("starting convertCheckinEntriesToActivityObjectsGowalla");

		// Set<String> setOfCatIDsofAOs = new TreeSet<String>();

		// convert checkinentries to activity objects
		for (Entry<String, TreeMap<Date, ArrayList<CheckinEntry>>> userEntry : checkinEntriesDatewise.entrySet()) // over
																													// users
		{
			String userID = userEntry.getKey();
			TreeMap<Date, ArrayList<ActivityObject>> dayWiseForThisUser = new TreeMap<>();

			for (Entry<Date, ArrayList<CheckinEntry>> dateEntry : userEntry.getValue().entrySet()) // over dates
			{
				Date date = dateEntry.getKey();
				ArrayList<ActivityObject> activityObjectsForThisUserThisDate = new ArrayList<>();

				// System.out.println(
				// "--* user:" + userID + " date:" + date.toString() + " has " + dateEntry.getValue().size() + "
				// checkinentries");

				for (CheckinEntry e : dateEntry.getValue())// over checkins
				{
					int activityID = e.getActivityID();
					int locationID = e.getLocationID();

					String activityName = String.valueOf(e.getActivityID());// "";// TODO
					String locationName = "";//
					Timestamp startTimestamp = e.getTimestamp();
					String startLatitude = e.getStartLatitude();
					String startLongitude = e.getStartLongitude();
					String startAltitude = "";//
					String userIDInside = e.getUserID();
					double distaneInMFromNext = e.getDistanceInMetersFromNext();
					long durationInSecFromNext = e.getDurationInSecsFromNext();

					// sanity check start
					if (userIDInside.equals(userID) == false)
					{
						System.err.println(
								"Error: sanity check failed in createUserTimelinesFromCheckinEntriesGowalla()");
					}
					// sanity check end

					LocationGowalla loc = locationObjects.get(String.valueOf(locationID));
					int photos_count = loc.getPhotos_count();
					int checkins_count = loc.getCheckins_count();
					int users_count = loc.getUsers_count();
					int radius_meters = loc.getRadius_meters();
					int highlights_count = loc.getHighlights_count();
					int items_count = loc.getItems_count();
					int max_items_count = loc.getMax_items_count();

					ActivityObject ao = new ActivityObject(activityID, locationID, activityName, locationName,
							startTimestamp, startLatitude, startLongitude, startAltitude, userID, photos_count,
							checkins_count, users_count, radius_meters, highlights_count, items_count, max_items_count,
							e.getWorkingLevelCatIDs(), distaneInMFromNext, durationInSecFromNext);
					// setOfCatIDsofAOs.add(ao.getActivityID());
					activityObjectsForThisUserThisDate.add(ao);
				}
				dayWiseForThisUser.put(date, activityObjectsForThisUserThisDate);
			}
			activityObjectsDatewise.put(userID, dayWiseForThisUser);
		}
		System.out.println("exiting convertCheckinEntriesToActivityObjectsGowalla");
		return activityObjectsDatewise;
	}

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
	// /// TODO dayTimelines.put(date, new UserDayTimeline(perDateActivityEventsForThisUserEntry.getValue(), date));
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

	/**
	 * Creates user day timelines from the given list of Activity Objects.
	 * 
	 * Activity events ---> day timelines (later, not here)---> user timelines
	 * 
	 * @param allActivityEvents
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> createUserTimelinesFromActivityObjects(
			ArrayList<ActivityObject> allActivityEvents)
	{
		LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines = new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
		// userid, usertimeline
		LinkedHashMap<String, ArrayList<ActivityObject>> perUserActivityEvents = new LinkedHashMap<String, ArrayList<ActivityObject>>();

		System.out.println("inside createUserTimelinesFromActivityEvents");

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

		for (Map.Entry<String, ArrayList<ActivityObject>> perUserActivityEventsEntry : perUserActivityEvents.entrySet()) // Iterate
																															// over
																															// Users
		{

			System.out.println("for user:" + perUserActivityEventsEntry.getKey() + " number of activity-objects ="
					+ perUserActivityEventsEntry.getValue().size());

			String userID = perUserActivityEventsEntry.getKey();

			ArrayList<ActivityObject> activityEventsForThisUser = perUserActivityEventsEntry.getValue();

			LinkedHashMap<Date, ArrayList<ActivityObject>> perDateActivityEventsForThisUser = new LinkedHashMap<Date, ArrayList<ActivityObject>>();

			for (int i = 0; i < activityEventsForThisUser.size(); i++) // iterare over activity events for this user
			{
				Date date = (Date) activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension",
						"Date"); // start date

				if (!(perDateActivityEventsForThisUser.containsKey(date)))
				{
					perDateActivityEventsForThisUser.put(date, new ArrayList<ActivityObject>());
				}

				perDateActivityEventsForThisUser.get(date).add(activityEventsForThisUser.get(i));
			}

			// perDateActivityEventsForThisUser has been created now.

			LinkedHashMap<Date, UserDayTimeline> dayTimelines = new LinkedHashMap<Date, UserDayTimeline>();

			for (Map.Entry<Date, ArrayList<ActivityObject>> perDateActivityEventsForThisUserEntry : perDateActivityEventsForThisUser
					.entrySet())
			{
				Date date = perDateActivityEventsForThisUserEntry.getKey();

				// $$ dayTimelines.put(date, new UserDayTimeline(perDateActivityEventsForThisUserEntry.getValue(),
				// date));

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
	public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> cleanDayTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	{
		System.out.println("Inside cleanDayTimelines(): total num of users before cleaning = " + usersTimelines.size());
		long ct = System.currentTimeMillis();
		LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> cleanedUserDayTimelines = new LinkedHashMap<>();

		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			if (Constant.verboseTimelineCleaning)
			{
				System.out.println("for user: " + usersTimelinesEntry.getKey() + "\n");
			}

			// LinkedHashMap<Date, UserDayTimeline> cleanedDayTimelines =
			// TimelineUtilities.cleanUserDayTimelines(usersTimelinesEntry.getValue());

			LinkedHashMap<Date, UserDayTimeline> cleanedDayTimelines = TimelineUtils.cleanUserDayTimelines(
					usersTimelinesEntry.getValue(), Constant.getCommonPath() + "LogCleanedDayTimelines_",
					usersTimelinesEntry.getKey());

			if (cleanedDayTimelines.size() > 0)
			{
				cleanedUserDayTimelines.put(usersTimelinesEntry.getKey(), cleanedDayTimelines);
			}

			// System.out.println();
		}
		System.out.println("\ttotal num of users after cleaning = " + cleanedUserDayTimelines.size());

		if (Constant.verboseTimelineCleaning)
		{
			System.out.println("exiting cleanDayTimelines() ");
		}

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
	public static LinkedHashMap<Date, UserDayTimeline> cleanUserDayTimelines(
			LinkedHashMap<Date, UserDayTimeline> userDayTimelines)
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
	public static LinkedHashMap<Date, UserDayTimeline> cleanUserDayTimelines(
			LinkedHashMap<Date, UserDayTimeline> userDayTimelines, String logFileNamePhrase, String user)
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
	 * 
	 * @param userAllDatesTimeslines
	 * @param percentageInTraining
	 * @return
	 */
	public static List<LinkedHashMap<Date, UserDayTimeline>> splitTestTrainingTimelines(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines, double percentageInTraining)
	{
		ArrayList<LinkedHashMap<Date, UserDayTimeline>> trainTestTimelines = new ArrayList<LinkedHashMap<Date, UserDayTimeline>>();

		int numberOfValidDays = 0;

		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().containsAtLeastOneValidActivity() == false)
			{ // if the day timelines contains no valid activity, then don't consider it for training or test
				System.err.println(
						"Error in splitTestTrainingTimelines: 45: userAllDatesTimeslines contains a day timeline with no valid activity, but we already tried to remove it");
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

		LinkedHashMap<Date, UserDayTimeline> userTrainingTimelines = new LinkedHashMap<Date, UserDayTimeline>();
		LinkedHashMap<Date, UserDayTimeline> userTestTimelines = new LinkedHashMap<Date, UserDayTimeline>();

		int count = 1;
		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
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
			System.err.println(
					"Error in splitTestTrainingTimelines: there are more than two (train+test) timelines in returned result, there are "
							+ trainTestTimelines.size() + " timelines.");
			System.exit(-43);
		}

		return trainTestTimelines;
	}

	public static void traverseUserTimelines(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines)
	{
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> userEntry : userTimelines.entrySet()) // Iterate
																											// over
																											// Users
		{
			System.out.println("User ID =" + userEntry.getKey());

			System.out.println("Number of day timelines for this user=" + userEntry.getValue().size());

			for (Map.Entry<Date, UserDayTimeline> dayTimelineEntry : userEntry.getValue().entrySet())
			{
				System.out.println("Date: " + dayTimelineEntry.getKey());
				// dayTimelineEntry.getValue().printActivityEventNamesInSequence();
				// 21Oct dayTimelineEntry.getValue().printActivityEventNamesWithTimestampsInSequence();
				System.out.println();
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
	public static HashMap<String, ArrayList<ActivityObject>> createTimelinesFromJsonArray(JSONArray jsonArray)
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

	public static LinkedHashMap<Date, UserDayTimeline> removeWeekendDayTimelines(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines)
	{
		boolean verbose = Constant.verboseTimelineCleaning;
		LinkedHashMap<Date, UserDayTimeline> datesTimelinesPruned = userAllDatesTimeslines;
		ArrayList<Date> datesToRemove = new ArrayList<Date>();
		if (verbose) System.out.print("Weekends to remove:");// + entry.getKey());
		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getKey().getDay() == 0 || entry.getKey().getDay() == 6)
			{
				datesToRemove.add(entry.getKey());
				if (verbose) System.out.print("," + entry.getKey());
			}
		}
		if (verbose) System.out.println("");
		for (Date dateToRemove : datesToRemove)
		{
			// System.out.print(datesToRemove.toString()+",");
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (Constant.verboseTimelineCleaning)
		{
			System.out.println("Num of days removed for removeWeekendDayTimelines =" + datesToRemove.size());
		}
		// System.out.println("Count of weekends removed ="+datesToRemove.size()); //THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE ENTRIES, UNCOMMENT TO SEE, HOWEVER IT DOES NOT
		// AFFECT OUR PURPORSE
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
	public static LinkedHashMap<Date, UserDayTimeline> removeWeekendDayTimelines(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines, String logFileName, String user)
	{
		// boolean verbose = Constant.verboseTimelineCleaning;
		LinkedHashMap<Date, UserDayTimeline> datesTimelinesPruned = userAllDatesTimeslines;
		ArrayList<Date> datesToRemove = new ArrayList<Date>();
		StringBuffer log = new StringBuffer();

		// if (verbose)
		// System.out.print("Weekends to remove:");// + entry.getKey());
		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getKey().getDay() == 0 || entry.getKey().getDay() == 6)
			{
				datesToRemove.add(entry.getKey());
				log.append(user + "," + entry.getKey() + "," + (entry.getKey().getDay()) + "\n");
				// if (verbose)
				// System.out.print("," + entry.getKey());
			}
		}
		// if (verbose)
		// System.out.println("");
		for (Date dateToRemove : datesToRemove)
		{
			// System.out.print(datesToRemove.toString()+",");
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (Constant.verboseTimelineCleaning)
		{
			System.out.println("Num of days removed for removeWeekendDayTimelines =" + datesToRemove.size());
		}
		// System.out.println("Count of weekends removed ="+datesToRemove.size()); //THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE ENTRIES, UNCOMMENT TO SEE, HOWEVER IT DOES NOT
		// AFFECT OUR PURPORSE
		WritingToFile.appendLineToFileAbsolute(log.toString(), logFileName);
		return datesTimelinesPruned;
	}

	public static LinkedHashMap<Date, UserDayTimeline> removeWeekdaysDayTimelines(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines)
	{
		boolean verbose = Constant.verboseTimelineCleaning;
		LinkedHashMap<Date, UserDayTimeline> datesTimelinesPruned = userAllDatesTimeslines;
		ArrayList<Date> datesToRemove = new ArrayList<Date>();

		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (!(entry.getKey().getDay() == 0 || entry.getKey().getDay() == 6))
			{
				datesToRemove.add(entry.getKey());
				if (verbose) System.out.println("Weekdays to remove=" + entry.getKey());
			}
		}

		// System.out.println("Weekend dates to remove");
		for (Date dateToRemove : datesToRemove)
		{
			// System.out.print(datesToRemove.toString()+",");
			datesTimelinesPruned.remove(dateToRemove);
		}

		// System.out.println("Count of weekends removed ="+datesToRemove.size()); //THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE ENTRIES, UNCOMMENT TO SEE, HOWEVER IT DOES NOT
		// AFFECT OUR PURPORSE
		return datesTimelinesPruned;
	}

	/**
	 * Removes day timelines with no valid activities in the day
	 * 
	 * @param userAllDatesTimeslines
	 * @return
	 */
	public static LinkedHashMap<Date, UserDayTimeline> removeDayTimelinesWithNoValidAct(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines)
	{
		// boolean verbose = Constant.verbose2;
		LinkedHashMap<Date, UserDayTimeline> datesTimelinesPruned = userAllDatesTimeslines;
		ArrayList<Date> datesToRemove = new ArrayList<Date>();

		if (Constant.verboseTimelineCleaning)
			System.out.print("Invalid days to remove for no valid activities in the day:");

		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().containsAtLeastOneValidActivity() == false)
			{
				datesToRemove.add(entry.getKey());
				if (Constant.verboseTimelineCleaning) System.out.print("," + entry.getKey());
			}
		}

		if (Constant.verboseTimelineCleaning) System.out.println("");

		/**
		 * @TODO check and change if the two loops in this method can be replace with one
		 */

		for (Date dateToRemove : datesToRemove)
		{
			// System.out.print(datesToRemove.toString()+",");
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (Constant.verboseTimelineCleaning)
		{
			System.out.println("Num of days removed for removeDayTimelinesWithNoValidAct =" + datesToRemove.size());
		}
		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());//THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE
		// ENTRIES,
		// UNCOMMENT TO SEE,
		// HOWEVER IT DOES NOT AFFECT OUR PURPORSE
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
	public static LinkedHashMap<Date, UserDayTimeline> removeDayTimelinesWithNoValidAct(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines, String logFileName, String user)
	{
		// boolean verbose = Constant.verbose2;
		LinkedHashMap<Date, UserDayTimeline> datesTimelinesPruned = userAllDatesTimeslines;
		ArrayList<Date> datesToRemove = new ArrayList<Date>();

		StringBuffer log = new StringBuffer();
		// StringBuffer log = new StringBuffer("User,DateToRemoveWithNoValidAct\n");
		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().containsAtLeastOneValidActivity() == false)
			{
				datesToRemove.add(entry.getKey());

				log.append(user + "," + entry.getKey() + "\n");
				// if (Constant.verboseTimelineCleaning)
				// System.out.print("," + entry.getKey());
			}
		}

		// log.append("\n");
		// if (Constant.verboseTimelineCleaning)
		// System.out.println("");

		/**
		 * @TODO check and change if the two loops in this method can be replace with one
		 */

		for (Date dateToRemove : datesToRemove)
		{
			// System.out.print(datesToRemove.toString()+",");
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (Constant.verboseTimelineCleaning)
		{
			System.out.println("Num of days removed for removeDayTimelinesWithNoValidAct =" + datesToRemove.size());
		}
		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());//THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE
		// ENTRIES,
		// UNCOMMENT TO SEE,
		// HOWEVER IT DOES NOT AFFECT OUR PURPORSE

		WritingToFile.appendLineToFileAbsolute(log.toString(), logFileName);
		return datesTimelinesPruned;
	}

	/**
	 * Removes day timelines which have <=1 distinct valid activities in a day.
	 * 
	 * @param userAllDatesTimeslines
	 * @return HashMap of day timelines <Date, UserDayTimeline>
	 */
	public static LinkedHashMap<Date, UserDayTimeline> removeDayTimelinesWithOneOrLessDistinctValidAct(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines)
	{
		boolean verbose = Constant.verboseTimelineCleaning;
		LinkedHashMap<Date, UserDayTimeline> datesTimelinesPruned = userAllDatesTimeslines;
		ArrayList<Date> datesToRemove = new ArrayList<Date>();
		if (verbose) System.out.print("Invalid days to remove for TimelinesWithLessThanOneDistinctValidAct:");// +
																												// entry.getKey());
		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().countNumberOfValidDistinctActivities() <= 1)
			{
				datesToRemove.add(entry.getKey());
				if (verbose) System.out.print("," + entry.getKey());
			}
		}
		if (verbose) System.out.println("");
		for (Date dateToRemove : datesToRemove)
		{
			// System.out.print(datesToRemove.toString()+",");
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (Constant.verboseTimelineCleaning)
		{
			System.out.println(
					"Num of days removed for removeDayTimelinesWithOneOrLessDistinctValidAct =" + datesToRemove.size());
		}
		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());//THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE
		// ENTRIES,
		// UNCOMMENT TO SEE,
		// HOWEVER IT DOES NOT AFFECT OUR PURPORSE
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
	public static LinkedHashMap<Date, UserDayTimeline> removeDayTimelinesWithOneOrLessDistinctValidAct(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines, String logFileName, String user)
	{
		// boolean verbose = Constant.verboseTimelineCleaning;
		LinkedHashMap<Date, UserDayTimeline> datesTimelinesPruned = userAllDatesTimeslines;
		ArrayList<Date> datesToRemove = new ArrayList<Date>();

		StringBuffer log = new StringBuffer();
		// if (verbose)
		// System.out.print("Invalid days to remove for TimelinesWithLessThanOneDistinctValidAct:");// +
		// entry.getKey());

		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().countNumberOfValidDistinctActivities() <= 1)
			{
				datesToRemove.add(entry.getKey());
				log.append(user + "," + entry.getKey() + "," + entry.getValue().countNumberOfValidDistinctActivities()
						+ "\n");
				// if (verbose)
				// System.out.print("," + entry.getKey());
			}
		}
		// if (verbose)
		// System.out.println("");
		for (Date dateToRemove : datesToRemove)
		{
			// System.out.print(datesToRemove.toString()+",");
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (Constant.verboseTimelineCleaning)
		{
			System.out.println(
					"Num of days removed for removeDayTimelinesWithOneOrLessDistinctValidAct =" + datesToRemove.size());
		}
		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());//THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE
		// ENTRIES,
		// UNCOMMENT TO SEE,
		// HOWEVER IT DOES NOT AFFECT OUR PURPORSE
		WritingToFile.appendLineToFileAbsolute(log.toString(), logFileName);
		return datesTimelinesPruned;
	}

	/**
	 * Removes day timelines which have <=1 distinct valid activities in a day.
	 * 
	 * @param userAllDatesTimeslines
	 * @return HashMap of day timelines <Date, UserDayTimeline>
	 */
	public static LinkedHashMap<Date, UserDayTimeline> removeDayTimelinesLessDistinctValidAct(
			LinkedHashMap<Date, UserDayTimeline> userAllDatesTimeslines, int lowerLimit)
	{
		boolean verbose = Constant.verboseTimelineCleaning;
		LinkedHashMap<Date, UserDayTimeline> datesTimelinesPruned = userAllDatesTimeslines;
		ArrayList<Date> datesToRemove = new ArrayList<Date>();

		if (verbose) System.out.print("Invalid days to remove for removeDayTimelinesLessDistinctValidAct:");// +
																											// entry.getKey());

		for (Map.Entry<Date, UserDayTimeline> entry : userAllDatesTimeslines.entrySet())
		{
			if (entry.getValue().countNumberOfValidDistinctActivities() < lowerLimit)
			{
				datesToRemove.add(entry.getKey());
				if (verbose) System.out.print("," + entry.getKey());
			}
		}
		if (verbose) System.out.println("");
		for (Date dateToRemove : datesToRemove)
		{
			// System.out.print(datesToRemove.toString()+",");
			datesTimelinesPruned.remove(dateToRemove);
		}

		if (Constant.verboseTimelineCleaning)
		{
			System.out.println("Num of days removed for removeDayTimelinesLessDistinctValidAct (lowerlimit ="
					+ lowerLimit + ") =" + datesToRemove.size());
		}
		// System.out.println("Total number of days="+userAllDatesTimeslines.size()+", Count of invalid days removed
		// ="+datesToRemove.size());//THERE IS SOME BUG, THE ARRAY HAS
		// DUPLICATE
		// ENTRIES,
		// UNCOMMENT TO SEE,
		// HOWEVER IT DOES NOT AFFECT OUR PURPORSE
		return datesTimelinesPruned;
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
	public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> rearrangeDayTimelinesByGivenOrder(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> map, int[] orderKeys)
	{
		LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> rearranged = new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();

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
	public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> rearrangeDayTimelinesOrderForDataset(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userMaps)
	{
		System.out.println("rearrangeDayTimelinesOrderForDataset received: " + userMaps.size() + " users");
		long ct1 = System.currentTimeMillis();
		LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> rearranged = new LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>();
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
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelinesOriginal)
	{
		boolean hasDuplicateDates = false;
		String usersWithDuplicateDates = new String();

		for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userEntry : usersDayTimelinesOriginal.entrySet())
		{
			String user = userEntry.getKey();

			ArrayList<String> dateAsStringList = new ArrayList<>();
			Set<String> dateAsStringSet = new HashSet();

			for (Entry<Date, UserDayTimeline> dateEntry : userEntry.getValue().entrySet())
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

		return new Pair(hasDuplicateDates, usersWithDuplicateDates);
	}

	/**
	 * Removes timelines with less than lowerLimit activity objects in a day
	 * 
	 * @param usersDayTimelinesOriginal
	 * @param lowerLimit
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> removeDayTimelinesWithLessAct(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelinesOriginal, int lowerLimit,
			String fileNameForLog)
	{
		System.out.println("Inside removeDayTimelinesWithLessValidAct");
		LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> result = new LinkedHashMap<>();
		StringBuffer log = new StringBuffer();
		StringBuffer datesRemoved = new StringBuffer();
		log.append("User, #days(daytimelines) removed\n");

		for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userEntry : usersDayTimelinesOriginal.entrySet())
		{
			String user = userEntry.getKey();

			LinkedHashMap<Date, UserDayTimeline> toKeepTimelines = new LinkedHashMap<>();

			int countOfDatesRemovedForThisUser = 0;
			for (Entry<Date, UserDayTimeline> dateEntry : userEntry.getValue().entrySet())
			{
				if (dateEntry.getValue().getActivityObjectsInDay().size() >= lowerLimit)
				{
					toKeepTimelines.put(dateEntry.getKey(), dateEntry.getValue());
				}
				else
				{
					countOfDatesRemovedForThisUser += 1;
					datesRemoved.append(dateEntry.getKey().toString() + "_");
				}
			}

			if (countOfDatesRemovedForThisUser > 0)
			{
				log.append(user + "," + countOfDatesRemovedForThisUser + "," + datesRemoved.toString() + "\n");
				// System.out.println("For user:" + user + " #days(/daytimelines) removed = " +
				// countOfDatesRemovedForThisUser + " (having <"
				// + lowerLimit + " aos per day)");
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
	public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> removeUsersWithLessDays(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelinesOriginal, int lowerLimit,
			String fileNameForLog)
	{
		System.out.println("Inside removeUsersWithLessDays");
		LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> result = new LinkedHashMap<>();
		StringBuffer log = new StringBuffer();
		log.append("UsersRemoved, NumOfDays\n");

		for (Entry<String, LinkedHashMap<Date, UserDayTimeline>> userEntry : usersDayTimelinesOriginal.entrySet())
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

		System.out.println("Number of timelines removed=" + numberOfTimelinesRemoved);
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

		System.out.println("Number of timelines removed=" + numberOfTimelinesRemoved);
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

		System.out.println("Number of timelines removed=" + numberOfTimelinesRemoved);
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
		LinkedHashMap<Integer, Pair<String, Double>> pruned = new LinkedHashMap<Integer, Pair<String, Double>>();

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

		System.out.println("Number of timelines removed=" + numberOfTimelinesRemoved);
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

		System.out.println("Number of timelines removed=" + numberOfTimelinesRemoved);
		return pruned;
	}

	/**
	 * TODO this method can be improved for performance
	 * 
	 * @param dayTimelinesForUser
	 * @param dateA
	 * @return
	 */
	public static UserDayTimeline getUserDayTimelineByDateFromMap(
			LinkedHashMap<Date, UserDayTimeline> dayTimelinesForUser, Date dateA)
	{
		for (Map.Entry<Date, UserDayTimeline> entry : dayTimelinesForUser.entrySet())
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

	public static void traverseMapOfDayTimelines(LinkedHashMap<Date, UserDayTimeline> map)
	{
		System.out.println("traversing map of day timelines");
		for (Map.Entry<Date, UserDayTimeline> entry : map.entrySet())
		{
			System.out.print("Date: " + entry.getKey());
			entry.getValue().printActivityObjectNamesInSequence();
			System.out.println();
		}
		System.out.println("-----------");
	}

	/**
	 * Find Candidate timelines, which are the timelines which contain the activity at the recommendation point (current
	 * Activity). Also, this candidate timeline must contain the activityAtRecomm point at non-last position and there
	 * is atleast one valid activity after this activityAtRecomm point
	 * 
	 * <p>
	 * converted to a static method on Dec 5 2016
	 * <p>
	 * 
	 * @param dayTimelinesForUser
	 * @param activitiesGuidingRecomm
	 * @param dateAtRecomm
	 * @param activityAtRecommPoint
	 * @return
	 */
	public static LinkedHashMap<Date, UserDayTimeline> extractDaywiseCandidateTimelines(
			LinkedHashMap<Date, UserDayTimeline> dayTimelinesForUser, ArrayList<ActivityObject> activitiesGuidingRecomm,
			Date dateAtRecomm, ActivityObject activityAtRecommPoint)
	{
		LinkedHashMap<Date, UserDayTimeline> candidateTimelines = new LinkedHashMap<Date, UserDayTimeline>();
		int count = 0;

		int totalNumberOfProbableCands = 0;
		int numCandsRejectedDueToNoCurrentActivityAtNonLast = 0;
		int numCandsRejectedDueToNoNextActivity = 0;

		for (Map.Entry<Date, UserDayTimeline> entry : dayTimelinesForUser.entrySet())
		{
			totalNumberOfProbableCands += 1;

			// Check if the timeline contains the activityAtRecomm point at non-last and the timeline is not same for
			// the day to be recommended (this should nt
			// be the case because test and training set are diffferent)
			// and there is atleast one valid activity after this activityAtRecomm point
			if (entry.getValue().countContainsActivityButNotAsLast(activityAtRecommPoint) > 0)// &&
																								// (entry.getKey().toString().equals(dateAtRecomm.toString())==false))
			{
				if (entry.getKey().toString().equals(dateAtRecomm.toString()) == true)
				{
					System.err.println(
							"Error: a prospective candidate timelines is of the same date as the dateToRecommend. Thus, not using training and test set correctly");
					continue;
				}

				if (entry.getValue().containsOnlySingleActivity() == false && entry.getValue()
						.hasAValidActivityAfterFirstOccurrenceOfThisActivity(activityAtRecommPoint) == true)
				{
					candidateTimelines.put(entry.getKey(), entry.getValue());
					count++;
				}
				else
					numCandsRejectedDueToNoNextActivity += 1;
			}
			else
				numCandsRejectedDueToNoCurrentActivityAtNonLast += 1;
		}
		if (count == 0) System.err.println("No candidate timelines found");
		return candidateTimelines;
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
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	{
		LinkedHashMap<String, Timeline> cleanedERTimelines = new LinkedHashMap<String, Timeline>();
		System.out.println("inside dayTimelinesToCleanedExpungedRearrangedTimelines()");
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			String userID = usersTimelinesEntry.getKey();
			LinkedHashMap<Date, UserDayTimeline> userDayTimelines = usersTimelinesEntry.getValue();

			userDayTimelines = cleanUserDayTimelines(userDayTimelines);

			Timeline timelineForUser = new Timeline(userDayTimelines); // converts the day time to continuous dayless
																		// timeline
			timelineForUser = UtilityBelt.expungeInvalids(timelineForUser); // expunges invalid activity objects

			cleanedERTimelines.put(userID, timelineForUser);
		}
		System.out.println("\t" + cleanedERTimelines.size() + " timelines created");
		cleanedERTimelines = rearrangeTimelinesOrderForDataset(cleanedERTimelines);
		System.out.println("\t" + cleanedERTimelines.size() + " timelines created");
		return cleanedERTimelines;
	}

	/**
	 * 
	 * @param usersTimelines
	 * @return LinkedHashMap<User ID as String, Timeline of the user with user id as integer as timeline id>
	 */
	public static LinkedHashMap<String, Timeline> dayTimelinesToTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	{
		LinkedHashMap<String, Timeline> timelines = new LinkedHashMap<String, Timeline>();
		if (usersTimelines.size() == 0 || usersTimelines == null)
		{
			new Exception(
					"Error in org.activity.util.UtilityBelt.dayTimelinesToTimelines(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>): userTimeline.size = "
							+ usersTimelines.size()).printStackTrace();
			;
		}
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> entry : usersTimelines.entrySet())
		{
			timelines.put(entry.getKey(), new Timeline(entry.getValue(), Integer.valueOf(entry.getKey())));
		}
		return timelines;
	}

}
