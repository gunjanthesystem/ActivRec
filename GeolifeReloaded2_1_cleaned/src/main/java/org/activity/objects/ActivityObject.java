package org.activity.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import org.activity.constants.Constant;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
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
public class ActivityObject implements Serializable
{
	private static final long serialVersionUID = 5056824311499867608L;

	/**
	 * this was to keep activity object generic but not entirely successfull IMHO
	 */
	ArrayList<Dimension> dimensions;//

	/**
	 * (User_ID, 2) //this was to keep activity object generic but not entirely successfull IMHO
	 */
	HashMap<String, String> dimensionIDNameValues; //

	int activityID;
	/**
	 * an activity object can have multiple location ids if it is a merged
	 */
	LinkedHashSet<Integer> locationIDs;
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
	double distanceTravelled;

	/**
	 * Gowalla dataset
	 */
	String userID;
	int photos_count, checkins_count, users_count, radius_meters, highlights_count, items_count, max_items_count;// spot_categories;
	double distanceInMFromNext;
	long durationInSecondsFromNext;

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
	public ActivityObject(int activityID, int locationID, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs)
	{

		// this.activityID = activityID;

		String splittedwlci[] = RegexUtils.patternDoubleUnderScore.split(workingLevelCatIDs);
		// $$ workingLevelCatIDs.split("__");

		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id

		this.locationIDs = new LinkedHashSet<Integer>();
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
	 * @param distanceInMFromNext
	 * @param durationInSecsFromNext
	 */
	public ActivityObject(int activityID, int locationID, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromNext,
			long durationInSecsFromNext)
	{

		// this.activityID = activityID;

		String splittedwlci[] = workingLevelCatIDs.split("__");
		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id

		this.locationIDs = new LinkedHashSet<Integer>();
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

		this.distanceInMFromNext = distanceInMFromNext;
		this.durationInSecondsFromNext = durationInSecsFromNext;

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
	 * @param distanceInMFromNext
	 * @param durationInSecsFromNext
	 */
	public ActivityObject(int activityID, ArrayList<Integer> locationIDs, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromNext,
			long durationInSecsFromNext)
	{

		// this.activityID = activityID;

		String splittedwlci[] = workingLevelCatIDs.split("__");
		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id

		this.locationIDs = new LinkedHashSet<Integer>(locationIDs);

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

		this.distanceInMFromNext = distanceInMFromNext;
		this.durationInSecondsFromNext = durationInSecsFromNext;

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
	 * @param distanceInMFromNext
	 * @param durationInSecsFromNext
	 * @param levelWiseCatIDs
	 */
	public ActivityObject(int activityID, ArrayList<Integer> locationIDs, String activityName, String locationName,
			Timestamp startTimestamp, String startLatitude, String startLongitude, String startAltitude, String userID,
			int photos_count, int checkins_count, int users_count, int radius_meters, int highlights_count,
			int items_count, int max_items_count, String workingLevelCatIDs, double distanceInMFromNext,
			long durationInSecsFromNext, String[] levelWiseCatIDs)
	{

		// this.activityID = activityID;

		String splittedwlci[] = workingLevelCatIDs.split("__");
		this.activityID = Integer.valueOf(splittedwlci[0]); // working directly with working level category id, only
															// considering one working level cat id

		this.locationIDs = new LinkedHashSet<Integer>(locationIDs);

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

		this.distanceInMFromNext = distanceInMFromNext;
		this.durationInSecondsFromNext = durationInSecsFromNext;

		// this.levelWiseCatIDs = levelWiseCatIDs;

	}

	public double getDistanceInMFromNext()
	{
		return distanceInMFromNext;
	}

	public void setDistanceInMFromNext(double distanceInMFromNext)
	{
		this.distanceInMFromNext = distanceInMFromNext;
	}

	public long getDurationInSecondsFromNext()
	{
		return durationInSecondsFromNext;
	}

	public void setDurationInSecondsFromNext(long durationInSecondsFromNext)
	{
		this.durationInSecondsFromNext = durationInSecondsFromNext;
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
				+ ", distanceTravelled=" + distanceTravelled + ", userID=" + userID + ", photos_count=" + photos_count
				+ ", checkins_count=" + checkins_count + ", users_count=" + users_count + ", radius_meters="
				+ radius_meters + ", highlights_count=" + highlights_count + ", items_count=" + items_count
				+ ", max_items_count=" + max_items_count + "]";
	}

	public String toStringAllGowalla()
	{
		return "activityID=" + activityID + "__locationID="
				+ this.getLocationIDs('-') /*
											 * + "__activityName=" + activityName + "__ locationName=" + locationName
											 */ + "__workLvlCat=" + workingLevelCatIDs + "__startTS=" + startTimestampInms
				+ "__startLat=" + startLatitude + "__startLon=" + startLongitude /*
																					 * + "__ startAlt=" + startAltitude
																					 */ + "__userID=" + userID
				+ "__photos_count=" + photos_count + "__cins_count=" + checkins_count + "__users_count=" + users_count
				+ "__radius_m=" + radius_meters + "__highlts_count=" + highlights_count + "__items_count=" + items_count
				+ "__max_items_count=" + max_items_count + "__distNext=" + distanceInMFromNext + "__durNext="
				+ durationInSecondsFromNext;
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
				+ max_items_count + "__distNext=" + distanceInMFromNext + "__durNext=" + durationInSecondsFromNext;
	}

	public String toString()
	{
		if (Constant.getDatabaseName().equals("dcu_data_2"))// // ;"geolife1";// default database name, dcu_data_2";/
			return activityName + "-" + startTimestampInms + "-" + durationInSeconds;// +" -"+startLatitude+",";
		else if (Constant.getDatabaseName().equals("geolife1"))
			return activityName + "-" + startTimestampInms + "-" + durationInSeconds + " -" + startLatitude + ","
					+ startLongitude + "-" + endLatitude + "," + endLongitude + "-" + avgAltitude;
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

	public double getDifferenceStartingGeoCoordinates(ActivityObject ao2)
	{
		return StatsUtils.haversine(startLatitude, startLongitude, ao2.getStartLatitude(), ao2.getStartLongitude());

	}

	public double getDifferenceEndingGeoCoordinates(ActivityObject ao2)
	{
		return StatsUtils.haversine(endLatitude, endLongitude, ao2.getEndLatitude(), ao2.getEndLongitude());

	}

	public double getDifferenceAltitude(ActivityObject ao2)
	{
		return Double.parseDouble(this.getAvgAltitude()) - Double.parseDouble(ao2.getAvgAltitude());

	}

	public static String getArrayListOfActivityObjectsAsString(ArrayList<ActivityObject> arr)
	{
		StringBuffer str = new StringBuffer("");

		for (ActivityObject ao : arr)
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
	public ActivityObject(HashMap<String, String> dimensionIDNameValues) // (User_ID, 0), (Location_ID, 10100), ...
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
			endDateString = getDimensionAttributeValue("Date_Dimension", "Date").toString(); // because in DCU dataset
																								// all Activity objects
																								// are broken over days
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

			this.distanceTravelled = StatsUtils.haversine(startLatitude, startLongitude, endLatitude, endLongitude);

			if (distanceTravelled > Constant.distanceTravelledAlert && Constant.checkForDistanceTravelledAnomaly)
			{
				System.out.println("Notice: distance travelled (high) = " + distanceTravelled
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

	/**
	 * @return the distanceTravelled
	 */
	public double getDistanceTravelled()
	{
		return distanceTravelled;
	}

	/**
	 * @param distanceTravelled
	 *            the distanceTravelled to set
	 */
	public void setDistanceTravelled(double distanceTravelled)
	{
		this.distanceTravelled = distanceTravelled;
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
	public HashSet<Integer> getLocationIDs()
	{
		return locationIDs;
	}

	public String getLocationIDs(char delimiter)
	{
		StringBuilder sb = new StringBuilder();
		locationIDs.stream().forEach(e -> sb.append(e + delimiter));
		return sb.toString();
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
	ActivityObject(String activityName, String location, long durationInSeconds, Timestamp startTimeStamp)
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
	public char getStringCode()
	{
		return StringCode.getCharCodeFromActivityID(this.activityID);
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
			System.err.println(
					PopUps.getCurrentStackTracedErrorMsg("Error in getDimensionAttributeValue() for dimension name = "
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
	public ActivityObject(String name, Timestamp start, Timestamp end)
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
