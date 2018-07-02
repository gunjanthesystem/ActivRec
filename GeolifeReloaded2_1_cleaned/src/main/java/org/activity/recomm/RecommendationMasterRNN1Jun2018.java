package org.activity.recomm;

//import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
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
import org.activity.constants.Enums.TypeOfThreshold;
import org.activity.constants.VerbosityConstants;
import org.activity.distances.AlignmentBasedDistance;
import org.activity.evaluation.Evaluation;
import org.activity.nn.BasicRNNWC2_SeqRec2018;
import org.activity.nn.LSTMCharModelling_SeqRecJun2018;
import org.activity.nn.NNUtils;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.objects.Triple;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.StringUtils;
import org.activity.util.TimelineExtractors;
import org.activity.util.TimelineTransformers;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

/**
 * 
 * <p>
 * Fork of RecommendationMasterMar2017AltAlgoSeqNov2017.java (removing, relocation methods) which was fork of
 * org.activity.recomm.RecommendationMasterMar2017GenSeq.RecommendationMasterMar2017GenSeq() for implementing
 * alternative sequence prediction algorithms
 * <p>
 * ALERT!: DO NOT USER THRESHOLDING WITH THIS CLASS since here the edit distances are normalised before thredholding,
 * however, they should have been normalised after thresholding as was the case in iiWAS version of the code This
 * generates recommendations using the matching-unit (MU) approach. Timelines are not viewed as Day Timeline but rather
 * as continuous timelines. The matching-unit can be specified as 'hours' or 'counts'.
 * 
 * Timelines are sets of Activity Objects, (and they do not necessarily belong to one day.) (the matching unit used here
 * HJ version) In this version we calculate edit distance of the full candidate and not the least distant subcandidate
 * 
 * @since 02 May, 2017
 * @author gunjan
 */
public class RecommendationMasterRNN1Jun2018 implements RecommendationMasterI// IRecommenderMaster
{
	private double matchingUnitInCountsOrHours;
	private double reductionInMatchingUnit = 0;

	// private Timeline trainingTimeline;
	// private Timeline testTimeline;
	// in case of MU approach {String, TimelineWithNext}, in case if daywise approach {Date as String, Timeline}
	private LinkedHashMap<String, Timeline> candidateTimelines;
	// here key is the TimelineID, which is already a class variable of Value,
	// So we could have used ArrayList but we used LinkedHashMap purely for search performance reasons

	private Date dateAtRecomm;
	private Time timeAtRecomm;
	private String userAtRecomm;
	private String userIDAtRecomm;
	private LinkedHashMap<String, String> candUserIDs;
	/**
	 * Current Timeline sequence of activity objects happening from the recomm point back until the matching unit
	 */
	private TimelineWithNext currentTimeline; // =current timelines
	private ArrayList<ActivityObject> activitiesGuidingRecomm; // Current Timeline ,
	private ActivityObject activityObjectAtRecommPoint; // current Activity Object
	// private String activityNameAtRecommPoint;// current Activity Name
	private ArrayList<Integer> primaryDimensionValAtRecommPoint;// when activity is primary dimension, this is an
																// activity id, when
	// location is the primary dimension, this is a list of location ids
	// (>1 if this is a merged object)
	/**
	 * {Cand TimelineID, Pair{Trace,Edit distance}} this LinkedHashMap is sorted by the value of edit distance in
	 * ascending order://formerly: editDistancesSortedMapFullCand
	 */
	private LinkedHashMap<String, Pair<String, Double>> distancesSortedMap;

	/**
	 * Relevant for daywise approach: the edit distance is computed for least distance subsequence in case current
	 * activity name occurs at multiple times in a day timeline {Cand TimelineID, End point index of least distant
	 * subsequence}
	 */
	private LinkedHashMap<String, Integer> endPointIndicesConsideredInCands;

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
	private LinkedHashMap<String, Pair<ActivityObject, Double>> nextActivityObjectsFromCands;

	/**
	 * This is only relevant when case type is 'CaseBasedV1' (Cand TimeineId, Edit distance of the end point activity
	 * object of this candidate timeline with the current activity object(activity at recomm point))
	 * <p>
	 * <font color = orange>currently its a similarity and not edit distance</font>
	 */
	private LinkedHashMap<String, Double> similarityOfEndPointActivityObjectCand;

	/**
	 * Recommended Activity names with their rank score, (earlier used to be LinkedHashMap<String, Double>>)
	 */
	private ArrayList<LinkedHashMap<String, Double>> recommendedActivityNamesWithRankscores;
	private ArrayList<String> rankedRecommendedActNamesWithRankScoresStr;
	private ArrayList<String> rankedRecommendedActNamesWithoutRankScoresStr;

	private boolean hasCandidateTimelines, hasCandidateTimelinesBelowThreshold;

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

	//////////////// Start of Added on 15 June 2018
	private static final boolean doesSeqRecIteratively = false;

	public static boolean getDoesSeqRecIteratively()
	{
		return doesSeqRecIteratively;
	}

	private int indexOfRecommSeq;

	public void setIndexOfRecommSeq(int i)
	{
		this.indexOfRecommSeq = i;
	}

	public int getIndexOfRecommSeq()
	{
		return indexOfRecommSeq;
	}

	//////////////// End of Added on 15 June 2018

	public LinkedHashMap<String, String> getCandUserIDs()
	{
		return candUserIDs;
	}

