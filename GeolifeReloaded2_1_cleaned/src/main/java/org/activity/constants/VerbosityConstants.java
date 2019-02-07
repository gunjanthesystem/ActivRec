package org.activity.constants;

/**
 * Contains constants for managing the verbosity of log output and writing of log files.
 * 
 * @author gunjan
 *
 */
public class VerbosityConstants
{
	boolean allVerboseForToy = false;

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
	public static boolean verbose = false;// false;// false;// false;// true;// false;// false;//
	public static final boolean verboseAKOM = false;// false;// false;// false;// true;// false;// false;//
	public static final boolean tempVerbose = false;// false;// true;// true;// false;// false;// true;
	public static final boolean verboseTimelineCleaning = false;// true;// false;// false; // verbosity level 2: if
	// false it further
	// minimises verbosity
	public static final boolean verboseSAX = false;// false;
	public static boolean verboseLevenstein = false;// false;// true;// false;// false;// false;// false;
	public static final boolean verboseNormalisation = false;// false;// false;
	// public static boolean debuggingMessageEditDistance = false;
	public static final boolean verboseHilbert = false;
	public static final boolean verboseOTMDSAM = false;
	public static final boolean verboseDistance = false;// false;// true;// false;// false;
	public static final boolean verboseRankScoreCalcToConsole = false;// false;
	public static final boolean verboseEvaluationMetricsToConsole = false;// true;// true;// false;
	public static final boolean verboseCombinedEDist = false;
	/**
	 * Primarily for correlated
	 * <p>
	 * Make sure written file DistranceDistribution.csv has the following indexOfUserID = 0,indexOfNormAED = 5,
	 * indexOfNormFED = 6, indexOfTotalDis = 7, indexOfRawAED = 8, indexOfRawFED = 9,
	 */
	public static final boolean verboseDistDistribution = false; // Correlation, SWITCH_Nov20
	public static final boolean verboseCandFilter = false;
	public static final boolean verboseMSD = true;

	/**
	 * Whether to write the file EditDistancePerRtPerCand.csv (note: the files 'UserId'RecommTimesWithEditDistance.csv
	 * and EditDistancePerRtPerCand.csv have some similar information and have corresponding records.
	 */
	public static final boolean WriteEditDistancePerRtPerCand = false;// false;// true;// false;// false;// false; //
	public static final boolean WriteCandInEditDistancePerRtPerCand = false;// false;// false;// false;
	public static boolean WriteNumActsPerRTPerCand = false;// true;// false; // this information is redundant as well

	public static final boolean WriteRedundant = false;
	public static final boolean WriteEditOperatationsInEditDistancePerRtPerCand = false;// false;// true;
	public static final boolean WriteEditSimilarityCalculations = false;// false;// true;// true;//false;// false;//
																		// false;
	public static final boolean WriteActivityObjectsInEditSimilarityCalculations = false;// false;// false;
	public static final boolean WriteActivityObjectsInEditSimilarityCalculationsTrimmed = false;// false;// false;
	public static final boolean WriteNormalisation = false;// false;// false;
	public static final boolean WriteEditDistancesOfAllEndPoints = false;// false;
	public static final boolean WriteRTVerseNormalisationLogs = false;// true;// Jan8_2019_disabled true;
	public static final boolean WriteCandAEDDiffs = true;//// Jan8_2019_disabled true;

	public static final boolean WriteMInOfMinAndMaxOfMaxRTV = false;//// Jan8_2019_disabled 30 Nov 2018
	/**
	 * Write each cand of each RT in separate line
	 */
	public static final boolean WriteNormalisationsSeparateLines = true;// false;// false;// false;
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
	public static final boolean WriteTopNextActivitiesWithoutDistance = false;
	public static final boolean WriteTopNextActivitiesWithDistance = false;

	public static final boolean WriteRaw = false;// true;// SWITCH

	public static final boolean writeRankedRecommsWOScoreForEachSeqIndex = true;// false;
	public static final boolean writeDataActualForEachSeqIndex = true;
	public static final boolean writeTrainTestTimelinesAOsPerUser = true;

