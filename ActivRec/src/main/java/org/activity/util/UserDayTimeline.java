package org.activity.util;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;

import org.activity.objects.Triple;

public class UserDayTimeline implements Serializable
{
	private static final long serialVersionUID = 2L;
	private ArrayList<ActivityObject> ActivityObjectsInDay;
	private String dateID, userID;

	private String dayName; // sunday, monday, etc..
	private Date date;

	public String getUserID()
	{
		return this.userID;
	}

	public String getDateID()
	{
		return dateID;
	}

	public int countNumberOfValidDistinctActivities()
	{
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < ActivityObjectsInDay.size(); i++)
		{
			if (UtilityBelt.isValidActivityName(ActivityObjectsInDay.get(i).getActivityName()))
			{
				set.add(ActivityObjectsInDay.get(i).getActivityName().trim());
			}
		}

		return set.size();
	}

	public int countNumberOfValidActivities()
	{
		int count = 0;
		for (int i = 0; i < ActivityObjectsInDay.size(); i++)
		{
			if (UtilityBelt.isValidActivityName(ActivityObjectsInDay.get(i).getActivityName()))
			{
				count += 1;
			}
		}

		return count;
	}

	public int getNumOfValidActivityObjectAfterThisTime(Timestamp timestamp)
	{
		int numOfValids = 0;

		while (getNextValidActivityAfterActivityAtThisTime(timestamp) != null)
		{
			numOfValids += 1;
			timestamp = getNextValidActivityAfterActivityAtThisTime(timestamp).getEndTimestamp();
		}

		System.out.println("Debug: num of valid after timestamp " + timestamp + " is: " + numOfValids);
		return numOfValids;

	}

	public ActivityObject getNextValidActivityAfterActivityAtThisTime(Timestamp timestamp)
	{
		// System.out.println("To find next activity event at :"+timestamp);
		ActivityObject nextValidActivityObject = null;

		int indexOfActivityObjectAtGivenTimestamp = getIndexOfActivityObjectsAtTime(timestamp);
		System.out.print(
				"inside:getNextValidActivityAfterActivityAtThisTime(): Index of activity event at this timestamp is:"
						+ indexOfActivityObjectAtGivenTimestamp + " \nNext valid activity after" + timestamp + " is ");
		if (indexOfActivityObjectAtGivenTimestamp == this.ActivityObjectsInDay.size() - 1)
		{
			// there are no next activities
			System.out.println("No next activity");
			return null;
		}

		for (int i = indexOfActivityObjectAtGivenTimestamp + 1; i < ActivityObjectsInDay.size(); i++)
		{
			if (UtilityBelt.isValidActivityName(ActivityObjectsInDay.get(i).getActivityName()))
			{
				nextValidActivityObject = ActivityObjectsInDay.get(i);
				break;
			}
		}

		if (nextValidActivityObject != null)
		{
			if (nextValidActivityObject.getActivityName()
					.equals(ActivityObjectsInDay.get(indexOfActivityObjectAtGivenTimestamp).getActivityName()))
			{
				System.err.println("\nWarning: Next Valid activity has same name as current activity (for timestamp:"
						+ timestamp + " userID:" + userID + ")Activity Name="
						+ ActivityObjectsInDay.get(indexOfActivityObjectAtGivenTimestamp).getActivityName());
			}

			System.out.println(nextValidActivityObject.getActivityName());
		}
		System.out.println("Warning: next valid activity after timestamp: " + timestamp + " is null");
		return nextValidActivityObject;
	}

	public ActivityObject getNextValidActivityAfterActivityAtThisPosition(int indexOfActivityObjectGiven)
	{
		ActivityObject nextValidActivityObject = null;

		System.out.print(
				"inside:getNextValidActivityAfterActivityAtThisTime(): Index of activity event at this timestamp is:"
						+ indexOfActivityObjectGiven + " \nNext valid activity after " + indexOfActivityObjectGiven
						+ " is ");
		if (indexOfActivityObjectGiven == this.ActivityObjectsInDay.size() - 1)
		{
			// there are no next activities
			System.out.println("No next activity");
			return null;
		}

		for (int i = indexOfActivityObjectGiven + 1; i < ActivityObjectsInDay.size(); i++)
		{
			// if((ActivityObjectsInDay.get(i).getActivityName().equalsIgnoreCase("Unknown") ||
			// ActivityObjectsInDay.get(i).getActivityName().equalsIgnoreCase("Others")) ==false)
			if (UtilityBelt.isValidActivityName(ActivityObjectsInDay.get(i).getActivityName()))
			// if(ActivityObjectsInDay.get(i).isInvalidActivityName() ==false)
			{
				nextValidActivityObject = ActivityObjectsInDay.get(i);
				break;
			}
		}

		if (nextValidActivityObject.getActivityName()
				.equals(ActivityObjectsInDay.get(indexOfActivityObjectGiven).getActivityName()))
		{
			System.err.println("\nWarning: Next Valid activity has same name as current activity (for index: "
					+ indexOfActivityObjectGiven + " userID:" + userID + ")Activity Name="
					+ ActivityObjectsInDay.get(indexOfActivityObjectGiven).getActivityName());
		}

		System.out.println(nextValidActivityObject.getActivityName());

		return nextValidActivityObject;
	}

	public static boolean isNoValidActivityAfterItInTheDay(int ActivityObjectIndex, UserDayTimeline theDayTimeline)
	{
		boolean isNoValidAfter = true;

		ArrayList<ActivityObject> eventsInDay = theDayTimeline.getActivityObjectsInDay();

		System.out.println("inside isNoValidActivityAfterItInTheDay");
		System.out.println("activityIndexAfterWhichToChecl=" + ActivityObjectIndex);

		theDayTimeline.printActivityObjectNamesInSequence();
		System.out.println("Number of activities in day=" + eventsInDay.size());

		if (ActivityObjectIndex == eventsInDay.size() - 1)
		{
			return true;
		}

		for (int i = ActivityObjectIndex + 1; i < eventsInDay.size(); i++)
		{
			if (UtilityBelt.isValidActivityName(eventsInDay.get(i).getActivityName()))
			{
				System.out.println("Activity making it false=" + eventsInDay.get(i).getActivityName());
				isNoValidAfter = false;
				break;
			}
		}

		System.out.println("No valid after is:" + isNoValidAfter);
		return isNoValidAfter;
	}

	public boolean containsOnlySingleActivity()
	{
		if (ActivityObjectsInDay.size() <= 1)
		{
			return true;
		}
		else
			return false;
	}

	/**
	 * Find if the day timelines contains atleast one of the recognised activities (except "unknown" and "others")
	 * 
	 * @return
	 */
	public boolean containsAtLeastOneValidActivity()
	{
		boolean containsValid = false;
		for (int i = 0; i < ActivityObjectsInDay.size(); i++)
		{
			if (UtilityBelt.isValidActivityName(ActivityObjectsInDay.get(i).getActivityName()))
			{
				containsValid = true;
			}
		}
		return containsValid;
	}

	public void printActivityObjectNamesInSequence()
	{
		for (int i = 0; i < ActivityObjectsInDay.size(); i++)
		{
			System.out.print(" >>" + ActivityObjectsInDay.get(i).getActivityName());
		}
	}

	public String getActivityObjectNamesInSequence()
	{
		String res = "";

		for (int i = 0; i < ActivityObjectsInDay.size(); i++)
		{
			res += (" >>" + ActivityObjectsInDay.get(i).getActivityName());
		}
		return res;
	}

	public String getActivityObjectNamesInSequenceWithFeatures()
	{
		String res = "";

		for (int i = 0; i < ActivityObjectsInDay.size(); i++)
		{
			res += (" >>" + ActivityObjectsInDay.get(i).getActivityName() + "--"
					+ ActivityObjectsInDay.get(i).getStartTimestamp() + "--"
					+ ActivityObjectsInDay.get(i).getDurationInSeconds());
		}
		return res;
	}

	public String getActivityObjectNamesWithTimestampsInSequence()
	{
		String res = null;
		for (int i = 0; i < ActivityObjectsInDay.size(); i++)
		{
			res += (" >>" + ActivityObjectsInDay.get(i).getActivityName() + "--"
					+ ActivityObjectsInDay.get(i).getStartTimestamp() + "--"
					+ ActivityObjectsInDay.get(i).getEndTimestamp());
		}
		return res.toString();
	}

	public void printActivityObjectNamesWithTimestampsInSequence()
	{
		System.out.print(getActivityObjectNamesWithTimestampsInSequence());
	}

	public ArrayList<ActivityObject> getActivityObjectsInDay()
	{
		return this.ActivityObjectsInDay;
	}

	public ArrayList<ActivityObject> getActivityObjectsInDayFromToIndex(int from, int to) // to is exclusive
	{
		if (to >= this.ActivityObjectsInDay.size())
		{
			System.err.println("Error in getActivityObjectsInDayFromToIndex: 'to' index out of bounds");
			return null;
		}

		ArrayList<ActivityObject> newList = new ArrayList<ActivityObject>();
		for (int i = from; i < to; i++)
		{
			newList.add(this.ActivityObjectsInDay.get(i));
		}
		return newList;
	}

	public String getActivityObjectsAsStringCode()
	{
		String stringCodeForDay = new String();

		for (int i = 0; i < ActivityObjectsInDay.size(); i++)
		{
			String activityName = ActivityObjectsInDay.get(i).getActivityName();

			// int activityID= generateSyntheticData.getActivityid(activityName);

			stringCodeForDay += StringCode.getStringCodeFromActivityName(activityName); // Character.toString
																						// ((char)(activityID+65));
																						// //getting the ascii code
																						// for (activity id+65)
		}

		return stringCodeForDay;
	}

	public int countContainsActivity(ActivityObject activityToCheck)
	{
		String activityName = activityToCheck.getActivityName();
		int containsCount = 0;

		for (int i = 0; i < this.ActivityObjectsInDay.size(); i++)
		{
			if (this.ActivityObjectsInDay.get(i).getActivityName().equals(activityName))
			{
				containsCount++;
			}
		}

		return containsCount;
	}

	public boolean hasActivityName(String activityNameToCheck)
	{
		String activityName = activityNameToCheck;
		int containsCount = 0;

		for (int i = 0; i < this.ActivityObjectsInDay.size(); i++)
		{
			if (this.ActivityObjectsInDay.get(i).getActivityName().equals(activityName))
			{
				containsCount++;
			}
		}
		if (containsCount > 0)
			return true;
		else
			return false;
	}

	public int countContainsActivityButNotAsLast(ActivityObject activityToCheck)
	{
		String activityName = activityToCheck.getActivityName();
		int containsCount = 0;

		for (int i = 0; i < this.ActivityObjectsInDay.size() - 1; i++)
		{
			if (this.ActivityObjectsInDay.get(i).getActivityName().equals(activityName))
			{
				containsCount++;
			}
		}

		return containsCount;
	}

	public boolean hasAValidActivityAfterFirstOccurrenceOfThisActivity(ActivityObject activityToCheck)
	{
		boolean hasValidAfter = false;

		int indexOfFirstOccurrence = getIndexOfFirstOccurrenceOfThisActivity(activityToCheck);

		try
		{

			if (indexOfFirstOccurrence < 0)
			{
				Exception errorException = new Exception(
						"Error in hasAValidActivityAfterFirstOccurrenceOfThisActivity: No Occurrence of the given activity in the given daytimeline, throwing exception");

			}

			if (indexOfFirstOccurrence < this.ActivityObjectsInDay.size() - 1) // not the last activity of the day
			{
				for (int i = indexOfFirstOccurrence + 1; i < this.ActivityObjectsInDay.size() - 1; i++)
				{
					// if((ActivityObjectsInDay.get(i).getActivityName().equalsIgnoreCase("Unknown") ||
					// ActivityObjectsInDay.get(i).getActivityName().equalsIgnoreCase("Others")) ==false)
					if (UtilityBelt.isValidActivityName(ActivityObjectsInDay.get(i).getActivityName()))
					// if(ActivityObjectsInDay.get(i).isInvalidActivityName() == false)
					{
						hasValidAfter = true;
						break;
					}
				}
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		return hasValidAfter;
	}

	public int getIndexOfFirstOccurrenceOfThisActivity(ActivityObject activityToCheck)
	{
		String activityName = activityToCheck.getActivityName();
		int indexOfFirstOccurrence = -99;

		for (int i = 0; i < this.ActivityObjectsInDay.size() - 1; i++)
		{
			if (this.ActivityObjectsInDay.get(i).getActivityName().equals(activityName))
			{
				indexOfFirstOccurrence = i;
				break;
			}
		}

		return indexOfFirstOccurrence;
	}

	public Timestamp getStartTimestampOfFirstOccurrenceOfThisActivity(ActivityObject activityToCheck)
	{
		String activityName = activityToCheck.getActivityName();
		Timestamp timestampOfFirstOccurrence = null;

		for (int i = 0; i < this.ActivityObjectsInDay.size() - 1; i++)
		{
			if (this.ActivityObjectsInDay.get(i).getActivityName().equals(activityName))
			{
				timestampOfFirstOccurrence = this.ActivityObjectsInDay.get(i).getStartTimestamp();
				break;
			}
		}

		return timestampOfFirstOccurrence;
	}

	UserDayTimeline()
	{

	}

	UserDayTimeline(ArrayList<ActivityObject> ActivityObjects, Date date)
	{
		this.date = date;
		if (ActivityObjects.size() == 0)
		{
			System.err.println("Error in creating Day Timeline: Empty Activity Events provided");
			System.exit(5);
		}

		else
		{
			this.ActivityObjectsInDay = ActivityObjects;

			if (isSameDay(ActivityObjectsInDay) == true)
			{
				this.dateID = ActivityObjectsInDay.get(0).getDimensionIDValue("Date_ID");

				this.dayName = ActivityObjectsInDay.get(0).getDimensionAttributeValue("Date_Dimension", "Week_Day")
						.toString();

				this.userID = ActivityObjectsInDay.get(0).getDimensionIDValue("User_ID");
			}

		}

	}

	public void createUserDayTimeline(ArrayList<ActivityObject> ActivityObjects, Date date)
	{
		this.date = date;

		if (ActivityObjects.size() == 0)
		{
			System.err.println("Error in creating Day Timeline: Empty Activity Events provided");
			System.exit(5);
		}

		else
		{
			this.ActivityObjectsInDay = ActivityObjects;

			if (isSameDay(ActivityObjectsInDay) == true)
			{
				this.dateID = ActivityObjectsInDay.get(0).getDimensionIDValue("Date_ID");

				this.dayName = ActivityObjectsInDay.get(0).getDimensionAttributeValue("Date_Dimension", "Week_Day")
						.toString();

				this.userID = ActivityObjectsInDay.get(0).getDimensionIDValue("User_ID");
			}
		}
	}

	public String getDayName()
	{
		return this.dayName;
	}

	// getNextActivityAfterActivityAtThisTime

	public ActivityObject getNextActivityAfterActivityAtThisTime(Timestamp timestamp)
	{
		System.out.println("To find next activity event at :" + timestamp);
		ActivityObject ae = ActivityObjectsInDay.get(getIndexOfActivityObjectsAtTime(timestamp) + 1);
		if (ae != null)
			return ae;
		else
			return new ActivityObject();
	}

	/**
	 * Finds the valid Activity Object in this timeline whose start time in the day is nearest to the start time of
	 * current Activity Object
	 * 
	 * 
	 * @param t
	 * @return a Pair with first value as the index of the found Activity Object in the timeline and the second value is
	 *         the absolute difference of the start time in day of this Activity Object and the start time of current
	 *         Activity Object
	 */
	public Triple<Integer, ActivityObject, Double> getTimeDiffValidActivityObjectWithStartTimeNearestTo(Timestamp t)
	{
		/** Seconds in that day before the timestamp t which is start timestamp of the current activity object **/
		long secsCO_ST_InDay = t.getHours() * 60 * 60 + t.getMinutes() * 60 + t.getSeconds();

		int indexOfActivityObjectNearestST = -9999;
		long leastDistantSTVal = 999999;

		for (int i = 0; i < this.ActivityObjectsInDay.size(); i++)
		{
			if (ActivityObjectsInDay.get(i).isInvalidActivityName()) continue;

			Timestamp aoTs = ActivityObjectsInDay.get(i).getStartTimestamp();

			/** Seconds in that day before the Activity Object's start timestamp **/
			long secsAO_ST_InDay = aoTs.getHours() * 60 * 60 + aoTs.getMinutes() * 60 + aoTs.getSeconds();

			long absDiffSecs = Math.abs(secsAO_ST_InDay - secsCO_ST_InDay);

			if (absDiffSecs < leastDistantSTVal)
			{
				leastDistantSTVal = absDiffSecs;
				indexOfActivityObjectNearestST = i;
			}
		}

		System.out.println("In the daytimeline (User = " + this.userID + ", Date=" + this.dateID + "). "
				+ "The index of Activity Object with ST nearest to current_ST(=" + t + "is: "
				+ indexOfActivityObjectNearestST + " with time diff of " + leastDistantSTVal);
		return new Triple(indexOfActivityObjectNearestST, this.ActivityObjectsInDay.get(indexOfActivityObjectNearestST),
				(double) leastDistantSTVal);
	}

	/**
	 * 
	 * note: if you want to find activity event at time t, make start and end timestamp equal upto the resolution
	 * (usually seconds) required.
	 * 
	 * @param startTimestampC
	 * @param endTimestampC
	 * @return
	 */
	/*
	 * public ArrayList<ActivityObject> getActivityObjectsBetweenTime(Timestamp startTimestampC, Timestamp
	 * endTimestampC) { ArrayList<ActivityObject> ActivityObjectsIn=new ArrayList<ActivityObject>();
	 * 
	 * for(int i=0;i<this.ActivityObjectsInDay.size();i++) { /*$$30Sep long
	 * intersectionOfActivityObjectAndIntervalInSeconds=
	 * ActivityObjectsInDay.get(i).intersectingIntervalInSeconds(startTimestampC, endTimestampC);
	 * if(intersectionOfActivityObjectAndIntervalInSeconds >0) { ActivityObjectsIn.add(ActivityObjectsInDay.get(i));
	 * }$$30Sep
	 */
	// 30Sep refactoring
	// long intersectionOfActivityObjectAndIntervalInSeconds=
	// ActivityObjectsInDay.get(i).intersectingIntervalInSeconds(startTimestampC, endTimestampC);
	/*
	 * if( ActivityObjectsInDay.get(i).doesOverlap(startTimestampC,
	 * endTimestampC))//intersectionOfActivityObjectAndIntervalInSeconds >0) {
	 * ActivityObjectsIn.add(ActivityObjectsInDay.get(i)); } // }
	 * 
	 * System.out.println("Intersect: The activity events inside "+startTimestampC+ " and "+endTimestampC+" are ");
	 * for(int i=0;i<ActivityObjectsIn.size();i++) { System.out.print(ActivityObjectsIn.get(i).getActivityName()); }
	 * return ActivityObjectsIn; }
	 */

	// ///
	/*
	 * /**
	 * 
	 * note: if you want to find activity event at time t, make start and end timestamp equal upto the resolution
	 * (usually seconds) required.
	 * 
	 * @param startTimestampC
	 * 
	 * @param endTimestampC
	 * 
	 * @return
	 */
	/*
	 * public ArrayList<ActivityObject> getActivityEventsBetweenTime(Timestamp startTimestampC, Timestamp endTimestampC)
	 * { ArrayList<ActivityObject> activityEventsIn=new ArrayList<ActivityObject>();
	 * 
	 * for(int i=0;i<this.ActivityObjectsInDay.size();i++) { long intersectionOfActivityEventAndIntervalInSeconds=
	 * ActivityObjectsInDay.get(i).intersectingIntervalInSeconds(startTimestampC, endTimestampC);
	 * if(intersectionOfActivityEventAndIntervalInSeconds >0) { activityEventsIn.add(ActivityObjectsInDay.get(i)); } }
	 * 
	 * System.out.println("Intersect: The activity events inside "+startTimestampC+ " and "+endTimestampC+" are ");
	 * for(int i=0;i<activityEventsIn.size();i++) { System.out.print(activityEventsIn.get(i).getActivityName()); }
	 * return activityEventsIn; }
	 */
	// ///

	/**
	 * 
	 * @param timestampC
	 * @return
	 */
	public int getIndexOfActivityObjectsAtTime(Timestamp timestampC)
	{
		int index = -99;
		int count = 0;
		for (int i = 0; i < this.ActivityObjectsInDay.size(); i++)
		{
			// System.out.println(" >> timestamp to check ="+timestampC+" startTimestamp for
			// this="+ActivityObjectsInDay.get(i).getStartTimestamp()+" end time stamp for
			// this="+ActivityObjectsInDay.get(i).getEndTimestamp());
			if (ActivityObjectsInDay.get(i).getStartTimestamp().getTime() <= timestampC.getTime()
					&& ActivityObjectsInDay.get(i).getEndTimestamp().getTime() >= timestampC.getTime()) // end point
																										// exclusive
			{ // NOTICE : DO I NEED TO MAKE IT EXCLUSIVE OF EITHER BEGIN TIME OR END?
				// System.out.println("Qualifies");
				index = i;
				count++;
				break;
			}
		}

		if (count > 1)
		{
			System.err.println(
					"Error in  getIndexOfActivityObjectsAtTime(): more than one activites identified at a given point of time for a user.");
		}

		if (count == 0)
		{
			System.out.println("Warning in getIndexOfActivityObjectsAtTime: no activity object found at: "
					+ timestampC.toString() + " while the activity objects in timeline are:\n");
			this.printActivityObjectNamesWithTimestampsInSequence();
		}
		// System.out.println("Intersect: The activity events inside "+timestampC+ " has "+startTimestamp+" are ");

		return index;
	}

	public ActivityObject getActivityObjectAtTime(Timestamp timestampC)
	{
		ActivityObject ao = null;
		int count = 0;

		for (int i = 0; i < this.ActivityObjectsInDay.size(); i++)
		{
			// System.out.println(" >> timestamp to check ="+timestampC+" startTimestamp for
			// this="+activityObjectsInTimeline.get(i).getStartTimestamp()+" end time stamp for
			// this="+activityObjectsInTimeline.get(i).getEndTimestamp());
			if (ActivityObjectsInDay.get(i).getStartTimestamp().getTime() <= timestampC.getTime()
					&& ActivityObjectsInDay.get(i).getEndTimestamp().getTime() >= timestampC.getTime()) // end
																										// point
																										// exclusive
			{ // NOTICE : DO I NEED TO MAKE IT EXCLUSIVE OF EITHER BEGIN TIME OR END?
				// System.out.println("Qualifies");
				ao = ActivityObjectsInDay.get(i);
				count++;
				break;
			}
		}

		if (count > 1)
		{
			System.err.println(
					"Error in  getActivityObjectAtTime(): more than one activites identified at a given point of time for a user.");
		}

		// System.out.println("Intersect: The activity objects inside "+timestampC+ " has "+startTimestamp+" are ");

		if (ao == null)
		{
			System.err
					.println("Error in  getActivityObjectAtTime(): No Activity object at this timestamp:" + timestampC);
			new Exception().printStackTrace();
		}
		return ao;
	}

	public ArrayList<ActivityObject> getActivityObjectsStartingOnBeforeTime(Timestamp startTimestampC)
	{
		ArrayList<ActivityObject> ActivityObjectsIn = new ArrayList<ActivityObject>();

		for (int i = 0; i < this.ActivityObjectsInDay.size(); i++)
		{
			if (ActivityObjectsInDay.get(i).startsOnOrBefore(startTimestampC))
			{
				ActivityObjectsIn.add(ActivityObjectsInDay.get(i));
			}
		}

		/*
		 * System.out.println("Intersect: The activity events inside "+startTimestampC+ " and "+endTimestampC+" are ");
		 * for(int i=0;i<ActivityObjectsIn.size();i++) { System.out.print(ActivityObjectsIn.get(i).getActivityName()); }
		 */
		return ActivityObjectsIn;
	}

	public ActivityObject getActivityObjectAtPosition(int n)
	{
		return this.ActivityObjectsInDay.get(n);
	}

	public boolean isSameDay(ArrayList<ActivityObject> ActivityObjectsInDay) // checks whether all activity events in
																				// the day timeline are of the same day
																				// or
																				// not
	{
		boolean sane = true;

		if (this.ActivityObjectsInDay.size() > 0)
		{
			Date date = (Date) ActivityObjectsInDay.get(0).getDimensionAttributeValue("Date_Dimension", "Date");

			for (int i = 1; i < ActivityObjectsInDay.size(); i++)
			{
				if (date.equals((Date) ActivityObjectsInDay.get(i).getDimensionAttributeValue("Date_Dimension",
						"Date")) == false)
				{
					sane = false;
				}
			}
		}

		if (!sane)
		{
			System.err.println("Error: Day Timeline  contains ActivityObjects from more than one day"); /*
																										 * with
																										 * Date_ID:"+ dateID+"
																										 */
			// System.exit(3);
		}
		return sane;
	}

}
