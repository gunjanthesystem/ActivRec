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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.activity.io.ReadingFromFile;
import org.activity.io.WritingToFile;
import org.activity.objects.DataEntry;
import org.activity.objects.LabelEntry;
import org.activity.objects.Pair;
import org.activity.objects.TrackListenEntry;
import org.activity.objects.TrajectoryEntry;
import org.activity.ui.PopUps;
import org.activity.util.Constant;
import org.activity.util.UtilityBelt;

/**
 * Do the last fm analysis for each user separately (as doing all together was causing GC error out of heap)
 * 
 * @author gunjan
 */
public class DatabaseCreatorLastFM2Quicker2ByUser extends DatabaseCreator
{
	// public static String commonPath="/run/media/gunjan/OS/Users/gunjan/Documents/DCU Data Works/Lifelog Working
	// dataset 3 july copy/";

	static ArrayList<String> modeNames;

	// static LinkedHashMap<String, TreeMap<Timestamp,String>> mapForAllData;
	// static LinkedHashMap<String, ArrayList<LabelEntry>> mapForLabelEntries;
	static TreeMap<Timestamp, TrackListenEntry> mapForAllData; // Note: LinkedHashMap is a decent choice for performance
	// (ref:http://www.javapractices.com/topic/TopicAction.do?Id=65)
	static TreeMap<Timestamp, TrackListenEntry> mapForAllDataTimeDifference;
	static TreeMap<Timestamp, TrackListenEntry> mapForAllDataMergedContinuousWithDuration;
	static TreeMap<Timestamp, TrackListenEntry> mapForAllDataMergedSandwichedWithDuration;

	static List<String> userIDsOriginal;
	static List<String> userIDs;
	static String dataSplitLabel;
	static long maxLengthOfObject; // in this case max length of song

	public static void setEssentialParameters()
	{
		/** Important: SET DEFAULT TIMEZONE **/
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		commonPath = "/run/media/gunjan/Space/GUNJAN/LastFMSpace/June22AllNoCaseC/";

		rawPathToRead = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/datasets/last.fm dataset/lastfm-dataset/Lastfm-dataset-1K-splitted/";
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/datasets/last.fm
		// dataset/lastfm-dataset/lastfm-dataset-1K/";
		nameForMapToBeSerialised = "mapForAllDataMergedPlusDurationLastFMApril28.map";

		continuityThresholdInSeconds = 420;// 7 mins 75th percentil of sample : 367 .. around 6 minutes //241; // Q1 of
											// top 100 songs on itune
											// ref:http://www.statcrunch.com/5.0/viewreport.php?reportid=28647&groupid=948
											// //5 * 60; // changed from 30
											// min in DCU dataset...., if two timestamps are separated by less than
											// equal to this value

		// have same mode name,
		// then they are assumed to be continuos
		assumeContinuesBeforeNextInSecs = 241;// Q1 of top 100 songs on itune7 * 60;// 227;// 2 * 60; // changed from 30
												// min in DCU dataset we assume that
		// if two activities have a start time gap of more than 'assumeContinuesBeforeNextInSecs' seconds ,
		// then the first activity continues for 'assumeContinuesBeforeNextInSecs' seconds before the next activity
		// starts.
		thresholdForMergingNotAvailables = 241;// 5 * 60;
		thresholdForMergingSandwiches = 241;// 10 * 60;

		timeDurationForLastSingletonTrajectoryEntry = 227;// 2 * 60;
	}

