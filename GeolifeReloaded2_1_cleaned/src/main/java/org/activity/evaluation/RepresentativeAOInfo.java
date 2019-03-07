package org.activity.evaluation;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.recomm.RecommendationMasterI;
import org.activity.sanityChecks.Sanity;
import org.activity.stats.HilbertCurveUtils;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.PerformanceAnalytics;
import org.activity.util.TimelineTransformers;

/**
 * One for each user.
 * 
 * @author gunjan
 * @since 20 Dec 2018
 */
public class RepresentativeAOInfo
{
	Map<Integer, Double> aggValOfEachFeatForEachPDValDurationInSecsFromPrev;
	Map<Integer, Double> aggValOfEachFeatForEachPDValDurationFromNext;
	Map<Integer, ArrayList<String>> aggStartLatLongEndLatLong_;
	Map<Integer, ArrayList<Pair<Long, Long>>> aggStartLatLongEndLatLongAsHSC;
	Map<Integer, Double> aggValOfEachFeatForEachPDValDuration;
	Map<Integer, Double> aggValOfEachFeatForEachPDValDistTrav;
	Map<Integer, Double> aggValOfEachFeatForEachPDValAvgAlt;
	Map<Integer, Double> aggValOfEachFeatForEachPDValDistFromPrev;
	Map<Integer, Integer> aggValOfEachFeatForEachPDValPopularity;
	Map<Integer, Integer> aggValOfEachFeatForEachPDValLocationID;// = new HashMap<>();
	Map<Integer, Integer> aggValOfEachFeatForEachPDValActivityID;// = new HashMap<>();
	String userId;

	@Override
	public String toString()
	{
		return "RepresentativeAOInfo [aggValOfEachFeatForEachPDValDurationInSecsFromPrev="
				+ aggValOfEachFeatForEachPDValDurationInSecsFromPrev + ", aggValOfEachFeatForEachPDValDurationFromNext="
				+ aggValOfEachFeatForEachPDValDurationFromNext + ", aggStartLatLongEndLatLong_="
				+ aggStartLatLongEndLatLong_ + ", aggStartLatLongEndLatLongAsHSC=" + aggStartLatLongEndLatLongAsHSC
				+ ", aggValOfEachFeatForEachPDValDuration=" + aggValOfEachFeatForEachPDValDuration
				+ ", aggValOfEachFeatForEachPDValDistTrav=" + aggValOfEachFeatForEachPDValDistTrav
				+ ", aggValOfEachFeatForEachPDValAvgAlt=" + aggValOfEachFeatForEachPDValAvgAlt
				+ ", aggValOfEachFeatForEachPDValDistFromPrev=" + aggValOfEachFeatForEachPDValDistFromPrev
				+ ", aggValOfEachFeatForEachPDValPopularity=" + aggValOfEachFeatForEachPDValPopularity
				+ ", aggValOfEachFeatForEachPDValLocationID=" + aggValOfEachFeatForEachPDValLocationID
				+ ", aggValOfEachFeatForEachPDValActivityID=" + aggValOfEachFeatForEachPDValActivityID + ", userId="
				+ userId + "]";
	}

