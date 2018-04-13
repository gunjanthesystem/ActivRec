package org.activity.constants;

/**
 * Contains constants for managing the verbosity of log output and writing of log files.
 * 
 * @author gunjan
 *
 */
public class VerbosityConstants
{
	public static boolean alignmentDistanceStringPrintedOnce = false;
	/**
	 * Check sanity of implementations for primary dimension perspective, these sanity checks are for when pd is
	 * activity id, since the earlier methods to compared with are for activity name and activity name and activity id
	 * are same for gowalla dataset.
	 */
	public static final boolean checkSanityPDImplementn = false;

	public static final boolean printSanityCheck = false;

	public static final boolean disableWritingToFileForSpeed = true;
	/**
	 * Controlling the verbosity of console log output
	 */
	public static final boolean verbose = false;// false;// false;// false;// true;// false;// false;//
	public static final boolean tempVerbose = false;// false;// true;// true;// false;// false;// true;
	public static final boolean verboseTimelineCleaning = false;// true;// false;// false; // verbosity level 2: if
	// false it further
	// minimises verbosity
	public static final boolean verboseSAX = false;// false;
	public static boolean verboseLevenstein = false;// true;// false;// false;// false;// false;
	public static final boolean verboseNormalisation = false;// false;// false;
	// public static boolean debuggingMessageEditDistance = false;
	public static final boolean verboseHilbert = false;
	public static final boolean verboseOTMDSAM = false;
	public static final boolean verboseDistance = false;// false;// true;// false;// false;
	public static final boolean verboseRankScoreCalcToConsole = false;// false;
	public static final boolean verboseEvaluationMetricsToConsole = false;// true;// true;// false;
	public static final boolean verboseCombinedEDist = false;
	public static final boolean verboseCandFilter = false;

	/**
	 * Whether to write the file EditDistancePerRtPerCand.csv (note: the files 'UserId'RecommTimesWithEditDistance.csv
	 * and EditDistancePerRtPerCand.csv have some similar information and have corresponding records.
	 */
	public static final boolean WriteEditDistancePerRtPerCand = false;// false;// true;// false;// false;// false; //
	public static final boolean WriteCandInEditDistancePerRtPerCand = false;// false;// false;// false;
	public static boolean WriteNumActsPerRTPerCand = false;// true;// false; // this information is redundant as well

	public static final boolean WriteRedundant = false;
	public static final boolean WriteEditOperatationsInEditDistancePerRtPerCand = false;// false;// true;
	public static final boolean WriteEditSimilarityCalculations = false;// true;// true;//false;// false;// false;
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
	public static final boolean WriteLocationMap = false;// true;
	public static final boolean WriteNumOfValidsAfterAnRTInSameDay = false;
	public static final boolean WriteNumberOfCandidateTimelinesBelow = false;
	public static final boolean WriteMaxNumberOfDistinctRecommendation = false;

	public static final boolean WriteFilterCandByCurActTimeThreshInSecs = false;

	/**
	 * to save writing non essential redudant stuffs
	 */
	public static final boolean WriteTopNextActivitiesWithoutDistance = false;
	public static final boolean WriteTopNextActivitiesWithDistance = false;

	public static final boolean WriteRaw = false;// SWITCH

	public static final boolean writeRankedRecommsWOScoreForEachSeqIndex = false;
	public static final boolean writeDataActualForEachSeqIndex = false;

}
