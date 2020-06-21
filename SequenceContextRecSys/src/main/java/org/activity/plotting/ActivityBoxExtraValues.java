
package org.activity.plotting;

/** Data extra values for storing close, high and low. */
public class ActivityBoxExtraValues
{
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
	public ActivityBoxExtraValues(double endTimestamp, String actName, int activityID, double startLatitude)
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
	public ActivityBoxExtraValues(double endTimestamp, String activityName, int activityID, String locationName)
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
