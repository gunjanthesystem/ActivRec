package org.activity.sanityChecks;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.OptionalLong;

import org.activity.objects.ActivityObject2018;
import org.activity.objects.Timeline;
import org.activity.ui.PopUps;
import org.activity.util.TimelineTransformers;
import org.activity.util.TimelineUtils;

public class TimelineSanityChecks
{

	/**
	 * Check if listOfActObjs1 < listOfActObjs2 with respect to time.
	 * 
	 * @param listOfActObjs1
	 * @param listOfActObjs2
	 */
	public static boolean checkIfChronoLogicalOrder(ArrayList<ActivityObject2018> listOfActObjs1,
			ArrayList<ActivityObject2018> listOfActObjs2)
	{
		boolean inOrder = true;

		if (TimelineUtils.isChronological(listOfActObjs1) == false)
		{
			System.err.println(PopUps
					.getTracedErrorMsg("Error in checkIfChronoLogicalOrder: listOfActObjs1 isChronological false"));
			listOfActObjs1.stream().forEachOrdered(ao -> System.err.println(">>" + ao.toStringAllGowallaTS()));
			inOrder = false;
		}

		// Need not be chronological
		// if (TimelineUtils.isChronological(listOfActObjs2) == false)
		// {
		// System.err.println(PopUps.getCurrentStackTracedErrorMsg(
		// "Error in checkIfChronoLogicalOrder: listOfActObjs2 isChronological false"));
		// listOfActObjs2.stream().forEachOrdered(ao -> System.err.println(">>" + ao.toStringAllGowallaTS()));
		// inOrder = false;
		// }

		OptionalLong maxTimeInList1 = listOfActObjs1.stream().mapToLong(ao -> ao.getEndTimestampInms()).max();
		OptionalLong minTimeInList2 = listOfActObjs2.stream().mapToLong(ao -> ao.getEndTimestampInms()).min();

		if (maxTimeInList1.isPresent() && minTimeInList2.isPresent())
		{
			if (maxTimeInList1.getAsLong() > minTimeInList2.getAsLong())
			{
				System.err.println(PopUps.getTracedErrorMsg(
						"Error in checkIfChronoLogicalOrder: maxTimeInList1.getAsLong()=" + maxTimeInList1.getAsLong()
								+ " > minTimeInList2.getAsLong() = " + minTimeInList2.getAsLong()));
				inOrder = false;
				return false;
			}
		}

		else
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in checkIfChronoLogicalOrder: maxTimeInList1.isPresent() = " + maxTimeInList1.isPresent()
							+ "&& minTimeInList2.isPresent() = " + minTimeInList2.isPresent()));
		}
		return inOrder;

	}

	/**
	 * To check if daywise and continuous timelines are same wrt to sequence of act names with their timestamps
	 * 
	 * @param dayTimelines
	 * @param timeline
	 * @return
	 */
	public static boolean isDaywiseAndContinousTimelinesSameWRTAoNameTS(LinkedHashMap<Date, Timeline> dayTimelines,
			Timeline timeline, boolean verbose)
	{
		Timeline dayTimelineAsContinuous = TimelineTransformers.dayTimelinesToATimeline(dayTimelines, false, true);
		String dayTimelineAsContinuousStringOfAONamesTS = dayTimelineAsContinuous
				.getActivityObjectNamesInSequenceWithFeatures();
		String timelineAsStringOfAONamesTS = timeline.getActivityObjectNamesInSequenceWithFeatures();

		if (verbose)
		{
			System.out
					.println("dayTimelineAsContinuousStringOfAONamesTS=\n" + dayTimelineAsContinuousStringOfAONamesTS);
			System.out.println("timelineAsStringOfAONamesTS=\n" + timelineAsStringOfAONamesTS);
		}
		return dayTimelineAsContinuousStringOfAONamesTS.equals(timelineAsStringOfAONamesTS);
	}

}
