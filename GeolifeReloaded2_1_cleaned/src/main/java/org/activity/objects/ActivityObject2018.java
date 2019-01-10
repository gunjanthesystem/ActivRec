package org.activity.objects;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.SanityConstants;
import org.activity.spatial.SpatialUtils;
import org.activity.stats.HilbertCurveUtils;
import org.activity.ui.PopUps;
import org.activity.util.ActivityObject;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.StringCode;
import org.activity.util.UtilityBelt;

/**
 * Trying to make it better and suitable for Gowalla dataset on 15 Sep 2016
 * <p>
 * thought on 5 April 2017. make the object leaner by removing redunant information (thought on 9 sept 2016: to reduce
 * size of object, remove method which can converted to static functional methods), but perhaps do not affect the size
 * of per object (ref:https://stackoverflow.com/questions/7060141/what-determines-java-object-size)
 * </p>
 * 
 * @author gunjan
 *
 */
public class ActivityObject2018 implements Serializable
{

	private static final long serialVersionUID = 5056824311499867608L;
	// private static PrimaryDimension primaryDimension = Constant.primaryDimension;

	/**
	 * this was to keep activity object generic but not entirely successfull IMHO
	 */
	ArrayList<Dimension> dimensions;//

	/**
	 * (User_ID, 2) //this was to keep activity object generic but not entirely successfull IMHO
	 */
	HashMap<String, String> dimensionIDNameValues; //

	private int activityID;
	/**
	 * an activity object can have multiple location ids if it is a merged
	 */
	// LinkedHashSet<Integer> locationIDs;
	ArrayList<Integer> locationIDs;
	int gridIndex;// introduced on 13 July 2018
	String activityName, locationName;
	/**
	 * workingLevelCatIDs are "__" separated catID for the given working level in hierarhcy
	 */
	String workingLevelCatIDs;//

	/**
	 * CAN BE REMOVED AS THEY CAN BE COMPUTED FROM DIRECT CAT ID and for efficiency stored in a lookup map [0] level 1
	 * catids, [1] level 2 catid
	 */
	// private String[] levelWiseCatIDs;

	/**
	 * Changed from sql.timestamp to long for performance reasons.
	 */
	private long startTimestampInms, endTimestampInms;

	/**
	 * Not available in Gowalla dataset
	 */
	long durationInSeconds;
	/**
	 * Not available in DCU_dataset. Available in Geolife dataset
	 */
	String startLatitude, endLatitude, startLongitude, endLongitude, startAltitude, endAltitude, avgAltitude;

	ArrayList<String> lats, lons; // multiple lats, lons if it is a merged
	/**
	 * Not available in DCU_dataset. Available in Geolife dataset
	 */
	double distanceTravelledInKm;

	/**
	 * Gowalla dataset
	 */
	String userID;
	int photos_count, checkins_count, users_count, radius_meters, highlights_count, items_count, max_items_count;// spot_categories;
	double distInMFromPrev, distInMFromNext;

	long durInSecFromPrev, durInSecFromNext;
	ZoneId timeZoneId;

	/**
	 * For Serialisation purposes.
	 * 
	 * @since 21 May 2018
	 */
	public ActivityObject2018()
	{

	}

	// Start of added on 14 Nov 2018

	// End of added on 14 Nov 2018

	/**
	 * @since April 9 2018
	 * @return
	 */
	public double getDistInMFromNext()
	{
		return distInMFromNext;
	}

	/**
	 * @since April 9 2018
	 * @return
	 */
	public long getDurInSecFromNext()
	{
		return durInSecFromNext;
	}

	/**
	 * Especially needed for representative recommended act objs.
	 * 
	 * @param timeZoneId
	 */
	public void setTimeZoneId(ZoneId timeZoneId)
	{
		this.timeZoneId = timeZoneId;
	}

	public ZoneId getTimeZoneId()
	{
		// if (timeZoneId == null)
		// {
		// PopUps.printTracedErrorMsg("NULL timezone id");
		// }
		return timeZoneId;
	}

	/**
	 * if there is one primary dimension val, i.e., no merger then no delimitation
	 * 
	 * @param delimiter
	 * @return
	 */
	public String getPrimaryDimensionVal(String delimiter)
	{
		return this.getPrimaryDimensionVal().stream().map(s -> s.toString()).collect(Collectors.joining(delimiter));
	}

	/**
	 * if there is one primary dimension val, i.e., no merger then no delimitation
	 * 
	 * @param delimiter
	 * @return
	 */
	public String getGivenDimensionVal(String delimiter, PrimaryDimension givenDimension)
	{
		return this.getGivenDimensionVal(givenDimension).stream().map(s -> s.toString())
				.collect(Collectors.joining(delimiter));
	}

	public long getGridID()
	{
		return this.gridIndex;
	}

	/**
	 * to add gridIndex to AOs in already created toy timelines
	 * 
	 * @since 6 Aug 2018
	 */
	public void setGridIndex(int givenGridIndex)
	{
		this.gridIndex = givenGridIndex;
	}

	/**
	 * 
	 * @param ao
	 * @param primaryDimension
	 * @return
	 */
	public ArrayList<Integer> getPrimaryDimensionVal()
	{
		switch (Constant.primaryDimension)
		{
		case ActivityID:
			ArrayList<Integer> arr = new ArrayList<>();
			arr.add(this.getActivityID());
			return arr;
		// return new ArrayList<>(this.getActivityID());// only one activity name is expected even when merged.
		case LocationID:
			LinkedHashSet<Integer> uniqueLocationIDs = new LinkedHashSet<>(this.getLocationIDs());
			return new ArrayList<Integer>(uniqueLocationIDs);
		// this.getLocationIDs();
		default:
			PopUps.printTracedErrorMsgWithExit("Unknown primary dimension val = " + Constant.primaryDimension);
			return null;
		}
	}

	/**
	 * 
	 * @param ao
	 * @param primaryDimension
	 * @return
	 */
	public ArrayList<Integer> getGivenDimensionVal(PrimaryDimension givenDimension)
	{
		switch (givenDimension)
		{
		case ActivityID:
			ArrayList<Integer> arr = new ArrayList<>();
			arr.add(this.getActivityID());
			return arr;
		// return new ArrayList<>(this.getActivityID());// only one activity name is expected even when merged.
		case LocationID:
			LinkedHashSet<Integer> uniqueLocationIDs = new LinkedHashSet<>(this.getLocationIDs());
			return new ArrayList<Integer>(uniqueLocationIDs);
		case LocationGridID:
			ArrayList<Integer> gridIndicesArr = new ArrayList<>();
			gridIndicesArr.add(this.gridIndex);
			return gridIndicesArr;
		// this.getLocationIDs();
		default:
			PopUps.printTracedErrorMsgWithExit("Unknown givenDimension dimension val = " + givenDimension);
			return null;
		}
	}

	// /**
	// *
	// * @param ao
	// * @param primaryDimension
	// * @return
	// */
	// public ArrayList<Integer> getGivenDimensionVal(PrimaryDimension givenDimension)
	// {
	// switch (givenDimension)
	// {
	// case ActivityID:
	// ArrayList<Integer> arr = new ArrayList<>();
	// arr.add(this.getActivityID());
	// return arr;
	// // return new ArrayList<>(this.getActivityID());// only one activity name is expected even when merged.
	// case LocationID:
	// LinkedHashSet<Integer> uniqueLocationIDs = new LinkedHashSet<>(this.getLocationIDs());
	// return new ArrayList<Integer>(uniqueLocationIDs);
	// case LocationGridID:
	// return this.gridID;
	// // this.getLocationIDs();
	// default:
	// PopUps.printTracedErrorMsgWithExit("Unknown primary dimension val = " + Constant.primaryDimension);
	// return null;
	// }
	// }

	/**
	 * Whether this activity object and ao2 are equal with respect to the current primary dimension. They are considered
	 * equal if they share any value, i.e., size of intersection of primary values>1
	 * 
	 * @param ao
	 * @param primaryDimension
	 * @return
	 */
	public boolean equalsWrtPrimaryDimension(ActivityObject2018 ao2)
	{
		switch (Constant.primaryDimension)
		{
		case ActivityID:
			return this.activityID == ao2.getActivityID();
		case LocationID:
			// actually this approach for Location ID also works for activity id but is slower since set
			// intersection, hence create a lighter methods for activityID
			Set<Integer> intersection = UtilityBelt.getIntersection(this.getPrimaryDimensionVal(),
					ao2.getPrimaryDimensionVal());

			if (intersection.size() > 0)
			{// System.out.println("intersection.size() = " + intersection.size() + " returning TRUE");
				return true;
			}
			else
			{ // System.out.println("intersection.size() = " + intersection.size() + " returning FALSE");
				return false;
			}
		default:
			PopUps.printTracedErrorMsgWithExit("Unknown primary dimension val = " + Constant.primaryDimension);
			return false;
		}
	}

	/**
	 * Whether this activity object and ao2 are equal with respect to the current primary dimension. They are considered
	 * equal if they share any value, i.e., size of intersection of primary values>1
	 * 
	 * @param ao
	 * @param primaryDimension
	 * @return
	 */
	public boolean equalsWrtGivenDimension(ActivityObject2018 ao2, PrimaryDimension givenDimension)
	{
		switch (givenDimension)
		{
		case ActivityID:
			return this.activityID == ao2.getActivityID();
		case LocationID:
			// actually this approach for Location ID also works for activity id but is slower since set
			// intersection, hence create a lighter methods for activityID
			Set<Integer> intersection = UtilityBelt.getIntersection(this.getPrimaryDimensionVal(),
					ao2.getPrimaryDimensionVal());

			if (intersection.size() > 0)
			{// System.out.println("intersection.size() = " + intersection.size() + " returning TRUE");
				return true;
			}
			else
			{ // System.out.println("intersection.size() = " + intersection.size() + " returning FALSE");
				return false;
			}
		case LocationGridID:
			return this.gridIndex == ao2.getGridID();
		default:
			PopUps.printTracedErrorMsgWithExit("Unknown primary dimension val = " + Constant.primaryDimension);
			return false;
		}
	}