	/**
	 * 
	 * @param trainingTimelines
	 * @param testTimelines
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userAtRecomm
	 * @param thresholdVal
	 * @param typeOfThreshold
	 * @param matchingUnitInCountsOrHours
	 * @param caseType
	 * @param lookPastType
	 * @param dummy
	 * @param actObjsToAddToCurrentTimeline
	 * @param trainTestTimelinesForAllUsersDW
	 * @param trainTimelinesAllUsersContinuous
	 * @param altSeqPredictor
	 * @param recommSeqLength
	 */
	public RecommendationMasterRNN1Jun2018(LinkedHashMap<Date, Timeline> trainingTimelines,
			LinkedHashMap<Date, Timeline> testTimelines, String dateAtRecomm, String timeAtRecomm, int userAtRecomm,
			double thresholdVal, Enums.TypeOfThreshold typeOfThreshold, Enums.CaseType caseType,
			Enums.LookPastType lookPastType, boolean dummy, ArrayList<ActivityObject> actObjsToAddToCurrentTimeline,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous, Enums.AltSeqPredictor altSeqPredictor,
			int recommSeqLength)
	{
		// PopUps.showMessage("called RecommendationMasterMar2017GenSeq");
		try
		{
			System.out
					.println("\n-----------Starting RecommendationMasterRNN1Jun2018 " + lookPastType + "-------------");

			String performanceFileName = Constant.getCommonPath() + "Performance.csv";
			long recommMasterT0 = System.currentTimeMillis();

			// initialiseDistancesUsed(Constant.getDistanceUsed());
			// editDistancesMemorizer = new EditDistanceMemorizer();
			this.lookPastType = lookPastType;
			// this.caseType = caseType;

			// if (!lookPastType.equals(LookPastType.Daywise) && !lookPastType.equals(LookPastType.ClosestTime)){
			// this.matchingUnitInCountsOrHours = matchingUnitInCountsOrHoursPassed;
			// }else if (lookPastType.equals(LookPastType.NGram)
			// || Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM))
			// {this.matchingUnitInCountsOrHours = 0;}
			errorExists = false;

			this.hasCandidateTimelines = true;
			this.nextActivityJustAfterRecommPointIsInvalid = false;

			// dd/mm/yyyy // okay java.sql.Date with no hidden time
			this.dateAtRecomm = DateTimeUtils.getDateFromDDMMYYYY(dateAtRecomm, RegexUtils.patternForwardSlash);
			this.timeAtRecomm = Time.valueOf(timeAtRecomm);
			this.userAtRecomm = Integer.toString(userAtRecomm);
			this.userIDAtRecomm = Integer.toString(userAtRecomm);
			System.out.println("	User at Recomm = " + this.userAtRecomm + "\tDate at Recomm = " + this.dateAtRecomm
					+ "\tTime at Recomm = " + this.timeAtRecomm + "\tmu=" + matchingUnitInCountsOrHours + "\n");// this.matchingUnitInCountsOrHours="
			// + this.matchingUnitInCountsOrHours);/

			//////
			Pair<TimelineWithNext, Double> extCurrTimelineRes = null;

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

			System.out.println("Current timeline = " + this.currentTimeline.getActivityObjectNamesInSequence());
			System.out.println("Current timelines.size() = " + this.currentTimeline.size());
			//////

			this.activitiesGuidingRecomm = currentTimeline.getActivityObjectsInTimeline(); // CURRENT TIMELINE

			// if (actObjsToAddToCurrentTimeline.size() > 0 && this.lookPastType == LookPastType.NCount)
			// {
			// if (reductionInMatchingUnit == 0 && activitiesGuidingRecomm.size() <= matchingUnitInCountsOrHours)
			// {
			// System.err.println("Error: actsGuidingRecomm.size():" + this.activitiesGuidingRecomm.size() + "<=MU"
			// + matchingUnitInCountsOrHours + " (even without reduced mu");
			// }
			// else if (activitiesGuidingRecomm.size() <= matchingUnitInCountsOrHours)
			// {
			// System.out.println("Warning: actsGuidingRecomm.size():" + this.activitiesGuidingRecomm.size()
			// + "<=MU" + matchingUnitInCountsOrHours + " (with mu reduction =" + reductionInMatchingUnit
			// + ")");
			// }
			// }

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

			/////////////////////////////
			long recommMasterT1 = System.currentTimeMillis();
			// System.out.println("mu in master= " + matchingUnitInCountsOrHours);
			// System.out.println("Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM) = "
			// + Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM));

			System.out.println("NO CAND EXTRACTION!");
			this.candidateTimelines = new LinkedHashMap<>(trainTimelinesAllUsersContinuous);

			if (Constant.sameRNNForALLUsers == true)
			{
				System.out.println(
						"Since sameRNNForALLUsers is TRUE, not removing the current users's training timeline from cands.");
			}
			else // then REMOVE THE CURRENT USER'S TRAINING TIMELINE FROM CANDS
			{
				// Only removing the current user's data from candidate.
				Timeline removedCandCurrUser = candidateTimelines.remove(userIDAtRecomm);
				if (removedCandCurrUser != null)
				{
					System.out.println("Removed userIDAtRecomm from cands");
				}
				else
				{
					PopUps.showError("userIDAtRecomm:" + userIDAtRecomm
							+ " supposed to be removed from cands was not in cands or had null value.");
				}
				// trainTimelinesAllUsersContinuous;//
			}

			// if (VerbosityConstants.verbose)
			{
				String s1 = "Inside recomm master :trainTestTimelinesForAllUsers.size()= "
						+ trainTestTimelinesForAllUsersDW.size() + " trainTimelinesAllUsersContinuous.size()="
						+ trainTimelinesAllUsersContinuous.size() + "candTimelines.size()= "
						+ candidateTimelines.size();
				System.out.println(s1);
			}

			// if (true)
			// {
			// StringBuilder sb = new StringBuilder("Candidate timelines:\n");
			// candidateTimelines.entrySet().stream()
			// .forEachOrdered(e -> sb.append(e.getValue().getActivityObjectNamesInSequence() + "\n"));
			// System.out.println("-----------");
			// }

			long recommMasterT2 = System.currentTimeMillis();
			candUserIDs = extractCandUserIDs(candidateTimelines);
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

				System.out.println("Number of candidate timelines =" + candidateTimelines.size());
				System.out.println("the candidate timelines are as follows:");
				candidateTimelines.entrySet().stream()
						.forEach(t -> System.out.println(t.getValue().getPrimaryDimensionValsInSequence()));
				// getActivityObjectNamesInSequence()));
			}

			// Start of curtain Dec 6
			// int lengthOfLongestCand = candidateTimelines.entrySet().parallelStream().mapToInt(e ->
			// e.getValue().size()) .max().getAsInt(); int maxLength = 0;
			// for (Timeline t : candidateTimelines.values())
			// { if (t.size() > maxLength) { maxLength = t.size(); } }
			// End of curtain Dec 6

			if (candidateTimelines.size() == 0)
			{
				System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
						+ " at time:" + timeAtRecomm + "  because there are no candidate timelines");
				// this.singleNextRecommendedActivity = null;
				this.hasCandidateTimelines = false;
				// this.topNextActivities =null;
				this.nextActivityObjectsFromCands = null;
				this.thresholdPruningNoEffect = true;
				return;
			}
			// Curtain Dec 6 start
			// else if (lengthOfLongestCand < Constant.AKOMHighestOrder)
			// {
			// System.out.println(
			// "Warning JUJU: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
			// + " at time:" + timeAtRecomm + " because lengthOfLongestCand " + lengthOfLongestCand
			// + "< Constant.AKOMHighestOrder" + Constant.AKOMHighestOrder + "\nmaxLength=" + maxLength
			// + " matchingUnitInCountsOrHours =" + this.matchingUnitInCountsOrHours);
			// // this.singleNextRecommendedActivity = null;
			// for (Timeline t : candidateTimelines.values())
			// {
			// System.out.print("\tlength of cand = " + t.size());
			//
			// }
			// // System.exit(0);
			//
			// this.hasCandidateTimelines = false;
			// // this.topNextActivities =null;
			// this.nextActivityObjectsFromCands = null;
			// this.thresholdPruningNoEffect = true;
			// return;
			// }
			// Curtain Dec 6 end
			else
			{
				// System.out.println("Eureka");
				this.hasCandidateTimelines = true;
			}
			// System.out.println("\nDebug note192_223: getActivityNamesGuidingRecommwithTimestamps() " +
			// getActivityNamesGuidingRecommwithTimestamps() +
			// " size of current timeline="
			// + currentTimeline.getActivityObjectsInTimeline().size());
			// /////////////////////
			// CHECK: HOW THE EFFECT OF THIS DIFFERS FROM THE EXPERIMENTS DONE FOR IIWAS: in iiWAS normalisation
			// was after thresholding (correct), here
			// normalisation is before thresholding which should be changed
			// long recommMasterT3 = System.currentTimeMillis();
			// Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>>
			// normalisedDistFromCandsRes = null;
			// Curtain not relevant 1 start
			// = getNormalisedDistancesForCandidateTimelines(
			// candidateTimelines, activitiesGuidingRecomm, caseType, this.userIDAtRecomm, this.dateAtRecomm,
			// this.timeAtRecomm, Constant.getDistanceUsed(), this.lookPastType, this.hjEditDistance,
			// this.featureWiseEditDistance, this.featureWiseWeightedEditDistance, this.OTMDSAMEditDistance,
			// this.editDistancesMemorizer);
			// Curtain not relevant 1 end
			// editDistancesMemorizer.serialise(this.userIDAtRecomm);

			// LinkedHashMap<String, Pair<String, Double>> distancesMapUnsorted = null;
			// curtain not relevant: normalisedDistFromCandsRes.getFirst();
			this.endPointIndicesConsideredInCands = null;// curtain not
															// relevant:normalisedDistFromCandsRes.getSecond();

