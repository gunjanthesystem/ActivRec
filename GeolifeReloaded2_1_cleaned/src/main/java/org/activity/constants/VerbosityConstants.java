package org.activity.constants;

/**
 * Contains constants for managing the verbosity of log output and writing of log files.
 * 
 * @author gunjan
 *
 */
public class VerbosityConstants
{

	// // Start of curtain 20 May 2018 for running Toy
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
	public static final boolean verboseNormalisation = true;// false;// false;
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
	public static final boolean WriteFilterTrainTimelinesByRecentDays = false;

	/**
	 * to save writing non essential redudant stuffs
	 */
	public static final boolean WriteTopNextActivitiesWithoutDistance = true;
	public static final boolean WriteTopNextActivitiesWithDistance = true;

	public static final boolean WriteRaw = true;// SWITCH

	public static final boolean writeRankedRecommsWOScoreForEachSeqIndex = true;// false;
	public static final boolean writeDataActualForEachSeqIndex = true;
	public static final boolean writeTrainTestTimelinesAOsPerUser = true;

	public static final boolean writeRNN1PredProbDistribution = true;
	// End of curtain 30 May 2018 for running Toy

	// ======================================================================================

	// Start of 30 May 2018 ALL TRUE for toy
	// public static boolean alignmentDistanceStringPrintedOnce = true;
	// /**
	// * Check sanity of implementations for primary dimension perspective, these sanity checks are for when pd is
	// * activity id, since the earlier methods to compared with are for activity name and activity name and activity id
	// * are same for gowalla dataset.
	// */
	// public static final boolean checkSanityPDImplementn = true;
	//
	// public static final boolean printSanityCheck = true;
	//
	// public static final boolean disableWritingToFileForSpeed = true;
	// /**
	// * Controlling the verbosity of console log output
	// */
	// public static final boolean verbose = true;// true;// true;// true;// true;// true;// true;//
	// public static final boolean tempVerbose = true;// true;// true;// true;// true;// true;// true;
	// public static final boolean verboseTimelineCleaning = true;// true;// true;// true; // verbosity level 2: if
	// // true it further
	// // minimises verbosity
	// public static final boolean verboseSAX = true;// true;
	// public static boolean verboseLevenstein = true;// true;// true;// true;// true;// true;
	// public static final boolean verboseNormalisation = true;// true;// true;
	// // public static boolean debuggingMessageEditDistance = true;
	// public static final boolean verboseHilbert = true;
	// public static final boolean verboseOTMDSAM = true;
	// public static final boolean verboseDistance = true;// true;// true;// true;// true;
	// public static final boolean verboseRankScoreCalcToConsole = true;// true;
	// public static final boolean verboseEvaluationMetricsToConsole = true;// true;// true;// true;
	// public static final boolean verboseCombinedEDist = true;
	// public static final boolean verboseCandFilter = true;
	//
	// /**
	// * Whether to write the file EditDistancePerRtPerCand.csv (note: the files 'UserId'RecommTimesWithEditDistance.csv
	// * and EditDistancePerRtPerCand.csv have some similar information and have corresponding records.
	// */
	// public static final boolean WriteEditDistancePerRtPerCand = true;// true;// true;// true;// true;// true; //
	// public static final boolean WriteCandInEditDistancePerRtPerCand = true;// true;// true;// true;
	// public static boolean WriteNumActsPerRTPerCand = true;// true;// true; // this information is redundant as well
	//
	// public static final boolean WriteRedundant = true;
	// public static final boolean WriteEditOperatationsInEditDistancePerRtPerCand = true;// true;// true;
	// public static final boolean WriteEditSimilarityCalculations = true;// true;// true;//true;// true;// true;
	// public static final boolean WriteActivityObjectsInEditSimilarityCalculations = true;// true;// true;
	// public static final boolean WriteNormalisation = true;// true;// true;
	// public static final boolean WriteEditDistancesOfAllEndPoints = true;// true;
	// /**
	// * Write each cand of each RT in separate line
	// */
	// public static final boolean WriteNormalisationsSeparateLines = true;// true;// true;
	// /**
	// * This is a useful file to write, disable it only when faster experiments are needed.
	// */
	// public static final boolean WriteRecommendationTimesWithEditDistance = true;// true;
	// public static final boolean WriteTimelines = true;
	// public static final boolean WriteLocationMap = true;// true;
	// public static final boolean WriteNumOfValidsAfterAnRTInSameDay = true;
	// public static final boolean WriteNumberOfCandidateTimelinesBelow = true;
	// public static final boolean WriteMaxNumberOfDistinctRecommendation = true;
	//
	// public static final boolean WriteFilterCandByCurActTimeThreshInSecs = true;
	// public static final boolean WriteFilterTrainTimelinesByRecentDays = true;
	//
	// /**
	// * to save writing non essential redudant stuffs
	// */
	// public static final boolean WriteTopNextActivitiesWithoutDistance = true;
	// public static final boolean WriteTopNextActivitiesWithDistance = true;
	//
	// public static final boolean WriteRaw = true;// SWITCH
	//
	// public static final boolean writeRankedRecommsWOScoreForEachSeqIndex = true;
	// public static final boolean writeDataActualForEachSeqIndex = true;
	// // public static final boolean writeCurrentTimelineForEachSeqIndex = true;
	// public static final boolean writeTrainTestTimelinesAOsPerUser = true;

	// End of 30 May 2018 ALL TRUE for toy
}