	public static final boolean writeRNN1PredProbDistribution = true;
	public static final boolean writeReplaceWtMultiplierMap = false;

	// End of curtain 30 May 2018 for running Toy

	// ======================================================================================

	// Start of 30 May 2018 ALL TRUE for toy
	// public static boolean alignmentDistanceStringPrintedOnce = false;
	// /**
	// * Check sanity of implementations for primary dimension perspective, these sanity checks are for when pd is
	// * activity id, since the earlier methods to compared with are for activity name and activity name and activity id
	// * are same for gowalla dataset.
	// */
	// public static final boolean checkSanityPDImplementn = false;
	//
	// public static final boolean printSanityCheck = false;
	//
	// public static final boolean disableWritingToFileForSpeed = true;
	// /**
	// * Controlling the verbosity of console log output
	// */
	// public static final boolean verbose = true;// false;// false;// false;// true;// false;// false;//
	// public static final boolean verboseAKOM = true;// false;// false;// false;// true;// false;// false;//
	// public static final boolean tempVerbose = false;// false;// true;// true;// false;// false;// true;
	// public static final boolean verboseTimelineCleaning = false;// true;// false;// false; // verbosity level 2: if
	// // false it further
	// // minimises verbosity
	// public static final boolean verboseSAX = false;// false;
	// public static boolean verboseLevenstein = true;// true;// false;// false;// false;// false;
	// public static final boolean verboseNormalisation = true;// false;// false;
	// // public static boolean debuggingMessageEditDistance = false;
	// public static final boolean verboseHilbert = false;
	// public static final boolean verboseOTMDSAM = false;
	// public static final boolean verboseDistance = true;// false;// true;// false;// false;
	// public static final boolean verboseRankScoreCalcToConsole = true;// false;
	// public static final boolean verboseEvaluationMetricsToConsole = false;// true;// true;// false;
	// public static final boolean verboseCombinedEDist = true;
	// public static final boolean verboseCandFilter = true;
	//
	// /**
	// * Whether to write the file EditDistancePerRtPerCand.csv (note: the files 'UserId'RecommTimesWithEditDistance.csv
	// * and EditDistancePerRtPerCand.csv have some similar information and have corresponding records.
	// */
	// public static final boolean WriteEditDistancePerRtPerCand = true;// false;// true;// false;// false;// false; //
	// public static final boolean WriteCandInEditDistancePerRtPerCand = true;// false;// false;// false;
	// public static boolean WriteNumActsPerRTPerCand = true;// true;// false; // this information is redundant as well
	//
	// public static final boolean WriteRedundant = false;
	// public static final boolean WriteEditOperatationsInEditDistancePerRtPerCand = true;// false;// true;
	// public static final boolean WriteEditSimilarityCalculations = true;// true;// true;//false;// false;// false;
	// public static final boolean WriteActivityObjectsInEditSimilarityCalculations = true;// false;// false;
	// public static final boolean WriteNormalisation = true;// false;// false;
	// public static final boolean WriteEditDistancesOfAllEndPoints = false;// false;
	// public static final boolean WriteRTVerseNormalisationLogs = true;
	//
	// /**
	// * Write each cand of each RT in separate line
	// */
	// public static final boolean WriteNormalisationsSeparateLines = true;// false;// false;
	// /**
	// * This is a useful file to write, disable it only when faster experiments are needed.
	// */
	// public static final boolean WriteRecommendationTimesWithEditDistance = true;// false;
	// public static final boolean WriteTimelines = false;
	// public static final boolean WriteLocationMap = false;// true;
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
	// public static final boolean writeRankedRecommsWOScoreForEachSeqIndex = true;// false;
	// public static final boolean writeDataActualForEachSeqIndex = true;
	// public static final boolean writeTrainTestTimelinesAOsPerUser = true;
	//
	// public static final boolean writeRNN1PredProbDistribution = true;
	// public static final boolean writeReplaceWtMultiplierMap = false;

	// End of 30 May 2018 ALL TRUE for toy
}
