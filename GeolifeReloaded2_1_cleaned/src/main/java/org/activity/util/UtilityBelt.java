package org.activity.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.generator.GenerateSyntheticData;
import org.activity.io.ReadingFromFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.CheckinEntry;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TrajectoryEntry;
import org.activity.ui.PopUps;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;

/**
 * 
 * @author gunjan
 *
 */
public class UtilityBelt
{

	/**
	 * Enforcing non-instantiability
	 */
	private UtilityBelt()
	{
		throw new AssertionError();
	}

	/**
	 * Reads values from the second column of the specified file
	 * 
	 * @param filePath
	 * @return
	 */
	public static double[] getTimeSeriesVals(String filePath)
	{
		List<Double> vals = ReadingFromFile.oneColumnReaderDouble(filePath, ",", 1, false);
		Double[] vals2 = vals.toArray(new Double[vals.size()]);
		double[] vals3 = ArrayUtils.toPrimitive(vals2);
		return vals3;
	}

	/**
	 * Converts a double to string avoiding the scientific(exponential) notation while still preserving the original
	 * mumber of digits
	 * 
	 * @param ldouble
	 * @return
	 */
	public static String toPlainStringSafely(double ldouble)
	{
		if (Double.isNaN(ldouble))
		{
			return "NaN";
		}
		return ((new BigDecimal(Double.toString(ldouble))).toPlainString());
	}

	// public static LinkedHashMap<String, Timeline> dayTimelinesToRearrangedTimelines(LinkedHashMap<String,
	// LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	// {
	// LinkedHashMap<String, Timeline> userTimelines = new LinkedHashMap<String, Timeline>();
	// for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
	// {
	// String userID = usersTimelinesEntry.getKey();
	// LinkedHashMap<Date, UserDayTimeline> userDayTimelines = usersTimelinesEntry.getValue();
	//
	// userDayTimelines = UtilityBelt.cleanUserDayTimelines(userDayTimelines);
	//
	// Timeline timelineForUser = new Timeline(userDayTimelines); // converts the day time to continuous dayless
	// timeline
	// // timelineForUser = UtilityBelt.expungeInvalids(timelineForUser); // expunges invalid activity objects
	//
	// userTimelines.put(userID, timelineForUser);
	// }
	// System.out.println("\t" + userTimelines.size() + " timelines created");
	// userTimelines = UtilityBelt.rearrangeTimelinesOrderForDataset(userTimelines);
	// System.out.println("\t" + userTimelines.size() + " timelines created");
	// return userTimelines;
	// }

	// public static LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>
	// dayTimelinesToRearrangedDayTimelines(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>
	// usersTimelines)
	// {
	// LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> userTimelines = new LinkedHashMap<String,
	// LinkedHashMap<Date, UserDayTimeline>>();
	//
	// for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
	// {
	// String userID = usersTimelinesEntry.getKey();
	// LinkedHashMap<Date, UserDayTimeline> userDayTimelines = usersTimelinesEntry.getValue();
	//
	// userDayTimelines = UtilityBelt.cleanUserDayTimelines(userDayTimelines);
	//
	// Timeline timelineForUser = new Timeline(userDayTimelines); // converts the day time to continuous dayless
	// timeline
	// // timelineForUser = UtilityBelt.expungeInvalids(timelineForUser); // expunges invalid activity objects
	//
	// userTimelines.put(userID, timelineForUser);
	// }
	// System.out.println("\t" + userTimelines.size() + " timelines created");
	// userTimelines = UtilityBelt.rearrangeTimelinesOrderForDataset(userTimelines);
	// System.out.println("\t" + userTimelines.size() + " timelines created");
	// return userTimelines;
	// }

	public static ArrayList<String> getListOfUsers(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersTimelines)
	{
		return (ArrayList<String>) usersTimelines.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());

