package org.activity.recomm;

//import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.Enums.TypeOfCandThreshold;
import org.activity.constants.Enums.TypeOfThreshold;
import org.activity.constants.VerbosityConstants;
import org.activity.distances.AlignmentBasedDistance;
import org.activity.distances.DistanceUtils;
import org.activity.distances.FeatureWiseEditDistance;
import org.activity.distances.FeatureWiseWeightedEditDistance;
import org.activity.distances.HJEditDistance;
import org.activity.distances.OTMDSAMEditDistance;
import org.activity.evaluation.Evaluation;
import org.activity.io.EditDistanceMemorizer;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.objects.Triple;
import org.activity.sanityChecks.Sanity;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.StringUtils;
import org.activity.util.TimelineExtractors;
import org.activity.util.TimelineTrimmers;
import org.activity.util.UtilityBelt;

/**
 * Used as of Nov 29 2017
 * 
 * <p>
 * Fork of org.activity.recomm.RecommendationMasterMar2017GenSeqNov2017 for multi dimensional approach
 * <p>
 * ALERT!: DO NOT USER THRESHOLDING WITH THIS CLASS since here the edit distances are normalised before thredholding,
 * however, they should have been normalised after thresholding as was the case in iiWAS version of the code This
 * generates recommendations using the matching-unit (MU) approach. Timelines are not viewed as Day Timeline but rather
 * as continuous timelines. The matching-unit can be specified as 'hours' or 'counts'.
 * 
 * Timelines are sets of Activity Objects, (and they do not necessarily belong to one day.) (the matching unit used here
 * HJ version) In this version we calculate edit distance of the full candidate and not the least distant subcandidate
 * 
 * @since 13 July 2018
 * @author gunjan
 */
public class RecommendationMasterMar2017GenSeqMultiDJul2018 implements RecommendationMasterMultiDI// IRecommenderMaster
{
	private double matchingUnitInCountsOrHours;
	private double reductionInMatchingUnit = 0;

	private PrimaryDimension primaryDimension, secondaryDimension;

	// private Timeline trainingTimeline;
	// private Timeline testTimeline;
	// in case of MU approach {String, TimelineWithNext}, in case if daywise approach {Date as String, Timeline}
	private LinkedHashMap<String, Timeline> candidateTimelinesPrimDim;
	// here key is the TimelineID, which is already a class variable of Value,
	// So we could have used ArrayList but we used LinkedHashMap purely for search performance reasons

	// in case of MU approach {TimelineIDString, TimelineWithNext}, if daywise approach {Date as String, Timeline}
	// candidate timelines for secondary dimension, e.g. location grid
	private LinkedHashMap<String, Timeline> candidateTimelinesSecDim;

	private Date dateAtRecomm;
	private Time timeAtRecomm;
	private String userAtRecomm;
	private String userIDAtRecomm;
	private LinkedHashMap<String, String> candUserIDsPrimaryDim;
	/**
	 * Current Timeline sequence of activity objects happening from the recomm point back until the matching unit
	 */
	private TimelineWithNext currentTimeline; // =current timelines
	private ArrayList<ActivityObject2018> activitiesGuidingRecomm; // Current Timeline ,
	private ActivityObject2018 activityObjectAtRecommPoint; // current Activity Object
	// private String activityNameAtRecommPoint;// current Activity Name
	private ArrayList<Integer> primaryDimensionValAtRecommPoint;// when activity is primary dimension, this is an
																// activity id, when
	// location is the primary dimension, this is a list of location ids
	// (>1 if this is a merged object)
	/**
	 * {Cand TimelineID, Pair{Trace,Edit distance}} this LinkedHashMap is sorted by the value of edit distance in
	 * ascending order://formerly: editDistancesSortedMapFullCand
	 * <p>
	 * 
	 */
	private LinkedHashMap<String, Pair<String, Double>> primaryDimDistancesSortedMap;
	private LinkedHashMap<String, Pair<String, Double>> secondaryDimDistancesSortedMap;

	/**
	 * Relevant for daywise approach: the edit distance is computed for least distance subsequence in case current
	 * activity name occurs at multiple times in a day timeline {Cand TimelineID, End point index of least distant
	 * subsequence}
	 */
	private LinkedHashMap<String, Integer> endPointIndicesConsideredInPDCands;
	private LinkedHashMap<String, Integer> endPointIndicesConsideredInSDCands;

	// private LinkedHashMap<String, ActivityObject> endPointActivityObjectsInCands;

	/*
	 * List of of top next activity objects with their edit distances and the timeline id of the candidate producing
	 * them. Triple <Next Activity Object,edit distance, TimelineID> formerly ArrayList<Triple<ActivityObject, Double,
	 * String>> nextActivityObjectsFromCands;// topNextActivityObjects
	 */
	/**
	 * List of of top next activity objects with their edit distances and the timeline id of the candidate producing
	 * them. {TimelineID, {Next Activity Object,edit distance}}
	 */
	private LinkedHashMap<String, Pair<ActivityObject2018, Double>> nextActivityObjectsFromPrimaryCands;

	private LinkedHashMap<String, Pair<ActivityObject2018, Double>> nextActivityObjectsFromSecondaryCands;

	/**
	 * This is only relevant when case type is 'CaseBasedV1' (Cand TimeineId, Edit distance of the end point activity
	 * object of this candidate timeline with the current activity object(activity at recomm point))
	 * <p>
	 * <font color = orange>currently its a similarity and not edit distance</font>
	 */
	private LinkedHashMap<String, Double> similarityOfEndPointActivityObjectCand;

	/**
	 * Recommended Activity names with their rank score
	 */
	private LinkedHashMap<String, Double> recommendedActivityNamesWithRankscores;
	private String rankedRecommendedActNamesWithRankScoresStr;
	private String rankedRecommendedActNamesWithoutRankScoresStr;

	/**
	 * Recommended Secondary dimension vals names with their rank score
	 * <p>
	 * added on 18 July 2018
	 */
	private LinkedHashMap<String, Double> recommendedSecondaryDimValsWithRankscores;
	private String rankedRecommendedSecDimValsWithRankScoresStr;
	private String rankedRecommendedSecDimValsWithoutRankScoresStr;

	private boolean hasCandidateTimelines, hasPrimaryDimCandTimelinesBelowThreshold;

	private boolean nextActivityJustAfterRecommPointIsInvalid;
	private double thresholdAsDistance = Double.MAX_VALUE;

	// private int fullCandOrSubCand; //0 means full cand .... 1 means subcand
	private boolean thresholdPruningNoEffect;

	public Enums.LookPastType lookPastType;// String
	// public int totalNumberOfProbableCands; // public int numCandsRejectedDueToNoCurrentActivityAtNonLast;
	// public int numCandsRejectedDueToNoNextActivity;
	// candidateTimelinesStatus; //1 for has candidate timelines, -1 for no candidate timelines because no past timeline
	// with current act, -2 for no candodate timelines because

	Enums.CaseType caseType; // String 'SimpleV3' for no case case, 'CaseBasedV1' for first cased based implementation

	// public static String commonPath ;//= Constant.commonPath;

	private boolean errorExists;

	private ArrayList<Double> normPDEditSimilarity, simEndActivityObjForCorr;
	// public double percentageDistanceThresh;
	/*
	 * threshold for choosing candidate timelines, those candidate timelines whose distance from the 'activity objects
	 * guiding recommendations' is higher than the cost of replacing 'percentageDistanceThresh' % of Activity Objects in
	 * the activities guiding recommendation are pruned out from set of candidate timelines
	 */

	HJEditDistance hjEditDistancePrimaryDim = null;
	HJEditDistance hjEditDistanceSecondaryDim = null;
	AlignmentBasedDistance alignmentBasedDistance = null;
	FeatureWiseEditDistance featureWiseEditDistance = null;
	FeatureWiseWeightedEditDistance featureWiseWeightedEditDistance = null;
	OTMDSAMEditDistance OTMDSAMEditDistance = null;

	/**
	 * Score (A<sub>O</sub>) = ∑ { 1- min( 1, |Stcand - RT|/60mins) }
	 **/
	protected static final double timeInSecsForRankScoreNormalisation = 60 * 60; // 60 mins

	/**
	 * NOT BEING USED AT THE MOMENT
	 */
	EditDistanceMemorizer editDistancesMemorizer;

