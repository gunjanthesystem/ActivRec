package org.activity.distances;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.CaseType;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.VerbosityConstants;
import org.activity.io.EditDistanceMemorizer;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.TimelineUtils;
import org.activity.util.UtilityBelt;

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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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
			Pair<LinkedHashMap<String, Pair<String, Double>>, LinkedHashMap<String, Integer>> editDistancesRes = TimelineUtils
					.getEditDistancesForDaywiseCandidateTimelines(candidateTimelines, activitiesGuidingRecomm,
							userIDAtRecomm, dateAtRecomm.toString(), timeAtRecomm.toString(),
							Constant.hasInvalidActivityNames, Constant.INVALID_ACTIVITY1, Constant.INVALID_ACTIVITY2,
							distanceUsed, hjEditDistance);

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
					OTMDSAMEditDistance, editDistancesMemorizer);

			// for SeqNCount and SeqNHours approach, tne end point index considered in the candidate is the last
			// activity object in that cand
			// endIndexSubseqConsideredInCand = (LinkedHashMap<String, Integer>) candidateTimelines.entrySet().stream()
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
			PopUps.printTracedErrorMsg(
					"Error: normalisedDistanceForCandTimelines.size=" + normalisedDistanceForCandTimelines.size());
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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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
							"Error in getEditDistancesForCandidateTimelineFullCand: unidentified case type"
									+ caseType));
					break;
			}

			candEditDistances.put(candidateTimelineId, editDistanceForThisCandidate);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}
		return candEditDistances;
	}

	/// Start of added on 9 Aug 2017
	// ////////
	/*
	 * Fork of getHJEditDistancesForCandidateTimelinesFullCand() <p> Added: Aug 9, 2017: for better performance
	 * (parallel) and memorising edit distance computations<p>
	 * 
	 * IMPORTANT POINT: THE CANDIDATE TIMELINE IS THE DIRECT CANDIDATE TIMELINE AND NOT THE LEAST DISTANT
	 * SUBCANDIDATE.<p> Returns a map where each entry corresponds to a candidate timeline. The value of an entry is the
	 * edit distance of that candidate timeline with the current timeline.
	 * 
	 * @param candidateTimelines
	 * 
	 * @param activitiesGuidingRecomm
	 * 
	 * @param caseType can be 'SimpleV3' or 'CaseBasedV1'
	 * 
	 * @param userAtRecomm used only for writing to file
	 * 
	 * @param dateAtRecomm used only for writing to file
	 * 
	 * @param timeAtRecomm used only for writing to file
	 * 
	 * @param hjEditDistance
	 * 
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getHJEditDistsForCandsFullCandParallelWithMemory(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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
			ArrayList<ActivityObject> activitiesGuidingRecomm, String userAtRecomm, String dateAtRecomm,
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
					editDistanceForThisCandidate = hjEditDistance.getHJEditDistanceWithTrace(
							candTimeline.getActivityObjectsInTimeline(), activitiesGuidingRecomm, userAtRecomm,
							dateAtRecomm, timeAtRecomm, candTimeline.getTimelineID());
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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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
								.getFeatureWiseEditDistanceInvalidsExpunged(
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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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
						editDistanceForThisCandidate = OTMDSAMEditDistance
								.getOTMDSAMEditDistanceWithoutEndCurrentActivity(
										entry.getValue().getActivityObjectsInTimeline(), activitiesGuidingRecomm,
										userAtRecomm, dateAtRecomm, timeAtRecomm, candidateTimelineId);
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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getNormalisedDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userIDAtRecomm, String dateAtRecomm, String timeAtRecomm,
			String distanceUsed, HJEditDistance hjEditDistance, FeatureWiseEditDistance featureWiseEditDistance,
			FeatureWiseWeightedEditDistance featureWiseWeightedEditDistance, OTMDSAMEditDistance OTMDSAMEditDistance,
			EditDistanceMemorizer editDistancesMemorizer)
	{

		switch (distanceUsed)
		{
			case "HJEditDistance":
				return getNormalisedHJEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
						activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(),
						timeAtRecomm.toString(), hjEditDistance, editDistancesMemorizer);
			case "FeatureWiseEditDistance":
				return getNormalisedFeatureWiseEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
						activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(),
						timeAtRecomm.toString(), featureWiseEditDistance);

			case "FeatureWiseWeightedEditDistance":
				return getNormalisedFeatureWiseWeightedEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
						activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(),
						timeAtRecomm.toString(), featureWiseWeightedEditDistance);

			case "OTMDSAMEditDistance":
				return getNormalisedOTMDSAMEditDistancesForCandidateTimelinesFullCand(candidateTimelines,
						activitiesGuidingRecomm, caseType, userIDAtRecomm, dateAtRecomm.toString(),
						timeAtRecomm.toString(), OTMDSAMEditDistance);
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
	 * @param hjEditDistance
	 * @return {CanditateTimelineID, Pair{Trace,Edit distance of this candidate}}
	 */

	public static LinkedHashMap<String, Pair<String, Double>> getNormalisedHJEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
			Enums.CaseType caseType, String userAtRecomm, String dateAtRecomm, String timeAtRecomm,
			HJEditDistance hjEditDistance, EditDistanceMemorizer editDistancesMemorizer)
	{
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
		LinkedHashMap<String, Pair<String, Double>> candEditDistances/* Parallel */ = getHJEditDistsForCandsFullCandParallelWithMemory(
				candidateTimelines, activitiesGuidingRecomm, caseType, userAtRecomm, dateAtRecomm, timeAtRecomm,
				hjEditDistance, editDistancesMemorizer);
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
		System.out.println("before filter candEditDistances.size():" + candEditDistances.size());
		if (Constant.filterTopCands > 0)
		{
			System.out.print("... filtering");
			LinkedHashMap<String, Pair<String, Double>> candEditDistancesSorted = (LinkedHashMap<String, Pair<String, Double>>) ComparatorUtils
					.sortByValueAscendingStrStrDoub(candEditDistances);

			LinkedHashMap<String, Pair<String, Double>> candEditDistancesSortedFiltered = new LinkedHashMap<>();

			int c = 0;
			for (Entry<String, Pair<String, Double>> candEntry : candEditDistancesSorted.entrySet())
			{
				c++;
				if (c > Constant.filterTopCands)
				{
					break;
				}
				candEditDistancesSortedFiltered.put(candEntry.getKey(), candEntry.getValue());
			}
			candEditDistances = candEditDistancesSortedFiltered;
		}

		System.out.println("after filter candEditDistances.size():" + candEditDistances.size());
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
	 * @param featureWiseEditDistance
	 * @return
	 */
	public static LinkedHashMap<String, Pair<String, Double>> getNormalisedFeatureWiseEditDistancesForCandidateTimelinesFullCand(
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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
			LinkedHashMap<String, Timeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
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
			WritingToFile.appendLineToFileAbsolute(toWrite, Constant.getCommonPath() + "NormalisationDistances.csv");
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
				WritingToFile.appendLineToFileAbsolute(toWrite,
						Constant.getCommonPath() + "NormalisationDistances.csv");
				j++;
			}

		}

		System.out.println(
				"getNormalisedDistancesOverTheSet: #Vals max=" + numOfValsAtMax + " #Vals min=" + numOfValsAtMin);
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
