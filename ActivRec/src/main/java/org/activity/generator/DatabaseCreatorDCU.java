package org.activity.generator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.ui.PopUps;

/**
 * Taken from iiWAS backup.
 * file:///run/media/gunjan/My%20Passport/Backups/antianaXPS/OS%20(windows%20folder)/Users/gunjan/Documents/LifeLog%20App/Versions/Version%2015%20Sep%2011am%20IIWAS
 * <p>
 * added on 10 Dec 2018 Reads
 * 
 * @author gunjan
 *
 */
public class DatabaseCreatorDCU
{
	// public static String commonPath="/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/Lifelog Working
	// dataset 3 july copy/";
	public static String commonPathToRead = "/run/media/gunjan/My Passport/DCUDataWorksDec2018/WorkingSet7July/";
	// "/run/media/gunjan/My Passport/Backups/antianaXPS/OS (windows folder)/Users/gunjan/Documents/DCU Data
	// Works/WorkingSet7July/";
	public static String commonPath = "/home/gunjan/Documents/UCD/Projects/Gowalla/DCUDataWorksDec2018/";
	// "/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/WorkingSet7July/";

	static final String[] activityNames = { "badImages", "Commuting", "Computer", "Eating", "Exercising", "Housework",
			"On the Phone", "Preparing Food", "Shopping", "Socialising", "Watching TV" };

	static LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllData;
	static LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllDataTimeDifference;
	static LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllDataMergedPlusDuration;

	static final String[] userNames = { "LU1", "LU2", "LU3", "LU4", "LU5" };
	public static final int continuityThresholdInSeconds = 30 * 60; // 5 mins, if two timestamps are separated by less
																	// than equal to this value, then they are assumed
																	// to be continuos
	public static final int assumeContinuesBeforeNextInSecs = 30 * 60; // we assume that
	// if two activities have a start time gap of more than 'assumeContinuesBeforeNextInSecs' seconds ,
	// then the first activity continues for 'assumeContinuesBeforeNextInSecs' seconds before the next activity starts.
	public static final int thresholdForMergingNotAvailables = 5 * 60;

	public static void main(String args[])
	{
		// String userNames={""
		// LinkedHashMap<String,String> userMap=new LinkedHashMap <String,String>();

		// userMap.put("LU1","C:\\Users\\gunjan\\Documents\\Lifelog working dataset\\Data_Set_LU1\\Data Set");
		try
		{

			// ConnectDatabaseV1.getTimestamp("B00000028_21I5H1_20140216_170559E.JPG,");

			// before executing it, delete all the file from the 'AllTogther' folder
			// $$ createDataset();
			mapForAllData = createAnnotatedImageFile(commonPathToRead);
			// traverseMapForAllData(mapForAllData);

			mapForAllDataTimeDifference = activitiesWithTimeDifferenceWithNext(mapForAllData); // its a getter method

			writeDataToFile(mapForAllDataTimeDifference, "TimeDifference");

			mapForAllDataMergedPlusDuration = mergeContinuousActivitiesAssignDuration(mapForAllData);

			WToFile.writeTypeOfOthersWithDuration(mapForAllDataMergedPlusDuration);

			mapForAllDataMergedPlusDuration = mergeSmallSandwiched(mapForAllDataMergedPlusDuration, "badImages");

			// mapForAllDataMergedPlusDuration = mergeSmallSandwiched(mapForAllDataMergedPlusDuration,"Not Available");

			/*
			 * mapForAllDataMergedPlusDuration= mergeCleanSmallNotAvailables(mapForAllDataMergedPlusDuration);
			 * mapForAllDataMergedPlusDuration= mergeConsectiveSimilars(mapForAllDataMergedPlusDuration);
			 */
			// mergeConsectiveSimilars

			writeDataToFile(mapForAllDataMergedPlusDuration, "AfterMergingContinuous");

			// System.out.println("----Merged Activity data with duration ----------");
			// traverseMapForAllData(mapForAllDataMergedPlusDuration);
			// System.out.println("----END OF Merged Activity data with duration----------");

			Serializer.serializeThis(mapForAllDataMergedPlusDuration,
					commonPath + "mapForAllDataMergedPlusDuration.map");

			// LinkedHashMap<String, TreeMap<Timestamp,String>> testSerializer=(LinkedHashMap<String,
			// TreeMap<Timestamp,String>>)(Serializer.deSerializeThis(commonPath+"mapForAllDataMergedPlusDuration.map"));
			// traverseMapForAllData(testSerializer);

			// ConnectDatabaseV1.insertIntoImageTable();
			// ConnectDatabaseV1.insertDummyIntoImageTable();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("End of program");
		PopUps.showMessage("End of data creation");
	}

	/**
	 * Merges continuous activities with same activity names and start timestamp difference of less than
	 * 'continuityThresholdInSeconds'.
	 * 
	 * Duration assigned is difference between the start-timestamp of this activity and start-timestamp of the next
	 * (different) activity. difference between the start-timestamp of this activity and start-timestamp of the next
	 * (different) activity BUT ONLY IF this difference is less than P2 minutes, otherwise the duration is P2 minutes.
	 * 
	 * Add 'Unknown'
	 * 
	 * Nuances of merging consecutive activities and calculation the duration of activities.
	 * 
	 * 
	 * @param mapForAllData
	 *            is LinkedHashMap of the form <username, <timestamp,'imagename||activityname'>>
	 * @return <UserName, <Timestamp,'activityname||durationInSeconds'>>
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, String>> mergeContinuousActivitiesAssignDuration(
			LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllData)
	{
		// <username , <start timestamp, 'activityname||durationinseconds'>
		LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, String>>();

		System.out.println("Merging continuous activities and assigning duration");
		for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : mapForAllData.entrySet())
		{
			String userName = entryForUser.getKey();
			System.out.println("\nUser =" + entryForUser.getKey());

			TreeMap<Timestamp, String> mapContinuousMerged = new TreeMap<Timestamp, String>();

			// Timestamp previousTimestamp= new Timestamp(0);
			// String previousActivityName = new String("");
			long durationInSeconds = 0;
			Timestamp startTimestamp;

			ArrayList<String> dataForCurrentUser = treeMapToArrayList(entryForUser.getValue());

			System.out.println("----Unmerged Activity data for user " + userName + "-----");
			traverseArrayList(dataForCurrentUser);
			System.out.println("----END OF Unmerged Activity data--" + userName + "--");

			for (int i = 0; i < dataForCurrentUser.size(); i++)
			{

				// startTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));

				Timestamp currentTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));

				String currentActivityName = getActivityNameFromDataEntry(dataForCurrentUser.get(i));

				// startTimestamp=currentTimestamp;

				if (i < dataForCurrentUser.size() - 1) // is not the last element of arraylist
				{
					// check if the next element should be merged with this one if they are continuos and have same
					// activity name
					Timestamp nextTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i + 1));
					String nextActivityName = getActivityNameFromDataEntry(dataForCurrentUser.get(i + 1));

					if (nextActivityName.equals(currentActivityName) && areContinuous(currentTimestamp, nextTimestamp))
					{
						durationInSeconds += (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;
						continue;
					}
					else
					{
						startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));

						long diffCurrentAndNextDifferentInSec = (nextTimestamp.getTime() - currentTimestamp.getTime())
								/ 1000;
						long secsItContinuesBeforeNext;

						if (diffCurrentAndNextDifferentInSec <= assumeContinuesBeforeNextInSecs)
						{
							secsItContinuesBeforeNext = diffCurrentAndNextDifferentInSec;
						}

						else
						{
							System.out.print("diffCurrentAndNextDifferentInSec (" + diffCurrentAndNextDifferentInSec
									+ ") > assumeContinuesBeforeNextInSecs");
							System.out.println("\n\t For user: " + userName + ", at currentTimestamp="
									+ currentTimestamp + ", currentActivityName=" + currentActivityName);

							secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;

							/* Put the new 'Unknown' entry///////////////// */
							long durationForNewUnknownActivity = diffCurrentAndNextDifferentInSec
									- assumeContinuesBeforeNextInSecs;
							Timestamp startOfNewUnknown = new Timestamp(
									currentTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));

