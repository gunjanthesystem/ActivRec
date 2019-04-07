package org.activity.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.activity.objects.ActivityObject2018;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.spatial.SpatialUtils;

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

		if (true)// add distance from prev and duration from prev
		{
			newActObjs = addDistanceFromPrevAndDurFromPrev(newActObjs);
		}

		Timeline newTimeline = new Timeline(newActObjs, true, true);

		System.out.println("oldActObjs.size()= " + oldActObjs.size() + " newActObjs.size()" + newActObjs.size()
				+ "\nsame num of aos = " + (oldActObjs.size() == newActObjs.size())
				+ "\nExiting convert2016UserDayTimelineTo2018Timeline():");

		return new Triple<>(newTimeline, actIDNameDict, locIDNameDict);
	}

	/**
	 * @return
	 * @since 8 Dec 2018
	 */
	public static ArrayList<ActivityObject2018> addDistanceFromPrevAndDurFromPrev(ArrayList<ActivityObject2018> givenAO)
	{
		ArrayList<ActivityObject2018> toReturn = new ArrayList<>(givenAO.size());

		double distFromPrev = 0, durationFromPrev = 0; // keep it 0 or -9999
		ActivityObject2018 prevAO = null;

		givenAO.get(0).setDistanceInMFromPrev(0);
		givenAO.get(0).setDurationInSecondsFromPrev(0);
		toReturn.add(givenAO.get(0));
		prevAO = givenAO.get(0);

		for (int i = 1; i < givenAO.size(); i++)
		{
			ActivityObject2018 currentAO = givenAO.get(i);
			double distFromInMFromPrev = (SpatialUtils.haversineFastMathV3NoRound(
					Double.parseDouble(currentAO.getStartLatitude()), Double.parseDouble(currentAO.getStartLongitude()),
					Double.parseDouble(prevAO.getEndLatitude()), Double.parseDouble(prevAO.getEndLongitude())))
					/ 1000.0d;
			long durationInSecondsFromPrev = (long) ((currentAO.getStartTimestampInms()
					- prevAO.getStartTimestampInms()) / 1000d);

			currentAO.setDistanceInMFromPrev(distFromInMFromPrev);
			currentAO.setDurationInSecondsFromPrev(durationInSecondsFromPrev);
			toReturn.add(currentAO);
			prevAO = currentAO;
		}
		return toReturn;
	}
	// public static ActivityObject2018 convert2016ActivityObjectTo2018ActivityObject(ActivityObject old)
	// {
	// ActivityObject2018 newActivityObject = new ActivityObject2018(old.getD);
	//
	// return newActivityObject;
	// }

}