	// ******************END OF PARAMETERS TO SET*****************************//
	public static void main(String args[])
	{
		System.out.println("Running starts");

		setEssentialParameters();// IMPORTANT
		System.out.println("CommonPath = " + commonPath);
		try
		{
			long ct1 = System.currentTimeMillis();

			Constant.setCommonPath(commonPath);
			// commonPath = Constant.getCommonPath();

			PrintStream consoleLogStream = new PrintStream(
					new File(commonPath + "consoleLogDatabaseCreatorLastFM.txt")); // Redirecting the console output
			System.out.println("Current DateTime: " + LocalDateTime.now());
			// System.setOut(new PrintStream(new FileOutputStream('/dev/stdout')));
			System.setOut(new PrintStream(consoleLogStream));
			System.setErr(consoleLogStream);
			System.out.println("Current DateTime: " + LocalDateTime.now());
			// ConnectDatabaseV1.getTimestamp("B00000028_21I5H1_20140216_170559E.JPG,");
			System.out.println("Default timezone = " + TimeZone.getDefault());
			System.out.println("\ncontinuityThresholdInSeconds=" + continuityThresholdInSeconds
					+ "\nassumeContinuesBeforeNextInSecs" + assumeContinuesBeforeNextInSecs
					+ "\thresholdForMergingSandwiches = " + thresholdForMergingSandwiches
					+ "\ntimeDurationForLastSingletonTrajectoryEntry" + timeDurationForLastSingletonTrajectoryEntry);

			for (int i = 1; i <= 992; i++) // 992
			{

				String splitID = "Split" + i;
				String pathToParse = rawPathToRead + "Split" + i + ".csv";// "dmicro.tsv";//
																			// "userid-timestamp-artid-artname-traid-traname.tsv";//
																			// "dmicro.tsv";//

				mapForAllData = createAnnotatedTracksMap(pathToParse);// createAnnotatedTracksMapReducer2(pathToParse);

				// $$mapForAllData = createAnnotatedTracksMap(); // read trajectory entries

				long numOfLineOfData = writeDataToFile2WithHeadersTLEOnlyAll(mapForAllData,
						"CreatedAnnotatedTrajectories",
						"userID\ttimestamp\tendTimestamp\tmbArtistID\tartistName\tmbTrackID\ttrackName\tdifferenceWithNextInSeconds\tdurationInSeconds\ttrajectoryIDsInCompactForm\tExtraComments\tbreakOverDaysCount",
						// "timestamp,
						// endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt",
						true, "\t", splitID);
				System.out.println("Num of lines of data written for created annotated data = " + numOfLineOfData); // this
																													// is
																													// for
																													// sanity
																													// check,
																													// this
																													// should
																													// be
																													// equal
																													// to
																													// the
																													// num
																													// of
																													// lines
																													// in
																													// the
																													// raw
																													// file
																													// read.
				// /////////////$$$$$$$$$$$$$$$$$$$$$44
				mapForAllDataTimeDifference = getDataEntriesWithTimeDifferenceWithNextTLE(mapForAllData); // add time
																											// difference
																											// with next
																											// to the
																											// trajectory
																											// entries
				writeDataToFile2WithHeadersTLEOnlyAll(mapForAllDataTimeDifference, "WithTimeDifference",
						"userID\ttimestamp\tendTimestamp\tmbArtistID\tartistName\tmbTrackID\ttrackName\tdifferenceWithNextInSeconds\tdurationInSeconds\ttrajectoryIDsInCompactForm\tExtraComments\tbreakOverDaysCount",
						// "timestamp,
						// endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt",
						true, "\t", splitID);

				boolean ts1SecAheadSanity = checkTimeStamp1AheadSanity(mapForAllDataTimeDifference);
				System.out.println("checkTimeStamp1AheadSanity check is sane? " + ts1SecAheadSanity);
				// /////////////$$$$$$$$$$$$$$$$$$$$$44

				mapForAllDataMergedContinuousWithDuration = mergeContinuousDataEntriesAssignDurationWithoutBOD3TLENoCaseC(
						mapForAllData, splitID);

				writeDataToFile2WithHeadersTLEOnlyAll(mapForAllDataMergedContinuousWithDuration,
						"AfterMergingContinuous",
						"userID\ttimestamp\tendTimestamp\tmbArtistID\tartistName\tmbTrackID\ttrackName\tdifferenceWithNextInSeconds\tdurationInSeconds\ttrajectoryIDsInCompactForm\tExtraComments\tbreakOverDaysCount",
						// "timestamp,
						// endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt",
						true, "\t", splitID);

				//// userID, date, startts, endts, hasmbArtistid, artistname,hasmusicbrainztrackid, trackname,
				//// diffeithnext
				writeDataToFile2WithHeadersTLEOnlyAllSlimmer(mapForAllDataMergedContinuousWithDuration,
						"AfterMergingContinuousSlimmer",
						"userID\tdate\ttimestamp\tendTimestamp\tHasmbArtistID\tartistName\tHasmbTrackID\ttrackName\tdifferenceWithNextInSeconds",
						// "timestamp,
						// endt,mode,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount,trajID,,latitude,longitude,alt",
						true, "\t", splitID);
			}
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

	/**
	 * While creating annotated track entries, some multiple entries were found for the same user and same timestamp. In
	 * such cases, the second data entry's timestamp was increased by 1 sec. However, we should be careful that this
	 * increase in timestamp of second data entry does not cause it clash with another next data entry. Conducting this
	 * check is the purpose of this method
	 * 
	 * @param mapForAllDataTimeDifference2
	 * @return
	 */
	private static boolean checkTimeStamp1AheadSanity(
			LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForData)
	{
		boolean isSane = true;
		for (Entry<String, TreeMap<Timestamp, TrackListenEntry>> entryForUser : mapForData.entrySet())
		{
			ArrayList<TrackListenEntry> dataForCurrentUser = (ArrayList<TrackListenEntry>) UtilityBelt
					.treeMapToArrayList(entryForUser.getValue());

			for (int i = 0; i < (dataForCurrentUser.size() - 1); i++)
			{
				if (dataForCurrentUser.get(i).getExtraComments().contains("ts1SecAheadDueToClash")
						&& dataForCurrentUser.get(i + 1).getExtraComments().contains("ts1SecAheadDueToClash"))
				{
					isSane = false;
					System.err.println("Error: checkTimeStamp1AheadSanity failed for following two data entries: \n"
							+ "\t" + dataForCurrentUser.get(i).toStringWithoutHeadersWithTrajID() + "\n\t"
							+ dataForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID());
				}
			}
		}
		return isSane;
	}

	private static boolean checkTimeStamp1AheadSanity(TreeMap<Timestamp, TrackListenEntry> mapForData)
	{
		boolean isSane = true;

		ArrayList<TrackListenEntry> dataForCurrentUser = (ArrayList<TrackListenEntry>) UtilityBelt
				.treeMapToArrayList(mapForData);

		for (int i = 0; i < (dataForCurrentUser.size() - 1); i++)
		{
			if (dataForCurrentUser.get(i).getExtraComments().contains("ts1SecAheadDueToClash")
					&& dataForCurrentUser.get(i + 1).getExtraComments().contains("ts1SecAheadDueToClash"))
			{
				isSane = false;
				System.err.println("Error: checkTimeStamp1AheadSanity failed for following two data entries: \n" + "\t"
						+ dataForCurrentUser.get(i).toStringWithoutHeadersWithTrajID() + "\n\t"
						+ dataForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID());
			}
		}

		return isSane;
	}

	private static void printLabelEntriesMap(LinkedHashMap<String, ArrayList<LabelEntry>> mapForLabelEntries)
	{
		StringBuffer stringToWrite = new StringBuffer("User, LabelEntry\n");
		for (Entry<String, ArrayList<LabelEntry>> e : mapForLabelEntries.entrySet())
		{
			String user = e.getKey();

			for (LabelEntry le : e.getValue())
			{
				stringToWrite.append(user + "," + le.toStringRaw() + "\n");
			}
		}
		WritingToFile.appendLineToFileAbsolute(stringToWrite.toString(), commonPath + "LabelEntriesMap.csv");
	}

	public static ArrayList<String> identifyOnlyTargetUsers()
	{
		ArrayList<String> listOfUsersWhoLabelled = new ArrayList<String>();
		int userIDs[] =
		{ 62, 84 };// , 52, 68, 167, 179, 153, 85, 128, 10 };
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
	public static LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mergeContinuousDataEntriesAssignDurationWithoutBOD3(
			LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllData)
	{
		System.out.println("-----------------Starting mergeContinuousDataEntriesAssignDurationWithoutBOD3\n");
		LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();

		LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();

		StringBuffer bwMergerCaseLogs = new StringBuffer();// WritingToFile.getBufferedWriterForNewFile(commonPath +
															// userID + "MergerCasesLog.csv");

		System.out.println("Merging continuous data and assigning duration without BOD");
		try
		{
			for (Map.Entry<String, TreeMap<Timestamp, DataEntry>> entryForUser : mapForAllData.entrySet())
			{
				int numOfMergerCaseA = 0, numOfMergerCaseB = 0, numOfMergerCaseC = 0, numOfLastTrajEntries = 0;
				int countOfContinuousMerged = 1;

				TreeMap<Timestamp, DataEntry> mapContinuousMerged = new TreeMap<Timestamp, DataEntry>();

				/** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
				TreeMap<Timestamp, DataEntry> unknownsInsertedWholes = new TreeMap<Timestamp, DataEntry>();

				String userID = entryForUser.getKey();// System.out.println("\nUser =" + userID);

				ArrayList<DataEntry> dataEntriesForCurrentUser = (ArrayList<DataEntry>) UtilityBelt
						.treeMapToArrayList(entryForUser.getValue());

				// $$System.out.println("----Unmerged Activity data for user "+userName+"-----");
				// $$traverseArrayList(dataForCurrentUser);
				// $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
				DataEntry currentAccumulativeTLE = null;// dataEntriesForCurrentUser.get(0);

				for (int i = 0; i < dataEntriesForCurrentUser.size(); i++)
				{
					if (currentAccumulativeTLE == null)
					{
						currentAccumulativeTLE = dataEntriesForCurrentUser.get(i);
					}

					Timestamp currentOriginalTimestamp = dataEntriesForCurrentUser.get(i).getTimestamp(); // the
																											// timestamp
																											// of the
																											// original
																											// (before
																											// merger)
																											// data
																											// entry.
					Timestamp currentCumulativeTLETimestamp = currentAccumulativeTLE.getTimestamp();

					long secsItContinuesBeforeNext = 0;

					System.out.println(
							"Reading current: " + dataEntriesForCurrentUser.get(i).toStringEssentialsWithoutHeaders());
					if (i < dataEntriesForCurrentUser.size() - 1) // is not the last element of arraylist
					{

						// check if the next element should be merged with this one if they are continuous and have same
						// activity name
						Timestamp nextTimestamp = dataEntriesForCurrentUser.get(i + 1).getTimestamp();

						// Case A: data entries can be mergeable and are less than p seconds apart.
						if (TrackListenEntry.isMergeableAllSame(dataEntriesForCurrentUser.get(i),
								dataEntriesForCurrentUser.get(i + 1))
								&& areContinuous(currentOriginalTimestamp, nextTimestamp)) // if current and next are
																							// same, gobble up the next
																							// one
						{

							currentAccumulativeTLE = TrackListenEntry.merge(currentAccumulativeTLE,
									dataEntriesForCurrentUser.get(i + 1));

							numOfMergerCaseA += 1;
							countOfContinuousMerged++;

							System.out.println(">> CaseA," + "next entry to gobble up:"
									+ dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders() + "\n");
							bwMergerCaseLogs.append("CaseA," + "next entry to gobble up:,"
									+ dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID() + "\n");
							continue;
						}

						else
						{
							long diffCurrentAndNextInSec = (nextTimestamp.getTime()
									- currentOriginalTimestamp.getTime()) / 1000;

							// Case B: data entries are not mergeable but are less than p seconds apart.
							if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were
																							// different activity names
							{

								numOfMergerCaseB += 1;
								secsItContinuesBeforeNext = 0;// diffCurrentAndNextInSec;

								System.out.println(">> CaseB, ( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec
										+ "<=" + assumeContinuesBeforeNextInSecs + ") next entry:"
										+ dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders()
										+ "\n");
								bwMergerCaseLogs.append("CaseB, ( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec
										+ "<=" + assumeContinuesBeforeNextInSecs + ") next entry:,"
										+ dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID()
										+ "\n");
							}

							else
							// Case C: data entries are not mergeable but are more than assumeContinuesBeforeNextInSecs
							// apart, hence we insert an 'Unknown' entry in the gap between
							// the currentCumulative entry and the next entry
							{

								numOfMergerCaseC += 1;
								secsItContinuesBeforeNext = // diffCurrentAndNextInSec
										(currentAccumulativeTLE.getTimestamp().getTime() / 1000)
												+ assumeContinuesBeforeNextInSecs;

								System.out.println(">> CaseC,( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec
										+ ">" + assumeContinuesBeforeNextInSecs + ")next entry:"
										+ dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders()
										+ "\n");
								bwMergerCaseLogs.append("CaseC,( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec
										+ ">" + assumeContinuesBeforeNextInSecs + ")next entry:,"
										+ dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID()
										+ "\n");

								// /////////
								currentAccumulativeTLE.setDifferenceWithNextInSeconds(secsItContinuesBeforeNext); // add
																													// the
																													// duration
																													// corresponding
																													// to
																													// curent
																													// data
																													// entry
								currentAccumulativeTLE.setDurationInSeconds(secsItContinuesBeforeNext);// add the
																										// duration
																										// corresponding
																										// to
								// //////

								/* Put the new 'Unknown' entry///////////////// */
								long durationForNewUnknownActivity = diffCurrentAndNextInSec
										- assumeContinuesBeforeNextInSecs;
								Timestamp startOfNewUnknown = new Timestamp(
										currentOriginalTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));

								TrackListenEntry te = new TrackListenEntry(startOfNewUnknown,
										durationForNewUnknownActivity, "Unknown", userID);

								mapContinuousMerged.put(startOfNewUnknown, te);
								unknownsInsertedWholes.put(startOfNewUnknown, te);
								/* End of put the new 'Unknown' entry///////////////// */

							} // end of else over Case C
						} // end of else over not Case A
					} // end of else over not last element
					else
					// is the last element
					{
						numOfLastTrajEntries += 1;

						secsItContinuesBeforeNext = timeDurationForLastSingletonTrajectoryEntry;
						// $$System.out.println("this is the last data point,\n duration in seconds =
						// "+durationInSeconds);
					}

					// currentAccumulativeTLE.setDifferenceWithNextInSeconds(currentAccumulativeTLE.getDifferenceWithNextInSeconds()
					// + secsItContinuesBeforeNext); // add the duration corresponding to curent data entry
					// currentAccumulativeTLE.setDurationInSeconds(currentAccumulativeTLE.getDurationInSeconds() +
					// secsItContinuesBeforeNext);// add the duration corresponding to
					// curent data entry

					System.out.println("~~~ putting accumlative in map: "
							+ currentAccumulativeTLE.toStringWithoutHeadersWithTrajID());
					mapContinuousMerged.put(currentAccumulativeTLE.getTimestamp(), currentAccumulativeTLE);
					currentAccumulativeTLE = null;
				} // end of for loop over trajectory entries for current user.

				mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);
				mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);

				bwMergerCaseLogs.append("User:" + userID + ",numOfTrajCaseA = " + numOfMergerCaseA
						+ ",numOfTrajCaseB = " + numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC
						+ " ,numOfLastTrajEntries = " + numOfLastTrajEntries);
				System.out.println("User:" + userID + ",numOfTrajCaseA = " + numOfMergerCaseA + ",numOfTrajCaseB = "
						+ numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC + " ,numOfLastTrajEntries = "
						+ numOfLastTrajEntries);

			} // end of for loop over users

			WritingToFile.writeToNewFile(bwMergerCaseLogs.toString(), commonPath + "MergerCasesLog.csv");

			WritingToFile.writeLinkedHashMapOfTreemapDE(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
					"User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeContinuousTrajectoriesAssignDurationWithoutBOD2()");
		}
		return mapForAllDataMergedPlusDuration;
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
	public static LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mergeContinuousDataEntriesAssignDurationWithoutBOD3TLE(
			LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForAllData)
	{
		System.out.println("-----------------Starting mergeContinuousDataEntriesAssignDurationWithoutBOD3\n");
		LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>();

		LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>();

		StringBuffer bwMergerCaseLogs = new StringBuffer();// WritingToFile.getBufferedWriterForNewFile(commonPath +
															// userID + "MergerCasesLog.csv");

		System.out.println("Merging continuous data and assigning duration without BOD");
		try
		{
			for (Map.Entry<String, TreeMap<Timestamp, TrackListenEntry>> entryForUser : mapForAllData.entrySet())
			{
				int numOfMergerCaseA = 0, numOfMergerCaseB = 0, numOfMergerCaseC = 0, numOfLastTrajEntries = 0;
				int countOfContinuousMerged = 1;

				TreeMap<Timestamp, TrackListenEntry> mapContinuousMerged = new TreeMap<Timestamp, TrackListenEntry>();

				/** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
				TreeMap<Timestamp, TrackListenEntry> unknownsInsertedWholes = new TreeMap<Timestamp, TrackListenEntry>();

				String userID = entryForUser.getKey();// System.out.println("\nUser =" + userID);

				ArrayList<TrackListenEntry> dataEntriesForCurrentUser = (ArrayList<TrackListenEntry>) UtilityBelt
						.treeMapToArrayList(entryForUser.getValue());

				// $$System.out.println("----Unmerged Activity data for user "+userName+"-----");
				// $$traverseArrayList(dataForCurrentUser);
				// $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
				TrackListenEntry currentAccumulativeTLE = null;// dataEntriesForCurrentUser.get(0);

				for (int i = 0; i < dataEntriesForCurrentUser.size(); i++)
				{
					if (currentAccumulativeTLE == null)
					{
						currentAccumulativeTLE = dataEntriesForCurrentUser.get(i);
					}

					Timestamp currentOriginalTimestamp = dataEntriesForCurrentUser.get(i).getTimestamp(); // the
																											// timestamp
																											// of the
																											// original
																											// (before
																											// merger)
																											// data
																											// entry.
					Timestamp currentCumulativeTLETimestamp = currentAccumulativeTLE.getTimestamp();

					long secsItContinuesBeforeNext = 0;

					System.out.println(
							"Reading current: " + dataEntriesForCurrentUser.get(i).toStringEssentialsWithoutHeaders());
					if (i < dataEntriesForCurrentUser.size() - 1) // is not the last element of arraylist
					{

						// check if the next element should be merged with this one if they are continuous and have same
						// activity name
						Timestamp nextTimestamp = dataEntriesForCurrentUser.get(i + 1).getTimestamp();

						// Case A: data entries can be mergeable and are less than p seconds apart.
						if (TrackListenEntry.isMergeableAllSame(dataEntriesForCurrentUser.get(i),
								dataEntriesForCurrentUser.get(i + 1))
								&& areContinuous(currentOriginalTimestamp, nextTimestamp)) // if current and next are
																							// same, gobble up the next
																							// one
						{

							currentAccumulativeTLE = TrackListenEntry.merge(currentAccumulativeTLE,
									dataEntriesForCurrentUser.get(i + 1));

							numOfMergerCaseA += 1;
							countOfContinuousMerged++;

							System.out.println(">> CaseA," + "next entry to gobble up:"
									+ dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders() + "\n");
							bwMergerCaseLogs.append("CaseA," + "next entry to gobble up:,"
									+ dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID() + "\n");
							continue;
						}

						else
						{
							long diffCurrentAndNextInSec = (nextTimestamp.getTime()
									- currentOriginalTimestamp.getTime()) / 1000;

							// Case B: data entries are not mergeable but are less than p seconds apart.
							if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were
																							// different activity names
							{

								numOfMergerCaseB += 1;
								secsItContinuesBeforeNext = 0;// diffCurrentAndNextInSec; we dont need to add duration
																// here as the acculated TLE already has the duration
																// added to
																// it

								System.out.println(">> CaseB, ( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec
										+ "<=" + assumeContinuesBeforeNextInSecs + ") next entry:"
										+ dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders()
										+ "\n");
								bwMergerCaseLogs.append("CaseB, ( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec
										+ "<=" + assumeContinuesBeforeNextInSecs + ") next entry:,"
										+ dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID()
										+ "\n");
							}

							else
							// Case C: data entries are not mergeable but are more than assumeContinuesBeforeNextInSecs
							// apart, hence we insert an 'Unknown' entry in the gap between
							// the currentCumulative entry and the next entry
							{

								numOfMergerCaseC += 1;
								secsItContinuesBeforeNext = // diffCurrentAndNextInSec
										(currentAccumulativeTLE.getTimestamp().getTime() / 1000)
												+ assumeContinuesBeforeNextInSecs;

								System.out.println(">> CaseC,( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec
										+ ">" + assumeContinuesBeforeNextInSecs + ")next entry:"
										+ dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders()
										+ "\n");
								bwMergerCaseLogs.append("CaseC,( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec
										+ ">" + assumeContinuesBeforeNextInSecs + ")next entry:,"
										+ dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID()
										+ "\n");

								// /////////
								currentAccumulativeTLE.setDifferenceWithNextInSeconds(secsItContinuesBeforeNext); // add
																													// the
																													// duration
																													// corresponding
																													// to
																													// curent
																													// data
																													// entry
								currentAccumulativeTLE.setDurationInSeconds(secsItContinuesBeforeNext);// add the
																										// duration
																										// corresponding
																										// to
								// //////

								/* Put the new 'Unknown' entry///////////////// */
								long durationForNewUnknownActivity = diffCurrentAndNextInSec
										- assumeContinuesBeforeNextInSecs;
								Timestamp startOfNewUnknown = new Timestamp(
										currentOriginalTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));

								TrackListenEntry te = new TrackListenEntry(startOfNewUnknown,
										durationForNewUnknownActivity, "Unknown", userID);

								mapContinuousMerged.put(startOfNewUnknown, te);
								unknownsInsertedWholes.put(startOfNewUnknown, te);
								/* End of put the new 'Unknown' entry///////////////// */

							} // end of else over Case C
						} // end of else over not Case A
					} // end of else over not last element
					else
					// is the last element
					{
						numOfLastTrajEntries += 1;

						secsItContinuesBeforeNext = timeDurationForLastSingletonTrajectoryEntry;
						// $$System.out.println("this is the last data point,\n duration in seconds =
						// "+durationInSeconds);
					}

					// currentAccumulativeTLE.setDifferenceWithNextInSeconds(currentAccumulativeTLE.getDifferenceWithNextInSeconds()
					// + secsItContinuesBeforeNext); // add the duration corresponding to curent data entry
					// currentAccumulativeTLE.setDurationInSeconds(currentAccumulativeTLE.getDurationInSeconds() +
					// secsItContinuesBeforeNext);// add the duration corresponding to
					// curent data entry

					System.out.println("~~~ putting accumlative in map: "
							+ currentAccumulativeTLE.toStringWithoutHeadersWithTrajID());
					mapContinuousMerged.put(currentAccumulativeTLE.getTimestamp(), currentAccumulativeTLE);
					currentAccumulativeTLE = null;
				} // end of for loop over trajectory entries for current user.

				mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);
				mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);

				bwMergerCaseLogs.append("User:" + userID + ",numOfTrajCaseA = " + numOfMergerCaseA
						+ ",numOfTrajCaseB = " + numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC
						+ " ,numOfLastTrajEntries = " + numOfLastTrajEntries);
				System.out.println("User:" + userID + ",numOfTrajCaseA = " + numOfMergerCaseA + ",numOfTrajCaseB = "
						+ numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC + " ,numOfLastTrajEntries = "
						+ numOfLastTrajEntries);

			} // end of for loop over users

			WritingToFile.writeToNewFile(bwMergerCaseLogs.toString(), commonPath + "MergerCasesLog.csv");

			WritingToFile.writeLinkedHashMapOfTreemapTLE(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
					"User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeContinuousDataEntriesAssignDurationWithoutBOD3TLE()");
		}
		return mapForAllDataMergedPlusDuration;
	}

	/**
	 * 
	 * Merging all consecutively same songs which are less than 420 secs apart. Not inserting unknowns for longer than p
	 * durations. No case C: no insertion of unknowns.
	 * 
	 * @param mapForAllData
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mergeContinuousDataEntriesAssignDurationWithoutBOD3TLENoCaseC(
			LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForAllData)
	{
		System.out.println("-----------------Starting mergeContinuousDataEntriesAssignDurationWithoutBOD3\n");
		LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>();

		LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>();

		StringBuffer bwMergerCaseLogs = new StringBuffer();// WritingToFile.getBufferedWriterForNewFile(commonPath +
															// userID + "MergerCasesLog.csv");

		System.out.println("Merging continuous data and assigning duration without BOD");
		try
		{
			for (Map.Entry<String, TreeMap<Timestamp, TrackListenEntry>> entryForUser : mapForAllData.entrySet())
			{
				int numOfMergerCaseA = 0, numOfMergerCaseB = 0, numOfMergerCaseC = 0, numOfLastTrajEntries = 0;
				int countOfContinuousMerged = 1;

				TreeMap<Timestamp, TrackListenEntry> mapContinuousMerged = new TreeMap<Timestamp, TrackListenEntry>();

				/** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
				TreeMap<Timestamp, TrackListenEntry> unknownsInsertedWholes = new TreeMap<Timestamp, TrackListenEntry>();

				String userID = entryForUser.getKey();// System.out.println("\nUser =" + userID);

				ArrayList<TrackListenEntry> dataEntriesForCurrentUser = (ArrayList<TrackListenEntry>) UtilityBelt
						.treeMapToArrayList(entryForUser.getValue());

				// $$System.out.println("----Unmerged Activity data for user "+userName+"-----");
				// $$traverseArrayList(dataForCurrentUser);
				// $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
				TrackListenEntry currentAccumulativeTLE = null;// dataEntriesForCurrentUser.get(0);

				for (int i = 0; i < dataEntriesForCurrentUser.size(); i++)
				{
					if (currentAccumulativeTLE == null)
					{
						currentAccumulativeTLE = dataEntriesForCurrentUser.get(i);
					}

					Timestamp currentOriginalTimestamp = dataEntriesForCurrentUser.get(i).getTimestamp(); // the
																											// timestamp
																											// of the
																											// original
																											// (before
																											// merger)
																											// data
																											// entry.
					Timestamp currentCumulativeTLETimestamp = currentAccumulativeTLE.getTimestamp();

					long secsItContinuesBeforeNext = 0;

					// System.out.println("Reading current: " +
					// dataEntriesForCurrentUser.get(i).toStringEssentialsWithoutHeaders());
					if (i < dataEntriesForCurrentUser.size() - 1) // is not the last element of arraylist
					{

						// check if the next element should be merged with this one if they are continuous and have same
						// activity name
						Timestamp nextTimestamp = dataEntriesForCurrentUser.get(i + 1).getTimestamp();

						// Case A: data entries can be mergeable and are less than p seconds apart.
						if (TrackListenEntry.isMergeableAllSame(dataEntriesForCurrentUser.get(i),
								dataEntriesForCurrentUser.get(i + 1))
								&& areContinuous(currentOriginalTimestamp, nextTimestamp)) // if current and next are
																							// same, gobble up the next
																							// one
						{

							currentAccumulativeTLE = TrackListenEntry.merge(currentAccumulativeTLE,
									dataEntriesForCurrentUser.get(i + 1));

							numOfMergerCaseA += 1;
							countOfContinuousMerged++;

							// System.out.println(">> CaseA," + "next entry to gobble up:"
							// + dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders() + "\n");
							// bwMergerCaseLogs.append("CaseA," + "next entry to gobble up:,"
							// + dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID() + "\n");
							continue;
						}

						else
						{
							long diffCurrentAndNextInSec = (nextTimestamp.getTime()
									- currentOriginalTimestamp.getTime()) / 1000;

							// Case B: data entries are not mergeable but are less than p seconds apart.
							// if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these
							// were different activity names
							// {

							numOfMergerCaseB += 1;
							secsItContinuesBeforeNext = 0;// diffCurrentAndNextInSec; we dont need to add duration here
															// as the acculated TLE already has the duration added to
															// it

							// System.out.println(">> CaseB, ( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec +
							// "<="
							// + assumeContinuesBeforeNextInSecs + ") next entry:"
							// + dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders() + "\n");
							// bwMergerCaseLogs.append("CaseB, ( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec +
							// "<="
							// + assumeContinuesBeforeNextInSecs + ") next entry:,"
							// + dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID() + "\n");
							// }

							// else
							// // Case C: data entries are not mergeable but are more than
							// assumeContinuesBeforeNextInSecs apart, hence we insert an 'Unknown' entry in the gap
							// between
							// // the currentCumulative entry and the next entry
							// {
							//
							// numOfMergerCaseC += 1;
							// secsItContinuesBeforeNext = // diffCurrentAndNextInSec
							// (currentAccumulativeTLE.getTimestamp().getTime() / 1000) +
							// assumeContinuesBeforeNextInSecs;
							//
							// System.out.println(">> CaseC,( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec +
							// ">"
							// + assumeContinuesBeforeNextInSecs + ")next entry:"
							// + dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders() + "\n");
							// bwMergerCaseLogs.append("CaseC,( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec +
							// ">"
							// + assumeContinuesBeforeNextInSecs + ")next entry:,"
							// + dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID() + "\n");
							//
							// // /////////
							// currentAccumulativeTLE.setDifferenceWithNextInSeconds(secsItContinuesBeforeNext); // add
							// the duration corresponding to curent data entry
							// currentAccumulativeTLE.setDurationInSeconds(secsItContinuesBeforeNext);// add the
							// duration corresponding to
							// // //////
							//
							// /* Put the new 'Unknown' entry///////////////// */
							// long durationForNewUnknownActivity = diffCurrentAndNextInSec -
							// assumeContinuesBeforeNextInSecs;
							// Timestamp startOfNewUnknown =
							// new Timestamp(currentOriginalTimestamp.getTime() + (assumeContinuesBeforeNextInSecs *
							// 1000));
							//
							// TrackListenEntry te =
							// new TrackListenEntry(startOfNewUnknown, durationForNewUnknownActivity, "Unknown",
							// userID);
							//
							// mapContinuousMerged.put(startOfNewUnknown, te);
							// unknownsInsertedWholes.put(startOfNewUnknown, te);
							// /* End of put the new 'Unknown' entry///////////////// */
							//
							// } // end of else over Case C
						} // end of else over not Case A
					} // end of else over not last element
					else
					// is the last element
					{
						numOfLastTrajEntries += 1;

						secsItContinuesBeforeNext = timeDurationForLastSingletonTrajectoryEntry;
						// $$System.out.println("this is the last data point,\n duration in seconds =
						// "+durationInSeconds);
					}

					// currentAccumulativeTLE.setDifferenceWithNextInSeconds(currentAccumulativeTLE.getDifferenceWithNextInSeconds()
					// + secsItContinuesBeforeNext); // add the duration corresponding to curent data entry
					// currentAccumulativeTLE.setDurationInSeconds(currentAccumulativeTLE.getDurationInSeconds() +
					// secsItContinuesBeforeNext);// add the duration corresponding to
					// curent data entry

					// System.out.println("~~~ putting accumlative in map: " +
					// currentAccumulativeTLE.toStringWithoutHeadersWithTrajID());
					mapContinuousMerged.put(currentAccumulativeTLE.getTimestamp(), currentAccumulativeTLE);
					currentAccumulativeTLE = null;
				} // end of for loop over trajectory entries for current user.

				mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);
				mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);

				bwMergerCaseLogs.append("User:" + userID + ",numOfTrajCaseA = " + numOfMergerCaseA
						+ ",numOfTrajCaseB = " + numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC
						+ " ,numOfLastTrajEntries = " + numOfLastTrajEntries);
				System.out.println("User:" + userID + ",numOfTrajCaseA = " + numOfMergerCaseA + ",numOfTrajCaseB = "
						+ numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC + " ,numOfLastTrajEntries = "
						+ numOfLastTrajEntries);

			} // end of for loop over users

			WritingToFile.writeToNewFile(bwMergerCaseLogs.toString(), commonPath + "MergerCasesLog.csv");

			WritingToFile.writeLinkedHashMapOfTreemapTLE(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
					"User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeContinuousDataEntriesAssignDurationWithoutBOD3TLE()");
		}
		return mapForAllDataMergedPlusDuration;
	}

	/**
	 * 
	 * Merging all consecutively same songs which are less than 420 secs apart. Not inserting unknowns for longer than p
	 * durations. No case C: no insertion of unknowns.
	 * 
	 * @param mapForAllData
	 * @return
	 */
	public static TreeMap<Timestamp, TrackListenEntry> mergeContinuousDataEntriesAssignDurationWithoutBOD3TLENoCaseC(
			TreeMap<Timestamp, TrackListenEntry> mapForAllData, String splitID)
	{
		System.out.println("-----------------Starting mergeContinuousDataEntriesAssignDurationWithoutBOD3\n");

		StringBuffer bwMergerCaseLogs = new StringBuffer();// WritingToFile.getBufferedWriterForNewFile(commonPath +
															// userID + "MergerCasesLog.csv");

		System.out.println("Merging continuous data and assigning duration without BOD");

		int numOfMergerCaseA = 0, numOfMergerCaseB = 0, numOfMergerCaseC = 0, numOfLastTrajEntries = 0;
		int countOfContinuousMerged = 1;

		TreeMap<Timestamp, TrackListenEntry> mapContinuousMerged = new TreeMap<Timestamp, TrackListenEntry>();

		/** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
		TreeMap<Timestamp, TrackListenEntry> unknownsInsertedWholes = new TreeMap<Timestamp, TrackListenEntry>();

		try
		{

			ArrayList<TrackListenEntry> dataEntriesForCurrentUser = (ArrayList<TrackListenEntry>) UtilityBelt
					.treeMapToArrayList(mapForAllData);

			// $$System.out.println("----Unmerged Activity data for user "+userName+"-----");
			// $$traverseArrayList(dataForCurrentUser);
			// $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
			TrackListenEntry currentAccumulativeTLE = null;// dataEntriesForCurrentUser.get(0);

			for (int i = 0; i < dataEntriesForCurrentUser.size(); i++)
			{
				if (currentAccumulativeTLE == null)
				{
					currentAccumulativeTLE = dataEntriesForCurrentUser.get(i);
				}

				Timestamp currentOriginalTimestamp = dataEntriesForCurrentUser.get(i).getTimestamp(); // the timestamp
																										// of the
																										// original
																										// (before
																										// merger) data
																										// entry.
				Timestamp currentCumulativeTLETimestamp = currentAccumulativeTLE.getTimestamp();

				long secsItContinuesBeforeNext = 0;

				// System.out.println("Reading current: " +
				// dataEntriesForCurrentUser.get(i).toStringEssentialsWithoutHeaders());
				if (i < dataEntriesForCurrentUser.size() - 1) // is not the last element of arraylist
				{

					// check if the next element should be merged with this one if they are continuous and have same
					// activity name
					Timestamp nextTimestamp = dataEntriesForCurrentUser.get(i + 1).getTimestamp();

					// Case A: data entries can be mergeable and are less than p seconds apart.
					if (TrackListenEntry.isMergeableAllSame(dataEntriesForCurrentUser.get(i),
							dataEntriesForCurrentUser.get(i + 1))
							&& areContinuous(currentOriginalTimestamp, nextTimestamp)) // if current and next are same,
																						// gobble up the next one
					{

						currentAccumulativeTLE = TrackListenEntry.merge(currentAccumulativeTLE,
								dataEntriesForCurrentUser.get(i + 1));

						numOfMergerCaseA += 1;
						countOfContinuousMerged++;

						// System.out.println(">> CaseA," + "next entry to gobble up:"
						// + dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders() + "\n");
						// bwMergerCaseLogs.append("CaseA," + "next entry to gobble up:,"
						// + dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID() + "\n");
						continue;
					}

					else
					{
						long diffCurrentAndNextInSec = (nextTimestamp.getTime() - currentOriginalTimestamp.getTime())
								/ 1000;

						// Case B: data entries are not mergeable but are less than p seconds apart.
						// if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were
						// different activity names
						// {

						numOfMergerCaseB += 1;
						secsItContinuesBeforeNext = 0;// diffCurrentAndNextInSec; we dont need to add duration here as
														// the acculated TLE already has the duration added to
														// it

						// System.out.println(">> CaseB, ( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec + "<="
						// + assumeContinuesBeforeNextInSecs + ") next entry:"
						// + dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders() + "\n");
						// bwMergerCaseLogs.append("CaseB, ( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec +
						// "<="
						// + assumeContinuesBeforeNextInSecs + ") next entry:,"
						// + dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID() + "\n");
						// }

						// else
						// // Case C: data entries are not mergeable but are more than assumeContinuesBeforeNextInSecs
						// apart, hence we insert an 'Unknown' entry in the gap
						// between
						// // the currentCumulative entry and the next entry
						// {
						//
						// numOfMergerCaseC += 1;
						// secsItContinuesBeforeNext = // diffCurrentAndNextInSec
						// (currentAccumulativeTLE.getTimestamp().getTime() / 1000) + assumeContinuesBeforeNextInSecs;
						//
						// System.out.println(">> CaseC,( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec + ">"
						// + assumeContinuesBeforeNextInSecs + ")next entry:"
						// + dataEntriesForCurrentUser.get(i + 1).toStringEssentialsWithoutHeaders() + "\n");
						// bwMergerCaseLogs.append("CaseC,( diffCurrentAndNextInSec = " + diffCurrentAndNextInSec + ">"
						// + assumeContinuesBeforeNextInSecs + ")next entry:,"
						// + dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID() + "\n");
						//
						// // /////////
						// currentAccumulativeTLE.setDifferenceWithNextInSeconds(secsItContinuesBeforeNext); // add the
						// duration corresponding to curent data entry
						// currentAccumulativeTLE.setDurationInSeconds(secsItContinuesBeforeNext);// add the duration
						// corresponding to
						// // //////
						//
						// /* Put the new 'Unknown' entry///////////////// */
						// long durationForNewUnknownActivity = diffCurrentAndNextInSec -
						// assumeContinuesBeforeNextInSecs;
						// Timestamp startOfNewUnknown =
						// new Timestamp(currentOriginalTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));
						//
						// TrackListenEntry te =
						// new TrackListenEntry(startOfNewUnknown, durationForNewUnknownActivity, "Unknown", userID);
						//
						// mapContinuousMerged.put(startOfNewUnknown, te);
						// unknownsInsertedWholes.put(startOfNewUnknown, te);
						// /* End of put the new 'Unknown' entry///////////////// */
						//
						// } // end of else over Case C
					} // end of else over not Case A
				} // end of else over not last element
				else
				// is the last element
				{
					numOfLastTrajEntries += 1;

					secsItContinuesBeforeNext = timeDurationForLastSingletonTrajectoryEntry;
					// $$System.out.println("this is the last data point,\n duration in seconds = "+durationInSeconds);
				}

				// currentAccumulativeTLE.setDifferenceWithNextInSeconds(currentAccumulativeTLE.getDifferenceWithNextInSeconds()
				// + secsItContinuesBeforeNext); // add the duration corresponding to curent data entry
				// currentAccumulativeTLE.setDurationInSeconds(currentAccumulativeTLE.getDurationInSeconds() +
				// secsItContinuesBeforeNext);// add the duration corresponding to
				// curent data entry

				// System.out.println("~~~ putting accumlative in map: " +
				// currentAccumulativeTLE.toStringWithoutHeadersWithTrajID());
				mapContinuousMerged.put(currentAccumulativeTLE.getTimestamp(), currentAccumulativeTLE);
				currentAccumulativeTLE = null;
			} // end of for over entries for this user.

			bwMergerCaseLogs.append(splitID + ": numOfTrajCaseA = " + numOfMergerCaseA + ",numOfTrajCaseB = "
					+ numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC + " ,numOfLastTrajEntries = "
					+ numOfLastTrajEntries + "\n");
			System.out.println(splitID + ": numOfTrajCaseA = " + numOfMergerCaseA + ",numOfTrajCaseB = "
					+ numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC + " ,numOfLastTrajEntries = "
					+ numOfLastTrajEntries + "\n");
			WritingToFile.appendLineToFileAbsolute(bwMergerCaseLogs.toString(), commonPath + "MergerCasesLog.csv");

			// WritingToFile.writeLinkedHashMapOfTreemapTLE(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
			// "User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeContinuousDataEntriesAssignDurationWithoutBOD3TLE()");
		}
		return mapContinuousMerged;
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
	public static LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mergeContinuousDataEntriesAssignDurationWithoutBOD3ConsideringLengthOfSong(
			LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllData)
	{
		LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();

		LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();

		StringBuffer bwMergerCaseLogs = new StringBuffer();// WritingToFile.getBufferedWriterForNewFile(commonPath +
															// userID + "MergerCasesLog.csv");

		System.out.println("Merging continuous data and assigning duration without BOD");
		try
		{
			for (Map.Entry<String, TreeMap<Timestamp, DataEntry>> entryForUser : mapForAllData.entrySet())
			{
				int numOfMergerCaseA = 0, numOfMergerCaseB = 0, numOfMergerCaseC = 0, numOfLastTrajEntries = 0;
				int countOfContinuousMerged = 1;

				TreeMap<Timestamp, DataEntry> mapContinuousMerged = new TreeMap<Timestamp, DataEntry>();

				/** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
				TreeMap<Timestamp, DataEntry> unknownsInsertedWholes = new TreeMap<Timestamp, DataEntry>();

				String userID = entryForUser.getKey();// System.out.println("\nUser =" + userID);

				ArrayList<DataEntry> dataEntriesForCurrentUser = (ArrayList<DataEntry>) UtilityBelt
						.treeMapToArrayList(entryForUser.getValue());

				// $$System.out.println("----Unmerged Activity data for user "+userName+"-----");
				// $$traverseArrayList(dataForCurrentUser);
				// $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
				DataEntry currentAccumulativeTLE = null;// dataEntriesForCurrentUser.get(0);

				for (int i = 0; i < dataEntriesForCurrentUser.size(); i++)
				{
					if (currentAccumulativeTLE == null)
					{
						currentAccumulativeTLE = dataEntriesForCurrentUser.get(i);
					}

					Timestamp currentOriginalTimestamp = dataEntriesForCurrentUser.get(i).getTimestamp(); // the
																											// timestamp
																											// of the
																											// original
																											// (before
																											// merger)
																											// data
																											// entry.
					Timestamp currentCumulativeTLETimestamp = currentAccumulativeTLE.getTimestamp();

					long secsItContinuesBeforeNext;
					if (i < dataEntriesForCurrentUser.size() - 1) // is not the last element of arraylist
					{

						// check if the next element should be merged with this one if they are continuous and have same
						// activity name
						Timestamp nextTimestamp = dataEntriesForCurrentUser.get(i + 1).getTimestamp();

						// Case A: data entries can be mergeable and are less than p seconds apart.
						if (TrackListenEntry.isMergeableAllSame(dataEntriesForCurrentUser.get(i),
								dataEntriesForCurrentUser.get(i + 1))
								&& areContinuous(currentOriginalTimestamp, nextTimestamp)) // if current and next are
																							// same, gobble up the next
																							// one
						{

							currentAccumulativeTLE = TrackListenEntry.merge(currentAccumulativeTLE,
									dataEntriesForCurrentUser.get(i + 1));
							numOfMergerCaseA += 1;
							countOfContinuousMerged++;
							bwMergerCaseLogs.append("CaseA," + "next entry:"
									+ dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID() + "\n");
							continue;
						}

						else
						{
							long diffCurrentAndNextInSec = (nextTimestamp.getTime()
									- currentOriginalTimestamp.getTime()) / 1000;

							// Case B: data entries are not mergeable but are less than p seconds apart.
							if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were
																							// different activity names
							{
								numOfMergerCaseB += 1;
								secsItContinuesBeforeNext = diffCurrentAndNextInSec;

								bwMergerCaseLogs.append("CaseB," + "next entry:"
										+ dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID()
										+ "\n");
							}

							else
							// Case C: data entries are not mergeable but are more than assumeContinuesBeforeNextInSecs
							// apart, hence we insert an 'Unknown' entry in the gap between
							// the currentCumulative entry and the next entry
							{
								numOfMergerCaseC += 1;
								secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;

								bwMergerCaseLogs.append("CaseC," + "next entry:"
										+ dataEntriesForCurrentUser.get(i + 1).toStringWithoutHeadersWithTrajID()
										+ "\n");

								/* Put the new 'Unknown' entry///////////////// */
								long durationForNewUnknownActivity = diffCurrentAndNextInSec
										- assumeContinuesBeforeNextInSecs;
								Timestamp startOfNewUnknown = new Timestamp(
										currentOriginalTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));

								TrackListenEntry te = new TrackListenEntry(startOfNewUnknown,
										durationForNewUnknownActivity, "Unknown", userID);

								mapContinuousMerged.put(startOfNewUnknown, te);
								unknownsInsertedWholes.put(startOfNewUnknown, te);
								/* End of put the new 'Unknown' entry///////////////// */

							} // end of else over Case C
						} // end of else over not Case A
					} // end of else over not last element
					else
					// is the last element
					{
						numOfLastTrajEntries += 1;

						secsItContinuesBeforeNext = timeDurationForLastSingletonTrajectoryEntry;
						// $$System.out.println("this is the last data point,\n duration in seconds =
						// "+durationInSeconds);
					}

					currentAccumulativeTLE.setDifferenceWithNextInSeconds(
							currentAccumulativeTLE.getDifferenceWithNextInSeconds() + secsItContinuesBeforeNext); // add
																													// the
																													// duration
																													// corresponding
																													// to
																													// curent
																													// data
																													// entry
					currentAccumulativeTLE.setDurationInSeconds(
							currentAccumulativeTLE.getDurationInSeconds() + secsItContinuesBeforeNext);// add the
																										// duration
																										// corresponding
																										// to
																										// curent data
																										// entry
					mapContinuousMerged.put(currentAccumulativeTLE.getTimestamp(), currentAccumulativeTLE);
					currentAccumulativeTLE = null;
				} // end of for loop over trajectory entries for current user.

				mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);
				mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);

				bwMergerCaseLogs.append("User:" + userID + ",numOfTrajCaseA = " + numOfMergerCaseA
						+ ",numOfTrajCaseB = " + numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC
						+ " ,numOfLastTrajEntries = " + numOfLastTrajEntries);
				System.out.println("User:" + userID + ",numOfTrajCaseA = " + numOfMergerCaseA + ",numOfTrajCaseB = "
						+ numOfMergerCaseB + ",numOfTrajCaseC = " + numOfMergerCaseC + " ,numOfLastTrajEntries = "
						+ numOfLastTrajEntries);

			} // end of for loop over users

			WritingToFile.writeToNewFile(bwMergerCaseLogs.toString(), commonPath + "MergerCasesLog.csv");

			WritingToFile.writeLinkedHashMapOfTreemapDE(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
					"User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeContinuousTrajectoriesAssignDurationWithoutBOD2()");
		}
		return mapForAllDataMergedPlusDuration;
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
	// public static LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>
	// mergeContinuousTrajectoriesAssignDurationWithoutBOD2(
	// LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllData)
	// {
	// LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllDataMergedPlusDuration =
	// new LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();
	// /*
	// * Note: using TreeMap is IMPORTANT here, because TreeMap will automatically sort by the timestamp, so we do not
	// need to be concerned about whether we add theactivities in
	// * correct order or not, if the timestamps are right, it will be stored correctly
	// */
	// LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllUnknownsWholes =
	// new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();
	//
	// System.out.println("Merging continuous trajectories and assigning duration without BOD");
	// try
	// {
	// for (Map.Entry<String, TreeMap<Timestamp, DataEntry>> entryForUser : mapForAllData.entrySet())
	// {
	// String userID = entryForUser.getKey();
	// BufferedWriter bwMergerCaseLogs = WritingToFile.getBufferedWriterForNewFile(commonPath + userID +
	// "MergerCasesLog.csv");
	// bwMergerCaseLogs.write("Case,Mode,DurationInSecs,CurrentTS, NextTS,Comment\n");
	//
	// System.out.println("\nUser =" + userID);
	//
	// int numOfTrajCaseA = 0, numOfTrajCaseB = 0, numOfTrajCaseC = 0, numOfLastTrajEntries = 0;
	//
	// int countOfContinuousMerged = 1;
	//
	// /** Records the "Unknown"s inserted, the <start timestamp of the insertion, duration of the unknown> */
	// TreeMap<Timestamp, TrajectoryEntry> unknownsInsertedWholes = new TreeMap<Timestamp, TrajectoryEntry>();
	// TreeMap<Timestamp, TrajectoryEntry> mapContinuousMerged = new TreeMap<Timestamp, TrajectoryEntry>();
	//
	// long durationInSeconds = 0;
	// ArrayList<String> newLati = new ArrayList<String>(), newLongi = new ArrayList<String>(), newAlti = new
	// ArrayList<String>();
	// ArrayList<String> newTrajID = new ArrayList<String>();
	//
	// long timeDiffWithNextInSeconds = 0; // do not delete. // not directly relevant
	// Timestamp startTimestamp;
	//
	// ArrayList<TrajectoryEntry> trajEntriesForCurrentUser =
	// UtilityBelt.treeMapToArrayListGeo(entryForUser.getValue());
	//
	// // $$System.out.println("----Unmerged Activity data for user "+userName+"-----");
	// // $$traverseArrayList(dataForCurrentUser);
	// // $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
	// for (int i = 0; i < trajEntriesForCurrentUser.size(); i++)
	// {
	// // startTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));
	// // ##
	// // $$System.out.println("\nReading: "+dataForCurrentUser.get(i).toString());
	// Timestamp currentTimestamp = trajEntriesForCurrentUser.get(i).getTimestamp();
	// String currentModeName = trajEntriesForCurrentUser.get(i).getMode();
	//
	// ArrayList<String> currentLat = trajEntriesForCurrentUser.get(i).getLatitude();
	// ArrayList<String> currentLon = trajEntriesForCurrentUser.get(i).getLongitude();
	// ArrayList<String> currentAlt = trajEntriesForCurrentUser.get(i).getAltitude();
	// ArrayList<String> currentTrajID = trajEntriesForCurrentUser.get(i).getTrajectoryID();
	//
	// newLati.addAll(currentLat);
	// newLongi.addAll(currentLon);
	// newAlti.addAll(currentAlt);
	// newTrajID.addAll(currentTrajID);
	// // startTimestamp=currentTimestamp;
	//
	// if (i < trajEntriesForCurrentUser.size() - 1) // is not the last element of arraylist
	// {
	// // check if the next element should be merged with this one if they are continuous and have same activity name
	// Timestamp nextTimestamp = trajEntriesForCurrentUser.get(i + 1).getTimestamp();
	// String nextModeName = trajEntriesForCurrentUser.get(i + 1).getMode();
	//
	// // ArrayList<Double> nextLat = dataForCurrentUser.get(i+1).getLatitude();
	// // ArrayList<Double> nextLon = dataForCurrentUser.get(i+1).getLongitude();
	// // ArrayList<Double> nextAlt = dataForCurrentUser.get(i+1).getAltitude();
	//
	// if (nextModeName.equals(currentModeName) && areContinuous(currentTimestamp, nextTimestamp))
	// {
	// numOfTrajCaseA += 1;
	// durationInSeconds += (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;
	//
	// timeDiffWithNextInSeconds =
	// trajEntriesForCurrentUser.get(i).getDifferenceWithNextInSeconds()
	// + trajEntriesForCurrentUser.get(i + 1).getDifferenceWithNextInSeconds(); // TODO CHECK IF NOT NEEDED
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
	// // ##bwMergerCaseLogs.write("CaseA: Continuous merged for mode=" + currentModeName + " durationInSeconds="+
	// durationInSeconds + "\n");
	// bwMergerCaseLogs.write("CaseA," + currentModeName + "," + durationInSeconds + "," + currentTimestamp + ","
	// + nextTimestamp + ",merged as continuous\n");
	// continue;
	// }
	//
	// else
	// {
	// startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));// durationInSeconds is
	// the accumulated duration from past
	// // merging
	// // ##System.out.println("new starttimestamp="+startTimestamp);
	//
	// long diffCurrentAndNextInSec = (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;
	// long secsItContinuesBeforeNext;
	//
	// if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were different activity
	// names
	// {
	// numOfTrajCaseB += 1;
	// secsItContinuesBeforeNext = diffCurrentAndNextInSec;
	// // ##bwMergerCaseLogs.write("CaseB: diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs,
	// secsItContinuesBeforeNext=" +
	// // secsItContinuesBeforeNext + "\n");
	// bwMergerCaseLogs.write("CaseB," + currentModeName + "," + durationInSeconds + "," + currentTimestamp + ","
	// + nextTimestamp + "," + diffCurrentAndNextInSec + "<=" + assumeContinuesBeforeNextInSecs
	// + " secsItContinuesBeforeNext =" + secsItContinuesBeforeNext + "\n");
	// }
	//
	// else
	// {
	// // System.out.println("\n\t For user: "+userID+", at currentTimestamp="+currentTimestamp+",
	// currentModeName="+currentModeName);
	// numOfTrajCaseC += 1;
	// secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;
	//
	// // ##bwMergerCaseLogs.write("CaseC: diffCurrentAndNextDifferentInSec (" + diffCurrentAndNextInSec + ") >"+
	// assumeContinuesBeforeNextInSecs + "\n");
	// bwMergerCaseLogs.write("CaseC," + currentModeName + "," + durationInSeconds + "," + currentTimestamp + ","
	// + nextTimestamp + "," + diffCurrentAndNextInSec + ">" + assumeContinuesBeforeNextInSecs
	// + " secsItContinuesBeforeNext =" + secsItContinuesBeforeNext + " put new Unknown\n");
	//
	// /* Put the new 'Unknown' entry///////////////// */
	// long durationForNewUnknownActivity = diffCurrentAndNextInSec - assumeContinuesBeforeNextInSecs;
	// Timestamp startOfNewUnknown =
	// new Timestamp(currentTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));
	//
	// // unknownsInsertedWholes.put(startOfNewUnknown, new TrajectoryEntry(startOfNewUnknown,
	// durationForNewUnknownActivity,"Unknown")); //
	// // String.valueOf(durationForNewUnknownActivity));
	//
	// TrajectoryEntry te = new TrajectoryEntry(startOfNewUnknown, durationForNewUnknownActivity, "Unknown");//
	// ,bodCount);
	// mapContinuousMerged.put(startOfNewUnknown, te);
	// unknownsInsertedWholes.put(startOfNewUnknown, te);
	// // $$System.out.println("Added Trajectory Entry: "+te.toString());
	// }
	//
	// durationInSeconds = durationInSeconds + secsItContinuesBeforeNext;
	//
	// TrajectoryEntry te = trajEntriesForCurrentUser.get(i);
	// te.setLatitude(newLati); // note: has to be done with set,..cant do with add becasue it will create problems if
	// more than two entries are merged
	// te.setLongitude(newLongi);
	// te.setAltitude(newAlti);
	// te.setTrajectoryID(newTrajID);
	//
	// te.setTimestamp(startTimestamp);
	// te.setDurationInSeconds(durationInSeconds);
	// // te.setDifferenceWithNextInSeconds(timeDiffWithNextInSeconds);
	//
	// mapContinuousMerged.put(startTimestamp, te);
	// // $$System.out.println("Added Trajectory Entry: "+te.toString());
	//
	// // durationInSeconds =0;
	// // timeDiffWithNextInSeconds =0;
	// // newLati.clear();newLongi.clear();newAlti.clear();
	// }
	//
	// }
	// else
	// // is the last element
	// {
	// numOfLastTrajEntries += 1;
	//
	// // $$System.out.println("this is the last data point,\n duration in seconds = "+durationInSeconds);
	//
	// startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));
	//
	// TrajectoryEntry te = trajEntriesForCurrentUser.get(i);
	// te.setLatitude(newLati); // note: has to be done with set,..cant do with add becasue it will create problems if
	// more than two entries are merged
	// te.setLongitude(newLongi);
	// te.setAltitude(newAlti);
	// te.setTrajectoryID(newTrajID);
	// te.setTimestamp(startTimestamp);
	// te.setDurationInSeconds(durationInSeconds + timeDurationForLastSingletonTrajectoryEntry);
	//
	// mapContinuousMerged.put(startTimestamp, te);
	//
	// // $$System.out.println("Added Trajectory Entry: "+te.toString());
	//
	// // newLati.clear();newLongi.clear();newAlti.clear();
	// // //////////////////////
	// // /*REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
	// currentActivityName+"||"+durationInSeconds); */
	// // durationInSeconds=0;
	// }
	//
	// // $$System.out.println("Clearing variables");//
	// durationInSeconds = 0;
	// timeDiffWithNextInSeconds = 0;
	// newLati.clear();
	// newLongi.clear();
	// newAlti.clear();
	// newTrajID.clear();
	// }// end of for loop over trajectory entries for current user.
	//
	// mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);
	// mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);
	//
	// bwMergerCaseLogs.write("User:" + userID + ",numOfTrajCaseA = " + numOfTrajCaseA + ",numOfTrajCaseB = " +
	// numOfTrajCaseB
	// + ",numOfTrajCaseC = " + numOfTrajCaseC + " ,numOfLastTrajEntries = " + numOfLastTrajEntries);
	// System.out.println("User:" + userID + ",numOfTrajCaseA = " + numOfTrajCaseA + ",numOfTrajCaseB = " +
	// numOfTrajCaseB
	// + ",numOfTrajCaseC = " + numOfTrajCaseC + " ,numOfLastTrajEntries = " + numOfLastTrajEntries);
	// bwMergerCaseLogs.close();
	// }// end of for loop over users
	//
	// WritingToFile.writeLinkedHashMapOfTreemap2(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
	// "User,Timestamp,DurationInSecs");
	// }
	// catch (Exception e)
	// {
	// PopUps.showException(e, "mergeContinuousTrajectoriesAssignDurationWithoutBOD2()");
	// }
	// return mapForAllDataMergedPlusDuration;
	// }
	//
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

		WritingToFile.writeLinkedHashMapOfTreemap(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
				"User,Timestamp,DurationInSecs");
		WritingToFile.writeLinkedHashMapOfTreemap(mapForAllUnknownsBrokenOverDays, "Unknown_BrokenOverDays_Inserted",
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
			WritingToFile.writeSimpleLinkedHashMapToFile(numberOfSandwichesFound, commonPath + "sandwichesPerUser_"
					+ activityNameToMerge + thresholdForMergingNotAvailables + "secs.csv", "User",
					"number_of_sandwiches");

			mapCleanedMerged.put(userName, arrayListToTreeMap(cleanedMergedDataForCurrentUser));

		}

		return mapCleanedMerged;
	}

	// /**
	// *
	// * @param mapForAllData
	// * @param activityNameToMerge
	// * @return
	// */
	// public static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>
	// mergeCleanSmallNotAvailablesTrajectoryEntries(
	// LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllData, String activityNameToMerge)
	// {
	// LinkedHashMap<String, TreeMap<Timestamp, String>> mapCleanedMerged = new LinkedHashMap<String, TreeMap<Timestamp,
	// String>>();
	// System.out.println("Merging and cleaning small " + activityNameToMerge);
	//
	// for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : mapForAllData.entrySet())
	// {
	// String userName = entryForUser.getKey();
	//
	// ArrayList<TrajectoryEntry> trajEntriesForCurrentUser =
	// UtilityBelt.treeMapToArrayListGeo(entryForUser.getValue());
	// ArrayList<TrajectoryEntry> cleanedMergedDataForCurrentUser = new ArrayList<TrajectoryEntry>();
	//
	// for (int i = 0; i < trajEntriesForCurrentUser.size(); i++)
	// {
	// String[] splittedDataEntryCurr = trajEntriesForCurrentUser.get(i).split(Pattern.quote("||"));
	// String currentActivityName = splittedDataEntryCurr[1];
	// Long currentActivityDuration = Long.valueOf(splittedDataEntryCurr[2]);
	//
	// if (i < trajEntriesForCurrentUser.size() - 1) // atleast one entry after this
	// {
	// String[] splittedDataEntryNext = trajEntriesForCurrentUser.get(i + 1).split(Pattern.quote("||"));
	// String nextActivityName = splittedDataEntryNext[1];
	// Long nextActivityDuration = Long.valueOf(splittedDataEntryNext[2]);
	//
	// if (nextActivityName.equalsIgnoreCase(activityNameToMerge) == false)
	// {
	// cleanedMergedDataForCurrentUser.add(trajEntriesForCurrentUser.get(i));
	// continue;
	// }
	//
	// else if (nextActivityName.equalsIgnoreCase(activityNameToMerge)
	// && nextActivityDuration < thresholdForMergingNotAvailables)
	// {
	// long newDuration = currentActivityDuration + nextActivityDuration;
	//
	// cleanedMergedDataForCurrentUser.add(splittedDataEntryCurr[0] + "||" + currentActivityName + "||" + newDuration);
	// i++;
	// continue;
	// }
	//
	// else
	// {
	// cleanedMergedDataForCurrentUser.add(trajEntriesForCurrentUser.get(i));
	// continue;
	// }
	// }
	//
	// else
	// {
	// cleanedMergedDataForCurrentUser.add(trajEntriesForCurrentUser.get(i));
	// }
	//
	// }
	//
	// mapCleanedMerged.put(userName, arrayListToTreeMap(cleanedMergedDataForCurrentUser));
	//
	// }
	//
	// return mapCleanedMerged;
	// }
	//
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
		System.out.println("Exiting createLabelEntryMap()");
		return mapForLabelEntry;
	}

	// public static LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> createAnnotatedTracksMapReducer()
	// {
	//
	// System.out.println("Inside createAnnotatedTracksMapMapReducer");
	//
	// String pathToParse = rawPathToRead + "userid-timestamp-artid-artname-traid-traname.tsv";
	//
	// ArrayList<LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>> allDataChunks =
	// new ArrayList<LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>>();
	// // = new ArrayList<LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();
	//
	// int reducedLineLength = 50000; // 5
	// long numOfLinesToRead = ReadingFromFile.getNumOfLines(pathToParse); // 22 line
	// System.out.println("numOfLinesToRead= " + numOfLinesToRead);
	//
	// long lineCounter = 0;
	//
	// while (lineCounter < numOfLinesToRead) // 0 to 21 lines
	// {
	// if ((lineCounter % reducedLineLength) == 0)
	// {
	// long startLineNum = lineCounter + 1;
	// long endLineNum = Math.min(lineCounter + reducedLineLength, numOfLinesToRead); // 0+5, 22 5+5,22 15,22 20,22
	// 25,22
	// System.out.println("startLineNum=" + startLineNum + " endLineNum=" + endLineNum);
	// allDataChunks.add(createAnnotatedTracksMap(startLineNum, endLineNum, pathToParse)); // 1 to 5, 6 to 10, 11 to 15,
	// 16 to 20, 20 to 11
	// }
	// lineCounter += reducedLineLength; // 0, 5,10,15,20
	//
	// }
	//
	// // LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllData = new LinkedHashMap<String,
	// TreeMap<Timestamp, DataEntry>>();
	// System.out.println("Exiting createAnnotatedTracksMapMapReducer");
	//
	// return consolidateDataChunks(allDataChunks);
	// }

	/**
	 * Merge the maps in the provided ArrayList into one map.
	 * 
	 * @param allDataChunks
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> consolidateDataChunks(
			ArrayList<LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>> allDataChunks)
	{
		LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> consolidatedData = new LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>();
		for (int i = 0; i < allDataChunks.size(); i++)
		{
			consolidatedData.putAll(allDataChunks.get(i));
		}
		return consolidatedData;
	}

	public static LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> createAnnotatedTracksMapReducer2(
			String pathToParse)
	{

		System.out.println("Inside createAnnotatedTracksMapMapReducer");

		ArrayList<LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>> allDataChunks = new ArrayList<LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>>();
		// = new ArrayList<LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();

		long numOfLinesToRead = ReadingFromFile.getNumOfLines(pathToParse); // 22 line
		System.out.println("total numOfLinesToRead= " + numOfLinesToRead);
		long startLineNum = 1;
		long lineNumForNext = -1;
		do
		{
			Pair<Long, LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>> dataChunkReceivedPair = createAnnotatedTracksMap(
					startLineNum, pathToParse);
			lineNumForNext = dataChunkReceivedPair.getFirst();
			allDataChunks.add(dataChunkReceivedPair.getSecond());

			if (lineNumForNext == numOfLinesToRead)
			{
				System.out.println(
						"Sanity check passed in createAnnotatedTracksMapReducer2(): all " + lineNumForNext + " read");
			}
			startLineNum = lineNumForNext;
		}
		while (lineNumForNext != numOfLinesToRead);

		// LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllData = new LinkedHashMap<String,
		// TreeMap<Timestamp, DataEntry>>();
		System.out.println("Exiting createAnnotatedTracksMapMapReducer2");

		return consolidateDataChunks(allDataChunks);
	}

	// public static TreeMap<Timestamp, TrackListenEntry> createAnnotatedTracksMapReducer3(String pathToParse)
	// {
	//
	// System.out.println("Inside createAnnotatedTracksMapMapReducer");
	//
	// ArrayList<LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>> allDataChunks =
	// new ArrayList<LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>>();
	// // = new ArrayList<LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();
	//
	// long numOfLinesToRead = ReadingFromFile.getNumOfLines(pathToParse); // 22 line
	// System.out.println("total numOfLinesToRead= " + numOfLinesToRead);
	// long startLineNum = 1;
	// long lineNumForNext = -1;
	// do
	// {
	// Pair<Long, LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>> dataChunkReceivedPair =
	// createAnnotatedTracksMap(startLineNum, pathToParse);
	// lineNumForNext = dataChunkReceivedPair.getFirst();
	// allDataChunks.add(dataChunkReceivedPair.getSecond());
	//
	// if (lineNumForNext == numOfLinesToRead)
	// {
	// System.out.println("Sanity check passed in createAnnotatedTracksMapReducer2(): all " + lineNumForNext + " read");
	// }
	// startLineNum = lineNumForNext;
	// }
	// while (lineNumForNext != numOfLinesToRead);
	//
	// // LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllData = new LinkedHashMap<String,
	// TreeMap<Timestamp, DataEntry>>();
	// System.out.println("Exiting createAnnotatedTracksMapMapReducer2");
	//
	// return consolidateDataChunks(allDataChunks);
	// }

	/**
	 * Reads the Tracklistening entries from the raw data and returns it as a map.
	 * 
	 * ] * @return LinkedHashMap<user id as String, TreeMap<Timestamp, TrackListenEntry>>
	 */
	/**
	 * Note: line counting starts from 1
	 * 
	 * @param startLine
	 * @param pathToParse
	 * @return Pair<line num for next iteration or num of lines read if end of file reached, LinkedHashMap<String,
	 *         TreeMap<Timestamp, DataEntry>>>
	 */
	public static Pair<Long, LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>> createAnnotatedTracksMap(
			long startLine, String pathToParse)
	{
		long ct1 = System.currentTimeMillis();
		Integer numOfUsersToDo = 100;
		BufferedReader br1 = null;
		System.out.println("createAnnotatedTracksMap called with startLine: " + startLine);

		/// <UserID
		LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForAllData = new LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>();
		String userID, musicbrainzartistid, artistname, musicbrainztrackid, trackname;// timestampString
		String previousUserID = null;
		String splitted[];
		TreeMap<Timestamp, TrackListenEntry> mapForThisUser; // temporary variable
		Timestamp timestamp; // temporary variable
		long countLines = 0; // -1

		StringBuffer warningLog = new StringBuffer();
		int checkNewUserOnlyOnce = 0;
		int numOfDistinctUsers = 1;

		Pattern delimiter = Pattern.compile("\t");
		TrackListenEntry tle = null;
		try
		{
			br1 = new BufferedReader(new FileReader(pathToParse));
			String trackEntryLine = "";

			while ((trackEntryLine = br1.readLine()) != null)// && countLines < 10000000)
			{
				countLines++;

				if (countLines < startLine)
				{
					continue;
				}

				if (countLines != 0 && (countLines % 10000) == 0)
				{
					System.out.println("--Num of lines read = " + countLines);
					// PopUps.showMessage("Num of lines read = " + countLines);
				}

				// splitted = trackEntryLine.split("\t");
				splitted = delimiter.split(trackEntryLine);// Pattern.split is more efficient that String.split// ref:
															// http://chrononsystems.com/blog/hidden-evils-of-javas-stringsplit-and-stringr
				userID = splitted[0];// .trim().replace("user", "").replace("_", "");// user_000001

				if (previousUserID != null && (previousUserID.equals(userID) == false))
				{
					numOfDistinctUsers++;
					if (numOfDistinctUsers > numOfUsersToDo) // only doing 4 users at a time
					{
						break;
						// System.out.println("createAnnotatedTracksMap exited with endline: " + countLines);
						// WritingToFile.appendLineToFileAbsolute(warningLog.toString(), commonPath +
						// "WarningLogs.txt");
						// return new Pair(countLines, mapForAllData);
					}
				}

				// System.out.println("user ID:" + userID);
				// timestampString = ;// .trim();
				timestamp = getTimestampLastFMData(splitted[1]);
				musicbrainzartistid = splitted[2];// .trim();
				artistname = splitted[3];// .trim();
				musicbrainztrackid = splitted[4];// .trim();
				trackname = splitted[5];// .trim();
				tle = new TrackListenEntry(userID, musicbrainzartistid, artistname, musicbrainztrackid, trackname,
						timestamp);

				mapForThisUser = null;//

				if (mapForAllData.containsKey(userID))
				{
					mapForThisUser = mapForAllData.get(userID);

					if (mapForThisUser.containsKey(timestamp))// /hoggr
					{
						warningLog.append("Warning! multiple data entries for user:" + userID + " for ts:" + timestamp
								+ ". will increase ts by 1 sec \n");

						tle.setExtraComments("ts1SecAheadDueToClash");
						timestamp = new Timestamp(timestamp.getTime() + 1000); // addding 1 sec to the original time.
						tle.setTimestamp(timestamp);// changing tle's timestamp

					} // Note: we were expecting only one data entry for each timestamp for each user.s
				}

				else
				{
					mapForThisUser = new TreeMap<Timestamp, TrackListenEntry>();
					checkNewUserOnlyOnce++;
				}
				mapForThisUser.put(timestamp, tle);
				mapForAllData.put(userID, mapForThisUser);

				previousUserID = userID;
			} // end of while reading the file line

			if (checkNewUserOnlyOnce > numOfUsersToDo)
			{
				System.err.println(
						"Error: Saniry check checkNewUserOnlyOnce failed in org.activity.generator.DatabaseCreatorLastFM.createAnnotatedTracksMap(long, String)");
			}
			long ct2 = System.currentTimeMillis();
			System.out.println("Exiting createAnnotatedTracksMap(" + startLine + "," + (countLines - 1) + "): read "
					+ (countLines - startLine) + " lines in " + ((ct2 - ct1) / 1000) + "secs");
		} // end of try
		catch (Exception e)
		{
			e.printStackTrace();
		}

		WritingToFile.appendLineToFileAbsolute(warningLog.toString(), commonPath + "WarningLogs.txt");

		return (new Pair(countLines, mapForAllData));
	}

	/**
	 * Note: line counting starts from 1
	 * 
	 * @param startLine
	 * @param pathToParse
	 * @return Pair<line num for next iteration or num of lines read if end of file reached, LinkedHashMap<String,
	 *         TreeMap<Timestamp, DataEntry>>>
	 */
	public static TreeMap<Timestamp, TrackListenEntry> createAnnotatedTracksMap(String pathToParse)
	{
		long ct1 = System.currentTimeMillis();
		Integer numOfUsersToDo = 100;
		BufferedReader br1 = null;
		System.out.println("createAnnotatedTracksMap called with pathToParse: " + pathToParse);

		TreeMap<Timestamp, TrackListenEntry> mapForThisUser = new TreeMap<Timestamp, TrackListenEntry>();
		String userID, musicbrainzartistid, artistname, musicbrainztrackid, trackname, previousUserID = null;// timestampString
		String splitted[];
		// TreeMap<Timestamp, TrackListenEntry> mapForThisUser; // temporary variable
		Timestamp timestamp; // temporary variable
		long countLines = 0; // -1

		StringBuffer warningLog = new StringBuffer();
		// int checkNewUserOnlyOnce = 0;
		int numOfDistinctUsers = 1;

		Pattern delimiter = Pattern.compile("\t");
		TrackListenEntry tle = null;
		try
		{
			br1 = new BufferedReader(new FileReader(pathToParse));
			String trackEntryLine = "";

			while ((trackEntryLine = br1.readLine()) != null)// && countLines < 10000000)
			{
				countLines++;

				// System.out.println("line read" + trackEntryLine);

				if (countLines != 0 && (countLines % 10000) == 0)
				{
					System.out.println("--Num of lines read = " + countLines);
				}

				splitted = delimiter.split(trackEntryLine);// Pattern.split is more efficient that String.split// ref:
				userID = splitted[0].trim().replace("user", "").replace("_", "");// user_000001

				if (previousUserID != null && (previousUserID.equals(userID) == false)) // juts for sanity check
				{
					numOfDistinctUsers++;
				}

				// System.out.println("user ID:" + userID);
				// timestampString = ;// .trim();
				timestamp = getTimestampLastFMData(splitted[1]);
				musicbrainzartistid = splitted[2];// .trim();
				artistname = splitted[3];// .trim();
				musicbrainztrackid = splitted[4];// .trim();
				trackname = splitted[5];// .trim();
				tle = new TrackListenEntry(userID, musicbrainzartistid, artistname, musicbrainztrackid, trackname,
						timestamp);

				// mapForThisUser = null;//

				if (mapForThisUser.containsKey(timestamp))// note: this can cause some songs to have same 0 secs
															// duration but the advantage is that we wont have any entry
															// lost due
															// to timestamp clash (we can use timestamp as key) and loss
															// of 1 sec should have a signficantly big affect overall
				{
					warningLog.append("Warning! multiple data entries for user:" + userID + " for ts:" + timestamp
							+ ". will increase ts by 1 sec \n");

					tle.setExtraComments("ts1SecAheadDueToClash");
					timestamp = new Timestamp(timestamp.getTime() + 1000); // addding 1 sec to the original time.
					tle.setTimestamp(timestamp);// changing tle's timestamp

				} // Note: we were expecting only one data entry for each timestamp for each user.s

				mapForThisUser.put(timestamp, tle);

				previousUserID = userID;
			} // end of while reading the file line

			if (numOfDistinctUsers > 1)
			{
				System.err.println(
						"Error: Sanity check checkNewUserOnlyOnce failed in DatabaseCreatorLastFM2Quicker2ByUser.createAnnotatedTracksMap(String) numOfDistinctUsers= "
								+ numOfDistinctUsers);
			}
			long ct2 = System.currentTimeMillis();
			System.out.println(" read " + countLines + " lines in " + ((ct2 - ct1) / 1000) + "secs");
		} // end of try
		catch (Exception e)
		{
			e.printStackTrace();
		}

		WritingToFile.appendLineToFileAbsolute(warningLog.toString(), commonPath + "WarningLogs.txt");

		return mapForThisUser;
	}

	/**
	 * Reads the Tracklistening entries from the raw data and returns it as a map.
	 * 
	 * ] * @return LinkedHashMap<user id as String, TreeMap<Timestamp, TrackListenEntry>>
	 */
	/**
	 * Note: line counting starts from 1
	 * 
	 * @param startLine
	 *            inclusive
	 * @param endline
	 *            inclusive
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> createAnnotatedTracksMap(long startLine,
			long endline, String pathToParse)
	{
		BufferedReader br1 = null;

		LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllData = new LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();
		String userID, timestampString, musicbrainzartistid, artistname, musicbrainztrackid, trackname;
		String splitted[];
		TreeMap<Timestamp, DataEntry> mapForThisUser; // temporary variable
		Timestamp timestamp; // temporary variable
		try
		{
			br1 = new BufferedReader(new FileReader(pathToParse));
			String trackEntryLine = "";

			int countLines = 0; // -1
			while ((trackEntryLine = br1.readLine()) != null)// && countLines < 10000000)
			{
				countLines++;

				if (countLines < startLine)
				{
					continue;
				}
				if (countLines > endline)
				{
					break;
				}

				// if (countLines != 0 && (countLines % 1000000) == 0)
				// {
				// System.out.println("Num of lines read = " + countLines);
				// // PopUps.showMessage("Num of lines read = " + countLines);
				// }

				userID = null;
				timestampString = null;
				musicbrainzartistid = null;
				artistname = null;
				musicbrainztrackid = null;
				trackname = null;
				timestamp = null;

				Pattern delimiter = Pattern.compile("\t");
				// splitted = trackEntryLine.split("\t");
				splitted = delimiter.split(trackEntryLine);// Pattern.split is more efficient that String.split// ref:
															// http://chrononsystems.com/blog/hidden-evils-of-javas-stringsplit-and-stringr
				userID = splitted[0].trim().replace("user", "").replace("_", "");// user_000001
				// System.out.println("user ID:" + userID);
				timestampString = splitted[1];// .trim();
				timestamp = getTimestampLastFMData(timestampString);
				musicbrainzartistid = splitted[2];// .trim();
				artistname = splitted[3];// .trim();
				musicbrainztrackid = splitted[4];// .trim();
				trackname = splitted[5];// .trim();
				TrackListenEntry tle = new TrackListenEntry(userID, musicbrainzartistid, artistname, musicbrainztrackid,
						trackname, timestamp);

				mapForThisUser = null;//
				if (mapForAllData.containsKey(userID))
				{
					mapForThisUser = mapForAllData.get(userID);

					if (mapForThisUser.containsKey(timestamp))// /hoggr
					{
						System.err.println("Warning1 ! multiple data entries for user:" + userID + " for ts:"
								+ timestamp + ". will increase ts by 1 sec ");

						tle.setExtraComments("ts1SecAheadDueToClash");
						timestamp = new Timestamp(timestamp.getTime() + 1000); // addding 1 sec to the original time.
						tle.setTimestamp(timestamp);// changing tle's timestamp

					} // Note: we were expecting only one data entry for each timestamp for each user.s
				}

				else
				{
					mapForThisUser = new TreeMap<Timestamp, DataEntry>();
				}
				mapForThisUser.put(timestamp, tle);
				mapForAllData.put(userID, mapForThisUser);
			}
			System.out.println("Exiting createAnnotatedTracksMap(start,end): read " + countLines + " lines.");
		} // end of try
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return mapForAllData;
	}

	/**
	 * Reads the Tracklistening entries from the raw data and returns it as a map.
	 * 
	 * ] * @return LinkedHashMap<user id as String, TreeMap<Timestamp, TrackListenEntry>>
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> createAnnotatedTracksMap()
	{
		String pathToParse = rawPathToRead + "userid-timestamp-artid-artname-traid-traname.tsv";
		BufferedReader br1 = null;

		LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllData = new LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();
		String userID, timestampString, musicbrainzartistid, artistname, musicbrainztrackid, trackname;
		String splitted[];
		TreeMap<Timestamp, DataEntry> mapForThisUser; // temporary variable
		Timestamp timestamp; // temporary variable
		try
		{
			br1 = new BufferedReader(new FileReader(pathToParse));
			String trackEntryLine = "";

			int countLines = -1;
			while ((trackEntryLine = br1.readLine()) != null)// && countLines < 10000000)
			{
				countLines++;

				if (countLines != 0 && (countLines % 1000000) == 0)
				{
					System.out.println("Num of lines read = " + countLines);
					// PopUps.showMessage("Num of lines read = " + countLines);
				}

				userID = null;
				timestampString = null;
				musicbrainzartistid = null;
				artistname = null;
				musicbrainztrackid = null;
				trackname = null;
				timestamp = null;

				Pattern delimiter = Pattern.compile("\t");
				// splitted = trackEntryLine.split("\t");
				splitted = delimiter.split(trackEntryLine);// Pattern.split is more efficient that String.split// ref:
															// http://chrononsystems.com/blog/hidden-evils-of-javas-stringsplit-and-stringr
				userID = splitted[0].trim().replace("user", "").replace("_", "");// user_000001
				// System.out.println("user ID:" + userID);
				timestampString = splitted[1];// .trim();
				timestamp = getTimestampLastFMData(timestampString);
				musicbrainzartistid = splitted[2];// .trim();
				artistname = splitted[3];// .trim();
				musicbrainztrackid = splitted[4];// .trim();
				trackname = splitted[5];// .trim();
				TrackListenEntry tle = new TrackListenEntry(userID, musicbrainzartistid, artistname, musicbrainztrackid,
						trackname, timestamp);

				mapForThisUser = null;//
				if (mapForAllData.containsKey(userID))
				{
					mapForThisUser = mapForAllData.get(userID);

					if (mapForThisUser.containsKey(timestamp))// /hoggr
					{
						System.err.println("Warning1 ! multiple data entries for user:" + userID + " for ts:"
								+ timestamp + ". will increase ts by 1 sec ");

						tle.setExtraComments("ts1SecAheadDueToClash");
						timestamp = new Timestamp(timestamp.getTime() + 1000); // addding 1 sec to the original time.
						tle.setTimestamp(timestamp);// changing tle's timestamp

					} // Note: we were expecting only one data entry for each timestamp for each user.s
				}

				else
				{
					mapForThisUser = new TreeMap<Timestamp, DataEntry>();
				}
				mapForThisUser.put(timestamp, tle);
				mapForAllData.put(userID, mapForThisUser);
			}
			System.out.println("Exiting createAnnotatedTracksMap(): read " + countLines + " lines.");
		} // end of try
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return mapForAllData;
	}

	// for (String userID : userIDs)
	// {
	// TreeMap<Timestamp, TrajectoryEntry> mapEachUser = new TreeMap<Timestamp, TrajectoryEntry>();
	// // System.out.println("creating annotated trajectory entry for user="+userID);
	// TreeMap<String, String> modesForAllTrajectoryFilesPerUser = new TreeMap<String, String>();
	// TrajectoryEntry.clearCountNegativesZerosInvalids(); // clear the counts, we will count for each specific user.
	//
	// String folderToLook = pathToParse + userID + "/Trajectory";
	// // check if labels.txt is there in the folder
	// File file = new File(folderToLook);
	//
	// // //
	// // // System.out.println("For User: " + userID + " traj files to be read are:");
	// // int fcount = 0;
	// // for (File fileEntry : file.listFiles())
	// // {
	// // String splittedFN[] = fileEntry.toString().split("/");
	// // String fn = splittedFN[splittedFN.length - 1];
	// // System.out.println("--" + (++fcount) + ". " + fn);
	// // }
	// // //
	// // reading trajectory entries from .PLT files
	// for (File fileEntry : file.listFiles()) // /for each trajectory file. Here each .plt file is one trajectory
	// {
	// // System.out.println("Reading fileEntry:" + fileEntry.toString());
	//
	// String trajectoryID = userID + "-" + fileEntry.getName().substring(0, fileEntry.getName().length() - 4); //
	// usedID__filename(without.plt)
	// // System.out.println("trajectoryID:" + trajectoryID);
	// br1 = new BufferedReader(new FileReader(fileEntry));
	//
	// String trajectoryEntryLine;
	//
	// int count = -1;
	// HashSet<String> modesForThisTrajectoryFile = new HashSet<String>(); // the prime purpose for this is to check
	// whether all entries in a trajectory file have
	// // the
	// // same mode.
	//
	// while ((trajectoryEntryLine = br1.readLine()) != null)
	// {
	// count++;
	// if (count <= 5) // skip the first 6 lines
	// continue;
	// // System.out.println(trajectoryEntryLine);
	// String entryArr[] = trajectoryEntryLine.split(",");
	//
	// String latitude = new String(entryArr[0]);// Double.parseDouble(entryArr[0]);
	// String longitude = new String(entryArr[1]);// Double.parseDouble(entryArr[1]),
	// String altitude = new String(entryArr[3]);// Double.parseDouble(entryArr[3]);
	//
	// Timestamp timeStamp = getTimestampGeoData(entryArr[5], entryArr[6]);
	// String mode = getModeForTrajectoryEntry(userID, timeStamp);
	// modesForThisTrajectoryFile.add(mode);
	//
	// TrajectoryEntry te = new TrajectoryEntry(latitude, longitude, altitude, timeStamp, mode, trajectoryID);
	// mapEachUser.put(timeStamp, te);
	//
	// // for debugging only Start
	// // if (trajectoryID.equals("128-20090329013106") || trajectoryID.equals("128-20090329025153"))
	// // {
	// // System.out.println(" line read: " + trajectoryEntryLine);
	// // System.out.println(" parsed line: line# " + count + " parsed timestamp = " + entryArr[5] + " " + entryArr[6]
	// // + " created timestamp = " + timeStamp.toGMTString());
	// //
	// // System.out.println(" stored: te " + te.toStringWithTrajID());
	// // }
	// // for debugging only End
	// // System.out.println(" User: " + userID + " traj file:" + fileEntry.getName() + " has " + count + " lines");
	// // System.out.println(">>"+latitude+" "+longitude+">>"+timeStamp+">>"+mode+"\n");
	// }
	//
	// // System.out.println(" User: " + userID + " traj file:" + fileEntry.getName() + " has " + count + " lines");
	// // if (br1 != null)
	// // {
	// br1.close();
	// // }
	// modesForAllTrajectoryFilesPerUser.put(fileEntry.getName(), modesForThisTrajectoryFile.size() + ","
	// + modesForThisTrajectoryFile.toString());
	// }// end of loop over trajectory files.
	//
	// modesForAllTrajectoryFilesAllUsers.put(userID, modesForAllTrajectoryFilesPerUser);
	//
	// WritingToFile.writeNegativeZeroInvalidsLatLonAlt(userID, "CountOfNegativeZeroUnknownAltLatLon");
	//
	// mapForAllData.put(userID, mapEachUser);
	// // System.out.println("putting maps of user:" + userID + " of size:" + mapEachUser.size());
	// System.out.println("put, User:" + userID + ",#TrajectoryEntries:" + mapEachUser.size());
	// }// end of loop over users
	//
	// WritingToFile.writeLinkedHashMapOfTreemapAllString(modesForAllTrajectoryFilesAllUsers, "ModesPerTrajectoryFiles",
	// "UserID,TrajectoryFile, NumberOfModes,Modes");
	// WritingToFile.writeNegativeZeroInvalidsLatLonAltFooter("CountOfNegativeZeroUnknownAltLatLon");
	// // WritingToFile.writeLinkedHashMapOfTreemapPureTrajectoryEntries(mapForAllData,
	// //
	// "AllDataWithAnnotation","UserID,Timestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount");
	//
	// System.out.println("\nSize of mapForAllData:" + mapForAllData.size());

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
	 * writeInStats("\n Number of jpg files in "+ userName+"_"+activityName+ ".txt = "+ numberOfEntriesNonEmpty);
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
	public static Timestamp getTimestampLastFMData(String timestampString)// , String timeString)
	{
		Timestamp timeStamp = null;
		try
		{
			Instant instant = Instant.parse(timestampString);
			timeStamp = Timestamp.from(instant);
		}
		catch (Exception e)
		{
			System.out.println("Exception " + e + " thrown for getting timestamp from " + timestampString);
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

			if (hours != timeStamp.getHours())
			{
				System.err.println("Alert TS1 in getTimestampGeoData: hours not equal:\nReceived dateString= "
						+ dateString + " timeString= " + timeString + "\n\tParsed hour:" + hours
						+ "\n\tCreated timestamp (toGMTStrng()): " + timeStamp.toGMTString()
						+ "\n\tCreated timestamp (toStrng()): " + timeStamp.toString());
			}
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

	/**
	 * Writes those traj entryies which have same traj id and mode of transport as the immediately preceeding traj entry
	 * 
	 * @param data
	 * @param filenameEndPhrase
	 * @param headers
	 * @param printHeaders
	 */
	public static void checkConsecutiveSameActivityNameTrajSensitive(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> data, String absfilename)
	{
		BufferedWriter bwConsecutiveSimilar = WritingToFile.getBufferedWriterForNewFile(absfilename);
		String toWrite = "User,TrajID,TimestampWhichIsSimilarToPrev,Mode\n";
		for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : data.entrySet())
		{
			try
			{
				String userName = entryForUser.getKey();
				// TrajectoryEntry previousTrajEntry =null;
				String previousActivityName = "", currentActivityName = "";
				String previousTrajID = "", currentTrajID = "";

				int count = 0;
				for (Map.Entry<Timestamp, TrajectoryEntry> entry : entryForUser.getValue().entrySet())
				{
					if (count == 0)// first eement
					{
						// previousTrajEntry = entry.getValue();
						previousActivityName = entry.getValue().getMode();
						previousTrajID = entry.getValue().getDistinctTrajectoryIDs("__");
						++count;
						continue;
					}
					currentActivityName = entry.getValue().getMode();
					currentTrajID = entry.getValue().getDistinctTrajectoryIDs("__");

					if (currentActivityName.equals(previousActivityName) && currentTrajID.equals(previousTrajID))
					{
						toWrite += userName + "," + currentTrajID + "," + entry.getValue().getTimestamp().toGMTString()
								+ "," + currentActivityName + "\n";
					}

					count++;
				}
				bwConsecutiveSimilar.append(toWrite);

				// bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		try
		{
			bwConsecutiveSimilar.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // "trajID,timestamp,
			// endt,mode,latitude,longitude,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount",

	}

	/**
	 * Find sandwiches A-O-A, with O of duration <= sandwichFillerDurationInSecs
	 * 
	 * @param data
	 * @param sandwichFillerDurationInSecs
	 * @param absfilename
	 */
	public static void findSandwichedTrajEntriesTrajSensitive(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> data, int sandwichFillerDurationInSecs,
			String absfilename)
	{
		BufferedWriter bwConsecutiveSimilar = WritingToFile.getBufferedWriterForNewFile(absfilename);
		String toWrite = "User,TrajID,StartTime,Mode,Duration,SanwichIndexIndex\n";
		for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : data.entrySet())
		{
			try
			{
				String userName = entryForUser.getKey();
				TrajectoryEntry firstTrajEntry = null, middleTrajEntry = null, currentTrajEntry = null;
				;
				String firstActivityName = "", middleActivityName = "", currentActivityName = "";
				String firstTrajID = "", middleTrajID = "", currentTrajID = "";

				int count = 0;
				for (Map.Entry<Timestamp, TrajectoryEntry> entry : entryForUser.getValue().entrySet())
				{
					if (count == 0)// first eement
					{
						firstTrajEntry = entry.getValue();
						firstActivityName = firstTrajEntry.getMode();
						firstTrajID = firstTrajEntry.getDistinctTrajectoryIDs("__");
						++count;
						continue;
					}

					else if (count == 1)// second eement
					{
						middleTrajEntry = entry.getValue();
						middleActivityName = middleTrajEntry.getMode();
						middleTrajID = middleTrajEntry.getDistinctTrajectoryIDs("__");
						++count;
						continue;
					}
					currentTrajEntry = entry.getValue();
					currentActivityName = currentTrajEntry.getMode();
					currentTrajID = currentTrajEntry.getDistinctTrajectoryIDs("__");

					if (currentActivityName.equals(firstActivityName) && currentTrajID.equals(firstTrajID)
							&& (middleTrajEntry.getDurationInSeconds() <= sandwichFillerDurationInSecs))
					{
						toWrite += userName + "," + currentTrajID + "," + firstTrajEntry.getTimestamp().toGMTString()
								+ "," + firstActivityName + "," + firstTrajEntry.getDurationInSeconds() + "1\n";
						toWrite += userName + "," + middleTrajID + "," + middleTrajEntry.getTimestamp().toGMTString()
								+ "," + middleActivityName + "," + middleTrajEntry.getDurationInSeconds() + "2\n";

						toWrite += userName + "," + currentTrajID + "," + currentTrajEntry.getTimestamp().toGMTString()
								+ "," + currentActivityName + "," + currentTrajEntry.getDurationInSeconds() + "3\n";
					}

					count++;
				}
				bwConsecutiveSimilar.append(toWrite);

				// bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			bwConsecutiveSimilar.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// "trajID,timestamp,
		// endt,mode,latitude,longitude,timedifferenceWithNextInSeconds,durationInSeconds,breakOverDaysCount",

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
						WritingToFile.appendLineToFileAbsolute("User:" + userName + "," + msg + "\n",
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
						WritingToFile
								.appendLineToFileAbsolute(
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
				BufferedWriter bwMergerCaseLogs = WritingToFile
						.getBufferedWriterForNewFile(commonPath + userID + "MergerCasesLog.csv");
				bwMergerCaseLogs.write("TrajId,Case,Mode,DurationInSecs,CurrentTS, NextTS,Comment\n");

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
					String trajectoryID = trajEntriesForCurrentUser.get(i).getDistinctTrajectoryIDs("__");
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
							bwMergerCaseLogs.write(trajectoryID + ",CaseA," + currentModeName + "," + durationInSeconds
									+ "," + currentTimestamp + "," + nextTimestamp + ",merged as continuous\n");
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
								bwMergerCaseLogs.write(trajectoryID + ",CaseB," + currentModeName + ","
										+ durationInSeconds + "," + currentTimestamp + "," + nextTimestamp + ","
										+ diffCurrentAndNextInSec + "<=" + assumeContinuesBeforeNextInSecs
										+ " secsItContinuesBeforeNext =" + secsItContinuesBeforeNext + "\n");
							}

							else
							{
								// System.out.println("\n\t For user: "+userID+", at
								// currentTimestamp="+currentTimestamp+", currentModeName="+currentModeName);
								numOfTrajCaseC += 1;
								secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;

								// ##bwMergerCaseLogs.write("CaseC: diffCurrentAndNextDifferentInSec (" +
								// diffCurrentAndNextInSec + ") >"+ assumeContinuesBeforeNextInSecs + "\n");
								bwMergerCaseLogs.write(
										trajectoryID + ",CaseC," + currentModeName + "," + durationInSeconds + ","
												+ currentTimestamp + "," + nextTimestamp + "," + diffCurrentAndNextInSec
												+ ">" + assumeContinuesBeforeNextInSecs + " secsItContinuesBeforeNext ="
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

			WritingToFile.writeLinkedHashMapOfTreemap2(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
					"User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeContinuousTrajectoriesAssignDurationWithoutBOD2()");
		}
		return mapForAllDataMergedPlusDuration;
	}

	// /////////////////////////////////////////////////
	/**
	 * For cases like M1-activityNameToMerge-M2: if the duration of activityNameToMerge <
	 * thresholdForMergingNotAvailables, and M1 and activityNameToMerge are of same trajID then, activityNameToMerge is
	 * merged with M1. Note: currently this is applied for "Not Available" as activityNameToMerge
	 * 
	 * @param mapForAllData
	 * @param activityNameToMerge
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mergeCleanSmallNotAvailableTrajSensitive(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllData, String activityNameToMerge)
	{
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();
		/*
		 * Note: using TreeMap is IMPORTANT here, because TreeMap will automatically sort by the timestamp, so we do not
		 * need to be concerned about whether we add theactivities in correct order or not, if the timestamps are right,
		 * it will be stored correctly
		 */
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();

		System.out.println("mergeCleanSmallNotAvailableTrajSensitive for: " + activityNameToMerge);
		try
		{
			for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : mapForAllData.entrySet())
			{
				String userID = entryForUser.getKey();
				BufferedWriter bwMergerCaseLogs = WritingToFile
						.getBufferedWriterForNewFile(commonPath + userID + "MergerCleanNotAvailableCasesLog.csv");
				bwMergerCaseLogs
						.write("TrajId,Case,CurrentMode,NextMode,NextDurationInSecs,CurrentTS, NextTS,Comment\n");

				System.out.println("\nUser =" + userID);

				int numOfTrajCaseA = 0/* , numOfTrajCaseB = 0, numOfTrajCaseC = 0, */, numOfLastTrajEntries = 0;

				int countOfContinuousMerged = 1;

				TreeMap<Timestamp, TrajectoryEntry> mapCleanedMerged = new TreeMap<Timestamp, TrajectoryEntry>();

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
					String trajectoryID = trajEntriesForCurrentUser.get(i).getDistinctTrajectoryIDs("__");
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

						// If the next activity is "Not Available", and it belongs to same trajID as current trajID and
						// its timestamp is less than thresholdForMergingNotAvailables
						// away from current timestamp, then merge it with the current activity.
						if (nextModeName.equals(activityNameToMerge)
								&& (((nextTimestamp.getTime() - currentTimestamp.getTime())
										/ 1000) < thresholdForMergingNotAvailables) // areContinuous(currentTimestamp,
																					// nextTimestamp)
								&& currentTrajID.equals(nextTrajectoryIDs))
						{
							numOfTrajCaseA += 1;
							durationInSeconds += (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;

							// timeDiffWithNextInSeconds =
							// trajEntriesForCurrentUser.get(i).getDifferenceWithNextInSeconds()
							// + trajEntriesForCurrentUser.get(i + 1).getDifferenceWithNextInSeconds(); // TODO CHECK IF
							// NOT NEEDED

							// newLati.addAll(currentLat);
							// newLongi.addAll(currentLon);
							// newAlti.addAll(currentAlt);

							// newLati.addAll(nextLat);
							// newLongi.addAll(nextLon);
							// newAlti.addAll(nextAlt);

							countOfContinuousMerged++;
							// ##bwMergerCaseLogs.write("CaseA: Continuous merged for mode=" + currentModeName + "
							// durationInSeconds="+ durationInSeconds + "\n");
							bwMergerCaseLogs.write(trajectoryID + ",CaseA," + currentModeName + "," + nextModeName + ","
									+ durationInSeconds + "," + currentTimestamp + "," + nextTimestamp + ",merged "
									+ activityNameToMerge + " of "
									+ ((nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000) + "secs duration"
									+ " at " + nextTimestamp + "\n");
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

							// if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these
							// were different activity names
							// {
							// numOfTrajCaseB += 1;
							secsItContinuesBeforeNext = diffCurrentAndNextInSec;
							// ##bwMergerCaseLogs.write("CaseB: diffCurrentAndNextInSec <=
							// assumeContinuesBeforeNextInSecs, secsItContinuesBeforeNext=" +
							// secsItContinuesBeforeNext + "\n");
							// bwMergerCaseLogs.write(trajectoryID + ",CaseB," + currentModeName + "," +
							// durationInSeconds + ","
							// + currentTimestamp + "," + nextTimestamp + "," + diffCurrentAndNextInSec + "<="
							// + assumeContinuesBeforeNextInSecs + " secsItContinuesBeforeNext =" +
							// secsItContinuesBeforeNext + "\n");
							// }

							// else
							// {
							// // System.out.println("\n\t For user: "+userID+", at
							// currentTimestamp="+currentTimestamp+", currentModeName="+currentModeName);
							// numOfTrajCaseC += 1;
							// secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;
							//
							// // ##bwMergerCaseLogs.write("CaseC: diffCurrentAndNextDifferentInSec (" +
							// diffCurrentAndNextInSec + ") >"+ assumeContinuesBeforeNextInSecs + "\n");
							// bwMergerCaseLogs.write(trajectoryID + ",CaseC," + currentModeName + "," +
							// durationInSeconds + ","
							// + currentTimestamp + "," + nextTimestamp + "," + diffCurrentAndNextInSec + ">"
							// + assumeContinuesBeforeNextInSecs + " secsItContinuesBeforeNext =" +
							// secsItContinuesBeforeNext
							// + " put new Unknown\n");
							//
							// /* Put the new 'Unknown' entry///////////////// */
							// long durationForNewUnknownActivity = diffCurrentAndNextInSec -
							// assumeContinuesBeforeNextInSecs;
							// Timestamp startOfNewUnknown =
							// new Timestamp(currentTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));
							//
							// // unknownsInsertedWholes.put(startOfNewUnknown, new TrajectoryEntry(startOfNewUnknown,
							// durationForNewUnknownActivity,"Unknown")); //
							// // String.valueOf(durationForNewUnknownActivity));
							//
							// TrajectoryEntry te = new TrajectoryEntry(startOfNewUnknown,
							// durationForNewUnknownActivity, "Unknown");// ,bodCount);
							// mapCleanedMerged.put(startOfNewUnknown, te);
							// unknownsInsertedWholes.put(startOfNewUnknown, te);
							// // $$System.out.println("Added Trajectory Entry: "+te.toString());
							// }

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

							mapCleanedMerged.put(startTimestamp, te);
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

						mapCleanedMerged.put(startTimestamp, te);

						// $$System.out.println("Added Trajectory Entry: "+te.toString());

						// newLati.clear();newLongi.clear();newAlti.clear();
						// //////////////////////
						// /*REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
						// currentActivityName+"||"+durationInSeconds); */
						// durationInSeconds=0;
					}

					// $$System.out.println("Clearing variables");//
					durationInSeconds = 0;
					// timeDiffWithNextInSeconds = 0;
					newLati.clear();
					newLongi.clear();
					newAlti.clear();
					newTrajID.clear();
				} // end of for loop over trajectory entries for current user.

				mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapCleanedMerged);
				// mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);

				bwMergerCaseLogs.write("User:" + userID + ",numOfTrajCaseA = " + numOfTrajCaseA // + ",numOfTrajCaseB =
																								// " + numOfTrajCaseB
				// + ",numOfTrajCaseC = " + numOfTrajCaseC
						+ " ,numOfLastTrajEntries = " + numOfLastTrajEntries);
				bwMergerCaseLogs.close();
			} // end of for loop over users

			// WritingToFile.writeLinkedHashMapOfTreemap2(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
			// "User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeCleanSmallNotAvailableTrajSensitive()");
		}
		return mapForAllDataMergedPlusDuration;
	}

	/**
	 * For cases like M1-activityNameToMerge-M1: if the duration of activityNameToMerge < thresholdForMergingSandwiches,
	 * and all three of same trajID then, activityNameToMerge is merged with M1. Note: currently this is applied for
	 * "Not Available" as activityNameToMerge . And writes sandwiches logs to "MergerSandwichesLog.csv"s
	 * 
	 * @param mapForAllData
	 * @param activityNameToMerge
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mergeSmallSandwichedTrajSensitive(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllData, String activityNameToMerge)
	{
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();
		/*
		 * Note: using TreeMap is IMPORTANT here, because TreeMap will automatically sort by the timestamp, so we do not
		 * need to be concerned about whether we add theactivities in correct order or not, if the timestamps are right,
		 * it will be stored correctly
		 */
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllUnknownsWholes = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();

		System.out.println("mergeSmallSandwichedTrajSensitive for: " + activityNameToMerge);
		try
		{
			for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entryForUser : mapForAllData.entrySet())
			{
				String userID = entryForUser.getKey();
				BufferedWriter bwMergerCaseLogs = WritingToFile.getBufferedWriterForNewFile(
						commonPath + userID + activityNameToMerge + "MergerSandwichesLog.csv");
				bwMergerCaseLogs.write(
						"TrajId,CurrentMode,NextMode,NextToNextMode,CurrentTS, NextTS,NextToNextTS,DurationOfNext,TimestampDifferenceForDuration\n");

				// System.out.println("\nUser =" + userID);

				int numOfSandwiches = 0/* , numOfTrajCaseB = 0, numOfTrajCaseC = 0, */, numOfLastTrajEntries = 0;

				// int countOfContinuousMerged = 1;

				TreeMap<Timestamp, TrajectoryEntry> mapCleanedMerged = new TreeMap<Timestamp, TrajectoryEntry>();

				long newDurationInSeconds = 0;
				ArrayList<String> newLati = new ArrayList<String>(), newLongi = new ArrayList<String>(),
						newAlti = new ArrayList<String>();
				ArrayList<String> newTrajID = new ArrayList<String>();
				TrajectoryEntry currentTE, nextTE, nextToNextTE;

				long timeDiffWithNextInSeconds = 0; // do not delete. // not directly relevant
				Timestamp startTimestamp;

				ArrayList<TrajectoryEntry> trajEntriesForCurrentUser = UtilityBelt
						.treeMapToArrayListGeo(entryForUser.getValue());

				for (int i = 0; i < trajEntriesForCurrentUser.size();)
				{
					// startTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));
					// ##
					// $$System.out.println("\nReading: "+dataForCurrentUser.get(i).toString());
					currentTE = trajEntriesForCurrentUser.get(i);
					String trajectoryID = currentTE.getDistinctTrajectoryIDs("__");
					Timestamp currentTimestamp = currentTE.getTimestamp();
					String currentModeName = currentTE.getMode();

					if (i <= trajEntriesForCurrentUser.size() - 3) // is not the last element of arraylist
					{
						// the filler
						nextTE = trajEntriesForCurrentUser.get(i + 1);
						Timestamp nextTimestamp = nextTE.getTimestamp();
						String nextModeName = nextTE.getMode();
						ArrayList<String> nextTrajectoryIDs = nextTE.getTrajectoryID();

						// the top layer
						nextToNextTE = trajEntriesForCurrentUser.get(i + 2);
						Timestamp nextNextTimestamp = nextToNextTE.getTimestamp();
						String nextNextModeName = nextToNextTE.getMode();
						ArrayList<String> nextNextTrajectoryIDs = nextToNextTE.getTrajectoryID();

						if ((IsValid(currentModeName)) && (nextModeName.equals(activityNameToMerge))
								&& (currentModeName.equals(nextNextModeName))
								&& (((nextNextTimestamp.getTime() - nextTimestamp.getTime())
										/ 1000) < thresholdForMergingSandwiches)
								&& belongToSameTrajectoryID(currentTE, nextTE, nextToNextTE))
						{
							numOfSandwiches += 1;
							// / durationInSeconds += (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;
							Timestamp tsOfAONextToThisSandwich = trajEntriesForCurrentUser.get(i + 3).getTimestamp();
							newDurationInSeconds = (tsOfAONextToThisSandwich.getTime() - currentTimestamp.getTime())
									/ 1000;

							newLati.clear();
							newLongi.clear();
							newAlti.clear();
							newTrajID.clear();

							newLati.addAll(currentTE.getLatitude());
							newLongi.addAll(currentTE.getLongitude());
							newAlti.addAll(currentTE.getAltitude());
							newTrajID.addAll(currentTE.getTrajectoryID());

							newLati.addAll(nextTE.getLatitude());
							newLongi.addAll(nextTE.getLongitude());
							newAlti.addAll(nextTE.getAltitude());
							newTrajID.addAll(nextTE.getTrajectoryID());

							newLati.addAll(nextToNextTE.getLatitude());
							newLongi.addAll(nextToNextTE.getLongitude());
							newAlti.addAll(nextToNextTE.getAltitude());
							newTrajID.addAll(nextToNextTE.getTrajectoryID());

							// currentTE
							// TrajectoryEntry te = trajEntriesForCurrentUser.get(i);
							currentTE.setLatitude(newLati); // note: has to be done with set,..cant do with add becasue
															// it will create problems if more than two entries are
															// merged
							currentTE.setLongitude(newLongi);
							currentTE.setAltitude(newAlti);
							currentTE.setTrajectoryID(newTrajID);

							currentTE.setTimestamp(currentTimestamp);
							currentTE.setDurationInSeconds(newDurationInSeconds);
							// te.setDifferenceWithNextInSeconds(timeDiffWithNextInSeconds);

							mapCleanedMerged.put(currentTimestamp, currentTE);

							bwMergerCaseLogs.write(trajectoryID + "," + currentModeName + "," + nextModeName + ","
									+ nextNextModeName + "," + currentTimestamp + "," + nextTimestamp + ","
									+ nextNextTimestamp + "," + nextTE.getDurationInSeconds() + ","
									+ ((nextNextTimestamp.getTime() - nextTimestamp.getTime()) / 1000) + "\n");
							// "TrajId,CurrentMode,NextMode,NextToNextMode,CurrentTS,
							// NextTS,NextToNextTS,DurationOfNext,TimestampDifferenceForDurationComment\n");
							i += 3;
							continue;
						}
						else
						{
							mapCleanedMerged.put(currentTimestamp, currentTE);
							i++;
						}

					}
					else
					// is the second last or last element
					{
						mapCleanedMerged.put(currentTimestamp, currentTE);
						i++;
					}
				} // end of for loop over trajectory entries for current user.

				mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapCleanedMerged);
				bwMergerCaseLogs.close();
			} // end of for loop over users

			// WritingToFile.writeLinkedHashMapOfTreemap2(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
			// "User,Timestamp,DurationInSecs");
		}
		catch (Exception e)
		{
			PopUps.showException(e, "mergeSmallSandwichedTrajSensitive()");
		}
		return mapForAllDataMergedPlusDuration;
	}

	/**
	 * ALERT: dataset specific TODO
	 * 
	 * @param currentModeName
	 * @return
	 */
	private static boolean IsValid(String currentModeName)
	{
		if (currentModeName.equals("Not Available") || currentModeName.equals("Unknown"))
			return false;
		else
			return true;
	}

	private static boolean belongToSameTrajectoryID(TrajectoryEntry currentTE, TrajectoryEntry nextTE,
			TrajectoryEntry nextToNextTE)
	{
		// boolean flag = false;

		String currentTIDs = currentTE.getDistinctTrajectoryIDs("__");
		String nextTIDs = nextTE.getDistinctTrajectoryIDs("__");
		String nextNextTIDs = nextToNextTE.getDistinctTrajectoryIDs("__");

		if (currentTIDs.equals(nextTIDs) && currentTIDs.equals(nextNextTIDs) && nextTIDs.equals(nextNextTIDs))
			return true;
		else
			return false;
	}
}
