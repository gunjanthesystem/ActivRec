package org.activity.objects;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.VerbosityConstants;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

/**
 * Timeline is a chronological sequence of Activity Objects
 * 
 * @author gunjan
 *
 */
public class Timeline implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * If shouldBelongToSingleDay, the timelineID is date, otherwise it is timeline count.
	 */
	String timelineID;
	boolean shouldBelongToSingleUser, shouldBelongToSingleDay;
	ArrayList<ActivityObject> activityObjectsInTimeline;

	/**
	 * Keep count of the number of Timelines created until now
	 */
	static int countTimelinesCreatedUntilNow = 0;

	/**
	 * Create Timeline from given Activity Objects
	 * 
	 * 
	 * @param activityObjects
	 * @param shouldBelongToSingleDay
	 *            it also checks if all ao belong to same day
	 * @param shouldBelongToSingleUser
	 *            <font color = orange>currently, not checking if the ao's belong to same user</font>
	 */
	public Timeline(ArrayList<ActivityObject> activityObjects, boolean shouldBelongToSingleDay,
			boolean shouldBelongToSingleUser)
	{
		if (activityObjects.size() == 0)
		{
			System.err.println(PopUps
					.getCurrentStackTracedErrorMsg("Error in creating Timeline: Empty Activity Objects provided"));
			System.exit(5);
		}
		if (Constant.checkIfTimelineCreatedIsChronological && !TimelineUtils.isChronological(activityObjects))
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error: in Timeline(ArrayList<ActivityObject> activityObjects), CHRONOLOGY NOT PRESERVED"));
		}
		if (shouldBelongToSingleDay && !TimelineUtils.isSameDay(activityObjects))
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error: in Timeline(ArrayList<ActivityObject> activityObjects), shouldBelongToSingleDay= "
							+ shouldBelongToSingleDay + " but TimelineUtils.isSameDay(activityObjects)="
							+ TimelineUtils.isSameDay(activityObjects)));
		}

		this.shouldBelongToSingleDay = shouldBelongToSingleDay;
		this.shouldBelongToSingleUser = shouldBelongToSingleUser;

		this.activityObjectsInTimeline = activityObjects;

		countTimelinesCreatedUntilNow += 1;

		if (shouldBelongToSingleDay)
		{
			timelineID = String.valueOf(DateTimeUtils.getDate(activityObjects.get(0).getStartTimestamp()));
		}
		else
		{
			timelineID = String.valueOf(countTimelinesCreatedUntilNow);
		}
	}

	/**
	 * Returns the number of valid distinct Activity Names in this Timeline
	 * 
	 * @return
	 */
	public int countNumberOfValidDistinctActivities()
	{
		HashSet<String> set = new HashSet<String>();

		for (ActivityObject ao : activityObjectsInTimeline)
		{
			String actName = ao.getActivityName();
			if (UtilityBelt.isValidActivityName(actName))
			{
				set.add(actName.trim());
			}
		}
		return set.size();
	}

	/**
	 * Returns the number of valid Activity Names/Objects in this Timeline
	 * 
	 * @return
	 */
	public int countNumberOfValidActivities()
	{
		int count = 0;
		for (ActivityObject ao : activityObjectsInTimeline)
		{
			String actName = ao.getActivityName();
			if (UtilityBelt.isValidActivityName(actName))
			{
				count++;
			}
		}
		return count;
	}

	public ActivityObject getNextValidActivityAfterActivityAtThisTime(Timestamp timestamp)
	{
		// System.out.println("To find next activity object at :"+timestamp);
		int indexOfActivityObjectAtGivenTimestamp = getIndexOfActivityObjectsAtTime(timestamp);
		return getNextValidActivityAfterActivityAtThisPosition(indexOfActivityObjectAtGivenTimestamp);
	}

	/**
	 * Returns the next valid Activity Object in the Timeline after the given index.
	 * 
	 * @param indexOfActivityObject
	 * @return
	 */
	public ActivityObject getNextValidActivityAfterActivityAtThisPosition(int indexOfActivityObject)
	{
		ActivityObject nextValidActivityObject = null;
		int indexOfNextValidActivityObject = -99;

		if (indexOfActivityObject == this.activityObjectsInTimeline.size() - 1)// there are no next activities
		{
			System.out.println("\t No next activity in this timeline");
			return null;
		}

		for (int i = indexOfActivityObject + 1; i < activityObjectsInTimeline.size(); i++)
		{
			if (UtilityBelt.isValidActivityName(activityObjectsInTimeline.get(i).getActivityName()))
			{
				nextValidActivityObject = activityObjectsInTimeline.get(i);
				indexOfNextValidActivityObject = i;
				break;
			}
			else
			{
				System.out.println("\t\t (note: immediate next was an invalid activity)");
				continue;
			}
		}

		if (nextValidActivityObject == null)
		{
			System.err.println("Warning: No next valid activity after this index in the given timeline");
			System.err.println("\tThe timeline is:" + this.getActivityObjectNamesInSequence());
			System.err.println("\tEnd index index is:" + indexOfActivityObject);
			return nextValidActivityObject;
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("To find next activity object after index :" + indexOfActivityObject);
			System.out
					.print("\t getNextValidActivityAfterActivityAtThisPosition(): Index of activity object to look after is "
							+ indexOfActivityObject);
			System.err.println("\t The timeline is:" + this.getActivityObjectNamesInSequence());
			if (nextValidActivityObject.getActivityName()
					.equals(activityObjectsInTimeline.get(indexOfActivityObject).getActivityName()))
			{
				System.err
						.println("\n\t Warning: Next Valid activity has same name as current activity (for timelineID:"
								+ timelineID + ") Activity Name="
								+ activityObjectsInTimeline.get(indexOfActivityObject).getActivityName());
				// System.err.println("\t The timeline is:"+this.getActivityObjectNamesInSequence());

				System.err.println("\t End point index was:" + indexOfActivityObject);
				System.err.println("\t Next valid activity object found at index:" + indexOfNextValidActivityObject);
			}
			System.out.println("\t Next valid activity is " + nextValidActivityObject.getActivityName());
		}

		return nextValidActivityObject;
	}

	/**
	 * Find if the timeline contains atleast one of the recognised activities (except "unknown" and "others")
	 * 
	 * @return
	 */
	public boolean containsAtLeastOneValidActivity()
	{
		return activityObjectsInTimeline.stream().anyMatch(ao -> UtilityBelt.isValidActivityName(ao.getActivityName()));
	}

	public void printActivityObjectNamesInSequence()
	{
		System.out.println(getActivityObjectNamesInSequence());
	}

	public String getActivityObjectNamesInSequence()
	{
		StringBuilder sb = new StringBuilder();
		activityObjectsInTimeline.stream().forEach(ao -> sb.append(" >>" + ao.getActivityName()));
		return sb.toString();
	}

	///////////////
	public String getActivityObjectNamesInSequenceWithFeatures()
	{
		String res = "";
		for (ActivityObject ao : activityObjectsInTimeline)
		{
			res += (" >>" + ao.getActivityName() + "--" + ao.getStartTimestamp() + "--" + ao.getDurationInSeconds());
		}
		return res;
	}

	///////////////

	public void printActivityObjectNamesWithTimestampsInSequence()
	{
		for (int i = 0; i < activityObjectsInTimeline.size(); i++)
		{
			System.out.print(">>" + activityObjectsInTimeline.get(i).getActivityName() + "--"
					+ activityObjectsInTimeline.get(i).getStartTimestamp() + "--"
					+ activityObjectsInTimeline.get(i).getEndTimestamp());
		}
	}

	public String getActivityObjectNamesWithTimestampsInSequence()
	{
		StringBuilder res = new StringBuilder();

		for (int i = 0; i < activityObjectsInTimeline.size(); i++)
		{
			res.append(">>" + activityObjectsInTimeline.get(i).getActivityName() + "--"
					+ activityObjectsInTimeline.get(i).getStartTimestamp() + "--"
					+ activityObjectsInTimeline.get(i).getEndTimestamp());
		}
		return res.toString();
	}

	public String getActivityObjectNamesWithoutTimestampsInSequence()
	{
		StringBuilder res = new StringBuilder("");

		for (int i = 0; i < activityObjectsInTimeline.size(); i++)
		{

			res.append(" >>" + activityObjectsInTimeline.get(i).getActivityName() + "--"
					+ activityObjectsInTimeline.get(i).getStartTimestamp() + "--"
					+ activityObjectsInTimeline.get(i).getEndTimestamp());
		}
		return res.toString();
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<ActivityObject> getActivityObjectsInTimeline()
	{
		return this.activityObjectsInTimeline;
	}

	/**
	 * 
	 * @return the number of activity-objects in timeline
	 */
	public int size()
	{
		return this.activityObjectsInTimeline.size();
	}

	/**
	 * 
	 * @param from
	 * @param to
	 *            <font color = orange>exclusive</font>
	 * @return ArrayList of Activity Objects in the timeline from the 'from' index until before the 'to' index
	 */
	public ArrayList<ActivityObject> getActivityObjectsInTimelineFromToIndex(int from, int to) // to is exclusive
	{
		if (to > this.activityObjectsInTimeline.size())
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in getActivityObjectsInTimelineFromToIndex: 'to' index out of bounds. Num of Activity Objects in Timeline="
							+ this.activityObjectsInTimeline.size() + " while 'to' index is=" + to));
			return null;
		}

		ArrayList<ActivityObject> newList = new ArrayList<ActivityObject>();
		for (int i = from; i < to; i++)
		{
			newList.add(this.activityObjectsInTimeline.get(i));
		}
		return newList;
	}

	/**
	 * Returns a string whose characters in sequence are the codes for the activity names of the timeline.
	 * <p>
	 * <font color= green>Capable of handling atleast 400 different kinds of activity names<font>
	 * 
	 * @return
	 */
	public String getActivityObjectsAsStringCode()
	{
		StringBuilder stringCodeForTimeline = new StringBuilder();

		activityObjectsInTimeline.stream().forEach(ao -> stringCodeForTimeline.append(ao.getStringCode()));

		if (this.getActivityObjectsInTimeline().size() != stringCodeForTimeline.length())
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in getActivityObjectsAsStringCode(): stringCodeOfTimeline.length()!= timelineForUser.getActivityObjectsInTimeline().size()"));
		}
		return stringCodeForTimeline.toString();
	}

	/**
	 * 
	 * @param activityToCheck
	 * @return
	 */
	public long countContainsActivityName(String activityNameToCheck)
	{
		return activityObjectsInTimeline.stream().filter(ao -> ao.getActivityName().equals(activityNameToCheck))
				.count();
	}

	/**
	 * 
	 * @param activityNameToCheck
	 * @return num of non-last activity objects with the same act name as activityNameToCheck
	 */
	public int countContainsActivityNameButNotAsLast(String activityNameToCheck)
	{
		int containsCount = 0;
		for (int i = 0; i < this.activityObjectsInTimeline.size() - 1; i++)
		{
			if (this.activityObjectsInTimeline.get(i).getActivityName().equals(activityNameToCheck))
			{
				containsCount++;
			}
		}
		return containsCount;
	}

	/**
	 * TODO: Make sure the new change ,i.e. running until <size() is compatible with implemention of both Daywise and
	 * start time approach.
	 * 
	 * @param actNameToCheck
	 * @return
	 */
	public boolean hasAValidActAfterFirstOccurOfThisActName(String actNameToCheck)// ActivityObject activityToCheck)
	{
		boolean hasValidAfter = false;

		int indexOfFirstOccurrence = getIndexOfFirstOccurOfThisActName(actNameToCheck);

		if (indexOfFirstOccurrence < 0)
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in hasAValidActAfterFirstOccurOfThisActName: No Occurrence of the given activity in the given timeline, throwing exception"));
		}

		// not the last activity of the timeline
		if (indexOfFirstOccurrence < this.activityObjectsInTimeline.size() - 1)
		{
			for (int i = indexOfFirstOccurrence + 1; i < this.activityObjectsInTimeline.size()/* - 1 */; i++)
			{
				if (UtilityBelt.isValidActivityName(activityObjectsInTimeline.get(i).getActivityName()))
				{
					hasValidAfter = true;
					break;
				}
			}
		}
		return hasValidAfter;
	}

	/**
	 * 
	 * @param activityNameToCheck
	 * @return
	 */
	public boolean hasActivityName(String activityNameToCheck)
	{
		return this.activityObjectsInTimeline.stream().anyMatch(ao -> ao.getActivityName().equals(activityNameToCheck));
	}

	/**
	 * TODO: Make sure the new change ,i.e. running until <size() is compatible with implemention of both Daywise and
	 * start time approach.
	 * 
	 * @param actNameToCheck
	 * @return
	 */
	public int getIndexOfFirstOccurOfThisActName(String actNameToCheck)
	{
		int indexOfFirstOccurrence = -99;

		for (int i = 0; i < this.activityObjectsInTimeline.size()/* - 1 */; i++)
		{
			if (this.activityObjectsInTimeline.get(i).getActivityName().equals(actNameToCheck))
			{
				indexOfFirstOccurrence = i;
				break;
			}
		}
		return indexOfFirstOccurrence;
	}

	/**
	 * 
	 * @param activityNameToCheck
	 * @return
	 */
	public Timestamp getStartTimestampOfFirstOccurOfThisActName(String activityNameToCheck)
	{
		Timestamp timestampOfFirstOccurrence = null;

		for (ActivityObject ao : this.activityObjectsInTimeline)
		{
			if (ao.getActivityName().equals(activityNameToCheck))
			{
				timestampOfFirstOccurrence = ao.getStartTimestamp();
				break;
			}
		}
		return timestampOfFirstOccurrence;
	}

	/**
	 * 
	 * @return
	 */
	public static int getCountTimelinesCreatedUntilNow()
	{
		return countTimelinesCreatedUntilNow;
	}

	/**
	 * 
	 * @param timestamp
	 * @return nextActivityAfterActivityAtThisTime, null otherwise
	 */
	public ActivityObject getNextActivityAfterActivityAtThisTime(Timestamp timestamp)
	{
		// System.out.println("To find next activity object at :"+timestamp);
		ActivityObject ae = activityObjectsInTimeline.get(getIndexOfActivityObjectsAtTime(timestamp) + 1);
		if (ae == null)
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in getNextActivityAfterActivityAtThisTime. No next activity after ts = " + timestamp));
		}
		return ae;
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	public ActivityObject getNextActivityAfterActivityAtThisPosition(int index)
	{
		ActivityObject ae = null;
		// System.out.println("To find next activity object at :"+timestamp);
		if (index + 1 > activityObjectsInTimeline.size() - 1)
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in getNextActivityAfterActivityAtThisPosition: array index out of bounds"));
		}
		ae = activityObjectsInTimeline.get(index + 1);
		return ae;
	}

	/**
	 * Returns Activity Objects falling between given start time and end time (all activity objects intersecting this
	 * starttime-endtime interval is included. note: if you want to find activity object at time t, make start and end
	 * timestamp equal upto the resolution (usually seconds) required.
	 * 
	 * @param startTimestampC
	 * @param endTimestampC
	 * @param verbose
	 * @return
	 */
	public ArrayList<ActivityObject> getActivityObjectsBetweenTime(Timestamp st, Timestamp et)
	{
		ArrayList<ActivityObject> activityObjectsIn = (ArrayList<ActivityObject>) activityObjectsInTimeline.stream()
				.filter(ao -> ao.doesOverlap(st, et)).collect(Collectors.toList());

		// ////// for testing correctness
		if (VerbosityConstants.verbose)
		{
			System.out.print(
					"\t Inside getActivityObjectsBetweenTime: activity objects inside " + st + " and " + et + " are: ");
			activityObjectsIn.stream().forEach(ao -> System.out.print(">>" + ao.getActivityName()));
			System.out.println();
		}
		// /////////////////////////////////
		return activityObjectsIn;
	}

	/**
	 * 
	 * @param ts
	 * @return
	 */
	public int getIndexOfActivityObjectsAtTime(Timestamp ts)
	{
		ArrayList<Integer> res = new ArrayList<>();
		for (int i = 0; i < this.activityObjectsInTimeline.size(); i++)
		{
			ActivityObject ao = activityObjectsInTimeline.get(i);
			if (ao.getStartTimestamp().getTime() <= ts.getTime() && ao.getEndTimestamp().getTime() >= ts.getTime())
			{
				res.add(i);
			}
		}

		if (res.size() == 0)
		{
			System.err.println(
					PopUps.getCurrentStackTracedErrorMsg("Error in getActivityObjectAtTime: No AO at ts:" + ts));
			return -99;
		}

		if (res.size() > 1)
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in getActivityObjectAtTime: " + res.size() + " AOs (>1) at ts:" + ts));
			return -99;
		}
		else
			return res.get(0);
	}

	/**
	 * Returns activity object at given timestamp.
	 * <p>
	 * (ao.getStartTimestamp().getTime() <= timestampC.getTime() && ao.getEndTimestamp().getTime() >=
	 * timestampC.getTime())
	 * 
	 * @param ts
	 * @return
	 */
	public ActivityObject getActivityObjectAtTime(Timestamp ts)
	{
		ArrayList<ActivityObject> res = new ArrayList<>();

		for (ActivityObject ao : activityObjectsInTimeline)
		{
			if (ao.getStartTimestamp().getTime() <= ts.getTime() && ao.getEndTimestamp().getTime() >= ts.getTime())
			{
				res.add(ao);
			}
		}

		if (res.size() == 0)
		{
			System.err.println(
					PopUps.getCurrentStackTracedErrorMsg("Error in getActivityObjectAtTime: No AO at ts:" + ts));
			return null;
		}

		if (res.size() > 1)
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in getActivityObjectAtTime: " + res.size() + " AOs (>1) at ts:" + ts));
			return null;
		}
		else
			return res.get(0);
	}

	/**
	 * 
	 * @param startTimestamp
	 * @return
	 */
	public ArrayList<ActivityObject> getActivityObjectsStartingOnBeforeTime(Timestamp startTimestamp)
	{
		return (ArrayList<ActivityObject>) activityObjectsInTimeline.stream()
				.filter(ao -> ao.startsOnOrBefore(startTimestamp)).collect(Collectors.toList());
	}

	/**
	 * n takes values from 0 ...
	 * 
	 * @param n
	 * @return
	 */
	public ActivityObject getActivityObjectAtPosition(int n)
	{
		return this.activityObjectsInTimeline.get(n);
	}

	/**
	 * 
	 * @return
	 */
	public LongSummaryStatistics getDurationStats()
	{
		return this.getActivityObjectsInTimeline().stream()
				.collect(Collectors.summarizingLong(ao -> ao.getDurationInSeconds()));
	}

	/**
	 * 
	 * @return
	 */
	public String getStartDayName()
	{
		Date startDate = DateTimeUtils.getDate(this.activityObjectsInTimeline.get(0).getStartTimestamp());
		return DateTimeUtils.getWeekDayFromWeekDayInt(startDate.getDay());
	}

	/**
	 * @deprecated be careful, as in current setup we are not replying on it and timeline id for daywise timeline is the
	 *             date as string while otherwise it is the count,i.e, serial number of the timeline as String
	 * @return
	 */
	public String getTimelineID()
	{
		return this.timelineID;
	}

	public void setTimelineID(String tid)
	{
		this.timelineID = tid;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isShouldBelongToSingleUser()
	{
		return shouldBelongToSingleUser;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isShouldBelongToSingleDay()
	{
		return shouldBelongToSingleDay;
	}

	/**
	 * Returns the act objs in the day while also checking if this timeline is a single day timeline.
	 * 
	 * @return list of activity objects
	 */
	public ArrayList<ActivityObject> getActivityObjectsInDay()
	{
		if (!this.shouldBelongToSingleDay)
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in Timeline.getActivityObjectsInDay(). shouldBelongToSingleDay= "
							+ shouldBelongToSingleDay));
		}
		return this.activityObjectsInTimeline;
	}

	/**
	 * 
	 * @return the userID of first activity object in the timeline while also checking if this timeline is a single user
	 *         timeline
	 */
	public String getUserID()
	{
		if (!this.shouldBelongToSingleUser)
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"Error in Timeline.getActivityObjectsInDay(). shouldBelongToSingleUser= "
							+ shouldBelongToSingleUser));
		}
		return this.activityObjectsInTimeline.get(0).userID;

	}

}
