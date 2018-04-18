package org.activity.stats;

import java.io.BufferedWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.TrajectoryEntry;
import org.activity.ui.PopUps;
import org.activity.util.UtilityBelt;

/**
 *
 * Todo:
 * 
 * remove weekend days.
 * 
 * num of trajectories per user
 * 
 * distribution of num of distinct modes of transport in trajectories for each user distribution of num of total modes
 * of transport in trajectories for each user
 * 
 * 
 * @author gunjan
 *
 *
 */
public class TrajectoryStats
{
	String pathToSerialisedData;
	String commonPath;
	LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID;

	public static void main(String args[])
	{
		new TrajectoryStats();
	}

	TrajectoryStats()
	{
		pathToSerialisedData = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/14Apr2016AllUsersDataGenerationInfS/mapForAllDataMergedPlusDuration13Apr2016Inf.map";
		// "/run/media/gunjan/HOME/gunjan/Geolife Data
		// Works/Mar152016AllUsersDataGeneration/mapForAllDataMergedPlusDuration15Mar2016.map";
		// "/run/media/gunjan/HOME/gunjan/Geolife Data
		// Works/Mar302016AllUsersDataGenerationInf/mapForAllDataMergedPlusDuration30Mar2016Inf.map";
		commonPath = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/TrajStats14April2016Inf/";

		// LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedContinuousWithDuration =
		// (LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>)
		// Serializer.deSerializeThis(pathToSerialisedData);
		TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 12, 2016
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedContinuousWithDuration = consolidateSerialisedMaps(
				pathToSerialisedData);

		LinkedHashMap<String, Integer> numOfTrajectoriesPerUser = getNumOfTrajectoriesPerUser(
				mapForAllDataMergedContinuousWithDuration);

		WToFile.writeLinkedHashMapStrInt(numOfTrajectoriesPerUser, commonPath + "NumOfTrajPerUser.csv");

		trajectoryEntriesByTrajID = createTrajectoryEntriesByTrajID(mapForAllDataMergedContinuousWithDuration);
		writeNumOfTrajIDsForEachUser(trajectoryEntriesByTrajID, "Original");
		trajectoryEntriesByTrajID = cleanTrajectories(trajectoryEntriesByTrajID);
		writeNumOfTrajIDsForEachUser(trajectoryEntriesByTrajID, "VO");// valids only
		writeTrajectoryEntriesByTrajID(trajectoryEntriesByTrajID, "VO");
		writeTrajectoryEntriesByTrajIDWithTimestamps(trajectoryEntriesByTrajID, "VO");
		writeNumOfTrajectoryEntriesForEachTrajID(trajectoryEntriesByTrajID, "VO");
		writeNumOfDistinctModesInTrajectoryEntriesForEachTrajID(trajectoryEntriesByTrajID, "VO");
		writeNumOfDistinctTrajecsForEachUser(trajectoryEntriesByTrajID, "VO");
		writeNumOfWeekendTrajIDsForEachUser(trajectoryEntriesByTrajID, "VO");// "TrajEntriesWithInvalidsRemoved");
		writeTrajIDsSpanningMultipleDaysForEachUser(trajectoryEntriesByTrajID, "VO");// "TrajEntriesWithInvalidsRemoved");

		trajectoryEntriesByTrajID = pruneByLengthTrajectories(trajectoryEntriesByTrajID, 4);
		writeNumOfTrajIDsForEachUser(trajectoryEntriesByTrajID, "VOGT4Only");// valids only
		writeTrajectoryEntriesByTrajID(trajectoryEntriesByTrajID, "VOGT4Only");
		// writeTrajectoryEntriesByTrajIDWithTimestamps(trajectoryEntriesByTrajID, "VOGT4Only");
		writeNumOfTrajectoryEntriesForEachTrajID(trajectoryEntriesByTrajID, "VOGT4Only");
		writeNumOfDistinctModesInTrajectoryEntriesForEachTrajID(trajectoryEntriesByTrajID, "VOGT4Only");
		writeNumOfDistinctTrajecsForEachUser(trajectoryEntriesByTrajID, "VOGT4Only");
		// writeNumOfWeekendTrajIDsForEachUser(trajectoryEntriesByTrajID, "VOGT4Only");//
		// "TrajEntriesWithInvalidsRemoved");
		// writeTrajIDsSpanningMultipleDaysForEachUser(trajectoryEntriesByTrajID, "VOGT4Only");//
		// "TrajEntriesWithInvalidsRemoved");
		PopUps.showMessage("Done");
	}

