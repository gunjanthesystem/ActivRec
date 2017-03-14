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
		NCount, NHours, Daywise, ClosestTime
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

}