	/**
	 * 
	 * @param topPrimaryDimensionVal
	 * @param recommTimestamp
	 * @param string
	 * @param recommMaster
	 * @param primaryDimension
	 * @param databaseName
	 * @return
	 */
	/**
	 * 
	 * @param recommTimestamp
	 * @param topPrimaryDimensionValInt
	 * @param recommMaster
	 * @param primaryDimension
	 * @param databaseName
	 * @return
	 */
	public ActivityObject2018 getRepresentativeAOForActName(Timestamp recommTimestamp,
			Integer topPrimaryDimensionValInt, RecommendationMasterI recommMaster, PrimaryDimension primaryDimension,
			String databaseName)
	{
		// NOTE: we only need to take care of feature which are used for edit distance computation.
		// Instantiate the representative activity object.
		int activityID = -1;
		ArrayList<Integer> locationIDs = new ArrayList<>();
		String activityName = "";
		String workingLevelCatIDs = "";
		// Integer topPrimaryDimensionValInt = Integer.valueOf(topPrimaryDimensionVal);

		switch (primaryDimension)
		{
		case ActivityID:
		{
			activityID = topPrimaryDimensionValInt;
			if (databaseName.equals("gowalla1"))
			{// for gowalla dataset, act id and act name are same
				activityName = String.valueOf(topPrimaryDimensionValInt);
				workingLevelCatIDs = topPrimaryDimensionValInt + "__";
			}
			// Constant.getActivityNames()
			ArrayList<Integer> locID = new ArrayList<>();
			locID.add(aggValOfEachFeatForEachPDValLocationID.get(topPrimaryDimensionValInt));
			break;
		}
		case LocationID:
		{
			locationIDs.add(topPrimaryDimensionValInt);
			workingLevelCatIDs = topPrimaryDimensionValInt + "__";
			// locationName =
			// DomainConstants.getLocIDLocationObjectDictionary().get(pdVal).getLocationName();
			// if (locationName == null || locationName.length() == 0)
			// { PopUps.printTracedErrorMsg("Error: fetched locationName= " + locationName); }
			activityID = aggValOfEachFeatForEachPDValActivityID.get(topPrimaryDimensionValInt);
			break;
		}
		default:
			PopUps.printTracedErrorMsgWithExit("Error: unknown primaryDimension = " + primaryDimension);
			break;
		}

		long newRecommTimestampInMs = recommTimestamp.getTime()
				+ (long) (1000 * aggValOfEachFeatForEachPDValDurationInSecsFromPrev.get(topPrimaryDimensionValInt));

		// ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
		// activityName, "", newRecommTimestamp, "", "", "", String.valueOf(userId), -1, -1, -1, -1, -1, -1, -1,
		// workingLevelCatIDs, -1, -1, new String[] { "" });
		ActivityObject2018 repAOForThisActNameForThisUser2 = null;
		long durationInSecs;
		switch (databaseName)
		{
		case "geolife1":
			ArrayList<String> startLatLonEndLatLon_ = aggStartLatLongEndLatLong_.get(topPrimaryDimensionValInt);
			String mostCommon = StatsUtils.mostCommon(startLatLonEndLatLon_);
			String splitted[] = mostCommon.split("_");
			String startLat = splitted[0];
			String startLon = splitted[1];
			String endLat = splitted[2];
			String endLon = splitted[3];

			durationInSecs = aggValOfEachFeatForEachPDValDuration.get(topPrimaryDimensionValInt).longValue();
			Double avgAlt = aggValOfEachFeatForEachPDValAvgAlt.get(topPrimaryDimensionValInt);
			// durationInSecs = Long
			// .valueOf(aggValOfEachFeatForEachPDValDuration.get(topPrimaryDimensionValInt).toString());
			// ActivityObject2018(String userID1, int activityID1, String activityName1, String workingLevelCatIDs1,
			// ArrayList<Integer> locationIDs1, String locationName1, String startLatitude1, String endLatitude1,
			// String startLongitude1, String endLongitude1, String avgAltitude1, double distanceTravelled1,
			// long startTimestampInms1, long endTimestampInms1, long durationInSeconds1)

			repAOForThisActNameForThisUser2 = new ActivityObject2018(userId, activityID, activityName,
					workingLevelCatIDs, locationIDs, "", startLat, endLat, startLon, endLon, avgAlt.toString(),
					aggValOfEachFeatForEachPDValDistTrav.get(topPrimaryDimensionValInt), (long) newRecommTimestampInMs,
					(long) newRecommTimestampInMs + (durationInSecs * 1000), durationInSecs);
			break;
		case "gowalla1":

			repAOForThisActNameForThisUser2 = new ActivityObject2018(activityID, locationIDs, activityName, "",
					new Timestamp(newRecommTimestampInMs), "", "", "", userId, -1,
					aggValOfEachFeatForEachPDValPopularity.get(topPrimaryDimensionValInt), -1, -1, -1, -1, -1,
					workingLevelCatIDs, aggValOfEachFeatForEachPDValDistFromPrev.get(topPrimaryDimensionValInt),
					aggValOfEachFeatForEachPDValDurationInSecsFromPrev.get(topPrimaryDimensionValInt).longValue(),
					ZoneId.of("UTC"), -1, -1, -1);
			repAOForThisActNameForThisUser2.setUserID(userId);
			break;
		case "dcu_data_2":

			durationInSecs = aggValOfEachFeatForEachPDValDuration.get(topPrimaryDimensionValInt).longValue();
			// durationInSecs = Long
			// .valueOf(aggValOfEachFeatForEachPDValDuration.get(topPrimaryDimensionValInt).toString());
			// repAOForThisActNameForThisUser2= new ActivityObject2018(String.(userId), activityName, activityId,
			// String.valueOf(activityId),
			// startTimestamp.getTime(), endTimestamp.getTime(), duration, durationFromPrevInSecs,
			// ZoneId.of("GMT"));
			repAOForThisActNameForThisUser2 = new ActivityObject2018(userId, activityName, activityID,
					String.valueOf(activityID), (long) newRecommTimestampInMs,
					(long) newRecommTimestampInMs + (durationInSecs * 1000), durationInSecs,
					aggValOfEachFeatForEachPDValDurationInSecsFromPrev.get(topPrimaryDimensionValInt).longValue(),
					ZoneId.of("GMT"));
			break;

		}// (int activityID, ArrayList<Integer> locationIDs, String activityName, String locationName,
			// Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String
			// userID,
			// int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			// int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromPrev,
			// long durationInSecsFromPrev, ZoneId timeZoneId, double distanceInMFromNext, long durationInSecFromNext,
			// int gridID)

		if (!DateTimeUtils.isSameDate(recommTimestamp, new Timestamp(newRecommTimestampInMs)))
		{
			System.out.print("Warning: recommTimestamp = " + recommTimestamp + " newRecommTimestampInMs= "
					+ newRecommTimestampInMs + " are not same day. medianPreceedingDuration = "
					+ aggValOfEachFeatForEachPDValDurationInSecsFromPrev.get(topPrimaryDimensionValInt)
					+ " for topPrimaryDimensionValInt =" + topPrimaryDimensionValInt);
		}
		//
		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug: getRepresentativeAO: old recommTimestamp="
					+ recommTimestamp.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ aggValOfEachFeatForEachPDValDurationInSecsFromPrev.get(topPrimaryDimensionValInt)
					+ " new recommendationTime=" + new Timestamp(newRecommTimestampInMs).toLocalDateTime().toString());
		}
		// System.out.println("repAO=" + repAOForThisActNameForThisUser.toStringAllGowallaTS());

		return repAOForThisActNameForThisUser2;

	}

	/**
	 * 
	 * /**
	 * 
	 * can i create repAO, for each user, for each AO, - computing only once - pre-computing and not just-in-time -
	 * handling both collaborative and non-collaborative cases.
	 * <p>
	 * Formerly called computeRepresentativeAOsDec2018
	 * 
	 * @param userId
	 * @param primaryDimension
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param isCollaborative
	 * @return
	 * @since 20 Dec 2018
	 */
	public RepresentativeAOInfo(String userId, PrimaryDimension primaryDimension,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, boolean isCollaborative)
	{
		long t1 = System.currentTimeMillis();
		this.userId = userId;
		List<?> listOfFeats = Enums.getFeaturesForDatabase(Constant.getDatabaseName());
		// LinkedHashMap<String, Map<Integer, ArrayList<T>>> mapOfEachFeatForEachPDVal = new LinkedHashMap<>(
		// listOfFeats.size());

		// Duration from prev is essential for all datasets
		Map<Integer, ArrayList<Long>> mapOfEachFeatForEachPDValDurFromPrev = getListOfFeatureVals(userId,
				trainTimelinesAllUsersContinuousFiltrd, isCollaborative,
				ActivityObject2018::getDurationInSecondsFromPrev);

		Map<Integer, ArrayList<Long>> mapOfEachFeatForEachPDValDurInSecFromNext = getListOfDurationFromNext(userId,
				trainTimelinesAllUsersContinuousFiltrd, isCollaborative);

		Map<Integer, ArrayList<Long>> mapOfEachFeatForEachPDValDuration = new HashMap<>();
		Map<Integer, ArrayList<Double>> mapOfEachFeatForEachPDValDistTrav = new HashMap<>();
		Map<Integer, ArrayList<String>> mapOfEachFeatForEachPDValAvgAlt = new HashMap<>();
		Map<Integer, ArrayList<Double>> mapOfEachFeatForEachPDValDistFromPrev = new HashMap<>();
		Map<Integer, ArrayList<Integer>> mapOfEachFeatForEachPDValPopularity = new HashMap<>();
		Map<Integer, ArrayList<Integer>> mapOfEachFeatForEachPDValLocationsIDs = new HashMap<>();
		Map<Integer, ArrayList<Integer>> mapOfEachFeatForEachPDValActivityIDs = new HashMap<>();

		this.aggValOfEachFeatForEachPDValDurationInSecsFromPrev = mapOfEachFeatForEachPDValDurFromPrev.entrySet()
				.stream().collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.getPercentileL(e.getValue(), 50)));
		this.aggValOfEachFeatForEachPDValDurationFromNext = mapOfEachFeatForEachPDValDurInSecFromNext.entrySet()
				.stream().collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.getPercentileL(e.getValue(), 50)));

		this.aggStartLatLongEndLatLong_ = new HashMap<>();
		this.aggStartLatLongEndLatLongAsHSC = new HashMap<>();
		this.aggValOfEachFeatForEachPDValDuration = new HashMap<>();
		this.aggValOfEachFeatForEachPDValDistTrav = new HashMap<>();
		this.aggValOfEachFeatForEachPDValAvgAlt = new HashMap<>();
		this.aggValOfEachFeatForEachPDValDistFromPrev = new HashMap<>();
		this.aggValOfEachFeatForEachPDValPopularity = new HashMap<>();
		this.aggValOfEachFeatForEachPDValLocationID = new HashMap<>();
		this.aggValOfEachFeatForEachPDValActivityID = new HashMap<>();

		// ActivityObject2018 ao;
		// ao.getLocationIDs()
		for (Object f : listOfFeats)
		{
			switch (f.toString())
			{

			case "ActNameF":
			{
				mapOfEachFeatForEachPDValActivityIDs = getListOfFeatureVals(userId,
						trainTimelinesAllUsersContinuousFiltrd, isCollaborative, ActivityObject2018::getActivityID);

				aggValOfEachFeatForEachPDValActivityID = mapOfEachFeatForEachPDValActivityIDs.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.mostCommon(e.getValue())));
				break;
			}

			case "StartTimeF":
			{
				break;
			}

			case "DurationFromPrevF":
			{
				break;
			}
			case "DurationF":
			{
				mapOfEachFeatForEachPDValDuration = getListOfFeatureVals(userId, trainTimelinesAllUsersContinuousFiltrd,
						isCollaborative, ActivityObject2018::getDurationInSeconds);
				aggValOfEachFeatForEachPDValDuration = mapOfEachFeatForEachPDValDuration.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.getPercentileL(e.getValue(), 50)));
				break;
			}

			case "DistTravelledF":
			{
				mapOfEachFeatForEachPDValDistTrav = getListOfFeatureVals(userId, trainTimelinesAllUsersContinuousFiltrd,
						isCollaborative, ActivityObject2018::getDistanceTravelled);
				aggValOfEachFeatForEachPDValDistTrav = mapOfEachFeatForEachPDValDistTrav.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.getPercentile(e.getValue(), 50)));
				break;
			}
			case "StartGeoF":
			{
				aggStartLatLongEndLatLong_ = getStartAndEndGeoCoordinatesForEachPDVal(userId,
						trainTimelinesAllUsersContinuousFiltrd, isCollaborative, "_");
				aggStartLatLongEndLatLongAsHSC = getStartAndEndGeoCoordASHSCIndexForEachPDVal(userId,
						trainTimelinesAllUsersContinuousFiltrd, isCollaborative);
				// mapOfEachFeatForEachPDValStartLat = getListOfFeatureVals(userId,
				// trainTimelinesAllUsersContinuousFiltrd,
				// isCollaborative, ActivityObject2018::getStartLatitude);
				// mapOfEachFeatForEachPDValStartLon = getListOfFeatureVals(userId,
				// trainTimelinesAllUsersContinuousFiltrd,
				// isCollaborative, ActivityObject2018::getStartLongitude);
				break;
			}
			case "LocationF":
			{
				mapOfEachFeatForEachPDValLocationsIDs = getListOfFeatureValsColl(userId,
						trainTimelinesAllUsersContinuousFiltrd, isCollaborative, ActivityObject2018::getLocationIDs);

				aggValOfEachFeatForEachPDValLocationID = mapOfEachFeatForEachPDValLocationsIDs.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.mostCommon(e.getValue())));

				if (aggStartLatLongEndLatLong_.size() == 0)
				{
					aggStartLatLongEndLatLong_ = getStartAndEndGeoCoordinatesForEachPDVal(userId,
							trainTimelinesAllUsersContinuousFiltrd, isCollaborative, "_");

					aggStartLatLongEndLatLongAsHSC = getStartAndEndGeoCoordASHSCIndexForEachPDVal(userId,
							trainTimelinesAllUsersContinuousFiltrd, isCollaborative);
					// mapOfEachFeatForEachPDValStartLat = getListOfFeatureVals(userId,
					// trainTimelinesAllUsersContinuousFiltrd, isCollaborative,
					// ActivityObject2018::getStartLatitude);
					// mapOfEachFeatForEachPDValStartLon = getListOfFeatureVals(userId,
					// trainTimelinesAllUsersContinuousFiltrd, isCollaborative,
					// ActivityObject2018::getStartLongitude);
				}
				break;
			}
			case "EndGeoF":
			{
				if (aggStartLatLongEndLatLong_.size() == 0)
				{
					aggStartLatLongEndLatLong_ = getStartAndEndGeoCoordinatesForEachPDVal(userId,
							trainTimelinesAllUsersContinuousFiltrd, isCollaborative, "_");
					aggStartLatLongEndLatLongAsHSC = getStartAndEndGeoCoordASHSCIndexForEachPDVal(userId,
							trainTimelinesAllUsersContinuousFiltrd, isCollaborative);
				}
				// mapOfEachFeatForEachPDValEndLat = getListOfFeatureVals(userId,
				// trainTimelinesAllUsersContinuousFiltrd,
				// isCollaborative, ActivityObject2018::getEndLatitude);
				// mapOfEachFeatForEachPDValEndLon = getListOfFeatureVals(userId,
				// trainTimelinesAllUsersContinuousFiltrd,
				// isCollaborative, ActivityObject2018::getEndLongitude);
				break;
			}
			case "AvgAltitudeF":
			{
				mapOfEachFeatForEachPDValAvgAlt = getListOfFeatureVals(userId, trainTimelinesAllUsersContinuousFiltrd,
						isCollaborative, ActivityObject2018::getAvgAltitude);
				aggValOfEachFeatForEachPDValAvgAlt = mapOfEachFeatForEachPDValAvgAlt.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.getPercentileS(e.getValue(), 50)));
				break;
			}
			case "DistFromPrevF":
			{
				mapOfEachFeatForEachPDValDistFromPrev = getListOfFeatureVals(userId,
						trainTimelinesAllUsersContinuousFiltrd, isCollaborative,
						ActivityObject2018::getDistanceInMFromPrev);
				aggValOfEachFeatForEachPDValDistFromPrev = mapOfEachFeatForEachPDValDistFromPrev.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.getPercentile(e.getValue(), 50)));
				break;
			}
			default:
				PopUps.printTracedErrorMsgWithExit("Error: unknown feature: " + f.toString());
			}
		}
		System.out.println(
				"Time taken by  RepresentativeAOInfo= " + ((System.currentTimeMillis() - t1) / 1000) + " secs");
	}
	//////////////////////////
	// ActivityObject2018 repAO = mapOfRepAOs.get(userId).get(topPrimaryDimensionVal);
	// if (repAO == null)
	// {
	// PopUps.printTracedErrorMsgWithExit("Error in getRepresentativeAO: topRecommActName:"
	// + topPrimaryDimensionVal + " not found in mapOfRepAo");
	// }
	//
	// double medianPreceedingDurationInms = mapOfMedianPreSuccDurationInms.get(userId).get(topPrimaryDimensionVal)
	// .getFirst();
	// if (medianPreceedingDurationInms <= 0)
	// {
	// PopUps.printTracedErrorMsgWithExit(
	// "Error in getRepresentativeAO: medianPreceedingDuration = " + medianPreceedingDurationInms);
	// }
	//
	// long recommTimeInms = recommendationTimestamp.getTime();
	//
	// Timestamp newRecommTimestamp = new Timestamp((long) (recommTimeInms + medianPreceedingDurationInms));
	//
	// if (!DateTimeUtils.isSameDate(recommendationTimestamp, newRecommTimestamp))
	// {
	// System.err.println("recommendationTime = " + recommendationTimestamp + " newRecommTimestamp= "
	// + newRecommTimestamp + " are not same day. medianPreceedingDuration = "
	// + medianPreceedingDurationInms + " for topRecommActName =" + topPrimaryDimensionVal);
	// }
	//
	// if (VerbosityConstants.verbose)
	// {
	// System.out.println("Debug: getRepresentativeAO: old recommendationTime="
	// + recommendationTimestamp.toLocalDateTime().toString() + "medianPreceedingDuration="
	// + medianPreceedingDurationInms + " new recommendationTime="
	// + newRecommTimestamp.toLocalDateTime().toString());
	// }
	// repAO.setEndTimestamp(newRecommTimestamp); // only end timestamp used, make sure there is no start timestamp,
	// // since we are making recommendation at end timestamps and gowalla act objs are points in time.
	// repAO.setStartTimestamp(newRecommTimestamp);
	//
	// // {
	// // System.err.println("Debug1357: inside getRepresentativeAO: recommendationTime = " + recommendationTime
	// // + " newRecommTimestamp= " + newRecommTimestamp + ". medianPreceedingDuration = "
	// // + medianPreceedingDuration + " for topRecommActName =" + topRecommActName);
	// // System.out.println("repAO = " + repAO.toStringAllGowallaTS());
	// // }

	// return null;// repAO;

	/**
	 * 
	 * @param userId
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param isCollaborative
	 * @param delimiter
	 * @return actID, list of Pairs {ao.getStartLatitude() + delimiter + ao.getStartLongitude() + delimiter +
	 *         ao.getEndLatitude() + delimiter + ao.getEndLongitude();>
	 * @since 20 Dec 2018
	 */
	private static Map<Integer, ArrayList<String>> getStartAndEndGeoCoordinatesForEachPDVal(String userId,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, boolean isCollaborative,
			String delimiter)
	{
		Map<Integer, ArrayList<String>> mapOfStartLatLonEndLatLonForEachPDVal = new HashMap<>();

		for (Entry<String, Timeline> e : trainTimelinesAllUsersContinuousFiltrd.entrySet())
		{
			String currUserID = (e.getKey());

			if ((isCollaborative && currUserID.equals(userId) == false)
					|| (!isCollaborative && currUserID.equals(userId)))
			{
				for (ActivityObject2018 ao : e.getValue().getActivityObjectsInTimeline())
				{
					ArrayList<Integer> pdVals = ao.getPrimaryDimensionVal();
					for (Integer pdVal : pdVals)
					{
						ArrayList<String> listOfFeatureVals = mapOfStartLatLonEndLatLonForEachPDVal.get(pdVal);

						if (listOfFeatureVals == null)
						{
							listOfFeatureVals = new ArrayList<>();
						}

						String val = ao.getStartLatitude() + delimiter + ao.getStartLongitude() + delimiter
								+ ao.getEndLatitude() + delimiter + ao.getEndLongitude();
						listOfFeatureVals.add(val);
						// listOfDurationFromPrev.add(ao.getDurationInSecondsFromPrev());
						mapOfStartLatLonEndLatLonForEachPDVal.put(pdVal, listOfFeatureVals);
					}
				}
			}
		}
		return mapOfStartLatLonEndLatLonForEachPDVal;
	}

	/**
	 * Storing start and end geo coordinates as Hilbert Space filling curve indices
	 * 
	 * @param userId
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param isCollaborative
	 * @return Map<PDVal, ArrayList<Pair<HSCIndexOfStartGeos, HSCIndexOfEndGeos>>>
	 * @since 20 Dec 2018
	 */
	private static Map<Integer, ArrayList<Pair<Long, Long>>> getStartAndEndGeoCoordASHSCIndexForEachPDVal(String userId,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, boolean isCollaborative)
	{
		Map<Integer, ArrayList<Pair<Long, Long>>> mapOfStartLatLonEndLatLonForEachPDVal = new HashMap<>();

		for (Entry<String, Timeline> e : trainTimelinesAllUsersContinuousFiltrd.entrySet())
		{
			String currUserID = (e.getKey());

			// if ((isCollaborative && currUserID != userId) || (!isCollaborative && currUserID == userId))
			if ((isCollaborative && currUserID.equals(userId) == false)
					|| (!isCollaborative && currUserID.equals(userId)))
			{
				for (ActivityObject2018 ao : e.getValue().getActivityObjectsInTimeline())
				{
					ArrayList<Integer> pdVals = ao.getPrimaryDimensionVal();
					for (Integer pdVal : pdVals)
					{
						ArrayList<Pair<Long, Long>> listOfFeatureVals = mapOfStartLatLonEndLatLonForEachPDVal
								.get(pdVal);

						if (listOfFeatureVals == null)
						{
							listOfFeatureVals = new ArrayList<>();
						}

						// to check is start and end lat, long exists for the dataset
						String startLat = ao.getStartLatitude();
						String endLat = ao.getEndLatitude();

						long hilbertSpaceFillingCurveIndexForStartGeo = startLat.length() > 0
								? HilbertCurveUtils.getCompactHilbertCurveIndex(startLat, ao.getStartLongitude())
								: -9999;

						long hilbertSpaceFillingCurveIndexForEndGeo = endLat.length() > 0
								? HilbertCurveUtils.getCompactHilbertCurveIndex(endLat, ao.getEndLongitude())
								: -9999;

						listOfFeatureVals.add(new Pair<>(hilbertSpaceFillingCurveIndexForStartGeo,
								hilbertSpaceFillingCurveIndexForEndGeo));
						// listOfDurationFromPrev.add(ao.getDurationInSecondsFromPrev());
						mapOfStartLatLonEndLatLonForEachPDVal.put(pdVal, listOfFeatureVals);
					}
				}
			}
		}
		return mapOfStartLatLonEndLatLonForEachPDVal;
	}

	/**
	 * 
	 * @param userId
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param isCollaborative
	 * @param getter
	 * @return
	 * @since 20 Dec 2018
	 */
	public static <T> Map<Integer, ArrayList<T>> getListOfFeatureVals(String userId,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, boolean isCollaborative,
			Function<ActivityObject2018, T> getter)
	{
		Map<Integer, ArrayList<T>> mapOfDurationFromPrevForEachPDVal = new HashMap<>();
		for (Entry<String, Timeline> e : trainTimelinesAllUsersContinuousFiltrd.entrySet())
		{
			String currUserID = e.getKey();

			// if ((isCollaborative && currUserID != userId) || (!isCollaborative && currUserID == userId))
			if ((isCollaborative && currUserID.equals(userId) == false)
					|| (!isCollaborative && currUserID.equals(userId)))
			{
				for (ActivityObject2018 ao : e.getValue().getActivityObjectsInTimeline())
				{
					ArrayList<Integer> pdVals = ao.getPrimaryDimensionVal();
					for (Integer pdVal : pdVals)
					{
						ArrayList<T> listOfFeatureVals = mapOfDurationFromPrevForEachPDVal.get(pdVal);

						if (listOfFeatureVals == null)
						{
							listOfFeatureVals = new ArrayList<>();
						}
						listOfFeatureVals.add(getter.apply(ao));
						// listOfDurationFromPrev.add(ao.getDurationInSecondsFromPrev());
						mapOfDurationFromPrevForEachPDVal.put(pdVal, listOfFeatureVals);
					}
				}
			}
		}
		return mapOfDurationFromPrevForEachPDVal;
	}

	/**
	 * 
	 * @param userId
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param isCollaborative
	 * @param getter
	 * @return
	 * @since 20 Dec 2018
	 */
	public static <T> Map<Integer, ArrayList<T>> getListOfFeatureValsColl(String userId,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, boolean isCollaborative,
			Function<ActivityObject2018, ArrayList<T>> getter)
	{
		Map<Integer, ArrayList<T>> mapOfDurationFromPrevForEachPDVal = new HashMap<>();
		for (Entry<String, Timeline> e : trainTimelinesAllUsersContinuousFiltrd.entrySet())
		{
			String currUserID = e.getKey();

			// if ((isCollaborative && currUserID != userId) || (!isCollaborative && currUserID == userId))
			if ((isCollaborative && currUserID.equals(userId) == false)
					|| (!isCollaborative && currUserID.equals(userId)))
			{
				for (ActivityObject2018 ao : e.getValue().getActivityObjectsInTimeline())
				{
					ArrayList<Integer> pdVals = ao.getPrimaryDimensionVal();
					for (Integer pdVal : pdVals)
					{
						ArrayList<T> listOfFeatureVals = mapOfDurationFromPrevForEachPDVal.get(pdVal);

						if (listOfFeatureVals == null)
						{
							listOfFeatureVals = new ArrayList<>();
						}
						listOfFeatureVals.addAll(getter.apply(ao));
						// listOfDurationFromPrev.add(ao.getDurationInSecondsFromPrev());
						mapOfDurationFromPrevForEachPDVal.put(pdVal, listOfFeatureVals);
					}
				}
			}
		}
		return mapOfDurationFromPrevForEachPDVal;
	}

	/**
	 * 
	 * @param userId
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param isCollaborative
	 * @param getter
	 * @return
	 * @since 20 Dec 2018
	 */
	public static Map<Integer, ArrayList<Long>> getListOfDurationFromNext(String userId,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, boolean isCollaborative)
	{
		Map<Integer, ArrayList<Long>> mapOfDurationFromNextForEachPDVal = new HashMap<>();

		// String prevUserID = "";
		for (Entry<String, Timeline> e : trainTimelinesAllUsersContinuousFiltrd.entrySet())
		{
			String currUserID = e.getKey();
			if ((isCollaborative && currUserID != userId) || (!isCollaborative && currUserID == userId))
			{
				ArrayList<Integer> prevPDVals = null;
				ArrayList<Integer> currPDVals = null;
				long prevStartTS = -1;
				long currStartTS = -1;

				for (ActivityObject2018 ao : e.getValue().getActivityObjectsInTimeline())
				{
					currStartTS = ao.getStartTimestampInms();
					ArrayList<Integer> pdVals = ao.getPrimaryDimensionVal();
					if (prevPDVals != null)
					{
						for (Integer pdVal : prevPDVals)
						{
							long durationFromNext = (currStartTS - prevStartTS) / 1000;// in secs
							ArrayList<Long> listOfFeatureVals = mapOfDurationFromNextForEachPDVal.get(pdVal);
							if (listOfFeatureVals == null)
							{
								listOfFeatureVals = new ArrayList<>();
							}
							listOfFeatureVals.add(durationFromNext);
							// listOfDurationFromPrev.add(ao.getDurationInSecondsFromPrev());
							mapOfDurationFromNextForEachPDVal.put(pdVal, listOfFeatureVals);
						}
					}
					prevPDVals = currPDVals;
					prevStartTS = currStartTS;
				}
				// prevUserID = currUserID;
			}
		}
		return mapOfDurationFromNextForEachPDVal;
	}

	///////////////////////////////// Moved from other classes
	/**
	 * 
	 * @param trainTestTimelinesForAllUsers
	 * @param uniqueLocIDs
	 * @param uniqueActivityIDs
	 * @param primaryDimension
	 * @param databaseName
	 * @return
	 */
	public static Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> buildRepresentativeAOsAllUsersPDVCOllAllUsers(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			Set<Integer> uniqueLocIDs, Set<Integer> uniqueActivityIDs, PrimaryDimension primaryDimension,
			String databaseName)
	{
		boolean sanityCheck = false;
		// String databaseName = Constant.getDatabaseName();
		System.out.println("Inside buildRepresentativeAOsAllUsersPDVCOllAllUsers using all users ");
		// LinkedHashMap<Integer, ActivityObject> repAOsForThisUser = null;
		// LinkedHashMap<Integer, Pair<Double, Double>> actMedianPreSuccDuration = null;
		Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> result = null;
		LinkedHashMap<String, Timeline> collTrainingTimelines = new LinkedHashMap<>();

		long t1 = System.currentTimeMillis();
		try
		{
			if (databaseName.equals("gowalla1") == false)
			{
				PopUps.printTracedErrorMsgWithExit("Error: database is  not gowalla1:" + databaseName);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// { System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1); }

			else
			{
				LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal = new LinkedHashMap<>();
				/**
				 * Could be useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDValInms = new LinkedHashMap<>();
				LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDValInms = new LinkedHashMap<>();

				// earlier version was using all possible vals fro PD but now using only those in training data.
				LinkedHashSet<Integer> distinctPDValsEncounteredInTraining = new LinkedHashSet<>();

				for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsers
						.entrySet())
				{
					Timeline userTrainingTimeline = TimelineTransformers
							.dayTimelinesToATimeline(trainTestForAUser.getValue().get(0), false, true);

					for (ActivityObject2018 ao : userTrainingTimeline.getActivityObjectsInTimeline())
					{
						distinctPDValsEncounteredInTraining.addAll(ao.getPrimaryDimensionVal());
					}
					collTrainingTimelines.put(trainTestForAUser.getKey(), userTrainingTimeline);
				}

				System.out.println("distinctPDValsEncounteredInCOllTraining.size() = "
						+ distinctPDValsEncounteredInTraining.size());

				// iterate over all possible primary dimension vals to initialise to preserve order.
				for (Integer pdVal : distinctPDValsEncounteredInTraining)
				{
					aosForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromPrevForEachPDValInms.put(pdVal, new ArrayList<>());
					durationFromNextForEachPDValInms.put(pdVal, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				int countAOs = 0;
				for (Entry<String, Timeline> trainingTimelineEntry : collTrainingTimelines.entrySet())
				{
					String userID = trainingTimelineEntry.getKey();

					// long durationFromPreviousInSecs = 0;
					long prevTimestampInms = 0;
					Set<Integer> prevPDValEncountered = null;

					for (ActivityObject2018 ao : trainingTimelineEntry.getValue().getActivityObjectsInTimeline())
					{
						countAOs += 1;
						Set<Integer> uniquePdValsInAO = new LinkedHashSet<>(ao.getPrimaryDimensionVal());
						for (Integer pdVal : uniquePdValsInAO)
						{
							// add this act object to the correct map entry in map of aos for given act names
							ArrayList<ActivityObject2018> aosStored = aosForEachPDVal.get(pdVal);
							if (aosStored == null)
							{
								PopUps.printTracedErrorMsg("Error: encountered pdval '" + pdVal
										+ "' is not in the list of pd vals from training data");
							}
							aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated
						}

						// store the preceeding and succeeding durations
						long currentTimestampInms = ao.getStartTimestamp().getTime();

						if (prevTimestampInms != 0)
						{ // add the preceeding duration for this AO
							for (Integer pdVal : uniquePdValsInAO)
							{
								durationFromPrevForEachPDValInms.get(pdVal)
										.add(currentTimestampInms - prevTimestampInms);
							}
						}

						if (prevPDValEncountered != null)
						{
							// add the succeeding duration for the previous AO
							for (Integer prevPDVal : prevPDValEncountered)
							{
								durationFromNextForEachPDValInms.get(prevPDVal)
										.add(currentTimestampInms - prevTimestampInms);
							}
						}

						prevTimestampInms = currentTimestampInms;
						prevPDValEncountered = uniquePdValsInAO;
					}
				}
				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					// userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(ao -> System.out
					// .print(ao.getPrimaryDimensionVal("/") + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachPDValue:");
					durationFromPrevForEachPDValInms.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachPDValue:");
					durationFromNextForEachPDValInms.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each pd value:");
					aosForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachPDVal.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = countAOs;// userTrainingTimelines.getActivityObjectsInTimeline().stream()
				// .count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// This sanity check below is relevant only for activity id as primary dimension, since it is in that
				// case each activity object has only one primary dimension val (activity id). It won't hold in case of
				// location ids since one activity object can have multiple location ids because of mergers, i.e,
				// sumOfCountOfAOsFroMap>sumOfCountOfAOsFromTimeline
				if (primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				else// (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (sumOfCountOfAOsFroMap < sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				result = computeRepresentativeActivityObjectForUserPDV2GenericUser(aosForEachPDVal,
						durationFromPrevForEachPDValInms, durationFromNextForEachPDValInms, databaseName,
						primaryDimension);

				// repAOsForThisUser = result.getFirst();
				// actMedianPreSuccDuration = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeAOsAllUsersPDVCOllAllUsers for all users  time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return result;

	}

	// Start of added on 20 Dec 2018
	/**
	 * NOT USED AT THE MOMENT
	 * 
	 * @param trainTestTimelinesForAllUsers
	 * @param uniqueLocIDs
	 * @param uniqueActivityIDs
	 * @return
	 */
	public static Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> buildRepresentativeAOsAllUsersPDVAllUsersDec2018(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			Set<Integer> uniqueLocIDs, Set<Integer> uniqueActivityIDs, String databaseName,
			PrimaryDimension primaryDimension)
	{
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeAOsAllUsersPDVAllUsersDec2018 using all users ");
		// LinkedHashMap<Integer, ActivityObject> repAOsForThisUser = null;
		// LinkedHashMap<Integer, Pair<Double, Double>> actMedianPreSuccDuration = null;
		Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> result = null;
		// LinkedHashMap<String,LinkedHashMap<Integer,ActivityObject2018>>

		LinkedHashMap<String, Timeline> collTrainingTimelines = new LinkedHashMap<>();

		long t1 = System.currentTimeMillis();
		try
		{
			// if (this.databaseName.equals("gowalla1") == false)
			// {
			// PopUps.printTracedErrorMsgWithExit("Error: database is not gowalla1:" + this.databaseName);
			// }
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// { System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1); }
			// else
			{
				LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal = new LinkedHashMap<>();
				/**
				 * Could be useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDValInms = new LinkedHashMap<>();
				LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDValInms = new LinkedHashMap<>();

				// earlier version was using all possible vals fro PD but now using only those in training data.
				LinkedHashSet<Integer> distinctPDValsEncounteredInTraining = new LinkedHashSet<>();

				for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsers
						.entrySet())
				{
					Timeline userTrainingTimeline = TimelineTransformers
							.dayTimelinesToATimeline(trainTestForAUser.getValue().get(0), false, true);

					for (ActivityObject2018 ao : userTrainingTimeline.getActivityObjectsInTimeline())
					{
						distinctPDValsEncounteredInTraining.addAll(ao.getPrimaryDimensionVal());
					}
					collTrainingTimelines.put(trainTestForAUser.getKey(), userTrainingTimeline);
				}

				System.out.println("distinctPDValsEncounteredInCOllTraining.size() = "
						+ distinctPDValsEncounteredInTraining.size());

				// iterate over all possible primary dimension vals to initialise to preserve order.
				for (Integer pdVal : distinctPDValsEncounteredInTraining)
				{
					aosForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromPrevForEachPDValInms.put(pdVal, new ArrayList<>());
					durationFromNextForEachPDValInms.put(pdVal, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				int countAOs = 0;
				for (Entry<String, Timeline> trainingTimelineEntry : collTrainingTimelines.entrySet())
				{
					String userID = trainingTimelineEntry.getKey();

					// long durationFromPreviousInSecs = 0;
					long prevTimestampInms = 0;
					Set<Integer> prevPDValEncountered = null;

					for (ActivityObject2018 ao : trainingTimelineEntry.getValue().getActivityObjectsInTimeline())
					{
						countAOs += 1;
						Set<Integer> uniquePdValsInAO = new LinkedHashSet<>(ao.getPrimaryDimensionVal());
						for (Integer pdVal : uniquePdValsInAO)
						{
							// add this act object to the correct map entry in map of aos for given act names
							ArrayList<ActivityObject2018> aosStored = aosForEachPDVal.get(pdVal);
							if (aosStored == null)
							{
								PopUps.printTracedErrorMsg("Error: encountered pdval '" + pdVal
										+ "' is not in the list of pd vals from training data");
							}
							aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated
						}

						// store the preceeding and succeeding durations
						long currentTimestampInms = ao.getStartTimestamp().getTime();

						if (prevTimestampInms != 0)
						{ // add the preceeding duration for this AO
							for (Integer pdVal : uniquePdValsInAO)
							{
								durationFromPrevForEachPDValInms.get(pdVal)
										.add(currentTimestampInms - prevTimestampInms);
							}
						}

						if (prevPDValEncountered != null)
						{
							// add the succeeding duration for the previous AO
							for (Integer prevPDVal : prevPDValEncountered)
							{
								durationFromNextForEachPDValInms.get(prevPDVal)
										.add(currentTimestampInms - prevTimestampInms);
							}
						}

						prevTimestampInms = currentTimestampInms;
						prevPDValEncountered = uniquePdValsInAO;
					}
				}
				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					// userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(ao -> System.out
					// .print(ao.getPrimaryDimensionVal("/") + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachPDValue:");
					durationFromPrevForEachPDValInms.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachPDValue:");
					durationFromNextForEachPDValInms.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each pd value:");
					aosForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachPDVal.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = countAOs;// userTrainingTimelines.getActivityObjectsInTimeline().stream()
				// .count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// This sanity check below is relevant only for activity id as primary dimension, since it is in that
				// case each activity object has only one primary dimension val (activity id). It won't hold in case of
				// location ids since one activity object can have multiple location ids because of mergers, i.e,
				// sumOfCountOfAOsFroMap>sumOfCountOfAOsFromTimeline
				if (primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				else// (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (sumOfCountOfAOsFroMap < sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				result = computeRepresentativeActivityObjectForUserPDV2GenericUser(aosForEachPDVal,
						durationFromPrevForEachPDValInms, durationFromNextForEachPDValInms, databaseName,
						primaryDimension);

				// repAOsForThisUser = result.getFirst();
				// actMedianPreSuccDuration = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeAOsAllUsersPDVCOllAllUsers for all users  time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return result;

	}

	/**
	 * 
	 * @param aosForEachPDVal
	 * @param durationFromPrevForEachPDValInms
	 * @param durationFromNextForEachPDValInms
	 * @param databaseName
	 * @param primaryDimension
	 * @return
	 */
	private static Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserPDV2GenericUser(
			LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal,
			LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDValInms,
			LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDValInms, String databaseName,
			PrimaryDimension primaryDimension)
	{
		System.out.println("Inside computeRepresentativeActivityObjectForUserPDV2genericUser for generic user");
		LinkedHashMap<Integer, ActivityObject2018> repAOs = new LinkedHashMap<>();
		LinkedHashMap<Integer, Pair<Double, Double>> actMedPreSuccDuration = new LinkedHashMap<>();

		System.out.println(
				PerformanceAnalytics.getHeapInformation() + "\n" + PerformanceAnalytics.getHeapPercentageFree());
		try
		{
			if (!databaseName.equals("gowalla1"))
			{
				PopUps.printTracedErrorMsgWithExit("Error: this method is currently only suitable for gowalla dataset");
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.
			for (Integer pdVal : durationFromPrevForEachPDValInms.keySet())// aosForEachPDVal.entrySet())
			{
				double medianDurationFromPrevForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromPrevForEachPDValInms.get(pdVal),
								"durationFromPrevForEachActName",
								"GenericUser__" + pdVal + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				double medianDurationFromNextForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromNextForEachPDValInms.get(pdVal),
								"durationFromPrevForEachActName",
								"GenericUser__" + pdVal + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				// Dummy because we are not going to actually use this, we will instead extract the timestamp from the
				// preceeding duration + timestamp of preceeding activity object. But we assign this median anyway to
				// avoid having it as numm which causes exception.
				Timestamp dummyMedianStartTS = new Timestamp(0);
				// Disable getMedianSecondsSinceMidnightTimestamp for speed as we were not using it anyway.
				// getMedianSecondsSinceMidnightTimestamp(
				// aos.stream().map(ao -> ao.getStartTimestamp()).collect(Collectors.toList()), userID,
				// pdVal.toString());

				// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
				// int medianCinsCount = (int) StatsUtils
				// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
				// .getPercentile(50);

				// NOTE: we only need to take care of feature which are used for edit distance computation.
				// Instantiate the representative activity object.
				int activityID = -1;
				ArrayList<Integer> locationIDs = new ArrayList<>();
				String activityName = "";
				// String locationName = "";
				String workingLevelCatIDs = "";

				switch (primaryDimension)
				{
				case ActivityID:
				{
					activityID = pdVal;
					activityName = String.valueOf(pdVal); // for gowalla dataset, act id and act name are same
					workingLevelCatIDs = pdVal + "__";
					break;
				}
				case LocationID:
				{
					locationIDs.add(pdVal);
					workingLevelCatIDs = pdVal + "__";
					// locationName =
					// DomainConstants.getLocIDLocationObjectDictionary().get(pdVal).getLocationName();
					// if (locationName == null || locationName.length() == 0)
					// { PopUps.printTracedErrorMsg("Error: fetched locationName= " + locationName); }
					break;
				}
				default:
					PopUps.printTracedErrorMsgWithExit(
							"Error: unknown primaryDimension = " + Constant.primaryDimension);
					break;
				}
				// ActivityObject(int activityID, ArrayList<Integer> locationIDs, String activityName, String
				// locationName, Timestamp startTimestamp, String startLatitude, String startLongitude, String
				// startAltitude, String userID, int photos_count, int checkins_count, int users_count, int
				// radius_meters, int highlights_count, int items_count, int max_items_count, String workingLevelCatIDs,
				// double distanceInMFromNext, long durationInSecsFromNext, String[] levelWiseCatIDs)

				int cins_count = -1;

				if (Constant.useMedianCinsForRepesentationAO)
				{
					cins_count = RecommendationTestsUtils.getMedianCheckinsCountForGivePDVal(aosForEachPDVal, pdVal);
				}

				ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
						activityName, "", dummyMedianStartTS, "", "", "", "GenericUser", -1, cins_count, -1, -1, -1, -1,
						-1, workingLevelCatIDs, -1, -1, new String[] { "" });

				repAOs.put(pdVal, repAOForThisActNameForThisUser);
				actMedPreSuccDuration.put(pdVal,
						new Pair<>(medianDurationFromPrevForEachActName, medianDurationFromNextForEachActName));

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Exiting computeRepresentativeActivityObjectForUserPDV2genericUser for generic user");
		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOs, actMedPreSuccDuration);
	}

	/**
	 * 
	 * @param trainTestTimelinesForAllUsers
	 * @param userId
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 * @return
	 */
	public static Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> prebuildRepresentativeActivityObjects(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers, int userId,
			LinkedHashMap<Date, Timeline> userTrainingTimelines, LinkedHashMap<Date, Timeline> userTestTimelines,
			String databaseName, PrimaryDimension primaryDimension)
	{
		Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResult = null;

		// if (Constant.collaborativeCandidates)
		// { repAOResult = buildRepresentativeAOsForUserPDVCOll(userId, trainTestTimelinesForAllUsers,
		// Constant.getUniqueLocIDs(), Constant.getUniqueActivityIDs()); } else {
		repAOResult = buildRepresentativeAOsForUserPDV2(userId,
				TimelineTransformers.dayTimelinesToATimeline(userTrainingTimelines, false, true),
				TimelineTransformers.dayTimelinesToATimeline(userTestTimelines, false, true),
				Constant.getUniqueLocIDs(), Constant.getUniqueActivityIDs(), databaseName, primaryDimension);

		// Start of Sanity Check for buildRepresentativeAOsForUserPD()
		if (VerbosityConstants.checkSanityPDImplementn && Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
		{
			// for Activity ID as primary dimension, the output of
			// buildRepresentativeAOsForUserPD and buildRepresentativeAOsForUser should be same
			// (except data type)
			Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> repAOResultActName = buildRepresentativeAOsForUser(
					userId, TimelineTransformers.dayTimelinesToATimeline(userTrainingTimelines, false, true),
					Constant.getActivityNames(),
					TimelineTransformers.dayTimelinesToATimeline(userTestTimelines, false, true), databaseName,
					primaryDimension);
			Sanity.compareOnlyNonEmpty(repAOResult, repAOResultActName);
		}
		// end of Sanity Check for buildRepresentativeAOsForUserPD()
		// }
		return repAOResult;
	}

	/**
	 * for each user, for each activity (category id), create a representative avg activity object.
	 * <p>
	 * For example,
	 * 
	 * for user 1, for activity ‘Asian Food’, create an Activity Object with activity name as ‘Asian Food’ and other
	 * features are the average values of the features all the ‘Asian Food’ activity objects occurring the training set
	 * of user 1.
	 * 
	 * 
	 * @param userId
	 * @param userTrainingTimelines
	 * @param allPossibleActivityNames
	 * @param userTestTimelines
	 *            only to find which act names occur in test but not in training
	 * @return
	 */
	public static Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> buildRepresentativeAOsForUser(
			int userId, Timeline userTrainingTimelines, String[] allPossibleActivityNames, Timeline userTestTimelines,
			String databaseName, PrimaryDimension primaryDimension)
	{
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeActivityObjects for user " + userId);
		LinkedHashMap<String, ActivityObject2018> repAOsForThisUser = null;
		LinkedHashMap<String, Pair<Double, Double>> actMedianPreSuccDuration = null;

		long t1 = System.currentTimeMillis();
		try
		{
			if (databaseName.equals("gowalla1") == false)
			{
				System.err.println("Error: database is  not gowalla1:" + databaseName);
				System.exit(-1);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// {
			// System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1);
			// }

			else
			{

				LinkedHashMap<String, ArrayList<ActivityObject2018>> aosForEachActName = new LinkedHashMap<>();

				/**
				 * Useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<String, ArrayList<Long>> durationFromPrevForEachActName = new LinkedHashMap<>();
				LinkedHashMap<String, ArrayList<Long>> durationFromNextForEachActName = new LinkedHashMap<>();

				// iterate over all possible activity names. initialise to preserve order.
				for (String actName : allPossibleActivityNames)
				{
					aosForEachActName.put(actName, new ArrayList<>());
					durationFromPrevForEachActName.put(actName, new ArrayList<>());
					durationFromNextForEachActName.put(actName, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				// long durationFromPreviousInSecs = 0;
				long prevTimestamp = 0;
				String prevActNameEncountered = null;

				LinkedHashSet<String> distinctActNamesEncounteredInTraining = new LinkedHashSet<>();

				for (ActivityObject2018 ao : userTrainingTimelines.getActivityObjectsInTimeline())
				{
					String actNameEncountered = ao.getActivityName();
					distinctActNamesEncounteredInTraining.add(actNameEncountered);

					// add this act objec to the correct map entry in map of aos for given act names
					ArrayList<ActivityObject2018> aosStored = aosForEachActName.get(actNameEncountered);
					if (aosStored == null)
					{
						System.err.println(PopUps.getTracedErrorMsg("Error: encountered act name '" + actNameEncountered
								+ "' is not in the list of all possible act names"));
					}
					aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated

					// store the preceeding and succeeding durations
					long currentTimestamp = ao.getStartTimestamp().getTime();

					if (prevTimestamp != 0)
					{ // add the preceeding duration for this AO
						durationFromPrevForEachActName.get(actNameEncountered).add(currentTimestamp - prevTimestamp);
					}

					if (prevActNameEncountered != null)
					{
						// add the succeeding duration for the previous AO
						durationFromNextForEachActName.get(prevActNameEncountered)
								.add(currentTimestamp - prevTimestamp);
					}
					prevTimestamp = currentTimestamp;
					prevActNameEncountered = actNameEncountered;
				}

				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(
							ao -> System.out.print(ao.getActivityName() + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachActName:");
					durationFromPrevForEachActName.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachActName:");
					durationFromNextForEachActName.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each act name:");
					aosForEachActName.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachActName.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = userTrainingTimelines.getActivityObjectsInTimeline().stream()
						.count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// System.out.println("Num of ");
				if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
				{
					System.err.println(
							"Sanity check failed in buildRepresentativeActivityObjects\n(sumOfCountOfAOsFroMap) != "
									+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
									+ sumOfCountOfAOsFromTimeline);
				}

				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> result = computeRepresentativeActivityObjectForUserV2(
						userId, aosForEachActName, durationFromPrevForEachActName, durationFromNextForEachActName,
						databaseName, primaryDimension);
				repAOsForThisUser = result.getFirst();
				actMedianPreSuccDuration = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeActivityObjects for user " + userId + " time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return new Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>>(
				repAOsForThisUser, actMedianPreSuccDuration);
	}

	/**
	 * Fork of buildRepresentativeAOsForUserPDV() intiated to reduce memory consumption (for primary dimension version
	 * 2)
	 * <p>
	 * for each user, for each activity (category id), create a representative avg activity object.
	 * <p>
	 * For example,
	 * 
	 * for user 1, for activity ‘Asian Food’, create an Activity Object with activity name as ‘Asian Food’ and other
	 * features are the average values of the features all the ‘Asian Food’ activity objects occurring the training set
	 * of user 1.
	 * 
	 * @param userId
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 *            only to find which act names occur in test but not in training
	 * @param uniqueLocIDs
	 * @param uniqueActivityIDs
	 * @return
	 */
	public static Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> buildRepresentativeAOsForUserPDV2(
			int userId, Timeline userTrainingTimelines, Timeline userTestTimelinesqq, Set<Integer> uniqueLocIDs,
			Set<Integer> uniqueActivityIDs, String databaseName, PrimaryDimension primaryDimension)
	{
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeAOsForUserPDV2 for user " + userId);
		LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = null;
		LinkedHashMap<Integer, Pair<Double, Double>> actMedianPreSuccDurationInms = null;

		long t1 = System.currentTimeMillis();
		try
		{
			if (databaseName.equals("gowalla1") == false)
			{
				PopUps.printTracedErrorMsgWithExit("Error: database is  not gowalla1:" + databaseName);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// { System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1); }

			else
			{
				LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal = new LinkedHashMap<>();
				/**
				 * Useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDValInms = new LinkedHashMap<>();
				LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDValInms = new LinkedHashMap<>();

				// earlier version was using all possible vals fro PD but now using only those in training data.
				LinkedHashSet<Integer> distinctPDValsEncounteredInTraining = new LinkedHashSet<>();

				for (ActivityObject2018 ao : userTrainingTimelines.getActivityObjectsInTimeline())
				{
					distinctPDValsEncounteredInTraining.addAll(ao.getPrimaryDimensionVal());
				}
				System.out.println(
						"distinctPDValsEncounteredInTraining.size() = " + distinctPDValsEncounteredInTraining.size());

				// iterate over all possible primary dimension vals to initialise to preserve order.
				for (Integer pdVal : distinctPDValsEncounteredInTraining)
				{
					aosForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromPrevForEachPDValInms.put(pdVal, new ArrayList<>());
					durationFromNextForEachPDValInms.put(pdVal, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				// long durationFromPreviousInSecs = 0;
				long prevTimestampInms = 0;
				Set<Integer> prevPDValEncountered = null;

				for (ActivityObject2018 ao : userTrainingTimelines.getActivityObjectsInTimeline())
				{
					Set<Integer> uniquePdValsInAO = new LinkedHashSet<>(ao.getPrimaryDimensionVal());
					for (Integer pdVal : uniquePdValsInAO)
					{
						// add this act object to the correct map entry in map of aos for given act names
						ArrayList<ActivityObject2018> aosStored = aosForEachPDVal.get(pdVal);
						if (aosStored == null)
						{
							PopUps.printTracedErrorMsg("Error: encountered pdval '" + pdVal
									+ "' is not in the list of pd vals from training data");
						}
						aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated
					}

					// store the preceeding and succeeding durations
					long currentTimestampInms = ao.getStartTimestamp().getTime();

					if (prevTimestampInms != 0)
					{ // add the preceeding duration for this AO
						for (Integer pdVal : uniquePdValsInAO)
						{
							durationFromPrevForEachPDValInms.get(pdVal).add(currentTimestampInms - prevTimestampInms);
						}
					}

					if (prevPDValEncountered != null)
					{
						// add the succeeding duration for the previous AO
						for (Integer prevPDVal : prevPDValEncountered)
						{
							durationFromNextForEachPDValInms.get(prevPDVal)
									.add(currentTimestampInms - prevTimestampInms);
						}
					}

					prevTimestampInms = currentTimestampInms;
					prevPDValEncountered = uniquePdValsInAO;
				}

				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(ao -> System.out
							.print(ao.getPrimaryDimensionVal("/") + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachPDValue:");
					durationFromPrevForEachPDValInms.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachPDValue:");
					durationFromNextForEachPDValInms.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each pd value:");
					aosForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachPDVal.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = userTrainingTimelines.getActivityObjectsInTimeline().stream()
						.count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// This sanity check below is relevant only for activity id as primary dimension, since it is in that
				// case each activity object has only one primary dimension val (activity id). It won't hold in case of
				// location ids since one activity object can have multiple location ids because of mergers, i.e,
				// sumOfCountOfAOsFroMap>sumOfCountOfAOsFromTimeline
				if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				else// (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (sumOfCountOfAOsFroMap < sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> result = computeRepresentativeActivityObjectForUserPDV2(
						userId, aosForEachPDVal, durationFromPrevForEachPDValInms, durationFromNextForEachPDValInms,
						databaseName, primaryDimension);

				repAOsForThisUser = result.getFirst();
				actMedianPreSuccDurationInms = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeAOsForUserPDV2 for user " + userId + " time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOsForThisUser, actMedianPreSuccDurationInms);
	}

	/**
	 * 
	 * @param userID
	 * @param aosForEachActName
	 * @param durationFromPrevForEachActName
	 * @param durationFromNextForEachActName
	 * @return
	 */
	public static Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserV1(
			int userID, LinkedHashMap<String, ArrayList<ActivityObject2018>> aosForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromPrevForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromNextForEachActName, String databaseName,
			PrimaryDimension primaryDimension)
	{
		System.out.println("Inside computeRepresentativeActivityObject for userID" + userID);
		LinkedHashMap<String, ActivityObject2018> repAOs = new LinkedHashMap<String, ActivityObject2018>();
		LinkedHashMap<String, Pair<Double, Double>> actMedPreSuccDuration = new LinkedHashMap<>();

		try
		{
			if (!databaseName.equals("gowalla1"))
			{
				System.err.println(
						PopUps.getTracedErrorMsg("Error: this method is currently only suitable for gowalla dataset"));
				System.exit(-1);
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.

			for (Entry<String, ArrayList<ActivityObject2018>> actNameEntry : aosForEachActName.entrySet())
			{
				String actName = actNameEntry.getKey();
				ArrayList<ActivityObject2018> aos = actNameEntry.getValue();

				double medianDurationFromPrevForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromPrevForEachActName.get(actName),
								"durationFromPrevForEachActName",
								userID + "__" + actName + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				double medianDurationFromNextForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromNextForEachActName.get(actName),
								"durationFromPrevForEachActName",
								userID + "__" + actName + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				// double medianSecondsFromMidnight =
				Timestamp mediaStartTS = RecommendationTestsUtils.getMedianSecondsSinceMidnightTimestamp(
						aos.stream().map(ao -> ao.getStartTimestamp()).collect(Collectors.toList()), userID, actName);

				double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
				int medianCinsCount = (int) StatsUtils.getDescriptiveStatistics(cinsCount, "cinsCount",
						userID + "__" + actName + "cinsCount.txt", false).getPercentile(50);

				// NOTE: we only need to take care of feature which are used for edit distance computation.
				ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(
						(int) Integer.valueOf(actName), new ArrayList<Integer>(), actName, "", mediaStartTS, "", "", "",
						String.valueOf(userID), -1, medianCinsCount, -1, -1, -1, -1, -1, actName + "__", -1, -1,
						new String[] { "" });

				repAOs.put(actName, repAOForThisActNameForThisUser);
				actMedPreSuccDuration.put(actName,
						new Pair<>(medianDurationFromPrevForEachActName, medianDurationFromNextForEachActName));

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Exiting computeRepresentativeActivityObject for userID" + userID);
		return new Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>>(repAOs,
				actMedPreSuccDuration);
	}

	/**
	 * TODO: ALERT REALISEDO N 8 FEB 2018: NOTE: CHECKINS COUNT IS -1 IN HERE, PROBABLY BETTER TO TAKE MEDIAN INSTEAD.
	 * 
	 * @param userID
	 * @param aosForEachActName
	 * @param durationFromPrevForEachActName
	 * @param durationFromNextForEachActName
	 * @return
	 */
	public static Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserV2(
			int userID, LinkedHashMap<String, ArrayList<ActivityObject2018>> aosForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromPrevForEachActName,
			LinkedHashMap<String, ArrayList<Long>> durationFromNextForEachActName, String databaseName,
			PrimaryDimension primaryDimension)
	{
		System.out.println("Inside computeRepresentativeActivityObject for userID" + userID);
		LinkedHashMap<String, ActivityObject2018> repAOs = new LinkedHashMap<String, ActivityObject2018>();
		LinkedHashMap<String, Pair<Double, Double>> actMedPreSuccDuration = new LinkedHashMap<>();

		try
		{
			if (!databaseName.equals("gowalla1"))
			{
				System.err.println(
						PopUps.getTracedErrorMsg("Error: this method is currently only suitable for gowalla dataset"));
				System.exit(-1);
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.
			for (Entry<String, ArrayList<ActivityObject2018>> actNameEntry : aosForEachActName.entrySet())
			{
				String actName = actNameEntry.getKey();
				ArrayList<ActivityObject2018> aos = actNameEntry.getValue();

				double medianDurationFromPrevForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromPrevForEachActName.get(actName),
								"durationFromPrevForEachActName",
								userID + "__" + actName + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				double medianDurationFromNextForEachActName = StatsUtils
						.getDescriptiveStatisticsLong(durationFromNextForEachActName.get(actName),
								"durationFromPrevForEachActName",
								userID + "__" + actName + "durationFromPrevForEachActName.txt", false)
						.getPercentile(50);

				// Dummy because we are not going to actually use this, we will instead extract the timestamp from the
				// preceeding duration + timestamp of preceeding activity object. But we assign this median anyway to
				// avoid having it as numm which causes exception.
				Timestamp dummyMedianStartTS = new Timestamp(0);
				// Disabled on 20 July 2017 to improve speed and compatibility for checking method from primary
				// dimension perspective. It was not being used anyway.
				// $ getMedianSecondsSinceMidnightTimestamp(
				// $ aos.stream().map(ao -> ao.getStartTimestamp()).collect(Collectors.toList()), userID, actName);

				// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
				// int medianCinsCount = (int) StatsUtils
				// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
				// .getPercentile(50);

				// NOTE: we only need to take care of feature which are used for edit distance computation.
				ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(
						(int) Integer.valueOf(actName), new ArrayList<Integer>(), actName, "", dummyMedianStartTS, "",
						"", "", String.valueOf(userID), -1, -1, -1, -1, -1, -1, -1, actName + "__", -1, -1,
						new String[] { "" });

				repAOs.put(actName, repAOForThisActNameForThisUser);
				actMedPreSuccDuration.put(actName,
						new Pair<>(medianDurationFromPrevForEachActName, medianDurationFromNextForEachActName));

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Exiting computeRepresentativeActivityObject for userID" + userID);
		return new Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>>(repAOs,
				actMedPreSuccDuration);
	}

	/**
	 * 
	 * @param userID
	 * @param aosForEachPDVal
	 * @param durationFromPrevForEachPDValInms
	 * @param durationFromNextForEachPDValInms
	 * @return
	 */
	public static Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> computeRepresentativeActivityObjectForUserPDV2(
			int userID, LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal,
			LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDValInms,
			LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDValInms, String databaseName,
			PrimaryDimension primaryDimension)
	{
		System.out.println("Inside computeRepresentativeActivityObjectForUserPDV2 for userID" + userID);
		LinkedHashMap<Integer, ActivityObject2018> repAOs = new LinkedHashMap<>();
		LinkedHashMap<Integer, Pair<Double, Double>> actMedPreSuccDurationInms = new LinkedHashMap<>();

		System.out.println(
				PerformanceAnalytics.getHeapInformation() + "\n" + PerformanceAnalytics.getHeapPercentageFree());
		try
		{
			if (!databaseName.equals("gowalla1"))
			{
				PopUps.printTracedErrorMsgWithExit("Error: this method is currently only suitable for gowalla dataset");
			}

			// feature of Gowalla activity object used in edit distance and necessary for recommendation.
			for (Integer pdVal : durationFromPrevForEachPDValInms.keySet())// aosForEachPDVal.entrySet())
			{
				double medianDurationFromPrevForEachActNameInms = StatsUtils.getDescriptiveStatisticsLong(
						durationFromPrevForEachPDValInms.get(pdVal), "durationFromPrevForEachActName",
						userID + "__" + pdVal + "durationFromPrevForEachActName.txt", false).getPercentile(50);

				double medianDurationFromNextForEachActNameInms = StatsUtils.getDescriptiveStatisticsLong(
						durationFromNextForEachPDValInms.get(pdVal), "durationFromPrevForEachActName",
						userID + "__" + pdVal + "durationFromPrevForEachActName.txt", false).getPercentile(50);

				// Dummy because we are not going to actually use this, we will instead extract the timestamp from the
				// preceeding duration + timestamp of preceeding activity object. But we assign this median anyway to
				// avoid having it as numm which causes exception.
				Timestamp dummyMedianStartTS = new Timestamp(0);
				// Disable getMedianSecondsSinceMidnightTimestamp for speed as we were not using it anyway.
				// getMedianSecondsSinceMidnightTimestamp(
				// aos.stream().map(ao -> ao.getStartTimestamp()).collect(Collectors.toList()), userID,
				// pdVal.toString());

				// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
				// int medianCinsCount = (int) StatsUtils
				// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
				// .getPercentile(50);

				// NOTE: we only need to take care of feature which are used for edit distance computation.
				// Instantiate the representative activity object.
				int activityID = -1;
				ArrayList<Integer> locationIDs = new ArrayList<>();
				String activityName = "";
				// String locationName = "";
				String workingLevelCatIDs = "";

				switch (primaryDimension)
				{
				case ActivityID:
				{
					activityID = pdVal;
					activityName = String.valueOf(pdVal); // for gowalla dataset, act id and act name are same
					workingLevelCatIDs = pdVal + "__";
					break;
				}
				case LocationID:
				{
					locationIDs.add(pdVal);
					workingLevelCatIDs = pdVal + "__";
					// locationName =
					// DomainConstants.getLocIDLocationObjectDictionary().get(pdVal).getLocationName();
					// if (locationName == null || locationName.length() == 0)
					// { PopUps.printTracedErrorMsg("Error: fetched locationName= " + locationName); }
					break;
				}
				default:
					PopUps.printTracedErrorMsgWithExit(
							"Error: unknown primaryDimension = " + Constant.primaryDimension);
					break;
				}
				// ActivityObject(int activityID, ArrayList<Integer> locationIDs, String activityName, String
				// locationName, Timestamp startTimestamp, String startLatitude, String startLongitude, String
				// startAltitude, String userID, int photos_count, int checkins_count, int users_count, int
				// radius_meters, int highlights_count, int items_count, int max_items_count, String workingLevelCatIDs,
				// double distanceInMFromNext, long durationInSecsFromNext, String[] levelWiseCatIDs)

				ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
						activityName, "", dummyMedianStartTS, "", "", "", String.valueOf(userID), -1, -1, -1, -1, -1,
						-1, -1, workingLevelCatIDs, -1, -1, new String[] { "" });

				repAOs.put(pdVal, repAOForThisActNameForThisUser);
				actMedPreSuccDurationInms.put(pdVal,
						new Pair<>(medianDurationFromPrevForEachActNameInms, medianDurationFromNextForEachActNameInms));

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Exiting computeRepresentativeActivityObjectForUserPDV2 for userID" + userID);
		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOs, actMedPreSuccDurationInms);
	}

	/**
	 * 
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param primaryDimension
	 * @param databaseName
	 * @param collaborativecandidates
	 * @return
	 */
	public static LinkedHashMap<String, RepresentativeAOInfo> buildRepresentativeAOInfosDec2018(
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, PrimaryDimension primaryDimension,
			String databaseName, boolean collaborativecandidates)
	{
		// PopUps.showMessage("Inside buildRepresentativeAOInfosDec2018 trainTimelinesAllUsersContinuousFiltrd.size()="
		// + trainTimelinesAllUsersContinuousFiltrd.size());
		StringBuilder sbLog = new StringBuilder("------ representativeAOInfosForAllUsers ------------------- \n");

		LinkedHashMap<String, RepresentativeAOInfo> representativeAOInfosForAllUsers = new LinkedHashMap<>();

		for (Entry<String, Timeline> userEntry : trainTimelinesAllUsersContinuousFiltrd.entrySet())
		{
			RepresentativeAOInfo repAOInfo = new RepresentativeAOInfo(userEntry.getKey(), primaryDimension,
					trainTimelinesAllUsersContinuousFiltrd, collaborativecandidates);
			representativeAOInfosForAllUsers.put(userEntry.getKey(), repAOInfo);

			sbLog.append(userEntry.getKey() + "--\n" + repAOInfo.toString() + "\n");
		}
		// PopUps.showMessage("Inside buildRepresentativeAOInfosDec2018: representativeAOInfosForAllUsers.size()= "
		// + representativeAOInfosForAllUsers.size() + "keySet = " + representativeAOInfosForAllUsers.keySet());
		return representativeAOInfosForAllUsers;
	}

	/**
	 * @param userId
	 * @param trainTestTimelinesForAllUsers
	 * @param uniqueLocIDs
	 * @param uniqueActivityIDs
	 * @return
	 */
	public static Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> buildRepresentativeAOsForUserPDVCOll(
			int userId, LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			Set<Integer> uniqueLocIDs, Set<Integer> uniqueActivityIDs, String databaseName,
			PrimaryDimension primaryDimension)
	{
		// BookMark
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeAOsForUserPDV2 for user " + userId);
		LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = null;
		LinkedHashMap<Integer, Pair<Double, Double>> actMedianPreSuccDurationInms = null;
		LinkedHashMap<String, Timeline> collTrainingTimelines = new LinkedHashMap<>();

		long t1 = System.currentTimeMillis();
		try
		{
			if (databaseName.equals("gowalla1") == false)
			{
				PopUps.printTracedErrorMsgWithExit("Error: database is  not gowalla1:" + databaseName);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// { System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1); }

			else
			{
				LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal = new LinkedHashMap<>();
				/**
				 * Useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDValInms = new LinkedHashMap<>();
				LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDValInms = new LinkedHashMap<>();

				// earlier version was using all possible vals fro PD but now using only those in training data.
				LinkedHashSet<Integer> distinctPDValsEncounteredInTraining = new LinkedHashSet<>();

				for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsers
						.entrySet())
				{
					int userIdCursor = Integer.valueOf(trainTestForAUser.getKey());
					if (userIdCursor != userId)
					{
						Timeline userTrainingTimeline = TimelineTransformers
								.dayTimelinesToATimeline(trainTestForAUser.getValue().get(0), false, true);

						for (ActivityObject2018 ao : userTrainingTimeline.getActivityObjectsInTimeline())
						{
							distinctPDValsEncounteredInTraining.addAll(ao.getPrimaryDimensionVal());
						}
						collTrainingTimelines.put(trainTestForAUser.getKey(), userTrainingTimeline);
					}
				}

				System.out.println("distinctPDValsEncounteredInCOllTraining.size() = "
						+ distinctPDValsEncounteredInTraining.size());

				// iterate over all possible primary dimension vals to initialise to preserve order.
				for (Integer pdVal : distinctPDValsEncounteredInTraining)
				{
					aosForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromPrevForEachPDValInms.put(pdVal, new ArrayList<>());
					durationFromNextForEachPDValInms.put(pdVal, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				int countAOs = 0;
				for (Entry<String, Timeline> trainingTimelineEntry : collTrainingTimelines.entrySet())
				{
					String userID = trainingTimelineEntry.getKey();

					// long durationFromPreviousInSecs = 0;
					long prevTimestampInms = 0;
					Set<Integer> prevPDValEncountered = null;

					for (ActivityObject2018 ao : trainingTimelineEntry.getValue().getActivityObjectsInTimeline())
					{
						countAOs += 1;
						Set<Integer> uniquePdValsInAO = new LinkedHashSet<>(ao.getPrimaryDimensionVal());
						for (Integer pdVal : uniquePdValsInAO)
						{
							// add this act object to the correct map entry in map of aos for given act names
							ArrayList<ActivityObject2018> aosStored = aosForEachPDVal.get(pdVal);
							if (aosStored == null)
							{
								PopUps.printTracedErrorMsg("Error: encountered pdval '" + pdVal
										+ "' is not in the list of pd vals from training data");
							}
							aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated
						}

						// store the preceeding and succeeding durations
						long currentTimestampInms = ao.getStartTimestamp().getTime();

						if (prevTimestampInms != 0)
						{ // add the preceeding duration for this AO
							for (Integer pdVal : uniquePdValsInAO)
							{
								durationFromPrevForEachPDValInms.get(pdVal)
										.add(currentTimestampInms - prevTimestampInms);
							}
						}

						if (prevPDValEncountered != null)
						{
							// add the succeeding duration for the previous AO
							for (Integer prevPDVal : prevPDValEncountered)
							{
								durationFromNextForEachPDValInms.get(prevPDVal)
										.add(currentTimestampInms - prevTimestampInms);
							}
						}

						prevTimestampInms = currentTimestampInms;
						prevPDValEncountered = uniquePdValsInAO;
					}
				}
				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					// userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(ao -> System.out
					// .print(ao.getPrimaryDimensionVal("/") + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachPDValue:");
					durationFromPrevForEachPDValInms.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachPDValue:");
					durationFromNextForEachPDValInms.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each pd value:");
					aosForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachPDVal.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = countAOs;// userTrainingTimelines.getActivityObjectsInTimeline().stream()
				// .count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// This sanity check below is relevant only for activity id as primary dimension, since it is in that
				// case each activity object has only one primary dimension val (activity id). It won't hold in case of
				// location ids since one activity object can have multiple location ids because of mergers, i.e,
				// sumOfCountOfAOsFroMap>sumOfCountOfAOsFromTimeline
				if (primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				else// (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (sumOfCountOfAOsFroMap < sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPDV2\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> result = computeRepresentativeActivityObjectForUserPDV2(
						userId, aosForEachPDVal, durationFromPrevForEachPDValInms, durationFromNextForEachPDValInms,
						databaseName, primaryDimension);

				repAOsForThisUser = result.getFirst();
				actMedianPreSuccDurationInms = result.getSecond();
			} // end of else
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeAOsForUserPDV2 for user " + userId + " time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOsForThisUser, actMedianPreSuccDurationInms);

	}

	/**
	 * for each user, for each activity (category id), create a representative avg activity object.
	 * <p>
	 * For example,
	 * 
	 * for user 1, for activity ‘Asian Food’, create an Activity Object with activity name as ‘Asian Food’ and other
	 * features are the average values of the features all the ‘Asian Food’ activity objects occurring the training set
	 * of user 1.
	 * 
	 * @param userId
	 * @param userTrainingTimelines
	 * @param userTestTimelines
	 *            only to find which act names occur in test but not in training
	 * @param uniqueLocIDs
	 * @param uniqueActivityIDs
	 * @return
	 */
	public static Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> buildRepresentativeAOsForUserPD(
			int userId, Timeline userTrainingTimelines, Timeline userTestTimelines, Set<Integer> uniqueLocIDs,
			Set<Integer> uniqueActivityIDs, String databaseName, PrimaryDimension primaryDimension)
	{
		// mapOfRepAOs;
		boolean sanityCheck = false;
		System.out.println("Inside buildRepresentativeAOsForUserPD for user " + userId);
		LinkedHashMap<Integer, ActivityObject2018> repAOsForThisUser = null;
		LinkedHashMap<Integer, Pair<Double, Double>> actMedianPreSuccDuration = null;

		long t1 = System.currentTimeMillis();
		try
		{
			if (databaseName.equals("gowalla1") == false)
			{
				PopUps.printTracedErrorMsgWithExit("Error: database is  not gowalla1:" + databaseName);
			}
			// if (mapOfRepAOs.containsKey(userId)) // USEFUL when we keep mapOfRepAOs as class variable
			// { System.err.println("Error: the user is already in mapOfRepAOs, this shouldn't have happened");
			// System.exit(-1); }

			else
			{
				LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal = new LinkedHashMap<>();
				/**
				 * Useful for deciding upon the start timestamp from representative AO
				 */
				LinkedHashMap<Integer, ArrayList<Long>> durationFromPrevForEachPDVal = new LinkedHashMap<>();
				LinkedHashMap<Integer, ArrayList<Long>> durationFromNextForEachPDVal = new LinkedHashMap<>();

				Set<Integer> allPossiblePDVals = null;
				if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					allPossiblePDVals = uniqueActivityIDs;// uniqueLocIDs,Set<Integer>
				}
				else if (Constant.primaryDimension.equals(PrimaryDimension.LocationID))
				{
					allPossiblePDVals = uniqueLocIDs;
				}
				else
				{
					PopUps.printTracedErrorMsgWithExit("Error: unknown primary dimension");
				}

				// iterate over all possible primary dimension vals to initialise to preserve order.
				for (Integer pdVal : allPossiblePDVals)
				{
					aosForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromPrevForEachPDVal.put(pdVal, new ArrayList<>());
					durationFromNextForEachPDVal.put(pdVal, new ArrayList<>());
				}

				// populate the map for list of aos for each act name
				// long durationFromPreviousInSecs = 0;
				long prevTimestamp = 0;
				Set<Integer> prevPDValEncountered = null;
				LinkedHashSet<Integer> distinctPDValsEncounteredInTraining = new LinkedHashSet<>();

				for (ActivityObject2018 ao : userTrainingTimelines.getActivityObjectsInTimeline())
				{
					Set<Integer> uniquePdValsInAO = new LinkedHashSet<>(ao.getPrimaryDimensionVal());
					distinctPDValsEncounteredInTraining.addAll(ao.getPrimaryDimensionVal());

					for (Integer pdVal : uniquePdValsInAO)
					{
						// add this act object to the correct map entry in map of aos for given act names
						ArrayList<ActivityObject2018> aosStored = aosForEachPDVal.get(pdVal);
						if (aosStored == null)
						{
							PopUps.printTracedErrorMsg("Error: encountered pdval '" + pdVal
									+ "' is not in the list of all possible pd vals");
						}
						aosStored.add(ao); // add the new AO encountered to the map's list. So map is updated
					}

					// store the preceeding and succeeding durations
					long currentTimestamp = ao.getStartTimestamp().getTime();

					if (prevTimestamp != 0)
					{ // add the preceeding duration for this AO
						for (Integer pdVal : uniquePdValsInAO)
						{
							durationFromPrevForEachPDVal.get(pdVal).add(currentTimestamp - prevTimestamp);
						}
					}

					if (prevPDValEncountered != null)
					{
						// add the succeeding duration for the previous AO
						for (Integer prevPDVal : prevPDValEncountered)
						{
							durationFromNextForEachPDVal.get(prevPDVal).add(currentTimestamp - prevTimestamp);
						}
					}

					prevTimestamp = currentTimestamp;
					prevPDValEncountered = uniquePdValsInAO;
				}

				/////////////

				// $$ analyseActNameNotInTraining(userId, allPossibleActivityNames,
				// distinctActNamesEncounteredInTraining,
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/" + "ActNamesNotInTraining.csv",
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/"
				// + "ActNamesInTestButNotInTraining.csv",
				// userTestTimelines);

				/////////////

				////////////////////////// sanity check
				if (sanityCheck)
				{
					System.out.println("Timeline of AOs");

					userTrainingTimelines.getActivityObjectsInTimeline().stream().forEachOrdered(ao -> System.out
							.print(ao.getPrimaryDimensionVal("/") + "-" + ao.getStartTimestampInms() + ">>"));

					System.out.println("durationFromPrevForEachPDValue:");
					durationFromPrevForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));

					System.out.println("durationFromNextForEachPDValue:");
					durationFromNextForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().toString()));
					// SANITY CHECK OK for durationFromPrevForEachActName durationFromNextForEachActName

					////////////////////////// sanity check
					System.out.println("Count of aos for each pd value:");
					aosForEachPDVal.entrySet().stream()
							.forEach(e -> System.out.println(e.getKey() + "--" + e.getValue().size()));
				}

				long sumOfCountOfAOsFroMap = aosForEachPDVal.entrySet().stream().flatMap(e -> e.getValue().stream())
						.count();
				long sumOfCountOfAOsFromTimeline = userTrainingTimelines.getActivityObjectsInTimeline().stream()
						.count();

				// .map(e -> e.getValue().getActivityObjectsInTimeline().size()).count();
				System.out.println("sumOfCountOfAOsFroMap= " + sumOfCountOfAOsFroMap);
				System.out.println("sumOfCountOfAOsFromTimeline= " + sumOfCountOfAOsFromTimeline);

				// This sanity check below is relevant only for activity id as primary dimension, since it is in that
				// case each activity object has only one primary dimension val (activity id). It won't hold in case of
				// location ids since one activity object can have multiple location ids because of mergers, i.e,
				// sumOfCountOfAOsFroMap>sumOfCountOfAOsFromTimeline
				if (primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (sumOfCountOfAOsFroMap != sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPD\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				else// (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (sumOfCountOfAOsFroMap < sumOfCountOfAOsFromTimeline)
					{
						PopUps.printTracedErrorMsg(
								"Sanity check failed in buildRepresentativeAOsForUserPD\n(sumOfCountOfAOsFroMap) != "
										+ sumOfCountOfAOsFroMap + " (sumOfCountOfAOsFromTimeline)"
										+ sumOfCountOfAOsFromTimeline);
					}
				}
				// SANITY CHECK OK for daosForEachActName

				////////////////////////// sanity check end

				Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> result = computeRepresentativeActivityObjectForUserPDV2(
						userId, aosForEachPDVal, durationFromPrevForEachPDVal, durationFromNextForEachPDVal,
						databaseName, primaryDimension);

				repAOsForThisUser = result.getFirst();
				actMedianPreSuccDuration = result.getSecond();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Exiting buildRepresentativeAOsForUserPD for user " + userId + " time taken = "
				+ ((t2 - t1) * 1.0 / 1000) + "secs");

		return new Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>(
				repAOsForThisUser, actMedianPreSuccDuration);
	}

	/**
	 * 
	 * @param topRecommActName
	 * @param mapOfRepAOs
	 * @param mapOfMedianPreSuccDuration
	 *            SHOULD BE IN MILLI SECONDS
	 * @param userId
	 * @param recommendationTime
	 * @return
	 */
	public static ActivityObject2018 getRepresentativeAO(String topRecommActName,
			LinkedHashMap<Integer, LinkedHashMap<String, ActivityObject2018>> mapOfRepAOs,
			LinkedHashMap<Integer, LinkedHashMap<String, Pair<Double, Double>>> mapOfMedianPreSuccDuration, int userId,
			Timestamp recommendationTime)
	{
		ActivityObject2018 repAO = mapOfRepAOs.get(userId).get(topRecommActName);
		if (repAO == null)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in getRepresentativeAO: topRecommActName:" + topRecommActName + " not found in mapOfRepAo"));
			System.exit(-1);
		}

		double medianPreceedingDuration = mapOfMedianPreSuccDuration.get(userId).get(topRecommActName).getFirst();
		if (medianPreceedingDuration <= 0)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in getRepresentativeAO: medianPreceedingDuration = " + medianPreceedingDuration));
			System.exit(-1);
		}

		long recommTime = recommendationTime.getTime();

		Timestamp newRecommTimestamp = new Timestamp((long) (recommTime + medianPreceedingDuration));

		if (!DateTimeUtils.isSameDate(recommendationTime, newRecommTimestamp))
		{
			System.err.println("recommendationTime = " + recommendationTime + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = " + medianPreceedingDuration
					+ " for topRecommActName =" + topRecommActName);
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug: getRepresentativeAO: old recommendationTime="
					+ recommendationTime.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDuration + "  new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		repAO.setEndTimestamp(newRecommTimestamp); // only end timestamp used, make sure there is no start timestamp,
		// since we are making recommendation at end timestamps and gowalla act objs are points in time.
		repAO.setStartTimestamp(newRecommTimestamp);

		// {
		// System.err.println("Debug1357: inside getRepresentativeAO: recommendationTime = " + recommendationTime
		// + " newRecommTimestamp= " + newRecommTimestamp + ". medianPreceedingDuration = "
		// + medianPreceedingDuration + " for topRecommActName =" + topRecommActName);
		// System.out.println("repAO = " + repAO.toStringAllGowallaTS());
		// }

		return repAO;
	}

	// /**
	// *
	// * Created to create rep AO from repAOResultGenericUser which has been created using training timelines of all
	// * users. (need to call only once for a test)
	// *
	// * <p>
	// * Fork of getRepresentativeAOCollGeneric. Instead of recreating a new ActivityObject, it just fetches the generic
	// * rep AO for this PD Val and modifies the relevant features accordingly.
	// *
	// * @param topPrimaryDimensionVal
	// * @param userId
	// * @param recommendationTime
	// * @param primaryDimension
	// * @param repAOResultGenericUser
	// * {ActID,RepAO}, {ActID,{medianDurFromPrevForEachActName, medianDurFromNextForEachActName}}
	// * @return
	// * @since 8 Feb 2018
	// */
	// public static ActivityObject2018 getRepresentativeAOCollGenericV2(Integer topPrimaryDimensionVal, int userId,
	// Timestamp recommendationTime, PrimaryDimension primaryDimension,
	// Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>>
	// repAOResultGenericUser)
	// {
	// if (Constant.getDatabaseName().equals("gowalla1") == false)
	// {
	// PopUps.printTracedErrorMsgWithExit(
	// "Error: not implemented this (getRepresentativeAOCollGenericV2() for besides gowalla1");
	// }
	// // StringBuilder verboseMsg = new StringBuilder();
	// // System.out.println("Inside getRepresentativeAOColl(): topPrimaryDimensionVal=" + topPrimaryDimensionVal +
	// // "\n");
	// // ArrayList<Long> durationPreceeding = new ArrayList<>();
	// // ArrayList<ActivityObject> aosWithSamePDVal = new ArrayList<>();
	//
	// // StringBuilder sb = new StringBuilder();
	// // System.out.println("durationPreceeding= " + durationPreceeding.toString());
	// // System.out.println("aosWithSamePDVal= ");
	// // aosWithSamePDVal.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
	// // System.out.println(sb.toString());
	//
	// double medianPreceedingDuration = repAOResultGenericUser.getSecond().get(topPrimaryDimensionVal).getFirst();
	//
	// // double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
	// // int medianCinsCount = (int) StatsUtils
	// // .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
	// // .getPercentile(50);
	//
	// // NOTE: we only need to take care of feature which are used for edit distance computation.
	// // Instantiate the representative activity object.
	//
	// Timestamp newRecommTimestamp = new Timestamp((long) (recommendationTime.getTime() + medianPreceedingDuration));
	//
	// ActivityObject2018 repAOForThisActNameForThisUser = repAOResultGenericUser.getFirst()
	// .get(topPrimaryDimensionVal);
	//
	// repAOForThisActNameForThisUser.setStartTimestamp(newRecommTimestamp);
	// repAOForThisActNameForThisUser.setEndTimestamp(newRecommTimestamp);
	// repAOForThisActNameForThisUser.setUserID(String.valueOf(userId));
	//
	// if (!DateTimeUtils.isSameDate(recommendationTime, newRecommTimestamp))
	// {
	// System.out.print("Warning: recommendationTime = " + recommendationTime + " newRecommTimestamp= "
	// + newRecommTimestamp + " are not same day. medianPreceedingDuration = " + medianPreceedingDuration
	// + " for topRecommActName =" + topPrimaryDimensionVal);
	// }
	// //
	// if (VerbosityConstants.verbose)
	// {
	// System.out.println("Debug getRepresentativeAOCollGeneric: getRepresentativeAO: old recommendationTime="
	// + recommendationTime.toLocalDateTime().toString() + "medianPreceedingDuration="
	// + medianPreceedingDuration + " new recommendationTime="
	// + newRecommTimestamp.toLocalDateTime().toString());
	// }
	// System.out.println("repAOV2=" + repAOForThisActNameForThisUser.toStringAllGowallaTS());
	//
	// return repAOForThisActNameForThisUser;
	// }

	/**
	 * 
	 * Created to create rep AO from repAOResultGenericUser which has been created using training timelines of all
	 * users. (need to call only once for a test)
	 * 
	 * <p>
	 * Fork of getRepresentativeAOCollGeneric. Instead of recreating a new ActivityObject, it just fetches the generic
	 * rep AO for this PD Val and modifies the relevant features accordingly.
	 * 
	 * @param topPrimaryDimensionVal
	 * @param userId
	 * @param recommendationTime
	 * @param primaryDimension
	 * @param repAOResultGenericUser
	 *            {ActID,RepAO}, {ActID,{medianDurFromPrevForEachActName, medianDurFromNextForEachActName}}
	 * @return
	 * @since 8 Feb 2018
	 */
	public static ActivityObject2018 getRepresentativeAOCollGenericV2(Integer topPrimaryDimensionVal, int userId,
			Timestamp recommendationTime, PrimaryDimension primaryDimension,
			Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResultGenericUser)
	{
		if (Constant.getDatabaseName().equals("gowalla1") == false)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: not implemented this (getRepresentativeAOCollGenericV2() for besides gowalla1");
		}
		// StringBuilder verboseMsg = new StringBuilder();
		// System.out.println("Inside getRepresentativeAOColl(): topPrimaryDimensionVal=" + topPrimaryDimensionVal +
		// "\n");
		// ArrayList<Long> durationPreceeding = new ArrayList<>();
		// ArrayList<ActivityObject> aosWithSamePDVal = new ArrayList<>();

		// StringBuilder sb = new StringBuilder();
		// System.out.println("durationPreceeding= " + durationPreceeding.toString());
		// System.out.println("aosWithSamePDVal= ");
		// aosWithSamePDVal.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
		// System.out.println(sb.toString());

		// changed from double to Double on 6 Aug 2018
		Double medianPreceedingDuration = repAOResultGenericUser.getSecond().get(topPrimaryDimensionVal).getFirst();
		if (medianPreceedingDuration == null)
		{
			PopUps.printTracedErrorMsg(
					"Error in getRepresentativeAOCollGenericV2(): medianPreceedingDuration not available for topPrimaryDimensionVal="
							+ topPrimaryDimensionVal);
		}

		// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
		// int medianCinsCount = (int) StatsUtils
		// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
		// .getPercentile(50);

		// NOTE: we only need to take care of feature which are used for edit distance computation.
		// Instantiate the representative activity object.

		Timestamp newRecommTimestamp = new Timestamp((long) (recommendationTime.getTime() + medianPreceedingDuration));

		ActivityObject2018 repAOForThisActNameForThisUser = repAOResultGenericUser.getFirst()
				.get(topPrimaryDimensionVal);

		repAOForThisActNameForThisUser.setStartTimestamp(newRecommTimestamp);
		repAOForThisActNameForThisUser.setEndTimestamp(newRecommTimestamp);
		repAOForThisActNameForThisUser.setUserID(String.valueOf(userId));

		if (!DateTimeUtils.isSameDate(recommendationTime, newRecommTimestamp))
		{
			System.out.print("Warning: recommendationTime = " + recommendationTime + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = " + medianPreceedingDuration
					+ " for topRecommActName =" + topPrimaryDimensionVal);
		}
		//
		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug getRepresentativeAOCollGeneric: getRepresentativeAO: old recommendationTime="
					+ recommendationTime.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDuration + " new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		System.out.println("repAOV2=" + repAOForThisActNameForThisUser.toStringAllGowallaTS());

		return repAOForThisActNameForThisUser;
	}

	//
	/**
	 * Created to create rep AO from repAOResultGenericUser which has been created using training timelines of all
	 * users. (need to call only once for a test)
	 * 
	 * 
	 * @param topPrimaryDimensionVal
	 * @param userId
	 * @param recommendationTimestamp
	 * @param primaryDimension
	 * @param repAOResultGenericUser
	 *            {ActID,RepAO}, {ActID,{medianDurFromPrevForEachActName, medianDurFromNextForEachActName}}
	 * @return
	 */
	public static ActivityObject2018 getRepresentativeAOCollGeneric(Integer topPrimaryDimensionVal, int userId,
			Timestamp recommendationTimestamp, PrimaryDimension primaryDimension,
			Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResultGenericUser)
	{
		// StringBuilder verboseMsg = new StringBuilder();
		// System.out.println("Inside getRepresentativeAOColl(): topPrimaryDimensionVal=" + topPrimaryDimensionVal +
		// "\n");

		ArrayList<Long> durationPreceeding = new ArrayList<>();
		ArrayList<ActivityObject2018> aosWithSamePDVal = new ArrayList<>();

		// StringBuilder sb = new StringBuilder();
		// System.out.println("durationPreceeding= " + durationPreceeding.toString());
		// System.out.println("aosWithSamePDVal= ");
		// aosWithSamePDVal.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
		// System.out.println(sb.toString());

		double medianPreceedingDuration = repAOResultGenericUser.getSecond().get(topPrimaryDimensionVal).getFirst();

		// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
		// int medianCinsCount = (int) StatsUtils
		// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
		// .getPercentile(50);

		// NOTE: we only need to take care of feature which are used for edit distance computation.
		// Instantiate the representative activity object.
		int activityID = -1;
		ArrayList<Integer> locationIDs = new ArrayList<>();
		String activityName = "";
		// String locationName = "";
		String workingLevelCatIDs = "";

		switch (primaryDimension)
		{
		case ActivityID:
		{
			activityID = topPrimaryDimensionVal;
			if (Constant.getDatabaseName().equals("gowalla1"))
			{// for gowalla dataset, act id and act name are same
				activityName = String.valueOf(topPrimaryDimensionVal);
				workingLevelCatIDs = topPrimaryDimensionVal + "__";
			}
			else
			{
				PopUps.printTracedErrorMsgWithExit("Error: not implemented this for besides gowalla1");
			}

			break;
		}
		case LocationID:
		{
			locationIDs.add(topPrimaryDimensionVal);
			workingLevelCatIDs = topPrimaryDimensionVal + "__";
			// locationName =
			// DomainConstants.getLocIDLocationObjectDictionary().get(pdVal).getLocationName();
			// if (locationName == null || locationName.length() == 0)
			// { PopUps.printTracedErrorMsg("Error: fetched locationName= " + locationName); }
			break;
		}
		default:
			PopUps.printTracedErrorMsgWithExit("Error: unknown primaryDimension = " + Constant.primaryDimension);
			break;
		}

		Timestamp newRecommTimestamp = new Timestamp(
				(long) (recommendationTimestamp.getTime() + medianPreceedingDuration));

		ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
				activityName, "", newRecommTimestamp, "", "", "", String.valueOf(userId), -1, -1, -1, -1, -1, -1, -1,
				workingLevelCatIDs, -1, -1, new String[] { "" });

		if (!DateTimeUtils.isSameDate(recommendationTimestamp, newRecommTimestamp))
		{
			System.out.print("Warning: recommendationTime = " + recommendationTimestamp + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = " + medianPreceedingDuration
					+ " for topRecommActName =" + topPrimaryDimensionVal);
		}
		//
		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug getRepresentativeAOCollGeneric: getRepresentativeAO: old recommendationTime="
					+ recommendationTimestamp.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDuration + " new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		System.out.println("repAO=" + repAOForThisActNameForThisUser.toStringAllGowallaTS());

		return repAOForThisActNameForThisUser;
	}

	//
	/**
	 * Created to compute just-in-time rep AO when only one cand timeline (most recent one) is taken from other users.
	 * Otherwise, if there are more cands, then probably precomputation is better than just in time as in here.
	 * 
	 * @param topPrimaryDimensionVal
	 * @param userId
	 * @param recommendationTimestamp
	 * @param primaryDimension
	 * @param recommMaster
	 * @return
	 * @since 1 August 2017
	 */
	public static ActivityObject2018 getRepresentativeAOColl(Integer topPrimaryDimensionVal, int userId,
			Timestamp recommendationTimestamp, PrimaryDimension primaryDimension, RecommendationMasterI recommMaster)
	{
		// StringBuilder verboseMsg = new StringBuilder();
		// System.out.println("Inside getRepresentativeAOColl(): topPrimaryDimensionVal=" + topPrimaryDimensionVal +
		// "\n");

		ArrayList<Long> durationPreceedingInms = new ArrayList<>();
		ArrayList<ActivityObject2018> aosWithSamePDVal = new ArrayList<>();

		// iterate over the candidate timelines (which are to be used to create representative activity objects)
		// System.out.println("#cands = " + recommMaster.getCandidateTimelineIDs().size());
		for (String candID : recommMaster.getCandidateTimelineIDs())
		{
			TimelineWithNext candTimeline = (TimelineWithNext) recommMaster.getCandidateTimeline(candID);
			// candTimeline.printActivityObjectNamesWithTimestampsInSequence();
			// System.out.println("next act=" + candTimeline.getNextActivityObject().toStringAllGowallaTS());
			ArrayList<ActivityObject2018> actObjs = candTimeline.getActivityObjectsInTimeline();
			actObjs.add(candTimeline.getNextActivityObject());

			// note cand will contain MU+1 acts, +1 next act
			// not always true, sometimes cand will have less aos
			// $$Sanity.eq(actObjs.size(), Constant.getCurrentMatchingUnit() + 2, "actObjs.size():" + actObjs.size()
			// $$ + "!= Constant.getCurrentMatchingUnit():" + Constant.getCurrentMatchingUnit());

			// System.out.println("actObjs.size():" + actObjs.size() + "!= Constant.getCurrentMatchingUnit():"
			// + Constant.getCurrentMatchingUnit());

			long prevTSInms = -99, currentTSInms = -99;
			for (ActivityObject2018 ao : actObjs)
			{
				currentTSInms = ao.getStartTimestamp().getTime();

				// contains the act name we are looking for
				if (ao.getPrimaryDimensionVal().contains(topPrimaryDimensionVal))
				{
					aosWithSamePDVal.add(ao);

					if (prevTSInms > 0)
					{
						durationPreceedingInms.add(currentTSInms - prevTSInms);
					}
				}
				prevTSInms = currentTSInms;
			}
			// System.out.println("");
		}

		// StringBuilder sb = new StringBuilder();
		// System.out.println("durationPreceeding= " + durationPreceeding.toString());
		// System.out.println("aosWithSamePDVal= ");
		// aosWithSamePDVal.stream().forEachOrdered(ao -> sb.append(ao.toStringAllGowallaTS() + ">>"));
		// System.out.println(sb.toString());

		double medianPreceedingDurationInms = StatsUtils.getDescriptiveStatisticsLong(durationPreceedingInms,
				"durationPreceeding", userId + "__" + topPrimaryDimensionVal + "durationPreceeding.txt", false)
				.getPercentile(50);

		// double[] cinsCount = aos.stream().mapToDouble(ao -> ao.getCheckins_count()).toArray();
		// int medianCinsCount = (int) StatsUtils
		// .getDescriptiveStatistics(cinsCount, "cinsCount", userID + "__" + actName + "cinsCount.txt")
		// .getPercentile(50);

		// NOTE: we only need to take care of feature which are used for edit distance computation.
		// Instantiate the representative activity object.
		int activityID = -1;
		ArrayList<Integer> locationIDs = new ArrayList<>();
		String activityName = "";
		// String locationName = "";
		String workingLevelCatIDs = "";

		switch (primaryDimension)
		{
		case ActivityID:
		{
			activityID = topPrimaryDimensionVal;
			if (Constant.getDatabaseName().equals("gowalla1"))
			{// for gowalla dataset, act id and act name are same
				activityName = String.valueOf(topPrimaryDimensionVal);
				workingLevelCatIDs = topPrimaryDimensionVal + "__";
			}
			else
			{
				PopUps.printTracedErrorMsgWithExit("Error: not implemented this for besides gowalla1");
			}

			break;
		}
		case LocationID:
		{
			locationIDs.add(topPrimaryDimensionVal);
			workingLevelCatIDs = topPrimaryDimensionVal + "__";
			// locationName =
			// DomainConstants.getLocIDLocationObjectDictionary().get(pdVal).getLocationName();
			// if (locationName == null || locationName.length() == 0)
			// { PopUps.printTracedErrorMsg("Error: fetched locationName= " + locationName); }
			break;
		}
		default:
			PopUps.printTracedErrorMsgWithExit("Error: unknown primaryDimension = " + Constant.primaryDimension);
			break;
		}

		Timestamp newRecommTimestamp = new Timestamp(
				(long) (recommendationTimestamp.getTime() + medianPreceedingDurationInms));

		ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
				activityName, "", newRecommTimestamp, "", "", "", String.valueOf(userId), -1, -1, -1, -1, -1, -1, -1,
				workingLevelCatIDs, -1, -1, new String[] { "" });

		if (!DateTimeUtils.isSameDate(recommendationTimestamp, newRecommTimestamp))
		{
			System.out.print("Warning: recommendationTime = " + recommendationTimestamp + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = "
					+ medianPreceedingDurationInms + " for topRecommActName =" + topPrimaryDimensionVal);
		}
		//
		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug: getRepresentativeAO: old recommendationTime="
					+ recommendationTimestamp.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDurationInms + " new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		// System.out.println("repAO=" + repAOForThisActNameForThisUser.toStringAllGowallaTS());

		return repAOForThisActNameForThisUser;
	}
	//

	//
	/**
	 * 
	 * @param topPrimaryDimensionVal
	 * @param mapOfRepAOs
	 * @param mapOfMedianPreSuccDurationInms
	 * @param userId
	 * @param recommendationTimestamp
	 * @param primaryDimension
	 * @return
	 * @since 14 July 2017
	 */
	public static ActivityObject2018 getRepresentativeAO(Integer topPrimaryDimensionVal,
			LinkedHashMap<Integer, LinkedHashMap<Integer, ActivityObject2018>> mapOfRepAOs,
			LinkedHashMap<Integer, LinkedHashMap<Integer, Pair<Double, Double>>> mapOfMedianPreSuccDurationInms,
			int userId, Timestamp recommendationTimestamp, PrimaryDimension primaryDimension)
	{
		ActivityObject2018 repAO = mapOfRepAOs.get(userId).get(topPrimaryDimensionVal);
		if (repAO == null)
		{
			PopUps.printTracedErrorMsgWithExit("Error in getRepresentativeAO: topRecommActName:"
					+ topPrimaryDimensionVal + " not found in mapOfRepAo");
		}

		double medianPreceedingDurationInms = mapOfMedianPreSuccDurationInms.get(userId).get(topPrimaryDimensionVal)
				.getFirst();
		if (medianPreceedingDurationInms <= 0)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error in getRepresentativeAO: medianPreceedingDuration = " + medianPreceedingDurationInms);
		}

		long recommTimeInms = recommendationTimestamp.getTime();

		Timestamp newRecommTimestamp = new Timestamp((long) (recommTimeInms + medianPreceedingDurationInms));

		if (!DateTimeUtils.isSameDate(recommendationTimestamp, newRecommTimestamp))
		{
			System.err.println("recommendationTime = " + recommendationTimestamp + " newRecommTimestamp= "
					+ newRecommTimestamp + " are not same day. medianPreceedingDuration = "
					+ medianPreceedingDurationInms + " for topRecommActName =" + topPrimaryDimensionVal);
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug: getRepresentativeAO: old recommendationTime="
					+ recommendationTimestamp.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ medianPreceedingDurationInms + "  new recommendationTime="
					+ newRecommTimestamp.toLocalDateTime().toString());
		}
		repAO.setEndTimestamp(newRecommTimestamp); // only end timestamp used, make sure there is no start timestamp,
		// since we are making recommendation at end timestamps and gowalla act objs are points in time.
		repAO.setStartTimestamp(newRecommTimestamp);

		// {
		// System.err.println("Debug1357: inside getRepresentativeAO: recommendationTime = " + recommendationTime
		// + " newRecommTimestamp= " + newRecommTimestamp + ". medianPreceedingDuration = "
		// + medianPreceedingDuration + " for topRecommActName =" + topRecommActName);
		// System.out.println("repAO = " + repAO.toStringAllGowallaTS());
		// }

		return repAO;
	}

	/**
	 * 
	 * @param preBuildRepAOGenericUser
	 * @param collaborativeCandidates
	 * @param buildRepAOJustInTime
	 * @param mapOfRepAOs
	 * @param mapOfMedianPreSuccDurationInms
	 * @param repAOResultGenericUser
	 * @param userId
	 * @param recommendationTimestamp
	 * @param topRecommendedPrimarDimensionVal
	 * @param recommMaster
	 * @param primaryDimension
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 *            added on 20 Dec 2018
	 * @return
	 */
	public static ActivityObject2018 getRepresentativeAOForActName(boolean preBuildRepAOGenericUser,
			boolean collaborativeCandidates, boolean buildRepAOJustInTime,
			LinkedHashMap<Integer, LinkedHashMap<Integer, ActivityObject2018>> mapOfRepAOs,
			LinkedHashMap<Integer, LinkedHashMap<Integer, Pair<Double, Double>>> mapOfMedianPreSuccDurationInms,
			Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResultGenericUser,
			int userId, Timestamp recommendationTimestamp, String topRecommendedPrimarDimensionVal,
			RecommendationMasterI recommMaster, PrimaryDimension primaryDimension,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, String commonPath)
	{
		ActivityObject2018 repAOForTopRecommActName = null;

		if (preBuildRepAOGenericUser)
		{
			// disabled on 9 Feb 2018 start
			// repAOForTopRecommActName =
			// getRepresentativeAOCollGeneric(Integer.valueOf(topRecommendedPrimarDimensionVal),
			// userId, recommendationTime, primaryDimension, repAOResultGenericUser);
			// disabled on 9 Feb 2018 end

			repAOForTopRecommActName = RepresentativeAOInfo.getRepresentativeAOCollGenericV2(
					Integer.valueOf(topRecommendedPrimarDimensionVal), userId, recommendationTimestamp,
					primaryDimension, repAOResultGenericUser);

			// Sanity check Feb8 Starts
			if (false)
			{
				ActivityObject2018 aoTemp = RepresentativeAOInfo.getRepresentativeAOCollGenericV2(
						Integer.valueOf(topRecommendedPrimarDimensionVal), userId, recommendationTimestamp,
						primaryDimension, repAOResultGenericUser);
				WToFile.appendLineToFileAbs(
						repAOForTopRecommActName.toStringAllGowallaTS() + "\n" + aoTemp.toStringAllGowallaTS() + "\n\n",
						commonPath + "getRepresentativeAOCollGenericV2SanityCheck.txt");
				// SANITY CHECK PASSED on Feb 9 2018
			}
			// Sanity check Feb8 Ends
		}
		else
		{
			if (collaborativeCandidates && buildRepAOJustInTime)
			{
				// for just-in-time computation of representative activity object
				repAOForTopRecommActName = RepresentativeAOInfo.getRepresentativeAOColl(
						Integer.valueOf(topRecommendedPrimarDimensionVal), userId, recommendationTimestamp,
						primaryDimension, recommMaster);
			}
			else
			{
				if (Constant.getDatabaseName().equals("gowalla1"))
				{
					repAOForTopRecommActName = RepresentativeAOInfo.getRepresentativeAO(
							Integer.valueOf(topRecommendedPrimarDimensionVal), mapOfRepAOs,
							mapOfMedianPreSuccDurationInms, userId, recommendationTimestamp, primaryDimension);
				}
				else
				{
					repAOForTopRecommActName = null;
					// TODO here RecommendationTestsUtils.getRepresentativeAODec2018(
					// Integer.valueOf(topRecommendedPrimarDimensionVal), mapOfRepAOs,
					// mapOfMedianPreSuccDurationInms, userId, recommendationTimestamp, primaryDimension,
					// trainTimelinesAllUsersContinuousFiltrd, collaborativeCandidates);

				}
				// also use this for collaborative approach when using prebuilt
				// representative AOs created from training timelines of other users.
			}
		}

		// added on 27 Feb 2018
		repAOForTopRecommActName.setTimeZoneId(recommMaster.getActivityObjectAtRecomm().getTimeZoneId());

		return repAOForTopRecommActName;
	}

}
