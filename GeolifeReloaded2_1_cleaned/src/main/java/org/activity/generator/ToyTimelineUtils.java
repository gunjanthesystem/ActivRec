package org.activity.generator;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.io.Serializer;
import org.activity.objects.ActivityObject;
import org.activity.objects.LocationGowalla;
import org.activity.objects.Timeline;
import org.activity.sanityChecks.Sanity;
import org.activity.stats.StatsUtils;
import org.activity.util.DateTimeUtils;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * 
 * @author gunjan
 *
 */
public class ToyTimelineUtils
{

	// tt
	/**
	 * To create toy timelines
	 * 
	 * Activity events ---> day timelines (later, not here)---> user timelines
	 * <p>
	 * <font color = red>make sure that the timezone is set appropriately</font>
	 * </p>
	 * 
	 * @param allActivityEvents
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 * @since May 14 2018
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createToyUserTimelinesFromCheckinEntriesGowallaFaster1_V2(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDaytimelinesGiven)
	{
		long ct1 = System.currentTimeMillis();
		// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDaytimelinesUnmodView = (LinkedHashMap<String,
		// LinkedHashMap<Date, Timeline>>) Collections
		// .unmodifiableMap(userDaytimelinesGiven);
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDaytimelinesUnmodView = (userDaytimelinesGiven);
		int numOfUsers = 5, minNumOfDaysPerUser = 5, maxNumOfDaysPerUser = 7, numOfUniqueActs = 5,
				minNumOfUniqueActIDsPerDay = 3;

		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> toyTimelines = new LinkedHashMap<>();

		List<String> selectedUsers = userDaytimelinesUnmodView.keySet().stream().limit(numOfUsers)
				.collect(Collectors.toList());

		for (Entry<String, LinkedHashMap<Date, Timeline>> uEntry : userDaytimelinesUnmodView.entrySet())
		{
			if (selectedUsers.contains(uEntry.getKey()))
			{
				LinkedHashMap<Date, Timeline> dayTimelinesForThisUser = uEntry.getValue();
				// LinkedHashMap<Date, Timeline> dayTimelinesWithMinUniqueActIDs = new LinkedHashMap<>();

				Map<Date, Timeline> dayTimelinesWithMinUniqueActIDs = dayTimelinesForThisUser.entrySet().stream()
						.filter(dE -> TimelineUtils.getUniqueActIDsInTimeline(dE.getValue())
								.size() >= minNumOfUniqueActIDsPerDay)
						.collect(Collectors.toMap(e -> (Date) e.getKey(), e -> (Timeline) e.getValue(), (e1, e2) -> e1,
								LinkedHashMap::new));

				// for (Entry<Date, Timeline> e : dayTimelinesForThisUser.entrySet())
				// {
				// Timeline t = e.getValue();
				// if(e.getValue())
				//
				// }
				// LinkedHashMap<Date, Timeline> selectedToyDayTimelinesForThisUser
				// =dayTimelinesWithMinUniqueActIDs.collect(Collectors.toM)
				// int numOfDayForThisUser = StatsUtils.randomInRange(minNumOfDaysPerUser, maxNumOfDaysPerUser);
				//
				// LinkedHashMap<Date, Timeline> selectedToyDayTimelinesForThisUser = new LinkedHashMap<>();
				// // select days with atleast 3 unique actID and not more than 10 acts in the day;
				// for (Entry<Date, Timeline> dayTimelineEntry : dayTimelinesForThisUser.entrySet())
				// {
				// Timeline dayTimeline = dayTimelineEntry.getValue();
				// Set<Integer> uniqueActIDsInDayTimeline = dayTimeline.getActivityObjectsInTimeline().stream()
				// .map(ao -> ao.getActivityID()).collect(Collectors.toSet());
				//
				// if (uniqueActIDsInDayTimeline.size() >= 3)
				// {
				// selectedToyDayTimelinesForThisUser.put(dayTimelineEntry.getKey(), dayTimelineEntry.getValue());
				// // if(selectedToyDayTimelinesForThisUser.size()==)
				// }
				//
				// }
				toyTimelines.put(uEntry.getKey(), (LinkedHashMap<Date, Timeline>) dayTimelinesWithMinUniqueActIDs);
			}
		}

		// find the frequency count of each act for each user.

		return toyTimelines;
	}
	// tt

	// mm
	/**
	 * To create toy timelines
	 * 
	 * Activity events ---> day timelines (later, not here)---> user timelines
	 * <p>
	 * <font color = red>make sure that the timezone is set appropriately</font>
	 * </p>
	 * 
	 * @param uniqueUserIDs
	 * @param uniqueActivityIDs
	 * @param uniqueLocationIDsPerActID
	 * @param uniqueLocIDs
	 * @param pathToGowallaPreProcessedData
	 * @return all users day timelines as LinkedHashMap<User id, LinkedHashMap<Date of timeline, UserDayTimeline>>
	 * @since May 24 2018
	 */
	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> createToyUserTimelinesManuallyGowallaFaster1_V2(
			Set<String> uniqueUserIDs, Set<Integer> uniqueActivityIDs,
			TreeMap<Integer, TreeSet<Integer>> uniqueLocationIDsPerActID, Set<Integer> uniqueLocIDs,
			String pathToGowallaPreProcessedData)
	{
		long ct1 = System.currentTimeMillis();

		Int2ObjectOpenHashMap<LocationGowalla> mapForAllLocationData = UtilityBelt
				.toFasterIntObjectOpenHashMap((LinkedHashMap<Integer, LocationGowalla>) Serializer
						.kryoDeSerializeThis(pathToGowallaPreProcessedData + "mapForAllLocationData.kryo"));

		int numOfUsers = 5, numOfUniqueActs = 5;// minNumOfDaysPerUser = 5, maxNumOfDaysPerUser = 7,
		// minNumOfUniqueActIDsPerDay = 3, maxNumOfUniqueActIDsPerDay = 5;

		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> toyTimelines = new LinkedHashMap<>(numOfUsers);

		// Selected userIDs
		List<String> selectedUserIDs = uniqueUserIDs.stream().limit(numOfUsers).collect(Collectors.toList());
		int[] userIndices = new int[] { 0, 1, 2, 3, 4 };// IntStream.rangeClosed(0, numOfUsers).toArray();//1,2,3,4,5
		Sanity.eq(numOfUsers, userIndices.length,
				"numOfUsers= " + numOfUsers + " != userIndices.length= " + userIndices.length);

		// Selected unique ActIDs over the full toy timelines
		// convert to arraylist to allow shuffling
		ArrayList<Integer> uniqueActIDsList = new ArrayList<>(uniqueActivityIDs);
		Collections.shuffle(uniqueActIDsList);// randmise the order
		List<Integer> uniqueActIDsSelected = uniqueActIDsList.stream().limit(numOfUniqueActs)
				.collect(Collectors.toList());

		// Random r = new Random();
		// r.nextInt(numOfUniqueActs);

		// Distribution for number of acts per day
		int[] numOfActsPerDay = new int[] { 3, 4, 5, 8 };
		double[] pmdForNumOfActsPerDay = new double[] { 0.2, 0.4, 0.25, 0.15 };// probability mass distribution
		EnumeratedIntegerDistribution distriForNumOfActsPerDay = new EnumeratedIntegerDistribution(numOfActsPerDay,
				pmdForNumOfActsPerDay);

		int[] numOfDaysForUserIndices = new int[] { 5, 3, 5, 6, 8 };

		long minStartTimestampInSecs = 1328119200;// Wednesday, February 1, 2012 6:00:00 PM
		long maxStartTimestampInSecs = 1359741600;// Friday, February 1, 2013 6:00:00 PM

		int[] timeGapsInMins = new int[] { 10, 20, 60 * 1, 60 * 2, 60 * 3 };
		double[] pmdForTimeGapsInMins = new double[] { 0.10, 0.20, 0.20, 0.25, 0.25 };
		EnumeratedIntegerDistribution distriForTimeGapsInMins = new EnumeratedIntegerDistribution(timeGapsInMins,
				pmdForTimeGapsInMins);

		int[] distGapsInKMs = new int[] { 50, 5, 1, 10, 25 };
		double[] pmdForDistGapsInKMs = new double[] { 0.10, 0.20, 0.20, 0.25, 0.25 };
		EnumeratedIntegerDistribution distriForDistGapsInKMs = new EnumeratedIntegerDistribution(distGapsInKMs,
				pmdForDistGapsInKMs);

		for (int u = 0; u < userIndices.length; u++)
		{
			String userID = selectedUserIDs.get(u);
			int numOfDays = numOfDaysForUserIndices[u];
			// get a random start date from min to +10 days
			long startTSForThisUser = minStartTimestampInSecs + StatsUtils.randomInRange(0, 10 * 24 * 60 * 60);

			LinkedHashMap<Date, Timeline> toyDayTimelinesForThisUser = new LinkedHashMap<>();

			long startTSForThisUserThisDay = startTSForThisUser;

			for (int d = 0; d < numOfDays; d++)
			{
				// Date dateForThisUserThisDay = DateTimeUtils.getDate(new Timestamp(startTSForThisUserThisDay));
				// long maxAllowableTSForThisDay = dateForThisUserThisDay.getTime();
				int numOfActsInThisDay = distriForNumOfActsPerDay.sample();

				Timeline timelineForThisDay = new Timeline();

				Date dateForThisUserThisDay = new Date(startTSForThisUserThisDay);

				long startTSForCurrentAO = startTSForThisUserThisDay;
				int randomActIDForCurrentAO = StatsUtils.randomInRangeWithBias(0, numOfUniqueActs - 1, 0, .35);

				// get location for this actID;
				List<Integer> locIDsForCurrentActID = new ArrayList<>(
						uniqueLocationIDsPerActID.get(randomActIDForCurrentAO));
				int randomLocIndex = StatsUtils.randomInRange(0, locIDsForCurrentActID.size() - 1);
				int randomLocIDForCurrentAO = locIDsForCurrentActID.get(randomLocIndex);
				long prevTS = -99;

				for (int a = 0; a < numOfActsInThisDay; a++)
				{
					// choose next actID randomly
					int randomActIDForNextAO = StatsUtils.randomInRangeWithBias(0, numOfUniqueActs - 1, 0, .35);

					// get location for next actID;
					List<Integer> locIDsForNextActID = new ArrayList<>(
							uniqueLocationIDsPerActID.get(randomActIDForNextAO));
					int randomLocIDForNextAO = locIDsForNextActID
							.get(StatsUtils.randomInRange(0, locIDsForNextActID.size() - 1));

					long startTSForNextAO = startTSForCurrentAO + distriForTimeGapsInMins.sample();

					String locationName = "", startLatitude = "", startLongitude = "", startAltitude = "";
					double distanceInMFromPrev = distriForDistGapsInKMs.sample();
					long durationInSecFromPrev = prevTS < 0 ? 0 : (startTSForCurrentAO - prevTS);

					double distanceInMFromNext = 0;
					long durationInSecFromNext = 0;
					ZoneId currentZoneId = ZoneId.of("UTC");// added on April 8 2018

					ArrayList<Integer> locIDs = new ArrayList<Integer>(randomLocIDForCurrentAO);
					int numOfLocIDs = locIDs.size();
					int photos_count = 0, checkins_count = 0, users_count = 0, radius_meters = 0, highlights_count = 0,
							items_count = 0, max_items_count = 0;

					// we need to compute the average of these atributes in case there are more than one place id
					// for a (merged) checkin entry
					for (Integer locationID : locIDs)
					{
						// LocationGowalla loc = locationObjects.get((locationID));
						LocationGowalla loc = mapForAllLocationData.get((int) locationID);
						if (loc == null)
						{
							System.err.println(
									"Error in createToyUserTimelinesManuallyGowallaFaster1_V2: No LocationGowalla object found for locationID="
											+ String.valueOf(locationID));
						}
						photos_count += loc.getPhotos_count();
						checkins_count += loc.getCheckins_count();
						users_count += loc.getUsers_count();
						radius_meters += loc.getRadius_meters();
						highlights_count = loc.getHighlights_count();
						items_count += loc.getItems_count();
						max_items_count += loc.getMax_items_count();
					}

					if (numOfLocIDs > 1)
					{
						// numOfCInsWithMultipleLocIDs += 1;
						photos_count = photos_count / numOfLocIDs;
						checkins_count = checkins_count / numOfLocIDs;
						users_count = users_count / numOfLocIDs;
						radius_meters = radius_meters / numOfLocIDs;
						highlights_count = highlights_count / numOfLocIDs;
						items_count = items_count / numOfLocIDs;
						max_items_count = max_items_count / numOfLocIDs;
					}

					ActivityObject ao = new ActivityObject(randomActIDForCurrentAO, locIDs,
							String.valueOf(randomActIDForCurrentAO), locationName, new Timestamp(startTSForCurrentAO),
							startLatitude, startLongitude, startAltitude, userID, photos_count, checkins_count,
							users_count, radius_meters, highlights_count, items_count, max_items_count,
							String.valueOf(randomActIDForCurrentAO), distanceInMFromPrev, durationInSecFromPrev,
							currentZoneId, distanceInMFromNext, durationInSecFromNext);

					timelineForThisDay.appendAO(ao);

					prevTS = startTSForCurrentAO;
					startTSForCurrentAO = startTSForNextAO;
					randomActIDForCurrentAO = randomActIDForNextAO;
					randomLocIDForCurrentAO = randomLocIDForNextAO;

					startTSForThisUserThisDay = startTSForNextAO;

					// if new startTS goes beyond the current day, stop addind AOs for this day and move to next day.
					if (DateTimeUtils.isSameDate(new Timestamp(startTSForNextAO),
							new Timestamp(startTSForThisUserThisDay)) == false)
					{
						break;
					}

				} // end of loop over AOs in the day

				toyDayTimelinesForThisUser.put(dateForThisUserThisDay, timelineForThisDay);
			} // end of loop over days

			toyTimelines.put(userID, toyDayTimelinesForThisUser);
		} // end of loop over users

		// toyTimelines.put(uEntry.getKey(), (LinkedHashMap<Date, Timeline>) dayTimelinesWithMinUniqueActIDs);
		// find the frequency count of each act for each user.

		return toyTimelines;
	}

}