			// long recommMasterT4 = System.currentTimeMillis();
			// long timeTakenToComputeNormEditDistances = recommMasterT4 - recommMasterT3;

			// System.out.println("\nDebug note192_229: getActivityNamesGuidingRecommwithTimestamps() " +
			// getActivityNamesGuidingRecommwithTimestamps() +" size of current timeline=" +
			// currentTimeline.getActivityObjectsInTimeline().size());
			// ########Sanity check
			// curtain not relevant: start
			// if (distancesMapUnsorted.size() != candidateTimelines.size())
			// {
			// if (Constant.filterTopCands > 0) // not expected when filtering is to be done
			// {
			// System.out.println("Alert: editDistancesMapUnsorted.size() (" + distancesMapUnsorted.size()
			// + ") != candidateTimelines.size() (" + candidateTimelines.size() + ")");
			// }
			// else
			// {
			// PopUps.printTracedErrorMsg("editDistancesMapUnsorted.size() (" + distancesMapUnsorted.size()
			// + ") != candidateTimelines.size() (" + candidateTimelines.size() + ")");
			// String distancesMapUnsortedAsString = distancesMapUnsorted.entrySet().stream()
			// .map(e -> e.getKey() + " - " + e.getValue().getFirst() + "_" + e.getValue().getSecond())
			// .collect(Collectors.joining("\n"));
			// String candidateTimelinesAsString = candidateTimelines.entrySet().stream().map(
			// e -> e.getKey() + " - " + e.getValue().getActivityObjectPDValsWithTimestampsInSequence())
			// // .getActivityObjectNamesWithTimestampsInSequence())
			// .collect(Collectors.joining("\n"));
			//
			// WritingToFile.appendLineToFileAbsolute(
			// "User = " + this.userIDAtRecomm + "\ndistancesMapUnsortedAsString =\n"
			// + distancesMapUnsortedAsString + "\n\n candidateTimelinesAsString =\n"
			// + candidateTimelinesAsString,
			// Constant.getCommonPath() + "ErrorLog376distancesMapUnsorted.txt");
			// errorExists = true;
			// }
			// }
			// curtain not relevant: end
			// ##############

			// curtain not relevant 2: start
			// // /// REMOVE candidate timelines which are above the distance THRESHOLD. (actually here we remove the
			// entry
			// // for such candidate timelines from the distance scores map. // no pruning for baseline closest ST
			// if (this.lookPastType.equals(Enums.LookPastType.ClosestTime) == false && Constant.useThreshold == true)
			// {// changed from "Constant.useThreshold ==false)" on May 10 but should not affect result since we were
			// not
			// // doing thresholding anyway
			// Triple<LinkedHashMap<String, Pair<String, Double>>, Double, Boolean> prunedRes = pruneAboveThreshold(
			// distancesMapUnsorted, typeOfThreshold, thresholdVal, activitiesGuidingRecomm);
			// distancesMapUnsorted = prunedRes.getFirst();
			// this.thresholdAsDistance = prunedRes.getSecond();
			// this.thresholdPruningNoEffect = prunedRes.getThird();
			// }
			// // curtain not relevant: end
			// // ////////////////////////////////
			//
			// if (distancesMapUnsorted.size() == 0)
			// {
			// System.out.println("Warning: No candidate timelines below threshold distance");
			// hasCandidateTimelinesBelowThreshold = false;
			// return;
			// }
			// else
			// {
			hasCandidateTimelinesBelowThreshold = true;
			// }
			//
			// // Is this sorting necessary?
			// // Disabling on Aug 3
			// if (Constant.filterTopCands <= 0) // because otherwise already sorted while filtering
			// {
			// distancesSortedMap = (LinkedHashMap<String, Pair<String, Double>>) ComparatorUtils
			// .sortByValueAscendingStrStrDoub(distancesMapUnsorted);
			// }
			// else
			// {// because already sorted while filtering
			// distancesSortedMap = distancesMapUnsorted;
			// }
			// if (caseType.equals(Enums.CaseType.CaseBasedV1))
			// {
			// System.out.println("this is CaseBasedV1");
			// this.similarityOfEndPointActivityObjectCand = getCaseSimilarityEndPointActivityObjectCand(
			// candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm,
			// this.dateAtRecomm.toString(), this.timeAtRecomm.toString(), alignmentBasedDistance);//
			// getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
			// }
			//
			// this.nextActivityObjectsFromCands = fetchNextActivityObjects(distancesSortedMap, candidateTimelines,
			// this.lookPastType, endPointIndicesConsideredInCands);
			//
			// if (!this.lookPastType.equals(Enums.LookPastType.ClosestTime))
			// {
			// if (!Sanity.eq(this.nextActivityObjectsFromCands.size(), distancesSortedMap.size(),
			// "Error at Sanity 349 (RecommenderMaster: this.topNextActivityObjects.size()"
			// + nextActivityObjectsFromCands.size() + "!= distancesSortedMap.size():"
			// + distancesSortedMap.size()))
			// {
			// errorExists = true;
			// }
			// // Disabled logging for performance
			// // System.out.println("this.nextActivityObjectsFromCands.size()= " +
			// // this.nextActivityObjectsFromCands.size()
			// // + "\ndistancesSortedMap.size()=" + distancesSortedMap.size()
			// // + "\nthis.candidateTimelines.size()=" + this.candidateTimelines.size());
			//
			// // this will not be true when thresholding
			// if (this.thresholdPruningNoEffect)
			// {
			// if (Constant.filterTopCands <= 0) // this sanity check is only valid when not filtering cands
			// {
			// if (!Sanity.eq(distancesSortedMap.size(), this.candidateTimelines.size(),
			// "Error at Sanity 349 (RecommenderMaster: editDistancesSortedMapFullCand.size()==
			// this.candidateTimelines.size() not satisfied"))
			// {
			// errorExists = true;
			// }
			// }
			// }
			//
			// }
			// else if (this.lookPastType.equals(Enums.LookPastType.ClosestTime))
			// {
			// this.nextActivityObjectsFromCands = new LinkedHashMap<>();
			// }
			// // curtain not relevant 2: end
			// //Start of not relevant curtain
			// if (VerbosityConstants.verbose)
			// {
			// System.out.println("---------editDistancesSortedMap.size()=" + distancesSortedMap.size());
			//
			// StringBuilder sbToWrite1 = new StringBuilder(
			// ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" + "\n lookPastType:" + lookPastType
			// + "\n The candidate timelines in increasing order of distance are:\n");
			// distancesSortedMap.entrySet().stream()
			// .forEach(e -> sbToWrite1.append("candID:" + e.getKey() + " dist:" + e.getValue().getSecond()
			// + "\n acts:" + candidateTimelines.get(e.getKey()).getPrimaryDimensionValsInSequence()
			// // .getActivityObjectNamesInSequence()
			// + "\n"));
			// sbToWrite1.append("Top next activities are:\n");// +this.topNextRecommendedActivities);
			// nextActivityObjectsFromCands.entrySet().stream()
			// .forEach(e -> sbToWrite1.append(" >>" + e.getValue().getFirst().getPrimaryDimensionVal()//
			// .getActivityName()
			// + ":" + e.getValue().getSecond()));
			// System.out.println(sbToWrite1.toString());
			//
			// System.out.println("\nDebug note192_end: getActivityNamesGuidingRecommwithTimestamps() "
			// + getActivityNamesGuidingRecommwithTimestamps() + " size of current timeline="
			// + currentTimeline.getActivityObjectsInTimeline().size());
			// }
			//
			// if (VerbosityConstants.WriteEditDistancePerRtPerCand)
			// {
			// WritingToFile.writeEditDistancesPerRtPerCand(this.userAtRecomm, this.dateAtRecomm, this.timeAtRecomm,
			// this.distancesSortedMap, this.candidateTimelines, this.nextActivityObjectsFromCands,
			// this.activitiesGuidingRecomm, activityObjectAtRecommPoint,
			// VerbosityConstants.WriteCandInEditDistancePerRtPerCand,
			// VerbosityConstants.WriteEditOperatationsInEditDistancePerRtPerCand,
			// this.endPointIndicesConsideredInCands, Constant.primaryDimension);
			// }
			// //End of not relevant curtain
			//////// Create ranked recommended act names

