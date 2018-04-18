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
import java.io.PrintStream;
//import java.math.String;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.activity.constants.Constant;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.LabelEntry;
import org.activity.objects.Pair;
import org.activity.objects.TrajectoryEntry;
import org.activity.ui.PopUps;
import org.activity.util.UtilityBelt;

/**
 * Reads
 * 
 * @author gunjan
 *
 */
public class DatabaseCreatorGeolife
{
	// public static String commonPath="/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/Lifelog Working
	// dataset 3 july copy/";

	static ArrayList<String> modeNames;
	/*
	 * static final String[] activityNames= {"badImages", "Commuting", "Computer", "Eating", "Exercising", "Housework",
	 * "On the Phone", "Preparing Food", "Shopping", "Socialising", "Watching TV"};
	 */

	// static LinkedHashMap<String, TreeMap<Timestamp,String>> mapForAllData;
	static LinkedHashMap<String, ArrayList<LabelEntry>> mapForLabelEntries;
	static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllData;

	static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataTimeDifference;
	static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedContinuousWithDuration;
	// static LinkedHashMap<String, TreeMap<Timestamp,String>> mapForAllDataMergedSandwichedWithDuration;

	static ArrayList<String> userIDs;

	// ******************PARAMETERS TO SET*****************************//
	public static String commonPath = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data Works/Mar162016AllUsersDataGeneration/";// ///"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Mar152016AllUsersDataGeneration/";//
																																										// May18AllUsersDataGeneration2/";//
																																										// //
																																										// =
																																										// "/run/media/gunjan/HOME/gunjan/Geolife
	// Data Works/";
	public static final String rawPathToRead = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data Works/Raw/Geolife Trajectories 1.3/Data/";
	static String nameForMapToBeSerialised = "mapForAllDataMergedPlusDuration16Mar2016.map";// ";//"mapForAllDataMergedPlusDuration18May2015_2.map";
	// static final String[] userNames= {"Stefan", "Tengqi","Cathal", "Zaher","Rami"};
	public static final int continuityThresholdInSeconds = 5 * 60; // changed from 30 min in DCU dataset...., if two
																	// timestamps are separated by less than equal to
																	// this value and
																	// have same mode name,
																	// then they are assumed to be continuos
	public static final int assumeContinuesBeforeNextInSecs = 2 * 60; // changed from 30 min in DCU dataset we assume
																		// that
	// if two activities have a start time gap of more than 'assumeContinuesBeforeNextInSecs' seconds ,
	// then the first activity continues for 'assumeContinuesBeforeNextInSecs' seconds before the next activity starts.
	public static final int thresholdForMergingNotAvailables = 5 * 60;

	public static final int timeDurationForLastSingletonTrajectoryEntry = 2 * 60;

