package org.activity.stats;

import java.io.File;
import java.io.PrintStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.activity.constants.Constant;
import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.objects.Timeline;
import org.activity.objects.TrajectoryEntry;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

public class ActivityStats
{
	static String pathToWrite;

	/**
	 * INCOMPLETE
	 * 
	 * @param usersDayTimelinesAll
	 */
	public static void getActivityStatsPerDay(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesAll)
	{
		Constant.setCommonPath(Constant.outputCoreResultsPath);

		String directoryToWrite;

		directoryToWrite = Constant.outputCoreResultsPath + Constant.getDatabaseName()
				+ LocalDateTime.now().getMonth().toString().substring(0, 3) + LocalDateTime.now().getDayOfMonth()
				+ "ActivityPerDayStats";

		new File(directoryToWrite).mkdir();
		pathToWrite = directoryToWrite + "/";
		Constant.setCommonPath(pathToWrite);
		PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + "ConsoleLog.txt");

		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines = new LinkedHashMap<>();
		usersDayTimelines = TimelineUtils.cleanDayTimelines(usersDayTimelinesAll);

	}

	/**
	 * INCOMPLETE
	 * 
	 * @param usersDayTimelinesAll
	 */
	public static void
			writeDistinctActivities(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesAll)
	{
		Constant.setCommonPath(Constant.outputCoreResultsPath);

		String directoryToWrite;

		directoryToWrite = Constant.outputCoreResultsPath + Constant.getDatabaseName()
				+ LocalDateTime.now().getMonth().toString().substring(0, 3) + LocalDateTime.now().getDayOfMonth()
				+ "ActivityPerDayStats";

		new File(directoryToWrite).mkdir();
		pathToWrite = directoryToWrite + "/";
		Constant.setCommonPath(pathToWrite);
		PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(pathToWrite + "ConsoleLog.txt");

		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines = new LinkedHashMap<>();
		usersDayTimelines = TimelineUtils.cleanDayTimelines(usersDayTimelinesAll);

	}

	public static void main(String[] args)
	{
		LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedPlusDuration;

		Constant.setCommonPath("/run/media/gunjan/HOME/gunjan/Geolife Data Works/");
		try
		{
			// WritingToFile.appendLineToFile("gunjan1", "test");
			//
			// WritingToFile.appendLineToFile("gunjan2", "test");
			//
			// File loadLog= new File(Constant.getCommonPath()+"WritingSomeStats.txt"); loadLog.delete();
			// loadLog.createNewFile();
			// PrintStream loadLogStream= new PrintStream(loadLog);
			// System.setOut(loadLogStream);
			// System.setErr(loadLogStream);

			mapForAllDataMergedPlusDuration = (LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>>) (Serializer
					.deSerializeThis(Constant.getCommonPath() + "mapForAllDataMergedPlusDuration.map"));
			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedPlusDuration,"Everything","MergedContinuous",false);
			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedPlusDuration,"Everything","MergedContinuousSandwiches",true);
			//
			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedPlusDuration,"Not
			// Available","MergedContinuousSandwiches",true);
			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedPlusDuration,"Unknown","MergedContinuousSandwiches",true);

			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedPlusDuration,"Not
			// Available","MergedContinuous",false);
			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedPlusDuration,"Unknown","MergedContinuous",false);
			//
			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedPlusDuration,"ValidsOnly","MergedContinuous",false);
			// WritingToFile.writeActivityTypeWithDurationGeo(mapForAllDataMergedPlusDuration,"ValidsOnly","MergedContinuous",true);
			getAllActivitiesGeoDistances(mapForAllDataMergedPlusDuration);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param mapForAllDataMergedPlusDuration
	 */
	public static void getAllActivitiesGeoDistances(
			LinkedHashMap<String, TreeMap<Timestamp, TrajectoryEntry>> mapForAllDataMergedPlusDuration)
	{
		for (Map.Entry<String, TreeMap<Timestamp, TrajectoryEntry>> entry : mapForAllDataMergedPlusDuration.entrySet())
		{
			String user = entry.getKey();

			ArrayList<TrajectoryEntry> listOfTrajectoryEntries = UtilityBelt.treeMapToArrayListGeo(entry.getValue());

			ArrayList<TrajectoryEntry> validTrajectoryEntries = new ArrayList<TrajectoryEntry>();

			// System.out.println("num of activity objects for user "+user+" are = "+listOfTrajectoryEntries.size());
			for (TrajectoryEntry te : listOfTrajectoryEntries)
			{
				if (UtilityBelt.isValidActivityName(te.getMode()) == true)
				{
					validTrajectoryEntries.add(te);
				}
			}

			System.out.println(
					"num of valid activity objects for user " + user + " are = " + validTrajectoryEntries.size());

			for (int i = 0; i < validTrajectoryEntries.size(); i++)
			{
				for (int j = i + 1; j < validTrajectoryEntries.size(); j++)
				{
					// System.out.println("i = "+i+" , j="+j);
					String startGeoLat1 = validTrajectoryEntries.get(i).getStartLat();
					String startGeoLon1 = validTrajectoryEntries.get(i).getStartLon();

					String startGeoLat2 = validTrajectoryEntries.get(j).getStartLat();
					String startGeoLon2 = validTrajectoryEntries.get(j).getStartLon();

					double startDiff = StatsUtils.haversine(startGeoLat1, startGeoLon1, startGeoLat2, startGeoLon2);

					WritingToFile.appendLineToFile(String.valueOf(StatsUtils.round(startDiff, 2)), user + "startDiff");

					String endGeoLat1 = validTrajectoryEntries.get(i).getEndLat();
					String endGeoLon1 = validTrajectoryEntries.get(i).getEndLon();

					String endGeoLat2 = validTrajectoryEntries.get(j).getEndLat();
					String endGeoLon2 = validTrajectoryEntries.get(j).getEndLon();

					double endDiff = StatsUtils.haversine(endGeoLat1, endGeoLon1, endGeoLat2, endGeoLon2);

					WritingToFile.appendLineToFile(String.valueOf(StatsUtils.round(endDiff, 2)), user + "endDiff");
					// validTrajectoryEntries.get(i),validTrajectoryEntries.get(j)
				}
			}

		}
	}
}
