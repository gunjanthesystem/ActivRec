package org.activity.ui.temp;

import java.util.ArrayList;
import java.util.List;

/**
 * Just some fake model object.
 */
public class TimelineEntry
{

	private int userID;

	public TimelineEntry(int year)
	{
		this.userID = year;
	}

	public int getUserID()
	{
		return userID;
	}

	private List<Double> values = new ArrayList<>();

	/**
	 * Stores the values shown in the chart.
	 */
	public List<Double> getValues()
	{
		return values;
	}
}