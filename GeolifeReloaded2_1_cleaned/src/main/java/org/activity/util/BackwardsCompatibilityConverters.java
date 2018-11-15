package org.activity.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.activity.objects.ActivityObject2018;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;

public class BackwardsCompatibilityConverters
{

	public static Triple<Timeline, TreeMap<Integer, String>, LinkedHashMap<Integer, String>> convert2016UserDayTimelineTo2018Timeline(
			UserDayTimeline oldTimeline)
	{
		TreeMap<Integer, String> actIDNameDict = new TreeMap<>();
		LinkedHashMap<Integer, String> locIDNameDict = new LinkedHashMap<>();

		System.out.println("Inside convert2016UserDayTimelineTo2018Timeline():");
		ArrayList<ActivityObject> oldActObjs = oldTimeline.getActivityObjectsInDay();
		ArrayList<ActivityObject2018> newActObjs = new ArrayList<>(oldActObjs.size());

		for (ActivityObject oao : oldActObjs)
		{
			ActivityObject2018 newAO = new ActivityObject2018(oao);
			newActObjs.add(newAO);

			actIDNameDict.put(newAO.getActivityID(), newAO.getActivityName());

			Integer locID = newAO.getLocationIDs().get(0);// since only one location in geolife;//TODO improve later
			locIDNameDict.put(locID, String.valueOf(locID));
		}

		Timeline newTimeline = new Timeline(newActObjs, true, true);

		System.out.println("oldActObjs.size()= " + oldActObjs.size() + " newActObjs.size()" + newActObjs.size());
		System.out.println("same num of aos = " + (oldActObjs.size() == newActObjs.size()));
		System.out.println("Exiting convert2016UserDayTimelineTo2018Timeline():");
		return new Triple<>(newTimeline, actIDNameDict, locIDNameDict);
	}

	// public static ActivityObject2018 convert2016ActivityObjectTo2018ActivityObject(ActivityObject old)
	// {
	// ActivityObject2018 newActivityObject = new ActivityObject2018(old.getD);
	//
	// return newActivityObject;
	// }

}