	// ******************END OF PARAMETERS TO SET*****************************//
	public static void main(String args[])
	{
		// String userNames={""
		// LinkedHashMap<String,String> userMap=new LinkedHashMap <String,String>();
		// userMap.put("Stefan","C:\\Users\\gunjan\\Documents\\Lifelog working dataset\\Data_Set_Stefan\\Data Set");
		try
		{
			long ct1 = System.currentTimeMillis();

			Constant.setCommonPath(commonPath);// April14_2015/DuringDataGeneration/");// commonPath);
			// commonPath = Constant.getCommonPath();
			// Redirecting the console output
			PrintStream consoleLogStream = new PrintStream(new File(commonPath + "consoleLogDatabaseCreatorGeo.txt"));
			// System.setOut(new PrintStream(new FileOutputStream('/dev/stdout')));
			System.setOut(new PrintStream(consoleLogStream));
			System.setErr(consoleLogStream);
			// ConnectDatabaseV1.getTimestamp("B00000028_21I5H1_20140216_170559E.JPG,");
			System.out.println("\ncontinuityThresholdInSeconds=" + continuityThresholdInSeconds
					+ "\nassumeContinuesBeforeNextInSecs" + assumeContinuesBeforeNextInSecs);
			// /userIDs= identifyUsers();

			// -XX:-UseGCOverheadLimit
			userIDs = identifyOnlyTargetUsers();// identifyUsers();//
			// userIDs= identifyOnlyGivenUsers(new int[]{60});
			mapForLabelEntries = createLabelEntryMap(); // read label entries

			long ct2 = System.currentTimeMillis();
			System.out.println("Creating Label Entry Map done in " + ((ct2 - ct1) / 1000) + " seconds since start");

			/*
			 * * 22 dec 2014 //$$ createDataset();
			 */
			mapForAllData = createAnnotatedTrajectoryMap(); // read trajectory entries

			long ct3 = System.currentTimeMillis();
			System.out.println("createAnnotatedTrajectoryMap done in " + ((ct3 - ct1) / 1000) + " seconds since start");
			/*
			 * //traverseMapForAllData(mapForAllData);
			 */
			mapForAllDataTimeDifference = getTrajectoryEntriesWithTimeDifferenceWithNext(mapForAllData); // add time
																											// difference
																											// with next
																											// to the
																											// trajectory
																											// entries
			// write all data to a file
			// ImP
			/*
			 * $$ writeDataToFile2WithHeaders( mapForAllDataTimeDifference, "TimeDifference",
			 * "timestamp, endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt"
			 * , true);$$
			 */
			// ImP

			// "timestamp,
			// endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt"
			// WritingToFile.writeActivityTypeWithTimeDifference(mapForAllDataTimeDifference, "Not Available",
			// "WithTimeDifferenceRaw"); for dcu data
			/*
			 * //WritingToFile.writeNotAnnotatedWithTimeDifference(mapForAllDataTimeDifference,
			 * "NotAnnotatedWithTimeDifference");
			 */
			long ct3_1 = System.currentTimeMillis();
			System.out.println("getTrajectoryEntriesWithTimeDifferenceWithNext done in " + ((ct3_1 - ct1) / 1000)
					+ " seconds since start");

			mapForAllDataMergedContinuousWithDuration = mergeContinuousTrajectoriesAssignDurationWithoutBOD2TrajSensitive(
					mapForAllData);
			// $$Used in UMAP mergeContinuousTrajectoriesAssignDurationWithoutBOD2(mapForAllData);

			// ImP
			// writeDataToFile2WithHeaders(
			// mapForAllDataMergedContinuousWithDuration,
			// "AfterMergingContinuous",
			// "timestamp,
			// endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt",
			// true);// "trajID,timestamp,
			// endt,mode,latitude,longitude,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount",

			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedContinuousWithDuration, "Not
			// Available", "MergedContinuous",
			// true);
			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedContinuousWithDuration, "Unknown",
			// "MergedContinuous", true);
			// ImP

			// $$ traverseMapForAllData(mapForAllDataMergedContinuousWithDuration);
			// mergeContinuousTrajectoriesAssignDurationWithoutBOD
			// writeDataToFile2(mapForAllDataMergedContinuousWithDuration,"AfterMergingContinuous");
			/*
			 * //##WritingToFile.writeActivityTypeWithDuration(mapForAllDataMergedContinuousWithDuration,"Not Available"
			 * ,"MergedContinuous");
			 * WritingToFile.writeActivityTypeWithDuration(mapForAllDataMergedContinuousWithDuration,"Unknown",
			 * "MergedContinuous");
			 * 
			 * 
			 * 
			 * //WritingToFile.writeOnlyNotAnnotatedOthersWithDuration(mapForAllDataMergedPlusDuration);
			 * 
			 * mapForAllDataMergedSandwichedWithDuration =
			 * mergeSmallSandwiched(mapForAllDataMergedContinuousWithDuration,"badImages");
			 * mapForAllDataMergedSandwichedWithDuration =
			 * mergeSmallSandwiched(mapForAllDataMergedContinuousWithDuration,"Not Available");
			 * 
			 * //##writeDataToFile(mapForAllDataMergedSandwichedWithDuration,"AfterMergingContinuousAndSandwiching");
			 * WritingToFile.writeActivityTypeWithDuration(mapForAllDataMergedSandwichedWithDuration,"Not Available"
			 * ,"MergedContinuousOnlySandwichedBothU_No_Thres");
			 * WritingToFile.writeActivityTypeWithDuration(mapForAllDataMergedSandwichedWithDuration,"Unknown",
			 * "MergedContinuousOnlySandwichedothU_No_Thres");
			 * WritingToFile.writeActivityTypeWithDuration(mapForAllDataMergedSandwichedWithDuration,"badImages",
			 * "MergedContinuousOnlySandwichedothU_No_Thres");
			 * 
			 * // mapForAllDataMergedPlusDuration =
			 * mergeSmallSandwiched(mapForAllDataMergedPlusDuration,"Not Available"); /*mapForAllDataMergedPlusDuration=
			 * mergeCleanSmallNotAvailables(mapForAllDataMergedPlusDuration); mapForAllDataMergedPlusDuration=
			 * mergeConsectiveSimilars(mapForAllDataMergedPlusDuration);
			 */
			// mergeConsectiveSimilars

			// System.out.println("----Merged Activity data with duration ----------");
			// traverseMapForAllData(mapForAllDataMergedPlusDuration);
			// System.out.println("----END OF Merged Activity data with duration----------");

			// ##
			// Serializer.serializeThis(mapForAllDataMergedSandwichedWithDuration,commonPath+"mapForAllDataMergedPlusDuration.map");

			// writeDataToFile2(mapForAllDataMergedContinuousWithDuration, "DataGenerated");
			// writeDataToFileWithTrajPurityCheck(mapForAllDataMergedContinuousWithDuration, "DataGenerated");
			writeDataToFile2WithHeadersWithTrajPurityCheck(mapForAllDataMergedContinuousWithDuration, "DataGenerated",
					"timestamp, endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,#distinctTrajIDs,#TrajIDs,trajID,,latitude,longitude,alt",
					true);
			long ct3_2 = System.currentTimeMillis();
			System.out.println("Will start serialisation now.  " + ((ct3_2 - ct1) / 1000) + " seconds since start");

			Serializer.serializeThis(mapForAllDataMergedContinuousWithDuration, commonPath + nameForMapToBeSerialised);// 14April2015.map");

			// LinkedHashMap<String, TreeMap<Timestamp,String>> testSerializer=(LinkedHashMap<String,
			// TreeMap<Timestamp,String>>)(Serializer.deSerializeThis(commonPath+"mapForAllDataMergedPlusDuration.map"));
			// traverseMapForAllData(testSerializer);

			// ConnectDatabaseV1.insertIntoImageTable();
			// ConnectDatabaseV1.insertDummyIntoImageTable();
			long ct4 = System.currentTimeMillis();
			PopUps.showMessage("All data creation done in " + ((ct4 - ct1) / 1000) + " seconds since start");
			consoleLogStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("End of program");
		PopUps.showMessage("End of data creation");
		System.exit(0);
	}

	public static ArrayList<String> identifyOnlyTargetUsers()
	{
		ArrayList<String> listOfUsersWhoLabelled = new ArrayList<String>();
		int userIDs[] = { 62, 84, 52, 68, 167, 179, 153, 85, 128, 10 };
		for (int i : userIDs)
		{
			String userID = String.format("%03d", i);
			listOfUsersWhoLabelled.add(userID);
		}

		return listOfUsersWhoLabelled;
	}

	public static ArrayList<String> identifyOnlyGivenUsers(int[] givenUsersArray)
	{
		ArrayList<String> listOfUsersWhoLabelled = new ArrayList<String>();
		// int userIDs[]={62,84,52,68,167,179,153,85,128,10};
		for (int i : givenUsersArray)
		{
			String userID = String.format("%03d", i);
			listOfUsersWhoLabelled.add(userID);
		}

		return listOfUsersWhoLabelled;
	}

	/**
	 * Identify users who have labelled their mode of transportation. (i.e., identify users who have 'labels.txt' file)
	 * 
	 * @return
	 */
	public static ArrayList<String> identifyUsers()
	{
		ArrayList<String> listOfUsersWhoLabelled = new ArrayList<String>();
		BufferedReader br = null;
		String pathToParse = rawPathToRead;// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to
											// Geolife Data Works/Raw/Geolife Trajectories 1.3/Data/";
		// commonPath + "Raw/Geolife Trajectories 1.3/Data/";
		try
		{
			for (int i = 0; i <= 181; i++)
			{
				String userID = String.format("%03d", i);
				String pathToLookInto = pathToParse + userID + "/";
				String fileAbsoluteToLook = pathToLookInto + "labels.txt";
				// check if labels.txt is there in the folder
				File file = new File(fileAbsoluteToLook);
				if (file.exists()) // users which have labels.txt... note: each users who have labelled their data have
									// one labels.txt file
				{
					listOfUsersWhoLabelled.add(userID);
				}
			}
		}
		catch (Exception e)
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
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		System.out.println("Number of users who labelled their data = " + listOfUsersWhoLabelled.size()
				+ "  (i.e., users for which labels.txt exists)\n");
		// for(String userID: listOfUsersWhoLabelled) { System.out.println(" "+userID); }
		return listOfUsersWhoLabelled;
	}

	/**
	 * Merges continuous activities with same activity names and start timestamp difference of less than
	 * 'continuityThresholdInSeconds'. without break over days
	 * 
	 * Duration assigned is difference between the start-timestamp of this activity and start-timestamp of the next
	 * (different) activity. difference between the start-timestamp of this activity and start-timestamp of the next
	 * (different) activity BUT ONLY IF this difference is less than P2 minutes, otherwise the duration is P2 minutes.
	 * 
	 * Adds 'Unknown' and writes the unknown inserted to a file "Unknown_Wholes_Inserted.csv" with columns
	 * "User,Timestamp,DurationInSecs"
	 * 
	 * Nuances of merging consecutive activities and calculation the duration of activities.
	 * 
	 * 
	 * @param mapForAllData
	 *            is LinkedHashMap of the form <username, <timestamp,TrajectoryEntry>>
	 * @return <UserName, <Timestamp,TrajectoryEntry>>
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mergeContinuousTrajectoriesAssignDurationWithoutBOD2(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllData)
	{
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();
		/*
		 * Note: using TreeMap is IMPORTANT here, because TreeMap will automatically sort by the timestamp, so we do not
		 * need to be concerned about whether we add theactivities in correct order or not, if the timestamps are right,
		 * it will be stored correctly
		 */
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();

		System.out.println("Merging continuous trajectories and assigning duration without BOD");
		try
		{
			for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : mapForAllData.entrySet())
			{
				String userID = entryForUser.getKey();
				BufferedWriter bwMergerCaseLogs = WToFile
						.getBWForNewFile(commonPath + userID + "MergerCasesLog.csv");
				bwMergerCaseLogs.write("Case,Mode,DurationInSecs,CurrentTS, NextTS,Comment\n");

				System.out.println("\nUser =" + userID);

				int numOfTrajCaseA = 0, numOfTrajCaseB = 0, numOfTrajCaseC = 0, numOfLastTrajEntries = 0;

				int countOfContinuousMerged = 1;

				/** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
				TreeMap<Timestamp, TrajectoryEntry> unknownsInsertedWholes = new TreeMap<Timestamp, TrajectoryEntry>();
				TreeMap<Timestamp, TrajectoryEntry> mapContinuousMerged = new TreeMap<Timestamp, TrajectoryEntry>();

				long durationInSeconds = 0;
				ArrayList<String> newLati = new ArrayList<String>(), newLongi = new ArrayList<String>(),
						newAlti = new ArrayList<String>();
				ArrayList<String> newTrajID = new ArrayList<String>();

				long timeDiffWithNextInSeconds = 0; // do not delete. // not directly relevant
				Timestamp startTimestamp;

				ArrayList<TrajectoryEntry> trajEntriesForCurrentUser = UtilityBelt
						.treeMapToArrayListGeo(entryForUser.getValue());

				// $$System.out.println("----Unmerged Activity data for user "+userName+"-----");
				// $$traverseArrayList(dataForCurrentUser);
				// $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
				for (int i = 0; i < trajEntriesForCurrentUser.size(); i++)
				{
					// startTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));
					// ##
					// $$System.out.println("\nReading: "+dataForCurrentUser.get(i).toString());
					Timestamp currentTimestamp = trajEntriesForCurrentUser.get(i).getTimestamp();
					String currentModeName = trajEntriesForCurrentUser.get(i).getMode();

					ArrayList<String> currentLat = trajEntriesForCurrentUser.get(i).getLatitude();
					ArrayList<String> currentLon = trajEntriesForCurrentUser.get(i).getLongitude();
					ArrayList<String> currentAlt = trajEntriesForCurrentUser.get(i).getAltitude();
					ArrayList<String> currentTrajID = trajEntriesForCurrentUser.get(i).getTrajectoryID();

					newLati.addAll(currentLat);
					newLongi.addAll(currentLon);
					newAlti.addAll(currentAlt);
					newTrajID.addAll(currentTrajID);
					// startTimestamp=currentTimestamp;

					if (i < trajEntriesForCurrentUser.size() - 1) // is not the last element of arraylist
					{
						// check if the next element should be merged with this one if they are continuous and have same
						// activity name
						Timestamp nextTimestamp = trajEntriesForCurrentUser.get(i + 1).getTimestamp();
						String nextModeName = trajEntriesForCurrentUser.get(i + 1).getMode();

						// ArrayList<Double> nextLat = dataForCurrentUser.get(i+1).getLatitude();
						// ArrayList<Double> nextLon = dataForCurrentUser.get(i+1).getLongitude();
						// ArrayList<Double> nextAlt = dataForCurrentUser.get(i+1).getAltitude();

						if (nextModeName.equals(currentModeName) && areContinuous(currentTimestamp, nextTimestamp))
						{
							numOfTrajCaseA += 1;
							durationInSeconds += (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;

							timeDiffWithNextInSeconds = trajEntriesForCurrentUser.get(i)
									.getDifferenceWithNextInSeconds()
									+ trajEntriesForCurrentUser.get(i + 1).getDifferenceWithNextInSeconds(); // TODO
																												// CHECK
																												// IF
																												// NOT
																												// NEEDED

							// newLati.addAll(currentLat);
							// newLongi.addAll(currentLon);
							// newAlti.addAll(currentAlt);

							// newLati.addAll(nextLat);
							// newLongi.addAll(nextLon);
							// newAlti.addAll(nextAlt);

							countOfContinuousMerged++;
							// ##bwMergerCaseLogs.write("CaseA: Continuous merged for mode=" + currentModeName + "
							// durationInSeconds="+ durationInSeconds + "\n");
							bwMergerCaseLogs.write("CaseA," + currentModeName + "," + durationInSeconds + ","
									+ currentTimestamp + "," + nextTimestamp + ",merged as continuous\n");
							continue;
						}

						else
						{
							startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));// durationInSeconds
																													// is
																													// the
																													// accumulated
																													// duration
																													// from
																													// past
																													// merging
							// ##System.out.println("new starttimestamp="+startTimestamp);

							long diffCurrentAndNextInSec = (nextTimestamp.getTime() - currentTimestamp.getTime())
									/ 1000;
							long secsItContinuesBeforeNext;

							if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were
																							// different activity names
							{
								numOfTrajCaseB += 1;
								secsItContinuesBeforeNext = diffCurrentAndNextInSec;
								// ##bwMergerCaseLogs.write("CaseB: diffCurrentAndNextInSec <=
								// assumeContinuesBeforeNextInSecs, secsItContinuesBeforeNext=" +
								// secsItContinuesBeforeNext + "\n");
								bwMergerCaseLogs.write("CaseB," + currentModeName + "," + durationInSeconds + ","
										+ currentTimestamp + "," + nextTimestamp + "," + diffCurrentAndNextInSec + "<="
										+ assumeContinuesBeforeNextInSecs + " secsItContinuesBeforeNext ="
										+ secsItContinuesBeforeNext + "\n");
							}

							else
							{
								// System.out.println("\n\t For user: "+userID+", at
								// currentTimestamp="+currentTimestamp+", currentModeName="+currentModeName);
								numOfTrajCaseC += 1;
								secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;

								// ##bwMergerCaseLogs.write("CaseC: diffCurrentAndNextDifferentInSec (" +
								// diffCurrentAndNextInSec + ") >"+ assumeContinuesBeforeNextInSecs + "\n");
								bwMergerCaseLogs.write("CaseC," + currentModeName + "," + durationInSeconds + ","
										+ currentTimestamp + "," + nextTimestamp + "," + diffCurrentAndNextInSec + ">"
										+ assumeContinuesBeforeNextInSecs + " secsItContinuesBeforeNext ="
										+ secsItContinuesBeforeNext + "  put new Unknown\n");

								/* Put the new 'Unknown' entry///////////////// */
								long durationForNewUnknownActivity = diffCurrentAndNextInSec
										- assumeContinuesBeforeNextInSecs;
								Timestamp startOfNewUnknown = new Timestamp(
										currentTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));

								// unknownsInsertedWholes.put(startOfNewUnknown, new TrajectoryEntry(startOfNewUnknown,
								// durationForNewUnknownActivity,"Unknown")); //
								// String.valueOf(durationForNewUnknownActivity));

								TrajectoryEntry te = new TrajectoryEntry(startOfNewUnknown,
										durationForNewUnknownActivity, "Unknown");// ,bodCount);
								mapContinuousMerged.put(startOfNewUnknown, te);
								unknownsInsertedWholes.put(startOfNewUnknown, te);
								// $$System.out.println("Added Trajectory Entry: "+te.toString());
							}

							durationInSeconds = durationInSeconds + secsItContinuesBeforeNext;

							TrajectoryEntry te = trajEntriesForCurrentUser.get(i);
							te.setLatitude(newLati); // note: has to be done with set,..cant do with add becasue it will
														// create problems if more than two entries are merged
							te.setLongitude(newLongi);
							te.setAltitude(newAlti);
							te.setTrajectoryID(newTrajID);

							te.setTimestamp(startTimestamp);
							te.setDurationInSeconds(durationInSeconds);
							// te.setDifferenceWithNextInSeconds(timeDiffWithNextInSeconds);

							mapContinuousMerged.put(startTimestamp, te);
							// $$System.out.println("Added Trajectory Entry: "+te.toString());