			// Non relevant Curtain start
			// this.recommendedActivityNamesWithRankscores = createRankedTopRecommendedActivityPDVals(
			// this.nextActivityObjectsFromCands, this.caseType, similarityOfEndPointActivityObjectCand,
			// this.lookPastType, this.distancesSortedMap);
			// Non relevant Curtain end

			this.recommendedActivityNamesWithRankscores = getTopPredictedRNNActivityPDVals(this.activitiesGuidingRecomm,
					this.lookPastType, this.candidateTimelines, 1, false, this.userIDAtRecomm, altSeqPredictor,
					recommSeqLength);

			// $$this.recommendedActivityNamesWithRankscores =

			// null when there is no AKOM prediction.Happens when the current activity is not in training timelines.
			if (recommendedActivityNamesWithRankscores == null || recommendedActivityNamesWithRankscores.size() == 0)
			{
				hasCandidateTimelines = false;
				rankedRecommendedActNamesWithRankScoresStr = null;
				nextActivityObjectsFromCands = null;
				thresholdPruningNoEffect = true;
				hasCandidateTimelinesBelowThreshold = false;
				return;
			}

			this.rankedRecommendedActNamesWithRankScoresStr = new ArrayList<>();
			this.rankedRecommendedActNamesWithoutRankScoresStr = new ArrayList<>();

			for (LinkedHashMap<String, Double> oneStep : recommendedActivityNamesWithRankscores)
			{
				rankedRecommendedActNamesWithRankScoresStr
						.add(getRankedRecommendedActivityPDvalsWithRankScoresString(oneStep));

				rankedRecommendedActNamesWithoutRankScoresStr
						.add(getRankedRecommendedActivityPDValsithoutRankScoresString(oneStep));
			}

			if (VerbosityConstants.verbose)
			{
				System.out.println(
						"rankedRecommendedActNamesWithRankScoresStr= " + rankedRecommendedActNamesWithRankScoresStr);
			}

			//
			// // Non relevant Curtain start
			// this.normEditSimilarity = (ArrayList<Double>) this.nextActivityObjectsFromCands.entrySet().stream()
			// .map(e -> e.getValue().getSecond()).collect(Collectors.toList());
			//
			//
			// if (this.caseType.equals(Enums.CaseType.CaseBasedV1))
			// {
			// this.simEndActivityObjForCorr = (ArrayList<Double>) this.nextActivityObjectsFromCands.entrySet()
			// .stream().map(nActObj -> similarityOfEndPointActivityObjectCand.get(nActObj.getKey()))
			// .collect(Collectors.toList());
			// }
			// // Non relevant Curtain end
			//
			if (VerbosityConstants.verbose)
			{
				System.out.println("Debug: rankedRecommendedActNamesWithRankScoresStr= "
						+ rankedRecommendedActNamesWithRankScoresStr);
				System.out.println("Debug: rankedRecommendedActNamesWithoutRankScoresStr= "
						+ rankedRecommendedActNamesWithoutRankScoresStr);
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

		catch (Exception e)
		{
			e.printStackTrace();
			// PopUps.getTracedErrorMsg("Exception in recommendation master");
			PopUps.printTracedErrorMsg("Exception in recommendation master");
		}

		System.out.println("\n^^^^^^^^^^^^^^^^Exiting Recommendation Master");
	}

