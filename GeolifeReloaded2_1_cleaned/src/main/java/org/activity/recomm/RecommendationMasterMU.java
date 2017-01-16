package org.activity.recomm;

//import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.activity.distances.AlignmentBasedDistance;
import org.activity.distances.FeatureWiseEditDistance;
import org.activity.distances.FeatureWiseWeightedEditDistance;
import org.activity.distances.HJEditDistance;
import org.activity.distances.OTMDSAMEditDistance;
import org.activity.evaluation.Evaluation;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.objects.Triple;
import org.activity.objects.UserDayTimeline;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.Constant;
import org.activity.util.StatsUtils;
import org.activity.util.StringCode;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * ALERT!: DO NOT USER THRESHOLDING WITH THIS CLASS since here the edit distances are normalised before thredholding,
 * however, they should have been normalised after thresholding as was the case in iiWAS version of the code This
 * generates recommendations using the matching-unit (MU) approach. Timelines are not viewed as Day Timeline but rather
 * as continuous timelines. The matching-unit can be specified as 'hours' or 'counts'.
 * 
 * Timelines are sets of Activity Objects, (and they do not necessarily belong to one day.) (the matching unit used here
 * HJ version) In this version we calculate edit distance of the full candidate and not the least distant subcandidate
 * 
 * @since 28 October, 2014
 * @author gunjan
 */
public class RecommendationMasterMU// implements IRecommenderMaster
{
	// LinkedHashMap<String,LinkedHashMap<Date,UserDayTimeline>> userTimelines; // LinkedHashMap<Date,UserDayTimeline>
	// dayTimelinesForUser;
	private double matchingUnitInCountsOrHours;

	private Timeline trainingTimeline;
	private Timeline testTimeline;
	private LinkedHashMap<Integer, TimelineWithNext> candidateTimelines; // here key is the TimelineID, which is already
																			// a class variable of Value, So we could
																			// have used ArrayList but we used
																			// LinkedHashMap purely for
																			// search performance reasons

	private TimelineWithNext currentTimeline; // =current timelines

	private Date dateAtRecomm;
	private Time timeAtRecomm;
	private String userAtRecomm;
	private String userIDAtRecomm;

	/**
	 * Current Timeline
	 */
	private ArrayList<ActivityObject> activitiesGuidingRecomm; // Current Timeline , sequence of activity objects
																// happening from the recomm point back until the
																// matching unit
	private ActivityObject activityAtRecommPoint; // current Activity Object
	private String activityNameAtRecommPoint;// current Activity Name

	/**
	 * List of of top next activity objects with their edit distances and the timeline id of the candidate producing
	 * them
	 */
	private ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityObjects;

	/**
	 * (Cand TimelineID, Pair<Trace,Edit distance>) this LinkedHashMap is sorted by the value of edit distance in
	 * ascending order
	 */
	private LinkedHashMap<Integer, Pair<String, Double>> editDistancesSortedMapFullCand; // for full candidate scenario

	/**
	 * This is only relevant when case type is 'CaseBasedV1' (Cand TimeineId, Edit distance of the end point activity
	 * object of this candidate timeline with the current activity object(activity at recomm point))
	 */
	private LinkedHashMap<Integer, Double> similarityOfEndPointActivityObjectCand; // currently its a similarity and not
																					// edit distance

	/**
	 * Recommended Activity names with their rank score
	 */
	private LinkedHashMap<String, Double> recommendedActivityNamesWithRankscores;

	private String rankedRecommendedActivityNameWithRankScores;
	private String rankedRecommendedActivityNameWithoutRankScores;

	private boolean hasCandidateTimelines, hasCandidateTimelinesBelowThreshold;

	private boolean nextActivityJustAfterRecommPointIsInvalid;
	private double thresholdAsDistance = 99999999;

	// private int fullCandOrSubCand; //0 means full cand .... 1 means subcand
	private boolean thresholdPruningNoEffect;

	public final String lookPastType; // public int totalNumberOfProbableCands; // public int
										// numCandsRejectedDueToNoCurrentActivityAtNonLast; // public int
										// numCandsRejectedDueToNoNextActivity;
										// //
										// candidateTimelinesStatus; //1 for has candidate timelines, -1 for no
										// candidate timelines because no past timeline
										// with current act, -2 for no candodate
										// timelines because

	String caseType; // 'SimpleV3' for no case case, 'CaseBasedV1' for first cased based implementation

	// public static String commonPath ;//= Constant.commonPath;

	private boolean errorExists;

	private ArrayList<Double> normEditSimilarity, simEndActivityObjForCorr;
	// public double percentageDistanceThresh;
	/*
	 * threshold for choosing candidate timelines, those candidate timelines whose distance from the 'activity objects
	 * guiding recommendations' is higher than the cost of replacing 'percentageDistanceThresh' % of Activity Objects in
	 * the activities guiding recommendation are pruned out from set of candidate timelines
	 */

	HJEditDistance hjEditDistance;// = new EditSimilarity();
	AlignmentBasedDistance alignmentBasedDistance;// = new EditSimilarity();
	FeatureWiseEditDistance featureWiseEditDistance;
	FeatureWiseWeightedEditDistance featureWiseWeightedEditDistance;
	OTMDSAMEditDistance OTMDSAMEditDistance;

	/**
	 * 
	 * @return
	 */
	public int initialiseDistancesUsed()
	{
		alignmentBasedDistance = new AlignmentBasedDistance(); // used for case based similarity
		String dname = Constant.getDistanceUsed(); // used for distance of candidate timelines from the current timeline

		switch (dname)
		{
		case "HJEditDistance":
			hjEditDistance = new HJEditDistance();
			break;

		case "FeatureWiseEditDistance":
			featureWiseEditDistance = new FeatureWiseEditDistance();
			break;

		case "FeatureWiseWeightedEditDistance":
			featureWiseWeightedEditDistance = new FeatureWiseWeightedEditDistance();
			break;

		case "OTMDSAMEditDistance":
			OTMDSAMEditDistance = new OTMDSAMEditDistance();
			break;

		default:
			PopUps.showError(
					"Error in org.activity.recomm.RecommendationMasterMU.initialiseDistanceUsed(): Unknown distance specified:"
							+ dname);
			System.err.println(
					"Error in org.activity.recomm.RecommendationMasterMU.initialiseDistanceUsed(): Unknown distance specified:"
							+ dname);
			// throw new Exception("Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance
			// specified:" + dname);
			System.exit(-1);
		}
		return 0;
	}

	/**
	 * Recommendation for a particular RT (FOR PERFORANCE, REMOVED ALL LOGGING PRINTS) (NOT INTENDED TO BE USED FOR
	 * EXPERIMENTS DUE TO NO LOGGING)
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
	 */
	public RecommendationMasterMU(LinkedHashMap<Date, UserDayTimeline> trainingTimelines,
			LinkedHashMap<Date, UserDayTimeline> testTimelines, String dateAtRecomm, String timeAtRecomm,
			int userAtRecomm, double thresholdVal, String typeOfThreshold, double matchingUnitInCountsOrHours,
			String caseType, String lookPastType, boolean dummy // counts or hours
	)
	{
		String performanceFileName = Constant.getCommonPath() + "Performance.csv";
		/// "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifePerformance/Test/Performance.csv";//
		/// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/Test/Performance.csv";

		long recommMasterT0 = System.currentTimeMillis();

		// hjEditDistance = new HJEditDistance();
		// alignmentBasedDistance = new AlignmentBasedDistance(); // used for case based similarity
		// featureWiseEditDistance = new FeatureWiseEditDistance();
		initialiseDistancesUsed();

		errorExists = false;
		this.lookPastType = lookPastType;

		LinkedHashMap<Integer, Pair<String, Double>> editDistancesMapUnsortedFullCand;
		// HJEditDistance dummy = new HJEditDistance();
		System.out.println("\n----------------Starting Recommender MasterMU " + lookPastType + "---------------------");
		this.matchingUnitInCountsOrHours = matchingUnitInCountsOrHours;

		this.hasCandidateTimelines = true;
		this.nextActivityJustAfterRecommPointIsInvalid = false;

		String[] splittedDate = dateAtRecomm.split("/"); // dd/mm/yyyy
		this.dateAtRecomm = new Date(Integer.parseInt(splittedDate[2]) - 1900, Integer.parseInt(splittedDate[1]) - 1,
				Integer.parseInt(splittedDate[0])); // okay java.sql.Date with no hidden time
		String[] splittedTime = timeAtRecomm.split(":"); // hh:mm:ss
		this.timeAtRecomm = Time.valueOf(timeAtRecomm);
		this.userAtRecomm = Integer.toString(userAtRecomm);
		this.userIDAtRecomm = Integer.toString(userAtRecomm);
		this.caseType = caseType;

		// $$LOGGINGSystem.out.println(" User at Recomm = " + this.userAtRecomm); //// LOGGING
		// $$LOGGINGSystem.out.println(" Date at Recomm = " + this.dateAtRecomm);
		// $$LOGGINGSystem.out.println(" Time at Recomm = " + this.timeAtRecomm);

		this.trainingTimeline = new Timeline(trainingTimelines); // converting day timelines into continuous timelines
																	// suitable to be used for matching unit
																	// views
		this.testTimeline = new Timeline(testTimelines);

		if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
		{
			this.trainingTimeline = UtilityBelt.expungeInvalids(trainingTimeline);
			this.testTimeline = UtilityBelt.expungeInvalids(testTimeline);

			System.out.println(
					"Expunging invalids before recommendation process: expunging from test and training timelines");
		}

		// //////////////////
		if (lookPastType.equalsIgnoreCase("Count"))
		{
			this.currentTimeline = TimelineWithNext.getCurrentTimelineFromLongerTimelineMUCount(testTimeline,
					this.dateAtRecomm, this.timeAtRecomm, this.userIDAtRecomm, this.matchingUnitInCountsOrHours);
		}

		else if (lookPastType.equalsIgnoreCase("Hrs"))
		{
			this.currentTimeline = TimelineWithNext.getCurrentTimelineFromLongerTimelineMUHours(testTimeline,
					this.dateAtRecomm, this.timeAtRecomm, this.userIDAtRecomm, this.matchingUnitInCountsOrHours);
		}
		else
		{
			System.err.println("Error: Unrecognised lookPastType in RecommendationMasterMUCount");
			System.exit(-154);
		}
		// ////////////////////
		if (currentTimeline == null)
		{
			System.err.println("Error: current timeline is empty");
			errorExists = true;
		}

		this.activitiesGuidingRecomm = currentTimeline.getActivityObjectsInTimeline(); // CURRENT TIMELINE
		// $$LOGGINGSystem.out.println("Current timeline: " +
		// currentTimeline.getActivityObjectNamesWithTimestampsInSequence() + "="
		// $$LOGGING+ this.activitiesGuidingRecomm.size());
		// $$LOGGINGSystem.out.print("Activities in the current timeline (as activities guiding recomm) are: \t");
		// $$LOGGINGfor (int i = 0; i < activitiesGuidingRecomm.size(); i++)
		// $$LOGGING{
		// $$LOGGING System.out.print(activitiesGuidingRecomm.get(i).getActivityName() + " ");
		// $$LOGGING}

		// $$LOGGINGSystem.out.println("\nDebug note192: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps()
		// $$LOGGING + "\n size of current timeline=" + currentTimeline.getActivityObjectsInTimeline().size());

		this.activityAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1); // current
																										// ActivityObject
		this.activityNameAtRecommPoint = this.activityAtRecommPoint.getActivityName();

		// $$LOGGINGSystem.out.println("\nActivity at Recomm point (Current Activity) =" + activityNameAtRecommPoint);//
		// this.activityAtRecommPoint.getActivityName()

		// All check OK
		// //////////////////////////
		long recommMasterT1 = System.currentTimeMillis();
		this.candidateTimelines = getCandidateTimelinesMU(trainingTimeline, matchingUnitInCountsOrHours, lookPastType);// ,this.dateAtRecomm);//,this.activitiesGuidingRecomm
		long recommMasterT2 = System.currentTimeMillis();
		long timeTakenToFetchCandidateTimelines = recommMasterT2 - recommMasterT1;
		// ///////////////////////////
		// $$LOGGINGSystem.out.println("Number of candidate timelines =" + candidateTimelines.size());
		// $$System.out.println("the candidate timelines are as follows:");
		// $$traverseMapOfTimelinesWithNext(candidateTimelines);

		if (candidateTimelines.size() == 0)
		{
			System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
					+ " at time:" + timeAtRecomm + "  because there are no candidate timelines");
			// this.singleNextRecommendedActivity = null;
			this.hasCandidateTimelines = false;
			// this.topNextActivities =null;
			this.topNextActivityObjects = null;
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
		// TODO CHECK: HOW THE EFFECT OF THIS DIFFERS FROM THE EXPERIMENTS DONE FOR IIWAS: in iiWAS normalisation was
		// after thresholding (correct), here
		// normalisation is before thresholding which should be changed
		long recommMasterT3 = System.currentTimeMillis();
		editDistancesMapUnsortedFullCand = getNormalisedDistancesForCandidateTimelinesFullCand(candidateTimelines,
				activitiesGuidingRecomm, caseType, this.userIDAtRecomm, this.dateAtRecomm.toString(),
				this.timeAtRecomm.toString(), Constant.getDistanceUsed());
		long recommMasterT4 = System.currentTimeMillis();
		long timeTakenToComputeNormEditDistances = recommMasterT4 - recommMasterT3;
		/*
		 * Old getHJEditDistancesForCandidateTimelinesFullCand(candidateTimelines, activitiesGuidingRecomm, caseType,
		 * this.userIDAtRecomm, this.dateAtRecomm.toString(), this.timeAtRecomm.toString());
		 */

		// getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
		// getNormalisedDistancesForCandidateTimelinesFullCand(candidateTimelines, activitiesGuidingRecomm, caseType,
		// this.userIDAtRecomm,
		// this.dateAtRecomm.toString(),
		// this.timeAtRecomm.toString(), Constant.getDistanceUsed());//
		// getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);

		// //////////////////////////////////
		// System.out.println("\nDebug note192_229: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		// ########Sanity check
		if (editDistancesMapUnsortedFullCand.size() != candidateTimelines.size())
		{
			System.err.println(
					"Error at Sanity 261 inside RecommendationMasterMU: editDistancesMapUnsorted.size() != candidateTimelines.size()");
			errorExists = true;
		}
		// ##############

