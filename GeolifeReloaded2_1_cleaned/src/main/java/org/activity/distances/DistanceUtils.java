package org.activity.distances;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.ActDistType;
import org.activity.constants.Enums.CaseType;
import org.activity.constants.Enums.GowGeoFeature;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.Enums.TypeOfCandThreshold;
import org.activity.constants.VerbosityConstants;
import org.activity.io.EditDistanceMemorizer;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.sanityChecks.Sanity;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.math3.stat.StatUtils;

/**
 * Moved methods from RecommSeqNov2017 class to here
 * 
 * @since 21 Nov 2017
 * @author gunjan
 *
 */
public class DistanceUtils
{

	/**
	 * Ensure that both vectors are of same length
	 * 
	 * @param vec1
	 * @param vec2
	 * @return
	 */
	public static double getChebyshevDistance(double[] vector1, double[] vector2)
	{
		if (vector1.length != vector2.length)
		{
			System.err.println("Error in getChebyshevDistance: compared vectors of different length");
			return -1;
		}

		if (vector1.length == 0)
		{
			System.err.println("Error in getChebyshevDistance: compared vectors are of zero length");
			return -1;
		}

		double maxDiff = -1d;

		for (int i = 0; i < vector1.length; i++)
		{
			double diff = Math.abs(vector1[i] - vector2[i]);
			if (diff > maxDiff)
			{
				maxDiff = diff;
			}
		}

		return maxDiff;
	}

	/**
	 * <p>
	 * NOT NORMALISING FOR CLOSEST TIME APPROACH
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param userIDAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param distanceUsed
	 * @param lookPastType
	 * @param hjEditDistance
	 * @param featureWiseEditDistance
	 * @param featureWiseWeightedEditDistance
	 * @param OTMDSAMEditDistance
	 * @return {CandID,Trace,EditDist} for MU and Daywise, {CandID,ActName of act obj with closest st,avs time diff in
	 *         secs} for closest st time.... Pair{{},{candID,indexOfEndPointConsideredInCand}}
	 *         <p>
	 *         The second element of the result pair is:
	 *         <p>
	 *         - for SeqNCount and SeqNHours approach, tne end point index considered in the candidate is the last
	 *         activity object in that cand
	 *         <p>
	 *         - for Daywise approach: {Date of CandidateTimeline as string, End point index of least distant
	 *         subsequence}}
	 *         <p>
	 *         - for ClosesetTime approach: {Date of CandidateTimeline as string, End point index of least distant
	 *         subsequence}}
	 */

