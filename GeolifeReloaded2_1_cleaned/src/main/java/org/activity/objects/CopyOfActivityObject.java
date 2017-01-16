package org.activity.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.activity.util.Constant;
import org.activity.util.DateTimeUtils;
import org.activity.util.StatsUtils;
import org.activity.util.StringCode;
import org.activity.util.UtilityBelt;

public class CopyOfActivityObject implements Serializable
{
	// TODO: allow use of activity ID instead of Activity Name for performance concerns
	ArrayList<Dimension> dimensions;
	String trajectoryID; // added on Mar 14, 2016

	HashMap<String, String> dimensionIDNameValues; // (User_ID, 2)

	int activityID, locationID;
	String activityName, locationName;
	Timestamp startTimestamp, endTimestamp;

	long durationInSeconds;
	/**
	 * Not available in DCU_dataset. Available in Geolife dataset
	 */
	String startLatitude, endLatitude, startLongitude, endLongitude, startAltitude, endAltitude, avgAltitude;
	/**
	 * Not available in DCU_dataset. Available in Geolife dataset
	 */
	double distanceTravelled;

	public String toString()
	{
		if (Constant.getDatabaseName().equals("dcu_data_2"))// // ;"geolife1";// default database name, dcu_data_2";/
			return activityName + "-" + startTimestamp + "-" + durationInSeconds;// +" -"+startLatitude+",";
		else if (Constant.getDatabaseName().equals("geolife1"))
			return activityName + "-" + startTimestamp + "-" + durationInSeconds + " -" + startLatitude + ","
					+ startLongitude + "-" + endLatitude + "," + endLongitude + "-" + avgAltitude;
		else
			return "empty";
	}

	public void setTrajectoryID(String trajectoryID)
	{
		this.trajectoryID = trajectoryID;
	}

	public String getTrajectoryID()
	{
		return this.trajectoryID;
	}

	// public double getDistanceTravelledInActivityObject()
	// {
	// // double distanceTravelled =-99;
	//
	// return UtilityBelt.haversine(startLatitude, startLongitude, endLatitude, endLongitude);
	//
	//
	// }

	public double getDifferenceStartingGeoCoordinates(CopyOfActivityObject ao2)
	{
		return StatsUtils.haversine(startLatitude, startLongitude, ao2.getStartLatitude(), ao2.getStartLongitude());

	}

	public double getDifferenceEndingGeoCoordinates(CopyOfActivityObject ao2)
	{
		return StatsUtils.haversine(endLatitude, endLongitude, ao2.getEndLatitude(), ao2.getEndLongitude());

	}

	public double getDifferenceAltitude(CopyOfActivityObject ao2)
	{
		return Double.parseDouble(this.getAvgAltitude()) - Double.parseDouble(ao2.getAvgAltitude());

	}