	/**
	 * 
	 * @param dname
	 * @param primaryDimension
	 * @param dynamicEDAlpha
	 * @param secondaryDimension
	 * @return
	 */
	private final int initialiseDistancesUsed(String dname, PrimaryDimension primaryDimension, double dynamicEDAlpha,
			PrimaryDimension secondaryDimension)
	{
		alignmentBasedDistance = new AlignmentBasedDistance(primaryDimension); // used for case based similarity
		//
		switch (dname)
		{
		case "HJEditDistance":
			if (dynamicEDAlpha < 0)
			{
				hjEditDistancePrimaryDim = new HJEditDistance(primaryDimension);
				hjEditDistanceSecondaryDim = new HJEditDistance(secondaryDimension);
			}
			else
			{
				hjEditDistancePrimaryDim = new HJEditDistance(dynamicEDAlpha, primaryDimension);
				hjEditDistanceSecondaryDim = new HJEditDistance(dynamicEDAlpha, secondaryDimension);
			}

			break;

		case "FeatureWiseEditDistance":
			featureWiseEditDistance = new FeatureWiseEditDistance(primaryDimension);
			break;

		case "FeatureWiseWeightedEditDistance":
			featureWiseWeightedEditDistance = new FeatureWiseWeightedEditDistance(primaryDimension);
			break;

		case "OTMDSAMEditDistance":
			OTMDSAMEditDistance = new OTMDSAMEditDistance(primaryDimension);
			break;

		default:
			PopUps.showError(
					"Error in org.activity.recomm.RecommendationMasterMU.initialiseDistanceUsed(): Unknown distance specified:"
							+ dname);
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in org.activity.recomm.RecommendationMasterMU.initialiseDistanceUsed(): Unknown distance specified:"
							+ dname));
			System.exit(-1);
		}
		return 0;
	}

	public LinkedHashMap<String, String> getCandUserIDs()
	{
		return candUserIDsPrimaryDim;
	}

	/**
	 * Recommendation for a particular RT
	 * 
	 * @param trainingTimelines
	 * @param testTimelines
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 *            start time of the current activity, equivalent to using end time of the current activity
	 * @param userAtRecomm
	 *            user for which recommendation is being generated
	 * @param thresholdVal
	 * @param typeOfThreshold
	 * @param matchingUnitInCountsOrHours
	 * @param caseType
	 * @param lookPastType
	 * @param dummy
	 * @param actObjsToAddToCurrentTimeline
	 * @param trainTestTimelinesForAllUsers
	 *            //added on 26th July 2017
	 * @param trainTimelinesAllUsersContinuous
	 *            to improve performance by avoiding repetitive conversion from daywise to continous timelines
	 */
	public RecommendationMasterMar2017GenSeqMultiDJul2018(LinkedHashMap<Date, Timeline> trainingTimelines,
			LinkedHashMap<Date, Timeline> testTimelines, String dateAtRecomm, String timeAtRecomm, int userAtRecomm,
			double thresholdVal, Enums.TypeOfThreshold typeOfThreshold, double matchingUnitInCountsOrHours,
			Enums.CaseType caseType, Enums.LookPastType lookPastType, boolean dummy,
			ArrayList<ActivityObject2018> actObjsToAddToCurrentTimeline,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous)
	{
		// PopUps.showMessage("called RecommendationMasterMar2017GenSeq");
		try
		{
			System.out.println("\n-----------Starting RecommendationMasterMar2017GenSeqMultiDJul2018 " + lookPastType
					+ "-------------");

			String performanceFileName = Constant.getCommonPath() + "Performance.csv";
			long recommMasterT0 = System.currentTimeMillis();

			this.primaryDimension = Constant.primaryDimension;
			this.secondaryDimension = Constant.secondaryDimension;

			initialiseDistancesUsed(Constant.getDistanceUsed(), primaryDimension, Constant.getDynamicEDAlpha(),
					secondaryDimension);
			System.out.println("hjEditDistancePrimaryDim.toString=" + this.hjEditDistancePrimaryDim.toString());
			System.out.println("hjEditDistanceSecondaryDim.toString=" + this.hjEditDistanceSecondaryDim.toString());

			editDistancesMemorizer = new EditDistanceMemorizer();
			this.lookPastType = lookPastType;
			this.caseType = caseType;

			// Changed but effectively same on 21 Nov 2017 start
			if (lookPastType.equals(LookPastType.NCount) || lookPastType.equals(LookPastType.NHours))
			{
				this.matchingUnitInCountsOrHours = matchingUnitInCountsOrHours;
			}
			else if (lookPastType.equals(LookPastType.Daywise) || lookPastType.equals(LookPastType.ClosestTime)
					|| lookPastType.equals(LookPastType.NGram))
			{
				this.matchingUnitInCountsOrHours = 0;
			}
			// Changed but effectively same on 21 Nov 2017 end

			errorExists = false;

			this.hasCandidateTimelines = true;
			this.nextActivityJustAfterRecommPointIsInvalid = false;

			// dd/mm/yyyy // okay java.sql.Date with no hidden time
			this.dateAtRecomm = DateTimeUtils.getDateFromDDMMYYYY(dateAtRecomm, RegexUtils.patternForwardSlash);
			this.timeAtRecomm = Time.valueOf(timeAtRecomm);
			this.userAtRecomm = Integer.toString(userAtRecomm);
			this.userIDAtRecomm = Integer.toString(userAtRecomm);
			System.out.println("	User at Recomm = " + this.userAtRecomm + "\tDate at Recomm = " + this.dateAtRecomm
					+ "\tTime at Recomm = " + this.timeAtRecomm + "\tthis.matchingUnitInCountsOrHours="
					+ this.matchingUnitInCountsOrHours);

			//////
			Pair<TimelineWithNext, Double> extCurrTimelineRes = null;

			// extracted current timeline resultant (after addition of prev recomm AOs)
			if (actObjsToAddToCurrentTimeline.size() > 0)
			{
				extCurrTimelineRes = TimelineExtractors.extractCurrentTimelineSeq(testTimelines, lookPastType,
						this.dateAtRecomm, this.timeAtRecomm, this.userIDAtRecomm, this.matchingUnitInCountsOrHours,
						actObjsToAddToCurrentTimeline);
			}
			else
			{
				extCurrTimelineRes = TimelineExtractors.extractCurrentTimeline(testTimelines, lookPastType,
						this.dateAtRecomm, this.timeAtRecomm, this.userIDAtRecomm, this.matchingUnitInCountsOrHours);
			}

			this.currentTimeline = extCurrTimelineRes.getFirst();
			this.reductionInMatchingUnit = extCurrTimelineRes.getSecond();
			//////

			this.activitiesGuidingRecomm = currentTimeline.getActivityObjectsInTimeline(); // CURRENT TIMELINE

			// Sanity Check start
			if (actObjsToAddToCurrentTimeline.size() > 0 && this.lookPastType == LookPastType.NCount)
			{
				if (reductionInMatchingUnit == 0 && activitiesGuidingRecomm.size() <= matchingUnitInCountsOrHours)
				{
					System.err.println("Error: actsGuidingRecomm.size():" + this.activitiesGuidingRecomm.size() + "<=MU"
							+ matchingUnitInCountsOrHours + " (even without reduced mu");
				}
				else if (activitiesGuidingRecomm.size() <= matchingUnitInCountsOrHours)
				{
					System.out.println("Warning: actsGuidingRecomm.size():" + this.activitiesGuidingRecomm.size()
							+ "<=MU" + matchingUnitInCountsOrHours + " (with mu reduction =" + reductionInMatchingUnit
							+ ")");
				}
			} // Sanity Check end

			this.activityObjectAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1);
			// this.activityNameAtRecommPoint = this.activityAtRecommPoint.getActivityName();
			this.primaryDimensionValAtRecommPoint = this.activityObjectAtRecommPoint.getPrimaryDimensionVal();

			// sanity check start
			if (actObjsToAddToCurrentTimeline.size() > 0)
			{
				if (primaryDimensionValAtRecommPoint.equals(actObjsToAddToCurrentTimeline
						.get(actObjsToAddToCurrentTimeline.size() - 1).getPrimaryDimensionVal()) == false)
				{
					System.err.println(
							"Error primary dimension vals of actAtRecommPoint and last act in acts to add do not match: primaryDimensionValAtRecommPoint= "
									+ primaryDimensionValAtRecommPoint + " last act in acts to add = "
									+ actObjsToAddToCurrentTimeline.get(actObjsToAddToCurrentTimeline.size() - 1)
											.getPrimaryDimensionVal());
				}
			}
			// sanity check end
			// //////////////////////////

			long recommMasterT1 = System.currentTimeMillis();
			System.out.println("Inside recomm master :trainTestTimelinesForAllUsers.size()= "
					+ trainTestTimelinesForAllUsers.size() + " trainTimelinesAllUsersContinuous.size()="
					+ trainTimelinesAllUsersContinuous.size());

			this.candidateTimelinesPrimDim = TimelineExtractors.extractCandidateTimelinesV2(trainingTimelines,
					lookPastType, this.dateAtRecomm, /* this.timeAtRecomm, */ this.userIDAtRecomm,
					matchingUnitInCountsOrHours, this.activityObjectAtRecommPoint, trainTestTimelinesForAllUsers,
					trainTimelinesAllUsersContinuous, this.primaryDimension);

			// start of added on 16 July 2018
			this.candidateTimelinesSecDim = TimelineExtractors.extractCandidateTimelinesV2(trainingTimelines,
					lookPastType, this.dateAtRecomm, /* this.timeAtRecomm, */ this.userIDAtRecomm,
					matchingUnitInCountsOrHours, this.activityObjectAtRecommPoint, trainTestTimelinesForAllUsers,
					trainTimelinesAllUsersContinuous, this.secondaryDimension);
			// end of added on 16 July 2018

			// NOTE: filterCandByCurActTimeThreshInSecs ONLY DONE FOR PRIMARY DIMENSION
			// Start of added on Feb 12 2018
			if (Constant.filterCandByCurActTimeThreshInSecs > 0)
			{
				System.out.println(
						"filtering CandByCurActTimeThreshInSecs= " + Constant.filterCandByCurActTimeThreshInSecs);
				this.candidateTimelinesPrimDim = removeCandsWithEndCurrActBeyondThresh(this.candidateTimelinesPrimDim,
						Constant.filterCandByCurActTimeThreshInSecs, activityObjectAtRecommPoint,
						VerbosityConstants.verboseCandFilter);
			}
			// End of added on Feb 12 2018

			long recommMasterT2 = System.currentTimeMillis();
			long timeTakenToFetchCandidateTimelines = recommMasterT2 - recommMasterT1;

			candUserIDsPrimaryDim = RecommMasterUtils.extractCandUserIDs(candidateTimelinesPrimDim);
			// ///////////////////////////
			if (VerbosityConstants.verbose)
			{
				System.out.println("activitiesGuidingRecomm.size()=" + activitiesGuidingRecomm.size()
						+ " matchingUnitInCountsOrHours=" + matchingUnitInCountsOrHours + " activityAtRecommPoint = "
						+ activityObjectAtRecommPoint.getActivityName());

				System.out
						.println("Current timeline: " + currentTimeline.getActivityObjectNamesWithTimestampsInSequence()
								+ "; activitiesGuidingRecomm.size =" + this.activitiesGuidingRecomm.size());
				// System.out.println("\nActivity at Recomm point (Current Activity) =" + activityNameAtRecommPoint);
				System.out.println("\nprimaryDimensionValAtRecommPoint at Recomm point (Current Activity) ="
						+ primaryDimensionValAtRecommPoint);
				System.out.println("\nsecondaryDimensionValAtRecommPoint at Recomm point (Current Activity) ="
						+ this.activityObjectAtRecommPoint.getGivenDimensionVal(",", this.secondaryDimension));

				System.out.println("Number of primary candidate timelines =" + candidateTimelinesPrimDim.size());
				System.out.println("the candidate timelines are as follows:");
				candidateTimelinesPrimDim.entrySet().stream()
						.forEach(t -> System.out.println(t.getValue().getPrimaryDimensionValsInSequence()));

				System.out.println("Number of secondary candidate timelines =" + candidateTimelinesSecDim.size());
				System.out.println("the secondary candidate timelines are as follows:");
				candidateTimelinesSecDim.entrySet().stream().forEach(
						t -> System.out.println(t.getValue().getGivenDimensionValsInSequence(this.secondaryDimension)));
				// getActivityObjectNamesInSequence()));
			}

			if (candidateTimelinesPrimDim.size() == 0)
			{
				System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
						+ " at time:" + timeAtRecomm + "  because there are no candidate timelines");
				this.hasCandidateTimelines = false;// this.singleNextRecommendedActivity = null;
				this.nextActivityObjectsFromPrimaryCands = null;// this.topNextActivities =null;
				this.thresholdPruningNoEffect = true;
				return;
			}
			else if (candidateTimelinesSecDim.size() == 0)
			{
				System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
						+ " at time:" + timeAtRecomm + "  because there are no secondary (" + this.secondaryDimension
						+ ") candidate timelines");
				WToFile.appendLineToFileAbs(
						userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candidateTimelinesPrimDim.size()
								+ "," + candidateTimelinesSecDim.size() + "\n",
						Constant.getCommonPath() + "RTsRejWithPrimaryButNoSecondaryCands.csv");
				this.hasCandidateTimelines = false;// this.singleNextRecommendedActivity = null;
				this.nextActivityObjectsFromPrimaryCands = null;// this.topNextActivities =null;
				this.thresholdPruningNoEffect = true;
				return;
			}
			else
			{
				this.hasCandidateTimelines = true;
			}
			// System.out.println("\nDebug note192_223: getActivityNamesGuidingRecommwithTimestamps() " +
			// getActivityNamesGuidingRecommwithTimestamps() +
			// " size of current timeline="
			// + currentTimeline.getActivityObjectsInTimeline().size());
			// /////////////////////
			// TODO CHECK: HOW THE EFFECT OF THIS DIFFERS FROM THE EXPERIMENTS DONE FOR IIWAS: in iiWAS normalisation
			// was after thresholding (correct), here normalisation is before thresholding which should be changed
			long recommMasterT3 = System.currentTimeMillis();
			Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> normalisedDistFromPrimaryDimCandsRes = DistanceUtils
					.getNormalisedDistancesForCandidateTimelines(candidateTimelinesPrimDim, activitiesGuidingRecomm,
							caseType, this.userIDAtRecomm, this.dateAtRecomm, this.timeAtRecomm,
							Constant.getDistanceUsed(), this.lookPastType, this.hjEditDistancePrimaryDim,
							this.featureWiseEditDistance, this.featureWiseWeightedEditDistance,
							this.OTMDSAMEditDistance, this.editDistancesMemorizer);
			// editDistancesMemorizer.serialise(this.userIDAtRecomm);

			// added on 17 July 2018
			Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> normalisedDistFromSecondaryDimCandsRes = DistanceUtils
					.getNormalisedDistancesForCandidateTimelines(candidateTimelinesSecDim, activitiesGuidingRecomm,
							caseType, this.userIDAtRecomm, this.dateAtRecomm, this.timeAtRecomm,
							Constant.getDistanceUsed(), this.lookPastType, this.hjEditDistanceSecondaryDim,
							this.featureWiseEditDistance, this.featureWiseWeightedEditDistance,
							this.OTMDSAMEditDistance, this.editDistancesMemorizer);

			LinkedHashMap<String, Pair<String, Double>> distancesMapPrimaryDimUnsorted = normalisedDistFromPrimaryDimCandsRes
					.getFirst();
			LinkedHashMap<String, Pair<String, Double>> distancesMapSecondaryDimUnsorted = normalisedDistFromSecondaryDimCandsRes
					.getFirst(); // added on 17 July 2018

			// start of added on 15 Aug 2018
			if (distancesMapPrimaryDimUnsorted.size() == 0)
			{
				System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
						+ " at time:" + timeAtRecomm
						+ "  because there are no candidate timelines (after dist computation/filtering)");
				this.hasCandidateTimelines = false;// this.singleNextRecommendedActivity = null;
				this.nextActivityObjectsFromPrimaryCands = null;// this.topNextActivities =null;
				this.thresholdPruningNoEffect = true;
				return;
			}
			else if (distancesMapSecondaryDimUnsorted.size() == 0)
			{
				System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
						+ " at time:" + timeAtRecomm + "  because there are no secondary (" + this.secondaryDimension
						+ ") candidate timelines (after dist computation/filtering)");
				WToFile.appendLineToFileAbs(
						userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candidateTimelinesPrimDim.size()
								+ "," + candidateTimelinesSecDim.size() + "\n",
						Constant.getCommonPath() + "RTsRejWithPrimButNoSecCandsAfterDistFiltering.csv");
				this.hasCandidateTimelines = false;// this.singleNextRecommendedActivity = null;
				this.nextActivityObjectsFromPrimaryCands = null;// this.topNextActivities =null;
				this.thresholdPruningNoEffect = true;
				return;
			}
			else
			{
				this.hasCandidateTimelines = true;
			}
			// end of added on 15 AUg 2018

			this.endPointIndicesConsideredInPDCands = normalisedDistFromPrimaryDimCandsRes.getSecond();
			this.endPointIndicesConsideredInSDCands = normalisedDistFromSecondaryDimCandsRes.getSecond();
			// added 17 July 2018

			long recommMasterT4 = System.currentTimeMillis();
			long timeTakenToComputeNormEditDistances = recommMasterT4 - recommMasterT3;

			// System.out.println("\nDebug note192_229: getActivityNamesGuidingRecommwithTimestamps() " +
			// getActivityNamesGuidingRecommwithTimestamps() +" size of current timeline=" +
			// currentTimeline.getActivityObjectsInTimeline().size());
			// ########Sanity check
			this.errorExists = sanityCheckCandsDistancesSize(distancesMapPrimaryDimUnsorted,
					this.candidateTimelinesPrimDim, this.userIDAtRecomm, Constant.getCommonPath());
			// added on 17 July 2018
			this.errorExists = sanityCheckCandsDistancesSize(distancesMapSecondaryDimUnsorted,
					this.candidateTimelinesSecDim, this.userIDAtRecomm, Constant.getCommonPath());
			// ##############

			// /// REMOVE candidate timelines which are above the distance THRESHOLD. (actually here we remove the entry
			// for such candidate timelines from the distance scores map. // no pruning for baseline closest ST
			if (this.lookPastType.equals(Enums.LookPastType.ClosestTime) == false && Constant.useiiWASThreshold == true)
			{// changed from "Constant.useThreshold ==false)" on May 10 but should not affect result since we were not
				// doing thresholding anyway
				Triple<LinkedHashMap<String, Pair<String, Double>>, Double, Boolean> prunedRes = pruneAboveThreshold(
						distancesMapPrimaryDimUnsorted, typeOfThreshold, thresholdVal, activitiesGuidingRecomm,
						this.primaryDimension);
				distancesMapPrimaryDimUnsorted = prunedRes.getFirst();
				this.thresholdAsDistance = prunedRes.getSecond();
				this.thresholdPruningNoEffect = prunedRes.getThird();
			}
			// ////////////////////////////////

			if (distancesMapPrimaryDimUnsorted.size() == 0)
			{
				System.out.println("Warning: No candidate timelines below threshold distance");
				hasPrimaryDimCandTimelinesBelowThreshold = false;
				return;
			}
			else
			{
				hasPrimaryDimCandTimelinesBelowThreshold = true;
			}

			// Is this sorting necessary? // Disabling on Aug 3
			primaryDimDistancesSortedMap = sortIfNecessary(distancesMapPrimaryDimUnsorted,
					Constant.typeOfCandThresholdPrimDim, Constant.nearestNeighbourCandEDThresholdPrimDim);
			secondaryDimDistancesSortedMap = sortIfNecessary(distancesMapSecondaryDimUnsorted,
					Constant.typeOfCandThresholdSecDim, Constant.nearestNeighbourCandEDThresholdSecDim);

			if (caseType.equals(Enums.CaseType.CaseBasedV1))
			{
				System.out.println("this is CaseBasedV1");
				this.similarityOfEndPointActivityObjectCand = getCaseSimilarityEndPointActivityObjectCand(
						candidateTimelinesPrimDim, activitiesGuidingRecomm, caseType, userAtRecomm,
						this.dateAtRecomm.toString(), this.timeAtRecomm.toString(), alignmentBasedDistance);// getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
			}

			this.nextActivityObjectsFromPrimaryCands = RecommMasterUtils.fetchNextActivityObjects(
					primaryDimDistancesSortedMap, candidateTimelinesPrimDim, this.lookPastType,
					endPointIndicesConsideredInPDCands);

			this.nextActivityObjectsFromSecondaryCands = RecommMasterUtils.fetchNextActivityObjects(
					secondaryDimDistancesSortedMap, candidateTimelinesSecDim, this.lookPastType,
					endPointIndicesConsideredInSDCands);// added 17 July 2018

			if (!this.lookPastType.equals(Enums.LookPastType.ClosestTime))
			{
				if (!Sanity.eq(this.nextActivityObjectsFromPrimaryCands.size(), primaryDimDistancesSortedMap.size(),
						"Error at Sanity 349a (RecommenderMaster: this.nextActivityObjectsFromPrimaryCands.size()"
								+ nextActivityObjectsFromPrimaryCands.size() + "!= primaryDimDistancesSortedMap.size():"
								+ primaryDimDistancesSortedMap.size()))
				{
					errorExists = true;
				}
				// added on 17 July 2018
				if (!Sanity.eq(this.nextActivityObjectsFromSecondaryCands.size(), secondaryDimDistancesSortedMap.size(),
						"Error at Sanity 349b (RecommenderMaster: this.nextActivityObjectsFromSecondaryCands.size()"
								+ nextActivityObjectsFromSecondaryCands.size()
								+ "!= secondaryDimDistancesSortedMap.size():" + secondaryDimDistancesSortedMap.size()))
				{
					errorExists = true;
				}

				// Disabled logging for performance
				// System.out.println("this.nextActivityObjectsFromCands.size()= " +
				// this.nextActivityObjectsFromCands.size()
				// + "\ndistancesSortedMap.size()=" + distancesSortedMap.size()
				// + "\nthis.candidateTimelines.size()=" + this.candidateTimelines.size());

				// this will not be true when thresholding
				if (this.thresholdPruningNoEffect)
				{// this sanity check is only valid when not filtering cands
					if (Constant.typeOfCandThresholdPrimDim == Enums.TypeOfCandThreshold.None)
					{
						if (!Sanity.eq(primaryDimDistancesSortedMap.size(), this.candidateTimelinesPrimDim.size(),
								"Error at Sanity 349 (RecommenderMaster: editDistancesSortedMapFullCand.size()== this.candidateTimelines.size()  not satisfied"))
						{
							errorExists = true;
						}
					}
				}

			}
			else if (this.lookPastType.equals(Enums.LookPastType.ClosestTime))
			{
				this.nextActivityObjectsFromPrimaryCands = new LinkedHashMap<>();
			}

			if (VerbosityConstants.verbose)
			{
				printCandsAndTopNexts(lookPastType, primaryDimDistancesSortedMap, candidateTimelinesPrimDim,
						primaryDimension, nextActivityObjectsFromPrimaryCands,
						getActivityNamesGuidingRecommwithTimestamps(), currentTimeline.getActivityObjectsInTimeline());

				printCandsAndTopNexts(lookPastType, secondaryDimDistancesSortedMap, candidateTimelinesSecDim,
						secondaryDimension, nextActivityObjectsFromSecondaryCands,
						getActivityNamesGuidingRecommwithTimestamps(), currentTimeline.getActivityObjectsInTimeline());

			}

			if (VerbosityConstants.WriteEditDistancePerRtPerCand)
			{
				WToFile.writeEditDistancesPerRtPerCand(this.userAtRecomm, this.dateAtRecomm, this.timeAtRecomm,
						this.primaryDimDistancesSortedMap, this.candidateTimelinesPrimDim,
						this.nextActivityObjectsFromPrimaryCands, this.activitiesGuidingRecomm,
						activityObjectAtRecommPoint, VerbosityConstants.WriteCandInEditDistancePerRtPerCand,
						VerbosityConstants.WriteEditOperatationsInEditDistancePerRtPerCand,
						this.endPointIndicesConsideredInPDCands, this.primaryDimension,
						Constant.getCommonPath() + "EditDistancePerRtPerPrimaryCand.csv");
				// added on 17 July 2018
				WToFile.writeEditDistancesPerRtPerCand(this.userAtRecomm, this.dateAtRecomm, this.timeAtRecomm,
						this.secondaryDimDistancesSortedMap, this.candidateTimelinesSecDim,
						this.nextActivityObjectsFromSecondaryCands, this.activitiesGuidingRecomm,
						activityObjectAtRecommPoint, VerbosityConstants.WriteCandInEditDistancePerRtPerCand,
						VerbosityConstants.WriteEditOperatationsInEditDistancePerRtPerCand,
						this.endPointIndicesConsideredInSDCands, this.secondaryDimension,
						Constant.getCommonPath() + "EditDistancePerRtPerSecondaryCand.csv");
			}

			//////// Create ranked recommended act names
			this.recommendedActivityNamesWithRankscores = RecommMasterUtils.createRankedTopRecommendedActivityGDVals(
					this.nextActivityObjectsFromPrimaryCands, this.caseType, similarityOfEndPointActivityObjectCand,
					this.lookPastType, this.primaryDimDistancesSortedMap, this.primaryDimension);

			//////// Create ranked secondary dimension vals. Added on 18 July 2018
			this.recommendedSecondaryDimValsWithRankscores = RecommMasterUtils.createRankedTopRecommendedActivityGDVals(
					this.nextActivityObjectsFromSecondaryCands, this.caseType, null, this.lookPastType,
					this.secondaryDimDistancesSortedMap, this.secondaryDimension);

			// Start of added on 20 Feb 2018
			if (Constant.scoreRecommsByLocProximity)
			{
				// this.recommendedActivityNamesWithRankscores = RecommUtils.reScoreRecommsIncludingLocationProximity(
				// Constant.wtScoreRecommsByLocProximity, this.recommendedActivityNamesWithRankscores);
			}
			// End of added on 20 Feb 2018

			this.rankedRecommendedActNamesWithRankScoresStr = getRankedRecommendedValsWithRankScoresString(
					this.recommendedActivityNamesWithRankscores);
			this.rankedRecommendedActNamesWithoutRankScoresStr = getRankedRecommendedValsithoutRankScoresString(
					this.recommendedActivityNamesWithRankscores);

			////////////////////// Added on 18 July 2018
			this.rankedRecommendedSecDimValsWithRankScoresStr = getRankedRecommendedValsWithRankScoresString(
					this.recommendedSecondaryDimValsWithRankscores);
			this.rankedRecommendedSecDimValsWithoutRankScoresStr = getRankedRecommendedValsithoutRankScoresString(
					this.recommendedSecondaryDimValsWithRankscores);
			///////////////////////

			//
			this.normPDEditSimilarity = (ArrayList<Double>) this.nextActivityObjectsFromPrimaryCands.entrySet().stream()
					.map(e -> e.getValue().getSecond()).collect(Collectors.toList());

			if (this.caseType.equals(Enums.CaseType.CaseBasedV1))
			{
				this.simEndActivityObjForCorr = (ArrayList<Double>) this.nextActivityObjectsFromPrimaryCands.entrySet()
						.stream().map(nActObj -> similarityOfEndPointActivityObjectCand.get(nActObj.getKey()))
						.collect(Collectors.toList());
			}
			//
			if (VerbosityConstants.verbose)
			{
				System.out.println("Debug: rankedRecommendedActNamesWithRankScoresStr= "
						+ rankedRecommendedActNamesWithRankScoresStr);
				System.out.println("Debug: rankedRecommendedActNamesWithoutRankScoresStr= "
						+ rankedRecommendedActNamesWithoutRankScoresStr);

				System.out.println("Debug: rankedRecommendedSecDimValsWithRankScoresStr= "
						+ rankedRecommendedSecDimValsWithRankScoresStr);
				System.out.println("Debug: rankedRecommendedSecDimValsWithoutRankScoresStr= "
						+ rankedRecommendedSecDimValsWithoutRankScoresStr);

				System.out.println("Constant.removeCurrentActivityNameFromRecommendations = "
						+ Constant.removeCurrentActivityNameFromRecommendations);
			}
			////////

			if (Constant.removeCurrentActivityNameFromRecommendations)
			{/*
				 * IMPORTANT: If the next activity after the current activity object in the current timeline is an
				 * invalid activity, then we can include the current activity in the list of recommended activities,
				 * otherwise the current activity has to be removed from the list of recommended activities
				 */
				if (currentTimeline.getImmediateNextActivityInvalid() == 0) // not invalid
				{
					// TODO
					// this.recommendedActivityNamesWithRankscores = removeRecommPointActivityFromRankedRecomm(
					// recommendedActivityNamesWithRankscores, activityNameAtRecommPoint);
					System.out.println("removing recomm point activity (Current Activity) from list of recommendation");
				}
			}

			// System.out.println("Debug note192_2: current timeline " +
			// currentTimeline.getActivityObjectNamesInSequence());

			////////
			long recommMasterTEnd = System.currentTimeMillis();

			// start of curtain for performance string writing
			// String performanceString = this.userIDAtRecomm + "," + this.dateAtRecomm + "," + this.timeAtRecomm + ","
			// + matchingUnitInCountsOrHours + "," + trainingTimelines.size() + "," + this.trainingTimeline.size() + ","
			// + this.activitiesGuidingRecomm.size() + "," + this.candidateTimelines.size() + ","
			// + getSumOfActivityObjects(candidateTimelines) + "," + (recommMasterTEnd - recommMasterT0) + ","
			// + timeTakenToFetchCandidateTimelines + "," + timeTakenToComputeNormEditDistances + "\n";
			// WritingToFile.appendLineToFileAbsolute(performanceString, performanceFileName);
			// end of curtain for performance string writing
			//////////////
		}

		catch (

		Exception e)
		{
			e.printStackTrace();
			PopUps.getTracedErrorMsg("Exception in recommendation master");
		}

		System.out.println("\n^^^^^^^^^^^^^^^^Exiting Recommendation Master");
	}

	/**
	 * if typeOfCandThreshold is NearestNeighbour and nearestNeighbourCandEDThresholdGivenDim is valid (>=1), then no
	 * need to sort the map as the map must have already been sorted during filtering using th threshold.
	 * <p>
	 * Extracted logic to this method.
	 * 
	 * @param givenDimDistancesSortedMap
	 * @param typeOfCandThreshold
	 * @param nearestneighbourcandedthresholdprimdim
	 * @return
	 * @since 23 July 2018
	 */
	private static LinkedHashMap<String, Pair<String, Double>> sortIfNecessary(
			LinkedHashMap<String, Pair<String, Double>> givenDimDistancesUnSortedMap,
			TypeOfCandThreshold typeOfCandThreshold, int nearestNeighbourCandEDThresholdGivenDim)
	{
		LinkedHashMap<String, Pair<String, Double>> givenDimDistancesSortedMap = null;

		if (typeOfCandThreshold.equals(Enums.TypeOfCandThreshold.NearestNeighbour)
				&& nearestNeighbourCandEDThresholdGivenDim >= 1)
		{ // because already sorted while filtering
			givenDimDistancesSortedMap = givenDimDistancesUnSortedMap;
		}
		else
		{
			givenDimDistancesSortedMap = (LinkedHashMap<String, Pair<String, Double>>) ComparatorUtils
					.sortByValueAscendingStrStrDoub(givenDimDistancesUnSortedMap);
		}

		// before 23 July 2018: this following commented out code was inside the constructor.
		// if (Constant.typeOfCandThreshold == Enums.TypeOfCandThreshold.NearestNeighbour
		// && Constant.nearestNeighbourCandEDThresholdPrimDim >= 1)
		// { // because already sorted while filtering
		// primaryDimDistancesSortedMap = distancesMapPrimaryDimUnsorted;
		// }
		// else
		// {
		// primaryDimDistancesSortedMap = (LinkedHashMap<String, Pair<String, Double>>) ComparatorUtils
		// .sortByValueAscendingStrStrDoub(distancesMapPrimaryDimUnsorted);
		// }
		// if (Constant.typeOfCandThreshold == Enums.TypeOfCandThreshold.NearestNeighbour
		// && Constant.nearestNeighbourCandEDThresholdSecDim >= 1)
		// { // because already sorted while filtering
		// secondaryDimDistancesSortedMap = distancesMapSecondaryDimUnsorted;// added 17 July 2018
		// }
		// else
		// {
		// secondaryDimDistancesSortedMap = (LinkedHashMap<String, Pair<String, Double>>) ComparatorUtils
		// .sortByValueAscendingStrStrDoub(distancesMapSecondaryDimUnsorted);// added 17 July 2018
		// }

		return givenDimDistancesSortedMap;
	}

	/**
	 * 
	 * @param lookPastType
	 * @param distancesSortedMap
	 * @param candidateTimelinesPrimDim
	 * @param givenDimension
	 * @param nextActivityObjectsFromPrimaryCands
	 * @param activityNamesGuidingRecommwithTimestamps
	 * @param AOSInCurrentTimeline
	 */
	private static void printCandsAndTopNexts(Enums.LookPastType lookPastType,
			LinkedHashMap<String, Pair<String, Double>> distancesSortedMap,
			LinkedHashMap<String, Timeline> candidateTimelinesPrimDim, PrimaryDimension givenDimension,
			LinkedHashMap<String, Pair<ActivityObject2018, Double>> nextActivityObjectsFromPrimaryCands,
			String activityNamesGuidingRecommwithTimestamps, ArrayList<ActivityObject2018> AOSInCurrentTimeline)
	{
		System.out.println("---------givenDimension = " + givenDimension + " DistancesSortedMap.size()="
				+ distancesSortedMap.size());
		StringBuilder sbToWrite1 = new StringBuilder(
				">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" + "\n lookPastType:" + lookPastType
						+ "\n The candidate timelines  in increasing order of distance are:\n");
		distancesSortedMap.entrySet().stream()
				.forEach(e -> sbToWrite1.append("candID:" + e.getKey() + " dist:" + e.getValue().getSecond()
						+ "\n acts:"
						+ candidateTimelinesPrimDim.get(e.getKey()).getGivenDimensionValsInSequence(givenDimension)
						// .getActivityObjectNamesInSequence()
						+ "\n"));
		sbToWrite1.append("Top next activities are:\n");// +this.topNextRecommendedActivities);
		nextActivityObjectsFromPrimaryCands.entrySet().stream()
				.forEach(e -> sbToWrite1.append(" >>" + e.getValue().getFirst().getGivenDimensionVal(givenDimension)
				// .getPrimaryDimensionVal()// .getActivityName()
						+ ":" + e.getValue().getSecond()));
		System.out.println(sbToWrite1.toString());
		System.out.println("\nDebug note192_end: getActivityNamesGuidingRecommwithTimestamps() "
				+ activityNamesGuidingRecommwithTimestamps + " size of current timeline="
				+ AOSInCurrentTimeline.size());
	}

	/**
	 * 
	 * @param candidateTimelines
	 * @param filterCandByCurActTimeThreshInSecs
	 * @param actObjAtRecommPoint
	 * @return
	 */
	private static LinkedHashMap<String, Timeline> removeCandsWithEndCurrActBeyondThresh(
			LinkedHashMap<String, Timeline> candidateTimelines, int filterCandByCurActTimeThreshInSecs,
			ActivityObject2018 actObjAtRecommPoint, boolean verbose)
	{
		LinkedHashMap<String, Timeline> filteredCands = new LinkedHashMap<>();
		// Timestamp tsOfAOAtRecommPoint = activityObjectAtRecommPoint.getStartTimestamp();
		long timeInDayAOAtRecommPoint = DateTimeUtils.getTimeInDayInSecondsZoned(
				actObjAtRecommPoint.getStartTimestampInms(), actObjAtRecommPoint.getTimeZoneId());

		StringBuilder sb = new StringBuilder(
				"Debug12Feb Constant.filterCandByCurActTimeThreshInSecs=" + filterCandByCurActTimeThreshInSecs);

		if (verbose)
		{
			sb.append("\ntsOfAOAtRecommPoint:"
					+ ZonedDateTime.ofInstant(Instant.ofEpochMilli(actObjAtRecommPoint.getStartTimestampInms()),
							actObjAtRecommPoint.getTimeZoneId())
					+ "\ntimeInDayAOAtRecommPoint=" + timeInDayAOAtRecommPoint);
		}

		for (Entry<String, Timeline> candEntry : candidateTimelines.entrySet())
		{
			ArrayList<ActivityObject2018> aosInCand = candEntry.getValue().getActivityObjectsInTimeline();
			ActivityObject2018 lastAO = aosInCand.get(aosInCand.size() - 1);
			// Timestamp tCurrInCand = lastAO.getStartTimestamp();
			// long timeInDayCurrInCand = DateTimeUtils.getTimeInDayInSeconds(tCurrInCand);
			long timeInDayCurrInCand = DateTimeUtils.getTimeInDayInSecondsZoned(lastAO.getStartTimestampInms(),
					lastAO.getTimeZoneId());

			long absDiff = Math.abs(timeInDayCurrInCand - timeInDayAOAtRecommPoint);

			if (verbose)
			{
				// sb.append("\ntCurrInCand:" + tCurrInCand + "\ntimeInDayCurrInCand=" + timeInDayCurrInCand
				// + "\n\tabsDiff=" + absDiff);
				sb.append("\ntCurrInCand:"
						+ ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastAO.getStartTimestampInms()),
								lastAO.getTimeZoneId())
						+ "\ntimeInDayCurrInCand=" + timeInDayCurrInCand + "\n\tabsDiff=" + absDiff);
			}
			if (absDiff <= filterCandByCurActTimeThreshInSecs)
			{
				filteredCands.put(candEntry.getKey(), candEntry.getValue());
				// $$sb.append("-accepting");
			}
			else
			{
				// $$sb.append("-Rejecting");
			}
		}

		if (verbose)
		{
			System.out.println("\n" + sb.toString());
		}

		int sizeBeforeFiltering = candidateTimelines.size();
		int sizeAfterFilering = filteredCands.size();

		if (VerbosityConstants.WriteFilterCandByCurActTimeThreshInSecs)
		{
			WToFile.appendLineToFileAbs(
					sizeBeforeFiltering + "," + sizeAfterFilering + ","
							+ ((100.0 * (sizeBeforeFiltering - sizeAfterFilering)) / sizeBeforeFiltering + "\n"),
					Constant.getOutputCoreResultsPath() + "removeCandsWithEndCurrActBeyondThreshLog.csv");
		}
		return filteredCands;
	}

	/**
	 * Checks if distancesMapUnsorted.size() != candidateTimelines.size()
	 * 
	 * @param distancesMapUnsorted
	 * @param candidateTimelines
	 * @param userIDAtRecomm
	 * @param commonPath
	 * @return
	 */
	private static boolean sanityCheckCandsDistancesSize(
			LinkedHashMap<String, Pair<String, Double>> distancesMapUnsorted,
			LinkedHashMap<String, Timeline> candidateTimelines, String userIDAtRecomm, String commonPath)
	{
		boolean errorExists = false;

		if (distancesMapUnsorted.size() != candidateTimelines.size())
		{// Constant.nearestNeighbourCandEDThreshold > 0) // not expected when filtering is to be done
			if (Constant.typeOfCandThresholdPrimDim != Enums.TypeOfCandThreshold.None)
			{// some cands might have been removed due to thresholding
				System.out.println("Alert: editDistancesMapUnsorted.size() (" + distancesMapUnsorted.size()
						+ ") != candidateTimelines.size() (" + candidateTimelines.size() + ") , typeOfCandThreshold="
						+ Constant.typeOfCandThresholdPrimDim);
			}
			else
			{
				PopUps.printTracedErrorMsg("editDistancesMapUnsorted.size() (" + distancesMapUnsorted.size()
						+ ") != candidateTimelines.size() (" + candidateTimelines.size() + ")");
				String distancesMapUnsortedAsString = distancesMapUnsorted.entrySet().stream()
						.map(e -> e.getKey() + " - " + e.getValue().getFirst() + "_" + e.getValue().getSecond())
						.collect(Collectors.joining("\n"));
				String candidateTimelinesAsString = candidateTimelines.entrySet().stream()
						.map(e -> e.getKey() + " - " + e.getValue().getActivityObjectPDValsWithTimestampsInSequence())
						// .getActivityObjectNamesWithTimestampsInSequence())
						.collect(Collectors.joining("\n"));

				WToFile.appendLineToFileAbs(
						"User = " + userIDAtRecomm + "\ndistancesMapUnsortedAsString =\n" + distancesMapUnsortedAsString
								+ "\n\n candidateTimelinesAsString =\n" + candidateTimelinesAsString,
						commonPath + "ErrorLog376distancesMapUnsorted.txt");
				errorExists = true;
			}
		}
		return errorExists;
	}

	/**
	 * 
	 * @param distancesMapUnsorted
	 * @param typeOfThreshold
	 * @param thresholdVal
	 * @param activitiesGuidingRecomm
	 * @param primaryDimension
	 * @return Triple{prunedDistancesMap,thresholdAsDistance,thresholdPruningNoEffect}
	 */
	private static Triple<LinkedHashMap<String, Pair<String, Double>>, Double, Boolean> pruneAboveThreshold(
			LinkedHashMap<String, Pair<String, Double>> distancesMapUnsorted, TypeOfThreshold typeOfThreshold,
			double thresholdVal, ArrayList<ActivityObject2018> activitiesGuidingRecomm, PrimaryDimension primaryDimension)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Inside pruneAboveThreshold:\n");

		double thresholdAsDistance = -1;
		if (typeOfThreshold.equals(Enums.TypeOfThreshold.Global))/// IgnoreCase("Global"))
		{
			thresholdAsDistance = thresholdVal / 100;
		}
		else if (typeOfThreshold.equals(Enums.TypeOfThreshold.Percent))// IgnoreCase("Percent"))
		{
			double maxEditDistance = (new AlignmentBasedDistance(primaryDimension))
					.maxEditDistance(activitiesGuidingRecomm);
			thresholdAsDistance = maxEditDistance * (thresholdVal / 100);
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: type of threshold unknown in recommendation master"));
			// errorExists = true;
			System.exit(-2);
		}
		sb.append("thresholdAsDistance=" + thresholdAsDistance + "\n before pruning distancesMapUnsorted =\n");
		for (Entry<String, Pair<String, Double>> e : distancesMapUnsorted.entrySet())
		{
			sb.append(e.getKey() + "--" + e.getValue().toString() + "\n");
		}

		int countCandBeforeThresholdPruning = distancesMapUnsorted.size();// distanceScoresSorted.size();
		distancesMapUnsorted = TimelineTrimmers.removeAboveThreshold4SSD(distancesMapUnsorted, thresholdAsDistance);//
		int countCandAfterThresholdPruning = distancesMapUnsorted.size();

		sb.append("After pruning distancesMapUnsorted =\n");
		for (Entry<String, Pair<String, Double>> e : distancesMapUnsorted.entrySet())
		{
			sb.append(e.getKey() + "--" + e.getValue().toString() + "\n");
		}

		sb.append("thresholdAsDistance=" + thresholdAsDistance + " countCandBeforeThresholdPruning="
				+ countCandBeforeThresholdPruning + "countCandAfterThresholdPruning=" + countCandAfterThresholdPruning
				+ "\n");
		if (VerbosityConstants.verbose)
		{
			System.out.println(sb.toString());
		}
		boolean thresholdPruningNoEffect = (countCandBeforeThresholdPruning == countCandAfterThresholdPruning);

		if (!thresholdPruningNoEffect)
		{
			// System.out.println("Ohh..threshold pruning is happening. Are you sure you wanted this?");// +msg);
			// PopUps.showMessage("Ohh..threshold pruning is happening. Are you sure you wanted this?");// +msg);
			if (!Constant.useiiWASThreshold)
			{
				System.err.println("Error: threshold pruning is happening.");// +msg);
			}
		}
		return new Triple<LinkedHashMap<String, Pair<String, Double>>, Double, Boolean>(distancesMapUnsorted,
				thresholdAsDistance, thresholdPruningNoEffect);
	}

	/**
	 * 
	 */
	public Date getDateAtRecomm()
	{
		return dateAtRecomm;
	}

	/**
	 * 
	 */
	public String getActivityNamesGuidingRecomm()
	{
		StringBuilder res = new StringBuilder();

		for (ActivityObject2018 ae : activitiesGuidingRecomm)
		{
			res = StringUtils.fCat(res, ">>", ae.getActivityName());
			// res.append(">>" + ae.getActivityName());
		}
		return res.toString();
	}

	// $$start here

	// // /
	// /**
	// * Generates a ranked list of recommended Activity Objects and sets recommendedActivityNamesRankscorePairs
	// *
	// * and calls the following setter methods setRecommendedActivityNamesWithRankscores
	// * setRankedRecommendedActivityNamesWithRankScores setRankedRecommendedActivityNamesWithoutRankScores
	// *
	// * Is function with Constants Beta and rank scoring
	// *
	// * @param topNextActivityObjectsWithDistance
	// */
	// public void createRankedTopRecommendedActivityNamesCaseBasedV1_2(
	// ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityObjectsWithDistance,
	// LinkedHashMap<Integer, Double> similarityOfEndPointActivityObjectCand)
	// // we might remove these arguments as these are already member variables of this class
	// {
	// String topRankedActivityNamesWithScore, topRankedActivityNamesWithoutScore;
	//
	// int numberOfTopNextActivityObjects = topNextActivityObjectsWithDistance.size();
	//
	// // System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
	// // topRecommendationsWithDistance="+topRecommendationsWithDistance);
	// System.out.println(
	// "Debug inside createRankedTopRecommendedActivityObjects:
	// numberOfTopNextActivityEvenst=numberOfTopNextActivityEvenst");
	// // System.out.print("Debug inside createRankedTopRecommendedActivityObjects: the read next activity objects are:
	// // ");
	// // / for calculating their correlation
	// ArrayList<Double> normEditSimilarity = new ArrayList<Double>();
	// ArrayList<Double> simEndActivityObjForCorr = new ArrayList<Double>();
	// LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<String, Double>(); //
	// <ActivityName,RankScore>
	// // /
	// Double maxEditDistanceValExceptEnd = 0d, minEditDistanceValExceptEnd = 10000d;
	//
	// int numberOfNextAOsAtMaxDistance = 0;
	// for (int i = 0; i < numberOfTopNextActivityObjects; i++)
	// {
	// Double editDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();
	// if (editDistanceVal > maxEditDistanceValExceptEnd)
	// {
	// maxEditDistanceValExceptEnd = editDistanceVal;
	// numberOfNextAOsAtMaxDistance = 1;
	// }
	//
	// else if (editDistanceVal == maxEditDistanceValExceptEnd)
	// {
	// numberOfNextAOsAtMaxDistance += 1;
	// }
	//
	// if (editDistanceVal < minEditDistanceValExceptEnd)
	// {
	// minEditDistanceValExceptEnd = editDistanceVal;
	// // numberOfNextAOsAtMaxDistance = 1;
	// }
	// // else if (editDistanceVal == maxEditDistanceValExceptEnd)
	// // {
	// // numberOfNextAOsAtMaxDistance += 1;
	// // }
	// }
	//
	// for (int i = 0; i < numberOfTopNextActivityObjects; i++)
	// {
	// String topNextActivityName = topNextActivityObjectsWithDistance.get(i).getFirst().getActivityName();
	// Double editDistanceValExceptEnd = topNextActivityObjectsWithDistance.get(i).getSecond();
	//
	// Integer candTimelineID = topNextActivityObjectsWithDistance.get(i).getThird();
	//
	// Double simEndPointActivityObject = similarityOfEndPointActivityObjectCand.get(candTimelineID);
	//
	// Double simRankScore;// represents similarity
	//
	// double normEditDistanceValExceptEnd = StatsUtils.minMaxNorm(editDistanceValExceptEnd,
	// maxEditDistanceValExceptEnd, minEditDistanceValExceptEnd);
	//
	// normEditSimilarity.add(1 - normEditDistanceValExceptEnd);
	// simEndActivityObjForCorr.add(simEndPointActivityObject);
	//
	// if (Constant.rankScoring.trim().equalsIgnoreCase("product"))
	// {
	// simRankScore = (1d - normEditDistanceValExceptEnd) * simEndPointActivityObject;
	// // simRankScore = (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) *
	// // simEndPointActivityObject;
	// // System.out.println("Prod RANK SCORE CALC=" + "(1-(" + editDistanceValExceptEnd + "/" +
	// // maxEditDistanceValExceptEnd + "))* (" +
	// // simEndPointActivityObject + ")");
	// System.out.println("Prod RANK SCORE CALC=" + "(1-(" + normEditDistanceValExceptEnd + "))* ("
	// + simEndPointActivityObject + ")");
	// }
	//
	// else
	// {
	// simRankScore = Constant.ALPHA * (1d - normEditDistanceValExceptEnd)
	// + (1 - Constant.ALPHA) * simEndPointActivityObject;
	// System.out.println("Sum RANK SCORE CALC=" + Constant.ALPHA + "*(1-(" + normEditDistanceValExceptEnd
	// + "))" + "" + "+" + (1 - Constant.ALPHA) + "*(" + simEndPointActivityObject + ")");
	// // simRankScore = Constant.BETA * (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) + (1 -
	// // Constant.BETA) *
	// // simEndPointActivityObject;
	// // System.out.println("Sum RANK SCORE CALC=" + Constant.BETA + "*(1-(" + editDistanceValExceptEnd + "/"
	// // + maxEditDistanceValExceptEnd + "))" +
	// // "" + "+" + (1 - Constant.BETA) +
	// // "*("
	// // + simEndPointActivityObject + ")");
	// }
	//
	// if (recommendedActivityNamesRankscorePairs.containsKey(topNextActivityName) == false)
	// {
	// recommendedActivityNamesRankscorePairs.put(topNextActivityName, simRankScore);
	// }
	//
	// else
	// {
	// recommendedActivityNamesRankscorePairs.put(topNextActivityName,
	// recommendedActivityNamesRankscorePairs.get(topNextActivityName) + simRankScore);
	// }
	// }
	//
	// System.out.println("Num of top next activity objects at Max distance=" + numberOfNextAOsAtMaxDistance);
	// if (numberOfNextAOsAtMaxDistance > 5)
	// {
	// System.out.println("Warning: num of top next aos at max distance(>5) =" + numberOfNextAOsAtMaxDistance);
	// }
	//
	// recommendedActivityNamesRankscorePairs = (LinkedHashMap<String, Double>) ComparatorUtils
	// .sortByValueDesc(recommendedActivityNamesRankscorePairs);
	// // Sorted in descending order of ranked score: higher ranked score means more top in rank (larger numeric value
	// // of rank)
	//
	// // ///////////IMPORTANT //////////////////////////////////////////////////////////
	// this.setRecommendedActivityNamesWithRankscores(recommendedActivityNamesRankscorePairs);
	//
	// this.setRankedRecommendedActivityNamesWithRankScores(recommendedActivityNamesRankscorePairs);
	// this.setRankedRecommendedActivityNamesWithoutRankScores(recommendedActivityNamesRankscorePairs);
	//
	// this.setNormEditSimilarity(normEditSimilarity);
	// this.setSimEndActivityObjForCorr(simEndActivityObjForCorr);
	// // /////////////////////////////////////////////////////////////////////
	//
	// topRankedActivityNamesWithScore = getRankedRecommendedActivityNamesWithRankScores();
	// topRankedActivityNamesWithoutScore = getRankedRecommendedActivityNamesWithoutRankScores();
	//
	// if (VerbosityConstants.verbose)
	// {
	// System.out.println(
	// "Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithScore= "
	// + topRankedActivityNamesWithScore);
	// System.out.println(
	// "Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithoutScore= "
	// + topRankedActivityNamesWithoutScore);
	// }
	// // return topRankedString;
	// }

	// private void setNormEditSimilarity(ArrayList<Double> arr)
	// {
	// this.normEditSimilarity = arr;
	// }
	//
	// private void setSimEndActivityObjForCorr(ArrayList<Double> arr)
	// {
	// this.simEndActivityObjForCorr = arr;
	// }

	// public double getAvgRestSimilarity()
	// {
	// if (normEditSimilarity == null)
	// {
	// if (Constant.caseType.equals("CaseBasedV1"))
	// {
	// System.err.println("Error: Norm edit similarity is null (not set)");
	// }
	//
	// // else
	// // {
	// // System.out.println("Alert: Norm edit similarity is null for caseType =" + Constant.caseType);
	// // }
	//
	// return Double.NaN;// -99999999;
	// }
	//
	// // PearsonsCorrelation pc = new PearsonsCorrelation();
	// Mean mean = new Mean();
	// return (mean.evaluate(UtilityBelt.toPrimitive(normEditSimilarity), 0, normEditSimilarity.size()));//
	// pc.correlation(UtilityBelt.toPrimitive(normEditSimilarity),
	// }

	// public double getAvgEndSimilarity()
	// {
	// if (simEndActivityObjForCorr == null)
	// {
	// if (Constant.caseType.equals("CaseBasedV1"))
	// {
	// System.err.println("Error: sim end activity object is null (not set)");
	// }
	//
	// // else
	// // {
	// // System.out.println("Alert: sim end activity object is null for caseType =" + Constant.caseType);
	// // }
	// // System.err.println("Error: sim end activity object are null (not set)");
	//
	// return Double.NaN;// -99999999;
	// }
	//
	// // PearsonsCorrelation pc = new PearsonsCorrelation();
	// Mean mean = new Mean();
	// return (mean.evaluate(UtilityBelt.toPrimitive(simEndActivityObjForCorr), 0, simEndActivityObjForCorr.size()));//
	// pc.correlation(UtilityBelt.toPrimitive(normEditSimilarity),
	// // UtilityBelt.toPrimitive(simEndActivityObjForCorr)));
	//
	// }

	// public static double getSDRestSimilarity()
	// {
	// if (normEditSimilarity == null)
	// {
	// if (Constant.caseType.equals("CaseBasedV1"))
	// {
	// System.err.println("Error: normEditSimilarity is null (not set)");
	// }
	//
	// // else
	// // {
	// // System.out.println("Alert: normEditSimilarity is null for caseType =" + Constant.caseType);
	// // }
	// // System.err.println("Error: Norm edit similarity and sim end activity object are null (not set)");
	// return Double.NaN;// -99999999;
	// }
	//
	// // PearsonsCorrelation pc = new PearsonsCorrelation();
	// StandardDeviation sd = new StandardDeviation();
	// return (sd.evaluate(UtilityBelt.toPrimitive(normEditSimilarity)));
	//
	// }

	/**
	 * Generates a ranked list of recommended Activity Objects and sets recommendedActivityNamesRankscorePairs
	 * 
	 * and calls the following setter methods setRecommendedActivityNamesWithRankscores
	 * setRankedRecommendedActivityNamesWithRankScores setRankedRecommendedActivityNamesWithoutRankScores
	 * 
	 * Is function with Constants Beta and rank scoring
	 * 
	 * @param topNextActivityObjectsWithDistance
	 */
	// public void createRankedTopRecommendedActivityNamesSimpleV3_2(
	// ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityObjectsWithDistance, String userAtRecomm,
	// String dateAtRecomm, String timeAtRecomm)
	// // LinkedHashMap<Integer, Double> similarityOfEndPointActivityObjectCand) // we might remove these arguments as
	// // these are already member variables of this
	// // class
	// {
	// String topRankedActivityNamesWithScore, topRankedActivityNamesWithoutScore;
	//
	// int numberOfTopNextActivityObjects = topNextActivityObjectsWithDistance.size();
	//
	// // System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
	// // topRecommendationsWithDistance="+topRecommendationsWithDistance);
	// System.out.println(
	// "Debug inside createRankedTopRecommendedActivityObjects:
	// numberOfTopNextActivityEvenst=numberOfTopNextActivityEvenst");
	// // System.out.print("Debug inside createRankedTopRecommendedActivityObjects: the read next activity objects are:
	// // ");
	//
	// LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<String, Double>(); //
	// <ActivityName,RankScore>
	//
	// Double maxEditDistanceVal = 0d, minEditDistanceVal = 10000d;
	//
	// int numberOfNextAOsAtMaxDistance = 0;
	// for (int i = 0; i < numberOfTopNextActivityObjects; i++)
	// {
	// Double editDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();
	// if (editDistanceVal > maxEditDistanceVal)
	// {
	// maxEditDistanceVal = editDistanceVal;
	// numberOfNextAOsAtMaxDistance = 1;
	// }
	//
	// else if (editDistanceVal == maxEditDistanceVal)
	// {
	// numberOfNextAOsAtMaxDistance += 1;
	// }
	//
	// if (editDistanceVal < minEditDistanceVal)
	// {
	// minEditDistanceVal = editDistanceVal;
	// // numberOfNextAOsAtMaxDistance = 1;
	// }
	// // else if (editDistanceVal == maxEditDistanceValExceptEnd)
	// // {
	// // numberOfNextAOsAtMaxDistance += 1;
	// // }
	// }
	// StringBuilder editDistances = new StringBuilder(), normalisedEditDistances = new StringBuilder();
	//
	// for (int i = 0; i < numberOfTopNextActivityObjects; i++)
	// {
	// String topNextActivityName = topNextActivityObjectsWithDistance.get(i).getFirst().getActivityName();
	// Double editDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();
	//
	// Integer candTimelineID = topNextActivityObjectsWithDistance.get(i).getThird();
	//
	// // Double simEndPointActivityObject = similarityOfEndPointActivityObjectCand.get(candTimelineID);
	//
	// Double simRankScore;// represents similarity
	//
	// double normEditDistanceVal = StatsUtils.minMaxNorm(editDistanceVal, maxEditDistanceVal, minEditDistanceVal);
	//
	// if (VerbosityConstants.WriteNormalisation)
	// {
	// editDistances.append("_" + editDistanceVal);
	// normalisedEditDistances.append("_" + normEditDistanceVal);
	// }
	//
	// simRankScore = (1d - normEditDistanceVal);// * simEndPointActivityObject;
	// // simRankScore = (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) *
	// // simEndPointActivityObject;
	// // System.out.println("Prod RANK SCORE CALC=" + "(1-(" + editDistanceValExceptEnd + "/" +
	// // maxEditDistanceValExceptEnd + "))* (" +
	// // simEndPointActivityObject + ")");
	// System.out.println("Simple RANK SCORE CALC=" + "(1-(" + normEditDistanceVal + "))");// * (" +
	// // simEndPointActivityObject
	// // + ")");
	//
	// if (recommendedActivityNamesRankscorePairs.containsKey(topNextActivityName) == false)
	// {
	// recommendedActivityNamesRankscorePairs.put(topNextActivityName, simRankScore);
	// }
	//
	// else
	// {
	// recommendedActivityNamesRankscorePairs.put(topNextActivityName,
	// recommendedActivityNamesRankscorePairs.get(topNextActivityName) + simRankScore);
	// }
	// }
	//
	// System.out.println("Num of top next activity objects at Max distance=" + numberOfNextAOsAtMaxDistance);
	// if (numberOfNextAOsAtMaxDistance > 5)
	// {
	// System.out.println("Warning: num of top next aos at max distance(>5) =" + numberOfNextAOsAtMaxDistance);
	// }
	//
	// recommendedActivityNamesRankscorePairs = (LinkedHashMap<String, Double>) ComparatorUtils
	// .sortByValueDesc(recommendedActivityNamesRankscorePairs);
	// // Sorted in descending order of ranked score: higher ranked score means more top in rank (larger numeric value
	// // of rank)
	//
	// // ///////////IMPORTANT //////////////////////////////////////////////////////////
	// this.setRecommendedActivityNamesWithRankscores(recommendedActivityNamesRankscorePairs);
	//
	// this.setRankedRecommendedActivityNamesWithRankScores(recommendedActivityNamesRankscorePairs);
	// this.setRankedRecommendedActivityNamesWithoutRankScores(recommendedActivityNamesRankscorePairs);
	//
	// // /////////////////////////////////////////////////////////////////////
	//
	// topRankedActivityNamesWithScore = getRankedRecommendedActivityNamesWithRankScores();
	// topRankedActivityNamesWithoutScore = getRankedRecommendedActivityNamesWithoutRankScores();
	//
	// if (VerbosityConstants.verbose)
	// {
	// System.out.println(
	// "Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithScore= "
	// + topRankedActivityNamesWithScore);
	// System.out.println(
	// "Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithoutScore= "
	// + topRankedActivityNamesWithoutScore);
	// }
	//
	// if (VerbosityConstants.WriteNormalisation)
	// {
	// String toWrite = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + editDistances + ","
	// + normalisedEditDistances + "\n";
	// WritingToFile.appendLineToFileAbsolute(toWrite, Constant.getCommonPath() + "NormalisationDistances.csv");
	// }
	// // return topRankedString;
	// }

	// /////////////////////////////////////////////////////////////////////
	// /**
	// * Set Map of <recommended Activity Name, sim rank score>
	// *
	// * @param recommendedActivityNamesRankscorePairs
	// */
	// public void setRecommendedActivityNamesWithRankscores(
	// LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs)
	// {
	// this.recommendedActivityNamesWithRankscores = recommendedActivityNamesRankscorePairs;
	// }

	public LinkedHashMap<String, Double> getRecommendedActivityNamesWithRankscores()
	{
		return this.recommendedActivityNamesWithRankscores;
	}

	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	/**
	 * Generate the string: '__recommendedActivityName1:simRankScore1__recommendedActivityName2:simRankScore2'
	 * 
	 * @param recommendedActivityValsRankscorePairs
	 */
	private static String getRankedRecommendedValsWithRankScoresString(
			LinkedHashMap<String, Double> recommendedActivityValsRankscorePairs)
	{
		StringBuilder topRankedString = new StringBuilder();// String topRankedString= new String();
		StringBuilder msg = new StringBuilder();
		for (Map.Entry<String, Double> entry : recommendedActivityValsRankscorePairs.entrySet())
		{
			String recommAct = entry.getKey();
			double roundedRankScore = Evaluation.round(entry.getValue(), 4);
			topRankedString.append("__" + recommAct + ":" + roundedRankScore);
			msg.append("recomm act:" + recommAct + ", rank score: " + roundedRankScore + "\n");
			// topRankedString+= "__"+entry.getKey()+":"+TestStats.round(entry.getValue(),4);
		}
		if (VerbosityConstants.verboseRankScoreCalcToConsole)
		{
			System.out.println(msg.toString() + "\n");
		}
		return topRankedString.toString();
	}

	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	/**
	 * Generate string as '__recommendedActivityName1__recommendedActivityName2'
	 * 
	 * @param recommendedActivityNameRankscorePairs
	 * @return
	 */
	private static String getRankedRecommendedValsithoutRankScoresString(
			LinkedHashMap<String, Double> recommendedActivityNameRankscorePairs)
	{
		StringBuilder rankedRecommendationWithoutRankScores = new StringBuilder();
		for (Map.Entry<String, Double> entry : recommendedActivityNameRankscorePairs.entrySet())
		{
			rankedRecommendationWithoutRankScores.append("__" + entry.getKey());
		}
		return rankedRecommendationWithoutRankScores.toString();
	}

	// /////////////////////////////////////////////////////////////////////

	/*
	 * public String getSingleNextRecommendedActivity() { return this.singleNextRecommendedActivity; }
	 */
	/*
	 * public String getTopRecommendedActivities() { return this.topNextRecommendedActivities; }
	 * 
	 * public String getTopRecommendedActivitiesWithoutDistance() { String result=""; String
	 * topActivities=this.topNextRecommendedActivities; String[] splitted1= topActivities.split("__");
	 * 
	 * for(int i=1;i<splitted1.length;i++) { String[] splitted2=splitted1[i].split(":");
	 * result=result+"__"+splitted2[0]; }
	 * 
	 * return result; }
	 */
	// //

	/// End of added on 9 Aug 2017

	// ////////
	/*
	 * Added: Oct 7, 2014: for IMPORTANT POINT: THE CANDIDATE TIMELINE IS THE DIRECT CANDIDATE TIMELINE AND NOT THE
	 * LEAST DISTANT SUBCANDIDATE.
	 */
	/**
	 * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the edit distance
	 * value between the end point activity object (current activity) of the candidate timeline and the current activity
	 * object (activity at recomm point)
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 *            can be 'SimpleV3' or 'CaseBasedV1'
	 * @param userID
	 *            user for which recommendation is being done
	 * @param dateAtRecomm
	 *            only for the purpose of writing to file
	 * @param timeAtRecomm
	 *            only for the purpose of writing to file
	 * @param alignmentBasedDistance
	 * @return <CanditateTimelineID to which this end point Activity Object belongs, edit distance of this end point
	 *         Activity Object of this candidate with end point Activity Object>
	 */
	public LinkedHashMap<String, Double> getCaseSimilarityEndPointActivityObjectCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, int userID, String dateAtRecomm, String timeAtRecomm,
			AlignmentBasedDistance alignmentBasedDistance)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<String, Double> candEndPointEditDistances = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> candEntry : candidateTimelines.entrySet())
		{
			ArrayList<ActivityObject2018> activityObjectsInCand = candEntry.getValue().getActivityObjectsInTimeline();
			Double endPointEditDistanceForThisCandidate = new Double(-9999);

			if (caseType.equals(Enums.CaseType.CaseBasedV1))// "CaseBasedV1")) // CaseBasedV1
			{
				ActivityObject2018 endPointActivityObjectCandidate = (activityObjectsInCand
						.get(activityObjectsInCand.size() - 1)); // only the end point activity
																	// object
				ActivityObject2018 endPointActivityObjectCurrentTimeline = (activitiesGuidingRecomm
						.get(activitiesGuidingRecomm.size() - 1)); // activityObjectAtRecommPoint

				switch (Constant.getDatabaseName()) // (Constant.DATABASE_NAME)
				{
				case "geolife1":
					endPointEditDistanceForThisCandidate = alignmentBasedDistance.getCaseBasedV1SimilarityGeolifeData(
							endPointActivityObjectCandidate, endPointActivityObjectCurrentTimeline, userID);
					break;
				case "dcu_data_2":
					endPointEditDistanceForThisCandidate = alignmentBasedDistance.getCaseBasedV1SimilarityDCUData(
							endPointActivityObjectCandidate, endPointActivityObjectCurrentTimeline, userID);
					break;
				default:
					System.err.println(PopUps.getTracedErrorMsg(
							"Error in getCaseSimilarityEndPointActivityObjectCand: unrecognised database name"));
					break;
				}

			}
			else if (caseType.equals(Enums.CaseType.SimpleV3))// "SimpleV3"))
			{
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistance(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				System.err.println(PopUps.getTracedErrorMsg(
						"ERROR in getEditDistancesOfEndPointActivityCand(): This method should not have been called for case type="
								+ caseType));
				errorExists = true;
				endPointEditDistanceForThisCandidate = null;
			}
			else
			{
				System.err.println(PopUps.getTracedErrorMsg(
						"ERROR in getEditDistancesOfEndPointActivityCand(): This method should not have been called for case type="
								+ caseType));
				errorExists = true;
				endPointEditDistanceForThisCandidate = null;
			}

			if (endPointEditDistanceForThisCandidate < 0)
			{
				System.err.println(PopUps.getTracedErrorMsg(
						"Error in getCaseSimilarityEndPointActivityObjectCand: endPointEditDistanceForThisCandidate "
								+ endPointEditDistanceForThisCandidate + " is not correct"));
				System.exit(-99);
			}

			candEndPointEditDistances.put(candEntry.getKey(), endPointEditDistanceForThisCandidate);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}

		return candEndPointEditDistances;
	}

	// /

	public static String getStringCodeOfActivityObjects(ArrayList<ActivityObject2018> activityObjects)
	{
		StringBuilder code = new StringBuilder();
		for (ActivityObject2018 ao : activityObjects)
		{
			code.append(ao.getCharCodeFromActID());
		}
		return code.toString();
	}

	////

	public boolean hasError()
	{
		return this.errorExists;
	}

	public void setErrorExists(boolean exists)
	{
		this.errorExists = exists;
	}

	/// Start of Methods for interface

	public int getNumOfCandidateTimelines()
	{
		return this.candidateTimelinesPrimDim.size();
	}

	public int getNumOfActsInActsGuidingRecomm()
	{
		return this.activitiesGuidingRecomm.size();
	}

	public int getNumOfValidActsInActsGuidingRecomm()
	{
		int count = 0;
		for (ActivityObject2018 ae : this.activitiesGuidingRecomm)
		{
			if (UtilityBelt.isValidActivityName(ae.getActivityName()))
			{
				count++;
			}
		}
		return count;
	}

	public Timeline getCandidateTimeline(String timelineID)
	{
		return this.candidateTimelinesPrimDim.get(timelineID);
	}

	public ArrayList<Timeline> getOnlyCandidateTimeslines()
	{
		return (ArrayList<Timeline>) this.candidateTimelinesPrimDim.entrySet().stream()
				.map(e -> (Timeline) e.getValue()).collect(Collectors.toList());
	}

	/**
	 * @since 18 July 2018
	 * @return
	 */
	public ArrayList<Timeline> getOnlySecDimCandidateTimeslines()
	{
		return (ArrayList<Timeline>) this.candidateTimelinesSecDim.entrySet().stream().map(e -> (Timeline) e.getValue())
				.collect(Collectors.toList());
	}

	public Set<String> getCandidateTimelineIDs()
	{
		return this.candidateTimelinesPrimDim.keySet();
	}

	/**
	 * take care of sec dim cands as well.
	 */
	public boolean hasCandidateTimeslines()
	{
		return hasCandidateTimelines;
	}

	public boolean hasCandidateTimelinesBelowThreshold()
	{
		return hasPrimaryDimCandTimelinesBelowThreshold;
	}

	public boolean hasThresholdPruningNoEffect()
	{
		return thresholdPruningNoEffect;
	}

	public boolean isNextActivityJustAfterRecommPointIsInvalid()
	{
		return this.nextActivityJustAfterRecommPointIsInvalid;
	}

	public ActivityObject2018 getActivityObjectAtRecomm()
	{
		return this.activityObjectAtRecommPoint;
	}

	/**
	 * 
	 * @return
	 */
	public double getThresholdAsDistance()
	{
		return thresholdAsDistance;
	}

	public int getNumOfCandTimelinesBelowThresh() // satisfying threshold
	{
		// if(hasCandidateTimelinesBelowThreshold==false)
		// {
		// System.err.println("Error: Sanity Check RM60 failed: trying to get number of candidate timelines below
		// threshold while there is no candidate below threshold, u shouldnt
		// have called this function");
		// }
		if (this.hasPrimaryDimCandTimelinesBelowThreshold)
		{/*
			 * Assuming that threshold has already been applied
			 */
			return this.primaryDimDistancesSortedMap.size();
		}
		else
		{
			return 0;
		}
	}

	/**
	 * @since 18 July 2018
	 */
	public int getNumOfSecDimCandTimelines() // satisfying threshold
	{
		// if(hasCandidateTimelinesBelowThreshold==false)
		// {
		// System.err.println("Error: Sanity Check RM60 failed: trying to get number of candidate timelines below
		// threshold while there is no candidate below threshold, u shouldnt
		// have called this function");
		// }
		if (secondaryDimDistancesSortedMap == null)
		{
			return 0;

		}
		else
		{
			return this.secondaryDimDistancesSortedMap.size();
		}
	}

	/**
	 * Returns next activity names as String
	 * 
	 * @return
	 */
	public String getNextActNamesWithoutDistString()
	{// LinkedHashMap<String, Pair<ActivityObject, Double>>
		StringBuilder result = new StringBuilder("");
		nextActivityObjectsFromPrimaryCands.entrySet().stream()
				.forEach(e -> result.append("__" + e.getValue().getFirst().getActivityName()));
		return result.toString();
	}

	/**
	 * Returns next activity names with distance as String
	 * 
	 * @return
	 */
	public String getNextActNamesWithDistString()
	{// LinkedHashMap<String, Pair<ActivityObject, Double>>
		StringBuilder result = new StringBuilder("");
		nextActivityObjectsFromPrimaryCands.entrySet().stream().forEach(e -> result
				.append("__" + e.getValue().getFirst().getActivityName() + ":" + e.getValue().getSecond().toString()));
		return result.toString();
	}

	// Names not changed to PD vals because this method is inherited from abstract class
	public String getActivityNamesGuidingRecommwithTimestamps()
	{
		StringBuilder res = new StringBuilder();
		for (ActivityObject2018 ae : activitiesGuidingRecomm)
		{
			res = StringUtils.fCat(res, "  ", ae.getActivityName(), "__", ae.getPrimaryDimensionVal().toString(), "__",
					ae.getStartTimestamp().toString(), "_to_", ae.getEndTimestamp().toString());
			// res.append(" " + ae.getActivityName() + "__" + ae.getStartTimestamp() + "_to_" + ae.getEndTimestamp());
		}
		return res.toString();
	}

	/**
	 * @return rankedRecommendedActivityPDValsithoutRankScoresString
	 */
	public String getRankedRecommendedActNamesWithoutRankScores()
	{
		return this.rankedRecommendedActNamesWithoutRankScoresStr;
	}

	public String getRankedRecommendedActNamesWithRankScores()
	{
		return this.rankedRecommendedActNamesWithRankScoresStr;
	}

	/**
	 * @since 18 July 2018
	 */
	public String getRankedRecommendedSecDimValsWithoutRankScores()
	{
		return this.rankedRecommendedSecDimValsWithoutRankScoresStr;
	}

	/**
	 * @since 18 July 2018
	 */
	public String getRankedRecommendedSecDimValsWithRankScores()
	{
		return this.rankedRecommendedSecDimValsWithRankScoresStr;
	}

	public int getNumOfDistinctRecommendations()
	{
		return recommendedActivityNamesWithRankscores.size();
	}

	/**
	 * @since 18 July 2018
	 */
	public int getNumOfDistinctSecDimRecommendations()
	{
		return recommendedSecondaryDimValsWithRankscores.size();
	}

	public LinkedHashMap<String, Pair<String, Double>> getDistancesSortedMap()
	{
		return this.primaryDimDistancesSortedMap;
	}

	public LinkedHashMap<String, Integer> getEndPointIndicesConsideredInCands()
	{
		return endPointIndicesConsideredInPDCands;
	}

	public ArrayList<ActivityObject2018> getActsGuidingRecomm()
	{
		return activitiesGuidingRecomm;
	}
	/// end of methods for interface

}

