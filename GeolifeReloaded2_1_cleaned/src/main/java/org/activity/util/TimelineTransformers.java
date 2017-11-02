package org.activity.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Timeline;
import org.activity.stats.HilbertCurveUtils;
import org.activity.stats.TimelineStats;
import org.activity.ui.PopUps;

/**
 * 
 * @author gunjan
 *
 */
public class TimelineTransformers
{

	/**
	 * 
	 * @param ts
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> toTimeSeriesIntWithZeroValuedInvalids(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> r = new LinkedHashMap<>();

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Integer> dataToPut = new LinkedHashMap<>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				int value = -99;
				if (dataEntry.getValue().isInvalidActivityName())
				{
					value = 0;
				}
				else
				{
					value = TimelineStats.getMagnifiedIntCodeForActivityObject(dataEntry.getValue());
				}
				dataToPut.put(dataEntry.getKey(), value);
			}
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	/**
	 * Transform sequence of Activity Objects to sequence of Integer codes
	 * 
	 * @param ts
	 * @param validsOnly
	 *            to choose if only valid Activity Objects should be added
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> toIntsFromActivityObjects(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>>();

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Integer> dataToPut = new LinkedHashMap<Timestamp, Integer>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				int value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = TimelineStats.getMagnifiedIntCodeForActivityObject(dataEntry.getValue());
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(dataEntry.getKey(), value);
				}

			}
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	/**
	 * Putting at equally spaced time intervals
	 * 
	 * @param ts
	 * @param validsOnly
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> toIntsFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Integer> dataToPut = new LinkedHashMap<Timestamp, Integer>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				int value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = TimelineStats.getMagnifiedIntCodeForActivityObject(dataEntry.getValue());
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(time, value);
					time = new Timestamp(time.getTime() + 1000 * 60 * 60);
				}

			}
			// r.put(String.valueOf(Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()))) + 1, dataToPut);
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> toDurationsFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Long>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<Timestamp, Long>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				long value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = dataEntry.getValue().getDurationInSeconds();
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(time, value);
					time = new Timestamp(time.getTime() + 1000 * 60 * 60);
				}

			}
			// r.put(String.valueOf(Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()))) + 1, dataToPut);
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	/**
	 * 
	 * @param ts
	 * @param validsOnly
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> toStartTimeFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Long>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<Timestamp, Long>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				long value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = DateTimeUtils.getTimeInDayInSeconds(dataEntry.getValue().getStartTimestamp());
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(time, value);
					time = new Timestamp(time.getTime() + 1000 * 60 * 60);
				}

			}
			// r.put(String.valueOf(Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()))) + 1, dataToPut);
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> toDistanceTravelledFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Double>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Double> dataToPut = new LinkedHashMap<Timestamp, Double>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				double value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = dataEntry.getValue().getDistanceTravelled();
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(time, value);
					time = new Timestamp(time.getTime() + 1000 * 60 * 60);
				}

			}
			// r.put(String.valueOf(Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()))) + 1, dataToPut);
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> toAvgAltitudeFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Double>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Double> dataToPut = new LinkedHashMap<Timestamp, Double>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				double value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = Double.valueOf(dataEntry.getValue().getAvgAltitude());
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(time, value);
					time = new Timestamp(time.getTime() + 1000 * 60 * 60);
				}

			}
			// r.put(String.valueOf(Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()))) + 1, dataToPut);
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> toStartAltitudeFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Double>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Double> dataToPut = new LinkedHashMap<Timestamp, Double>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				double value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = Double.valueOf(dataEntry.getValue().getStartAltitude());
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(time, value);
					time = new Timestamp(time.getTime() + 1000 * 60 * 60);
				}

			}
			// r.put(String.valueOf(Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()))) + 1, dataToPut);
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> toEndAltitudeFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Double>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Double> dataToPut = new LinkedHashMap<Timestamp, Double>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				double value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = Double.valueOf(dataEntry.getValue().getEndAltitude());
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(time, value);
					time = new Timestamp(time.getTime() + 1000 * 60 * 60);
				}

			}
			// r.put(String.valueOf(Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()))) + 1, dataToPut);
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> toStartGeoCoordinatesFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Long>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<Timestamp, Long>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				long value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					double latitude1 = Double.parseDouble(dataEntry.getValue().getStartLatitude());
					double longitude1 = Double.parseDouble(dataEntry.getValue().getStartLongitude());

					long latitude1AsLong = (long) (latitude1 * Constant.decimalPlacesInGeocordinatesForComputations);
					long longitude1AsLong = (long) (longitude1 * Constant.decimalPlacesInGeocordinatesForComputations);
					value = HilbertCurveUtils.getCompactHilbertCurveIndex(latitude1AsLong, longitude1AsLong);
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(time, value);
					time = new Timestamp(time.getTime() + 1000 * 60 * 60);
				}

			}
			// r.put(String.valueOf(Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()))) + 1, dataToPut);
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> toEndGeoCoordinatesFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Long>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<Timestamp, Long>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				long value = -99;
				ActivityObject ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					double latitude1 = Double.parseDouble(dataEntry.getValue().getEndLatitude());
					double longitude1 = Double.parseDouble(dataEntry.getValue().getEndLongitude());

					long latitude1AsLong = (long) (latitude1 * Constant.decimalPlacesInGeocordinatesForComputations);
					long longitude1AsLong = (long) (longitude1 * Constant.decimalPlacesInGeocordinatesForComputations);
					value = HilbertCurveUtils.getCompactHilbertCurveIndex(latitude1AsLong, longitude1AsLong);
					// WritingToFile.appendLineToFile("Activity Name:" + dataEntry.getValue().getActivityName() + "," +
					// value, "ActivityCodeForSeries");
					dataToPut.put(time, value);
					time = new Timestamp(time.getTime() + 1000 * 60 * 60);
				}

			}
			// r.put(String.valueOf(Constant.getIndexOfUserID(Integer.valueOf(entry.getKey()))) + 1, dataToPut);
			r.put(entry.getKey(), dataToPut);
		}
		return r;
	}

	/**
	 * Transform sequence of Activity Objects to sequence of character(as String) codes
	 * 
	 * @param ts
	 * @param validsOnly
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, String>> toCharsFromActivityObjects(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, String>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, String>>();
		System.out.println("inside toCharsFromActivityObjects");
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, String> dataToPut = new LinkedHashMap<Timestamp, String>();

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				char value = 'X';
				ActivityObject ao = dataEntry.getValue();

				if (ao == null) // is ao is null, will happen if the timestamp corresponds to a place where an invalid
								// was there but has now been removed.
				{
					continue;
				}

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = dataEntry.getValue().getCharCode();
					dataToPut.put(dataEntry.getKey(), String.valueOf(value));
				}

			}
			r.put(entry.getKey(), dataToPut);
		}
		System.out.println("exiting toCharsFromActivityObjects");
		return r;
	}

	/**
	 * 
	 * @param ts
	 * @param validsOnly
	 * @return
	 */
	public static LinkedHashMap<String, String> toCharsFromActivityObjectsNoTimestamp(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, String> r = new LinkedHashMap<String, String>();
		System.out.println("inside toCharsFromActivityObjectsNoTimestamp");

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			StringBuilder dataToPut = new StringBuilder();
			// = new LinkedHashMap<Timestamp, String>();
			int numberOfAOs = entry.getValue().size();
			int numberOfNullAOs = 0;

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				String value = "X";
				ActivityObject ao = dataEntry.getValue();

				if (ao == null) // is ao is null, will happen if the timestamp corresponds to a place where an invalid
								// was there but has now been removed.
				{
					numberOfNullAOs++;
					continue;
				}

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = String.valueOf(dataEntry.getValue().getCharCode());
					dataToPut.append(value);
				}

			}

			System.out.println("User:" + entry.getKey() + " Num of AOs read:" + numberOfAOs + " number of null AOs="
					+ numberOfNullAOs + "  Length of timeline string:" + dataToPut.length());
			r.put(entry.getKey(), dataToPut.toString());
			// System.out.println(">>" + entry.getKey() + ":" + dataToPut.toString());
		}
		System.out.println("exiting toCharsFromActivityObjectsNoTimestamp");
		return r;
	}

	/**
	 * 
	 * @param ts
	 * @param validsOnly
	 * @return
	 */
	public static LinkedHashMap<String, String> toCharsFromActivityObjectsNoTimestamp2(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, String> r = new LinkedHashMap<String, String>();
		System.out.println("inside toCharsFromActivityObjectsNoTimestamp");

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject>> entry : ts.entrySet())
		{
			StringBuilder dataToPut = new StringBuilder();
			// = new LinkedHashMap<Timestamp, String>();
			int numberOfAOs = entry.getValue().size();
			int numberOfNullAOs = 0;

			for (Map.Entry<Timestamp, ActivityObject> dataEntry : entry.getValue().entrySet())
			{
				String value = "X";
				ActivityObject ao = dataEntry.getValue();

				if (ao == null) // is ao is null, will happen if the timestamp corresponds to a place where an invalid
								// was there but has now been removed.
				{
					numberOfNullAOs++;
					continue;
				}

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = String.valueOf(dataEntry.getValue().getCharCode());
					dataToPut.append(value);
				}

			}

			System.out.println("User:" + entry.getKey() + " Num of AOs read:" + numberOfAOs + " number of null AOs="
					+ numberOfNullAOs + "  Length of timeline string:" + dataToPut.length());
			r.put(entry.getKey(), dataToPut.toString());
			// System.out.println(">>" + entry.getKey() + ":" + dataToPut.toString());
		}
		System.out.println("exiting toCharsFromActivityObjectsNoTimestamp");
		return r;
	}

	/**
	 * 
	 * @param dayTimelines
	 * @param intervalSizeInSeconds
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> transformToEqualIntervalTimeSeriesDayWise(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsersDayTimelines, int intervalSizeInSeconds)
	{
		System.out.println("inside transformToEqualIntervalTimeSeriesDayWise");
		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> timeSeries = new LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>>();

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : allUsersDayTimelines.entrySet())
		{
			String userID = entry.getKey();
			LinkedHashMap<Date, Timeline> dayTimelines = entry.getValue();

			LinkedHashMap<Timestamp, ActivityObject> dataPoints = new LinkedHashMap<Timestamp, ActivityObject>();

			for (Map.Entry<Date, Timeline> entryForDay : dayTimelines.entrySet())
			{
				Timeline dayTimeline = entryForDay.getValue();

				Timestamp cursorTimestamp = dayTimeline.getActivityObjectAtPosition(0).getStartTimestamp();
				Timestamp endTimestamp = dayTimeline
						.getActivityObjectAtPosition(dayTimeline.getActivityObjectsInDay().size() - 1)
						.getEndTimestamp();

				while (cursorTimestamp.before(endTimestamp))
				{
					ActivityObject aoToPut = dayTimeline.getActivityObjectAtTime(cursorTimestamp); // aoToPut is null if
																									// there is no ao at
																									// that time, will
																									// happen when an
																									// invalid
																									// was remove from
																									// that
																									// position
					dataPoints.put(cursorTimestamp, aoToPut);
					cursorTimestamp = new Timestamp(cursorTimestamp.getTime() + intervalSizeInSeconds * 1000); // increment
																												// by a
																												// intervalSizeInSeconds
				}
				dataPoints.put(endTimestamp, dayTimeline.getActivityObjectAtTime(endTimestamp));
			}

			timeSeries.put(userID, dataPoints);
		}
		System.out.println("exiting transformToEqualIntervalTimeSeriesDayWise");
		return timeSeries;
	}

	/**
	 * This is NOT equal interval time series, this is a sequence of activity objects, each with their start timestamp
	 * 
	 * @param dayTimelines
	 * @param intervalSizeInSeconds
	 * @return
	 */
	// StartTimestamp, Activity Object
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> transformToSequenceDayWise(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsersDayTimelines)
	{
		System.out.println("inside transformToSequenceDayWise");
		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> sequenceOfAOs = new LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>>();

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : allUsersDayTimelines.entrySet())
		{
			String userID = entry.getKey();
			LinkedHashMap<Date, Timeline> dayTimelines = entry.getValue();

			LinkedHashMap<Timestamp, ActivityObject> dataPoints = new LinkedHashMap<Timestamp, ActivityObject>();

			int countOfAOs = 0;

			for (Map.Entry<Date, Timeline> entryForDay : dayTimelines.entrySet())
			{
				Timeline dayTimeline = entryForDay.getValue();
				for (int i = 0; i < dayTimeline.getActivityObjectsInDay().size(); i++)
				{
					ActivityObject ao = dayTimeline.getActivityObjectAtPosition(i);
					dataPoints.put(ao.getStartTimestamp(), ao);
					countOfAOs++;
				}
			}
			System.out.println(
					"Number of AOs for user " + userID + " = " + countOfAOs + " , dataPoints" + dataPoints.size());

			if (countOfAOs != dataPoints.size())
			{
				System.err.println(
						"Error in org.activity.stats.TimelineStats.transformToSequenceDayWise() countOfAOs != dataPoints.size()");
			}
			sequenceOfAOs.put(userID, dataPoints);
		}
		System.out.println("exiting transformToSequenceDayWise");
		return sequenceOfAOs;
	}

	/**
	 * 
	 * @param t
	 * @param intervalSizeInSeconds
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> transformToEqualIntervalTimeSeries(
			LinkedHashMap<String, Timeline> timelines, int intervalSizeInSeconds)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> timeSeries = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> entry : timelines.entrySet())
		{
			String userID = entry.getKey();
			Timeline timeline = entry.getValue();

			LinkedHashMap<Timestamp, ActivityObject> dataPoints = new LinkedHashMap<>();

			Timestamp cursorTimestamp = timeline.getActivityObjectAtPosition(0).getStartTimestamp();
			Timestamp endTimestamp = timeline
					.getActivityObjectAtPosition(timeline.getActivityObjectsInTimeline().size() - 1).getEndTimestamp();

			while (cursorTimestamp.before(endTimestamp))
			{
				dataPoints.put(cursorTimestamp, timeline.getActivityObjectAtTime(cursorTimestamp));
				cursorTimestamp = new Timestamp(cursorTimestamp.getTime() + intervalSizeInSeconds * 1000);
				// increment by a second
			}
			dataPoints.put(endTimestamp, timeline.getActivityObjectAtTime(endTimestamp));

			timeSeries.put(userID, dataPoints);
		}

		return timeSeries;
	}

	/**
	 * 
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 * @param userAllDatesTimeslines
	 */
	public static void writeUserActNamesSeqAsCode(int userID, LinkedHashMap<Date, Timeline> userTrainingTimelines,
			LinkedHashMap<Date, Timeline> userTestTimelines, LinkedHashMap<Date, Timeline> userAllDatesTimeslines)
	{
		StringBuilder sbTraining = new StringBuilder(String.valueOf(userID) + ",");
		userTrainingTimelines.entrySet().stream()
				.forEachOrdered(tl -> sbTraining.append(tl.getValue().getActivityObjectsAsStringCode(",")));
		sbTraining.append("\n");
		WritingToFile.appendLineToFileAbsolute(sbTraining.toString(),
				Constant.getCommonPath() + "TrainingTimelinesAsSeqOfCodes.csv");
	}

	/**
	 * 
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 * @param userAllDatesTimeslines
	 */
	public static void writeUserActNamesSeqAsNames(int userID, LinkedHashMap<Date, Timeline> userTrainingTimelines,
			LinkedHashMap<Date, Timeline> userTestTimelines, LinkedHashMap<Date, Timeline> userAllDatesTimeslines)
	{
		StringBuilder sbTraining = new StringBuilder(String.valueOf(userID) + ",");

		userTrainingTimelines.entrySet().stream()
				.forEachOrdered(tl -> sbTraining.append(timelineToSeqOfActNames(tl.getValue(), ",")));

		sbTraining.append("\n");
		WritingToFile.appendLineToFileAbsolute(sbTraining.toString(),
				Constant.getCommonPath() + "TrainingTimelinesAsSeqOfCodes.csv");
	}

	/**
	 * 
	 * @param timeline
	 * @param catIDNameDictionary
	 * @param delimiter
	 * @return seq of activity names (actual category names extracted from the catid name dictionary) delimited by the
	 *         given delimiter
	 */
	public static String timelineToSeqOfActNames(Timeline timeline, String delimiter)
	{
		StringBuilder s = new StringBuilder();
		ArrayList<ActivityObject> aosInTimeline = timeline.getActivityObjectsInTimeline();

		TreeMap<Integer, String> catIDNameDictionary = DomainConstants.catIDNameDictionary;

		for (ActivityObject ao : aosInTimeline)
		{
			String catIDName = catIDNameDictionary.get(Integer.valueOf(ao.getActivityName()));
			if (catIDName == null || catIDName.length() == 0)
			{
				System.err.println(
						PopUps.getTracedErrorMsg("Error: Didnt find cat id name for cat id :" + ao.getActivityName()));
			}
			s.append(catIDName).append(delimiter);
		}

		// aosInTimeline.stream()
		// .forEachOrdered(ao -> s.append(catIDNameDictionary.get(Integer.valueOf(ao.getActivityName()))));

		return s.toString();
	}

	/**
	 * 
	 * @param timeline
	 * @param catIDNameDictionary
	 * @param delimiter
	 * @return seq of activity names (actual category names extracted from the catid name dictionary) delimited by the
	 *         given delimiter
	 */
	public static String timelineToSeqOfActIDs(Timeline timeline, String delimiter)
	{
		StringBuilder s = new StringBuilder();
		ArrayList<ActivityObject> aosInTimeline = timeline.getActivityObjectsInTimeline();
		// TreeMap<Integer, String> catIDNameDictionary = Constant.catIDNameDictionary;
		for (ActivityObject ao : aosInTimeline)
		{
			Integer catID = Integer.valueOf(ao.getActivityName());
			if (catID == null || catID < 0)
			{
				System.err.println(PopUps.getTracedErrorMsg(
						"Error: atID == null || catID < 0:  cat id :" + catID + " for actname" + ao.getActivityName()));
			}
			s.append(catID).append(delimiter);
		}

		// aosInTimeline.stream()
		// .forEachOrdered(ao -> s.append(catIDNameDictionary.get(Integer.valueOf(ao.getActivityName()))));

		return s.toString();
	}

	/**
	 * 
	 * @param timeline
	 * @param catIDNameDictionary
	 * @param delimiter
	 * @return seq of activity names (actual category names extracted from the catid name dictionary) delimited by the
	 *         given delimiter
	 */
	public static ArrayList<Integer> timelineToSeqOfActIDs(ArrayList<ActivityObject> givenAOs)
	{
		return (ArrayList<Integer>) givenAOs.stream().map(ao -> Integer.valueOf(ao.getActivityName()))
				.collect(Collectors.toList());
	}

	/**
	 * 
	 * @param givenAOs
	 * @return seq of activity names (actual category names extracted from the catid name dictionary) delimited by the
	 *         given delimiter
	 */
	public static ArrayList<Integer> timelineToSeqOfActIDs(ArrayList<ActivityObject> givenAOs, boolean verbose)
	{

		ArrayList<Integer> res = (ArrayList<Integer>) givenAOs.stream().map(ao -> Integer.valueOf(ao.getActivityName()))
				.collect(Collectors.toList());

		if (verbose)
		{
			StringBuilder sb = new StringBuilder();
			givenAOs.stream().forEachOrdered(ao -> sb.append(ao.getActivityID() + ">>"));
			sb.append("\n");
			res.stream().forEachOrdered(i -> sb.append(i + ">>"));
			System.out.println("---timelineToSeqOfActIDs verbose-\n" + sb.toString() + "\n-----\n");
		}

		return res;
	}
}
