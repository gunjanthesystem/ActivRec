package org.activity.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.CheckinEntry;
import org.activity.objects.CheckinEntryV2;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Timeline;
import org.activity.stats.HilbertCurveUtils;
import org.activity.stats.TimelineStats;
import org.activity.ui.PopUps;

import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2CharOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

/**
 * 
 * @author gunjan
 *
 */
public class TimelineTransformers
{

	/**
	 * @since 18 July
	 */
	public static String getActivityGDGuidingRecomm(PrimaryDimension givenDimension,
			ArrayList<ActivityObject2018> activitiesGuidingRecomm)
	{
		StringBuilder res = new StringBuilder();

		for (ActivityObject2018 ae : activitiesGuidingRecomm)
		{
			res = StringUtils.fCat(res, ">>", ae.getGivenDimensionVal("|", givenDimension));
			// res.append(">>" + ae.getActivityName());
		}
		return res.toString();
	}

	/**
	 * 
	 * @param ts
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> toTimeSeriesIntWithZeroValuedInvalids(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> r = new LinkedHashMap<>();

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Integer> dataToPut = new LinkedHashMap<>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>>();

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Integer> dataToPut = new LinkedHashMap<Timestamp, Integer>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				int value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Integer> dataToPut = new LinkedHashMap<Timestamp, Integer>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				int value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Long>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<Timestamp, Long>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				long value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Long>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<Timestamp, Long>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				long value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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

	/**
	 * 
	 * @param ts
	 * @param validsOnly
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> toStartTimeFromActivityObjectsDummyTime2(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Long>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<Timestamp, Long>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				long value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = DateTimeUtils.getTimeInDayInSecondsZoned(dataEntry.getValue().getStartTimestampInms(),
							dataEntry.getValue().getTimeZoneId());
					// DateTimeUtils.getTimeInDayInSeconds(dataEntry.getValue().getStartTimestamp());
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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Double>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Double> dataToPut = new LinkedHashMap<Timestamp, Double>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				double value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Double>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Double> dataToPut = new LinkedHashMap<Timestamp, Double>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				double value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Double>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Double> dataToPut = new LinkedHashMap<Timestamp, Double>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				double value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Double>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Double> dataToPut = new LinkedHashMap<Timestamp, Double>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				double value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Long>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<Timestamp, Long>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				long value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, Long>>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<Timestamp, Long>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				long value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

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

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> toLocationGridsFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				Long value = -99L;
				ActivityObject2018 ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = dataEntry.getValue().getGridID();
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

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> toPopularityFromActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> r = new LinkedHashMap<>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Integer> dataToPut = new LinkedHashMap<>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				Integer value = -99;
				ActivityObject2018 ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = dataEntry.getValue().getCheckins_count();
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

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> toDistInMFromPrevActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Double>> r = new LinkedHashMap<>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Double> dataToPut = new LinkedHashMap<>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				Double value = -99d;
				ActivityObject2018 ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = dataEntry.getValue().getDistanceInMFromPrev();
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

	public static LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> toDurInSecFromPrevActivityObjectsDummyTime(
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, Long>> r = new LinkedHashMap<>();
		Timestamp time = new Timestamp(9, 1, 1, 1, 1, 1, 0);
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, Long> dataToPut = new LinkedHashMap<>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				Long value = -99L;
				ActivityObject2018 ao = dataEntry.getValue();

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = dataEntry.getValue().getDurationInSecondsFromPrev();
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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, String>> r = new LinkedHashMap<String, LinkedHashMap<Timestamp, String>>();
		System.out.println("inside toCharsFromActivityObjects");
		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			LinkedHashMap<Timestamp, String> dataToPut = new LinkedHashMap<Timestamp, String>();

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				char value = 'X';
				ActivityObject2018 ao = dataEntry.getValue();

				if (ao == null) // is ao is null, will happen if the timestamp corresponds to a place where an invalid
								// was there but has now been removed.
				{
					continue;
				}

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = dataEntry.getValue().getCharCodeFromActID();
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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, String> r = new LinkedHashMap<String, String>();
		System.out.println("inside toCharsFromActivityObjectsNoTimestamp");

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			StringBuilder dataToPut = new StringBuilder();
			// = new LinkedHashMap<Timestamp, String>();
			int numberOfAOs = entry.getValue().size();
			int numberOfNullAOs = 0;

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				String value = "X";
				ActivityObject2018 ao = dataEntry.getValue();

				if (ao == null) // is ao is null, will happen if the timestamp corresponds to a place where an invalid
								// was there but has now been removed.
				{
					numberOfNullAOs++;
					continue;
				}

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = String.valueOf(dataEntry.getValue().getCharCodeFromActID());
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
			LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> ts, boolean validsOnly)
	{
		LinkedHashMap<String, String> r = new LinkedHashMap<String, String>();
		System.out.println("inside toCharsFromActivityObjectsNoTimestamp");

		for (Map.Entry<String, LinkedHashMap<Timestamp, ActivityObject2018>> entry : ts.entrySet())
		{
			StringBuilder dataToPut = new StringBuilder();
			// = new LinkedHashMap<Timestamp, String>();
			int numberOfAOs = entry.getValue().size();
			int numberOfNullAOs = 0;

			for (Map.Entry<Timestamp, ActivityObject2018> dataEntry : entry.getValue().entrySet())
			{
				String value = "X";
				ActivityObject2018 ao = dataEntry.getValue();

				if (ao == null) // is ao is null, will happen if the timestamp corresponds to a place where an invalid
								// was there but has now been removed.
				{
					numberOfNullAOs++;
					continue;
				}

				if (!validsOnly || (ao.isInvalidActivityName() == false))
				{
					value = String.valueOf(dataEntry.getValue().getCharCodeFromActID());
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
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> transformToEqualIntervalTimeSeriesDayWise(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsersDayTimelines, int intervalSizeInSeconds)
	{
		System.out.println("inside transformToEqualIntervalTimeSeriesDayWise");
		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> timeSeries = new LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>>();

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : allUsersDayTimelines.entrySet())
		{
			String userID = entry.getKey();
			LinkedHashMap<Date, Timeline> dayTimelines = entry.getValue();

			LinkedHashMap<Timestamp, ActivityObject2018> dataPoints = new LinkedHashMap<Timestamp, ActivityObject2018>();

			for (Map.Entry<Date, Timeline> entryForDay : dayTimelines.entrySet())
			{
				Timeline dayTimeline = entryForDay.getValue();

				Timestamp cursorTimestamp = dayTimeline.getActivityObjectAtPosition(0).getStartTimestamp();
				Timestamp endTimestamp = dayTimeline
						.getActivityObjectAtPosition(dayTimeline.getActivityObjectsInDay().size() - 1)
						.getEndTimestamp();

				while (cursorTimestamp.before(endTimestamp))
				{
					ActivityObject2018 aoToPut = dayTimeline.getActivityObjectAtTime(cursorTimestamp); // aoToPut is
																										// null if
																										// there is no
																										// ao at
																										// that time,
																										// will
																										// happen when
																										// an
																										// invalid
																										// was remove
																										// from
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
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> transformToSequenceDayWise(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> allUsersDayTimelines)
	{
		System.out.println("inside transformToSequenceDayWise");
		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> sequenceOfAOs = new LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>>();

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : allUsersDayTimelines.entrySet())
		{
			String userID = entry.getKey();
			LinkedHashMap<Date, Timeline> dayTimelines = entry.getValue();

			LinkedHashMap<Timestamp, ActivityObject2018> dataPoints = new LinkedHashMap<Timestamp, ActivityObject2018>();

			int countOfAOs = 0;

			for (Map.Entry<Date, Timeline> entryForDay : dayTimelines.entrySet())
			{
				Timeline dayTimeline = entryForDay.getValue();
				for (int i = 0; i < dayTimeline.getActivityObjectsInDay().size(); i++)
				{
					ActivityObject2018 ao = dayTimeline.getActivityObjectAtPosition(i);
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
	public static LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> transformToEqualIntervalTimeSeries(
			LinkedHashMap<String, Timeline> timelines, int intervalSizeInSeconds)
	{
		LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject2018>> timeSeries = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> entry : timelines.entrySet())
		{
			String userID = entry.getKey();
			Timeline timeline = entry.getValue();

			LinkedHashMap<Timestamp, ActivityObject2018> dataPoints = new LinkedHashMap<>();

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
	 * @param timeline
	 * @param catIDNameDictionary
	 * @param delimiter
	 * @return seq of activity names (actual category names extracted from the catid name dictionary) delimited by the
	 *         given delimiter
	 */
	public static String timelineToSeqOfActNames(Timeline timeline, String delimiter)
	{
		StringBuilder s = new StringBuilder();
		ArrayList<ActivityObject2018> aosInTimeline = timeline.getActivityObjectsInTimeline();

		TreeMap<Integer, String> catIDNameDictionary = DomainConstants.catIDNameDictionary;

		for (ActivityObject2018 ao : aosInTimeline)
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
		ArrayList<ActivityObject2018> aosInTimeline = timeline.getActivityObjectsInTimeline();
		// TreeMap<Integer, String> catIDNameDictionary = Constant.catIDNameDictionary;
		for (ActivityObject2018 ao : aosInTimeline)
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
	public static ArrayList<Integer> timelineToSeqOfActIDs(ArrayList<ActivityObject2018> givenAOs)
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
	public static ArrayList<Integer> timelineToSeqOfActIDsV0(ArrayList<ActivityObject2018> givenAOs, boolean verbose)
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

	/**
	 * Convert list of activity objects to a list of activity IDs.
	 * <p>
	 * Created to use for each loop instead of streams as stream was running out memory.
	 * 
	 * @param givenAOs
	 * @return seq of activity names (actual category names extracted from the catid name dictionary) delimited by the
	 *         given delimiter
	 * @since 15 Dec 2017
	 */
	public static ArrayList<Integer> listOfActObjsToListOfActIDs(ArrayList<ActivityObject2018> givenAOs,
			boolean verbose)
	{
		ArrayList<Integer> seqOfActIDs2 = new ArrayList<>(givenAOs.size());
		// ArrayList<Integer> seqOfActIDs = new ArrayList<>(givenAOs.size());
		// for (ActivityObject ao : givenAOs){ seqOfActIDs.add(Integer.valueOf(ao.getActivityName()));}

		for (ActivityObject2018 ao : givenAOs)
		{
			seqOfActIDs2.add(ao.getActivityID());
		}

		// start of sanity check Passed
		// if (true){if (seqOfActIDs.equals(seqOfActIDs2)){
		// System.out.println("Sanity check Dec 15_2 passed");
		// }else{System.out.println("Sanity check Dec 15_2 failed");} }
		// end of sanity check

		if (verbose)
		{
			StringBuilder sb = new StringBuilder();
			givenAOs.stream().forEachOrdered(ao -> sb.append(ao.getActivityID() + ">>"));
			sb.append("\n");
			seqOfActIDs2.stream().forEachOrdered(i -> sb.append(i + ">>"));
			System.out.println("---timelineToSeqOfActIDs verbose-\n" + sb.toString() + "\n-----\n");
		}

		return seqOfActIDs2;
	}

	/**
	 * Convert list of activity objects to a list of given dimensionVals
	 * <p>
	 * Created to use for each loop instead of streams as stream was running out memory.
	 * <p>
	 * <font color="red"><b>Alert!! if any ActivityObject has multiple given dimension val, we select only the first
	 * one.</b></font>
	 * 
	 * @param givenAOs
	 * @param verbose
	 * @param givenDimension
	 * 
	 * @return seq of activity names (actual category names extracted from the catid name dictionary) delimited by the
	 *         given delimiter
	 * @since 5 Aug 2019
	 */
	public static ArrayList<Integer> listOfActObjsToListOfGivenDimensionVals(ArrayList<ActivityObject2018> givenAOs,
			boolean verbose, PrimaryDimension givenDimension)
	{
		ArrayList<Integer> seqOfActIDs2 = new ArrayList<>(givenAOs.size());
		// ArrayList<Integer> seqOfActIDs = new ArrayList<>(givenAOs.size());
		// for (ActivityObject ao : givenAOs){ seqOfActIDs.add(Integer.valueOf(ao.getActivityName()));}
		int countOfActObjsWithMultipleGivenDimensionVals = 0;

		for (ActivityObject2018 ao : givenAOs)
		{
			ArrayList<Integer> givenDimensionVals = ao.getGivenDimensionVal(givenDimension);
			if (givenDimensionVals.size() > 1)
			{
				countOfActObjsWithMultipleGivenDimensionVals += 1;
			}

			seqOfActIDs2.add(givenDimensionVals.get(0));// select only the first val
		}

		// start of sanity check Passed
		// if (true){if (seqOfActIDs.equals(seqOfActIDs2)){
		// System.out.println("Sanity check Dec 15_2 passed");
		// }else{System.out.println("Sanity check Dec 15_2 failed");} }
		// end of sanity check
		if (countOfActObjsWithMultipleGivenDimensionVals > 0)
		{
			System.out.println(
					"DebugAug5: Warning in listOfActObjsToListOfGivenDimensionVals: countOfActObjsWithMultipleGivenDimensionVals(>0)="
							+ countOfActObjsWithMultipleGivenDimensionVals);
		}

		if (verbose)
		{
			StringBuilder sb = new StringBuilder();
			givenAOs.stream().forEachOrdered(ao -> sb.append(ao.getGivenDimensionVal("_", givenDimension) + ">>"));
			sb.append("\n");
			seqOfActIDs2.stream().forEachOrdered(i -> sb.append(i + ">>"));
			System.out.println("---timelineToSeqOfActIDs verbose-\n" + sb.toString() + "\n-----\n");
		}

		return seqOfActIDs2;
	}

	/**
	 * 
	 * @param givenAOs
	 * @param verbose
	 * @param actIDCharCodeMap
	 *            (expected get it from Constant class)
	 * @return
	 * @since 14 June 2018
	 */
	public static ArrayList<Character> listOfActObjsToListOfCharCodesFromActIDs(ArrayList<ActivityObject2018> givenAOs,
			boolean verbose, Int2CharOpenHashMap actIDCharCodeMap)
	{
		ArrayList<Character> seqOfActIDs2 = new ArrayList<>(givenAOs.size());
		// ArrayList<Integer> seqOfActIDs = new ArrayList<>(givenAOs.size());
		// for (ActivityObject ao : givenAOs){ seqOfActIDs.add(Integer.valueOf(ao.getActivityName()));}

		for (ActivityObject2018 ao : givenAOs)
		{
			seqOfActIDs2.add(actIDCharCodeMap.get(ao.getActivityID()));
			// seqOfActIDs2.add(ao.getCharCodeFromActID());
		}

		// start of sanity check Passed
		// if (true){if (seqOfActIDs.equals(seqOfActIDs2)){
		// System.out.println("Sanity check Dec 15_2 passed");
		// }else{System.out.println("Sanity check Dec 15_2 failed");} }
		// end of sanity check

		if (verbose)
		{
			StringBuilder sb = new StringBuilder();
			givenAOs.stream().forEachOrdered(ao -> sb.append(ao.getActivityID() + ">>"));
			sb.append("\n");
			seqOfActIDs2.stream().forEachOrdered(i -> sb.append(i + ">>"));
			System.out.println("---listOfActObjsToListOfCharCodesFromActIDs verbose-\n" + sb.toString() + "\n-----\n");
		}

		return seqOfActIDs2;
	}

	/**
	 * 
	 * @param givenAOs
	 * @param verbose
	 * @param actIDCharCodeMap
	 *            (expected get it from Constant class)
	 * @return
	 * @since 14 June 2018
	 */
	public static ArrayList<Integer> listOfCharCodesToActIDs(List<Character> charCodes, boolean verbose,
			Char2IntOpenHashMap charCodeActIDMap)
	{
		ArrayList<Integer> seqOfActIDs = new ArrayList<>(charCodes.size());
		// ArrayList<Integer> seqOfActIDs = new ArrayList<>(givenAOs.size());
		// for (ActivityObject ao : givenAOs){ seqOfActIDs.add(Integer.valueOf(ao.getActivityName()));}

		for (Character c : charCodes)
		{
			seqOfActIDs.add(charCodeActIDMap.get(c.charValue()));
			// seqOfActIDs2.add(ao.getCharCodeFromActID());
		}

		// start of sanity check Passed
		// if (true){if (seqOfActIDs.equals(seqOfActIDs2)){
		// System.out.println("Sanity check Dec 15_2 passed");
		// }else{System.out.println("Sanity check Dec 15_2 failed");} }
		// end of sanity check

		if (verbose)
		{
			StringBuilder sb = new StringBuilder();
			charCodes.stream().forEachOrdered(c -> sb.append(c.charValue() + ">>"));
			sb.append("\n");
			seqOfActIDs.stream().forEachOrdered(i -> sb.append(i + ">>"));
			System.out.println("---listOfCharCodesToActIDs verbose-\n" + sb.toString() + "\n-----\n");
		}

		return seqOfActIDs;
	}

	/**
	 * Cleaned , expunge all invalid activity objects and rearrange according to user id order in Constant.userID for
	 * the current dataset </br>
	 * <font color="red">NOT NEED ANYMORE AS IT HAS BEEN BROKEN DOWN INTO SMALLER SINGLE PURPOSE FUNCTIONS</font>
	 * 
	 * @param usersTimelines
	 * @return
	 */
	public static LinkedHashMap<String, Timeline> dayTimelinesToCleanedExpungedRearrangedTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersTimelines)
	{
		LinkedHashMap<String, Timeline> cleanedERTimelines = new LinkedHashMap<>();
		System.out.println("inside dayTimelinesToCleanedExpungedRearrangedTimelines()");

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			String userID = usersTimelinesEntry.getKey();
			LinkedHashMap<Date, Timeline> userDayTimelines = usersTimelinesEntry.getValue();

			userDayTimelines = TimelineTrimmers.cleanUserDayTimelines(userDayTimelines);

			// converts the day time to continuous dayless //// new Timeline(userDayTimelines);
			Timeline timelineForUser = dayTimelinesToATimeline(userDayTimelines, false, true);

			timelineForUser = TimelineTrimmers.expungeInvalids(timelineForUser); // expunges invalid activity objects

			cleanedERTimelines.put(userID, timelineForUser);
		}
		System.out.println("\t" + cleanedERTimelines.size() + " timelines created");
		cleanedERTimelines = TimelineTransformers.rearrangeTimelinesOrderForDataset(cleanedERTimelines);
		System.out.println("\t" + cleanedERTimelines.size() + " timelines created");
		return cleanedERTimelines;
	}

	////
	/**
	 * 
	 * shouldBelongToSingleDay: false
	 * <p>
	 * shouldBelongToSingleUser: true
	 * 
	 * @param usersTimelines
	 * @return LinkedHashMap<User ID as String, Timeline of the user with user id as integer as timeline id>
	 */
	public static LinkedHashMap<String, Timeline> dayTimelinesToTimelines(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersTimelines)
	{
		LinkedHashMap<String, Timeline> timelines = new LinkedHashMap<>();
		if (usersTimelines.size() == 0 || usersTimelines == null)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in dayTimelinesToTimelines(): userTimeline.size = " + usersTimelines.size()));
		}
		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : usersTimelines.entrySet())
		{
			Timeline timelinesOfAllDatesCombined = dayTimelinesToATimeline(entry.getValue(), false, true);
			timelines.put(entry.getKey(), timelinesOfAllDatesCombined);
			// timelines.put(entry.getKey(), new Timeline(entry.getValue(), Integer.valueOf(entry.getKey())));
		}
		return timelines;
	}

	/**
	 * Creates a Timeline consisting of the Activity Objects from the given LinkedHashMap of UserDay Timelines thus
	 * essentially converts mulitple day timelines into a single continuous single timeline
	 * 
	 * @param dayTimelines
	 *            LinkedHashMap of UserDayTimelines
	 * 
	 * @param shouldBelongToSingleDay
	 *            should the Timeline to be returned belong to a single day
	 * @param shouldBelongToSingleUser
	 *            should the Timeline to be returned belong to a single user
	 */
	public static Timeline dayTimelinesToATimeline(LinkedHashMap<Date, Timeline> dayTimelines,
			boolean shouldBelongToSingleDay, boolean shouldBelongToSingleUser)
	{
		long dt = System.currentTimeMillis();
		Timeline concatenatedTimeline = null;

		ArrayList<ActivityObject2018> allActivityObjects = new ArrayList<>();
		// ArrayList<ActivityObject> allActivityObjectsDummy = new ArrayList<>();

		// long t1 = System.nanoTime();
		// TODO: this method is a signficant performance eater, find way to optimise it.
		for (Map.Entry<Date, Timeline> entryOneDayTimeline : dayTimelines.entrySet())
		{
			allActivityObjects.addAll(entryOneDayTimeline.getValue().getActivityObjectsInTimeline());
		}
		// long t2 = System.nanoTime();

		// Start of dummy: was trying Collections.addAll if it gave better performance but it wasnt better
		// for (Map.Entry<Date, Timeline> entryOneDayTimeline : dayTimelines.entrySet())
		// {
		// ArrayList<ActivityObject> aosInThisDayTimeline = entryOneDayTimeline.getValue()
		// .getActivityObjectsInTimeline();
		// Collections.addAll(allActivityObjectsDummy,
		// aosInThisDayTimeline.toArray(new ActivityObject[aosInThisDayTimeline.size()]));
		// }
		// long t3 = System.nanoTime();

		// System.out.println("Approach original: " + (t2 - t1) + " ns");
		// System.out.println("Approach new : " + (t3 - t2) + " ns");
		// System.out.println("% change : " + 100.0 * ((t3 - t2) - (t2 - t1)) / (t2 - t1));
		// System.out.println("allActivityObjectsDummy.equals(allActivityObjects) = "
		// + allActivityObjectsDummy.equals(allActivityObjects));
		// end of dummy

		if (allActivityObjects.size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in 'dayTimelinesToATimeline' creating Timeline: Empty Activity Objects provided"));
			System.exit(-1);
		}

		else
		{
			concatenatedTimeline = new Timeline(allActivityObjects, shouldBelongToSingleDay, shouldBelongToSingleUser);
			if (VerbosityConstants.verbose)
			{
				System.out.println("Creating timelines for " + dayTimelines.size() + " daytimelines  takes "
						+ (System.currentTimeMillis() - dt) + " ms");
			}
		}
		return concatenatedTimeline;
	}

	//
	/**
	 * Same as dayTimelinesToATimeline but with String keys instead of Date
	 * <p>
	 * Creates a Timeline consisting of the Activity Objects from the given LinkedHashMap of UserDay Timelines thus
	 * essentially converts mulitple day timelines into a single continuous single timeline
	 * 
	 * @param dayTimelines
	 *            LinkedHashMap of UserDayTimelines
	 * 
	 * @param shouldBelongToSingleDay
	 *            should the Timeline to be returned belong to a single day
	 * @param shouldBelongToSingleUser
	 *            should the Timeline to be returned belong to a single user
	 * @since 12 June 2017
	 */
	public static Timeline dayTimelinesToATimeline2(LinkedHashMap<String, Timeline> dayTimelines,
			boolean shouldBelongToSingleDay, boolean shouldBelongToSingleUser)
	{
		long dt = System.currentTimeMillis();
		Timeline concatenatedTimeline = null;

		ArrayList<ActivityObject2018> allActivityObjects = new ArrayList<>();

		for (Map.Entry<String, Timeline> entry : dayTimelines.entrySet())
		{
			allActivityObjects.addAll(entry.getValue().getActivityObjectsInTimeline());
		}

		if (allActivityObjects.size() == 0)
		{
			PopUps.printTracedErrorMsgWithExit("creating Timeline: Empty Activity Objects provided");
		}

		else
		{
			concatenatedTimeline = new Timeline(allActivityObjects, shouldBelongToSingleDay, shouldBelongToSingleUser);
			if (VerbosityConstants.verbose)
			{
				System.out.println("Creating timelines for " + dayTimelines.size() + " daytimelines  takes "
						+ (System.currentTimeMillis() - dt) / 1000 + " secs");
			}
		}
		return concatenatedTimeline;
	}

	/**
	 * For sanity checking convertTimewiseMapToDatewiseMap()
	 * 
	 * @param dft
	 *            timezone to use
	 */
	public static void checkConvertTimewiseMapToDatewiseMap2(TimeZone tz)
	{
		LinkedHashMap<String, TreeMap<Timestamp, Integer>> timewiseMap = new LinkedHashMap<>();
		LinkedHashMap<String, TreeMap<Date, ArrayList<Integer>>> dateWiseMap = new LinkedHashMap<>();

		for (int i = 0; i <= 5; i++)
		{
			TreeMap<Timestamp, Integer> timeMap = new TreeMap<>();

			Instant instant = Instant.now();
			Timestamp ts = Timestamp.from(instant);

			for (int j = 0; j <= 6; j++)
			{
				timeMap.put(new Timestamp(ts.getTime() + (j * 60000000)), j);
			}
			timewiseMap.put("User" + i, timeMap);
		}

		for (Entry<String, TreeMap<Timestamp, Integer>> userW : timewiseMap.entrySet())
		{
			System.out.println("timewise:\nUser = " + userW.getKey());

			for (Entry<Timestamp, Integer> timeW : userW.getValue().entrySet())
			{
				System.out.println(" timew entry:" + timeW.getKey() + "-" + timeW.getValue());
			}
		}

		dateWiseMap = convertTimewiseMapToDatewiseMap2(timewiseMap, tz);

		for (Entry<String, TreeMap<Date, ArrayList<Integer>>> userW : dateWiseMap.entrySet())
		{
			System.out.println("Datewise:\nUser = " + userW.getKey());

			for (Entry<Date, ArrayList<Integer>> timeW : userW.getValue().entrySet())
			{
				System.out.println("Date = " + timeW.getKey().toString());
				// System.out.println("Date = " + timeW.getKey().toGMTString() + " " + timeW.getKey().toLocaleString());
				System.out.println(" datew entry:" + timeW.getKey() + "-" + timeW.getValue().toString());
			}
		}

	}

	/**
	 * For sanity checking convertTimewiseMapToDatewiseMap()
	 * 
	 * @param dft
	 *            timezone to use
	 */
	public static void checkConvertTimewiseMapToDatewiseMap(TimeZone dft)
	{
		LinkedHashMap<String, TreeMap<Timestamp, Integer>> timewiseMap = new LinkedHashMap<>();
		LinkedHashMap<String, TreeMap<LocalDate, ArrayList<Integer>>> dateWiseMap = new LinkedHashMap<>();

		for (int i = 0; i <= 5; i++)
		{
			TreeMap<Timestamp, Integer> timeMap = new TreeMap<>();

			Instant instant = Instant.now();
			Timestamp ts = Timestamp.from(instant);

			for (int j = 0; j <= 6; j++)
			{
				timeMap.put(new Timestamp(ts.getTime() + (j * 60000000)), j);
			}
			timewiseMap.put("User" + i, timeMap);
		}

		for (Entry<String, TreeMap<Timestamp, Integer>> userW : timewiseMap.entrySet())
		{
			System.out.println("timewise:\nUser = " + userW.getKey());

			for (Entry<Timestamp, Integer> timeW : userW.getValue().entrySet())
			{
				System.out.println(" timew entry:" + timeW.getKey() + "-" + timeW.getValue());
			}
		}

		dateWiseMap = convertTimewiseMapToDatewiseMap(timewiseMap, dft);

		for (Entry<String, TreeMap<LocalDate, ArrayList<Integer>>> userW : dateWiseMap.entrySet())
		{
			System.out.println("Datewise:\nUser = " + userW.getKey());

			for (Entry<LocalDate, ArrayList<Integer>> timeW : userW.getValue().entrySet())
			{
				System.out.println("Date = " + timeW.getKey().toString());
				// System.out.println("Date = " + timeW.getKey().toGMTString() + " " + timeW.getKey().toLocaleString());
				System.out.println(" datew entry:" + timeW.getKey() + "-" + timeW.getValue().toString());
			}
		}

	}

	/**
	 * For sanity checking convertTimewiseMapToDatewiseMap()
	 * 
	 * @param dft
	 *            timezone to use
	 */
	public static void checkConvertTimewiseMapToDatewiseMap()
	{
		LinkedHashMap<String, TreeMap<Timestamp, Integer>> timewiseMap = new LinkedHashMap<>();
		LinkedHashMap<String, TreeMap<Date, ArrayList<Integer>>> dateWiseMap = new LinkedHashMap<>();

		for (int i = 0; i <= 5; i++)
		{
			TreeMap<Timestamp, Integer> timeMap = new TreeMap<>();

			Instant instant = Instant.now();
			Timestamp ts = Timestamp.from(instant);

			for (int j = 0; j <= 6; j++)
			{
				timeMap.put(new Timestamp(ts.getTime() + (j * 60000000)), j);
			}
			timewiseMap.put("User" + i, timeMap);
		}

		for (Entry<String, TreeMap<Timestamp, Integer>> userW : timewiseMap.entrySet())
		{
			System.out.println("timewise:\nUser = " + userW.getKey());

			for (Entry<Timestamp, Integer> timeW : userW.getValue().entrySet())
			{
				System.out.println(" timew entry:" + timeW.getKey() + "-" + timeW.getValue());
			}
		}

		dateWiseMap = convertTimewiseMapToDatewiseMap(timewiseMap);

		for (Entry<String, TreeMap<Date, ArrayList<Integer>>> userW : dateWiseMap.entrySet())
		{
			System.out.println("Datewise:\nUser = " + userW.getKey());

			for (Entry<Date, ArrayList<Integer>> timeW : userW.getValue().entrySet())
			{
				System.out.println("Date = " + timeW.getKey().toString());
				// System.out.println("Date = " + timeW.getKey().toGMTString() + " " + timeW.getKey().toLocaleString());
				System.out.println(" datew entry:" + timeW.getKey() + "-" + timeW.getValue().toString());
			}
		}

	}

	// public static String traverserMapofMaps(Map<T,Map<>>)
	/**
	 * 
	 * 
	 * convert a timestamp wise map tp date wise map
	 * <p>
	 * <font color = red>make sure that the timezone is set appropriately</font>
	 * </p>
	 * 
	 * @param <T>
	 * 
	 * @param allActivityEvents
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 */
	public static <T> LinkedHashMap<String, TreeMap<LocalDate, ArrayList<T>>> convertTimewiseMapToDatewiseMap(
			LinkedHashMap<String, TreeMap<Timestamp, T>> timewiseMap, TimeZone dft)
	{
		LinkedHashMap<String, TreeMap<LocalDate, ArrayList<T>>> daywiseMap = new LinkedHashMap<>();
		System.out.println("starting convertTimewiseMapToDatewiseMap");

		for (Map.Entry<String, TreeMap<Timestamp, T>> perUserCheckinEntry : timewiseMap.entrySet()) // Iterate over
																									// Users
		{
			String userID = perUserCheckinEntry.getKey();
			TreeMap<Timestamp, T> checkinEntriesForThisUser = perUserCheckinEntry.getValue();

			TreeMap<LocalDate, ArrayList<T>> daywiseForThisUser = new TreeMap<>();

			for (Map.Entry<Timestamp, T> checkin : checkinEntriesForThisUser.entrySet()) // iterare over activity events
																							// for this user
			{
				// Date date = DateTimeUtils.getDate(checkin.getKey());// (Date)
				// activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension", "Date"); // start
				// date
				LocalDate ldate = DateTimeUtils.getLocalDate(checkin.getKey(), dft);

				if ((daywiseForThisUser.containsKey(ldate)) == false)
				{
					daywiseForThisUser.put(ldate, new ArrayList<T>());
				}
				daywiseForThisUser.get(ldate).add(checkin.getValue());
			}

			daywiseMap.put(userID, daywiseForThisUser);
		}
		return daywiseMap;
	}

	/**
	 * 
	 * <font color = red>Error: hidden time component in java.sql.Date causing duplicate date keys</font>
	 * 
	 * convert a timestamp wise map to date wise map
	 * 
	 * @param <T>
	 * 
	 * @param allActivityEvents
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 */
	public static <T> LinkedHashMap<String, TreeMap<Date, ArrayList<T>>> convertTimewiseMapToDatewiseMap(
			LinkedHashMap<String, TreeMap<Timestamp, T>> timewiseMap)
	{
		LinkedHashMap<String, TreeMap<Date, ArrayList<T>>> daywiseMap = new LinkedHashMap<>();
		System.out.println("starting convertTimewiseMapToDatewiseMap");

		for (Map.Entry<String, TreeMap<Timestamp, T>> perUserCheckinEntry : timewiseMap.entrySet()) // Iterate over
																									// Users
		{
			String userID = perUserCheckinEntry.getKey();
			TreeMap<Timestamp, T> checkinEntriesForThisUser = perUserCheckinEntry.getValue();

			TreeMap<Date, ArrayList<T>> daywiseForThisUser = new TreeMap<>();

			for (Map.Entry<Timestamp, T> checkin : checkinEntriesForThisUser.entrySet()) // iterare over activity events
																							// for this user
			{
				Date date = DateTimeUtils.getDate(checkin.getKey());
				// (Date) activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension", "Date"); //
				// start date
				// Date ldate = DateTimeUtils.getLocalDate(checkin.getKey(), dft);

				if ((daywiseForThisUser.containsKey(date)) == false)
				{
					daywiseForThisUser.put(date, new ArrayList<T>());
				}
				daywiseForThisUser.get(date).add(checkin.getValue());
			}

			daywiseMap.put(userID, daywiseForThisUser);
		}
		return daywiseMap;
	}

	/**
	 * 
	 * <font color = green>Issue Solved: Prevented hidden time component in java.sql.Date from causing duplicate date
	 * keys by timestamp->LocalDate->sql.Date</font>
	 * <p>
	 * convert a timestamp wise map to date wise map
	 * </p>
	 * 
	 * @param <T>
	 * 
	 * @param allActivityEvents
	 * @param tz
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 */
	public static <T> LinkedHashMap<String, TreeMap<Date, ArrayList<T>>> convertTimewiseMapToDatewiseMap2(
			LinkedHashMap<String, TreeMap<Timestamp, T>> timewiseMap, TimeZone tz)
	{
		LinkedHashMap<String, TreeMap<Date, ArrayList<T>>> daywiseMap = new LinkedHashMap<>();
		System.out.println("starting convertTimewiseMapToDatewiseMap");

		for (Map.Entry<String, TreeMap<Timestamp, T>> perUserCheckinEntry : timewiseMap.entrySet())
		{ // Iterate over Users

			String userID = perUserCheckinEntry.getKey();
			TreeMap<Timestamp, T> checkinEntriesForThisUser = perUserCheckinEntry.getValue();

			TreeMap<Date, ArrayList<T>> daywiseForThisUser = new TreeMap<>();

			for (Map.Entry<Timestamp, T> checkin : checkinEntriesForThisUser.entrySet())
			{
				// iterare over activity events for this user
				LocalDate ldate = DateTimeUtils.getLocalDate(checkin.getKey(), tz);// converting to ldate to removing
																					// hidden time.
				Date date = Date.valueOf(ldate);// ldate.get//DateTimeUtils.getDate(checkin.getKey());// (Date)
												// activityEventsForThisUser.get(i).getDimensionAttributeValue("Date_Dimension",
												// "Date"); // start date
				// Date ldate = DateTimeUtils.getLocalDate(checkin.getKey(), dft);

				if ((daywiseForThisUser.containsKey(date)) == false)
				{
					daywiseForThisUser.put(date, new ArrayList<T>());
				}
				daywiseForThisUser.get(date).add(checkin.getValue());
			}
			daywiseMap.put(userID, daywiseForThisUser);
		}
		return daywiseMap;
	}

	/**
	 * convert checkinentries to activity objects
	 * 
	 * @param checkinEntriesDatewise
	 * @param locationObjects
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject2018>>> convertCheckinEntriesToActivityObjectsGowalla(
			LinkedHashMap<String, TreeMap<Date, ArrayList<CheckinEntry>>> checkinEntriesDatewise,
			/* LinkedHashMap<Integer, LocationGowalla> locationObjects) */
			Int2ObjectOpenHashMap<LocationGowalla> locationObjects)
	{
		System.out.println(
				"Inside convertCheckinEntriesToActivityObjectsGowalla(): Alert: Constant.assignFallbackZoneIdWhenConvertCinsToAO="
						+ Constant.assignFallbackZoneIdWhenConvertCinsToAO);
		LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject2018>>> activityObjectsDatewise = new LinkedHashMap<>(
				(int) (Math.ceil(checkinEntriesDatewise.size() / 0.75)));

		Set<Integer> uniqueActIDs = new LinkedHashSet<>();// added on April 6 2018
		Set<String> uniqueWorkingLevelActIDs = new LinkedHashSet<>();// added on April 6 2018
		List<Integer> actIDs = new ArrayList<>();// added on April 6 2018
		List<String> workingLevelActIDs = new ArrayList<>();// added on April 6 2018

		System.out.println("starting convertCheckinEntriesToActivityObjectsGowalla");
		System.out.println("Num of locationObjects received = " + locationObjects.size() + " with keys as follows");

		if (VerbosityConstants.WriteLocationMap)
		{// locationObjects.keySet().stream().forEach(e -> System.out.print(String.valueOf(e) + "||"));
			StringBuilder locInfo = new StringBuilder();

			// locationObjects.entrySet().forEach(e -> locInfo.append(e.getValue().toString() + "\n"));
			locationObjects.values().forEach(e -> locInfo.append(e.toString() + "\n"));

			WToFile.writeToNewFile(locInfo.toString(), Constant.getOutputCoreResultsPath() + "LocationMap.csv");
		} // System.out.println("Num of locationObjects received = " + locationObjects.size());

		int numOfCInsWithMultipleLocIDs = 0, numOfCInsWithMultipleDistinctLocIDs = 0,
				numOfCInsWithMultipleWorkingLevelCatIDs = 0, numOfCIns = 0;
		HashMap<String, String> actsOfCinsWithMultipleWorkLevelCatIDs = new HashMap<>();
		IntSortedSet locIDsWithNoTZ = new IntRBTreeSet();
		IntSortedSet locIDsWithNoFallbackTZ = new IntRBTreeSet();
		int numOfCinsWithNullZoneIDs = 0, numOfCinsWithEvenFallbackNullZoneIds = 0;

		// {userid, locids, num of null zone ids for this user id locid}
		Map<String, Map<List<Integer>, Long>> userIdLocIdNumOfNullZoneCInsMap = new LinkedHashMap<>();
		// Set<String> setOfCatIDsofAOs = new TreeSet<String>();

		// convert checkinentries to activity objects
		for (Entry<String, TreeMap<Date, ArrayList<CheckinEntry>>> userEntry : checkinEntriesDatewise.entrySet())
		{// over users
			ZoneId fallbackZoneId = null;
			int numOfCinsForThisUser = 0;

			String userID = userEntry.getKey();
			TreeMap<Date, ArrayList<ActivityObject2018>> dayWiseForThisUser = new TreeMap<>();
			// System.out.print("\nuser: " + userID);
			for (Entry<Date, ArrayList<CheckinEntry>> dateEntry : userEntry.getValue().entrySet()) // over dates
			{
				// Date date = dateEntry.getKey();
				ArrayList<ActivityObject2018> activityObjectsForThisUserThisDate = new ArrayList<>(
						dateEntry.getValue().size());

				// System.out.println(
				// "--* user:" + userID + " date:" + date.toString() + " has " + dateEntry.getValue().size() + "
				// checkinentries");

				for (CheckinEntry cin : dateEntry.getValue())// over checkins
				{
					numOfCIns += 1;
					numOfCinsForThisUser += 1;

					int activityID = Integer.valueOf(cin.getActivityID());
					String activityName = String.valueOf(cin.getActivityID());// "";

					Timestamp startTimestamp = cin.getTimestamp(); // timestamp of first cin with its a merged one
					String startLatitude = cin.getStartLatitude(); // of first cin if its a merged one
					String startLongitude = cin.getStartLongitude();// of first cin if its a merged one
					String startAltitude = "";//
					String userIDInside = cin.getUserID();
					double distaneInMFromPrev = cin.getDistanceInMetersFromPrev();// of first cin if its a merged one
					long durationInSecFromPrev = cin.getDurationInSecsFromPrev();// getDurInSecsFromNext();// of first
																					// cin if its a merged one

					String[] levelWiseCatIDs = cin.getLevelWiseCatIDs();
					// int locationID = Integer.valueOf(e.getLocationID());
					ArrayList<Integer> locIDs = cin.getLocationIDs();// e.getLocationIDs());
					String locationName = "";//
					if (RegexUtils.patternDoubleUnderScore.split(cin.getWorkingLevelCatIDs()).length > 1)
					{
						numOfCInsWithMultipleWorkingLevelCatIDs += 1;
						actsOfCinsWithMultipleWorkLevelCatIDs.put(activityName, cin.getWorkingLevelCatIDs());
					}
					// sanity check start
					if (userIDInside.equals(userID) == false)
					{
						System.err.println(
								"Error: sanity check failed in createUserTimelinesFromCheckinEntriesGowalla()");
					}
					// sanity check end

					int numOfLocIDs = locIDs.size();
					int photos_count = 0, checkins_count = 0, users_count = 0, radius_meters = 0, highlights_count = 0,
							items_count = 0, max_items_count = 0;
					// we need to compute the average of these atributes in case there are more than one place id for a
					// (merged) checkin entry
					for (Integer locationID : locIDs)
					{
						// LocationGowalla loc = locationObjects.get((locationID));
						LocationGowalla loc = locationObjects.get((int) locationID);
						if (loc == null)
						{
							System.err.println(
									"Error in convertCheckinEntriesToActivityObjectsGowalla: No LocationGowalla object found for locationID="
											+ String.valueOf(locationID));
						}

						photos_count += loc.getPhotos_count();
						checkins_count += loc.getCheckins_count();
						users_count += loc.getUsers_count();
						radius_meters += loc.getRadius_meters();
						highlights_count = loc.getHighlights_count();
						items_count += loc.getItems_count();
						max_items_count += loc.getMax_items_count();
					}

					if (numOfLocIDs > 1)
					{
						numOfCInsWithMultipleLocIDs += 1;
						photos_count = photos_count / numOfLocIDs;
						checkins_count = checkins_count / numOfLocIDs;
						users_count = users_count / numOfLocIDs;
						radius_meters = radius_meters / numOfLocIDs;
						highlights_count = highlights_count / numOfLocIDs;
						items_count = items_count / numOfLocIDs;
						max_items_count = max_items_count / numOfLocIDs;
					}

					if (new HashSet<Integer>(locIDs).size() > 1) // if more than 1 distinct locations
					{
						numOfCInsWithMultipleDistinctLocIDs += 1;
					}
					// LocationGowalla loc = locationObjects.get(String.valueOf(locationID));
					// int photos_count = loc.getPhotos_count();
					// int checkins_count = loc.getCheckins_count();
					// int users_count = loc.getUsers_count();
					// int radius_meters = loc.getRadius_meters();
					// int highlights_count = loc.getHighlights_count();
					// int items_count = loc.getItems_count();
					// int max_items_count = loc.getMax_items_count();
					ZoneId currentZoneId = DomainConstants.getGowallaLocZoneId(locIDs);
					if (currentZoneId == null)
					{ /////
						Map<List<Integer>, Long> mapForUserNTZ = userIdLocIdNumOfNullZoneCInsMap.get(userID);
						if (mapForUserNTZ != null)
						{
							Long numOfNZCinsForThisUserLocID = mapForUserNTZ.get(locIDs);
							if (numOfNZCinsForThisUserLocID != null)
							{
								mapForUserNTZ.put(locIDs, numOfNZCinsForThisUserLocID + 1);
							}
							else
							{
								mapForUserNTZ.put(locIDs, (long) 1);
							}
						}
						else
						{
							mapForUserNTZ = new LinkedHashMap<List<Integer>, Long>();
							mapForUserNTZ.put(locIDs, (long) 1);
							userIdLocIdNumOfNullZoneCInsMap.put(userID, mapForUserNTZ);
						}
						/////
						numOfCinsWithNullZoneIDs += 1;
						locIDsWithNoTZ.addAll(locIDs);

						if (Constant.assignFallbackZoneIdWhenConvertCinsToAO)
						{
							currentZoneId = fallbackZoneId;
							// WritingToFile.appendLineToFileAbsolute(userID + "," + locIDs + "," + startTimestamp +
							// "\n",
							// Constant.getOutputCoreResultsPath() + "NullZoneIDs.csv");
						}
						if (fallbackZoneId == null)
						{
							locIDsWithNoFallbackTZ.addAll(locIDs);
							numOfCinsWithEvenFallbackNullZoneIds += 1;
							// WritingToFile.appendLineToFileAbsolute(userID + "," + locIDs + "," + startTimestamp + ","
							// + numOfCinsForThisUser + "\n",Constant.getOutputCoreResultsPath() +
							// "FallbackNullZoneIDs.csv");
						}
					}

					ActivityObject2018 ao = new ActivityObject2018(activityID, locIDs, activityName, locationName,
							startTimestamp, startLatitude, startLongitude, startAltitude, userID, photos_count,
							checkins_count, users_count, radius_meters, highlights_count, items_count, max_items_count,
							cin.getWorkingLevelCatIDs(), distaneInMFromPrev, durationInSecFromPrev, levelWiseCatIDs,
							currentZoneId);

					// Start of April 6
					uniqueActIDs.add(ao.getActivityID());
					actIDs.add(ao.getActivityID());
					uniqueWorkingLevelActIDs.addAll(
							Arrays.asList(RegexUtils.patternDoubleUnderScore.split(ao.getWorkingLevelCatIDs())));
					workingLevelActIDs.add(ao.getWorkingLevelCatIDs());
					// End of April 6

					// setOfCatIDsofAOs.add(ao.getActivityID());
					activityObjectsForThisUserThisDate.add(ao);

					fallbackZoneId = currentZoneId;
				}
				dayWiseForThisUser.put(dateEntry.getKey(), activityObjectsForThisUserThisDate);
			}
			activityObjectsDatewise.put(userID, dayWiseForThisUser);
		}

		System.out.println("locIDsWithNoTZ.size()= " + locIDsWithNoTZ.size());
		System.out.println("locIDsWithNoFallbackTZ.size()= " + locIDsWithNoFallbackTZ.size());

		WToFile.writeToNewFile(locIDsWithNoTZ.stream().map(String::valueOf).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "UniqueLocIDsWithNoTZ.csv");

		WToFile.writeToNewFile(locIDsWithNoFallbackTZ.stream().map(String::valueOf).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "UniqueLocIDsWithNoFallbackTZ.csv");

		// find loc ids in gowalla 5 days train test data which is known to have no TZ is found to have no fallback tz
		// as well.
		locIDsWithNoFallbackTZ.retainAll(DomainConstants.locIDsIn5DaysTrainTestDataWithNullTZ);
		WToFile.writeToNewFile(locIDsWithNoFallbackTZ.stream().map(String::valueOf).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "UniqueLocIDsWithNoFallbackTZIn5DaysTrainTestNoTZ.csv");

		System.out.println(
				"\n loc ids in Gowalla5daysTrainTest data which is known to have no TZ is found to have no fallback tz as well="
						+ locIDsWithNoFallbackTZ.toString());

		System.out.println(" numOfCinsWithNullZoneIDs= " + numOfCinsWithNullZoneIDs + "  "
				+ ((numOfCinsWithNullZoneIDs * 1.0) / numOfCIns) + "% of total cins");
		System.out.println(" numOfCinsWithEvenFallbackNullZoneIds= " + numOfCinsWithEvenFallbackNullZoneIds + "  "
				+ ((numOfCinsWithEvenFallbackNullZoneIds * 1.0) / numOfCIns) + "% of total cins");
		System.out.println(" numOfCIns = " + numOfCIns);
		System.out.println(" numOfCInsWithMultipleLocIDs = " + numOfCInsWithMultipleLocIDs);
		System.out.println(" numOfCInsWithMultipleDistinctLocIDs = " + numOfCInsWithMultipleDistinctLocIDs);
		System.out.println(" numOfCInsWithMultipleWorkingLevelCatIDs = " + numOfCInsWithMultipleWorkingLevelCatIDs
				+ " for working level = " + DomainConstants.gowallaWorkingCatLevel);

		// Start of added on April 6 2018
		System.out.println(" num of unique actIDs = " + uniqueActIDs.size());
		System.out.println(" num of unique uniqueWorkingLevelActIDs = " + uniqueWorkingLevelActIDs.size());

		WToFile.writeToNewFile(uniqueActIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "ZZuniqueActIDs.csv");
		WToFile.writeToNewFile(
				uniqueWorkingLevelActIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "ZZuniqueWorkingLevelActIDs.csv");
		WToFile.writeToNewFile(actIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "ZZactIDs.csv");
		WToFile.writeToNewFile(workingLevelActIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "ZZworkingLevelActIDs.csv");
		// End of added on April 6 2018

		WToFile.writeToNewFile(
				actsOfCinsWithMultipleWorkLevelCatIDs.entrySet().stream().map(e -> e.getKey() + "-" + e.getValue())
						.collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "MapActsOfCinsWithMultipleWorkLevelCatIDs.csv");

		WToFile.writeMapOfMap(userIdLocIdNumOfNullZoneCInsMap, "UserID;LocID;NumOfNullTZCins\n", ";",
				Constant.getOutputCoreResultsPath() + "userIdLocIdNumOfNullZoneCInsMap.csv");

		System.out.println("exiting convertCheckinEntriesToActivityObjectsGowalla");
		return activityObjectsDatewise;

	}

	/**
	 * Fork of convertCheckinEntriesToActivityObjectsGowalla() primarily:
	 * <ul>
	 * <li>to allow for CheckinEntryV2 instead of CheckinEntry</li>
	 * <li>no need to timezone as timestamps have been normalised to local time in their respective locations</li>
	 * </ul>
	 * <p>
	 * convert checkinentries to activity objects
	 * 
	 * @param checkinEntriesDatewise
	 * @param locationObjects
	 * @return
	 * @since April 9 2018
	 */
	public static LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject2018>>> convertCheckinEntriesToActivityObjectsGowallaV2(
			LinkedHashMap<String, TreeMap<Date, ArrayList<CheckinEntryV2>>> checkinEntriesDatewise,
			/* LinkedHashMap<Integer, LocationGowalla> locationObjects) */
			Int2ObjectOpenHashMap<LocationGowalla> locationObjects)
	{
		System.out.println("Inside convertCheckinEntriesToActivityObjectsGowallaV2():");
		// DomainConstants.setGridIDLocIDGowallaMaps();
		Map<Long, Integer> locIDGridIndexMap = DomainConstants.getLocIDGridIndexGowallaMap();

		LinkedHashMap<String, TreeMap<Date, ArrayList<ActivityObject2018>>> activityObjectsDatewise = new LinkedHashMap<>(
				(int) (Math.ceil(checkinEntriesDatewise.size() / 0.75)));

		Set<Integer> uniqueActIDs = new LinkedHashSet<>();// added on April 6 2018
		Set<String> uniqueWorkingLevelActIDs = new LinkedHashSet<>();// added on April 6 2018
		List<Integer> actIDs = new ArrayList<>();// added on April 6 2018
		List<String> workingLevelActIDs = new ArrayList<>();// added on April 6 2018
		Set<ZoneId> uniqueZoneIDs = new HashSet<>();

		System.out.println("starting convertCheckinEntriesToActivityObjectsGowalla");
		System.out.println("Num of locationObjects received = " + locationObjects.size() + " with keys as follows");

		if (VerbosityConstants.WriteLocationMap)
		{// locationObjects.keySet().stream().forEach(e -> System.out.print(String.valueOf(e) + "||"));
			StringBuilder locInfo = new StringBuilder();

			// locationObjects.entrySet().forEach(e -> locInfo.append(e.getValue().toString() + "\n"));
			locationObjects.values().forEach(e -> locInfo.append(e.toString() + "\n"));

			WToFile.writeToNewFile(locInfo.toString(), Constant.getOutputCoreResultsPath() + "LocationMap.csv");
		} // System.out.println("Num of locationObjects received = " + locationObjects.size());

		int numOfCInsWithMultipleLocIDs = 0, numOfCInsWithMultipleDistinctLocIDs = 0,
				numOfCInsWithMultipleWorkingLevelCatIDs = 0, numOfCIns = 0;
		HashMap<String, String> actsOfCinsWithMultipleWorkLevelCatIDs = new HashMap<>();
		// IntSortedSet locIDsWithNoTZ = new IntRBTreeSet();
		// IntSortedSet locIDsWithNoFallbackTZ = new IntRBTreeSet();
		// int numOfCinsWithNullZoneIDs = 0, numOfCinsWithEvenFallbackNullZoneIds = 0;
		// {userid, locids, num of null zone ids for this user id locid}
		// Map<String, Map<List<Integer>, Long>> userIdLocIdNumOfNullZoneCInsMap = new LinkedHashMap<>();
		// Set<String> setOfCatIDsofAOs = new TreeSet<String>();

		// convert checkinentries to activity objects
		for (Entry<String, TreeMap<Date, ArrayList<CheckinEntryV2>>> userEntry : checkinEntriesDatewise.entrySet())
		{// over users
			// ZoneId fallbackZoneId = null;
			int numOfCinsForThisUser = 0;

			String userID = userEntry.getKey();
			TreeMap<Date, ArrayList<ActivityObject2018>> dayWiseForThisUser = new TreeMap<>();
			// System.out.print("\nuser: " + userID);
			for (Entry<Date, ArrayList<CheckinEntryV2>> dateEntry : userEntry.getValue().entrySet()) // over dates
			{
				// Date date = dateEntry.getKey();
				ArrayList<ActivityObject2018> activityObjectsForThisUserThisDate = new ArrayList<>(
						dateEntry.getValue().size());

				// System.out.println( "--* user:" + userID + " date:" + date.toString() + " has " +
				// dateEntry.getValue().size() + "checkinentries");

				for (CheckinEntryV2 cin : dateEntry.getValue())// over checkins
				{
					numOfCIns += 1;
					numOfCinsForThisUser += 1;

					int activityID = Integer.valueOf(cin.getActivityID());
					String activityName = String.valueOf(cin.getActivityID());// "";

					Timestamp startTimestamp = cin.getTimestamp(); // timestamp of first cin with its a merged one
					String startLatitude = cin.getStartLatitude(); // of first cin if its a merged one
					String startLongitude = cin.getStartLongitude();// of first cin if its a merged one
					String startAltitude = "";//
					String userIDInside = cin.getUserID();
					double distaneInMFromPrev = cin.getDistanceInMetersFromPrev();// of first cin if its a merged one
					long durationInSecFromPrev = cin.getDurationInSecsFromPrev();// getDurInSecsFromNext();// of first
																					// cin if its a merged one

					double distanceInMFromNext = cin.getDistanceInMeterFromNextCheckin();// added on April 8 2018
					long durationInSecFromNext = cin.getDurationInSecsFromNextCheckin();// added on April 8 2018
					// ZoneId currentZoneId = ZoneId.of(cin.getTz());// added on April 8 2018

					// All actObject assigned a dummy UTC timezone, as timestamps have been normalised to local
					// timstamps in respective timezones,
					ZoneId currentZoneId = ZoneId.of("UTC");// added on April 8 2018

					String[] levelWiseCatIDs = cin.getLevelWiseCatIDs();// NOT USED in ActivityObject
					// int locationID = Integer.valueOf(e.getLocationID());
					ArrayList<Integer> locIDs = cin.getLocationIDs();// e.getLocationIDs());
					String locationName = "";//
					if (RegexUtils.patternDoubleUnderScore.split(cin.getWorkingLevelCatIDs()).length > 1)
					{
						numOfCInsWithMultipleWorkingLevelCatIDs += 1;
						actsOfCinsWithMultipleWorkLevelCatIDs.put(activityName, cin.getWorkingLevelCatIDs());
					}
					// sanity check start
					if (userIDInside.equals(userID) == false)
					{
						System.err.println(
								"Error: sanity check failed in createUserTimelinesFromCheckinEntriesGowalla()");
					}
					// sanity check end

					int numOfLocIDs = locIDs.size();
					int photos_count = 0, checkins_count = 0, users_count = 0, radius_meters = 0, highlights_count = 0,
							items_count = 0, max_items_count = 0;
					// we need to compute the average of these atributes in case there are more than one place id for a
					// (merged) checkin entry
					for (Integer locationID : locIDs)
					{
						// LocationGowalla loc = locationObjects.get((locationID));
						LocationGowalla loc = locationObjects.get((int) locationID);
						if (loc == null)
						{
							System.err.println(
									"Error in convertCheckinEntriesToActivityObjectsGowalla: No LocationGowalla object found for locationID="
											+ String.valueOf(locationID));
						}

						photos_count += loc.getPhotos_count();
						checkins_count += loc.getCheckins_count();
						users_count += loc.getUsers_count();
						radius_meters += loc.getRadius_meters();
						highlights_count = loc.getHighlights_count();
						items_count += loc.getItems_count();
						max_items_count += loc.getMax_items_count();
					}

					if (numOfLocIDs > 1)
					{
						numOfCInsWithMultipleLocIDs += 1;
						photos_count = photos_count / numOfLocIDs;
						checkins_count = checkins_count / numOfLocIDs;
						users_count = users_count / numOfLocIDs;
						radius_meters = radius_meters / numOfLocIDs;
						highlights_count = highlights_count / numOfLocIDs;
						items_count = items_count / numOfLocIDs;
						max_items_count = max_items_count / numOfLocIDs;
					}

					if (new HashSet<Integer>(locIDs).size() > 1) // if more than 1 distinct locations
					{
						numOfCInsWithMultipleDistinctLocIDs += 1;
					}

					int gridIndex = TimelineUtils.getGridIndex(locIDs, locIDGridIndexMap, userID, startTimestamp);
					// ZoneId currentZoneId = DomainConstants.getGowallaLocZoneId(locIDs);
					ActivityObject2018 ao = new ActivityObject2018(
							activityID, locIDs, activityName, locationName, startTimestamp, startLatitude,
							startLongitude, startAltitude, userID, photos_count, checkins_count, users_count,
							radius_meters, highlights_count, items_count, max_items_count, cin.getWorkingLevelCatIDs(),
							distaneInMFromPrev, durationInSecFromPrev, /* levelWiseCatIDs, */
							currentZoneId, distanceInMFromNext, durationInSecFromNext, gridIndex);

					// Start of sanity check of April 6
					uniqueActIDs.add(ao.getActivityID());
					actIDs.add(ao.getActivityID());
					uniqueWorkingLevelActIDs.addAll(
							Arrays.asList(RegexUtils.patternDoubleUnderScore.split(ao.getWorkingLevelCatIDs())));
					workingLevelActIDs.add(ao.getWorkingLevelCatIDs());
					uniqueZoneIDs.add(ao.getTimeZoneId());
					// End of sanity check of April 6

					// setOfCatIDsofAOs.add(ao.getActivityID());
					activityObjectsForThisUserThisDate.add(ao);
					// fallbackZoneId = currentZoneId;
				}
				dayWiseForThisUser.put(dateEntry.getKey(), activityObjectsForThisUserThisDate);
			}
			activityObjectsDatewise.put(userID, dayWiseForThisUser);
		}

		System.out.println(" numOfCIns = " + numOfCIns);
		System.out.println(" numOfCInsWithMultipleLocIDs = " + numOfCInsWithMultipleLocIDs);
		System.out.println(" numOfCInsWithMultipleDistinctLocIDs = " + numOfCInsWithMultipleDistinctLocIDs);
		System.out.println(" numOfCInsWithMultipleWorkingLevelCatIDs = " + numOfCInsWithMultipleWorkingLevelCatIDs
				+ " for working level = " + DomainConstants.gowallaWorkingCatLevel);

		// Start of added on April 6 2018
		String phrase = "CinToAOConv_";
		System.out.println(" num of unique zoneIDs = " + uniqueZoneIDs.size());
		System.out.println(" num of unique actIDs = " + uniqueActIDs.size());
		System.out.println(" num of unique uniqueWorkingLevelActIDs = " + uniqueWorkingLevelActIDs.size());
		WToFile.writeToNewFile(uniqueActIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + phrase + "UniqueActIDs.csv");
		WToFile.writeToNewFile(
				uniqueWorkingLevelActIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + phrase + "UniqueWorkingLevelActIDs.csv");
		WToFile.writeToNewFile(actIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + phrase + "ActIDs.csv");
		WToFile.writeToNewFile(workingLevelActIDs.stream().map(e -> e.toString()).collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + phrase + "WorkingLevelActIDs.csv");
		// End of added on April 6 2018

		WToFile.writeToNewFile(
				actsOfCinsWithMultipleWorkLevelCatIDs.entrySet().stream().map(e -> e.getKey() + "-" + e.getValue())
						.collect(Collectors.joining("\n")),
				Constant.getOutputCoreResultsPath() + "MapActsOfCinsWithMultipleWorkLevelCatIDs.csv");

		System.out.println("exiting convertCheckinEntriesToActivityObjectsGowalla");
		return activityObjectsDatewise;

	}

	public static LinkedHashMap<String, Timeline> rearrangeTimelinesByGivenOrder(LinkedHashMap<String, Timeline> map,
			int[] orderKeys)
	{
		LinkedHashMap<String, Timeline> rearranged = new LinkedHashMap<String, Timeline>();

		for (int key : orderKeys)
		{
			String keyString = Integer.toString(key);
			rearranged.put(keyString, map.get(keyString));
		}

		return rearranged;
	}

	/**
	 * 
	 * @param map
	 * @param orderKeys
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> rearrangeDayTimelinesByGivenOrder(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> map, int[] orderKeys)
	{
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> rearranged = new LinkedHashMap<>();

		for (int key : orderKeys)
		{
			String keyString = Integer.toString(key);
			if (map.containsKey(keyString) == false)
			{
				continue;
			}
			else
			{
				rearranged.put(keyString, map.get(keyString));
			}
		}
		return rearranged;
	}

	/**
	 * Rearranges the map where key is user id , according to the user id order prescribed in Constant.userIDs which is
	 * determined by the dataset used
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, Integer> rearrangeOrderForDataset(LinkedHashMap<String, Integer> userMaps)
	{
		LinkedHashMap<String, Integer> rearranged = new LinkedHashMap<String, Integer>();
		rearranged = UtilityBelt.rearrangeByGivenOrder(userMaps, Constant.getUserIDs());
		return rearranged;
	}

	/**
	 * Rearranges the map where key is user id , according to the user id order prescribed in Constant.userIDs which is
	 * determined by the dataset used
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, Timeline> rearrangeTimelinesOrderForDataset(
			LinkedHashMap<String, Timeline> userMaps)
	{
		LinkedHashMap<String, Timeline> rearranged = new LinkedHashMap<String, Timeline>();
		rearranged = rearrangeTimelinesByGivenOrder(userMaps, Constant.getUserIDs());
		return rearranged;
	}

	/**
	 * Rearranges the map where key is user id , according to the user id order prescribed in Constant.userIDs which is
	 * determined by the dataset used
	 * 
	 * @param map
	 * @return
	 */
	// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines = new LinkedHashMap<String,
	// LinkedHashMap<Date, UserDayTimeline>>();
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> rearrangeDayTimelinesOrderForDataset(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userMaps)
	{
		System.out.println("rearrangeDayTimelinesOrderForDataset received: " + userMaps.size() + " users");
		long ct1 = System.currentTimeMillis();
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> rearranged = new LinkedHashMap<>();
		rearranged = rearrangeDayTimelinesByGivenOrder(userMaps, Constant.getUserIDs());
		long ct2 = System.currentTimeMillis();
		System.out.println("rearrangeDayTimelinesOrderForDataset returned: " + rearranged.size() + " users"
				+ ". time taken: " + ((ct2 - ct1) / 1000) + " secs");
		return rearranged;
	}

	/**
	 * Convert list of activity objects to a list of activity IDs.
	 * <p>
	 * Created to use for each loop instead of streams as stream was running out memory.
	 * 
	 * @param givenAOs
	 * @return seq of activity names (actual category names extracted from the catid name dictionary) delimited by the
	 *         given delimiter
	 * @since 15 Dec 2017
	 */
	// public static char[] listOfActObjsToArrayOfCodeChars(ArrayList<ActivityObject> givenAOs, boolean verbose)
	// {
	// char[] res = new char[givenAOs.size()];
	//
	// int i = 0;
	// for (ActivityObject ao : givenAOs)
	// {
	// res[i++] = ao.getCharCode();
	// }
	//
	// // start of sanity check Passed
	// // if (true){if (seqOfActIDs.equals(seqOfActIDs2)){
	// // System.out.println("Sanity check Dec 15_2 passed");
	// // }else{System.out.println("Sanity check Dec 15_2 failed");} }
	// // end of sanity check
	//
	// if (verbose)
	// {
	// StringBuilder sb = new StringBuilder();
	// givenAOs.stream().forEachOrdered(ao -> sb.append(ao.getActivityID() + ">>"));
	// sb.append("\n");
	// new String(res).chars().mapToObj(i -> (char) i).forEachOrdered(i -> sb.append(i + ">>"));
	// System.out.println("---listOfActObjsToArrayOfCodeChars verbose-\n" + sb.toString() + "\n-----\n");
	// }
	//
	// return seqOfActIDs2;
	// }
}