	public static String getArrayListOfActivityObjectsAsString(ArrayList<CopyOfActivityObject> arr)
	{
		StringBuffer str = new StringBuffer("");

		for (CopyOfActivityObject ao : arr)
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
	CopyOfActivityObject(HashMap<String, String> dimensionIDNameValues) // (User_ID, 0), (Location_ID, 10100), ...
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
		this.startTimestamp = DateTimeUtils.getTimestamp(startTimeString, startDateString); // in iiWAS ver, dateString
																							// is used here instead of
																							// startDateString
		this.endTimestamp = DateTimeUtils.getTimestamp(endTimeString, endDateString);// in iiWAS ver, dateString is used
																						// here instead of endDateString

		this.durationInSeconds = (this.endTimestamp.getTime() - this.startTimestamp.getTime()) / 1000 + 1; // +1 because
																											// 1 seconds
																											// was
																											// decremented
																											// while
																											// loading
																											// data for
																											// resolving
																											// consecutive
																											// activities
																											// primarliy
																											// for
																											// visualisation

		if (this.durationInSeconds < 0)
		{
			System.err.println("Error: Negative duration in seconds:startTimestamp=" + startTimestamp + " endTimestamp="
					+ endTimestamp + " i.e., " + this.endTimestamp.getTime() + "-" + this.startTimestamp.getTime());
			System.err.println("\t\t StartDateString:" + startDateString + " StartTimeString:" + startTimeString
					+ "\n\t\t EndDateString:" + endDateString + " EndTimeString:" + endTimeString);
		}

		// System.out.println("Exiting ActivityObject contructor-----------");
		// System.out.println("Activity Event Create: number of dimensions"+dimensions.size()); // Debug Info: count the
		// occurence of this in output to see if the number of
		// activity events
		// generated
		// is correct:
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
	 * Create empty Activity Object
	 */
	CopyOfActivityObject()
	{
		dimensions = new ArrayList<Dimension>();
		dimensionIDNameValues = new HashMap<String, String>();
		activityName = "empty";
		activityID = locationID = -99;
	}

	// String thisConstructorIsForTest
	/**
	 * <font color="red">ONLY FOR TEST/DEBUGGING PURPOSES!</font>
	 * 
	 * @param activityName
	 * @param location
	 * @param durationInSeconds
	 * @param startTimeStamp
	 */
	CopyOfActivityObject(String activityName, String location, long durationInSeconds, Timestamp startTimeStamp)
	{
		this.activityName = activityName;
		this.durationInSeconds = durationInSeconds;
		this.locationName = location;
		this.startTimestamp = startTimeStamp;
	}

	/**
	 * An Activity Object is invalid if the Activity name is 'Unknown' or 'Not Available'
	 * 
	 * @return
	 */
	public boolean isInvalidActivityName()
	{
		boolean invalid = false;

		if (UtilityBelt.isValidActivityName(this.activityName) == false)// (this.activityName.trim().equals("Unknown")||this.activityName.trim().equals("Not
																		// Available"))//equals("Others"))
		{
			invalid = true;
		}

		return invalid;
	}

	public ArrayList<Dimension> getDimensions()
	{
		return dimensions;
	}

	public Timestamp getMiddleTimestamp()
	{
		Timestamp middleTimestamp = null;

		middleTimestamp = new Timestamp(
				startTimestamp.getTime() + ((endTimestamp.getTime() - startTimestamp.getTime()) / 2));

		return middleTimestamp;

	}

	public boolean equals(CopyOfActivityObject aeToCompare)
	{
		boolean equal = true;

		if (this.activityName != aeToCompare.activityName) return false;

		// else if()

		return equal;
	}

	/**
	 * Returns the 1-character string code from the Activity Name. This code is derived from the ActivityID and hence is
	 * guaranteed to be unique for at least 107 activities.
	 * 
	 * @return
	 */
	public String getStringCode()
	{
		/*
		 * String code = new String(); String activityName= this.activityName; int activityID=
		 * generateSyntheticData.getActivityid(activityName); code= Character.toString ((char)(activityID+65));
		 */
		return StringCode.getStringCodeFromActivityName(this.activityName);
	}

	/**
	 * Returns the 1-character string code to be used for the Activity Name. This code is derived from the ActivityID
	 * and hence is guaranteed to be unique for at least 107 activities.
	 * 
	 * @param ActivityObjects
	 * @return
	 */
	public static String getStringCodeForActivityObjects(ArrayList<CopyOfActivityObject> ActivityObjects)
	{
		String code = new String();

		if (Constant.verboseSAX) System.out.println("Inside getStringCodeForActivityObjects");

		for (int i = 0; i < ActivityObjects.size(); i++)
		{
			// String activityName= ActivityObjects.get(i).getActivityName();
			// int activityID= generateSyntheticData.getActivityid(activityName);

			code += ActivityObjects.get(i).getStringCode(); // Character.toString ((char)(activityID+65)); //getting the
															// ascii code for (activity id+65)

			if (Constant.verboseSAX) System.out.print(ActivityObjects.get(i).getActivityName() + " ");
		}

		if (Constant.verboseSAX)
		{
			System.out.println("Code: " + code);
		}
		return code;
	}

	public long getDurationInSeconds()
	{
		return this.durationInSeconds;
	}

	public String getLocationName()
	{
		return this.locationName;
	}

	public String getDimensionIDValue(String dimensionIDName)
	{
		return this.dimensionIDNameValues.get(dimensionIDName);
	}

	public Object getDimensionAttributeValue(String dimensionName, String dimensionAttributeName) // (User_Dimension,
																									// User_Name)
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
			System.err.println("Erros in getDimensionAttributeValue() for dimension name = " + dimensionName
					+ ", dimension attribute name = " + dimensionAttributeName
					+ "\n No such dimension found for this activity event.");

			System.exit(2); // Check later if it is wise or unwise to exit in such case
		}

		else
		{
			dimensionAttributeValue = dimensionToFetch.getValueOfDimensionAttribute(dimensionAttributeName);
		}

