// package org.activity.objects;
//
// import java.sql.Date;
// import java.sql.Timestamp;
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.LinkedHashMap;
// import java.util.LongSummaryStatistics;
// import java.util.Map;
// import java.util.stream.Collectors;
//
// import org.activity.constants.Constant;
// import org.activity.constants.VerbosityConstants;
// import org.activity.util.StringCode;
// import org.activity.util.TimelineUtils;
// import org.activity.util.UtilityBelt;
//
/// **
// * Timeline is a chronological sequence of Activity Objects
// *
// * @author gunjan
// *
// */
// public class Timeline_backup14March2017 implements TimelineI
// {
// Integer timelineID;
// ArrayList<ActivityObject> activityObjectsInTimeline;
//
// /**
// * Keep count of the number of Timelines created until now
// */
// static int countTimelinesCreatedUntilNow = 0;
//
// /**
// * Create an empty timeline
// */
// public Timeline_backup14March2017()
// {
// }
//
// /**
// * Create a Timeline with give ID and list of ActivityObjects
// *
// * @param id
// * @param arrOs
// */
// public Timeline_backup14March2017(Integer id, ArrayList<ActivityObject> arrOs)
// {
// this.timelineID = id;
// this.activityObjectsInTimeline = arrOs;
// }
//
// /**
// * Create Timeline from given Activity Objects
// *
// * @param activityObjects
// */
// public Timeline_backup14March2017(ArrayList<ActivityObject> activityObjects)
// {
// /*
// * if(activityObjects.size() ==0 ) {
// * System.err.println("Error in creating Timeline: Empty Activity Objects provided"); System.exit(5); }
// *
// * else
// */
// {
// countTimelinesCreatedUntilNow += 1;
// this.activityObjectsInTimeline = activityObjects;
// timelineID = countTimelinesCreatedUntilNow;// new UID().toString();
// /*
// * if(isSameDay(activityObjectsInTimeline) == true) {
// * this.dateID=activityObjectsInTimeline.get(0).getDimensionIDValue("Date_ID");
// *
// * this.dayName=activityObjectsInTimeline.get(0).getDimensionAttributeValue("Date_Dimension",
// * "Week_Day").toString();
// *
// * this.userID=activityObjectsInTimeline.get(0).getDimensionIDValue("User_ID"); }
// */
// }
//
// if (!TimelineUtils.isChronological(activityObjects))
// {
// System.err
// .println("Error: in Timeline(ArrayList<ActivityObject> activityObjects), CHRONOLOGY NOT PRESERVED");
// }
//
// }
//
// /**
// * Create Timeline from a given UserDayTimeline
// *
// * @param dayTimeline
// */
// public Timeline_backup14March2017(UserDayTimeline dayTimeline)
// {
// ArrayList<ActivityObject> activityObjects = dayTimeline.getActivityObjectsInDay();
//
// if (activityObjects.size() == 0)
// {
// System.err.println("Error in creating Timeline: Empty Activity Objects provided");
// System.exit(5);
// }
//
// else
// {
// countTimelinesCreatedUntilNow += 1;
// this.activityObjectsInTimeline = activityObjects;
// timelineID = countTimelinesCreatedUntilNow;// new UID().toString();
// /*
// * if(isSameDay(activityObjectsInTimeline) == true) {
// * this.dateID=activityObjectsInTimeline.get(0).getDimensionIDValue("Date_ID");
// *
// * this.dayName=activityObjectsInTimeline.get(0).getDimensionAttributeValue("Date_Dimension",
// * "Week_Day").toString();
// *
// * this.userID=activityObjectsInTimeline.get(0).getDimensionIDValue("User_ID"); }
// */
// }
// }
//
// /**
// * Creates a Timeline consisting of the Activity Objects from the given LinkedHashMap of UserDayTimelines thus
// * essentially converts mulitple day timelines into a single continuous single timeline
// *
// * @param dayTimelines
// * LinkedHashMap of UserDayTimelines
// */
// public Timeline_backup14March2017(LinkedHashMap<Date, UserDayTimeline> dayTimelines)
// {
// long dt = System.currentTimeMillis();
// ArrayList<ActivityObject> activityObjects = new ArrayList<ActivityObject>();
//
// for (Map.Entry<Date, UserDayTimeline> entry : dayTimelines.entrySet())
// {
// UserDayTimeline dayTimelineEntry = entry.getValue();
// activityObjects.addAll(dayTimelineEntry.getActivityObjectsInDay());
// }
//
// if (activityObjects.size() == 0)
// {
// System.err.println("Error in creating Timeline: Empty Activity Objects provided");
// System.exit(5);
// }
//
// else
// {
// countTimelinesCreatedUntilNow += 1;
// this.activityObjectsInTimeline = activityObjects;
// timelineID = countTimelinesCreatedUntilNow;// new UID().toString();
// }
//
// if (Constant.checkIfTimelineCreatedIsChronological)
// {
// if (!TimelineUtils.isChronological(activityObjects))
// {
// System.err.println(
// "Error: in Timeline(LinkedHashMap<Date,UserDayTimeline> dayTimelines), CHRONOLOGY NOT PRESERVED");
// }
// }
// long lt = System.currentTimeMillis();
//
// if (VerbosityConstants.verboseTimelineCleaning) System.out.println(
// "Creating timelines for " + dayTimelines.size() + " daytimelines takes " + (lt - dt) / 1000 + " secs");
// }
//
// /**
// * Creates a Timeline consisting of the Activity Objects from the given LinkedHashMap of UserDayTimelines thus
// * essentially converts mulitple day timelines into a single continuous single timeline with given ID for the
// * timeline
// *
// * @param dayTimelines
// * @param timelineID
// * this can be the user id to whom the timeline belongs or any another identifier relevant for the need.
// */
// public Timeline_backup14March2017(LinkedHashMap<Date, UserDayTimeline> dayTimelines, int timelineID)
// {
// long dt = System.currentTimeMillis();
// ArrayList<ActivityObject> activityObjects = new ArrayList<ActivityObject>();
//
// if (dayTimelines.size() == 0 || dayTimelines == null)
// {
// new Exception("Error creating new Timeline: num of day timelines " + dayTimelines.size());
// }
//
// for (Map.Entry<Date, UserDayTimeline> entry : dayTimelines.entrySet())
// {
// UserDayTimeline dayTimelineEntry = entry.getValue();
// activityObjects.addAll(dayTimelineEntry.getActivityObjectsInDay());
// }
//
// // if (activityObjects.size() == 0) commented out on April 19 2016 begin
// // {
// // System.err.println("Error in creating Timeline: Empty Activity Objects provided");
// // new Exception("Error in creating Timeline: Empty Activity Objects provided").printStackTrace();
// // System.exit(5);
// // }
//
// // else commented out on April 19 2016 end
// {
// countTimelinesCreatedUntilNow += 1;
// this.activityObjectsInTimeline = activityObjects;
// this.timelineID = timelineID;// new UID().toString();
// }
//
// if (!TimelineUtils.isChronological(activityObjects))
// {
// System.err.println(
// "Error: in Timeline(LinkedHashMap<Date,UserDayTimeline> dayTimelines), CHRONOLOGY NOT PRESERVED");
// }
// long lt = System.currentTimeMillis();
//
// if (VerbosityConstants.verboseTimelineCleaning) System.out.println(
// "Creating timelines for " + dayTimelines.size() + " daytimelines takes " + (lt - dt) / 1000 + " secs");
// }
//
// public Integer getTimelineID()
// {
// return this.timelineID;
// }
//
// /**
// * Returns the number of valid distinct Activity Names in this Timeline
// *
// * @return
// */
// public int countNumberOfValidDistinctActivities()
// {
// HashSet<String> set = new HashSet<String>();
//
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// if (UtilityBelt.isValidActivityName(activityObjectsInTimeline.get(i).getActivityName()))
// // ((activityObjectsInTimeline.get(i).getActivityName().equalsIgnoreCase("Unknown") ||
// // activityObjectsInTimeline.get(i).getActivityName().equalsIgnoreCase("Others"))
// // ==false)
// {
// set.add(activityObjectsInTimeline.get(i).getActivityName().trim());
// }
// }
// return set.size();
// }
//
// /**
// * Returns the number of valid Activity Names/Objects in this Timeline
// *
// * @return
// */
// public int countNumberOfValidActivities()
// {
// int count = 0;
//
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// if (UtilityBelt.isValidActivityName(activityObjectsInTimeline.get(i).getActivityName()))
// // ((activityObjectsInTimeline.get(i).getActivityName().equalsIgnoreCase("Unknown") ||
// // activityObjectsInTimeline.get(i).getActivityName().equalsIgnoreCase("Others"))
// // ==false)
// {
// count++;
// }
// }
//
// return count;
// }
//
// public ActivityObject getNextValidActivityAfterActivityAtThisTime(Timestamp timestamp)
// {
// // System.out.println("To find next activity object at :"+timestamp);
// ActivityObject nextValidActivityObject = null;
// int indexOfActivityObjectAtGivenTimestamp = getIndexOfActivityObjectsAtTime(timestamp);
// return getNextValidActivityAfterActivityAtThisPosition(indexOfActivityObjectAtGivenTimestamp);
// }
//
// /**
// * Returns a list of distinct dates in the timeline
// *
// * @return list of distinct dates in the times
// */
// public ArrayList<Date> getAllDistinctDates()
// {
// ArrayList<Date> allDistinctDates = new ArrayList<Date>();
//
// System.out.println("Inside getAllDistinctDays");
//
// if (this.activityObjectsInTimeline.size() > 0)
// {
// Date date = (Date) activityObjectsInTimeline.get(0).getDimensionAttributeValue("Date_Dimension", "Date");
// if (!allDistinctDates.contains(date))
// {
// allDistinctDates.add(date);
// }
// }
//
// return allDistinctDates;
// }
//
// /**
// * Returns the next valid Activity Object in the Timeline after the given index.
// *
// * @param indexOfActivityObject
// * @return
// */
// public ActivityObject getNextValidActivityAfterActivityAtThisPosition(int indexOfActivityObject)
// {
// // System.out.println("To find next activity object at :"+timestamp);
// ActivityObject nextValidActivityObject = null;
// int indexOfNextValidActivityObject = -99;
//
// // $$System.out.print("\t getNextValidActivityAfterActivityAtThisPosition(): Index of activity object to look
// // after is " + indexOfActivityObject +
// // " \n\t Next valid activity is ");
//
// if (indexOfActivityObject == this.activityObjectsInTimeline.size() - 1)// there are no next activities
// {
// System.out.println("\t No next activity in this timeline");
// return null;
// }
//
// for (int i = indexOfActivityObject + 1; i < activityObjectsInTimeline.size(); i++)
// {
// if (UtilityBelt.isValidActivityName(activityObjectsInTimeline.get(i).getActivityName())) //
// if((activityObjectsInTimeline.get(i).getActivityName().equalsIgnoreCase("Unknown")
// // ||
// // activityObjectsInTimeline.get(i).getActivityName().equalsIgnoreCase("Others"))
// // ==false)
// {
// nextValidActivityObject = activityObjectsInTimeline.get(i);
// indexOfNextValidActivityObject = i;
// break;
// }
// else
// {
// System.out.println("\t\t (note: immediate next was an invalid activity)");
// continue;
// }
// }
//
// if (nextValidActivityObject == null)
// {
// return nextValidActivityObject;
// }
//
// if (nextValidActivityObject.getActivityName()
// .equals(activityObjectsInTimeline.get(indexOfActivityObject).getActivityName()))
// {
// if (VerbosityConstants.verbose)
// {
// System.err
// .println("\n\t Warning: Next Valid activity has same name as current activity (for timelineID:"
// + timelineID + ") Activity Name="
// + activityObjectsInTimeline.get(indexOfActivityObject).getActivityName());
// // System.err.println("\t The timeline is:"+this.getActivityObjectNamesInSequence());
//
// System.err.println("\t End point index was:" + indexOfActivityObject);
// System.err.println("\t Next valid activity object found at index:" + indexOfNextValidActivityObject);
// }
// }
// /*
// * if(nextValidActivityObject==null) WHY DEAD CODE {
// * System.err.println("Warning: No next valid activity after this index in the given timeline");
// * System.err.println("\tThe timeline is:"+this.getActivityObjectNamesInSequence());
// * System.err.println("\tEnd index index is:"+indexOfActivityObject); }
// */
// // $$System.out.println(nextValidActivityObject.getActivityName());
// return nextValidActivityObject;
// }
//
// public boolean containsOnlySingleActivity()
// {
// if (activityObjectsInTimeline.size() <= 1)
// {
// return true;
// }
// else
// return false;
// }
//
// /**
// * Find if the timeline contains atleast one of the recognised activities (except "unknown" and "others")
// *
// * @return
// */
// public boolean containsAtLeastOneValidActivity()
// {
// boolean containsValid = false;
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// String actName = activityObjectsInTimeline.get(i).getActivityName();
//
// if (UtilityBelt.isValidActivityName(actName))
// // if( (actName.equalsIgnoreCase("Unknown")||actName.equalsIgnoreCase("Others")) == false)
// {
// containsValid = true;
// }
// }
// return containsValid;
// }
//
// public void printActivityObjectNamesInSequence()
// {
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// System.out.print(" >>" + activityObjectsInTimeline.get(i).getActivityName());
// }
// }
//
// public String[] getActivityObjectNamesInSequenceStringArray()
// {
// String[] res = new String[activityObjectsInTimeline.size()];
//
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// res[i] = activityObjectsInTimeline.get(i).getActivityName();
// }
// return res;
// }
//
// public String getActivityObjectNamesInSequence()
// {
// StringBuilder res = new StringBuilder("");
//
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// res.append(">" + activityObjectsInTimeline.get(i).getActivityName());
// }
// return res.toString();
// }
//
// public void printActivityObjectNamesWithTimestampsInSequence()
// {
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// System.out.print(">>" + activityObjectsInTimeline.get(i).getActivityName() + "--"
// + activityObjectsInTimeline.get(i).getStartTimestamp() + "--"
// + activityObjectsInTimeline.get(i).getEndTimestamp());
// }
// }
//
// public String getActivityObjectNamesWithTimestampsInSequence()
// {
// StringBuilder res = new StringBuilder();
//
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// res.append(">>" + activityObjectsInTimeline.get(i).getActivityName() + "--"
// + activityObjectsInTimeline.get(i).getStartTimestamp() + "--"
// + activityObjectsInTimeline.get(i).getEndTimestamp());
// // System.out.print(" >>"+activityObjectsInTimeline.get(i).getActivityName()+"--"
// // +activityObjectsInTimeline.get(i).getStartTimestamp()+"--"
// // +activityObjectsInTimeline.get(i).getEndTimestamp());
// }
// return res.toString();
// }
//
// public String getActivityObjectNamesWithoutTimestampsInSequence()
// {
// StringBuilder res = new StringBuilder("");
//
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
//
// res.append(" >>" + activityObjectsInTimeline.get(i).getActivityName() + "--"
// + activityObjectsInTimeline.get(i).getStartTimestamp() + "--"
// + activityObjectsInTimeline.get(i).getEndTimestamp());
// // System.out.print(" >>"+activityObjectsInTimeline.get(i).getActivityName()+"--"
// // +activityObjectsInTimeline.get(i).getStartTimestamp()+"--"
// // +activityObjectsInTimeline.get(i).getEndTimestamp());
// }
// return res.toString();
// }
//
// /**
// *
// * @return
// */
// public ArrayList<ActivityObject> getActivityObjectsInTimeline()
// {
// return this.activityObjectsInTimeline;
// }
//
// /**
// *
// * @return the number of activity-objects in timeline
// */
// public int size()
// {
// return this.activityObjectsInTimeline.size();
// }
//
// /**
// *
// * @param from
// * @param to
// * @return ArrayList of Activity Objects in the timeline from the 'from' index until before the 'to' index
// */
// public ArrayList<ActivityObject> getActivityObjectsInTimelineFromToIndex(int from, int to) // to is exclusive
// {
// if (to > this.activityObjectsInTimeline.size())
// {
// System.err.println(
// "Error in getActivityObjectsInTimelineFromToIndex: 'to' index out of bounds. Num of Activity Objects in Timeline="
// + this.activityObjectsInTimeline.size() + " while 'to' index is=" + to);
// // "");
// return null;
// }
//
// ArrayList<ActivityObject> newList = new ArrayList<ActivityObject>();
// for (int i = from; i < to; i++)
// {
// newList.add(this.activityObjectsInTimeline.get(i));
// }
// return newList;
// }
//
// /**
// * Returns a string whose characters in sequence are the codes for the activity names of the timeline.
// *
// * @return
// */
// public String getActivityObjectsAsStringCode()
// {
// StringBuilder stringCodeForTimeline = new StringBuilder();// changed from String to StringBuilder on 20 Sep 2016
//
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// String activityName = activityObjectsInTimeline.get(i).getActivityName();
//
// // int activityID= generateSyntheticData.getActivityid(activityName);
//
// stringCodeForTimeline.append(StringCode.getStringCodeFromActivityName(activityName)); // Character.toString
// // ((char)(activityID+65));
// // //getting the
// // ascii code for
// // (activity id+65)
// }
//
// if (this.getActivityObjectsInTimeline().size() != stringCodeForTimeline.length())
// {
// System.err.println(
// "Error in getActivityObjectsAsStringCode(): stringCodeOfTimeline.length()!=
// timelineForUser.getActivityObjectsInTimeline().size()");
// }
//
// return stringCodeForTimeline.toString();
// }
//
// /**
// *
// * @return
// */
// public ArrayList<Integer> getActivityObjectsAsSequenceOfNumericCodes()
// {
// ArrayList<Integer> numericCodes = new ArrayList<Integer>();
// String stringCodeForTimeline = new String();
//
// for (int i = 0; i < activityObjectsInTimeline.size(); i++)
// {
// String activityName = activityObjectsInTimeline.get(i).getActivityName();
//
// // int activityID= generateSyntheticData.getActivityid(activityName);
//
// stringCodeForTimeline += StringCode.getStringCodeFromActivityName(activityName); // Character.toString
// // ((char)(activityID+65));
// // //getting the ascii
// // code for (activity
// // id+65)
// }
//
// if (this.getActivityObjectsInTimeline().size() != stringCodeForTimeline.length())
// {
// System.err.println(
// "Error in getActivityObjectsAsStringCode(): stringCodeOfTimeline.length()!=
// timelineForUser.getActivityObjectsInTimeline().size()");
// }
//
// return numericCodes;
// }
//
// public int countContainsActivity(ActivityObject activityToCheck)
// {
// String activityName = activityToCheck.getActivityName();
// int containsCount = 0;
//
// for (int i = 0; i < this.activityObjectsInTimeline.size(); i++)
// {
// if (this.activityObjectsInTimeline.get(i).getActivityName().equals(activityName))
// {
// containsCount++;
// }
// }
//
// return containsCount;
// }
//
// public int countContainsActivityButNotAsLast(ActivityObject activityToCheck)
// {
// String activityName = activityToCheck.getActivityName();
// int containsCount = 0;
//
// for (int i = 0; i < this.activityObjectsInTimeline.size() - 1; i++)
// {
// if (this.activityObjectsInTimeline.get(i).getActivityName().equals(activityName))
// {
// containsCount++;
// }
// }
//
// return containsCount;
// }
//
// public boolean hasAValidActivityAfterFirstOccurrenceOfThisActivity(ActivityObject activityToCheck)
// {
// boolean hasValidAfter = false;
//
// int indexOfFirstOccurrence = getIndexOfFirstOccurrenceOfThisActivity(activityToCheck);
//
// try
// {
//
// if (indexOfFirstOccurrence < 0)
// {
// Exception errorException = new Exception(
// "Error in hasAValidActivityAfterFirstOccurrenceOfThisActivity: No Occurrence of the given activity in the given
// timeline, throwing exception");
//
// }
//
// if (indexOfFirstOccurrence < this.activityObjectsInTimeline.size() - 1) // not the last activity of the
// // timeline
// {
// for (int i = indexOfFirstOccurrence + 1; i < this.activityObjectsInTimeline.size() - 1; i++)
// {
// if (UtilityBelt.isValidActivityName(activityObjectsInTimeline.get(i).getActivityName()))
// // if((activityObjectsInTimeline.get(i).getActivityName().equalsIgnoreCase("Unknown") ||
// // activityObjectsInTimeline.get(i).getActivityName().equalsIgnoreCase("Others"))
// // ==false)
// {
// hasValidAfter = true;
// break;
// }
// }
// }
// }
//
// catch (Exception e)
// {
// e.printStackTrace();
// }
// return hasValidAfter;
// }
//
// public int getIndexOfFirstOccurrenceOfThisActivity(ActivityObject activityToCheck)
// {
// String activityName = activityToCheck.getActivityName();
// int indexOfFirstOccurrence = -99;
//
// for (int i = 0; i < this.activityObjectsInTimeline.size() - 1; i++)
// {
// if (this.activityObjectsInTimeline.get(i).getActivityName().equals(activityName))
// {
// indexOfFirstOccurrence = i;
// break;
// }
// }
//
// return indexOfFirstOccurrence;
// }
//
// public Timestamp getStartTimestampOfFirstOccurrenceOfThisActivity(ActivityObject activityToCheck)
// {
// String activityName = activityToCheck.getActivityName();
// Timestamp timestampOfFirstOccurrence = null;
//
// for (int i = 0; i < this.activityObjectsInTimeline.size() - 1; i++)
// {
// if (this.activityObjectsInTimeline.get(i).getActivityName().equals(activityName))
// {
// timestampOfFirstOccurrence = this.activityObjectsInTimeline.get(i).getStartTimestamp();
// break;
// }
// }
// return timestampOfFirstOccurrence;
// }
//
// public static int getCountTimelinesCreatedUntilNow()
// {
// return countTimelinesCreatedUntilNow;
// }
//
// public ActivityObject getNextActivityAfterActivityAtThisTime(Timestamp timestamp)
// {
// // System.out.println("To find next activity object at :"+timestamp);
//
// ActivityObject ae = activityObjectsInTimeline.get(getIndexOfActivityObjectsAtTime(timestamp) + 1);
// if (ae != null)
// return ae;
// else
// return new ActivityObject(); // returning empty activity object
// }
//
// public ActivityObject getNextActivityAfterActivityAtThisPosition(int index)
// {
// // System.out.println("To find next activity object at :"+timestamp);
// if (index + 1 > activityObjectsInTimeline.size() - 1)
// {
// System.err.println("Error in getNextActivityAfterActivityAtThisPosition: array index out of bounds");
// return new ActivityObject();
// }
//
// ActivityObject ae = activityObjectsInTimeline.get(index + 1);
// if (ae != null)
// return ae;
// else
// return new ActivityObject(); // returning empty activity object
// }
//
// public ActivityObject getActivityObjectAtTime2(Timestamp t)
// {
// ArrayList<ActivityObject> aos = getActivityObjectsBetweenTime(t, t);
//
// if (aos.size() > 1)
// {
// System.err.println("Error in getActivityObjectAtTime(), aos.size()>1");
// return null;
// }
//
// if (aos.size() == 0 || aos == null)
// {
// System.err.println("Error in getActivityObjectAtTime(), aos.size()=" + aos.size());
// return null;
// }
// else
// return aos.get(0);
// }
//
// /**
// * Returns Activity Objects falling between given start time and end time (all activity objects intersecting this
// * starttime-endtime interval is included. note: if you want to find activity object at time t, make start and end
// * timestamp equal upto the resolution (usually seconds) required.
// *
// * @param startTimestampC
// * @param endTimestampC
// * @param verbose
// * @return
// */
// public ArrayList<ActivityObject> getActivityObjectsBetweenTime(Timestamp startTimestampC, Timestamp endTimestampC)
// {
// ArrayList<ActivityObject> activityObjectsIn = new ArrayList<ActivityObject>();
//
// for (int i = 0; i < this.activityObjectsInTimeline.size(); i++)
// {
// /*
// * $$30Seplong intersectionOfActivityObjectAndIntervalInSeconds=
// * activityObjectsInTimeline.get(i).intersectingIntervalInSeconds(startTimestampC, endTimestampC);
// * if(intersectionOfActivityObjectAndIntervalInSeconds >0) {
// * activityObjectsIn.add(activityObjectsInTimeline.get(i)); } $$30Sep
// */
// // 30Sep addition
// if (activityObjectsInTimeline.get(i).doesOverlap(startTimestampC, endTimestampC))
// {
// activityObjectsIn.add(activityObjectsInTimeline.get(i));
// }
// }
//
// // ////// for testing correctness
// if (VerbosityConstants.verbose)
// {
// System.out.print("\t Inside getActivityObjectsBetweenTime: activity objects inside " + startTimestampC
// + " and " + endTimestampC + " are: ");
// for (int i = 0; i < activityObjectsIn.size(); i++)
// {
// System.out.print(">>" + activityObjectsIn.get(i).getActivityName());
// }
//
// System.out.println();
// }
// // /////////////////////////////////
// return activityObjectsIn;
// }
//
// public int getIndexOfActivityObjectsAtTime(Timestamp timestampC)
// {
// int index = -99;
// int count = 0;
// for (int i = 0; i < this.activityObjectsInTimeline.size(); i++)
// {
// // System.out.println(" >> timestamp to check ="+timestampC+" startTimestamp for
// // this="+activityObjectsInTimeline.get(i).getStartTimestamp()+" end time stamp for
// // this="+activityObjectsInTimeline.get(i).getEndTimestamp());
// if (activityObjectsInTimeline.get(i).getStartTimestamp().getTime() <= timestampC.getTime()
// && activityObjectsInTimeline.get(i).getEndTimestamp().getTime() >= timestampC.getTime()) // end
// // point
// // exclusive
// { // NOTICE : DO I NEED TO MAKE IT EXCLUSIVE OF EITHER BEGIN TIME OR END?
// // System.out.println("Qualifies");
// index = i;
// count++;
// break;
// }
// }
//
// if (count > 1)
// {
// System.err.println(
// "Error in getIndexOfActivityObjectsAtTime(): more than one activites identified at a given point of time for a
// user.");
// }
//
// // System.out.println("Intersect: The activity objects inside "+timestampC+ " has "+startTimestamp+" are ");
//
// if (index == -99)
// {
// System.err.println(
// "Error in getIndexOfActivityObjectsAtTime(): No Activity object at this timestamp:" + timestampC);
// new Exception(
// "Error in getIndexOfActivityObjectsAtTime(): No Activity object at this timestamp:" + timestampC);
// }
// return index;
// }
//
// public ActivityObject getActivityObjectAtTime(Timestamp timestampC)
// {
// ActivityObject ao = null;
// int count = 0;
//
// for (int i = 0; i < this.activityObjectsInTimeline.size(); i++)
// {
// // System.out.println(" >> timestamp to check ="+timestampC+" startTimestamp for
// // this="+activityObjectsInTimeline.get(i).getStartTimestamp()+" end time stamp for
// // this="+activityObjectsInTimeline.get(i).getEndTimestamp());
// if (activityObjectsInTimeline.get(i).getStartTimestamp().getTime() <= timestampC.getTime()
// && activityObjectsInTimeline.get(i).getEndTimestamp().getTime() >= timestampC.getTime()) // end
// // point
// // exclusive
// { // NOTICE : DO I NEED TO MAKE IT EXCLUSIVE OF EITHER BEGIN TIME OR END?
// // System.out.println("Qualifies");
// ao = activityObjectsInTimeline.get(i);
// count++;
// break;
// }
// }
//
// if (count > 1)
// {
// System.err.println(
// "Error in getIndexOfActivityObjectsAtTime(): more than one activites identified at a given point of time for a
// user.");
// }
//
// // System.out.println("Intersect: The activity objects inside "+timestampC+ " has "+startTimestamp+" are ");
//
// if (ao == null)
// {
// System.err.println(
// "Error in getIndexOfActivityObjectsAtTime(): No Activity object at this timestamp:" + timestampC);
// }
// return ao;
// }
//
// /**
// *
// * @param startTimestampC
// * @return
// */
// public ArrayList<ActivityObject> getActivityObjectsStartingOnBeforeTime(Timestamp startTimestampC)
// {
// ArrayList<ActivityObject> activityObjectsIn = new ArrayList<ActivityObject>();
//
// for (int i = 0; i < this.activityObjectsInTimeline.size(); i++)
// {
// if (activityObjectsInTimeline.get(i).startsOnOrBefore(startTimestampC))
// {
// activityObjectsIn.add(activityObjectsInTimeline.get(i));
// }
// }
//
// /*
// * System.out.println("Intersect: The activity objects inside "+startTimestampC+ " and "+endTimestampC+" are ");
// * for(int i=0;i<activityObjectsIn.size();i++) { System.out.print(activityObjectsIn.get(i).getActivityName()); }
// */
// return activityObjectsIn;
// }
//
// /**
// * n takes values from 0 ...
// *
// * @param n
// * @return
// */
// public ActivityObject getActivityObjectAtPosition(int n)
// {
// return this.activityObjectsInTimeline.get(n);
// }
//
// /**
// * Checks whether all the ActivityObjects in the Timeline are of the same day or not.
// *
// * @return
// */
// public boolean isSameDay()// ArrayList<ActivityObject> activityObjectsInDay) // checks whether all activity objects
// // in the timeline are of the same day or not
// {
// boolean sane = true;
//
// if (this.activityObjectsInTimeline.size() > 0)
// {
// Date date = (Date) activityObjectsInTimeline.get(0).getDimensionAttributeValue("Date_Dimension", "Date");
//
// for (int i = 1; i < activityObjectsInTimeline.size(); i++)
// {
// if (date.equals((Date) activityObjectsInTimeline.get(i).getDimensionAttributeValue("Date_Dimension",
// "Date")) == false)
// {
// sane = false;
// }
// }
// }
//
// if (!sane)
// {
// System.err.println(
// "Timeline contains ActivityObjects from more than one day"); /* with Date_ID:"+ dateID+" */
// // System.exit(3);
// }
// return sane;
// }
//
// public LongSummaryStatistics getDurationStats()
// {
// // int min = -9999;
// LongSummaryStatistics summary = null;
//
// ArrayList<ActivityObject> allAOs = new ArrayList<ActivityObject>();
// allAOs = this.getActivityObjectsInTimeline();
//
// summary = allAOs.stream().collect(Collectors.summarizingLong(ao -> ao.getDurationInSeconds()));// .map(ao ->
// // ao.getDurationInSeconds()).
//
// return summary;
//
// }
// }
