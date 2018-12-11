package org.activity.sanityChecks;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import org.activity.constants.Constant;
import org.activity.io.Serializer;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.util.TimelineCreators;
import org.activity.util.TimelineUtils;
import org.activity.util.UserDayTimeline;

public class TimelineUtilsChecks
{
	static final String actNames[] = { "a", "b", "c" };

	public static void setup()
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 21, 2016
		Constant.setDefaultTimeZone("UTC");
	}

	/**
	 * disabled on 9 Dec 2018
	 * 
	 * @param args
	 */
	public static void main0(String args[])
	{
		setup();
		check12June(); // All Okay, is following spill over days.
	}

	public static void main(String args[])
	{
		System.out.println("Here in Oct 2014");
		String pathToData = "./dataToRead/DCULLFromBackup/AllUserTimelines.obj";
		try
		{
			LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersDayTimelinesOct2014 = (LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>) Serializer
					.deSerializeThis(pathToData);
			System.out.println("usersDayTimelinesOct2014.size()=" + usersDayTimelinesOct2014);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	//

	/**
	 * Check the following methods:
	 * <p>
	 * TimelineUtils.getUniqueDates()
	 * <p>
	 * TimelineUtils.createTimestampsToLookAt()
	 * <p>
	 * org.activity.objects.Timeline.getTimeDiffValidAOWithStartTimeNearestTo()
	 * <p>
	 */
	public static void check12June()
	{
		Timeline timeline1 = createDummyTimeline(20);
		timeline1.printActivityObjectNamesWithTimestampsInSequence("\n");

		LinkedHashSet<LocalDate> uniqueDatesInCands = TimelineUtils.getUniqueDates(timeline1, true);

		Timestamp startTimestampOfActObjAtRecommPoint = new Timestamp(1900, 1 - 1, 0, 23, 55, 0, 0);

		ArrayList<Timestamp> timestampsToLookInto = TimelineCreators.createTimestampsToLookAt(uniqueDatesInCands,
				startTimestampOfActObjAtRecommPoint, true);

		for (Timestamp tsToLookInto : timestampsToLookInto)
		{

			Date d = new Date(tsToLookInto.getYear() - 1900, tsToLookInto.getMonth() - 1, tsToLookInto.getDate());
			/*
			 * For this cand timeline, find the Activity Object with start timestamp nearest to the start timestamp of
			 * current Activity Object and the distance is diff of their start times
			 */
			Triple<Integer, ActivityObject2018, Double> score = timeline1
					.getTimeDiffValidAOWithStartTimeNearestTo(tsToLookInto, true);

		}
	}

	public static Timeline createDummyTimeline(int numOfAOs)
	{
		Timestamp ts0 = new Timestamp(2010 - 1900, 1 - 1, 1, 0, 0, 0, 0);
		ArrayList<ActivityObject2018> aos = new ArrayList<>();

		int randomActIndex, randomNextSecs;
		Timestamp currentTS = ts0;
		for (int i = 0; i < numOfAOs; i++)
		{
			randomActIndex = ThreadLocalRandom.current().nextInt(0, actNames.length);
			randomNextSecs = ThreadLocalRandom.current().nextInt(1000, 20000);

			Timestamp ts = new Timestamp(currentTS.getTime() + randomNextSecs * 1000);
			aos.add(new ActivityObject2018(actNames[randomActIndex], ts, ts));
			currentTS = ts;
		}

		return new Timeline(aos, false, true);
	}
}