	/**
	 * 
	 * @param activitiesGuidingRecomm
	 * @param lookPastType
	 * @param candidateTimelines
	 * @param constantValScore
	 * @param verbose
	 * @param userID
	 * @param alternateSeqPredictor
	 * @param recommSeqLength
	 * @return one linkedHashmapfor each step
	 * @throws Exception
	 */
	private ArrayList<LinkedHashMap<String, Double>> getTopPredictedRNNActivityPDVals(
			ArrayList<ActivityObject> activitiesGuidingRecomm, LookPastType lookPastType,
			LinkedHashMap<String, Timeline> candidateTimelines, double constantValScore, boolean verbose, String userID,
			Enums.AltSeqPredictor alternateSeqPredictor, int recommSeqLength) throws Exception
	{
		int numOfNextPredictions = recommSeqLength;
		ArrayList<LinkedHashMap<String, Double>> res = new ArrayList<>();

		if (lookPastType.equals(Enums.LookPastType.Daywise) || lookPastType.equals(Enums.LookPastType.NCount))
		{
			// System.out.println("Current timeline:");
			// Convert current timeline to a seq of integers
			ArrayList<Character> currSeq = TimelineTransformers.listOfActObjsToListOfCharCodesFromActIDs(
					activitiesGuidingRecomm, false, Constant.getActIDCharCodeMap());

			// if NCount matching, then the next activity should be included in the training seq.
			LinkedHashMap<String, Timeline> candidateTimelinesWithNextAppended = candidateTimelines;

			// However, for Pure AKOM approach there is no Next act since the candidate timeline from each user is its
			// entire trainining timeline (reduced or not reduced)
			// if (alternateSeqPredictor.equals(Enums.AltSeqPredictor.AKOM)
			// && !alternateSeqPredictor.equals(Enums.AltSeqPredictor.PureAKOM))
			// {
			// for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
			// {
			// TimelineWithNext t = (TimelineWithNext) candT.getValue();
			// t.appendAO(t.getNextActivityObject());
			// }
			// }

			// Convert cand timeline to a list of seq of integers
			// On 15 Dec 2017 removed candTimelinesAsSeq to be as internal to getAKOMPredictedSymbol()
			// ArrayList<ArrayList<Integer>> candTimelinesAsSeq = new ArrayList<>();

			// System.out.println("Cand timelines:");

			// System.out.println("predictedNextSymbol = ");
			// TimelineTransformers.timelineToSeqOfActIDs(timeline, delimiter)
			List<Character> predSymbol = getRNNPredictedSymbolLSTM1(userID, currSeq, candidateTimelinesWithNextAppended,
					alternateSeqPredictor, numOfNextPredictions, verbose);
			// getRNNPredictedSymbol(userID, currSeq, candidateTimelinesWithNextAppended,
			// alternateSeqPredictor, numOfNextPredictions, verbose);

			// String userID, ArrayList<Character> currSeq,
			// LinkedHashMap<String, Timeline> candidateTimelinesWithNextAppended,
			// Enums.AltSeqPredictor alternateSeqPredictor, int nextHowManyPredictions
			// candTimelinesAsSeq);

			// System.out.println("predictedNextSymbol = " +
			// SeqPredictor p = new SeqPredictor(candTimelinesAsSeq, currSeq, highestOrder, verbose);
			// StringCode.getCharCodeFromActivityID(activityID)
			// Integer predictedActID = Integer.valueOf(predSymbol);

			ArrayList<Integer> predictedActIDs = TimelineTransformers.listOfCharCodesToActIDs(predSymbol, verbose,
					Constant.getCharCodeActIDMap());
			System.out.println("predictedActIDs = " + predictedActIDs.toString());

			if (predictedActIDs.size() == 0)
			{
				System.out.println("Warning! Return predictedActIDs.size() == 0, no predictions");
				return null;
			}

			for (Integer predictedActID : predictedActIDs)
			{
				LinkedHashMap<String, Double> oneStepRes = new LinkedHashMap<>();
				oneStepRes.put(String.valueOf(predictedActID), constantValScore);
				res.add(oneStepRes);
			}

			return res;
			// res.put(predictedActID.toString(), constantValScore);
			// return res;
			// return createRankedTopRecommendedActivityNamesClosestTime(distancesSortedMap);
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg("Error:unrecognised lookpast type = " + lookPastType));
			return null;
		}

	}

	/**
	 * 
	 *
	 * @param userID
	 * @param currSeq
	 * @param candidateTimelinesWithNextAppended
	 * @param alternateSeqPredictor
	 * @param nextHowManyPredictions
	 * @param verbose
	 * @return
	 * @throws Exception
	 * @deprecated
	 */
	private List<Character> getRNNPredictedSymbol(String userID, ArrayList<Character> currSeq,
			LinkedHashMap<String, Timeline> candidateTimelinesWithNextAppended,
			Enums.AltSeqPredictor alternateSeqPredictor, int nextHowManyPredictions, boolean verbose) throws Exception
	{
		ArrayList<ArrayList<Character>> candTimelinesAsSeq = new ArrayList<>();
		List<Character> predSymbol = new ArrayList<>();

		BasicRNNWC2_SeqRec2018 seqPredictor = null;
		boolean savedReTrain = false;
		BasicRNNWC2_SeqRec2018 sanityCheckSeqPredictor = null;

		if (Constant.sameRNNForAllRTsOfAUser)// && alternateSeqPredictor.equals(Enums.AltSeqPredictor.RNN1)
		{
			seqPredictor = BasicRNNWC2_SeqRec2018.getRNNPredictorsForEachUserStored(userID);

			if (seqPredictor == null) // RNN NOT already trained for this user
			{
				for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
				{
					candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfCharCodesFromActIDs(
							candT.getValue().getActivityObjectsInTimeline(), false, Constant.getActIDCharCodeMap()));
				}
				seqPredictor = new BasicRNNWC2_SeqRec2018(candTimelinesAsSeq, userID, verbose);// verbose);
			}
			else
			{
				savedReTrain = true;
				System.out.println("Ajooba: already trained RNN for this user:" + userID);
			}
		}
		else if (Constant.sameRNNForALLUsers)// RNN trained only once and reused for all users for all RTs
		{
			String userIDForAllUsers = "IDForAllUsers";
			seqPredictor = BasicRNNWC2_SeqRec2018.getRNNPredictorsForEachUserStored(userIDForAllUsers);

			if (seqPredictor == null) // RNN NOT already trained
			{
				for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
				{
					candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfCharCodesFromActIDs(
							candT.getValue().getActivityObjectsInTimeline(), false, Constant.getActIDCharCodeMap()));
				}
				System.out.println(
						"Num of users in cand timelines (#cands) = " + candidateTimelinesWithNextAppended.size());
				seqPredictor = new BasicRNNWC2_SeqRec2018(candTimelinesAsSeq, userIDForAllUsers, verbose);// verbose);
			}
			else
			{
				savedReTrain = true;
				System.out.println("Ajooba: (sameRNNForALLUsers) already trained RNN:" + userID);
			}
		}
		else
		{
			for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
			{
				candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfCharCodesFromActIDs(
						candT.getValue().getActivityObjectsInTimeline(), false, Constant.getActIDCharCodeMap()));
			}
			seqPredictor = new BasicRNNWC2_SeqRec2018(candTimelinesAsSeq, userID, verbose);// verbose);
		}

		predSymbol = seqPredictor.predictNextNValues2(nextHowManyPredictions, false, currSeq);// verbose);

		// Start of Sanity CHeck
		// if (savedReTrain)// PASSED
		// {
		// // training again and checking if predSYmbol same as fetched from pretrained model
		// for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
		// {
		// candTimelinesAsSeq.add(TimelineTransformers
		// .timelineToSeqOfActIDs(candT.getValue().getActivityObjectsInTimeline(), false));
		// }
		// AKOMSeqPredictor sanityCheckseqPredictor = new AKOMSeqPredictor(candTimelinesAsSeq, highestOrder, false,
		// userID);// verbose);
		// int sanityCheckPredSymbol = seqPredictor.getAKOMPrediction(currSeq, false);// verbose);
		//
		// Sanity.eq(sanityCheckPredSymbol, predSymbol,
		// "Sanity Error sanityCheckPredSymbol=" + sanityCheckPredSymbol + "!= predSymbol" + predSymbol);
		// System.out.println(
		// "SanityCHeck sanityCheckPredSymbol=" + sanityCheckPredSymbol + ", predSymbol=" + predSymbol);
		// }
		// End of Sanity check

		return predSymbol;
	}

	/**
	 * 
	 *
	 * @param userID
	 * @param currSeq
	 * @param candidateTimelinesWithNextAppended
	 * @param alternateSeqPredictor
	 * @param nextHowManyPredictions
	 * @param verbose
	 * @return
	 * @throws Exception
	 */
	private List<Character> getRNNPredictedSymbolLSTM1(String userID, ArrayList<Character> currSeq,
			LinkedHashMap<String, Timeline> candidateTimelinesWithNextAppended,
			Enums.AltSeqPredictor alternateSeqPredictor, int nextHowManyPredictions, boolean verbose) throws Exception
	{
		ArrayList<ArrayList<Character>> candTimelinesAsSeq = new ArrayList<>();
		List<Character> predSymbol = new ArrayList<>();
		System.out.println("----> Inside getRNNPredictedSymbolLSTM1.\nNum of users in cand timelines (#cands) = "
				+ candidateTimelinesWithNextAppended.size());

		LSTMCharModelling_SeqRecJun2018 seqPredictor = null;
		boolean savedReTrain = false;
		LSTMCharModelling_SeqRecJun2018 sanityCheckSeqPredictor = null;

		if (Constant.sameRNNForALLUsers)// RNN trained only once and reused for all users for all RTs
		{
			System.out.println("sameRNNForALLUsers!!");
			String userIDForAllUsers = "IDForAllUsers";
			seqPredictor = LSTMCharModelling_SeqRecJun2018.getLSTMPredictorsForEachUserStored(userIDForAllUsers);

			if (seqPredictor == null) // RNN NOT already trained
			{
				for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
				{
					candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfCharCodesFromActIDs(
							candT.getValue().getActivityObjectsInTimeline(), false, Constant.getActIDCharCodeMap()));
				}

				seqPredictor = new LSTMCharModelling_SeqRecJun2018(candTimelinesAsSeq, userIDForAllUsers, verbose);// verbose);
			}
			else
			{
				savedReTrain = true;
				System.out.println("Ajooba: (sameRNNForALLUsers) already trained RNN:" + userID);
			}
		}
		else if (Constant.sameRNNForAllRTsOfAUser)// && alternateSeqPredictor.equals(Enums.AltSeqPredictor.RNN1)
		{
			seqPredictor = LSTMCharModelling_SeqRecJun2018.getLSTMPredictorsForEachUserStored(userID);

			if (seqPredictor == null) // RNN NOT already trained for this user
			{
				for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
				{
					candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfCharCodesFromActIDs(
							candT.getValue().getActivityObjectsInTimeline(), false, Constant.getActIDCharCodeMap()));
				}
				seqPredictor = new LSTMCharModelling_SeqRecJun2018(candTimelinesAsSeq, userID, verbose);// verbose);
			}
			else
			{
				savedReTrain = true;
				System.out.println("Ajooba: already trained RNN for this user:" + userID);
			}
		}

		else
		{
			for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
			{
				candTimelinesAsSeq.add(TimelineTransformers.listOfActObjsToListOfCharCodesFromActIDs(
						candT.getValue().getActivityObjectsInTimeline(), false, Constant.getActIDCharCodeMap()));
			}
			seqPredictor = new LSTMCharModelling_SeqRecJun2018(candTimelinesAsSeq, userID, verbose);// verbose);
		}

		predSymbol = seqPredictor.predictNextNValues5(NNUtils.listToCharArr(currSeq), nextHowManyPredictions, verbose);

		// Start of Sanity CHeck
		// if (savedReTrain)// PASSED
		// {
		// // training again and checking if predSYmbol same as fetched from pretrained model
		// for (Entry<String, Timeline> candT : candidateTimelinesWithNextAppended.entrySet())
		// {
		// candTimelinesAsSeq.add(TimelineTransformers
		// .timelineToSeqOfActIDs(candT.getValue().getActivityObjectsInTimeline(), false));
		// }
		// AKOMSeqPredictor sanityCheckseqPredictor = new AKOMSeqPredictor(candTimelinesAsSeq, highestOrder, false,
		// userID);// verbose);
		// int sanityCheckPredSymbol = seqPredictor.getAKOMPrediction(currSeq, false);// verbose);
		//
		// Sanity.eq(sanityCheckPredSymbol, predSymbol,
		// "Sanity Error sanityCheckPredSymbol=" + sanityCheckPredSymbol + "!= predSymbol" + predSymbol);
		// System.out.println(
		// "SanityCHeck sanityCheckPredSymbol=" + sanityCheckPredSymbol + ", predSymbol=" + predSymbol);
		// }
		// End of Sanity check
		System.out.println("----> Exiting getRNNPredictedSymbolLSTM1.");
		return predSymbol;
	}

	/**
	 * 
	 * @param candidateTimelines
	 * @return
	 */
	private static LinkedHashMap<String, String> extractCandUserIDs(LinkedHashMap<String, Timeline> candidateTimelines)
			throws Exception
	{
		LinkedHashMap<String, String> candUserIDs = new LinkedHashMap<>();
		try
		{
			for (Entry<String, Timeline> candE : candidateTimelines.entrySet())
			{
				if (candE.getValue().isShouldBelongToSingleUser())
				{
					candUserIDs.put(candE.getKey(), candE.getValue().getUserID());
				}
				else
				{
					PopUps.printTracedErrorMsgWithExit("Error: not taking care of this case");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return candUserIDs;
	}

	/**
	 * 
	 * @param distancesMapUnsorted
	 * @param typeOfThreshold
	 * @param thresholdVal
	 * @param activitiesGuidingRecomm
	 * @return Triple{prunedDistancesMap,thresholdAsDistance,thresholdPruningNoEffect}
	 */
	private static Triple<LinkedHashMap<String, Pair<String, Double>>, Double, Boolean> pruneAboveThreshold(
			LinkedHashMap<String, Pair<String, Double>> distancesMapUnsorted, TypeOfThreshold typeOfThreshold,
			double thresholdVal, ArrayList<ActivityObject> activitiesGuidingRecomm)
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
			double maxEditDistance = (new AlignmentBasedDistance()).maxEditDistance(activitiesGuidingRecomm);
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
		distancesMapUnsorted = TimelineUtils.removeAboveThreshold4SSD(distancesMapUnsorted, thresholdAsDistance);//
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

		for (ActivityObject ae : activitiesGuidingRecomm)
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

	/**
	 * 
	 * 
	 * @return
	 */
	public LinkedHashMap<String, Double> getRecommendedActivityNamesWithRankscores()
	{
		// PopUps.printTracedErrorMsgWithExit("Error: Not suitable as method has seq recommendation");
		// return null;
		return this.recommendedActivityNamesWithRankscores.get(this.indexOfRecommSeq);
	}

	public LinkedHashMap<String, Double> getRecommendedActivityNamesWithRankscores(int step)
	{
		return this.recommendedActivityNamesWithRankscores.get(step);
	}

	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	/**
	 * Generate the string: '__recommendedActivityName1:simRankScore1__recommendedActivityName2:simRankScore2'
	 * 
	 * @param recommendedActivityPDValRankscorePairs
	 */
	private static String getRankedRecommendedActivityPDvalsWithRankScoresString(
			LinkedHashMap<String, Double> recommendedActivityPDValRankscorePairs)
	{
		StringBuilder topRankedString = new StringBuilder();// String topRankedString= new String();
		StringBuilder msg = new StringBuilder();
		for (Map.Entry<String, Double> entry : recommendedActivityPDValRankscorePairs.entrySet())
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
	private static String getRankedRecommendedActivityPDValsithoutRankScoresString(
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

	/**
	 * Fetches the next Activity Objects with their edit distance from the candidate timelines (wrt Current Timeline)
	 * <p>
	 * <font color = orange> casts Timeline to TimelineWithNext, this should be the case for non-daywise,i.e. NCount and
	 * NHours approaches.
	 * 
	 * @param editDistanceSortedFullCand
	 * @param candidateTimelines
	 * @return TimelineID,Pair{Next Activity Object,edit distance}
	 */
	public static LinkedHashMap<String, Pair<ActivityObject, Double>> fetchNextActivityObjectsFromNext(
			LinkedHashMap<String, Pair<String, Double>> editDistanceSortedFullCand,
			LinkedHashMap<String, Timeline> candidateTimelines)
	{
		// TimelineID,Pair{Next Activity Object,edit distance}
		LinkedHashMap<String, Pair<ActivityObject, Double>> nextActObjs = new LinkedHashMap<>();
		// ArrayList<Triple<ActivityObject, Double, String>> topActivityObjects = new ArrayList<>();
		// Triple <Next Activity Object,edit distance, TimelineID>

		try
		{
			// Disabled on Aug 2 2017 as its just logging
			if (VerbosityConstants.verbose)
			{
				if (editDistanceSortedFullCand.size() < 5)
				{
					System.err.println("\nWarning: #cands = editDistanceSortedFullCand.size() ="
							+ editDistanceSortedFullCand.size() + "<5");
					// errorExists = true;
				}
			}

			for (Map.Entry<String, Pair<String, Double>> candDistEntry : editDistanceSortedFullCand.entrySet())
			{
				String candID = candDistEntry.getKey();
				Double editDistanceForCandidate = candDistEntry.getValue().getSecond();

				TimelineWithNext candidateTimeline = (TimelineWithNext) candidateTimelines.get(candID);
				ActivityObject nextActivityObjectForCand = candidateTimeline.getNextActivityObject();

				if (candidateTimeline.size() <= 0)
				{
					System.err.println(PopUps.getTracedErrorMsg("Error :candID=" + candID
							+ " not found, thus candidateTimeline.size=" + candidateTimeline.size()));
				}
				else if (nextActivityObjectForCand == null)
				{
					System.err.println(PopUps.getTracedErrorMsg("Error nextActivityObjectForCand == null"));
				}

				nextActObjs.put(candID,
						new Pair<ActivityObject, Double>(nextActivityObjectForCand, editDistanceForCandidate));
				// topActivityObjects.add(new Triple<ActivityObject, Double, String>(
				// simCandidateTimeline.getNextActivityObject(), editDistanceForSimCandidate, simCandidateID));
				// take the next activity object (next activity object is the valid next activity object)
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (nextActObjs == null || nextActObjs.size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: nextActObjs.size() = ") + nextActObjs.size());
		}
		// this.topNextActivityObjects = topActivityObjects;
		return nextActObjs;
	}

	/**
	 * 
	 * @param activitiesGuidingRecomm
	 * @param distanceScoresSorted
	 * @param dayTimelinesForUser
	 * @return
	 */
	public static LinkedHashMap<String, Pair<ActivityObject, Double>> fetchNextActivityObjectsDaywise(
			LinkedHashMap<String, Pair<String, Double>> editDistanceSorted,
			LinkedHashMap<String, Timeline> candidateTimelines, LinkedHashMap<String, Integer> endPointIndices)
	{
		// System.out.println("\n-----------------Inside fetchNextActivityObjectsDaywise");
		LinkedHashMap<String, Pair<ActivityObject, Double>> nextActObjs = new LinkedHashMap<>();

		if (editDistanceSorted.size() < 5)
		{
			System.err.println("\nWarning: # candidate timelines =" + editDistanceSorted.size() + "<5");
		}

		try
		{
			for (Map.Entry<String, Pair<String, Double>> candDistEntry : editDistanceSorted.entrySet())
			{
				String timelineID = candDistEntry.getKey();
				Double distanceOfCandTimeline = candDistEntry.getValue().getSecond();

				Timeline candUserDayTimeline = candidateTimelines.get(timelineID);
				int endPointIndexInCand = endPointIndices.get(timelineID);
				// TimelineUtils.getUserDayTimelineByDateFromMap(dayTimelinesForUser, dateOfCandTimeline);
				if (candUserDayTimeline == null)
				{
					System.err.println(PopUps.getTracedErrorMsg("Error: candUserDayTimeline is null"));
					System.exit(-1);
				}
				if (!candUserDayTimeline.isShouldBelongToSingleDay())
				{
					System.err.println(PopUps.getTracedErrorMsg(
							"Error: for daytimeline candUserDayTimeline.isShouldBelongToSingleDay()= "
									+ candUserDayTimeline.isShouldBelongToSingleDay()));
					System.exit(-1);
				}
				if (endPointIndexInCand < 0)
				{
					System.err.println(PopUps
							.getTracedErrorMsg("Error: for daytimeline endPointIndexInCand=" + endPointIndexInCand));
					System.exit(-1);
				}

				ActivityObject nextValidAO = candUserDayTimeline
						.getNextValidActivityAfterActivityAtThisPositionPD(endPointIndexInCand);
				nextActObjs.put(timelineID, new Pair<ActivityObject, Double>(nextValidAO, distanceOfCandTimeline));

				if (VerbosityConstants.verbose)
				{
					System.out.println("timelineID=" + timelineID + " endPointIndexInCand =" + endPointIndexInCand);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (nextActObjs == null || nextActObjs.size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: nextActObjs.size() = ") + nextActObjs.size());
		}

		// System.out.println("-------exiting fetchNextActivityObjectsDaywise\n");
		return nextActObjs;
	}

	/**
	 * Fetches the next Activity Objects with their edit distance from the candidate timelines (wrt Current Timeline)
	 * 
	 * @param editDistanceSorted
	 * @param candidateTimelines
	 * @return TimelineID,Pair{Next Activity Object,edit distance}
	 */
	public static LinkedHashMap<String, Pair<ActivityObject, Double>> fetchNextActivityObjects(
			LinkedHashMap<String, Pair<String, Double>> editDistanceSorted,
			LinkedHashMap<String, Timeline> candidateTimelines, Enums.LookPastType lookPastType,
			LinkedHashMap<String, Integer> endPointIndicesForDaywise)
	{

		switch (lookPastType)
		{
			case Daywise:
				return fetchNextActivityObjectsDaywise(editDistanceSorted, candidateTimelines,
						endPointIndicesForDaywise);
			case NCount:
				return fetchNextActivityObjectsFromNext(editDistanceSorted, candidateTimelines);
			case NHours:
				return fetchNextActivityObjectsFromNext(editDistanceSorted, candidateTimelines);
			case ClosestTime:
				return null;
			case NGram:
				return fetchNextActivityObjectsFromNext(editDistanceSorted, candidateTimelines);
			default:
				System.err.println(PopUps.getTracedErrorMsg("Error:unrecognised lookPastType = " + lookPastType));
				return null;
		}

	}

	/// End of added on 9 Aug 2017

	/**
	 * 
	 * @param normalisedCandEditDistances
	 * @return
	 */
	public static LinkedHashMap<String, Pair<String, Double>> aggregatedFeatureWiseDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> normalisedCandEditDistances)
	{
		LinkedHashMap<String, Pair<String, Double>> aggregatedFeatureWiseDistances = new LinkedHashMap<>();

		for (Map.Entry<String, LinkedHashMap<String, Pair<String, Double>>> entry : normalisedCandEditDistances
				.entrySet()) // iterating over cands
		{
			String candID = entry.getKey();
			LinkedHashMap<String, Pair<String, Double>> normalisedFeatureWiseDistances = entry.getValue();

			int featureIndex = 0;
			double distanceAggregatedOverFeatures = 0;

			for (Map.Entry<String, Pair<String, Double>> distEntry : normalisedFeatureWiseDistances.entrySet())
			// iterating over distance for each feature
			{
				double normalisedDistanceValue = distEntry.getValue().getSecond();
				distanceAggregatedOverFeatures += normalisedDistanceValue;
				featureIndex++;
			}

			distanceAggregatedOverFeatures = StatsUtils
					.round(distanceAggregatedOverFeatures / Constant.getNumberOfFeatures(), 4);
			aggregatedFeatureWiseDistances.put(candID, new Pair("", distanceAggregatedOverFeatures));
		}
		return aggregatedFeatureWiseDistances;
	}

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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
			Enums.CaseType caseType, int userID, String dateAtRecomm, String timeAtRecomm,
			AlignmentBasedDistance alignmentBasedDistance)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<String, Double> candEndPointEditDistances = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> candEntry : candidateTimelines.entrySet())
		{
			ArrayList<ActivityObject> activityObjectsInCand = candEntry.getValue().getActivityObjectsInTimeline();
			Double endPointEditDistanceForThisCandidate = new Double(-9999);

			if (caseType.equals(Enums.CaseType.CaseBasedV1))// "CaseBasedV1")) // CaseBasedV1
			{
				ActivityObject endPointActivityObjectCandidate = (activityObjectsInCand
						.get(activityObjectsInCand.size() - 1)); // only the end point activity
																	// object
				ActivityObject endPointActivityObjectCurrentTimeline = (activitiesGuidingRecomm
						.get(activitiesGuidingRecomm.size() - 1)); // activityObjectAtRecommPoint

				switch (Constant.getDatabaseName()) // (Constant.DATABASE_NAME)
				{
					case "geolife1":
						endPointEditDistanceForThisCandidate = alignmentBasedDistance
								.getCaseBasedV1SimilarityGeolifeData(endPointActivityObjectCandidate,
										endPointActivityObjectCurrentTimeline, userID);
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

	public static String getStringCodeOfActivityObjects(ArrayList<ActivityObject> activityObjects)
	{
		StringBuilder code = new StringBuilder();
		for (ActivityObject ao : activityObjects)
		{
			code.append(ao.getCharCodeFromActID());
		}
		return code.toString();
	}

	////

	public static LinkedHashMap<String, Double> removeRecommPointActivityFromRankedRecomm(
			LinkedHashMap<String, Double> recommendedActivityNamesWithRankscores, String activityNameAtRecommPoint)
	{
		// String activityNameAtRecommPoint = activityAtRecommPoint.getActivityName();
		System.out.println("removeRecommPointActivityFromRankedRecomm called");
		Double d = recommendedActivityNamesWithRankscores.remove(activityNameAtRecommPoint);
		if (d == null)
		{
			System.out.println("Note: removeRecommPointActivityFromRankedRecomm: curr act not in recommendation");
		}
		return recommendedActivityNamesWithRankscores;
	}

	public static ArrayList<Integer> getIndicesOfEndPointActivityInDayButNotAsLast(String userDayActivitiesAsStringCode,
			String codeOfEndPointActivity)
	{
		// System.out.println("\nDebug getIndicesOfEndPointActivityInDayButNotAsLast:
		// userDayActivitiesAsStringCode="+userDayActivitiesAsStringCode+" and
		// codeOfEndPointActivity="+codeOfEndPointActivity);
		ArrayList<Integer> indicesOfEndPointActivityInDay = new ArrayList<Integer>();

		int index = userDayActivitiesAsStringCode.indexOf(codeOfEndPointActivity);

		while (index >= 0)
		{
			// System.out.println(index);
			if (index != (userDayActivitiesAsStringCode.length() - 1)) // not last index
			{
				indicesOfEndPointActivityInDay.add(index);
			}
			index = userDayActivitiesAsStringCode.indexOf(codeOfEndPointActivity, index + 1);
		}
		return indicesOfEndPointActivityInDay;
	}

	public static ArrayList<Integer> getIndicesOfEndPointActivityInTimeline(String userActivitiesAsStringCode,
			String codeOfEndPointActivity)
	{
		// System.out.println("\nDebug getIndicesOfEndPointActivityInDayButNotAsLast:
		// userDayActivitiesAsStringCode="+userDayActivitiesAsStringCode+" and
		// codeOfEndPointActivity="+codeOfEndPointActivity);
		ArrayList<Integer> indicesOfEndPointActivityInTimeline = new ArrayList<Integer>();

		int index = userActivitiesAsStringCode.indexOf(codeOfEndPointActivity);

		while (index >= 0)
		{
			indicesOfEndPointActivityInTimeline.add(index);
			index = userActivitiesAsStringCode.indexOf(codeOfEndPointActivity, index + 1);
		}
		return indicesOfEndPointActivityInTimeline;
	}

	/**
	 */
	public static long getSumOfActivityObjects(LinkedHashMap<Integer, Timeline> map)
	{
		long count = 0;

		for (Map.Entry<Integer, Timeline> entry : map.entrySet())
		{
			int a = entry.getValue().countNumberOfValidActivities();
			int b = entry.getValue().size();

			if (a != b)
			{
				PopUps.showError(
						"Error in getSumOfActivityObjects a should be equal to be since we removed invalid aos beforehand but a = "
								+ a + " and b=" + b);
			}
			count += a;
		}
		return count;
	}

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
		return this.candidateTimelines.size();
	}

	public int getNumOfActsInActsGuidingRecomm()
	{
		return this.activitiesGuidingRecomm.size();
	}

	public int getNumOfValidActsInActsGuidingRecomm()
	{
		int count = 0;
		for (ActivityObject ae : this.activitiesGuidingRecomm)
		{
			if (UtilityBelt.isValidActivityName(ae.getActivityName()))
			{
				count++;
			}
		}
		return count++;
	}

	public Timeline getCandidateTimeline(String timelineID)
	{
		return this.candidateTimelines.get(timelineID);
	}

	public ArrayList<Timeline> getOnlyCandidateTimeslines()
	{
		return (ArrayList<Timeline>) this.candidateTimelines.entrySet().stream().map(e -> (Timeline) e.getValue())
				.collect(Collectors.toList());
	}

	public Set<String> getCandidateTimelineIDs()
	{
		return this.candidateTimelines.keySet();
	}

	public boolean hasCandidateTimeslines()
	{
		return hasCandidateTimelines;
	}

	public boolean hasCandidateTimelinesBelowThreshold()
	{
		return hasCandidateTimelinesBelowThreshold;
	}

	public boolean hasThresholdPruningNoEffect()
	{
		return thresholdPruningNoEffect;
	}

	public boolean isNextActivityJustAfterRecommPointIsInvalid()
	{
		return this.nextActivityJustAfterRecommPointIsInvalid;
	}

	public ActivityObject getActivityObjectAtRecomm()
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

		if (Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.RNN1))
		{
			return candidateTimelines.size();
		}
		else
		{
			/*
			 * Assuming that threshold has already been applied
			 */
			return this.distancesSortedMap.size();
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
		nextActivityObjectsFromCands.entrySet().stream()
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
		nextActivityObjectsFromCands.entrySet().stream().forEach(e -> result
				.append("__" + e.getValue().getFirst().getActivityName() + ":" + e.getValue().getSecond().toString()));
		return result.toString();
	}

	// Names not changed to PD vals because this method is inherited from abstract class
	public String getActivityNamesGuidingRecommwithTimestamps()
	{
		StringBuilder res = new StringBuilder();
		for (ActivityObject ae : activitiesGuidingRecomm)
		{
			res = StringUtils.fCat(res, "  ", ae.getActivityName(), "__", ae.getPrimaryDimensionVal().toString(), "__",
					ae.getStartTimestamp().toString(), "_to_", ae.getEndTimestamp().toString());
			// res.append(" " + ae.getActivityName() + "__" + ae.getStartTimestamp() + "_to_" + ae.getEndTimestamp());
		}
		return res.toString();
	}

	/**
	 * Not suitable as method has seq recom, instead call getRankedRecommendedActNamesWithoutRankScores(onestep)
	 * 
	 * @return rankedRecommendedActivityPDValsithoutRankScoresString
	 */
	public String getRankedRecommendedActNamesWithoutRankScores()
	{
		// PopUps.printTracedErrorMsgWithExit("Error: Not suitable as method has seq recommendation");
		// return null;
		return this.rankedRecommendedActNamesWithoutRankScoresStr.get(this.indexOfRecommSeq);
		// return this.rankedRecommendedActNamesWithoutRankScoresStr;
	}

	public String getRankedRecommendedActNamesWithoutRankScores(int step)
	{
		return this.rankedRecommendedActNamesWithoutRankScoresStr.get(step);
	}

	/**
	 * 
	 */
	public String getRankedRecommendedActNamesWithRankScores()
	{
		// return this.rankedRecommendedActNamesWithRankScoresStr.get(this.indexOfRecommSeq);
		// PopUps.printTracedErrorMsgWithExit("Error: Not suitable as method has seq recommendation");
		// return null;
		// return this.rankedRecommendedActNamesWithRankScoresStr;
		return this.rankedRecommendedActNamesWithRankScoresStr.get(this.indexOfRecommSeq);
	}

	public String getRankedRecommendedActNamesWithRankScores(int step)
	{
		return this.rankedRecommendedActNamesWithRankScoresStr.get(step);
	}

	public int getNumOfDistinctRecommendations()
	{
		return recommendedActivityNamesWithRankscores.size();
	}

	public LinkedHashMap<String, Pair<String, Double>> getDistancesSortedMap()
	{
		return this.distancesSortedMap;
	}

	public LinkedHashMap<String, Integer> getEndPointIndicesConsideredInCands()
	{
		return endPointIndicesConsideredInCands;
	}

	public ArrayList<ActivityObject> getActsGuidingRecomm()
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
// public LinkedHashMap<Integer, Double> getEditDistan