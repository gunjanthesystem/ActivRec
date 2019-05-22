package org.activity.constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.tree.DefaultMutableTreeNode;

import org.activity.constants.Enums.ActDistType;
import org.activity.constants.Enums.DistToSimScoring;
import org.activity.constants.Enums.EditDistanceTimeDistanceType;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.Enums.TypeOfCandThreshold;
import org.activity.distances.AlignmentBasedDistance;
import org.activity.generator.DatabaseCreatorGowallaQuicker0;
import org.activity.io.EditDistanceMemorizer;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.Pair;
import org.activity.objects.TraceMatrix;
import org.activity.ui.PopUps;
import org.activity.ui.UIUtilityBox;
import org.activity.util.StringCode;
import org.activity.util.UtilityBelt;

import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2CharOpenHashMap;
import weka.classifiers.Classifier;

/**
 * Stores the global constant use throughout the framework and thus all member variable and methods are static.
 * 
 * @author gunjan TODO: try to minimise or remove public access to variable to reduce of chances of accidental changes
 *         in state of global variables
 */
public final class Constant
{
	public static final boolean verboseSAX = false;// false;TEMP FOR BACKUP COMPATIBILITY
	public static final boolean checkForDistanceTravelledAnomaly = false;// false;TEMP FOR BACKUP COMPATIBILITY
	// ////************* PARAMETERS TO BE SET ********************//////
	public static final boolean removeCurrentActivityNameFromRecommendations = false;// false;// true;

	// addded as constant on 19 July 2018
	public static final int lengthOfRecommendedSequence = 1;// TODO
	/**
	 * whether there threshold should be applied on candidate timelines based on edit distance
	 */
	// public static final boolean candidateThresholding = false; //
	public static final Enums.TypeOfThreshold[] typeOfiiWASThresholds = { Enums.TypeOfThreshold.Global };
	// String[] typeOfThresholds = { "Global" };// Global"};//"Percent", "None"

	/**
	 * Determines if thresholding is used to eliminate candidate timelines beyond the threshold distance. Thresholding
	 * as applied in iiWAS paper
	 * 
	 */
	public static final boolean useiiWASThreshold = false;

	/**
	 * Determines whether tolerance is used for comparing features when calculating edit distance
	 * <p>
	 * Not sure whether i used tolerance in the geolife experiments or not. I think i was using it and then i was told
	 * to not use it, I don't remember what was the setting for final experiment. But it could be the case that I was
	 * using tolerance. I think I was.
	 * <p>
	 * But i don't want to waste time on investigating this now as I critically need to move my research "Forward"
	 */
	public static final boolean useTolerance = true;// false;

	public static final boolean useHierarchicalDistance = false;// THIS IS BROKEN NOW BECAUSE OF REFACTORING AROUND
																// 3 AUG 2018. NEEDS TO BE RE-DONE
	// true; // SWITCH_NOV10//
	/**
	 * Determines whether the sorting of candiates is stable or unstable
	 */
	public static final boolean breakTiesWithShuffle = true;// true;// true; TEMP TODO

	// public static boolean write = false; // public static boolean writeAllDayTimelinesPerUser = true;

	/**
	 * Expunge the invalid Activity Objects even before the recommendation process starts
	 */
	public static final boolean EXPUNGE_INVALIDS_B4_RECOMM_PROCESS = true;// false;// true;
	// NOT NEEDED IN CASE OF GOWALLA //TODO check the places where this is involved so that it can safely be set to
	// false

	public static final Enums.TypeOfExperiment typeOfExperiment = Enums.TypeOfExperiment.RecommendationTests;

	// SWITCH_DEC20
	public static Enums.LookPastType lookPastType = Enums.LookPastType.NCount;//// TODO .NCount;//
	//// SWITCH_NOV10
	// NCount;// ClosestTime;// .NGram;// .Daywise;
	// Note that: current timeline extraction for PureAKOM is same as for NCount.
	// PureAKOM has no cand extraction
	// SWITCH_DEC20
	public static final ActDistType actLevelDistType = ActDistType.MyLevenshtein;// .MyLevenshtein;//MySimpleLevenshtein;//
	// TODO

	public static final boolean useForeignAwareLevenshtein = false; /// added on 9 Jan 2019//TODO

	public static final boolean useJan7DistanceComputations = false;// TODO
	public static final boolean considerFeatureValOrValDiff = false;// TODO true for val, false for val diff

	// ensureCandEndWithCurrAct ONLY IMPLEMENTED YET FOR NCOUNT
	public static final boolean ensureCandEndWithCurrAct = true;// true;// TEMP TODO

	public static final boolean ensureHasDaywiseCandsForEvalCompatibility = true;// TODO //added 14 Jan 2018

	public static Enums.AltSeqPredictor altSeqPredictor = Enums.AltSeqPredictor.PureAKOM;//// TODO SWITCH_NOV10
	// .RNN1;AKOM

	private static int AKOMHighestOrder = 1;// 1;// 3;// SWITCH_NOV10
	private static int RNNCurrentActivitityLength = 1;
	public static final boolean sameAKOMForAllRTsOfAUser = true;// SWITCH_NOV10
	public static final boolean sameRNNForAllRTsOfAUser = true;// SWITCH_NOV10
	public static final boolean sameRNNForALLUsers = true;// SWITCH_JUN

	/**
	 * determines if current timeline is allowed to go beyond the day boundaries, note that until the KDD paper, we were
	 * restricting this baseline to day boundaries
	 */
	public static final boolean DaywiseAllowSpillOverDaysOfCurr = true;

	/**
	 * determines if current timeline is allowed to go beyond the day boundaries, note that until the KDD paper, we were
	 * restricting this baseline to day boundaries
	 */
	public static final boolean ClosestTimeAllowSpillOverDays = true;

	/**
	 * Determines if candidate timelines only includes those days which contain the current activity name
	 */
	public static final boolean ClosestTimeFilterCandidates = false;// false; TODO

	public static final EditDistanceTimeDistanceType editDistTimeDistType = EditDistanceTimeDistanceType.NearerScaled;
	// .FurtherScaled;

	private static String dynamicDistanceUsed = "HJEditDistance"; // "FeatureWiseEditDistance",FeatureWiseEditDistance,
	// OTMDSAMEditDistance

	public static boolean useJarForMySimpleLevenshteinDistance = false;// true;

	/****** Evaluation Constants Start ***********/
	public static final boolean EvalPrecisionRecallFMeasure = true;// true;// false;
	/****** Evaluation Constants End ***********/

	static TimeZone timeZoneForExperiments = null;

	public static final int RoundingPrecision = 4;

	/**
	 * determines the hierarchical level of the activity name to be used in edit distance computation
	 */
	public static final int HierarchicalCatIDLevelForEditDistance = -1;// 2;// 1;// 2, -1 when not used

	public static final boolean buildRepAOJustInTime = false;
	public static final boolean preBuildRepAOGenericUser = true; // TODO think about it

	// --------------------------------------------------------------------------//
	// Start of parameters for Candidate timelines
	public static boolean collaborativeCandidates = false;// SWITCH_DEC20 // TODO

	// Number of candidate timelines extracted from each user in collaborative approach
	public static final boolean only1CandFromEachCollUser = false; // SWITCH_NOV10
	public static int numOfCandsFromEachCollUser = -1;//// SWITCH_NOV10

	/** the dates for each cand from the neighbours must be < the current date **/
	public static final boolean onlyPastFromRecommDateInCandInColl = false;// true;// false;

	public static boolean filterTrainingTimelinesByRecentDays = false;// SWITCH_DEC20 true;// TODO true;//
																		// SWITCH_NOV10
	private static int recentDaysInTrainingTimelines = 5;// 5;// SWITCH_NOV10

	// Filtering the candidate timeline ..
	public static Enums.TypeOfCandThreshold typeOfCandThresholdPrimDim = TypeOfCandThreshold.None;// TODO
	// .NearestNeighbour;//NearestNeighbour,
	// added on 8 Aug 2018
	public static final Enums.TypeOfCandThreshold typeOfCandThresholdSecDim = TypeOfCandThreshold.None;// .NearestNeighbour;
	// None,Percentile // SWITCH_NOV10

	public static final int filterCandByCurActTimeThreshInSecs = -1;// 10800;// -1; 18000; 3600 7200; //SWITCH_NOV10

	/**
	 * Keep only the n perecentile of candidates for each RT based on the lowest (unnormalised) edit distance, Scale:
	 * 0-100
	 */
	public static final double percentileCandEDThreshold = -1;// 55;// 25;// 100;// 25;// SWITCH_NOV10
	/**
	 * Select top n candidate by (unnormalised) edit distance,
	 */
	public static int nearestNeighbourCandEDThresholdPrimDim = -1;// TODO 500;// 750;// 500;// 500;/// -1;//
																	// 100;//
																	// 1500;// 100;//
	public static double candEDValThresholdPrimDim = -1;
	// -1 for no filter//SWITCH_NOV10
	// added on 23 July 2018 to keep it separate from the threshold used for primary dimension
	public static final int nearestNeighbourCandEDThresholdSecDim = 100;// changed to 100 on 8th May 2019500;// 0;
	public static final double candEDValThresholdSecDim = -1;// 0.5;

	// End of parameters for Candidate timelines
	// --------------------------------------------------------------------------//

	public static final boolean For9kUsers = false;// false;// ;// false; //SWITCH_NOV10

	////////////////////////////////////////////////////////////////////////

	public static final double ClosestTimeDiffThresholdInSecs = 10800; // 3 hrs

	public static final boolean NGramColl = false;// SWITCH_NOV10

	public static final boolean useMedianCinsForRepesentationAO = true; // "-1"// SWITCH_NOV10
	public static final boolean checkEDSanity = false;// true;// true;// SWITCH_NOV10

	private static double dynamicEDAlpha;// = 1;// 0.8;// 0.5;// SWITCH_NOV10
	public static boolean noFED = false;// Nov 15 2018
	public static boolean noAED = false;// Nov 15 2018
	// NOTE: if EDAlpha is not -1, then an alpha based combination of AED and FED is used. Here AED and FED can be
	// normalised either through RTV normalisation or through max possible AED and max possible FED normalisation
	public static final double[] EDAlphas = { 0.5 };// , 1 };// , 1 };// 1/* , 0.5 */ };// 0.5, 0.4, 0.6, 1, 0.8,
													// 0.7
	// };// 0.6
	// };//0.5, 1, 1};// 0.5, 1 };// 0.4, 0.5, 0.7,1 // TODO // SWITCH_NOV20
	// { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };= { 0.5, 1, 0.75, 0.25, 0.15, 0 };// -1 };// 0.5, 1, 0.75,
	// 0, 0.25, 0.35, 0.15};//SWITCH_NOV10 added on 12 Sep 2018// , 0.75, 0.25, 0 };// 0.25, 0.75, 1, 0 };// 0.75/*
	// 0.35, 0.75, 1, 0.15, 0, */

