package org.activity.constants;

public class Enums
{
	public Enums()
	{
	}

	/**
	 * NCount, NHours, Daywise
	 * 
	 * @author gunjan
	 *
	 */
	public enum LookPastType
	{
		NCount, NHours, Daywise, ClosestTime, NGram;
	}

	public enum AltSeqPredictor
	{
		AKOM;
	}

	/**
	 * CaseBasedV1, SimpleV3
	 * 
	 * @author gunjan
	 *
	 */
	public enum CaseType
	{
		CaseBasedV1, SimpleV3
	}

	/**
	 * Global, Percent;
	 * 
	 * @author gunjan
	 *
	 */
	public enum TypeOfThreshold
	{
		Global, Percent;
	}
	// typeOfThresholds[];// = { "Global" };// Global"};//"Percent"

	public enum EditDistanceTimeDistanceType
	{
		BinaryThreshold, NearerScaled, FurtherScaled;
	}

	public enum SummaryStat
	{
		Mean, Median, Mode;
	}

	public enum PrimaryDimension
	{
		ActivityID, LocationID;
	}
}