	private LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> consolidateSerialisedMaps(String path)
	{
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedContinuousWithDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>();
		for (int i = 0; i <= 65; i += 5)
		{
			String pathToSerialisedData = path + i;
			mapForAllDataMergedContinuousWithDuration
					.putAll((LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>) Serializer
							.deSerializeThis(pathToSerialisedData));
		}

		// for (int i = 63; i <= 66; i += 3)
		// {
		// String pathToSerialisedData =
		// "/run/media/gunjan/HOME/gunjan/Geolife Data
		// Works/Mar162016AllUsersDataGenerationA/mapForAllDataMergedPlusDuration16Mar2016.map"
		// + i;
		// mapForAllDataMergedContinuousWithDuration.putAll((LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>)
		// Serializer
		// .deSerializeThis(pathToSerialisedData));
		// }
		//
		System.out.println("Consolidated map is of size: " + mapForAllDataMergedContinuousWithDuration.size());
		return mapForAllDataMergedContinuousWithDuration;
	}

	public String trajectoryEntriesAsStringOfModes(ArrayList<TrajectoryEntry> list)// , String delimiter)
	{
		StringBuffer s = new StringBuffer();

		for (TrajectoryEntry te : list)
		{
			s.append(">>" + te.getMode());
		}
		return s.toString();
	}

	public String trajectoryEntriesAsStringOfModesWithTimestamps(ArrayList<TrajectoryEntry> list)// , String delimiter)
	{
		StringBuffer s = new StringBuffer();

		for (TrajectoryEntry te : list)
		{
			s.append(">>" + te.getTimestamp().toGMTString() + "_" + te.getMode());
		}
		return s.toString();
	}

