package org.activity.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activity.constants.Constant;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ComparatorUtils
{

	/**
	 * Used for sanity checks and validations
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean assertEquals(Object a, Object b)
	{
		if (a == b)
		{
			return true;
		}

		else
		{
			String stringA, stringB;
			if (a == null)
				stringA = "null";
			else
				stringA = a.toString();

			if (b == null)
				stringB = "null";
			else
				stringB = b.toString();

			Exception e = new Exception(
					"Assertion failed - Error in assertEquals ( " + stringA + " != " + stringB + " )");
			PopUps.showException(e, "assertEquals");
			// PopUps.showError("Error: Assertion failed: \n" + ExceptionUtils.getStackTrace(e));
			System.err.println("Error: Assertion failed: \n" + ExceptionUtils.getStackTrace(e));
			return false;
		}
	}

	/**
	 * Used for sanity checks and validations
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean assertNotNull(Object a)
	{
		if (a == null)
		{
			Exception e = new Exception("Assertion failed - Error in assertNotNull: " + a.toString() + " is NULL");
			PopUps.showException(e, "assertNotNull");
			// PopUps.showError("Error: Assertion failed: \n" + ExceptionUtils.getStackTrace(e));
			System.err.println("Error: Assertion failed: \n" + ExceptionUtils.getStackTrace(e));
			return false;
		}
		else
			return true;
	}

	/**
	 * Sorts a map in decreasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc(Map<K, V> map)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());

		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}

		Collections.sort(list, new Comparator<Map.Entry<K, V>>()
			{
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				{
					return (o2.getValue()).compareTo(o1.getValue());
				}
			});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sorts a map in decreasing order of value
	 * 
	 * 
	 * @param map
	 * @param isStable
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean isStable)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());

		if (!isStable)
		{
			Collections.shuffle(list);
		}
		// PopUps.showMessage("just before sorting");
		Collections.sort(list, new Comparator<Map.Entry<K, V>>()
			{
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				{
					return (o2.getValue()).compareTo(o1.getValue());
				}
			});
		// PopUps.showMessage("just after sorting");

		Map<K, V> result = new LinkedHashMap<>();
		int count = 0;
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
			// if (count++ % 100 == 0)
			// {
			// PopUps.showMessage("converted for " + count + " of " + list.size() + " elements");
			// }
		}

		PopUps.showMessage("just after creating converting back list to map");
		return result;
	}

	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAscending(Map<K, V> map)
	{
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<K, V>>()
			{
				@Override
				public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
				{
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, Pair<Integer, Double>> sortByValueAscending2(
			LinkedHashMap<String, Pair<Integer, Double>> map)
	{
		List<Map.Entry<String, Pair<Integer, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<String, Pair<Integer, Double>>>()
			{
				@Override
				public int compare(Map.Entry<String, Pair<Integer, Double>> o1,
						Map.Entry<String, Pair<Integer, Double>> o2)
				{
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

		LinkedHashMap<String, Pair<Integer, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<String, Pair<Integer, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<String,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Integer, Pair<String, Double>> sortByValueAscendingIntStrDoub(
			LinkedHashMap<Integer, Pair<String, Double>> map)
	{
		List<Map.Entry<Integer, Pair<String, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Integer, Pair<String, Double>>>()
			{
				@Override
				public int compare(Map.Entry<Integer, Pair<String, Double>> o1,
						Map.Entry<Integer, Pair<String, Double>> o2)
				{
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

		LinkedHashMap<Integer, Pair<String, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Integer, Pair<String, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<String,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<String, Pair<String, Double>> sortByValueAscendingStrStrDoub(
			LinkedHashMap<String, Pair<String, Double>> map)
	{
		List<Map.Entry<String, Pair<String, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<String, Pair<String, Double>>>()
			{
				@Override
				public int compare(Map.Entry<String, Pair<String, Double>> o1,
						Map.Entry<String, Pair<String, Double>> o2)
				{
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

		LinkedHashMap<String, Pair<String, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<String, Pair<String, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Integer, Pair<Integer, Double>> sortByValueAscending3(
			LinkedHashMap<Integer, Pair<Integer, Double>> map)
	{
		List<Map.Entry<Integer, Pair<Integer, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Integer, Pair<Integer, Double>>>()
			{
				@Override
				public int compare(Map.Entry<Integer, Pair<Integer, Double>> o1,
						Map.Entry<Integer, Pair<Integer, Double>> o2)
				{
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

		LinkedHashMap<Integer, Pair<Integer, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Integer, Pair<Integer, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Date, Pair<Integer, Double>> sortByValueAscending4(
			LinkedHashMap<Date, Pair<Integer, Double>> map)
	{
		List<Map.Entry<Date, Pair<Integer, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Date, Pair<Integer, Double>>>()
			{
				@Override
				public int compare(Map.Entry<Date, Pair<Integer, Double>> o1, Map.Entry<Date, Pair<Integer, Double>> o2)
				{
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

		LinkedHashMap<Date, Pair<Integer, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Date, Pair<Integer, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * @todo to make it for generic Triples sorted by the third value /** Sorts a map in increasing order of value
	 * 
	 *       It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 *       note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component
	 *       (Double)
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> sortByValueAscending5(
			LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> map)
	{
		List<Map.Entry<Date, Triple<Integer, ActivityObject, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Date, Triple<Integer, ActivityObject, Double>>>()
			{
				@Override
				public int compare(Map.Entry<Date, Triple<Integer, ActivityObject, Double>> o1,
						Map.Entry<Date, Triple<Integer, ActivityObject, Double>> o2)
				{
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

		LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Date, Triple<Integer, ActivityObject, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sorts a map in increasing order of value
	 * 
	 * It is an unstable sort (forced by shuffle) to randomly break ties
	 * 
	 * note: In case the Value 'V' is a Pair<Integer,Double>, the comparison is done on the second component (Double)
	 * 
	 * @param map
	 * @return
	 */
	public static LinkedHashMap<Date, Triple<Integer, String, Double>> sortTripleByThirdValueAscending6(
			LinkedHashMap<Date, Triple<Integer, String, Double>> map)
	{
		List<Map.Entry<Date, Triple<Integer, String, Double>>> list = new LinkedList<>(map.entrySet());
		if (Constant.breakTiesWithShuffle)
		{
			Collections.shuffle(list);
		}
		Collections.sort(list, new Comparator<Map.Entry<Date, Triple<Integer, String, Double>>>()
			{
				@Override
				public int compare(Map.Entry<Date, Triple<Integer, String, Double>> o1,
						Map.Entry<Date, Triple<Integer, String, Double>> o2)
				{
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

		LinkedHashMap<Date, Triple<Integer, String, Double>> result = new LinkedHashMap<>();
		for (Map.Entry<Date, Triple<Integer, String, Double>> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * 
	 * @param timestampString
	 *            in ISO 8601 format
	 * @return
	 */
	// public static Instant getInstantFromISOString(String timestampString)// , String timeString)
	// {
	// Instant instant = null;
	// try
	// {
	// instant = Instant.parse(timestampString);
	// }
	// catch (Exception e)
	// {
	// System.out.println("Exception " + e + " thrown for getting timestamp from " + timestampString);
	// e.printStackTrace();
	// }
	// return instant;
	// }

	public static Timestamp getEarliestTimestamp(ArrayList<ActivityObject> activityEvents)
	{
		Timestamp earliestTimestamp = new Timestamp(9999 - 1900, 0, 0, 0, 0, 0, 0);

		for (ActivityObject activityEvent : activityEvents)
		{
			if (activityEvent.getStartTimestamp().before(earliestTimestamp))
				earliestTimestamp = activityEvent.getStartTimestamp();
		}

		System.out.println("Earliest timestamp for this array of activity events is" + earliestTimestamp);
		return earliestTimestamp;
	}

	public static Timestamp getLastTimestamp(ArrayList<ActivityObject> activityEvents)
	{
		Timestamp lastTimestamp = new Timestamp(0 - 1900, 0, 0, 0, 0, 0, 0);

		for (ActivityObject activityEvent : activityEvents)
		{
			if (activityEvent.getEndTimestamp().after(lastTimestamp)) lastTimestamp = activityEvent.getEndTimestamp();
		}

		System.out.println("last timestamp for this array of activity events is" + lastTimestamp);
		return lastTimestamp;
	}

	public static Timestamp getEarliestOfAllTimestamp(HashMap<String, ArrayList<ActivityObject>> timelinesToAggregate)
	{

		Iterator timelinesIterator = timelinesToAggregate.entrySet().iterator();
		Map.Entry timelineEntry1 = (Map.Entry) timelinesIterator.next();

		ArrayList<ActivityObject> activityEvents1 = (ArrayList<ActivityObject>) timelineEntry1.getValue();
		Timestamp earliestOfAll = getEarliestTimestamp(activityEvents1);

		while (timelinesIterator.hasNext())
		{
			Map.Entry timelineEntry = (Map.Entry) timelinesIterator.next();
			ArrayList<ActivityObject> activityEvents = (ArrayList<ActivityObject>) timelineEntry.getValue();
			Timestamp currentEarliest = getEarliestTimestamp(activityEvents);

			if (currentEarliest.before(earliestOfAll)) earliestOfAll = currentEarliest;

		}

		System.out.println("Earliest of all is:" + earliestOfAll);
		return earliestOfAll;
	}

	public static Timestamp getLastOfAllTimestamp(HashMap<String, ArrayList<ActivityObject>> timelinesToAggregate)
	{
		Iterator timelinesIterator = timelinesToAggregate.entrySet().iterator();
		Map.Entry timelineEntry1 = (Map.Entry) timelinesIterator.next();

		ArrayList<ActivityObject> activityEvents1 = (ArrayList<ActivityObject>) timelineEntry1.getValue();
		Timestamp lastOfAll = getLastTimestamp(activityEvents1);
		// Iterator timelinesIterator = timelinesToAggregate.entrySet().iterator();
		while (timelinesIterator.hasNext())
		{
			Map.Entry timelineEntry = (Map.Entry) timelinesIterator.next();
			ArrayList<ActivityObject> activityEvents = (ArrayList<ActivityObject>) timelineEntry.getValue();
			Timestamp currentLast = getEarliestTimestamp(activityEvents);

			if (currentLast.after(lastOfAll)) lastOfAll = currentLast;

		}

		System.out.println("Last of all is:" + lastOfAll);
		return lastOfAll;
	}

	public static boolean areAllStringsInListSame(ArrayList<String> listToCheck)
	{

		boolean same = true;

		if (listToCheck.size() < 2)
		{
			System.err.println("Error in areAllStringInListSame(): less than 2 elements in list");
			return true;
		}

		String first = listToCheck.get(0);

		for (int i = 1; i < listToCheck.size(); i++)
		{
			if (listToCheck.get(i).equals(first) == false)
			{
				same = false;
				break;
			}
		}
		return same;
	}

	/**
	 * To compare user IDs
	 * 
	 * @return
	 * @throws RuntimeException
	 */
	public static Comparator<String> getUserIDComparator() throws RuntimeException
	{
		return new Comparator<String>()
			{
				public int compare(String s1, String s2)
				{// both string must contains user...not doing User because of ignoring case, we can user
					// StringUtils.containsIgnoreCase() alternatively but that might affect
					// performance
					String s1C = s1, s2C = s2;
					if (s1.contains("ser") == true && s2.contains("ser") == true)
					{
						s1C = s1C.replaceAll("[^0-9]", "");
						// PopUps.showMessage("xxxx---"+s1c+" "+s2C);
						s2C = s2C.replaceAll("[^0-9]", "");
						// PopUps.showMessage("xxxx---" + s1C + " " + s2C);

						return Integer.compare(Integer.valueOf(s1C), Integer.valueOf(s2C));
					}
					else
					{
						throw new RuntimeException(
								"Error in getUserIDComparator.compare(): the strings to compare do not contain 'ser' ");
					}
				}
			};
	}

	/**
	 * source: http://codereview.stackexchange.com/questions/37201/finding-all-indices-of-largest-value-in-an-array
	 * 
	 * @param numbers
	 * @return
	 */
	public static int[] findLargeNumberIndices(double[] numbers)
	{
		// create an array of at least 8 members.
		// We may need to make this bigger during processing in case
		// there's more than 8 values with the same large value
		int[] indices = new int[Math.max(numbers.length / 16, 8)];
		// how many large values do we have?
		int count = 0;
		// what is the largest value we have?
		double largeNumber = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < numbers.length; i++)
		{
			if (numbers[i] > largeNumber)
			{
				// we have a new large number value... reset our history....
				largeNumber = numbers[i];
				// setting count to zero is enough to 'clear' our previous references.
				count = 0;
				// we know there's space for at least index 0. No need to check.
				// note how we post-increment - this is a 'pattern'.
				indices[count++] = i;
			}
			else if (numbers[i] == largeNumber)
			{
				// we have another large value.
				if (count == indices.length)
				{
					// need to make more space for indices... increase array by 25%
					// count >>> 2 is the same as count / 4 ....
					indices = Arrays.copyOf(indices, count + (count >>> 2));
				}
				// again, use the post-increment
				indices[count++] = i;
			}
		}
		// return the number of values that are valid only.
		return Arrays.copyOf(indices, count);
	}

	/**
	 * Find duplicates in the given list. Source:
	 * https://stackoverflow.com/questions/7414667/identify-duplicates-in-a-list
	 * 
	 * @param list
	 * @return
	 */
	@SuppressWarnings("unused")
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

	/////////////////
	// TODO this method needs refactoring (30 Sep changes: intersectingIntervalInSeconds replaced by doesOverlap
	// 21Oct public static String getActivityNameForInterval(Timestamp earliestTimestamp, Timestamp lastTimestamp, int
	// intervalIndex, int timeUnitInSeconds,
	// ArrayList<ActivityObject>
	// activityEvents)
	// {
	// //$$30Sep
	// System.err.println("ERROR: This method needs refactoring");
	// PopUps.showMessage("ERROR: This method needs refactoring");
	// //
	//
	//
	// String activityNameToAssign= "not found";
	//
	// Timestamp startInterval = getIncrementedTimestamp(earliestTimestamp,(intervalIndex * timeUnitInSeconds));
	// Timestamp endInterval = getIncrementedTimestamp(earliestTimestamp,((intervalIndex+1) * timeUnitInSeconds));
	// if(endInterval.getTime()> lastTimestamp.getTime())
	// endInterval=lastTimestamp;
	//
	// //$$System.out.print("startinterval:"+startInterval+"endinterval:"+endInterval);
	//
	// for(ActivityObject activityEvent: activityEvents)
	// {
	// if(activityEvent.fullyContainsInterval(startInterval,endInterval)) // the interval falls inside only one activity
	// event
	// {
	// activityNameToAssign = activityEvent.getActivityName();
	// //$$System.out.print("**contains**");
	// }
	// }
	//
	// if(activityNameToAssign.equals("not found")) // the interval falls inside multiple activity events
	// {
	// long longestDuration=0; //in seconds
	// for(ActivityObject activityEvent: activityEvents)
	// {
	// if(activityEvent.intersectingIntervalInSeconds(startInterval,endInterval)> longestDuration) // the interval falls
	// inside only one activity event
	// {
	// longestDuration = activityEvent.intersectingIntervalInSeconds(startInterval,endInterval);
	// activityNameToAssign = activityEvent.getActivityName();
	// }
	// }
	// if(longestDuration>0)
	// {
	//
	// //$$System.out.print("**intersects**");
	// }
	// }
	//
	// if(activityNameToAssign.equals("not found"))
	// {
	// if(startInterval.getMinutes()==59 && endInterval.getHours() ==0 && endInterval.getMinutes()==0)
	// {
	// // all is well because this is the last minute of the day
	// //according to our current data. we have left an interval of a minute before the next day starts
	// }
	// else
	// {
	// System.out.println("Error inside getActivityNameForInterval(): No activity name found for given timeinterval
	// "+startInterval+":"+endInterval+" assigning 'Others'");
	// //System.exit(0);
	//
	// }
	// activityNameToAssign="Others";
	// }
	//
	// //System.out.print(activityNameToAssign+"-"+"\n");
	// return activityNameToAssign;
	// }
	//

	/*
	 * /** Fetches the current timeline from the given longer timeline from the recommendation point back until the
	 * matching unit length.
	 * 
	 * @param longerTimeline the timelines (test timeline) from which the current timeline is to be extracted
	 * 
	 * @param dateAtRecomm
	 * 
	 * @param timeAtRecomm
	 * 
	 * @param userIDAtRecomm
	 * 
	 * @param matchingUnitInHours
	 * 
	 * @return
	 */
	/*
	 * public static TimelineWithNext getCurrentTimelineFromLongerTimeline(Timeline longerTimeline,Date dateAtRecomm,
	 * Time timeAtRecomm, String userIDAtRecomm, int matchingUnitInHours) {
	 * System.out.println("Inside getCurrentTimelineFromLongerTimeline");
	 * 
	 * Timestamp currentEndTimestamp = new
	 * Timestamp(dateAtRecomm.getYear(),dateAtRecomm.getMonth(),dateAtRecomm.getDate(),
	 * timeAtRecomm.getHours(),timeAtRecomm.getMinutes(), timeAtRecomm.getSeconds(),0); long
	 * currentEndTime=currentEndTimestamp.getTime(); Timestamp currentStartTimestamp = new Timestamp(currentEndTime-
	 * (matchingUnitInHours*60*60*1000));
	 * 
	 * System.out.println("Starttime of current timeline="+currentStartTimestamp);
	 * System.out.println("Endtime of current timeline="+currentEndTimestamp);
	 * 
	 * 
	 * //identify the recommendation point in longer timeline
	 * 
	 * ArrayList<ActivityObject>
	 * activityEventsInCurrentTimeline=longerTimeline.getActivityEventsBetweenTime(currentStartTimestamp,
	 * currentEndTimestamp);
	 * 
	 * ActivityObject nextValidActivityEvent=
	 * longerTimeline.getNextValidActivityAfterActivityAtThisTime(currentEndTimestamp); ActivityObject nextActivityEvent
	 * = longerTimeline.getNextActivityAfterActivityAtThisTime(currentEndTimestamp);
	 * 
	 * int isInvalid=-99; if(nextActivityEvent.isInvalidActivityName()) { isInvalid=1; } else isInvalid=-1;
	 * 
	 * TimelineWithNext currentTimeline= new TimelineWithNext(activityEventsInCurrentTimeline,nextValidActivityEvent);
	 * currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);
	 * 
	 * return currentTimeline; }
	 */

}
