package org.activity.objects;

//import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
//import java.rmi.server.UID;
import java.util.ArrayList;

import org.activity.util.Constant;

/**
 * Timeline is a chronological sequence of Activity Objects with the next Activity Object to be considered after this
 * sequence of Activity Objects
 * 
 * @author gunjan
 *
 */
public class TimelineWithNext extends Timeline
{
	ActivityObject nextActivityObject; // currently the nextActivityObject is set to be always a valid ActivityObject
	int immediateNextActivityIsInvalid = -1; // -1 means 'not determined yet', '0' means 'was not invalid', '1' means
												// 'was invalid'

	/**
	 * Create Timeline from given Activity Objects and the given next Activity Object to be considered
	 * 
	 * @param activityObjects
	 */
	public TimelineWithNext(ArrayList<ActivityObject> activityObjects, ActivityObject nextActivityObject)
	{
		super(activityObjects);
		this.nextActivityObject = nextActivityObject;
	}

	public ActivityObject getNextActivityObject()
	{
		return this.nextActivityObject;
	}

	/**
	 * Sets whether the immediate next Activity Object is valid or not
	 * 
	 * @param code
	 *            -1 means 'not determined yet', '0' means 'was not invalid', '1' means 'was invalid'
	 */
	public void setImmediateNextActivityIsInvalid(int code)
	{
		this.immediateNextActivityIsInvalid = code;
	}

	public int getImmediateNextActivityInvalid()
	{
		return immediateNextActivityIsInvalid;
	}

	/**
	 * Fetches the current timeline from the given longer timeline from the recommendation point back until the matching
	 * unit length.
	 * 
	 * @param longerTimeline
	 *            the timelines (test timeline) from which the current timeline is to be extracted
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInHours
	 * @return
	 */
	public static TimelineWithNext getCurrentTimelineFromLongerTimelineMUHours(Timeline longerTimeline,
			Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm, double matchingUnitInHours)
	{
		System.out.println("------- Inside getCurrentTimelineFromLongerTimelineMUHours");

		Timestamp currentEndTimestamp = new Timestamp(dateAtRecomm.getYear(), dateAtRecomm.getMonth(),
				dateAtRecomm.getDate(), timeAtRecomm.getHours(), timeAtRecomm.getMinutes(), timeAtRecomm.getSeconds(),
				0);
		long currentEndTime = currentEndTimestamp.getTime();

		// this is a safe cast in this case
		long matchingUnitInMilliSeconds = (long) (matchingUnitInHours * 60 * 60 * 1000);// multiply(new
																						// BigDecimal(60*60*1000))).longValue();

		Timestamp currentStartTimestamp = new Timestamp(currentEndTime - matchingUnitInMilliSeconds);

		System.out.println("Starttime of current timeline=" + currentStartTimestamp);
		System.out.println("Endtime of current timeline=" + currentEndTimestamp);

		// identify the recommendation point in longer timeline

		ArrayList<ActivityObject> activityObjectsInCurrentTimeline = longerTimeline
				.getActivityObjectsBetweenTime(currentStartTimestamp, currentEndTimestamp);

		ActivityObject nextValidActivityObject = longerTimeline
				.getNextValidActivityAfterActivityAtThisTime(currentEndTimestamp);
		ActivityObject nextActivityObject = longerTimeline.getNextActivityAfterActivityAtThisTime(currentEndTimestamp);

		int isInvalid = -99;
		if (nextActivityObject.isInvalidActivityName())
		{
			isInvalid = 1;
		}
		else
			isInvalid = -1;

		TimelineWithNext currentTimeline = new TimelineWithNext(activityObjectsInCurrentTimeline,
				nextValidActivityObject);
		currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);
		System.out.println("------- Exiting getCurrentTimelineFromLongerTimelineMUHours");
		return currentTimeline;
	}

	/**
	 * Fetches the current timeline from the given longer timeline from the recommendation point back until the matching
	 * unit count Activity Objects.
	 * 
	 * @param longerTimeline
	 *            the timelines (test timeline) from which the current timeline is to be extracted
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCounts
	 * @return
	 */
	public static TimelineWithNext getCurrentTimelineFromLongerTimelineMUCount(Timeline longerTimeline,
			Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm, double matchingUnitInCountsD)
	{
		int matchingUnitInCounts = (int) matchingUnitInCountsD;
		System.out.println("------Inside getCurrentTimelineFromLongerTimelineMUCount");

		if (Constant.verbose)
		{
			System.out.println("longer timeline=" + longerTimeline.getActivityObjectNamesWithTimestampsInSequence());// getActivityObjectNamesInSequence());
		}

		Timestamp currentEndTimestamp = new Timestamp(dateAtRecomm.getYear(), dateAtRecomm.getMonth(),
				dateAtRecomm.getDate(), timeAtRecomm.getHours(), timeAtRecomm.getMinutes(), timeAtRecomm.getSeconds(),
				0);
		// long currentEndTime=currentEndTimestamp.getTime();

		int indexOfCurrentEnd = longerTimeline.getIndexOfActivityObjectsAtTime(currentEndTimestamp);

		if (indexOfCurrentEnd - matchingUnitInCounts < 0)
		{
			System.out.println(
					"Warning: reducing matching units since don't have enough past to look in past,indexOfCurrentEnd("
							+ indexOfCurrentEnd + ")" + "- matchingUnitInCounts(" + matchingUnitInCounts + ")="
							+ (indexOfCurrentEnd - matchingUnitInCounts));
			matchingUnitInCounts = indexOfCurrentEnd;
		}

		// this is a safe cast in this case
		// long matchingUnitInMilliSeconds= (long)(matchingUnitInHours*60*60*1000);//multiply(new
		// BigDecimal(60*60*1000))).longValue();

		// Timestamp currentStartTimestamp = new Timestamp(currentEndTime- matchingUnitInMilliSeconds);

		int indexOfCurrentStart = indexOfCurrentEnd - matchingUnitInCounts;

		System.out.println("Start index of current timeline=" + indexOfCurrentStart);
		System.out.println("End index of current timeline=" + indexOfCurrentEnd);

		// identify the recommendation point in longer timeline

		ArrayList<ActivityObject> activityObjectsInCurrentTimeline = longerTimeline
				.getActivityObjectsInTimelineFromToIndex(indexOfCurrentStart, indexOfCurrentEnd + 1);

		ActivityObject nextValidActivityObject = longerTimeline
				.getNextValidActivityAfterActivityAtThisPosition(indexOfCurrentEnd);
		ActivityObject nextActivityObject = longerTimeline
				.getNextActivityAfterActivityAtThisPosition(indexOfCurrentEnd);

		int isInvalid = -99;
		if (nextActivityObject.isInvalidActivityName())
		{
			isInvalid = 1;
		}
		else
			isInvalid = -1;

		TimelineWithNext currentTimeline = new TimelineWithNext(activityObjectsInCurrentTimeline,
				nextValidActivityObject);
		currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);

		// System.out.println("Current timeline="+currentTimeline.getActivityObjectNamesInSequence());
		if (currentTimeline.getActivityObjectsInTimeline().size() != (matchingUnitInCounts + 1))
		// note: this is matching unit in counts reduced
		{
			System.err.println("Error: the current timeline does have #activity objs = adjusted matching unit");
		}
		System.out.println("Adjusted MU:" + matchingUnitInCounts);// + " Current Timeline:" +
																	// currentTimeline.getActivityObjectNamesWithTimestampsInSequence());
		System.out.println("------Exiting getCurrentTimelineFromLongerTimelineMUCount");
		return currentTimeline;
	}
}