		// ArrayList<String> users = new ArrayList<String>();
		// for (Map.Entry<String, LinkedHashMap<Date, Timeline>> usersTimelinesEntry : usersTimelines.entrySet())
		// {
		// users.add(usersTimelinesEntry.getKey());
		// }
		// return users;

	}

	// public static long getCountOfActivityObjects(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>>
	// usersTimelines)
	// {
	// long count = 0;
	//
	// for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
	// {
	// for (Map.Entry<Date, UserDayTimeline> entry : usersTimelinesEntry.getValue().entrySet())
	// {
	// if (entry.getValue().containsAtLeastOneValidActivity() == false)
	// {
	// datesToRemove.add(entry.getKey());
	// if (Constant.verboseTimelineCleaning)
	// System.out.print("," + entry.getKey());
	// }
	// }
	//
	// }
	//
	// return count;
	//
	// }

	/**
	 * 
	 * @param array
	 * @return
	 */
	public static double[] toPrimitive(ArrayList<Double> array)
	{
		if (array == null)
		{
			return null;
		}
		else if (array.size() == 0)
		{
			return new double[0];
		}

		final double[] result = new double[array.size()];

		for (int i = 0; i < array.size(); i++)
		{
			result[i] = array.get(i).doubleValue();
		}
		return result;
	}

	/**
	 * 
	 * @param toCheck
	 * @return
	 */
	public static boolean isValidActivityName(String toCheck)
	{
		if (Constant.getDatabaseName().equals("gowalla1")) // gowalla has no invalid activity names
		{// to speed up
			return true;
		}
		if ((toCheck.trim().equalsIgnoreCase(Constant.INVALID_ACTIVITY1))
				|| (toCheck.trim().equalsIgnoreCase(Constant.INVALID_ACTIVITY2)))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * 
	 * 
	 * @param treeMap
	 * @return ArrayList of strings of the form 'timestampInMilliSeconds||ImageName||ActivityName' or more generally
	 *         'timestampInMilliSeconds||String1`the old separator inherited`String2'
	 */
	public static ArrayList<String> treeMapToArrayListString(TreeMap<Timestamp, String> treeMap)
	{
		ArrayList<String> arrayList = new ArrayList<String>();

		for (Map.Entry<Timestamp, String> entry : treeMap.entrySet())
		{
			String timestampString = Long.toString(entry.getKey().getTime());
			String imageNameActivityName = entry.getValue();

			arrayList.add(timestampString + "||" + imageNameActivityName);
		}

		if (treeMap.size() == arrayList.size())
		{
			System.out.println("TreeMap converted to arraylist succesfully");
		}
		return arrayList;
	}

	public static ArrayList<TrajectoryEntry> treeMapToArrayListGeo(TreeMap<Timestamp, TrajectoryEntry> treeMap)
	{
		ArrayList<TrajectoryEntry> arrayList = new ArrayList<TrajectoryEntry>();

		for (Map.Entry<Timestamp, TrajectoryEntry> entry : treeMap.entrySet())
		{
			arrayList.add(entry.getValue());
		}

		// if (treeMap.size() == arrayList.size())
		// {
		// System.out.println("TreeMap converted to arraylist succesfully");
		// }

		if (treeMap.size() != arrayList.size())
		{
			String msg = "Error in TreeMap conversion to arraylist";
			PopUps.showException(new Exception(msg),
					"org.activity.util.UtilityBelt.treeMapToArrayListGeo(TreeMap<Timestamp, TrajectoryEntry>)");
			// PopUps.showError(msg);
		}
		return arrayList;
	}

	public static ArrayList<CheckinEntry> treeMapToArrayListCheckinEntry(TreeMap<Timestamp, CheckinEntry> treeMap)
	{
		ArrayList<CheckinEntry> arrayList = new ArrayList<CheckinEntry>();

		for (Map.Entry<Timestamp, CheckinEntry> entry : treeMap.entrySet())
		{
			arrayList.add(entry.getValue());
		}

		// if (treeMap.size() == arrayList.size())
		// {
		// System.out.println("TreeMap converted to arraylist succesfully");
		// }

		if (treeMap.size() != arrayList.size())
		{
			String msg = "Error in TreeMap conversion to arraylist";
			PopUps.showException(new Exception(msg),
					"org.activity.util.UtilityBelt.treeMapToArrayListCheckinEntry(TreeMap<Timestamp, CheckinEntry>)");
			// PopUps.showError(msg);
		}
		return arrayList;
	}

	public static ArrayList<?> treeMapToArrayList(TreeMap<Timestamp, ?> treeMap)
	{
		ArrayList arrayList = new ArrayList();

		for (Map.Entry<Timestamp, ?> entry : treeMap.entrySet())
		{
			arrayList.add(entry.getValue());
		}

		// if (treeMap.size() == arrayList.size())
		// {
		// System.out.println("TreeMap converted to arraylist succesfully");
		// }

		if (treeMap.size() != arrayList.size())
		{
			String msg = "Error in TreeMap conversion to arraylist";
			PopUps.showException(new Exception(msg),
					"org.activity.util.UtilityBelt.treeMapToArrayListGeo(TreeMap<Timestamp, TrajectoryEntry>)");
			// PopUps.showError(msg);
		}
		return arrayList;
	}

	/**
	 * removes invalids altitudes, i.e, those which are below 0 and not just -777
	 * 
	 * in
	 * 
	 * @param vals
	 * @return
	 */
	public static ArrayList<String> removeInvalidAlts(ArrayList<String> vals)
	{
		ArrayList<String> res = new ArrayList<String>();

		for (String val : vals)
		{
			if (Double.parseDouble(val) >= 0)// != -777)
			{
				res.add(val);
			}
			else
			{
			}
		}

		return res;
	}

	public static int getCountOfLevel1Ops(String editOpsTrace)
	{
		int countOfL1Ops = 0;

		countOfL1Ops += StringUtils.countSubstring("_I(", editOpsTrace);
		countOfL1Ops += StringUtils.countSubstring("_D(", editOpsTrace);
		countOfL1Ops += StringUtils.countSubstring("_Sao(", editOpsTrace);

		return countOfL1Ops;
	}

	public static int getCountOfLevelInsertions(String editOpsTrace)
	{
		int countOfL1Ops = 0;

		countOfL1Ops += StringUtils.countSubstring("_I(", editOpsTrace);

		return countOfL1Ops;
	}

	// public static int getCountOfLevelDeletions(String editOpsTrace)
	// {
	// int countOfL1Ops=0;
	//
	//
	// countOfL1Ops += countSubstring("_D(",editOpsTrace);
	//
	//
	// return countOfL1Ops;
	// }
	//
	// public static int getCountOfLevelDeletions(String editOpsTrace)
	// {
	// int countOfL1Ops=0;
	//
	//
	// countOfL1Ops += countSubstring("_D(",editOpsTrace);
	//
	//
	// return countOfL1Ops;
	// }
	//
	//

	public static int getCountOfLevel2Ops(String editOpsTrace)
	{
		int countOfL2Ops = 0;

		countOfL2Ops += StringUtils.countSubstring("_Sst(", editOpsTrace);
		countOfL2Ops += StringUtils.countSubstring("_Sd(", editOpsTrace);
		countOfL2Ops += StringUtils.countSubstring("_Sstd(", editOpsTrace);

		return countOfL2Ops;
	}

	// public static TreeMap<Integer, Long> sortByValue2(TreeMap<Integer, Long> map, boolean isStable)
	// {
	// List<Map.Entry<Integer, Long>> list = new LinkedList<>(map.entrySet());
	//
	// if (!isStable)
	// {
	// Collections.shuffle(list);
	// }
	// // PopUps.showMessage("just before sorting");
	// Collections.sort(list, new Comparator<Map.Entry<Integer, Long>>()
	// {
	// @Override
	// public int compare(Map.Entry<Integer, Long> o1, Map.Entry<Integer, Long> o2)
	// {
	// return (o2.getValue()).compareTo(o1.getValue());
	// }
	// });
	// // PopUps.showMessage("just after sorting");
	//
	// Map<Integer, Long> result = new LinkedHashMap<>();
	// int count = 0;
	// for (Map.Entry<K, V> entry : list)
	// {
	// result.put(entry.getKey(), entry.getValue());
	// // if (count++ % 100 == 0)
	// // {
	// // PopUps.showMessage("converted for " + count + " of " + list.size() + " elements");
	// // }
	// }
	//
	// PopUps.showMessage("just after creating converting back list to map");
	// return result;
	// }

	// public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueJava8(Map<K, V> map)
	// {
	// Comparator<Entry<K, V>> byValue = (entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue());
	//
	// Optional<Entry<K, V>> val = map.entrySet().stream().sorted(byValue.reversed()).findFirst();
	// if (val.isPresent())
	// {
	// Map<K, V> res = val;
	// return res;
	// }
	// else
	// return null;
	// // return val;
	// }

	/*
	 * static Map sortByValueAscending(Map map)
	 * //http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java { List list = new
	 * LinkedList(map.entrySet()); Collections.sort(list, new Comparator() { public int compare(Object o1, Object o2) {
	 * return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue()); } });
	 * 
	 * Map result = new LinkedHashMap(); for (Iterator it = list.iterator(); it.hasNext();) { Map.Entry entry =
	 * (Map.Entry)it.next(); result.put(entry.getKey(), entry.getValue()); } return result; }
	 */

	// ////

	public static String getDimensionNameFromDimenionIDName(String dimensionID)
	{
		return (dimensionID.split("_"))[0] + "_Dimension";
	}

	// replace attribute name with Integer code
	public static String reduceJSONString(String selectAttributeString, String jsonArrayForDataTableString)
	{

		System.out.println("inside reduce json with selec" + selectAttributeString + " json of size ="
				+ jsonArrayForDataTableString.length());
		String jsonS = jsonArrayForDataTableString;

		HashMap<String, Integer> attEnumerated = new HashMap<String, Integer>(); // < User_ID, 1>

		int counter = 1;
		for (String att : selectAttributeString.split(","))
		{
			System.out.println("att=" + att);
			String splittedAttName[] = att.split("\\."); // for user_dimension_table.User_ID
			System.out.println(att.split("\\."));
			System.out.println(att.split("\\.").length);

			System.out.println(splittedAttName.length);
			System.out.println(splittedAttName[0]);
			attEnumerated.put(splittedAttName[1], new Integer(counter));
			counter++;
		}

		for (Map.Entry<String, Integer> entry : attEnumerated.entrySet())
		{
			jsonS.replace(entry.getKey(), entry.getValue().toString());
			// System.out.println("Key : " + entry.getKey() + " Value : "
			// + entry.getValue());
		}

		System.out.println("reduce json for " + selectAttributeString + " is " + jsonS);
		return jsonS;

	}

	public static int getLengthInIntervalsOfSplittedTimelines(HashMap<String, ArrayList<String>> timeLines)
	{
		Iterator it = timeLines.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pairs = (Map.Entry) it.next();
			ArrayList<String> activityNames = (ArrayList<String>) pairs.getValue();

			return activityNames.size();
		}
		return 0;
	}

	/*
	 * $$21Oct public static HashMap<String, ArrayList<String>> getSplittedTimelines(HashMap<String,
	 * ArrayList<ActivityObject>> timelinesToAggregate, int timeUnitInSeconds) { HashMap<String, ArrayList<String>>
	 * splittedTimelinesForComparison = new HashMap<String,ArrayList<String>> (); /* Key: Identifier for timeline (can
	 * be UserID in some case) Value: An ArrayList of Activity Names (which is an ordered array of time intervals) start
	 * time of an activity is = (earliestTime of all the activities in timelines)+ (indexInArrrayList) * timeunit end
	 * time of an activity is = (earliestTime of all the activities in timelines)+ (indexInArrrayList+1) * timeunit
	 */

	/*
	 * $$21Oct Iterator timelinesIterator = timelinesToAggregate.entrySet().iterator();
	 * 
	 * Timestamp earliestTimestamp=getEarliestOfAllTimestamp(timelinesToAggregate); Timestamp
	 * lastTimestamp=getLastOfAllTimestamp(timelinesToAggregate);
	 * 
	 * while (timelinesIterator.hasNext()) { Map.Entry timelineEntry = (Map.Entry)timelinesIterator.next();
	 * 
	 * String id= timelineEntry.getKey().toString(); ArrayList<ActivityObject> activityEvents=
	 * (ArrayList<ActivityObject>) timelineEntry.getValue();
	 * 
	 * // Timestamp earliestTimestamp = getEarliestTimestamp(activityEvents); // Timestamp lastTimestamp =
	 * getLastTimestamp(activityEvents);
	 * 
	 * if( (lastTimestamp.getTime() - earliestTimestamp.getTime()) <0)
	 * System.err.println("Error (in getSplittedTimelines()): Error for length of timelines");
	 * 
	 * long lengthOfTimelineInSeconds= (lastTimestamp.getTime() - earliestTimestamp.getTime())/1000; // this should
	 * leave no remainder as our data collection is at not lower than seconds level long numberOfIndices=
	 * (lengthOfTimelineInSeconds / timeUnitInSeconds);
	 * System.out.println("Length of timeline in seconds ="+lengthOfTimelineInSeconds+
	 * "; number of whole indices="+numberOfIndices);
	 * 
	 * ArrayList<String> activityNamesForSplittedTimeline=new ArrayList<String> ();
	 * 
	 * int index=0; for(;index<=numberOfIndices; )//int secondsIterator=0;secondsIterator<=lengthOfTimelineInSeconds;) {
	 * String activityNameForThisInterval= getActivityNameForInterval(earliestTimestamp,lastTimestamp,
	 * index,timeUnitInSeconds,activityEvents); //System.out.print("@@ adding: "+activityNameForThisInterval+" ");
	 * activityNamesForSplittedTimeline.add(activityNameForThisInterval); //secondsIterator =
	 * secondsIterator+timeUnitInSeconds; index ++; }
	 * 
	 * //if lengthOfTimeline is not perfectly divisible by timeUnitInSeconds. // then the last remainder seconds make up
	 * the last element of arrayList (which is of smaller duration than timeUnitInSeconds; if((lengthOfTimelineInSeconds
	 * % timeUnitInSeconds) !=0) { //NOT SURE/SURE BUT NOT WHY String activityNameForThisInterval=
	 * getActivityNameForInterval(earliestTimestamp,lastTimestamp, index,timeUnitInSeconds,activityEvents); //NOT
	 * SURE/SURE BUT NOT WHY activityNamesForSplittedTimeline.add(activityNameForThisInterval);
	 * System.out.println("has remainder ="+(lengthOfTimelineInSeconds % timeUnitInSeconds)+" seconds"); }
	 * 
	 * System.out.println("Length of ArrayList of splitted timeline is "+activityNamesForSplittedTimeline.size());
	 * 
	 * splittedTimelinesForComparison.put(id, activityNamesForSplittedTimeline);
	 * 
	 * System.out.println(); //timelinesIterator.remove(); // avoids a ConcurrentModificationException }
	 * 
	 * return splittedTimelinesForComparison; }
	 */// 21Oct

	public static ArrayList<String> getActivityNamesAtIndex(int intervalIndex,
			HashMap<String, ArrayList<String>> splittedTimelinesForComparison)
	{
		ArrayList<String> activityNamesForComparison = new ArrayList<String>();
		Iterator it = splittedTimelinesForComparison.entrySet().iterator();
		int count = 0; // we will aggregate only two timelines at a time.

		while (it.hasNext()) // one iteration for one timeline
		{
			Map.Entry pairs = (Map.Entry) it.next();

			ArrayList<String> activityNames = (ArrayList<String>) pairs.getValue();
			activityNamesForComparison.add(activityNames.get(intervalIndex));
			count++;
		} // all activity names for comparison collected

		return activityNamesForComparison;
	}

	/**
	 * Checks if the Activity names from the given timelines at the next intervalIndex are different
	 * 
	 * @param intervalIndex
	 * @param splittedTimelinesForComparison
	 * @return true if the activity names are different, false if all the activity names are same
	 */
	public static boolean nextIntervalActivityNamesAreDifferent(int intervalIndex,
			HashMap<String, ArrayList<String>> splittedTimelinesForComparison)
	{
		boolean different = true;

		ArrayList<String> activityNamesForComparison = new ArrayList<String>();

		Iterator it = splittedTimelinesForComparison.entrySet().iterator();
		int count = 0; // we will aggregate only two timelines at a time.

		while (it.hasNext())// Iterating over timelines, each iteration corresponds to a single timeline && count <2)
		{
			Map.Entry pairs = (Map.Entry) it.next();

			ArrayList<String> activityNames = (ArrayList<String>) pairs.getValue();
			activityNamesForComparison.add(activityNames.get(intervalIndex + 1)); // add the activityName at that index
																					// for the current timeline
			count++;
		} // all activity names for comparison collected

		boolean same = true; // all activity names at that interval index for all the splittedtimelines are same

		String firstName = activityNamesForComparison.get(0);
		for (int i = 1; i < activityNamesForComparison.size(); i++)
		{
			if (activityNamesForComparison.get(i).equals(firstName) == false)
			{
				same = false;
				break;
			}
		}
		different = !same;

		return different;
		// only for two timelines
		// return !(activityNamesForComparison.get(0).equals(activityNamesForComparison.get(1)));
	}

	/**
	 * Checks if the Activity names from the given timelines at the next intervalIndex are different and and also belong
	 * to different categories
	 * 
	 * @param intervalIndex
	 * @param splittedTimelinesForComparison
	 * @return true if the activity names and categories are different, false if all the activity names and categories
	 *         are same
	 */
	public static boolean nextIntervalActivityNamesAreTotallyDifferent(int intervalIndex,
			HashMap<String, ArrayList<String>> splittedTimelinesForComparison)
	{
		boolean different = true;

		ArrayList<String> activityNamesForComparison = new ArrayList<String>();

		ArrayList<String> activityCategoriesForComparison = new ArrayList<String>();

		Iterator it = splittedTimelinesForComparison.entrySet().iterator();
		int count = 0; // we will aggregate only two timelines at a time.

		while (it.hasNext())// Iterating over timelines, each iteration corresponds to a single timeline && count <2)
		{
			Map.Entry pairs = (Map.Entry) it.next();

			ArrayList<String> activityNames = (ArrayList<String>) pairs.getValue();
			activityNamesForComparison.add(activityNames.get(intervalIndex + 1)); // add the activityName at that index
																					// for the current timeline
			activityCategoriesForComparison
					.add(GenerateSyntheticData.getActivityCategory(activityNames.get(intervalIndex + 1))); // add the
																											// activityName
																											// at that
																											// index for
																											// the
																											// current
																											// timeline
			count++;
		} // all activity names for comparison collected

		boolean sameName = ComparatorUtils.areAllStringsInListSame(activityNamesForComparison);// all activity names at
																								// that interval
		// index for all the splittedtimelines
		// are same

		boolean sameCategory = true; // all activity names at that interval index for all the splittedtimelines are of
										// same category
		if (sameName == false) // if all the activity names at next intervalIndex are not same, we check if they are of
								// same category or not
		{
			sameCategory = ComparatorUtils.areAllStringsInListSame(activityCategoriesForComparison);
		}
		/*
		 * true; // all activity names at that interval index for all the splittedtimelines are same
		 * 
		 * String firstName=activityNamesForComparison.get(0); for(int i=1;i<activityNamesForComparison.size();i++) {
		 * if(activityNamesForComparison.get(i).equals(firstName) == false) { sameName =false; break; } }
		 */

		/*
		 * boolean sameCategory = true; // all activity names at that interval index for all the splittedtimelines are
		 * of same category if(sameName == false ) // if all the activity names at next intervalIndex are not same, we
		 * check if they are of same category or not { String firstCategory=activityCategoriesForComparison.get(0);
		 * 
		 * for(int i=1;i<activityCategoriesForComparison.size();i++) {
		 * if(activityCategoriesForComparison.get(i).equals(firstCategory) == false) { sameCategory =false; break; } } }
		 */

		different = !(sameName && sameCategory);

		// Checking if it works (only for aggregating two timelines
		/*
		 * if(!sameName && sameCategory) {
		 * System.out.println("Check: not same name but same category- "+activityNamesForComparison.get(0)+" and "
		 * +activityNamesForComparison.get(1)); }
		 */

		return different;
		// only for two timelines
		// return !(activityNamesForComparison.get(0).equals(activityNamesForComparison.get(1)));
	}

	/**
	 * If there is only one non other activity in the arraylist, then that non other activity name is returned
	 * 
	 * @param listToCheck
	 * @return the single non-other activity name
	 */
	public static String ifOnlyOneNonOtherInList(ArrayList<String> listToCheck)
	{

		// boolean onlyOneNonOther = false;
		int countOfNonOthers = 0;
		String theNonOtherName = "null";

		for (int i = 0; i < listToCheck.size(); i++)
		{
			if (listToCheck.get(i).equalsIgnoreCase("Others") == false)
			{
				countOfNonOthers++;
				theNonOtherName = listToCheck.get(i);
			}
		}

		if (countOfNonOthers == 1)
		{
			return theNonOtherName;
		}

		return "null";
	}

	/**
	 * Retunr the number of unique elements in the ArrayList (primarily used to find the number of unique activity names
	 * in the disagreement space)
	 * 
	 * @param arrayList
	 * @return
	 */
	public static int getCountUniqueActivites(ArrayList<String> listToCount)
	{
		ArrayList<String> dummyList = new ArrayList<String>();

		for (int i = 0; i < listToCount.size(); i++)
		{
			if (!(dummyList.contains(listToCount.get(i))))
			{
				dummyList.add(listToCount.get(i));
			}

		}

		return dummyList.size();
	}

	public static ArrayList<String> getActivityCategoriesList(ArrayList<String> activityNames)
	{
		ArrayList<String> activityCategories = new ArrayList<String>();

		for (int i = 0; i < activityNames.size(); i++)
		{
			activityCategories.add(GenerateSyntheticData.getActivityCategory(activityNames.get(i)));
		}

		if (activityCategories.size() != activityNames.size())
		{
			System.err.println("Error: in getting activity category list from activity name list");
		}

		return activityCategories;
	}

	// /*21Oct
	// public static String aggregateTimelines(HashMap<String, ArrayList<ActivityObject>> timeLinesToAggregate,String
	// aggregateByString, String methodForDisagreementSpace)
	// {
	// System.out.println("***********inside aggregate timelines:************"+methodForDisagreementSpace);
	// System.out.println(" number of timelines to agggregate = "+timeLinesToAggregate.size());
	//
	// int thresholdForDisagreementSpace= timeLinesToAggregate.size()+1;
	// System.out.println("Threshold for disagreement space: num of unique activitiy names
	// <="+thresholdForDisagreementSpace);
	//
	// ArrayList<String> aggregatedSplittedTimeline = new ArrayList<String> ();
	//
	//
	// //Step 1
	// int timeUnitInSeconds=getTimeUnitInSeconds();
	//
	// //Step 2: Split the timelines into timeunit sized time intervals.
	// HashMap<String, ArrayList<String>> splittedTimelinesForComparison =getSplittedTimelines(timeLinesToAggregate,
	// timeUnitInSeconds);
	// System.out.println("number of splitted timelines ="+splittedTimelinesForComparison.size());
	// /* Key: Identifier for timeline (can be UserID in some case)
	// Value: An ArrayList of Activity Names (which is an ordered array of time intervals)
	// start time of an activity is = (earliestTime of all the activities in timelines)+ (index) * timeunit
	// end time of an activity is = (earliestTime of all the activities in timelines)+ (index+1) * timeunit
	// */
	// //$$traverseSplittedTimelines(splittedTimelinesForComparison);
	//
	// // all splitted timelines will be of same length.
	// /*21Oct int lengthInIntervalsOfSplittedTimelines=
	// getLengthInIntervalsOfSplittedTimelines(splittedTimelinesForComparison);
	// System.out.println("length of splitted timelines ="+lengthInIntervalsOfSplittedTimelines+" intervals");
	//
	// // Step 3: actually aggregate the timelines
	// ArrayList<String> disagreementSpace= new ArrayList<String> ();
	// int disagreementLengthInIntervals =0;
	// int addCounter=0;
	// for(int intervalIndex=0;intervalIndex<lengthInIntervalsOfSplittedTimelines;)//intervalIndex++)
	// {
	// // disagreementLengthInIntervals =0;
	//
	// //check for case a, b and c
	// ArrayList<String> activityNamesForComparison= new ArrayList<String>();
	//
	// activityNamesForComparison=getActivityNamesAtIndex(intervalIndex, splittedTimelinesForComparison);
	//
	// //System.out.println(activityNamesForComparison.size()+" activity names for comparison");
	// //Case a
	// if(areAllStringsInListSame(activityNamesForComparison))//activityNamesForComparison.get(0).equals(activityNamesForComparison.get(1)))
	// {
	// //$$ENABLESystem.out.println("Case A "+activityNamesForComparison.get(0)+" and
	// "+activityNamesForComparison.get(1));
	// aggregatedSplittedTimeline.add(activityNamesForComparison.get(0));
	// addCounter++;
	// intervalIndex++;
	// disagreementSpace.clear();
	// disagreementLengthInIntervals =0;
	// continue;
	// }
	//
	// //Case b
	//
	// //ifOnlyOneNonOtherInList
	//
	//
	// if(ifOnlyOneNonOtherInList(activityNamesForComparison).equals("null") == false) //something other than "null"
	// returned
	// {
	// //$$ENABLESystem.out.println("Case B "+activityNamesForComparison.get(0)+" and
	// "+activityNamesForComparison.get(1));
	// aggregatedSplittedTimeline.add(ifOnlyOneNonOtherInList(activityNamesForComparison));
	// addCounter++;
	// intervalIndex++;
	// disagreementSpace.clear();
	// disagreementLengthInIntervals =0;
	// continue;
	// }
	//
	//
	// //Case c
	// ArrayList<String> activityCategoriesForComparison= getActivityCategoriesList(activityNamesForComparison);
	//
	//
	// if(areAllStringsInListSame(activityCategoriesForComparison))
	// {
	// //$$ENABLESystem.out.println("Case C "+activityNamesForComparison.get(0)+" and
	// "+activityNamesForComparison.get(1));
	// aggregatedSplittedTimeline.add(activityCategoriesForComparison.get(0));
	// addCounter++;
	// intervalIndex++;
	// disagreementSpace.clear();
	// disagreementLengthInIntervals =0;
	// continue;
	// }
	//
	// //Case d
	// //$$System.out.println("Case D "+activityNamesForComparison.get(0)+" and "+activityNamesForComparison.get(1));
	//
	// disagreementSpace.addAll(activityNamesForComparison);
	// disagreementLengthInIntervals++;
	//
	//
	// //enlarge the disagreement space as long as the activities are differnt
	// while(true)
	// {
	// intervalIndex++;
	// if(( intervalIndex<lengthInIntervalsOfSplittedTimelines)
	// && (nextIntervalActivityNamesAreTotallyDifferent(intervalIndex-1,splittedTimelinesForComparison))
	// // && (getCountUniqueActivites(disagreementSpace) <= thresholdForDisagreementSpace)
	// )
	// {
	//
	// if(getCountUniqueActivites(disagreementSpace) > thresholdForDisagreementSpace) //CHECK suppposedly this leads to
	// inclusion of one extra (time unit) sized interval (check it
	// by running for
	// 1 hr
	// time unit size)
	// {
	// //$$System.out.println("breaking because number of actities in space= "+disagreementSpace.size()+"the count of
	// unique activites in disagreement space
	// is"+getCountUniqueActivites(disagreementSpace));
	//
	// break;
	// }
	// activityNamesForComparison=getActivityNamesAtIndex(intervalIndex, splittedTimelinesForComparison);
	// disagreementSpace.addAll(activityNamesForComparison);
	// disagreementLengthInIntervals++;
	//
	// //$$ENABLE
	// System.out.println("number of actities in space= "+disagreementSpace.size()+"the count of unique activites in
	// disagreement space
	// is"+getCountUniqueActivites(disagreementSpace));
	// /*if(getCountUniqueActivites(disagreementSpace) > thresholdForDisagreementSpace)
	// {
	// System.out.println("breaking");
	// break;
	// }
	// else
	// */ continue;
	//
	// }
	// else
	// {
	// break;
	// }
	//
	// }
	//
	// System.out.println("Number of elements in disagreementSpace ="+disagreementSpace.size());
	// System.out.println("disagreementLengthInIntervals ="+disagreementLengthInIntervals);
	//
	// String activityNameForDisagreementSpace=" ";
	// switch (methodForDisagreementSpace)
	// {
	// case "MaxFrequency":
	// activityNameForDisagreementSpace = getMaximumFrequencyActivityName(disagreementSpace);
	// break;
	// case "ThresholdFrequency":
	// activityNameForDisagreementSpace = getThresholdFrequencyActivityName(disagreementSpace,ThresholdFrequency,true);
	// break;
	// /* case "AdhocTaxonomy":
	// activityNameForDisagreementSpace = getSuperAdhocTaxonomyActivityName(disagreementSpace);
	// break;
	// case "Cluster":
	// activityNameForDisagreementSpace = getSuperClusterActivityName(disagreementSpace);
	// break;*/
	// default:
	// System.err.println("Error for disagreement space: UNRECOGNIZED method for aggregating disagreement space.");
	// break;
	//
	// }
	//
	// System.out.println(" activityNameForDisagreementSpace = "+activityNameForDisagreementSpace);
	//
	//
	// for(int i=0;i<disagreementLengthInIntervals;i++)
	// {
	// aggregatedSplittedTimeline.add(activityNameForDisagreementSpace);
	// addCounter++;
	// }
	// disagreementSpace.clear();
	// disagreementLengthInIntervals =0;
	// // intervalIndex++;
	// }
	//
	//
	// // SOME ISSUE WITH TRaversing timeline to split as splitted timeline
	// System.out.println("First is"+splittedTimelinesForComparison.get("0").size()+" elements");
	// System.out.println("Second is"+splittedTimelinesForComparison.get("1").size()+" elements");
	// System.out.println("Aggregated is"+aggregatedSplittedTimeline.size()+" elements");
	// System.out.println("add counter is"+addCounter);
	//
	// // System.out.println("Second is"+splittedTimelinesForComparison.get(1).size()+" elements");
	//
	//
	// // UNCOMMENT BELOW TO SEE SPLITTED TIMELINES
	// //$$traverseSingleSplittedTimeline("First timeline splitted",splittedTimelinesForComparison.get("0"));
	// //$$traverseSingleSplittedTimeline("Second timeline splitted",splittedTimelinesForComparison.get("1"));
	// //$$traverseSingleSplittedTimeline("Aggregated timeline splitted",aggregatedSplittedTimeline);
	//
	// System.out.println("XXXXXend of aggregate timeline XXXXXXXXXXXXX");
	// Timestamp earliestTimestamp= getEarliestTimestamp(timeLinesToAggregate.get("0"));
	// Timestamp lastTimestamp = getLastTimestamp(timeLinesToAggregate.get("0"));
	// System.out.println("Earliest timestamp="+getEarliestTimestamp(timeLinesToAggregate.get("0")) );
	//
	// String aggregatedTimelineasJsonString = createJSONString(aggregatedSplittedTimeline,earliestTimestamp,
	// lastTimestamp);
	// return aggregatedTimelineasJsonString;
	// }
	// //21Oct

	public static String createJSONString(ArrayList<String> aggregatedSplittedTimeline, Timestamp earliestTimestamp,
			Timestamp lastTimestamp)
	{
		String JSONString = "[";
		int timeUnitInSeconds = DateTimeUtils.getTimeUnitInSeconds();
		int lengthInIntervalsOfSplittedTimelines = aggregatedSplittedTimeline.size();// getLengthInIntervalsOfSplittedTimelines(aggregatedSplittedTimeline);
		// System.out.println("Inside create JSONString from splitter timeline");
		System.out.println("Inside create JSONString from splitter timeline : size of splitted timeline ="
				+ lengthInIntervalsOfSplittedTimelines);

		int counter = 0;
		for (; counter < aggregatedSplittedTimeline.size();)// String activityName: aggregatedSplittedTimeline)
		{
			// Timestamp startTimestamp= earliestTimestamp + counter* timeUnitInSeconds ;
			// Timestamp = earliestTimestamp + (counter+1)* timeUnitInSeconds ;
			String activityName = aggregatedSplittedTimeline.get(counter);
			// if(activityName.equals("Others"))
			// activityName="Ajooba";

			Timestamp startTimestamp = DateTimeUtils.getIncrementedTimestamp(earliestTimestamp,
					(counter * timeUnitInSeconds));
			Timestamp endTimestamp = DateTimeUtils.getIncrementedTimestamp(earliestTimestamp,
					((counter + 1) * timeUnitInSeconds));

			// Merge consecutively similar activities
			while (((counter + 1) < lengthInIntervalsOfSplittedTimelines)
					&& (activityName.equals(aggregatedSplittedTimeline.get(counter + 1)))
					&& DateTimeUtils.isSameDate(startTimestamp, endTimestamp) // startTimestamp.getDate()
			// ==
			// endTimestamp.getDate()
			// // do
			// not merge
			// activities
			// from
			// different
			// days
			// (because
			// it
			// makes
			// sense and
			// also
			// because
			// it causes
			// problem
			// because
			// this
			// merger
			// would
			// mean we
			// will have
			// to allow
			// (startTimestamp.getHour()
			// >
			// endTimestamp.getHour())
			) // matching date to identify same day
			{
				counter++;
				endTimestamp = DateTimeUtils.getIncrementedTimestamp(earliestTimestamp,
						((counter + 1) * timeUnitInSeconds)); // end time stamp of the new merged activity event
				// check if it is split for the last interval of the day
			}

			// check if it is split for the last interval of the day
			if (startTimestamp.getHours() > endTimestamp.getHours() // also WORKS (endTimestamp.getHours()== 0)
			)// && (endTimestamp.getMinutes()*60) < getTimeUnitInSeconds())
			{
				Timestamp newEndTimestamp = new Timestamp(endTimestamp.getYear(), endTimestamp.getMonth(),
						endTimestamp.getDay(), 23, 59, 59, 0);
				endTimestamp = newEndTimestamp;
			}

			// System.out.println("End time stamp: "+endTimestamp);
			if (endTimestamp.getTime() > lastTimestamp.getTime()) endTimestamp = lastTimestamp;

			endTimestamp = new Timestamp(endTimestamp.getTime() - 1); // decrease a millisecond
			// in the above statement we decrease a milisecond from the end timestamp, so there is a gap of one
			// millisecond before the end of activity and start of next activity.
			// Without this gap
			// in
			// time intervals (i.e,
			// if the end of an activity was exactly the same as start of next activity), it was causing rendering delay
			// in the visjs timeline visualisation.

			String JSONObjectString = " {\"Activity Name\":\"" + activityName + "\",";

			JSONObjectString = JSONObjectString + "\"End Time\":\"" + DateTimeUtils.getTimeString(endTimestamp) + "\",";
			JSONObjectString = JSONObjectString + "\"Start Time\":\"" + DateTimeUtils.getTimeString(startTimestamp)
					+ "\",";

			JSONObjectString = JSONObjectString + "\"User ID\":" + 99 + ",";
			JSONObjectString = JSONObjectString + "\"Date\":\"" + DateTimeUtils.getDateString(startTimestamp) + "\" },";

			JSONString = JSONString + JSONObjectString;
			counter++;
		}

		JSONString = JSONString.substring(0, JSONString.length() - 1);

		JSONString = JSONString + "]";

		// System.out.println("JSON String is--------------"+JSONString);

		return JSONString;
	}

	public static HashMap<String, Integer> getActivityNamesCountMap(ArrayList<String> disagreementSpace)
	{
		HashMap<String, Integer> activityNamesCount = new HashMap<String, Integer>();
		// < Activity Name, number of times the activity name appear in the disagreement space>
		for (int i = 0; i < disagreementSpace.size(); i++)
		{
			String currentActivityName = disagreementSpace.get(i);
			if (!(activityNamesCount.containsKey(currentActivityName)))
			{
				activityNamesCount.put(currentActivityName, new Integer(1));
			}
			else
			{
				activityNamesCount.put(currentActivityName,
						new Integer(activityNamesCount.get(currentActivityName).intValue() + 1));
			}

		}
		return activityNamesCount;
	}

	public static String getMaximumFrequencyActivityName(ArrayList<String> disagreementSpace)
	{
		String maximalActivityName = "not";
		String maximalsString = "";// useful in case of more than one maximals
		// ArrayList<String> maximals=new ArrayList<String> ();
		int maximalCount = 0;

		HashMap<String, Integer> activityNamesCount = getActivityNamesCountMap(disagreementSpace);
		// < Activity Name, number of times the activity name appear in the disagreement space>

		for (Map.Entry<String, Integer> entry : activityNamesCount.entrySet())
		{
			if (entry.getValue().intValue() > maximalCount)
			{
				maximalActivityName = entry.getKey();
				maximalCount = entry.getValue();
			}
		}

		if (countOfAboveThresholdFrequencyActivityName(disagreementSpace, maximalCount - 1) > 1)
		{
			maximalActivityName = getThresholdFrequencyActivityName(disagreementSpace, maximalCount - 1, true); // in
																												// case
																												// of
																												// ties
																												// at
																												// maximal
																												// count
		}

		else
			// single maximal
			maximalActivityName = getThresholdFrequencyActivityName(disagreementSpace, maximalCount - 1, false); // in
																													// case
																													// of
																													// ties
																													// at
																													// maximal
																													// count
		// $$ENABLESystem.out.print(" maximal activity name="+maximalActivityName+", with count="+maximalCount);

		return maximalActivityName;
	}

	/**
	 * 
	 * 
	 * @param disagreementSpace
	 * @param thresholdFrequency
	 * @param withCount
	 *            if set to true the activity name above threshold will be catenated with the count of occurrence of
	 *            that activity in the current disagreement space, for example: Computer#100
	 * @return
	 */
	public static String getThresholdFrequencyActivityName(ArrayList<String> disagreementSpace, int thresholdFrequency,
			boolean withCount)
	{
		String aboveThresholdActivityNames = "";
		int count = 0;

		HashMap<String, Integer> activityNamesCount = getActivityNamesCountMap(disagreementSpace);
		// < Activity Name, number of times the activity name appear in the disagreement space>

		for (Map.Entry<String, Integer> entry : activityNamesCount.entrySet())
		{
			if (entry.getValue().intValue() > thresholdFrequency)
			{
				aboveThresholdActivityNames = aboveThresholdActivityNames + "," + entry.getKey();// +"#"+entry.getValue();

				if (withCount)
				{
					aboveThresholdActivityNames = aboveThresholdActivityNames + "#" + entry.getValue();
				}
				count++;
			}
		}

		// $$ENABLESystem.out.print(" Above Threshold("+ thresholdFrequency+") activity
		// names="+aboveThresholdActivityNames);
		if (count == 0)
		{
			System.out.println("No activity name above threshold, hence we use maxFrequency ");
			aboveThresholdActivityNames = getMaximumFrequencyActivityName(disagreementSpace);
		}

		else
			// trim first comma
			aboveThresholdActivityNames = aboveThresholdActivityNames.substring(1);

		return aboveThresholdActivityNames;
	}

	//
	public static int countOfAboveThresholdFrequencyActivityName(ArrayList<String> disagreementSpace,
			int thresholdFrequency)
	{
		// String aboveThresholdActivityNames= "";
		int count = 0;

		HashMap<String, Integer> activityNamesCount = getActivityNamesCountMap(disagreementSpace);
		// < Activity Name, number of times the activity name appear in the disagreement space>

		for (Map.Entry<String, Integer> entry : activityNamesCount.entrySet())
		{
			if (entry.getValue().intValue() > thresholdFrequency)
			{

				count++;
			}
		}

		return count;
	}

	//

	/**
	 * Takes in a JSON Array, extracts sets of Dimension IDs for each element from the Array and create Activity Objects
	 * using those sets of Dimension IDs.
	 * 
	 * @param jsonArray
	 * @return an ArrayList of all activityEvents created from the jsonArray (note: this json array was formed from all
	 *         values satisfying select and where clause of query)
	 */
	public static ArrayList<ActivityObject> createActivityObjectsFromJsonArray(JSONArray jsonArray)
	{
		// HashMap<String,ArrayList<ActivityObject>> timeLines = new HashMap<String,ArrayList<ActivityObject>> ();
		// Key: Identifier for timeline (can be UserID in some case)
		// Value: An ArrayList of ActivityEvents
		ArrayList<ActivityObject> allActivityObjects = new ArrayList<ActivityObject>();

		System.out.println("inside createActivityEventsFromJsonArray: checking parsing json array");
		System.out.println("number of elements in json array = " + jsonArray.length());
		long dt = System.currentTimeMillis();

		try
		{
			for (int i = 0; i < jsonArray.length(); i++)
			{
				if ((i % 1000 == 0))// || (i == (jsonArray.length()-1)))
				{
					System.out.println(" JSON Array index:" + i);// //@toremoveatruntime
				}
				HashMap<String, String> dimensionIDsForActivityEvent = new HashMap<String, String>();// (User_ID, 0),
																										// (Location_ID,
																										// 10100), ...
				// if(i%100==0)
				// System.out.println(i);
				/*
				 * TO DO:Can DO: make it generic, extract primary key names from key table in database instead of
				 * writing explicity
				 */
				dimensionIDsForActivityEvent.put("User_ID", jsonArray.getJSONObject(i).get("User_ID").toString());
				dimensionIDsForActivityEvent.put("Activity_ID",
						jsonArray.getJSONObject(i).get("Activity_ID").toString());
				dimensionIDsForActivityEvent.put("Time_ID", jsonArray.getJSONObject(i).get("Time_ID").toString());
				dimensionIDsForActivityEvent.put("Date_ID", jsonArray.getJSONObject(i).get("Date_ID").toString());
				dimensionIDsForActivityEvent.put("Location_ID",
						jsonArray.getJSONObject(i).get("Location_ID").toString());

				allActivityObjects.add(new ActivityObject(dimensionIDsForActivityEvent));
			} // note: the ActivityEvents obtained from the database are not in chronological order, thus the ArrayList
				// as of now does not contain Activity Events in chronological
				// order. Also its
				// a
				// good assumption for
				// results obtained using any general database.
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		long lt = System.currentTimeMillis();

		System.out.println("exiting createActivityEventsFromJsonArray with " + allActivityObjects.size()
				+ " Activity Objects created in" + (lt - dt) / 1000 + " secs");
		return allActivityObjects;
	}

	/*
	 * public static HashMap<String,ArrayList<UserDayTimeline>> createUserTimelinesFromJsonArray(JSONArray jsonArray) {
	 * HashMap<String, ArrayList<UserDayTimeline>> userTimelines= new HashMap<String, ArrayList<UserDayTimeline>> ();
	 * 
	 * // Key: userid // Value: An ArrayList of day timeline
	 * 
	 * 
	 * HashMap<String, ArrayList<ActivityObject>> perUserActivityEvents= new HashMap<String,
	 * ArrayList<ActivityObject>>();
	 * 
	 * //ArrayList<ActivityObject> allActivityEvents= new ArrayList<ActivityObject> ();
	 * 
	 * System.out.println("inside createUserTimelinesFromJsonArray: checking parsing json array");
	 * System.out.println("number of elements in json array = "+jsonArray.length());
	 * 
	 * try { for(int i=0;i<jsonArray.length();i++) { HashMap<String,String> dimensionIDsForActivityEvent = new
	 * HashMap<String,String> ();
	 * 
	 * /*TO DO:Can DO: make it generic, extract primary key names from key table in database instead of writing
	 * explicity
	 */
	/*
	 * dimensionIDsForActivityEvent.put("User_ID",jsonArray.getJSONObject(i).get("User_ID").toString());
	 * dimensionIDsForActivityEvent.put("Activity_ID",jsonArray.getJSONObject(i).get("Activity_ID").toString());
	 * dimensionIDsForActivityEvent.put("Time_ID",jsonArray.getJSONObject(i).get("Time_ID").toString());
	 * dimensionIDsForActivityEvent.put("Date_ID",jsonArray.getJSONObject(i).get("Date_ID").toString());
	 * dimensionIDsForActivityEvent.put("Location_ID",jsonArray.getJSONObject(i).get("Location_ID").toString());
	 * 
	 * String userId= jsonArray.getJSONObject(i).get("User_ID").toString();
	 * 
	 * ActivityObject activityEventFormed= new ActivityObject(dimensionIDsForActivityEvent);
	 * 
	 * //String dateString=activityEventFormed.geDimensionAttributeValue("Date_Dimension","Date").toString();
	 * 
	 * if(!(userTimelines.containsKey(userId))) // if its a new user , create a new entry in hashmap {
	 * userTimelines.put(userId, new ArrayList<UserDayTimeline> ()); }
	 * 
	 * // get corresponding timeline (ArrayList of ActivityEvents) and add the Activity Event to that ArrayList
	 * ActivityObject activityEvent=new ActivityObject(activityName, startTime, endTime);
	 * timeLines.get(userID).add(activityEvent);
	 * 
	 * 
	 * allActivityEvents.add(new ActivityObject(dimensionIDsForActivityEvent)); } //note: the ActivityEvents obtained
	 * from the database are not in chronological order, thus the ArrayList as of now does not contain Activity Events
	 * in chronological order. Also its a good assumption for results obtained using any general database. }
	 * 
	 * catch(Exception e) { e.printStackTrace(); }
	 * 
	 * return allActivityEvents; }
	 */

	public static void traverseActivityEvents(ArrayList<ActivityObject> activityEvents)
	{
		System.out.println("** Traversing Activity Events **");
		for (int i = 0; i < activityEvents.size(); i++)
		{
			(activityEvents.get(i)).traverseActivityObject();
		}
		System.out.println("** End of Traversing Activity Events **");
	}

	public static int safeLongToInt(long l)
	{
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	public static String getActivityNamesFromArrayList(ArrayList<ActivityObject> activityEvents)
	{
		String res = "";

		for (ActivityObject ae : activityEvents)
		{
			res += "_" + ae.getActivityName();
		}
		return res;
	}

	public static Integer getIntegerByDateFromMap(LinkedHashMap<Date, Integer> map, Date dateA)
	{
		for (Map.Entry<Date, Integer> entry : map.entrySet())
		{
			// System.out.println("Date ="+entry.getKey());
			// if(entry.getKey().toString().equals((new Date(2014-1900,4-1,10)).toString()))
			// System.out.println("!!!!!!!!E U R E K A !!!!!!!");

			if (entry.getKey().toString().equals(dateA.toString()))
			{
				// System.out.println("!!!!!!!FOUND THE O N E!!!!!!");
				return entry.getValue();
			}
		}

		return null;

	}

	/**
	 * 
	 * @param userID
	 * @return index of the user id in the list of user ids
	 */
	public static int getIndexOfUserID(int userID)
	{
		int res = -99;
		int userIDs[] = Constant.userIDs;
		for (int i = 0; i < userIDs.length; i++)
		{
			if (userIDs[i] == userID)
			{
				res = i;
				break;
			}
		}

		if (res < 0)
		{
			System.err.println("Error in UtilityBelt.getIndexOfUserID for user " + userID
					+ ": userID not found in class array userIDs");
			System.err.println("User ids in class array:");
			for (int k : userIDs)
			{
				System.err.print(k + ",");
			}

			new Exception().printStackTrace();
		}
		return res;
	}

	/**
	 * Rearranges the map according to the given order of keys. This is useful when original userIDs are to be
	 * considered in a different order, for example, as with Geolife dataset.
	 * 
	 * @param map
	 * @param orderKeys
	 * @return
	 */
	public static LinkedHashMap<String, Integer> rearrangeByGivenOrder(LinkedHashMap<String, Integer> map,
			int[] orderKeys)
	{
		LinkedHashMap<String, Integer> rearranged = new LinkedHashMap<String, Integer>();

		for (int key : orderKeys)
		{
			String keyString = Integer.toString(key);
			rearranged.put(keyString, map.get(keyString));
		}

		return rearranged;
	}

	/**
	 * changes the userIDs to be from 1 to n
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reformatUserIDs(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> map)
	{
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> rearranged = new LinkedHashMap<>();

		for (Map.Entry<String, LinkedHashMap<Date, Timeline>> entry : map.entrySet())
		{
			int newUserID = UtilityBelt.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1;
			rearranged.put(String.valueOf(newUserID), entry.getValue());
		}
		return rearranged;
	}

	/**
	 * Execute any *nix shell command and returns the output as String
	 * 
	 * @param command
	 * @return
	 */
	public static String executeShellCommand(String command)
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try
		{
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";

			int count = 0;
			while ((line = reader.readLine()) != null)
			{
				// System.out.println("aaa");
				output.append(line + "\n");
				count++;
			}
			System.out.println(count - 1 + " lines");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return output.toString();
	}

	public static String executeShellCommand4(String command)
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try
		{
			System.out.println("starting executeShellCommand4");
			p = Runtime.getRuntime().exec(command);
			System.out.println("before waitFor");
			// p.waitFor();
			System.out.println("after waitFor");
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";

			int count = 0;
			while ((line = reader.readLine()).equals("end of program") == false)
			{
				// System.out.println("aaa");
				output.append(line + "\n");
				count++;
			}
			System.out.println(count - 1 + " lines");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return output.toString();
	}

	public static String executeShellCommand2(String command)
	{
		StringBuffer output = new StringBuffer();
		Process p;
		try
		{
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";

			int count = 0;
			while (true)
			{
				line = reader.readLine();

				System.out.println("aaa");

				if (count >= 10)
				{
					break;
				}
				if (line == null)
				{
					line = "";
				}

				else if (line.equals("end of program"))
				{
					break;
				}

				output.append(line + "\n");
				count++;
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return output.toString();
	}

	public static void showErrorExceptionPopup(String msg)
	{
		System.err.println(msg);
		new Exception(msg);
		PopUps.showError(msg);
	}

	/**
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static Set<Integer> getIntersection(Set<Integer> s1, Set<Integer> s2)
	{
		Set<Integer> intersection = new HashSet<Integer>(s1);
		intersection.retainAll(s2);
		return intersection;
	}
	// public static

	/**
	 * Find duplicates in a Collection
	 * 
	 * @param list
	 * @return
	 */
	public static <T> Set<T> findDuplicates(Collection<T> list)
	{
		Set<T> duplicates = new LinkedHashSet<T>();
		Set<T> uniques = new HashSet<T>();

		for (T t : list)
		{
			if (!uniques.add(t))
			{
				duplicates.add(t);
			}
		}

		return duplicates;
	}
	// public static void writeCatLevelInfo()
	// {
	// TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	//
	// Triple catLevelMaps = getCategoryMapsFromJSON();
	// LinkedHashMap<Integer, String> level1Map = (LinkedHashMap<Integer, String>) catLevelMaps.getFirst();
	// LinkedHashMap<Integer, String> level2Map = (LinkedHashMap<Integer, String>) catLevelMaps.getSecond();
	// LinkedHashMap<Integer, String> level3Map = (LinkedHashMap<Integer, String>) catLevelMaps.getThird();
	//
	// TreeMap<Integer, Long> level1CkeckinCountMap = new TreeMap<Integer, Long>();// )
	// TreeMap<Integer, Long> level2CkeckinCountMap = new TreeMap<Integer, Long>();
	// TreeMap<Integer, Long> level3CkeckinCountMap = new TreeMap<Integer, Long>();
	// TreeMap<Integer, Long> noneLevelCkeckinCountMap = new TreeMap<Integer, Long>();
	//
	// String fileNameToRead =
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnly.csv";
	// String fileNameToWrite =
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyWithLevelsV2_2.csv";
	//
	// String fileNameToWriteCatLevelDistro =
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyCatLevelDistro";
	//
	// int countOfLines = 0;
	// StringBuffer sbuf = new StringBuffer();
	// String lineRead;
	//
	// int l1Count = 0, l2Count = 0, l3Count = 0, notFoundInAnyLevel = 0;
	// ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();
	// // int lengthOfReadTokens = -1;
	// try
	// {
	// BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
	// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileNameToWrite);
	//
	// while ((lineRead = br.readLine()) != null)
	// {
	// countOfLines += 1;
	// int isLevel1 = 0, isLevel2 = 0, isLevel3 = 0;
	// int foundInLevels = 0;
	//
	// String[] splittedLine = lineRead.split(",");
	//
	// if (countOfLines == 1)
	// {
	// sbuf.append(splittedLine[1] + "," + splittedLine[2] + "," + splittedLine[3] + "," + splittedLine[4] + ","
	// + splittedLine[5] + ",IsLevel1,IsLevel2,IsLevel3\n");
	// continue;
	// }
	// // System.out.println("splittedLine[3] =" + splittedLine[3]);
	// // System.out.println("splittedLine[1] =" + splittedLine[1]);
	// Integer catID = Integer.valueOf(splittedLine[3].replaceAll("\"", ""));
	//
	// if (level1Map.containsKey(catID))
	// {
	// isLevel1 = 1;
	// foundInLevels++;
	// l1Count++;
	// level1CkeckinCountMap.put(catID, level1CkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
	// }
	// if (level2Map.containsKey(catID))
	// {
	// isLevel2 = 1;
	// foundInLevels++;
	// l2Count++;
	// level2CkeckinCountMap.put(catID, level2CkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
	// }
	//
	// if (level3Map.containsKey(catID))
	// {
	// isLevel3 = 1;
	// foundInLevels++;
	// l3Count++;
	// level3CkeckinCountMap.put(catID, level3CkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
	// }
	//
	// if (foundInLevels == 0)
	// {
	// catIDsNotFoundInAnyLevel.add(catID);
	// notFoundInAnyLevel++;
	// noneLevelCkeckinCountMap.put(catID, noneLevelCkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
	// }
	//
	// if (foundInLevels > 1 && catID != 201)
	// {
	// System.err.println("Error: catID " + catID + " found in multiple levels " + isLevel1 + "," + isLevel3 + "," +
	// isLevel3);
	// }
	//
	// sbuf.append(splittedLine[1] + "," + splittedLine[2] + "," + splittedLine[3] + "," + splittedLine[4] + "," +
	// splittedLine[5]
	// + "," + isLevel1 + "," + isLevel2 + "," + isLevel3 + "\n");
	//
	// // if (countOfLines % 4000 == 0)
	// // {
	// bw.write(sbuf.toString());
	// sbuf.setLength(0);
	// // }
	// }
	//
	// bw.close();
	// br.close();
	//
	// System.out.println("Num of checkins read: " + (countOfLines - 1));
	// System.out.println("Num of level1 in checkins: " + l1Count);
	// System.out.println("Num of level2 in checkins: " + l2Count);
	// System.out.println("Num of level3 in checkins: " + l3Count);
	// System.out.println("Num of checkins with catID in no levelMap: " + notFoundInAnyLevel);
	//
	// WritingToFile.appendLineToFileAbsolute(StringUtils.join(catIDsNotFoundInAnyLevel.toArray(), ","),
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/CatsInNoMaps.csv");
	//
	// writeCheckInDistributionOverCatIDs(level1CkeckinCountMap, level2CkeckinCountMap, level3CkeckinCountMap,
	// noneLevelCkeckinCountMap, fileNameToWriteCatLevelDistro);
	// // catIDsNotFoundInAnyLevel
	// // bw.write(sbuf.toString());
	// // sbuf.setLength(0);
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// }
	//
	//
	// bw.close();
	// br.close();
	//
	// System.out.println("Num of checkins read: " + (countOfLines - 1));
	// System.out.println("Num of level1 in checkins: " + l1Count);
	// System.out.println("Num of level2 in checkins: " + l2Count);
	// System.out.println("Num of level3 in checkins: " + l3Count);
	// System.out.println("Num of checkins with catID in no levelMap: " + notFoundInAnyLevel);
	//
	// WritingToFile.appendLineToFileAbsolute(StringUtils.join(catIDsNotFoundInAnyLevel.toArray(), ","),
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/CatsInNoMaps.csv");
	//
	// writeCheckInDistributionOverCatIDs(level1CkeckinCountMap, level2CkeckinCountMap, level3CkeckinCountMap,
	// noneLevelCkeckinCountMap, fileNameToWriteCatLevelDistro);
	// // catIDsNotFoundInAnyLevel
	// // bw.write(sbuf.toString());
	// // sbuf.setLength(0);
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// }
	//

	/**
	 * 
	 * @param map
	 * @param name
	 */
	public static void traverseStringStringPair(LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> map,
			String name)
	{
		System.out.println("-----------Traversing " + name);
	
		// iterating over cands
		for (Map.Entry<String, LinkedHashMap<String, Pair<String, Double>>> entry : map.entrySet())
		{
			System.out.print("Cand=" + entry.getKey());
			LinkedHashMap<String, Pair<String, Double>> featureWiseDistances = entry.getValue();
	
			for (Map.Entry<String, Pair<String, Double>> distEntry : featureWiseDistances.entrySet())
			// iterating over distance for each feature
			{
				System.out.print("Feature names=" + distEntry.getKey());
				System.out.print("val =" + distEntry.getValue().getSecond());
			}
			System.out.println();
		}
		System.out.println("-----------");
	}

	/**
	 * 
	 * @param map
	 * @param name
	 */
	public static void traverseStringPair(LinkedHashMap<String, Pair<String, Double>> map, String name)
	{
		System.out.println("-----------Traversing " + name);
		// iterating over distance for each feature
		for (Map.Entry<String, Pair<String, Double>> distEntry : map.entrySet())
		{
			System.out.print("Cand =" + distEntry.getKey());
			System.out.print("val =" + distEntry.getValue().getSecond());
		}
		System.out.println("\n-----------");
	}

}
