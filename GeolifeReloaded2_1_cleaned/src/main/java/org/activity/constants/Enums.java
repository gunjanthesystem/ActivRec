package org.activity.constants;

public class Enums
{
	public Enums()
	{
	}

	/**
	 * 
	 * @author gunjan
	 * @since April 14 2018
	 */
	public enum GowallaFeatures
	{
		ActNameF, StartTimeF, LocationF, PopularityF, DistFromPrevF, DurationFromPrevF;
	}

	public enum TypeOfExperiment
	{
		RecommendationTests, TimelineStats, TimelineClustering;
	}

	public enum UserSets
	{
		Users9k, User916;
	}

	/**
	 * NCount, NHours, Daywise
	 * 
	 * @author gunjan
	 *
	 */
	public enum LookPastType
	{
		NCount, NHours, Daywise, ClosestTime, NGram, None;
	}

	public enum AltSeqPredictor
	{
		None, AKOM, PureAKOM, RNN1;
		// PureAKOM: not matching , i.e., no candidate generation using any matching approach, feeding the complete (or
		// truncated) training timeline as a single timeline
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

	public enum TypeOfCandThreshold
	{
		None, NearestNeighbour, Percentile, NearestNeighbourWithEDValThresh;// EDValThresh added on 8 Aug 2018
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
		ActivityID, LocationID, LocationGridID;
	}
}
