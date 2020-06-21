package org.activity.ui.temp;

import java.util.ArrayList;
import java.util.List;

/**
 * Just some fake model object.
 */
class YearEntry
{

	private int year;

	YearEntry(int year)
	{
		this.year = year;
	}

	public int getYear()
	{
		return year;
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