	/**
	 * (correctness verified by raw data)
	 * 
	 * @param trajectoryEntriesByTrajID
	 * @param fileNamePharse
	 */
	public void writeTrajectoryEntriesByTrajID(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID,
			String fileNamePharse)
	{
		try
		{
			BufferedWriter bw = WToFile
					.getBWForNewFile(commonPath + fileNamePharse + "TrajectoryEntriesByTrajID.csv");
			bw.write("User,TrajID, #TrajecEntries,#DistinctModes,TrajectoryEntriesAsMode\n");
			for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> e : trajectoryEntriesByTrajID
					.entrySet())
			{
				String userID = e.getKey();

				for (Entry<String, ArrayList<TrajectoryEntry>> tidLevel : e.getValue().entrySet())
				{
					String tid = tidLevel.getKey();
					ArrayList<TrajectoryEntry> tEntries = tidLevel.getValue();
					bw.write(userID + "," + tid + "," + tEntries.size() + "," + getNumberOfDistinctModes(tEntries) + ","
							+ trajectoryEntriesAsStringOfModes(tEntries) + "\n");
				}
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "traverseTrajectoryEntriesByTrajID()");
		}
	}

	/**
	 * (correctness verified by raw data)
	 * 
	 * @param trajectoryEntriesByTrajID
	 * @param fileNamePharse
	 */
	public void writeTrajectoryEntriesByTrajIDWithTimestamps(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID,
			String fileNamePharse)
	{
		try
		{
			BufferedWriter bw = WToFile.getBWForNewFile(
					commonPath + fileNamePharse + "TrajectoryEntriesByTrajIDWithTimestamps.csv");
			bw.write("User,TrajID, #TrajecEntries,#DistinctModes,TrajectoryEntriesAsMode\n");
			for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> e : trajectoryEntriesByTrajID
					.entrySet())
			{
				String userID = e.getKey();

				for (Entry<String, ArrayList<TrajectoryEntry>> tidLevel : e.getValue().entrySet())
				{
					String tid = tidLevel.getKey();
					ArrayList<TrajectoryEntry> tEntries = tidLevel.getValue();
					bw.write(userID + "," + tid + "," + tEntries.size() + "," + getNumberOfDistinctModes(tEntries) + ","
							+ trajectoryEntriesAsStringOfModesWithTimestamps(tEntries) + "\n");
				}
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "traverseTrajectoryEntriesByTrajID()");
		}
	}

	/**
	 * Each row: user ;Each cell: num of trajectory entries for a single trajId. intended for viewing distriubution
	 * 
	 * @param trajectoryEntriesByTrajID
	 * @param fileNamePharse
	 */
	public void writeNumOfTrajectoryEntriesForEachTrajID(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID,
			String fileNamePharse)
	{
		try
		{
			BufferedWriter bw = WToFile.getBWForNewFile(
					commonPath + fileNamePharse + "NumOfTrajectoryEntriesForEachTrajID.csv");
			// bw.write("User,TrajID, #TrajecEntries,TrajectoryEntriesAsMode\n");
			for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> e : trajectoryEntriesByTrajID
					.entrySet())
			{
				String userID = e.getKey();
				bw.write(userID);// ; + ",");
				for (Entry<String, ArrayList<TrajectoryEntry>> tidLevel : e.getValue().entrySet())
				{
					String tid = tidLevel.getKey();
					ArrayList<TrajectoryEntry> tEntries = tidLevel.getValue();
					bw.write("," + tEntries.size());// + "," + trajectoryEntriesAsString(tEntries) + "\n");
				}
				bw.newLine();
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "traverseTrajectoryEntriesByTrajID()");
		}
	}

	public Integer getNumberOfDistinctModes(ArrayList<TrajectoryEntry> list)
	{
		LinkedHashSet<String> set = new LinkedHashSet<String>();

		for (TrajectoryEntry te : list)
		{
			set.add(te.getMode());
		}

		return set.size();
	}

	/**
	 * Each row: user ;Each cell: num of trajectory entries for a single trajId. intended for viewing distriubution
	 * 
	 * @param trajectoryEntriesByTrajID
	 * @param fileNamePharse
	 */
	public void writeNumOfDistinctModesInTrajectoryEntriesForEachTrajID(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID,
			String fileNamePharse)
	{
		try
		{
			BufferedWriter bw = WToFile.getBWForNewFile(
					commonPath + fileNamePharse + "NumOfDistinctModesInTrajectoryEntriesForEachTrajID.csv");
			// bw.write("User,TrajID, #TrajecEntries,TrajectoryEntriesAsMode\n");
			for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> e : trajectoryEntriesByTrajID
					.entrySet())
			{
				String userID = e.getKey();
				bw.write(userID);// ; + ",");
				for (Entry<String, ArrayList<TrajectoryEntry>> tidLevel : e.getValue().entrySet())
				{
					String tid = tidLevel.getKey();
					ArrayList<TrajectoryEntry> tEntries = tidLevel.getValue();
					bw.write("," + getNumberOfDistinctModes(tEntries));// + "," + trajectoryEntriesAsString(tEntries) +
																		// "\n");
				}
				bw.newLine();
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "traverseTrajectoryEntriesByTrajID()");
		}

	}

	/**
	 * Num of trajectory IDs having distinct sequence of modes BySequenceOfModes
	 * 
	 * @param tidLevel
	 * @return
	 */
	public Integer getNumOfDistinctBySequenceOfModesTrajectories(
			LinkedHashMap<String, ArrayList<TrajectoryEntry>> tidLevel)
	{
		LinkedHashSet<String> set = new LinkedHashSet();

		for (Entry<String, ArrayList<TrajectoryEntry>> e : tidLevel.entrySet())
		{
			set.add(this.trajectoryEntriesAsStringOfModes(e.getValue()));
		}
		return set.size();

	}

	public void writeNumOfDistinctTrajecsForEachUser(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID,
			String fileNamePharse)
	{
		try
		{
			BufferedWriter bw = WToFile
					.getBWForNewFile(commonPath + fileNamePharse + "NumOfDistinctTrajecsForEachUser.csv");
			bw.write("User,#TrajectoriesWithDistinctSequenceOfModes\n");
			for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> e : trajectoryEntriesByTrajID
					.entrySet())
			{
				String userID = e.getKey();
				bw.write(userID + "," + getNumOfDistinctBySequenceOfModesTrajectories(e.getValue()));
				bw.newLine();
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "traverseTrajectoryEntriesByTrajID()");
		}
	}

	public void writeNumOfTrajIDsForEachUser(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID,
			String fileNamePharse)
	{
		try
		{
			BufferedWriter bw = WToFile
					.getBWForNewFile(commonPath + fileNamePharse + "NumOfTrajIDsForEachUser.csv");
			bw.write("User,#TrajIDs\n");
			for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> e : trajectoryEntriesByTrajID
					.entrySet())
			{
				String userID = e.getKey();
				int numOfTrajIDs = e.getValue().size();
				bw.write(userID + "," + numOfTrajIDs);
				bw.newLine();
				// System.out.println("User: "+userID+" num of traj")
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "traverseTrajectoryEntriesByTrajID()");
		}
	}

	public Integer getNumOfTrajIDsStartInWeekEnd(LinkedHashMap<String, ArrayList<TrajectoryEntry>> tidLevel)
	{
		int res = 0;
		for (Entry<String, ArrayList<TrajectoryEntry>> e : tidLevel.entrySet())
		{
			int dayOfFirstTrajectoryEntry = e.getValue().get(0).getTimestamp().getDay(); // first trajectory entry
			if (dayOfFirstTrajectoryEntry == 0 || dayOfFirstTrajectoryEntry == 6)
			{
				res++;
			}
		}
		return res;
	}

	public Integer getNumOfTrajIDsSpanningMultipleDays(LinkedHashMap<String, ArrayList<TrajectoryEntry>> tidLevel)
	{
		int res = 0;
		for (Entry<String, ArrayList<TrajectoryEntry>> e : tidLevel.entrySet())
		{
			if (getNumOfDaysTrajectoryEntriesSpan(e.getValue()) > 1)
			{
				res++;
			}
		}
		return res;
	}

	public Integer getNumOfDaysTrajectoryEntriesSpan(ArrayList<TrajectoryEntry> tes)
	{
		LinkedHashSet<String> s = new LinkedHashSet<String>();

		for (TrajectoryEntry e : tes)
		{
			Timestamp ts = e.getTimestamp();
			s.add(ts.getDate() + "_" + ts.getMonth() + "_" + ts.getYear());
		}
		return s.size();
	}

	public void writeTrajIDsSpanningMultipleDaysForEachUser(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID,
			String fileNamePharse)
	{
		try
		{
			BufferedWriter bw = WToFile
					.getBWForNewFile(commonPath + fileNamePharse + "TrajIDsSpanningMultipleDays.csv");
			bw.write("User,TrajID,numOfDaysSpans\n");
			for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> e : trajectoryEntriesByTrajID
					.entrySet())
			{
				String userID = e.getKey();

				for (Entry<String, ArrayList<TrajectoryEntry>> tidLevel : e.getValue().entrySet())
				{
					int numOfDaysSpans = getNumOfDaysTrajectoryEntriesSpan(tidLevel.getValue());
					if (numOfDaysSpans > 1)
					{
						bw.write(userID + "," + tidLevel.getKey() + "," + numOfDaysSpans + ","
								+ trajectoryEntriesAsStringOfModesWithTimestamps(tidLevel.getValue()));
						bw.newLine();
					}

				}

				// System.out.println("User: "+userID+" num of traj")
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "traverseTrajectoryEntriesByTrajID()");
		}
	}

	public void writeNumOfWeekendTrajIDsForEachUser(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID,
			String fileNamePharse)
	{
		try
		{
			BufferedWriter bw = WToFile
					.getBWForNewFile(commonPath + fileNamePharse + "NumOfWeekendTrajIDsForEachUser.csv");
			bw.write("User,#WeekendStartTrajIDs,#WeekdayStartTrajIDs, #TrajIDs,#TrajIDsSpaningMultipleDays\n");
			for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> e : trajectoryEntriesByTrajID
					.entrySet())
			{
				String userID = e.getKey();
				int numOfTrajIDs = e.getValue().size();
				int numOfTrajIDsStartingWeekend = getNumOfTrajIDsStartInWeekEnd(e.getValue());
				int numOfTrajIDsStartingWeekday = numOfTrajIDs - numOfTrajIDsStartingWeekend;// getNumOfTrajIDsStartInWeekEnd(e.getValue());

				int numOfTrajIDsSpanningMultipleDays = getNumOfTrajIDsSpanningMultipleDays(e.getValue());
				bw.write(userID + "," + numOfTrajIDsStartingWeekend + "," + numOfTrajIDsStartingWeekday + ","
						+ numOfTrajIDs + "," + numOfTrajIDsSpanningMultipleDays);
				bw.newLine();
				// System.out.println("User: "+userID+" num of traj")
			}
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showException(e, "traverseTrajectoryEntriesByTrajID()");
		}
	}

	/**
	 * Removes trajectory entries which are not valid mode of transport and subsequently removed those trajectory ids
	 * which do any have any trajectory entry with valid mode of transport
	 * 
	 * @param trajectoryEntriesByTrajID
	 * @return
	 */
	private LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> cleanTrajectories(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID)
	{

		LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> res = new LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>>();

		for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> u : trajectoryEntriesByTrajID.entrySet())
		{
			LinkedHashMap<String, ArrayList<TrajectoryEntry>> cleanedTidLevel = new LinkedHashMap<String, ArrayList<TrajectoryEntry>>();

			for (Entry<String, ArrayList<TrajectoryEntry>> tidLevel : u.getValue().entrySet())
			{
				ArrayList<TrajectoryEntry> onlyValidTrajectoryEntries = new ArrayList<TrajectoryEntry>();

				for (TrajectoryEntry te : tidLevel.getValue())
				{
					if (this.isValidActivityName(te.getMode()))
					{
						onlyValidTrajectoryEntries.add(te);
					}
				}

				if (onlyValidTrajectoryEntries.size() > 0) // the trajectoryID has atleast one trajectory entry with
															// valid mode of transport
					cleanedTidLevel.put(tidLevel.getKey(), onlyValidTrajectoryEntries);
			}
			res.put(u.getKey(), cleanedTidLevel);
		}
		return res;
	}

	/**
	 * Remove trajectories of length < atleastLength
	 * 
	 * @param trajectoryEntriesByTrajID
	 * @param atleastLength
	 * @return
	 */
	private LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> pruneByLengthTrajectories(
			LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajID,
			int atleastLength)
	{

		LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> res = new LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>>();

		for (Entry<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> u : trajectoryEntriesByTrajID.entrySet())
		{
			LinkedHashMap<String, ArrayList<TrajectoryEntry>> prunedTidLevel = new LinkedHashMap<String, ArrayList<TrajectoryEntry>>();

			for (Entry<String, ArrayList<TrajectoryEntry>> tidLevel : u.getValue().entrySet())
			{
				if (tidLevel.getValue().size() >= atleastLength) // the trajectoryID has atleast one trajectory entry
																	// with valid mode of transport
					prunedTidLevel.put(tidLevel.getKey(), tidLevel.getValue());
			}
			res.put(u.getKey(), prunedTidLevel);
		}
		return res;
	}

	/**
	 * 
	 * @param trajectoryEntriesByTrajID2
	 * @return
	 */
	private LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> createTrajectoryEntriesByTrajID(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> dataMap)
	{
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>> trajectoryEntriesByTrajIDResult = new LinkedHashMap<String, LinkedHashMap<String, ArrayList<TrajectoryEntry>>>();

		// to group by trajectory ID
		for (Entry<String, TreeMap<Timestamp, TrajectoryEntry>> e : dataMap.entrySet())
		{
			String userID = e.getKey();
			ArrayList<TrajectoryEntry> trajEntriesForCurrentUser = UtilityBelt.treeMapToArrayListGeo(e.getValue());

			LinkedHashMap<String, ArrayList<TrajectoryEntry>> trajEntriesForCurrentUserByTrajID = new LinkedHashMap<String, ArrayList<TrajectoryEntry>>();

			for (TrajectoryEntry te : trajEntriesForCurrentUser)
			{
				if (te.getNumberOfDistinctTrajectoryIDs() > 1)
				{
					PopUps.showException(
							new Exception("User:" + userID + " timestamp: " + te.getTimestamp() + " has "
									+ te.getNumberOfDistinctTrajectoryIDs() + "(>1) TrajIDs"),
							"createTrajectoryEntriesByTrajID");
					System.exit(-5);
				}
				else
				{
					String tid = te.getTrajectoryID().get(0);
					ArrayList<TrajectoryEntry> newArr;

					// if (this.isValidActivityName(te.getMode()) == false)
					// {
					// continue;
					// }
					if (trajEntriesForCurrentUserByTrajID.containsKey(tid))
					{
						newArr = trajEntriesForCurrentUserByTrajID.get(tid);
					}
					else
					{
						newArr = new ArrayList<TrajectoryEntry>();
					}
					newArr.add(te);
					trajEntriesForCurrentUserByTrajID.put(tid, newArr);
				}
			}
			trajectoryEntriesByTrajIDResult.put(userID, trajEntriesForCurrentUserByTrajID);
		}

		return trajectoryEntriesByTrajIDResult;
	}

	public LinkedHashMap<String, Integer> getNumOfTrajectoriesPerUser(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> dataMap)
	{
		LinkedHashMap<String, Integer> numOfTrajectoriesPerUser = new LinkedHashMap<String, Integer>();

		for (Entry<String, TreeMap<Timestamp, TrajectoryEntry>> e : dataMap.entrySet())
		{
			numOfTrajectoriesPerUser.put(e.getKey(), getNumberOfTrajectIDsWithValidActivityNames(e.getValue()));
		}
		return numOfTrajectoriesPerUser;
	}

	// public LinkedHashMap<String, Integer> getNumOfTrajectoriesStartingInWeekendPerUser(
	// LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> dataMap)
	// {
	// LinkedHashMap<String, Integer> numOfTrajectoriesPerUser = new LinkedHashMap<String, Integer>();
	//
	// for (Entry<String, TreeMap<Timestamp, TrajectoryEntry>> e : dataMap.entrySet())
	// {
	// numOfTrajectoriesPerUser.put(e.getKey(), getNumberOfTrajectIDsWithValidActivityNames(e.getValue()));
	// }
	// return numOfTrajectoriesPerUser;
	// }
	//
	/**
	 * 
	 * @param trajEntries
	 * @return Num of (distinct) trajectory IDs in the given traject entries.
	 */
	public Integer getNumberOfTrajectIDsWithValidActivityNames(TreeMap<Timestamp, TrajectoryEntry> trajEntries)
	{
		LinkedHashSet<String> trajIDsSet = new LinkedHashSet<String>();

		for (Entry<Timestamp, TrajectoryEntry> te : trajEntries.entrySet())
		{
			if (isValidActivityName(te.getValue().getMode()))
			{
				trajIDsSet.addAll(te.getValue().getTrajectoryID());
			}
		}
		return trajIDsSet.size();
	}

	// public LinkedHashMap<String, Integer> getNumOfTrajectoriesPerUserWithMoreThanOnDisinctValidMOT(
	// LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> dataMap)
	// {
	// LinkedHashMap<String, Integer> numOfTrajectoriesPerUser = new LinkedHashMap<String, Integer>();
	//
	// for (Entry<String, TreeMap<Timestamp, TrajectoryEntry>> e : dataMap.entrySet())
	// {
	//
	// numOfTrajectoriesPerUser.put(e.getKey(), e.getValue().size());
	// }
	// return numOfTrajectoriesPerUser;
	// }

	/**
	 * Is valid activity name for geolife dataset.
	 * 
	 * @param s
	 * @return
	 */
	public boolean isValidActivityName(String s)
	{
		if (s.trim().equals("Unknown") || s.trim().equals("Not Available"))
		{
			return false;
		}
		else
			return true;
	}

}