		// /// REMOVE candidate timelines which are above the distance THRESHOLD. (actually here we remove the entry for
		// such candidate timelines from the
		// distance scores map
		if (typeOfThreshold.equalsIgnoreCase("Global"))
		{
			this.thresholdAsDistance = thresholdVal;
		}
		else if (typeOfThreshold.equalsIgnoreCase("Percent"))
		{
			double maxEditDistance = (new AlignmentBasedDistance()).maxEditDistance(activitiesGuidingRecomm);
			this.thresholdAsDistance = maxEditDistance * (thresholdVal / 100);
		}
		else
		{
			System.err.println("Error: type of threshold unknown in recommendation master");
			errorExists = true;
			System.exit(-2);
		}
		// System.out.println("\nDebug note192_255: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		// $$ int countCandBeforeThresholdPruning = editDistancesMapUnsortedFullCand.size();//
		// distanceScoresSorted.size();

		// $$REMOVED FOR PERFORMANCE AS IT HAS NO EFFECT BUT JUST A CHECK// editDistancesMapUnsortedFullCand =
		// $$UtilityBelt.removeAboveThreshold4FullCandISD(editDistancesMapUnsortedFullCand, thresholdAsDistance);//
		// distanceScoresSorted=
		// UtilityBelt.removeAboveThreshold2(distanceScoresSorted,thresholdAsDistance);
		// $$int countCandAfterThresholdPruning = editDistancesMapUnsortedFullCand.size();

		// $$this.thresholdPruningNoEffect = (countCandBeforeThresholdPruning == countCandAfterThresholdPruning);
		// System.out.println("\nDebug note192_263: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		// $$ if (!thresholdPruningNoEffect)
		// $${
		// $$PopUps.showMessage("Ohh..threshold pruning is happening. Are you sure you wanted this?");// +msg);
		// $$}
		// ////////////////////////////////

		if (editDistancesMapUnsortedFullCand.size() == 0)
		{
			System.out.println("Warning: No candidate timelines below threshold distance");
			hasCandidateTimelinesBelowThreshold = false;
			return;
		}

		else
		{
			hasCandidateTimelinesBelowThreshold = true;
		}

		// //////////////////////////////
		// System.out.println("\nDebug note192_282: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		editDistancesSortedMapFullCand = (LinkedHashMap<Integer, Pair<String, Double>>) ComparatorUtils
				.sortByValueAscendingIntStrDoub(editDistancesMapUnsortedFullCand); // Now distanceScoresSorted
		// contains the String Id for

		if (caseType.equals("CaseBasedV1"))
		{
			System.out.println("this is CaseBasedV1");
			// this.editDistanceOfEndPointActivityObjectCand =
			// getEditDistancesOfEndPointActivityObjectCand(candidateTimelines,activitiesGuidingRecomm,caseType);//getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
			this.similarityOfEndPointActivityObjectCand = getCaseSimilarityEndPointActivityObjectCand(
					candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, this.dateAtRecomm.toString(),
					this.timeAtRecomm.toString());// getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
		}

		this.topNextActivityObjects = fetchNextActivityObjects(editDistancesSortedMapFullCand, candidateTimelines);

		System.out.println("---------editDistancesSortedMapFullCand.size()=" + editDistancesSortedMapFullCand.size());

		if (Constant.verbose)
		{
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");

			System.out.println("\n" + "\n The candidate timelines  in increasing order of distance are:");
			traverseCandidateTimelineWithEditDistance();// editDistancesSortedMapFullCand);

			System.out.println("Top next activities are: ");// +this.topNextRecommendedActivities);
			traverseTopNextActivities();
		}
		// System.out.println("\nDebug note192_308: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		// ########Sanity check
		if (this.topNextActivityObjects.size() == editDistancesSortedMapFullCand.size()
				&& editDistancesSortedMapFullCand.size() == this.candidateTimelines.size())
		{
			// System.out.println("Sanity Check 349 Passed");
		}
		else
		{
			System.err.println(
					"Error at Sanity 349 (RecommenderMasterMU: this.topNextActivityObjects.size() == editDistancesSortedMapFullCand.size() && editDistancesSortedMapFullCand.size()== this.candidateTimelines.size()  not satisfied");
			errorExists = true;
		}

		// ##############
		if (caseType.equals("CaseBasedV1"))
		{
			// rankScore= (1d-(editDistanceValExceptEnd/maxEditDistanceValExceptEnd))* (1d-
			// (endPointActivityEditDistanceVal/maxEditDistanceValOfEnd));
			// createRankedTopRecommendedActivityNamesCaseBasedV1(this.topNextActivityObjects,
			// this.similarityOfEndPointActivityObjectCand);

			// rankScore= (1d-(editDistanceValExceptEnd/maxEditDistanceValExceptEnd))* simEndPointActivityObject;
			createRankedTopRecommendedActivityNamesCaseBasedV1_3(this.topNextActivityObjects,
					this.similarityOfEndPointActivityObjectCand);
		}

		else
		{
			// createRankedTopRecommendedActivityNames
			createRankedTopRecommendedActivityNamesSimpleV3_3(this.topNextActivityObjects);// , this.userAtRecomm,
																							// dateAtRecomm,
																							// timeAtRecomm);
		}

		// System.out.println("Next recommended 5 activity is: "+this.topNextRecommendedActivities);

		/*
		 * IMPORTANT: If the next activity after the current activity object in the current timeline is an invalid
		 * activity, then we can include the current activity in the list of recommended activities, otherwise the
		 * current activity has to be removed from the list of recommended activities
		 */
		if (currentTimeline.getImmediateNextActivityInvalid() == 0) // not invalid
		{
			removeRecommPointActivityFromRankedRecomm();
			System.out.println("removing recomm point activity (Current Activity) from list of recommendation");
		}

		// $$LOGGING WritingToFile.writeDistanceScoresSortedMapMU(this.userAtRecomm, this.dateAtRecomm,
		// this.timeAtRecomm,
		// $$LOGGING this.editDistancesSortedMapFullCand, this.candidateTimelines, this.topNextActivityObjects,
		// this.activitiesGuidingRecomm,
		// $$LOGGING Constant.WriteCandInEditDistancePerRtPerCand,
		// Constant.WriteEditOperatationsInEditDistancePerRtPerCand);

		// $$LOGGING System.out.println(
		// $$LOGGING "\nDebug note192_end: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps()
		// $$LOGGING + " size of current timeline=" + currentTimeline.getActivityObjectsInTimeline().size());
		// System.out.println("Debug note192_2: current timeline " +
		// currentTimeline.getActivityObjectNamesInSequence());

		////////
		long recommMasterTEnd = System.currentTimeMillis();
		// UserAtRecomm,DateAtRecomm,TimeAtRecomm,MatchingUnit,NumOfTrainingDays,NumOfValidActObjsInTrainingTimelines,NumOfValidActObjsInCurrentTimelines,
		// NumOfCandidateTimelines,SumOfValidActObjsInAllCandTimelines,TotalTimeForRecomm,
		// TimeToGenerateCands,TimeToComputeEdistDistances

		// start of curtain for performance string writing
		// String performanceString = this.userIDAtRecomm + "," + this.dateAtRecomm + "," + this.timeAtRecomm + ","
		// + matchingUnitInCountsOrHours + "," + trainingTimelines.size() + "," + this.trainingTimeline.size() + ","
		// + this.activitiesGuidingRecomm.size() + "," + this.candidateTimelines.size() + ","
		// + getSumOfActivityObjects(candidateTimelines) + "," + (recommMasterTEnd - recommMasterT0) + ","
		// + timeTakenToFetchCandidateTimelines + "," + timeTakenToComputeNormEditDistances + "\n";
		// WritingToFile.appendLineToFileAbsolute(performanceString, performanceFileName);
		// end of curtain for performance string writing

		// System.out.pri
		//////////////

		System.out.println("\n^^^^^^^^^^^^^^^^Exiting Recommendation Master");
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
	 */
	public RecommendationMasterMU(LinkedHashMap<Date, UserDayTimeline> trainingTimelines,
			LinkedHashMap<Date, UserDayTimeline> testTimelines, String dateAtRecomm, String timeAtRecomm,
			int userAtRecomm, double thresholdVal, String typeOfThreshold, double matchingUnitInCountsOrHours,
			String caseType, String lookPastType // counts or hours
	)
	{
		String performanceFileName = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/Test/Performance.csv";
		StringBuilder performanceString = new StringBuilder();
		long recommMasterT0 = System.currentTimeMillis();
		performanceString.append("Start:" + recommMasterT0);
		// hjEditDistance = new HJEditDistance();
		// alignmentBasedDistance = new AlignmentBasedDistance(); // used for case based similarity
		// featureWiseEditDistance = new FeatureWiseEditDistance();
		initialiseDistancesUsed();

		errorExists = false;
		this.lookPastType = lookPastType;

		LinkedHashMap<Integer, Pair<String, Double>> editDistancesMapUnsortedFullCand;
		// HJEditDistance dummy = new HJEditDistance();
		System.out.println("\n----------------Starting Recommender MasterMU " + lookPastType + "---------------------");
		this.matchingUnitInCountsOrHours = matchingUnitInCountsOrHours;

		this.hasCandidateTimelines = true;
		this.nextActivityJustAfterRecommPointIsInvalid = false;

		String[] splittedDate = dateAtRecomm.split("/"); // dd/mm/yyyy
		this.dateAtRecomm = new Date(Integer.parseInt(splittedDate[2]) - 1900, Integer.parseInt(splittedDate[1]) - 1,
				Integer.parseInt(splittedDate[0]));
		String[] splittedTime = timeAtRecomm.split(":"); // hh:mm:ss
		this.timeAtRecomm = Time.valueOf(timeAtRecomm);
		this.userAtRecomm = Integer.toString(userAtRecomm);
		this.userIDAtRecomm = Integer.toString(userAtRecomm);
		this.caseType = caseType;

		System.out.println("	User at Recomm = " + this.userAtRecomm);
		System.out.println("	Date at Recomm = " + this.dateAtRecomm);
		System.out.println("	Time at Recomm = " + this.timeAtRecomm);

		this.trainingTimeline = new Timeline(trainingTimelines); // converting day timelines into continuous timelines
																	// suitable to be used for matching unit
																	// views
		this.testTimeline = new Timeline(testTimelines);

		if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
		{
			this.trainingTimeline = UtilityBelt.expungeInvalids(trainingTimeline);
			this.testTimeline = UtilityBelt.expungeInvalids(testTimeline);

			System.out.println(
					"Expunging invalids before recommendation process: expunging from test and training timelines");
		}

		// //////////////////
		if (lookPastType.equalsIgnoreCase("Count"))
		{
			this.currentTimeline = TimelineWithNext.getCurrentTimelineFromLongerTimelineMUCount(testTimeline,
					this.dateAtRecomm, this.timeAtRecomm, this.userIDAtRecomm, this.matchingUnitInCountsOrHours);
		}

		else if (lookPastType.equalsIgnoreCase("Hrs"))
		{
			this.currentTimeline = TimelineWithNext.getCurrentTimelineFromLongerTimelineMUHours(testTimeline,
					this.dateAtRecomm, this.timeAtRecomm, this.userIDAtRecomm, this.matchingUnitInCountsOrHours);
		}
		else
		{
			System.err.println("Error: Unrecognised lookPastType in RecommendationMasterMUCount");
			System.exit(-154);
		}
		// ////////////////////
		if (currentTimeline == null)
		{
			System.err.println("Error: current timeline is empty");
			errorExists = true;
		}

		this.activitiesGuidingRecomm = currentTimeline.getActivityObjectsInTimeline(); // CURRENT TIMELINE
		System.out.println("Current timeline: " + currentTimeline.getActivityObjectNamesWithTimestampsInSequence() + "="
				+ this.activitiesGuidingRecomm.size());
		System.out.print("Activities in the current timeline (as activities guiding recomm) are: \t");
		for (int i = 0; i < activitiesGuidingRecomm.size(); i++)
		{
			System.out.print(activitiesGuidingRecomm.get(i).getActivityName() + " ");
		}

		System.out.println("\nDebug note192: getActivityNamesGuidingRecommwithTimestamps() "
				+ getActivityNamesGuidingRecommwithTimestamps() + "\n size of current timeline="
				+ currentTimeline.getActivityObjectsInTimeline().size());

		this.activityAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1); // current
																										// ActivityObject
		this.activityNameAtRecommPoint = this.activityAtRecommPoint.getActivityName();

		System.out.println("\nActivity at Recomm point (Current Activity) =" + activityNameAtRecommPoint);// this.activityAtRecommPoint.getActivityName()

		// All check OK
		// //////////////////////////
		this.candidateTimelines = getCandidateTimelinesMU(trainingTimeline, matchingUnitInCountsOrHours, lookPastType);// ,this.dateAtRecomm);//,this.activitiesGuidingRecomm

		// ///////////////////////////
		System.out.println("Number of candidate timelines =" + candidateTimelines.size());
		// $$System.out.println("the candidate timelines are as follows:");
		// $$traverseMapOfTimelinesWithNext(candidateTimelines);

		if (candidateTimelines.size() == 0)
		{
			System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
					+ " at time:" + timeAtRecomm + "  because there are no candidate timelines");
			// this.singleNextRecommendedActivity = null;
			this.hasCandidateTimelines = false;
			// this.topNextActivities =null;
			this.topNextActivityObjects = null;
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
		// TODO CHECK: HOW THE EFFECT OF THIS DIFFERS FROM THE EXPERIMENTS DONE FOR IIWAS: in iiWAS normalisation was
		// after thresholding (correct), here
		// normalisation is before thresholding which should be changed
		// TODO
		editDistancesMapUnsortedFullCand = getNormalisedDistancesForCandidateTimelinesFullCand(candidateTimelines,
				activitiesGuidingRecomm, caseType, this.userIDAtRecomm, this.dateAtRecomm.toString(),
				this.timeAtRecomm.toString(), Constant.getDistanceUsed());

		/*
		 * Old getHJEditDistancesForCandidateTimelinesFullCand(candidateTimelines, activitiesGuidingRecomm, caseType,
		 * this.userIDAtRecomm, this.dateAtRecomm.toString(), this.timeAtRecomm.toString());
		 */

		// getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
		// getNormalisedDistancesForCandidateTimelinesFullCand(candidateTimelines, activitiesGuidingRecomm, caseType,
		// this.userIDAtRecomm,
		// this.dateAtRecomm.toString(),
		// this.timeAtRecomm.toString(), Constant.getDistanceUsed());//
		// getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);

		// //////////////////////////////////
		// System.out.println("\nDebug note192_229: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		// ########Sanity check
		if (editDistancesMapUnsortedFullCand.size() != candidateTimelines.size())
		{
			System.err.println(
					"Error at Sanity 261 inside RecommendationMasterMU: editDistancesMapUnsorted.size() != candidateTimelines.size()");
			errorExists = true;
		}
		// ##############

		// /// REMOVE candidate timelines which are above the distance THRESHOLD. (actually here we remove the entry for
		// such candidate timelines from the
		// distance scores map
		if (typeOfThreshold.equalsIgnoreCase("Global"))
		{
			this.thresholdAsDistance = thresholdVal;
		}
		else if (typeOfThreshold.equalsIgnoreCase("Percent"))
		{
			double maxEditDistance = (new AlignmentBasedDistance()).maxEditDistance(activitiesGuidingRecomm);
			this.thresholdAsDistance = maxEditDistance * (thresholdVal / 100);
		}
		else
		{
			System.err.println("Error: type of threshold unknown in recommendation master");
			errorExists = true;
			System.exit(-2);
		}
		// System.out.println("\nDebug note192_255: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		int countCandBeforeThresholdPruning = editDistancesMapUnsortedFullCand.size();// distanceScoresSorted.size();

		editDistancesMapUnsortedFullCand = TimelineUtils
				.removeAboveThreshold4FullCandISD(editDistancesMapUnsortedFullCand, thresholdAsDistance);// distanceScoresSorted=
																											// UtilityBelt.removeAboveThreshold2(distanceScoresSorted,thresholdAsDistance);
		int countCandAfterThresholdPruning = editDistancesMapUnsortedFullCand.size();

		this.thresholdPruningNoEffect = (countCandBeforeThresholdPruning == countCandAfterThresholdPruning);
		// System.out.println("\nDebug note192_263: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		if (!thresholdPruningNoEffect)
		{
			PopUps.showMessage("Ohh..threshold pruning is happening. Are you sure you wanted this?");// +msg);
		}
		// ////////////////////////////////

		if (editDistancesMapUnsortedFullCand.size() == 0)
		{
			System.out.println("Warning: No candidate timelines below threshold distance");
			hasCandidateTimelinesBelowThreshold = false;
			return;
		}

		else
		{
			hasCandidateTimelinesBelowThreshold = true;
		}

		// //////////////////////////////
		// System.out.println("\nDebug note192_282: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		editDistancesSortedMapFullCand = (LinkedHashMap<Integer, Pair<String, Double>>) ComparatorUtils
				.sortByValueAscendingIntStrDoub(editDistancesMapUnsortedFullCand); // Now distanceScoresSorted
		// contains the String Id for

		if (caseType.equals("CaseBasedV1"))
		{
			System.out.println("this is CaseBasedV1");
			// this.editDistanceOfEndPointActivityObjectCand =
			// getEditDistancesOfEndPointActivityObjectCand(candidateTimelines,activitiesGuidingRecomm,caseType);//getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
			this.similarityOfEndPointActivityObjectCand = getCaseSimilarityEndPointActivityObjectCand(
					candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, this.dateAtRecomm.toString(),
					this.timeAtRecomm.toString());// getDistanceScoresforCandidateTimelines(candidateTimelines,activitiesGuidingRecomm);
		}

		this.topNextActivityObjects = fetchNextActivityObjects(editDistancesSortedMapFullCand, candidateTimelines);

		System.out.println("---------editDistancesSortedMapFullCand.size()=" + editDistancesSortedMapFullCand.size());

		if (Constant.verbose)
		{
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");

			System.out.println("\n" + "\n The candidate timelines  in increasing order of distance are:");
			traverseCandidateTimelineWithEditDistance();// editDistancesSortedMapFullCand);

			System.out.println("\nTop next activities are: ");// +this.topNextRecommendedActivities);
			traverseTopNextActivities();
		}
		// System.out.println("\nDebug note192_308: getActivityNamesGuidingRecommwithTimestamps() " +
		// getActivityNamesGuidingRecommwithTimestamps() +
		// " size of current timeline="
		// + currentTimeline.getActivityObjectsInTimeline().size());
		// ########Sanity check
		if (this.topNextActivityObjects.size() == editDistancesSortedMapFullCand.size()
				&& editDistancesSortedMapFullCand.size() == this.candidateTimelines.size())
		{
			// System.out.println("Sanity Check 349 Passed");
		}
		else
		{
			System.err.println(
					"Error at Sanity 349 (RecommenderMasterMU: this.topNextActivityObjects.size() == editDistancesSortedMapFullCand.size() && editDistancesSortedMapFullCand.size()== this.candidateTimelines.size()  not satisfied");
			errorExists = true;
		}

		// ##############
		if (caseType.equals("CaseBasedV1"))
		{
			// rankScore= (1d-(editDistanceValExceptEnd/maxEditDistanceValExceptEnd))* (1d-
			// (endPointActivityEditDistanceVal/maxEditDistanceValOfEnd));
			// createRankedTopRecommendedActivityNamesCaseBasedV1(this.topNextActivityObjects,
			// this.similarityOfEndPointActivityObjectCand);

			// rankScore= (1d-(editDistanceValExceptEnd/maxEditDistanceValExceptEnd))* simEndPointActivityObject;
			createRankedTopRecommendedActivityNamesCaseBasedV1_3(this.topNextActivityObjects,
					this.similarityOfEndPointActivityObjectCand);
		}

		else
		{
			// createRankedTopRecommendedActivityNames
			createRankedTopRecommendedActivityNamesSimpleV3_3(this.topNextActivityObjects);// , this.userAtRecomm,
																							// dateAtRecomm,
																							// timeAtRecomm);
		}

		// System.out.println("Next recommended 5 activity is: "+this.topNextRecommendedActivities);

		/*
		 * IMPORTANT: If the next activity after the current activity object in the current timeline is an invalid
		 * activity, then we can include the current activity in the list of recommended activities, otherwise the
		 * current activity has to be removed from the list of recommended activities
		 */
		if (currentTimeline.getImmediateNextActivityInvalid() == 0) // not invalid
		{
			removeRecommPointActivityFromRankedRecomm();
			System.out.println("removing recomm point activity (Current Activity) from list of recommendation");
		}

		WritingToFile.writeDistanceScoresSortedMapMU(this.userAtRecomm, this.dateAtRecomm, this.timeAtRecomm,
				this.editDistancesSortedMapFullCand, this.candidateTimelines, this.topNextActivityObjects,
				this.activitiesGuidingRecomm, Constant.WriteCandInEditDistancePerRtPerCand,
				Constant.WriteEditOperatationsInEditDistancePerRtPerCand);

		System.out.println("\nDebug note192_end: getActivityNamesGuidingRecommwithTimestamps() "
				+ getActivityNamesGuidingRecommwithTimestamps() + " size of current timeline="
				+ currentTimeline.getActivityObjectsInTimeline().size());
		// System.out.println("Debug note192_2: current timeline " +
		// currentTimeline.getActivityObjectNamesInSequence());

		long recommMasterTEnd = System.currentTimeMillis();

