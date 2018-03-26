package org.activity.plotting;

import java.util.ArrayList;
import java.util.List;

public class TimelineEntry
{

	private String userID;
	/**
	 * {For this time, a list of actEntries, where each actEntry is a long of Strings
	 */
	private List<List<String>> values;

	public TimelineEntry(String userID)
	{
		this.userID = userID;
		values = new ArrayList<>();
	}

	public String getUserID()
	{
		return userID;
	}

	/**
	 * Stores the values shown in the chart.
	 */
	public List<List<String>> getValues()
	{
		return values;
	}

	public void setValues(List<List<String>> values)
	{
		this.values = values;
	}
}