package org.activity.evaluation;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.LeakyBucket;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.objects.Triple;
import org.activity.recomm.RecommendationMasterI;
import org.activity.sanityChecks.Sanity;
import org.activity.spatial.SpatialUtils;
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
	// added on 21 March 2019
	// actID, List of Triples(firstlocID, Pair(Pair(StartLat,StartLon),Pair(EndLat,EndLon)), AvgAlt)
	// Map<Integer, ArrayList<Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String>>>
	// aggStartLatLongEndLatLongAvgAlt;
	// actID, List of [firstlocID, StartLat,StartLon,EndLat,EndLon, AvgAlt,GridIndex]
	Map<Integer, ArrayList<String[]>> aggStartLatLongEndLatLongAvgAltGridIndxFaster;
	static final boolean useAggStartLatLongEndLatLongAvgAlt = true; // to disable aggStartLatLongEndLatLong_ to save
																	// memory space.

	// disabled temporarily on 22 Mar 2019 as not used and takes up significant space
	Map<Integer, ArrayList<Pair<Long, Long>>> aggStartLatLongEndLatLongAsHSC;
	static final boolean disableAggStartLatLongEndLatLongAsHSC = true;
	Map<Integer, Double> aggValOfEachFeatForEachPDValDuration;
	Map<Integer, Double> aggValOfEachFeatForEachPDValDistTrav;
	Map<Integer, Double> aggValOfEachFeatForEachPDValAvgAlt;
	Map<Integer, Double> aggValOfEachFeatForEachPDValDistFromPrev;
	Map<Integer, Double> aggValOfEachFeatForEachPDValPopularity;
	Map<Integer, Integer> aggValOfEachFeatForEachPDValLocationID;// = new HashMap<>();
	Map<Integer, Integer> aggValOfEachFeatForEachPDValActivityID;// = new HashMap<>();
	String userId;

	@Override
	public String toString()
	{
		return "RepresentativeAOInfo [\naggValOfEachFeatForEachPDValDurationInSecsFromPrev="
				+ aggValOfEachFeatForEachPDValDurationInSecsFromPrev
				+ ", \naggValOfEachFeatForEachPDValDurationFromNext=" + aggValOfEachFeatForEachPDValDurationFromNext
				+ ", \naggStartLatLongEndLatLong_=" + aggStartLatLongEndLatLong_
				// + ", \naggStartLatLongEndLatLongAvgAlt=" + aggStartLatLongEndLatLongAvgAlt
				+ ", \naggStartLatLongEndLatLongAvgAltGridIndexFaster="
				+ aggStartLatLongEndLatLongAvgAltGridIndxFaster.entrySet().stream()
						.map(e -> e.getKey() + "->"
								+ e.getValue().stream().map(v -> Arrays.asList(v).toString())
										.collect(Collectors.joining(",")))
						.collect(Collectors.joining("\n"))
				+ ", \naggStartLatLongEndLatLongAsHSC=" + aggStartLatLongEndLatLongAsHSC
				+ ", \naggValOfEachFeatForEachPDValDuration=" + aggValOfEachFeatForEachPDValDuration
				+ ", \naggValOfEachFeatForEachPDValDistTrav=" + aggValOfEachFeatForEachPDValDistTrav
				+ ", \naggValOfEachFeatForEachPDValAvgAlt=" + aggValOfEachFeatForEachPDValAvgAlt
				+ ", \naggValOfEachFeatForEachPDValDistFromPrev=" + aggValOfEachFeatForEachPDValDistFromPrev
				+ ", \naggValOfEachFeatForEachPDValPopularity=" + aggValOfEachFeatForEachPDValPopularity
				+ ", \naggValOfEachFeatForEachPDValLocationID=" + aggValOfEachFeatForEachPDValLocationID
				+ ", \naggValOfEachFeatForEachPDValActivityID=" + aggValOfEachFeatForEachPDValActivityID + ", userId="
				+ userId + "\n]";
	}

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
		System.out.println(
				"Inside getRepresentativeAOForActName : for topPrimaryDimensionValInt = " + topPrimaryDimensionValInt);
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
			// ArrayList<Integer> locID = new ArrayList<>();//disabled on 4 April 2019
			if (aggValOfEachFeatForEachPDValLocationID.size() > 0) // added on 5 April since geolife wont have locID
			{
				locationIDs.add(aggValOfEachFeatForEachPDValLocationID.get(topPrimaryDimensionValInt));
			}
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

		double aggValOdDurationInSecsFromPrevForThisPDVal = aggValOfEachFeatForEachPDValDurationInSecsFromPrev
				.get(topPrimaryDimensionValInt);
		if (aggValOdDurationInSecsFromPrevForThisPDVal < 1)
		{
			aggValOdDurationInSecsFromPrevForThisPDVal += 1;
		}
		// renamed from newRecommTimestampInMs on 28 Mar 2019
		long newStartTimestampInMs = recommTimestamp.getTime()
				+ (long) (1000 * aggValOdDurationInSecsFromPrevForThisPDVal);
		// aggValOfEachFeatForEachPDValDurationInSecsFromPrev.get(topPrimaryDimensionValInt));

		// ActivityObject2018 repAOForThisActNameForThisUser = new ActivityObject2018(activityID, locationIDs,
		// activityName, "", newRecommTimestamp, "", "", "", String.valueOf(userId), -1, -1, -1, -1, -1, -1, -1,
		// workingLevelCatIDs, -1, -1, new String[] { "" });
		ActivityObject2018 repAOForThisActNameForThisUser2 = null;
		Long durationInSecs = null;
		// boolean userBetterRep = Constant.useBetterRepAOForLoc;// false;
		String startLat = "", startLon = "", endLat = "", endLon = "";// added on 5 April 2019

		switch (databaseName)
		{
		case "geolife1":

			// String startLat, startLon, endLat, endLon;
			Double avgAlt = Double.NaN;
			Double distTravelled = Double.NaN;
			if (!Constant.useBetterRepAOForLoc)// TODO temp
			{// Simple approach: most common lat long for that PD val and median (agg value) of avg altitude. In this
				// case, avg alt is not tied to the lat lon.
				ArrayList<String> startLatLonEndLatLon_ = aggStartLatLongEndLatLong_.get(topPrimaryDimensionValInt);
				String mostCommon = StatsUtils.mostCommon(startLatLonEndLatLon_);
				String splitted[] = mostCommon.split("_");
				startLat = splitted[0];
				startLon = splitted[1];
				endLat = splitted[2];
				endLon = splitted[3];

				avgAlt = aggValOfEachFeatForEachPDValAvgAlt.get(topPrimaryDimensionValInt);
				distTravelled = aggValOfEachFeatForEachPDValDistTrav.get(topPrimaryDimensionValInt);
			}
			else
			{
				// of all the start and end geo pairs associated with that activity/pdval in the training timelines,
				// find the start geo which is closest to the start geo of the last activity performed by the user.
				// - also take avg altitude associated with the chosen geo-coordinates, since geo-coordinates and start
				// altitude are tied to each other.
				ActivityObject2018 currentAO = recommMaster.getActivityObjectAtRecomm();
				// Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String>
				String[] nearestLoc = getStartEndLatLongAvgAltGridIndexNearestToCurrentAOEndFaster(
						currentAO.getEndLatitude(), currentAO.getEndLongitude(),
						aggStartLatLongEndLatLongAvgAltGridIndxFaster.get(topPrimaryDimensionValInt), -1);

				startLat = nearestLoc[1];// .getSecond().getFirst().getFirst();
				startLon = nearestLoc[2];// .getSecond().getFirst().getSecond();
				endLat = nearestLoc[3];// .getSecond().getSecond().getFirst();
				endLon = nearestLoc[4];// .getSecond().getSecond().getSecond();
				avgAlt = Double.valueOf(nearestLoc[5]);// getThird());

				distTravelled = SpatialUtils.haversineFastMathV3NoRound(startLat, startLon, endLat, endLon);

			}
			durationInSecs = aggValOfEachFeatForEachPDValDuration.get(topPrimaryDimensionValInt).longValue();

			// TODO : compute duration from the disTravelled and avg speed for this pd val.

			// durationInSecs = Long
			// .valueOf(aggValOfEachFeatForEachPDValDuration.get(topPrimaryDimensionValInt).toString());
			// ActivityObject2018(String userID1, int activityID1, String activityName1, String workingLevelCatIDs1,
			// ArrayList<Integer> locationIDs1, String locationName1, String startLatitude1, String endLatitude1,
			// String startLongitude1, String endLongitude1, String avgAltitude1, double distanceTravelled1,
			// long startTimestampInms1, long endTimestampInms1, long durationInSeconds1)

			// start of debug 5 April 2019
			if (false)
			{
				if (locationIDs == null)
				{
					PopUps.showMessage("Debug 5 April: locationIDs");
				}
				else
				{
					PopUps.showMessage(
							"Debug 5 April: locationIDs.size() = " + locationIDs.size() + "\n locIDs = " + locationIDs);
				}
			}
			// end of debug 5 April 2019

			repAOForThisActNameForThisUser2 = new ActivityObject2018(userId, activityID, activityName,
					workingLevelCatIDs, locationIDs, "", startLat, endLat, startLon, endLon, avgAlt.toString(),
					distTravelled, (long) newStartTimestampInMs, (long) newStartTimestampInMs + (durationInSecs * 1000),
					durationInSecs);
			break;

		case "gowalla1":
			int popularity = aggValOfEachFeatForEachPDValPopularity.get(topPrimaryDimensionValInt).intValue();
			double distFromPrev = aggValOfEachFeatForEachPDValDistFromPrev.get(topPrimaryDimensionValInt);
			long durFromPrev = aggValOfEachFeatForEachPDValDurationInSecsFromPrev.get(topPrimaryDimensionValInt)
					.longValue();

			if (!Constant.useBetterRepAOForLoc)
			{
				// use most popular locationID for this feature already assigned earlier
				if (locationIDs.size() == 0)
				{
					PopUps.printTracedErrorMsgWithExit("Error: locationIDs.size()==0");
				}
			}
			else
			{// added on 3 April 2019
				// of all the start and end geo pairs associated with that activity/pdval in the training timelines,
				// find the start geo which is closest to the start geo of the last activity performed by the user.
				// - also take avg altitude associated with the chosen geo-coordinates, since geo-coordinates and start
				// altitude are tied to each other.
				ActivityObject2018 currentAO = recommMaster.getActivityObjectAtRecomm();
				String[] nearestLoc = getStartEndLatLongAvgAltGridIndexNearestToCurrentAOEndFaster(
						currentAO.getStartLatitude(), currentAO.getStartLongitude(),
						aggStartLatLongEndLatLongAvgAltGridIndxFaster.get(topPrimaryDimensionValInt),
						currentAO.getGridIndex());
				startLat = nearestLoc[1];// .getSecond().getFirst().getFirst();
				startLon = nearestLoc[2];// .getSecond().getFirst().getSecond();

				locationIDs.clear();
				locationIDs.add(Integer.valueOf(nearestLoc[0]));// .getFirst()));
				// // String[] locIDsS = RegexUtils.patternUnderScore.split(nearestLoc.getFirst());
				// for (String s : locIDsS)
				// {
				// locationIDs.add(Integer.valueOf(s));
				// }
				// endLat = nearestLoc.getSecond().getSecond().getFirst();
				// endLon = nearestLoc.getSecond().getSecond().getSecond();
				// avgAlt = Double.valueOf(nearestLoc.getThird());
				// distTravelled = SpatialUtils.haversineFastMathV3NoRound(startLat, startLon, endLat, endLon);
			}

			repAOForThisActNameForThisUser2 = new ActivityObject2018(activityID, locationIDs, activityName, "",
					new Timestamp(newStartTimestampInMs), startLat, startLon, "", userId, -1, popularity, -1, -1, -1,
					-1, -1, workingLevelCatIDs, distFromPrev, durFromPrev, ZoneId.of("UTC"), -1, -1, -1);
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
					String.valueOf(activityID), (long) newStartTimestampInMs,
					(long) newStartTimestampInMs + (durationInSecs * 1000), durationInSecs,
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

		if (!DateTimeUtils.isSameDate(recommTimestamp, new Timestamp(newStartTimestampInMs)))

		{
			System.out.print("Warning: recommTimestamp = " + recommTimestamp + " newRecommTimestampInMs= "
					+ newStartTimestampInMs + " are not same day. medianPreceedingDuration = "
					+ aggValOfEachFeatForEachPDValDurationInSecsFromPrev.get(topPrimaryDimensionValInt)
					+ " for topPrimaryDimensionValInt =" + topPrimaryDimensionValInt);
		}
		//
		if (VerbosityConstants.verbose)
		{
			System.out.println("Debug: getRepresentativeAO: old recommTimestamp="
					+ recommTimestamp.toLocalDateTime().toString() + "medianPreceedingDuration="
					+ aggValOfEachFeatForEachPDValDurationInSecsFromPrev.get(topPrimaryDimensionValInt)
					+ " new recommendationTime=" + new Timestamp(newStartTimestampInMs).toLocalDateTime().toString());
		}
		// System.out.println("repAO=" + repAOForThisActNameForThisUser.toStringAllGowallaTS());

		if (false)
		{
			String s = "\nrepAO()\nrecommTimestamp = " + recommTimestamp + "\ntopPrimaryDimensionValInt = "
					+ topPrimaryDimensionValInt + "\ndatabaseName = " + databaseName
					+ "\n------------\nrepAOForThisActNameForThisUser = \n"
					+ ActivityObject2018.getHeaderForStringAllGowallaTSWithNameForHeaded24Dec(" ", ",") + "\n"
					+ repAOForThisActNameForThisUser2.toStringAllGowallaTSWithNameForHeaded24Dec(",");
			WToFile.appendLineToFileAbs(s, Constant.getCommonPath() + "DebugGetRepresentativeAOForActName.csv");
		}
		return repAOForThisActNameForThisUser2;

	}

	/**
	 * 
	 * @param endLatitude
	 * @param endLongitude
	 * @param listOfStartLatLongEndLatLongAvgAltForThisPDVal
	 * @return Triples(locIDDelimitedBy_, Pair(Pair(StartLat,StartLon),Pair(EndLat,EndLon)), AvgAlt)
	 * 
	 * @since 21 March 2019
	 */
	private static Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String> getStartEndLatLongAvgAltNearestToCurrentAOEnd(
			String endLatitude, String endLongitude,
			ArrayList<Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String>> listOfStartLatLongEndLatLongAvgAltForThisPDVal)
	{

		Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String> nearestLoc = new Triple<>();

		double nearestDistInKms = Double.MAX_VALUE;
		try
		{
			for (Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String> e : listOfStartLatLongEndLatLongAvgAltForThisPDVal)
			{
				String startLat = e.getSecond().getFirst().getFirst();
				String startLon = e.getSecond().getFirst().getSecond();
				double dist = SpatialUtils.haversineFastMathV3NoRound(startLat, startLon, endLatitude, endLongitude);

				if (dist < nearestDistInKms)
				{
					nearestLoc = e;
					nearestDistInKms = dist;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (VerbosityConstants.verbose || true)
		{
			System.out.print("Inside getStartEndLatLongAvgAltNearestToCurrentAOEnd: endLatitude = " + endLatitude
					+ " endLongitude=" + endLongitude + " nearest loc = " + nearestLoc.toString()
					+ " nearestDistInKms=");
			System.out.printf("%f kms\n", nearestDistInKms);
		}

		return nearestLoc;
	}

	/**
	 * 
	 * @param endLatitude
	 * @param endLongitude
	 * @param listOfStartLatLongEndLatLongAvgAltForThisPDValFaster
	 * @param gridID
	 *            for gowalla, faster computation using gridIndices rather than haversinse
	 * @return Triples(locIDDelimitedBy_, Pair(Pair(StartLat,StartLon),Pair(EndLat,EndLon)), AvgAlt, GridIndex)
	 * 
	 * @since 21 March 2019
	 */
	private static String[] getStartEndLatLongAvgAltGridIndexNearestToCurrentAOEndFaster(String endLatitude,
			String endLongitude, ArrayList<String[]> listOfStartLatLongEndLatLongAvgAltForThisPDValFaster, int gridID)
	{

		String[] nearestLoc = new String[6];
		long t1 = -999, t2 = -999, t3 = -999, t4 = -999;
		double nearestDistInKms = Double.MAX_VALUE;
		try
		{
			if (gridID >= 0) // for gowalla
			{
				if (Constant.useHaversineDistInLocationFED == false)
				{
					// WToFile.writeToNewFile(
					// "Error: Constant.useHaversineDistInLocationFED=false which means necessary objects storing
					// precomputed haversine distances between gridIndices are not deserialised",
					// Constant.getCommonPath() + "ErrorExceptionEncountered.txt");
					PopUps.printTracedErrorMsgWithExit(
							"Error: Constant.useHaversineDistInLocationFED=false which means necessary objects storingprecomputed haversine distances between gridIndices are not deserialised");
				}
				t1 = System.currentTimeMillis();
				for (String[] e : listOfStartLatLongEndLatLongAvgAltForThisPDValFaster)
				{
					// String startLat = e[1];// e.getSecond().getFirst().getFirst();
					// String startLon = e[2];// .getSecond().getFirst().getSecond();
					double dist = (gridID == Integer.valueOf(e[6])) ? 0
							: DomainConstants.getHaversineDistForGridIndexPairs(gridID, Integer.valueOf(e[6]));
					// SpatialUtils.haversineFastMathV3NoRound(startLat, startLon, endLatitude,
					// endLongitude);
					if (dist < nearestDistInKms)
					{
						nearestLoc = e;
						nearestDistInKms = dist;
					}
				}
				t2 = System.currentTimeMillis();

			}
			else // disabled for comparison
			{
				t3 = System.currentTimeMillis();
				for (String[] e : listOfStartLatLongEndLatLongAvgAltForThisPDValFaster)
				{
					String startLat = e[1];// e.getSecond().getFirst().getFirst();
					String startLon = e[2];// .getSecond().getFirst().getSecond();
					double dist = SpatialUtils.haversineFastMathV3NoRound(startLat, startLon, endLatitude,
							endLongitude);

					if (dist < nearestDistInKms)
					{
						nearestLoc = e;
						nearestDistInKms = dist;
					}
				}
				t4 = System.currentTimeMillis();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (VerbosityConstants.verbose || true)
		{
			System.out.print("Inside getStartEndLatLongAvgAltNearestToCurrentAOEnd: endLatitude = " + endLatitude
					+ " endLongitude=" + endLongitude + " nearest loc = " + nearestLoc.toString()
					+ " nearestDistInKms=");
			System.out.printf("%f kms\n", nearestDistInKms);
		}

		// WToFile.appendLineToFileAbs((t2 - t1) + "," + (t4 - t3) + "," + ((t2 - t1) - (t4 - t3)) + "\n",
		// Constant.getOutputCoreResultsPath() + "DebugApril6GridVsHaversineTime.csv");

		return nearestLoc;
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
		// PopUps.showMessage("Inside RepresentativeAOInfo()");
		System.out.println("Alert: useAggStartLatLongEndLatLongAvgAlt = " + useAggStartLatLongEndLatLongAvgAlt);
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
		// this.aggStartLatLongEndLatLongAvgAlt = new HashMap<>();
		this.aggStartLatLongEndLatLongAvgAltGridIndxFaster = new HashMap<>();

		this.aggValOfEachFeatForEachPDValDuration = new HashMap<>();
		this.aggValOfEachFeatForEachPDValDistTrav = new HashMap<>();

		this.aggValOfEachFeatForEachPDValAvgAlt = new HashMap<>();
		this.aggValOfEachFeatForEachPDValDistFromPrev = new HashMap<>();
		this.aggValOfEachFeatForEachPDValPopularity = new HashMap<>();
		this.aggValOfEachFeatForEachPDValLocationID = new HashMap<>();
		this.aggValOfEachFeatForEachPDValActivityID = new HashMap<>();

		// ActivityObject2018 ao;
		// ao.getLocationIDs()
		// PopUps.showMessage("here1 RepresentativeAOInfo()\nlistOfFeats = " + listOfFeats);
		for (Object f : listOfFeats)
		{
			String featureAsString = f.toString();
			// PopUps.showMessage("here2 featureAsString = " + featureAsString);

			switch (featureAsString)
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
				break;// no need since determined from recommender activity
			}

			case "ActNameLevel1F":
			{
				break; // not used as of 20 Mar 2019
			}

			case "DurationFromPrevF":
			{
				break;// already computed above
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

			case "PopularityF":
			{// TODO 19 Mar 2019: check this later
				// ActivityObject2018 ao;
				mapOfEachFeatForEachPDValPopularity = getListOfFeatureVals(userId,
						trainTimelinesAllUsersContinuousFiltrd, isCollaborative, ActivityObject2018::getCheckins_count);//
				aggValOfEachFeatForEachPDValPopularity = mapOfEachFeatForEachPDValPopularity.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.getPercentileInt(e.getValue(), 50)));
				break;
			}

			case "StartGeoF":
			{

				if (!disableAggStartLatLongEndLatLongAsHSC)
				{
					aggStartLatLongEndLatLongAsHSC = getStartAndEndGeoCoordASHSCIndexForEachPDVal(userId,
							trainTimelinesAllUsersContinuousFiltrd, isCollaborative);
				}
				// added on 21 March 2019
				if (useAggStartLatLongEndLatLongAvgAlt)
				{
					aggStartLatLongEndLatLongAvgAltGridIndxFaster = getStartAndEndGeoCoordinatesWithAvgAltForEachPDValFaster(
							userId, trainTimelinesAllUsersContinuousFiltrd, isCollaborative);
				}
				else
				{
					aggStartLatLongEndLatLong_ = getStartAndEndGeoCoordinatesForEachPDVal(userId,
							trainTimelinesAllUsersContinuousFiltrd, isCollaborative, "_");

				}

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
				// PopUps.showMessage("here locf0");
				aggValOfEachFeatForEachPDValLocationID = mapOfEachFeatForEachPDValLocationsIDs.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> StatsUtils.mostCommon(e.getValue())));

				// PopUps.showMessage("here locf1");
				if (aggStartLatLongEndLatLong_.size() == 0 && !useAggStartLatLongEndLatLongAvgAlt)
				{
					// PopUps.showMessage("here locf2");
					aggStartLatLongEndLatLong_ = getStartAndEndGeoCoordinatesForEachPDVal(userId,
							trainTimelinesAllUsersContinuousFiltrd, isCollaborative, "_");

					// PopUps.showMessage("here locf3");
					if (!disableAggStartLatLongEndLatLongAsHSC)
					{
						aggStartLatLongEndLatLongAsHSC = getStartAndEndGeoCoordASHSCIndexForEachPDVal(userId,
								trainTimelinesAllUsersContinuousFiltrd, isCollaborative);
					}

					// PopUps.showMessage("here locf4");
					// mapOfEachFeatForEachPDValStartLat = getListOfFeatureVals(userId,
					// trainTimelinesAllUsersContinuousFiltrd, isCollaborative,
					// ActivityObject2018::getStartLatitude);
					// mapOfEachFeatForEachPDValStartLon = getListOfFeatureVals(userId,
					// trainTimelinesAllUsersContinuousFiltrd, isCollaborative,
					// ActivityObject2018::getStartLongitude);
				}
				// PopUps.showMessage("here locf5");
				if (aggStartLatLongEndLatLongAvgAltGridIndxFaster.size() == 0)
				{
					// PopUps.showMessage("here locf6");
					// added on 21 March 2019
					aggStartLatLongEndLatLongAvgAltGridIndxFaster = getStartAndEndGeoCoordinatesWithAvgAltForEachPDValFaster(
							userId, trainTimelinesAllUsersContinuousFiltrd, isCollaborative);
				}
				// PopUps.showMessage("here locf7");
				break;
			}
			case "EndGeoF":
			{
				if (aggStartLatLongEndLatLong_.size() == 0)
				{
					aggStartLatLongEndLatLong_ = getStartAndEndGeoCoordinatesForEachPDVal(userId,
							trainTimelinesAllUsersContinuousFiltrd, isCollaborative, "_");

					if (!disableAggStartLatLongEndLatLongAsHSC)
					{
						aggStartLatLongEndLatLongAsHSC = getStartAndEndGeoCoordASHSCIndexForEachPDVal(userId,
								trainTimelinesAllUsersContinuousFiltrd, isCollaborative);
					}
				}
				if (aggStartLatLongEndLatLongAvgAltGridIndxFaster.size() == 0)
				{
					// added on 21 March 2019
					aggStartLatLongEndLatLongAvgAltGridIndxFaster = getStartAndEndGeoCoordinatesWithAvgAltForEachPDValFaster(
							userId, trainTimelinesAllUsersContinuousFiltrd, isCollaborative);
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
		System.out.println("For userId = " + userId + " Time taken by  RepresentativeAOInfo= "
				+ ((System.currentTimeMillis() - t1) / 1000) + " secs" + "\n"
				+ PerformanceAnalytics.getHeapInformation());

		// PopUps.showMessage("Exiting RepresentativeAOInfo()");
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
	 *         ao.getEndLatitude() + delimiter + ao.getEndLongitude();
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

	///////////
	/**
	 * Fork of getStartAndEndGeoCoordinatesForEachPDVal() to include AvgAltitude associated with each Start End geo
	 * coorindates
	 * 
	 * @param userId
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param isCollaborative
	 * @return actID, List of Pair(Pair(Pair(StartLat,StartLon),Pair(EndLat,EndLon)), AvgAlt)
	 * @since 21 March 2019
	 */
	private static Map<Integer, ArrayList<Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String>>> getStartAndEndGeoCoordinatesWithAvgAltForEachPDVal(
			String userId, LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd,
			boolean isCollaborative)
	{
		// List of Pair(Pair(Pair(StartLat,StartLon),Pair(EndLat,EndLon)), AvgAlt)
		Map<Integer, ArrayList<Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String>>> mapOfStartLatLonEndLatLonAvgAltForEachPDVal = new HashMap<>();

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
						ArrayList<Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String>> listOfFeatureVals = mapOfStartLatLonEndLatLonAvgAltForEachPDVal
								.get(pdVal);

						if (listOfFeatureVals == null)
						{
							listOfFeatureVals = new ArrayList<>();
						}

						Pair<String, String> startGeoCoordinates = new Pair<>(ao.getStartLatitude(),
								ao.getStartLongitude());
						Pair<String, String> endGeoCoordinates = new Pair<>(ao.getEndLatitude(), ao.getEndLongitude());
						// String locID = ao.getLocationIDs().get(0);
						String locIDs = String.valueOf(ao.getLocationIDs().get(0));// ao.getLocationIDs('_'); //
																					// multiple locationIDs
						// permitted for merged locations
						// running our of heapspace, heance using one locID only
						// String val = ao.getStartLatitude() + delimiter + ao.getStartLongitude() + delimiter
						// + ao.getEndLatitude() + delimiter + ao.getEndLongitude();
						listOfFeatureVals.add(
								new Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String>(locIDs,
										new Pair<>(startGeoCoordinates, endGeoCoordinates), ao.getAvgAltitude()));
						// listOfDurationFromPrev.add(ao.getDurationInSecondsFromPrev());
						mapOfStartLatLonEndLatLonAvgAltForEachPDVal.put(pdVal, listOfFeatureVals);
					}
				}
			}
		}
		System.out.println("mapOfStartLatLonEndLatLonAvgAltForEachPDVal.size () = "
				+ mapOfStartLatLonEndLatLonAvgAltForEachPDVal.size());
		return mapOfStartLatLonEndLatLonAvgAltForEachPDVal;
	}

	/**
	 * Fork of getStartAndEndGeoCoordinatesWithAvgAltForEachPDVal() to user list of String arrays for better memory
	 * performance coorindates
	 * 
	 * @param userId
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param isCollaborative
	 * @return actID, List of Pair(Pair(Pair(StartLat,StartLon),Pair(EndLat,EndLon)), AvgAlt, gridIndex)
	 * @since 21 March 2019
	 */
	private static Map<Integer, ArrayList<String[]>> getStartAndEndGeoCoordinatesWithAvgAltForEachPDValFaster(
			String userId, LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd,
			boolean isCollaborative)
	{
		// List of Pair(Pair(Pair(StartLat,StartLon),Pair(EndLat,EndLon)), AvgAlt, gridIndex)
		Map<Integer, ArrayList<String[]>> mapOfStartLatLonEndLatLonAvgAltGridIndxForEachPDVal = new HashMap<>();

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
						ArrayList<String[]> listOfFeatureVals = mapOfStartLatLonEndLatLonAvgAltGridIndxForEachPDVal
								.get(pdVal);

						if (listOfFeatureVals == null)
						{
							listOfFeatureVals = new ArrayList<>();
						}

						String[] valsToStore = new String[7];
						valsToStore[0] = String.valueOf(ao.getLocationIDs().get(0));
						valsToStore[1] = ao.getStartLatitude();
						valsToStore[2] = ao.getStartLongitude();
						valsToStore[3] = ao.getEndLatitude();
						valsToStore[4] = ao.getEndLongitude();
						valsToStore[5] = ao.getAvgAltitude();
						valsToStore[6] = String.valueOf(ao.getGridIndex());

						// Pair<String, String> startGeoCoordinates = new Pair<>(ao.getStartLatitude(),
						// ao.getStartLongitude());
						// Pair<String, String> endGeoCoordinates = new Pair<>(ao.getEndLatitude(),
						// ao.getEndLongitude());
						// String locID = ao.getLocationIDs().get(0);
						// String locIDs = String.valueOf(ao.getLocationIDs().get(0));// ao.getLocationIDs('_'); //
						// multiple locationIDs
						// permitted for merged locations
						// running our of heapspace, heance using one locID only
						// String val = ao.getStartLatitude() + delimiter + ao.getStartLongitude() + delimiter
						// + ao.getEndLatitude() + delimiter + ao.getEndLongitude();
						listOfFeatureVals.add(valsToStore);
						// new Triple<String, Pair<Pair<String, String>, Pair<String, String>>, String>(locIDs,
						// new Pair<>(startGeoCoordinates, endGeoCoordinates), ao.getAvgAltitude()));
						// listOfDurationFromPrev.add(ao.getDurationInSecondsFromPrev());
						mapOfStartLatLonEndLatLonAvgAltGridIndxForEachPDVal.put(pdVal, listOfFeatureVals);
					}
				}
			}
		}
		System.out.println("mapOfStartLatLonEndLatLonAvgAltGridIdForEachPDVal.size () = "
				+ mapOfStartLatLonEndLatLonAvgAltGridIndxForEachPDVal.size());
		return mapOfStartLatLonEndLatLonAvgAltGridIndxForEachPDVal;
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

						long hilbertSpaceFillingCurveIndexForEndGeo = -9999;
						if (endLat != null)
						{
							hilbertSpaceFillingCurveIndexForEndGeo = endLat.length() > 0
									? HilbertCurveUtils.getCompactHilbertCurveIndex(endLat, ao.getEndLongitude())
									: -9999;
						}

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
					cins_count = RecommTestsUtils.getMedianCheckinsCountForGivePDVal(aosForEachPDVal, pdVal);
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
				Timestamp mediaStartTS = RecommTestsUtils.getMedianSecondsSinceMidnightTimestamp(
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
		boolean verboseRepAOInfo = true;// databaseName.contains("oalla") ? false : true;
		LeakyBucket lbWriter = new LeakyBucket(1000,
				Constant.getCommonPath() + "buildRepresentativeAOInfosDec2018Log.txt", false);
		lbWriter.addToLeakyBucketWithNewline(
				"------Inside buildRepresentativeAOInfosDec2018: representativeAOInfosForAllUsers ------------------- ");
		// StringBuilder sbLog = new StringBuilder();
		LinkedHashMap<String, RepresentativeAOInfo> representativeAOInfosForAllUsers = new LinkedHashMap<>();

		for (Entry<String, Timeline> userEntry : trainTimelinesAllUsersContinuousFiltrd.entrySet())
		{
			RepresentativeAOInfo repAOInfo = new RepresentativeAOInfo(userEntry.getKey(), primaryDimension,
					trainTimelinesAllUsersContinuousFiltrd, collaborativecandidates);
			representativeAOInfosForAllUsers.put(userEntry.getKey(), repAOInfo);

			if (verboseRepAOInfo)
			{
				// since for gowalla dataset, the written file can be large, write it only for around 2% of the users.
				if (!databaseName.contains("gowalla1") || Math.random() < 0.01)
				{
					lbWriter.addToLeakyBucketWithNewline(userEntry.getKey() + "--\n" + repAOInfo.toString());
				}
				// sbLog.append(userEntry.getKey() + "--\n" + repAOInfo.toString() + "\n");
			}
		}
		// PopUps.showMessage("here 1: buildRepresentativeAOInfosDec2018");
		if (verboseRepAOInfo)
		{// PopUps.showMessage

			lbWriter.addToLeakyBucketWithNewline(
					"representativeAOInfosForAllUsers.size()= " + representativeAOInfosForAllUsers.size() + "keySet = "
							+ representativeAOInfosForAllUsers.keySet() + "\n\n");
			lbWriter.flushLeakyBucket();
			// String s = "\n***************\nInside buildRepresentativeAOInfosDec2018:
			// representativeAOInfosForAllUsers.size()= "
			// + representativeAOInfosForAllUsers.size() + "keySet = " + representativeAOInfosForAllUsers.keySet()
			// + "\n-------\n" + sbLog.toString();

			// WToFile.appendLineToFileAbs(s, Constant.getCommonPath() + "buildRepresentativeAOInfosDec2018Log.txt");
			// System.exit(0);// TODO
		}

		//
		return representativeAOInfosForAllUsers;
	}

	/**
	 * 
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @param primaryDimension
	 * @param databaseName
	 * @param collaborativecandidates
	 * @return
	 */
	public static RepresentativeAOInfo buildRepresentativeAOInfosDec2018SingleUser(String userID,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, PrimaryDimension primaryDimension,
			String databaseName, boolean collaborativecandidates)
	{
		// PopUps.showMessage("Inside buildRepresentativeAOInfosDec2018 trainTimelinesAllUsersContinuousFiltrd.size()="
		// + trainTimelinesAllUsersContinuousFiltrd.size());
		boolean verboseRepAOInfo = true;// databaseName.contains("oalla") ? false : true;

		// StringBuilder sbLog = new StringBuilder();
		// RepresentativeAOInfo representativeAOInfoForGivenUser = new LinkedHashMap<>();

		// for (Entry<String, Timeline> userEntry : trainTimelinesAllUsersContinuousFiltrd.entrySet())
		// {
		RepresentativeAOInfo repAOInfo = new RepresentativeAOInfo(userID, primaryDimension,
				trainTimelinesAllUsersContinuousFiltrd, collaborativecandidates);
		// representativeAOInfosForAllUsers.put(userEntry.getKey(), repAOInfo);

		if (verboseRepAOInfo)
		{
			// since for gowalla dataset, the written file can be large, write it only for around 2% of the users.
			if (!databaseName.contains("gowalla1") || Math.random() < 0.007)
			{
				// LeakyBucket lbWriter = new LeakyBucket(1000,
				// Constant.getCommonPath() + "buildRepresentativeAOInfosDec2018Log_User" + userID + ".txt",
				// false);
				// lbWriter.addToLeakyBucketWithNewline(
				// "------Inside buildRepresentativeAOInfosDec2018SingleUser: representativeAOInfosFor userID= "
				// + userID);
				// lbWriter.addToLeakyBucketWithNewline(userID + "--\n" + repAOInfo.toString());
				// lbWriter.flushLeakyBucket();
				WToFile.appendLineToFileAbs(userID + "--\n" + repAOInfo.toString() + "\n",
						Constant.getCommonPath() + "buildRepresentativeAOInfosDec2018Log.txt");
			}
			// sbLog.append(userEntry.getKey() + "--\n" + repAOInfo.toString() + "\n");
		}
		// }
		// PopUps.showMessage("here 1: buildRepresentativeAOInfosDec2018");
		// if (verboseRepAOInfo)
		// {// PopUps.showMessage
		//
		// // lbWriter.addToLeakyBucketWithNewline(
		// // "representativeAOInfosForAllUsers.size()= " + representativeAOInfosForAllUsers.size() + "keySet = "
		// // + representativeAOInfosForAllUsers.keySet() + "\n\n");
		//
		// String s = "\n***************\nInside buildRepresentativeAOInfosDec2018:
		// representativeAOInfosForAllUsers.size()= "
		// + representativeAOInfosForAllUsers.size() + "keySet = " + representativeAOInfosForAllUsers.keySet()
		// + "\n-------\n" + sbLog.toString();
		//
		// WToFile.appendLineToFileAbs(s, Constant.getCommonPath() + "buildRepresentativeAOInfosDec2018Log.txt");
		// // System.exit(0);//
		// }

		//
		return repAOInfo;
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