	public static final boolean disableRoundingEDCompute = true; // SWITCH_NOV10
	public static final boolean scoreRecommsByLocProximity = false;// SWITCH_NOV10
	public static final double wtScoreRecommsByLocProximity = 0.2;// SWITCH_NOV10

	//// TODO
	public static final boolean useActivityNameInFED = true; // KEEP ALWAYS TRUE FOR ACT AS PD
	public static boolean useStartTimeInFED = true;// SWITCH_NOV10

	// Only for gowalla1
	public static final boolean useLocationInFED = true;// SWITCH_NOV10
	public static boolean useHaversineDistInLocationFED = false;// SWITCH_NOV10 //added on Aug 11 2018
	public static final boolean usePopularityInFED = true;// SWITCH_NOV10
	public static boolean useDistFromPrevInFED = true;// SWITCH_NOV10
	public static boolean useDurationFromPrevInFED = true;// SWITCH_NOV10

	// Only for geolife1, added in Nov 18, 2018
	public static boolean useDurationInFED = true;// SWITCH_NOV10
	public static final boolean useDistTravelledInFED = true;// SWITCH_NOV10 //added on Aug 11 2018
	public static final boolean useStartGeoInFED = true;// SWITCH_NOV10
	public static final boolean useEndGeoInFED = true;// SWITCH_NOV10
	public static final boolean useAvgAltitudeInFED = true;// SWITCH_NOV10

	// Only for dcu_data_2, added in Dec 15, 2018
	// public static final boolean useEndTimeInFED = true;// SWITCH_NOV10

	public static final boolean useRTVerseNormalisationForED = true;// true; // TEMP TODO KEEP IT true, false version

	public static final boolean computeFEDForEachAOInRTVerse = false;// TODO
	public static final boolean computeFEDForEachFeatureSeqInRTVerse = true;// true;TODO
	public static final int takeMeanOrMedianOfFeatDiffsAcrossAllAOsInCandForFeatSeq = 0;// 0 for mean, 1 for median

	public static final boolean useMSDInFEDInRTVerse = false;// false;
	public static final boolean useLog2InFEDInRTVerse = false;// false;

	// may not have following process up to date (Aug 3, 2018)// SWITCH_April24
	public static double percentileForRTVerseMaxForFEDNorm = -1;// 10;//// TODO 10;// 75;// -1// SWITCH_April24
	public static final boolean fixedValPerFeatForRTVerseMaxMinForFEDNorm = false;// 75;// -1// SWITCH_Dec1
	public static final double percentileForRTVerseMaxForAEDNorm = -1;// //TODO 75;// SWITCH //added on 15 Aug 2018
	// For no features used, also set EDAlpha=1, so that the computed values for dAct are not multiplied by EDAlpha and
	// reduced.

	public static final double threshNormAEDForCand = -1;// added on 14 Aug 2018
	public static final double threshNormFEDForCand = -1;// -1;// added on 14 Aug 2018

	public static final boolean normaliseCandDistsAgainAfterFiltering = true;
	/**
	 * If enabled, in Edit distance, instead of computing feature level edit distance just for activity objects which
	 * matchin act name across the compared timelines, computed feature level edit distance over all act objs
	 */
	public static boolean useFeatureDistancesOfAllActs = true;// true;//// TODO true;// SWITCH_NOV10

	// need to implement it in AlignmentBasedDistance.getFeatureLevelDistanceGowallaPD25Feb2018() before turning true
	public static final boolean useDistFromNextInFED = false;
	public static final boolean useDurationFromNextInFED = false;

	public static final boolean useDecayInFED = false;// SWITCH_NOV10
	public static final boolean useTimeDecayInAED = false; /// added on 20 Aug 2018//TODO
	public static final double powerOfTimeDecayInAED = -1; /// added on 20 Aug 2018
	public static final boolean assignFallbackZoneIdWhenConvertCinsToAO = false;// true;//// SWITCH_NOV10
	public static boolean useRandomlySampled100Users = false;// //TODO toySwitch// SWITCH_NOV10
	// runForAllUsersAtOnce and useRandomlySampled100Users to be changed simultaneously
	/**
	 * Use only subset of the users from the randomly sampled users (useful for running small sample experiments for
	 * faster iterations)
	 */
	public static final boolean useSelectedGTZeroUsersFromRandomlySampled100Users = false;
	private static String dynamicPathToRandomlySampledUserIndices = "";

	public static boolean runForAllUsersAtOnce = true;// //TODO toySwitch // SWITCH_April8
	public static final boolean useCheckinEntryV2 = true;// keep it true as the other verion may not be uptodate
															// (Aug3,2018) SWITCH_April8
	public static boolean reduceAndCleanTimelinesBeforeRecomm = true;// false for gowalla// true for others;//
																		// SWITCH_April8

	public static final boolean cleanTimelinesAgainInsideRecommendationTests = false;// SWITCH_April11
	public static final boolean cleanTimelinesAgainInsideTrainTestSplit = false;// SWITCH_April24

	public static boolean debugFeb24_2018 = false;// SWITCH_NOV10
	public static final boolean useToyTimelines = false;// true;// toySwitch/SWITCH_AUG6

	// public static final int numOfHiddenLayersInRNN1 = 3;// 3;
	// public static final int numOfNeuronsInEachHiddenLayerInRNN1 = 500;
	public static final int numOfTrainingEpochsInRNN1 = 500;
	// public static final boolean varWidthPerHiddenLayerRNN1 = false;// true;
	public static final int[] neuronsInHiddenLayersRNN1 = { 500, 500, 500 };// { 512, 256 };
	public static final double learningRateInRNN1 = 0.001;
	public static final double l2RegularisationCoeffRNN1 = 0.001;
	public static final int exampleLengthInRNN1 = 1000;
	public static final int miniBatchSizeInRNN1 = 256;// 256;
	// public static final int lengthOfBPTTInRNN1 = 256; // 5587

	public static final boolean doVisualizationRNN1 = true;

	public static final boolean mapLocIDToGridID = true;

	public static final boolean doSecondaryDimension = true;
	public static final PrimaryDimension secondaryDimension = PrimaryDimension.LocationGridID;// LocationID;
	public static final boolean debug18July2018 = false;
	public static final boolean doWeightedEditDistanceForSecDim = false;// true;//SWITCH_AUG
	// public static GridDistancesProvider gdDistProvider; // added on 26 July 2018
	public static final double maxDistanceThresholdForLocGridDissmilarity = 25;// kms

	static String DATABASE_NAME = "gowalla1";// "geolife1";// "fsny1";// "dcu_data_2", "geolife1", "gowalla1"
												// ,"fsny1"// default
												// database name,
	// dcu_data_2";// "geolife1";// "start_base_2";databaseName

	public static final DistToSimScoring distToSimScoring = DistToSimScoring.OneMinusD;

	public static final boolean searchForOptimalFeatureWts = false;// true;
	public static final boolean purelyRandomPredictionNov25 = false;
	public static final boolean doForJupyterBaselines = false;// addded on 31 Dec 2018

	public static final boolean buildRepAOInfoPerUserForMemoryPerformance = false; // TODO done for single dimension
																					// recommendation but not yet done
																					// for multi dimension
																					// recommendation. added on 4 April
																					// 2019

	public static final boolean useBetterRepAOForLoc = false;// added on 6 April 2019

	////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Start of parameters less likely to change //////////////////

	// **** Parameters to set **** DO NOT CHANGE ****//
	public static final PrimaryDimension primaryDimension = PrimaryDimension.ActivityID;// LocationID;

	public static String rankScoring = "";// "sum";// default product"; // "sum"
	public static final Enums.CaseType caseType = Enums.CaseType.SimpleV3;// null;// String caseType CaseBasedV1";//
	// default// CaseBasedV1 " or SimpleV3

	// Redudant since we have lookPastType/**
	// * This variable is not used for anything currently but just to write to console the type of matching
	// static String typeOfTimelineMatching;// = "Daywise"; // N-count, N-hours

	/**
	 * ALPHA value for sum-based rank scoring
	 */
	public static double ALPHA = -99;// 0.25d;

	// before 14 Nov 2018
	// public static final boolean toSerializeJSONArray = false, toDeSerializeJSONArray = false, toCreateTimelines =
	// true, // false,
	// toSerializeTimelines = false, toDeSerializeTimelines = false;

	// since 14 Nov 2018 //for geolife
	// public static final boolean toSerializeJSONArray = false, toDeSerializeJSONArray = false, toCreateTimelines =
	// false, // false,
	// toSerializeTimelines = false, toDeSerializeTimelines = true;

	// since 14 Nov 2018 //for dcu_data_2
	// public static final boolean toSerializeJSONArray = false, toDeSerializeJSONArray = false, toCreateTimelines =
	// true, // false,
	// toSerializeTimelines = false, toDeSerializeTimelines = false;
	public static Boolean toSerializeJSONArray = null, toDeSerializeJSONArray = null, toCreateTimelines = null,
			toSerializeTimelines = null, toDeSerializeTimelines = null;

	public static boolean hasInvalidActivityNames;// = false;

	public static String INVALID_ACTIVITY1 = "";// "Unknown";
	public static String INVALID_ACTIVITY2 = "";// "Not Available";

	// Added on 11 July 2017
	public static Integer INVALID_ACTIVITY1_ID = null;// -997;// "Unknown";
	public static Integer INVALID_ACTIVITY2_ID = null;// -998;// "Not Available";
	// end of Added on 11 July 2017

	public static final double maxForNorm = 9999999; // assuming unnormalised edit distance is never greater than this
	public static final double minForNorm = -9999999; // assuming unnormalised edit distance is never lower than this.
	// note: in current form edit distance cannot be negative

	public static final double distanceTravelledAlert = 200; // kms
	public static final double unknownDistanceTravelled = -9999;

	/**
	 * <p>
	 * <font color="red">Num of decimal digits to be kept in latitude and longitude. Latitude and longitude HAVE to be
	 * kept in database in decimal format and they HAVE to have atmost 6 decimal places.</font> This affects during
	 * hilbert space filled curve index for linearisation of geo coordinates. <b>This limits the precision of hilbert
	 * sace filling curve index</b>
	 * </p>
	 */
	public static final int decimalPlacesInGeocordinatesForComputations = 100000;// 1000000;

	public static String geolife1howManyUsers = "UsersAbove10RTs";// "UsersAbove10RTs";
	// "AllUsers";// "TenUsers";// "AllUsers" "UsersAbove10RTs"

	public static final String errorFileName = "ErrorExceptionLogFile.txt";
	public static final String warningFileName = "WarniningLogFile.txt";
	public static final String messageFileName = "MessageLogFile.txt";
	public static final String configFileName = "ConfigLogFile.txt";

	/**
	 * Number of past activities to look excluding the current activity
	 */
	public static final double matchingUnitAsPastCount[] = { 3, 0, 1, 2, 4, 6, 8, 5, 7 };// { 3, 0, 1, 2, /* 3, */ 4, 6,
	// 8;// { 0, 1, 2, 3, 4,// 6,};//2, 4,6, 8, 1, 3, 10 11, 12,13,14, 15,// 16,// 17, 18, 19, 20,21, 22, 23, 24,26, 28,
	// 30 };// , 32,// 34,36, 38,40,42 };