/////////////////////////////////////////////////
/// **
// *
// * @param map
// * @return
// */
// public long getSumOfActivityObjects(LinkedHashMap<Integer, TimelineWithNext> map)
// {
// long count = 0;
//
// for (Map.Entry<Integer, TimelineWithNext> entry : map.entrySet())
// {
// int a = entry.getValue().countNumberOfValidActivities();
// int b = entry.getValue().size();
//
// if (a != b)
// {
// PopUps.showError(
// "Error in getSumOfActivityObjects a should be equal to be since we removed invalid aos beforehand but a = "
// + a + " and b=" + b);
// }
// count += a;
// }
// return count;
// }
// public static String getNextValidActivityNameAsCode(String topSimilarUserDayActivitiesAsStringCode,
// int endPointIndexForSubsequenceWithHighestSimilarity)
// {
// String unknownAsCode = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY1);// ("Unknown");
// String othersAsCode = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY2);// ("Others");
// String nextValidActivity = null;
//
// for (int i = endPointIndexForSubsequenceWithHighestSimilarity + 1; i < topSimilarUserDayActivitiesAsStringCode
// .length(); i++)
// {
// if (String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i)).equals(unknownAsCode)
// || String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i)).equals(othersAsCode))
// {
// continue;
// }
// else
// {
// nextValidActivity = String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i));
// }
// }
// return nextValidActivity;
// }

