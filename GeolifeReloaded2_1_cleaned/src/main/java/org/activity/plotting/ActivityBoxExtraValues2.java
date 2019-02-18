
package org.activity.plotting;

import java.util.EnumMap;

import org.activity.constants.Enums;

/** Data extra values for storing close, high and low. */
/**
 * Fork of ActivityBoxExtraValues for a more generic version
 * <p>
 * Incomplete and not used as of 17 Feb 2019
 * 
 * @author gunjan
 *
 */
public class ActivityBoxExtraValues2
{
	EnumMap<Enums.GowGeoFeature, String> featureValues;
	private double endTimestamp;
	private String activityName;
	private int activityID;
	private double startLatitude;
	private double endLatitude;
	private String locationName;

	/**
	 * 
	 * @param endTimestamp
	 * @param actName
	 * @param activityID
	 * @param startLatitude
	 */
	public ActivityBoxExtraValues2(double endTimestamp, String actName, int activityID, double startLatitude)
	{
		// System.out.println("ActivityBoxExtraValues() created");
		this.endTimestamp = endTimestamp;
		this.activityName = actName;
		this.activityID = activityID;
		this.startLatitude = startLatitude;
	}

	/**
	 * 
	 * @param endTimestamp
	 * @param activityName
	 * @param activityID
	 * @param locationName
	 */
	public ActivityBoxExtraValues2(double endTimestamp, String activityName, int activityID, String locationName)
	{
		this.endTimestamp = endTimestamp;
		this.activityName = activityName;
		this.activityID = activityID;
		this.locationName = locationName;
	}

	public double getEndTimestamp()
	{
		return endTimestamp;
	}

	public String getActivityName()
	{
		return activityName;
	}

	public int getActivityID()
	{
		return activityID;
	}

	public double getStartLatitude()
	{
		return startLatitude;
	}

	public double getEndLatitude()
	{
		return endLatitude;
	}

	private static final String FORMAT = "ActivityBoxExtraValues{end=%f, ActName=%s, ActID=%d, LocName=%s}";

	@Override
	public String toString()
	{
		// String endTSString = new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(endTimestamp);
		//
		// return "endTS="+endTimestamp+","
		return "ajooba";
		// return String.format(FORMAT, endTimestamp, activityName, activityID, locationName);

	}
}
