package org.activity.distances;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activity.objects.ActivityObject;
import org.activity.objects.Timeline;
import org.activity.objects.UserDayTimeline;
import org.activity.util.Constant;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

public class ExperimentDistances
{
	
	public ExperimentDistances(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines)
	{
		System.out.println("Entering ExperimentDistances");
		
		ArrayList<Timeline> arrayOfTimelines = getArrayListOfTimelines(usersTimelines, 2);
		
		traverseArrayOfTimelines(arrayOfTimelines);
		getSizeOfTimelines(arrayOfTimelines);
		
		ArrayList<ArrayList<ActivityObject>> arrOfArrActObject = getActivityObjectsSubList(arrayOfTimelines.get(0), arrayOfTimelines.get(1), 0, 10);
		
		OTMDSAMEditDistance otDist = new OTMDSAMEditDistance();
		// otDist.getFeatureStringLevenshteinSAXWithTrace(arrOfArrActObject.get(0), arrOfArrActObject.get(1));
		otDist.getFeatureStringLevenshteinSAXWithTrace(getActivityObjectsSubList(arrayOfTimelines.get(0), 0, 5), getActivityObjectsSubList(arrayOfTimelines.get(1), 0, 8));
	}
	
	public static ArrayList<Timeline> getArrayListOfTimelines(LinkedHashMap<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelines, int numOfTimelines)
	{
		ArrayList<Timeline> arrayOfTimelines = new ArrayList<Timeline>();
		int count = 0;
		for (Map.Entry<String, LinkedHashMap<Date, UserDayTimeline>> usersTimelinesEntry : usersTimelines.entrySet())
		{
			if (++count > numOfTimelines)
				break;
			String userID = usersTimelinesEntry.getKey();
			LinkedHashMap<Date, UserDayTimeline> userDayTimelines = usersTimelinesEntry.getValue();
			// System.out.println("\nUser ID: " + userID);
			userDayTimelines = TimelineUtils.cleanUserDayTimelines(userDayTimelines);
			
			Timeline timeline = new Timeline(userDayTimelines);
			
			if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
			{
				timeline = UtilityBelt.expungeInvalids(timeline);
			}
			
			arrayOfTimelines.add(timeline); // converts the day time to continuous dayless timeline
		}
		return arrayOfTimelines;
	}
	
	public static void traverseArrayOfTimelines(ArrayList<Timeline> arr)
	{
		int count = 0;
		for (Timeline t : arr)
		{
			System.out.println(++count);
			t.printActivityObjectNamesWithTimestampsInSequence();
		}
		System.out.println("");
	}
	
	public static void getSizeOfTimelines(ArrayList<Timeline> arr)
	{
		System.out.println("Num of activity-objects in timeline: ");
		arr.stream().map(t -> t.getActivityObjectsInTimeline().size()).forEach(System.out::println);
		System.out.println("min = " + arr.stream().min(Comparator.comparing(t -> t.getActivityObjectsInTimeline().size())).get().getActivityObjectsInTimeline().size());
		System.out.println();
	}
	
	public static ArrayList<ActivityObject> getActivityObjectsSubList(Timeline t1, int startIndex, int endIndex)
	{
		System.out.println("Inside getActivityObjectsSubList");
		ArrayList<ActivityObject> a1 = new ArrayList<ActivityObject>();
		try
		{
			a1 = t1.getActivityObjectsInTimelineFromToIndex(startIndex, endIndex);
			
			a1.stream().map(ao -> ao.getActivityName() + " ").forEach(System.out::print);
			System.out.println();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return a1;
	}
	
	public static ArrayList<ArrayList<ActivityObject>> getActivityObjectsSubList(Timeline t1, Timeline t2, int startIndex, int endIndex)
	{
		System.out.println("Inside getActivityObjectsSubList");
		ArrayList<ArrayList<ActivityObject>> arrArr = new ArrayList<ArrayList<ActivityObject>>();
		try
		{
			ArrayList<ActivityObject> a1 = new ArrayList<>();
			
			ArrayList<ActivityObject> a2 = new ArrayList<>();
			
			a1 = t1.getActivityObjectsInTimelineFromToIndex(startIndex, endIndex);
			a2 = t2.getActivityObjectsInTimelineFromToIndex(startIndex, endIndex);
			
			arrArr.add(a1);
			arrArr.add(a2);
			
			a1.stream().map(ao -> ao.getActivityName() + " ").forEach(System.out::print);
			System.out.println();
			a2.stream().map(ao -> ao.getActivityName() + " ").forEach(System.out::print);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return arrArr;
	}
}