// End of curtain 9 march 2017: commented out because the other constructor is essentially doing the same thing but
// with fewer logging for performance reasons
// /**
// * Recommendation for a particular RT
// *
// * @param trainingTimelines
// * @param testTimelines
// * @param dateAtRecomm
// * @param timeAtRecomm
// * start time of the current activity, equivalent to using end time of the current activity
// * @param userAtRecomm
// * user for which recommendation is being generated
// * @param thresholdVal
// * @param typeOfThreshold
// * @param matchingUnitInCountsOrHours
// * @param caseType
// * @param lookPastType
// */
// public RecommendationMasterMUMar2017(LinkedHashMap<Date, UserDayTimeline> trainingTimelines,
// LinkedHashMap<Date, UserDayTimeline> testTimelines, String dateAtRecomm, String timeAtRecomm,
// int userAtRecomm, double thresholdVal, String typeOfThreshold, double matchingUnitInCountsOrHours,
// String caseType, String lookPastType // counts or hours
// )
// {
// String performanceFileName = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/Test/Performance.csv";
// StringBuilder performanceString = new StringBuilder();
// long recommMasterT0 = System.currentTimeMillis();
// performanceString.append("Start:" + recommMasterT0);
// // hjEditDistance = new HJEditDistance();
// // alignmentBasedDistance = new AlignmentBasedDistance(); // used for case based similarity
// // featureWiseEditDistance = new FeatureWiseEditDistance();
// initialiseDistancesUsed();
//
// errorExists = false;
// this.lookPastType = lookPastType;
//
// LinkedHashMap<Integer, Pair<String, Double>> editDistancesMapUnsortedFullCand;
// // HJEditDistance dummy = new HJEditDistance();
// System.out.println("\n----------------Starting Recommender MasterMU " + lookPastType + "---------------------");
// this.matchingUnitInCountsOrHours = matchingUnitInCountsOrHours;
//
// this.hasCandidateTimelines = true;
// this.nextActivityJustAfterRecommPointIsInvalid = false;
//
// String[] splittedDate = dateAtRecomm.split("/"); // dd/mm/yyyy
// this.dateAtRecomm = new Date(Integer.parseInt(splittedDate[2]) - 1900, Integer.parseInt(splittedDate[1]) - 1,
// Integer.parseInt(splittedDate[0]));
// String[] splittedTime = timeAtRecomm.split(":"); // hh:mm:ss
// this.timeAtRecomm = Time.valueOf(timeAtRecomm);
// this.userAtRecomm = Integer.toString(userAtRecomm);
// this.userIDAtRecomm = Integer.toString(userAtRecomm);
// this.caseType = caseType;
//
// System.out.println(" User at Recomm = " + this.userAtRecomm);
// System.out.println(" Date at Recomm = " + this.dateAtRecomm);
// System.out.println(" Time at Recomm = " + this.timeAtRecomm);
//
// this.trainingTimeline = new Timeline(trainingTimelines); // converting day timelines into continuous timelines
// // suitable to be used for matching unit
// // views
// this.testTimeline = new Timeline(testTimelines);
//
// if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
// {
// this.trainingTimeline = TimelineUtils.expungeInvalids(trainingTimeline);
// this.testTimeline = TimelineUtils.expungeInvalids(testTimeline);
//
// System.out.println(
// "Expunging invalids before recommendation process: expunging from test and training timelines");
// }
//
// // //////////////////
// if (lookPastType.equalsIgnoreCase("Count"))
// {
// this.currentTimeline = TimelineWithNext.getCurrentTimelineFromLongerTimelineMUCount(testTimeline,
// this.dateAtRecomm, this.timeAtRecomm, this.userIDAtRecomm, this.matchingUnitInCountsOrHours);
// }
//
// else if (lookPastType.equalsIgnoreCase("Hrs"))
// {
// this.currentTimeline = TimelineWithNext.getCurrentTimelineFromLongerTimelineMUHours(testTimeline,
// this.dateAtRecomm, this.timeAtRecomm, this.userIDAtRecomm, this.matchingUnitInCountsOrHours);
// }
// else
// {
// System.err.println("Error: Unrecognised lookPastType in RecommendationMasterMUCount");
// System.exit(-154);
// }
// // ////////////////////
// if (currentTimeline == null)
// {
// System.err.println("Error: current timeline is empty");
// errorExists = true;
// }
//
// this.activitiesGuidingRecomm = currentTimeline.getActivityObjectsInTimeline(); // CURRENT TIMELINE
// System.out.println("Current timeline: " + currentTimeline.getActivityObjectNamesWithTimestampsInSequence() + "="
// + this.activitiesGuidingRecomm.size());
// System.out.print("Activities in the current timeline (as activities guiding recomm) are: \t");
// for (int i = 0; i < activitiesGuidingRecomm.size(); i++)
// {
// System.out.print(activitiesGuidingRecomm.get(i).getActivityName() + " ");
// }
//
// System.out.println("\nDebug note192: getActivityNamesGuidingRecommwithTimestamps() "
// + getActivityNamesGuidingRecommwithTimestamps() + "\n size of current timeline="
// + currentTimeline.getActivityObjectsInTimeline().size());
//
// this.activityAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1); // current
// // ActivityObject
// this.activityNameAtRecommPoint = this.activityAtRecommPoint.getActivityName();
//
// System.out.println("\nActivity at Recomm point (Current Activity) =" + activityNameAtRecommPoint);//
// this.activityAtRecommPoint.getActivityName()
//
// // All check OK
// // //////////////////////////
// this.candidateTimelines = getCandidateTimelinesMU(trainingTimeline, matchingUnitInCountsOrHours, lookPastType);//
// ,this.dateAtRecomm);//,this.activitiesGuidingRecomm
//
// // ///////////////////////////
// System.out.println("Number of candidate timelines =" + candidateTimelines.size());
// // $$System.out.println("the candidate timelines are as follows:");
// // $$traverseMapOfTimelinesWithNext(candidateTimelines);
//
// if (candidateTimelines.size() == 0)
// {
// System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
// + " at time:" + timeAtRecomm + " because there are no candidate timelines");
// // this.singleNextRecommendedActivity = null;
// this.hasCandidateTimelines = false;
// // this.topNextActivities =null;
// this.topNextActivityObjects = null;
// this.thresholdPruningNoEffect = true;
// return;
// }
// else
// {
// this.hasCandidateTimelines = true;
// }
// // System.out.println("\nDebug note192_223: getActivityNamesGuidingRecommwithTimestamps() " +
// // getActivityNamesGuidingRecommwithTimestamps() +
// // " size of current timeline="
// // + currentTimeline.getActivityObjectsInTimeline().size());
// // /////////////////////
// // TODO CHECK: HOW THE EFFECT OF THIS DIFFERS FROM THE EXPERIMENTS DONE FOR IIWAS: in iiWAS normalisation was
// // after thresholding (correct), here
// // normalisation is before thresholding which should be changed
// // TODO
// editDistancesMapUnsortedFullCand = getNormalisedDistancesForCandidateTimelinesFullCand(candidateTimelines,
// activitiesGuidingRecomm, caseType, this.userIDAtRecomm, this.dateAtRecomm.toString(),
// this.timeAtRecomm.toString(), Constant.getDistanceUsed());
//
// /*
// * Old getHJEditDistancesForCandidateTimelinesFullCand(candidateTimelines, activitiesGuidingRecomm, caseType,
// * this.userIDAtRecomm, this.dateAtRecomm.toString(), this.timeAtRecomm.toString());
// */
//
// // getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
// // getNormalisedDistancesForCandidateTimelinesFullCand(candidateTimelines, activitiesGuidingRecomm, caseType,
// // this.userIDAtRecomm,
// // this.dateAtRecomm.toString(),
// // this.timeAtRecomm.toString(), Constant.getDistanceUsed());//
// // getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
//
// // //////////////////////////////////
// // System.out.println("\nDebug note192_229: getActivityNamesGuidingRecommwithTimestamps() " +
// // getActivityNamesGuidingRecommwithTimestamps() +
// // " size of current timeline="
// // + currentTimeline.getActivityObjectsInTimeline().size());
// // ########Sanity check
// if (editDistancesMapUnsortedFullCand.size() != candidateTimelines.size())
// {
// System.err.println(
// "Error at Sanity 261 inside RecommendationMasterMU: editDistancesMapUnsorted.size() !=
// candidateTimelines.size()");
// errorExists = true;
// }
// // ##############
//
// // /// REMOVE candidate timelines which are above the distance THRESHOLD. (actually here we remove the entry for
// // such candidate timelines from the
// // distance scores map
// if (typeOfThreshold.equalsIgnoreCase("Global"))
// {
// this.thresholdAsDistance = thresholdVal;
// }
// else if (typeOfThreshold.equalsIgnoreCase("Percent"))
// {
// double maxEditDistance = (new AlignmentBasedDistance()).maxEditDistance(activitiesGuidingRecomm);
// this.thresholdAsDistance = maxEditDistance * (thresholdVal / 100);
// }
// else
// {
// System.err.println("Error: type of threshold unknown in recommendation master");
// errorExists = true;
// System.exit(-2);
// }
// // System.out.println("\nDebug note192_255: getActivityNamesGuidingRecommwithTimestamps() " +
// // getActivityNamesGuidingRecommwithTimestamps() +
// // " size of current timeline="
// // + currentTimeline.getActivityObjectsInTimeline().size());
// int countCandBeforeThresholdPruning = editDistancesMapUnsortedFullCand.size();// distanceScoresSorted.size();
//
// editDistancesMapUnsortedFullCand = TimelineUtils
// .removeAboveThreshold4FullCandISD(editDistancesMapUnsortedFullCand, thresholdAsDistance);// distanceScoresSorted=
// // UtilityBelt.removeAboveThreshold2(distanceScoresSorted,thresholdAsDistance);
// int countCandAfterThresholdPruning = editDistancesMapUnsortedFullCand.size();
//
// this.thresholdPruningNoEffect = (countCandBeforeThresholdPruning == countCandAfterThresholdPruning);
// // System.out.println("\nDebug note192_263: getActivityNamesGuidingRecommwithTimestamps() " +
// // getActivityNamesGuidingRecommwithTimestamps() +
// // " size of current timeline="
// // + currentTimeline.getActivityObjectsInTimeline().size());
// if (!thresholdPruningNoEffect)
// {
// PopUps.showMessage("Ohh..threshold pruning is happening. Are you sure you wanted this?");// +msg);
// }
// // ////////////////////////////////
//
// if (editDistancesMapUnsortedFullCand.size() == 0)
// {
// System.out.println("Warning: No candidate timelines below threshold distance");
// hasCandidateTimelinesBelowThreshold = false;
// return;
// }
//
// else
// {
// hasCandidateTimelinesBelowThreshold = true;
// }
//
// // //////////////////////////////
// // System.out.println("\nDebug note192_282: getActivityNamesGuidingRecommwithTimestamps() " +
// // getActivityNamesGuidingRecommwithTimestamps() +
// // " size of current timeline="
// // + currentTimeline.getActivityObjectsInTimeline().size());
// editDistancesSortedMapFullCand = (LinkedHashMap<Integer, Pair<String, Double>>) ComparatorUtils
// .sortByValueAscendingIntStrDoub(editDistancesMapUnsortedFullCand); // Now distanceScoresSorted
// // contains the String Id for
//
// if (caseType.equals("CaseBasedV1"))
// {
// System.out.println("this is CaseBasedV1");
// // this.editDistanceOfEndPointActivityObjectCand =
// //
// getEditDistancesOfEndPointActivityObjectCand(candidateTimelines,activitiesGuidingRecomm,caseType);//getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
// this.similarityOfEndPointActivityObjectCand = getCaseSimilarityEndPointActivityObjectCand(
// candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, this.dateAtRecomm.toString(),
// this.timeAtRecomm.toString());//
// getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
// }
//
// this.topNextActivityObjects = fetchNextActivityObjects(editDistancesSortedMapFullCand, candidateTimelines);
//
// System.out.println("---------editDistancesSortedMapFullCand.size()=" + editDistancesSortedMapFullCand.size());
//
// if (Constant.verbose)
// {
// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
//
// System.out.println("\n" + "\n The candidate timelines in increasing order of distance are:");
// traverseCandidateTimelineWithEditDistance();// editDistancesSortedMapFullCand);
//
// System.out.println("\nTop next activities are: ");// +this.topNextRecommendedActivities);
// traverseTopNextActivities();
// }
// // System.out.println("\nDebug note192_308: getActivityNamesGuidingRecommwithTimestamps() " +
// // getActivityNamesGuidingRecommwithTimestamps() +
// // " size of current timeline="
// // + currentTimeline.getActivityObjectsInTimeline().size());
// // ########Sanity check
// if (this.topNextActivityObjects.size() == editDistancesSortedMapFullCand.size()
// && editDistancesSortedMapFullCand.size() == this.candidateTimelines.size())
// {
// // System.out.println("Sanity Check 349 Passed");
// }
// else
// {
// System.err.println(
// "Error at Sanity 349 (RecommenderMasterMU: this.topNextActivityObjects.size() ==
// editDistancesSortedMapFullCand.size() && editDistancesSortedMapFullCand.size()== this.candidateTimelines.size()
// not satisfied");
// errorExists = true;
// }
//
// // ##############
// if (caseType.equals("CaseBasedV1"))
// {
// // rankScore= (1d-(editDistanceValExceptEnd/maxEditDistanceValExceptEnd))* (1d-
// // (endPointActivityEditDistanceVal/maxEditDistanceValOfEnd));
// // createRankedTopRecommendedActivityNamesCaseBasedV1(this.topNextActivityObjects,
// // this.similarityOfEndPointActivityObjectCand);
//
// // rankScore= (1d-(editDistanceValExceptEnd/maxEditDistanceValExceptEnd))* simEndPointActivityObject;
// createRankedTopRecommendedActivityNamesCaseBasedV1_3(this.topNextActivityObjects,
// this.similarityOfEndPointActivityObjectCand);
// }
//
// else
// {
// // createRankedTopRecommendedActivityNames
// createRankedTopRecommendedActivityNamesSimpleV3_3(this.topNextActivityObjects);// , this.userAtRecomm,
// // dateAtRecomm,
// // timeAtRecomm);
// }
//
// // System.out.println("Next recommended 5 activity is: "+this.topNextRecommendedActivities);
//
// /*
// * IMPORTANT: If the next activity after the current activity object in the current timeline is an invalid
// * activity, then we can include the current activity in the list of recommended activities, otherwise the
// * current activity has to be removed from the list of recommended activities
// */
// if (currentTimeline.getImmediateNextActivityInvalid() == 0) // not invalid
// {
// removeRecommPointActivityFromRankedRecomm();
// System.out.println("removing recomm point activity (Current Activity) from list of recommendation");
// }
//
// WritingToFile.writeDistanceScoresSortedMapMU(this.userAtRecomm, this.dateAtRecomm, this.timeAtRecomm,
// this.editDistancesSortedMapFullCand, this.candidateTimelines, this.topNextActivityObjects,
// this.activitiesGuidingRecomm, Constant.WriteCandInEditDistancePerRtPerCand,
// Constant.WriteEditOperatationsInEditDistancePerRtPerCand);
//
// System.out.println("\nDebug note192_end: getActivityNamesGuidingRecommwithTimestamps() "
// + getActivityNamesGuidingRecommwithTimestamps() + " size of current timeline="
// + currentTimeline.getActivityObjectsInTimeline().size());
// // System.out.println("Debug note192_2: current timeline " +
// // currentTimeline.getActivityObjectNamesInSequence());
//
// long recommMasterTEnd = System.currentTimeMillis();
//
// System.out.println("\n^^^^^^^^^^^^^^^^Exiting Recommendation Master");
// }
// End of curtain 9 march 2017

