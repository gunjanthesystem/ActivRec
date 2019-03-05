package org.activity.constants;

import java.util.Arrays;
import java.util.List;

public class Enums
{
	public Enums()
	{
	}

	public enum ActDistType
	{
		Jaccard, Jaro, Hamming, LongestCommonSubsequence, MyLevenshtein, AllZeroDistance, RandomDistance;
	}

	public enum FeatDistType
	{

	}

	/**
	 * 
	 * @author gunjan
	 * @since April 14 2018
	 */
	public enum GowallaFeature
	{
		ActNameF, StartTimeF, LocationF, PopularityF, DistFromPrevF, DurationFromPrevF, ActNameLevel1F;
	}

	/**
	 * - start time - duration - distance travelled - start geo - end geo - avg altitude
	 * 
	 * @author gunjan
	 * @since November 17, 2018
	 */
	public enum GeolifeFeature
	{
		ActNameF, StartTimeF, DurationF, DistTravelledF, StartGeoF, EndGeoF, AvgAltitudeF, DistFromPrevF, DurationFromPrevF;
	}

	public enum DCUFeature
	{
		ActNameF, StartTimeF, DurationF;
	}

	/**
	 * 
	 * @param databaseName
	 * @return
	 */
	public static List<?> getFeaturesForDatabase(String databaseName)
	{
		switch (databaseName)
		{
		case ("gowalla1"):
			return Arrays.asList(GowallaFeature.values());
		case ("geolife1"):
			return Arrays.asList(GeolifeFeature.values());
		case ("dcu_data_2"):
			return Arrays.asList(DCUFeature.values());
		}
		return null;
	}

	/**
	 * 
	 * @author gunjan
	 * @since Novemeber 18, 2018
	 */
	public enum GowGeoFeature
	{
		ActNameF, StartTimeF, LocationF, PopularityF, DistFromPrevF, DurationFromPrevF, DurationF, DistTravelledF, StartGeoF, EndGeoF, AvgAltitudeF;// ,
																																					// EndTimeF;
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
		None, AKOM, PureAKOM, RNN1, ClosestTime, HighOccur, HighDur;
		// Note: when using PureAKOM, keep LookPastType as NCount
		// Note: when using HighOccur, HighDur keep LookPastType as Daywise
		// Note: when using HClosestTime keep LookPastType as ClosestTime
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
		ActivityID, LocationID, LocationGridID, Duration;// Duration added on Nov18 2018 but not checked to be fully
															// implemented
	}
}