							/*
							 * Break the Unknown activity event across days
							 *////////////////////
							TreeMap<Timestamp, Long> mapBreakedUnknownActivitiesTimes = breakActivityEventOverDays(
									startOfNewUnknown, durationForNewUnknownActivity);

							for (Map.Entry<Timestamp, Long> entry : mapBreakedUnknownActivitiesTimes.entrySet())
							{
								// System.out.println("Start date="+entry.getKey()+" Duration in
								// seconds:"+entry.getValue());
								mapContinuousMerged.put(entry.getKey(),
										"Unknown" + "||" + entry.getValue().longValue());

							}
							//////////////////////

							// mapContinuousMerged.put(startOfNewUnknown, "Unknown"+"||"+durationForNewUnknownActivity);
							System.out.println("Adding: Unknown,  starttimestamp:" + startOfNewUnknown + " duration:"
									+ durationForNewUnknownActivity);
							/* */////////////
						}

						durationInSeconds = durationInSeconds + secsItContinuesBeforeNext;

						/*
						 * Break the activity event across days
						 */////////////////////
						TreeMap<Timestamp, Long> mapBreakedActivitiesTimes = breakActivityEventOverDays(startTimestamp,
								durationInSeconds);

						for (Map.Entry<Timestamp, Long> entry : mapBreakedActivitiesTimes.entrySet())
						{
							// System.out.println("Start date="+entry.getKey()+" Duration in
							// seconds:"+entry.getValue());
							mapContinuousMerged.put(entry.getKey(),
									currentActivityName + "||" + entry.getValue().longValue());

						}
						//////////////////////

						/*
						 * REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
						 * currentActivityName+"||"+durationInSeconds);
						 */

						durationInSeconds = 0;
					}

				}
				else
				{
					startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));

					/*
					 * Break the activity event across days
					 */////////////////////
					TreeMap<Timestamp, Long> mapBreakedActivitiesTimes = breakActivityEventOverDays(startTimestamp,
							durationInSeconds);

					for (Map.Entry<Timestamp, Long> entry : mapBreakedActivitiesTimes.entrySet())
					{
						// System.out.println("Start date="+entry.getKey()+" Duration in seconds:"+entry.getValue());
						mapContinuousMerged.put(entry.getKey(),
								currentActivityName + "||" + entry.getValue().longValue());

					}
					//////////////////////

					/*
					 * REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
					 * currentActivityName+"||"+durationInSeconds);
					 */
					// durationInSeconds=0;
				}

			}

			mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);

		}
		return mapForAllDataMergedPlusDuration;
	}

	// ***************8

	// public static LinkedHashMap<String, TreeMap<Timestamp,String>> mergeContinuousActivities(LinkedHashMap<String,
	// TreeMap<Timestamp,String>> mapForAllData)
	// {
	// //<username , <start timestamp, 'activityname||durationinseconds'>
	// LinkedHashMap<String, TreeMap<Timestamp,String>> mapForAllDataMergedPlusDuration= new LinkedHashMap<String,
	// TreeMap<Timestamp,String>> ();
	//
	// System.out.println("Merging continuous activities and assigning duration");
	// for (Map.Entry<String, TreeMap<Timestamp,String>> entryForUser : mapForAllData.entrySet())
	// {
	// String userName=entryForUser.getKey();
	//
	// TreeMap<Timestamp,String> mapContinuousMerged= new TreeMap<Timestamp,String>();
	//
	// long durationInSeconds=0;
	// Timestamp startTimestamp;
	//
	// ArrayList<String> dataForCurrentUser = treeMapToArrayList(entryForUser.getValue());
	//
	//
	// for(int i=0;i<dataForCurrentUser.size();i++)
	// {
	//
	// String[] splittedDataEntryCurr=dataForCurrentUser.get(i).split(Pattern.quote("||"));
	// String currentActivityName=splittedDataEntryCurr[1];
	// Long currentActivityDuration=Long.valueOf(splittedDataEntryCurr[2]);
	//
	// if(i<dataForCurrentUser.size()-1) // is not the last element of arraylist
	// {
	// //check if the next element should be merged with this one if they are continuos and have same activity name
	// String[] splittedDataEntryNext=dataForCurrentUser.get(i+1).split(Pattern.quote("||"));
	// String nextActivityName=splittedDataEntryNext[1];
	// Long nextActivityDuration=Long.valueOf(splittedDataEntryNext[2]);
	//
	// if(nextActivityName.equals(currentActivityName) && currentActivityName.equalsIgnoreCase("Unknown")==false)
	// {
	// durationInSeconds += nextActivityDuration;
	// continue;
	// }
	// else
	// {
	// mapContinuousMerged.put(splittedDataEntryCurr[0], currentActivityName+"||"+durationInSeconds);
	// durationInSeconds =0;
	// }
	//
	//
	// }
	// else
	// {
	// startTimestamp = new Timestamp(currentTimestamp.getTime()-(durationInSeconds*1000));
	//
	//
	// /*
	// * Break the activity event across days
	// */////////////////////
	// TreeMap<Timestamp,Long> mapBreakedActivitiesTimes=breakActivityEventOverDays(startTimestamp, durationInSeconds);
	//
	// for (Map.Entry<Timestamp, Long> entry : mapBreakedActivitiesTimes.entrySet())
	// {
	// //System.out.println("Start date="+entry.getKey()+" Duration in seconds:"+entry.getValue());
	// mapContinuousMerged.put(entry.getKey(), currentActivityName+"||"+entry.getValue().longValue());
	//
	// }
	// //////////////////////
	//
	//
	// /*REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
	// currentActivityName+"||"+durationInSeconds); */
	// //durationInSeconds=0;
	// }
	//
	// }
	//
	// mapForAllDataMergedPlusDuration.put(entryForUser.getKey(),mapContinuousMerged);
	//
	// }
	// return mapForAllDataMergedPlusDuration;
	// }
	// *****************

	////////////////////////////////////
	public static LinkedHashMap<String, TreeMap<Timestamp, String>> mergeSmallSandwiched(
			LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllData, String activityNameToMerge)
	{
		LinkedHashMap<String, TreeMap<Timestamp, String>> mapCleanedMerged = new LinkedHashMap<String, TreeMap<Timestamp, String>>();

		System.out.println("Inside mergeSmallSandwiched for " + activityNameToMerge);

		LinkedHashMap<String, Integer> numberOfSandwichesFound = new LinkedHashMap<String, Integer>();

		for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : mapForAllData.entrySet())
		{
			String userName = entryForUser.getKey();
			System.out.println("For user:" + userName);

			int numberOfSandwichesForThisUser = 0;

			ArrayList<String> dataForCurrentUser = treeMapToArrayList(entryForUser.getValue());
			ArrayList<String> cleanedMergedDataForCurrentUser = new ArrayList<String>();

			for (int i = 0; i < dataForCurrentUser.size(); i++)
			{
				System.out.println(dataForCurrentUser.get(i));
				String[] splittedDataEntryCurr = dataForCurrentUser.get(i).split(Pattern.quote("||"));
				String currentActivityName = splittedDataEntryCurr[1];
				System.out.println(splittedDataEntryCurr[2]);
				Long currentActivityDuration = Long.valueOf(splittedDataEntryCurr[2]);

				Long durationToWrite = currentActivityDuration;

				while (i <= dataForCurrentUser.size() - 3) //
				{
					String[] splittedDataEntryNext = dataForCurrentUser.get(i + 1).split(Pattern.quote("||"));
					String nextActivityName = splittedDataEntryNext[1];
					Long nextActivityDuration = Long.valueOf(splittedDataEntryNext[2]);

					if (nextActivityName.equalsIgnoreCase(activityNameToMerge) == false)
					{
						cleanedMergedDataForCurrentUser
								.add(splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + durationToWrite);
						System.out.println("Case next is not " + activityNameToMerge + ": "
								+ (splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + durationToWrite));
						durationToWrite = 0l;
						break;
					}

					if (currentActivityName.equalsIgnoreCase("Unknown"))
					{
						cleanedMergedDataForCurrentUser
								.add(splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + durationToWrite);
						System.out.println("Current is unknown: "
								+ (splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + durationToWrite));

						durationToWrite = 0l;
						break;
					}

					String[] splittedDataEntryNextNext = dataForCurrentUser.get(i + 2).split(Pattern.quote("||"));
					String nextNextActivityName = splittedDataEntryNextNext[1];
					Long nextNextActivityDuration = Long.valueOf(splittedDataEntryNextNext[2]);

					if (nextActivityName.equalsIgnoreCase(activityNameToMerge)
							&& (nextActivityDuration < thresholdForMergingNotAvailables)
							&& nextNextActivityName.equalsIgnoreCase(currentActivityName)) // sandwich found
					{
						durationToWrite = durationToWrite + nextActivityDuration + nextNextActivityDuration;
						i += 2;
						System.out.println("Found sandwich: moving 2 steps ahead");
						numberOfSandwichesForThisUser++;
						continue;
					}

					else
					{
						cleanedMergedDataForCurrentUser
								.add(splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + durationToWrite);

						System.out.println("Case: final else to write "
								+ (splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + durationToWrite));

						durationToWrite = 0l;
						break;
					}

				}

				if (i > dataForCurrentUser.size() - 3)
				{

					cleanedMergedDataForCurrentUser.add(dataForCurrentUser.get(i));

					System.out.println("Case: last events: "
							+ (splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + durationToWrite));

					// i++;
					durationToWrite = 0l;
				}

			}

			numberOfSandwichesFound.put(userName, numberOfSandwichesForThisUser);
			WToFile.writeSimpleLinkedHashMapToFile(numberOfSandwichesFound,
					commonPath + "sandwichesPerUser" + activityNameToMerge + thresholdForMergingNotAvailables + ".csv",
					"User", "number_of_sandwiches");

			mapCleanedMerged.put(userName, arrayListToTreeMap(cleanedMergedDataForCurrentUser));

		}

		return mapCleanedMerged;
	}
	////////////////////////////////////

	/////////////////////
	public static LinkedHashMap<String, TreeMap<Timestamp, String>> mergeCleanSmallNotAvailables(
			LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllData, String activityNameToMerge)
	{
		// <username , <start timestamp, 'activityname||durationinseconds'>
		LinkedHashMap<String, TreeMap<Timestamp, String>> mapCleanedMerged = new LinkedHashMap<String, TreeMap<Timestamp, String>>();

		System.out.println("Merging and cleaning small " + activityNameToMerge);

		for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : mapForAllData.entrySet())
		{
			String userName = entryForUser.getKey();
			// System.out.println("\nUser ="+entryForUser.getKey());

			ArrayList<String> dataForCurrentUser = treeMapToArrayList(entryForUser.getValue());
			ArrayList<String> cleanedMergedDataForCurrentUser = new ArrayList<String>();

			for (int i = 0; i < dataForCurrentUser.size(); i++)
			{
				String[] splittedDataEntryCurr = dataForCurrentUser.get(i).split(Pattern.quote("||"));
				String currentActivityName = splittedDataEntryCurr[1];
				Long currentActivityDuration = Long.valueOf(splittedDataEntryCurr[2]);

				if (i < dataForCurrentUser.size() - 1) // atleast one entry after this
				{
					String[] splittedDataEntryNext = dataForCurrentUser.get(i + 1).split(Pattern.quote("||"));
					String nextActivityName = splittedDataEntryNext[1];
					Long nextActivityDuration = Long.valueOf(splittedDataEntryNext[2]);

					if (nextActivityName.equalsIgnoreCase(activityNameToMerge) == false)
					{
						cleanedMergedDataForCurrentUser.add(dataForCurrentUser.get(i));
						continue;
					}

					else if (nextActivityName.equalsIgnoreCase(activityNameToMerge)
							&& nextActivityDuration < thresholdForMergingNotAvailables)
					{
						long newDuration = currentActivityDuration + nextActivityDuration;

						cleanedMergedDataForCurrentUser
								.add(splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + newDuration);
						i++;
						continue;
					}

					else
					{
						cleanedMergedDataForCurrentUser.add(dataForCurrentUser.get(i));
						continue;
					}
				}

				else
				{
					cleanedMergedDataForCurrentUser.add(dataForCurrentUser.get(i));
				}

			}

			mapCleanedMerged.put(userName, arrayListToTreeMap(cleanedMergedDataForCurrentUser));

		}

		return mapCleanedMerged;
	}

	////
	/**
	 * Merge consecutive activities with same name except 'Unknown'
	 * 
	 * @param mapForAllData
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, String>> mergeConsectiveSimilars(
			LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllData)
	{
		// <username , <start timestamp, 'activityname||durationinseconds'>
		LinkedHashMap<String, TreeMap<Timestamp, String>> mapCleanedMerged = new LinkedHashMap<String, TreeMap<Timestamp, String>>();

		System.out.println("Merging consective similars");

		for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : mapForAllData.entrySet())
		{
			String userName = entryForUser.getKey();
			// System.out.println("\nUser ="+entryForUser.getKey());

			ArrayList<String> dataForCurrentUser = treeMapToArrayList(entryForUser.getValue());
			ArrayList<String> cleanedMergedDataForCurrentUser = new ArrayList<String>();

			long newDuration = 0;

			int i = 0;
			while (i < dataForCurrentUser.size() - 1) // for(int i=0;i<dataForCurrentUser.size();i++)
			{
				String[] splittedDataEntryCurr = dataForCurrentUser.get(i).split(Pattern.quote("||"));
				String currentActivityName = splittedDataEntryCurr[1];
				Long currentActivityDuration = Long.valueOf(splittedDataEntryCurr[2]);

				newDuration = newDuration + currentActivityDuration;

				String[] splittedDataEntryNext = dataForCurrentUser.get(i + 1).split(Pattern.quote("||"));
				String nextActivityName = splittedDataEntryNext[1];
				Long nextActivityDuration = Long.valueOf(splittedDataEntryNext[2]);

				if (nextActivityName.equalsIgnoreCase(currentActivityName)
						&& currentActivityName.equalsIgnoreCase("Unknown") == false)
				{
					i++;
				}

				else // when next is different or when current is 'Unknown' in which case we do not merge next
				{
					cleanedMergedDataForCurrentUser
							.add(splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + newDuration);
					newDuration = 0;
					i++;
				}
			}

			String[] splittedDataEntryLast = dataForCurrentUser.get(dataForCurrentUser.size() - 1)
					.split(Pattern.quote("||"));
			String lastActivityName = splittedDataEntryLast[1];
			Long lastActivityDuration = Long.valueOf(splittedDataEntryLast[2]);

			cleanedMergedDataForCurrentUser
					.add(splittedDataEntryLast[0] + "||" + lastActivityName + "||" + lastActivityDuration);

			mapCleanedMerged.put(userName, arrayListToTreeMap(cleanedMergedDataForCurrentUser));

		}

		return mapCleanedMerged;
	}

	//////
	// public static LinkedHashMap<String, TreeMap<Timestamp,String>> mergeConsectiveSimilars(LinkedHashMap<String,
	// TreeMap<Timestamp,String>> mapForAllData)
	// {
	// //<username , <start timestamp, 'activityname||durationinseconds'>
	// LinkedHashMap<String, TreeMap<Timestamp,String>> mapCleanedMerged= new LinkedHashMap<String,
	// TreeMap<Timestamp,String>> ();
	//
	// System.out.println("Merging consective similars");
	//
	//
	// for (Map.Entry<String, TreeMap<Timestamp,String>> entryForUser : mapForAllData.entrySet())
	// {
	// String userName=entryForUser.getKey();
	// // System.out.println("\nUser ="+entryForUser.getKey());
	//
	// ArrayList<String> dataForCurrentUser = treeMapToArrayList(entryForUser.getValue());
	// ArrayList<String> cleanedMergedDataForCurrentUser= new ArrayList<String>();
	//
	// long newDuration=0;
	//
	// for(int i=0;i<dataForCurrentUser.size();i++)
	// {
	// String[] splittedDataEntryCurr=dataForCurrentUser.get(i).split(Pattern.quote("||"));
	// String currentActivityName=splittedDataEntryCurr[1];
	// Long currentActivityDuration=Long.valueOf(splittedDataEntryCurr[2]);
	//
	// newDuration= currentActivityDuration;
	//
	// if(i<dataForCurrentUser.size()-1) //atleast one entry after this
	// {
	// String[] splittedDataEntryNext=dataForCurrentUser.get(i+1).split(Pattern.quote("||"));
	// String nextActivityName=splittedDataEntryNext[1];
	// Long nextActivityDuration=Long.valueOf(splittedDataEntryNext[2]);
	//
	// while(nextActivityName.equalsIgnoreCase(currentActivityName) && i<dataForCurrentUser.size()-1)
	// {
	// newDuration = newDuration+nextActivityDuration;
	// i++;
	//
	// if(i == dataForCurrentUser.size()-1)
	// {
	// break;
	// }
	// splittedDataEntryNext=dataForCurrentUser.get(i+1).split(Pattern.quote("||"));
	// nextActivityName=splittedDataEntryNext[1];
	// nextActivityDuration=Long.valueOf(splittedDataEntryNext[2]);
	// //continue;
	// }
	//
	// cleanedMergedDataForCurrentUser.add(splittedDataEntryCurr[0]+"||"+currentActivityName+"||"+newDuration);
	//
	// }
	//
	// else cleanedMergedDataForCurrentUser.add(dataForCurrentUser.get(i));
	// }
	//
	// mapCleanedMerged.put(userName, arrayListToTreeMap(cleanedMergedDataForCurrentUser));
	//
	// }
	//
	// return mapCleanedMerged;
	// }

	//////

	////////////////////

	///
	public static LinkedHashMap<String, TreeMap<Timestamp, String>> activitiesWithTimeDifferenceWithNext(
			LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllData)
	{
		// <username , <start timestamp, 'activityname||durationinseconds'>
		LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllDataNotMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, String>>();

		for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : mapForAllData.entrySet())
		{
			String userName = entryForUser.getKey();
			System.out.println("\nUser =" + entryForUser.getKey());

			TreeMap<Timestamp, String> mapContinuousNotMerged = new TreeMap<Timestamp, String>();

			long diffWithNextInSeconds = 0;

			ArrayList<String> dataForCurrentUser = treeMapToArrayList(entryForUser.getValue());

			for (int i = 0; i < dataForCurrentUser.size(); i++)
			{

				Timestamp currentTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));
				String currentActivityName = getActivityNameFromDataEntry(dataForCurrentUser.get(i));

				if (i < dataForCurrentUser.size() - 1) // is not the last element of arraylist
				{
					Timestamp nextTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i + 1));
					diffWithNextInSeconds = (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;
					mapContinuousNotMerged.put(currentTimestamp, diffWithNextInSeconds + "," + currentActivityName);
				}
				else
				{
					mapContinuousNotMerged.put(currentTimestamp, "0," + currentActivityName);
				}

			}
			mapForAllDataNotMergedPlusDuration.put(entryForUser.getKey(), mapContinuousNotMerged);
		}
		return mapForAllDataNotMergedPlusDuration;
	}

	///

	public static int differenceInSeconds(Timestamp previousTimestamp, Timestamp nextTimestamp)
	{
		int differenceInSeconds = 0;

		if (previousTimestamp.getTime() != 0)
		{
			differenceInSeconds = (int) (nextTimestamp.getTime() - previousTimestamp.getTime()) / 1000;

			if (differenceInSeconds < 1)
				System.err.println("Error in differenceInSeconds(): (nextTimestamp-previousTimestamp) is negative");
		}

		return differenceInSeconds;
	}

	/**
	 * Return true of the two timestamps have a time difference of less than a the 'continuity threshold in seconds'
	 * 
	 * @param timestamp1
	 * @param timestamp2
	 */
	public static boolean areContinuous(Timestamp timestamp1, Timestamp timestamp2)
	{
		long differenceInSeconds = Math.abs(timestamp1.getTime() - timestamp2.getTime()) / 1000;

		if (differenceInSeconds <= continuityThresholdInSeconds)
			return true;
		else
			return false;
	}

	public static LinkedHashMap<String, TreeMap<Timestamp, String>> createAnnotatedImageFile(String commonPathToRead)
	{
		BufferedReader br = null;
		BufferedWriter bw = null;

		// <username , <timstamp, 'imagename||activityname'>
		LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllData = new LinkedHashMap<String, TreeMap<Timestamp, String>>();

		for (String userName : userNames)
		{

			TreeMap<Timestamp, String> mapEachPhoto = new TreeMap<Timestamp, String>();
			System.out.println("creating annotated files for user=" + userName);
			try
			{

				File file = new File(commonPathToRead + "AllTogether7July/" + userName + "_AnnotatedJPGFiles.txt");
				System.out.println(file.getAbsoluteFile());
				file.delete();
				file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);

				br = new BufferedReader(
						new FileReader(commonPathToRead + "AllTogether7July/" + userName + "_JPGFiles.txt"));

				String sCurrentImageName;
				while ((sCurrentImageName = br.readLine()) != null)
				{
					// System.out.println(sCurrentImageName);

					String activityName = getActivityName(userName, sCurrentImageName, commonPathToRead);

					bw.write(sCurrentImageName + "||" + activityName + "\n");
					// System.out.println(getTimestamp(sCurrentImageName)+"--"+sCurrentImageName+"||"+activityName);
					// System.out.println(".");
					mapEachPhoto.put(getTimestamp(sCurrentImageName), sCurrentImageName + "||" + activityName);
				}
				mapForAllData.put(userName, mapEachPhoto);
			}

			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if (br != null)
					{
						br.close();
					}
					if (bw != null)
					{
						bw.close();
					}
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		}

		System.out.println(" Size of mapForAllData:" + mapForAllData.size());

		return mapForAllData;
	}

	public static void traverseMapForAllData(LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllData)
	{
		for (Map.Entry<String, TreeMap<Timestamp, String>> entry : mapForAllData.entrySet())
		{
			System.out.println("\nUser =" + entry.getKey());

			for (Map.Entry<Timestamp, String> entryMapEachPhoto : entry.getValue().entrySet())
			{
				System.out.print(entryMapEachPhoto.getKey());
				System.out.print("   " + entryMapEachPhoto.getValue());
				System.out.print("\n");
			}
		}
	}

	/**
	 * 
	 * 
	 * @param treeMap
	 * @return ArrayList of strings of the form 'timestampInMilliSeconds||ImageName||ActivityName' or more generally
	 *         'timestampInMilliSeconds||String1`the old separator inherited`String2'
	 */
	public static ArrayList<String> treeMapToArrayList(TreeMap<Timestamp, String> treeMap)
	{
		ArrayList<String> arrayList = new ArrayList<String>();

		for (Map.Entry<Timestamp, String> entry : treeMap.entrySet())
		{
			String timestampString = Long.toString(entry.getKey().getTime());
			String imageNameActivityName = entry.getValue();

			arrayList.add(timestampString + "||" + imageNameActivityName);
		}

		if (treeMap.size() == arrayList.size())
		{
			System.out.println("TreeMap converted to arraylist succesfully");
		}
		return arrayList;
	}

	// timestampInMilliSeconds||ImageName||ActivityName
	/**
	 * 
	 * 
	 * @param arrayListToConvert
	 * @return
	 */
	public static TreeMap<Timestamp, String> arrayListToTreeMap(ArrayList<String> arrayListToConvert)
	{
		TreeMap<Timestamp, String> treeMap = new TreeMap<Timestamp, String>();

		for (int i = 0; i < arrayListToConvert.size(); i++)
		{
			String[] splitted = arrayListToConvert.get(i).split(Pattern.quote("||"));

			Timestamp timeStamp = new Timestamp(Long.valueOf(splitted[0]));

			String stringForValue = new String();

			for (int j = 1; j < splitted.length - 1; j++)
			{
				stringForValue += splitted[j] + "||";
			}

			stringForValue += splitted[splitted.length - 1];

			treeMap.put(timeStamp, stringForValue);
		}

		return treeMap;

	}

	/**
	 * 
	 * 
	 * @param dataEntryForAnImage
	 *            must be of the form '<Timestamp in milliseconds as String>||ImageName||ActivityName'
	 * @return timestamp extracted
	 */
	public static Timestamp getTimestampFromDataEntry(String dataEntryForAnImage)
	{
		Timestamp timeStamp = null;
		// System.out.println("data entry="+dataEntryForAnImage);
		String[] splitted = dataEntryForAnImage.split(Pattern.quote("||"));

		// System.out.println("length of splitted is "+splitted.size());
		// System.out.println("splitted 0 is "+splitted[0]);
		timeStamp = new Timestamp(Long.valueOf(splitted[0]).longValue());

		return timeStamp;
	}

	/**
	 * 
	 * 
	 * @param dataEntryForAnImage
	 *            must be of the form '<Timestamp in milliseconds as String>||ImageName||ActivityName'
	 * @return timestamp extracted
	 */
	public static String getActivityNameFromDataEntry(String dataEntryForAnImage)
	{
		// String activityName= new String();
		String[] splitted = dataEntryForAnImage.split(Pattern.quote("||"));

		return splitted[2];
	}

	public static void traverseArrayList(ArrayList<String> arr)
	{
		System.out.println("traversing arraylist");
		for (int i = 0; i < arr.size(); i++)
		{
			System.out.println(arr.get(i));
		}
	}

	// startOfNewUnknown,durationForNewUnknownActivity
	/**
	 * 
	 * @param startOfNewUnknown
	 * @param durationInSeconds
	 * @return true if activity spane over multiple days.
	 */
	public static boolean spansOverMultipleDays(Timestamp startTimestamp, long durationInSeconds)
	{
		boolean spansOverDays = false;

		Timestamp endTimestamp = new Timestamp(startTimestamp.getTime() + (durationInSeconds * 1000));

		if (startTimestamp.getDate() != endTimestamp.getDate() || startTimestamp.getMonth() != endTimestamp.getMonth())
		{
			spansOverDays = true;
		}

		return spansOverDays;
	}

	/**
	 * To break activity events spanning over multiple days into activity events contained in single days.
	 * 
	 * 
	 * @param startTimestamp
	 * @param durationInSeconds
	 * @return
	 */
	// Tested OK
	public static TreeMap<Timestamp, Long> breakActivityEventOverDays(Timestamp startTimestamp, long durationInSeconds)
	{
		TreeMap<Timestamp, Long> treeMap = new TreeMap<Timestamp, Long>();

		Timestamp startTimestampNow = startTimestamp;
		long durationInSecondsLeft = durationInSeconds;

		int count = 0;

		while (durationInSecondsLeft > 0)
		{
			Timestamp endTimestampNow = new Timestamp(startTimestampNow.getYear(), startTimestampNow.getMonth(),
					startTimestampNow.getDate(), 23, 59, 59, 0);
			long diffNowInSeconds = endTimestampNow.getTime() / 1000 - startTimestampNow.getTime() / 1000;

			// System.out.println("durationInSecondsLeft="+durationInSecondsLeft+" diffNowInSeconds="+diffNowInSeconds);
			if (durationInSecondsLeft > (diffNowInSeconds + 1)) // this means it spans more than the current day.
			{ // CHECK THE ramifications of this +1 , this is done because 1 seconds is lost in 23:59:59 and further
				// when creating objects in database, we take the end time as (duration -1seconds)

				treeMap.put(startTimestampNow, new Long(diffNowInSeconds + 1));

				durationInSecondsLeft = durationInSecondsLeft - (diffNowInSeconds + 1);
				startTimestampNow = new Timestamp(startTimestampNow.getTime() + (diffNowInSeconds * 1000 + 1000));

				continue;
			}

			else
			{
				treeMap.put(startTimestampNow, new Long(durationInSecondsLeft));
				count++;
				break;
			}

		}

		return treeMap;
	}

	public static String getActivityName(String userName, String imageName, String commonPathToRead)
	{
		String activityName = "Not Available";

		for (int iterator = 0; iterator < activityNames.length; iterator++)
		{
			if (containsText(commonPathToRead + "AllTogether7July/" + userName + "_" + activityNames[iterator] + ".txt",
					imageName))
			{
				// System.out.println("## found ##");
				activityName = activityNames[iterator];
				return activityName;
			}
		}
		return activityName;
	}

	public static boolean containsText(String fileName, String textToValidate)
	{
		Boolean contains = false;
		// System.out.println("inside contains text: to check if "+fileName+" contains "+textToValidate+"\n");
		BufferedReader br = null;
		try
		{
			String currentLine;

			br = new BufferedReader(new FileReader(fileName));

			while ((currentLine = br.readLine()) != null)
			{
				if (currentLine.contains(textToValidate))
				{
					contains = true;
					br.close();
					return contains;
				}
			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return contains;
	}

	/**
	 * Creates contents for AllTogether7thJUly folder: the complete dataset with which we work.
	 * 
	 */
	public static void createDataset(String commonPathToRead)
	{
		String datasetAddressLU1 = commonPathToRead + "Data_Set_LU1/Data Set";
		listFilesForFolder(new File(datasetAddressLU1), datasetAddressLU1, "LU1", commonPathToRead);

		String datasetAddressLU2 = commonPathToRead + "Data_Set_TengQi/tengqi";
		listFilesForFolder(new File(datasetAddressLU2), datasetAddressLU2, "LU2", commonPathToRead);

		String datasetAddressLU3 = commonPathToRead + "Data_Set_LU3/two weeks of data/two weeks of data";
		listFilesForFolder(new File(datasetAddressLU3), datasetAddressLU3, "LU3", commonPathToRead);

		String datasetAddressLU4 = commonPathToRead + "Data_Set_LU4";
		listFilesForFolder(new File(datasetAddressLU4), datasetAddressLU4, "LU4", commonPathToRead);

		String datasetAddressLU5 = commonPathToRead + "Data_Set_LU5";
		listFilesForFolder(new File(datasetAddressLU5), datasetAddressLU5, "LU5", commonPathToRead);

		System.out.println("list files for folder completed");

		countFilesAndWriteStats(commonPathToRead);
	}

	/**
	 * Reads for the files from the folder for a given user and create the following files for them: 1)
	 * <username>_JPGFiles.txt: containing the list of names of all jpg files for that user 2) one files for each of the
	 * Activity names <username>_<categoryname>.txt containing the names of JPG files for this category.
	 * 
	 * @param folder
	 * @param path
	 * @param userName
	 */
	public static void listFilesForFolder(final File folder, String path, String userName, String commonPathToRead)
	{
		// int count=0;
		String categories[] = { "badImages", "Commuting", "Computer", "Eating", "Exercising", "Housework",
				"On the Phone", "Preparing Food", "Shopping", "Socialising", "Watching TV" };
		int countOfJPG = 0;// , countOfCategoryAssignments=0;;
		int countOfActivityFilesFound = 0;
		int countOfJPGFilesMentionedInAllActivityFiles = 0;

		path = commonPathToRead + "AllTogether7July/";

		for (File fileEntry : folder.listFiles())
		{
			if (fileEntry.isDirectory())
			{
				System.out.print("Directory: " + fileEntry + "");
				if (fileEntry.getName().toString().contains("thumbs"))
				{
					System.out.println("found thumbs");
				}
				else
					listFilesForFolder(fileEntry, path, userName, commonPathToRead);
			}
			else
			{
				System.out.print("Files (not directory)" + fileEntry.getName() + "");

				// check if the file name is for jpg files, if yes then add it to the list of jpg files.
				if (fileEntry.getName().toString().contains("jpg") || fileEntry.getName().toString().contains("JPG")
						|| fileEntry.getName().toString().contains("JPEG"))
				{
					countOfJPG++;
					appendStringToFile(path + userName + "_JPGFiles.txt", fileEntry.getName());
				}

				// check if it is a 'Listing of jpg files for category' files, like commuting.ann
				for (int i = 0; i < categories.length; i++)
				{
					String categoryName = categories[i];
					// System.out.println("Category Name check = "+categoryName+"\n");
					if (fileEntry.getName().toString().contains(categoryName))
					{
						countOfActivityFilesFound++;

						System.out.println(fileEntry.getName() + " is an 'Listing of jpg in category' files");
						// countOfCategoryAssignments;
						// System.out.println(fileEntry.getAbsolutePath());

						countOfJPGFilesMentionedInAllActivityFiles += appendFileContentsToFile(
								path + userName + "_" + categoryName + ".txt", fileEntry.getAbsolutePath(), userName);
					}
				}
			}
		}

		// writeInStats("\nFor user: "+userName+"\n\tTotal count of JPG files="+countOfJPG+" Total count of Activity
		// Files found="+countOfActivityFilesFound+" Total count of JPG files mentioned in all activity files"
		// + "="+countOfJPGFilesMentionedInAllActivityFiles);
		System.out.println("*** ");
	}

	public static void appendStringToFile(String fileName, String textToWrite)
	{
		FileWriter output = null;
		try
		{
			output = new FileWriter(fileName, true);
			BufferedWriter writer = new BufferedWriter(output);

			writer.append(textToWrite + "\n");
			writer.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		finally
		{
			if (output != null)
			{
				try
				{
					output.flush();
					output.close();
				}
				catch (IOException e)
				{

				}
			}
		}
	}

	/**
	 * Append the contents of a given file to the end of another files
	 * 
	 * @param fileToWriteTo
	 * @param fileToRead
	 * @param userName
	 * @return
	 */
	public static int appendFileContentsToFile(String fileToWriteTo, String fileToRead, String userName)
	{
		BufferedReader br = null;
		int countNonEmptyLines = 0;

		try
		{
			String currentLine;
			// System.out.println("OOOO writing activity file file ="+fileToWriteTo);
			br = new BufferedReader(new FileReader(fileToRead));

			if ((currentLine = br.readLine()) == null)
			{
				System.out.println(fileToRead + " is empty");
				appendStringToFile(fileToWriteTo, "empty");
				br.close();
				return 0;
			}

			while ((currentLine = br.readLine()) != null)
			{
				// System.out.println("bazooka"+cu<<<rrentLine);
				if (currentLine.trim().isEmpty())
				{
					System.err.println("Reading contents from file:" + fileToRead + ", current lines is empty");
				}

				else
				{
					appendStringToFile(fileToWriteTo, currentLine);
					countNonEmptyLines++;
				}

			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return countNonEmptyLines;

	}

	public static void writeInStats(String content)
	{
		try
		{
			File file = new File(commonPath + "stats.csv");

			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void countFilesAndWriteStats(String commonPathToRead)
	{
		try
		{
			LinkedHashMap<String, Integer> countPerActivityName = new LinkedHashMap<String, Integer>();
			for (String activityName : activityNames)
			{
				countPerActivityName.put(activityName, new Integer(0));
			}

			for (String userName : userNames)
			{
				writeInStats("\n For user: " + userName);
				int numberOfEntriesInJPGFiles = countLinesNotEmptyStringlines(
						commonPathToRead + "AllTogether7July/" + userName + "_JPGFiles.txt");
				writeInStats("\n Number of JPG Files=" + numberOfEntriesInJPGFiles);

				int totalNumberEntriesOverAllActivities = 0;
				for (String activityName : activityNames)
				{
					int numberOfEntriesNonEmpty = countLinesNotEmptyStringlines(
							commonPathToRead + "AllTogether7July/" + userName + "_" + activityName + ".txt");
					writeInStats("\n Number of jpg files in " + userName + "_" + activityName + ".txt = "
							+ numberOfEntriesNonEmpty);

					countPerActivityName.put(activityName,
							countPerActivityName.get(activityName) + numberOfEntriesNonEmpty);

					totalNumberEntriesOverAllActivities += numberOfEntriesNonEmpty;
				}
				writeInStats("\n Total number jpg files over all Activities =" + totalNumberEntriesOverAllActivities);
				writeInStats("\n Difference between total num of images and total num of annotated images: "
						+ numberOfEntriesInJPGFiles + " - " + totalNumberEntriesOverAllActivities + " = "
						+ (numberOfEntriesInJPGFiles - totalNumberEntriesOverAllActivities) + "\n");
			}

			writeInStats("\n------------\n");
			writeInStats("\nTotal:\n");
			for (Map.Entry<String, Integer> entry : countPerActivityName.entrySet())
			{

				writeInStats(
						"\n Num of images annotated with Activity Name: " + entry.getKey() + "  = " + entry.getValue());

			}
			writeInStats("\n------------\n");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static int countLinesNotEmptyStringlines(String filename) throws IOException
	{
		BufferedReader br = null;
		int countNonEmptyStringlines = 0;
		try
		{

			String sCurrentLine;
			br = new BufferedReader(new FileReader(filename));

			while ((sCurrentLine = br.readLine()) != null)
			{
				if (sCurrentLine.trim().equalsIgnoreCase("empty") == false)
				{
					countNonEmptyStringlines++;
				}

				else
				{
					System.out.println("empty found");
				}
				// System.out.println(sCurrentLine);
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null) br.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		return countNonEmptyStringlines;
	}

	/**
	 * Get Timestamp from image name
	 * 
	 * @param imageName
	 * @return
	 */
	/*
	 * public static Timestamp getTimestamp(String imageName) { Timestamp timeStamp=null; int year=0, month=0, day=0,
	 * hours=0, minutes=0, seconds=0;
	 * 
	 * //Pattern imageNamePattern= Pattern.compile("((.*)(_)(.*)(_)("); StringTokenizer tokenizer= new
	 * StringTokenizer(imageName,"_"); int count=0;
	 * 
	 * try { while(tokenizer.hasMoreTokens()) { String token=tokenizer.nextToken();
	 * //System.out.println("token ="+token+" count="+count);
	 * 
	 * if(count == 2) { year=Integer.parseInt(token.substring(0,4)); month=Integer.parseInt(token.substring(4,6));
	 * day=Integer.parseInt(token.substring(6,8)); }
	 * 
	 * if(count == 3) { hours=Integer.parseInt(token.substring(0,2)); minutes=Integer.parseInt(token.substring(2,4));
	 * seconds=Integer.parseInt(token.substring(4,6)); } count++;
	 * 
	 * }
	 * 
	 * //System.out.println(year+ " "+month+" "+day+" "+hours+" "+minutes+" "+seconds); timeStamp=new
	 * Timestamp(year-1900,month-1,day,hours,minutes, seconds,0); /// CHECK it out
	 * //System.out.println("Time stamp"+timeStamp); } catch(Exception e) {
	 * System.out.println("Exception "+e+" thrown for getting timestamo from "+ imageName); e.printStackTrace(); }
	 * return timeStamp; }
	 */

	public static Timestamp getTimestamp(String imageName)
	{
		Timestamp timeStamp = null;
		int year = 0, month = 0, day = 0, hours = 0, minutes = 0, seconds = 0;

		// Pattern imageNamePattern= Pattern.compile("((.*)(_)(.*)(_)(");
		String[] splitted = imageName.split("_");
		int count = 0;

		try
		{

			String dateString = splitted[splitted.length - 2];

			year = Integer.parseInt(dateString.substring(0, 4));
			month = Integer.parseInt(dateString.substring(4, 6));
			day = Integer.parseInt(dateString.substring(6, 8));

			String timeString = splitted[splitted.length - 1];

			hours = Integer.parseInt(timeString.substring(0, 2));
			minutes = Integer.parseInt(timeString.substring(2, 4));
			seconds = Integer.parseInt(timeString.substring(4, 6));

			// System.out.println(year+ " "+month+" "+day+" "+hours+" "+minutes+" "+seconds);
			timeStamp = new Timestamp(year - 1900, month - 1, day, hours, minutes, seconds, 0); /// CHECK it out
			// System.out.println("Time stamp"+timeStamp);
		}
		catch (Exception e)
		{
			System.out.println("Exception " + e + " thrown for getting timestamo from " + imageName);
			e.printStackTrace();
		}
		return timeStamp;
	}

	public static void writeDataToFile(LinkedHashMap<String, TreeMap<Timestamp, String>> data, String filenameEndPhrase)
	{
		for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : data.entrySet())
		{
			try
			{
				String userName = entryForUser.getKey();

				System.out.println("\nUser =" + entryForUser.getKey());
				String fileName = commonPath + userName + filenameEndPhrase + ".csv";

				File file = new File(fileName);

				file.delete();
				if (!file.exists())
				{
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);

				TreeMap<Timestamp, String> mapForEachUser = new TreeMap<Timestamp, String>();

				for (Map.Entry<Timestamp, String> entry : entryForUser.getValue().entrySet())
				{
					System.out.println(entry.getKey() + "," + entry.getValue());
					bw.write(entry.getKey() + "," + entry.getValue() + "\n");
				}

				bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/*
	 * File file = new File(commonPath+"stats.csv");
	 * 
	 * if (!file.exists()) { file.createNewFile(); }
	 * 
	 * FileWriter fw = new FileWriter(file.getAbsoluteFile(),true); BufferedWriter bw = new BufferedWriter(fw);
	 * bw.write(content); bw.close();
	 * 
	 */

	public static int countLines(String filename) throws IOException
	{
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try
		{
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1)
			{
				empty = false;
				for (int i = 0; i < readChars; ++i)
				{
					if (c[i] == '\n')
					{
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		}
		finally
		{
			is.close();
		}
	}
}