	/**
	 * Whether this activity object and ao2 are equal with respect to the current primary dimension. They are considered
	 * equal if they share any value, i.e., size of intersection of primary values>1
	 * 
	 * @param ao
	 * @param primaryDimension
	 * @return
	 * @deprecated
	 */
	public boolean equalsWrtPrimaryDimension0(ActivityObject2018 ao2)
	{
		// actually this approach for Location ID also works for activity id but is slower since set
		// intersection, hence create a lighter methods for activityID
		Set<Integer> intersection = UtilityBelt.getIntersection(this.getPrimaryDimensionVal(),
				ao2.getPrimaryDimensionVal());

		if (intersection.size() > 0)
		{// System.out.println("intersection.size() = " + intersection.size() + " returning TRUE");
			return true;
		}
		else
		{ // System.out.println("intersection.size() = " + intersection.size() + " returning FALSE");
			return false;
		}
	}

	/**
	 * Whether this activity object and primaryDimensionValToCompare are equal with respect to the current primary
	 * dimension. They are considered equal if they share any value, i.e., size of intersection of primary values>1
	 * 
	 * @param ao
	 * @param primaryDimension
	 * @return
	 */
	public boolean equalsWrtPrimaryDimension(ArrayList<Integer> primaryDimensionValToCompare)
	{
		if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
		{
			if (primaryDimensionValToCompare.size() != 1)
			{
				PopUps.printTracedErrorMsgWithExit(" Error: expected 1 val for primaryDimensionValToCompare but got "
						+ primaryDimensionValToCompare.size() + " vals, Constant.primaryDimension="
						+ Constant.primaryDimension);
			}
			return (primaryDimensionValToCompare.get(0) == this.activityID);
		}
		else // is generic and will also apply for activity id as primary dimension but the above if block written for
				// better performance
		{
			Set<Integer> intersection = UtilityBelt.getIntersection(this.getPrimaryDimensionVal(),
					primaryDimensionValToCompare);
			if (intersection.size() > 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	/**
	 * Constructor for Gowalla activity object
	 * 
	 * @param activityID
	 * @param locationID
	 * @param activityName
	 * @param locationName
	 * @param startTimestamp
	 * @param startLatitude
	 * @param startLongitude
	 * @param startAltitude
	 * @param userID
	 * @param photos_count
	 * @param checkins_count
	 * @param users_count
	 * @param radius_meters
	 * @param highlights_count
	 * @param items_count
	 * @param max_items_count
	 */
	public ActivityObject2018(int activityID, int locationID, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs)
	{

		// this.activityID = activityID;

		String splittedwlci[] = RegexUtils.patternDoubleUnderScore.split(workingLevelCatIDs);
		// $$ workingLevelCatIDs.split("__");

		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id

		this.locationIDs = new ArrayList<>();// LinkedHashSet<Integer>();
		locationIDs.add(locationID);
		this.activityName = splittedwlci[0];// String.valueOf(activityID);// activityName;
		this.locationName = locationName;
		this.startTimestampInms = startTimestamp.getTime();
		this.endTimestampInms = startTimestamp.getTime();
		this.startLatitude = startLatitude;
		this.startLongitude = startLongitude;
		this.startAltitude = startAltitude;
		this.userID = userID;
		this.photos_count = photos_count;
		this.checkins_count = checkins_count;
		this.users_count = users_count;
		this.radius_meters = radius_meters;
		this.highlights_count = highlights_count;
		this.items_count = items_count;
		this.max_items_count = max_items_count;
		this.workingLevelCatIDs = workingLevelCatIDs;
	}

	/**
	 * 
	 * @param activityID
	 * @param locationID
	 * @param activityName
	 * @param locationName
	 * @param startTimestamp
	 * @param startLatitude
	 * @param startLongitude
	 * @param startAltitude
	 * @param userID
	 * @param photos_count
	 * @param checkins_count
	 * @param users_count
	 * @param radius_meters
	 * @param highlights_count
	 * @param items_count
	 * @param max_items_count
	 * @param workingLevelCatIDs
	 * @param distanceInMFromPrev
	 * @param durationInSecsFromPrev
	 */
	public ActivityObject2018(int activityID, int locationID, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromPrev,
			long durationInSecsFromPrev)
	{

		// this.activityID = activityID;

		String splittedwlci[] = workingLevelCatIDs.split("__");
		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id

		this.locationIDs = new ArrayList<Integer>();
		locationIDs.add(locationID);

		this.activityName = splittedwlci[0];// String.valueOf(activityID);// activityName;
		this.locationName = locationName;
		this.startTimestampInms = startTimestamp.getTime();
		this.endTimestampInms = startTimestamp.getTime();
		this.startLatitude = startLatitude;
		this.startLongitude = startLongitude;
		this.startAltitude = startAltitude;
		this.userID = userID;
		this.photos_count = photos_count;
		this.checkins_count = checkins_count;
		this.users_count = users_count;
		this.radius_meters = radius_meters;
		this.highlights_count = highlights_count;
		this.items_count = items_count;
		this.max_items_count = max_items_count;
		this.workingLevelCatIDs = workingLevelCatIDs;

		this.distInMFromPrev = distanceInMFromPrev;
		this.durInSecFromPrev = durationInSecsFromPrev;

	}

	/**
	 * 
	 * @param activityID
	 * @param locationIDs
	 *            list of locations id. this will >1 in case of mergers. note: we store the location id in the activity
	 *            object as Set and not as List
	 * @param activityName
	 * @param locationName
	 * @param startTimestamp
	 * @param startLatitude
	 * @param startLongitude
	 * @param startAltitude
	 * @param userID
	 * @param photos_count
	 * @param checkins_count
	 * @param users_count
	 * @param radius_meters
	 * @param highlights_count
	 * @param items_count
	 * @param max_items_count
	 * @param workingLevelCatIDs
	 * @param distanceInMFromPrev
	 * @param durationInSecsFromPrev
	 */
	public ActivityObject2018(int activityID, ArrayList<Integer> locationIDs, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromPrev,
			long durationInSecsFromPrev)
	{

		// this.activityID = activityID;

		String splittedwlci[] = workingLevelCatIDs.split("__");
		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id

		this.locationIDs = new ArrayList<Integer>(locationIDs);

		this.activityName = splittedwlci[0];// String.valueOf(activityID);// activityName;
		this.locationName = locationName;
		this.startTimestampInms = startTimestamp.getTime();
		this.endTimestampInms = startTimestamp.getTime();
		this.startLatitude = startLatitude;
		this.startLongitude = startLongitude;
		this.startAltitude = startAltitude;
		this.userID = userID;
		this.photos_count = photos_count;
		this.checkins_count = checkins_count;
		this.users_count = users_count;
		this.radius_meters = radius_meters;
		this.highlights_count = highlights_count;
		this.items_count = items_count;
		this.max_items_count = max_items_count;
		this.workingLevelCatIDs = workingLevelCatIDs;

		this.distInMFromPrev = distanceInMFromPrev;
		this.durInSecFromPrev = durationInSecsFromPrev;

	}

	/**
	 * Constructor for Gowalla
	 * 
	 * @param activityID
	 * @param locationIDs
	 *            list of locations id. this will >1 in case of mergers. note: we store the location id in the activity
	 *            object as Set and not as List
	 * @param activityName
	 * @param locationName
	 * @param startTimestamp
	 * @param startLatitude
	 * @param startLongitude
	 * @param startAltitude
	 * @param userID
	 * @param photos_count
	 * @param checkins_count
	 * @param users_count
	 * @param radius_meters
	 * @param highlights_count
	 * @param items_count
	 * @param max_items_count
	 * @param workingLevelCatIDs
	 * @param distanceInMFromPrev
	 * @param durationInSecsFromPrev
	 * @param levelWiseCatIDs
	 * @param timeZoneId
	 */
	public ActivityObject2018(int activityID, ArrayList<Integer> locationIDs, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromPrev,
			long durationInSecsFromPrev, String[] levelWiseCatIDs, ZoneId timeZoneId)
	{
		// this.activityID = activityID;
		this.timeZoneId = timeZoneId;
		String splittedwlci[] = RegexUtils.patternDoubleUnderScore.split(workingLevelCatIDs);// workingLevelCatIDs.split("__");
		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id
		this.locationIDs = new ArrayList<>(locationIDs);

		this.activityName = splittedwlci[0];// String.valueOf(activityID);// activityName;
		this.locationName = locationName;
		this.startTimestampInms = startTimestamp.getTime();
		this.endTimestampInms = startTimestamp.getTime();
		this.startLatitude = startLatitude;
		this.startLongitude = startLongitude;
		this.startAltitude = startAltitude;
		this.userID = userID;
		this.photos_count = photos_count;
		this.checkins_count = checkins_count;
		this.users_count = users_count;
		this.radius_meters = radius_meters;
		this.highlights_count = highlights_count;
		this.items_count = items_count;
		this.max_items_count = max_items_count;
		this.workingLevelCatIDs = workingLevelCatIDs;

		this.distInMFromPrev = distanceInMFromPrev;
		this.durInSecFromPrev = durationInSecsFromPrev;
		// this.levelWiseCatIDs = levelWiseCatIDs;
	}

	/**
	 * USED AS OF DEC 2018
	 * 
	 * @param activityID
	 * @param locationIDs
	 * @param activityName
	 * @param locationName
	 * @param startTimestamp
	 * @param startLatitude
	 * @param startLongitude
	 * @param startAltitude
	 * @param userID
	 * @param photos_count
	 * @param checkins_count
	 * @param users_count
	 * @param radius_meters
	 * @param highlights_count
	 * @param items_count
	 * @param max_items_count
	 * @param workingLevelCatIDs
	 * @param distanceInMFromPrev
	 * @param durationInSecsFromPrev
	 * @param timeZoneId
	 * @param distanceInMFromNext
	 * @param durationInSecFromNext
	 * @param gridID
	 *            introduced on 13 July 2018
	 * @since 13 July 2018
	 */
	public ActivityObject2018(int activityID, ArrayList<Integer> locationIDs, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromPrev,
			long durationInSecsFromPrev, ZoneId timeZoneId, double distanceInMFromNext, long durationInSecFromNext,
			int gridID)
	{
		this(activityID, locationIDs, activityName, locationName, startTimestamp, startLatitude, startLongitude,
				startAltitude, userID, photos_count, checkins_count, users_count, radius_meters, highlights_count,
				items_count, max_items_count, workingLevelCatIDs, distanceInMFromPrev, durationInSecsFromPrev,
				timeZoneId, distanceInMFromNext, durationInSecFromNext);

		this.gridIndex = gridID;
	}

	/**
	 * 
	 * @param activityID
	 * @param locationIDs
	 * @param activityName
	 * @param locationName
	 * @param startTimestamp
	 * @param startLatitude
	 * @param startLongitude
	 * @param startAltitude
	 * @param userID
	 * @param photos_count
	 * @param checkins_count
	 * @param users_count
	 * @param radius_meters
	 * @param highlights_count
	 * @param items_count
	 * @param max_items_count
	 * @param workingLevelCatIDs
	 * @param distanceInMFromPrev
	 * @param durationInSecsFromPrev
	 * @param timeZoneId
	 * @param distanceInMFromNext
	 * @param durationInSecFromNext
	 *            <p>
	 * @until used until 12 July 2018
	 */
	public ActivityObject2018(int activityID, ArrayList<Integer> locationIDs, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromPrev,
			long durationInSecsFromPrev, ZoneId timeZoneId, double distanceInMFromNext, long durationInSecFromNext)
	{// this.activityID = activityID;
		this.timeZoneId = timeZoneId;
		String splittedwlci[] = RegexUtils.patternDoubleUnderScore.split(workingLevelCatIDs);// workingLevelCatIDs.split("__");
		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id
		this.locationIDs = new ArrayList<>(locationIDs);

		this.activityName = splittedwlci[0];// String.valueOf(activityID);// activityName;
		this.locationName = locationName;
		this.startTimestampInms = startTimestamp.getTime();
		this.endTimestampInms = startTimestamp.getTime();
		this.startLatitude = startLatitude;
		this.startLongitude = startLongitude;
		this.startAltitude = startAltitude;
		this.userID = userID;
		this.photos_count = photos_count;
		this.checkins_count = checkins_count;
		this.users_count = users_count;
		this.radius_meters = radius_meters;
		this.highlights_count = highlights_count;
		this.items_count = items_count;
		this.max_items_count = max_items_count;
		this.workingLevelCatIDs = workingLevelCatIDs;

		this.distInMFromPrev = distanceInMFromPrev;
		this.durInSecFromPrev = durationInSecsFromPrev;
		// this.levelWiseCatIDs = levelWiseCatIDs;

		this.distInMFromNext = distanceInMFromNext;
		this.durInSecFromNext = durationInSecFromNext;

	}

	/**
	 * Constructor for Gowalla
	 * 
	 * @param activityID
	 * @param locationIDs
	 *            list of locations id. this will >1 in case of mergers. note: we store the location id in the activity
	 *            object as Set and not as List
	 * @param activityName
	 * @param locationName
	 * @param startTimestamp
	 * @param startLatitude
	 * @param startLongitude
	 * @param startAltitude
	 * @param userID
	 * @param photos_count
	 * @param checkins_count
	 * @param users_count
	 * @param radius_meters
	 * @param highlights_count
	 * @param items_count
	 * @param max_items_count
	 * @param workingLevelCatIDs
	 * @param distanceInMFromPrev
	 * @param durationInSecsFromPrev
	 * @param levelWiseCatIDs
	 */
	public ActivityObject2018(int activityID, ArrayList<Integer> locationIDs, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromPrev,
			long durationInSecsFromPrev, String[] levelWiseCatIDs)
	{
		// this.activityID = activityID;
		String splittedwlci[] = RegexUtils.patternDoubleUnderScore.split(workingLevelCatIDs);// workingLevelCatIDs.split("__");
		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id
		this.locationIDs = new ArrayList<>(locationIDs);

		this.activityName = splittedwlci[0];// String.valueOf(activityID);// activityName;
		this.locationName = locationName;
		this.startTimestampInms = startTimestamp.getTime();
		this.endTimestampInms = startTimestamp.getTime();
		this.startLatitude = startLatitude;
		this.startLongitude = startLongitude;
		this.startAltitude = startAltitude;
		this.userID = userID;
		this.photos_count = photos_count;
		this.checkins_count = checkins_count;
		this.users_count = users_count;
		this.radius_meters = radius_meters;
		this.highlights_count = highlights_count;
		this.items_count = items_count;
		this.max_items_count = max_items_count;
		this.workingLevelCatIDs = workingLevelCatIDs;

		this.distInMFromPrev = distanceInMFromPrev;
		this.durInSecFromPrev = durationInSecsFromPrev;
		// this.levelWiseCatIDs = levelWiseCatIDs;
	}

	public double getDistanceInMFromPrev()
	{
		return distInMFromPrev;
	}

	public void setDistanceInMFromPrev(double distanceInMFromPrev)
	{
		this.distInMFromPrev = distanceInMFromPrev;
	}

	public long getDurationInSecondsFromPrev()
	{
		return durInSecFromPrev;
	}

	public void setDurationInSecondsFromPrev(long durationInSecondsFromPrev)
	{
		this.durInSecFromPrev = durationInSecondsFromPrev;
	}

	public String toStringAll()
	{
		return "ActivityObject [dimensions=" + dimensions + ", dimensionIDNameValues=" + dimensionIDNameValues
				+ ", activityID=" + activityID + ", locationID=" + this.getLocationIDs('-') + ", activityName="
				+ activityName + ", locationName=" + locationName + ", workingLevelCatIDs=" + workingLevelCatIDs
				+ ", startTimestamp=" + startTimestampInms + ", endTimestamp=" + endTimestampInms
				+ ", durationInSeconds=" + durationInSeconds + ", startLatitude=" + startLatitude + ", endLatitude="
				+ endLatitude + ", startLongitude=" + startLongitude + ", endLongitude=" + endLongitude
				+ ", startAltitude=" + startAltitude + ", endAltitude=" + endAltitude + ", avgAltitude=" + avgAltitude
				+ ", distanceTravelled=" + distanceTravelledInKm + ", userID=" + userID + ", photos_count="
				+ photos_count + ", checkins_count=" + checkins_count + ", users_count=" + users_count
				+ ", radius_meters=" + radius_meters + ", highlights_count=" + highlights_count + ", items_count="
				+ items_count + ", max_items_count=" + max_items_count + "]";
	}

	public String toStringAllGowalla()
	{
		return "activityID=" + activityID + "__locationID="
				+ this.getLocationIDs('-') /*
											 * + "__activityName=" + activityName + "__ locationName=" + locationName
											 */ + "__workLvlCat=" + workingLevelCatIDs + "__startTS="
				+ startTimestampInms + "__startLat=" + startLatitude + "__startLon="
				+ startLongitude /*
									 * + "__ startAlt=" + startAltitude
									 */ + "__userID=" + userID + "__photos_count=" + photos_count + "__cins_count="
				+ checkins_count + "__users_count=" + users_count + "__radius_m=" + radius_meters + "__highlts_count="
				+ highlights_count + "__items_count=" + items_count + "__max_items_count=" + max_items_count
				+ "__distPrev=" + distInMFromPrev + "__durPrev=" + durInSecFromPrev + "__gridID" + gridIndex;
	}

	public String toStringAllGowallaTS()
	{
		return "actID=" + activityID + "__locID="
				+ this.getLocationIDs('-') /*
											 * + "__activityName=" + activityName + "__ locationName=" + locationName
											 */ + "__workLvlCat=" + workingLevelCatIDs + "__stTS="
				+ Instant.ofEpochMilli(startTimestampInms).toString()
				// + LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestampInms), ZoneId.systemDefault())
				+ "__stLat=" + startLatitude + "__stLon=" + startLongitude /*
																			 * + "__ startAlt=" + startAltitude
																			 */ + "__uID=" + userID + "__photos_c="
				+ photos_count + "__cins_c=" + checkins_count + "__users_c=" + users_count + "__radius_m="
				+ radius_meters + "__highlts_count=" + highlights_count + "__items_c=" + items_count + "__max_items_c="
				+ max_items_count + "__distPrev=" + distInMFromPrev + "__durPrev=" + durInSecFromPrev + "__gridID"
				+ gridIndex;
	}

	public String toStringAllGowallaTSWithName()
	{

		// if (DomainConstants.getLocIDLocationObjectDictionary() == null)
		// {
		// System.out.println("Error: DomainConstants.locIDLocationObjectDictionary ==null");
		// }
		String locationName = locationIDs.stream().map(lid -> DomainConstants.getLocationIDNameDictionary().get(lid))
				.collect(Collectors.joining("-"));
		// .getLocIDLocationObjectDictionary().get(lid).locationName)

		return "actID=" + activityID + "__locID=" + this.getLocationIDs('-') + "__activityName="
				+ DomainConstants.catIDNameDictionary.get(activityID) + "__ locationName=" + locationName
				+ "__workLvlCat=" + workingLevelCatIDs + "__stTS=" + Instant.ofEpochMilli(startTimestampInms).toString()
				// + LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestampInms), ZoneId.systemDefault())
				+ "__stLat=" + startLatitude + "__stLon=" + startLongitude /*
																			 * + "__ startAlt=" + startAltitude
																			 */ + "__uID=" + userID + "__photos_c="
				+ photos_count + "__cins_c=" + checkins_count + "__users_c=" + users_count + "__radius_m="
				+ radius_meters + "__highlts_count=" + highlights_count + "__items_c=" + items_count + "__max_items_c="
				+ max_items_count + "__distPrev=" + distInMFromPrev + "__durPrev=" + durInSecFromPrev + "__gridID"
				+ gridIndex;
	}

	/**
	 * 
	 * @param delimiter
	 * @return
	 */
	public String toStringAllGowallaTSWithNameForHeaded(String delimiter)
	{

		// PopUps.showMessage("locationIDs = " + locationIDs);
		// PopUps.showMessage(
		// "DomainConstants.getLocationIDNameDictionary() = " + DomainConstants.getLocationIDNameDictionary());
		String locationName = locationIDs == null ? ""
				: locationIDs.stream().map(lid -> DomainConstants.getLocationIDNameDictionary().get(lid))
						.collect(Collectors.joining("-"));
		// .getLocIDLocationObjectDictionary().get(lid).locationName)

		String additionalGeolifeFeatures = Constant.getDatabaseName().equals("geolife1")
				? delimiter + this.durationInSeconds + delimiter + this.distanceTravelledInKm + delimiter
						+ this.startLongitude + delimiter + this.startLongitude + delimiter + this.endLatitude
						+ delimiter + this.endLongitude + delimiter + this.avgAltitude
				: "";

		String additionalDCUFeatures = Constant.getDatabaseName().equals("dcu_data_2")
				? delimiter + this.durationInSeconds
				: "";

		return activityID + delimiter + this.getLocationIDs('-') + delimiter
				+ DomainConstants.catIDNameDictionary.get(activityID) + delimiter + locationName + delimiter
				+ workingLevelCatIDs + delimiter + Instant.ofEpochMilli(startTimestampInms).toString()
				// + LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestampInms), ZoneId.systemDefault())
				+ delimiter + startLatitude + delimiter + startLongitude
				/* + "__ startAlt=" + startAltitude */ + delimiter + userID + delimiter + photos_count + delimiter
				+ checkins_count + delimiter + users_count + delimiter + radius_meters + delimiter + highlights_count
				+ delimiter + items_count + delimiter + max_items_count + delimiter + distInMFromPrev + delimiter
				+ durInSecFromPrev + delimiter + gridIndex + additionalGeolifeFeatures + additionalDCUFeatures;
	}

	/**
	 * 
	 * @param delimiter
	 * @return
	 * @since 24 Dec 2018
	 */
	public String toStringAllGowallaTSWithNameForHeaded24Dec(String delimiter)
	{
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(4);
		// System.out.println(df.format(myvalue));

		// PopUps.showMessage("locationIDs = " + locationIDs);
		// PopUps.showMessage(
		// "DomainConstants.getLocationIDNameDictionary() = " + DomainConstants.getLocationIDNameDictionary());
		String locationName = locationIDs == null ? ""
				: locationIDs.stream().map(lid -> DomainConstants.getLocationIDNameDictionary().get(lid))
						.collect(Collectors.joining("-"));
		// .getLocIDLocationObjectDictionary().get(lid).locationName)

		String additionalGeolifeFeatures = Constant.getDatabaseName().equals("geolife1")
				? delimiter + this.durationInSeconds + delimiter + df.format(this.distanceTravelledInKm) + delimiter
						+ this.startLatitude + delimiter + this.startLongitude + delimiter + this.endLatitude
						+ delimiter + this.endLongitude + delimiter + this.avgAltitude
				: "";

		String additionalDCUFeatures = Constant.getDatabaseName().equals("dcu_data_2")
				? delimiter + this.durationInSeconds
				: "";

		String additionalGowallaFeatures = Constant.getDatabaseName().equals("gowalla1")
				? delimiter + workingLevelCatIDs + delimiter + startLatitude + delimiter + startLongitude + delimiter
						+ photos_count + delimiter + checkins_count + delimiter + users_count + delimiter
						+ radius_meters + delimiter + highlights_count + delimiter + items_count + delimiter
						+ max_items_count + delimiter + gridIndex + delimiter + this.getLocationIDs('-') + delimiter
						+ locationName
				: "";

		Timestamp startTS = new Timestamp(startTimestampInms);
		int hourOfDay = startTS.getHours();
		int weekDay = startTS.getDay();
		// String tsISOString = startTS.toInstant().toString();
		String dateOnly = new Date(startTimestampInms).toString();

		String brokenDownTS = weekDay + delimiter + dateOnly + delimiter + hourOfDay;

		return userID + delimiter + brokenDownTS + delimiter + activityID + delimiter
				+ DomainConstants.catIDNameDictionary.get(activityID) + delimiter
				+ Instant.ofEpochMilli(startTimestampInms).toString()
				// + LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestampInms), ZoneId.systemDefault())
				+ additionalGowallaFeatures + delimiter + df.format(distInMFromPrev) + delimiter + durInSecFromPrev
				+ additionalGeolifeFeatures + additionalDCUFeatures;
	}

	/**
	 * Header for writing activity objc
	 * 
	 * @param delimiter
	 * @return
	 */
	public static String getHeaderForStringAllGowallaTSWithNameForHeaded(String delimiter)
	{
		String additionalGeolifeFeatures = Constant.getDatabaseName().equals("geolife1")
				? delimiter + "durationInSeconds" + delimiter + "distanceTravelled" + delimiter + "startLongitude"
						+ delimiter + "startLongitude" + delimiter + "endLatitude" + delimiter + "endLongitude"
						+ delimiter + "avgAltitude"
				: "";

		String additionalDCUFeatures = Constant.getDatabaseName().equals("dcu_data_2") ? delimiter + "durationInSeconds"
				: "";

		return "actID" + delimiter + "locID" + delimiter + "activityName" + delimiter + "locationName" + delimiter
				+ "workLvlCat" + delimiter + "stTS" + delimiter + "stLat" + delimiter + "stLon" + delimiter + "uID"
				+ delimiter + "photos_c" + delimiter + "cins_c" + delimiter + "users_c" + delimiter + "radius_m"
				+ delimiter + "highlts_count" + delimiter + "items_c" + delimiter + "max_items_c" + delimiter
				+ "distPrev" + delimiter + "durPrev" + delimiter + "gridID" + additionalGeolifeFeatures
				+ additionalDCUFeatures;
	}

	/**
	 * Header for writing activity objc
	 * 
	 * @param delimiter
	 * @return
	 */
	public static String getHeaderForStringAllGowallaTSWithNameForHeaded24Dec(String delimiter)
	{
		String additionalGeolifeFeatures = Constant.getDatabaseName().equals("geolife1")
				? delimiter + "durationInSeconds" + delimiter + "distanceTravelledInKm" + delimiter + "startLatitude"
						+ delimiter + "startLongitude" + delimiter + "endLatitude" + delimiter + "endLongitude"
						+ delimiter + "avgAltitude"
				: "";

		String additionalDCUFeatures = Constant.getDatabaseName().equals("dcu_data_2") ? delimiter + "durationInSeconds"
				: "";

		String additionalGowallaFeatures = Constant.getDatabaseName().equals("gowalla1")
				? delimiter + "workLvlCat" + delimiter + "stLat" + delimiter + "stLon" + delimiter + "photos_c"
						+ delimiter + "cins_c" + delimiter + "users_c" + delimiter + "radius_m" + delimiter
						+ "highlts_count" + delimiter + "items_c" + delimiter + "max_items_c" + delimiter + "gridID"
						+ delimiter + "locID" + delimiter + "locationName"

				// + "startLatitude" + delimiter + "startLongitude" + delimiter + "userID" + delimiter
				// + "photos_count" + delimiter + "checkins_count" + delimiter + "users_count" + delimiter
				// + "radius_meters" + delimiter + "highlights_count" + delimiter + "items_count" + delimiter
				// + "max_items_count"
				: "";

		String brokenDownTS = "weekDay" + delimiter + "dateOnly" + delimiter + "hourOfDay";

		return "uID" + delimiter + brokenDownTS + delimiter + "actID" + delimiter + "activityName" + delimiter + "stTS"
				+ delimiter + "distInMPrev" + delimiter + "durInSecPrev" + additionalGeolifeFeatures
				+ additionalDCUFeatures;
	}

	/**
	 * Header for writing activity objc
	 * 
	 * @param delimiter
	 * @return
	 */
	public String getHeaderForStringAllGeolifeWithNameForHeaded(String delimiter)
	{

		return activityName + delimiter + startTimestampInms + delimiter + durationInSeconds + delimiter
				+ distanceTravelledInKm + delimiter + startLatitude + delimiter + startLongitude + delimiter
				+ endLatitude + delimiter + endLongitude + delimiter + avgAltitude;
	}

	/**
	 * Header for writing activity objc
	 * 
	 * @param delimiter
	 * @return
	 */
	public static String getHeaderForStringAllGeolifeWithNameForHeaded2(ActivityObject2018 ao, String delimiter)
	{
		long secsSinceMidnight = DateTimeUtils.getTimeInDayInSecondsZoned(ao.getStartTimestampInms(), ZoneId.of("UTC"));
		long startGeo = ao.getStartLatitude() == null ? -9999
				: HilbertCurveUtils.getCompactHilbertCurveIndex(ao.getStartLatitude(), ao.getStartLongitude());
		long endGeo = ao.getEndLatitude() == null ? -9999
				: HilbertCurveUtils.getCompactHilbertCurveIndex(ao.getEndLatitude(), ao.getEndLongitude());

		return secsSinceMidnight + delimiter + ao.getDurationInSeconds() + delimiter + ao.getDistanceTravelled()
				+ delimiter + /* ao.getStartLatitude() + delimiter + ao.getStartLongitude() + delimiter + */startGeo
				+ delimiter + /* ao.getEndLatitude() + delimiter + ao.getEndLongitude() + delimiter + */ endGeo
				+ delimiter + ao.getAvgAltitude() + delimiter + ao.getActivityName();
	}

	public String toString()
	{
		if (Constant.getDatabaseName().equals("dcu_data_2"))// // ;"geolife1";// default database name, dcu_data_2";/
			return activityName + "-" + startTimestampInms + "-" + durationInSeconds;// +" -"+startLatitude+",";
		else if (Constant.getDatabaseName().equals("geolife1"))
			return activityName + "-" + startTimestampInms + "-" + durationInSeconds + " -" + startLatitude + "-"
					+ startLongitude + "-" + endLatitude + "-" + endLongitude + "-" + avgAltitude;
		else if (Constant.getDatabaseName().equals("gowalla1"))
			return this.toStringAllGowalla();
		else
			return "empty";
	}

	// public double getDistanceTravelledInActivityObject()
	// {
	// // double distanceTravelled =-99;
	//
	// return UtilityBelt.haversine(startLatitude, startLongitude, endLatitude, endLongitude);
	//
	//
	// }

	/**
	 * 
	 * @param ao2
	 * @return haversine distance
	 */
	public double getDifferenceStartingGeoCoordinates(ActivityObject2018 ao2)
	{
		return SpatialUtils.haversine(startLatitude, startLongitude, ao2.getStartLatitude(), ao2.getStartLongitude());

	}

	/**
	 * 
	 * @param ao2
	 * @return haversine distance
	 */
	public double getDifferenceEndingGeoCoordinates(ActivityObject2018 ao2)
	{
		return SpatialUtils.haversine(endLatitude, endLongitude, ao2.getEndLatitude(), ao2.getEndLongitude());

	}

	public double getDifferenceAltitude(ActivityObject2018 ao2)
	{
		return Double.parseDouble(this.getAvgAltitude()) - Double.parseDouble(ao2.getAvgAltitude());

	}

	public static String getArrayListOfActivityObjectsAsString(ArrayList<ActivityObject2018> arr)
	{
		StringBuffer str = new StringBuffer("");

		for (ActivityObject2018 ao : arr)
		{
			str.append(">>" + ao.toString());
		}
		return str.toString();
	}

	/**
	 * Creates an Activity Object given the values for the dimension Id in the form of Map of <DimensionID Name,
	 * correspoding dimensions's value></br>
	 * <font color="orange"> Note: used for creating Activity Objects for Timeline from raw data. </font>
	 * 
	 * @param dimensionIDNameValues
	 */
	public ActivityObject2018(HashMap<String, String> dimensionIDNameValues) // (User_ID, 0), (Location_ID, 10100), ...
	{
		// System.out.println("Inside ActivityObject contructor"); //@toremoveatruntime

		// ////////create an ArrayList of Dimension Objects(created using dimension name with values) for this
		// ActivityObject
		this.dimensions = new ArrayList<Dimension>();

		for (Map.Entry<String, String> dimensionIDNameValue : dimensionIDNameValues.entrySet())
		{
			// System.out.println("dimensionIDName:" + dimensionIDNameValue.getKey() + " dimensionIDValue:" +
			// dimensionIDNameValue.getValue());
			String dimensionName = UtilityBelt.getDimensionNameFromDimenionIDName(dimensionIDNameValue.getKey());

			dimensions.add(new Dimension(dimensionName, dimensionIDNameValue.getValue()));

			this.dimensionIDNameValues = dimensionIDNameValues; // check if it works correctly without allocating memory
																// to the new hashmap.
		}
		// /////////
		// System.out.println("dimensionIDNameValues created");

		// storing it as class attribute to minimize number of sql requests otherwise
		this.activityName = getDimensionAttributeValue("Activity_Dimension", "Activity_Name").toString();
		String startTimeString = getDimensionAttributeValue("Time_Dimension", "Start_Time").toString();

		// System.out.println("getDimensionAttributeValue(Date_Dimension,Date) is null:
		// "+getDimensionAttributeValue("Date_Dimension","Date") == null);
		// System.out.println("getDimensionAttributeValue(Time_Dimension,Date) is null:
		// "+getDimensionAttributeValue("Time_Dimension","Time") == null);

		String startDateString = getDimensionAttributeValue("Date_Dimension", "Date").toString();// dateString in iiWAS
																									// version

		String endTimeString = getDimensionAttributeValue("Time_Dimension", "End_Time").toString();
		String endDateString; // not present in iiWAS version
		/**
		 * Not in DCU_Dataset
		 */
		if (Constant.getDatabaseName().equalsIgnoreCase("dcu_data_2"))// (Constant.DATABASE_NAME.equalsIgnoreCase("dcu_data_2"))
		{
			endDateString = getDimensionAttributeValue("Date_Dimension", "Date").toString();
			// because in DCU dataset all Activity objects are broken over days
		}
		else
		{// geolife1
			endDateString = getDimensionAttributeValue("Date_Dimension", "End_Date").toString();
		}
		// String durationInSecondsString = getDimensionAttributeValue("Time_Dimension","End_Time").toString();
		this.locationName = getDimensionAttributeValue("Location_Dimension", "Location_Name").toString();
		if (Constant.getDatabaseName().equalsIgnoreCase("dcu_data_2") == false)// (Constant.DATABASE_NAME.equalsIgnoreCase("dcu_data_2")
																				// == false)
		{
			this.startLatitude = getDimensionAttributeValue("Location_Dimension", "Start_Latitude").toString();
			this.endLatitude = getDimensionAttributeValue("Location_Dimension", "End_Latitude").toString();

			this.startLongitude = getDimensionAttributeValue("Location_Dimension", "Start_Longitude").toString();
			this.endLongitude = getDimensionAttributeValue("Location_Dimension", "End_Longitude").toString();

			this.startAltitude = getDimensionAttributeValue("Location_Dimension", "Start_Altitude").toString();
			this.endAltitude = getDimensionAttributeValue("Location_Dimension", "End_Altitude").toString();

			this.avgAltitude = getDimensionAttributeValue("Location_Dimension", "Avg_Altitude").toString();

			this.distanceTravelledInKm = SpatialUtils.haversine(startLatitude, startLongitude, endLatitude,
					endLongitude);

			if (distanceTravelledInKm > Constant.distanceTravelledAlert
					&& SanityConstants.checkForDistanceTravelledAnomaly)
			{
				System.out.println("Notice: distance travelled (high) = " + distanceTravelledInKm
						+ " for transportation mode = " + activityName);
			}
		}
		// THIS IS TIME NOT TIMESTAMP..AS DATE IS SAME
		this.startTimestampInms = DateTimeUtils.getTimestampAsLongms(startTimeString, startDateString); // in iiWAS ver,
		// dateString
		// is used here instead of
		// startDateString
		this.endTimestampInms = DateTimeUtils.getTimestampAsLongms(endTimeString, endDateString);// in iiWAS ver,
																									// dateString is
		// used
		// here instead of
		// endDateString

		this.durationInSeconds = (this.endTimestampInms - this.startTimestampInms) / 1000 + 1;
		// +1 because 1 seconds
		// was decremented while loading data for resolving consecutive activities primarliy for visualisation

		if (this.durationInSeconds < 0)
		{
			System.err.println(
					"Error: Negative duration in seconds:startTimestamp=" + startTimestampInms + " endTimestamp="
							+ endTimestampInms + " i.e., " + this.endTimestampInms + "-" + this.startTimestampInms);
			System.err.println("\t\t StartDateString:" + startDateString + " StartTimeString:" + startTimeString
					+ "\n\t\t EndDateString:" + endDateString + " EndTimeString:" + endTimeString);
		}

		// System.out.println("Exiting ActivityObject contructor-----------");
		// System.out.println("Activity Event Create: number of dimensions"+dimensions.size()); // Debug Info: count the
		// occurence of this in output to see if the number of activity events generated is correct:
		// checked(on 27 June 1pm) 887 for Tessa and Yakub
	}

	/// Start of Nov 14 2018
	/**
	 * Created to instantiate RepAO for Geolife
	 * 
	 * @param userID1
	 * @param activityID1
	 * @param activityName1
	 * @param workingLevelCatIDs1
	 * @param locationIDs1
	 * @param locationName1
	 * @param startLatitude1
	 * @param endLatitude1
	 * @param startLongitude1
	 * @param endLongitude1
	 * @param avgAltitude1
	 * @param distanceTravelled1
	 * @param startTimestampInms1
	 * @param endTimestampInms1
	 * @param durationInSeconds1
	 * @since 21 Dec 2018
	 */
	public ActivityObject2018(String userID1, int activityID1, String activityName1, String workingLevelCatIDs1,
			ArrayList<Integer> locationIDs1, String locationName1, String startLatitude1, String endLatitude1,
			String startLongitude1, String endLongitude1, String avgAltitude1, double distanceTravelled1,
			long startTimestampInms1, long endTimestampInms1, long durationInSeconds1)
	{
		this.userID = userID1;
		this.activityID = activityID1;

		this.activityName = activityName1;
		this.workingLevelCatIDs = workingLevelCatIDs1;
		this.locationIDs = locationIDs1;
		this.locationName = locationName1;
		this.startLatitude = startLatitude1;
		this.endLatitude = endLatitude1;

		this.startLongitude = startLongitude1;
		this.endLongitude = endLongitude1;

		// this.startAltitude = startAltitude1;
		// this.endAltitude = endAltitude1;

		this.avgAltitude = avgAltitude1;

		this.distanceTravelledInKm = distanceTravelled1;

		this.startTimestampInms = startTimestampInms1;
		// DateTimeUtils.getTimestampAsLongms(startTimeString, startDateString); // in iiWAS ver,
		// dateString
		// is used here instead of
		// startDateString
		this.endTimestampInms = endTimestampInms1;

		// DateTimeUtils.getTimestampAsLongms(endTimeString, endDateString);// in iiWAS ver,
		// dateString is
		// used
		// here instead of
		// endDateString

		this.durationInSeconds = durationInSeconds1;
		this.timeZoneId = ZoneId.of("UTC");

	}

	public ActivityObject2018(ActivityObject oldAO) // (User_ID, 0),
	// (Location_ID,
	// 10100), ...
	{
		System.out.println("\nInside ActivityObject converter contructor"); // @toremoveatruntime
		System.out.println("oldAO= \n" + oldAO.toString());
		System.out.println("------------");
		oldAO.traverseDimensionIDNameValues();
		// Location_ID getDimensionAttributeValue: 128 Date_ID getDimensionAttributeValue: 252 Activity_ID
		// getDimensionAttributeValue: 3 Time_ID getDimensionAttributeValue: 252 User_ID getDimensionAttributeValue: 10
		this.userID = oldAO.getDimensionIDValue("User_ID");
		this.activityID = Integer.valueOf(oldAO.getDimensionIDValue("Activity_ID"));
		this.workingLevelCatIDs = String.valueOf(activityID);
		this.locationIDs = new ArrayList<>(1);
		this.locationIDs.add(Integer.valueOf(oldAO.getDimensionIDValue("Location_ID")));
		System.out.println("------------");

		// ////////create an ArrayList of Dimension Objects(created using dimension name with values) for this
		// ActivityObject
		// this.dimensions = new ArrayList<Dimension>();
		// for (Map.Entry<String, String> dimensionIDNameValue : dimensionIDNameValues.entrySet())
		// {
		// // System.out.println("dimensionIDName:" + dimensionIDNameValue.getKey() + " dimensionIDValue:" +
		// // dimensionIDNameValue.getValue());
		// String dimensionName = UtilityBelt.getDimensionNameFromDimenionIDName(dimensionIDNameValue.getKey());
		// dimensions.add(new Dimension(dimensionName, dimensionIDNameValue.getValue()));
		// this.dimensionIDNameValues = dimensionIDNameValues; // check if it works correctly without allocating memory
		// // to the new hashmap.
		// }
		// /////////
		// System.out.println("dimensionIDNameValues created");

		// storing it as class attribute to minimize number of sql requests otherwise
		this.activityName = oldAO.getActivityName();

		// getDimensionAttributeValue("Activity_Dimension", "Activity_Name").toString();
		// String startTimeString = getDimensionAttributeValue("Time_Dimension", "Start_Time").toString();

		// System.out.println("getDimensionAttributeValue(Date_Dimension,Date) is null:
		// "+getDimensionAttributeValue("Date_Dimension","Date") == null);
		// System.out.println("getDimensionAttributeValue(Time_Dimension,Date) is null:
		// "+getDimensionAttributeValue("Time_Dimension","Time") == null);
		// String startDateString = getDimensionAttributeValue("Date_Dimension", "Date").toString();// dateString in
		// iiWAS version
		// String endTimeString = getDimensionAttributeValue("Time_Dimension", "End_Time").toString();
		// String endDateString; // not present in iiWAS version
		/**
		 * Not in DCU_Dataset
		 */
		// if (Constant.getDatabaseName().equalsIgnoreCase("dcu_data_2"))//
		// (Constant.DATABASE_NAME.equalsIgnoreCase("dcu_data_2"))
		// {
		// endDateString = getDimensionAttributeValue("Date_Dimension", "Date").toString();
		// // because in DCU dataset all Activity objects are broken over days
		// }
		// else
		// {// geolife1
		// endDateString = getDimensionAttributeValue("Date_Dimension", "End_Date").toString();
		// }
		// String durationInSecondsString = getDimensionAttributeValue("Time_Dimension","End_Time").toString();
		this.locationName = oldAO.getDimensionIDValue("Location_ID");// oldAO.getLocationName(); since location names
																		// were NA
		// getDimensionAttributeValue("Location_Dimension", "Location_Name").toString();
		if (Constant.getDatabaseName().equalsIgnoreCase("geolife1"))
		// (Constant.DATABASE_NAME.equalsIgnoreCase("dcu_data_2") == false)
		{
			this.startLatitude = oldAO.getStartLatitude();
			this.endLatitude = oldAO.getEndLatitude();

			this.startLongitude = oldAO.getStartLongitude();
			this.endLongitude = oldAO.getEndLongitude();

			this.startAltitude = oldAO.getStartAltitude();
			this.endAltitude = oldAO.getEndAltitude();

			this.avgAltitude = oldAO.getAvgAltitude();

			this.distanceTravelledInKm = oldAO.getDistanceTravelled();
			// SpatialUtils.haversine(startLatitude, startLongitude, endLatitude, endLongitude);

			if (distanceTravelledInKm > Constant.distanceTravelledAlert
					&& SanityConstants.checkForDistanceTravelledAnomaly)
			{
				System.out.println("Notice: distance travelled (high) = " + distanceTravelledInKm
						+ " for transportation mode = " + activityName);
			}
		}
		// THIS IS TIME NOT TIMESTAMP..AS DATE IS SAME
		this.startTimestampInms = oldAO.getStartTimestamp().getTime();
		// DateTimeUtils.getTimestampAsLongms(startTimeString, startDateString); // in iiWAS ver,
		// dateString
		// is used here instead of
		// startDateString
		this.endTimestampInms = oldAO.getEndTimestamp().getTime();

		// DateTimeUtils.getTimestampAsLongms(endTimeString, endDateString);// in iiWAS ver,
		// dateString is
		// used
		// here instead of
		// endDateString

		this.durationInSeconds = oldAO.getDurationInSeconds();

		System.out.println("oldAO.getStartTimestamp() = " + oldAO.getStartTimestamp() + " startTimestampInms= "
				+ startTimestampInms);
		System.out.println(
				"oldAO.getEndTimestamp() = " + oldAO.getEndTimestamp() + " endTimestampInms= " + endTimestampInms);
		System.out.println("durationInSeconds" + durationInSeconds);

		System.out.println("endTimestampInms-startTimestampInms=" + (endTimestampInms - startTimestampInms));
		// (this.endTimestampInms - this.startTimestampInms) / 1000 + 1;
		// +1 because 1 seconds
		// was decremented while loading data for resolving consecutive activities primarliy for visualisation

		// if (this.durationInSeconds < 0)
		// {
		// System.err.println(
		// "Error: Negative duration in seconds:startTimestamp=" + startTimestampInms + " endTimestamp="
		// + endTimestampInms + " i.e., " + this.endTimestampInms + "-" + this.startTimestampInms);
		// System.err.println("\t\t StartDateString:" + startDateString + " StartTimeString:" + startTimeString
		// + "\n\t\t EndDateString:" + endDateString + " EndTimeString:" + endTimeString);
		// }

		// System.out.println("Exiting ActivityObject contructor-----------");
		// System.out.println("Activity Event Create: number of dimensions"+dimensions.size()); // Debug Info: count the
		// occurence of this in output to see if the number of activity events generated is correct:
		// checked(on 27 June 1pm) 887 for Tessa and Yakub

		this.timeZoneId = ZoneId.of("UTC");
		System.out.println("new ao=\n" + this.toString() + "\nagain=\n" + this.toStringAll() + "\n");
	}

	/// End of Nov 14 2018

	// Start of Dec 15 2018 for DCU Lifelog dataset
	/**
	 * For DCU lifelog dataset to be directly created from serialised maps.
	 * 
	 * 
	 * @param userID
	 * @param activityName
	 * @param activityID
	 * @param workingLevelCatIDs
	 * @param startTimestampInms
	 * @param endTimestampInms
	 * @param durationInSeconds
	 * @param durInSecFromPrev
	 * @param timeZoneId
	 * 
	 * @since 15 December 2018
	 */
	public ActivityObject2018(String userID, String activityName, int activityID, String workingLevelCatIDs,
			long startTimestampInms, long endTimestampInms, long durationInSeconds, long durInSecFromPrev,
			ZoneId timeZoneId)
	{
		this.userID = userID;
		this.activityName = activityName;
		this.activityID = activityID;
		this.workingLevelCatIDs = workingLevelCatIDs;
		this.startTimestampInms = startTimestampInms;
		this.endTimestampInms = endTimestampInms;
		this.durationInSeconds = durationInSeconds;
		this.durInSecFromPrev = durInSecFromPrev;
		this.timeZoneId = timeZoneId;
		ArrayList<Integer> dummyLocIDs = new ArrayList<>();
		dummyLocIDs.add(-1);
		this.locationIDs = dummyLocIDs;

	}
	// End of Dec 15 2018 for DCU Lifelog dataset

	/**
	 * @return the distanceTravelled
	 */
	public double getDistanceTravelled()
	{
		return distanceTravelledInKm;
	}

	/**
	 * @param distanceTravelled
	 *            the distanceTravelled to set
	 */
	public void setDistanceTravelled(double distanceTravelled)
	{
		this.distanceTravelledInKm = distanceTravelled;
	}

	/**
	 * @return the startLatitude
	 */
	public String getStartLatitude()
	{
		return startLatitude;
	}

	/**
	 * @param startLatitude
	 *            the startLatitude to set
	 */
	public void setStartLatitude(String startLatitude)
	{
		this.startLatitude = startLatitude;
	}

	/**
	 * @return the endLatitude
	 */
	public String getEndLatitude()
	{
		return endLatitude;
	}

	/**
	 * @param endLatitude
	 *            the endLatitude to set
	 */
	public void setEndLatitude(String endLatitude)
	{
		this.endLatitude = endLatitude;
	}

	/**
	 * @return the startLongitude
	 */
	public String getStartLongitude()
	{
		return startLongitude;
	}

	/**
	 * @param startLongitude
	 *            the startLongitude to set
	 */
	public void setStartLongitude(String startLongitude)
	{
		this.startLongitude = startLongitude;
	}

	/**
	 * @return the endLongitude
	 */
	public String getEndLongitude()
	{
		return endLongitude;
	}

	/**
	 * @param endLongitude
	 *            the endLongitude to set
	 */
	public void setEndLongitude(String endLongitude)
	{
		this.endLongitude = endLongitude;
	}

	/**
	 * @return the startAltitude
	 */
	public String getStartAltitude()
	{
		return startAltitude;
	}

	/**
	 * @param startAltitude
	 *            the startAltitude to set
	 */
	public void setStartAltitude(String startAltitude)
	{
		this.startAltitude = startAltitude;
	}

	/**
	 * @return the endAltitude
	 */
	public String getEndAltitude()
	{
		return endAltitude;
	}

	/**
	 * @param endAltitude
	 *            the endAltitude to set
	 */
	public void setEndAltitude(String endAltitude)
	{
		this.endAltitude = endAltitude;
	}

	/**
	 * @return the avgAltitude
	 */
	public String getAvgAltitude()
	{
		return avgAltitude;
	}

	/**
	 * @param avgAltitude
	 *            the avgAltitude to set
	 */
	public void setAvgAltitude(String avgAltitude)
	{
		this.avgAltitude = avgAltitude;
	}

	/**
	 * 
	 * @return
	 */
	public HashSet<Integer> getUniqueLocationIDs()
	{
		return new LinkedHashSet<Integer>(locationIDs);
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<Integer> getLocationIDs()
	{
		return locationIDs;
	}

	public String getLocationIDs(char delimiter)
	{
		// //Before 17 July 2017 Start
		// StringBuilder sb = new StringBuilder();
		// locationIDs.stream().forEach(e -> sb.append(e + delimiter));
		// return sb.toString();
		// //Before 17 July 2017 End

		return locationIDs.stream().map(e -> e.toString()).collect(Collectors.joining(String.valueOf(delimiter)));
	}

	// /**
	// *
	// * @return
	// */
	// public String[] getLevelWiseCatIDs()
	// {
	// return levelWiseCatIDs;
	// }

	// /**
	// * Create empty Activity Object
	// */
	// public ActivityObject()
	// {
	// dimensions = new ArrayList<Dimension>();
	// dimensionIDNameValues = new HashMap<String, String>();
	// activityName = "empty";
	// activityID = -99;
	// this.locationIDs = new LinkedHashSet<Integer>();
	//
	// }

	// String thisConstructorIsForTest
	/**
	 * <font color="red">ONLY FOR TEST/DEBUGGING PURPOSES!</font>
	 * 
	 * @param activityName
	 * @param location
	 * @param durationInSeconds
	 * @param startTimeStamp
	 */
	ActivityObject2018(String activityName, String location, long durationInSeconds, Timestamp startTimeStamp)
	{
		this.activityName = activityName;
		this.durationInSeconds = durationInSeconds;
		this.locationName = location;
		this.startTimestampInms = startTimeStamp.getTime();
	}

	/**
	 * An Activity Object is invalid if the Activity name is 'Unknown' or 'Not Available'
	 * 
	 * @return
	 */
	public boolean isInvalidActivityName()
	{
		return !UtilityBelt.isValidActivityName(this.activityName);
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<Dimension> getDimensions()
	{
		return dimensions;
	}

	// /**
	// * Diabled on 5th April 2017 as not called.
	// * @return
	// */
	// public Timestamp getMiddleTimestamp()
	// {
	// return new Timestamp(startTimestamp.getTime() + (endTimestamp.getTime() - startTimestamp.getTime()) / 2);
	// }

	// public boolean equals(ActivityObject aeToCompare)
	// {
	// boolean $ = true;
	//
	// if (this.activityName != aeToCompare.activityName) return false;
	// return $;
	// }

	// public String getWorkingLevelCatIDs()
	// {
	// return workingLevelCatIDs;
	// }

	/**
	 * Set working level cat id. This is relevant when there exists hierarchy of category IDs.
	 * 
	 * @param workingLevelCatIDs
	 */
	public void setWorkingLevelCatIDs(String workingLevelCatIDs)
	{
		this.workingLevelCatIDs = workingLevelCatIDs;
	}

	/**
	 * @deprecated Used for the iiwas and geolife experiment where the num of unique activities were <=10
	 *             <p>
	 *             Returns the 1-character string code from the Activity Name. This code is derived from the ActivityID
	 *             and hence is guaranteed to be unique for at least 107 activities.
	 * 
	 * @return
	 */
	public String getStringCode_v0()
	{
		/*
		 * String code = new String(); String activityName= this.activityName; int activityID=
		 * generateSyntheticData.getActivityid(activityName); code= Character.toString ((char)(activityID+65));
		 */
		return StringCode.getStringCodeFromActivityName(this.activityName);
	}

	/**
	 * Returns the 1-character string code from the ActivityID and hence is guaranteed to be unique for at least 400
	 * activities.
	 * 
	 * @since 30 Nov 2016
	 * @return
	 */
	public char getCharCodeFromActID()
	{
		// PopUps.printTracedWarningMsg("DebugMar9: look who is calling\n");
		return StringCode.getCharCodeFromActivityID(this.activityID);
	}

	/**
	 * Returns the 1-character string code from the ActivityID and hence is guaranteed to be unique for at least 400
	 * activities.
	 * 
	 * @since 9 March 2018
	 * @return
	 */
	public char getCharCodeV2()
	{
		return Constant.getActIDCharCodeMap().get(this.activityID);
		// return DomainConstants.getCatIDCharCodeMap()(this.activityID);
	}

	public long getDurationInSeconds()
	{
		return this.durationInSeconds;
	}

	// public String getLocationName()
	// {
	// return this.locationName;
	// }

	public String getDimensionIDValue(String dimensionIDName)
	{
		return this.dimensionIDNameValues.get(dimensionIDName);
	}

	/**
	 * // (User_Dimension, // User_Name)
	 * 
	 * @param dimensionName
	 * @param dimensionAttributeName
	 * @return
	 */
	public Object getDimensionAttributeValue(String dimensionName, String dimensionAttributeName)
	{
		Object dimensionAttributeValue = new Object();
		Dimension dimensionToFetch = null;

		for (int i = 0; i < dimensions.size(); i++)
		{
			if (dimensions.get(i).getDimensionName().equalsIgnoreCase(dimensionName))
			{
				dimensionToFetch = dimensions.get(i);
				break;
			}
		}

		if (dimensionToFetch == null)
		{
			System.err.println(PopUps.getTracedErrorMsg("Error in getDimensionAttributeValue() for dimension name = "
					+ dimensionName + ", dimension attribute name = " + dimensionAttributeName
					+ "\n No such dimension found for this activity event."));
			System.exit(2); // Check later if it is wise or unwise to exit in such case
		}

		else
		{
			dimensionAttributeValue = dimensionToFetch.getValueOfDimensionAttribute(dimensionAttributeName);
		}

		return dimensionAttributeValue;
	}

	/**
	 * 
	 * @param name
	 * @param start
	 * @param end
	 */
	public ActivityObject2018(String name, Timestamp start, Timestamp end)
	{
		// userName=user;
		activityName = name;
		startTimestampInms = start.getTime();
		endTimestampInms = end.getTime();
	}

	public String getActivityName()
	{
		return activityName;
	}

	public int getActivityID()
	{
		return this.activityID;
	}

	/**
	 * TODO: change this and corresponding calling methods to work with long
	 * 
	 * @return
	 */
	public Timestamp getStartTimestamp()
	{
		return new Timestamp(startTimestampInms);
	}

	/**
	 * 
	 * @return
	 */
	public long getStartTimestampInms()
	{
		return startTimestampInms;
	}

	/**
	 * TODO: change this and corresponding calling methods to work with long
	 * 
	 * @return
	 */
	public Timestamp getEndTimestamp()
	{
		return new Timestamp(endTimestampInms);
	}

	/**
	 * 
	 * @return
	 */
	public long getEndTimestampInms()
	{
		return endTimestampInms;
	}

	/**
	 * 
	 * @return
	 */
	public LocalDate getEndDate()
	{
		return getEndTimestamp().toLocalDateTime().toLocalDate();/// DateTimeUtils.getDate(endTimestamp);
	}

	// /////////////////////////// To be removed later after refactoring
	public void setActivityName(String name)
	{
		activityName = name;
	}

	public void setStartTimestamp(Timestamp start)
	{
		startTimestampInms = start.getTime();
	}

	public void setEndTimestamp(Timestamp end)
	{
		endTimestampInms = end.getTime();
	}

	public String getUserID()
	{
		return userID;
	}

	public void setUserID(String userID)
	{
		this.userID = userID;
	}

	public int getPhotos_count()
	{
		return photos_count;
	}

	public void setPhotos_count(int photos_count)
	{
		this.photos_count = photos_count;
	}

	/**
	 * this is actually the avg checkins count if this is an activity object created of merged multiple checkins
	 * 
	 * @return
	 */
	public int getCheckins_count()
	{
		// if (checkins_count <= 0)
		// {
		// System.out.println("Alert:for AO name:" + this.getActivityName() + "AO id:" + this.activityID
		// + " checkins count= " + checkins_count + " AO=\n" + this.toStringAllGowallaTSWithName());
		//
		// }
		return checkins_count;
	}

	public void setCheckins_count(int checkins_count)
	{
		this.checkins_count = checkins_count;
	}

	public int getUsers_count()
	{
		return users_count;
	}

	public void setUsers_count(int users_count)
	{
		this.users_count = users_count;
	}

	public int getRadius_meters()
	{
		return radius_meters;
	}

	public void setRadius_meters(int radius_meters)
	{
		this.radius_meters = radius_meters;
	}

	public int getHighlights_count()
	{
		return highlights_count;
	}

	public String getWorkingLevelCatIDs()
	{
		return workingLevelCatIDs;
	}

	public void setHighlights_count(int highlights_count)
	{
		this.highlights_count = highlights_count;
	}

	public int getItems_count()
	{
		return items_count;
	}

	public void setItems_count(int items_count)
	{
		this.items_count = items_count;
	}

	public int getMax_items_count()
	{
		return max_items_count;
	}

	public void setMax_items_count(int max_items_count)
	{
		this.max_items_count = max_items_count;
	}

	// ///////////////////////////////////////////////////////////////////////

	/**
	 * 
	 * @param startInterval
	 * @param endInterval
	 * @return
	 */
	public boolean fullyContainsInterval(Timestamp startInterval, Timestamp endInterval)
	{
		boolean value = false;

		/*
		 * If Activity Event: AAAAAAAAAAAA and interval to check: iiiiii
		 */
		// if(this.startTimestamp.before(startInterval) && this.endTimestamp.after(endInterval))
		if ((this.startTimestampInms <= startInterval.getTime()) && (this.endTimestampInms >= endInterval.getTime()))
		{
			value = true;
		}

		return value;
	}

	/**
	 * TODO: remove creating of Timestamp
	 * 
	 * @param startStampPoint
	 * @return
	 */
	public boolean startsOnOrBefore(Timestamp startStampPoint)
	{
		if (new Timestamp(startTimestampInms).before(startStampPoint)
				|| new Timestamp(startTimestampInms).equals(startStampPoint))
			return true;
		else
			return false;

	}

	// 21Oct
	/**
	 * <p>
	 * Determines if this activity object start-end timestamps overlap with the given start-end timestamps.
	 * </p>
	 * used in matching unit case to fetch timelines, see class Timeline
	 * 
	 * @param startInterval
	 * @param endInterval
	 * @return
	 */
	public boolean doesOverlap(Timestamp startInterval, Timestamp endInterval)
	{
		return ((this.startTimestampInms <= endInterval.getTime())
				&& (this.endTimestampInms >= startInterval.getTime()));
	} // courtesy:http://goo.gl/pnR3p1

	public HashMap<String, String> getDimensionIDNameValues()
	{
		return dimensionIDNameValues;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + activityID;
		result = prime * result + ((activityName == null) ? 0 : activityName.hashCode());
		result = prime * result + ((avgAltitude == null) ? 0 : avgAltitude.hashCode());
		result = prime * result + checkins_count;
		result = prime * result + ((dimensionIDNameValues == null) ? 0 : dimensionIDNameValues.hashCode());
		result = prime * result + ((dimensions == null) ? 0 : dimensions.hashCode());
		long temp;
		temp = Double.doubleToLongBits(distInMFromPrev);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(distanceTravelledInKm);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (durationInSeconds ^ (durationInSeconds >>> 32));
		result = prime * result + (int) (durInSecFromPrev ^ (durInSecFromPrev >>> 32));
		result = prime * result + ((endAltitude == null) ? 0 : endAltitude.hashCode());
		result = prime * result + ((endLatitude == null) ? 0 : endLatitude.hashCode());
		result = prime * result + ((endLongitude == null) ? 0 : endLongitude.hashCode());
		result = prime * result + (int) (endTimestampInms ^ (endTimestampInms >>> 32));
		result = prime * result + highlights_count;
		result = prime * result + items_count;
		result = prime * result + ((lats == null) ? 0 : lats.hashCode());
		result = prime * result + ((locationIDs == null) ? 0 : locationIDs.hashCode());
		result = prime * result + ((locationName == null) ? 0 : locationName.hashCode());
		result = prime * result + ((lons == null) ? 0 : lons.hashCode());
		result = prime * result + max_items_count;
		result = prime * result + photos_count;
		result = prime * result + radius_meters;
		result = prime * result + ((startAltitude == null) ? 0 : startAltitude.hashCode());
		result = prime * result + ((startLatitude == null) ? 0 : startLatitude.hashCode());
		result = prime * result + ((startLongitude == null) ? 0 : startLongitude.hashCode());
		result = prime * result + (int) (startTimestampInms ^ (startTimestampInms >>> 32));
		result = prime * result + ((timeZoneId == null) ? 0 : timeZoneId.hashCode());
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		result = prime * result + users_count;
		result = prime * result + ((workingLevelCatIDs == null) ? 0 : workingLevelCatIDs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ActivityObject2018 other = (ActivityObject2018) obj;
		if (activityID != other.activityID) return false;
		if (activityName == null)
		{
			if (other.activityName != null) return false;
		}
		else if (!activityName.equals(other.activityName)) return false;
		if (avgAltitude == null)
		{
			if (other.avgAltitude != null) return false;
		}
		else if (!avgAltitude.equals(other.avgAltitude)) return false;
		if (checkins_count != other.checkins_count) return false;
		if (dimensionIDNameValues == null)
		{
			if (other.dimensionIDNameValues != null) return false;
		}
		else if (!dimensionIDNameValues.equals(other.dimensionIDNameValues)) return false;
		if (dimensions == null)
		{
			if (other.dimensions != null) return false;
		}
		else if (!dimensions.equals(other.dimensions)) return false;
		if (Double.doubleToLongBits(distInMFromPrev) != Double.doubleToLongBits(other.distInMFromPrev)) return false;
		if (Double.doubleToLongBits(distanceTravelledInKm) != Double.doubleToLongBits(other.distanceTravelledInKm))
			return false;
		if (durationInSeconds != other.durationInSeconds) return false;
		if (durInSecFromPrev != other.durInSecFromPrev) return false;
		if (endAltitude == null)
		{
			if (other.endAltitude != null) return false;
		}
		else if (!endAltitude.equals(other.endAltitude)) return false;
		if (endLatitude == null)
		{
			if (other.endLatitude != null) return false;
		}
		else if (!endLatitude.equals(other.endLatitude)) return false;
		if (endLongitude == null)
		{
			if (other.endLongitude != null) return false;
		}
		else if (!endLongitude.equals(other.endLongitude)) return false;
		if (endTimestampInms != other.endTimestampInms) return false;
		if (highlights_count != other.highlights_count) return false;
		if (items_count != other.items_count) return false;
		if (lats == null)
		{
			if (other.lats != null) return false;
		}
		else if (!lats.equals(other.lats)) return false;
		if (locationIDs == null)
		{
			if (other.locationIDs != null) return false;
		}
		else if (!locationIDs.equals(other.locationIDs)) return false;
		if (locationName == null)
		{
			if (other.locationName != null) return false;
		}
		else if (!locationName.equals(other.locationName)) return false;
		if (lons == null)
		{
			if (other.lons != null) return false;
		}
		else if (!lons.equals(other.lons)) return false;
		if (max_items_count != other.max_items_count) return false;
		if (photos_count != other.photos_count) return false;
		if (radius_meters != other.radius_meters) return false;
		if (startAltitude == null)
		{
			if (other.startAltitude != null) return false;
		}
		else if (!startAltitude.equals(other.startAltitude)) return false;
		if (startLatitude == null)
		{
			if (other.startLatitude != null) return false;
		}
		else if (!startLatitude.equals(other.startLatitude)) return false;
		if (startLongitude == null)
		{
			if (other.startLongitude != null) return false;
		}
		else if (!startLongitude.equals(other.startLongitude)) return false;
		if (startTimestampInms != other.startTimestampInms) return false;
		if (timeZoneId == null)
		{
			if (other.timeZoneId != null) return false;
		}
		else if (!timeZoneId.equals(other.timeZoneId)) return false;
		if (userID == null)
		{
			if (other.userID != null) return false;
		}
		else if (!userID.equals(other.userID)) return false;
		if (users_count != other.users_count) return false;
		if (workingLevelCatIDs == null)
		{
			if (other.workingLevelCatIDs != null) return false;
		}
		else if (!workingLevelCatIDs.equals(other.workingLevelCatIDs)) return false;
		return true;
	}

}

//////////////////////////////////// DEACTIVATED CODE BELOW//////
/*
 * /** UNTESTED
 * 
 * @param startInterval
 * 
 * @param endInterval
 * 
 * @return
 */
// $$30Sep UNTESTED
// CHECK WHTHER THIS METHOD IS CORRECT LOGICALLY AS WELL AS FOR THIS USE CASE
/*
 * public long intersectingIntervalInSeconds(Timestamp startInterval, Timestamp endInterval)
 * 
 * { return ( (endInterval.getTime()-this.startTimestamp.getTime()) + (this.endTimestamp.getTime() -
 * startInterval.getTime()) )/1000; }
 */
/*
 * $$30Sep /**` Computes the intersection of the activity event on time axis with a given time interval.
 * 
 * @param startInterval
 * 
 * @param endInterval
 * 
 * @return
 */
/*
 * $$30Seppublic long intersectingIntervalInSeconds(Timestamp startInterval, Timestamp endInterval) { long
 * intersectionInSeconds=0;
 * 
 * /* If Activity Event: AAAAAAAAAAA or AAAAAAAA and interval to check: iiiiiiiiiii iiiiiiiiiiiiiii
 */
/*
 * if(this.startTimestamp.before(startInterval) && this.endTimestamp.before(endInterval) &&
 * this.endTimestamp.after(startInterval) )
 */
/*
 * $$30Sep if( (this.startTimestamp.getTime()<=startInterval.getTime()) && this.endTimestamp.before(endInterval) &&
 * this.endTimestamp.after(startInterval) ) { intersectionInSeconds= (this.endTimestamp.getTime() -
 * startInterval.getTime()) / 1000;
 * 
 * //added on Sep 30, 2014 if(( (endInterval.getTime() -startInterval.getTime()) / 1000)==0) { intersectionInSeconds=1;
 * // to include an activity whose start time and end time are equal (it can happen as we are substrating 1second from
 * duration.) } //////// }
 * 
 * /* If Activity Event:
 * >>Unknown>>Others>>Computer>>Others>>Computer>>Others>>Computer>>Others>>Computer>>Others>>Computer>>Others>>
 * Eating>>Others>>Socialising>>Others>>Commuting >>Others>>Socialising >>Others>>Socialising
 * >>Others>>Socialising>>Others>>Socialising>>Others AAAAAAAAAAA or AAAAAAAAA and interval to check: iiiiiiiiiii
 * iiiiiiiiiiiii
 */
/*
 * else if(this.startTimestamp.after(startInterval) && this.startTimestamp.before(endInterval) &&
 * this.endTimestamp.after(endInterval) )
 */
/*
 * $$30Sepelse if(this.startTimestamp.after(startInterval) && this.startTimestamp.before(endInterval) &&
 * (this.endTimestamp.getTime()>=endInterval.getTime()) ) { intersectionInSeconds=
 * (endInterval.getTime()-this.startTimestamp.getTime()) / 1000;
 * 
 * //added on Sep 30, 2014 if(( (endInterval.getTime() -startInterval.getTime()) / 1000)==0) { intersectionInSeconds=1;
 * // to include an activity whose start time and end time are equal (it can happen as we are substrating 1second from
 * duration.) } //////////// }
 * 
 * 
 * /* If Activity Event: AAAAAA and interval to check: iiiiiiiiiii
 */
/*
 * $$30Sepelse if(this.startTimestamp.after(startInterval) /*$$30Sep && this.startTimestamp.before(endInterval) &&
 * this.endTimestamp.before(endInterval) && this.endTimestamp.after(startInterval) ) { intersectionInSeconds=
 * (this.endTimestamp.getTime() -this.startTimestamp.getTime()) / 1000;
 * 
 * //added on Sep 30, 2014 if(( (endInterval.getTime() -startInterval.getTime()) / 1000)==0) { intersectionInSeconds=1;
 * // to include an activity whose start time and end time are equal (it can happen as we are substrating 1second from
 * duration.) } //////////// }
 * 
 * ////////// Addition on 29 September, 2014 : This refactoring should not affect the results of previous result /*Not
 * sure if we need this If Activity Event: AAAAAAAA or AAAAAAAAAAA and interval to check iiii iiiiiiiiiii *
 */
/*
 * $$30Sepelse if(this.startTimestamp.getTime()<= startInterval.getTime() && this.endTimestamp.getTime() >=
 * endInterval.getTime() ) {
 * System.out.println("Inside intersectingIntervalInSeconds: case of concern about 29 sep refactoring");
 * System.out.println("endInterval.getTime()="+endInterval.getTime()+"  startInterval.getTime()"+startInterval.
 * getTime()); intersectionInSeconds= (endInterval.getTime() -startInterval.getTime()) / 1000;
 * 
 * //added on Sep 30, 2014 if(( (endInterval.getTime() -startInterval.getTime()) / 1000)==0) { intersectionInSeconds=1;
 * // to include an activity whose start time and end time are equal (it can happen as we are substrating 1second from
 * duration.) } //////////// }
 * 
 * 
 * return intersectionInSeconds; }
 */
// /older IIWAS
// /**`
// * Computes the inersection of the activity event on time axis with a given time interval.
// *
// * @param startInterval
// * @param endInterval
// * @return
// */
// public long intersectingIntervalInSeconds(Timestamp startInterval, Timestamp endInterval)
// {
// long intersectionInSeconds=0;
//
// /*
// * If Activity Event: AAAAAAAAAAA
// * and interval to check: iiiiiiiiiii
// */
// /*if(this.startTimestamp.before(startInterval)
// && this.endTimestamp.before(endInterval)
// && this.endTimestamp.after(startInterval)
// )
// */
// if( (this.startTimestamp.getTime()<=startInterval.getTime())
// && this.endTimestamp.before(endInterval)
// && this.endTimestamp.after(startInterval)
// )
// {
// intersectionInSeconds= (this.endTimestamp.getTime() - startInterval.getTime()) / 1000;
// }
//
// /*
// * If Activity Event: AAAAAAAAAAA
// * and interval to check: iiiiiiiiiii
// */
// /*else if(this.startTimestamp.after(startInterval)
// && this.startTimestamp.before(endInterval)
// && this.endTimestamp.after(endInterval)
// )*/
// else if(this.startTimestamp.after(startInterval)
// && this.startTimestamp.before(endInterval)
// && (this.endTimestamp.getTime()>=endInterval.getTime())
// )
// {
// intersectionInSeconds= (endInterval.getTime()-this.startTimestamp.getTime()) / 1000;
// }
//
//
// /*
// * If Activity Event: AAAAAA
// * and interval to check: iiiiiiiiiii
// */
// else if(this.startTimestamp.after(startInterval)
// && this.startTimestamp.before(endInterval)
// && this.endTimestamp.before(endInterval)
// && this.endTimestamp.after(startInterval)
// )
// {
// intersectionInSeconds= (this.endTimestamp.getTime() -this.startTimestamp.getTime()) / 1000;
// }
//
// return intersectionInSeconds;
// }

// /