		System.out.println("\n^^^^^^^^^^^^^^^^Exiting Recommendation Master");
	}

	public int getNumberOfDistinctRecommendations()
	{
		return recommendedActivityNamesWithRankscores.size();
	}

	public Date getDateAtRecomm()
	{
		return dateAtRecomm;
	}

	public ActivityObject getActivityObjectAtRecomm()
	{
		return this.activityAtRecommPoint;
	}

	public ArrayList<ActivityObject> getActivitiesGuidingRecomm()
	{
		return activitiesGuidingRecomm;
	}

	public String getActivityNamesGuidingRecomm()
	{
		String res = "";

		for (ActivityObject ae : activitiesGuidingRecomm)
		{
			res += ">>" + ae.getActivityName();
		}
		return res;
	}

	public String getActivityNamesGuidingRecommwithTimestamps()
	{
		String res = "";

		for (ActivityObject ae : activitiesGuidingRecomm)
		{
			res += "  " + ae.getActivityName() + "__" + ae.getStartTimestamp() + "_to_" + ae.getEndTimestamp();
		}
		return res;
	}

	// $$start here

	// /
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
	public void createRankedTopRecommendedActivityNamesCaseBasedV1_2(
			ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityObjectsWithDistance,
			LinkedHashMap<Integer, Double> similarityOfEndPointActivityObjectCand) // we might remove these arguments as
																					// these are already member
																					// variables of
																					// this class
	{
		String topRankedActivityNamesWithScore, topRankedActivityNamesWithoutScore;

		int numberOfTopNextActivityObjects = topNextActivityObjectsWithDistance.size();

		// System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
		// topRecommendationsWithDistance="+topRecommendationsWithDistance);
		System.out.println(
				"Debug inside createRankedTopRecommendedActivityObjects: numberOfTopNextActivityEvenst=numberOfTopNextActivityEvenst");
		// System.out.print("Debug inside createRankedTopRecommendedActivityObjects: the read next activity objects are:
		// ");
		// / for calculating their correlation
		ArrayList<Double> normEditSimilarity = new ArrayList<Double>();
		ArrayList<Double> simEndActivityObjForCorr = new ArrayList<Double>();
		LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<String, Double>(); // <ActivityName,RankScore>
		// /
		Double maxEditDistanceValExceptEnd = 0d, minEditDistanceValExceptEnd = 10000d;

		int numberOfNextAOsAtMaxDistance = 0;
		for (int i = 0; i < numberOfTopNextActivityObjects; i++)
		{
			Double editDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();
			if (editDistanceVal > maxEditDistanceValExceptEnd)
			{
				maxEditDistanceValExceptEnd = editDistanceVal;
				numberOfNextAOsAtMaxDistance = 1;
			}

			else if (editDistanceVal == maxEditDistanceValExceptEnd)
			{
				numberOfNextAOsAtMaxDistance += 1;
			}

			if (editDistanceVal < minEditDistanceValExceptEnd)
			{
				minEditDistanceValExceptEnd = editDistanceVal;
				// numberOfNextAOsAtMaxDistance = 1;
			}
			// else if (editDistanceVal == maxEditDistanceValExceptEnd)
			// {
			// numberOfNextAOsAtMaxDistance += 1;
			// }
		}

		for (int i = 0; i < numberOfTopNextActivityObjects; i++)
		{
			String topNextActivityName = topNextActivityObjectsWithDistance.get(i).getFirst().getActivityName();
			Double editDistanceValExceptEnd = topNextActivityObjectsWithDistance.get(i).getSecond();

			Integer candTimelineID = topNextActivityObjectsWithDistance.get(i).getThird();

			Double simEndPointActivityObject = similarityOfEndPointActivityObjectCand.get(candTimelineID);

			Double simRankScore;// represents similarity

			double normEditDistanceValExceptEnd = minMaxNorm(editDistanceValExceptEnd, maxEditDistanceValExceptEnd,
					minEditDistanceValExceptEnd);

			normEditSimilarity.add(1 - normEditDistanceValExceptEnd);
			simEndActivityObjForCorr.add(simEndPointActivityObject);

			if (Constant.rankScoring.trim().equalsIgnoreCase("product"))
			{
				simRankScore = (1d - normEditDistanceValExceptEnd) * simEndPointActivityObject;
				// simRankScore = (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) *
				// simEndPointActivityObject;
				// System.out.println("Prod RANK SCORE CALC=" + "(1-(" + editDistanceValExceptEnd + "/" +
				// maxEditDistanceValExceptEnd + "))* (" +
				// simEndPointActivityObject + ")");
				System.out.println("Prod RANK SCORE CALC=" + "(1-(" + normEditDistanceValExceptEnd + "))* ("
						+ simEndPointActivityObject + ")");
			}

			else
			{
				simRankScore = Constant.ALPHA * (1d - normEditDistanceValExceptEnd)
						+ (1 - Constant.ALPHA) * simEndPointActivityObject;
				System.out.println("Sum RANK SCORE CALC=" + Constant.ALPHA + "*(1-(" + normEditDistanceValExceptEnd
						+ "))" + "" + "+" + (1 - Constant.ALPHA) + "*(" + simEndPointActivityObject + ")");
				// simRankScore = Constant.BETA * (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) + (1 -
				// Constant.BETA) *
				// simEndPointActivityObject;
				// System.out.println("Sum RANK SCORE CALC=" + Constant.BETA + "*(1-(" + editDistanceValExceptEnd + "/"
				// + maxEditDistanceValExceptEnd + "))" +
				// "" + "+" + (1 - Constant.BETA) +
				// "*("
				// + simEndPointActivityObject + ")");
			}

			if (recommendedActivityNamesRankscorePairs.containsKey(topNextActivityName) == false)
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName, simRankScore);
			}

			else
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName,
						recommendedActivityNamesRankscorePairs.get(topNextActivityName) + simRankScore);
			}
		}

		System.out.println("Num of top next activity objects at Max distance=" + numberOfNextAOsAtMaxDistance);
		if (numberOfNextAOsAtMaxDistance > 5)
		{
			System.out.println("Warning: num of top next aos at max distance(>5) =" + numberOfNextAOsAtMaxDistance);
		}

		recommendedActivityNamesRankscorePairs = (LinkedHashMap<String, Double>) ComparatorUtils
				.sortByValueDesc(recommendedActivityNamesRankscorePairs); // Sorted in
																			// descending
																			// order of
																			// ranked
																			// score:
																			// higher
																			// ranked
																			// score
																			// means
																			// more top
																			// in rank
																			// (larger
																			// numeric
																			// value of
																			// rank)

		// ///////////IMPORTANT //////////////////////////////////////////////////////////
		this.setRecommendedActivityNamesWithRankscores(recommendedActivityNamesRankscorePairs);

		this.setRankedRecommendedActivityNamesWithRankScores(recommendedActivityNamesRankscorePairs);
		this.setRankedRecommendedActivityNamesWithoutRankScores(recommendedActivityNamesRankscorePairs);

		this.setNormEditSimilarity(normEditSimilarity);
		this.setSimEndActivityObjForCorr(simEndActivityObjForCorr);
		// /////////////////////////////////////////////////////////////////////

		topRankedActivityNamesWithScore = getRankedRecommendedActivityNamesWithRankScores();
		topRankedActivityNamesWithoutScore = getRankedRecommendedActivityNamesWithoutRankScores();

		if (Constant.verbose)
		{
			System.out.println(
					"Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithScore=  "
							+ topRankedActivityNamesWithScore);
			System.out.println(
					"Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithoutScore=  "
							+ topRankedActivityNamesWithoutScore);
		}
		// return topRankedString;
	}

	/**
	 * Generates a ranked list of recommended Activity Objects and sets recommendedActivityNamesRankscorePairs
	 * 
	 * and calls the following setter methods setRecommendedActivityNamesWithRankscores
	 * setRankedRecommendedActivityNamesWithRankScores setRankedRecommendedActivityNamesWithoutRankScores
	 * 
	 * Is function with Constants Beta and rank scoring
	 * 
	 * @since IN VERSION 2 WE HAD MIN MAX NORMALISATION INSIDE THIS FUNCTION, IN THIS V3 WE WILL NOT HAVE NORMALISATION
	 *        OF EDIT DISTANCE INSIDE THIS FUNCTION AS THE NORMALISATION IS DONE BEFOREHAND IN THE METHOD WHICH FETCHED
	 *        THE NORMALISED EDIT DISTANCE FOR CANDIDATE TIMELINES
	 * 
	 * @param topNextActivityObjectsWithDistance
	 */
	public void createRankedTopRecommendedActivityNamesCaseBasedV1_3(
			ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityObjectsWithDistance,
			LinkedHashMap<Integer, Double> similarityOfEndPointActivityObjectCand) // we might remove these arguments as
																					// these are already member
																					// variables of
																					// this class
	{
		String topRankedActivityNamesWithScore, topRankedActivityNamesWithoutScore;

		int numberOfTopNextActivityObjects = topNextActivityObjectsWithDistance.size();

		// System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
		// topRecommendationsWithDistance="+topRecommendationsWithDistance);
		System.out.println(
				"Debug inside createRankedTopRecommendedActivityObjects: numberOfTopNextActivityEvenst=numberOfTopNextActivityEvenst");
		// System.out.print("Debug inside createRankedTopRecommendedActivityObjects: the read next activity objects are:
		// ");
		// / for calculating their correlation
		ArrayList<Double> normEditSimilarity = new ArrayList<Double>();
		ArrayList<Double> simEndActivityObjForCorr = new ArrayList<Double>();
		LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<String, Double>(); // <ActivityName,RankScore>

		for (int i = 0; i < numberOfTopNextActivityObjects; i++)
		{
			String topNextActivityName = topNextActivityObjectsWithDistance.get(i).getFirst().getActivityName();
			double normEditDistanceValExceptEnd = topNextActivityObjectsWithDistance.get(i).getSecond();

			Integer candTimelineID = topNextActivityObjectsWithDistance.get(i).getThird();
			Double simEndPointActivityObject = similarityOfEndPointActivityObjectCand.get(candTimelineID);

			Double simRankScore;// represents similarity

			normEditSimilarity.add(1 - normEditDistanceValExceptEnd);
			simEndActivityObjForCorr.add(simEndPointActivityObject);

			if (Constant.rankScoring.trim().equalsIgnoreCase("product"))
			{
				simRankScore = (1d - normEditDistanceValExceptEnd) * simEndPointActivityObject;
				// simRankScore = (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) *
				// simEndPointActivityObject;
				// System.out.println("Prod RANK SCORE CALC=" + "(1-(" + editDistanceValExceptEnd + "/" +
				// maxEditDistanceValExceptEnd + "))* (" +
				// simEndPointActivityObject + ")");
				System.out.println("Prod RANK SCORE CALC=" + "(1-(" + normEditDistanceValExceptEnd + "))* ("
						+ simEndPointActivityObject + ")");
			}

			else
			{
				simRankScore = Constant.ALPHA * (1d - normEditDistanceValExceptEnd)
						+ (1 - Constant.ALPHA) * simEndPointActivityObject;
				System.out.println("Sum RANK SCORE CALC=" + Constant.ALPHA + "*(1-(" + normEditDistanceValExceptEnd
						+ "))" + "" + "+" + (1 - Constant.ALPHA) + "*(" + simEndPointActivityObject + ")");
				// simRankScore = Constant.BETA * (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) + (1 -
				// Constant.BETA) *
				// simEndPointActivityObject;
				// System.out.println("Sum RANK SCORE CALC=" + Constant.BETA + "*(1-(" + editDistanceValExceptEnd + "/"
				// + maxEditDistanceValExceptEnd + "))" +
				// "" + "+" + (1 - Constant.BETA) +
				// "*("
				// + simEndPointActivityObject + ")");
			}

			if (recommendedActivityNamesRankscorePairs.containsKey(topNextActivityName) == false)
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName, simRankScore);
			}

			else
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName,
						recommendedActivityNamesRankscorePairs.get(topNextActivityName) + simRankScore);
			}
		}
		// // Sorted in descending order of ranked score: higher ranked score means more top in rank (larger numeric
		// value of rank)
		recommendedActivityNamesRankscorePairs = (LinkedHashMap<String, Double>) ComparatorUtils
				.sortByValueDesc(recommendedActivityNamesRankscorePairs);
		// ///////////IMPORTANT //////////////////////////////////////////////////////////
		this.setRecommendedActivityNamesWithRankscores(recommendedActivityNamesRankscorePairs);

		this.setRankedRecommendedActivityNamesWithRankScores(recommendedActivityNamesRankscorePairs);
		this.setRankedRecommendedActivityNamesWithoutRankScores(recommendedActivityNamesRankscorePairs);

		this.setNormEditSimilarity(normEditSimilarity);
		this.setSimEndActivityObjForCorr(simEndActivityObjForCorr);
		// /////////////////////////////////////////////////////////////////////

		topRankedActivityNamesWithScore = getRankedRecommendedActivityNamesWithRankScores();
		topRankedActivityNamesWithoutScore = getRankedRecommendedActivityNamesWithoutRankScores();

		if (Constant.verbose)
		{
			System.out.println(
					"Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithScore=  "
							+ topRankedActivityNamesWithScore);
			System.out.println(
					"Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithoutScore=  "
							+ topRankedActivityNamesWithoutScore);
		}
		// return topRankedString;
	}

	public void setNormEditSimilarity(ArrayList<Double> arr)
	{
		this.normEditSimilarity = arr;
	}

	public void setSimEndActivityObjForCorr(ArrayList<Double> arr)
	{
		this.simEndActivityObjForCorr = arr;
	}

	/**
	 * Only relevant for case-based approach
	 * 
	 * @return the correlation between the rest and end similarites of candidate timelines with current timelines
	 */
	public double getRestAndEndSimilaritiesCorrelation()
	{
		if (normEditSimilarity == null || simEndActivityObjForCorr == null)
		{
			if (Constant.caseType.equals("CaseBasedV1"))
			{
				System.err.println("Error: Norm edit similarity and/or sim end activity object are null (not set)");
			}

			// else
			// {
			// System.out.println("Alert: Norm edit similarity and/or sim end activity object are null for caseType =" +
			// Constant.caseType);
			// }

			return Double.NaN;// -99999999;
		}

		PearsonsCorrelation pc = new PearsonsCorrelation();
		return (pc.correlation(UtilityBelt.toPrimitive(normEditSimilarity),
				UtilityBelt.toPrimitive(simEndActivityObjForCorr)));
	}

	public double getAvgRestSimilarity()
	{
		if (normEditSimilarity == null)
		{
			if (Constant.caseType.equals("CaseBasedV1"))
			{
				System.err.println("Error: Norm edit similarity is null (not set)");
			}

			// else
			// {
			// System.out.println("Alert: Norm edit similarity is null for caseType =" + Constant.caseType);
			// }

			return Double.NaN;// -99999999;
		}

		// PearsonsCorrelation pc = new PearsonsCorrelation();
		Mean mean = new Mean();
		return (mean.evaluate(UtilityBelt.toPrimitive(normEditSimilarity), 0, normEditSimilarity.size()));// pc.correlation(UtilityBelt.toPrimitive(normEditSimilarity),
	}

	public double getAvgEndSimilarity()
	{
		if (simEndActivityObjForCorr == null)
		{
			if (Constant.caseType.equals("CaseBasedV1"))
			{
				System.err.println("Error: sim end activity object is null (not set)");
			}

			// else
			// {
			// System.out.println("Alert: sim end activity object is null for caseType =" + Constant.caseType);
			// }
			// System.err.println("Error: sim end activity object are null (not set)");

			return Double.NaN;// -99999999;
		}

		// PearsonsCorrelation pc = new PearsonsCorrelation();
		Mean mean = new Mean();
		return (mean.evaluate(UtilityBelt.toPrimitive(simEndActivityObjForCorr), 0, simEndActivityObjForCorr.size()));// pc.correlation(UtilityBelt.toPrimitive(normEditSimilarity),
		// UtilityBelt.toPrimitive(simEndActivityObjForCorr)));

	}

	public double getSDRestSimilarity()
	{
		if (normEditSimilarity == null)
		{
			if (Constant.caseType.equals("CaseBasedV1"))
			{
				System.err.println("Error: normEditSimilarity is null (not set)");
			}

			// else
			// {
			// System.out.println("Alert: normEditSimilarity is null for caseType =" + Constant.caseType);
			// }
			// System.err.println("Error: Norm edit similarity and sim end activity object are null (not set)");
			return Double.NaN;// -99999999;
		}

		// PearsonsCorrelation pc = new PearsonsCorrelation();
		StandardDeviation sd = new StandardDeviation();
		return (sd.evaluate(UtilityBelt.toPrimitive(normEditSimilarity)));// , 0, normEditSimilarity.size()));//
																			// pc.correlation(UtilityBelt.toPrimitive(normEditSimilarity),
																			// UtilityBelt.toPrimitive(simEndActivityObjForCorr)));

	}

	public double getSDEndSimilarity()
	{
		if (simEndActivityObjForCorr == null)
		{
			if (Constant.caseType.equals("CaseBasedV1"))
			{
				System.err.println("Error: sim end activity object is null (not set)");
			}

			// else
			// {
			// System.out.println("Alert: sim end activity object is null for caseType =" + Constant.caseType);
			// }

			return Double.NaN;// -99999999;
		}

		// PearsonsCorrelation pc = new PearsonsCorrelation();
		StandardDeviation sd = new StandardDeviation();
		return (sd.evaluate(UtilityBelt.toPrimitive(simEndActivityObjForCorr)));// , 0, normEditSimilarity.size()));//
																				// pc.correlation(UtilityBelt.toPrimitive(normEditSimilarity),

	}

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
	public void createRankedTopRecommendedActivityNamesSimpleV3_2(
			ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityObjectsWithDistance, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm)
	// LinkedHashMap<Integer, Double> similarityOfEndPointActivityObjectCand) // we might remove these arguments as
	// these are already member variables of this
	// class
	{
		String topRankedActivityNamesWithScore, topRankedActivityNamesWithoutScore;

		int numberOfTopNextActivityObjects = topNextActivityObjectsWithDistance.size();

		// System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
		// topRecommendationsWithDistance="+topRecommendationsWithDistance);
		System.out.println(
				"Debug inside createRankedTopRecommendedActivityObjects: numberOfTopNextActivityEvenst=numberOfTopNextActivityEvenst");
		// System.out.print("Debug inside createRankedTopRecommendedActivityObjects: the read next activity objects are:
		// ");

		LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<String, Double>(); // <ActivityName,RankScore>

		Double maxEditDistanceVal = 0d, minEditDistanceVal = 10000d;

		int numberOfNextAOsAtMaxDistance = 0;
		for (int i = 0; i < numberOfTopNextActivityObjects; i++)
		{
			Double editDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();
			if (editDistanceVal > maxEditDistanceVal)
			{
				maxEditDistanceVal = editDistanceVal;
				numberOfNextAOsAtMaxDistance = 1;
			}

			else if (editDistanceVal == maxEditDistanceVal)
			{
				numberOfNextAOsAtMaxDistance += 1;
			}

			if (editDistanceVal < minEditDistanceVal)
			{
				minEditDistanceVal = editDistanceVal;
				// numberOfNextAOsAtMaxDistance = 1;
			}
			// else if (editDistanceVal == maxEditDistanceValExceptEnd)
			// {
			// numberOfNextAOsAtMaxDistance += 1;
			// }
		}
		StringBuilder editDistances = new StringBuilder(), normalisedEditDistances = new StringBuilder();

		for (int i = 0; i < numberOfTopNextActivityObjects; i++)
		{
			String topNextActivityName = topNextActivityObjectsWithDistance.get(i).getFirst().getActivityName();
			Double editDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();

			Integer candTimelineID = topNextActivityObjectsWithDistance.get(i).getThird();

			// Double simEndPointActivityObject = similarityOfEndPointActivityObjectCand.get(candTimelineID);

			Double simRankScore;// represents similarity

			double normEditDistanceVal = minMaxNorm(editDistanceVal, maxEditDistanceVal, minEditDistanceVal);

			if (Constant.WriteNormalisation)
			{
				editDistances.append("_" + editDistanceVal);
				normalisedEditDistances.append("_" + normEditDistanceVal);
			}

			simRankScore = (1d - normEditDistanceVal);// * simEndPointActivityObject;
			// simRankScore = (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) *
			// simEndPointActivityObject;
			// System.out.println("Prod RANK SCORE CALC=" + "(1-(" + editDistanceValExceptEnd + "/" +
			// maxEditDistanceValExceptEnd + "))* (" +
			// simEndPointActivityObject + ")");
			System.out.println("Simple RANK SCORE CALC=" + "(1-(" + normEditDistanceVal + "))");// * (" +
																								// simEndPointActivityObject
																								// + ")");

			if (recommendedActivityNamesRankscorePairs.containsKey(topNextActivityName) == false)
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName, simRankScore);
			}

			else
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName,
						recommendedActivityNamesRankscorePairs.get(topNextActivityName) + simRankScore);
			}
		}

		System.out.println("Num of top next activity objects at Max distance=" + numberOfNextAOsAtMaxDistance);
		if (numberOfNextAOsAtMaxDistance > 5)
		{
			System.out.println("Warning: num of top next aos at max distance(>5) =" + numberOfNextAOsAtMaxDistance);
		}

		recommendedActivityNamesRankscorePairs = (LinkedHashMap<String, Double>) ComparatorUtils
				.sortByValueDesc(recommendedActivityNamesRankscorePairs); // Sorted in
																			// descending
																			// order of
																			// ranked
																			// score:
																			// higher
																			// ranked
																			// score
																			// means
		// more top in rank (larger numeric value of rank)

		// ///////////IMPORTANT //////////////////////////////////////////////////////////
		this.setRecommendedActivityNamesWithRankscores(recommendedActivityNamesRankscorePairs);

		this.setRankedRecommendedActivityNamesWithRankScores(recommendedActivityNamesRankscorePairs);
		this.setRankedRecommendedActivityNamesWithoutRankScores(recommendedActivityNamesRankscorePairs);

		// /////////////////////////////////////////////////////////////////////

		topRankedActivityNamesWithScore = getRankedRecommendedActivityNamesWithRankScores();
		topRankedActivityNamesWithoutScore = getRankedRecommendedActivityNamesWithoutRankScores();

		if (Constant.verbose)
		{
			System.out.println(
					"Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithScore=  "
							+ topRankedActivityNamesWithScore);
			System.out.println(
					"Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithoutScore=  "
							+ topRankedActivityNamesWithoutScore);
		}

		if (Constant.WriteNormalisation)
		{
			String toWrite = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + editDistances + ","
					+ normalisedEditDistances + "\n";
			WritingToFile.appendLineToFileAbsolute(toWrite, Constant.getCommonPath() + "NormalisationDistances.csv");
		}
		// return topRankedString;
	}

	/**
	 * Generates a ranked list of recommended Activity Objects and sets recommendedActivityNamesRankscorePairs
	 * 
	 * and calls the following setter methods setRecommendedActivityNamesWithRankscores
	 * setRankedRecommendedActivityNamesWithRankScores setRankedRecommendedActivityNamesWithoutRankScores
	 * 
	 * Is function with Constants Beta and rank scoring
	 * 
	 * @since IN VERSION 2 WE HAD MIN MAX NORMALISATION INSIDE THIS FUNCTION, IN THIS V3 WE WILL NOT HAVE NORMALISATION
	 *        OF EDIT DISTANCE INSIDE THIS FUNCTION AS THE NORMALISATION IS DONE BEFOREHAND IN THE METHOD WHICH FETCHED
	 *        THE NORMALISED EDIT DISTANCE FOR CANDIDATE TIMELINES
	 * @param topNextActivityObjectsWithDistance
	 */
	public void createRankedTopRecommendedActivityNamesSimpleV3_3(
			ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityObjectsWithDistance)
	// LinkedHashMap<Integer, Double> similarityOfEndPointActivityObjectCand) // we might remove these arguments as
	// these are already member variables of this
	// class
	{
		String topRankedActivityNamesWithScore, topRankedActivityNamesWithoutScore;

		int numberOfTopNextActivityObjects = topNextActivityObjectsWithDistance.size();

		// System.out.println("Debug inside createRankedTopRecommendedActivityObjects:
		// topRecommendationsWithDistance="+topRecommendationsWithDistance);
		System.out.println("\nDebug inside createRankedTopRecommendedActivityObjects:");// numberOfTopNextActivityEvenst=numberOfTopNextActivityEvenst");
		// System.out.print("Debug inside createRankedTopRecommendedActivityObjects: the read next activity objects are:
		// ");

		LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<String, Double>(); // <ActivityName,RankScore>

		StringBuilder rankScoreCalc = new StringBuilder();
		for (int i = 0; i < numberOfTopNextActivityObjects; i++)
		{
			String topNextActivityName = topNextActivityObjectsWithDistance.get(i).getFirst().getActivityName();
			double normEditDistanceVal = topNextActivityObjectsWithDistance.get(i).getSecond();

			Integer candTimelineID = topNextActivityObjectsWithDistance.get(i).getThird();

			// Double simEndPointActivityObject = similarityOfEndPointActivityObjectCand.get(candTimelineID);

			double simRankScore;// represents similarity

			simRankScore = (1d - normEditDistanceVal);// * simEndPointActivityObject;
			// simRankScore = (1d - (editDistanceValExceptEnd / maxEditDistanceValExceptEnd)) *
			// simEndPointActivityObject;
			// System.out.println("Prod RANK SCORE CALC=" + "(1-(" + editDistanceValExceptEnd + "/" +
			// maxEditDistanceValExceptEnd + "))* (" +
			// simEndPointActivityObject + ")");
			rankScoreCalc.append("Simple RANK SCORE (1- normED) =" + "1-" + normEditDistanceVal + "\n");// * (" +
																										// simEndPointActivityObject
																										// + ")");

			if (recommendedActivityNamesRankscorePairs.containsKey(topNextActivityName) == false)
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName, simRankScore);
			}

			else
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName,
						recommendedActivityNamesRankscorePairs.get(topNextActivityName) + simRankScore);
			}
		}

		if (Constant.verboseRankScoreCalcToConsole)
		{
			System.out.println(rankScoreCalc.toString());
		}

		// Sorted in descending order of ranked score: higher ranked score means more top in rank (larger numeric value
		// of rank)
		recommendedActivityNamesRankscorePairs = (LinkedHashMap<String, Double>) ComparatorUtils
				.sortByValueDesc(recommendedActivityNamesRankscorePairs);

		// ///////////IMPORTANT //////////////////////////////////////////////////////////
		this.setRecommendedActivityNamesWithRankscores(recommendedActivityNamesRankscorePairs);

		this.setRankedRecommendedActivityNamesWithRankScores(recommendedActivityNamesRankscorePairs);
		this.setRankedRecommendedActivityNamesWithoutRankScores(recommendedActivityNamesRankscorePairs);

		// /////////////////////////////////////////////////////////////////////

		topRankedActivityNamesWithScore = getRankedRecommendedActivityNamesWithRankScores();
		topRankedActivityNamesWithoutScore = getRankedRecommendedActivityNamesWithoutRankScores();

		if (Constant.verbose)
		{
			System.out.println(
					"Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithScore=  "
							+ topRankedActivityNamesWithScore);
			System.out.println(
					"Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithoutScore=  "
							+ topRankedActivityNamesWithoutScore);
		}
		// return topRankedString;
	}

	/**
	 * Returns min max norm if max - min >0 else return 0 (as distance) ...leading to 1 as similarity (rounded off to 4
	 * decimal places)
	 * 
	 * @param val
	 * @param max
	 * @param min
	 * @return
	 */
	public double minMaxNorm(double val, double max, double min)
	{
		if ((max - min) > 0)
		{
			return StatsUtils.round(((val - min) / (max - min)), 4);
		}
		else
		{
			System.err.println("Warning: Alert!! minMaxNorm: max - min <=0 =" + (max - min));
			return 0;
		}

	}

	// /////////////////////////////////////////////////////////////////////
	/**
	 * Set Map of <recommended Activity Name, sim rank score>
	 * 
	 * @param recommendedActivityNamesRankscorePairs
	 */
	public void setRecommendedActivityNamesWithRankscores(
			LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs)
	{
		this.recommendedActivityNamesWithRankscores = recommendedActivityNamesRankscorePairs;
	}

	public LinkedHashMap<String, Double> getRecommendedActivityNamesWithRankscores()
	{
		return this.recommendedActivityNamesWithRankscores;
	}

	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	/**
	 * Set string as '__recommendedActivityName1:simRankScore1__recommendedActivityName2:simRankScore2'
	 * 
	 * @param recommendedActivityNameRankscorePairs
	 */
	public void setRankedRecommendedActivityNamesWithRankScores(
			LinkedHashMap<String, Double> recommendedActivityNameRankscorePairs)
	{
		StringBuilder topRankedString = new StringBuilder();// String topRankedString= new String();
		StringBuilder msg = new StringBuilder();
		for (Map.Entry<String, Double> entry : recommendedActivityNameRankscorePairs.entrySet())
		{
			String recommAct = entry.getKey();
			double roundedRankScore = Evaluation.round(entry.getValue(), 4);
			topRankedString.append("__" + recommAct + ":" + roundedRankScore);
			msg.append("recomm act:" + recommAct + ", rank score: " + roundedRankScore + "\n");
			// topRankedString+= "__"+entry.getKey()+":"+TestStats.round(entry.getValue(),4);
		}

		this.rankedRecommendedActivityNameWithRankScores = topRankedString.toString();

		if (Constant.verboseRankScoreCalcToConsole)
		{
			System.out.println(msg.toString() + "\n");
		}

	}

	public String getRankedRecommendedActivityNamesWithRankScores()
	{
		return this.rankedRecommendedActivityNameWithRankScores;
	}

	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	/**
	 * Set string as '__recommendedActivityName1__recommendedActivityName2'
	 * 
	 * @param recommendedActivityNameRankscorePairs
	 * @return
	 */
	public void setRankedRecommendedActivityNamesWithoutRankScores(
			LinkedHashMap<String, Double> recommendedActivityNameRankscorePairs)
	{
		StringBuilder rankedRecommendationWithoutRankScores = new StringBuilder();// String
																					// rankedRecommendationWithoutRankScores=new
																					// String();
		for (Map.Entry<String, Double> entry : recommendedActivityNameRankscorePairs.entrySet())
		{
			rankedRecommendationWithoutRankScores.append("__" + entry.getKey());// rankedRecommendationWithoutRankScores+=
																				// "__"+entry.getKey();
		}

		this.rankedRecommendedActivityNameWithoutRankScores = rankedRecommendationWithoutRankScores.toString();
	}

	public String getRankedRecommendedActivityNamesWithoutRankScores()
	{
		return this.rankedRecommendedActivityNameWithoutRankScores;
	}

	// /////////////////////////////////////////////////////////////////////

	public void removeRecommPointActivityFromRankedRecomm()
	{
		// String activityNameAtRecommPoint = activityAtRecommPoint.getActivityName();
		this.recommendedActivityNamesWithRankscores.remove(activityNameAtRecommPoint);
	}

	public boolean hasCandidateTimeslines()
	{
		return hasCandidateTimelines;
	}

	/**
	 * Returns top next activity names as String
	 * 
	 * @return
	 */
	public String getTopNextActivityNamesWithoutDistanceString()
	{
		StringBuilder result = new StringBuilder("");// String result="";
		for (int i = 0; i < this.topNextActivityObjects.size(); i++)
		{
			Triple<ActivityObject, Double, Integer> pairAE = this.topNextActivityObjects.get(i);
			result.append("__" + pairAE.getFirst().getActivityName());// result=result+"__"+pairAE.getFirst().getActivityName();
		}
		return result.toString();
	}

	/**
	 * Returns top next activity names with distance as String
	 * 
	 * @return
	 */
	public String getTopNextActivityNamesWithDistanceString()
	{
		StringBuilder result = new StringBuilder(""); // String result="";

		for (int i = 0; i < this.topNextActivityObjects.size(); i++)
		{
			Triple<ActivityObject, Double, Integer> pairAE = this.topNextActivityObjects.get(i);

			result.append("__" + pairAE.getFirst().getActivityName() + ":" + pairAE.getSecond().toString());
			// result=result+"__"+pairAE.getFirst().getActivityName()+":"+pairAE.getSecond().toString();
		}
		return result.toString();

	}

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
	 * 
	 * @param editDistanceSortedFullCand
	 * @param candidateTimelines
	 * @return
	 */
	public ArrayList<Triple<ActivityObject, Double, Integer>> fetchNextActivityObjects(
			LinkedHashMap<Integer, Pair<String, Double>> editDistanceSortedFullCand,
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines)
	{
		ArrayList<Triple<ActivityObject, Double, Integer>> topActivityObjects = new ArrayList<Triple<ActivityObject, Double, Integer>>(); // Pair
																																			// <Next
																																			// Activity
																																			// Object,edit
																																			// distance,
																																			// TimelineID>

		if (editDistanceSortedFullCand.size() < 5)
		{
			System.err.println(
					"\nWarning: the number of similar timelines =" + editDistanceSortedFullCand.size() + " < " + 5);
			// errorExists = true;
		}

		for (Map.Entry<Integer, Pair<String, Double>> entryInSim : editDistanceSortedFullCand.entrySet())
		{
			Integer simCandidateID = entryInSim.getKey();
			TimelineWithNext simCandidateTimeline = candidateTimelines.get(simCandidateID);

			Double editDistanceForSimCandidate = entryInSim.getValue().getSecond();

			topActivityObjects.add(new Triple<ActivityObject, Double, Integer>(
					simCandidateTimeline.getNextActivityObject(), editDistanceForSimCandidate, simCandidateID)); // take
																													// the
																													// next
																													// activity
																													// object
																													// (next
																													// activity
																													// object
																													// is
																													// the
																													// valid
																													// next
																													// activity
																													// object)
		}

		// this.topNextActivityObjects = topActivityObjects;

		return topActivityObjects;
	}

	/**
	 * TODO : write the description for triple
	 * 
	 * @return
	 */
	public ArrayList<Triple<ActivityObject, Double, Integer>> getTopNextActivityObjects()
	{
		return this.topNextActivityObjects;
	}

	public static String getNextValidActivityNameAsCode(String topSimilarUserDayActivitiesAsStringCode,
			int endPointIndexForSubsequenceWithHighestSimilarity)
	{
		String unknownAsCode = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY1);// ("Unknown");
		String othersAsCode = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY2);// ("Others");
		String nextValidActivity = null;
		for (int i = endPointIndexForSubsequenceWithHighestSimilarity + 1; i < topSimilarUserDayActivitiesAsStringCode
				.length(); i++)
		{
			if (String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i)).equals(unknownAsCode)
					|| String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i)).equals(othersAsCode))
			{
				continue;
			}
			else
			{
				nextValidActivity = String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i));
			}
		}
		return nextValidActivity;
	}

	// ////////
	/*
	 * Added: Oct 5, 2014: for IMPORTANT POINT: THE CANDIDATE TIMELINE IS THE DIRECT CANDIDATE TIMELINE AND NOT THE
	 * LEAST DISTANT SUBCANDIDATE.
	 */
	/**
	 * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the edit distance of
	 * that candidate timeline with the current timeline.
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 *            can be 'SimpleV3' or 'CaseBasedV1'
	 * 
	 * @param userAtRecomm
	 *            used only for writing to file
	 * @param dateAtRecomm
	 *            used only for writing to file
	 * @param timeAtRecomm
	 *            used only for writing to file
	 * @return <CanditateTimelineID, Pair<Trace,Edit distance of this candidate>>
	 */
	public LinkedHashMap<Integer, Pair<String, Double>> getHJEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<Integer, Pair<String, Double>> candEditDistances = new LinkedHashMap<Integer, Pair<String, Double>>();

		for (Map.Entry<Integer, TimelineWithNext> entry : candidateTimelines.entrySet())
		{
			// EditSimilarity editSimilarity = new EditSimilarity();
			Pair<String, Double> editDistanceForThisCandidate = null;
			Integer candidateTimelineId = entry.getKey();

			switch (caseType)
			{
			case "CaseBasedV1":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS) // invalids are already expunged, no need to expunge
																	// again
				{
					editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceWithoutEndCurrentActivity(
							entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, new Long(candidateTimelineId));
				}
				else
				{
					editDistanceForThisCandidate = hjEditDistance
							.getHJEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm,
									userAtRecomm, dateAtRecomm, timeAtRecomm, new Long(candidateTimelineId));
				}
				break;

			case "SimpleV3":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithTrace(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
				{
					editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceWithTrace(
							entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, new Long(candidateTimelineId));
				}
				else
				{
					editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceInvalidsExpunged(
							entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, new Long(candidateTimelineId));
				}
				break;

			default:
				System.err.println("Error in getEditDistancesForCandidateTimelineFullCand: unidentified case type");
				break;
			}
			/*
			 * if(caseType.equals("CaseBasedV1")) { editDistanceForThisCandidate =
			 * editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),
			 * activitiesGuidingRecomm); } else //SimpleV3 { editDistanceForThisCandidate =
			 * editSimilarity.getEditDistance(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
			 * }
			 */
			candEditDistances.put(candidateTimelineId, editDistanceForThisCandidate);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}
		return candEditDistances;
	}

	/**
	 * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the edit distance of
	 * that candidate timeline with the current timeline.
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 *            activities from the current timeline
	 * @param caseType
	 *            can be 'SimpleV3' or 'CaseBasedV1'
	 * 
	 * @param userAtRecomm
	 *            used only for writing to file
	 * @param dateAtRecomm
	 *            used only for writing to file
	 * @param timeAtRecomm
	 *            used only for writing to file
	 * @return <CanditateTimelineID, <FeatureName,>Pair<Trace,Edit distance of this candidate>>>
	 */
	public LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> getFeatureWiseEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> candEditDistancesFeatureWise = new LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>>();

		for (Map.Entry<Integer, TimelineWithNext> entry : candidateTimelines.entrySet())
		{
			LinkedHashMap<String, Pair<String, Double>> featureWiseEditDistancesForThisCandidate = null;
			Integer candidateTimelineId = entry.getKey();

			switch (caseType)
			{
			case "CaseBasedV1":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS) // invalids are already expunged, no need to expunge
																	// again
				{
					featureWiseEditDistancesForThisCandidate = featureWiseEditDistance
							.getFeatureWiseEditDistanceWithoutEndCurrentActivity(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);// ,
																												// userAtRecomm,
																												// dateAtRecomm,
																												// timeAtRecomm,
																												// candidateTimelineId);
				}
				else
				{
					featureWiseEditDistancesForThisCandidate = featureWiseEditDistance
							.getFeatureWiseEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);// ,
																												// userAtRecomm,
																												// dateAtRecomm,
																												// timeAtRecomm,
																												// candidateTimelineId);
				}
				break;

			case "SimpleV3":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithTrace(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
				{
					featureWiseEditDistancesForThisCandidate = featureWiseEditDistance
							.getFeatureWiseEditDistanceWithTrace(entry.getValue().getActivityObjectsInTimeline(),
									activitiesGuidingRecomm);// , userAtRecomm, dateAtRecomm, timeAtRecomm,
																// candidateTimelineId);
				}
				else
				{
					featureWiseEditDistancesForThisCandidate = featureWiseEditDistance
							.getFeatureWiseEditDistanceInvalidsExpunged(entry.getValue().getActivityObjectsInTimeline(),
									activitiesGuidingRecomm);// , userAtRecomm, dateAtRecomm, timeAtRecomm,
																// candidateTimelineId);
				}
				break;

			default:
				System.err.println("Error in getEditDistancesForCandidateTimelineFullCand: unidentified case type");
				break;
			}
			/*
			 * if(caseType.equals("CaseBasedV1")) { editDistanceForThisCandidate =
			 * editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),
			 * activitiesGuidingRecomm); } else //SimpleV3 { editDistanceForThisCandidate =
			 * editSimilarity.getEditDistance(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
			 * }
			 */
			candEditDistancesFeatureWise.put(candidateTimelineId, featureWiseEditDistancesForThisCandidate);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}
		return candEditDistancesFeatureWise;
	}

	/**
	 * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the OTMDSAM edit
	 * distance of that candidate timeline with the current timeline.
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 *            can be 'SimpleV3' or 'CaseBasedV1'
	 * 
	 * @param userAtRecomm
	 *            used only for writing to file
	 * @param dateAtRecomm
	 *            used only for writing to file
	 * @param timeAtRecomm
	 *            used only for writing to file
	 * @return <CanditateTimelineID, Pair<Trace,Edit distance of this candidate>>
	 */
	public LinkedHashMap<Integer, Pair<String, Double>> getOTMDSAMEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<Integer, Pair<String, Double>> candEditDistances = new LinkedHashMap<Integer, Pair<String, Double>>();

		for (Map.Entry<Integer, TimelineWithNext> entry : candidateTimelines.entrySet())
		{
			// EditSimilarity editSimilarity = new EditSimilarity();
			Pair<String, Double> editDistanceForThisCandidate = null;
			Integer candidateTimelineId = entry.getKey();

			switch (caseType)
			{
			case "CaseBasedV1":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS) // invalids are already expunged, no need to expunge
																	// again
				{
					editDistanceForThisCandidate = OTMDSAMEditDistance.getOTMDSAMEditDistanceWithoutEndCurrentActivity(
							entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, candidateTimelineId);
				}
				else
				{
					editDistanceForThisCandidate = OTMDSAMEditDistance
							.getOTMDSAMEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm,
									userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelineId);
				}
				break;

			case "SimpleV3":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithTrace(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
				{
					editDistanceForThisCandidate = OTMDSAMEditDistance.getOTMDSAMEditDistanceWithTrace(
							entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, candidateTimelineId);
				}
				else
				{
					editDistanceForThisCandidate = OTMDSAMEditDistance.getOTMDSAMEditDistanceInvalidsExpunged(
							entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, candidateTimelineId);
				}
				break;

			default:
				System.err.println("Error in getEditDistancesForCandidateTimelineFullCand: unidentified case type");
				break;
			}
			/*
			 * if(caseType.equals("CaseBasedV1")) { editDistanceForThisCandidate =
			 * editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),
			 * activitiesGuidingRecomm); } else //SimpleV3 { editDistanceForThisCandidate =
			 * editSimilarity.getEditDistance(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
			 * }
			 */
			candEditDistances.put(candidateTimelineId, editDistanceForThisCandidate);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}
		return candEditDistances;
	}

	/**
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @return
	 */
	public LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> getFeatureWiseWeightedEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> candEditDistancesFeatureWise = new LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>>();

		for (Map.Entry<Integer, TimelineWithNext> entry : candidateTimelines.entrySet())
		{
			LinkedHashMap<String, Pair<String, Double>> featureWiseWeightedEditDistancesForThisCandidate = null;
			Integer candidateTimelineId = entry.getKey();

			switch (caseType)
			{
			case "CaseBasedV1":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS) // invalids are already expunged, no need to expunge
																	// again
				{
					featureWiseWeightedEditDistancesForThisCandidate = featureWiseWeightedEditDistance
							.getFeatureWiseWeightedEditDistanceWithoutEndCurrentActivity(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);// ,
																												// userAtRecomm,
																												// dateAtRecomm,
																												// timeAtRecomm,
																												// candidateTimelineId);
				}
				else
				{
					featureWiseWeightedEditDistancesForThisCandidate = featureWiseWeightedEditDistance
							.getFeatureWiseWeightedEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);// ,
																												// userAtRecomm,
																												// dateAtRecomm,
																												// timeAtRecomm,
																												// candidateTimelineId);
				}
				break;

			case "SimpleV3":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithTrace(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
				{
					featureWiseWeightedEditDistancesForThisCandidate = featureWiseWeightedEditDistance
							.getFeatureWiseWeightedEditDistanceRawValsWithTrace(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);// ,
																												// userAtRecomm,
																												// dateAtRecomm,
																												// timeAtRecomm,
																												// candidateTimelineId);
				}
				else
				{
					featureWiseWeightedEditDistancesForThisCandidate = featureWiseWeightedEditDistance
							.getFeatureWiseWeightedEditDistanceInvalidsExpunged(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);// ,
																												// userAtRecomm,
																												// dateAtRecomm,
																												// timeAtRecomm,
																												// candidateTimelineId);
				}
				break;

			default:
				System.err.println("Error in getEditDistancesForCandidateTimelineFullCand: unidentified case type");
				break;
			}
			/*
			 * if(caseType.equals("CaseBasedV1")) { editDistanceForThisCandidate =
			 * editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),
			 * activitiesGuidingRecomm); } else //SimpleV3 { editDistanceForThisCandidate =
			 * editSimilarity.getEditDistance(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
			 * }
			 */
			candEditDistancesFeatureWise.put(candidateTimelineId, featureWiseWeightedEditDistancesForThisCandidate);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}
		return candEditDistancesFeatureWise;
	}

	/**
	 * Checks for the distance used and calls the appropriate method for getting normalised distance for candidate
	 * timelines.
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param distanceUsed
	 * @return
	 */
	public LinkedHashMap<Integer, Pair<String, Double>> getNormalisedDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm, String distanceUsed)
	{

		switch (distanceUsed)
		{
		case "HJEditDistance":
			return getNormalisedHJEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
					activitiesGuidingRecomm, caseType, this.userIDAtRecomm, this.dateAtRecomm.toString(),
					this.timeAtRecomm.toString());
		case "FeatureWiseEditDistance":
			return getNormalisedFeatureWiseEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
					activitiesGuidingRecomm, caseType, this.userIDAtRecomm, this.dateAtRecomm.toString(),
					this.timeAtRecomm.toString());

		case "FeatureWiseWeightedEditDistance":
			return getNormalisedFeatureWiseWeightedEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
					activitiesGuidingRecomm, caseType, this.userIDAtRecomm, this.dateAtRecomm.toString(),
					this.timeAtRecomm.toString());

		case "OTMDSAMEditDistance":
			return getNormalisedOTMDSAMEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
					activitiesGuidingRecomm, caseType, this.userIDAtRecomm, this.dateAtRecomm.toString(),
					this.timeAtRecomm.toString());
		// break;
		default:
			PopUps.showError(
					"Error in org.activity.recomm.RecommendationMasterMU.getNormalisedDistancesForCandidateTimelinesFullCand(): Unknown distance specified:"
							+ distanceUsed);
			System.err.println(
					"Error in org.activity.recomm.RecommendationMasterMU.getNormalisedDistancesForCandidateTimelinesFullCand(): Unknown distance specified:"
							+ distanceUsed);
			// throw new Exception("Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance
			// specified:" + dname);
			System.exit(-1);
		}
		System.err.println(
				"Error in org.activity.recomm.RecommendationMasterMU.getNormalisedDistancesForCandidateTimelinesFullCand()"
						+ " reaching unreachable code");
		System.exit(-2);
		return null;
	}

	/**
	 * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the edit distance of
	 * that candidate timeline with the current timeline.
	 * 
	 * DOING THE NORMALISATION HERE ITSELF AND SEE IF IT GIVES DIFFERENT RESULT THAN DOING NORMALISATION WHILE
	 * CALCULATING SCORE. CHECKED: SAME RESULTS, NORMALISATION CORRECT
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 *            can be 'SimpleV3' or 'CaseBasedV1'
	 * 
	 * @param userAtRecomm
	 *            used only for writing to file
	 * @param dateAtRecomm
	 *            used only for writing to file
	 * @param timeAtRecomm
	 *            used only for writing to file
	 * @return <CanditateTimelineID, Pair<Trace,Edit distance of this candidate>>
	 */
	public LinkedHashMap<Integer, Pair<String, Double>> getNormalisedHJEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm)
	{
		LinkedHashMap<Integer, Pair<String, Double>> candEditDistances = getHJEditDistancesForCandidateTimelinesFullCand(
				candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm);

		LinkedHashMap<Integer, Pair<String, Double>> normalisedCandEditDistances = normalisedDistancesOverTheSet(
				candEditDistances, userAtRecomm, dateAtRecomm, timeAtRecomm);

		return normalisedCandEditDistances;
	}

	/**
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @return
	 */
	public LinkedHashMap<Integer, Pair<String, Double>> getNormalisedFeatureWiseEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm)
	{
		LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> candEditDistancesFeatureWise = getFeatureWiseEditDistancesForCandidateTimelinesFullCand(
				candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm);

		LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> normalisedCandEditDistances = normalisedFeatureWiseDistancesOverTheSet(
				candEditDistancesFeatureWise);
		LinkedHashMap<Integer, Pair<String, Double>> aggregatedNormalisedCandEditDistances = aggregatedFeatureWiseDistancesForCandidateTimelinesFullCand(
				normalisedCandEditDistances);

		if (Constant.verboseNormalisation)
		{
			traverseLLP(normalisedCandEditDistances, " Normalised Feature wise Edit Distances");
			traverseLP(aggregatedNormalisedCandEditDistances, "Aggregated Normalised  Feature wise Edit Distances");
		}
		return aggregatedNormalisedCandEditDistances;
	}

	public LinkedHashMap<Integer, Pair<String, Double>> getNormalisedOTMDSAMEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm)
	{
		LinkedHashMap<Integer, Pair<String, Double>> candEditDistances = getOTMDSAMEditDistancesForCandidateTimelinesFullCand(
				candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm);

		LinkedHashMap<Integer, Pair<String, Double>> normalisedCandEditDistances = normalisedDistancesOverTheSet(
				candEditDistances, userAtRecomm, dateAtRecomm, timeAtRecomm);

		return normalisedCandEditDistances;
	}

	/**
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @return
	 */
	public LinkedHashMap<Integer, Pair<String, Double>> getNormalisedFeatureWiseWeightedEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm)
	{
		LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> candEditDistancesFeatureWise = getFeatureWiseWeightedEditDistancesForCandidateTimelinesFullCand(
				candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm);

		LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> normalisedCandEditDistances = normalisedFeatureWiseDistancesOverTheSet(
				candEditDistancesFeatureWise);
		LinkedHashMap<Integer, Pair<String, Double>> aggregatedNormalisedCandEditDistances = aggregatedFeatureWiseDistancesForCandidateTimelinesFullCand(
				normalisedCandEditDistances);

		if (Constant.verboseNormalisation)
		{
			traverseLLP(normalisedCandEditDistances, " Normalised Weighted Edit Distances");
			traverseLP(aggregatedNormalisedCandEditDistances, "Aggregated Normalised Weighted Edit Distances");
		}
		return aggregatedNormalisedCandEditDistances;
	}

	public void traverseLLP(LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> map, String name)
	{
		System.out.println("-----------Traversing " + name);

		for (Map.Entry<Integer, LinkedHashMap<String, Pair<String, Double>>> entry : map.entrySet()) // iterating over
																										// cands
		{
			System.out.print("Cand=" + entry.getKey());
			LinkedHashMap<String, Pair<String, Double>> featureWiseDistances = entry.getValue();

			for (Map.Entry<String, Pair<String, Double>> distEntry : featureWiseDistances.entrySet()) // iterating over
																										// distance for
																										// each feature
			{
				System.out.print("Feature names=" + distEntry.getKey());
				System.out.print("val =" + distEntry.getValue().getSecond());
			}
			System.out.println();
		}
		System.out.println("-----------");

	}

	public void traverseLP(LinkedHashMap<Integer, Pair<String, Double>> map, String name)
	{
		System.out.println("-----------Traversing " + name);

		for (Map.Entry<Integer, Pair<String, Double>> distEntry : map.entrySet()) // iterating over distance for each
																					// feature
		{
			System.out.print("Cand =" + distEntry.getKey());
			System.out.print("val =" + distEntry.getValue().getSecond());
		}
		System.out.println();

		System.out.println("-----------");

	}

	/**
	 * 
	 * @param setOfFeatureWiseDistances
	 * @return
	 */
	public LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> normalisedFeatureWiseDistancesOverTheSet(
			LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> setOfFeatureWiseDistances)
	{

		LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> normalisedDistancesPerCand = new LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>>();

		int numOfFeatures = Constant.getNumberOfFeatures();

		double[] maxs = new double[numOfFeatures];// maxs for each feature
		double[] mins = new double[numOfFeatures];

		for (int i = 0; i < numOfFeatures; i++)
		{
			maxs[i] = Constant.minForNorm;
			mins[i] = Constant.maxForNorm;
		}

		for (Map.Entry<Integer, LinkedHashMap<String, Pair<String, Double>>> entry : setOfFeatureWiseDistances
				.entrySet()) // iterating over cands
		{
			Integer candID = entry.getKey();
			LinkedHashMap<String, Pair<String, Double>> featureWiseDistances = entry.getValue();

			int featureIndex = 0;
			for (Map.Entry<String, Pair<String, Double>> distEntry : featureWiseDistances.entrySet()) // iterating over
																										// distance for
																										// each feature
			{
				String featureName = distEntry.getKey();
				double distanceValue = distEntry.getValue().getSecond();
				if (Constant.verboseNormalisation)
					System.out.println("reading:" + featureName + "  distance:" + distanceValue);

				if (distanceValue > maxs[featureIndex])
				{
					maxs[featureIndex] = distanceValue;
					// if (Constant.verboseNormalisation)
					// System.out.println("maxs[" + featureIndex + "] = " + distanceValue);
				}
				// else
				// {
				// if (Constant.verboseNormalisation)
				// System.out.println("no effect max");
				// }

				if (distanceValue < mins[featureIndex])
				{
					mins[featureIndex] = distanceValue;
					// if (Constant.verboseNormalisation)
					// System.out.println("mins[" + featureIndex + "] = " + distanceValue);
				}

				// else
				// {
				// if (Constant.verboseNormalisation)
				// System.out.println("no effect min");
				// }
				featureIndex++;
			}
		}
		System.out.print("Before normalisation:\n");
		if (Constant.verboseNormalisation)
		{
			for (Map.Entry<Integer, LinkedHashMap<String, Pair<String, Double>>> entry : setOfFeatureWiseDistances
					.entrySet()) // iterating over cands
			{
				System.out.print("Cand id:" + entry.getKey() + "-");
				LinkedHashMap<String, Pair<String, Double>> featureWiseDistances = entry.getValue();

				for (Map.Entry<String, Pair<String, Double>> distEntry : featureWiseDistances.entrySet()) // iterating
																											// over
																											// distance
																											// for each
																											// feature
				{
					System.out.print(distEntry.getKey() + ":" + distEntry.getValue().getSecond() + " ");
				}
				System.out.println();
			}

			for (int k = 0; k < numOfFeatures; k++)
			{
				System.out.println(" max for " + (k + 1) + "th feature=" + maxs[k]);
				System.out.println(" min for " + (k + 1) + "th feature=" + mins[k]);
			}
		}

		// LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> normalisedDistancesPerCand

		for (Map.Entry<Integer, LinkedHashMap<String, Pair<String, Double>>> entry : setOfFeatureWiseDistances
				.entrySet()) // iterating over cands
		{
			Integer candID = entry.getKey();
			LinkedHashMap<String, Pair<String, Double>> featureWiseDistances = entry.getValue();
			LinkedHashMap<String, Pair<String, Double>> normalisedFeatureWiseDistances = new LinkedHashMap<String, Pair<String, Double>>();

			int featureIndex = 0;
			for (Map.Entry<String, Pair<String, Double>> distEntry : featureWiseDistances.entrySet()) // iterating over
																										// distance for
																										// each feature
			{
				String featureName = distEntry.getKey();
				double distanceValue = distEntry.getValue().getSecond();
				double normalisedDistanceValue = minMaxNorm(distanceValue, maxs[featureIndex], mins[featureIndex]);
				normalisedFeatureWiseDistances.put(featureName,
						new Pair<String, Double>(distEntry.getValue().getFirst(), normalisedDistanceValue));
				featureIndex++;
			}
			normalisedDistancesPerCand.put(candID, normalisedFeatureWiseDistances);
		}

		return normalisedDistancesPerCand;
	}

	public LinkedHashMap<Integer, Pair<String, Double>> aggregatedFeatureWiseDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<Integer, LinkedHashMap<String, Pair<String, Double>>> normalisedCandEditDistances)
	{
		LinkedHashMap<Integer, Pair<String, Double>> aggregatedFeatureWiseDistances = new LinkedHashMap<Integer, Pair<String, Double>>();

		for (Map.Entry<Integer, LinkedHashMap<String, Pair<String, Double>>> entry : normalisedCandEditDistances
				.entrySet()) // iterating over cands
		{
			Integer candID = entry.getKey();
			LinkedHashMap<String, Pair<String, Double>> normalisedFeatureWiseDistances = entry.getValue();

			int featureIndex = 0;
			double distanceAggregatedOverFeatures = 0;

			for (Map.Entry<String, Pair<String, Double>> distEntry : normalisedFeatureWiseDistances.entrySet()) // iterating
																												// over
																												// distance
																												// for
																												// each
																												// feature
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

	/**
	 * Normalises the given edit distance over the candidates, i.e., does a min-max normalisation over the set of edit
	 * distance passed to it.
	 * 
	 * @param setOfDistances
	 * @param userAtRecomm
	 *            just for writing to file
	 * @param dateAtRecomm
	 *            just for writing to file
	 * @param timeAtRecomm
	 *            just for writing to file
	 * @return distances normalised over the set.
	 */
	public LinkedHashMap<Integer, Pair<String, Double>> normalisedDistancesOverTheSet(
			LinkedHashMap<Integer, Pair<String, Double>> setOfDistances, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm)
	{
		LinkedHashMap<Integer, Pair<String, Double>> normalisedDistances = new LinkedHashMap<Integer, Pair<String, Double>>();

		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

		int numOfValsAtMax = 0, numOfValsAtMin = 0;
		StringBuilder editDistances = new StringBuilder(), normalisedEditDistances = new StringBuilder();
		ArrayList<Double> editDistancesList = new ArrayList<Double>();
		ArrayList<Double> normalisedEditDistancesList = new ArrayList<Double>();

		int i = 0;
		for (Map.Entry<Integer, Pair<String, Double>> distEntry : setOfDistances.entrySet())
		{
			// Integer candTimelineID = distEntry.getKey();
			Double editDistanceVal = distEntry.getValue().getSecond();

			if (Constant.WriteNormalisation)
			{
				editDistances.append("_" + editDistanceVal);
				editDistancesList.add(editDistanceVal);
			}
			if (editDistanceVal < min)
			{
				min = editDistanceVal;
				numOfValsAtMin = 1;
			}
			else if (editDistanceVal == min)
			{
				numOfValsAtMin++;
			}

			if (editDistanceVal > max)
			{
				max = editDistanceVal;
				numOfValsAtMax = 1;
			}
			else if (editDistanceVal == max)
			{
				numOfValsAtMax++;
			}
			i++;
		}

		for (Map.Entry<Integer, Pair<String, Double>> distEntry : setOfDistances.entrySet())
		{
			Double normalisedEditDistanceVal = Double.valueOf(minMaxNorm(distEntry.getValue().getSecond(), max, min));

			if (Constant.WriteNormalisation)
			{
				normalisedEditDistances.append("_" + normalisedEditDistanceVal);
				normalisedEditDistancesList.add(normalisedEditDistanceVal);
			}
			normalisedDistances.put(distEntry.getKey(),
					new Pair<String, Double>(distEntry.getValue().getFirst(), normalisedEditDistanceVal));
		}

		if (Constant.WriteNormalisation && !Constant.WriteNormalisationsSeparateLines)
		{
			Collections.sort(normalisedEditDistancesList);
			Collections.sort(editDistancesList);
			String toWrite = userAtRecomm + "||" + dateAtRecomm + "||" + timeAtRecomm + "||" + editDistancesList + "||"
					+ normalisedEditDistancesList + "\n";
			WritingToFile.appendLineToFileAbsolute(toWrite, Constant.getCommonPath() + "NormalisationDistances.csv");
		}

		if (Constant.WriteNormalisationsSeparateLines)
		{
			Collections.sort(normalisedEditDistancesList);
			Collections.sort(editDistancesList);
			int j = 0;
			for (Double raw : editDistancesList)
			{
				String toWrite = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + raw + ","
						+ normalisedEditDistancesList.get(j) + "\n";
				WritingToFile.appendLineToFileAbsolute(toWrite,
						Constant.getCommonPath() + "NormalisationDistances.csv");
				j++;
			}

		}

		System.out.println(
				"getNormalisedDistancesOverTheSet: #Vals max=" + numOfValsAtMax + " #Vals min=" + numOfValsAtMin);
		return normalisedDistances;
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
	 * @return <CanditateTimelineID to which this end point Activity Object belongs, edit distance of this end point
	 *         Activity Object of this candidate with end point Activity Object>
	 */
	public LinkedHashMap<Integer, Double> getCaseSimilarityEndPointActivityObjectCand(
			LinkedHashMap<Integer, TimelineWithNext> candidateTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String caseType, int userID, String dateAtRecomm,
			String timeAtRecomm)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<Integer, Double> candEndPointEditDistances = new LinkedHashMap<Integer, Double>();

		for (Map.Entry<Integer, TimelineWithNext> entry : candidateTimelines.entrySet())
		{
			TimelineWithNext candInConcern = entry.getValue();
			ArrayList<ActivityObject> activityObjectsInCand = candInConcern.getActivityObjectsInTimeline();

			// EditSimilarity editSimilarity = new EditSimilarity();
			Double endPointEditDistanceForThisCandidate = new Double(-9999);
			if (caseType.equals("CaseBasedV1")) // CaseBasedV1
			{
				ActivityObject endPointActivityObjectCandidate = (activityObjectsInCand
						.get(activityObjectsInCand.size() - 1)); // only the end point activity
																	// object
				ActivityObject endPointActivityObjectCurrentTimeline = (activitiesGuidingRecomm
						.get(activitiesGuidingRecomm.size() - 1)); // activityObjectAtRecommPoint

				switch (Constant.getDatabaseName())
				// (Constant.DATABASE_NAME)
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
					System.err.println(
							"Error in getCaseSimilarityEndPointActivityObjectCand: unrecognised database name");
					break;
				}

			}

			else if (caseType.equals("SimpleV3"))
			{
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistance(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				System.err.println(
						"ERROR in getEditDistancesOfEndPointActivityCand(): This method should not have been called for case type="
								+ caseType);
				errorExists = true;
				endPointEditDistanceForThisCandidate = null;
			}
			else
			{
				errorExists = true;
				endPointEditDistanceForThisCandidate = null;
			}
			if (endPointEditDistanceForThisCandidate < 0)
			{
				System.err.println(
						"Error in getCaseSimilarityEndPointActivityObjectCand: endPointEditDistanceForThisCandidate "
								+ endPointEditDistanceForThisCandidate + " is not correct");
				System.exit(-99);
			}

			candEndPointEditDistances.put(entry.getKey(), endPointEditDistanceForThisCandidate);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}

		return candEndPointEditDistances;
	}

	// /

	public String getStringCodeOfActivityObjects(ArrayList<ActivityObject> activityObjects)
	{
		String code = new String();

		for (int i = 0; i < activityObjects.size(); i++)
		{
			// String activityName= activityObjects.get(i).getActivityName();
			// int activityID= generateSyntheticData.getActivityid(activityName);

			code += activityObjects.get(i).getStringCode(); // Character.toString ((char)(activityID+65)); //getting the
															// ascii code for (activity id+65)
		}

		return code;
	}

	/**
	 * Returns candidate timelines extracted from the training timeline.
	 * 
	 * @param trainingTimeline
	 * @param matchingUnit
	 * @param lookPastType
	 * @return
	 */
	public LinkedHashMap<Integer, TimelineWithNext> getCandidateTimelinesMU(Timeline trainingTimeline,
			double matchingUnit, String lookPastType)
	{
		if (lookPastType.equalsIgnoreCase("Count"))
		{
			if (matchingUnit % 1 != 0)
			{
				System.out.println("Warning: matching unit" + matchingUnit
						+ " is not integer while the lookPastType is Count. We will use the integer value.");
			}
			return getCandidateTimelinesMUCount(trainingTimeline, new Double(matchingUnit).intValue());
		}

		else if (lookPastType.equalsIgnoreCase("Hrs"))
		{
			return getCandidateTimelinesMUHours(trainingTimeline, matchingUnit);
		}
		else
		{
			System.err.println("Error in getCandidateTimelinesMU: Unrecognised matching unit type " + lookPastType);
			System.exit(-2);
			return null;
		}
	}

	/**
	 * Create and fetch candidate timelines from the training timelines. Finding Candidate timelines: iterate through
	 * the training timelines for each occurence of the Current Activity Name in the candidate timeline, extract the
	 * sequence of activity objects from that occurrence_index back until the matching unit number of activity objects
	 * this forms a candidate timeline
	 * 
	 * @param dayTimelinesForUser
	 * @return
	 */
	public LinkedHashMap<Integer, TimelineWithNext> getCandidateTimelinesMUCount(Timeline trainingTimeline,
			int matchingUnitInCounts)// ,
										// /*ArrayList<ActivityObject>
										// activitiesGuidingRecomm,*/
										// //Date//dateAtRecomm)
	{
		int count = 0;
		// int matchingUnitInCounts = (int) this.matchingUnitInCountsOrHours;
		LinkedHashMap<Integer, TimelineWithNext> candidateTimelines = new LinkedHashMap<Integer, TimelineWithNext>();

		// $$System.out.println("\nInside getCandidateTimelines()");// for creating timelines");
		// totalNumberOfProbableCands=0;
		// numCandsRejectedDueToNoCurrentActivityAtNonLast=0;
		int numCandsRejectedDueToNoValidNextActivity = 0;
		ArrayList<ActivityObject> activityObjectsInTraining = trainingTimeline.getActivityObjectsInTimeline();
		// $$System.out.println("Number of activity objects in training timeline=" + activityObjectsInTraining.size());
		// $$System.out.println("Current activity (activityAtRecommPoint)=" +
		// this.activityAtRecommPoint.getActivityName());
		// trainingTimeline.printActivityObjectNamesWithTimestampsInSequence();

		/**
		 * Note: candidate timelines can be formed from the first index of the training timeline UNLIKE matching unit in
		 * hours
		 */
		for (int i = 0; i < activityObjectsInTraining.size() - 1; i++) // starting from the first activity and goes
																		// until second last activity.
		{
			ActivityObject ae = activityObjectsInTraining.get(i);

			if (ae.getActivityName().equals(this.activityNameAtRecommPoint)) // same name as current activity
			{
				// Timestamp newCandEndTimestamp= new
				// Timestamp(ae.getStartTimestamp().getTime()+ae.getDurationInSeconds()*1000-1000); //decreasing 1
				// second
				// (because this is convention followed
				// in
				// data generation)
				int newCandEndIndex = i;
				// NOTE: going back matchingUnitCounts FROM THE index.
				int newCandStartIndex = (newCandEndIndex - matchingUnitInCounts) >= 0
						? (newCandEndIndex - matchingUnitInCounts) : 0;

				// $$System.out.println("\n\tStart index of candidate timeline=" + newCandStartIndex);
				// $$System.out.println("\tEnd index of candidate timeline=" + newCandEndIndex);

				ArrayList<ActivityObject> activityObjectsForCandidate = trainingTimeline
						.getActivityObjectsInTimelineFromToIndex(newCandStartIndex, newCandEndIndex + 1);
				// getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp);
				ActivityObject nextValidActivityForCandidate = trainingTimeline
						.getNextValidActivityAfterActivityAtThisPosition(newCandEndIndex);

				if (nextValidActivityForCandidate == null)
				{
					numCandsRejectedDueToNoValidNextActivity += 1;
					System.out.println("\tThis candidate rejected due to no next valid activity object;");
					continue;
				}
				TimelineWithNext newCandidate = new TimelineWithNext(activityObjectsForCandidate,
						nextValidActivityForCandidate);// trainingTimeline.getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp));
				// $$System.out.println("Created new candidate timeline (with next)");
				// $$System.out.println("\tActivity names:" + newCandidate.getActivityObjectNamesInSequence());
				// $$System.out.println("\tNext activity:" + newCandidate.getNextActivityObject().getActivityName());
				candidateTimelines.put(newCandidate.getTimelineID(), newCandidate);
			}
		}
		return candidateTimelines;
	}

	/**
	 * Create and fetch candidate timelines from the training timelines
	 * 
	 * @param dayTimelinesForUser
	 * @return
	 */
	public LinkedHashMap<Integer, TimelineWithNext> getCandidateTimelinesMUHours(Timeline trainingTimeline,
			double matchingUnitInHours)// ,
										// /*ArrayList<ActivityObject>
										// activitiesGuidingRecomm,*/
										// //Date//dateAtRecomm)
	{
		int count = 0;
		LinkedHashMap<Integer, TimelineWithNext> candidateTimelines = new LinkedHashMap<Integer, TimelineWithNext>();

		System.out.println("\nInside getCandidateTimelines()");// for creating timelines");
		// totalNumberOfProbableCands=0;
		// numCandsRejectedDueToNoCurrentActivityAtNonLast=0;
		int numCandsRejectedDueToNoValidNextActivity = 0;
		ArrayList<ActivityObject> activityObjectsInTraining = trainingTimeline.getActivityObjectsInTimeline();
		System.out.println("Number of activity objects in training timeline=" + activityObjectsInTraining.size());
		System.out.println("Current activity (activityAtRecommPoint)=" + this.activityNameAtRecommPoint);
		// trainingTimeline.printActivityObjectNamesWithTimestampsInSequence();

		for (int i = 1; i < activityObjectsInTraining.size() - 1; i++) // starting from the second activity and goes
																		// until second last activity.
		{
			ActivityObject ae = activityObjectsInTraining.get(i);

			if (ae.getActivityName().equals(this.activityNameAtRecommPoint)) // same name as current activity
			{
				Timestamp newCandEndTimestamp = new Timestamp(
						ae.getStartTimestamp().getTime() + ae.getDurationInSeconds() * 1000 - 1000); // decreasing 1
																										// second
																										// (because
																										// this is
																										// convention
																										// followed
																										// in data
																										// generation)

				// NOTE: going back matchingUnitHours FROM THE START TIMESTAMP and not the end timestamp.

				// this cast is safe because in this case number of milliseconds won't be in decimals
				long matchingUnitInMilliSeconds = (long) (matchingUnitInHours * 60 * 60 * 1000);// .multiply(new
																								// BigDecimal(60*60*1000)).longValue();
				Timestamp newCandStartTimestamp = new Timestamp(
						ae.getStartTimestamp().getTime() - matchingUnitInMilliSeconds);

				// $$System.out.println("\n\tStarttime of candidate timeline=" + newCandStartTimestamp);
				// $$System.out.println("\tEndtime of candidate timeline=" + newCandEndTimestamp);

				/*
				 * Note: if newCandStartTimestamp here is earlier than when the training timeline started, even then
				 * this works correctly since we are considering intersection of what is available
				 */
				ArrayList<ActivityObject> activityObjectsForCandidate = trainingTimeline
						.getActivityObjectsBetweenTime(newCandStartTimestamp, newCandEndTimestamp);
				ActivityObject nextValidActivityForCandidate = trainingTimeline
						.getNextValidActivityAfterActivityAtThisTime(newCandEndTimestamp);

				if (nextValidActivityForCandidate == null)
				{
					numCandsRejectedDueToNoValidNextActivity += 1;
					System.out.println("\tThis candidate rejected due to no next valid activity object;");
					continue;
				}
				TimelineWithNext newCandidate = new TimelineWithNext(activityObjectsForCandidate,
						nextValidActivityForCandidate);// trainingTimeline.getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp));
				// $$System.out.println("Created new candidate timeline (with next)");
				// $$System.out.println("\tActivity names:" + newCandidate.getActivityObjectNamesInSequence());
				// $$System.out.println("\tNext activity:" + newCandidate.getNextActivityObject().getActivityName());

				candidateTimelines.put(newCandidate.getTimelineID(), newCandidate);
			}
		}

		return candidateTimelines;
	}

	public ArrayList<Integer> getIndicesOfEndPointActivityInDayButNotAsLast(String userDayActivitiesAsStringCode,
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

	public ArrayList<Integer> getIndicesOfEndPointActivityInTimeline(String userActivitiesAsStringCode,
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
	 * Returns a Map of candidate timelines
	 * 
	 * @return
	 */
	public LinkedHashMap<Integer, TimelineWithNext> getCandidateTimeslines()
	{
		return this.candidateTimelines;
	}

	public void traverseMapOfTimelines(LinkedHashMap<String, Timeline> map)
	{
		System.out.println("traversing map of day timelines");

		for (Map.Entry<String, Timeline> entry : map.entrySet())
		{
			System.out.print("ID: " + entry.getKey());
			entry.getValue().printActivityObjectNamesInSequence();
			System.out.println();
		}
		System.out.println("-----------");
	}

	public void traverseMapOfTimelinesWithNext(LinkedHashMap<Integer, TimelineWithNext> map)
	{
		System.out.println("traversing map of timelines");

		for (Map.Entry<Integer, TimelineWithNext> entry : map.entrySet())
		{
			System.out.print("ID: " + entry.getKey());
			entry.getValue().printActivityObjectNamesInSequence();
			System.out.println("\t ** Next Activity Object (name=): "
					+ entry.getValue().getNextActivityObject().getActivityName());
		}
		System.out.println("-----------");
	}

	public long getSumOfActivityObjects(LinkedHashMap<Integer, TimelineWithNext> map)
	{
		long count = 0;

		for (Map.Entry<Integer, TimelineWithNext> entry : map.entrySet())
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

	/**
	 * Traverse candidate timelines with edit distance and print them.
	 * 
	 * @param editDistancesSortedMapFullCand
	 * @return
	 */
	public int traverseCandidateTimelineWithEditDistance()// LinkedHashMap<Integer, Pair<String, Double>>
															// editDistancesSortedMapFullCand)
	{
		if (editDistancesSortedMapFullCand.size() > 0)
		{
			for (Map.Entry<Integer, Pair<String, Double>> entry : editDistancesSortedMapFullCand.entrySet())
			{
				Integer timelineID = entry.getKey();
				Double editDistance = entry.getValue().getSecond();

				System.out.println("\n\tCand timeline ID: " + timelineID);
				System.out.print("\tEdit dist: " + editDistance);

				Timeline candidateTimeline = this.candidateTimelines.get(timelineID);

				ArrayList<ActivityObject> activityObjectsInCand = candidateTimeline.getActivityObjectsInTimeline();
				System.out.println("\nact objs:");
				for (int i = 0; i < activityObjectsInCand.size(); i++)
				{
					System.out.print(">>" + activityObjectsInCand.get(i).getActivityName());

				}
			}
		}
		else
		{
			System.out.println(
					"Warning in traverseCandidateTimelineWithEditDistance(): editDistancesSortedMapFullCand is empty.");
		}
		return 0;
	}

	/**
	 * Traverse top next activity objects' names with the edit distance of the candidate timeline which produced them
	 * 
	 * @return
	 */
	public int traverseTopNextActivities()
	{
		System.out.println();
		if (this.topNextActivityObjects.size() > 0)
		{
			for (int i = 0; i < this.topNextActivityObjects.size(); i++)
			{
				ActivityObject ae = this.topNextActivityObjects.get(i).getFirst();
				Double dist = this.topNextActivityObjects.get(i).getSecond();
				System.out.print(" >>" + ae.getActivityName() + ":" + dist);
			}
		}
		else
		{
			System.out.println("Warning in traverseTopNextActivities(): there are no top next activity objects.");
		}
		return 0;
	}

	public int getNumberOfValidActivitiesInActivitesGuidingRecommendation()
	{
		int count = 0;
		for (ActivityObject ae : this.activitiesGuidingRecomm)
		{
			if (UtilityBelt.isValidActivityName(ae.getActivityName()))
			// (!( (ae.getActivityName().equalsIgnoreCase("Others")) ||
			// (ae.getActivityName().equalsIgnoreCase("Unknown")) ))
			{
				count++;
			}
		}
		return count++;
	}

	public int getNumberOfActivitiesInActivitesGuidingRecommendation()
	{
		return this.activitiesGuidingRecomm.size();
	}

	public int getNumberOfCandidateTimelinesBelowThreshold() // satisfying threshold
	{
		// if(hasCandidateTimelinesBelowThreshold==false)
		// {
		// System.err.println("Error: Sanity Check RM60 failed: trying to get number of candidate timelines below
		// threshold while there is no candidate below threshold, u shouldnt
		// have called this function");
		// }
		/*
		 * Assuming that threshold has already been applied
		 */
		return this.editDistancesSortedMapFullCand.size();
	}

	/**
	 * 
	 * @return
	 */
	public double getThresholdAsDistance()
	{
		return thresholdAsDistance;
	}

	public boolean isNextActivityJustAfterRecommPointIsInvalid()
	{
		return this.nextActivityJustAfterRecommPointIsInvalid;
	}

	public boolean hasThresholdPruningNoEffect()
	{
		return thresholdPruningNoEffect;
	}

	public boolean hasCandidateTimelinesBelowThreshold()
	{
		return hasCandidateTimelinesBelowThreshold;
	}

	public LinkedHashMap<Integer, Pair<String, Double>> getEditDistancesSortedMapFullCand()// getDistanceScoresSorted()
	{
		return this.editDistancesSortedMapFullCand;
	}

	public boolean hasError()
	{
		return this.errorExists;
	}

	public void setErrorExists(boolean exists)
	{
		this.errorExists = exists;
	}
}

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