	// @SuppressWarnings("unused")
	public static Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> getNormalisedDistancesForCandidateTimelines(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			CaseType caseType, String userIDAtRecomm, Date dateAtRecomm, Time timeAtRecomm, String distanceUsed,
			LookPastType lookPastType, HJEditDistance hjEditDistance, FeatureWiseEditDistance featureWiseEditDistance,
			FeatureWiseWeightedEditDistance featureWiseWeightedEditDistance, OTMDSAMEditDistance OTMDSAMEditDistance,
			EditDistanceMemorizer editDistancesMemorizer)
	{
		// {CandID,Trace,EditDist}
		LinkedHashMap<String, Pair<String, Double>> normalisedDistanceForCandTimelines = null;

		// {CandID, EndIndexOfLeastDistantSubsequene} //this is relevant for daywise as curr act can occur multiple
		// times in same cand
		LinkedHashMap<String, Integer> endIndexSubseqConsideredInCand = null;

		if (lookPastType.equals(Enums.LookPastType.Daywise))
		{
			// if (Constant.useRTVerseNormalisationForED)
			// {System.err.println(PopUps.getTracedErrorMsg("Error: RTVerse normalisation not implemented for Daywise
			// approach"));}
			Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> editDistancesRes =
					// TimelineUtils
					// .getEditDistancesForDaywiseCandidateTimelines17Dec2018(candidateTimelines,
					// activitiesGuidingRecomm,
					// userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
					// Constant.hasInvalidActivityNames, Constant.INVALID_ACTIVITY1, Constant.INVALID_ACTIVITY2,
					// distanceUsed, hjEditDistance);
					TimelineUtils.getEditDistancesForDaywiseCandidateTimelines17Dec2018(candidateTimelines,
							activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(),
							timeAtRecomm.toString(), distanceUsed, hjEditDistance, featureWiseEditDistance,
							featureWiseWeightedEditDistance, OTMDSAMEditDistance, editDistancesMemorizer, lookPastType);

			// probably no need of normalising over the set again, because since 17 Dec 2018 implementation, the
			// distance returned are already normalised
			LinkedHashMap<String, Pair<String, Double>> candEditDistances = editDistancesRes.getFirst();
			normalisedDistanceForCandTimelines = normalisedDistancesOverTheSet(candEditDistances, userIDAtRecomm,
					dateAtRecomm.toString(), timeAtRecomm.toString());

			endIndexSubseqConsideredInCand = editDistancesRes.getSecond();

		}
		else if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours))
		{
			normalisedDistanceForCandTimelines = getNormalisedDistancesForCandidateTimelinesFullCand(candidateTimelines,
					activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
					distanceUsed, hjEditDistance, featureWiseEditDistance, featureWiseWeightedEditDistance,
					OTMDSAMEditDistance, editDistancesMemorizer, lookPastType);

			// for SeqNCount and SeqNHours approach, the end point index considered in the candidate is the last
			// activity object in that cand
			// endIndexSubseqConsideredInCand = (LinkedHashMap<String, Integer>)
			// candidateTimelines.entrySet().stream()
			// .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().size() - 1));
			endIndexSubseqConsideredInCand = candidateTimelines.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().size() - 1, (v1, v2) -> v1,
							LinkedHashMap<String, Integer>::new));
		}
		///
		else if (lookPastType.equals(Enums.LookPastType.NGram))
		{
			LinkedHashMap<String, Pair<String, Double>> candEditDistances = new LinkedHashMap<>();
			for (String candID : candidateTimelines.keySet())
			{
				candEditDistances.put(candID, new Pair<>("", Double.valueOf(0)));// assigning dist of 0 so, sim score
																					// will be 1.
			}
			normalisedDistanceForCandTimelines = candEditDistances;

			// for SeqNCount and SeqNHours approach, tne end point index considered in the candidate is the last
			// activity object in that cand TODO
			// endIndexSubseqConsideredInCand = (LinkedHashMap<String, Integer>) candidateTimelines.entrySet().stream()
			// .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().size() - 1));
			endIndexSubseqConsideredInCand = candidateTimelines.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().size() - 1, (v1, v2) -> v1,
							LinkedHashMap<String, Integer>::new));
		}
		///

		else if (lookPastType.equals(Enums.LookPastType.ClosestTime))
		{
			Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> editDistancesRes = null;

			if (Constant.ClosestTimeAllowSpillOverDays == false)
			{
				editDistancesRes = TimelineUtils.getClosestTimeDistancesForDaywiseCandidateTimelines(candidateTimelines,
						activitiesGuidingRecomm, userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
						Constant.hasInvalidActivityNames, Constant.INVALID_ACTIVITY1, Constant.INVALID_ACTIVITY2,
						distanceUsed);
			}
			else
			{
				// Curtain start 15 Aug
				// editDistancesRes = TimelineUtils.getClosestTimeDistancesForCandidateTimelinesColl(candidateTimelines,
				// activitiesGuidingRecomm, userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
				// Constant.hasInvalidActivityNames, Constant.INVALID_ACTIVITY1, Constant.INVALID_ACTIVITY2,
				// distanceUsed, false);
				// Curtain end 15 Aug

				editDistancesRes = TimelineUtils.getClosestTimeDistsForCandTimelinesColl1CandPerNeighbour(
						candidateTimelines, activitiesGuidingRecomm, userIDAtRecomm, dateAtRecomm.toString(),
						timeAtRecomm.toString(), Constant.hasInvalidActivityNames, Constant.INVALID_ACTIVITY1,
						Constant.INVALID_ACTIVITY2, distanceUsed, /* Constant.ClosestTimeDiffThresholdInSecs * 1000, */
						false);

			}

			LinkedHashMap<String, Pair<String, Double>> candEditDistances = editDistancesRes.getFirst();
			normalisedDistanceForCandTimelines = candEditDistances; // 15 Aug 2017, NOT NORMALISING TO PRESERVE THE
																	// ACTUAL TIME DIFF
			// Aug 15, 2017: Noticed that: for closest time approach, i was normalising the distance (time difference)
			// over each set of candidate timelines (like the Ncount approach). And then while computing the score i am
			// using score = 1-min(1,timeDiff/60mins). THIS IS INCORRECT since the timediff at this stage is not the
			// actual timediff but normalised time diff, hence should not compared with 60 mins. Hence, time difference
			// should not be normalised.
			// see: createRankedTopRecommendedActivityNamesClosestTime()

			// Start of Disabled on Aug 15 2017
			// normalisedDistancesOverTheSet(candEditDistances, userIDAtRecomm,
			// dateAtRecomm.toString(), timeAtRecomm.toString());
			// Start of Disabled on Aug 15 2017
			endIndexSubseqConsideredInCand = editDistancesRes.getSecond();
		}

		else
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: Unrecognised lookPastType "));
			System.exit(-1);
		}

		if (normalisedDistanceForCandTimelines == null || normalisedDistanceForCandTimelines.size() == 0)
		{
			// PopUps.printTracedErrorMsg(
			// "Error: normalisedDistanceForCandTimelines.size=" + normalisedDistanceForCandTimelines.size());
			System.err.println(
					"Warning: normalisedDistanceForCandTimelines.size=" + normalisedDistanceForCandTimelines.size());
		}

		return new Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>>(
				normalisedDistanceForCandTimelines, endIndexSubseqConsideredInCand);
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
	 * @param hjEditDistance
	 * 
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getHJEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			HJEditDistance hjEditDistance)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<String, Pair<String, Double>> candEditDistances = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
		{
			Pair<String, Double> editDistanceForThisCandidate = null;
			String candidateTimelineId = entry.getKey();

			switch (caseType)
			{
			case CaseBasedV1:
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
				// invalids are already expunged, no need to expunge again
				{
					editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceWithoutEndCurrentActivity(
							entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, candidateTimelineId);
				}
				else
				{
					editDistanceForThisCandidate = hjEditDistance
							.getHJEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm,
									userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelineId);
				}
				break;

			case SimpleV3:// "SimpleV3":
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
				{
					editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceWithTrace(
							entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, candidateTimelineId);
				}
				else
				{
					editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceInvalidsExpunged(
							entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, candidateTimelineId);
				}
				break;

			default:
				System.err.println(PopUps.getTracedErrorMsg(
						"Error in getEditDistancesForCandidateTimelineFullCand: unidentified case type" + caseType));
				break;
			}

			candEditDistances.put(candidateTimelineId, editDistanceForThisCandidate);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}
		return candEditDistances;
	}

	/// Start of added on 9 Aug 2017
	// ////////
	/**
	 * Fork of getHJEditDistancesForCandidateTimelinesFullCand()
	 * <p>
	 * Added: Aug 9, 2017: for better performance (parallel) and memorising edit distance computations
	 * <p>
	 * 
	 * IMPORTANT POINT: THE CANDIDATE TIMELINE IS THE DIRECT CANDIDATE TIMELINE AND NOT THE LEAST DISTANT SUBCANDIDATE.
	 * <p>
	 * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the edit distance of
	 * that candidate timeline with the current timeline.
	 * 
	 * @param candidateTimelines
	 * 
	 * @param activitiesGuidingRecomm
	 * 
	 * @param caseType
	 *            can be 'SimpleV3' or 'CaseBasedV1'
	 * 
	 * @param userAtRecomm
	 *            used only for writing to file
	 * 
	 * @param dateAtRecomm
	 *            used only for writing to file
	 * 
	 * @param timeAtRecomm
	 *            used only for writing to file
	 * 
	 * @param hjEditDistance
	 * 
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 * @since Aug 9, 2017
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getHJEditDistsForCandsFullCandParallelWithMemory(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			HJEditDistance hjEditDistance, EditDistanceMemorizer editDistancesMemorizer)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<String, Pair<String, Double>> candEditDistances = new LinkedHashMap<>();

		candEditDistances = candidateTimelines.entrySet().parallelStream().collect(Collectors.toMap(
				e -> (String) e.getKey(),
				e -> (Pair<String, Double>) getEditDistances(e.getValue(), activitiesGuidingRecomm, userAtRecomm,
						dateAtRecomm, timeAtRecomm, e.getKey(), caseType, hjEditDistance, editDistancesMemorizer),
				(oldValue, newValue) -> newValue, LinkedHashMap::new));

		if (Constant.memorizeEditDistance)
		{
			String currentTimelineID = Timeline.getTimelineIDFromAOs(activitiesGuidingRecomm);
			// System.out.println("activitiesGuidingRecomm.size()=" + activitiesGuidingRecomm.size());
			// long t1 = System.currentTimeMillis();
			// for (Entry<String, Pair<String, Double>> candEditDist : candEditDistances.entrySet())
			// {
			// Constant.addToEditDistanceMemorizer(candEditDist.getKey(), currentTimelineID, candEditDist.getValue());
			// }
			// long t2 = System.currentTimeMillis();

			// Start of 10 Aug temp curtain 1
			candEditDistances.entrySet().stream()
					.forEach(e -> Constant.addToEditDistanceMemorizer(e.getKey(), currentTimelineID, e.getValue()));
			// End of 10 Aug temp curtain 1
			// long t3 = System.currentTimeMillis();
		}
		// System.out.println("Iter: " + (t2 - t1));
		// System.out.println("Stre: " + (t3 - t2));
		return candEditDistances;
	}

	/**
	 * TODO: INCOMPLETE AS OF 8 JAN 2019
	 * <p>
	 * Fork of
	 * org.activity.distances.DistanceUtils.getHJEditDistsByDiffsForCandsFullCandParallelWithMemory13April2018RTV() to
	 * use feat pairs instead of feat diffs for more flexibility of FED and allowing plugging of different distance
	 * types for AED.
	 * <p>
	 * 
	 * IMPORTANT POINT: THE CANDIDATE TIMELINE IS THE DIRECT CANDIDATE TIMELINE AND NOT THE LEAST DISTANT SUBCANDIDATE.
	 * <p>
	 * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the edit distance of
	 * that candidate timeline with the current timeline.
	 * <p>
	 * For RTVerse normalisation
	 * 
	 * @param candidateTimelines
	 * 
	 * @param activitiesGuidingRecomm
	 * 
	 * @param caseType
	 *            can be 'SimpleV3' or 'CaseBasedV1'
	 * 
	 * @param userAtRecomm
	 *            used only for writing to file
	 * 
	 * @param dateAtRecomm
	 *            used only for writing to file
	 * 
	 * @param timeAtRecomm
	 *            used only for writing to file
	 * 
	 * @param hjEditDistance
	 * @param editDistancesMemorizer
	 * @param lookPastType
	 *            added on 17 Dec 2018
	 * 
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}},
	 * @since 7 Jan 2018
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getHJEditDistsByDiffsForCandsFullCandParallelWithMemory7Jan2019RTV(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			HJEditDistance hjEditDistance, EditDistanceMemorizer editDistancesMemorizer, LookPastType lookPastType)
	{
		// PopUps.showMessage("Inside getHJEditDistsByDiffsForCandsFullCandParallelWithMemory13April2018RTV");// TODO
		// LinkedHashMap<String, Integer> endIndexSubseqConsideredInCand = null;// added on 17 Dec 2018 for daywise
		// approach
		if (Constant.memorizeEditDistance)
		{
			PopUps.showError("Error: memorizeEditDistance not implemented here");
		}

		// // <CandidateTimeline ID, Edit distance>
		// LinkedHashMap<String, Pair<String, Double>> candEditDistances = new LinkedHashMap<>();

		// CandTimelineID,{EDTrace,ActED, list of differences of Gowalla feature for each ActObj in this cand timeline}
		// CandTimelineID,Triple{TraceAsString,ActLevelEditDistance,List of EnumMap of {GowallaFeatures,
		// DiffForThatFeature} one for each correspnding AO comparison}}
		// LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs = new
		// LinkedHashMap<>(candidateTimelines.size());

		// Start of added on 26 July 2018
		// getListOfUniqueGivenDimensionValsInRTVerse(candidateTimelines, activitiesGuidingRecomm,
		// hjEditDistance.primaryDimension);
		// End of added on 26 July 2018

		// Start of code to be parallelised
		// for (Entry<String, Timeline> e : candidateTimelines.entrySet())
		// {// loop over candidates
		// Triple<String, Double, List<EnumMap<GowallaFeatures, Double>>> res = getActEditDistancesFeatDiffs(
		// e.getValue(), activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm, e.getKey(),
		// caseType, hjEditDistance, editDistancesMemorizer);//candAEDFeatDiffs.put(e.getKey(), res);}
		// Alternatively
		// TODO: TEMPORARILY DISABLE PARALLEL

		// use this when null issue is resolved: resolved
		// if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours))
		// consider the full cand
		LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>>> candAEDFeatValPairs = candidateTimelines
				.entrySet()/* .parallelStream() */.stream()
				.collect(Collectors.toMap(e -> (String) e.getKey(),
						e -> getActEditDistancesFeatValPairs(e.getValue(), activitiesGuidingRecomm, userAtRecomm,
								dateAtRecomm, timeAtRecomm, e.getKey(), caseType, hjEditDistance,
								editDistancesMemorizer, Constant.actLevelDistType),
						(oldValue, newValue) -> newValue, LinkedHashMap::new));
		// else if (lookPastType.equals(Enums.LookPastType.Daywise))// added on 17 Dec 2018 to use RTV for daywise
		// {// consider the least distant subcandidate
		// candAEDFeatDiffs = getLeastDistCandActEditDistancesFeatDiffs();
		// } //was trying to implement RTV normalisation for daywise approach but found this approach unsuitable.
		// else
		// {System.err.println(PopUps.getTracedErrorMsg("Error: Unrecognised lookPastType = " + lookPastType));
		// System.exit(-1);}

		// end of code to be parallelised
		if (VerbosityConstants.WriteCandAEDDiffs)
		{
			writeCandAEDFeatValPairs(candAEDFeatValPairs, "candAEDFeatDiffsFromDistanceComputation");
		}
		//// start of MSD
		// if (Constant.useMSDInFEDInRTVerse)
		// {// square all the diffs in candAEDFeatDiffs
		// candAEDFeatDiffs = squareAllTheDiffs(candAEDFeatDiffs);
		// // now the minOfMins and maxOfMaxs will also be from the squared values.
		// if (VerbosityConstants.WriteCandAEDDiffs)
		// {
		// writeCandAEDDiffs(candAEDFeatDiffs, "candAEDFeatDiffsAfterSquaring");
		// }
		// }
		// if (Constant.useMSDInFEDInRTVerse)
		// {// square all the diffs in candAEDFeatDiffs
		// candAEDFeatDiffs = log2AllTheDiffs(candAEDFeatDiffs);
		// // now the minOfMins and maxOfMaxs will also be from the squared values.
		// if (VerbosityConstants.WriteCandAEDDiffs)
		// {
		// writeCandAEDDiffs(candAEDFeatDiffs, "candAEDFeatDiffsAfterSquaring");
		// }
		// }
		//// end of MSD

		/////////////////// Start of finding min max
		// combined together for one return statement from abstracted method
		// note that in the case of Alpha =1, there are no feature diffs in candAEDFeatDiffs
		Pair<EnumMap<GowGeoFeature, Double>, EnumMap<GowGeoFeature, Double>> minOfMinsAndMaxOfMaxOfFeatureVals = getMinOfMinsAndMaxOfMaxOfFeatureValPairs(
				userAtRecomm, dateAtRecomm, timeAtRecomm, hjEditDistance, candAEDFeatValPairs,
				Constant.fixedValPerFeatForRTVerseMaxMinForFEDNorm);

		//////////////////////////////////////////////////////

		LinkedHashMap<String, Pair<String, Double>> candEditDistancesRes = null;
		// Start of Sanity Check if no logging version is giving identical output as logging version:

		if (true)
		{// TODO INCOMPLETED AS OF 8 JAN 2019
			if (Constant.getDynamicEDAlpha() == 1)
			{
				// normalise AED
				candEditDistancesRes = minMaxNormalisedAED8Jan2019(candAEDFeatValPairs,
						Constant.percentileForRTVerseMaxForAEDNorm);
			}
			else
			{
				PopUps.printTracedErrorMsgWithExit(
						"Error: not implemented for Alpha!=1, Constant.getDynamicEDAlpha() = "
								+ Constant.getDynamicEDAlpha());
			}
			// // Start of Jan 7 2019 curtain
			// LinkedHashMap<String, Pair<String, Double>> candEditDistancesLogging = null;
			//
			// // Option 1: AO-wise FED
			// // - Compute FED for each AO and then takes the mead FED over all AOs in that cand as the FED for that
			// cand.
			// // - implementation: DistanceUtils.getRTVerseMinMaxNormalisedEditDistances()
			// // Option 2: Feat-wise FED
			// // - Compute FED for each candidate by using mean feature diff for each feature across all AOs in that
			// cand
			// // - implementation:.DistanceUtils.getRTVerseMinMaxNormalisedEditDistancesFeatSeqApproach()
			// if (Constant.computeFEDForEachAOInRTVerse)
			// {
			// // System.out
			// // .println("Doing computeFEDForEachAOInRTVerse with usingMSD= " + Constant.useMSDInFEDInRTVerse);
			// candEditDistancesLogging = getRTVerseMinMaxNormalisedEditDistances(candAEDFeatDiffs,
			// minOfMinsAndMaxOfMaxOfFeatureVals.getFirst(), minOfMinsAndMaxOfMaxOfFeatureVals.getSecond(),
			// hjEditDistance, activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm,
			// candidateTimelines);
			// }
			// else if (Constant.computeFEDForEachFeatureSeqInRTVerse)
			// {
			// // System.out.println(
			// // "Doing computeFEDForEachFeatureSeqInRTVerse with usingMSD= " + Constant.useMSDInFEDInRTVerse);
			// candEditDistancesLogging = getRTVerseMinMaxNormalisedEditDistancesFeatSeqApproach(candAEDFeatDiffs,
			// minOfMinsAndMaxOfMaxOfFeatureVals.getFirst(), minOfMinsAndMaxOfMaxOfFeatureVals.getSecond(),
			// hjEditDistance, activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm,
			// candidateTimelines);
			// }
			//
			// // boolean isNoLoggingSane = candEditDistancesNoLogging.equals(candEditDistancesLogging);
			// // System.out.println("candEditDistancesNoLogging.equals(candEditDistancesLogging) = " +
			// isNoLoggingSane);
			// // WToFile.appendLineToFileAbs(String.valueOf(isNoLoggingSane) + "\n",
			// // Constant.getCommonPath() + "DebugApr25RTVerseNoLogSanity.csv");
			// candEditDistancesRes = candEditDistancesLogging;
			// // End of Jan 7 2019 curtain
		}

		if (VerbosityConstants.WriteCandAEDDiffs)
		{
			writeCandAEDDists(candEditDistancesRes, "candNormalisedEditDistancesRes");
		}
		return candEditDistancesRes;
	}

	/**
	 * 
	 * @param candAEDFeatValPairs
	 * @param percentileForRTVerseMaxForAEDNorm
	 * @return
	 * @since 8 Jan 2019
	 */
	private static LinkedHashMap<String, Pair<String, Double>> minMaxNormalisedAED8Jan2019(
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>>> candAEDFeatValPairs,
			double percentileForRTVerseMaxForAEDNorm)
	{

		LinkedHashMap<String, Pair<String, Double>> res = new LinkedHashMap<>(candAEDFeatValPairs.size());

		double maxActEDOverAllCands = Double.MIN_VALUE;
		// candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond()).max().getAsDouble();

		// Start of added on 15 Aug 2018
		// double percentileForRTVerseMaxForAEDNorm = Constant.percentileForRTVerseMaxForAEDNorm;
		if (percentileForRTVerseMaxForAEDNorm == -1)
		{
			maxActEDOverAllCands = candAEDFeatValPairs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
					.max().getAsDouble();
		}
		else
		{
			List<Double> listOfAEDs = candAEDFeatValPairs.entrySet().stream().map(e -> e.getValue().getSecond())
					.collect(Collectors.toList());
			maxActEDOverAllCands = StatsUtils.getPercentile(listOfAEDs, percentileForRTVerseMaxForAEDNorm);
		}

		double minActEDOverAllCands = candAEDFeatValPairs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
				.min().getAsDouble();

		for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>>> e : candAEDFeatValPairs
				.entrySet())
		{
			double actDistForThisCand = e.getValue().getSecond();
			double normActDistForThisCand = StatsUtils.minMaxNormWORoundWithUpperBound(actDistForThisCand,
					maxActEDOverAllCands, minActEDOverAllCands, 1d, false);
			res.put(e.getKey(), new Pair<>(e.getValue().getFirst(), normActDistForThisCand));
		}

		return res;
	}

	/**
	 * Fork of getHJEditDistsForCandsFullCandParallelWithMemory()
	 * <p>
	 * 
	 * IMPORTANT POINT: THE CANDIDATE TIMELINE IS THE DIRECT CANDIDATE TIMELINE AND NOT THE LEAST DISTANT SUBCANDIDATE.
	 * <p>
	 * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the edit distance of
	 * that candidate timeline with the current timeline.
	 * <p>
	 * For RTVerse normalisation
	 * 
	 * @param candidateTimelines
	 * 
	 * @param activitiesGuidingRecomm
	 * 
	 * @param caseType
	 *            can be 'SimpleV3' or 'CaseBasedV1'
	 * 
	 * @param userAtRecomm
	 *            used only for writing to file
	 * 
	 * @param dateAtRecomm
	 *            used only for writing to file
	 * 
	 * @param timeAtRecomm
	 *            used only for writing to file
	 * 
	 * @param hjEditDistance
	 * @param editDistancesMemorizer
	 * @param lookPastType
	 *            added on 17 Dec 2018
	 * 
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}},
	 * @since April 13 2018
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getHJEditDistsByDiffsForCandsFullCandParallelWithMemory13April2018RTV(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			HJEditDistance hjEditDistance, EditDistanceMemorizer editDistancesMemorizer, LookPastType lookPastType)
	{
		// PopUps.showMessage("Inside getHJEditDistsByDiffsForCandsFullCandParallelWithMemory13April2018RTV");// TODO
		// LinkedHashMap<String, Integer> endIndexSubseqConsideredInCand = null;// added on 17 Dec 2018 for daywise
		// approach
		if (Constant.memorizeEditDistance)
		{
			PopUps.showError("Error: memorizeEditDistance not implemented here");
		}

		// // <CandidateTimeline ID, Edit distance>
		// LinkedHashMap<String, Pair<String, Double>> candEditDistances = new LinkedHashMap<>();

		// CandTimelineID,{EDTrace,ActED, list of differences of Gowalla feature for each ActObj in this cand timeline}
		// CandTimelineID,Triple{TraceAsString,ActLevelEditDistance,List of EnumMap of {GowallaFeatures,
		// DiffForThatFeature} one for each correspnding AO comparison}}
		LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs = new LinkedHashMap<>(
				candidateTimelines.size());

		// Start of added on 26 July 2018
		// getListOfUniqueGivenDimensionValsInRTVerse(candidateTimelines, activitiesGuidingRecomm,
		// hjEditDistance.primaryDimension);
		// End of added on 26 July 2018

		// Start of code to be parallelised
		// for (Entry<String, Timeline> e : candidateTimelines.entrySet())
		// {// loop over candidates
		// Triple<String, Double, List<EnumMap<GowallaFeatures, Double>>> res = getActEditDistancesFeatDiffs(
		// e.getValue(), activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm, e.getKey(),
		// caseType, hjEditDistance, editDistancesMemorizer);//candAEDFeatDiffs.put(e.getKey(), res);}
		// Alternatively
		// TODO: TEMPORARILY DISABLE PARALLEL

		if (false)// longer version to find null issue//temp 18 July 2018
		{// not suitable if works for Geolife, better to avoid using this unless is checked again.
			for (Entry<String, Timeline> e : candidateTimelines.entrySet())
			{
				String key = (String) e.getKey();
				Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>> value = getActEditDistancesFeatDiffs(
						e.getValue(), activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm, e.getKey(),
						caseType, hjEditDistance, editDistancesMemorizer);
				candAEDFeatDiffs.put(key, value);
			}
		}
		else
		{
			// use this when null issue is resolved: resolved
			// if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours))
			{// consider the full cand
				candAEDFeatDiffs = candidateTimelines.entrySet()/* .parallelStream() */.stream()
						.collect(Collectors.toMap(e -> (String) e.getKey(),
								e -> getActEditDistancesFeatDiffs(e.getValue(), activitiesGuidingRecomm, userAtRecomm,
										dateAtRecomm, timeAtRecomm, e.getKey(), caseType, hjEditDistance,
										editDistancesMemorizer),
								(oldValue, newValue) -> newValue, LinkedHashMap::new));
			}
			// else if (lookPastType.equals(Enums.LookPastType.Daywise))// added on 17 Dec 2018 to use RTV for daywise
			// {// consider the least distant subcandidate
			// candAEDFeatDiffs = getLeastDistCandActEditDistancesFeatDiffs();
			// } //was trying to implement RTV normalisation for daywise approach but found this approach unsuitable.
			// else
			// {System.err.println(PopUps.getTracedErrorMsg("Error: Unrecognised lookPastType = " + lookPastType));
			// System.exit(-1);}

		}
		// end of code to be parallelised
		if (VerbosityConstants.WriteCandAEDDiffs)
		{
			writeCandAEDDiffs(candAEDFeatDiffs, "candAEDFeatDiffsFromDistanceComputation");
		}
		//// start of MSD
		if (Constant.useMSDInFEDInRTVerse)
		{// square all the diffs in candAEDFeatDiffs
			candAEDFeatDiffs = squareAllTheDiffs(candAEDFeatDiffs);
			// now the minOfMins and maxOfMaxs will also be from the squared values.
			if (VerbosityConstants.WriteCandAEDDiffs)
			{
				writeCandAEDDiffs(candAEDFeatDiffs, "candAEDFeatDiffsAfterSquaring");
			}
		}
		if (Constant.useMSDInFEDInRTVerse)
		{// square all the diffs in candAEDFeatDiffs
			candAEDFeatDiffs = log2AllTheDiffs(candAEDFeatDiffs);
			// now the minOfMins and maxOfMaxs will also be from the squared values.
			if (VerbosityConstants.WriteCandAEDDiffs)
			{
				writeCandAEDDiffs(candAEDFeatDiffs, "candAEDFeatDiffsAfterSquaring");
			}
		}
		//// end of MSD

		/////////////////// Start of finding min max
		// combined together for one return statement from abstracted method
		// note that in the case of Alpha =1, there are no feature diffs in candAEDFeatDiffs
		Pair<EnumMap<GowGeoFeature, Double>, EnumMap<GowGeoFeature, Double>> minOfMinsAndMaxOfMaxOfFeatureDiffs = getMinOfMinsAndMaxOfMaxOfFeatureDiffs(
				userAtRecomm, dateAtRecomm, timeAtRecomm, hjEditDistance, candAEDFeatDiffs,
				Constant.fixedValPerFeatForRTVerseMaxMinForFEDNorm);

		//////////////////////////////////////////////////////

		LinkedHashMap<String, Pair<String, Double>> candEditDistancesRes = null;
		// Start of Sanity Check if no logging version is giving identical output as logging version:
		if (true)// Sanity Check passed on April 25 2018
		{
			LinkedHashMap<String, Pair<String, Double>> candEditDistancesLogging = null;

			// Option 1: AO-wise FED
			// - Compute FED for each AO and then takes the mead FED over all AOs in that cand as the FED for that cand.
			// - implementation: DistanceUtils.getRTVerseMinMaxNormalisedEditDistances()
			// Option 2: Feat-wise FED
			// - Compute FED for each candidate by using mean feature diff for each feature across all AOs in that cand
			// - implementation:.DistanceUtils.getRTVerseMinMaxNormalisedEditDistancesFeatSeqApproach()
			if (Constant.computeFEDForEachAOInRTVerse)
			{
				System.out
						.println("Doing computeFEDForEachAOInRTVerse with usingMSD= " + Constant.useMSDInFEDInRTVerse);
				candEditDistancesLogging = getRTVerseMinMaxNormalisedEditDistances(candAEDFeatDiffs,
						minOfMinsAndMaxOfMaxOfFeatureDiffs.getFirst(), minOfMinsAndMaxOfMaxOfFeatureDiffs.getSecond(),
						hjEditDistance, activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm,
						candidateTimelines);
			}
			else if (Constant.computeFEDForEachFeatureSeqInRTVerse)
			{
				System.out.println(
						"Doing computeFEDForEachFeatureSeqInRTVerse with usingMSD= " + Constant.useMSDInFEDInRTVerse);
				candEditDistancesLogging = getRTVerseMinMaxNormalisedEditDistancesFeatSeqApproach(candAEDFeatDiffs,
						minOfMinsAndMaxOfMaxOfFeatureDiffs.getFirst(), minOfMinsAndMaxOfMaxOfFeatureDiffs.getSecond(),
						hjEditDistance, activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm,
						candidateTimelines);
			}

			// boolean isNoLoggingSane = candEditDistancesNoLogging.equals(candEditDistancesLogging);
			// System.out.println("candEditDistancesNoLogging.equals(candEditDistancesLogging) = " + isNoLoggingSane);
			// WToFile.appendLineToFileAbs(String.valueOf(isNoLoggingSane) + "\n",
			// Constant.getCommonPath() + "DebugApr25RTVerseNoLogSanity.csv");
			candEditDistancesRes = candEditDistancesLogging;
		}
		// End of Sanity Check if no logging version is giving identical output as logging version:
		else
		{// NOT VERIFIED for Geolife
			// <CandidateTimeline ID, Edit distance>
			LinkedHashMap<String, Pair<String, Double>> candEditDistancesNoLogging = getRTVerseMinMaxNormalisedEditDistancesNoLogging(
					candAEDFeatDiffs, minOfMinsAndMaxOfMaxOfFeatureDiffs.getFirst(),
					minOfMinsAndMaxOfMaxOfFeatureDiffs.getSecond(), hjEditDistance, activitiesGuidingRecomm,
					userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelines);
			candEditDistancesRes = candEditDistancesNoLogging;
		}
		return candEditDistancesRes;
	}

	/**
	 * Square all the diffs for mean squared diff approach
	 * 
	 * @param candAEDFeatDiffs
	 * @return
	 * @since 25 Nov 2018
	 */
	private static LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> squareAllTheDiffs(
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs)
	{
		LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> res = new LinkedHashMap<>(
				candAEDFeatDiffs.size());
		// StringBuilder sbLog = new StringBuilder();

		// loop over cands
		for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candEntry : candAEDFeatDiffs
				.entrySet())
		{
			List<EnumMap<GowGeoFeature, Double>> listOfEnumMapsWithSqDiffsForThisCand = new ArrayList<>(
					candEntry.getValue().getThird().size());

			// loop over AOs
			for (EnumMap<GowGeoFeature, Double> aoEntry : candEntry.getValue().getThird())
			{
				EnumMap<GowGeoFeature, Double> featureSqdDiffForThisAO = new EnumMap<>(GowGeoFeature.class);

				// loop over features of each AO.
				for (Entry<GowGeoFeature, Double> featureEntry : aoEntry.entrySet())
				{
					featureSqdDiffForThisAO.put(featureEntry.getKey(),
							featureEntry.getValue() * featureEntry.getValue());
				}
				listOfEnumMapsWithSqDiffsForThisCand.add(featureSqdDiffForThisAO);
			}
			res.put(candEntry.getKey(), new Triple<>(candEntry.getValue().getFirst(), candEntry.getValue().getSecond(),
					listOfEnumMapsWithSqDiffsForThisCand));
		}
		return res;
	}

	/**
	 * 
	 * @param candAEDFeatDiffs
	 * @return
	 * @since 7 Dec 2018
	 */
	private static LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> log2AllTheDiffs(
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs)
	{
		LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> res = new LinkedHashMap<>(
				candAEDFeatDiffs.size());
		// StringBuilder sbLog = new StringBuilder();

		// loop over cands
		for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candEntry : candAEDFeatDiffs
				.entrySet())
		{
			List<EnumMap<GowGeoFeature, Double>> listOfEnumMapsWithSqDiffsForThisCand = new ArrayList<>(
					candEntry.getValue().getThird().size());

			// loop over AOs
			for (EnumMap<GowGeoFeature, Double> aoEntry : candEntry.getValue().getThird())
			{
				EnumMap<GowGeoFeature, Double> featureSqdDiffForThisAO = new EnumMap<>(GowGeoFeature.class);

				// loop over features of each AO.
				for (Entry<GowGeoFeature, Double> featureEntry : aoEntry.entrySet())
				{
					featureSqdDiffForThisAO.put(featureEntry.getKey(), Math.log(featureEntry.getValue()) / Math.log(2));
				}
				listOfEnumMapsWithSqDiffsForThisCand.add(featureSqdDiffForThisAO);
			}
			res.put(candEntry.getKey(), new Triple<>(candEntry.getValue().getFirst(), candEntry.getValue().getSecond(),
					listOfEnumMapsWithSqDiffsForThisCand));
		}
		return res;
	}

	/**
	 * 
	 * @param candAEDFeatDiffs
	 * @since 25 Nov 2018
	 */
	private static void writeCandAEDDiffs(
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs,
			String fileNamePhrase)
	{
		StringBuilder sb = new StringBuilder();

		// loop over each cand
		for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> e : candAEDFeatDiffs
				.entrySet())
		{
			sb.append(e.getKey() + "," + e.getValue().getFirst() + "," + e.getValue().getSecond());

			// loop over Feature enum maps for each AO
			for (EnumMap<GowGeoFeature, Double> listEntry : e.getValue().getThird())
			{
				// loop over features of each AO.
				for (Entry<GowGeoFeature, Double> q : listEntry.entrySet())
				{
					sb.append("," + q.getValue());
				}
			}
			sb.append("\n");
		}
		WToFile.appendLineToFileAbs(sb.toString(), Constant.getCommonPath() + fileNamePhrase + ".csv");
	}

	/**
	 * 
	 * @param candAEDFeatDiffs
	 * @since 7 Jan 2019
	 */
	private static void writeCandAEDFeatValPairs(
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>>> candAEDFeatValPairs,
			String fileNamePhrase)
	{
		StringBuilder sb = new StringBuilder();

		// loop over each cand
		for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>>> e : candAEDFeatValPairs
				.entrySet())
		{
			sb.append(e.getKey() + "," + e.getValue().getFirst() + "," + e.getValue().getSecond());

			// loop over Feature enum maps for each AO
			for (EnumMap<GowGeoFeature, Pair<String, String>> listEntry : e.getValue().getThird())
			{
				// loop over features of each AO.
				for (Entry<GowGeoFeature, Pair<String, String>> q : listEntry.entrySet())
				{
					sb.append("," + q.getValue().getFirst() + "__" + q.getValue().getSecond());
				}
			}
			sb.append("\n");
		}
		WToFile.appendLineToFileAbs(sb.toString(), Constant.getCommonPath() + fileNamePhrase + ".csv");
	}

	/**
	 * 
	 * @param candAEDFeatDiffs
	 * @since 7 Jan 2019
	 */
	private static void writeCandAEDDists(LinkedHashMap<String, Pair<String, Double>> res, String fileNamePhrase)
	{
		StringBuilder sb = new StringBuilder();

		// loop over each cand
		for (Entry<String, Pair<String, Double>> e : res.entrySet())
		{
			sb.append(e.getKey() + "," + e.getValue().getFirst() + "," + e.getValue().getSecond());

			sb.append("\n");
		}
		WToFile.appendLineToFileAbs(sb.toString(), Constant.getCommonPath() + fileNamePhrase + ".csv");
	}

	/////////
	/**
	 * Fork of DistanceUtils.getMinOfMinsAndMaxOfMaxOfFeatureDiffs()
	 * <p>
	 * To find max of max of feature diffs and min of min of feature val pairs for RTVerse normalisation.
	 * 
	 * @param userAtRecomm
	 *            only for logging
	 * @param dateAtRecomm
	 *            only for logging
	 * @param timeAtRecomm
	 *            only for logging
	 * @param hjEditDistance
	 * @param candAEDFeatDiffs
	 *            one map entry for each cand, and for each cand, a list of enumaps, one for each AO in the cand
	 * @param useFixedMaxForEachFeature
	 *            instead of pth percentils
	 * @return
	 * @since 7 Jan 2019
	 */
	private static Pair<EnumMap<GowGeoFeature, Double>, EnumMap<GowGeoFeature, Double>> getMinOfMinsAndMaxOfMaxOfFeatureValPairs(
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, HJEditDistance hjEditDistance,
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>>> candAEDFeatValPairs,
			boolean useFixedMaxForEachFeature)
	{
		boolean shouldComputeFeatureLevelDistance = hjEditDistance.getShouldComputeFeatureLevelDistance();
		StringBuilder sbToWrite = new StringBuilder(userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm);
		EnumMap<GowGeoFeature, Double> minOfMinOfVals = null;
		EnumMap<GowGeoFeature, Double> maxOfMaxOfVals = null;

		// loop over all res to find max and min for normalisation
		// For each candidate, finding max feature diff for each gowalla feature over all the activity objects in that
		// candidate timeline. (horizontal aggregations)
		List<EnumMap<GowGeoFeature, DoubleSummaryStatistics>> summaryStatForEachCand = null;
		if (shouldComputeFeatureLevelDistance)
		{
			summaryStatForEachCand = candAEDFeatValPairs.entrySet().stream()
					.map(e -> HJEditDistance.getSummaryStatsForEachFeatureValsPairsOverListOfAOsInACand(
							e.getValue().getThird(), userAtRecomm, dateAtRecomm, timeAtRecomm, e.getKey()))
					.collect(Collectors.toList());
		}

		// Aggregation (across candidate timelines) of aggregation (across activity objects in each cand
		// timeline:summaryStatForEachCand)

		if (shouldComputeFeatureLevelDistance)
		{ // Compute minOfMinOfDiffs and maxOfMaxOfDiff (pth percentile as max)
			minOfMinOfVals = new EnumMap<>(GowGeoFeature.class);
			maxOfMaxOfVals = new EnumMap<>(GowGeoFeature.class);

			if (useFixedMaxForEachFeature)
			{
				String databaseName = Constant.getDatabaseName();
				if (databaseName.equals("geolife1"))
				{
					// Nov30MaxMin
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 10800d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 10800d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 5d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 5d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 5d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 60d);
					//

					// Dec1MaxMin
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 10800d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 10800d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 2d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 2d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 2d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 40d);

					// Dec2MaxMin
					// MaxStTimeDiff:- 5 hrs
					// MaxDuration:- 1 hrs
					// DistTravelled:- 2km,
					// StGeoDiff:- 4km
					// EndGeoDiff:- 4km
					// AvgAlt:- 60m
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 18000d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 3600d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 2d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 4d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 4d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 60d);

					// Dec2MaxMin2
					// MaxStTimeDiff:- 8 hrs
					// MaxDuration:- 0.5 hrs
					// DistTravelled:- 2km,
					// StGeoDiff:- 10km
					// EndGeoDiff:- 10km
					// AvgAlt:- 100m
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 28800d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 1800d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 2d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 10d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 10d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 100d);

					// Dec2MaxMin3
					// MaxStTimeDiff:- 8 hrs
					// MaxDuration:- 1 hrs
					// DistTravelled:- 2km,
					// StGeoDiff:- 10km
					// EndGeoDiff:- 10km
					// AvgAlt:- 100m
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 28800d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 3600d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 2d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 10d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 10d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 100d);

					// Dec3MaxMin
					// MaxStTimeDiff:- 1 hr
					// MaxDuration:- 1 hr
					// DistTravelled:- 1km,
					// StGeoDiff:- 1km
					// EndGeoDiff:- 1km
					// AvgAlt:- 20m
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 3600d);// secs, 1 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 3600d);// sec, 1 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 1d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 1d);// 1km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 1d);// 1km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 20d);

					// Dec4MaxMin
					// MaxStTimeDiff:- 5 hrs
					// MaxDuration:- 20 mins
					// DistTravelled:- 500m,
					// StGeoDiff:- 10km
					// EndGeoDiff:- 10km
					// AvgAlt:- 60m

					maxOfMaxOfVals.put(GowGeoFeature.StartTimeF, (5d * 60 * 60));// secs, 1 hrs
					maxOfMaxOfVals.put(GowGeoFeature.DurationF, 20 * 60d);// sec, 1 hrs
					maxOfMaxOfVals.put(GowGeoFeature.DistTravelledF, 0.5d);// 5 km
					maxOfMaxOfVals.put(GowGeoFeature.StartGeoF, 10d);// 1km
					maxOfMaxOfVals.put(GowGeoFeature.EndGeoF, 10d);// 1km
					maxOfMaxOfVals.put(GowGeoFeature.AvgAltitudeF, 60d);

					minOfMinOfVals.put(GowGeoFeature.StartTimeF, 0d);// secs, 3 hrs
					minOfMinOfVals.put(GowGeoFeature.DurationF, 0d);// sec, 3 hrs
					minOfMinOfVals.put(GowGeoFeature.DistTravelledF, 0d);// 5 km
					minOfMinOfVals.put(GowGeoFeature.StartGeoF, 0d);// 5km
					minOfMinOfVals.put(GowGeoFeature.EndGeoF, 0d);// 5km
					minOfMinOfVals.put(GowGeoFeature.AvgAltitudeF, 0d);
				}
				else
				{
					PopUps.showError(
							"getMinOfMinsAndMaxOfMaxOfFeatureDiffs not implemented for database" + databaseName);
				}
			}
			else // use the pth percentile max
			{
				minOfMinOfVals = HJEditDistance.getSummaryStatOfSummaryStatForEachFeatureDiffOverList(
						summaryStatForEachCand, 0, userAtRecomm, dateAtRecomm, timeAtRecomm);

				double percentileForRTVerseMaxForFEDNorm = Constant.percentileForRTVerseMaxForFEDNorm;
				if (percentileForRTVerseMaxForFEDNorm == -1)
				{
					maxOfMaxOfVals = HJEditDistance.getSummaryStatOfSummaryStatForEachFeatureDiffOverList(
							summaryStatForEachCand, 1, userAtRecomm, dateAtRecomm, timeAtRecomm);
				}
				/////////////////// End of finding min max
				// Start of May8 addition
				else if (percentileForRTVerseMaxForFEDNorm > -1)// then replace maxOfMax by pth percentile val
				{
					// list over cands and then list over each AO in that cand
					List<List<EnumMap<GowGeoFeature, Pair<String, String>>>> listOfListOfFeatValPairs = candAEDFeatValPairs
							.entrySet().stream().map(e -> e.getValue().getThird()).collect(Collectors.toList());

					EnumMap<GowGeoFeature, Double> pRTVersePercentileOfDiffs = HJEditDistance
							.getPthPercentileInRTVerseOfValPairs(listOfListOfFeatValPairs,
									percentileForRTVerseMaxForFEDNorm);// 75);

					if (true)// sanity checking percentil implementation and maxOfMax implementation gives same result
					{
						sanityCheckRTVersePthPercentileByMinMaxFeatValPairs(minOfMinOfVals, maxOfMaxOfVals,
								listOfListOfFeatValPairs);
					}

					// replace maxOfMax by pPercentileVal
					maxOfMaxOfVals = pRTVersePercentileOfDiffs;
				}
			}
			// End of May 8 addition

			if (maxOfMaxOfVals.size() == 0)
			{
				PopUps.showError("Error: maxOfMaxOfDiffs.size() = " + maxOfMaxOfVals.size());
			}
		}

		if (shouldComputeFeatureLevelDistance && VerbosityConstants.WriteMInOfMinAndMaxOfMaxRTV)// sanity check
		{
			StringBuilder sbMin = new StringBuilder();
			StringBuilder sbMax = new StringBuilder();

			minOfMinOfVals.entrySet().stream().forEachOrdered(e -> sbMin.append(e.getValue() + ","));
			maxOfMaxOfVals.entrySet().stream().forEachOrdered(e -> sbMax.append(e.getValue() + ","));
			WToFile.appendLineToFileAbs(sbMin.toString() + "\n", Constant.getCommonPath() + "minOfMinOfDiffs.csv");
			WToFile.appendLineToFileAbs(sbMax.toString() + "\n", Constant.getCommonPath() + "maxOfMaxOfDiffs.csv");
		}
		return new Pair<>(minOfMinOfVals, maxOfMaxOfVals);
	}

	/////////

	/**
	 * To find max of max of feature diffs and min of min of feature diffs for RTVerse normalisation.
	 * 
	 * @param userAtRecomm
	 *            only for logging
	 * @param dateAtRecomm
	 *            only for logging
	 * @param timeAtRecomm
	 *            only for logging
	 * @param hjEditDistance
	 * @param candAEDFeatDiffs
	 *            one map entry for each cand, and for each cand, a list of enumaps, one for each AO in the cand
	 * @param useFixedMaxForEachFeature
	 *            instead of pth percentils
	 * @return
	 * @since 24 Nov 2018
	 */
	private static Pair<EnumMap<GowGeoFeature, Double>, EnumMap<GowGeoFeature, Double>> getMinOfMinsAndMaxOfMaxOfFeatureDiffs(
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm, HJEditDistance hjEditDistance,
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs,
			boolean useFixedMaxForEachFeature)
	{
		boolean shouldComputeFeatureLevelDistance = hjEditDistance.getShouldComputeFeatureLevelDistance();
		StringBuilder sbToWrite = new StringBuilder(userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm);
		EnumMap<GowGeoFeature, Double> minOfMinOfDiffs = null;
		EnumMap<GowGeoFeature, Double> maxOfMaxOfDiffs = null;

		// loop over all res to find max and min for normalisation
		// For each candidate, finding max feature diff for each gowalla feature over all the activity objects in that
		// candidate timeline. (horizontal aggregations)
		List<EnumMap<GowGeoFeature, DoubleSummaryStatistics>> summaryStatForEachCand = null;
		if (shouldComputeFeatureLevelDistance)
		{
			summaryStatForEachCand = candAEDFeatDiffs.entrySet().stream()
					.map(e -> HJEditDistance.getSummaryStatsForEachFeatureDiffOverListOfAOsInACand(
							e.getValue().getThird(), userAtRecomm, dateAtRecomm, timeAtRecomm, e.getKey()))
					.collect(Collectors.toList());
		}

		// Aggregation (across candidate timelines) of aggregation (across activity objects in each cand
		// timeline:summaryStatForEachCand)

		if (shouldComputeFeatureLevelDistance)
		{ // Compute minOfMinOfDiffs and maxOfMaxOfDiff (pth percentile as max)
			minOfMinOfDiffs = new EnumMap<>(GowGeoFeature.class);
			maxOfMaxOfDiffs = new EnumMap<>(GowGeoFeature.class);

			if (useFixedMaxForEachFeature)
			{
				String databaseName = Constant.getDatabaseName();
				if (databaseName.equals("geolife1"))
				{
					// Nov30MaxMin
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 10800d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 10800d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 5d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 5d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 5d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 60d);
					//

					// Dec1MaxMin
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 10800d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 10800d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 2d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 2d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 2d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 40d);

					// Dec2MaxMin
					// MaxStTimeDiff:- 5 hrs
					// MaxDuration:- 1 hrs
					// DistTravelled:- 2km,
					// StGeoDiff:- 4km
					// EndGeoDiff:- 4km
					// AvgAlt:- 60m
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 18000d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 3600d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 2d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 4d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 4d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 60d);

					// Dec2MaxMin2
					// MaxStTimeDiff:- 8 hrs
					// MaxDuration:- 0.5 hrs
					// DistTravelled:- 2km,
					// StGeoDiff:- 10km
					// EndGeoDiff:- 10km
					// AvgAlt:- 100m
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 28800d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 1800d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 2d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 10d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 10d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 100d);

					// Dec2MaxMin3
					// MaxStTimeDiff:- 8 hrs
					// MaxDuration:- 1 hrs
					// DistTravelled:- 2km,
					// StGeoDiff:- 10km
					// EndGeoDiff:- 10km
					// AvgAlt:- 100m
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 28800d);// secs, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 3600d);// sec, 3 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 2d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 10d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 10d);// 5km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 100d);

					// Dec3MaxMin
					// MaxStTimeDiff:- 1 hr
					// MaxDuration:- 1 hr
					// DistTravelled:- 1km,
					// StGeoDiff:- 1km
					// EndGeoDiff:- 1km
					// AvgAlt:- 20m
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, 3600d);// secs, 1 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 3600d);// sec, 1 hrs
					// maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 1d);// 5 km
					// maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 1d);// 1km
					// maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 1d);// 1km
					// maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 20d);

					// Dec4MaxMin
					// MaxStTimeDiff:- 5 hrs
					// MaxDuration:- 20 mins
					// DistTravelled:- 500m,
					// StGeoDiff:- 10km
					// EndGeoDiff:- 10km
					// AvgAlt:- 60m

					maxOfMaxOfDiffs.put(GowGeoFeature.StartTimeF, (5d * 60 * 60));// secs, 1 hrs
					maxOfMaxOfDiffs.put(GowGeoFeature.DurationF, 20 * 60d);// sec, 1 hrs
					maxOfMaxOfDiffs.put(GowGeoFeature.DistTravelledF, 0.5d);// 5 km
					maxOfMaxOfDiffs.put(GowGeoFeature.StartGeoF, 10d);// 1km
					maxOfMaxOfDiffs.put(GowGeoFeature.EndGeoF, 10d);// 1km
					maxOfMaxOfDiffs.put(GowGeoFeature.AvgAltitudeF, 60d);

					minOfMinOfDiffs.put(GowGeoFeature.StartTimeF, 0d);// secs, 3 hrs
					minOfMinOfDiffs.put(GowGeoFeature.DurationF, 0d);// sec, 3 hrs
					minOfMinOfDiffs.put(GowGeoFeature.DistTravelledF, 0d);// 5 km
					minOfMinOfDiffs.put(GowGeoFeature.StartGeoF, 0d);// 5km
					minOfMinOfDiffs.put(GowGeoFeature.EndGeoF, 0d);// 5km
					minOfMinOfDiffs.put(GowGeoFeature.AvgAltitudeF, 0d);
				}
				else
				{
					PopUps.showError(
							"getMinOfMinsAndMaxOfMaxOfFeatureDiffs not implemented for database" + databaseName);
				}
			}
			else // use the pth percentile max
			{
				minOfMinOfDiffs = HJEditDistance.getSummaryStatOfSummaryStatForEachFeatureDiffOverList(
						summaryStatForEachCand, 0, userAtRecomm, dateAtRecomm, timeAtRecomm);

				double percentileForRTVerseMaxForFEDNorm = Constant.percentileForRTVerseMaxForFEDNorm;
				if (percentileForRTVerseMaxForFEDNorm == -1)
				{
					maxOfMaxOfDiffs = HJEditDistance.getSummaryStatOfSummaryStatForEachFeatureDiffOverList(
							summaryStatForEachCand, 1, userAtRecomm, dateAtRecomm, timeAtRecomm);
				}
				/////////////////// End of finding min max
				// Start of May8 addition
				else if (percentileForRTVerseMaxForFEDNorm > -1)// then replace maxOfMax by pth percentile val
				{
					// list over cands and then list over each AO in that cand
					List<List<EnumMap<GowGeoFeature, Double>>> listOfListOfFeatDiffs = candAEDFeatDiffs.entrySet()
							.stream().map(e -> e.getValue().getThird()).collect(Collectors.toList());

					EnumMap<GowGeoFeature, Double> pRTVersePercentileOfDiffs = HJEditDistance
							.getPthPercentileInRTVerseOfDiffs(listOfListOfFeatDiffs, percentileForRTVerseMaxForFEDNorm);// 75);

					if (false)// sanity checking percentil implementation and maxOfMax implementation gives same result
					{// passed ok on May 8 2018
						sanityCheckRTVersePthPercentileByMinMax(minOfMinOfDiffs, maxOfMaxOfDiffs,
								listOfListOfFeatDiffs);
					}

					// replace maxOfMax by pPercentileVal
					maxOfMaxOfDiffs = pRTVersePercentileOfDiffs;
				}
			}
			// End of May 8 addition

			if (maxOfMaxOfDiffs.size() == 0)
			{
				PopUps.showError("Error: maxOfMaxOfDiffs.size() = " + maxOfMaxOfDiffs.size());
			}
		}

		if (shouldComputeFeatureLevelDistance && VerbosityConstants.WriteMInOfMinAndMaxOfMaxRTV)// sanity check
		{
			StringBuilder sbMin = new StringBuilder();
			StringBuilder sbMax = new StringBuilder();

			minOfMinOfDiffs.entrySet().stream().forEachOrdered(e -> sbMin.append(e.getValue() + ","));
			maxOfMaxOfDiffs.entrySet().stream().forEachOrdered(e -> sbMax.append(e.getValue() + ","));
			WToFile.appendLineToFileAbs(sbMin.toString() + "\n", Constant.getCommonPath() + "minOfMinOfDiffs.csv");
			WToFile.appendLineToFileAbs(sbMax.toString() + "\n", Constant.getCommonPath() + "maxOfMaxOfDiffs.csv");
		}
		return new Pair<>(minOfMinOfDiffs, maxOfMaxOfDiffs);

	}

	/**
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param givenDimension
	 * @since 26 July 2018
	 */
	private static void getListOfUniqueGivenDimensionValsInRTVerse(LinkedHashMap<String, Timeline> candidateTimelines,
			ArrayList<ActivityObject2018> activitiesGuidingRecomm, PrimaryDimension givenDimension)
	{
		Set<Integer> uniqueLocGridIDInRTVerse = new TreeSet<>();
		List<Integer> uniqueLocGridIDInRTVerseList = new ArrayList<>();
		uniqueLocGridIDInRTVerse
				.addAll(activitiesGuidingRecomm.stream().map(ao -> ao.getGivenDimensionVal(givenDimension))
						.flatMap(l -> l.stream()).collect(Collectors.toSet()));
		for (Entry<String, Timeline> e : candidateTimelines.entrySet())
		{
			Set<Integer> uniqueLocIDsInThisCand = e.getValue().getActivityObjectsInTimeline().stream()
					.map(ao -> ao.getGivenDimensionVal(givenDimension)).flatMap(l -> l.stream())
					.collect(Collectors.toSet());
			uniqueLocGridIDInRTVerse.addAll(uniqueLocIDsInThisCand);
		}
		uniqueLocGridIDInRTVerseList.addAll(uniqueLocGridIDInRTVerse);
		System.out.println("uniqueLocGridIDInRTVerseList.size() = " + uniqueLocGridIDInRTVerse);
	}

	/**
	 * Fork oorg.activity.distances.DistanceUtils.sanityCheckRTVersePthPercentileByMinMax()
	 * 
	 * @param minOfMinOfDiffs
	 * @param maxOfMaxOfDiffs
	 * @param listOfListOfFeatDiffs
	 * @return
	 */
	private static boolean sanityCheckRTVersePthPercentileByMinMaxFeatValPairs(
			EnumMap<GowGeoFeature, Double> minOfMinOfDiffs, EnumMap<GowGeoFeature, Double> maxOfMaxOfDiffs,
			List<List<EnumMap<GowGeoFeature, Pair<String, String>>>> listOfListOfFeatValPairs)
	{
		boolean sane = true;
		EnumMap<GowGeoFeature, Double> p100RTVersePercentileOfDiffs = HJEditDistance
				.getPthPercentileInRTVerseOfValPairs(listOfListOfFeatValPairs, 100);

		EnumMap<GowGeoFeature, Double> p1RTVersePercentileOfDiffs = HJEditDistance
				.getPthPercentileInRTVerseOfValPairs(listOfListOfFeatValPairs, 1e-55);

		if (p100RTVersePercentileOfDiffs.equals(maxOfMaxOfDiffs) == false)
		{
			PopUps.showError("Error p100RTVersePercentileOfDiffs.equals(maxOfMaxOfDiffs)==false");
			sane = false;
		}
		if (p1RTVersePercentileOfDiffs.equals(minOfMinOfDiffs) == false)
		{
			PopUps.showError("Error p1RTVersePercentileOfDiffs.equals(minOfMinOfDiffs)==false");
			sane = false;
		}
		WToFile.appendLineToFileAbs(
				"p100RTVersePercentileOfDiffs=\n" + p100RTVersePercentileOfDiffs.toString() + "\n"
						+ "maxOfMaxOfDiffs=\n" + maxOfMaxOfDiffs.toString() + "\n" + "p1RTVersePercentileOfDiffs=\n"
						+ p1RTVersePercentileOfDiffs.toString() + "\n" + "minOfMinOfDiffs=\n"
						+ minOfMinOfDiffs.toString() + "\nsane=" + sane + "\n\n",
				Constant.getCommonPath() + "Debug9Jan2019PthPercentileInRTVerseSanityLog.csv");
		return sane;
	}

	/**
	 * passed ok on May 8 2018
	 * 
	 * @param minOfMinOfDiffs
	 * @param maxOfMaxOfDiffs
	 * @param listOfListOfFeatDiffs
	 */
	private static boolean sanityCheckRTVersePthPercentileByMinMax(EnumMap<GowGeoFeature, Double> minOfMinOfDiffs,
			EnumMap<GowGeoFeature, Double> maxOfMaxOfDiffs,
			List<List<EnumMap<GowGeoFeature, Double>>> listOfListOfFeatDiffs)
	{
		boolean sane = true;
		EnumMap<GowGeoFeature, Double> p100RTVersePercentileOfDiffs = HJEditDistance
				.getPthPercentileInRTVerseOfDiffs(listOfListOfFeatDiffs, 100);

		EnumMap<GowGeoFeature, Double> p1RTVersePercentileOfDiffs = HJEditDistance
				.getPthPercentileInRTVerseOfDiffs(listOfListOfFeatDiffs, 1e-55);

		if (p100RTVersePercentileOfDiffs.equals(maxOfMaxOfDiffs) == false)
		{
			PopUps.showError("Error p100RTVersePercentileOfDiffs.equals(maxOfMaxOfDiffs)==false");
			sane = false;
		}
		if (p1RTVersePercentileOfDiffs.equals(minOfMinOfDiffs) == false)
		{
			PopUps.showError("Error p1RTVersePercentileOfDiffs.equals(minOfMinOfDiffs)==false");
			sane = false;
		}
		WToFile.appendLineToFileAbs(
				"p100RTVersePercentileOfDiffs=\n" + p100RTVersePercentileOfDiffs.toString() + "\n"
						+ "maxOfMaxOfDiffs=\n" + maxOfMaxOfDiffs.toString() + "\n" + "p1RTVersePercentileOfDiffs=\n"
						+ p1RTVersePercentileOfDiffs.toString() + "\n" + "minOfMinOfDiffs=\n"
						+ minOfMinOfDiffs.toString() + "\nsane=" + sane + "\n\n",
				Constant.getCommonPath() + "Debug8MayPthPercentileInRTVerseSanityLog.csv");
		return sane;
	}

	/**
	 * Fork of org.activity.distances.DistanceUtils.getRTVerseMinMaxNormalisedEditDistancesV1Incorrect()
	 * <p>
	 * Min-max normalise each feature difference to obtain feature level distance, where min/max for each featureDiff is
	 * the min/max feature diff over all (compared to current) activity objects of all (compared) candidates.
	 * <p>
	 * Compute FED for each AO and then takes the mead FED over all AOs in that cand as the FED for that cand.
	 * 
	 * @param candAEDFeatDiffs
	 * @param minOfMinOfDiffs
	 * @param maxOfMaxOfDiffs
	 * @param hjEditDistance
	 * @param timeAtRecomm
	 *            just for logging
	 * @param dateAtRecomm
	 *            just for logging
	 * @param userAtRecomm
	 *            just for logging
	 * @param activitiesGuidingRecomm
	 *            just for logging
	 * @param candidateTimelines
	 *            just for logging
	 * @since April 22 2018
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 */
	private static LinkedHashMap<String, Pair<String, Double>> getRTVerseMinMaxNormalisedEditDistances(
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs,
			EnumMap<GowGeoFeature, Double> minOfMinOfDiffs, EnumMap<GowGeoFeature, Double> maxOfMaxOfDiffs,
			HJEditDistance hjEditDistance, ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm, LinkedHashMap<String, Timeline> candidateTimelines)
	{
		LinkedHashMap<String, Pair<String, Double>> res = new LinkedHashMap<>(candAEDFeatDiffs.size());

		EnumMap<GowGeoFeature, Double> featureWeightMap = hjEditDistance.getFeatureWeightMap();

		double EDAlpha = Constant.getDynamicEDAlpha();// .dynamicEDAlpha;
		double EDBeta = 1 - EDAlpha;

		/////
		if (hjEditDistance.getShouldComputeFeatureLevelDistance() == false && EDAlpha != 1)
		{
			System.err
					.println("Warning: -- Since no features are being used it is suggested to set Constant.EDAlpha=1.\n"
							+ "so that the computed values for dAct are not multiplied by EDAlpha and reduced.");
		}
		/////

		// double EDGamma;
		double sumOfWtOfFeaturesUsedExceptPD = hjEditDistance.getSumOfWeightOfFeaturesExceptPrimaryDimension();

		// Start of Get max of ActED over all cand
		double maxActEDOverAllCands = Double.MIN_VALUE;
		// candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond()).max().getAsDouble();

		// Start of added on 15 Aug 2018
		double percentileForRTVerseMaxForAEDNorm = Constant.percentileForRTVerseMaxForAEDNorm;
		if (percentileForRTVerseMaxForAEDNorm == -1)
		{
			maxActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond()).max()
					.getAsDouble();
		}
		else
		{
			List<Double> listOfAEDs = candAEDFeatDiffs.entrySet().stream().map(e -> e.getValue().getSecond())
					.collect(Collectors.toList());
			maxActEDOverAllCands = StatsUtils.getPercentile(listOfAEDs, percentileForRTVerseMaxForAEDNorm);
		}
		// End of added on 15 Aug 2018

		if (true)// sanity check
		{
			if (maxActEDOverAllCands < Constant.epsilonForFloatZero)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("Debug18July:maxActEDOverAllCands= " + maxActEDOverAllCands + "\n");
				candAEDFeatDiffs.entrySet().stream()// .mapToDouble(e -> e.getValue().getSecond())
						.forEachOrdered(
								e -> sb.append(" " + e.getValue().getFirst() + "--" + e.getValue().getSecond() + ","));
				System.out.println(sb.toString());
			}
		}

		double minActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
				.min().getAsDouble();
		// End of Get max of ActED over all cand

		// Start of initialsing logging
		StringBuilder logTxt = new StringBuilder(
				"\n--------- START OF getRTVerseMinMaxNormalisedEditDistances() with EDAlpha=" + EDAlpha + " EDBeta="
						+ EDBeta + " sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD
						+ " maxActEDOverAllCands" + maxActEDOverAllCands + " minActEDOverAllCands="
						+ minActEDOverAllCands + '\n');
		featureWeightMap.entrySet().stream()
				.forEachOrdered(e -> logTxt.append(" " + e.getKey().toString() + "-" + e.getValue()));
		StringBuilder logEachCand = new StringBuilder();// one for each cand
		// candID,AEDTraceForThisCand,ActDistForThisCand,countOfAOForThisCand,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featureDistForThisAOForThisCand,featureDistForThisCand,normActDistForThisCand,resultantEditDist
		StringBuilder logEachAOAllCands = new StringBuilder();// one for each AO of each cand
		String currentTimeline = activitiesGuidingRecomm.stream().map(ao -> ao.getPrimaryDimensionVal("|"))
				.collect(Collectors.joining(">"));
		String rtInfo = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candAEDFeatDiffs.size() + ","
				+ currentTimeline;
		// end of initialising logging

		int indexOfCandForThisRT = -1;

		/** {CandID, candInfo, NormAED, NormFED} **/
		Map<String, String> rejectedCandsDueToAEDFEDThreshold = new LinkedHashMap<>();

		StringBuilder logAOsThisCandHeader = new StringBuilder(
				"\nuserAtRecomm,dateAtRecomm,timeAtRecomm,candAEDFeatDiffs.size(),currentTimeline,candID,indexOfCandForThisRT,candTimelineAsString,AEDTraceForThisCand,actDistForThisCand,");

		// Loop over cands
		for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candEntry : candAEDFeatDiffs
				.entrySet())
		{
			indexOfCandForThisRT += 1;
			String candID = candEntry.getKey();

			String AEDTraceForThisCand = candEntry.getValue().getFirst();
			double actDistForThisCand = candEntry.getValue().getSecond();

			/////////
			StringBuilder logAOsThisCand = new StringBuilder();
			// StringBuilder logAOsThisCandHeader = new StringBuilder();
			String candTimelineAsString = candidateTimelines.get(candID).getActivityObjectsInTimeline().stream()
					.map(ao -> ao.getActivityName()).collect(Collectors.joining(">"));
			logTxt.append("\n\tcandID=" + candID + " AEDTraceForThisCand=" + AEDTraceForThisCand
					+ " ActDistForThisCand=" + actDistForThisCand + " will now loop over list of AOs for this cand:");
			String candInfo = rtInfo + "," + candID + "," + indexOfCandForThisRT + "," + candTimelineAsString + ","
					+ AEDTraceForThisCand + "," + actDistForThisCand;

			String candInfoLeaner = rtInfo + "," + candID + "," + indexOfCandForThisRT;
			////////////

			List<EnumMap<GowGeoFeature, Double>> listOfAOsForThisCand = candEntry.getValue().getThird();
			// note: list in in intial order, i.e., least recent AO to most recent AO by time.
			double sumOfNormFDsOverAOsOfThisCand = 0;
			double[] normFDsOverAOsOfThisCand = new double[listOfAOsForThisCand.size()];

			int indexOfAOForThisCand = -1;

			// loop over the list of AO for this cand
			for (EnumMap<GowGeoFeature, Double> mapOfFeatureDiffForAnAO : listOfAOsForThisCand)
			{
				indexOfAOForThisCand += 1;
				double featDistForThisAOForThisCand = 0;// double sanityCheckFeatureWtSum = 0;

				///////
				logTxt.append(
						"\n\t\tcountOfAOForThisCand=" + indexOfAOForThisCand + " now loop over features for this AO");
				// logEachAO
				logAOsThisCand.append(candInfo + "," + indexOfAOForThisCand + ",");
				if (indexOfAOForThisCand == 1)
				{
					logAOsThisCandHeader.append("indexOfAOForThisCand,");
				}
				////////

				// loop over each of the Gowalla Feature
				for (Entry<GowGeoFeature, Double> diffEntry : mapOfFeatureDiffForAnAO.entrySet())
				{
					GowGeoFeature featureID = diffEntry.getKey();

					// $$Disabled on May 14: double normalisedFeatureDiffVal =
					// StatsUtils.minMaxNormWORound(diffEntry.getValue(),maxOfMaxOfDiffs.get(featureID),
					// minOfMinOfDiffs.get(featureID));

					double normalisedFeatureDiffVal = StatsUtils.minMaxNormWORoundWithUpperBound(diffEntry.getValue(),
							maxOfMaxOfDiffs.get(featureID), minOfMinOfDiffs.get(featureID), 1.0d, false);

					double wtForThisFeature = featureWeightMap.get(featureID);
					featDistForThisAOForThisCand += (wtForThisFeature * normalisedFeatureDiffVal);

					//////
					logTxt.append("\n\t\t\tfeatureID=" + featureID + " featDiff=" + diffEntry.getValue()
							+ " normalisedFeatureDiffVal=" + normalisedFeatureDiffVal + " wtForThisFeature="
							+ wtForThisFeature + " featureDistForThisAOForThisCand=" + featDistForThisAOForThisCand
							+ " maxOfMaxOfDiffs=" + maxOfMaxOfDiffs.get(featureID) + " minOfMinOfDiffs="
							+ minOfMinOfDiffs.get(featureID));
					logAOsThisCand.append(StatsUtils.roundAsString(diffEntry.getValue(), 4) + ","
							+ StatsUtils.roundAsString(normalisedFeatureDiffVal, 4) + ",");// rounding for logging

					if (indexOfAOForThisCand == 1)
					{
						logAOsThisCandHeader.append(featureID.toString() + "," + featureID.toString() + "Normalised,");
					}
					////// sanityCheckFeatureWtSum += wtForThisFeature;
				} // end of loop over Gowalla features

				// For SanityCheck sanityCheckFeatureWtSum
				// if (true){ Sanity.eq(sanityCheckFeatureWtSum,
				// sumOfWtOfFeaturesUsedExceptPD,"Error:sanityCheckFeatureWtSum=" +sanityCheckFeatureWtSum+ "
				// sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD);}

				double normFDForThisAOForThisCand = featDistForThisAOForThisCand / sumOfWtOfFeaturesUsedExceptPD;
				sumOfNormFDsOverAOsOfThisCand += normFDForThisAOForThisCand;
				normFDsOverAOsOfThisCand[indexOfAOForThisCand] = normFDForThisAOForThisCand;

				/////////
				logTxt.append("\n\t\tfeatDistForThisAOForThisCand=" + featDistForThisAOForThisCand);
				logAOsThisCand.append("|" + "," + StatsUtils.roundAsString(normFDForThisAOForThisCand, 4) + "\n");
				if (indexOfAOForThisCand == 1)
				{
					logAOsThisCandHeader.append("|,normFDForThisAOForThisCand,");
				}
				////////
			} // end of loop over the list of AOs for this cand

			// double normActDistForThisCand = StatsUtils.minMaxNormWORound(actDistForThisCand, maxActEDOverAllCands,
			// minActEDOverAllCands);
			double normActDistForThisCand = StatsUtils.minMaxNormWORoundWithUpperBound(actDistForThisCand,
					maxActEDOverAllCands, minActEDOverAllCands, 1d, false);
			//

			double meanOverAOsNormFDForThisCand = sumOfNormFDsOverAOsOfThisCand / listOfAOsForThisCand.size();
			double medianOverAOsNormFDForThisCand = StatUtils.percentile(normFDsOverAOsOfThisCand, 50);
			double varianceOverAOsNormFDForThisCand = StatUtils.variance(normFDsOverAOsOfThisCand);
			double stdDevOverAOsNormFDForThisCand = Math.sqrt(varianceOverAOsNormFDForThisCand);
			double meanUponStdDev = meanOverAOsNormFDForThisCand / stdDevOverAOsNormFDForThisCand;

			// IMPORTANT
			double resultantEditDist = -999999;
			boolean thisCandRejected = false;
			String thisCandRejectedString = "NR";

			double threshNormAEDForCand = Constant.threshNormAEDForCand;
			double threshNormFEDForCand = Constant.threshNormFEDForCand;

			if (EDBeta == 0)
			{// since 0*NaN is NaN
				if (threshNormAEDForCand != -1 && normActDistForThisCand >= threshNormAEDForCand)// added on 14 Aug 2018
				{
					thisCandRejected = true;// reject this cand
					thisCandRejectedString = "AR";
				}
				else
				{
					resultantEditDist = (EDAlpha) * normActDistForThisCand;
				}
			}
			else
			{
				if (threshNormAEDForCand != -1 && normActDistForThisCand >= threshNormAEDForCand)// added on 14 Aug 2018
				{
					thisCandRejected = true;// reject this cand
					thisCandRejectedString = "AR";
				}
				else if (threshNormFEDForCand != -1 && meanOverAOsNormFDForThisCand >= threshNormFEDForCand)
				{
					thisCandRejected = true;// reject this cand
					thisCandRejectedString = "FR";
				}
				else
				{
					resultantEditDist = (EDAlpha) * normActDistForThisCand + (EDBeta) * meanOverAOsNormFDForThisCand;
				}
			}

			if (thisCandRejected == false)
			{
				res.put(candEntry.getKey(), new Pair<>(AEDTraceForThisCand, resultantEditDist));
			}
			else
			{
				if (VerbosityConstants.WriteRTVerseNormalisationLogs)
				{
					rejectedCandsDueToAEDFEDThreshold.put(candID,
							candInfo + "," + normActDistForThisCand + "," + meanOverAOsNormFDForThisCand);
				}
				// else
				// {// less expensive for writing
				// rejectedCandsDueToAEDFEDThreshold.put(candID, candInfo);//}
			}

			/////////
			logTxt.append("\n\tsumOfFeatDistsOverAOsOfThisCand=" + sumOfNormFDsOverAOsOfThisCand
					+ " normActDistForThisCand=" + normActDistForThisCand + " normFDForThisCand="
					+ meanOverAOsNormFDForThisCand + "\n\t-->resultantEditDist=" + resultantEditDist);
			logEachCand.append(candInfo + "," + normActDistForThisCand + "," + sumOfNormFDsOverAOsOfThisCand + ","
					+ meanOverAOsNormFDForThisCand + "," + medianOverAOsNormFDForThisCand + ","
					+ stdDevOverAOsNormFDForThisCand + "," + meanUponStdDev + "," + resultantEditDist + ","
					+ thisCandRejectedString + "\n");
			// logEachAO.append(",,,,,,,,,,,,,,,," + featureDistForThisCand + "," + normActDistForThisCand + ","
			// + resultantEditDist + "\n");

			// Sanity check start
			if (meanOverAOsNormFDForThisCand > 1)
			{
				WToFile.appendLineToFileAbs(meanOverAOsNormFDForThisCand + "\n" + logAOsThisCand,
						Constant.getCommonPath() + "ErrorDebug22April2018.csv");
				// System.out.println("\nDebug22April2018: normFeatureDistForThisCand=" + normFeatureDistForThisCand
				// + "\nlogAOThisCand=\n" + logAOsThisCand.toString() + "\nsumOfWtOfFeaturesUsedExceptPD="
				// + sumOfWtOfFeaturesUsedExceptPD);
			}
			// Sanity check end
			logEachAOAllCands.append(logAOsThisCand.toString());
			/////////
		} // end of loop over cands

		String commonPath = Constant.getCommonPath();
		if (VerbosityConstants.WriteRTVerseNormalisationLogs || VerbosityConstants.verboseDistDistribution)// logging
		{
			// WToFile.appendLineToFileAbs(logTxt.toString() + "\n",
			// Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistances.txt");
			WToFile.appendLineToFileAbs(logEachCand.toString(),
					commonPath + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachCand.csv");

			if (VerbosityConstants.WriteRTVerseNormalisationLogs)
			{
				// header:
				// userAtRecomm,dateAtRecomm,timeAtRecomm,candAEDFeatDiffs.size(),currentTimeline,candID,indexOfCandForThisRT,candTimelineAsString,AEDTraceForThisCand,actDistForThisCand,normActDistForThisCand,sumOfNormFDsOverAOsOfThisCand,meanOverAOsNormFDForThisCand,medianOverAOsNormFDForThisCand,stdDevOverAOsNormFDForThisCand,meanUponStdDev,resultantEditDist
				WToFile.appendLineToFileAbs(logEachAOAllCands.toString(),
						commonPath + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachAO.csv");
				WToFile.appendLineToFileAbs(logAOsThisCandHeader.toString(),
						commonPath + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachAOHeader.csv");
			}
		}

		// write rejected cands for AED FED Threshold, added on 14 Aug 2018
		if (VerbosityConstants.WriteRTVerseNormalisationLogs || VerbosityConstants.verboseDistDistribution)
		{
			StringBuilder sb1 = new StringBuilder();
			rejectedCandsDueToAEDFEDThreshold.entrySet().stream()
					.forEachOrdered(e -> sb1.append(e.getKey() + "," + e.getValue() + "\n"));
			WToFile.appendLineToFileAbs(sb1.toString(), commonPath + "LogOfRejectedCands.csv");
		}

		if (res.size() == 0)// all candidates filtered out due to AED FED Threshold
		{
			WToFile.appendLineToFileAbs(rtInfo + "\n", commonPath + "recommPointsRejAllCandFiltrdAEDFEDThresh.csv");
		}

		logTxt.append("\n---------End  getRTVerseMinMaxNormalisedEditDistances()\n");
		if (VerbosityConstants.verbose)
		{
			System.out.println(logTxt.toString());
		}

		return res;
	}

	// start of added on 23 Nov 2018

	/**
	 * Fork of org.activity.distances.DistanceUtils.getRTVerseMinMaxNormalisedEditDistances()
	 * <p>
	 * Aggregate across AO first, i.e. mean over each featureDiff sequence. FED = wtd sum of mean of eeach featureDiff
	 * sequence.
	 * <p>
	 * Min-max normalise each feature difference to obtain feature level distance, where min/max for each featureDiff is
	 * the min/max feature diff over all (compared to current) activity objects of all (compared) candidates.
	 * <p>
	 * 
	 * @param candAEDFeatDiffs
	 * @param minOfMinOfFeatDiffs
	 * @param maxOfMaxOfFeatDiffs
	 * @param hjEditDistance
	 * @param timeAtRecomm
	 *            just for logging
	 * @param dateAtRecomm
	 *            just for logging
	 * @param userAtRecomm
	 *            just for logging
	 * @param activitiesGuidingRecomm
	 *            just for logging
	 * @param candidateTimelines
	 *            just for logging
	 * @since Nov 23 2018
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 */
	private static LinkedHashMap<String, Pair<String, Double>> getRTVerseMinMaxNormalisedEditDistancesFeatSeqApproach(
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs,
			EnumMap<GowGeoFeature, Double> minOfMinOfFeatDiffs, EnumMap<GowGeoFeature, Double> maxOfMaxOfFeatDiffs,
			HJEditDistance hjEditDistance, ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm, LinkedHashMap<String, Timeline> candidateTimelines)
	{
		// PopUps.showMessage("Inside getRTVerseMinMaxNormalisedEditDistancesFeatSeqApproach");
		// boolean useMSD = Constant.useMSDInFEDInRTVerse;
		LinkedHashMap<String, Pair<String, Double>> res = new LinkedHashMap<>(candAEDFeatDiffs.size());

		EnumMap<GowGeoFeature, Double> featureWeightMap = hjEditDistance.getFeatureWeightMap();

		double EDAlpha = Constant.getDynamicEDAlpha();// .dynamicEDAlpha;
		double EDBeta = 1 - EDAlpha;

		boolean shouldComputedFED = hjEditDistance.getShouldComputeFeatureLevelDistance();
		/////
		if (shouldComputedFED == false && EDAlpha != 1)
		{
			System.err
					.println("Warning: -- Since no features are being used it is suggested to set Constant.EDAlpha=1.\n"
							+ "so that the computed values for dAct are not multiplied by EDAlpha and reduced.");
		}
		/////

		// double EDGamma;
		double sumOfWtOfFeaturesUsedExceptPD = hjEditDistance.getSumOfWeightOfFeaturesExceptPrimaryDimension();

		// Start of Get max of ActED over all cand
		double maxActEDOverAllCands = Double.MIN_VALUE;
		// candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond()).max().getAsDouble();

		// Start of added on 15 Aug 2018
		double percentileForRTVerseMaxForAEDNorm = Constant.percentileForRTVerseMaxForAEDNorm;
		if (percentileForRTVerseMaxForAEDNorm == -1)
		{
			maxActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond()).max()
					.getAsDouble();
		}
		else
		{
			List<Double> listOfAEDs = candAEDFeatDiffs.entrySet().stream().map(e -> e.getValue().getSecond())
					.collect(Collectors.toList());
			maxActEDOverAllCands = StatsUtils.getPercentile(listOfAEDs, percentileForRTVerseMaxForAEDNorm);
		}
		// End of added on 15 Aug 2018

		if (true)// sanity check
		{
			if (maxActEDOverAllCands < Constant.epsilonForFloatZero)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("Debug18July:maxActEDOverAllCands= " + maxActEDOverAllCands + "\n");
				candAEDFeatDiffs.entrySet().stream()// .mapToDouble(e -> e.getValue().getSecond())
						.forEachOrdered(
								e -> sb.append(" " + e.getValue().getFirst() + "--" + e.getValue().getSecond() + ","));
				System.out.println(sb.toString());
			}
		}

		double minActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
				.min().getAsDouble();
		// End of Get max of ActED over all cand

		// Start of initialsing logging
		StringBuilder logTxt = new StringBuilder(
				"\n--------- START OF getRTVerseMinMaxNormalisedEditDistances() with EDAlpha=" + EDAlpha + " EDBeta="
						+ EDBeta + " sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD
						+ " maxActEDOverAllCands" + maxActEDOverAllCands + " minActEDOverAllCands="
						+ minActEDOverAllCands + '\n');
		featureWeightMap.entrySet().stream()
				.forEachOrdered(e -> logTxt.append(" " + e.getKey().toString() + "-" + e.getValue()));
		StringBuilder logEachCand = new StringBuilder();// one for each cand
		// candID,AEDTraceForThisCand,ActDistForThisCand,countOfAOForThisCand,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featureDistForThisAOForThisCand,featureDistForThisCand,normActDistForThisCand,resultantEditDist
		StringBuilder logEachAOAllCands = new StringBuilder();// one for each AO of each cand
		String currentTimeline = "";// activitiesGuidingRecomm.stream().map(ao -> ao.getPrimaryDimensionVal("|"))
		// .collect(Collectors.joining(">"));
		currentTimeline = activitiesGuidingRecomm.stream().map(ao -> ao.getActivityName())
				.collect(Collectors.joining(">"));

		String rtInfo = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candAEDFeatDiffs.size() + ","
				+ currentTimeline;
		// end of initialising logging

		int indexOfCandForThisRT = -1;

		/** {CandID, candInfo, NormAED, NormFED} **/
		Map<String, String> rejectedCandsDueToAEDFEDThreshold = new LinkedHashMap<>();
		ArrayList<GowGeoFeature> listOfFeatures = null;
		StringBuilder logAOsThisCandHeader = new StringBuilder(
				"\nuserAtRecomm,dateAtRecomm,timeAtRecomm,candAEDFeatDiffs.size(),currentTimeline,candID,indexOfCandForThisRT,candTimelineAsString,AEDTraceForThisCand,actDistForThisCand,");
		// Loop over cands
		for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candEntry : candAEDFeatDiffs
				.entrySet())
		{
			indexOfCandForThisRT += 1;
			String candID = candEntry.getKey();

			String AEDTraceForThisCand = candEntry.getValue().getFirst();
			double actDistForThisCand = candEntry.getValue().getSecond();

			/////////
			StringBuilder logAOsThisCand = new StringBuilder();
			String candTimelineAsString = candidateTimelines.get(candID).getActivityObjectsInTimeline().stream()
					.map(ao -> ao.getActivityName()).collect(Collectors.joining(">"));
			logTxt.append("\n\tcandID=" + candID + " AEDTraceForThisCand=" + AEDTraceForThisCand
					+ " ActDistForThisCand=" + actDistForThisCand + " will now loop over list of AOs for this cand:");
			String candInfo = rtInfo + "," + candID + "," + indexOfCandForThisRT + "," + candTimelineAsString + ","
					+ AEDTraceForThisCand + "," + actDistForThisCand;

			String candInfoLeaner = rtInfo + "," + candID + "," + indexOfCandForThisRT;
			////////////

			List<EnumMap<GowGeoFeature, Double>> listOfAOsForThisCand = candEntry.getValue().getThird();
			// note: list in in intial order, i.e., least recent AO to most recent AO by time.
			// double sumOfNormFDsOverAOsOfThisCand = 0;double[] normFDsOverAOsOfThisCand = new
			// double[listOfAOsForThisCand.size()];
			// start of FED computation
			double normFeatDistForThisCand = -9999;
			// assuming same features across all AOs
			// Set<GowGeoFeature> listOfFeatures = null;
			// ArrayList<GowGeoFeature> listOfFeatures = null;
			EnumMap<GowGeoFeature, List<Double>> valsForEachFeatDiffAcrossAOsForThisCand = new EnumMap<>(
					GowGeoFeature.class);
			EnumMap<GowGeoFeature, List<Double>> normValsForEachFeatDiffAcrossAOsForThisCand = new EnumMap<>(
					GowGeoFeature.class);

			if (EDAlpha != 1)
			{
				listOfFeatures = new ArrayList<GowGeoFeature>(listOfAOsForThisCand.get(0).keySet());
				// int indexOfAOForThisCand = -1;
				// EnumMap<GowGeoFeature, DoubleSummaryStatistics> summaryOfNormValsForEachFeatureAcrossAOsForThisCand =
				// new EnumMap<>( GowGeoFeature.class);

				// loop over each feature
				for (GowGeoFeature feature : listOfFeatures)
				{
					// loop over AOs in this cand
					List<Double> valsForThisFeatureAcrossAllAOsInThisCand = listOfAOsForThisCand.stream()
							.map(e -> e.get(feature)).collect(Collectors.toList());
					// // loop over AOs in this cand
					// DoubleSummaryStatistics summaryStatForThisFeatAcrossAllAOInThisCand =
					// listOfAOsForThisCand.stream()
					// .map(e -> e.get(feature)).mapToDouble(Double::doubleValue).summaryStatistics();
					// summaryStatForEachFeatureAcrossAOsForThisCand.put(feature,
					// summaryStatForThisFeatAcrossAllAOInThisCand);
					valsForEachFeatDiffAcrossAOsForThisCand.put(feature, valsForThisFeatureAcrossAllAOsInThisCand);
				}

				// RTVerse normalise each val for each feature across AOs for this cand
				// loop over each feature
				for (Entry<GowGeoFeature, List<Double>> featureEntry : valsForEachFeatDiffAcrossAOsForThisCand
						.entrySet())
				{
					GowGeoFeature featureID = featureEntry.getKey();
					List<Double> diffValsForThisFeature = featureEntry.getValue();
					// loop over val for this feature across each AO
					List<Double> normalisedDiffValForThisFeatureForAllAOs = diffValsForThisFeature.stream()
							.map(diffValForThisFeatureForAO -> StatsUtils.minMaxNormWORoundWithUpperBound(
									diffValForThisFeatureForAO, maxOfMaxOfFeatDiffs.get(featureID),
									minOfMinOfFeatDiffs.get(featureID), 1.0d, false))
							.collect(Collectors.toList());
					normValsForEachFeatDiffAcrossAOsForThisCand.put(featureID,
							normalisedDiffValForThisFeatureForAllAOs);
				}
				// compute the Summary stat of normalised diff val of each feature across all AOs in the cand

				// Compute FED for this cand
				double wtdSumOfFeatDiffForThisCand = 0, sumOfFeatureWts = 0;
				for (Entry<GowGeoFeature, List<Double>> featureEntry : valsForEachFeatDiffAcrossAOsForThisCand
						.entrySet())
				{
					GowGeoFeature featureID = featureEntry.getKey();

					List<Double> diffValsForThisFeatAcrossAllAOsInCand = normValsForEachFeatDiffAcrossAOsForThisCand
							.get(featureID).stream().collect(Collectors.toList());

					double wtForThisFeature = featureWeightMap.get(featureID);
					// double diffValToUse = summaryStatForThisFeatAcrossAOsOfThisCand.getAverage();
					// Not doing Root of mean of the squared diffs as min and max in norm is of sqd vals
					// if (useMSD){diffValToUse = Math.sqrt(summaryStatForThisFeatAcrossAOsOfThisCand.getAverage());}

					// $$wtdSumOfFeatDiffForThisCand += (wtForThisFeature
					// $$ * summaryStatForThisFeatAcrossAOsOfThisCand.getAverage());//disabled on 8 Dec
					double aggValForFeatureAcrossAllAOsInCand = -9999;
					if (Constant.takeMeanOrMedianOfFeatDiffsAcrossAllAOsInCandForFeatSeq == 0)
					{
						DoubleSummaryStatistics summaryStatForThisFeatAcrossAOsOfThisCand = normValsForEachFeatDiffAcrossAOsForThisCand
								.get(featureID).stream().mapToDouble(Double::doubleValue).summaryStatistics();
						aggValForFeatureAcrossAllAOsInCand = summaryStatForThisFeatAcrossAOsOfThisCand.getAverage();
					}
					else
					{
						aggValForFeatureAcrossAllAOsInCand = StatsUtils
								.getPercentile(diffValsForThisFeatAcrossAllAOsInCand, 50);
					}
					wtdSumOfFeatDiffForThisCand += (wtForThisFeature * aggValForFeatureAcrossAllAOsInCand);

					sumOfFeatureWts += wtForThisFeature;

					//////
					logTxt.append("\n\t\t\tfeatureID=" + featureID + ",diffValToUse="
							+ aggValForFeatureAcrossAllAOsInCand + ",normalisedFeatureDiffVal="
							+ normValsForEachFeatDiffAcrossAOsForThisCand.get(featureID).stream()
									.map(v -> String.valueOf(v)).collect(Collectors.joining(",")).toString()
							+ ",wtForThisFeature=" + wtForThisFeature + ",maxOfMaxOfDiffs="
							+ maxOfMaxOfFeatDiffs.get(featureID) + ",minOfMinOfDiffs="
							+ minOfMinOfFeatDiffs.get(featureID));
				}
				normFeatDistForThisCand = wtdSumOfFeatDiffForThisCand / sumOfFeatureWts;
			} // end of FED computation

			logTxt.append(",normFeatDistForThisCand=" + normFeatDistForThisCand + "\n");

			///////////// Start of just for logging Nov25
			StringBuilder sbFeatureDiffAllAOsInCand = new StringBuilder();// added on Dec 3 2018
			// rounding forlogging
			String normFeatDistForThisCandRounded = StatsUtils.roundAsString(normFeatDistForThisCand, 4);
			if (VerbosityConstants.WriteRTVerseNormalisationLogs || VerbosityConstants.verboseDistDistribution)// logging
			{
				for (int indexOfAO = 0; indexOfAO < listOfAOsForThisCand.size(); indexOfAO++)
				{
					logAOsThisCand.append(candInfo + "," + indexOfAO + ",");
					if (indexOfAO == 0 && indexOfCandForThisRT == 1)
					{
						logAOsThisCandHeader.append("indexOfAO,");
					}
					if (shouldComputedFED)
					{
						// String nameOfFeaturesInSeq = "";
						for (GowGeoFeature feature : listOfFeatures)
						{
							// nameOfFeaturesInSeq += feature.toString() + ",";
							String s = StatsUtils.roundAsString(
									valsForEachFeatDiffAcrossAOsForThisCand.get(feature).get(indexOfAO), 4)
									+ ","
									+ StatsUtils.roundAsString(
											normValsForEachFeatDiffAcrossAOsForThisCand.get(feature).get(indexOfAO), 4)
									+ ",";
							logAOsThisCand.append(s);
							if (indexOfAO == 0 && indexOfCandForThisRT == 1)
							{
								logAOsThisCandHeader
										.append(feature.toString() + "," + feature.toString() + "Normalised,");
							}
							// sbFeatureDiffAllAOsInCand.append(s);//Disabled on Dec 7 2018. not writingfeat diff of
							// each AO in cand for each cand was row. This info is already there in log with each AO as
							// row and adding the info to sbFeatureDiffAllAOsInCand was making the log for each cand of
							// inconsistent number of cols. Also, this will bring down the size of log file a bit.
						}
					}
					logAOsThisCand.append("|" + "," + normFeatDistForThisCandRounded + "\n"); // written for for each AO
					if (indexOfAO == 0 && indexOfCandForThisRT == 1)
					{
						logAOsThisCandHeader.append("|,normFDForThisAOForThisCand");
					}
					// in cand for consistency though it will be same as it is a characteristic of cand and not AO
					// individually.
					// sbFeatureDiffAllAOsInCand.append(r);
					// logAOsThisCand.append("|" + "," + StatsUtils.roundAsString(normFDForThisAOForThisCand, 4) +
					// "\n");
					// logAOsThisCand.append("\n");
				}
				sbFeatureDiffAllAOsInCand.append("|" + "," + normFeatDistForThisCandRounded);
			}
			//////////// End of just for logging Nov25

			double normActDistForThisCand = StatsUtils.minMaxNormWORoundWithUpperBound(actDistForThisCand,
					maxActEDOverAllCands, minActEDOverAllCands, 1d, false);

			// double meanOverAOsNormFDForThisCand = sumOfNormFDsOverAOsOfThisCand / listOfAOsForThisCand.size();
			// double medianOverAOsNormFDForThisCand = StatUtils.percentile(normFDsOverAOsOfThisCand, 50);
			// double varianceOverAOsNormFDForThisCand = StatUtils.variance(normFDsOverAOsOfThisCand);
			// double stdDevOverAOsNormFDForThisCand = Math.sqrt(varianceOverAOsNormFDForThisCand);
			// double meanUponStdDev = meanOverAOsNormFDForThisCand / stdDevOverAOsNormFDForThisCand;

			// IMPORTANT
			double resultantEditDist = -999999;
			boolean thisCandRejected = false;
			String thisCandRejectedString = "NR";

			double threshNormAEDForCand = Constant.threshNormAEDForCand;
			double threshNormFEDForCand = Constant.threshNormFEDForCand;

			if (EDBeta == 0)
			{// since 0*NaN is NaN
				if (threshNormAEDForCand != -1 && normActDistForThisCand >= threshNormAEDForCand)// added on 14 Aug 2018
				{
					thisCandRejected = true;// reject this cand
					thisCandRejectedString = "AR";
				}
				else
				{
					resultantEditDist = (EDAlpha) * normActDistForThisCand;
				}
			}
			else
			{
				if (threshNormAEDForCand != -1 && normActDistForThisCand >= threshNormAEDForCand)// added on 14 Aug 2018
				{
					thisCandRejected = true;// reject this cand
					thisCandRejectedString = "AR";
				}
				else if (threshNormFEDForCand != -1 && normFeatDistForThisCand >= threshNormFEDForCand)
				{
					thisCandRejected = true;// reject this cand
					thisCandRejectedString = "FR";
				}
				else
				{
					resultantEditDist = (EDAlpha) * normActDistForThisCand + (EDBeta) * normFeatDistForThisCand;//
					// resultantEditDist = (EDAlpha) * normActDistForThisCand + (EDBeta) * 1;//baseline
				}
			}

			if (thisCandRejected == false)
			{
				res.put(candEntry.getKey(), new Pair<>(AEDTraceForThisCand, resultantEditDist));
			}
			else
			{
				if (VerbosityConstants.WriteRTVerseNormalisationLogs)
				{
					rejectedCandsDueToAEDFEDThreshold.put(candID,
							candInfo + "," + normActDistForThisCand + "," + normFeatDistForThisCand);
				}
				// else
				// {// less expensive for writing
				// rejectedCandsDueToAEDFEDThreshold.put(candID, candInfo);//}
			}

			/////////
			logTxt.append("\n\tnormActDistForThisCand=" + normActDistForThisCand + ", normFDForThisCand="
					+ normFeatDistForThisCand + "\n\t-->resultantEditDist=" + resultantEditDist);

			// logEachCand.append(candInfo + "," + normActDistForThisCand + "," + normFeatDistForThisCand + ","
			// + resultantEditDist + "," + thisCandRejectedString + "\n");
			// keeping this same order even with empty values because this file will be read later for finding
			// correlated between distance
			logEachCand.append(candInfo + "," + normActDistForThisCand + "," + " "/* sumOfNormFDsOverAOsOfThisCand */
					+ "," + normFeatDistForThisCand/* meanOverAOsNormFDForThisCand */ + "," + " "
					/* medianOverAOsNormFDForThisCand */ + "," + " "/* stdDevOverAOsNormFDForThisCand */ + ","
					+ " "/* meanUponStdDev */ + "," + resultantEditDist + "," + thisCandRejectedString + ","
					+ sbFeatureDiffAllAOsInCand.toString() + "\n");

			// logEachAO.append(",,,,,,,,,,,,,,,," + featureDistForThisCand + "," + normActDistForThisCand + ","
			// + resultantEditDist + "\n");

			logEachAOAllCands.append(logAOsThisCand.toString());
			/////////
		} // end of loop over cands

		String commonPath = Constant.getCommonPath();
		if (VerbosityConstants.WriteRTVerseNormalisationLogs || VerbosityConstants.verboseDistDistribution)// logging
		{
			// WToFile.appendLineToFileAbs(logTxt.toString() + "\n",
			// Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistances.txt");
			WToFile.appendLineToFileAbs(logEachCand.toString(),
					commonPath + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachCand.csv");
			if (VerbosityConstants.WriteRTVerseNormalisationLogs)
			{
				// header:
				// userAtRecomm,dateAtRecomm,timeAtRecomm,candAEDFeatDiffs.size(),currentTimeline,candID,indexOfCandForThisRT,candTimelineAsString,AEDTraceForThisCand,actDistForThisCand,normActDistForThisCand,sumOfNormFDsOverAOsOfThisCand,meanOverAOsNormFDForThisCand,medianOverAOsNormFDForThisCand,stdDevOverAOsNormFDForThisCand,meanUponStdDev,resultantEditDist
				WToFile.appendLineToFileAbs(logEachAOAllCands.toString(),
						commonPath + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachAO.csv");
				WToFile.appendLineToFileAbs(logAOsThisCandHeader.toString(),
						commonPath + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachAOHeader.csv");
			}
		}

		// write rejected cands for AED FED Threshold, added on 14 Aug 2018
		if (VerbosityConstants.WriteRTVerseNormalisationLogs || VerbosityConstants.verboseDistDistribution)
		{
			StringBuilder sb1 = new StringBuilder();
			rejectedCandsDueToAEDFEDThreshold.entrySet().stream()
					.forEachOrdered(e -> sb1.append(e.getKey() + "," + e.getValue() + "\n"));
			WToFile.appendLineToFileAbs(sb1.toString(), commonPath + "LogOfRejectedCands.csv");
		}

		if (res.size() == 0)// all candidates filtered out due to AED FED Threshold
		{
			WToFile.appendLineToFileAbs(rtInfo + "\n", commonPath + "recommPointsRejAllCandFiltrdAEDFEDThresh.csv");
		}

		if (VerbosityConstants.verbose)// true
		{
			logTxt.append("\n---------End  getRTVerseMinMaxNormalisedEditDistancesFS()\n");
			System.out.println(logTxt.toString());
			WToFile.appendLineToFileAbs(logTxt.toString(),
					commonPath + "DebugNov23getRTVerseMinMaxNormalisedEditDistancesFeatSeqApproach.txt");
		}

		System.out.println("INFO: orderOfFeatureDiffs = " + listOfFeatures);
		return res;

	}

	// end of added on 23 Nov 2018
	/**
	 * TODO modified the following method with shouldComputeFeatureLevelDiffs
	 * <P>
	 * check the modification and ensure logging version as same modification
	 * <P>
	 * Fork of org.activity.distances.DistanceUtils.getRTVerseMinMaxNormalisedEditDistances(), removing all logging for
	 * performance
	 * <p>
	 * Min-max normalise each feature difference to obtain feature level distance, where min/max for each featureDiff is
	 * the min/max feature diff over all (compared to current) activity objects of all (compared) candidates.
	 * <p>
	 * 
	 * @param candAEDFeatDiffs
	 * @param minOfMinOfDiffs
	 * @param maxOfMaxOfDiffs
	 * @param hjEditDistance
	 * @param timeAtRecomm
	 *            just for logging
	 * @param dateAtRecomm
	 *            just for logging
	 * @param userAtRecomm
	 *            just for logging
	 * @param activitiesGuidingRecomm
	 *            just for logging
	 * @param candidateTimelines
	 *            just for logging
	 * @since April 22 2018
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 * @deprecated on 12 Aug 2018 as there is a small chance that it may not have been updated to be equivalent to the
	 *             logging version.
	 */
	private static LinkedHashMap<String, Pair<String, Double>> getRTVerseMinMaxNormalisedEditDistancesNoLogging(
			LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs,
			EnumMap<GowGeoFeature, Double> minOfMinOfDiffs, EnumMap<GowGeoFeature, Double> maxOfMaxOfDiffs,
			HJEditDistance hjEditDistance, ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm, LinkedHashMap<String, Timeline> candidateTimelines)
	{
		LinkedHashMap<String, Pair<String, Double>> res = new LinkedHashMap<>(candAEDFeatDiffs.size());
		EnumMap<GowGeoFeature, Double> featureWeightMap = hjEditDistance.getFeatureWeightMap();
		boolean shouldComputeFeatureLevelDiffs = hjEditDistance.getShouldComputeFeatureLevelDistance();

		double EDAlpha = Constant.getDynamicEDAlpha();// .dynamicEDAlpha;
		double EDBeta = 1 - EDAlpha;

		/////
		if (hjEditDistance.getShouldComputeFeatureLevelDistance() == false && EDAlpha != 1)
		{
			System.err
					.println("Warning: -- Since no features are being used it is suggested to set Constant.EDAlpha=1.\n"
							+ "so that the computed values for dAct are not multiplied by EDAlpha and reduced.");
		}
		/////

		// double EDGamma;
		double sumOfWtOfFeaturesUsedExceptPD = hjEditDistance.getSumOfWeightOfFeaturesExceptPrimaryDimension();

		// Start of Get max of ActED over all cand
		double maxActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
				.max().getAsDouble();
		double minActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
				.min().getAsDouble();
		// End of Get max of ActED over all cand

		// Start of initialsing logging
		// StringBuilder logTxt = new StringBuilder(
		// "\n--------- START OF getRTVerseMinMaxNormalisedEditDistances() with EDAlpha=" + EDAlpha + " EDBeta="
		// + EDBeta + " sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD
		// + " maxActEDOverAllCands" + maxActEDOverAllCands + " minActEDOverAllCands="
		// + minActEDOverAllCands + '\n');
		// featureWeightMap.entrySet().stream()
		// .forEachOrdered(e -> logTxt.append(" " + e.getKey().toString() + "-" + e.getValue()));
		// StringBuilder logEachCand = new StringBuilder();// one for each cand
		// candID,AEDTraceForThisCand,ActDistForThisCand,countOfAOForThisCand,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featureDistForThisAOForThisCand,featureDistForThisCand,normActDistForThisCand,resultantEditDist
		// StringBuilder logEachAOAllCands = new StringBuilder();// one for each AO of each cand
		// String currentTimeline = activitiesGuidingRecomm.stream().map(ao -> ao.getPrimaryDimensionVal("|"))
		// .collect(Collectors.joining(">"));
		// String rtInfo = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candAEDFeatDiffs.size();
		// end of initialising logging

		int indexOfCandForThisRT = -1;

		// Loop over cands
		for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candEntry : candAEDFeatDiffs
				.entrySet())
		{
			indexOfCandForThisRT += 1;
			String candID = candEntry.getKey();

			String AEDTraceForThisCand = candEntry.getValue().getFirst();
			double actDistForThisCand = candEntry.getValue().getSecond();

			/////////
			// StringBuilder logAOsThisCand = new StringBuilder();
			// String candTimelineAsString = candidateTimelines.get(candID).getActivityObjectsInTimeline().stream()
			// .map(ao -> ao.getActivityName()).collect(Collectors.joining(">"));
			// logTxt.append("\n\tcandID=" + candID + " AEDTraceForThisCand=" + AEDTraceForThisCand
			// + " ActDistForThisCand=" + actDistForThisCand + " will now loop over list of AOs for this cand:");
			// String candInfo = rtInfo + "," + /* candID */indexOfCandForThisRT + "," + currentTimeline + ","
			// + candTimelineAsString + "," + AEDTraceForThisCand + "," + actDistForThisCand;
			////////////

			List<EnumMap<GowGeoFeature, Double>> listOfAOsForThisCand = candEntry.getValue().getThird();
			// note: list in in intial order, i.e., least recent AO to most recent AO by time.
			double sumOfNormFDsOverAOsOfThisCand = 0;
			double[] normFDsOverAOsOfThisCand = new double[listOfAOsForThisCand.size()];

			int indexOfAOForThisCand = -1;

			if (shouldComputeFeatureLevelDiffs)// this if added on 13 July
			{
				// loop over the list of AO for this cand
				for (EnumMap<GowGeoFeature, Double> mapOfFeatureDiffForAnAO : listOfAOsForThisCand)
				{
					indexOfAOForThisCand += 1;
					double featDistForThisAOForThisCand = 0;// double sanityCheckFeatureWtSum = 0;
					///////
					// logTxt.append(
					// "\n\t\tcountOfAOForThisCand=" + indexOfAOForThisCand + " now loop over features for this AO");
					// logEachAO
					// logAOsThisCand.append(candInfo + "," + indexOfAOForThisCand + ",");
					////////
					// loop over each of the Gowalla Feature
					for (Entry<GowGeoFeature, Double> diffEntry : mapOfFeatureDiffForAnAO.entrySet())
					{
						GowGeoFeature featureID = diffEntry.getKey();

						// $$Disabled on May 14: double normalisedFeatureDiffVal =
						// StatsUtils.minMaxNormWORound(diffEntry.getValue(),maxOfMaxOfDiffs.get(featureID),
						// minOfMinOfDiffs.get(featureID));

						double normalisedFeatureDiffVal = StatsUtils.minMaxNormWORoundWithUpperBound(
								diffEntry.getValue(), maxOfMaxOfDiffs.get(featureID), minOfMinOfDiffs.get(featureID),
								1.0d, false);

						double wtForThisFeature = featureWeightMap.get(featureID);
						featDistForThisAOForThisCand += (wtForThisFeature * normalisedFeatureDiffVal);

						//////
						// logTxt.append("\n\t\t\tfeatureID=" + featureID + " featDiff=" + diffEntry.getValue()
						// + " normalisedFeatureDiffVal=" + normalisedFeatureDiffVal + " wtForThisFeature="
						// + wtForThisFeature + " featureDistForThisAOForThisCand=" + featDistForThisAOForThisCand
						// + " maxOfMaxOfDiffs=" + maxOfMaxOfDiffs.get(featureID) + " minOfMinOfDiffs="
						// + minOfMinOfDiffs.get(featureID));
						// logAOsThisCand.append(StatsUtils.roundAsString(diffEntry.getValue(), 4) + ","
						// + StatsUtils.roundAsString(normalisedFeatureDiffVal, 4) + ",");// rounding for logging
						////// sanityCheckFeatureWtSum += wtForThisFeature;
					} // end of loop over Gowalla features

					// For SanityCheck sanityCheckFeatureWtSum
					// if (true){ Sanity.eq(sanityCheckFeatureWtSum,
					// sumOfWtOfFeaturesUsedExceptPD,"Error:sanityCheckFeatureWtSum=" +sanityCheckFeatureWtSum+ "
					// sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD);}

					double normFDForThisAOForThisCand = featDistForThisAOForThisCand / sumOfWtOfFeaturesUsedExceptPD;
					sumOfNormFDsOverAOsOfThisCand += normFDForThisAOForThisCand;
					normFDsOverAOsOfThisCand[indexOfAOForThisCand] = normFDForThisAOForThisCand;

					/////////
					// logTxt.append("\n\t\tfeatDistForThisAOForThisCand=" + featDistForThisAOForThisCand);
					// logAOsThisCand.append("|" + "," + StatsUtils.roundAsString(normFDForThisAOForThisCand, 4) +
					///////// "\n");
					////////
				} // end of loop over the list of AOs for this cand
			} // end of if shouldComputeFeatureLevelDiffs

			double normActDistForThisCand = StatsUtils.minMaxNormWORound(actDistForThisCand, maxActEDOverAllCands,
					minActEDOverAllCands);

			// IMPORTANT
			double resultantEditDist = -999999;
			if (EDBeta == 0)// shouldComputeFeatureLevelDiffs is true
			{// since 0*NaN is NaN
				resultantEditDist = (EDAlpha) * normActDistForThisCand;
			}
			else
			{
				double meanOverAOsNormFDForThisCand = sumOfNormFDsOverAOsOfThisCand / listOfAOsForThisCand.size();
				// double medianOverAOsNormFDForThisCand = StatUtils.percentile(normFDsOverAOsOfThisCand, 50);
				// double varianceOverAOsNormFDForThisCand = StatUtils.variance(normFDsOverAOsOfThisCand);
				// double stdDevOverAOsNormFDForThisCand = Math.sqrt(varianceOverAOsNormFDForThisCand);
				// double meanUponStdDev = meanOverAOsNormFDForThisCand / stdDevOverAOsNormFDForThisCand;

				resultantEditDist = (EDAlpha) * normActDistForThisCand + (EDBeta) * meanOverAOsNormFDForThisCand;

				// Sanity check start
				if (meanOverAOsNormFDForThisCand > 1)
				{
					WToFile.appendLineToFileAbs(meanOverAOsNormFDForThisCand + "\n",
							Constant.getCommonPath() + "ErrorDebug22April2018.csv");
					// System.out.println("\nDebug22April2018: normFeatureDistForThisCand=" + normFeatureDistForThisCand
					// + "\nlogAOThisCand=\n" + logAOsThisCand.toString() + "\nsumOfWtOfFeaturesUsedExceptPD="
					// + sumOfWtOfFeaturesUsedExceptPD);
				}
				// Sanity check end
			}

			res.put(candEntry.getKey(), new Pair<>(AEDTraceForThisCand, resultantEditDist));

			/////////
			// logTxt.append("\n\tsumOfFeatDistsOverAOsOfThisCand=" + sumOfNormFDsOverAOsOfThisCand
			// + " normActDistForThisCand=" + normActDistForThisCand + " normFDForThisCand="
			// + meanOverAOsNormFDForThisCand + "\n\t-->resultantEditDist=" + resultantEditDist);
			// logEachCand.append(candInfo + "," + normActDistForThisCand + "," + sumOfNormFDsOverAOsOfThisCand + ","
			// + meanOverAOsNormFDForThisCand + "," + medianOverAOsNormFDForThisCand + ","
			// + stdDevOverAOsNormFDForThisCand + "," + meanUponStdDev + "," + resultantEditDist + "\n");
			// logEachAO.append(",,,,,,,,,,,,,,,," + featureDistForThisCand + "," + normActDistForThisCand + ","
			// + resultantEditDist + "\n");

			// logEachAOAllCands.append(logAOsThisCand.toString());
			/////////
		} // end of loop over cands

		// if (true)// logging
		// {
		// WToFile.appendLineToFileAbs(logTxt.toString() + "\n",
		// Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistances.txt");
		// WToFile.appendLineToFileAbs(logEachCand.toString(),
		// Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachCand.csv");
		// WToFile.appendLineToFileAbs(logEachAOAllCands.toString(),
		// Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachAO.csv");
		// }

		// logTxt.append("\n---------End getRTVerseMinMaxNormalisedEditDistances()\n");
		return res;
	}

	/**
	 * INCOMPLETE
	 * <p>
	 * Fork of org.activity.distances.DistanceUtils.getRTVerseMinMaxNormalisedEditDistances()
	 * <p>
	 * Min-max normalise each feature difference to obtain feature level distance, where min/max for each featureDiff is
	 * the min/max feature diff over all (compared to current) activity objects of all (compared) candidates.
	 * <p>
	 * 
	 * @param candAEDFeatDiffs
	 * @param minOfMinOfDiffs
	 * @param maxOfMaxOfDiffs
	 * @param hjEditDistance
	 * @param timeAtRecomm
	 *            just for logging
	 * @param dateAtRecomm
	 *            just for logging
	 * @param userAtRecomm
	 *            just for logging
	 * @param activitiesGuidingRecomm
	 *            just for logging
	 * @param candidateTimelines
	 *            just for logging
	 * @since April 27 2018
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 * @deprecated
	 */
	// private static LinkedHashMap<String, Pair<String, Double>> getRTVerseMinMaxNormalisedEditDistancesV3April27(
	// LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs,
	// EnumMap<GowGeoFeature, Double> minOfMinOfDiffs, EnumMap<GowGeoFeature, Double> maxOfMaxOfDiffs,
	// HJEditDistance hjEditDistance, ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm,
	// String dateAtRecomm, String timeAtRecomm, LinkedHashMap<String, Timeline> candidateTimelines)
	// {
	// LinkedHashMap<String, Pair<String, Double>> res = new LinkedHashMap<>(candAEDFeatDiffs.size());
	//
	// EnumMap<GowGeoFeature, Double> featureWeightMap = hjEditDistance.getFeatureWeightMap();
	//
	// double EDAlpha = Constant.getDynamicEDAlpha();// .dynamicEDAlpha;
	// double EDBeta = 1 - EDAlpha;
	//
	// /////
	// if (hjEditDistance.getShouldComputeFeatureLevelDistance() == false && EDAlpha != 1)
	// {
	// System.err
	// .println("Warning: -- Since no features are being used it is suggested to set Constant.EDAlpha=1.\n"
	// + "so that the computed values for dAct are not multiplied by EDAlpha and reduced.");
	// }
	// /////
	//
	// double EDGamma;
	// double sumOfWtOfFeaturesUsedExceptPD = hjEditDistance.getSumOfWeightOfFeaturesExceptPrimaryDimension();
	//
	// // Start of Get max of ActED over all cand
	// double maxActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
	// .max().getAsDouble();
	// double minActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
	// .min().getAsDouble();
	// // End of Get max of ActED over all cand
	//
	// // Start of initialsing logging
	// StringBuilder logTxt = new StringBuilder(
	// "\n--------- START OF getRTVerseMinMaxNormalisedEditDistances() with EDAlpha=" + EDAlpha + " EDBeta="
	// + EDBeta + " sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD
	// + " maxActEDOverAllCands" + maxActEDOverAllCands + " minActEDOverAllCands="
	// + minActEDOverAllCands + '\n');
	// featureWeightMap.entrySet().stream()
	// .forEachOrdered(e -> logTxt.append(" " + e.getKey().toString() + "-" + e.getValue()));
	// StringBuilder logEachCand = new StringBuilder();// one for each cand
	// //
	// candID,AEDTraceForThisCand,ActDistForThisCand,countOfAOForThisCand,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featureDistForThisAOForThisCand,featureDistForThisCand,normActDistForThisCand,resultantEditDist
	// StringBuilder logEachAOAllCands = new StringBuilder();// one for each AO of each cand
	// String currentTimeline = activitiesGuidingRecomm.stream().map(ao -> ao.getPrimaryDimensionVal("|"))
	// .collect(Collectors.joining(">"));
	// String rtInfo = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candAEDFeatDiffs.size() + ","
	// + currentTimeline;
	// // end of initialising logging
	//
	// int indexOfCandForThisRT = -1;
	//
	// // Loop over cands
	// for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candEntry : candAEDFeatDiffs
	// .entrySet())
	// {
	// indexOfCandForThisRT += 1;
	// String candID = candEntry.getKey();
	//
	// String AEDTraceForThisCand = candEntry.getValue().getFirst();
	// double actDistForThisCand = candEntry.getValue().getSecond();
	//
	// /////////
	// StringBuilder logAOsThisCand = new StringBuilder();
	// String candTimelineAsString = candidateTimelines.get(candID).getActivityObjectsInTimeline().stream()
	// .map(ao -> ao.getActivityName()).collect(Collectors.joining(">"));
	// logTxt.append("\n\tcandID=" + candID + " AEDTraceForThisCand=" + AEDTraceForThisCand
	// + " ActDistForThisCand=" + actDistForThisCand + " will now loop over list of AOs for this cand:");
	// String candInfo = rtInfo + "," + candID + "," + indexOfCandForThisRT + "," + candTimelineAsString + ","
	// + AEDTraceForThisCand + "," + actDistForThisCand;
	// ////////////
	//
	// List<EnumMap<GowGeoFeature, Double>> listOfAOsForThisCand = candEntry.getValue().getThird();
	// // note: list in in intial order, i.e., least recent AO to most recent AO by time.
	// double sumOfNormFDsOverAOsOfThisCand = 0;
	// double[] normFDsOverAOsOfThisCand = new double[listOfAOsForThisCand.size()];
	//
	// int indexOfAOForThisCand = -1;
	//
	// // loop over the list of AO for this cand
	// for (EnumMap<GowGeoFeature, Double> mapOfFeatureDiffForAnAO : listOfAOsForThisCand)
	// {
	// indexOfAOForThisCand += 1;
	// double featDistForThisAOForThisCand = 0;// double sanityCheckFeatureWtSum = 0;
	//
	// ///////
	// logTxt.append(
	// "\n\t\tcountOfAOForThisCand=" + indexOfAOForThisCand + " now loop over features for this AO");
	// // logEachAO
	// logAOsThisCand.append(candInfo + "," + indexOfAOForThisCand + ",");
	// ////////
	//
	// // loop over each of the Gowalla Feature
	// for (Entry<GowGeoFeature, Double> diffEntry : mapOfFeatureDiffForAnAO.entrySet())
	// {
	// GowGeoFeature featureID = diffEntry.getKey();
	// double normalisedFeatureDiffVal = StatsUtils.minMaxNormWORound(diffEntry.getValue(),
	// maxOfMaxOfDiffs.get(featureID), minOfMinOfDiffs.get(featureID));
	//
	// double wtForThisFeature = featureWeightMap.get(featureID);
	// featDistForThisAOForThisCand += (wtForThisFeature * normalisedFeatureDiffVal);
	//
	// //////
	// logTxt.append("\n\t\t\tfeatureID=" + featureID + " featDiff=" + diffEntry.getValue()
	// + " normalisedFeatureDiffVal=" + normalisedFeatureDiffVal + " wtForThisFeature="
	// + wtForThisFeature + " featureDistForThisAOForThisCand=" + featDistForThisAOForThisCand
	// + " maxOfMaxOfDiffs=" + maxOfMaxOfDiffs.get(featureID) + " minOfMinOfDiffs="
	// + minOfMinOfDiffs.get(featureID));
	// logAOsThisCand.append(StatsUtils.roundAsString(diffEntry.getValue(), 4) + ","
	// + StatsUtils.roundAsString(normalisedFeatureDiffVal, 4) + ",");// rounding for logging
	// ////// sanityCheckFeatureWtSum += wtForThisFeature;
	// } // end of loop over Gowalla features
	//
	// // For SanityCheck sanityCheckFeatureWtSum
	// // if (true){ Sanity.eq(sanityCheckFeatureWtSum,
	// // sumOfWtOfFeaturesUsedExceptPD,"Error:sanityCheckFeatureWtSum=" +sanityCheckFeatureWtSum+ "
	// // sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD);}
	//
	// double normFDForThisAOForThisCand = featDistForThisAOForThisCand / sumOfWtOfFeaturesUsedExceptPD;
	// sumOfNormFDsOverAOsOfThisCand += normFDForThisAOForThisCand;
	// normFDsOverAOsOfThisCand[indexOfAOForThisCand] = normFDForThisAOForThisCand;
	//
	// /////////
	// logTxt.append("\n\t\tfeatDistForThisAOForThisCand=" + featDistForThisAOForThisCand);
	// logAOsThisCand.append("|" + "," + StatsUtils.roundAsString(normFDForThisAOForThisCand, 4) + "\n");
	// ////////
	// } // end of loop over the list of AOs for this cand
	//
	// double normActDistForThisCand = StatsUtils.minMaxNormWORound(actDistForThisCand, maxActEDOverAllCands,
	// minActEDOverAllCands);
	// double meanOverAOsNormFDForThisCand = sumOfNormFDsOverAOsOfThisCand / listOfAOsForThisCand.size();
	// double medianOverAOsNormFDForThisCand = StatUtils.percentile(normFDsOverAOsOfThisCand, 50);
	// double varianceOverAOsNormFDForThisCand = StatUtils.variance(normFDsOverAOsOfThisCand);
	// double stdDevOverAOsNormFDForThisCand = Math.sqrt(varianceOverAOsNormFDForThisCand);
	// double meanUponStdDev = meanOverAOsNormFDForThisCand / stdDevOverAOsNormFDForThisCand;
	//
	// // IMPORTANT
	// double resultantEditDist = -999999;
	// if (EDBeta == 0)
	// {// since 0*NaN is NaN
	// resultantEditDist = (EDAlpha) * normActDistForThisCand;
	// }
	// else
	// {
	// resultantEditDist = (EDAlpha) * normActDistForThisCand + (EDBeta) * meanOverAOsNormFDForThisCand;
	// }
	//
	// res.put(candEntry.getKey(), new Pair<>(AEDTraceForThisCand, resultantEditDist));
	//
	// /////////
	// logTxt.append("\n\tsumOfFeatDistsOverAOsOfThisCand=" + sumOfNormFDsOverAOsOfThisCand
	// + " normActDistForThisCand=" + normActDistForThisCand + " normFDForThisCand="
	// + meanOverAOsNormFDForThisCand + "\n\t-->resultantEditDist=" + resultantEditDist);
	// logEachCand.append(candInfo + "," + normActDistForThisCand + "," + sumOfNormFDsOverAOsOfThisCand + ","
	// + meanOverAOsNormFDForThisCand + "," + medianOverAOsNormFDForThisCand + ","
	// + stdDevOverAOsNormFDForThisCand + "," + meanUponStdDev + "," + resultantEditDist + "\n");
	// // logEachAO.append(",,,,,,,,,,,,,,,," + featureDistForThisCand + "," + normActDistForThisCand + ","
	// // + resultantEditDist + "\n");
	//
	// // Sanity check start
	// if (meanOverAOsNormFDForThisCand > 1)
	// {
	// WToFile.appendLineToFileAbs(meanOverAOsNormFDForThisCand + "\n" + logAOsThisCand,
	// Constant.getCommonPath() + "Debug22April2018.csv");
	// // System.out.println("\nDebug22April2018: normFeatureDistForThisCand=" + normFeatureDistForThisCand
	// // + "\nlogAOThisCand=\n" + logAOsThisCand.toString() + "\nsumOfWtOfFeaturesUsedExceptPD="
	// // + sumOfWtOfFeaturesUsedExceptPD);
	// }
	// // Sanity check end
	// logEachAOAllCands.append(logAOsThisCand.toString());
	// /////////
	// } // end of loop over cands
	//
	// if (true)// logging
	// {
	// // WToFile.appendLineToFileAbs(logTxt.toString() + "\n",
	// // Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistances.txt");
	// WToFile.appendLineToFileAbs(logEachCand.toString(),
	// Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachCand.csv");
	// WToFile.appendLineToFileAbs(logEachAOAllCands.toString(),
	// Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachAO.csv");
	// }
	//
	// logTxt.append("\n---------End getRTVerseMinMaxNormalisedEditDistances()\n");
	// return res;
	//
	// }

	/**
	 * Likely to be incorrect due to issue in aggegation of feature level distance over AOs for each cand.
	 * <p>
	 * Min-max normalise each feature difference to obtain feature level distance, where min/max for each featureDiff is
	 * the min/max feature diff over all (compared to current) activity objects of all (compared) candidates.
	 * <p>
	 * TODO: need to be SANITY CHECKED
	 * </p>
	 * 
	 * @param candAEDFeatDiffs
	 * @param minOfMinOfDiffs
	 * @param maxOfMaxOfDiffs
	 * @param hjEditDistance
	 * @param timeAtRecomm
	 *            just for logging
	 * @param dateAtRecomm
	 *            just for logging
	 * @param userAtRecomm
	 *            just for logging
	 * @param activitiesGuidingRecomm
	 *            just for logging
	 * @param candidateTimelines
	 *            just for logging
	 * @since April 17 2018
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 * @deprecated
	 */
	// private static LinkedHashMap<String, Pair<String, Double>> getRTVerseMinMaxNormalisedEditDistancesV1Incorrect(
	// LinkedHashMap<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candAEDFeatDiffs,
	// EnumMap<GowGeoFeature, Double> minOfMinOfDiffs, EnumMap<GowGeoFeature, Double> maxOfMaxOfDiffs,
	// HJEditDistance hjEditDistance, ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm,
	// String dateAtRecomm, String timeAtRecomm, LinkedHashMap<String, Timeline> candidateTimelines)
	// {
	// LinkedHashMap<String, Pair<String, Double>> res = new LinkedHashMap<>(candAEDFeatDiffs.size());
	//
	// EnumMap<GowGeoFeature, Double> featureWeightMap = hjEditDistance.getFeatureWeightMap();
	//
	// double EDAlpha = Constant.getDynamicEDAlpha();// .dynamicEDAlpha;
	// double EDBeta = 1 - EDAlpha;
	// double EDGamma;
	// double sumOfWtOfFeaturesUsedExceptPD = hjEditDistance.getSumOfWeightOfFeaturesExceptPrimaryDimension();
	//
	// // Start of Get max of ActED over all cand
	// double maxActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
	// .max().getAsDouble();
	// double minActEDOverAllCands = candAEDFeatDiffs.entrySet().stream().mapToDouble(e -> e.getValue().getSecond())
	// .min().getAsDouble();
	// // End of Get max of ActED over all cand
	//
	// // Start of initialsing logging
	// StringBuilder logTxt = new StringBuilder(
	// "\n--------- START OF getRTVerseMinMaxNormalisedEditDistances() with EDAlpha=" + EDAlpha + " EDBeta="
	// + EDBeta + " sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD
	// + " maxActEDOverAllCands" + maxActEDOverAllCands + " minActEDOverAllCands="
	// + minActEDOverAllCands + '\n');
	// featureWeightMap.entrySet().stream()
	// .forEachOrdered(e -> logTxt.append(" " + e.getKey().toString() + "-" + e.getValue()));
	// StringBuilder logEachCand = new StringBuilder();// one for each cand
	// //
	// candID,AEDTraceForThisCand,ActDistForThisCand,countOfAOForThisCand,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featDiff,featureDistForThisAOForThisCand,featureDistForThisCand,normActDistForThisCand,resultantEditDist
	// StringBuilder logEachAOAllCands = new StringBuilder();// one for each AO of each cand
	// String currentTimeline = activitiesGuidingRecomm.stream().map(ao -> ao.getPrimaryDimensionVal("|"))
	// .collect(Collectors.joining(">"));
	// String rtInfo = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + candAEDFeatDiffs.size();
	// // end of initialising logging
	//
	// int indexOfCandForThisRT = -1;
	//
	// // Loop over cands
	// for (Entry<String, Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>>> candEntry : candAEDFeatDiffs
	// .entrySet())
	// {
	// indexOfCandForThisRT += 1;
	// String candID = candEntry.getKey();
	// String candTimelineAsString = candidateTimelines.get(candID).getActivityObjectsInTimeline().stream()
	// .map(ao -> ao.getActivityName()).collect(Collectors.joining(">"));
	// double featureDistForThisCand = 0;
	//
	// String AEDTraceForThisCand = candEntry.getValue().getFirst();
	// double ActDistanceForThisCand = candEntry.getValue().getSecond();
	//
	// /////////
	// StringBuilder logAOsThisCand = new StringBuilder();
	// logTxt.append("\n\tcandID=" + candID + " featureDistForThisCand=" + featureDistForThisCand
	// + " AEDTraceForThisCand=" + AEDTraceForThisCand + " ActDistForThisCand=" + ActDistanceForThisCand
	// + " will now loop over list of AOs for this cand:");
	// String candInfo = rtInfo + "," + /* candID */indexOfCandForThisRT + "," + currentTimeline + ","
	// + candTimelineAsString + "," + AEDTraceForThisCand + "," + ActDistanceForThisCand;
	//
	// ////////////
	//
	// List<EnumMap<GowGeoFeature, Double>> listOfAOsForThisCand = candEntry.getValue().getThird();
	// // note: list in in intial order, i.e., least recent AO to most recent AO by time.
	//
	// int indexOfAOForThisCand = -1;
	//
	// // loop over the list of AO for this cand
	// for (EnumMap<GowGeoFeature, Double> mapOfFeatureDiffForAnAO : listOfAOsForThisCand)
	// {
	// indexOfAOForThisCand += 1;
	// double featureDistForThisAOForThisCand = 0;
	// double sanityCheckFeatureWtSum = 0;
	//
	// ///////
	// logTxt.append(
	// "\n\t\tcountOfAOForThisCand=" + indexOfAOForThisCand + " now loop over features for this AO");
	// // logEachAO
	// logAOsThisCand.append(candInfo + "," + indexOfAOForThisCand + ",");
	// ////////
	//
	// // loop over each of the Gowalla Feature
	// for (Entry<GowGeoFeature, Double> diffEntry : mapOfFeatureDiffForAnAO.entrySet())
	// {
	// GowGeoFeature featureID = diffEntry.getKey();
	// double normalisedFeatureDiffVal = StatsUtils.minMaxNormWORound(diffEntry.getValue(),
	// maxOfMaxOfDiffs.get(featureID), minOfMinOfDiffs.get(featureID));
	//
	// double wtForThisFeature = featureWeightMap.get(featureID);
	// featureDistForThisAOForThisCand += (wtForThisFeature * normalisedFeatureDiffVal);
	//
	// //////
	// logTxt.append("\n\t\t\tfeatureID=" + featureID + " featDiff=" + diffEntry.getValue()
	// + " normalisedFeatureDiffVal=" + normalisedFeatureDiffVal + " wtForThisFeature="
	// + wtForThisFeature + " featureDistForThisAOForThisCand=" + featureDistForThisAOForThisCand
	// + " maxOfMaxOfDiffs=" + maxOfMaxOfDiffs.get(featureID) + " minOfMinOfDiffs="
	// + minOfMinOfDiffs.get(featureID));
	// // logEachAO.
	// logAOsThisCand.append(StatsUtils.roundAsString(diffEntry.getValue(), 4) + ","
	// + StatsUtils.roundAsString(normalisedFeatureDiffVal, 4) + ",");// rounding for logging
	// //////
	// sanityCheckFeatureWtSum += wtForThisFeature;
	// }
	// /////////
	// logTxt.append("\n\t\tfeatureDistForThisAOForThisCand=" + featureDistForThisAOForThisCand);
	// // logEachAO.
	// logAOsThisCand.append("|" + "," + StatsUtils.roundAsString(featureDistForThisAOForThisCand, 4) + "\n");
	// ////////
	// // For SanityCheck sanityCheckFeatureWtSum
	// if (true)
	// {
	// Sanity.eq(sanityCheckFeatureWtSum, sumOfWtOfFeaturesUsedExceptPD,
	// "Error:sanityCheckFeatureWtSum=" + sanityCheckFeatureWtSum
	// + " sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD);
	// // System.out.println("sanityCheckFeatureWtSum=" + sanityCheckFeatureWtSum
	// // + " sumOfWtOfFeaturesUsedExceptPD=" + sumOfWtOfFeaturesUsedExceptPD);
	// }
	// featureDistForThisCand += featureDistForThisAOForThisCand;
	//
	// } // end of loop over the list of AOs for this cand
	//
	// double normActDistForThisCand = StatsUtils.minMaxNormWORound(ActDistanceForThisCand, maxActEDOverAllCands,
	// minActEDOverAllCands);
	// double normFeatureDistForThisCand = featureDistForThisCand / sumOfWtOfFeaturesUsedExceptPD;
	//
	// // IMPORTANT
	// double resultantEditDist = (EDAlpha) * normActDistForThisCand + (EDBeta) * normFeatureDistForThisCand;
	//
	// res.put(candEntry.getKey(), new Pair<>(AEDTraceForThisCand, resultantEditDist));
	//
	// /////////
	// logTxt.append("\n\tfeatureDistForThisCand=" + featureDistForThisCand + " normActDistForThisCand="
	// + normActDistForThisCand + " normFeatureDistForThisCand=" + normFeatureDistForThisCand
	// + "\n\t-->resultantEditDist=" + resultantEditDist);
	// logEachCand.append(candInfo + "," + normActDistForThisCand + "," + featureDistForThisCand + ","
	// + normFeatureDistForThisCand + "," + resultantEditDist + "\n");
	// // logEachAO.append(",,,,,,,,,,,,,,,," + featureDistForThisCand + "," + normActDistForThisCand + ","
	// // + resultantEditDist + "\n");
	//
	// // Sanity check start
	// if (normFeatureDistForThisCand > 1)
	// {
	// WToFile.appendLineToFileAbs(normFeatureDistForThisCand + "\n" + logAOsThisCand,
	// Constant.getCommonPath() + "Debug22April2018.csv");
	// // System.out.println("\nDebug22April2018: normFeatureDistForThisCand=" + normFeatureDistForThisCand
	// // + "\nlogAOThisCand=\n" + logAOsThisCand.toString() + "\nsumOfWtOfFeaturesUsedExceptPD="
	// // + sumOfWtOfFeaturesUsedExceptPD);
	//
	// }
	// // Sanity check end
	// logEachAOAllCands.append(logAOsThisCand.toString());
	// /////////
	// } // end of loop over cands
	//
	// if (true)// logging
	// {
	// // WToFile.appendLineToFileAbs(logTxt.toString() + "\n",
	// // Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistances.txt");
	// WToFile.appendLineToFileAbs(logEachCand.toString(),
	// Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachCand.csv");
	// WToFile.appendLineToFileAbs(logEachAOAllCands.toString(),
	// Constant.getCommonPath() + "LogOfgetRTVerseMinMaxNormalisedEditDistancesEachAO.csv");
	// }
	//
	// logTxt.append("\n---------End getRTVerseMinMaxNormalisedEditDistances()\n");
	// return res;
	// }

	/**
	 * Created to facilitate parallel computation of edit distances
	 * 
	 * @param candTimeline
	 * @param activitiesGuidingRecomm
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param candTimelineID
	 * @param hjEditDistance
	 * @param caseType
	 * @return
	 * @since 9 Aug 2017
	 */
	public static Pair<String, Double> getEditDistances(Timeline candTimeline,
			ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, String candTimelineID, CaseType caseType, HJEditDistance hjEditDistance,
			EditDistanceMemorizer editDistancesMemorizer)
	{
		Pair<String, Double> editDistanceForThisCandidate = null;

		switch (caseType)
		{
		case CaseBasedV1:
			if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
			// invalids are already expunged, no need to expunge again
			{
				editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceWithoutEndCurrentActivity(
						candTimeline.getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
						dateAtRecomm, timeAtRecomm, candTimeline.getTimelineID());
			}
			else
			{
				editDistanceForThisCandidate = hjEditDistance
						.getHJEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
								candTimeline.getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
								dateAtRecomm, timeAtRecomm, candTimeline.getTimelineID());
			}
			break;

		case SimpleV3:// "SimpleV3":
			if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
			{
				long t1 = System.nanoTime();
				editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceWithTrace(
						candTimeline.getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
						dateAtRecomm, timeAtRecomm, candTimeline.getTimelineID());
				long t2 = System.nanoTime();

				if (false)
				{

					long t3 = System.nanoTime();
					Pair<String, Double> editDistanceForThisCandidateDUmmy = hjEditDistance
							.getHJEditDistanceWithTraceBefore1Mar2018(candTimeline.getActivityObjectsInTimeline(),
									activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm,
									candTimeline.getTimelineID());
					long t4 = System.nanoTime();
					// StringBuilder sb = new StringBuilder((t2 - t1) + "," + (t4 - t3) + "\n");
					// Sanity checked OK
					// sb.append("editDistanceForThisCandidateDUmmy.equals(editDistanceForThisCandidate) = "
					// + editDistanceForThisCandidateDUmmy.equals(editDistanceForThisCandidate) + "\n");
					// sb.append("editDistanceForThisCandidateDUmmy=" + editDistanceForThisCandidateDUmmy.toString()
					// + "\neditDistanceForThisCandidate=" + editDistanceForThisCandidate.toString() + "\n");
					WToFile.appendLineToFileAbs((t2 - t1) + "," + (t4 - t3) + "\n",
							Constant.getCommonPath() + "Mar2_DebugED.txt");
				}

			}
			else
			{
				editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceInvalidsExpunged(
						candTimeline.getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
						dateAtRecomm, timeAtRecomm, candTimeline.getTimelineID());
			}
			break;

		default:
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in getEditDistancesForCandidateTimelineFullCand: unidentified case type" + caseType));
			break;
		}

		// editDistancesMemorizer.addToMemory(candTimelineID, Timeline.getTimelineIDFromAOs(activitiesGuidingRecomm),
		// editDistanceForThisCandidate);

		// Constant.addToEditDistanceMemorizer(candTimelineID, Timeline.getTimelineIDFromAOs(activitiesGuidingRecomm),
		// editDistanceForThisCandidate);

		return editDistanceForThisCandidate;
	}

	/**
	 * Fork of DistanceUtils.getActEditDistancesFeatDiffs() to have feat val pairs instead of feat diffs
	 * <p>
	 * Select the correct Edit Dist, Feature difference method for the given case type
	 * 
	 * @param candTimeline
	 * @param activitiesGuidingRecomm
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param candTimelineID
	 * @param caseType
	 * @param hjEditDistance
	 * @param editDistancesMemorizer
	 * @return Triple{TraceAsString,ActLevelEditDistance,List of EnumMap of {GowallaFeatures, FeatValPairs of
	 *         correspondins AOs for that feat} one for each correspnding AO comparison}}
	 * @since Jan 7 2019
	 */
	public static Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>> getActEditDistancesFeatValPairs(
			Timeline candTimeline, ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm, String candTimelineID, CaseType caseType,
			HJEditDistance hjEditDistance, EditDistanceMemorizer editDistancesMemorizer,
			ActDistType actLevelDistanceType)
	{
		// PopUps.showMessage("Inside getActEditDistancesFeatDiffs");//
		// Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>> actEDFeatDiffsForThisCandidate = null;
		Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>> actEDFeatDiffsForThisCandidateV2 = null;

		switch (caseType)
		{
		case SimpleV3:// "SimpleV3":
			long t1 = System.nanoTime();
			// editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceWithTrace(
			// candTimeline.getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
			// dateAtRecomm, timeAtRecomm, candTimeline.getTimelineID());
			if (true)// temp
			{
				if (hjEditDistance == null)
				{
					System.out.println("hjEditDistance==null");
				}
				if (candTimeline == null)
				{
					System.out.println("candTimeline==null");
				}
				if (candTimeline.getActivityObjectsInTimeline() == null)
				{
					System.out.println("candTimeline.getActivityObjectsInTimeline()==null");
				}
				if (activitiesGuidingRecomm == null)
				{
					System.out.println("activitiesGuidingRecomm==null");
				}
				if (userAtRecomm == null)
				{
					System.out.println("userAtRecomm==null");
				}
				if (dateAtRecomm == null)
				{
					System.out.println("dateAtRecomm==null");
				}
				if (timeAtRecomm == null)
				{
					System.out.println("timeAtRecomm==null");
				}
				if (candTimeline.getTimelineID() == null)
				{
					System.out.println(" candTimeline.getTimelineID()m==null");
				}
			}

			actEDFeatDiffsForThisCandidateV2 = hjEditDistance.getActEditDistWithTrace_FeatValPairs_5Jan2019(
					candTimeline.getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm, dateAtRecomm,
					timeAtRecomm, candTimeline.getTimelineID(), actLevelDistanceType);

			// Start of added on 7 Jan 2019
			if (false)// SANITY CHECKING CORRECTNESS OF getActEditDistWithTrace_FeatValPairs_5Jan2019 by matching
						// actLevel distances with MySimpleLevenshtein
			{
				Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>> actEDFeatDiffsForThisCandidate = hjEditDistance
						.getActEditDistWithTrace_FeatDiffs_13April2018(candTimeline.getActivityObjectsInTimeline(),
								activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm,
								candTimeline.getTimelineID());
				Sanity.eq(actEDFeatDiffsForThisCandidate.getFirst(), actEDFeatDiffsForThisCandidateV2.getFirst(),
						"Error checkJan7_2019");
				Sanity.eq(actEDFeatDiffsForThisCandidate.getSecond(), actEDFeatDiffsForThisCandidateV2.getSecond(),
						"Error checkJan7_2019");
				Sanity.checkJan7_2019(actEDFeatDiffsForThisCandidate, actEDFeatDiffsForThisCandidateV2, true,
						Constant.getCommonPath() + "SanityCheck9Jan2019.txt");
				// SANITY CHECK OKAY on 8 Jan 2019
			}
			// End of added on 7 Jan 2019

			long t2 = System.nanoTime();
			break;

		default:
			System.err.println(PopUps.getTracedErrorMsg("Error in getEditDistances_FeatDiffs: unidentified case type"
					+ caseType + "\nNOTE: this method has not been implemented yet for all case types"));
			break;
		}

		// editDistancesMemorizer.addToMemory(candTimelineID, Timeline.getTimelineIDFromAOs(activitiesGuidingRecomm),
		// editDistanceForThisCandidate);

		// Constant.addToEditDistanceMemorizer(candTimelineID, Timeline.getTimelineIDFromAOs(activitiesGuidingRecomm),
		// editDistanceForThisCandidate);

		return actEDFeatDiffsForThisCandidateV2;
	}

	//
	/**
	 * Select the correct Edit Dist, Feature difference method for the given case type
	 * 
	 * @param candTimeline
	 * @param activitiesGuidingRecomm
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param candTimelineID
	 * @param caseType
	 * @param hjEditDistance
	 * @param editDistancesMemorizer
	 * @return Triple{TraceAsString,ActLevelEditDistance,List of EnumMap of {GowallaFeatures, DiffForThatFeature} one
	 *         for each correspnding AO comparison}}
	 * @since April 14 2018
	 */
	public static Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>> getActEditDistancesFeatDiffs(
			Timeline candTimeline, ArrayList<ActivityObject2018> activitiesGuidingRecomm, String userAtRecomm,
			String dateAtRecomm, String timeAtRecomm, String candTimelineID, CaseType caseType,
			HJEditDistance hjEditDistance, EditDistanceMemorizer editDistancesMemorizer)
	{
		// PopUps.showMessage("Inside getActEditDistancesFeatDiffs");// TODO
		Triple<String, Double, List<EnumMap<GowGeoFeature, Double>>> actEDFeatDiffsForThisCandidate = null;

		switch (caseType)
		{
		case SimpleV3:// "SimpleV3":
			long t1 = System.nanoTime();
			// editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceWithTrace(
			// candTimeline.getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
			// dateAtRecomm, timeAtRecomm, candTimeline.getTimelineID());
			if (true)// temp
			{
				if (hjEditDistance == null)
				{
					System.out.println("hjEditDistance==null");
				}
				if (candTimeline == null)
				{
					System.out.println("candTimeline==null");
				}
				if (candTimeline.getActivityObjectsInTimeline() == null)
				{
					System.out.println("candTimeline.getActivityObjectsInTimeline()==null");
				}
				if (activitiesGuidingRecomm == null)
				{
					System.out.println("activitiesGuidingRecomm==null");
				}
				if (userAtRecomm == null)
				{
					System.out.println("userAtRecomm==null");
				}
				if (dateAtRecomm == null)
				{
					System.out.println("dateAtRecomm==null");
				}
				if (timeAtRecomm == null)
				{
					System.out.println("timeAtRecomm==null");
				}
				if (candTimeline.getTimelineID() == null)
				{
					System.out.println(" candTimeline.getTimelineID()m==null");
				}
			}

			actEDFeatDiffsForThisCandidate = hjEditDistance.getActEditDistWithTrace_FeatDiffs_13April2018(
					candTimeline.getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm, dateAtRecomm,
					timeAtRecomm, candTimeline.getTimelineID());
			// getActEditDistWithTrace_FeatDiffs_13April2018

			// Start of added on 7 Jan 2019
			if (false)// SANITY CHECKING CORRECTNESS OF getActEditDistWithTrace_FeatValPairs_5Jan2019 by matching
						// actLevel distances with MySimpleLevenshtein
			{
				Triple<String, Double, List<EnumMap<GowGeoFeature, Pair<String, String>>>> actEDFeatDiffsForThisCandidateV2 = hjEditDistance
						.getActEditDistWithTrace_FeatValPairs_5Jan2019(candTimeline.getActivityObjectsInTimeline(),
								activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm,
								candTimeline.getTimelineID(), Constant.actLevelDistType);

				Sanity.eq(actEDFeatDiffsForThisCandidate.getFirst(), actEDFeatDiffsForThisCandidateV2.getFirst(),
						"Error checkJan7_2019");
				Sanity.eq(actEDFeatDiffsForThisCandidate.getSecond(), actEDFeatDiffsForThisCandidateV2.getSecond(),
						"Error checkJan7_2019");

				Sanity.checkJan7_2019(actEDFeatDiffsForThisCandidate, actEDFeatDiffsForThisCandidateV2, true,
						Constant.getCommonPath() + "SanityCheck9Jan2019.txt");
				// SANITY CHECK OKAY on 8 Jan 2019
			}
			// End of added on 7 Jan 2019

			long t2 = System.nanoTime();
			break;

		default:
			System.err.println(PopUps.getTracedErrorMsg("Error in getEditDistances_FeatDiffs: unidentified case type"
					+ caseType + "\nNOTE: this method has not been implemented yet for all case types"));
			break;
		}

		// editDistancesMemorizer.addToMemory(candTimelineID, Timeline.getTimelineIDFromAOs(activitiesGuidingRecomm),
		// editDistanceForThisCandidate);

		// Constant.addToEditDistanceMemorizer(candTimelineID, Timeline.getTimelineIDFromAOs(activitiesGuidingRecomm),
		// editDistanceForThisCandidate);

		return actEDFeatDiffsForThisCandidate;
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
	public static LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> getFeatureWiseEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			FeatureWiseEditDistance featureWiseEditDistance)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> candEditDistancesFeatureWise = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
		{
			LinkedHashMap<String, Pair<String, Double>> featureWiseEditDistancesForThisCandidate = null;
			String candidateTimelineId = entry.getKey();

			switch (caseType)
			{
			case CaseBasedV1:// "CaseBasedV1":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS) // invalids are already expunged, no need to
																	// expunge
																	// again
				{
					featureWiseEditDistancesForThisCandidate = featureWiseEditDistance
							.getFeatureWiseEditDistanceWithoutEndCurrentActivity(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);
				}
				else
				{
					featureWiseEditDistancesForThisCandidate = featureWiseEditDistance
							.getFeatureWiseEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);
				}
				break;

			case SimpleV3:// "SimpleV3":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithTrace(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
				{
					featureWiseEditDistancesForThisCandidate = featureWiseEditDistance
							.getFeatureWiseEditDistanceWithTrace(entry.getValue().getActivityObjectsInTimeline(),
									activitiesGuidingRecomm);// ,
																// userAtRecomm,
																// dateAtRecomm,
																// timeAtRecomm,
																// candidateTimelineId);
				}
				else
				{
					featureWiseEditDistancesForThisCandidate = featureWiseEditDistance
							.getFeatureWiseEditDistanceInvalidsExpunged(entry.getValue().getActivityObjectsInTimeline(),
									activitiesGuidingRecomm);// ,
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
	 * @param OTMDSAMEditDistance
	 * @return <CanditateTimelineID, Pair<Trace,Edit distance of this candidate>>
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getOTMDSAMEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			OTMDSAMEditDistance OTMDSAMEditDistance)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<String, Pair<String, Double>> candEditDistances = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
		{
			// EditSimilarity editSimilarity = new EditSimilarity();
			Pair<String, Double> editDistanceForThisCandidate = null;
			String candidateTimelineId = entry.getKey();

			switch (caseType)
			{
			case CaseBasedV1:// "CaseBasedV1":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS) // invalids are already expunged, no need to
																	// expunge
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

			case SimpleV3:// "SimpleV3":
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
	 * @param featureWiseWeightedEditDistance
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> getFeatureWiseWeightedEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			FeatureWiseWeightedEditDistance featureWiseWeightedEditDistance)
	{
		// <CandidateTimeline ID, Edit distance>
		LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> candEditDistancesFeatureWise = new LinkedHashMap<>();

		for (Map.Entry<String, Timeline> entry : candidateTimelines.entrySet())
		{
			LinkedHashMap<String, Pair<String, Double>> featureWiseWeightedEditDistancesForThisCandidate = null;
			String candidateTimelineId = entry.getKey();

			switch (caseType)
			{
			case CaseBasedV1:// "CaseBasedV1":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithoutEndCurrentActivity(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS) // invalids are already expunged, no need to
																	// expunge
																	// again
				{
					featureWiseWeightedEditDistancesForThisCandidate = featureWiseWeightedEditDistance
							.getFeatureWiseWeightedEditDistanceWithoutEndCurrentActivity(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);
				}
				else
				{
					featureWiseWeightedEditDistancesForThisCandidate = featureWiseWeightedEditDistance
							.getFeatureWiseWeightedEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);
				}
				break;

			case SimpleV3:// "SimpleV3":
				// editDistanceForThisCandidate =
				// editSimilarity.getEditDistanceWithTrace(entry.getValue().getActivityObjectsInTimeline(),activitiesGuidingRecomm);
				if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
				{
					featureWiseWeightedEditDistancesForThisCandidate = featureWiseWeightedEditDistance
							.getFeatureWiseWeightedEditDistanceRawValsWithTrace(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);
				}
				else
				{
					featureWiseWeightedEditDistancesForThisCandidate = featureWiseWeightedEditDistance
							.getFeatureWiseWeightedEditDistanceInvalidsExpunged(
									entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm);
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
	 * @param userIDAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param distanceUsed
	 * @param hjEditDistance
	 * @param featureWiseEditDistance
	 * @param featureWiseWeightedEditDistance
	 * @param OTMDSAMEditDistance
	 * @param editDistancesMemorizer
	 * @param lookPastType
	 *            added on 17 Dec 2018
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getNormalisedDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userIDAtRecomm, String dateAtRecomm, String timeAtRecomm,
			String distanceUsed, HJEditDistance hjEditDistance, FeatureWiseEditDistance featureWiseEditDistance,
			FeatureWiseWeightedEditDistance featureWiseWeightedEditDistance, OTMDSAMEditDistance OTMDSAMEditDistance,
			EditDistanceMemorizer editDistancesMemorizer, LookPastType lookPastType)
	{

		switch (distanceUsed)
		{
		case "HJEditDistance":
			return getNormalisedHJEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
					activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
					hjEditDistance, editDistancesMemorizer, lookPastType);
		case "FeatureWiseEditDistance":
			return getNormalisedFeatureWiseEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
					activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
					featureWiseEditDistance);

		case "FeatureWiseWeightedEditDistance":
			return getNormalisedFeatureWiseWeightedEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
					activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
					featureWiseWeightedEditDistance);

		case "OTMDSAMEditDistance":
			return getNormalisedOTMDSAMEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
					activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
					OTMDSAMEditDistance);
		default:
			PopUps.showError(
					"Error in org.activity.recomm.RecommendationMasterMU.getNormalisedDistancesForCandidateTimelinesFullCand():Unknown distance specified:"
							+ distanceUsed);
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in org.activity.recomm.RecommendationMasterMU.getNormalisedDistancesForCandidateTimelinesFullCand(): Unknown distance specified:"
							+ distanceUsed));
			// throw new Exception("Error in org.activity.util.Constant.setDistanceUsed(String): Unknown distance
			// specified:" + dname);
			System.exit(-1);
		}
		System.err.println(PopUps.getTracedErrorMsg(
				"Error in org.activity.recomm.RecommendationMasterMU.getNormalisedDistancesForCandidateTimelinesFullCand()"
						+ " reaching unreachable code"));
		System.exit(-2);
		return null;
	}

	/**
	 * Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the edit distance of
	 * that candidate timeline with the current timeline.
	 * <p>
	 * <b>Normalised over all candidates for this RT</b>
	 * <p>
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
	 * @param hjEditDistance
	 *            the primary dimension for hjEditDistance is the given dimension (i.e., the dimension used to extract
	 *            the candidate timelines)
	 * @param editDistancesMemorizer
	 * @param lookPastType
	 *            added on 17 Dec 2018
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}},
	 */

	public static LinkedHashMap<String, Pair<String, Double>> getNormalisedHJEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			HJEditDistance hjEditDistance, EditDistanceMemorizer editDistancesMemorizer, LookPastType lookPastType)
	{
		// PopUps.showMessage("Inside getNormalisedHJEditDistancesForCandidateTimelinesFullCand\n");
		LinkedHashMap<String, Integer> endIndexSubseqConsideredInCand = null;// added on 17 Dec 2018 for daywise
		// {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}

		// CurtainA start
		// long t1 = System.currentTimeMillis();
		// LinkedHashMap<String, Pair<String, Double>> candEditDistances =
		// getHJEditDistancesForCandidateTimelinesFullCand(
		// candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm,
		// hjEditDistance);
		// long t2 = System.currentTimeMillis();
		//// CurtainA end

		// long t3 = System.currentTimeMillis();
		LinkedHashMap<String, Pair<String, Double>> candEditDistances;
		if (Constant.useJan7DistanceComputations)
		{// as of Jan 8 2019, finished implementation only for AED
			candEditDistances = getHJEditDistsByDiffsForCandsFullCandParallelWithMemory7Jan2019RTV(candidateTimelines,
					activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm, hjEditDistance,
					editDistancesMemorizer, lookPastType);
		}
		else
		{
			if (Constant.useRTVerseNormalisationForED == false)
			{ /* Parallel */
				candEditDistances = getHJEditDistsForCandsFullCandParallelWithMemory(candidateTimelines,
						activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm, hjEditDistance,
						editDistancesMemorizer);
			}
			else // use RT verse normalisation
			{ /* Parallel */
				candEditDistances = getHJEditDistsByDiffsForCandsFullCandParallelWithMemory13April2018RTV(
						candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm,
						hjEditDistance, editDistancesMemorizer, lookPastType);

				// candEditDistances = candEditDistancesRes.getFirst();
				// endIndexSubseqConsideredInCand = candEditDistancesRes.getSecond();
				/// Start of added on 2 Dec 2018

				// End of added on 2 Dec 2018
			}
		}
		// long t4 = System.currentTimeMillis();

		// Start Sanity check
		// System.out.println("Debug Aug 8 :1");
		// System.out.println("getHJEditDistancesForCandidateTimelinesFullCand = \t" + (t2 - t1) + " ms");
		// System.out.println("getHJEditDistsForCandsFullCandParallelWithMemory = \t" + (t4 - t3) + " ms");
		//
		// System.out.println("candEditDistances.size() = \t" + candEditDistances.size());
		// System.out.println("candEditDistancesParallel.size() = \t" + candEditDistancesParallel.size());
		//
		// System.out.println("candEditDistances.equals(candEditDistancesParallel) =\t"
		// + candEditDistances.equals(candEditDistancesParallel));
		//
		// if (candEditDistances.equals(candEditDistancesParallel) == false)
		// {
		// PopUps.printTracedErrorMsg("candEditDistances.equals(candEditDistancesParallel)==false");
		// }
		// // StringBuilder sbTemp1 = new StringBuilder();
		// // sbTemp1.append("candEditDistances:\n");
		// // candEditDistances.entrySet().stream()
		// // .forEachOrdered(e -> sbTemp1.append(e.getKey() + "--" + e.getValue() + "\n"));
		// // sbTemp1.append("candEditDistancesParallel:\n");
		// // candEditDistancesParallel.entrySet().stream()
		// // .forEachOrdered(e -> sbTemp1.append(e.getKey() + "--" + e.getValue() + "\n"));
		// // System.out.println(sbTemp1.toString());
		// End Sanity check
		System.out.println("Dimension for cand= " + hjEditDistance.primaryDimension);
		System.out.println("before filter candEditDistances.size():" + candEditDistances.size());
		// System.out.println("Constant.typeOfCandThreshold= " + Constant.typeOfCandThreshold);
		// System.out.println("Constant.typeOfCandThreshold.equals(Enums.TypeOfCandThreshold.NearestNeighbour)= "
		// + Constant.typeOfCandThreshold.equals(Enums.TypeOfCandThreshold.NearestNeighbour));

		TypeOfCandThreshold typeOfCandThresholdForGivenDimension = null;
		if (hjEditDistance.primaryDimension.equals(Constant.primaryDimension))
		{
			typeOfCandThresholdForGivenDimension = Constant.typeOfCandThresholdPrimDim;
		}
		if (hjEditDistance.primaryDimension.equals(Constant.secondaryDimension))
		{
			typeOfCandThresholdForGivenDimension = Constant.typeOfCandThresholdSecDim;
		}

		if (candEditDistances.size() > 0)
		{

			if (typeOfCandThresholdForGivenDimension.equals(Enums.TypeOfCandThreshold.NearestNeighbour)
					|| typeOfCandThresholdForGivenDimension
							.equals(Enums.TypeOfCandThreshold.NearestNeighbourWithEDValThresh))
			{
				candEditDistances = filterCandsNearestNeighbours(candEditDistances,
						Constant.getNearestNeighbourCandEDThresholdGivenDim(hjEditDistance.primaryDimension));
			}
			if (typeOfCandThresholdForGivenDimension.equals(Enums.TypeOfCandThreshold.Percentile))
			{
				// if (Constant.percentileCandEDThreshold != 100)// Not NO Threshold
				candEditDistances = filterCandsPercentileED(candEditDistances, Constant.percentileCandEDThreshold);
			}
			if (typeOfCandThresholdForGivenDimension.equals(Enums.TypeOfCandThreshold.NearestNeighbourWithEDValThresh))
			{
				candEditDistances = filterCandsEDThreshold(candEditDistances,
						Constant.getEDThresholdGivenDim(hjEditDistance.primaryDimension));
			}
			if (typeOfCandThresholdForGivenDimension.equals(Enums.TypeOfCandThreshold.None))
			{
				System.out.println("Alert! no filtering cands");
			}
		}
		else
		{
			System.out.println("No cands, hence no need to filter");
		}

		System.out.println("\nafter filter candEditDistances.size():" + candEditDistances.size()
				+ " typeOfCandThresholdForGivenDimension=" + typeOfCandThresholdForGivenDimension);

		LinkedHashMap<String, Pair<String, Double>> normalisedCandEditDistances = null;

		// if (Constant.useRTVerseNormalisationForED)
		// {// not necessary as already normalised
		// normalisedCandEditDistances = candEditDistances;
		// }
		// else
		// {//makes sense to do normalisation again since some candidates have been filtered out, hence the min and max
		// ED over the set might have changed.
		if (Constant.normaliseCandDistsAgainAfterFiltering)
		{
			normalisedCandEditDistances = normalisedDistancesOverTheSet(candEditDistances, userAtRecomm, dateAtRecomm,
					timeAtRecomm);
		}
		else
		{
			normalisedCandEditDistances = candEditDistances;
		}
		// }
		return normalisedCandEditDistances;
	}

	/**
	 * Keep only the top nearestNeighbourThreshold number of candidates by lower edit distance
	 * 
	 * @param candEditDistances
	 * @param nearestNeighbourThreshold
	 * @return
	 */
	private static LinkedHashMap<String, Pair<String, Double>> filterCandsNearestNeighbours(
			LinkedHashMap<String, Pair<String, Double>> candEditDistances, int nearestNeighbourThreshold)
	{
		System.out
				.print("... filtering CandsNearestNeighbours, nearestNeighbourThreshold= " + nearestNeighbourThreshold);

		if (nearestNeighbourThreshold < 0)
		{
			PopUps.showError("Error in filterCandsNearestNeighbours. nearestNeighbourThreshold <0 = "
					+ nearestNeighbourThreshold);
			System.exit(-1);
		}

		LinkedHashMap<String, Pair<String, Double>> candEditDistancesSorted = (LinkedHashMap<String, Pair<String, Double>>) ComparatorUtils
				.sortByValueAscendingStrStrDoub(candEditDistances);

		LinkedHashMap<String, Pair<String, Double>> candEditDistancesSortedFiltered = new LinkedHashMap<>();

		int c = 0;
		for (Entry<String, Pair<String, Double>> candEntry : candEditDistancesSorted.entrySet())
		{
			c++;
			if (c > nearestNeighbourThreshold)
			{
				break;
			}
			String s1 = candEntry.getKey();
			candEditDistancesSortedFiltered.put(s1, candEntry.getValue());
		}
		return candEditDistancesSortedFiltered;
	}

	/**
	 * Keep only the top nearestNeighbourThreshold qualtil of candidates by lower edit distance
	 * 
	 * @param candEditDistances
	 * @param percentileCandEDThreshold
	 *            range 0-100
	 * @return
	 */
	private static LinkedHashMap<String, Pair<String, Double>> filterCandsPercentileED(
			LinkedHashMap<String, Pair<String, Double>> candEditDistances, double percentileCandEDThreshold)
	{

		LinkedHashMap<String, Pair<String, Double>> candEditDistancesFiltered = new LinkedHashMap<>();

		List<Double> listOfEditDistances = candEditDistances.entrySet().stream().map(e -> e.getValue().getSecond())
				.collect(Collectors.toList());

		double percentileVal = StatsUtils.getPercentile(listOfEditDistances, percentileCandEDThreshold);

		for (Entry<String, Pair<String, Double>> candEntry : candEditDistances.entrySet())
		{
			if (candEntry.getValue().getSecond() <= percentileVal)
			{
				candEditDistancesFiltered.put(candEntry.getKey(), candEntry.getValue());
			}
		}

		if (VerbosityConstants.tempVerbose)
		{
			System.out.print("... filtering CandsPercentileED");
			StringBuilder sb = new StringBuilder();
			sb.append("\nlistOfEditDistances = " + listOfEditDistances + "\npercentileVal=" + percentileVal
					+ "\nBefore Filter candEditDistances:\n");
			candEditDistances.entrySet().stream().forEachOrdered(
					e -> sb.append("\t" + e.getKey() + "-" + e.getValue().getSecond().toString() + "\n"));
			sb.append("\n candEditDistancesFiltered:\n");
			candEditDistancesFiltered.entrySet().stream().forEachOrdered(
					e -> sb.append("\t" + e.getKey() + "-" + e.getValue().getSecond().toString() + "\n"));
			System.out.println(sb.toString());
		}
		return candEditDistancesFiltered;
	}

	/**
	 * Keep only the cand with ED <= x
	 * 
	 * @param candEditDistances
	 * @param edUpperBoundThreshold
	 * @return
	 * @since 9 Aug 2018
	 */
	private static LinkedHashMap<String, Pair<String, Double>> filterCandsEDThreshold(
			LinkedHashMap<String, Pair<String, Double>> candEditDistances, double edUpperBoundThreshold)
	{
		System.out.print("... filtering filterCandsEDThreshold, edUpperBoundThreshold <= " + edUpperBoundThreshold);

		if (edUpperBoundThreshold < 0 || edUpperBoundThreshold > 1)
		{
			PopUps.showError(
					"Error in filterCandsEDThreshold. edUpperBoundThreshold (<0||>1) = " + edUpperBoundThreshold);
			System.exit(-1);
		}

		// LinkedHashMap<String, Pair<String, Double>> candEditDistancesSorted = (LinkedHashMap<String, Pair<String,
		// Double>>) ComparatorUtils.sortByValueAscendingStrStrDoub(candEditDistances);

		LinkedHashMap<String, Pair<String, Double>> candEditDistancesFiltered = new LinkedHashMap<>();
		StringBuilder sbSanityLog = new StringBuilder();
		int countOfErrors = 0;

		for (Entry<String, Pair<String, Double>> candEntry : candEditDistances.entrySet())
		{
			Double edVal = candEntry.getValue().getSecond();
			if (edVal < 0 || edVal > 1)
			{
				sbSanityLog.append("Warning 9 Aug in filterCandsEDThreshold()! edVal >1||<0 , edVal= " + edVal + "\n");
				countOfErrors += 1;
			}

			if (edVal <= edUpperBoundThreshold)
			{
				candEditDistancesFiltered.put(candEntry.getKey(), candEntry.getValue());
			}
		}
		System.out.println(sbSanityLog.toString());
		System.out.println("Num of cand with ED out of norm range = " + countOfErrors);
		return candEditDistancesFiltered;
	}

	/**
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param featureWiseEditDistance
	 * @return
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getNormalisedFeatureWiseEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			FeatureWiseEditDistance featureWiseEditDistance)
	{
		LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> candEditDistancesFeatureWise = getFeatureWiseEditDistancesForCandidateTimelinesFullCand(
				candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm,
				featureWiseEditDistance);

		LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> normalisedCandEditDistances = normalisedFeatureWiseDistancesOverTheSet(
				candEditDistancesFeatureWise);
		LinkedHashMap<String, Pair<String, Double>> aggregatedNormalisedCandEditDistances = DistanceUtils
				.aggregatedFeatureWiseDistancesForCandidateTimelinesFullCand(normalisedCandEditDistances);

		if (VerbosityConstants.verboseNormalisation)
		{
			UtilityBelt.traverseStringStringPair(normalisedCandEditDistances,
					" Normalised Feature wise Edit Distances");
			UtilityBelt.traverseStringPair(aggregatedNormalisedCandEditDistances,
					"Aggregated Normalised  Feature wise Edit Distances");
		}
		return aggregatedNormalisedCandEditDistances;
	}

	/**
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param caseType
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param OTMDSAMEditDistance
	 * @return
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getNormalisedOTMDSAMEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			OTMDSAMEditDistance OTMDSAMEditDistance)
	{
		LinkedHashMap<String, Pair<String, Double>> candEditDistances = getOTMDSAMEditDistancesForCandidateTimelinesFullCand(
				candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm,
				OTMDSAMEditDistance);

		LinkedHashMap<String, Pair<String, Double>> normalisedCandEditDistances = normalisedDistancesOverTheSet(
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
	 * @param featureWiseWeightedEditDistance
	 * @return
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getNormalisedFeatureWiseWeightedEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject2018> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			FeatureWiseWeightedEditDistance featureWiseWeightedEditDistance)
	{
		LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> candEditDistancesFeatureWise = getFeatureWiseWeightedEditDistancesForCandidateTimelinesFullCand(
				candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm,
				featureWiseWeightedEditDistance);

		LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> normalisedCandEditDistances = normalisedFeatureWiseDistancesOverTheSet(
				candEditDistancesFeatureWise);
		LinkedHashMap<String, Pair<String, Double>> aggregatedNormalisedCandEditDistances = DistanceUtils
				.aggregatedFeatureWiseDistancesForCandidateTimelinesFullCand(normalisedCandEditDistances);

		if (VerbosityConstants.verboseNormalisation)
		{
			UtilityBelt.traverseStringStringPair(normalisedCandEditDistances, " Normalised Weighted Edit Distances");
			UtilityBelt.traverseStringPair(aggregatedNormalisedCandEditDistances,
					"Aggregated Normalised Weighted Edit Distances");
		}
		return aggregatedNormalisedCandEditDistances;
	}

	/**
	 * 
	 * @param setOfFeatureWiseDistances
	 * @return
	 */
	public static LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> normalisedFeatureWiseDistancesOverTheSet(
			LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> setOfFeatureWiseDistances)
	{

		LinkedHashMap<String, LinkedHashMap<String, Pair<String, Double>>> normalisedDistancesPerCand = new LinkedHashMap<>();

		int numOfFeatures = Constant.getNumberOfFeatures();

		double[] maxs = new double[numOfFeatures];// maxs for each feature
		double[] mins = new double[numOfFeatures];

		for (int i = 0; i < numOfFeatures; i++)
		{
			maxs[i] = Constant.minForNorm;
			mins[i] = Constant.maxForNorm;
		}

		for (Map.Entry<String, LinkedHashMap<String, Pair<String, Double>>> entry : setOfFeatureWiseDistances
				.entrySet()) // iterating over cands
		{// String candID = entry.getKey();
			LinkedHashMap<String, Pair<String, Double>> featureWiseDistances = entry.getValue();

			int featureIndex = 0;

			// iterating over distance for each feature
			for (Map.Entry<String, Pair<String, Double>> distEntry : featureWiseDistances.entrySet())
			{
				String featureName = distEntry.getKey();
				double distanceValue = distEntry.getValue().getSecond();
				if (VerbosityConstants.verboseNormalisation)
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
		if (VerbosityConstants.verboseNormalisation)
		{// iterating over cands
			for (Map.Entry<String, LinkedHashMap<String, Pair<String, Double>>> entry : setOfFeatureWiseDistances
					.entrySet())
			{
				System.out.print("Cand id:" + entry.getKey() + "-");
				LinkedHashMap<String, Pair<String, Double>> featureWiseDistances = entry.getValue();

				// iterating over distance for each feature
				for (Map.Entry<String, Pair<String, Double>> distEntry : featureWiseDistances.entrySet())
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

		for (Map.Entry<String, LinkedHashMap<String, Pair<String, Double>>> entry : setOfFeatureWiseDistances
				.entrySet()) // iterating over cands
		{
			String candID = entry.getKey();
			LinkedHashMap<String, Pair<String, Double>> featureWiseDistances = entry.getValue();
			LinkedHashMap<String, Pair<String, Double>> normalisedFeatureWiseDistances = new LinkedHashMap<String, Pair<String, Double>>();

			int featureIndex = 0;
			for (Map.Entry<String, Pair<String, Double>> distEntry : featureWiseDistances.entrySet())
			// iterating over distance for each feature
			{
				String featureName = distEntry.getKey();
				double distanceValue = distEntry.getValue().getSecond();
				double normalisedDistanceValue = StatsUtils.minMaxNorm(distanceValue, maxs[featureIndex],
						mins[featureIndex]);
				normalisedFeatureWiseDistances.put(featureName,
						new Pair<String, Double>(distEntry.getValue().getFirst(), normalisedDistanceValue));
				featureIndex++;
			}
			normalisedDistancesPerCand.put(candID, normalisedFeatureWiseDistances);
		}

		return normalisedDistancesPerCand;
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
	public static LinkedHashMap<String, Pair<String, Double>> normalisedDistancesOverTheSet(
			LinkedHashMap<String, Pair<String, Double>> setOfDistances, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm)
	{
		LinkedHashMap<String, Pair<String, Double>> normalisedDistances = new LinkedHashMap<>();

		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

		int numOfValsAtMax = 0, numOfValsAtMin = 0;
		// StringBuilder editDistancesLog = new StringBuilder();// , normalisedEditDistancesLog = new StringBuilder();
		ArrayList<Double> editDistancesLogList = new ArrayList<>();
		ArrayList<Double> normalisedEditDistancesLogList = new ArrayList<>();

		// find the max and min and how many at max and min
		int i = 0;
		for (Map.Entry<String, Pair<String, Double>> distEntry : setOfDistances.entrySet())
		{
			// Integer candTimelineID = distEntry.getKey();
			Double editDistanceVal = distEntry.getValue().getSecond();

			if (VerbosityConstants.WriteNormalisation)
			{
				// editDistancesLog.append("_" + editDistanceVal);
				editDistancesLogList.add(editDistanceVal);
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

		for (Map.Entry<String, Pair<String, Double>> distEntry : setOfDistances.entrySet())
		{
			if (Constant.doMaxNormalisationWhenNormalisationEDsOverCandSet)// TODO: doing max normalisation TEMPORARY
			{
				min = 0;
			}
			Double normalisedEditDistanceVal = Double
					.valueOf(StatsUtils.minMaxNorm(distEntry.getValue().getSecond(), max, min));

			// if (normalisedEditDistanceVal == 0)
			// {
			// System.out.println("Debug: normalisedEditDistanceVal=" + normalisedEditDistanceVal
			// + " distEntry.getValue().getSecond()= " + distEntry.getValue().getSecond() + " max=" + max
			// + " min=" + min);
			// }

			if (VerbosityConstants.WriteNormalisation)
			{
				// normalisedEditDistancesLog.append("_" + normalisedEditDistanceVal);
				normalisedEditDistancesLogList.add(normalisedEditDistanceVal);
			}
			normalisedDistances.put(distEntry.getKey(),
					new Pair<String, Double>(distEntry.getValue().getFirst(), normalisedEditDistanceVal));
		}

		if (VerbosityConstants.WriteNormalisation && !VerbosityConstants.WriteNormalisationsSeparateLines)
		{
			Collections.sort(normalisedEditDistancesLogList);
			Collections.sort(editDistancesLogList);
			String toWrite = userAtRecomm + "||" + dateAtRecomm + "||" + timeAtRecomm + "||" + editDistancesLogList
					+ "||" + normalisedEditDistancesLogList + "\n";
			WToFile.appendLineToFileAbs(toWrite, Constant.getCommonPath() + "NormalisationDistances.csv");
		}

		if (VerbosityConstants.WriteNormalisationsSeparateLines)
		{
			Collections.sort(normalisedEditDistancesLogList);
			Collections.sort(editDistancesLogList);
			int j = 0;
			for (Double raw : editDistancesLogList)
			{
				String toWrite = userAtRecomm + "," + dateAtRecomm + "," + timeAtRecomm + "," + raw + ","
						+ normalisedEditDistancesLogList.get(j) + "\n";
				WToFile.appendLineToFileAbs(toWrite, Constant.getCommonPath() + "NormalisationDistances.csv");
				j++;
			}

		}

		System.out.println(
				"getNormalisedDistancesOverTheSet: #Vals max=" + numOfValsAtMax + " #Vals min=" + numOfValsAtMin);

		if (true)
		{
			if (normalisedDistances.equals(setOfDistances))
			{
				System.out.println(
						"Alert! org.activity.distances.DistanceUtils.normalisedDistancesOverTheSet() had no effect.");
			}
			else
			{
				System.out.println(
						"Alert! org.activity.distances.DistanceUtils.normalisedDistancesOverTheSet() HAS effect.");
			}
		}

		return normalisedDistances;
	}

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
}
