package org.activity.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.activity.constants.Constant;
import org.activity.objects.ActivityObject;
import org.activity.objects.CheckinEntry;
import org.activity.objects.CheckinEntryV2;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Timeline;
import org.json.JSONArray;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class TimelineCreators
{

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
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createUserTimelinesFromCheckinEntriesGowallaFaster1(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> checkinEntries,
			Int2ObjectOpenHashMap<LocationGowalla> mapForAllLocationData)
	{
		long ct1 = System.currentTimeMillis();
	
		System.out.println("starting createUserTimelinesFromCheckinEntriesGowalla");
		LinkedHashMap<String, TreeMap<Date, ArrayList<CheckinEntry>>> checkinEntriesDatewise = TimelineTransformers.convertTimewiseMapToDatewiseMap2(
				checkinEntries, Constant.getTimeZone());
	
		// Start of added on Aug 10 2017
		// // Filter out days which have more than 500 checkins
		// StringBuilder sb = new StringBuilder();
		// checkinEntriesDatewise.entrySet().stream().forEachOrdered(e->sb.append(str));
		// End of added on Aug 10 2017
	
		LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> activityObjectsDatewise = TimelineTransformers.convertCheckinEntriesToActivityObjectsGowalla(
				checkinEntriesDatewise, mapForAllLocationData);
	
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
	 * Fork of createUserTimelinesFromCheckinEntriesGowallaFaster1() primarily to allow for CheckinEntryV2 instead of
	 * CheckinEntry Creates user day timelines from the given list of Activity Objects.
	 * 
	 * Activity events ---> day timelines (later, not here)---> user timelines
	 * <p>
	 * <font color = red>make sure that the timezone is set appropriately</font>
	 * </p>
	 * 
	 * @param allActivityEvents
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 * @since April 9 2018
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createUserTimelinesFromCheckinEntriesGowallaFaster1_V2(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntryV2>> checkinEntries,
			Int2ObjectOpenHashMap<LocationGowalla> mapForAllLocationData)
	{
		long ct1 = System.currentTimeMillis();
	
		System.out.println("starting createUserTimelinesFromCheckinEntriesGowalla");
		LinkedHashMap<String, TreeMap<Date, ArrayList<CheckinEntryV2>>> checkinEntriesDatewise = TimelineTransformers.convertTimewiseMapToDatewiseMap2(
				checkinEntries, Constant.getTimeZone());
	
		// Start of added on Aug 10 2017
		// // Filter out days which have more than 500 checkins
		// StringBuilder sb = new StringBuilder();
		// checkinEntriesDatewise.entrySet().stream().forEachOrdered(e->sb.append(str));
		// End of added on Aug 10 2017
	
		LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject>>> activityObjectsDatewise = TimelineTransformers.convertCheckinEntriesToActivityObjectsGowallaV2(
				checkinEntriesDatewise, mapForAllLocationData);
	
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

}
