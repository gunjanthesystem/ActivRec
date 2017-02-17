package org.activity.util;

/**
 * Contains some utilities method which could be reused.
 * 
 * @since 16 Feb 2016 12:21am
 * @author gunjan
 *
 */
public class DatageneratorUtils
{

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
	// public static LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mergeContinuousWithoutBOD2(
	// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllData, String pathToWrite)
	// {
	// String commonPath = pathToWrite;
	// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>> mapForAllDataMergedPlusDuration = new
	// LinkedHashMap<String, TreeMap<Timestamp, CheckinEntry>>();
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
	// BufferedWriter bwMergerCaseLogs = WritingToFile
	// .getBufferedWriterForNewFile(commonPath + userID + "MergerCasesLog.csv");
	// bwMergerCaseLogs.write("Case,Mode,DurationInSecs,CurrentTS, NextTS,Comment\n");
	//
	// System.out.println("\nUser =" + userID);
	//
	// int numOfTrajCaseA = 0, numOfTrajCaseB = 0, numOfTrajCaseC = 0, numOfLastTrajEntries = 0;
	//
	// int countOfContinuousMerged = 1;
	//
	// TreeMap<Timestamp, CheckinEntry> mapContinuousMerged = new TreeMap<Timestamp, CheckinEntry>();
	//
	// long durationInSeconds = 0;
	//
	// ArrayList<String> newLati = new ArrayList<String>(), newLongi = new ArrayList<String>(),
	// newAlti = new ArrayList<String>();
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
	// // startTimestamp = getTimestampFromDataEntry(dataForCurrentUser.get(i));
	// // ##
	// // $$System.out.println("\nReading: "+dataForCurrentUser.get(i).toString());
	// CheckinEntry ce = entriesForCurrentUser.get(i);
	//
	// Timestamp currentTimestamp = ce.getTimestamp();
	// String currentActName = String.valueOf(ce.getActivityID());// .getMode();
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
	// // CHECK
	// // IF
	// // NOT
	// // NEEDED
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
	// mapContinuousMerged.put(startOfNewUnknown, te);
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
	// mapContinuousMerged.put(startTimestamp, te);
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
	// mapForAllDataMergedPlusDuration.put(entryForUser.getKey(), mapContinuousMerged);
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
	// return mapForAllDataMergedPlusDuration;
	// }
}