		return dimensionAttributeValue;
	}

	public void traverseDimensionIDNameValues()
	{
		for (Map.Entry<String, String> entry : this.dimensionIDNameValues.entrySet())
		{
			System.out.print(" " + entry.getKey() + " getDimensionAttributeValue: " + entry.getValue() + " ");
		}
		System.out.println("");
	}

	// TODO
	public String writeDimensionIDNameValues()
	{
		StringBuffer s = new StringBuffer();

		for (Map.Entry<String, String> entry : this.dimensionIDNameValues.entrySet())
		{
			// s.append
			// System.out.print(" " + entry.getKey() + " getDimensionAttributeValue: " + entry.getValue() + " ");
		}
		System.out.println("");
		return null;
	}

	public void traverseActivityEvent()
	{
		System.out.println("\n---Traversing Activity Event:--");
		System.out.print("----Dimensions ID are: ");
		traverseDimensionIDNameValues();

		System.out.println("----Dimension attributes are: ");

		for (int i = 0; i < dimensions.size(); i++)
		{
			Dimension dimension = dimensions.get(i);
			dimension.traverseDimensionAttributeNameValuepairs();
		}
	}

	// TODO
	public static String writeActivityEvent()
	{
		return null;
	}

	CopyOfActivityObject(String name, Timestamp start, Timestamp end)
	{
		// userName=user;
		activityName = name;
		startTimestamp = start;
		endTimestamp = end;
	}

	public String getActivityName()
	{
		return activityName;
	}

	public Timestamp getStartTimestamp()
	{
		return startTimestamp;
	}

	public Timestamp getEndTimestamp()
	{
		return endTimestamp;
	}

	// /////////////////////////// To be removed later after refactoring
	public void setActivityName(String name)
	{
		activityName = name;
	}

	public void setStartTimestamp(Timestamp start)
	{
		startTimestamp = start;
	}

	public void setEndTimestamp(Timestamp end)
	{
		endTimestamp = end;
	}

	// ///////////////////////////////////////////////////////////////////////

	public boolean fullyContainsInterval(Timestamp startInterval, Timestamp endInterval)
	{
		boolean value = false;

		/*
		 * If Activity Event: AAAAAAAAAAAA and interval to check: iiiiii
		 */
		// if(this.startTimestamp.before(startInterval) && this.endTimestamp.after(endInterval))
		if ((this.startTimestamp.getTime() <= startInterval.getTime())
				&& (this.endTimestamp.getTime() >= endInterval.getTime()))
		{
			value = true;
		}

		return value;
	}

	public boolean startsOnOrBefore(Timestamp startStampPoint)
	{

		if (this.startTimestamp.before(startStampPoint) || this.startTimestamp.equals(startStampPoint))
			return true;
		else
			return false;

	}

	// 21Oct
	/**
	 * used in matching unnit case to fetch timelines, see class Timeline
	 * 
	 * @param startInterval
	 * @param endInterval
	 * @return
	 */
	public boolean doesOverlap(Timestamp startInterval, Timestamp endInterval)
	{
		return ((this.startTimestamp.getTime() <= endInterval.getTime())
				&& (this.endTimestamp.getTime() >= startInterval.getTime()));
	} // courtesy:http://goo.gl/pnR3p1

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
	// TODO CHECK WHTHER THIS METHOD IS CORRECT LOGICALLY AS WELL AS FOR THIS USE CASE
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
	 * //added on Sep 30, 2014 if(( (endInterval.getTime() -startInterval.getTime()) / 1000)==0) {
	 * intersectionInSeconds=1; // to include an activity whose start time and end time are equal (it can happen as we
	 * are substrating 1second from duration.) } //////// }
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
	 * //added on Sep 30, 2014 if(( (endInterval.getTime() -startInterval.getTime()) / 1000)==0) {
	 * intersectionInSeconds=1; // to include an activity whose start time and end time are equal (it can happen as we
	 * are substrating 1second from duration.) } //////////// }
	 * 
	 * 
	 * /* If Activity Event: AAAAAA and interval to check: iiiiiiiiiii
	 */
	/*
	 * $$30Sepelse if(this.startTimestamp.after(startInterval) /*$$30Sep && this.startTimestamp.before(endInterval) &&
	 * this.endTimestamp.before(endInterval) && this.endTimestamp.after(startInterval) ) { intersectionInSeconds=
	 * (this.endTimestamp.getTime() -this.startTimestamp.getTime()) / 1000;
	 * 
	 * //added on Sep 30, 2014 if(( (endInterval.getTime() -startInterval.getTime()) / 1000)==0) {
	 * intersectionInSeconds=1; // to include an activity whose start time and end time are equal (it can happen as we
	 * are substrating 1second from duration.) } //////////// }
	 * 
	 * ////////// Addition on 29 September, 2014 : This refactoring should not affect the results of previous result
	 * /*Not sure if we need this If Activity Event: AAAAAAAA or AAAAAAAAAAA and interval to check iiii iiiiiiiiiii *
	 */
	/*
	 * $$30Sepelse if(this.startTimestamp.getTime()<= startInterval.getTime() && this.endTimestamp.getTime() >=
	 * endInterval.getTime() ) {
	 * System.out.println("Inside intersectingIntervalInSeconds: case of concern about 29 sep refactoring");
	 * System.out.println("endInterval.getTime()="+endInterval.getTime()+"  startInterval.getTime()"+startInterval.
	 * getTime()); intersectionInSeconds= (endInterval.getTime() -startInterval.getTime()) / 1000;
	 * 
	 * //added on Sep 30, 2014 if(( (endInterval.getTime() -startInterval.getTime()) / 1000)==0) {
	 * intersectionInSeconds=1; // to include an activity whose start time and end time are equal (it can happen as we
	 * are substrating 1second from duration.) } //////////// }
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

}
