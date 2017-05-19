package org.activity.constants;

/**
 * Contains constants for managing the verbosity of log output and writing of log files.
 * 
 * @author gunjan
 *
 */
public class VerbosityConstants
{

	/**
	 * Controlling the verbosity of console log output
	 */
	public static final boolean verbose = false;// false;// false;// true;// false;// false;
	public static final boolean verboseTimelineCleaning = false;// true;// false;// false; // verbosity level 2: if
	// false it further
	// minimises verbosity
	public static final boolean verboseSAX = false;// false;
	public static boolean verboseLevenstein = false;// false;// false;// false;
	public static final boolean verboseNormalisation = false;// false;// false;
	// public static boolean debuggingMessageEditDistance = false;
	public static final boolean verboseHilbert = false;
	public static final boolean verboseOTMDSAM = false;
	public static final boolean verboseDistance = false;// false;
	public static final boolean verboseRankScoreCalcToConsole = false;// false;
	public static final boolean verboseEvaluationMetricsToConsole = false;// false;
	/**
	 * Whether to write the file EditDistancePerRtPerCand.csv (note: the files 'UserId'RecommTimesWithEditDistance.csv
	 * and EditDistancePerRtPerCand.csv have some similar information and have corresponding records.
	 */
	public static final boolean WriteEditDistancePerRtPerCand = false;// true;// false;// false;// false; //
	public static final boolean WriteCandInEditDistancePerRtPerCand = false;// false;// false;// false;
	public static boolean WriteNumActsPerRTPerCand = false;// false; // this information is redundant as well
	public static final boolean WriteRedundant = false;
	public static final boolean WriteEditOperatationsInEditDistancePerRtPerCand = false;// true;// false;// false;//
																						// false;
	public static final boolean WriteEditSimilarityCalculations = false;// false;// false;
	public static final boolean WriteActivityObjectsInEditSimilarityCalculations = false;// false;// false;
	public static final boolean WriteNormalisation = false;// false;// false;
	public static final boolean WriteEditDistancesOfAllEndPoints = false;// false;
	/**
	 * Write each cand of each RT in separate line
	 */
	public static final boolean WriteNormalisationsSeparateLines = false;// false;// false;
	/**
	 * This is a useful file to write, disable it only when faster experiments are needed.
	 */
	public static final boolean WriteRecommendationTimesWithEditDistance = false;// false;
	public static final boolean WriteTimelines = false;
	public static final boolean WriteLocationMap = false;
	public static final boolean WriteNumOfValidsAfterAnRTInSameDay = false;

	/**
	 * to save writing non essential redudant stuffs
	 */
	public static final boolean WriteTopNextActivitiesWithoutDistance = false;
	public static final boolean WriteTopNextActivitiesWithDistance = false;

}
