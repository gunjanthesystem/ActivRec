package org.activity.stats;

import java.sql.Date;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Timeline;
import org.activity.util.DateTimeUtils;
import org.activity.util.TimelineTransformers;

/**
 * 
 * @author gunjan
 * @since 19 Nov 2018
 */
public class FeatureStats
{

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		diffs("/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FeatsOfTrainingTimelines.csv");
	}

	public static void writeFeatDistributionForEachUsersTrainingTimelines(int userId,
			LinkedHashMap<Date, Timeline> userTrainingTimelines, String absFileNameToAppend)
	{
		StringBuilder sb = new StringBuilder();

		for (Entry<Date, Timeline> entry : userTrainingTimelines.entrySet())
		{
			for (ActivityObject2018 ao : entry.getValue().getActivityObjectsInTimeline())
			{
				sb.append(ActivityObject2018.getHeaderForStringAllGeolifeWithNameForHeaded2(ao, ",") + "\n");
			}
		}
		WToFile.appendLineToFileAbs(sb.toString(), absFileNameToAppend);
	}

	/**
	 * 
	 * @param dayTimelines
	 * @param windowLength
	 * @return
	 */
	public static List<List<ActivityObject2018>> splitDayTimelineToSlidingWindows(
			LinkedHashMap<Date, Timeline> dayTimelines, int windowLength)
	{
		// convert daily timeline to single timeline
		// converting day timelines into continuous timelines suitable to be used for matching unit views
		Timeline trainTimeline = TimelineTransformers.dayTimelinesToATimeline(dayTimelines, false, true);
		// split it into progressive subsequence (sliding windows of width MU
		int numOfAOS = trainTimeline.size();
		List<List<ActivityObject2018>> listOfWindows = new ArrayList<>();
		for (int index = 0; index < (numOfAOS - windowLength); index++)
		{
			listOfWindows.add(trainTimeline.getActivityObjectsInTimelineFromToIndex(index, index + windowLength));
		}
		return listOfWindows;
	}

	/**
	 * 
	 * @param userID
	 * @param userTrainingTimelines
	 * @param absFileNameToAppend
	 * @param muCount
	 */
	public static void writeFeatDistributionForEachUsersTrainingTimelinesSlidingWindowWise(int userID,
			LinkedHashMap<Date, Timeline> userTrainingTimelines, String absFileNameToAppend, int muCount)
	{
		List<List<ActivityObject2018>> listOfWindows = splitDayTimelineToSlidingWindows(userTrainingTimelines, muCount);

		StringBuilder sb = new StringBuilder();

		for (List<ActivityObject2018> window : listOfWindows)
		{
			sb.append(userID + "," + window.stream().map(ao -> ao.getActivityName()).collect(Collectors.joining(">"))
					+ ",");
			for (ActivityObject2018 ao : window)
			{
				sb.append(ActivityObject2018.getHeaderForStringAllGeolifeWithNameForHeaded2(ao, ",") + ",");
			}
			// get activity name of last activity object
			sb.append(window.get(muCount - 1).getActivityName() + "\n");
		}
		WToFile.appendLineToFileAbs(sb.toString(), absFileNameToAppend);
	}

	public static void diffs(String absFileToRead)
	{
		List<String> actNames = ReadingFromFile.oneColumnReaderString(absFileToRead, ",", 0, false);
		Map<String, Long> counts = actNames.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		System.out.println("Count of actName = " + counts + "");

		List<Double> startTimes = ReadingFromFile.oneColumnReaderDouble(absFileToRead, ",", 1, false);
		List<String> startTimesString = ReadingFromFile.oneColumnReaderString(absFileToRead, ",", 1, false);

		System.out.println(StatsUtils.toStringDescriptiveStats(startTimes, "startTimes"));
		List<Double> timeInDayInHrs = startTimesString.stream().map(v -> new Long(v))
				.map(l -> DateTimeUtils.getTimeInDayInSecondsZoned(l, ZoneId.of("UTC"))).map(l -> (l / (60 * 60)))
				.map(l -> Double.valueOf(l.toString())).collect(Collectors.toList());

		System.out.println(StatsUtils.toStringDescriptiveStats(timeInDayInHrs, "timeInDayInHrs"));

		List<Double> durations = ReadingFromFile.oneColumnReaderDouble(absFileToRead, ",", 2, false);
		System.out.println(StatsUtils.toStringDescriptiveStats(durations, "durations"));

		List<Double> distTravelled = ReadingFromFile.oneColumnReaderDouble(absFileToRead, ",", 3, false);
		System.out.println(StatsUtils.toStringDescriptiveStats(distTravelled, "distTravelled"));

		List<Double> startGeoLat = ReadingFromFile.oneColumnReaderDouble(absFileToRead, ",", 4, false);
		System.out.println(StatsUtils.toStringDescriptiveStats(startGeoLat, "startGeoLat"));

		List<Double> startGeoLon = ReadingFromFile.oneColumnReaderDouble(absFileToRead, ",", 5, false);
		System.out.println(StatsUtils.toStringDescriptiveStats(startGeoLon, "startGeoLon"));

		List<Double> endGeoLat = ReadingFromFile.oneColumnReaderDouble(absFileToRead, ",", 5, false);
		System.out.println(StatsUtils.toStringDescriptiveStats(endGeoLat, "endGeoLat"));

		List<Double> endGeoLon = ReadingFromFile.oneColumnReaderDouble(absFileToRead, ",", 6, false);
		System.out.println(StatsUtils.toStringDescriptiveStats(endGeoLon, "endGeoLon"));

		List<Double> avgAltitude = ReadingFromFile.oneColumnReaderDouble(absFileToRead, ",", 7, false);
		System.out.println(StatsUtils.toStringDescriptiveStats(avgAltitude, "avgAltitude"));
		// List<Double> durations = ReadingFromFile.oneColumnReaderDouble(absFileToRead, ",", 3, false);
	}

}
