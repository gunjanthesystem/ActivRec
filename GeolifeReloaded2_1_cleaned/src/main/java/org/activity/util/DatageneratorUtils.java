package org.activity.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.activity.objects.CheckinEntry;

/***
 * Contains some utilities method which could be reused.**
 * 
 * @since 16 Feb 2016 12:21am
 * @author gunjan
 *
 */
public class DatageneratorUtils
{
	// static int assumeContinuousThresholdInSeconds = 60 * 10;
	// static int assumeContinuousThresholdInMeters = 600;

	/**
	 * Merge checkins based on the given thresholds
	 * <p>
	 * <font color = orange>Note: distanceFromPrev and durationFromPrev for the merged checkin is considered to be the
	 * corresponding values for the first checkin.
	 * <p>
	 * This results in following: if a checkin which is a merger of 3 checkins is followed by a single unmerged checkin.
	 * Then the distanceFromPrev and durationFromPrev for the merged checkin is the corresponding values for the first
	 * merged checkin while distanceFromPrev and durationFromPrev for the following unmerged checkin is the values
	 * corresponding to the distance and difference of this unmerged checking from the last (3rd) of the merged checkin.
	 * </font>
	 * 
	 * 
	 * @param mapForAllData
	 * @param pathToWrite
	 * @param assumeContinuousThresholdInSeconds
	 * @param assumeContinuousThresholdInMeters
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mergeContinuousGowallaWithoutBOD4(
			LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllData, String pathToWrite,
			int assumeContinuousThresholdInSeconds, int assumeContinuousThresholdInMeters)
	{
		String commonPath = pathToWrite;
		LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForDataMerged = new LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>();

		System.out.println("mergeContinuousGowallaWithoutBOD4 called with assumeContinuousThresholdInSeconds = "
				+ assumeContinuousThresholdInSeconds + " and assumeContinuousThresholdInMeters = "
				+ assumeContinuousThresholdInMeters);

		long numOfConsecActNameSame = 0, numOfConsecActNameSameMerged = 0;
		// StringBuilder sbMergerCaseLogs = new StringBuilder();
		// BufferedWriter bwMergerCaseLogs = WritingToFile
		// .getBufferedWriterForNewFile(commonPath + userID + "MergerCasesLog.csv");

		/*
		 * Note: using TreeMap is IMPORTANT here, because TreeMap will automatically sort by the timestamp, so we do not
		 * need to be concerned about whether we add the activities in correct order or not, if the timestamps are
		 * right, it will be stored correctly
		 */
		System.out.println("Merging continuous without BOD");
		try
		{
			// sbMergerCaseLogs.append("User,Case,ActName,CurrentTS, NextTS,Comment\n");
			for (Map.Entry<String, TreeMap<Timestamp, CheckinEntry>> entryForUser : mapForAllData.entrySet())
			{
				String userID = entryForUser.getKey();
				// $$System.out.println("\nUser =" + userID);

				// int numOfTrajCaseA = 0, numOfTrajCaseB = 0, numOfTrajCaseC = 0, numOfLastTrajEntries = 0;
				// int numOfPlaceIDMerger
				int countOfContinuousMerged = 1;

				TreeMap<Timestamp, CheckinEntry> continuousMergedForThisUser = new TreeMap<Timestamp, CheckinEntry>();

				CheckinEntry previousCheckinEntry = null; // should be null before starting for each user
				ArrayList<CheckinEntry> checkinsToMerge = new ArrayList<CheckinEntry>(); // accumulated checkins to
																							// merge into one

				int countOfCheckins = 0;
				for (Entry<Timestamp, CheckinEntry> checkinEntries : entryForUser.getValue().entrySet())
				{
					countOfCheckins += 1;
					CheckinEntry currentCheckinEntry = checkinEntries.getValue();
					// $$System.out.println(
					// $$ "Reading checkin " + countOfCheckins + " " + currentCheckinEntry.toStringWithoutHeaders());

					if (countOfCheckins == 1)
					{
						previousCheckinEntry = checkinEntries.getValue();
						checkinsToMerge.add(previousCheckinEntry);
						// $$System.out.println(
						// $$ "first checkin: continuing, checkinsToMerge.size() = " + checkinsToMerge.size());
						continue;
					}
					else
					{
						if (currentCheckinEntry.getActivityID() == previousCheckinEntry.getActivityID())
						{
							numOfConsecActNameSame += 1;
						}

						// CheckinEntry currentCheckinEntry = checkinEntries.getValue();

						// should this be merged with previous checkin
						if (currentCheckinEntry.getDurationInSecsFromPrev() <= assumeContinuousThresholdInSeconds
								&& currentCheckinEntry
										.getDistanceInMetersFromPrev() <= assumeContinuousThresholdInMeters
								&& (currentCheckinEntry.getActivityID() == previousCheckinEntry.getActivityID()))
						{
							// merge
							numOfConsecActNameSameMerged += 1;
							checkinsToMerge.add(currentCheckinEntry);
							// $$System.out.println("add to merge, checkinsToMerge.size() = " + checkinsToMerge.size());
						}
						else
						{
							// $$System.out.println("not same, checkinsToMerge.size() = " + checkinsToMerge.size());
							// $$System.out.println("merge previous ones and add curr to merge = " +
							// checkinsToMerge.size());

							// merge the previously accumulated checkins, put in result map
							CheckinEntry mergedCheckinEntry = mergeCheckins(checkinsToMerge);
							continuousMergedForThisUser.put(mergedCheckinEntry.getTimestamp(), mergedCheckinEntry);
							checkinsToMerge.clear();
							// add current checkin to merge
							checkinsToMerge.add(currentCheckinEntry);
							// $$System.out.println("checkinsToMerge.size() = " + checkinsToMerge.size());
						}
						previousCheckinEntry = currentCheckinEntry;
					}
				} // end of loop over checkins for this user
				mapForDataMerged.put(userID, continuousMergedForThisUser);
			} // end of loop over checkins for all users
		}
		catch (Exception e)
		{
			System.err.println("Exception in mergeContinuousGowallaWithoutBOD4");
			e.printStackTrace();
			// PopUps.showException(e, "mergeContinuousGowallaWithoutBOD4()");
		}
		System.out.println("numOfConsecActNameSame = " + numOfConsecActNameSame + " " + "numOfConsecActNameSameMerged ="
				+ numOfConsecActNameSameMerged + " percentage of consecs merged = "
				+ (numOfConsecActNameSameMerged * 100.0 / numOfConsecActNameSame * 1.0));
		return mapForDataMerged;
	}

	/**
	 * <font color = orange>Note: distanceFromPrev and durationFromPrev for the merged checkin is considered to be the
	 * corresponding values for the first checkin.
	 * <p>
	 * This results in following: if a checkin which is a merger of 3 checkins is followed by a single unmerged checkin.
	 * Then the distanceFromPrev and durationFromPrev for the merged checkin is the corresponding values for the first
	 * merged checkin while distanceFromPrev and durationFromPrev for the following unmerged checkin is the values
	 * corresponding to the distance and difference of this unmerged checking from the last (3rd) of the merged checkin.
	 * </font>
	 * 
	 * @param checkinsToMerge
	 * @return
	 */
	private static CheckinEntry mergeCheckins(ArrayList<CheckinEntry> checkinsToMerge)
	{

		if (checkinsToMerge.size() == 1)
		{
			return checkinsToMerge.get(0);
		}

		// CheckinEntry(String userID, Integer locationID, Timestamp ts, String latitude, String longitude,
		// Integer catID, String workingLevelCatIDs, double distanceInMetersFromNext, long durationInSecsFromNext)

		String userID = checkinsToMerge.get(0).getUserID();
		Timestamp ts = checkinsToMerge.get(0).getTimestamp();
		Integer catID = checkinsToMerge.get(0).getActivityID();
		String workingLevelCatIDs = checkinsToMerge.get(0).getWorkingLevelCatIDs();
		double distanceInMFromPrev = checkinsToMerge.get(0).getDistanceInMetersFromPrev();
		long durationInSecsFromPrev = checkinsToMerge.get(0).getDurationInSecsFromPrev();

		ArrayList<Integer> locationIDs = new ArrayList<>();
		ArrayList<String> lats = new ArrayList<>();
		ArrayList<String> lons = new ArrayList<>();

		for (CheckinEntry ce : checkinsToMerge)
		{
			locationIDs.addAll(ce.getLocationIDs());
			lats.addAll(ce.getStartLats());
			lons.addAll(ce.getStartLons());
		}

		CheckinEntry mergedCheckin = new CheckinEntry(userID, locationIDs, ts, lats, lons, catID, workingLevelCatIDs,
				distanceInMFromPrev, durationInSecsFromPrev);

		return mergedCheckin;
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
	// public static LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mergeContinuousWithoutBOD3(
	// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllData, String pathToWrite)
	// {
	// String commonPath = pathToWrite;
	// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForDataMerged = new LinkedHashMap<String,
	// TreeMap<Timestamp, CheckinEntry>>();
	//
	// /*
	// * Note: using TreeMap is IMPORTANT here, because TreeMap will automatically sort by the timestamp, so we do not
	// * need to be concerned about whether we add the activities in correct order or not, if the timestamps are
	// * right, it will be stored correctly
	// */
	//
	// System.out.println("Merging continuous without BOD");
	// try
	// {
	// for (Map.Entry<String, TreeMap<Timestamp, CheckinEntry>> entryForUser : mapForAllData.entrySet())
	// {
	// String userID = entryForUser.getKey();
	// System.out.println("\nUser =" + userID);
	//
	// BufferedWriter bwMergerCaseLogs = WritingToFile
	// .getBufferedWriterForNewFile(commonPath + userID + "MergerCasesLog.csv");
	// bwMergerCaseLogs.write("Case,ActName,CurrentTS, NextTS,Comment\n");
	//
	// int numOfTrajCaseA = 0, numOfTrajCaseB = 0, numOfTrajCaseC = 0, numOfLastTrajEntries = 0;
	// int countOfContinuousMerged = 1;
	//
	// TreeMap<Timestamp, CheckinEntry> continuousMergedForThisUser = new TreeMap<Timestamp, CheckinEntry>();
	//
	// long durationInSeconds = 0;
	//
	// ArrayList<String> newLati = new ArrayList<String>(), newLongi = new ArrayList<String>(),
	// newPlaceID = new ArrayList<String>();
	//
	// long timeDiffWithNextInSeconds = 0; // do not delete. // not directly relevant
	// Timestamp startTimestamp;
	//
	// ArrayList<CheckinEntry> entriesForCurrentUser = UtilityBelt
	// .treeMapToArrayListCheckinEntry(entryForUser.getValue());
	//
	// // $$System.out.println("----Unmerged Activity data for user "+userName+"-----");
	// // $$traverseArrayList(dataForCurrentUser);
	// // $$System.out.println("----END OF Unmerged Activity data--"+userName+"--");
	// for (int i = 0; i < entriesForCurrentUser.size(); i++)
	// {
	// // $$System.out.println("\nReading: "+dataForCurrentUser.get(i).toString());
	// CheckinEntry ce = entriesForCurrentUser.get(i);
	//
	// Timestamp currentTimestamp = ce.getTimestamp();
	// String currentActName = String.valueOf(ce.getActivityID());// .getMode();
	//
	// ArrayList<String> currentLat = ce.getStartLats();// .getLatitude();
	// ArrayList<String> currentLon = ce.getStartLons();// trajEntriesForCurrentUser.get(i).getLongitude();
	// ArrayList<String> currentPlaceID = ce.getLocationIDs();// trajEntriesForCurrentUser.get(i).getTrajectoryID();
	//
	// newLati.addAll(currentLat);
	// newLongi.addAll(currentLon);
	// newPlaceID.addAll(currentPlaceID);
	// // startTimestamp=currentTimestamp;
	//
	// if (i < ce.size() - 1) // is not the last element of arraylist
	// {
	// // check if the next element should be merged with this one if they are continuous and have same
	// // activity name
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
	// timeDiffWithNextInSeconds = trajEntriesForCurrentUser.get(i)
	// .getDifferenceWithNextInSeconds()
	// + trajEntriesForCurrentUser.get(i + 1).getDifferenceWithNextInSeconds(); // TODO
	//
	// countOfContinuousMerged++;
	// // ##bwMergerCaseLogs.write("CaseA: Continuous merged for mode=" + currentModeName + "
	// // durationInSeconds="+ durationInSeconds + "\n");
	// bwMergerCaseLogs.write("CaseA," + currentModeName + "," + durationInSeconds + ","
	// + currentTimestamp + "," + nextTimestamp + ",merged as continuous\n");
	// continue;
	// }
	//
	// else
	// {
	// startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));// durationInSeconds
	// // is
	// // the
	// // accumulated
	// // duration
	// // from
	// // past
	// // merging
	// // ##System.out.println("new starttimestamp="+startTimestamp);
	//
	// long diffCurrentAndNextInSec = (nextTimestamp.getTime() - currentTimestamp.getTime())
	// / 1000;
	// long secsItContinuesBeforeNext;
	//
	// if (diffCurrentAndNextInSec <= assumeContinuesBeforeNextInSecs) // in this case these were
	// // different activity names
	// {
	// numOfTrajCaseB += 1;
	// secsItContinuesBeforeNext = diffCurrentAndNextInSec;
	// // ##bwMergerCaseLogs.write("CaseB: diffCurrentAndNextInSec <=
	// // assumeContinuesBeforeNextInSecs, secsItContinuesBeforeNext=" +
	// // secsItContinuesBeforeNext + "\n");
	// bwMergerCaseLogs.write("CaseB," + currentModeName + "," + durationInSeconds + ","
	// + currentTimestamp + "," + nextTimestamp + "," + diffCurrentAndNextInSec + "<="
	// + assumeContinuesBeforeNextInSecs + " secsItContinuesBeforeNext ="
	// + secsItContinuesBeforeNext + "\n");
	// }
	//
	// else
	// {
	// // System.out.println("\n\t For user: "+userID+", at
	// // currentTimestamp="+currentTimestamp+", currentModeName="+currentModeName);
	// numOfTrajCaseC += 1;
	// secsItContinuesBeforeNext = assumeContinuesBeforeNextInSecs;
	//
	// // ##bwMergerCaseLogs.write("CaseC: diffCurrentAndNextDifferentInSec (" +
	// // diffCurrentAndNextInSec + ") >"+ assumeContinuesBeforeNextInSecs + "\n");
	// bwMergerCaseLogs.write("CaseC," + currentModeName + "," + durationInSeconds + ","
	// + currentTimestamp + "," + nextTimestamp + "," + diffCurrentAndNextInSec + ">"
	// + assumeContinuesBeforeNextInSecs + " secsItContinuesBeforeNext ="
	// + secsItContinuesBeforeNext + " put new Unknown\n");
	//
	// /* Put the new 'Unknown' entry///////////////// */
	// long durationForNewUnknownActivity = diffCurrentAndNextInSec
	// - assumeContinuesBeforeNextInSecs;
	// Timestamp startOfNewUnknown = new Timestamp(
	// currentTimestamp.getTime() + (assumeContinuesBeforeNextInSecs * 1000));
	//
	// // unknownsInsertedWholes.put(startOfNewUnknown, new TrajectoryEntry(startOfNewUnknown,
	// // durationForNewUnknownActivity,"Unknown")); //
	// // String.valueOf(durationForNewUnknownActivity));
	//
	// TrajectoryEntry te = new TrajectoryEntry(startOfNewUnknown,
	// durationForNewUnknownActivity, "Unknown");// ,bodCount);
	// continuousMergedForThisUser.put(startOfNewUnknown, te);
	// unknownsInsertedWholes.put(startOfNewUnknown, te);
	// // $$System.out.println("Added Trajectory Entry: "+te.toString());
	// }
	//
	// durationInSeconds = durationInSeconds + secsItContinuesBeforeNext;
	//
	// TrajectoryEntry te = trajEntriesForCurrentUser.get(i);
	// te.setLatitude(newLati); // note: has to be done with set,..cant do with add becasue it will
	// // create problems if more than two entries are merged
	// te.setLongitude(newLongi);
	// te.setAltitude(newAlti);
	// te.setTrajectoryID(newTrajID);
	//
	// te.setTimestamp(startTimestamp);
	// te.setDurationInSeconds(durationInSeconds);
	// // te.setDifferenceWithNextInSeconds(timeDiffWithNextInSeconds);
	//
	// continuousMergedForThisUser.put(startTimestamp, te);
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
	// // $$System.out.println("this is the last data point,\n duration in seconds =
	// // "+durationInSeconds);
	//
	// startTimestamp = new Timestamp(currentTimestamp.getTime() - (durationInSeconds * 1000));
	//
	// TrajectoryEntry te = trajEntriesForCurrentUser.get(i);
	// te.setLatitude(newLati); // note: has to be done with set,..cant do with add becasue it will
	// // create problems if more than two entries are merged
	// te.setLongitude(newLongi);
	// te.setAltitude(newAlti);
	// te.setTrajectoryID(newTrajID);
	// te.setTimestamp(startTimestamp);
	// te.setDurationInSeconds(durationInSeconds + timeDurationForLastSingletonTrajectoryEntry);
	//
	// continuousMergedForThisUser.put(startTimestamp, te);
	//
	// // $$System.out.println("Added Trajectory Entry: "+te.toString());
	//
	// // newLati.clear();newLongi.clear();newAlti.clear();
	// // //////////////////////
	// // /*REPLACED BY BREAKED ACTIVITIES mapContinuousMerged.put(startTimestamp,
	// // currentActivityName+"||"+durationInSeconds); */
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
	// } // end of for loop over trajectory entries for current user.
	//
	// mapForAllDataMerged.put(entryForUser.getKey(), continuousMergedForThisUser);
	// mapForAllUnknownsWholes.put(entryForUser.getKey(), unknownsInsertedWholes);
	//
	// bwMergerCaseLogs.write("User:" + userID + ",numOfTrajCaseA = " + numOfTrajCaseA + ",numOfTrajCaseB = "
	// + numOfTrajCaseB + ",numOfTrajCaseC = " + numOfTrajCaseC + " ,numOfLastTrajEntries = "
	// + numOfLastTrajEntries);
	// System.out.println("User:" + userID + ",numOfTrajCaseA = " + numOfTrajCaseA + ",numOfTrajCaseB = "
	// + numOfTrajCaseB + ",numOfTrajCaseC = " + numOfTrajCaseC + " ,numOfLastTrajEntries = "
	// + numOfLastTrajEntries);
	// bwMergerCaseLogs.close();
	// } // end of for loop over users
	//
	// WritingToFile.writeLinkedHashMapOfTreemap2(mapForAllUnknownsWholes, "Unknown_Wholes_Inserted",
	// "User,Timestamp,DurationInSecs");
	// }
	// catch (Exception e)
	// {
	// PopUps.showException(e, "mergeContinuousTrajectoriesAssignDurationWithoutBOD2()");
	// }
	// return mapForAllDataMerged;
	// }
}