// /////////
// /

// //////////
// for case based V1
/*
 * 
 * rankScore= (1d-(editDistanceValExceptEnd/maxEditDistanceValExceptEnd))* (1d-
 * (endPointActivityEditDistanceVal/maxEditDistanceValOfEnd));
 * 
 * Generates a ranked list of recommended Activity Objects
 * 
 * setRecommendedActivityNamesRankscorePairs setRankedRecommendedActivityNamesWithRankScores
 * setRankedRecommendedActivityNamesWithoutRankScores
 * 
 * @param topNextActivityObjectsWithDistance
 */
// public void createRankedTopRecommendedActivityNamesCaseBasedV1(ArrayList<Triple<ActivityObject, Double, Integer>>
// topNextActivityObjectsWithDistance,
// LinkedHashMap<Integer, Double> editDistanceOfEndPointActivityObjectCand) // we might remove these arguments as these
// are already member variables of this
// class
// {
// String topRankedActivityNamesWithScore, topRankedActivityNamesWithoutScore;
//
// int numberOfTopNextActivityObjects = topNextActivityObjectsWithDistance.size();
//
// // System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
// topRecommendationsWithDistance="+topRecommendationsWithDistance);
// System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
// numberOfTopNextActivityEvenst=numberOfTopNextActivityEvenst");
// // System.out.print("Debug inside createRankedTopRecommendedActivityObjects: the read next activity objects are: ");
//
// LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<String, Double>(); //
// <ActivityName,RankScore>
//
// Double maxEditDistanceValExceptEnd = 0d;
// Double maxEditDistanceValOfEnd = 0d;
//
// for (int i = 0; i < numberOfTopNextActivityObjects; i++)
// {
// Double editDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();
//
// if (editDistanceVal > maxEditDistanceValExceptEnd)
// {
// maxEditDistanceValExceptEnd = editDistanceVal;
// }
// }
//
// // finding maximum of endpointEditDistance
// for (Map.Entry<Integer, Double> entry : editDistanceOfEndPointActivityObjectCand.entrySet())
// {
// Double editDistanceVal1 = entry.getValue();
//
// if (editDistanceVal1 > maxEditDistanceValOfEnd)
// {
// maxEditDistanceValOfEnd = editDistanceVal1;
// }
// }
//
// for (int i = 0; i < numberOfTopNextActivityObjects; i++)
// {
// String topNextActivityName = topNextActivityObjectsWithDistance.get(i).getFirst().getActivityName();
// Double editDistanceValExceptEnd = topNextActivityObjectsWithDistance.get(i).getSecond();
//
// Integer candTimelineID = topNextActivityObjectsWithDistance.get(i).getThird();
//
// Double endPointActivityEditDistanceVal = editDistanceOfEndPointActivityObjectCand.get(candTimelineID);
//
// Double rankScore = (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) * (1d -
// (endPointActivityEditDistanceVal / maxEditDistanceValOfEnd));
//
// System.out.println("RANK SCORE CALCULATION=" + "(1d-(" + editDistanceValExceptEnd + "/" + maxEditDistanceValExceptEnd
// + "))* (1d- (" +
// endPointActivityEditDistanceVal + "/"
// + maxEditDistanceValOfEnd + "))");
//
// if (recommendedActivityNamesRankscorePairs.containsKey(topNextActivityName) == false)
// {
// recommendedActivityNamesRankscorePairs.put(topNextActivityName, rankScore);
// }
//
// else
// {
// recommendedActivityNamesRankscorePairs.put(topNextActivityName,
// recommendedActivityNamesRankscorePairs.get(topNextActivityName) + rankScore);
// }
// }
//
// System.out.println();
//
// recommendedActivityNamesRankscorePairs = (LinkedHashMap<String, Double>)
// UtilityBelt.sortByValue(recommendedActivityNamesRankscorePairs); // Sorted in
// descending order of ranked score:
// higher
// // ranked score means
// // higher value of rank
//
// // ///////////IMPORTANT //////////////////////////////////////////////////////////
// this.setRecommendedActivityNamesWithRankscores(recommendedActivityNamesRankscorePairs);
//
// this.setRankedRecommendedActivityNamesWithRankScores(recommendedActivityNamesRankscorePairs);
// this.setRankedRecommendedActivityNamesWithoutRankScores(recommendedActivityNamesRankscorePairs);
//
// // /////////////////////////////////////////////////////////////////////
//
// topRankedActivityNamesWithScore = getRankedRecommendedActivityNamesWithRankScores();
// topRankedActivityNamesWithoutScore = getRankedRecommendedActivityNamesWithoutRankScores();
// System.out.println("Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithScore= " +
// topRankedActivityNamesWithScore);
// System.out.println("Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithoutScore= " +
// topRankedActivityNamesWithoutScore);
//
// // return topRankedString;
// }

