package org.activity.util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;

public class TimelineTrimmers
{

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
		WToFile.appendLineToFileAbs(log.toString(), logFileName);
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
	
		WToFile.appendLineToFileAbs(log.toString(), logFileName);
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
		WToFile.appendLineToFileAbs(log.toString(), logFileName);
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
		WToFile.writeToNewFile("IndexOfUser,userID\n", absFileNameForLog);
		int indexOfUser = -1, numOfUsersSkippedGT553MaxActsPerDay = 0;
		///// Remove timelines for blacklisted users
		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersDayTimelinesOriginal.entrySet())
		{
			indexOfUser += 1;
			if (DomainConstants.isGowallaUserIDWithGT553MaxActsPerDay(Integer.valueOf(userEntry.getKey())))
			{
				System.out.println(" " + indexOfUser + " Removing user: " + userEntry.getKey()
						+ " as in gowallaUserIDsWithGT553MaxActsPerDay");
				WToFile.appendLineToFileAbs(indexOfUser + "," + userEntry.getKey() + "\n", absFileNameForLog);
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
	
		WToFile.writeToNewFile(log.toString(), fileNameForLog);
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
	
		WToFile.writeToNewFile(log.toString(), fileNameForLog);
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
	
		WToFile.writeToNewFile(log.toString(), fileNameForLog);
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
			expungedTimelines.put(entry.getKey(), TimelineTrimmers.expungeInvalids(entry.getValue()));
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
				e -> TimelineTrimmers.expungeInvalids(e.getValue()), (v1, v2) -> v1, LinkedHashMap<Date, Timeline>::new));
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
	
		ArrayList<ActivityObject2018> arrayToPrune = timelineToPrune.getActivityObjectsInTimeline();
		ArrayList<ActivityObject2018> arrPruned = (ArrayList<ActivityObject2018>) arrayToPrune.stream()
				.filter(ao -> ao.isInvalidActivityName() == false).collect(Collectors.toList());
	
		Timeline prunedTimeline = new Timeline(arrPruned, timelineToPrune.isShouldBelongToSingleDay(),
				timelineToPrune.isShouldBelongToSingleUser());
		prunedTimeline.setTimelineID(timelineToPrune.getTimelineID());
		return prunedTimeline;
	}

	/**
	 * Removes day timelines with no valid activity, with <=1 distinct valid activity, and the weekend day timelines.
	 * (removeDayTimelinesWithNoValidAct(),removeDayTimelinesWithOneOrLessDistinctValidAct(),removeWeekendDayTimelines()
	 * <b><font color="red">Note: this does not expunges all invalid activity objects from the timeline</font></b>
	 * 
	 * @param usersTimelines
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> cleanUsersDayTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersTimelines)
	{
		System.out.println("Inside cleanDayTimelines(): total num of users before cleaning = " + usersTimelines.size());
		long ct = System.currentTimeMillis();
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> cleanedUserDayTimelines = new LinkedHashMap<>();
	
		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			LinkedHashMap<Date, Timeline> cleanedDayTimelines = TimelineTrimmers.cleanUserDayTimelines(
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
		userDayTimelines = removeDayTimelinesWithNoValidAct(userDayTimelines);
		userDayTimelines = removeDayTimelinesWithOneOrLessDistinctValidAct(userDayTimelines);
		userDayTimelines = removeWeekendDayTimelines(userDayTimelines);
	
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
	
		userDayTimelines = removeDayTimelinesWithNoValidAct(userDayTimelines,
				removeDayTimelinesWithNoValidActLog, user);
		userDayTimelines = removeDayTimelinesWithOneOrLessDistinctValidAct(userDayTimelines,
				logFileNamePhrase + "RemoveDayTimelinesWithOneOrLessDistinctValidAct.csv", user);
		userDayTimelines = removeWeekendDayTimelines(userDayTimelines,
				logFileNamePhrase + "RemoveWeekendDayTimelines.csv", user);
	
		return userDayTimelines;
	}

}