							// durationInSeconds =0;
							// timeDiffWithNextInSeconds =0;
							// newLati.clear();newLongi.clear();newAlti.clear();
						}

					}
					else
					// is the last element
					{
						numOfLastTrajEntries += 1;

						// $$System.out.println("this is the last data point,\n duration in seconds =
						// "+durationInSeconds);

						startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));

						TrajectoryEntry te = trajEntriesForCurrentUser.get(i);
						te.setLatitude(newLati); // note: has to be done with set,..cant do with add becasue it will
													// create problems if more than two entries are merged
						te.setLongitude(newLongi);
						te.setAltitude(newAlti);
						te.setTrajectoryID(newTrajID);
						te.setTimestamp(startTimestamp);
						te.setDurationInSeconds(durationInSeconds + timeDurationForLastSingletonTrajectoryEntry);

						mapContinuousMerged.put(startTimestamp, te);

						// $$System.out.println("Added Trajectory Entry: "+te.toString());

						// newLati.clear();newLongi.clear();newAlti.clear();
						// //////////////////////
						// /*REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
						// currentActivityName+"||"+durationInSeconds); */
						// durationInSeconds=0;
					}

					// $$System.out.println("Clearing variables");//
					durationInSeconds = 0;
					timeDiffWithNextInSeconds = 0;
					newLati.clear();
					newLongi.clear();
					newAlti.clear();
					newTrajID.clear();
				} // end of for loop over trajectory entries for current user.

				mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);
				mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);

				bwMergerCaseLogs.write("User:" + userID + ",numOfTrajCaseA = " + numOfTrajCaseA + ",numOfTrajCaseB = "
						+ numOfTrajCaseB + ",numOfTrajCaseC = " + numOfTrajCaseC + " ,numOfLastTrajEntries = "
						+ numOfLastTrajEntries);
				System.out.println("User:" + userID + ",numOfTrajCaseA = " + numOfTrajCaseA + ",numOfTrajCaseB = "
						+ numOfTrajCaseB + ",numOfTrajCaseC = " + numOfTrajCaseC + " ,numOfLastTrajEntries = "
						+ numOfLastTrajEntries);
				bwMergerCaseLogs.close();
			} // end of for loop over users

			WToFile.writeLinkedHashMapOfTreemapTrajEntry(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
					"User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeContinuousTrajectoriesAssignDurationWithoutBOD2()");
		}
		return mapForAllDataMergedPlusDuration;
	}

	// //

	/**
	 * Merges continuous activities with same activity names and start timestamp difference of less than
	 * 'continuityThresholdInSeconds'. without break over days
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

	// THIS ONE IF NOT CORRECT
	// public static LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>>
	// mergeContinuousTrajectoriesAssignDurationWithoutBOD
	// (LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>> mapForAllData)
	// {
	// /*<username , <start timestamp, 'activityname||durationinseconds||numOfImagesMerged||bodCode'> bod stands for
	// 'break over days'*/
	// LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>> mapForAllDataMergedPlusDuration= new
	// LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>> ();
	// /*Note: using TreeMap is IMPORTANThere, because TreeMap will automatically sort by the timestamp, so we do not
	// need to be concerned about whether we add the
	// *activities in correct order or not, if the timestamps are right, it will be stored correctly
	// */
	//
	// LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String,
	// TreeMap<Timestamp,TrajectoryEntry>> ();
	// LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllUnknownsBrokenOverDays = new
	// LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>> ();
	//
	// System.out.println("Merging continuous trajectories and assigning duration");
	//
	// for (Map.Entry<String, TreeMap<Timestamp,TrajectoryEntry>> entryForUser : mapForAllData.entrySet())
	// {
	// String userID=entryForUser.getKey();
	//
	// int countOfContinuousMerged=1;
	//
	// /** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
	// TreeMap<Timestamp,TrajectoryEntry> unknownsInsertedWholes= new TreeMap<Timestamp,TrajectoryEntry> ();
	//
	// /** Records the "Unknown"s inserted BROKEN OVER DAYS, the <start timestamp of the insertion, duration of the
	// unknown> */
	// TreeMap<Timestamp,TrajectoryEntry> unknownsInsertedBrokenOverDays= new TreeMap<Timestamp,TrajectoryEntry> ();
	//
	// System.out.println("\nUser ="+entryForUser.getKey());
	//
	// TreeMap<Timestamp,TrajectoryEntry> mapContinuousMerged= new TreeMap<Timestamp,TrajectoryEntry>();
	//
	// // Timestamp previousTimestamp= new Timestamp(0);
	// //String previousActivityName = new String("");
	// long durationInSeconds=0;
	// ArrayList<Double> newLati= new ArrayList<Double>(),newLongi= new ArrayList<Double>() ,newAlti= new
	// ArrayList<Double>();
	// long timeDiffWithNextInSeconds = 0; //not directly relevant
	// Timestamp startTimestamp;
	//
	// ArrayList<TrajectoryEntry> dataForCurrentUser = treeMapToArrayList2(entryForUser.getValue());
	// //$$System.out.println("----Unmerged Activity data for user "+userName+"-----");
	// //$$traverseArrayList(dataForCurrentUser);
	// //$$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
	// for(int i=0;i<dataForCurrentUser.size();i++)
	// {
	// //startTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));
	// //##
	// System.out.println("\nReading: "+dataForCurrentUser.get(i).toString());
	//
	// Timestamp currentTimestamp = dataForCurrentUser.get(i).getTimestamp();
	// String currentModeName = dataForCurrentUser.get(i).getMode();
	// ArrayList<Double> currentLat = dataForCurrentUser.get(i).getLatitude();
	// ArrayList<Double> currentLon = dataForCurrentUser.get(i).getLongitude();
	// ArrayList<Double> currentAlt = dataForCurrentUser.get(i).getAltitude();
	//
	// newLati.addAll(currentLat);
	// newLongi.addAll(currentLon);
	// newAlti.addAll(currentAlt);
	// //startTimestamp=currentTimestamp;
	//
	// if(i<dataForCurrentUser.size()-1) // is not the last element of arraylist
	// {
	// //check if the next element should be merged with this one if they are continuous and have same activity name
	// Timestamp nextTimestamp = dataForCurrentUser.get(i+1).getTimestamp();
	// String nextModeName = dataForCurrentUser.get(i+1).getMode();
	//
	// ArrayList<Double> nextLat = dataForCurrentUser.get(i+1).getLatitude();
	// ArrayList<Double> nextLon = dataForCurrentUser.get(i+1).getLongitude();
	// ArrayList<Double> nextAlt = dataForCurrentUser.get(i+1).getAltitude();
	//
	// if(nextModeName.equals(currentModeName) && areContinuous(currentTimestamp, nextTimestamp) )
	// {
	// durationInSeconds += (nextTimestamp.getTime() -currentTimestamp.getTime())/1000;
	//
	// timeDiffWithNextInSeconds = dataForCurrentUser.get(i).getDifferenceWithNextInSeconds() +
	// dataForCurrentUser.get(i+1).getDifferenceWithNextInSeconds();
	//
	// // newLati.addAll(currentLat);
	// // newLongi.addAll(currentLon);
	// // newAlti.addAll(currentAlt);
	//
	// // newLati.addAll(nextLat);
	// // newLongi.addAll(nextLon);
	// // newAlti.addAll(nextAlt);
	//
	// countOfContinuousMerged++;
	// //##
	// System.out.println("Case 1: Continuous merged for mode="+currentModeName+"
	// durationInSeconds="+durationInSeconds);
	// continue;
	// }
	// else
	// {
	// startTimestamp = new Timestamp(currentTimestamp.getTime()-(durationInSeconds*1000));//durationInSeconds is the
	// accumulated duration from past merging
	// //##System.out.println("new starttimestamp="+startTimestamp);
	//
	// long diffCurrentAndNextInSec = (nextTimestamp.getTime() -currentTimestamp.getTime())/1000 ;
	// long secsItContinuesBeforeNext;
	//
	// if(diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were different activity
	// names
	// {
	// secsItContinuesBeforeNext = diffCurrentAndNextInSec;
	// //##
	// System.out.println("Case2: diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs,
	// secsItContinuesBeforeNext="+secsItContinuesBeforeNext);
	// }
	//
	// else
	// {
	// //##
	// System.out.println("Case3: diffCurrentAndNextDifferentInSec ("+diffCurrentAndNextInSec +") >
	// assumeContinuesBeforeNextInSecs");
	// //System.out.println("\n\t For user: "+userID+", at currentTimestamp="+currentTimestamp+",
	// currentModeName="+currentModeName);
	//
	// secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;
	//
	// /* Put the new 'Unknown' entry/////////////////*/
	// long durationForNewUnknownActivity= diffCurrentAndNextInSec - assumeContinuesBeforeNextInSecs;
	// Timestamp startOfNewUnknown=new Timestamp(currentTimestamp.getTime()+ (assumeContinuesBeforeNextInSecs*1000));
	// unknownsInsertedWholes.put(startOfNewUnknown, new TrajectoryEntry(startOfNewUnknown,
	// durationForNewUnknownActivity,"Unknown")); //
	// String.valueOf(durationForNewUnknownActivity));
	// TrajectoryEntry te=new TrajectoryEntry(startOfNewUnknown,durationForNewUnknownActivity,"Unknown");//,bodCount);
	// mapContinuousMerged.put(startOfNewUnknown,te);
	// System.out.println("Added Trajectory Entry: "+te.toString());
	// }
	// durationInSeconds = durationInSeconds + secsItContinuesBeforeNext;
	//
	//
	// TrajectoryEntry te = dataForCurrentUser.get(i);
	// te.setLatitude(newLati); //note: has to be done with set,..cant do with add becasue it will create problems if
	// more than two entries are merged
	// te.setLongitude(newLongi);
	// te.setAltitude(newAlti);
	//
	// te.setTimestamp(startTimestamp);;
	// te.setDurationInSeconds(durationInSeconds);
	// // te.setDifferenceWithNextInSeconds(timeDiffWithNextInSeconds);
	//
	// mapContinuousMerged.put(startTimestamp, te);
	// System.out.println("Added Trajectory Entry: "+te.toString());
	//
	// durationInSeconds =0;
	// timeDiffWithNextInSeconds =0;
	// newLati.clear();newLongi.clear();newAlti.clear();
	// }
	//
	//
	// }
	// else //is the last element
	// {
	// startTimestamp = new Timestamp(currentTimestamp.getTime()-(durationInSeconds*1000));
	// mapContinuousMerged.put(startTimestamp, dataForCurrentUser.get(i));
	// System.out.println("Added Trajectory Entry: "+dataForCurrentUser.get(i).toString());
	//
	// newLati.clear();newLongi.clear();newAlti.clear();
	// //////////////////////
	// /*REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
	// currentActivityName+"||"+durationInSeconds); */
	// //durationInSeconds=0;
	// }
	//
	// }
	//
	// mapForAllDataMergedPlusDuration.put(entryForUser.getKey(),mapContinuousMerged);
	// mapForAllUnknownsWholes.put(entryForUser.getKey(),unknownsInsertedWholes);
	// mapForAllUnknownsBrokenOverDays.put(entryForUser.getKey(),unknownsInsertedBrokenOverDays);
	// }
	//
	// WritingToFile.writeLinkedHashMapOfTreemap2(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
	// "User,Timestamp,DurationInSecs");
	// WritingToFile.writeLinkedHashMapOfTreemap2(mapForAllUnknownsBrokenOverDays, "Unknown_BrokenOverDays_Inserted",
	// "User,Timestamp,DurationInSecs");
	//
	// return mapForAllDataMergedPlusDuration;
	// }
	//

	// //

	// /**
	// * Merges continuous activities with same activity names and start timestamp difference of less than
	// 'continuityThresholdInSeconds'.
	// *
	// * Duration assigned is
	// * difference between the start-timestamp of this activity and start-timestamp of the next (different) activity.
	// * difference between the start-timestamp of this activity and start-timestamp of the next (different) activity
	// BUT ONLY IF this difference is less than P2 minutes, otherwise
	// the duration is P2
	// minutes.
	// *
	// * Add 'Unknown'
	// *
	// * Nuances of merging consecutive activities and calculation the duration of activities.
	// *
	// *
	// * @param mapForAllData is LinkedHashMap of the form <username, <timestamp,'imagename||activityname'>>
	// * @return <UserName, <Timestamp,'activityname||durationInSeconds'>>
	// */
	// public static LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>>
	// mergeContinuousTrajectoriesAssignDuration(LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>>
	// mapForAllData)
	// {
	// /*<username , <start timestamp, 'activityname||durationinseconds||numOfImagesMerged||bodCode'> bod stands for
	// 'break over days'*/
	// LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>> mapForAllDataMergedPlusDuration= new
	// LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>> ();
	// /*Note: using TreeMap is IMPORTANThere, because TreeMap will automatically sort by the timestamp, so we do not
	// need to be concerned about whether we add the
	// *activities in correct order or not, if the timestamps are right, it will be stored correctly
	// */
	//
	// LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String,
	// TreeMap<Timestamp,TrajectoryEntry>> ();
	// LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllUnknownsBrokenOverDays = new
	// LinkedHashMap<String, TreeMap<Timestamp,TrajectoryEntry>> ();
	//
	// System.out.println("Merging continuous trajectories and assigning duration");
	//
	// for (Map.Entry<String, TreeMap<Timestamp,TrajectoryEntry>> entryForUser : mapForAllData.entrySet())
	// {
	// String userID=entryForUser.getKey();
	//
	// int countOfContinuousMerged=1;
	//
	// /** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
	// TreeMap<Timestamp,TrajectoryEntry> unknownsInsertedWholes= new TreeMap<Timestamp,TrajectoryEntry> ();
	//
	// /** Records the "Unknown"s inserted BROKEN OVER DAYS, the <start timestamp of the insertion, duration of the
	// unknown> */
	// TreeMap<Timestamp,TrajectoryEntry> unknownsInsertedBrokenOverDays= new TreeMap<Timestamp,TrajectoryEntry> ();
	//
	// System.out.println("\nUser ="+entryForUser.getKey());
	//
	// TreeMap<Timestamp,TrajectoryEntry> mapContinuousMerged= new TreeMap<Timestamp,TrajectoryEntry>();
	//
	// // Timestamp previousTimestamp= new Timestamp(0);
	// //String previousActivityName = new String("");
	// long durationInSeconds=0;
	// ArrayList<Double> lati= new ArrayList<Double>(),longi= new ArrayList<Double>() ,alti= new ArrayList<Double>();
	// long timeDiffWithNextInSeconds = 0; //not directly relevant
	// Timestamp startTimestamp;
	//
	// ArrayList<TrajectoryEntry> dataForCurrentUser = treeMapToArrayList2(entryForUser.getValue());
	//
	// //$$System.out.println("----Unmerged Activity data for user "+userName+"-----");
	//
	// //$$traverseArrayList(dataForCurrentUser);
	//
	// //$$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
	//
	// for(int i=0;i<dataForCurrentUser.size();i++)
	// {
	// //startTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));
	// //##System.out.println("Reading: "+dataForCurrentUser.get(i).toString());
	// Timestamp currentTimestamp = dataForCurrentUser.get(i).getTimestamp();
	// String currentModeName = dataForCurrentUser.get(i).getMode();
	// ArrayList<Double> currentLat = dataForCurrentUser.get(i).getLatitude();
	// ArrayList<Double> currentLon = dataForCurrentUser.get(i).getLongitude();
	// ArrayList<Double> currentAlt = dataForCurrentUser.get(i).getAltitude();
	//
	// //startTimestamp=currentTimestamp;
	//
	// if(i<dataForCurrentUser.size()-1) // is not the last element of arraylist
	// {
	// //check if the next element should be merged with this one if they are continuous and have same activity name
	// Timestamp nextTimestamp = dataForCurrentUser.get(i+1).getTimestamp();
	// String nextModeName = dataForCurrentUser.get(i+1).getMode();
	//
	// ArrayList<Double> nextLat = dataForCurrentUser.get(i+1).getLatitude();
	// ArrayList<Double> nextLon = dataForCurrentUser.get(i+1).getLongitude();
	// ArrayList<Double> nextAlt = dataForCurrentUser.get(i+1).getAltitude();
	//
	// if(nextModeName.equals(currentModeName) && areContinuous(currentTimestamp, nextTimestamp) )
	// {
	// durationInSeconds += (nextTimestamp.getTime() -currentTimestamp.getTime())/1000;
	//
	// timeDiffWithNextInSeconds = dataForCurrentUser.get(i).getDifferenceWithNextInSeconds() +
	// dataForCurrentUser.get(i+1).getDifferenceWithNextInSeconds();
	// lati.addAll(currentLat);lati.addAll(nextLat);
	// longi.addAll(currentLon);longi.addAll(nextLon);
	// alti.addAll(currentAlt);alti.addAll(nextAlt);
	//
	// countOfContinuousMerged++;
	// //##System.out.println("Case 1: Continuous merged for mode="+currentModeName+"
	// durationInSeconds="+durationInSeconds);
	// continue;
	// }
	// else
	// {
	// startTimestamp = new Timestamp(currentTimestamp.getTime()-(durationInSeconds*1000));//durationInSeconds is the
	// accumulated duration from past merging
	// //##System.out.println("new starttimestamp="+startTimestamp);
	//
	// long diffCurrentAndNextInSec = (nextTimestamp.getTime() -currentTimestamp.getTime())/1000 ;
	// long secsItContinuesBeforeNext;
	//
	// if(diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were different activity
	// names
	// {
	// secsItContinuesBeforeNext = diffCurrentAndNextInSec;
	// //##System.out.println("Case2: diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs,
	// secsItContinuesBeforeNext="+secsItContinuesBeforeNext);
	// }
	//
	// else
	// {
	// //##System.out.print("Case3: diffCurrentAndNextDifferentInSec ("+diffCurrentAndNextInSec +") >
	// assumeContinuesBeforeNextInSecs");
	// //System.out.println("\n\t For user: "+userID+", at currentTimestamp="+currentTimestamp+",
	// currentModeName="+currentModeName);
	//
	// secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;
	//
	// /* Put the new 'Unknown' entry/////////////////*/
	// long durationForNewUnknownActivity= diffCurrentAndNextInSec - assumeContinuesBeforeNextInSecs;
	// Timestamp startOfNewUnknown=new Timestamp(currentTimestamp.getTime()+ (assumeContinuesBeforeNextInSecs*1000));
	//
	// unknownsInsertedWholes.put(startOfNewUnknown, new TrajectoryEntry(startOfNewUnknown,
	// durationForNewUnknownActivity,"Unknown")); //
	// String.valueOf(durationForNewUnknownActivity));
	// /*
	// * Break the Unknown activity event across days
	// *////////////////////
	// TreeMap<Timestamp,Long> mapBreakedUnknownActivitiesTimes=breakActivityEventOverDays(startOfNewUnknown,
	// durationForNewUnknownActivity);
	//
	// int bodCount=1; //bod is breaking over days.....bod1, bod2,bod3
	// for (Map.Entry<Timestamp, Long> entry : mapBreakedUnknownActivitiesTimes.entrySet())
	// {
	// //System.out.println("Start date="+entry.getKey()+" Duration in seconds:"+entry.getValue());
	// Timestamp stTimestamp= entry.getKey();
	// Long durationInSecs= entry.getValue();
	//
	// TrajectoryEntry te=new TrajectoryEntry(stTimestamp,durationInSecs,"Unknown",bodCount);
	//
	// mapContinuousMerged.put(entry.getKey(), te);//"Unknown"+"||"+entry.getValue().longValue()+"||1||bod"+bodCount);
	// //##System.out.println("Adding Unknown:"+te.toString());
	// unknownsInsertedBrokenOverDays.put(entry.getKey(),te);//String.valueOf(entry.getValue())+"||bod"+bodCount);
	// bodCount++;
	// }
	//
	// //////////////////////
	//
	// //mapContinuousMerged.put(startOfNewUnknown, "Unknown"+"||"+durationForNewUnknownActivity);
	// //$$System.out.println("Added: Unknown, starttimestamp:"+startOfNewUnknown+"
	// duration:"+durationForNewUnknownActivity);
	// /* */////////////
	// }
	//
	// durationInSeconds = durationInSeconds + secsItContinuesBeforeNext;
	//
	//
	// /*
	// * Break the activity event across days
	// */////////////////////
	// TreeMap<Timestamp,Long> mapBreakedActivitiesTimes=breakActivityEventOverDays(startTimestamp, durationInSeconds);
	// int bodCount=1; //bod is breaking over days.....bod1, bod2,bod3
	// for (Map.Entry<Timestamp, Long> entry : mapBreakedActivitiesTimes.entrySet())
	// {
	// //System.out.println("Start date="+entry.getKey()+" Duration in seconds:"+entry.getValue());
	// Timestamp stTimestamp= entry.getKey();
	// Long durationInSecs= entry.getValue();
	//
	// if(stTimestamp.getHours() < (new Timestamp(stTimestamp.getTime()+durationInSecs)).getHours())
	// {
	// System.err.println("Error 347 in databasecreator: improper breaking over days");
	// }
	//
	// TrajectoryEntry te = dataForCurrentUser.get(i);
	//
	// te.setTimestamp(stTimestamp);;
	// te.setDurationInSeconds(durationInSecs);
	// te.setBreakOverDaysCount(bodCount);
	// te.setDifferenceWithNextInSeconds(timeDiffWithNextInSeconds);
	//
	// mapContinuousMerged.put(entry.getKey(), te);
	// //##System.out.println("Step4:Adding: "+te.toString());
	// // mapContinuousMerged.put(entry.getKey(),
	// currentActivityName+"||"+entry.getValue().longValue()+"||"+countOfContinuousMerged+"||bod"+bodCount);
	// bodCount++;
	// }
	// //////////////////////
	//
	// /*REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
	// currentActivityName+"||"+durationInSeconds); */
	//
	// durationInSeconds =0;
	// timeDiffWithNextInSeconds =0;
	// }
	//
	//
	// }
	// else //is the last element
	// {
	// startTimestamp = new Timestamp(currentTimestamp.getTime()-(durationInSeconds*1000));
	// /*Break the activity event across days*/
	// ////////////////////
	// TreeMap<Timestamp,Long> mapBreakedActivitiesTimes=breakActivityEventOverDays(startTimestamp, durationInSeconds);
	//
	// int bodCount=1;
	// for (Map.Entry<Timestamp, Long> entry : mapBreakedActivitiesTimes.entrySet())
	// {
	// //System.out.println("Start date="+entry.getKey()+" Duration in seconds:"+entry.getValue());
	//
	// Timestamp stTimestamp= entry.getKey();
	// Long durationInSecs= entry.getValue();
	// TrajectoryEntry te = dataForCurrentUser.get(i);
	// te.setDurationInSeconds(durationInSecs);
	// te.setBreakOverDaysCount(bodCount);
	// mapContinuousMerged.put(entry.getKey(), te);
	// //##System.out.println("Last:Adding: "+te.toString());
	// bodCount++;
	// //mapContinuousMerged.put(entry.getKey(), currentActivityName+"||"+entry.getValue().longValue()+"||1||1");
	// }
	// //////////////////////
	// /*REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
	// currentActivityName+"||"+durationInSeconds); */
	// //durationInSeconds=0;
	// }
	//
	// }
	//
	// mapForAllDataMergedPlusDuration.put(entryForUser.getKey(),mapContinuousMerged);
	// mapForAllUnknownsWholes.put(entryForUser.getKey(),unknownsInsertedWholes);
	// mapForAllUnknownsBrokenOverDays.put(entryForUser.getKey(),unknownsInsertedBrokenOverDays);
	// }
	//
	// WritingToFile.writeLinkedHashMapOfTreemap2(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
	// "User,Timestamp,DurationInSecs");
	// WritingToFile.writeLinkedHashMapOfTreemap2(mapForAllUnknownsBrokenOverDays, "Unknown_BrokenOverDays_Inserted",
	// "User,Timestamp,DurationInSecs");
	//
	// return mapForAllDataMergedPlusDuration;
	// }

	//

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
		/*
		 * <username , <start timestamp, 'activityname||durationinseconds||numOfImagesMerged||bodCode'> bod stands for
		 * 'break over days'
		 */
		LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, String>>();
		/*
		 * Note: using TreeMap is IMPORTANThere, because TreeMap will automatically sort by the timestamp, so we do not
		 * need to be concerned about whether we add theactivities in correct order or not, if the timestamps are right,
		 * it will be stored correctly
		 */

		LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllUnknownsWholes = new LinkedHashMap<String, TreeMap<Timestamp, String>>();
		LinkedHashMap<String, TreeMap<Timestamp, String>> mapForAllUnknownsBrokenOverDays = new LinkedHashMap<String, TreeMap<Timestamp, String>>();

		System.out.println("Merging continuous activities and assigning duration");

		for (Map.Entry<String, TreeMap<Timestamp, String>> entryForUser : mapForAllData.entrySet())
		{
			String userName = entryForUser.getKey();

			int countOfContinuousMerged = 1;

			/** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
			TreeMap<Timestamp, String> unknownsInsertedWholes = new TreeMap<Timestamp, String>();

			/**
			 * Records the "Unknown"s inserted BROKEN OVER DAYS, the <start timestamp of the insertion, duration of the
			 * unknown>
			 */
			TreeMap<Timestamp, String> unknownsInsertedBrokenOverDays = new TreeMap<Timestamp, String>();

			System.out.println("\nUser =" + entryForUser.getKey());

			TreeMap<Timestamp, String> mapContinuousMerged = new TreeMap<Timestamp, String>();

			// Timestamp previousTimestamp= new Timestamp(0);
			// String previousActivityName = new String("");
			long durationInSeconds = 0;
			Timestamp startTimestamp;

			ArrayList<String> dataForCurrentUser = UtilityBelt.treeMapToArrayListString(entryForUser.getValue());

			// $$System.out.println("----Unmerged Activity data for user "+userName+"-----");

			// $$traverseArrayList(dataForCurrentUser);

			// $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");

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
						countOfContinuousMerged++;
						// $$System.out.println("Continuous merged for Activity="+currentActivityName);
						continue;
					}
					else
					{
						startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));// durationInSeconds
																												// is
																												// the
																												// accumulated
																												// duration
																												// from
																												// past
																												// merging

						long diffCurrentAndNextInSec = (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;
						long secsItContinuesBeforeNext;

						if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were
																						// different activity names
						{
							secsItContinuesBeforeNext = diffCurrentAndNextInSec;
						}

						else
						{
							System.out.print("diffCurrentAndNextDifferentInSec (" + diffCurrentAndNextInSec
									+ ") > assumeContinuesBeforeNextInSecs");
							System.out.println("\n\t For user: " + userName + ", at currentTimestamp="
									+ currentTimestamp + ", currentActivityName=" + currentActivityName);

							secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;

							/* Put the new 'Unknown' entry///////////////// */
							long durationForNewUnknownActivity = diffCurrentAndNextInSec
									- assumeContinuesBeforeNextInSecs;
							Timestamp startOfNewUnknown = new Timestamp(
									currentTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));

							unknownsInsertedWholes.put(startOfNewUnknown,
									String.valueOf(durationForNewUnknownActivity));
							/*
							 * Break the Unknown activity event across days
							 */// /////////////////
							TreeMap<Timestamp, Long> mapBreakedUnknownActivitiesTimes = breakActivityEventOverDays(
									startOfNewUnknown, durationForNewUnknownActivity);

							int bodCount = 1; // bod is breaking over days.....bod1, bod2,bod3
							for (Map.Entry<Timestamp, Long> entry : mapBreakedUnknownActivitiesTimes.entrySet())
							{
								// System.out.println("Start date="+entry.getKey()+" Duration in
								// seconds:"+entry.getValue());
								mapContinuousMerged.put(entry.getKey(),
										"Unknown" + "||" + entry.getValue().longValue() + "||1||bod" + bodCount);

								unknownsInsertedBrokenOverDays.put(entry.getKey(),
										String.valueOf(entry.getValue()) + "||bod" + bodCount);
								bodCount++;
							}

							// ////////////////////

							// mapContinuousMerged.put(startOfNewUnknown, "Unknown"+"||"+durationForNewUnknownActivity);
							// $$System.out.println("Added: Unknown, starttimestamp:"+startOfNewUnknown+"
							// duration:"+durationForNewUnknownActivity);
							/* */// //////////
						}

						durationInSeconds = durationInSeconds + secsItContinuesBeforeNext;

						/*
						 * Break the activity event across days
						 */// //////////////////
						TreeMap<Timestamp, Long> mapBreakedActivitiesTimes = breakActivityEventOverDays(startTimestamp,
								durationInSeconds);
						int bodCount = 1; // bod is breaking over days.....bod1, bod2,bod3
						for (Map.Entry<Timestamp, Long> entry : mapBreakedActivitiesTimes.entrySet())
						{
							// System.out.println("Start date="+entry.getKey()+" Duration in
							// seconds:"+entry.getValue());
							mapContinuousMerged.put(entry.getKey(),
									currentActivityName + "||" + entry.getValue().longValue() + "||"
											+ countOfContinuousMerged + "||bod" + bodCount);
							bodCount++;
						}
						// ////////////////////

						/*
						 * REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
						 * currentActivityName+"||"+durationInSeconds);
						 */

						durationInSeconds = 0;
					}

				}
				else
				// is the last element
				{
					startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));
					/* Break the activity event across days */
					// //////////////////
					TreeMap<Timestamp, Long> mapBreakedActivitiesTimes = breakActivityEventOverDays(startTimestamp,
							durationInSeconds);

					for (Map.Entry<Timestamp, Long> entry : mapBreakedActivitiesTimes.entrySet())
					{
						// System.out.println("Start date="+entry.getKey()+" Duration in seconds:"+entry.getValue());
						mapContinuousMerged.put(entry.getKey(),
								currentActivityName + "||" + entry.getValue().longValue() + "||1||1");

					}
					// ////////////////////

					/*
					 * REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
					 * currentActivityName+"||"+durationInSeconds);
					 */
					// durationInSeconds=0;
				}

			}

			mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);
			mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);
			mapForAllUnknownsBrokenOverDays.put(entryForUser.getKey(), unknownsInsertedBrokenOverDays);
		}

		WToFile.writeLinkedHashMapOfTreemap(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
				"User,Timestamp,DurationInSecs");
		WToFile.writeLinkedHashMapOfTreemap(mapForAllUnknownsBrokenOverDays, "Unknown_BrokenOverDays_Inserted",
				"User,Timestamp,DurationInSecs");

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

	// //////////////////////////////////
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

			ArrayList<String> dataForCurrentUser = UtilityBelt.treeMapToArrayListString(entryForUser.getValue());
			ArrayList<String> cleanedMergedDataForCurrentUser = new ArrayList<String>();

			for (int i = 0; i < dataForCurrentUser.size(); i++)
			{
				// $$System.out.println(dataForCurrentUser.get(i));
				String[] splittedDataEntryCurr = dataForCurrentUser.get(i).split(Pattern.quote("||"));
				String currentActivityName = splittedDataEntryCurr[1];
				// $$System.out.println(splittedDataEntryCurr[2]);
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
						// $$System.out.println("Case next is not "+activityNameToMerge+":
						// "+(splittedDataEntryCurr[0]+"||"+currentActivityName+"||"+durationToWrite));
						durationToWrite = 0l;
						break;
					}

					if (currentActivityName.equalsIgnoreCase("Unknown"))
					{
						cleanedMergedDataForCurrentUser
								.add(splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + durationToWrite);
						// $$System.out.println("Current is unknown:
						// "+(splittedDataEntryCurr[0]+"||"+currentActivityName+"||"+durationToWrite));

						durationToWrite = 0l;
						break;
					}

					String[] splittedDataEntryNextNext = dataForCurrentUser.get(i + 2).split(Pattern.quote("||"));
					String nextNextActivityName = splittedDataEntryNextNext[1];
					Long nextNextActivityDuration = Long.valueOf(splittedDataEntryNextNext[2]);

					if (nextActivityName.equalsIgnoreCase(activityNameToMerge) &&
					/* ##(nextActivityDuration < thresholdForMergingNotAvailables) && */
							nextNextActivityName.equalsIgnoreCase(currentActivityName)) // sandwich found
					{
						durationToWrite = durationToWrite + nextActivityDuration + nextNextActivityDuration;
						i += 2;
						// $$System.out.println("Found sandwich: moving 2 steps ahead");
						numberOfSandwichesForThisUser++;
						continue;
					}

					else
					{
						cleanedMergedDataForCurrentUser
								.add(splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + durationToWrite);

						// $$System.out.println("Case: final else to write
						// "+(splittedDataEntryCurr[0]+"||"+currentActivityName+"||"+durationToWrite));

						durationToWrite = 0l;
						break;
					}

				}

				if (i > dataForCurrentUser.size() - 3)
				{

					cleanedMergedDataForCurrentUser.add(dataForCurrentUser.get(i));

					// $$System.out.println("Case: last events:
					// "+(splittedDataEntryCurr[0]+"||"+currentActivityName+"||"+durationToWrite));

					// i++;
					durationToWrite = 0l;
				}

			}

			numberOfSandwichesFound.put(userName, numberOfSandwichesForThisUser);
			WToFile.writeSimpleLinkedHashMapToFile(numberOfSandwichesFound, commonPath + "sandwichesPerUser_"
					+ activityNameToMerge + thresholdForMergingNotAvailables + "secs.csv", "User",
					"number_of_sandwiches");

			mapCleanedMerged.put(userName, arrayListToTreeMap(cleanedMergedDataForCurrentUser));

		}

		return mapCleanedMerged;
	}

	// //////////////////////////////////

	// ///////////////////
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

			ArrayList<String> dataForCurrentUser = UtilityBelt.treeMapToArrayListString(entryForUser.getValue());
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

	// //
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

			ArrayList<String> dataForCurrentUser = UtilityBelt.treeMapToArrayListString(entryForUser.getValue());
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

				else
				// when next is different or when current is 'Unknown' in which case we do not merge next
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

	// ////
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

	// ////

	// //////////////////
	/**
	 * Takes in the mapForAllData comprising of TrajectoryEntries, adds 'time difference with next in seconds' to all
	 * the TrajectoryEntries in it and returns the enriched map. And writes time difference between consecutive
	 * trajectory entries to a file names '..TimeDifferenceAll.csv' with columns UserID,TimeDifferenceWithNextInSeconds.
	 * 
	 * @param mapForAllData
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> getTrajectoryEntriesWithTimeDifferenceWithNext(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllData)
	{
		// <username , <start timestamp, trajectory entry>
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataNotMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();

		ArrayList<Pair<String, Long>> timeDifferencesBetweenDataPointAllUsers = new ArrayList<Pair<String, Long>>();

		System.out.println("inside getTrajectoryEntriesWithTimeDifferenceWithNext");
		for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : mapForAllData.entrySet())
		{
			String userID = entryForUser.getKey();
			// System.out.println("\nUser ="+entryForUser.getKey());

			TreeMap<Timestamp, TrajectoryEntry> mapContinuousNotMerged = new TreeMap<Timestamp, TrajectoryEntry>();

			long diffWithNextInSeconds = 0;

			ArrayList<TrajectoryEntry> dataForCurrentUser = UtilityBelt.treeMapToArrayListGeo(entryForUser.getValue());

			for (int i = 0; i < dataForCurrentUser.size(); i++)
			{
				TrajectoryEntry te = dataForCurrentUser.get(i);

				Timestamp currentTimestamp = te.getTimestamp();
				String currentActivityName = te.getMode(); // probably this line is not needed TODO check

				if (i < dataForCurrentUser.size() - 1) // is not the last element of arraylist
				{
					Timestamp nextTimestamp = (dataForCurrentUser.get(i + 1)).getTimestamp();
					diffWithNextInSeconds = (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;

					te.setDifferenceWithNextInSeconds(diffWithNextInSeconds);

					mapContinuousNotMerged.put(currentTimestamp, te);
					timeDifferencesBetweenDataPointAllUsers.add(new Pair(userID, diffWithNextInSeconds));
				}
				else
				{
					te.setDifferenceWithNextInSeconds(0);
					timeDifferencesBetweenDataPointAllUsers.add(new Pair(userID, diffWithNextInSeconds));
					mapContinuousNotMerged.put(currentTimestamp, te);
				}

			}
			mapForAllDataNotMergedPlusDuration.put(entryForUser.getKey(), mapContinuousNotMerged);
			System.out.println("put, User:" + userID + ", #TrajectoryEntries:" + mapContinuousNotMerged.size());
		}

		WToFile.writeArrayList(timeDifferencesBetweenDataPointAllUsers, "TimeDifferenceAll",
				"UserID,TimeDifferenceWithNextInSeconds");
		System.out.println("exiting getTrajectoryEntriesWithTimeDifferenceWithNext");

		return mapForAllDataNotMergedPlusDuration;
	}

	// /
	public static LinkedHashMap<String, TreeMap<Timestamp, String>> getActivitiesWithTimeDifferenceWithNext(
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

			ArrayList<String> dataForCurrentUser = UtilityBelt.treeMapToArrayListString(entryForUser.getValue());

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

	// /

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

	/**
	 * Reads lables entries and returns all label entries as a map.
	 * 
	 * @return LinkedHashMap<user id as String , ArrayList<LabelEntry>>
	 */
	public static LinkedHashMap<String, ArrayList<LabelEntry>> createLabelEntryMap()
	{
		BufferedReader br1, br2 = null;
		LinkedHashMap<String, ArrayList<LabelEntry>> mapForLabelEntry = new LinkedHashMap<String, ArrayList<LabelEntry>>();
		System.out.println(userIDs);
		String pathToParse = rawPathToRead;// commonPath + "Raw/Geolife Trajectories 1.3/Data/";

		modeNames = new ArrayList<String>();

		for (String userID : userIDs)
		{
			// System.out.println("creating label entry map for user="+userID);
			ArrayList<LabelEntry> labelEntriesForUser = new ArrayList<LabelEntry>();
			try
			{
				File file = new File(pathToParse + userID + "/labels.txt");

				br1 = new BufferedReader(new FileReader(file));

				String labelEntryLine;

				int count = -1;
				while ((labelEntryLine = br1.readLine()) != null)
				{
					count++;
					if (count == 0) // skip the first line
						continue;
					// System.out.println(labelEntryLine);
					String entryArr[] = labelEntryLine.split(Pattern.quote("\t"));
					String entryArr0[] = entryArr[0].split(" ");
					String entryArr1[] = entryArr[1].split(" ");
					// System.out.println(entryArr.length);
					// System.out.println(entryArr[1]);
					Timestamp startTimestamp = getTimestampGeoData(entryArr0[0], entryArr0[1]);
					Timestamp endTimestamp = getTimestampGeoData(entryArr1[0], entryArr1[1]);
					String mode = entryArr[2];

					LabelEntry le = new LabelEntry(startTimestamp, endTimestamp, mode);
					labelEntriesForUser.add(le);

					if (!modeNames.contains(mode))
					{
						modeNames.add(mode);
					}
					// System.out.println(">>"+startTimestamp+" "+endTimestamp+">>"+mode);
				}
				// System.out.println(labelEntriesForUser.size() + " labelentried added for user " + userID);
				System.out.println("User: " + userID + ", #LabelEntriesAdded:" + labelEntriesForUser.size());
				mapForLabelEntry.put(userID, labelEntriesForUser);
				// if(br1 != null)
				// {
				br1.close();
				// }
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("Size of mapForLabelEntry:" + mapForLabelEntry.size()
				+ " (should be same as num of users having labels.txt");
		System.out.println("Number of mode names:" + modeNames.size());
		System.out.println(modeNames);
		return mapForLabelEntry;
	}

	/**
	 * Get the transportation mode corresponding to the given timestamp for the given user CHECKED 100% correctly
	 * working
	 * 
	 * @param timestamp
	 * @return
	 */
	public static String getModeForTrajectoryEntry(String userID, Timestamp timestamp)
	{
		String mode = "null";

		ArrayList<LabelEntry> labelEntries = mapForLabelEntries.get(userID);

		for (LabelEntry labelEntry : labelEntries)
		{
			if (labelEntry.contains(timestamp) == true)
			{
				// System.out.println(labelEntry.toString()+ "contains "+timestamp);
				return labelEntry.getMode();
			}
		}
		return "Not Available";
	}

	/**
	 * Reads the Trajectory entries from the raw data and returns it as a map. And writes the modes per trajectory files
	 * in "ModesPerTrajectoryFiles.csv" with columns. Also write the number of negative, zero and invalid latitude,
	 * longitude and altitude for trjaectory entry of users. "UserID,TrajectoryFile, NumberOfModes,Modes"
	 * 
	 * @return LinkedHashMap<user id as String, TreeMap<Timestamp, TrajectoryEntry>>
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> createAnnotatedTrajectoryMap()
	{
		String pathToParse = rawPathToRead;// commonPath + "Raw/Geolife Trajectories 1.3/Data/";

		BufferedReader br1, br2 = null;

		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllData = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();

		// userID, trajectory file, (number of modes for that trajectory file , set of these modes)
		LinkedHashMap<String, TreeMap<String, String>> modesForAllTrajectoryFilesAllUsers = new LinkedHashMap<String, TreeMap<String, String>>();// the
																																					// prime
																																					// purpose
																																					// for
																																					// this
																																					// is
																																					// to
																																					// check
																																					// whether
																																					// all

		System.out.println("UserIDs: " + userIDs);

		try
		{
			WToFile.writeNegativeZeroInvalidsLatLonAltHeader("CountOfNegativeZeroUnknownAltLatLon");

			for (String userID : userIDs)
			{
				TreeMap<Timestamp, TrajectoryEntry> mapEachUser = new TreeMap<Timestamp, TrajectoryEntry>();
				// System.out.println("creating annotated trajectory entry for user="+userID);
				TreeMap<String, String> modesForAllTrajectoryFilesPerUser = new TreeMap<String, String>();
				TrajectoryEntry.clearCountNegativesZerosInvalids(); // clear the counts, we will count for each specific
																	// user.

				String folderToLook = pathToParse + userID + "/Trajectory";
				// check if labels.txt is there in the folder
				File file = new File(folderToLook);

				// reading trajectory entries from .PLT files
				for (File fileEntry : file.listFiles()) // /for each trajectory file. Here each .plt file is one
														// trajectory
				{
					// System.out.println(fileEntry);

					String trajectoryID = userID + "-"
							+ fileEntry.getName().substring(0, fileEntry.getName().length() - 4); // usedID__filename(without.plt)

					br1 = new BufferedReader(new FileReader(fileEntry));

					String trajectoryEntryLine;

					int count = -1;
					HashSet<String> modesForThisTrajectoryFile = new HashSet<String>(); // the prime purpose for this is
																						// to check whether all entries
																						// in a trajectory file have the
																						// same mode.

					while ((trajectoryEntryLine = br1.readLine()) != null)
					{
						count++;
						if (count <= 5) // skip the first 6 lines
							continue;
						// System.out.println(trajectoryEntryLine);
						String entryArr[] = trajectoryEntryLine.split(",");

						String latitude = new String(entryArr[0]);// Double.parseDouble(entryArr[0]);
						String longitude = new String(entryArr[1]);// Double.parseDouble(entryArr[1]),
						String altitude = new String(entryArr[3]);// Double.parseDouble(entryArr[3]);

						Timestamp timeStamp = getTimestampGeoData(entryArr[5], entryArr[6]);
						String mode = getModeForTrajectoryEntry(userID, timeStamp);
						modesForThisTrajectoryFile.add(mode);

						TrajectoryEntry te = new TrajectoryEntry(latitude, longitude, altitude, timeStamp, mode,
								trajectoryID);
						mapEachUser.put(timeStamp, te);
						// System.out.println(">>"+latitude+" "+longitude+">>"+timeStamp+">>"+mode+"\n");
					}
					// if (br1 != null)
					// {
					br1.close();
					// }
					modesForAllTrajectoryFilesPerUser.put(fileEntry.getName(),
							modesForThisTrajectoryFile.size() + "," + modesForThisTrajectoryFile.toString());
				} // end of loop over trajectory files.

				modesForAllTrajectoryFilesAllUsers.put(userID, modesForAllTrajectoryFilesPerUser);

				WToFile.writeNegativeZeroInvalidsLatLonAlt(userID, "CountOfNegativeZeroUnknownAltLatLon");

				mapForAllData.put(userID, mapEachUser);
				// System.out.println("putting maps of user:" + userID + " of size:" + mapEachUser.size());
				System.out.println("put, User:" + userID + ",#TrajectoryEntries:" + mapEachUser.size());
			} // end of loop over users

			WToFile.writeLinkedHashMapOfTreemapAllString(modesForAllTrajectoryFilesAllUsers,
					"ModesPerTrajectoryFiles", "UserID,TrajectoryFile, NumberOfModes,Modes");
			WToFile.writeNegativeZeroInvalidsLatLonAltFooter("CountOfNegativeZeroUnknownAltLatLon");
			// WritingToFile.writeLinkedHashMapOfTreemapPureTrajectoryEntries(mapForAllData,
			// "AllDataWithAnnotation","UserID,Timestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount");

			System.out.println("\nSize of mapForAllData:" + mapForAllData.size());
		} // end of try
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return mapForAllData;
	}

	/*
	 * public static LinkedHashMap<String, TreeMap<Timestamp,String>> createAnnotatedImageFile() { BufferedReader br =
	 * null; BufferedWriter bw =null;
	 * 
	 * //<username , <timstamp, 'imagename||activityname'> LinkedHashMap<String, TreeMap<Timestamp,String>>
	 * mapForAllData= new LinkedHashMap<String, TreeMap<Timestamp,String>> ();
	 * 
	 * for(String userName: userNames) {
	 * 
	 * TreeMap<Timestamp,String> mapEachPhoto= new TreeMap<Timestamp,String>();
	 * System.out.println("creating annotated files for user="+userName); try {
	 * 
	 * File file = new File(commonPath+"AllTogether7July/"+userName+"_AnnotatedJPGFiles.txt");
	 * System.out.println(file.getAbsoluteFile()); file.delete(); file.createNewFile(); FileWriter fw = new
	 * FileWriter(file.getAbsoluteFile()); bw = new BufferedWriter(fw);
	 * 
	 * br = new BufferedReader(new FileReader(commonPath+"AllTogether7July/"+userName+"_JPGFiles.txt"));
	 * 
	 * String sCurrentImageName; while ((sCurrentImageName = br.readLine()) != null) {
	 * //System.out.println(sCurrentImageName);
	 * 
	 * String activityName= getActivityName(userName,sCurrentImageName);
	 * 
	 * bw.write(sCurrentImageName+"||"+activityName+"\n");
	 * //System.out.println(getTimestamp(sCurrentImageName)+"--"+sCurrentImageName+"||"+activityName);
	 * //System.out.println("."); mapEachPhoto.put(getTimestamp(sCurrentImageName),sCurrentImageName+"||"+activityName);
	 * } mapForAllData.put(userName,mapEachPhoto); }
	 * 
	 * 
	 * catch (IOException e) { e.printStackTrace(); } finally { try { if (br != null) { br.close(); } if (bw != null) {
	 * bw.close(); } } catch (IOException ex) { ex.printStackTrace(); } } }
	 * 
	 * System.out.println(" Size of mapForAllData:"+mapForAllData.size());
	 * 
	 * return mapForAllData; }
	 */
	public static void traverseMapForAllData(LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllData)
	{
		for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entry : mapForAllData.entrySet())
		{
			System.out.println("\nUser =" + entry.getKey());

			for (Map.Entry<Timestamp, TrajectoryEntry> entryMapEachPhoto : entry.getValue().entrySet())
			{
				System.out.print(entryMapEachPhoto.getKey());
				System.out.print("   " + entryMapEachPhoto.getValue().toString());
				System.out.print("\n");
			}
		}
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

	public static TreeMap<Timestamp, TrajectoryEntry> arrayListToTreeMap2(ArrayList<TrajectoryEntry> arrayListToConvert)
	{
		TreeMap<Timestamp, TrajectoryEntry> treeMap = new TreeMap<Timestamp, TrajectoryEntry>();

		for (TrajectoryEntry te : arrayListToConvert)
		{
			treeMap.put(te.getTimestamp(), te);
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
	// <Start timestamp, duration in seconds>
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
				// when creating objects in database, we take the end time as
				// (duration -1seconds)

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

	/*
	 * public static String getActivityName(String userName, String imageName) { String activityName= "Not Available";
	 * 
	 * for(int iterator=0;iterator<activityNames.length;iterator++) {
	 * if(containsText(commonPath+"AllTogether7July/"+userName+"_"+activityNames[iterator]+".txt", imageName)) {
	 * //System.out.println("## found ##"); activityName=activityNames[iterator]; return activityName; } } return
	 * activityName; }
	 */

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
	public static void createDataset()
	{
		String datasetAddressStefan = commonPath + "Data_Set_Stefan/Data Set";
		listFilesForFolder(new File(datasetAddressStefan), datasetAddressStefan, "Stefan");

		String datasetAddressTengqi = commonPath + "Data_Set_TengQi/tengqi";
		listFilesForFolder(new File(datasetAddressTengqi), datasetAddressTengqi, "Tengqi");

		String datasetAddressCathal = commonPath + "Data_Set_Cathal/two weeks of data/two weeks of data";
		listFilesForFolder(new File(datasetAddressCathal), datasetAddressCathal, "Cathal");

		String datasetAddressZaher = commonPath + "Data_Set_Zaher";
		listFilesForFolder(new File(datasetAddressZaher), datasetAddressZaher, "Zaher");

		String datasetAddressRami = commonPath + "Data_Set_Rami";
		listFilesForFolder(new File(datasetAddressRami), datasetAddressRami, "Rami");

		System.out.println("list files for folder completed");

		// countFilesAndWriteStats();
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
	public static void listFilesForFolder(final File folder, String path, String userName)
	{
		// int count=0;
		String categories[] = { "badImages", "Commuting", "Computer", "Eating", "Exercising", "Housework",
				"On the Phone", "Preparing Food", "Shopping", "Socialising", "Watching TV" };
		int countOfJPG = 0;// , countOfCategoryAssignments=0;;
		int countOfActivityFilesFound = 0;
		int countOfJPGFilesMentionedInAllActivityFiles = 0;

		path = commonPath + "AllTogether7July/";

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
					listFilesForFolder(fileEntry, path, userName);
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

	/*
	 * public static void countFilesAndWriteStats() { try { LinkedHashMap<String,Integer> countPerActivityName= new
	 * LinkedHashMap<String,Integer> (); for(String activityName: activityNames) {
	 * countPerActivityName.put(activityName, new Integer(0)); }
	 * 
	 * 
	 * for(String userName: userNames) { writeInStats("\n For user: "+userName); int numberOfEntriesInJPGFiles=
	 * countLinesNotEmptyStringlines(commonPath+"AllTogether7July/"+userName+"_JPGFiles.txt");
	 * writeInStats("\n Number of JPG Files="+ numberOfEntriesInJPGFiles);
	 * 
	 * int totalNumberEntriesOverAllActivities=0; for(String activityName: activityNames) { int numberOfEntriesNonEmpty
	 * = countLinesNotEmptyStringlines(commonPath+"AllTogether7July/"+userName+"_"+activityName+".txt");
	 * writeInStats("\n Number of jpg files in "+ userName+"_"+activityName+".txt = "+ numberOfEntriesNonEmpty);
	 * 
	 * countPerActivityName.put(activityName, countPerActivityName.get(activityName) + numberOfEntriesNonEmpty);
	 * 
	 * totalNumberEntriesOverAllActivities += numberOfEntriesNonEmpty; }
	 * writeInStats("\n Total number jpg files over all Activities ="+ totalNumberEntriesOverAllActivities);
	 * writeInStats("\n Difference between total num of images and total num of annotated images: "
	 * +numberOfEntriesInJPGFiles+" - "+totalNumberEntriesOverAllActivities+" = "+
	 * (numberOfEntriesInJPGFiles-totalNumberEntriesOverAllActivities)+"\n"); }
	 * 
	 * writeInStats("\n------------\n");writeInStats("\nTotal:\n"); for (Map.Entry<String, Integer> entry :
	 * countPerActivityName.entrySet()) {
	 * 
	 * writeInStats("\n Num of images annotated with Activity Name: "+entry.getKey()+"  = "+entry.getValue());
	 * 
	 * } writeInStats("\n------------\n");
	 * 
	 * } catch(Exception e) { e.printStackTrace(); }
	 * 
	 * }
	 */

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
			timeStamp = new Timestamp(year - 1900, month - 1, day, hours, minutes, seconds, 0); // / CHECK it out
			// System.out.println("Time stamp"+timeStamp);
		}
		catch (Exception e)
		{
			System.out.println("Exception " + e + " thrown for getting timestamo from " + imageName);
			e.printStackTrace();
		}
		return timeStamp;
	}

	// 2007-08-04,03:30:32
	// 0123456789 012345678
	public static Timestamp getTimestampGeoData(String dateString, String timeString)
	{
		Timestamp timeStamp = null;
		int year = 0, month = 0, day = 0, hours = 0, minutes = 0, seconds = 0;

		try
		{
			year = Integer.parseInt(dateString.substring(0, 4));
			month = Integer.parseInt(dateString.substring(5, 7));
			day = Integer.parseInt(dateString.substring(8, 10));

			hours = Integer.parseInt(timeString.substring(0, 2));
			minutes = Integer.parseInt(timeString.substring(3, 5));
			seconds = Integer.parseInt(timeString.substring(6, 8));

			// System.out.println(year+ " "+month+" "+day+" "+hours+" "+minutes+" "+seconds);
			timeStamp = new Timestamp(year - 1900, month - 1, day, hours, minutes, seconds, 0); // / CHECK it out
			// System.out.println("Time stamp"+timeStamp);
		}
		catch (Exception e)
		{
			System.out
					.println("Exception " + e + " thrown for getting timestamp from " + dateString + " " + timeString);
			e.printStackTrace();
		}
		return timeStamp;
	}

	public static void writeDataToFile2(LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> data,
			String filenameEndPhrase)
	{
		for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : data.entrySet())
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

				for (Map.Entry<Timestamp, TrajectoryEntry> entry : entryForUser.getValue().entrySet())
				{
					// $$System.out.println(entry.getKey()+","+entry.getValue());
					bw.write(entry.getValue().toStringWithTrajID() + "\n");
				}

				bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void writeDataToFileWithTrajPurityCheck(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> data, String filenameEndPhrase)
	{
		for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : data.entrySet())
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

				for (Map.Entry<Timestamp, TrajectoryEntry> entry : entryForUser.getValue().entrySet())
				{
					// $$System.out.println(entry.getKey()+","+entry.getValue());
					String msg = entry.getValue().toStringWithTrajIDWithTrajPurityCheck();
					bw.write(msg + "\n");

					if (entry.getValue().getNumberOfDistinctTrajectoryIDs() > 1)
					{
						WToFile.appendLineToFileAbs("User:" + userName + "," + msg + "\n",
								commonPath + "MergedTrajEntriesWithMoreThanOneTrajIDs.csv");
					}
					// return "t:" + timestamp + ",mod:" + mode + " ,endt:" + endTimestamp + ", timeDiffWithNextInSecs:"
					/*
					 * + this.differenceWithNextInSeconds + ",  durationInSeconds:" + this.durationInSeconds +
					 * ",  bodCount:" + this.breakOverDaysCount + ", lat:" + lat.toString() + ", lon:" + lon.toString()
					 * + ", alt:" + alt.toString() + ",#distinctTid:" + getNumberOfDistinctTrjactoryIDs() + ",tid:" +
					 * trajectoryID.toString().replaceAll(",", "__");
					 */
				}

				bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Writes all the Trajectory Entries for all users to a file.
	 * 
	 * @param data
	 * @param filenameEndPhrase
	 * @param headers
	 * @param printHeaders
	 */
	public static void writeDataToFile2WithHeadersWithTrajPurityCheck(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> data, String filenameEndPhrase, String headers,
			boolean printHeaders)
	{
		for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : data.entrySet())
		{
			try
			{
				String userName = entryForUser.getKey();

				// System.out.println("\nUser =" + entryForUser.getKey());
				String fileName = Constant.getCommonPath() + userName + filenameEndPhrase + ".csv";

				File file = new File(fileName);

				file.delete();
				if (!file.exists())
				{
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);

				TreeMap<Timestamp, String> mapForEachUser = new TreeMap<Timestamp, String>();

				if (printHeaders)
				{
					bw.write(headers + "\n");
				}

				for (Map.Entry<Timestamp, TrajectoryEntry> entry : entryForUser.getValue().entrySet())
				{
					// $$System.out.println(entry.getKey()+","+entry.getValue());
					bw.write(entry.getValue().toStringWithoutHeadersWithTrajIDPurityCheck() + "\n");

					if (entry.getValue().getNumberOfDistinctTrajectoryIDs() > 1)
					{
						WToFile
								.appendLineToFileAbs(
										"User:" + userName + ","
												+ entry.getValue().toStringWithTrajIDWithTrajPurityCheck() + "\n",
										commonPath + "MergedTrajEntriesWithMoreThanOneTrajIDs.csv");
					}
					// "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
				}

				bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		} // "trajID,timestamp,
			// endt,mode,latitude,longitude,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount",

	}

	/**
	 * Writes all the Trajectory Entries for all users to a file.
	 * 
	 * @param data
	 * @param filenameEndPhrase
	 * @param headers
	 * @param printHeaders
	 */
	public static void writeDataToFile2WithHeaders(LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> data,
			String filenameEndPhrase, String headers, boolean printHeaders)
	{
		for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : data.entrySet())
		{
			try
			{
				String userName = entryForUser.getKey();

				// System.out.println("\nUser =" + entryForUser.getKey());
				String fileName = Constant.getCommonPath() + userName + filenameEndPhrase + ".csv";

				File file = new File(fileName);

				file.delete();
				if (!file.exists())
				{
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);

				TreeMap<Timestamp, String> mapForEachUser = new TreeMap<Timestamp, String>();

				if (printHeaders)
				{
					bw.write(headers + "\n");
				}

				for (Map.Entry<Timestamp, TrajectoryEntry> entry : entryForUser.getValue().entrySet())
				{
					// $$System.out.println(entry.getKey()+","+entry.getValue());
					bw.write(entry.getValue().toStringWithoutHeadersWithTrajID() + "\n");
					// "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
				}

				bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		} // "trajID,timestamp,
			// endt,mode,latitude,longitude,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount",

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
					// $$System.out.println(entry.getKey()+","+entry.getValue());
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

	/**
	 * Merges continuous activities with same activity names and start timestamp difference of less than
	 * 'continuityThresholdInSeconds'. without break over days and same trajectory ID
	 * 
	 * Duration assigned is difference between the start-timestamp of this activity and start-timestamp of the next
	 * (different) activity. difference between the start-timestamp of this activity and start-timestamp of the next
	 * (different) activity BUT ONLY IF this difference is less than P2 minutes, otherwise the duration is P2 minutes.
	 * 
	 * Adds 'Unknown' and writes the unknown inserted to a file "Unknown_Wholes_Inserted.csv" with columns
	 * "User,Timestamp,DurationInSecs"
	 * 
	 * Nuances of merging consecutive activities and calculation the duration of activities.
	 * 
	 * 
	 * @param mapForAllData
	 *            is LinkedHashMap of the form <username, <timestamp,TrajectoryEntry>>
	 * @return <UserName, <Timestamp,TrajectoryEntry>>
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mergeContinuousTrajectoriesAssignDurationWithoutBOD2TrajSensitive(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllData)
	{
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();
		/*
		 * Note: using TreeMap is IMPORTANT here, because TreeMap will automatically sort by the timestamp, so we do not
		 * need to be concerned about whether we add theactivities in correct order or not, if the timestamps are right,
		 * it will be stored correctly
		 */
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();

		System.out.println("Merging continuous trajectories and assigning duration without BOD");
		try
		{
			for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : mapForAllData.entrySet())
			{
				String userID = entryForUser.getKey();
				BufferedWriter bwMergerCaseLogs = WToFile
						.getBWForNewFile(commonPath + userID + "MergerCasesLog.csv");
				bwMergerCaseLogs.write("Case,Mode,DurationInSecs,CurrentTS, NextTS,Comment\n");

				System.out.println("\nUser =" + userID);

				int numOfTrajCaseA = 0, numOfTrajCaseB = 0, numOfTrajCaseC = 0, numOfLastTrajEntries = 0;

				int countOfContinuousMerged = 1;

				/** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
				TreeMap<Timestamp, TrajectoryEntry> unknownsInsertedWholes = new TreeMap<Timestamp, TrajectoryEntry>();
				TreeMap<Timestamp, TrajectoryEntry> mapContinuousMerged = new TreeMap<Timestamp, TrajectoryEntry>();

				long durationInSeconds = 0;
				ArrayList<String> newLati = new ArrayList<String>(), newLongi = new ArrayList<String>(),
						newAlti = new ArrayList<String>();
				ArrayList<String> newTrajID = new ArrayList<String>();

				long timeDiffWithNextInSeconds = 0; // do not delete. // not directly relevant
				Timestamp startTimestamp;

				ArrayList<TrajectoryEntry> trajEntriesForCurrentUser = UtilityBelt
						.treeMapToArrayListGeo(entryForUser.getValue());

				// $$System.out.println("----Unmerged Activity data for user "+userName+"-----");
				// $$traverseArrayList(dataForCurrentUser);
				// $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
				for (int i = 0; i < trajEntriesForCurrentUser.size(); i++)
				{
					// startTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));
					// ##
					// $$System.out.println("\nReading: "+dataForCurrentUser.get(i).toString());
					Timestamp currentTimestamp = trajEntriesForCurrentUser.get(i).getTimestamp();
					String currentModeName = trajEntriesForCurrentUser.get(i).getMode();

					ArrayList<String> currentLat = trajEntriesForCurrentUser.get(i).getLatitude();
					ArrayList<String> currentLon = trajEntriesForCurrentUser.get(i).getLongitude();
					ArrayList<String> currentAlt = trajEntriesForCurrentUser.get(i).getAltitude();
					ArrayList<String> currentTrajID = trajEntriesForCurrentUser.get(i).getTrajectoryID();

					newLati.addAll(currentLat);
					newLongi.addAll(currentLon);
					newAlti.addAll(currentAlt);
					newTrajID.addAll(currentTrajID);
					// startTimestamp=currentTimestamp;

					if (i < trajEntriesForCurrentUser.size() - 1) // is not the last element of arraylist
					{
						// check if the next element should be merged with this one if they are continuous and have same
						// activity name
						Timestamp nextTimestamp = trajEntriesForCurrentUser.get(i + 1).getTimestamp();
						String nextModeName = trajEntriesForCurrentUser.get(i + 1).getMode();
						ArrayList<String> nextTrajectoryIDs = trajEntriesForCurrentUser.get(i + 1).getTrajectoryID();
						// ArrayList<Double> nextLat = dataForCurrentUser.get(i+1).getLatitude();
						// ArrayList<Double> nextLon = dataForCurrentUser.get(i+1).getLongitude();
						// ArrayList<Double> nextAlt = dataForCurrentUser.get(i+1).getAltitude();

						if (nextModeName.equals(currentModeName) && areContinuous(currentTimestamp, nextTimestamp)
								&& currentTrajID.equals(nextTrajectoryIDs))
						{
							numOfTrajCaseA += 1;
							durationInSeconds += (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;

							timeDiffWithNextInSeconds = trajEntriesForCurrentUser.get(i)
									.getDifferenceWithNextInSeconds()
									+ trajEntriesForCurrentUser.get(i + 1).getDifferenceWithNextInSeconds(); // TODO
																												// CHECK
																												// IF
																												// NOT
																												// NEEDED

							// newLati.addAll(currentLat);
							// newLongi.addAll(currentLon);
							// newAlti.addAll(currentAlt);

							// newLati.addAll(nextLat);
							// newLongi.addAll(nextLon);
							// newAlti.addAll(nextAlt);

							countOfContinuousMerged++;
							// ##bwMergerCaseLogs.write("CaseA: Continuous merged for mode=" + currentModeName + "
							// durationInSeconds="+ durationInSeconds + "\n");
							bwMergerCaseLogs.write("CaseA," + currentModeName + "," + durationInSeconds + ","
									+ currentTimestamp + "," + nextTimestamp + ",merged as continuous\n");
							continue;
						}

						else
						{
							startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));// durationInSeconds
																													// is
																													// the
																													// accumulated
																													// duration
																													// from
																													// past
																													// merging
							// ##System.out.println("new starttimestamp="+startTimestamp);

							long diffCurrentAndNextInSec = (nextTimestamp.getTime() - currentTimestamp.getTime())
									/ 1000;
							long secsItContinuesBeforeNext;

							if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were
																							// different activity names
							{
								numOfTrajCaseB += 1;
								secsItContinuesBeforeNext = diffCurrentAndNextInSec;
								// ##bwMergerCaseLogs.write("CaseB: diffCurrentAndNextInSec <=
								// assumeContinuesBeforeNextInSecs, secsItContinuesBeforeNext=" +
								// secsItContinuesBeforeNext + "\n");
								bwMergerCaseLogs.write("CaseB," + currentModeName + "," + durationInSeconds + ","
										+ currentTimestamp + "," + nextTimestamp + "," + diffCurrentAndNextInSec + "<="
										+ assumeContinuesBeforeNextInSecs + " secsItContinuesBeforeNext ="
										+ secsItContinuesBeforeNext + "\n");
							}

							else
							{
								// System.out.println("\n\t For user: "+userID+", at
								// currentTimestamp="+currentTimestamp+", currentModeName="+currentModeName);
								numOfTrajCaseC += 1;
								secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;

								// ##bwMergerCaseLogs.write("CaseC: diffCurrentAndNextDifferentInSec (" +
								// diffCurrentAndNextInSec + ") >"+ assumeContinuesBeforeNextInSecs + "\n");
								bwMergerCaseLogs.write("CaseC," + currentModeName + "," + durationInSeconds + ","
										+ currentTimestamp + "," + nextTimestamp + "," + diffCurrentAndNextInSec + ">"
										+ assumeContinuesBeforeNextInSecs + " secsItContinuesBeforeNext ="
										+ secsItContinuesBeforeNext + "  put new Unknown\n");

								/* Put the new 'Unknown' entry///////////////// */
								long durationForNewUnknownActivity = diffCurrentAndNextInSec
										- assumeContinuesBeforeNextInSecs;
								Timestamp startOfNewUnknown = new Timestamp(
										currentTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));

								// unknownsInsertedWholes.put(startOfNewUnknown, new TrajectoryEntry(startOfNewUnknown,
								// durationForNewUnknownActivity,"Unknown")); //
								// String.valueOf(durationForNewUnknownActivity));

								TrajectoryEntry te = new TrajectoryEntry(startOfNewUnknown,
										durationForNewUnknownActivity, "Unknown");// ,bodCount);
								mapContinuousMerged.put(startOfNewUnknown, te);
								unknownsInsertedWholes.put(startOfNewUnknown, te);
								// $$System.out.println("Added Trajectory Entry: "+te.toString());
							}

							durationInSeconds = durationInSeconds + secsItContinuesBeforeNext;

							TrajectoryEntry te = trajEntriesForCurrentUser.get(i);
							te.setLatitude(newLati); // note: has to be done with set,..cant do with add becasue it will
														// create problems if more than two entries are merged
							te.setLongitude(newLongi);
							te.setAltitude(newAlti);
							te.setTrajectoryID(newTrajID);

							te.setTimestamp(startTimestamp);
							te.setDurationInSeconds(durationInSeconds);
							// te.setDifferenceWithNextInSeconds(timeDiffWithNextInSeconds);

							mapContinuousMerged.put(startTimestamp, te);
							// $$System.out.println("Added Trajectory Entry: "+te.toString());

							// durationInSeconds =0;
							// timeDiffWithNextInSeconds =0;
							// newLati.clear();newLongi.clear();newAlti.clear();
						}

					}
					else
					// is the last element
					{
						numOfLastTrajEntries += 1;

						// $$System.out.println("this is the last data point,\n duration in seconds =
						// "+durationInSeconds);

						startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));

						TrajectoryEntry te = trajEntriesForCurrentUser.get(i);
						te.setLatitude(newLati); // note: has to be done with set,..cant do with add becasue it will
													// create problems if more than two entries are merged
						te.setLongitude(newLongi);
						te.setAltitude(newAlti);
						te.setTrajectoryID(newTrajID);
						te.setTimestamp(startTimestamp);
						te.setDurationInSeconds(durationInSeconds + timeDurationForLastSingletonTrajectoryEntry);

						mapContinuousMerged.put(startTimestamp, te);

						// $$System.out.println("Added Trajectory Entry: "+te.toString());

						// newLati.clear();newLongi.clear();newAlti.clear();
						// //////////////////////
						// /*REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
						// currentActivityName+"||"+durationInSeconds); */
						// durationInSeconds=0;
					}

					// $$System.out.println("Clearing variables");//
					durationInSeconds = 0;
					timeDiffWithNextInSeconds = 0;
					newLati.clear();
					newLongi.clear();
					newAlti.clear();
					newTrajID.clear();
				} // end of for loop over trajectory entries for current user.

				mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);
				mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);

				bwMergerCaseLogs.write("User:" + userID + ",numOfTrajCaseA = " + numOfTrajCaseA + ",numOfTrajCaseB = "
						+ numOfTrajCaseB + ",numOfTrajCaseC = " + numOfTrajCaseC + " ,numOfLastTrajEntries = "
						+ numOfLastTrajEntries);
				System.out.println("User:" + userID + ",numOfTrajCaseA = " + numOfTrajCaseA + ",numOfTrajCaseB = "
						+ numOfTrajCaseB + ",numOfTrajCaseC = " + numOfTrajCaseC + " ,numOfLastTrajEntries = "
						+ numOfLastTrajEntries);
				bwMergerCaseLogs.close();
			} // end of for loop over users

			WToFile.writeLinkedHashMapOfTreemapTrajEntry(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
					"User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeContinuousTrajectoriesAssignDurationWithoutBOD2()");
		}
		return mapForAllDataMergedPlusDuration;
	}
}