	public static final double matchingUnitAsPastCountFixed[] = { 0, 1, 2, 3, 4, 6, 8 };
	public static final double matchingUnitHrsArray[] = { 0.5, 1, 2, 3, 4, 6, 8 };
	// { 0.5, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
	// 17, 18, 19, 20, 21, 22, 23, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42 };

	public static final double matchingDummy[] = { -1 };
	// public static final double matchingUnitHrsArray[] = { 24, 26, 28, 30, 32, 34, 36, 38, 40, 42 };

	/**
	 * Percentage/100 for training test split of dataset
	 */
	public static final double percentageInTraining = 0.8;// 0.8;

	/**
	 * Determines if connection pooling is used for database connections
	 */
	public static final boolean USEPOOLED = true;

	/**
	 * Whether to consider all features while calculating feature wise edit distance
	 */
	public static boolean considerAllFeaturesForFeatureWiseEditDistance = false;// true;

	/**
	 * If all features are not to be used for feature wise edit distance, then choose which features are to be used.
	 */
	public static boolean considerActivityNameInFeatureWiseEditDistance = false,
			considerStartTimeInFeatureWiseEditDistance = false, considerDurationInFeatureWiseEditDistance = false,
			considerDistanceTravelledInFeatureWiseEditDistance = false,
			considerStartGeoCoordinatesInFeatureWiseEditDistance = false,
			considerEndGeoCoordinatesInFeatureWiseEditDistance = false,
			considerAvgAltitudeInFeatureWiseEditDistance = false;

	public static boolean UsingSQLDatabase;

	/**
	 * to have the same RTs in daywise and MU, some RTs in MU have to be blacklisted as they did not had any cand
	 * timeline in daywise
	 */
	public static final boolean BLACKLISTING = false;// true;// true;

	public static final boolean blacklistingUsersWithLargeMaxActsPerDay = true;

	public static final boolean DoBaselineDuration = true, DoBaselineOccurrence = true, DoBaselineNGramSeq = false;

	///////////////////////////// End of parameters less likely to change ///////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Start of variable declarations //////////////////////////
	static String commonPath; // ALWAYS UPADTE THE CURRENT PATH TO THE CURRENT WORKING PATH
	/**
	 * Path to the folder were all the results will be stored
	 */
	static String outputCoreResultsPath = "";

	// @Deprecated
	static String[] activityNames;
	// static Map<Integer, Integer> actIDNameIndexMap;// <actID, index of actID in activityNames array>
	@Deprecated
	static Map<String, Integer> actIDNameIndexMap;// <actID, index of actID in activityNames array>

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private static TreeSet<Integer> uniqueActivityIDs; // changed to TreeSet on 31 Dec 2018
	private static LinkedHashMap<String, TreeSet<Integer>> uniquePDValsPerUser;

	private static Set<Integer> uniqueLocationIDs;
	private static TreeMap<Integer, TreeSet<Integer>> uniqueLocationIDsPerActID;// actIDLocIDsMap;

	// map of {userID,{unique actIDs for this user, {unique locIDs for this actID for this userID}}}
	private static TreeMap<String, TreeMap<Integer, LinkedHashSet<Integer>>> userIDActIDLocIDsMap;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	// public static TreeMap<Integer, Character> catIDCharCodeMap = null;
	// public static TreeMap<Character, Integer> charCodeCatIDMap = null;
	static Int2CharOpenHashMap actIDCharCodeMap = null;
	public static Char2IntOpenHashMap charCodeActIDMap = null;

	public static ArrayList<String> activityNamesGowallaLabels;

	static double currentMatchingUnit = -99; // stores the current matching unit at all times, used for some
	// sanity checks

	public static TraceMatrix reusableTraceMatrix;
	static EditDistanceMemorizer editDistancesMemorizer;
	public static final boolean memorizeEditDistance = false;
	final static int editDistancesMemorizerBufferSize = 1;// 000000;

	public static final boolean needsToPruneFirstUnknown = true;// true for dcu_data_2 false;

	public static final double epsilonForFloatZero = 1.0E-50;// to compare if floating point numbers are equal.
	public static final double epsilonForFloatZeroRelaxed = 1.0E-5;// to compare if floating point numbers are equal.
	public static final boolean doMaxNormalisationWhenNormalisationEDsOverCandSet = false;//
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// End of variable declarations //////////////////////////////////
	//// DO NOT CHANGE THE ABOVE COMMENTED LINE AS IT IS USED AS A MARKER WHEN REFLECTING THIS FILE
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	// public static String getPathToRandomlySampledUserIndices()
	// {
	// return pathToRandomlySampledUserIndices;
	// }
	//
	// public static void setPathToRandomlySampledUserIndices(String pathToRandomlySampledUserIndices)
	// {
	// Constant.pathToRandomlySampledUserIndices = pathToRandomlySampledUserIndices;
	// }
	//

	public static double getDynamicEDAlpha()
	{
		return dynamicEDAlpha;
	}

	public static void setDynamicEDAlpha(double dynamicEDAlpha)
	{
		Constant.dynamicEDAlpha = dynamicEDAlpha;
	}

	public static String getDynamicPathToRandomlySampledUserIndices()
	{
		return dynamicPathToRandomlySampledUserIndices;
	}

	public static void setDynamicPathToRandomlySampledUserIndices(String dynamicPathToRandomlySampledUserIndices)
	{
		Constant.dynamicPathToRandomlySampledUserIndices = dynamicPathToRandomlySampledUserIndices;
	}

	/**
	 * 
	 * @param givenDimension
	 * @return
	 * @since 25 July 2018
	 */
	public static final int getNearestNeighbourCandEDThresholdGivenDim(PrimaryDimension givenDimension)
	{
		if (givenDimension.equals(Constant.primaryDimension))
		{
			return nearestNeighbourCandEDThresholdPrimDim;
		}
		else if (givenDimension.equals(Constant.secondaryDimension))
		{
			return nearestNeighbourCandEDThresholdSecDim;
		}
		PopUps.showError(
				"Error in getNearestNeighbourCandEDThresholdGivenDim: unrecognised given dimension: " + givenDimension);
		return -9999;

	}

	/**
	 * 
	 * @param givenDimension
	 * @return
	 * @since 9 Aug 2018
	 */
	public static final double getEDThresholdGivenDim(PrimaryDimension givenDimension)
	{
		if (givenDimension.equals(Constant.primaryDimension))
		{
			return candEDValThresholdPrimDim;
		}
		else if (givenDimension.equals(Constant.secondaryDimension))
		{
			return candEDValThresholdSecDim;
		}
		PopUps.showError("Error in geEDThresholdGivenDim: unrecognised given dimension: " + givenDimension);
		return -9999;

	}

	// public static final int nearestNeighbourCandEDThresholdSecDim = 100;

	public static final boolean equalsForFloat(double a, double b)
	{
		if (Math.abs(a - b) < Constant.epsilonForFloatZero)
			return true;
		else
			return false;
	}

	public static final boolean equalsForFloat(Double a, Double b)
	{
		if (Math.abs(a - b) < Constant.epsilonForFloatZero)
			return true;
		else
			return false;
	}

	/**
	 * 
	 * @return
	 */
	public static int getAKOMHighestOrder()
	{
		return AKOMHighestOrder;
	}

	/**
	 * 
	 * @param aKOMHighestOrder
	 */
	public static void setAKOMHighestOrder(int aKOMHighestOrder)
	{
		AKOMHighestOrder = aKOMHighestOrder;
	}

	/**
	 * 
	 * @return
	 */
	public static int getRecentDaysInTrainingTimelines()
	{
		return recentDaysInTrainingTimelines;
	}

	/**
	 * 
	 * @param recentDaysInTrainingTimelines
	 */
	public static void setRecentDaysInTrainingTimelines(int recentDaysInTrainingTimelines)
	{
		Constant.recentDaysInTrainingTimelines = recentDaysInTrainingTimelines;
	}

	/**
	 * 
	 * @return
	 */
	public static String getOutputCoreResultsPath()
	{
		return outputCoreResultsPath;
	}

	/**
	 * 
	 * @param outputCoreResultsPath
	 */
	public static void setOutputCoreResultsPath(String outputCoreResultsPath)
	{
		Constant.outputCoreResultsPath = outputCoreResultsPath;
	}

	/**
	 * 
	 * @param candTimelineID
	 * @param currentTimelineID
	 * @param editDistanceForThisCandidate
	 */
	public static void addToEditDistanceMemorizer(String candTimelineID, String currentTimelineID,
			Pair<String, Double> editDistanceForThisCandidate)
	{
		editDistancesMemorizer.addToMemory(candTimelineID, currentTimelineID, editDistanceForThisCandidate);
	}

	public static void setDefaultTimeZone(String timeZoneString)
	{
		timeZoneForExperiments = TimeZone.getTimeZone(timeZoneString);
		TimeZone.setDefault(timeZoneForExperiments);
	}

	public static TimeZone getTimeZone()
	{
		return timeZoneForExperiments;
	}

	////////////
	/**
	 * Return matching unit array based on lookPastType and altSeqPredictor
	 * 
	 * @param lookPastType
	 * @param altSeqPredictor
	 * @return
	 */
	public static double[] getMatchingUnitArray(Enums.LookPastType lookPastType, Enums.AltSeqPredictor altSeqPredictor)
	{
		double matchingUnitArray[] = new double[] { -99 };

		if (altSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM))// "Hrs"))
		{
			// matchingUnitArray = new double[] { 0 }; // disabled on 31 Jan 2018
			matchingUnitArray = new double[] { Constant.AKOMHighestOrder - 1 }; // added on 31 Jan 2018
			// System.out.println("Here set");
			// PopUps.showError("Here Set");
		}
		else if (altSeqPredictor.equals(Enums.AltSeqPredictor.RNN1))
		{
			matchingUnitArray = new double[] { Constant.RNNCurrentActivitityLength - 1 };
		}

		else if (lookPastType.equals(Enums.LookPastType.NCount))// "Count"))
		{
			matchingUnitArray = Constant.matchingUnitAsPastCount;
		}
		else if (lookPastType.equals(Enums.LookPastType.NHours))// "Hrs"))
		{
			matchingUnitArray = Constant.matchingUnitHrsArray;
		}
		else if (lookPastType.equals(Enums.LookPastType.Daywise))// "Hrs"))
		{
			matchingUnitArray = new double[] { -9999 };
		}
		else if (lookPastType.equals(Enums.LookPastType.ClosestTime))// "Hrs"))
		{
			matchingUnitArray = new double[] { -9999 };
		}
		// else if (lookPastType.equals(Enums.LookPastType.ClosestTime))// "Hrs"))
		// {
		// matchingUnitArray = new double[] { -9999 };
		// }

		else if (lookPastType.equals(Enums.LookPastType.NGram))// "Hrs"))
		{
			matchingUnitArray = new double[] { 0 };
		}

