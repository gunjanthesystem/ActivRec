package org.activity.objects;

public class ActivityTableRow
{
	String user, day, activity, location, activityStartTime, activityEndTime, date; // rows to show in table

	public ActivityTableRow()
	{

	}

	public ActivityTableRow(String user, String day, String activity, String location, String activityStartTime,
			String activityEndTime, String date)
	{
		this.user = user;
		this.day = day;
		this.activity = activity;
		this.location = location;
		this.activityStartTime = "\"" + activityStartTime + "\"";
		this.activityEndTime = "\"" + activityEndTime + "\"";
		this.date = date;
	}

	public static String[] getNameOfHeaders()
	{
		String nameOfHeaders[] =
		{ "User", "Day", "Activity", "Location", "Start Time", "End Time", "Date" };
		return nameOfHeaders;
	}

	public static String[] getVariableNames()
	{
		String variableNames[] =
		{ "user", "day", "activity", "location", "activityStartTime", "activityEndTime", "date" };
		return variableNames;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getDay()
	{
		return day;
	}

	public void setDay(String day)
	{
		this.day = day;
	}

	public String getActivity()
	{
		return activity;
	}

	public void setActivity(String activity)
	{
		this.activity = activity;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public String getActivityStartTime()
	{
		return activityStartTime;
	}

	public void setActivityStartTime(String activityStartTime)
	{
		this.activityStartTime = activityStartTime;
	}

	public String getActivityEndTime()
	{
		return activityEndTime;
	}

	public void setActivityEndTime(String activityEndTime)
	{
		this.activityEndTime = activityEndTime;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

}
