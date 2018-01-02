package org.activity.constants;

public class SanityConstants
{

	public static final boolean checkForDistanceTravelledAnomaly = false;
	public static final boolean checkForHaversineAnomaly = true; // false;
	public static final boolean checkArrayOfFeatures = false;
	/**
	 * Can be disabled for better performance for subsequent runs if previous run of experiments shows that timelines
	 * created had no chronological anomaly
	 */
	public static final boolean checkIfTimelineCreatedIsChronological = false;// true;// false;

	private SanityConstants()
	{

	}

}