		// else if (Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM))// "Hrs"))
		// {
		// matchingUnitArray = new double[] { 0 };
		// System.out.println("Here set");
		// PopUps.showError("Here Set");
		// }
		// else if
		else
		{
			System.err.println(
					"Error: unknown look past type in in setMatchingUnitArray() RecommendationTests():" + lookPastType);
			System.exit(-1);
		}
		// PopUps.showMessage("matchingUnitArray = " + Arrays.toString(matchingUnitArray));
		return matchingUnitArray;
	}

	/////////////
	/**
	 * Determines if the given matching unit array has any value in fractions. </br>
	 * Useful to ensure no damage done if converting double to integer
	 * 
	 * @param matchingUnitsArrayUsed
	 * @return
	 */
	public static boolean isFractionsInMatchingUnits(double matchingUnitsArrayUsed[])
	{
		// boolean flag = true;
		for (double mu : matchingUnitsArrayUsed)
		{
			if (mu % 1 != 0)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Don't let anyone instantiate this class.
	 */
	private Constant()
	{
	}

	// /////////
	/**
	 * note: all variables are static for this class
	 * 
	 * @param givenCommonpath
	 */
	/**
	 * setDatabaseName setUserIDs setInvalidNames setActivityNames setCommonPath
	 * 
	 * @param givenCommonpath
	 * @param databaseName
	 */
	public static void initialise(String givenCommonpath, String databaseName)
	{
		Constant.setDatabaseName(databaseName);
		Constant.UsingSQLDatabase = false;
		Constant.setUserIDs();
		Constant.setInvalidNames();
		Constant.setActivityNames();
		Constant.setCommonPath(givenCommonpath);

		// Constant.setDistanceUsed("HJEditDistance");
	}

	/**
	 * 
	 * /**
	 * 
	 * @param givenCommonpath
	 * @param databaseName
	 * @param catIDsHierDistSerialisedFile
	 * @param pathToSerialisedCatIDNameDictionary
	 * @param pathToSerialisedLocationObjects
	 * @param pathToSerialisedUserObjects
	 * @param pathToSerialisedGowallaLocZoneIdMap
	 * @param canSetGridIndexPairHaversineDistMaps
	 *            //added on 7 Aug 2018 to avoid unessential deserialisation in case of evaluation.
	 */
	public static void initialise(String givenCommonpath, String databaseName, String catIDsHierDistSerialisedFile,
			String pathToSerialisedCatIDNameDictionary, String pathToSerialisedLocationObjects,
			String pathToSerialisedUserObjects, String pathToSerialisedGowallaLocZoneIdMap,
			boolean canSetGridIndexPairHaversineDistMaps)
	{
		initialise(databaseName, catIDsHierDistSerialisedFile, pathToSerialisedCatIDNameDictionary,
				pathToSerialisedLocationObjects, pathToSerialisedUserObjects, pathToSerialisedGowallaLocZoneIdMap,
				canSetGridIndexPairHaversineDistMaps);
		Constant.setCommonPath(givenCommonpath);
	}

	/**
	 * 
	 * Note on 15 Dec 2018:
	 * <p>
	 * setInvalidActIDs() onlycalled when org.activity.constants.DomainConstants.catIDNameDictionary is not null. Hence,
	 * setInvalidActIDs will not be called while DCU timelines are being created, hence, it is important to call this
	 * method again org.activity.constants.Constant.initialise(String, String, String, String, String, String, boolean)
	 * after creation of DCU timelines.
	 * <p>
	 * 
	 * @param givenCommonpath
	 * @param databaseName
	 * @param catIDsHierDistSerialisedFile
	 * @param pathToSerialisedCatIDNameDictionary
	 * @param pathToSerialisedLocationObjects
	 * @param pathToSerialisedUserObjects
	 * @param pathToSerialisedGowallaLocZoneIdMap
	 * @param canSetGridIndexPairHaversineDistMaps
	 *            //added on 7 Aug 2018 to avoid unessential deserialisation in case of evaluation.
	 */
	public static void initialise(String databaseName, String catIDsHierDistSerialisedFile,
			String pathToSerialisedCatIDNameDictionary, String pathToSerialisedLocationObjects,
			String pathToSerialisedUserObjects, String pathToSerialisedGowallaLocZoneIdMap,
			boolean canSetGridIndexPairHaversineDistMaps)
	{

		Constant.setDatabaseSpecificConstants(databaseName);
		Constant.setDatabaseName(databaseName);
		Constant.UsingSQLDatabase = false;
		Constant.setUserIDs();
		Constant.setInvalidNames();
		Constant.setActivityNames();
		Constant.setConstantsForTimelineCreation(databaseName);
		// Constant.setCommonPath(givenCommonpath);
		if (databaseName.equals("gowalla1"))
		{
			DomainConstants.setCatIDsHierarchicalDistance(catIDsHierDistSerialisedFile);
			DomainConstants.setCatIDNameDictionary(pathToSerialisedCatIDNameDictionary);
			// Disabled setLocIDLocationObjectDictionary as it was not essential, we only needed loc name and full
			// objects
			// were taking signficantmemory space.
			DomainConstants.setLocIDLocationObjectDictionary(pathToSerialisedLocationObjects);
			DomainConstants.setLocationIDNameDictionary(DomainConstants.getLocIDLocationObjectDictionary());// pathToSerialisedLocationObjects);
			Constant.setActIDCharCodeMap(DomainConstants.catIDNameDictionary);
			DomainConstants.setCatIDGivenLevelCatIDMap();
			DomainConstants.setUserIDUserObjectDictionary(pathToSerialisedUserObjects);
			DomainConstants.setGowallaLocZoneIdMap(pathToSerialisedGowallaLocZoneIdMap);
			DomainConstants.setGridIDLocIDGowallaMaps();
			// TODO: take the path as an argument.
			DomainConstants.setCatIDLevelWiseCatIDsList(PathConstants.pathToSerialisedLevelWiseCatIDsDict);
		}

		if (Constant.memorizeEditDistance)
		{
			editDistancesMemorizer = new EditDistanceMemorizer(Constant.editDistancesMemorizerBufferSize);
		} // Constant.setDistanceUsed("HJEditDistance");

		setActIDNameIndexMap(databaseName, Constant.getActivityNames());

		if (databaseName.equals("gowalla1"))
		{
			if (Constant.doWeightedEditDistanceForSecDim
					|| (Constant.useHaversineDistInLocationFED && Constant.useLocationInFED))
			{
				DomainConstants.setGridIndexPairHaversineDistMaps();
				// gdDistProvider = new GridDistancesProvider(PathConstants.pathToSerialisedGridIndexPairDist,
				// PathConstants.pathToSerialisedGridIndexPairDistConverter);
			}
		}

		if (databaseName.equals("dcu_data_2"))
		{
			Constant.hasInvalidActivityNames = true;
			if (DomainConstants.catIDNameDictionary != null)
			{
				setInvalidActIDs();
			}
		}
		else
		{
			Constant.hasInvalidActivityNames = false;
		}

	}

	public static void setDatabaseSpecificConstants(String databaseName)
	{
		switch (databaseName)
		{
		case "dcu_data_2":
			collaborativeCandidates = false;
			filterTrainingTimelinesByRecentDays = false;
			typeOfCandThresholdPrimDim = TypeOfCandThreshold.NearestNeighbour;// TODO
			nearestNeighbourCandEDThresholdPrimDim = 50;// 100;// 100;// TODO 500;// 750;// 500;// 500;/// -1;//
			// candEDValThresholdPrimDim = 0.5; // TEMP TODO
			percentileForRTVerseMaxForFEDNorm = 10;// 10;// 20;//// TODO 10;// 75;// -1// SWITCH_April24
			useFeatureDistancesOfAllActs = true;//// TODO true;// SWITCH_NOV10
			useRandomlySampled100Users = false;// //TODO toySwitch// SWITCH_NOV10
			runForAllUsersAtOnce = true;// //TODO toySwitch // SWITCH_April8
			reduceAndCleanTimelinesBeforeRecomm = true; // false for gowalla// true for others;//

			// Constant.useDurationInFED = false;// TODO TEMP
			break;

		case "geolife1":
			collaborativeCandidates = false;
			filterTrainingTimelinesByRecentDays = false;
			// typeOfCandThresholdPrimDim = TypeOfCandThreshold.NearestNeighbour;// TODO
			typeOfCandThresholdPrimDim = TypeOfCandThreshold.NearestNeighbour;// .NearestNeighbour;// TEMP TODO
			nearestNeighbourCandEDThresholdPrimDim = 100;// 100;// 50;// 500;// TEMP 500;// TODO 500;// 750;// 500;//
			// candEDValThresholdPrimDim = 0.5; // TEMP TODO
			percentileForRTVerseMaxForFEDNorm = 10;// 75;// 10;//// TODO 10;// 75;// -1// SWITCH_April24
			useFeatureDistancesOfAllActs = true;// TEMP true;// TODO true;// SWITCH_NOV10
			useRandomlySampled100Users = false;// //TODO toySwitch// SWITCH_NOV10
			runForAllUsersAtOnce = true;// //TODO toySwitch // SWITCH_April8
			reduceAndCleanTimelinesBeforeRecomm = true; // false for gowalla// true for others;//
			useDistFromPrevInFED = false;
			useDurationFromPrevInFED = false;
			break;

		case "gowalla1":
			collaborativeCandidates = true;// true; TODO TEMP ALERT ALERT
			filterTrainingTimelinesByRecentDays = true;// true; TODO TEMP ALERT ALERT
			typeOfCandThresholdPrimDim = TypeOfCandThreshold.NearestNeighbour;// TODO
			nearestNeighbourCandEDThresholdPrimDim = 100;// TODO 500;// 750;// 500;// 500;/// -1;//
			percentileForRTVerseMaxForFEDNorm = 10;//// TODO 10;// 75;// -1// SWITCH_April24
			useFeatureDistancesOfAllActs = true;//// TODO true;// SWITCH_NOV10
			useRandomlySampled100Users = true;// //TODO toySwitch// SWITCH_NOV10
			runForAllUsersAtOnce = false;// //TODO toySwitch // SWITCH_April8
			reduceAndCleanTimelinesBeforeRecomm = false; // false for gowalla// true for others;//
			useHaversineDistInLocationFED = false;// TEMPtrue;// true;// TEMP TODO IMPORTANT TODO TODO TODO

			break;

		case "fsny1":
			collaborativeCandidates = true;
			filterTrainingTimelinesByRecentDays = true;
			typeOfCandThresholdPrimDim = TypeOfCandThreshold.NearestNeighbour;// TODO
			nearestNeighbourCandEDThresholdPrimDim = 500;// TODO 500;// 750;// 500;// 500;/// -1;//
			percentileForRTVerseMaxForFEDNorm = -1;//// TODO 10;// 75;// -1// SWITCH_April24
			useFeatureDistancesOfAllActs = true;//// TODO true;// SWITCH_NOV10
			useRandomlySampled100Users = false;// //TODO toySwitch// SWITCH_NOV10
			runForAllUsersAtOnce = true;// //TODO toySwitch // SWITCH_April8
			reduceAndCleanTimelinesBeforeRecomm = false; // false for gowalla// true for others;//

			break;
		default:
			PopUps.printTracedErrorMsgWithExit(
					"setConstantsForTimelineCreation: Error: unknown database :" + databaseName);

		}

	}

	public static void setConstantsForTimelineCreation(String databaseName)
	{
		switch (databaseName)
		{
		case "dcu_data_2":
			toSerializeJSONArray = false;
			toDeSerializeJSONArray = false;
			toCreateTimelines = true;
			toSerializeTimelines = false;
			toDeSerializeTimelines = false;
			break;

		case "geolife1":
			toSerializeJSONArray = false;
			toDeSerializeJSONArray = false;
			toCreateTimelines = false;
			toSerializeTimelines = false;
			toDeSerializeTimelines = true;
			break;

		case "gowalla1":
			toSerializeJSONArray = false;
			toDeSerializeJSONArray = false;
			toCreateTimelines = true;
			toSerializeTimelines = false;
			toDeSerializeTimelines = false;
			break;

		case "fsny1":
			toSerializeJSONArray = false;
			toDeSerializeJSONArray = false;
			toCreateTimelines = true;
			toSerializeTimelines = false;
			toDeSerializeTimelines = false;
			break;

		default:
			PopUps.printTracedErrorMsgWithExit(
					"setConstantsForTimelineCreation: Error: unknown database :" + databaseName);

		}

	}

	/**
	 * @since Aug 7 2018
	 */
	public static void releaseHeavyObjectsNotNeededAfterRecommendation()
	{
		DomainConstants.unsetGridIndexPairHaversineDistMaps();
	}
	//

	/**
	 * setDatabaseName setUserIDs setInvalidNames setActivityNames setCommonPath setReusableTraceMatrix
	 * 
	 * 
	 * @param givenCommonpath
	 * @param databaseName
	 * @param maxActsInDay
	 */
	public static void initialise(String givenCommonpath, String databaseName, int maxActsInDay)
	{
		Constant.setDatabaseName(databaseName);
		Constant.UsingSQLDatabase = false;
		Constant.setUserIDs();
		Constant.setInvalidNames();
		Constant.setActivityNames();
		Constant.setCommonPath(givenCommonpath);
		Constant.setReusableTraceMatrix(maxActsInDay, maxActsInDay);
		// Constant.setDistanceUsed("HJEditDistance");
	}
	// /**
	// * Seems its not being used 26 Jab 2016)
	// * @param givenCommonpath
	// * @param databaseName
	// * @param distUsed
	// */
	// public static void initialise(String givenCommonpath, String databaseName, String distUsed)
	// {
	// Constant.setDatabaseName(databaseName);
	// Constant.setUserIDs();
	// Constant.setInvalidNames();
	// Constant.setActivityNames();
	// Constant.setCommonPath(givenCommonpath);
	// // Constant.setDistanceUsed(distUsed);
	// }

	// /////////
	/**
	 * Set actIDNameIndexMap which is map of <actID, index of actID in activityNames array>
	 * 
	 * @param databaseName
	 * @param activityNames
	 * @return
	 */
	private static boolean setActIDNameIndexMap(String databaseName, String[] activityNames)
	{
		Map<String, Integer> res = new LinkedHashMap<>(activityNames.length);

		if (databaseName.equals("gowalla1") || databaseName.equals("geolife1") || databaseName.equals("dcu_data_2")
				|| databaseName.equals("fsny1"))
		{
			int index = 0;
			for (String activityName : activityNames)
			{ // since in gowalla dataset act name is act id
				// res.put(Integer.valueOf(activityName), index++);
				res.put((activityName), index++);
			}
			actIDNameIndexMap = res;

			if (databaseName.equals("geolife1"))
			{
				System.err.println("Warning: setActIDNameIndexMap not yet verified for " + databaseName);
			}
			return true;
		}
		else
		{
			PopUps.printTracedErrorMsgWithExit("Not checked correctness for databaseName=" + databaseName);
		}
		return false;

	}

	/**
	 * Get actIDNameIndexMap which is map of <actID, index of actID in activityNames array>
	 * 
	 * @return
	 */
	public static Map<String, Integer> getActIDNameIndexMap()
	{
		return actIDNameIndexMap;
	}

	/**
	 * Get index of actID in activityNames.
	 * <p>
	 * This can be obtained from the map of <actID, index of actID in activityNames array>
	 */
	public static Integer getIndexOfActIDInActNames(Integer actID)
	{
		// System.out.println("actIDNameIndexMap.size()=" + actIDNameIndexMap.size() + " given actID=" + actID
		// + " contains=" + actIDNameIndexMap.containsKey(actID));

		// System.out.println(actIDNameIndexMap);
		return actIDNameIndexMap.get(actID);
	}

	public static Classifier getClassifierUsed()
	{
		return ClusteringConstants.classifierUsed;
	}

	public static void setClassifier(Classifier classifier)
	{
		ClusteringConstants.classifierUsed = classifier;
	}

	/**
	 * 
	 * @return
	 */
	public static double getCurrentMatchingUnit()
	{
		return currentMatchingUnit;
	}

	/**
	 * 
	 * @param currentMatchingUnit
	 */
	public static void setCurrentMatchingUnit(double currentMatchingUnit)
	{
		Constant.currentMatchingUnit = currentMatchingUnit;
	}

	/**
	 * set the features to be considered for feature wise edit distance
	 * 
	 * @param activityName
	 * @param startTime
	 * @param duration
	 * @param distanceTravelled
	 * @param startGeo
	 * @param endGeo
	 * @param avgAltitude
	 */
	public static void setFeatureToConsiderForFeatureWiseEditDistance(boolean activityName, boolean startTime,
			boolean duration, boolean distanceTravelled, boolean startGeo, boolean endGeo, boolean avgAltitude)
	{
		considerActivityNameInFeatureWiseEditDistance = activityName;
		considerStartTimeInFeatureWiseEditDistance = startTime;
		considerDurationInFeatureWiseEditDistance = duration;
		considerDistanceTravelledInFeatureWiseEditDistance = distanceTravelled;
		considerStartGeoCoordinatesInFeatureWiseEditDistance = startGeo;
		considerEndGeoCoordinatesInFeatureWiseEditDistance = endGeo;
		considerAvgAltitudeInFeatureWiseEditDistance = avgAltitude;
	}

	private static int userIDs[];

	// public static void setWriteAllDayTimelinesPerUser(boolean value)
	// {
	// writeAllDayTimelinesPerUser = value;
	// }
	// removeDayTimelinesWithOneOrLessDistinctValidAct

	public static void setDistanceUsed(String dname) // throws Exception
	{
		dname = dname.trim();

		switch (dname)
		{
		case "HJEditDistance":
			dynamicDistanceUsed = "HJEditDistance";
			break;
		case "FeatureWiseEditDistance":
			dynamicDistanceUsed = "FeatureWiseEditDistance";
			break;

		case "FeatureWiseWeightedEditDistance":
			dynamicDistanceUsed = "FeatureWiseWeightedEditDistance";
			break;

		case "OTMDSAMEditDistance":
			dynamicDistanceUsed = "OTMDSAMEditDistance";
			break;
		default:
			PopUps.showError(
					"Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance specified:" + dname);
			System.err.println(
					"Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance specified:" + dname);
			// throw new
			// Exception("Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance specified:"
			// + dname);
			System.exit(-1);
		}
		System.out.println("setDistanceUsed() called: dynamicDistanceUsed= " + dynamicDistanceUsed);
		// if(dname.trim().equals("HJEditDistance")
	}

	public static String getDistanceUsed()
	{
		return dynamicDistanceUsed;
	}

	/**
	 * Sets the common path for the whole application: this is the current path for reading and writing for data.
	 * 
	 * @param commonpath
	 */
	public static void setCommonPath(String commonpath)
	{
		commonPath = commonpath;// "/home/gunjan/MATLAB/bin/DCU data works/July20/New_10_Aug/";
	}// public static final String dataLoaderPath="/home/gunjan/MATLAB/bin/DCU data works/July20/New_10_Aug/";

	public static void setReusableTraceMatrix(int wordLen1, int wordLen2)
	{
		reusableTraceMatrix = new TraceMatrix(wordLen1, wordLen2);
	}

	public static String getCommonPath()
	{
		return commonPath;
	}

	public static String cleanedDayTimelines()
	{
		return commonPath;
	}

	public static String getInvalidActivity1()
	{
		return INVALID_ACTIVITY1;
	}

	public static String getInvalidActivity2()
	{
		return INVALID_ACTIVITY2;
	}

	// public static void setVerbose(boolean value)
	// {
	// VerbosityConstants.verbose = value;
	// }

	public static boolean getVerbose()
	{
		return VerbosityConstants.verbose;
	}

	public static void setWriteNumActsmatchingUnit(boolean value)
	{
		VerbosityConstants.WriteNumActsPerRTPerCand = value;
	}

	/**
	 * <font color = red>for gowalla </font>
	 */
	public static void setUserIDs(int givenUserIDs[])
	{
		userIDs = givenUserIDs;
	}

	/**
	 * <font color = red>not yet set for gowalla </font>
	 */
	public static void setUserIDs()
	{
		try
		{
			switch (DATABASE_NAME)
			{
			case "geolife1":
				switch (Constant.geolife1howManyUsers)
				{
				case "AllUsers":
					userIDs = DomainConstants.allUserIDsGeolifeData;
					break;
				case "TenUsers":
					userIDs = DomainConstants.tenUserIDsGeolifeData;
					break;
				case "UsersAbove10RTs":
					userIDs = DomainConstants.above10RTsUserIDsGeolifeData;
					break;
				default:
					UtilityBelt
							.showErrorExceptionPopup("unknown Constant.howManyUsers =" + Constant.geolife1howManyUsers);
					break;
				}
				// userIDs = userIDsGeolifeData;
				break;
			case "dcu_data_2":
				userIDs = DomainConstants.userIDsDCUData;
				break;
			case "gowalla1":
				userIDs = DomainConstants.gowallaUserIDs;
				break;
			default:
				System.out.println(DATABASE_NAME.equals("dcu_data_2"));
				System.err.println("Error in setUserIDs: unrecognised database name:" + DATABASE_NAME);
				throw new Exception();
				// break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param rawUserID
	 * @return
	 */
	public static int getIndexOfUserID(int rawUserID)
	{
		for (int i = 0; i < userIDs.length; i++)
		{
			if (userIDs[i] == rawUserID)
			{
				return i;
			}
		}
		return 99;
	}

	public static void setInvalidNames()
	{
		try
		{
			switch (DATABASE_NAME)
			{
			case "geolife1":
				INVALID_ACTIVITY1 = "Unknown";
				INVALID_ACTIVITY2 = "Not Available";
				break;
			case "dcu_data_2":
				INVALID_ACTIVITY1 = "Unknown";
				INVALID_ACTIVITY2 = "Others";
				break;

			case "gowalla1":
				INVALID_ACTIVITY1 = "Unknown";
				INVALID_ACTIVITY2 = "Not Available";
				break;

			default:
				System.err.println("Error in setInvalidNames: unrecognised database name:" + DATABASE_NAME);
				throw new Exception();
				// break;
			}
			// Constant.INVALID_ACTIVITY1_ID = DomainConstants.getCatIDForCatName(INVALID_ACTIVITY1);
			// Constant.INVALID_ACTIVITY2_ID = DomainConstants.getCatIDForCatName(INVALID_ACTIVITY2);
			System.out.println("Inside setInvalidNames(): INVALID_ACTIVITY1 = " + INVALID_ACTIVITY1
					+ " INVALID_ACTIVITY2 = " + INVALID_ACTIVITY2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @since 16 Dec 2018
	 */
	public static void setInvalidActIDs()
	{
		Constant.INVALID_ACTIVITY1_ID = DomainConstants.getCatIDForCatName(INVALID_ACTIVITY1);
		Constant.INVALID_ACTIVITY2_ID = DomainConstants.getCatIDForCatName(INVALID_ACTIVITY2);
		System.out.println("Inside setInvalidActIDs(): INVALID_ACTIVITY1 = " + INVALID_ACTIVITY1
				+ " INVALID_ACTIVITY1_ID = " + INVALID_ACTIVITY1_ID + " INVALID_ACTIVITY2 = " + INVALID_ACTIVITY2_ID);

	}

	public static void setActivityNames()
	{
		try
		{
			switch (DATABASE_NAME)
			{
			case "geolife1":
				activityNames = DomainConstants.GeolifeActivityNames;
				break;
			case "dcu_data_2":
				activityNames = DomainConstants.DCUDataActivityNames;
				break;
			case "gowalla1":
				DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
						.deSerializeThis(DatabaseCreatorGowallaQuicker0.categoryHierarchyTreeFileName);
				LinkedHashSet<String> res = UIUtilityBox.getNodesAtGivenDepth(DomainConstants.gowallaWorkingCatLevel,
						rootOfCategoryTree);
				System.out.println(
						"num of nodes at depth " + DomainConstants.gowallaWorkingCatLevel + " are: " + res.size());
				activityNames = res.toArray(new String[res.size()]);

				// StringBuilder sb = new StringBuilder();
				System.out.println("Constant.activityNames=\n"
						+ Arrays.asList(activityNames).stream().collect(Collectors.joining(",")));

				// activityNamesGowallaLabels = (ArrayList<String>) Arrays.asList(activityNames).stream()
				// .map(a -> DomainConstants.catIDNameDictionary.get(a)).collect(Collectors.toList());
				// gowallaActivityNames;
				break;
			default:
				System.err.println("Error: in setActivityNames: unrecognised database name:" + DATABASE_NAME);
				throw new Exception();
				// break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param givenActNames
	 * @since 6 June 2018
	 */
	public static void setActivityNames(String[] givenActNames)
	{
		activityNames = givenActNames;
	}

	public static LinkedHashMap<String, TreeSet<Integer>> getUniquePDValsPerUser()
	{
		return uniquePDValsPerUser;
	}

	public static void setUniquePDValsPerUser(LinkedHashMap<String, TreeSet<Integer>> uniquePDValsPerUser)
	{
		Constant.uniquePDValsPerUser = uniquePDValsPerUser;
	}

	/**
	 * 
	 * @return map of {userID,{unique actIDs for this user, {unique locIDs for this actID for this userID}}}
	 */
	public static TreeMap<String, TreeMap<Integer, LinkedHashSet<Integer>>> getUserIDActIDLocIDsMap()
	{
		return userIDActIDLocIDsMap;
	}

	/**
	 * 
	 * @param userIDActIDLocIDsMap
	 */
	public static void setUserIDActIDLocIDsMap(
			TreeMap<String, TreeMap<Integer, LinkedHashSet<Integer>>> userIDActIDLocIDsMap)
	{
		Constant.userIDActIDLocIDsMap = userIDActIDLocIDsMap;
	}

	public static TreeMap<Integer, TreeSet<Integer>> getUniqueLocationIDsPerActID()
	{
		return uniqueLocationIDsPerActID;
	}

	public static void setUniqueLocationIDsPerActID(TreeMap<Integer, TreeSet<Integer>> uniqueLocationIDsPerActID)
	{
		Constant.uniqueLocationIDsPerActID = uniqueLocationIDsPerActID;
	}

	public static Set<Integer> getUniqueLocIDs()
	{
		return uniqueLocationIDs;
	}

	public static void setUniqueLocIDs(Set<Integer> locIDs)
	{
		try
		{
			switch (DATABASE_NAME)
			{
			// case "geolife1":
			// activityNames = DomainConstants.GeolifeActivityNames;
			// break;
			// case "dcu_data_2":
			// activityNames = DomainConstants.DCUDataActivityNames;
			// break;
			case "gowalla1":
				uniqueLocationIDs = locIDs;
				break;

			case "geolife1":
				uniqueLocationIDs = locIDs;// NOT TESTED, Added Nov 14 2018
				break;
			default:
				PopUps.printTracedErrorMsgWithExit(
						"Error: in setActivityNames: unrecognised database name:" + DATABASE_NAME);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static Set<Integer> getUniqueActivityIDs()
	{
		return uniqueActivityIDs;
	}

	/**
	 * 
	 * @return
	 * @since 27 Dec 2018
	 */
	public static TreeSet<Integer> getUniqueValidActivityIDs()
	{
		Set<Integer> s = uniqueActivityIDs.stream()
				.filter(v -> (v != Constant.INVALID_ACTIVITY1_ID && v != Constant.INVALID_ACTIVITY2_ID))
				.collect(Collectors.toSet());
		return new TreeSet<>(s);
		// return uniqueActivityIDs;
	}

	public static void setUniqueActivityIDs(Set<Integer> activityIDs)
	{
		try
		{
			switch (DATABASE_NAME)
			{
			// case "geolife1":
			// activityNames = DomainConstants.GeolifeActivityNames;
			// break;
			// case "dcu_data_2":
			// activityNames = DomainConstants.DCUDataActivityNames;
			// break;
			case "gowalla1":
				uniqueActivityIDs = (TreeSet<Integer>) activityIDs;
				break;
			case "geolife1":
				uniqueActivityIDs = (TreeSet<Integer>) activityIDs;// ADDED NOV 2018, NOT TESTED
				break;
			case "dcu_data_2":
				uniqueActivityIDs = (TreeSet<Integer>) activityIDs;// ADDED DEC 2018, NOT TESTED
				break;
			default:
				PopUps.printTracedErrorMsgWithExit(
						"Error: in setActivityNames: unrecognised database name:" + DATABASE_NAME);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// public static void setActIdCharCodeMap(Set<Integer> activityIDs)
	// {
	// actIdCharCodeMap = new Int2CharOpenHashMap(activityIDs.size());
	// try
	// {
	// if (activityIDs != null)
	// {
	// for (int actID : activityIDs)
	// {
	// actIdCharCodeMap.put(actID, StringCode.getC)
	// }
	// }
	// else
	// {
	// PopUps.printTracedErrorMsgWithExit(
	// "Error: in setActivityNames: unrecognised database name:" + DATABASE_NAME);
	// }
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	public static int getNumberOfFeatures()
	{

		switch (DATABASE_NAME)
		{
		case "geolife1":
			return 7;// or 5 //activity name, start time, duration, dist travelled, start geo, end geo, avg altitude
		// break;
		case "dcu_data_2":
			return 3; // activity name, start time, duration
		case "gowalla1":
			return 2; // activity name, start time
		// break;
		default:
			System.err.println("Error: in setActivityNames: unrecognised database name:" + DATABASE_NAME);
			return -1;
		}

	}

	/**
	 * 
	 * @return
	 */
	public static String[] getFeatureNames()
	{
		String databaseName = Constant.getDatabaseName();
		if (databaseName.equals("gowalla1"))
		{
			return Arrays.stream(Enums.GowallaFeature.values()).map(e -> e.toString()).toArray(String[]::new);
		}

		if ((Constant.getDatabaseName().equals("geolife1") || Constant.getDatabaseName().equals("dcu_data_2")
				|| Constant.getDatabaseName().equals("gowalla1")) == false)
		{
			PopUps.showError(
					"Error in getFeaturesName: unreliable for databases other than geolife and dcu, current database name ="
							+ Constant.getDatabaseName());
			new Exception(
					"Error in getFeaturesName: unreliable for databases other than geolife and dcu, current database name ="
							+ Constant.getDatabaseName()).printStackTrace();
		}

		int num = getNumberOfFeatures();
		String featureNamesSub[] = new String[num];

		for (int i = 0; i < num; i++)
		{
			if (Constant.getDatabaseName().equals("gowalla1"))
			{
				featureNamesSub[i] = DomainConstants.gowallaFeatureNames[i];
			}
			else
			{
				featureNamesSub[i] = DomainConstants.featureNames[i];
			}
		}
		return featureNamesSub;// or 5 //activity name, start time, duration, dist travelled, start geo, end geo, avg
								// altitude
	}

	public static String[] getActivityNames()
	{
		if (activityNames.length == 0)
		{
			System.err.println("Error: in Constant.getActvitityNames(): activity names not set");
		}

		return activityNames;
	}

	public static boolean areActivityNamesInCorrectOrder(LinkedHashMap<String, ?> toTest)
	{
		// System.out.println("Inside areActivityNamesInCorrectOrder");
		boolean res = true;

		int i = 0;

		for (Map.Entry<String, ?> entry : toTest.entrySet())
		{
			while ((activityNames[i].equals(INVALID_ACTIVITY1) || activityNames[i].equals(INVALID_ACTIVITY2))
					&& i < (activityNames.length - 1))
			{
				i++;
			}

			// System.out.println("Map entry= " + entry.getKey() + " Constant.activitynames = " + activityNames[i]);
			if (entry.getKey().equals(activityNames[i]) == false)
			{
				// System.out.println("return false");
				return false;
			}
			i += 1;
		}
		// System.out.println("Exiting areActivityNamesInCorrectOrder");
		return res;
	}

	public static int[] getUserIDs()
	{
		if (userIDs != null)
		{
			return userIDs;
		}
		else
		{
			System.err.println("Alert! in Constant.getUserIDs: userIDs is NULL"); // user ids are not set set beforehand
																					// in some case such as for gowalla
																					// dataset
			return null;
		}

	}

	public static String[] getStringUserIDs()
	{
		if (userIDs != null)
		{
			Stream.of(userIDs).map(u -> u.toString()).toArray();
			return null;// userIDs;
		}
		else
		{
			System.err.println("Error in Constant.getUserIDs: userIDs is NULL");
			return null;
		}

	}

	/**
	 * With given number of digits (with added preceeding zeros)
	 * 
	 * @param numOfDigits
	 * @return
	 */
	public static String[] getStringUserIDs(int numOfDigits)
	{
		if (userIDs != null)
		{
			// Stream.of(userIDs).map(u -> addPreceedingZeros(u, numOfDigits)).toArray();
			String[] res = new String[userIDs.length];

			int count = 0;
			for (int userID : userIDs)
			{
				res[count] = addPreceedingZeros(String.valueOf(userID), numOfDigits);
			}

			return res;// userIDs;
		}
		else
		{
			System.err.println("Error in Constant.getUserIDs: userIDs is NULL");
			return null;
		}

	}

	/**
	 * 
	 * @param s
	 * @param len
	 * @return
	 */
	public static String addPreceedingZeros(String s, int len)
	{
		// String r = null;
		int diff = len - s.length();
		String preceeding = new String();
		if (diff > 0)
		{
			for (int j = 1; j <= len; j++)
			{
				preceeding = "0" + preceeding;
			}
		}
		return (preceeding + s);
	}

	public static void setDatabaseName(String dname)
	{
		DATABASE_NAME = dname;
	}

	public static String getDatabaseName()
	{
		return DATABASE_NAME;
	}

	public static void setRankScoring(String rankscoring)
	{
		rankScoring = rankscoring;
	}

	public static String getRankScoring()
	{
		return rankScoring;
	}

	/**
	 * 
	 * @param databaseName
	 * @return
	 */
	public static boolean hasGeoCoordinates(String databaseName)
	{
		if (databaseName.equals("geolife1"))
			return true;
		else
			return false;
	}

	/**
	 * 
	 * @return
	 */
	public static String getAllGlobalConstants()
	{
		StringBuilder s = new StringBuilder();

		s.append("~~~~~~~~~~~~~~~~~~ALL GLOBAL CONSTANTS~~~~~~~~~~~~~~~~~~~\nDatabase used:" + DATABASE_NAME);
		s.append("\ntimeZoneForExperiments:" + timeZoneForExperiments.toString());
		s.append("\nINVALID ACTIVITY 1:" + INVALID_ACTIVITY1);
		s.append("\nINVALID ACTIVITY 2:" + INVALID_ACTIVITY2);
		s.append("\nINVALID_ACTIVITY1_ID:" + INVALID_ACTIVITY1_ID);
		s.append("\nINVALID_ACTIVITY2_ID:" + INVALID_ACTIVITY2_ID);
		s.append("\nCommon path:" + commonPath);
		s.append("\npercentageInTraining:" + percentageInTraining);
		s.append("\nactivityNames:" + Arrays.toString(activityNames));
		s.append("\nPrimaryDimension:" + primaryDimension);
		s.append("\ntypeOfExperiment:" + typeOfExperiment);
		s.append("\nlookPastType:" + lookPastType);
		s.append("\nactLevelDistType:" + actLevelDistType);
		s.append("\nuseForeignAwareLevenshtein:" + useForeignAwareLevenshtein);
		s.append("\nuseJan7DistanceComputations:" + useJan7DistanceComputations);
		s.append("\nconsiderFeatureValOrValDiff:" + considerFeatureValOrValDiff);
		s.append("\nensureCandEndWithCurrAct:" + ensureCandEndWithCurrAct);
		s.append("\nensureHasDaywiseCandsForEvalCompatibility:" + ensureHasDaywiseCandsForEvalCompatibility);
		s.append("\naltSeqPredictor:" + altSeqPredictor);
		s.append("\nAKOMHighestOrder:" + AKOMHighestOrder);
		s.append("\nRNNCurrentActivitityLength:" + RNNCurrentActivitityLength);
		s.append("\nsameAKOMForAllRTsOfAUser:" + sameAKOMForAllRTsOfAUser);
		s.append("\nsameRNNForAllRTsOfAUser:" + sameRNNForAllRTsOfAUser);
		s.append("\nsameRNNForALLUsers:" + sameRNNForALLUsers);

		s.append("\nDaywiseAllowSpillOverDaysOfCurr:" + DaywiseAllowSpillOverDaysOfCurr);
		s.append("\nClosestTimeAllowSpillOverDays:" + ClosestTimeAllowSpillOverDays);
		s.append("\nClosestTimeFilterCandidates:" + ClosestTimeFilterCandidates);
		s.append("\ncaseType:" + caseType);
		s.append("\nrankScoring: " + rankScoring);
		s.append("\nALPHA:" + ALPHA);
		s.append("\nWeights of features: "
				+ (new AlignmentBasedDistance(Constant.primaryDimension)).getAllWeightsOfFeaturesForPrint());
		s.append("\ndistanceUsed:" + dynamicDistanceUsed);

		s.append("\nuseTolerance:" + useTolerance);

		s.append("\ntypeOfThresholds:" + Arrays.asList(typeOfiiWASThresholds));
		s.append("\nuseThreshold:" + useiiWASThreshold);
		s.append("\nbreakTiesWithShuffle:" + breakTiesWithShuffle);
		s.append("\nEXPUNGE_INVALIDS_B4_RECOMM_PROCESS:" + EXPUNGE_INVALIDS_B4_RECOMM_PROCESS);
		s.append("\nBLACKLISTING:" + BLACKLISTING);
		s.append("\nblacklistingUsersWithLargeMaxActsPerDay:" + blacklistingUsersWithLargeMaxActsPerDay);

		s.append("\nremoveCurrentActivityNameFromRecommendations:" + removeCurrentActivityNameFromRecommendations);
		s.append("\nhasInvalidActivityNames:" + hasInvalidActivityNames);

		s.append("\neditDistTimeDistType:" + editDistTimeDistType);

		s.append("\nRoundingPrecision:" + RoundingPrecision);
		s.append("\nEvalPrecisionRecallFMeasure:" + EvalPrecisionRecallFMeasure);
		s.append("\ndecimalPlacesInGeocordinatesForComputations:" + decimalPlacesInGeocordinatesForComputations);

		s.append("\nDoBaselineDuration:" + DoBaselineDuration);
		s.append("\nDoBaselineOccurrence:" + DoBaselineOccurrence);
		s.append("\nDoBaselineNGramSeq:" + DoBaselineNGramSeq);

		s.append("\ncheckIfTimelineCreatedIsChronological:" + SanityConstants.checkIfTimelineCreatedIsChronological);
		s.append("\ncheckArrayOfFeatures:" + SanityConstants.checkArrayOfFeatures);
		s.append("\ncheckForHaversineAnomaly:" + SanityConstants.checkForHaversineAnomaly);
		s.append("\ncheckForDistanceTravelledAnomaly:" + SanityConstants.checkForDistanceTravelledAnomaly);

		s.append("\nuseJarForMySimpleLevenshteinDistance:" + useJarForMySimpleLevenshteinDistance);
		// useJarForMySimpleLevenshteinDistance

		s.append("\nuseHierarchicalDistance:" + useHierarchicalDistance);
		s.append("\nHierarchicalLevelForEditDistance:" + HierarchicalCatIDLevelForEditDistance);
		s.append("\nbuildRepAOJustInTime:" + buildRepAOJustInTime);
		s.append("\npreBuildRepAOGenericUser:" + preBuildRepAOGenericUser);

		//
		s.append("\ncollaborativeCandidates:" + collaborativeCandidates);
		s.append("\nonly1CandFromEachCollUser:" + only1CandFromEachCollUser);
		s.append("\nnumOfCandsFromEachCollUser:" + numOfCandsFromEachCollUser);

		s.append("\nfilterTrainingTimelinesByRecentDays:" + filterTrainingTimelinesByRecentDays);
		s.append("\nrecentDaysInTrainingTimelines:" + recentDaysInTrainingTimelines);

		s.append("\nonlyPastFromRecommDateInCandInColl:" + onlyPastFromRecommDateInCandInColl);
		s.append("\ntypeOfCandThresholdPrimDim:" + typeOfCandThresholdPrimDim);
		s.append("\ntypeOfCandThresholdSecDim:" + typeOfCandThresholdSecDim);
		s.append("\nfilterCandByCurActTimeThreshInSecs:" + filterCandByCurActTimeThreshInSecs);

		s.append("\npercentileCandEDThreshold:" + percentileCandEDThreshold);
		s.append("\nnearestNeighbourCandEDThresholdPrimDim:" + nearestNeighbourCandEDThresholdPrimDim);
		s.append("\nnearestNeighbourCandEDThresholdSecDim:" + nearestNeighbourCandEDThresholdSecDim);

		s.append("\ncandEDValThresholdPrimDim:" + candEDValThresholdPrimDim);
		s.append("\ncandEDValThresholdSecDim:" + candEDValThresholdSecDim);

		s.append("\neditDistancesMemorizerBufferSize:" + editDistancesMemorizerBufferSize);
		s.append("\nmemorizeEditDistance:" + memorizeEditDistance);
		s.append("\nneedsToPruneFirstUnknown:" + needsToPruneFirstUnknown);
		s.append("\ndoMaxNormalisationWhenNormalisationEDsOverCandSet:"
				+ doMaxNormalisationWhenNormalisationEDsOverCandSet);
		s.append("\nFor9kUsers:" + For9kUsers);

		s.append("\nuseMedianCinsForRepesentationAO:" + useMedianCinsForRepesentationAO);
		s.append("\ncheckEDSanity:" + checkEDSanity);
		s.append("\ndynamicEDAlpha:" + dynamicEDAlpha);
		s.append("\nEDAlphas:" + Arrays.asList(EDAlphas));
		s.append("\ndisableRoundingEDCompute:" + disableRoundingEDCompute);
		s.append("\nscoreRecommsByLocProximity:" + scoreRecommsByLocProximity);
		s.append("\nwtScoreRecommsByLocProximity:" + wtScoreRecommsByLocProximity);

		s.append("\nuseActivityNameInFED:" + useActivityNameInFED);
		s.append("\nuseStartTimeInFED:" + useStartTimeInFED);

		// For gowalla1
		s.append("\nuseLocationInFED:" + useLocationInFED);
		s.append("\nuseHaversineDistInLocationFED:" + useHaversineDistInLocationFED);
		s.append("\nusePopularityInFED:" + usePopularityInFED);
		s.append("\nuseDistFromPrevInFED:" + useDistFromPrevInFED);
		s.append("\nuseDurationFromPrevInFED:" + useDurationFromPrevInFED);

		// for geolife1
		s.append("\nuseDurationInFED:" + useDurationInFED);
		s.append("\nuseDistTravelledInFED:" + useDistTravelledInFED);
		s.append("\nuseStartGeoInFED:" + useStartGeoInFED);
		s.append("\nuseEndGeoInFED:" + useEndGeoInFED);
		s.append("\nuseAvgAltitudeInFED:" + useAvgAltitudeInFED);

		// for dcu_data_2
		// s.append("\nuseEndTimeInFED:" + useEndTimeInFED);

		s.append("\nuseDecayInFeatureLevelED:" + useDecayInFED);
		s.append("\nuseTimeDecayInAED:" + useTimeDecayInAED);
		s.append("\npowerOfTimeDecayInAED:" + powerOfTimeDecayInAED);

		s.append("\nassignFallbackZoneId:" + assignFallbackZoneIdWhenConvertCinsToAO);
		s.append("\nrandomLySample100Users:" + useRandomlySampled100Users);
		s.append("\nuseSelectedGTZeroUsersFromRandomlySampled100Users:"
				+ useSelectedGTZeroUsersFromRandomlySampled100Users);
		s.append("\ndynamicPathToRandomlySampledUserIndices:" + dynamicPathToRandomlySampledUserIndices);
		s.append("\nuseCheckinEntryV2:" + useCheckinEntryV2);
		s.append("\nrunForAllUsersAtOnce:" + runForAllUsersAtOnce);
		s.append("\nreduceAndCleanTimelinesBeforeRecomm:" + reduceAndCleanTimelinesBeforeRecomm);
		s.append("\nuseFeatureDistancesOfAllActs:" + useFeatureDistancesOfAllActs);
		s.append("\ncleanTimelinesAgainInsideRecommendationTests:" + cleanTimelinesAgainInsideRecommendationTests);
		s.append("\ncleanTimelinesAgainInsideTrainTestSplit:" + cleanTimelinesAgainInsideTrainTestSplit);
		s.append("\nuseRTVerseNormalisationForED:" + useRTVerseNormalisationForED);
		s.append("\ncomputeFEDForEachAOInRTVerse:" + computeFEDForEachAOInRTVerse);
		s.append("\ncomputeFEDForEachFeatureSeqInRTVerse:" + computeFEDForEachFeatureSeqInRTVerse);
		s.append("\ntakeMeanOrMedianOfFeatDiffsAcrossAllAOsInCandForFeatSeq:"
				+ takeMeanOrMedianOfFeatDiffsAcrossAllAOsInCandForFeatSeq);
		s.append("\nuseMSDInFEDInRTVerse:" + useMSDInFEDInRTVerse);
		s.append("\nuseLog2InFEDInRTVerse:" + useLog2InFEDInRTVerse);
		s.append("\npercentileForRTVerseMaxForFEDNorm:" + percentileForRTVerseMaxForFEDNorm);
		s.append("\nfixedValPerFeatForRTVerseMaxMinForFEDNorm:" + fixedValPerFeatForRTVerseMaxMinForFEDNorm);
		s.append("\npercentileForRTVerseMaxForAEDNorm:" + percentileForRTVerseMaxForAEDNorm);
		s.append("\nthreshNormAEDForCand:" + threshNormAEDForCand);
		s.append("\nthreshNormFEDForCand:" + threshNormFEDForCand);
		s.append("\nnormaliseCandDistsAgainAfterFiltering:" + normaliseCandDistsAgainAfterFiltering);
		s.append("\nuseToyTimelines:" + useToyTimelines);
		// s.append("\nnumOfHiddenLayersInRNN1:" + numOfHiddenLayersInRNN1);
		// s.append("\nnumOfNeuronsInEachHiddenLayerInRNN1:" + numOfNeuronsInEachHiddenLayerInRNN1);
		s.append("\nnumOfTrainingEpochsInRNN1:" + Constant.numOfTrainingEpochsInRNN1);
		s.append("\nlearningRateInRNN1:" + Constant.learningRateInRNN1);
		s.append("\nl2RegularisationCoeffRNN1:" + Constant.l2RegularisationCoeffRNN1);

		s.append("\nexampleLengthInRNN1:" + Constant.exampleLengthInRNN1);
		s.append("\nminiBatchSizeInRNN1:" + Constant.miniBatchSizeInRNN1);

		// s.append("\nvarWidthPerHiddenLayerRNN1:" + Constant.varWidthPerHiddenLayerRNN1);
		s.append("\nneuronsInHiddenLayersRNN1:" + Arrays.toString(Constant.neuronsInHiddenLayersRNN1));

		s.append("\ndoVisualizationRNN1:" + Constant.doVisualizationRNN1);
		s.append("\nmapLocIDToGridID:" + Constant.mapLocIDToGridID);
		s.append("\ndoSecondaryDimension:" + Constant.doSecondaryDimension);
		s.append("\nsecondaryDimension:" + Constant.secondaryDimension);
		s.append("\ndoWeightedEditDistanceForSecDim:" + Constant.doWeightedEditDistanceForSecDim);
		s.append("\ndistToSimScoring:" + Constant.maxDistanceThresholdForLocGridDissmilarity);
		s.append("\nsearchForOptimalFeatureWts:" + Constant.distToSimScoring);
		s.append("\nsearchForOptimalFeatureWts:" + Constant.searchForOptimalFeatureWts);
		s.append("\npurelyRandomPredictionNov25:" + Constant.purelyRandomPredictionNov25);
		s.append("\ndoForJupyterBaselines:" + Constant.doForJupyterBaselines);
		s.append("\nbuildRepAOInfoPerUserForMemoryPerformance:" + Constant.buildRepAOInfoPerUserForMemoryPerformance);
		s.append("\nuseBetterRepAOForLoc:" + Constant.useBetterRepAOForLoc);
		// s.append("\n:" + );
		if (dynamicDistanceUsed.equals("FeatureWiseEditDistance"))
		{
			s.append("\nConsider all features for feature wise edit distance:"
					+ considerAllFeaturesForFeatureWiseEditDistance);

			if (considerAllFeaturesForFeatureWiseEditDistance == false)
			{
				s.append("\nconsiderActivityNameInFeatureWiseEditDistance:"
						+ considerActivityNameInFeatureWiseEditDistance);
				s.append("\nconsiderStartTimeInFeatureWiseEditDistance:" + considerStartTimeInFeatureWiseEditDistance);
				s.append("\nconsiderDurationInFeatureWiseEditDistance:" + considerDurationInFeatureWiseEditDistance);
				s.append("\nconsiderDistanceTravelledInFeatureWiseEditDistance:"
						+ considerDistanceTravelledInFeatureWiseEditDistance);
				s.append("\nconsiderStartGeoCordinatesInFeatureWiseEditDistance:"
						+ considerStartGeoCoordinatesInFeatureWiseEditDistance);
				s.append("\nconsiderEndGeoCordinatesInFeatureWiseEditDistance:"
						+ considerEndGeoCoordinatesInFeatureWiseEditDistance);
				s.append("\nconsiderAvgAltitudeInFeatureWiseEditDistance:"
						+ considerAvgAltitudeInFeatureWiseEditDistance);
			}
		}
		s.append("\nepsilonForFloatZero:" + epsilonForFloatZero);
		s.append("\nepsilonForFloatZeroRelaxed:" + epsilonForFloatZeroRelaxed);
		s.append("\nlengthOfRecommendedSequence:" + lengthOfRecommendedSequence);
		s.append("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		return s.toString();

	}

	/**
	 * Checks if all core parameters (database name, , case type and alpha) are set
	 * 
	 * @return
	 */
	public static boolean checkAllParametersSet()
	{
		boolean allSet = true;
		if (DATABASE_NAME.length() == 0)
		{
			System.err.println("Database name is empty");
			allSet = false;
		}
		// if (rankScoring.length() == 0)
		// {
		// System.err.println("rankScoring name is empty");
		// allSet = false;
		// }
		if (caseType == null)// .length() == 0)
		{
			System.err.println("caseType name is null");
			allSet = false;
		}
		if (ALPHA < 0 && rankScoring.equals("sum"))
		{
			System.err.println("ALPHA  is less than 0 (not set) for Sum-based Rank Scoring");
			allSet = false;
		}
		// if (commonPath == null)// .length() == 0)
		// {
		// System.err.println("commonPath name is null");
		// allSet = false;
		// }
		return allSet;
	}

	/**
	 * @param catIDNameDictionary
	 */
	public static void setActIDCharCodeMap(Map<Integer, String> catIDNameDictionary)
	{
		try
		{
			if (catIDNameDictionary == null)
			{
				PopUps.getTracedErrorMsg("catIDNameDictionary is null");
				System.exit(-2);
			}

			int size = catIDNameDictionary.size();
			actIDCharCodeMap = new Int2CharOpenHashMap(size);// HashBiMap.create(catIDNameDictionary.size());
			charCodeActIDMap = new Char2IntOpenHashMap(size);
			// new HashBiMap<Integer, Character>();
			for (Integer actID : catIDNameDictionary.keySet())
			{
				char charCode = StringCode.getCharCodeFromActivityID(actID);
				int actId = actID.intValue();
				actIDCharCodeMap.put(actId, charCode);
				charCodeActIDMap.put(charCode, actId);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static Int2CharOpenHashMap getActIDCharCodeMap()
	{
		return actIDCharCodeMap;
	}

	public static Char2IntOpenHashMap getCharCodeActIDMap()
	{
		return charCodeActIDMap;
	}

	/**
	 * Read the paramter setting in the Constant.java source file and write it to the given path.
	 * 
	 * @param absCOnstantConfigFileToWrite
	 */
	public static void reflectTheConfigInConstantFile(String absCOnstantConfigFileToWrite)
	{
		String absFileNameToRead = "./src/main/java/org/activity/constants/Constant.java";
		StringBuilder sb = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new FileReader(absFileNameToRead)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				if (false)
				{
					if (line.contains("/ End of variable declarations /"))
					{
						break;
					}
				}
				sb.append(line + "\n");
			}
			WToFile.writeToNewFile(sb.toString(), absCOnstantConfigFileToWrite);
			System.out.println("reflected the config in Constant.java\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}

// commented out below this

/// **
// * Set the type of timeline matching. The currently supported types of timeline matching are: Daywise, N-count and
// * N-hours
// *
// * @param name
// */
// public static void setTypeOfTimelineMatching(String name)
// {
// name = name.trim();
//
// if (name != "Daywise" && name != "N-count" && name != "N-hours")
// {
// String msg = "Error in org.activity.util.Constant.setTypeOfTimelineMatching(String): unknown type of time line
// matching:"
// + name;
// PopUps.showError(msg);
// System.err.println(msg);
//
// }
// else
// {
// typeOfTimelineMatching = name;
// }
// }

// /**
// * Returns the type of timeline matching
// *
// * @return
// */
// public static String getTypeOfTimelineMatching()
// {
// return typeOfTimelineMatching;
// }

/// **
// * note: all variables are static for this class
// *
// * @param givenCommonpath
// */
// public Constant(String givenCommonpath, String databaseName)
// {
// Constant.setDatabaseName(databaseName);
// Constant.setUserIDs();
// Constant.setInvalidNames();
// Constant.setActivityNames();
// Constant.setCommonPath(givenCommonpath);
// // Constant.setDistanceUsed("HJEditDistance");
// }
//
// public Constant(String givenCommonpath, String databaseName, String distUsed)
// {
// Constant.setDatabaseName(databaseName);
// Constant.setUserIDs();
// Constant.setInvalidNames();
// Constant.setActivityNames();
// Constant.setCommonPath(givenCommonpath);
// // Constant.setDistanceUsed(distUsed);
// }