// /////////

// *

/*
 * Generates a ranked list of recommended Activity Objects
 * 
 * setRecommendedActivityNamesRankscorePairs setRankedRecommendedActivityNamesWithRankScores
 * setRankedRecommendedActivityNamesWithoutRankScores
 * 
 * @param topNextActivityObjectsWithDistance
 */
// public void createRankedTopRecommendedActivityNames(ArrayList<Triple<ActivityObject, Double, Integer>>
// topNextActivityObjectsWithDistance)
// {
// String topRankedActivityNamesWithScore, topRankedActivityNamesWithoutScore;
//
// int numberOfTopNextActivityObjects = topNextActivityObjectsWithDistance.size();
//
// // System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
// topRecommendationsWithDistance="+topRecommendationsWithDistance);
// System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
// numberOfTopNextActivityEvenst=numberOfTopNextActivityEvenst");
// // System.out.print("Debug inside createRankedTopRecommendedActivityObjects: the read next activity objects are: ");
//
// LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<String, Double>(); //
// <ActivityName,RankScore>
//
// Double maxEditDistanceVal = 0d;
//
// for (int i = 0; i < numberOfTopNextActivityObjects; i++)
// {
// Double editDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();
//
// if (editDistanceVal > maxEditDistanceVal)
// {
// maxEditDistanceVal = editDistanceVal;
// }
// }
//
// for (int i = 0; i < numberOfTopNextActivityObjects; i++)
// {
// String topNextActivityName = topNextActivityObjectsWithDistance.get(i).getFirst().getActivityName();
// Double editDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();
//
// Double rankScore = 1d - (editDistanceVal / maxEditDistanceVal);
//
// if (recommendedActivityNamesRankscorePairs.containsKey(topNextActivityName) == false)
// {
// recommendedActivityNamesRankscorePairs.put(topNextActivityName, rankScore);
// }
//
// else
// {
// recommendedActivityNamesRankscorePairs.put(topNextActivityName,
// recommendedActivityNamesRankscorePairs.get(topNextActivityName) + rankScore);
// }
// }
//
// System.out.println();
//
// recommendedActivityNamesRankscorePairs = (LinkedHashMap<String, Double>)
// UtilityBelt.sortByValue(recommendedActivityNamesRankscorePairs); // Sorted in
// descending order of ranked score:
// higher
// // ranked score means
// // higher value of rank
//
// // ///////////IMPORTANT //////////////////////////////////////////////////////////
// this.setRecommendedActivityNamesWithRankscores(recommendedActivityNamesRankscorePairs);
//
// this.setRankedRecommendedActivityNamesWithRankScores(recommendedActivityNamesRankscorePairs);
// this.setRankedRecommendedActivityNamesWithoutRankScores(recommendedActivityNamesRankscorePairs);
//
// // /////////////////////////////////////////////////////////////////////
//
// topRankedActivityNamesWithScore = getRankedRecommendedActivityNamesWithRankScores();
// topRankedActivityNamesWithoutScore = getRankedRecommendedActivityNamesWithoutRankScores();
// System.out.println("Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithScore= " +
// topRankedActivityNamesWithScore);
// System.out.println("Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithoutScore= " +
// topRankedActivityNamesWithoutScore);
//
// // return topRankedString;
// }

//
// // ////////
// /*
// * Added: Oct 7, 2014: for IMPORTANT POINT: THE CANDIDATE TIMELINE IS THE DIRECT CANDIDATE TIMELINE AND NOT THE LEAST
// DISTANT SUBCANDIDATE.
// */
// /**
// * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the edit distance
// value between the end point activity object
// (current activity) of the candidate
// * timeline and the current activity object (activity at recomm point)
// *
// * @param candidateTimelines
// * @param activitiesGuidingRecomm
// * @param caseType
// * can be 'SimpleV3' or 'CaseBasedV1'
// * @return <CanditateTimelineID, edit distance of this end point Activity Object of this candidate with end point
// Activity Object>
// */
// public LinkedHashMap<Integer, Double> getEditDistancesOfEndPointActivityObjectCand(LinkedHashMap<Integer,
// TimelineWithNext> candidateTimelines,
// ArrayList<ActivityObject>
// activitiesGuidingRecomm,
// String caseType)
// {
// // <CandidateTimeline ID, Edit distance>
// LinkedHashMap<Integer, Double> candEndPointEditDistances = new LinkedHashMap<Integer, Double>();
//
// for (Map.Entry<Integer, TimelineWithNext> entry : candidateTimelines.entrySet())
// {
// TimelineWithNext candInConcern = entry.getValue();
// ArrayList<ActivityObject> activityObjectsInCand = candInConcern.getActivityObjectsInTimeline();
//
// // EditSimilarity editSimilarity = new EditSimilarity();
// Double endPointEditDistanceForThisCandidate;
// if (caseType.equals("SimpleV3"))
// {
// // editDistanceForThisCandidate =
// editSimilarity.getEditDistance(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
// System.err.println("ERROR in getEditDistancesOfEndPointActivityCand(): This method should not have been called for
// case type=" + caseType);
// errorExists = true;
// endPointEditDistanceForThisCandidate = null;
// }
// else
// // CaseBasedV1
// {
// ArrayList<ActivityObject> endPointActivityObjectCandidate = new ArrayList<ActivityObject>();
// endPointActivityObjectCandidate.add(activityObjectsInCand.get(activityObjectsInCand.size() - 1)); // only the end
// point activity object
//
// ArrayList<ActivityObject> endPointActivityObjectCurrentTimeline = new ArrayList<ActivityObject>();
// endPointActivityObjectCurrentTimeline.add(activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1)); //
// activityObjectAtRecommPoint
//
// endPointEditDistanceForThisCandidate = editSimilarity.getEditDistance(endPointActivityObjectCandidate,
// endPointActivityObjectCurrentTimeline);
// }
//
// candEndPointEditDistances.put(entry.getKey(), endPointEditDistanceForThisCandidate);
// // System.out.println("now we put "+entry.getKey()+" and score="+score);
// }
//
// return candEndPointEditDistances;
// }
