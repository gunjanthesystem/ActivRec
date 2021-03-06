package org.activity.util;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.TimelineWithNext;
import org.activity.objects.Triple;
import org.activity.sanityChecks.Sanity;
import org.activity.sanityChecks.TimelineSanityChecks;
import org.activity.ui.PopUps;

/**
 * @since 16 Nov 2017
 * @author gunjan All the method brought have been been moved in from RecommendationMasterMar2017AltAlgoSeq()
 */
public class TimelineExtractors
{

	/**
	 * 
	 * @param testTimelinesOrig
	 * @param lookPastType2
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCountsOrHours
	 * @return
	 */
	public static Pair<TimelineWithNext, Double> extractCurrentTimeline(LinkedHashMap<Date, Timeline> testTimelinesOrig,
			LookPastType lookPastType2, Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm,
			double matchingUnitInCountsOrHours)
	{
		TimelineWithNext extractedCurrentTimeline = null;
		LinkedHashMap<Date, Timeline> testTimelinesDaywise = testTimelinesOrig;
		double reductionInMu = 0;// done when not enough acts in past to look into, only relevant for NCount

		if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
		{
			if (Constant.hasInvalidActivityNames)
			{
				testTimelinesDaywise = TimelineTrimmers.expungeInvalidsDayTimelines(testTimelinesOrig);
				// $$System.out.println("Expunging invalids before recommendation process: expunging test timelines");
			}
			else
			{
				// $$System.out.println("Data assumed to have no invalid act names to be expunged from test timelines");
			}
		}

		// //////////////////
		if (lookPastType2.equals(Enums.LookPastType.Daywise) || lookPastType2.equals(Enums.LookPastType.ClosestTime))
		{
			extractedCurrentTimeline = TimelineExtractors.getCurrentTimelineFromLongerTimelineDaywise(
					testTimelinesDaywise, dateAtRecomm, timeAtRecomm, userIDAtRecomm);
			// for closest-time approach, only the current activity name is important, we do not actually need the
			// complete current timeline
		}
		else
		{
			// converting day timelines into continuous timelines suitable to be used for matching unit views
			Timeline testTimeline = TimelineTransformers.dayTimelinesToATimeline(testTimelinesDaywise, false, true);

			if (lookPastType2.equals(Enums.LookPastType.NCount) || lookPastType2.equals(Enums.LookPastType.NGram))
			{
				Pair<TimelineWithNext, Double> result = TimelineExtractors.getCurrentTimelineFromLongerTimelineMUCount(
						testTimeline, dateAtRecomm, timeAtRecomm, userIDAtRecomm, matchingUnitInCountsOrHours);
				extractedCurrentTimeline = result.getFirst();
				reductionInMu = result.getSecond();
			}

			else if (lookPastType2.equals(Enums.LookPastType.NHours))
			{
				extractedCurrentTimeline = TimelineExtractors.getCurrentTimelineFromLongerTimelineMUHours(testTimeline,
						dateAtRecomm, timeAtRecomm, userIDAtRecomm, matchingUnitInCountsOrHours);
			}

			else
			{
				System.err.println(PopUps.getTracedErrorMsg("Error: Unrecognised lookPastType "));
				System.exit(-1);
			}
		}
		// ////////////////////
		if (extractedCurrentTimeline == null || extractedCurrentTimeline.getActivityObjectsInTimeline().size() == 0)
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: current timeline is empty"));
			System.exit(-1);
			// this.errorExists = true;
		}
		// if (VerbosityConstants.verbose)
		{
			System.out.println(
					"Extracted current timeline: " + extractedCurrentTimeline.getPrimaryDimensionValsInSequence());
		}
		return new Pair<>(extractedCurrentTimeline, reductionInMu);
	}

	/**
	 * <p>
	 * Note: the extracted current timeline does not have a next act obj
	 * 
	 * @param testTimelinesOrig
	 * @param lookPastType
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCountsOrHours
	 * @param actObjsToAddToCurrentTimeline
	 * @return
	 * @since May 2, 2017
	 */
	public static Pair<TimelineWithNext, Double> extractCurrentTimelineSeq(
			LinkedHashMap<Date, Timeline> testTimelinesOrig, LookPastType lookPastType, Date dateAtRecomm,
			Time timeAtRecomm, String userIDAtRecomm, double matchingUnitInCountsOrHours,
			ArrayList<ActivityObject2018> actObjsToAddToCurrentTimeline)
	{
		// System.out.println("called extractCurrentTimelineSeq");
		Pair<TimelineWithNext, Double> extractedCurrentTimelineResult = extractCurrentTimeline(testTimelinesOrig,
				lookPastType, dateAtRecomm, timeAtRecomm, userIDAtRecomm, matchingUnitInCountsOrHours);

		TimelineWithNext extractedCurrentTimeline = extractedCurrentTimelineResult.getFirst();
		System.out.println("actObjsToAddToCurrentTimeline.size() = " + actObjsToAddToCurrentTimeline.size());
		// //////////////////
		ArrayList<ActivityObject2018> actObjsForCurrTimeline = new ArrayList<>(
				extractedCurrentTimeline.getActivityObjectsInTimeline());

		// sanity check is act objs to add are later than act objs in timeline
		TimelineSanityChecks.checkIfChronoLogicalOrder(actObjsForCurrTimeline, actObjsToAddToCurrentTimeline);

		// temp for debug
		// { System.out.println("Debug 847 inside extractCurrentTimelineSeq, \nactObjsForCurrTimeline = ");
		// actObjsForCurrTimeline.stream().forEachOrdered(ao -> System.err.println(">>" + ao.toStringAllGowallaTS()));
		// System.out.println("actObjsToAddToCurrentTimeline = ");
		// actObjsToAddToCurrentTimeline.stream()
		// .forEachOrdered(ao -> System.err.println(">>" + ao.toStringAllGowallaTS())); }

		// this addition can cause the timeline to spill over days
		actObjsForCurrTimeline.addAll(actObjsToAddToCurrentTimeline);

		// NOTE: WE ARE NOT SETTING NEXT ACTIVITY OBJECT OF CURRENT TIMELINE HERE.
		if (lookPastType.equals(Enums.LookPastType.Daywise))// || lookPastType.equals(Enums.LookPastType.ClosestTime))
		{
			extractedCurrentTimeline = new TimelineWithNext(actObjsForCurrTimeline, null, true, true);
		}

		else if (lookPastType.equals(Enums.LookPastType.ClosestTime))
		{
			if (Constant.ClosestTimeAllowSpillOverDays)
			{// ALLOWING SPILL OVER ONTO NEXT DAY
				extractedCurrentTimeline = new TimelineWithNext(actObjsForCurrTimeline, null, false, true);
			}
			else
			{
				extractedCurrentTimeline = new TimelineWithNext(actObjsForCurrTimeline, null, true, true);
			}
		}

		else if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours)
				|| lookPastType.equals(Enums.LookPastType.NGram))
		{
			extractedCurrentTimeline = new TimelineWithNext(actObjsForCurrTimeline, null, false, true);
		}

		extractedCurrentTimeline.setImmediateNextActivityIsInvalid(-1);
		// ////////////////////
		if (extractedCurrentTimeline.getActivityObjectsInTimeline().size() == 0)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: extractCurrentTimeline extractedCurrentTimeline.getActivityObjectsInTimeline().size()="
							+ extractedCurrentTimeline.getActivityObjectsInTimeline().size());
			// this.errorExists = true;
		}
		if (VerbosityConstants.verbose)
		{
			System.out.println("Extracted current timeline extractCurrentTimeline: "
					+ extractedCurrentTimeline.getActivityObjectNamesInSequence());
		}

		return new Pair<>(extractedCurrentTimeline, extractedCurrentTimelineResult.getSecond());

	}

	/**
	 * 
	 * @param trainingTimelineOrig
	 * @param lookPastType
	 * @param dateAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCountsOrHours
	 * @param activityAtRecommPoint
	 * @param trainTestTimelinesForAllUsersOrig
	 * @param trainTimelinesAllUsersContinuous
	 * @return {candID,candTimeline}
	 * @deprecated on Aug 5 2018, superceeded by extractCandidateTimelinesV2 which is now effectively equivalent but
	 *             with difference in some comments
	 */
	public static LinkedHashMap<String, Timeline> extractCandidateTimelines(
			LinkedHashMap<Date, Timeline> trainingTimelineOrig, LookPastType lookPastType, Date dateAtRecomm,
			/* Time timeAtRecomm, */ String userIDAtRecomm, double matchingUnitInCountsOrHours,
			ActivityObject2018 activityAtRecommPoint,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersOrig,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous, PrimaryDimension dimensionToMatch)
	{
		LinkedHashMap<String, Timeline> candidateTimelines = null;
		LinkedHashMap<Date, Timeline> trainingTimelinesDaywise = trainingTimelineOrig;
		LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers = trainTestTimelinesForAllUsersOrig;

		// System.out.println("Inside extractCandidateTimelines :trainTestTimelinesForAllUsers.size()= "
		// + trainTestTimelinesForAllUsers.size());

		if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
		{
			if (Constant.hasInvalidActivityNames)
			{
				trainingTimelinesDaywise = TimelineTrimmers.expungeInvalidsDayTimelines(trainingTimelineOrig);
				trainTestTimelinesForAllUsers = TimelineTrimmers
						.expungeInvalidsDayTimelinesAllUsers(trainTestTimelinesForAllUsersOrig);
				// $$ System.out.println("Expunging invalids before recommendation process: expunging training
				// timelines");
			}
			else
			{
				// $$System.out.println("Data assumed to have no invalid act names to be expunged from training
				// timelines");
			}
		}

		// //////////////////
		if (lookPastType.equals(Enums.LookPastType.Daywise))
		{
			if (Constant.collaborativeCandidates)
			{
				candidateTimelines = extractDaywiseCandidateTimelinesCollV2(dateAtRecomm, userIDAtRecomm,
						activityAtRecommPoint, trainTestTimelinesForAllUsers, Constant.only1CandFromEachCollUser,
						Constant.onlyPastFromRecommDateInCandInColl);
			}

			else
			{
				// Obtain {Date,Timeline}
				LinkedHashMap<Date, Timeline> candidateTimelinesDate = TimelineExtractors
						.extractDaywiseCandidateTimelines(trainingTimelinesDaywise, dateAtRecomm,
								activityAtRecommPoint);

				// convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>)
				candidateTimelines = candidateTimelinesDate.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue(), (v1, v2) -> v1,
								LinkedHashMap<String, Timeline>::new));
			}
		}

		/// aaaaaaa
		else if (lookPastType.equals(Enums.LookPastType.ClosestTime) && Constant.ClosestTimeFilterCandidates)

		{
			if (Constant.collaborativeCandidates)
			{
				candidateTimelines = extractCandidateTimelinesClosestTimeColl(activityAtRecommPoint, userIDAtRecomm,
						trainTimelinesAllUsersContinuous, 24);

				// extractCandClosestTimeColl2(userIDAtRecomm, trainTestTimelinesForAllUsers);
				//
				// candidateTimeline = extractCandidateTimelinesMUColl(/* trainingTimeline, */
				// trainTestTimelinesForAllUsers, matchingUnitInCountsOrHours, lookPastType2,
				// activityAtRecommPoint, userIDAtRecomm, trainTimelinesAllUsersContinuous);
			}
			else
			{
				// Obtain {Date,Timeline}
				LinkedHashMap<Date, Timeline> candidateTimelinesDate = TimelineExtractors
						.extractDaywiseCandidateTimelines(trainingTimelinesDaywise, dateAtRecomm,
								activityAtRecommPoint);

				// convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>)
				candidateTimelines = candidateTimelinesDate.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue(), (v1, v2) -> v1,
								LinkedHashMap<String, Timeline>::new));
			}
		}
		// aaaaaaa

		// take all training day timelines as candidate timelines, i.e., no filtering of candidate timelines for closest
		// time approach
		else if (lookPastType.equals(Enums.LookPastType.ClosestTime) && !Constant.ClosestTimeFilterCandidates)
		{
			if (Constant.collaborativeCandidates)
			{
				candidateTimelines = extractCandClosestTimeColl1(userIDAtRecomm, trainTestTimelinesForAllUsers);
			}
			else
			{
				// convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>)
				candidateTimelines = trainingTimelinesDaywise.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue(), (v1, v2) -> v1,
								LinkedHashMap<String, Timeline>::new));

				Sanity.eq(candidateTimelines.size(), trainingTimelinesDaywise.size(),
						"candidateTimelines.size() = " + candidateTimelines.size()
								+ "!= trainingTimelinesDaywise.size() = " + trainingTimelinesDaywise.size());
			}
		}
		else if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours))
		{

			LinkedHashMap<String, TimelineWithNext> candidateTimelinesWithNext = null;

			if (Constant.collaborativeCandidates)
			{
				candidateTimelinesWithNext = TimelineExtractors.extractCandidateTimelinesMUColl(/* trainingTimeline, */
						/* trainTestTimelinesForAllUsers, */ matchingUnitInCountsOrHours, lookPastType,
						activityAtRecommPoint, userIDAtRecomm, trainTimelinesAllUsersContinuous, dimensionToMatch);
			}
			else
			{
				// converting day timelines into continuous timelines suitable to be used for matching unit views
				Timeline trainingTimeline = TimelineTransformers.dayTimelinesToATimeline(trainingTimelinesDaywise,
						false, true);
				// Obtain {String,TimelineWithNext}

				candidateTimelinesWithNext = TimelineExtractors.extractCandidateTimelinesMU(trainingTimeline,
						matchingUnitInCountsOrHours, lookPastType, activityAtRecommPoint);
			}

			// convert to {String,Timeline}
			candidateTimelines = candidateTimelinesWithNext.entrySet().stream().collect(Collectors
					.toMap(e -> e.getKey(), e -> e.getValue(), (v1, v2) -> v1, LinkedHashMap<String, Timeline>::new));
		}

		///
		else if (lookPastType.equals(Enums.LookPastType.NGram))
		{
			LinkedHashMap<String, TimelineWithNext> candidateTimelinesWithNext = null;

			if (Constant.collaborativeCandidates)
			{
				candidateTimelinesWithNext = TimelineExtractors.extractCandidateTimelinesMUColl(/* trainingTimeline, */
						/* trainTestTimelinesForAllUsers, */ 0, lookPastType, activityAtRecommPoint, userIDAtRecomm,
						trainTimelinesAllUsersContinuous, dimensionToMatch);
			}
			else
			{
				PopUps.printTracedErrorMsgWithExit(
						"Error: not implemented for this case here, instead look at the dedicated Recomm test for NGram.");
			}

			// convert to {String,Timeline}
			candidateTimelines = candidateTimelinesWithNext.entrySet().stream().collect(Collectors
					.toMap(e -> e.getKey(), e -> e.getValue(), (v1, v2) -> v1, LinkedHashMap<String, Timeline>::new));
		}
		///
		else
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: Unrecognised lookPastType "));
			System.exit(-1);
		}

		// ////////////////////
		if (candidateTimelines == null)// || candidateTimelines.size() < 1)
		{
			System.out.println(PopUps.getCurrentStackTracedWarningMsg("Warning: candidate timeline is empty"));
			// this.errorExists = true;
		}
		return candidateTimelines;
	}

	/**
	 * 
	 * @param dateAtRecomm
	 * @param userIDAtRecomm
	 * @param activityAtRecommPoint
	 * @param trainTestTimelinesForAllUsers
	 * @param only1CandFromEachCollUser
	 * @param onlyPastFromRecommDateInCandInColl
	 * @return {UserID_Date, CandTimeline}
	 *         <p>
	 *         <b> key as UserID_Date instead of Date since we should allow cands from different users but same date
	 *         </b> (Changed on April 11 2018).
	 * @deprecated on Aug 5 2018 as superceeded by extractDaywiseCandidateTimelinesCollV2 which has additional logic for
	 *             the case (Constant.numOfCandsFromEachCollUser > 0)
	 */
	public static LinkedHashMap<String, Timeline> extractDaywiseCandidateTimelinesColl(Date dateAtRecomm,
			String userIDAtRecomm, ActivityObject2018 activityAtRecommPoint,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			boolean only1CandFromEachCollUser, boolean onlyPastFromRecommDateInCandInColl)
	{
		LinkedHashMap<String, Timeline> dwCandidateTimelines = new LinkedHashMap<>();

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> entryForAUser : trainTestTimelinesForAllUsers
				.entrySet())// go over all users
		{
			String userID = entryForAUser.getKey();

			if (userIDAtRecomm != userID)// skip current user
			{
				// get training timeline
				LinkedHashMap<Date, Timeline> dwTrainingTimelinesForThisUser = entryForAUser.getValue().get(0);

				// Extract candidate timelines for this user as {Date,Timeline}
				Map<Date, Timeline> candTimelinesFromThisUser = TimelineExtractors.extractDaywiseCandidateTimelines(
						dwTrainingTimelinesForThisUser, dateAtRecomm, activityAtRecommPoint);

				if (candTimelinesFromThisUser.size() == 0)
				{
					continue;
				}

				// TreeMap<Date, Timeline> candidateTimelinesDateSorted = (TreeMap<Date, Timeline>)
				// candidateTimelinesDate;
				// here
				if (only1CandFromEachCollUser)
				{
					// convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>) and put in the
					// the map collecting only most recent cand from each user.
					Date mostRecentNonFutureDateInCands;

					if (onlyPastFromRecommDateInCandInColl)
					{
						do
						{
							mostRecentNonFutureDateInCands = Collections.max(candTimelinesFromThisUser.keySet());
						}
						while (mostRecentNonFutureDateInCands.after(dateAtRecomm)
								|| mostRecentNonFutureDateInCands.equals(dateAtRecomm));
					}
					else
					{
						mostRecentNonFutureDateInCands = Collections.max(candTimelinesFromThisUser.keySet());
					}

					if (VerbosityConstants.verbose)
					{
						System.out.println("Sanity check 1 Nov:\n all dates= "
								+ candTimelinesFromThisUser.keySet().toString() + "\n\nmostRecentNonFutureDateInCands="
								+ mostRecentNonFutureDateInCands.toString() + " dateAtRecomm= " + dateAtRecomm);
					}

					dwCandidateTimelines.put(userIDAtRecomm + "_" + mostRecentNonFutureDateInCands.toString(),
							candTimelinesFromThisUser.get(mostRecentNonFutureDateInCands));
				}
				else
				{ // convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>) and put in the
					// the map collecting cands from all users
					for (Entry<Date, Timeline> e : candTimelinesFromThisUser.entrySet())
					{
						dwCandidateTimelines.put(userIDAtRecomm + "_" + e.getKey(), e.getValue());
					}
					// Disabling the stream version as it seems difficult to read
					// $$ Map<String, Timeline> x = (candidateTimelinesDate.entrySet().stream()
					// $$ .collect(Collectors.toMap(e -> userIDAtRecomm + "_" + e.getKey().toString(),
					// $$ e -> e.getValue(), (v1, v2) -> v1, LinkedHashMap<String, Timeline>::new)));
					// $$dwCandidateTimelines.putAll(x);
				}
			}
		} // end of loop over users
		return dwCandidateTimelines;
	}

	/**
	 * DONE: checked and safely superceeded extractCandidateTimelines() with this.
	 * <p>
	 * Until 4 Aug 2018 the only change used to be in Daywise collaborative approach , however, since 5 Aug 2018, there
	 * is no change except few comments when compared to extractCandidateTimelines()
	 * 
	 * @param trainingTimelineOrig
	 * @param lookPastType
	 * @param dateAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCountsOrHours
	 * @param activityAtRecommPoint
	 * @param trainTestTimelinesForAllUsersOrig
	 * @param trainTimelinesAllUsersContinuous
	 * @return {candID,candTimeline}
	 */
	public static LinkedHashMap<String, Timeline> extractCandidateTimelinesV2(
			LinkedHashMap<Date, Timeline> trainingTimelineOrig, LookPastType lookPastType, Date dateAtRecomm,
			/* Time timeAtRecomm, */ String userIDAtRecomm, double matchingUnitInCountsOrHours,
			ActivityObject2018 activityAtRecommPoint,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersOrig,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous, PrimaryDimension dimensionToMatch)
	{
		LinkedHashMap<String, Timeline> candidateTimelines = null;
		LinkedHashMap<Date, Timeline> trainingTimelinesDaywise = trainingTimelineOrig;
		LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers = trainTestTimelinesForAllUsersOrig;
		// StringBuilder sb = new StringBuilder("Inside extractCandidateTimelinesV2
		// :trainTestTimelinesForAllUsers.size()= "+ trainTestTimelinesForAllUsers.size() + "\n");

		if (true)// disabled on 21 Dec for speed
		{
			if (Constant.EXPUNGE_INVALIDS_B4_RECOMM_PROCESS)
			{
				if (Constant.hasInvalidActivityNames)
				{
					trainingTimelinesDaywise = TimelineTrimmers.expungeInvalidsDayTimelines(trainingTimelineOrig);
					trainTestTimelinesForAllUsers = TimelineTrimmers
							.expungeInvalidsDayTimelinesAllUsers(trainTestTimelinesForAllUsersOrig);
					// sb.append("Expunging invalids before recommendation process: expunging training timelines");
				}
				else
				{
					// $$System.out.println("Data assumed to have no invalid act names to be expunged from training
					// timelines");
				}
			}
		}
		// //////////////////
		if (lookPastType.equals(Enums.LookPastType.Daywise))
		{
			if (Constant.collaborativeCandidates)
			{
				candidateTimelines = extractDaywiseCandidateTimelinesCollV2(dateAtRecomm, userIDAtRecomm,
						activityAtRecommPoint, trainTestTimelinesForAllUsers, Constant.only1CandFromEachCollUser,
						Constant.onlyPastFromRecommDateInCandInColl);
			}

			else
			{
				// sb.append("For Daywise: Non collaborative\n");
				// Obtain {Date,Timeline}
				LinkedHashMap<Date, Timeline> candidateTimelinesDate = TimelineExtractors
						.extractDaywiseCandidateTimelines(trainingTimelinesDaywise, dateAtRecomm,
								activityAtRecommPoint);

				// convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>)
				candidateTimelines = candidateTimelinesDate.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue(), (v1, v2) -> v1,
								LinkedHashMap<String, Timeline>::new));
			}
		}

		/// aaaaaaa
		else if (lookPastType.equals(Enums.LookPastType.ClosestTime) && Constant.ClosestTimeFilterCandidates)
		{// comment on 16 Jan 2019
			// probably this should be completely same as Daywise approach. Need to think about it later. TODO
			if (Constant.collaborativeCandidates)
			{
				candidateTimelines = extractCandidateTimelinesClosestTimeColl(activityAtRecommPoint, userIDAtRecomm,
						trainTimelinesAllUsersContinuous, 24);
				// extractCandClosestTimeColl2(userIDAtRecomm, trainTestTimelinesForAllUsers);
				// candidateTimeline = extractCandidateTimelinesMUColl(/* trainingTimeline, */
				// trainTestTimelinesForAllUsers, matchingUnitInCountsOrHours, lookPastType2,
				// activityAtRecommPoint, userIDAtRecomm, trainTimelinesAllUsersContinuous);
			}
			else
			{
				// Obtain {Date,Timeline}
				LinkedHashMap<Date, Timeline> candidateTimelinesDate = TimelineExtractors
						.extractDaywiseCandidateTimelines(trainingTimelinesDaywise, dateAtRecomm,
								activityAtRecommPoint);

				// convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>)
				candidateTimelines = candidateTimelinesDate.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue(), (v1, v2) -> v1,
								LinkedHashMap<String, Timeline>::new));
			}
		}
		// aaaaaaa
		// take all training day timelines as candidate timelines, i.e., no filtering of candidate timelines for closest
		// time approach. Decided on 16 Jan 2019 that this is more apt for baseline
		else if (lookPastType.equals(Enums.LookPastType.ClosestTime) && !Constant.ClosestTimeFilterCandidates)
		{
			if (Constant.collaborativeCandidates)
			{
				candidateTimelines = extractCandClosestTimeColl1(userIDAtRecomm, trainTestTimelinesForAllUsers);
			}
			else
			{
				// convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>)
				candidateTimelines = trainingTimelinesDaywise.entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue(), (v1, v2) -> v1,
								LinkedHashMap<String, Timeline>::new));

				Sanity.eq(candidateTimelines.size(), trainingTimelinesDaywise.size(),
						"candidateTimelines.size() = " + candidateTimelines.size()
								+ "!= trainingTimelinesDaywise.size() = " + trainingTimelinesDaywise.size());
			}
		}
		else if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours))
		{
			LinkedHashMap<String, TimelineWithNext> candidateTimelinesWithNext = null;

			if (Constant.collaborativeCandidates)
			{
				// sb.append("For NCount NHours: collaborative\n");
				candidateTimelinesWithNext = TimelineExtractors.extractCandidateTimelinesMUColl(/* trainingTimeline, */
						/* trainTestTimelinesForAllUsers, */ matchingUnitInCountsOrHours, lookPastType,
						activityAtRecommPoint, userIDAtRecomm, trainTimelinesAllUsersContinuous, dimensionToMatch);
			}
			else
			{
				// sb.append("For NCount NHours: Non collaborative\n");
				// converting day timelines into continuous timelines suitable to be used for matching unit views
				Timeline trainingTimeline = TimelineTransformers.dayTimelinesToATimeline(trainingTimelinesDaywise,
						false, true);
				// Obtain {String,TimelineWithNext}

				candidateTimelinesWithNext = TimelineExtractors.extractCandidateTimelinesMU(trainingTimeline,
						matchingUnitInCountsOrHours, lookPastType, activityAtRecommPoint);
			}

			// convert to {String,Timeline}
			candidateTimelines = candidateTimelinesWithNext.entrySet().stream().collect(Collectors
					.toMap(e -> e.getKey(), e -> e.getValue(), (v1, v2) -> v1, LinkedHashMap<String, Timeline>::new));
		}

		///
		else if (lookPastType.equals(Enums.LookPastType.NGram))
		{
			LinkedHashMap<String, TimelineWithNext> candidateTimelinesWithNext = null;

			if (Constant.collaborativeCandidates)
			{
				// sb.append("For NGram: collaborative\n");
				candidateTimelinesWithNext = TimelineExtractors.extractCandidateTimelinesMUColl(/* trainingTimeline, */
						/* trainTestTimelinesForAllUsers, */ 0, lookPastType, activityAtRecommPoint, userIDAtRecomm,
						trainTimelinesAllUsersContinuous, dimensionToMatch);
			}
			else
			{
				// sb.append("For NGram: Non collaborative\n");
				PopUps.printTracedErrorMsgWithExit(
						"Error: not implemented for this case here, instead look at the dedicated Recomm test for NGram.");
			}

			// convert to {String,Timeline}
			candidateTimelines = candidateTimelinesWithNext.entrySet().stream().collect(Collectors
					.toMap(e -> e.getKey(), e -> e.getValue(), (v1, v2) -> v1, LinkedHashMap<String, Timeline>::new));
		}
		///
		else
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: Unrecognised lookPastType "));
			System.exit(-1);
		}

		// ////////////////////
		if (candidateTimelines == null)// || candidateTimelines.size() < 1)
		{
			System.out.println(PopUps.getCurrentStackTracedWarningMsg("Warning: candidate timeline is empty"));
			// this.errorExists = true;
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println("Inside extractCandidateTimelinesV2(): trainTestTimelinesForAllUsers.size() = "
					+ trainTestTimelinesForAllUsers.size());
		}
		// if (VerbosityConstants.tempVerbose)
		// {
		// System.out.println(sb.toString());
		// }
		return candidateTimelines;
	}

	/**
	 * created through trefactoring: extracted from calling method on 5 Aug 2018. so the logic existed inside the
	 * calling method earlier and now has been extracted as a method.
	 * 
	 * @param dateAtRecomm
	 * @param userIDAtRecomm
	 * @param activityAtRecommPoint
	 * @param trainTestTimelinesForAllUsers
	 * @return {UserID_Date, CandTimeline}
	 *         <p>
	 *         <b> key as UserID_Date instead of Date since we should allow cands from different users but same date
	 *         </b>
	 * @since Aug 5 2018
	 */
	public static LinkedHashMap<String, Timeline> extractDaywiseCandidateTimelinesCollV2(Date dateAtRecomm,
			String userIDAtRecomm, ActivityObject2018 activityAtRecommPoint,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			boolean only1CandFromEachCollUser, boolean onlyPastFromRecommDateInCandInColl)
	{
		LinkedHashMap<String, Timeline> dwCandidateTimelines = new LinkedHashMap<>();

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> entryForAUser : trainTestTimelinesForAllUsers
				.entrySet())// go over all users
		{
			String userID = entryForAUser.getKey();
			if (userIDAtRecomm != userID)// skip current user
			{
				// get training timeline
				LinkedHashMap<Date, Timeline> dwTrainingTimelinesForThisUser = entryForAUser.getValue().get(0);

				// Extract candidate timelines for this user as {Date,Timeline}
				Map<Date, Timeline> candTimelinesFromThisUser = TimelineExtractors.extractDaywiseCandidateTimelines(
						dwTrainingTimelinesForThisUser, dateAtRecomm, activityAtRecommPoint);

				if (candTimelinesFromThisUser.size() == 0)
				{
					continue;
				}

				// TreeMap<Date, Timeline> candidateTimelinesDateSorted = (TreeMap<Date, Timeline>)
				// candidateTimelinesDate;
				// here
				if (only1CandFromEachCollUser)
				{
					// convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>) and put in the
					// the map collecting only most recent cand from each user.
					Date mostRecentNonFutureDateInCands;

					if (onlyPastFromRecommDateInCandInColl)
					{
						do
						{
							mostRecentNonFutureDateInCands = Collections.max(candTimelinesFromThisUser.keySet());
						}
						while (mostRecentNonFutureDateInCands.after(dateAtRecomm)
								|| mostRecentNonFutureDateInCands.equals(dateAtRecomm));
					}
					else
					{
						mostRecentNonFutureDateInCands = Collections.max(candTimelinesFromThisUser.keySet());
					}

					if (VerbosityConstants.verbose)
					{
						System.out.println("Sanity check 1 Nov:\n all dates= "
								+ candTimelinesFromThisUser.keySet().toString() + "\n\nmostRecentNonFutureDateInCands="
								+ mostRecentNonFutureDateInCands.toString() + " dateAtRecomm= " + dateAtRecomm);
					}
					// mostRecentNonFutureDateInCands
					dwCandidateTimelines.put(userIDAtRecomm + "_" + mostRecentNonFutureDateInCands.toString(),
							candTimelinesFromThisUser.get(mostRecentNonFutureDateInCands));
				}
				else if (Constant.numOfCandsFromEachCollUser > 0)// Daywise
				{
					// select only the most recent numOfCandsFromEachCollUser candidates.
					List<Date> mostRecentReqDates = candTimelinesFromThisUser.keySet().stream().sorted()
							.skip(candTimelinesFromThisUser.size() - Constant.numOfCandsFromEachCollUser <= 0 ? 0
									: candTimelinesFromThisUser.size() - Constant.numOfCandsFromEachCollUser)
							.collect(Collectors.toList());

					Map<String, Timeline> x = new LinkedHashMap<>();
					mostRecentReqDates.stream().forEachOrdered(λ -> x.put(λ + "", candTimelinesFromThisUser.get(λ)));

					// Map<String, Timeline> x = (candidateTimelinesDate.entrySet().stream().sorted()
					// .skip(candidateTimelinesDate.size() - Constant.numOfCandsFromEachCollUser)
					// .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue(),
					// (v1, v2) -> v1, LinkedHashMap<String, Timeline>::new)));

					//
					if (false)// sanity check
					{
						StringBuilder sb1 = new StringBuilder();
						sb1.append("Sanity check 17 Nov 2017");
						sb1.append("all candidateTimelinesDate for user:" + userID + "\n");
						candTimelinesFromThisUser.entrySet().stream()
								.forEachOrdered(e -> sb1.append(e.getKey() + "-" + e.getValue() + "\n"));
						sb1.append("chosen candidateTimelinesDate for user:" + userID
								+ "Constant.numOfCandsFromEachCollUser= " + Constant.numOfCandsFromEachCollUser + "\n");
						x.entrySet().stream().forEachOrdered(e -> sb1.append(e.getKey() + "-" + e.getValue() + "\n"));
						System.out.println(sb1 + "");
					}
					//
					dwCandidateTimelines.putAll(x);
				}
				else
				{
					// convert to {Date as String, Timeline} to (LinkedHashMap<String, Timeline>) and put in the
					// the map collecting cands from all users
					for (Entry<Date, Timeline> e : candTimelinesFromThisUser.entrySet())
					{
						dwCandidateTimelines.put(userIDAtRecomm + "_" + e.getKey(), e.getValue());
					}
					// dwCandidateTimelines.putAll(candidateTimelinesDate.entrySet().stream()
					// .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue(),
					// (v1, v2) -> v1, LinkedHashMap<String, Timeline>::new)));

					// Disabling the stream version as it seems difficult to read
					// $$Map<String, Timeline> x = (candTimelinesFromThisUser.entrySet().stream()
					// $$ .collect(Collectors.toMap(e -> e.getKey().toString(),
					// $$e -> e.getValue(), (v1, v2) -> v1,LinkedHashMap<String, Timeline>::new)));
					// $$dwCandidateTimelines.putAll(x); // doing it in two steps, because gradle gave compilation
					// error in one step.
				}
			}
		} // end of loop over users
		return dwCandidateTimelines;
	}

	/**
	 * Take all training day timelines as candidate timelines, i.e., no filtering of candidate timelines for closest
	 * time approach
	 * 
	 * @param userIDAtRecomm
	 * @param trainTestTimelinesForAllUsers
	 * @return Map{userID__DateAsString,cand timeline} where cand timelines are all the training day timelines from
	 *         other users except current user
	 */
	private static LinkedHashMap<String, Timeline> extractCandClosestTimeColl1(String userIDAtRecomm,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers)
	{
		LinkedHashMap<String, Timeline> candidateTimelines = new LinkedHashMap<>();
		// all training timelines of all other users to be considered as candidate timelines.
		int numOfTrainingTimelinesForAllOtherUsers = 0;// for sanity check

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsers
				.entrySet())
		{
			String userIdCursor = trainTestForAUser.getKey();
			LinkedHashMap<Date, Timeline> trainingTimelineForThisUserDate = trainTestForAUser.getValue().get(0);

			if (!userIDAtRecomm.equals(userIdCursor))// exclude the current user.
			{
				numOfTrainingTimelinesForAllOtherUsers += trainingTimelineForThisUserDate.size();

				// convert to {Date as String, Timeline} to (LinkedHashMap<userID__DateAsString, Timeline>)
				LinkedHashMap<String, Timeline> candidateTimelinesFromThisUser = trainingTimelineForThisUserDate
						.entrySet().stream().collect(Collectors.toMap(e -> userIdCursor + "__" + e.getKey().toString(),
								e -> e.getValue(), (v1, v2) -> v1, LinkedHashMap<String, Timeline>::new));
				candidateTimelines.putAll(candidateTimelinesFromThisUser);
			}
		}
		// Verbosity start for sanity check
		System.out.println("candidateTimelines.size() = " + candidateTimelines.size()
				+ " numOfTrainingTimelinesForAllOtherUsers = " + numOfTrainingTimelinesForAllOtherUsers);

		if (VerbosityConstants.verbose)
		{
			StringBuilder sbT = new StringBuilder();
			sbT.append("Candidate timelines =\n");
			for (Entry<String, Timeline> a : candidateTimelines.entrySet())
			{
				sbT.append("\n" + a.getKey() + ": \n" + a.getValue().getActivityObjectNamesInSequence());
			}
			System.out.println(sbT.toString());
		}
		// Verbosity end for sanity check

		Sanity.eq(candidateTimelines.size(), numOfTrainingTimelinesForAllOtherUsers,
				"candidateTimelines.size() = " + candidateTimelines.size()
						+ "!= numOfTrainingTimelinesForAllOtherUsers = " + numOfTrainingTimelinesForAllOtherUsers);
		return candidateTimelines;
	}

	////
	///
	/**
	 * For each ocurrence of current act in training timeline, one candidate is formed including all AOs in
	 * hoursExtension before and after it.
	 * <p>
	 * Create and fetch candidate timelines from the training timelines of other users. Finding Candidate timelines: for
	 * each other user, iterate through the training timelines for the occurence of the Current Activity Name in the
	 * candidate timeline, extract the sequence of activity objects from that occurrence_index until hoursExtension on
	 * either side.
	 * 
	 * 
	 * .In this way, we will have a NUMBER OF or only 1 (if Constant.only1CandFromEachCollUser is true) candidate
	 * timelines from each user who have atleast one non-last occurrence of Current Activity Names in their training
	 * timelines
	 * 
	 * @param matchingUnitInCounts
	 * @param activityAtRecommPoint
	 * @param userIDAtRecomm
	 * @param trainTimelinesAllUsersContinuous
	 * @return
	 * @since 15Aug 2017
	 */
	private static LinkedHashMap<String, Timeline> extractCandidateTimelinesClosestTimeColl(
			ActivityObject2018 activityAtRecommPoint, String userIDAtRecomm,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous, int hoursExtension)
	{
		LinkedHashMap<String, Timeline> candidateTimelines = new LinkedHashMap<>();
		// long tS = System.nanoTime();

		System.out.println("\nInside extractCandidateTimelinesClosestTimeColl(): userIDAtRecomm=" + userIDAtRecomm);
		// // // for // creating // timelines");
		// System.out.println("Inside extractCandidateTimelinesMUCountColl :trainTestTimelinesForAllUsers.size()= "
		// + trainTestTimelinesForAllUsers.size());
		// System.out.println("activityAtRecommPoint:" + activityAtRecommPoint.getPrimaryDimensionVal("/"));
		// totalNumberOfProbableCands=0; numCandsRejectedDueToNoCurrentActivityAtNonLast=0;
		int numCandsRejectedDueToNoValidNextActivity = 0;
		// long numOfAOsCompared = 0;
		int numUsersRejectedDueToNoValidCands = 0;

		// for user non-current user, get the training timelines
		for (Entry<String, Timeline> trainTimelineAUser : trainTimelinesAllUsersContinuous.entrySet())
		{
			String userIdCursor = trainTimelineAUser.getKey();

			if (!userIDAtRecomm.equals(userIdCursor))// exclude the current user.
			{
				Timeline trainingTimelineForThisUser = trainTimelineAUser.getValue();

				int numOfValidCurrentActsEncountered = 0;
				// System.out.println("userIDAtRecomm=" + userIDAtRecomm + " userIdCursor=" + userIdCursor);
				// find the most recent occurrence of current activity name
				// get training timeline
				ArrayList<ActivityObject2018> activityObjectsInTraining = trainingTimelineForThisUser
						.getActivityObjectsInTimeline();

				// System.out.println("Num of activity objects in training timeline=" +
				// activityObjectsInTraining.size());

				// $$System.out.println("Current activity (activityAtRecommPoint)=" +
				// this.activityAtRecommPoint.getActivityName());

				// System.out.println("cand:" + trainingTimelineForThisUser.getPrimaryDimensionValsInSequence());

				// long t1 = System.currentTimeMillis();
				// starting from the second last activity and goes until first activity.
				for (int i = activityObjectsInTraining.size() - 2; i >= 0; i--)
				// for (int i = 0; i < activityObjectsInTraining.size() - 1; i++)
				{
					ActivityObject2018 ae = activityObjectsInTraining.get(i);
					// numOfAOsCompared += 1;
					// System.out.println("ae = " + ae.getPrimaryDimensionVal("/"));
					// System.out.println("activityAtRecommPoint:" + activityAtRecommPoint.getPrimaryDimensionVal("/"));
					// start sanity check for equalsWrtPrimaryDimension() //Disabled as already checked in earlier
					// methods
					// if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
					// {boolean a = ae.equalsWrtPrimaryDimension(activityAtRecommPoint);
					// boolean b = ae.getActivityName().equals(activityAtRecommPoint.getActivityName());
					// Sanity.eq(a, b,
					// "\nactivityAtRecommPoint=" + activityAtRecommPoint.toStringAllGowallaTS() + "\nae = "
					// + ae.toStringAllGowallaTS() + "\nae.pdvals = " + ae.getPrimaryDimensionVal("/")
					// // + "\nactivityAtRecommPoint=" + activityAtRecommPoint.toStringAllGowallaTS()
					// + "\nae.equalsWrtPrimaryDimension(activityAtRecommPoint) =" + a
					// + " != ae.getActivityName().equals(activityAtRecommPoint.getActivityName()) ="
					// + b + "\n");}
					// end sanity check

					if (ae.equalsWrtPrimaryDimension(activityAtRecommPoint)) // same name as current activity)
					{
						// Timestamp newCandEndTimestamp= new
						// Timestamp(ae.getStartTimestamp().getTime()+ae.getDurationInSeconds()*1000-1000); //decreasing
						// 1 second (because this is convention followed in data generation)
						// int newCandAnchorIndex = i;// newCandEndIndex
						// NOTE: going back matchingUnitCounts FROM THE index.

						// take the min of end timestamp as 24 hrs extension and end timestamp of seconds last AO in
						// training
						long newEndTSInms = ae.getEndTimestampInms() + (hoursExtension * 60 * 60 * 1000);
						long endTSOfSecondLastAOInTraining = activityObjectsInTraining
								.get(activityObjectsInTraining.size() - 1).getEndTimestampInms();

						Timestamp newEndTS = new Timestamp(Math.min(newEndTSInms, endTSOfSecondLastAOInTraining));
						Timestamp newStartTS = new Timestamp(
								ae.getEndTimestampInms() - (hoursExtension * 60 * 60 * 1000));

						ArrayList<ActivityObject2018> activityObjectsForCandidate = trainingTimelineForThisUser
								.getActivityObjectsBetweenTime(newStartTS, newEndTS);

						// int newCandStartIndex = (newCandAnchorIndex - matchingUnitInCounts) >= 0
						// ? (newCandAnchorIndex - matchingUnitInCounts)
						// : 0;

						// ArrayList<ActivityObject> activityObjectsForCandidate = trainingTimelineForThisUser
						// .getActivityObjectsInTimelineFromToIndex(newCandStartIndex, newCandAnchorIndex + 1);
						// getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp);

						// next activity after last activity on AOs for this candidate
						// ActivityObject nextValidActivityForCandidate = trainingTimelineForThisUser
						// .getNextValidActivityAfterActivityAtThisTime(activityObjectsForCandidate
						// .get(activityObjectsForCandidate.size() - 1).getEndTimestamp());

						// if (nextValidActivityForCandidate == null)
						// {
						// numCandsRejectedDueToNoValidNextActivity += 1;
						// System.out.println("\tThis candidate rejected due to no next valid activity object;");
						// // if (i == 0)// this was first activity of the timeline
						// // { System.out.println("\tThis iser rejected due to no next valid cand");
						// // numUsersRejectedDueToNoValidCands += 1;break;
						// // } else{
						// continue;
						// // }
						// }
						// else
						// {
						numOfValidCurrentActsEncountered += 1;
						Timeline newCandidate = new Timeline(activityObjectsForCandidate, false, true);// trainingTimeline.getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp));
						// System.out.println(
						// "Created new candidate timeline (with next) from user (" + userIdCursor + ")");
						// System.out.println("\tActivity names:" +
						// newCandidate.getActivityObjectNamesInSequence());
						// System.out.println(
						// "\tNext activity:" + newCandidate.getNextActivityObject().getActivityName());
						candidateTimelines.put(userIdCursor + "__" + newCandidate.getTimelineID(), newCandidate);

						if (VerbosityConstants.verbose)
						{
							System.out.println("\n\tae.getEndTimestampInms()=" + ae.getEndTimestamp());
							System.out.println("\tnewEndTS=" + newEndTS);
							System.out.println("\tnewStartTS=" + newStartTS);
							System.out.println(
									"\tactivityObjectsForCandidate.size()=" + activityObjectsForCandidate.size());
							System.out.println("userIdCursor + \"__\" + newCandidate.getTimelineID()= " + userIdCursor
									+ "__" + newCandidate.getTimelineID());
						}

						if (Constant.only1CandFromEachCollUser)
						{
							break; // we only take one candidate timelines from each other user
						}
						// }
					} // end of act name match
				} // end of loop over acts in train timeline

				if (numOfValidCurrentActsEncountered == 0)
				{
					// $$ DIsabled for performance
					// $$System.out.println("\tU:" + userIdCursor + " rejected due to no cur act");
					numUsersRejectedDueToNoValidCands += 1;
				}
			} // end of if user id matches
		} // end of loop over users

		// long t2 = System.nanoTime();
		// System.out.println("total numOfAOsCompared = " + numOfAOsCompared);
		// System.out.println("compared " + numOfAOsCompared + "(t2 - t1)=" + (t2 - tS) + " AOS: avg time "
		// + ((t2 - tS) * 1.0) / numOfAOsCompared + "ns");

		System.out.println("trainTimelinesAllUsersContinuous.size() = " + trainTimelinesAllUsersContinuous.size()
				+ "\tcandidateTimelines.size() = " + candidateTimelines.size()
				+ "\tnumUsersRejectedDueToNoValidCands = " + numUsersRejectedDueToNoValidCands
				+ "\tnumCandsRejectedDueToNoValidNextActivity=" + numCandsRejectedDueToNoValidNextActivity);

		if (Constant.only1CandFromEachCollUser)
		{
			Sanity.eq(trainTimelinesAllUsersContinuous.size() - 1,
					(candidateTimelines.size() + numUsersRejectedDueToNoValidCands),
					"trainTestTimelinesForAllUsers.size()!= (candidateTimelines.size() + numUsersRejectedDueToNoValidCands)");
		}
		// System.out.println("\n Exiting extractCandidateTimelinesMUCountColl(): userIDAtRecomm=" + userIDAtRecomm);//
		// for
		return candidateTimelines;
	}

	/**
	 * Find Candidate timelines, which are the timelines which contain the activity at the recommendation point (current
	 * Activity). Also, this candidate timeline must contain the activityAtRecomm point at non-last position and there
	 * is atleast one valid activity after this activityAtRecomm point
	 * 
	 * <p>
	 * converted to a static method on Dec 5 2016
	 * <p>
	 * 
	 * 
	 * @param dayTimelinesForUser
	 * @param dateAtRecomm
	 * @param activityAtRecommPoint
	 * @return
	 */
	public static LinkedHashMap<Date, Timeline> extractDaywiseCandidateTimelines(
			LinkedHashMap<Date, Timeline> dayTimelinesForUser, // ArrayList<ActivityObject> activitiesGuidingRecomm,
			Date dateAtRecomm, ActivityObject2018 activityAtRecommPoint)
	{
		LinkedHashMap<Date, Timeline> candidateTimelines = new LinkedHashMap<>();
		int count = 0;
		int totalNumberOfProbableCands = 0;
		int numCandsRejectedDueToNoCurrentActivityAtNonLast = 0;
		int numCandsRejectedDueToNoNextActivity = 0;

		for (Map.Entry<Date, Timeline> dayTimelineEntry : dayTimelinesForUser.entrySet())
		{
			totalNumberOfProbableCands += 1;
			Date dayOfTimeline = dayTimelineEntry.getKey();
			Timeline dayTimeline = dayTimelineEntry.getValue();

			if (!dayTimeline.isShouldBelongToSingleDay())
			{// sanity checking if its a day timeline
				// System.err.println(PopUps.getTracedErrorMsg(
				PopUps.printTracedErrorMsg(
						"Error in extractDaywiseCandidateTimelines: dayTimeline.isShouldBelongToSingleDay()="
								+ dayTimeline.isShouldBelongToSingleDay());
			}

			// start sanity check for countContainsPrimaryDimensionValButNotAsLast()
			if (VerbosityConstants.checkSanityPDImplementn
					&& Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
			{
				int a = dayTimeline.countContainsActivityNameButNotAsLast(activityAtRecommPoint.getActivityName());
				int b = dayTimeline
						.countContainsPrimaryDimensionValButNotAsLast(activityAtRecommPoint.getPrimaryDimensionVal());
				Sanity.eq(a, b, "\nactivityAtRecommPoint=" + activityAtRecommPoint.toStringAllGowallaTS()
						+ "\ndayTimeline=" + dayTimeline.getActivityObjectNamesInSequence()
						+ "\ndayTimeline.countContainsActivityNameButNotAsLast(actAtRecommPoint.getActivityName()) = "
						+ a
						+ " != dayTimeline.countContainsPrimaryDimensionValButNotAsLast(actAtRecommPoint.getPrimaryDimensionVal())"
						+ b);
			}
			// end sanity check

			// Check if the timeline contains the activityAtRecomm point at non-last and the timeline is not same for
			// the day to be recommended (this should not be the case because test and training set are diffferent)
			// and there is atleast one valid activity after this activityAtRecomm point
			// Constant.ensureCandEndWithCurrAct == false ||
			if (dayTimeline
					.countContainsPrimaryDimensionValButNotAsLast(activityAtRecommPoint.getPrimaryDimensionVal()) > 0)
			// disabled on 11 Jul'16
			// if(dayTimeline.countContainsActivityNameButNotAsLast(activityAtRecommPoint.getActivityName()) > 0)
			// && (entry.getKey().toString().equals(dateAtRecomm.toString())==false))
			{
				// if ((Constant.noFutureCandInColl == true)
				if ((dayOfTimeline.equals(dateAtRecomm)) || (dayOfTimeline.after(dateAtRecomm)))
				{
					if (Constant.onlyPastFromRecommDateInCandInColl && (Constant.collaborativeCandidates))
					{
						// $ System.out.println("Ignoring:dayOfTimeline=" + dayOfTimeline + " dateAtRecomm =" +
						// dateAtRecomm
						// $ + " a prospective candidate timelines is of the same or after the date as the
						// dateToRecommend.");
						continue;
					}
					else // allowing future cand extraction here be CAREFUL
					{
						// TODO:TEMp Disable//$$ System.out.println("Warning in extractDaywiseCandidateTimelines
						// :dayOfTimeline=" + dayOfTimeline
						// $$ + " dateAtRecomm =" + dateAtRecomm
						// $$ + " a prospective candidate timelines is of the same or after the date as the
						// dateToRecommend. Thus, not using training and test set correctly");
					}
				}

				// start sanity check for hasAValidActAfterFirstOccurOfThisPrimaryDimensionVal()
				if (VerbosityConstants.checkSanityPDImplementn
						&& Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					boolean a = dayTimeline
							.hasAValidActAfterFirstOccurOfThisActName(activityAtRecommPoint.getActivityName());
					boolean b = dayTimeline.hasAValidActAfterFirstOccurOfThisPrimaryDimensionVal(
							activityAtRecommPoint.getPrimaryDimensionVal());
					Sanity.eq(a, b,
							"dayTimeline.hasAValidActAfterFirstOccurOfThisActName(activityAtRecommPoint.getActivityName()) = "
									+ a
									+ " != dayTimeline.hasAValidActAfterFirstOccurOfThisPrimaryDimensionVal(actAtRecommPoint.getPrimaryDimensionVal())"
									+ b);
				}
				// end sanity check

				if (dayTimeline.getActivityObjectsInTimeline().size() > 1
						&& dayTimeline.hasAValidActAfterFirstOccurOfThisPrimaryDimensionVal(
								activityAtRecommPoint.getPrimaryDimensionVal()))
				// .hasAValidActAfterFirstOccurOfThisActName(activityAtRecommPoint.getActivityName()))
				// if (dayTimeline.containsOnlySingleActivity() == false && dayTimeline
				// .hasAValidActivityAfterFirstOccurrenceOfThisActivity(activityAtRecommPoint) == true)
				{
					candidateTimelines.put(dayOfTimeline, dayTimeline);
					count++;
				}
				else
				{
					numCandsRejectedDueToNoNextActivity += 1;
				}
			}
			else
			{
				numCandsRejectedDueToNoCurrentActivityAtNonLast += 1;
			}
		}

		if (VerbosityConstants.verbose)// || VerbosityConstants.tempVerbose)
		{
			System.out.println("Inside extractDaywiseCandidateTimelines:dayTimelinesForUser.size()="
					+ dayTimelinesForUser.size() + " #cand timelines = " + count
					+ " numCandsRejectedDueToNoNextActivity =" + numCandsRejectedDueToNoNextActivity
					+ "  numCandsRejectedDueToNoCurrentActivityAtNonLast ="
					+ numCandsRejectedDueToNoCurrentActivityAtNonLast);
		}

		if (VerbosityConstants.printSanityCheck)
		{
			if (count == 0)
			{
				System.err.println("Warning: No DaywiseCandidateTimelines found");
			}
		}
		return candidateTimelines;
	}

	/**
	 * Returns candidate timelines extracted from the training timeline.
	 * 
	 * @param trainingTimeline
	 * @param matchingUnit
	 * @param lookPastType
	 * @param activityAtRecommPoint
	 * @return
	 */
	public static LinkedHashMap<String, TimelineWithNext> extractCandidateTimelinesMU(Timeline trainingTimeline,
			double matchingUnit, Enums.LookPastType lookPastType, ActivityObject2018 activityAtRecommPoint)
	{
		if (lookPastType.equals(Enums.LookPastType.NCount))// IgnoreCase("Count"))
		{
			if (matchingUnit % 1 != 0)
			{
				System.out.println("Warning: matching unit" + matchingUnit
						+ " is not integer while the lookPastType is Count. We will use the integer value.");
			}
			return extractCandidateTimelinesMUCount(trainingTimeline, new Double(matchingUnit).intValue(),
					activityAtRecommPoint);
		}

		else if (lookPastType.equals(Enums.LookPastType.NHours))// .equalsIgnoreCase("Hrs"))
		{
			return extractCandidateTimelinesMUHours(trainingTimeline, matchingUnit, activityAtRecommPoint);
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in getCandidateTimelinesMU: Unrecognised matching unit type " + lookPastType));
			System.exit(-2);
			return null;
		}
	}

	/**
	 * 
	 * @param matchingUnit
	 * @param lookPastType
	 * @param activityAtRecommPoint
	 * @param userIDAtRecomm
	 * @param trainTimelinesAllUsersContinuous
	 * @param dimensionToMatch
	 * @return
	 */
	public static LinkedHashMap<String, TimelineWithNext> extractCandidateTimelinesMUColl(
			// LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			double matchingUnit, LookPastType lookPastType, ActivityObject2018 activityAtRecommPoint,
			String userIDAtRecomm, LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous,
			PrimaryDimension dimensionToMatch)
	{
		System.out.println("Inside extractCandidateTimelinesMUColl : mu=" + matchingUnit + " dimensionToMatch="
				+ dimensionToMatch);
		// trainTestTimelinesForAllUsers.size()= "+ trainTestTimelinesForAllUsers.size() + "

		if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NGram))// IgnoreCase("Count"))
		{
			if (matchingUnit % 1 != 0)
			{
				System.out.println("Warning extractCandidateTimelinesMUColl: matching unit" + matchingUnit
						+ " is not integer while the lookPastType is Count. We will use the integer value.");
			}

			// return extractCandidateTimelinesMUCountColl(trainingTimeline, trainTestTimelinesForAllUsers,
			// new Double(matchingUnit).intValue(), activityAtRecommPoint, userIDAtRecomm);

			return extractCandidateTimelinesMUCountHoursColl(/* trainingTimeline, trainTestTimelinesForAllUsers, */
					new Double(matchingUnit).intValue(), activityAtRecommPoint, userIDAtRecomm,
					trainTimelinesAllUsersContinuous, dimensionToMatch, true);
		}
		// TODO: implement MU hours
		else if (lookPastType.equals(Enums.LookPastType.NHours))// .equalsIgnoreCase("Hrs"))
		{
			// return extractCandidateTimelinesMUHours(trainingTimeline, matchingUnit, activityAtRecommPoint);
			return extractCandidateTimelinesMUCountHoursColl(/* trainingTimeline, trainTestTimelinesForAllUsers, */
					new Double(matchingUnit).intValue(), activityAtRecommPoint, userIDAtRecomm,
					trainTimelinesAllUsersContinuous, dimensionToMatch, false);
		}
		// if (lookPastType.equals(Enums.LookPastType.ClosestTime))// IgnoreCase("Count"))
		// {
		// return extractCandidateTimelinesClosestTimeColl(/* trainingTimeline, trainTestTimelinesForAllUsers, */
		// new Double(matchingUnit).intValue(), activityAtRecommPoint, userIDAtRecomm,
		// trainTimelinesAllUsersContinuous, 24);
		// }

		else
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in extractCandidateTimelinesMUColl: Unrecognised matching unit type " + lookPastType));
			System.exit(-2);
			return null;
		}
	}

	/**
	 * Returns candidate timelines extracted from the training timeline.
	 * 
	 * @param trainingTimeline
	 * @param matchingUnit
	 * @param lookPastType
	 * @param activityAtRecommPoint
	 * @return
	 * @deprecated (most probably (realized on 7 March 2019))
	 */
	public static LinkedHashMap<String, TimelineWithNext> extractCandidateTimelinesMUColl(Timeline trainingTimeline,
			double matchingUnit, Enums.LookPastType lookPastType, ActivityObject2018 activityAtRecommPoint)
	{
		if (lookPastType.equals(Enums.LookPastType.NCount))// IgnoreCase("Count"))
		{
			if (matchingUnit % 1 != 0)
			{
				System.out.println("Warning: matching unit" + matchingUnit
						+ " is not integer while the lookPastType is Count. We will use the integer value.");
			}
			return extractCandidateTimelinesMUCount(trainingTimeline, new Double(matchingUnit).intValue(),
					activityAtRecommPoint);
		}

		else if (lookPastType.equals(Enums.LookPastType.NHours))// .equalsIgnoreCase("Hrs"))
		{
			return extractCandidateTimelinesMUHours(trainingTimeline, matchingUnit, activityAtRecommPoint);
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in getCandidateTimelinesMU: Unrecognised matching unit type " + lookPastType));
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
	public static LinkedHashMap<String, TimelineWithNext> extractCandidateTimelinesMUCount(Timeline trainingTimeline,
			int matchingUnitInCounts, ActivityObject2018 activityAtRecommPoint)
	// ArrayList<ActivityObject>// activitiesGuidingRecomm,*/// //Date//dateAtRecomm)
	{
		int count = 0;
		// int matchingUnitInCounts = (int) this.matchingUnitInCountsOrHours;
		LinkedHashMap<String, TimelineWithNext> candidateTimelines = new LinkedHashMap<>();

		// $$System.out.println("\nInside getCandidateTimelines()");// for creating timelines");
		// totalNumberOfProbableCands=0;
		// numCandsRejectedDueToNoCurrentActivityAtNonLast=0;
		int numCandsRejectedDueToNoValidNextActivity = 0;
		ArrayList<ActivityObject2018> activityObjectsInTraining = trainingTimeline.getActivityObjectsInTimeline();
		// $$System.out.println("Number of activity objects in training timeline=" + activityObjectsInTraining.size());
		// $$System.out.println("Current activity (activityAtRecommPoint)=" +
		// this.activityAtRecommPoint.getActivityName());
		// trainingTimeline.printActivityObjectNamesWithTimestampsInSequence();

		/**
		 * Note: candidate timelines can be formed from the first index of the training timeline UNLIKE matching unit in
		 * hours
		 */
		// starting from the first activity and goes until second last activity.
		for (int i = 0; i < activityObjectsInTraining.size() - 1; i++)
		{
			ActivityObject2018 ae = activityObjectsInTraining.get(i);

			// start sanity check for equalsWrtPrimaryDimension() //can be removed after check
			if (VerbosityConstants.checkSanityPDImplementn
					&& Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
			{
				boolean a = ae.equalsWrtPrimaryDimension(activityAtRecommPoint);
				boolean b = ae.getActivityName().equals(activityAtRecommPoint.getActivityName());
				Sanity.eq(a, b, "\nactivityAtRecommPoint=" + activityAtRecommPoint.toStringAllGowallaTS() + "\nae = "
						+ ae.toStringAllGowallaTS() + "\nae.pdvals = " + ae.getPrimaryDimensionVal("/")
						// + "\nactivityAtRecommPoint=" + activityAtRecommPoint.toStringAllGowallaTS()
						+ "\nae.equalsWrtPrimaryDimension(activityAtRecommPoint) =" + a
						+ " != ae.getActivityName().equals(activityAtRecommPoint.getActivityName()) =" + b + "\n");
			}
			// end sanity check

			// same name as current activity)//added 11 Jan2019
			if (Constant.ensureCandEndWithCurrAct == false || ae.equalsWrtPrimaryDimension(activityAtRecommPoint))
			// if (ae.equalsWrtPrimaryDimension(activityAtRecommPoint)) // same name as current activity)
			// ae.getActivityName().equals(activityAtRecommPoint.getActivityName())) // same name as current
			// activity
			{
				// Timestamp newCandEndTimestamp= new
				// Timestamp(ae.getStartTimestamp().getTime()+ae.getDurationInSeconds()*1000-1000); //decreasing 1
				// second (because this is convention followed in data generation)
				int newCandEndIndex = i;
				// NOTE: going back matchingUnitCounts FROM THE index.
				int newCandStartIndex = (newCandEndIndex - matchingUnitInCounts) >= 0
						? (newCandEndIndex - matchingUnitInCounts)
						: 0;

				// $$System.out.println("\n\tStart index of candidate timeline=" + newCandStartIndex);
				// $$System.out.println("\tEnd index of candidate timeline=" + newCandEndIndex);

				ArrayList<ActivityObject2018> activityObjectsForCandidate = trainingTimeline
						.getActivityObjectsInTimelineFromToIndex(newCandStartIndex, newCandEndIndex + 1);
				// getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp);
				ActivityObject2018 nextValidActivityForCandidate = trainingTimeline
						.getNextValidActivityAfterActivityAtThisPositionPD(newCandEndIndex);

				if (nextValidActivityForCandidate == null)
				{
					numCandsRejectedDueToNoValidNextActivity += 1;
					System.out.println("\tThis candidate rejected due to no next valid activity object;");
					continue;
				}
				TimelineWithNext newCandidate = new TimelineWithNext(activityObjectsForCandidate,
						nextValidActivityForCandidate, false, true);// trainingTimeline.getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp));
				// $$System.out.println("Created new candidate timeline (with next)");
				// $$System.out.println("\tActivity names:" + newCandidate.getActivityObjectNamesInSequence());
				// $$System.out.println("\tNext activity:" + newCandidate.getNextActivityObject().getActivityName());
				candidateTimelines.put(newCandidate.getTimelineID(), newCandidate);
			}
		}
		return candidateTimelines;
	}

	///
	/**
	 * Create and fetch candidate timelines from the training timelines of other users. Finding Candidate timelines: for
	 * each other user, iterate through the training timelines for the occurence of the Current Activity Name in the
	 * candidate timeline, extract the sequence of activity objects from that occurrence_index back until the matching
	 * unit number of activity objects this forms a candidate timeline for that user. In this way, we will have a NUMBER
	 * OF or only 1 (if Constant.only1CandFromEachCollUser is true) candidate timelines from each user who have atleast
	 * one non-last occurrence of Current Activity Names in their training timelines. Extended to also work for NHours
	 * approach.
	 * 
	 * @param matchingUnitInCountsOrHours
	 * @param activityAtRecommPoint
	 * @param userIDAtRecomm
	 * @param trainTimelinesAllUsersContinuous
	 * @param dimensionToMatch
	 *            (added on 16 July 2018) which dimension to match, initially it was activity name/id, later extended to
	 *            allow location/gridID
	 * @param NCountOrNHours
	 *            true for NCount, false for NHours (added on 7 Mar 2019)
	 * @return
	 * @since 3 Aug 2017
	 */
	static LinkedHashMap<String, TimelineWithNext> extractCandidateTimelinesMUCountHoursColl(
			// Timeline trainingTimelineqqq,
			// LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersZZ,
			int matchingUnitInCountsOrHours, ActivityObject2018 activityAtRecommPoint, String userIDAtRecomm,
			LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous, PrimaryDimension dimensionToMatch,
			boolean NCountOrNHours)
	{
		LinkedHashMap<String, TimelineWithNext> candidateTimelines = new LinkedHashMap<>();
		long tS = System.nanoTime();

		System.out.println("\nInside extractCandidateTimelinesMUCountColl(): userIDAtRecomm=" + userIDAtRecomm + "mu = "
				+ matchingUnitInCountsOrHours);//
		// for creating timelines");
		// System.out.println("Inside extractCandidateTimelinesMUCountColl :trainTestTimelinesForAllUsers.size()= "
		// + trainTestTimelinesForAllUsers.size());
		// System.out.println("activityAtRecommPoint:" + activityAtRecommPoint.getPrimaryDimensionVal("/"));
		// totalNumberOfProbableCands=0;
		// numCandsRejectedDueToNoCurrentActivityAtNonLast=0;
		int numCandsRejectedDueToNoValidNextActivity = 0;
		// long numOfAOsCompared = 0;

		int numUsersRejectedDueToNoValidCands = 0;

		// for user non-current user, get the training timelines
		for (Entry<String, Timeline> trainTimelineAUser : trainTimelinesAllUsersContinuous.entrySet())
		{
			String userIdCursor = trainTimelineAUser.getKey();

			if (!userIDAtRecomm.equals(userIdCursor))// exclude the current user.
			{
				Timeline trainingTimelineForThisUser = trainTimelineAUser.getValue();

				int numOfValidCurrentActsEncountered = 0;
				// System.out.println("userIDAtRecomm=" + userIDAtRecomm + " userIdCursor=" + userIdCursor);
				// find the most recent occurrence of current activity name
				// get training timeline
				ArrayList<ActivityObject2018> activityObjectsInTraining = trainingTimelineForThisUser
						.getActivityObjectsInTimeline();

				// System.out.println("Num of activity objects in training timeline=" +
				// activityObjectsInTraining.size());

				// System.out
				// .println("Current activity (activityAtRecommPoint)=" + activityAtRecommPoint.getActivityName());
				// System.out.println("cand:" + trainingTimelineForThisUser.getPrimaryDimensionValsInSequence());

				// long t1 = System.currentTimeMillis();
				// starting from the second last activity and goes until first activity.
				for (int i = activityObjectsInTraining.size() - 2; i >= 0; i--)
				// for (int i = 0; i < activityObjectsInTraining.size() - 1; i++)
				{
					ActivityObject2018 ae = activityObjectsInTraining.get(i);
					// numOfAOsCompared += 1;
					// System.out.println("ae = " + ae.getPrimaryDimensionVal("/"));
					// System.out.println("activityAtRecommPoint:" + activityAtRecommPoint.getPrimaryDimensionVal("/"));
					// start sanity check for equalsWrtPrimaryDimension() //Disabled as already checked in earlier
					// methods
					// if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
					// {boolean a = ae.equalsWrtPrimaryDimension(activityAtRecommPoint);
					// boolean b = ae.getActivityName().equals(activityAtRecommPoint.getActivityName());
					// Sanity.eq(a, b,
					// "\nactivityAtRecommPoint=" + activityAtRecommPoint.toStringAllGowallaTS() + "\nae = "
					// + ae.toStringAllGowallaTS() + "\nae.pdvals = " + ae.getPrimaryDimensionVal("/")
					// // + "\nactivityAtRecommPoint=" + activityAtRecommPoint.toStringAllGowallaTS()
					// + "\nae.equalsWrtPrimaryDimension(activityAtRecommPoint) =" + a
					// + " != ae.getActivityName().equals(activityAtRecommPoint.getActivityName()) ="
					// + b + "\n");}
					// end sanity check

					// if (ae.equalsWrtPrimaryDimension(activityAtRecommPoint)) // same name as current activity)
					if (Constant.ensureCandEndWithCurrAct == false
							|| ae.equalsWrtGivenDimension(activityAtRecommPoint, dimensionToMatch))
					{
						ArrayList<ActivityObject2018> activityObjectsForCandidate = null;
						ActivityObject2018 nextValidActivityForCandidate = null;

						if (NCountOrNHours)
						{ ////// FOR NCOUNT/////////////////////////////////////////////////////
							int matchingUnitInCounts = matchingUnitInCountsOrHours;
							int newCandEndIndex = i;
							// NOTE: going back matchingUnitCounts FROM THE index.
							int newCandStartIndex = (newCandEndIndex - matchingUnitInCounts) >= 0
									? (newCandEndIndex - matchingUnitInCounts)
									: 0;
							// $$System.out.println("\n\tStart index of candidate timeline=" + newCandStartIndex);
							// $$System.out.println("\tEnd index of candidate timeline=" + newCandEndIndex);
							// System.out.println("\tmatchingUnitInCounts=" + matchingUnitInCounts);
							// System.out.println( "\tnewCandEndIndex - matchingUnitInCounts=" + (newCandEndIndex -
							// matchingUnitInCounts));
							activityObjectsForCandidate = trainingTimelineForThisUser
									.getActivityObjectsInTimelineFromToIndex(newCandStartIndex, newCandEndIndex + 1);
							nextValidActivityForCandidate = trainingTimelineForThisUser
									.getNextValidActivityAfterActivityAtThisPositionPD(newCandEndIndex);
						}
						else
						{ ////// FOR NHours///////////////////////////////////////////////////// added on 7 Mar 2019,
							////// needs to be sanity checked TODO
							Triple<ArrayList<ActivityObject2018>, ActivityObject2018, Pair<Long, Long>> MUHoursCand = getAOsForNewCandidateTimelineMUHours(
									trainingTimelineForThisUser, matchingUnitInCountsOrHours, ae);
							activityObjectsForCandidate = MUHoursCand.getFirst();
							nextValidActivityForCandidate = MUHoursCand.getSecond();
						}

						if (nextValidActivityForCandidate == null)
						{
							numCandsRejectedDueToNoValidNextActivity += 1;
							System.out.println("\tThis candidate rejected due to no next valid activity object;");
							// if (i == 0)// this was first activity of the timeline
							// { System.out.println("\tThis iser rejected due to no next valid cand");
							// numUsersRejectedDueToNoValidCands += 1;break;
							// } else{
							continue;
							// }
						}
						else
						{
							numOfValidCurrentActsEncountered += 1;
							TimelineWithNext newCandidate = new TimelineWithNext(activityObjectsForCandidate,
									nextValidActivityForCandidate, false, true);// trainingTimeline.getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp));
							// System.out.println(
							// "Created new candidate timeline (with next) from user (" + userIdCursor + ")");
							// System.out.println("\tActivity names:" +
							// newCandidate.getActivityObjectNamesInSequence());
							// System.out.println(
							// "\tNext activity:" + newCandidate.getNextActivityObject().getActivityName());
							candidateTimelines.put(newCandidate.getTimelineID(), newCandidate);
							if (Constant.only1CandFromEachCollUser)
							{
								break; // we only take one candidate timelines from each other user
							}
							else if (Constant.numOfCandsFromEachCollUser > 1
									&& candidateTimelines.size() > Constant.numOfCandsFromEachCollUser)
							{
								break;
							}
						}
					} // end of act name match
				} // end of loop over acts in train timeline

				if (numOfValidCurrentActsEncountered == 0)
				{
					// $$ DIsabled for performance
					// $$System.out.println("\tU:" + userIdCursor + " rejected due to no cur act");
					numUsersRejectedDueToNoValidCands += 1;
				}
			} // end of if user id matches
		} // end of loop over users

		// long t2 = System.nanoTime();
		// System.out.println("total numOfAOsCompared = " + numOfAOsCompared);
		// System.out.println("compared " + numOfAOsCompared + "(t2 - t1)=" + (t2 - tS) + " AOS: avg time "
		// + ((t2 - tS) * 1.0) / numOfAOsCompared + "ns");

		System.out.println("trainTimelinesAllUsersContinuous.size() = " + trainTimelinesAllUsersContinuous.size()
				+ "\ncandidateTimelines.size() = " + candidateTimelines.size()
				+ "\nnumUsersRejectedDueToNoValidCands = " + numUsersRejectedDueToNoValidCands
				+ "\nnumCandsRejectedDueToNoValidNextActivity=" + numCandsRejectedDueToNoValidNextActivity);

		if (Constant.only1CandFromEachCollUser)
		{
			Sanity.eq(trainTimelinesAllUsersContinuous.size() - 1,
					(candidateTimelines.size() + numUsersRejectedDueToNoValidCands),
					"trainTestTimelinesForAllUsers.size()!= (candidateTimelines.size() + numUsersRejectedDueToNoValidCands)");
		}
		// $$System.out.println("\n Exiting extractCandidateTimelinesMUCountColl(): userIDAtRecomm=" +
		// userIDAtRecomm);//
		// for
		return candidateTimelines;
	}

	///
	//
	/**
	 * Create and fetch candidate timelines from the training timelines of other users. Finding Candidate timelines: for
	 * each user, iterate through the training timelines for the most recent occurence of the Current Activity Name in
	 * the candidate timeline, extract the sequence of activity objects from that occurrence_index back until the
	 * matching unit number of activity objects this forms a candidate timeline for that user. In this we will have one
	 * candidate timeline from each user who have atleast one non-last occurrence of Current Activity Names in their
	 * training timelines
	 * 
	 * @param trainingTimeline
	 * @param trainTestTimelinesForAllUsers
	 * @param matchingUnitInCounts
	 * @param activityAtRecommPoint
	 * @param userIDAtRecomm
	 * @return
	 * @since 26 July 2017
	 * @deprecated (most probably, realized on 7 Mar 2019)
	 */
	private static LinkedHashMap<String, TimelineWithNext> extractCandidateTimelinesMUCountColl(
			Timeline trainingTimelineqqq,
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsers,
			int matchingUnitInCounts, ActivityObject2018 activityAtRecommPoint, String userIDAtRecomm)
	{
		LinkedHashMap<String, TimelineWithNext> candidateTimelines = new LinkedHashMap<>();
		// long tS = System.nanoTime();

		// System.out.println("\nInside extractCandidateTimelinesMUCountColl(): userIDAtRecomm=" + userIDAtRecomm);//
		// for creating timelines");
		// System.out.println("Inside extractCandidateTimelinesMUCountColl :trainTestTimelinesForAllUsers.size()= "
		// + trainTestTimelinesForAllUsers.size());
		// System.out.println("activityAtRecommPoint:" + activityAtRecommPoint.getPrimaryDimensionVal("/"));
		// totalNumberOfProbableCands=0;
		// numCandsRejectedDueToNoCurrentActivityAtNonLast=0;
		int numCandsRejectedDueToNoValidNextActivity = 0;
		// long numOfAOsCompared = 0;

		int numUsersRejectedDueToNoValidCands = 0;
		// for user non-current user, get the training timelines
		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsers
				.entrySet())
		{
			String userIdCursor = trainTestForAUser.getKey();

			if (!userIDAtRecomm.equals(userIdCursor))// exclude the current user.
			{
				LinkedHashMap<Date, Timeline> trainingTimelineForThisUserDate = trainTestForAUser.getValue().get(0);
				Timeline trainingTimelineForThisUser = TimelineTransformers
						.dayTimelinesToATimeline(trainingTimelineForThisUserDate, false, true);
				// convert datetime to continouse timeline

				int numOfValidCurrentActsEncountered = 0;
				// System.out.println("userIDAtRecomm=" + userIDAtRecomm + " userIdCursor=" + userIdCursor);
				// find the most recent occurrence of current activity name
				// get training timeline
				ArrayList<ActivityObject2018> activityObjectsInTraining = trainingTimelineForThisUser
						.getActivityObjectsInTimeline();

				// System.out.println("Num of activity objects in training timeline=" +
				// activityObjectsInTraining.size());

				// $$System.out.println("Current activity (activityAtRecommPoint)=" +
				// this.activityAtRecommPoint.getActivityName());

				// System.out.println("cand:" + trainingTimelineForThisUser.getPrimaryDimensionValsInSequence());

				// long t1 = System.currentTimeMillis();
				// starting from the second last activity and goes until first activity.
				for (int i = activityObjectsInTraining.size() - 2; i >= 0; i--)
				// for (int i = 0; i < activityObjectsInTraining.size() - 1; i++)
				{
					ActivityObject2018 ae = activityObjectsInTraining.get(i);
					// numOfAOsCompared += 1;
					// System.out.println("ae = " + ae.getPrimaryDimensionVal("/"));
					// System.out.println("activityAtRecommPoint:" + activityAtRecommPoint.getPrimaryDimensionVal("/"));
					// start sanity check for equalsWrtPrimaryDimension() //Disabled as already checked in earlier
					// methods
					// if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
					// {boolean a = ae.equalsWrtPrimaryDimension(activityAtRecommPoint);
					// boolean b = ae.getActivityName().equals(activityAtRecommPoint.getActivityName());
					// Sanity.eq(a, b,
					// "\nactivityAtRecommPoint=" + activityAtRecommPoint.toStringAllGowallaTS() + "\nae = "
					// + ae.toStringAllGowallaTS() + "\nae.pdvals = " + ae.getPrimaryDimensionVal("/")
					// // + "\nactivityAtRecommPoint=" + activityAtRecommPoint.toStringAllGowallaTS()
					// + "\nae.equalsWrtPrimaryDimension(activityAtRecommPoint) =" + a
					// + " != ae.getActivityName().equals(activityAtRecommPoint.getActivityName()) ="
					// + b + "\n");}
					// end sanity check

					if (ae.equalsWrtPrimaryDimension(activityAtRecommPoint)) // same name as current activity)
					{

						// Timestamp newCandEndTimestamp= new
						// Timestamp(ae.getStartTimestamp().getTime()+ae.getDurationInSeconds()*1000-1000); //decreasing
						// 1
						// second (because this is convention followed in data generation)
						int newCandEndIndex = i;
						// NOTE: going back matchingUnitCounts FROM THE index.
						int newCandStartIndex = (newCandEndIndex - matchingUnitInCounts) >= 0
								? (newCandEndIndex - matchingUnitInCounts)
								: 0;

						// System.out.println("\n\tStart index of candidate timeline=" + newCandStartIndex);
						// System.out.println("\tEnd index of candidate timeline=" + newCandEndIndex);

						ArrayList<ActivityObject2018> activityObjectsForCandidate = trainingTimelineForThisUser
								.getActivityObjectsInTimelineFromToIndex(newCandStartIndex, newCandEndIndex + 1);
						// getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp);
						ActivityObject2018 nextValidActivityForCandidate = trainingTimelineForThisUser
								.getNextValidActivityAfterActivityAtThisPositionPD(newCandEndIndex);

						if (nextValidActivityForCandidate == null)
						{
							numCandsRejectedDueToNoValidNextActivity += 1;
							System.out.println("\tThis candidate rejected due to no next valid activity object;");

							// if (i == 0)// this was first activity of the timeline
							// {
							// System.out.println("\tThis iser rejected due to no next valid cand");
							// numUsersRejectedDueToNoValidCands += 1;
							// break;
							// }
							// else
							// {
							continue;
							// }
						}
						else
						{
							numOfValidCurrentActsEncountered += 1;
							TimelineWithNext newCandidate = new TimelineWithNext(activityObjectsForCandidate,
									nextValidActivityForCandidate, false, true);// trainingTimeline.getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp));
							// System.out.println(
							// "Created new candidate timeline (with next) from user (" + userIdCursor + ")");
							// System.out.println("\tActivity names:" +
							// newCandidate.getActivityObjectNamesInSequence());
							// System.out.println(
							// "\tNext activity:" + newCandidate.getNextActivityObject().getActivityName());
							candidateTimelines.put(newCandidate.getTimelineID(), newCandidate);

							break; // we only take one candidate timelines from each other user
						}
					} // end of act name match
				} // end of loop over acts in train timeline

				if (numOfValidCurrentActsEncountered == 0)
				{
					// $$ DIsabled for performance
					// $$System.out.println("\tU:" + userIdCursor + " rejected due to no cur act");
					numUsersRejectedDueToNoValidCands += 1;
				}
			} // end of if user id matches
		} // end of loop over users

		// long t2 = System.nanoTime();
		// System.out.println("total numOfAOsCompared = " + numOfAOsCompared);
		// System.out.println("compared " + numOfAOsCompared + "(t2 - t1)=" + (t2 - tS) + " AOS: avg time "
		// + ((t2 - tS) * 1.0) / numOfAOsCompared + "ns");

		// System.out.println("trainTestTimelinesForAllUsers.size() = " + trainTestTimelinesForAllUsers.size());
		// System.out.println("candidateTimelines.size() = " + candidateTimelines.size());
		System.out.println("numUsersRejectedDueToNoValidCands = " + numUsersRejectedDueToNoValidCands);
		Sanity.eq(trainTestTimelinesForAllUsers.size() - 1,
				(candidateTimelines.size() + numUsersRejectedDueToNoValidCands),
				"trainTestTimelinesForAllUsers.size()!= (candidateTimelines.size() + numUsersRejectedDueToNoValidCands)");

		// System.out.println("\n Exiting extractCandidateTimelinesMUCountColl(): userIDAtRecomm=" + userIDAtRecomm);//
		// for
		return candidateTimelines;
	}

	/**
	 * Create and fetch candidate timelines from the training timelines
	 * 
	 * @param dayTimelinesForUser
	 * @return
	 */
	public static LinkedHashMap<String, TimelineWithNext> extractCandidateTimelinesMUHours(Timeline trainingTimeline,
			double matchingUnitInHours, ActivityObject2018 activityAtRecommPoint)
	// ArrayList<ActivityObject> activitiesGuidingRecomm,Date//dateAtRecomm)
	{
		int count = 0;
		LinkedHashMap<String, TimelineWithNext> candidateTimelines = new LinkedHashMap<>();

		System.out.println("\nInside extractCandidateTimelinesMUHours()");// for creating timelines");
		// totalNumberOfProbableCands=0;
		// numCandsRejectedDueToNoCurrentActivityAtNonLast=0;
		int numCandsRejectedDueToNoValidNextActivity = 0;
		ArrayList<ActivityObject2018> activityObjectsInTraining = trainingTimeline.getActivityObjectsInTimeline();
		System.out.println("Number of activity objects in training timeline=" + activityObjectsInTraining.size());
		System.out.println(
				"Current activity (activityAtRecommPoint)=" + activityAtRecommPoint.getPrimaryDimensionVal("__"));

		if (VerbosityConstants.verbose)// sanity check Dec 18 2018
		{
			// Timeline trainingTimeline,double matchingUnitInHours, ActivityObject2018 activityAtRecommPoint
			StringBuilder sb = new StringBuilder();
			sb.append("Debug18Dec2018: Timeline:\n" + trainingTimeline.getActivityObjectNamesWithTimestampsInSequence()
					+ "\nmatchingUnitInHours=" + matchingUnitInHours + "\nactivityAtRecommPoint="
					+ activityAtRecommPoint.getPrimaryDimensionVal("_") + "CandTimelines=\n");
			System.out.println(sb.toString() + "\n");
		}
		// trainingTimeline.printActivityObjectNamesWithTimestampsInSequence();

		// starting from the second activity and goes until second last activity.
		for (int i = 1; i < activityObjectsInTraining.size() - 1; i++)
		{
			ActivityObject2018 ae = activityObjectsInTraining.get(i);

			// start sanity check for equalsWrtPrimaryDimension() //can be removed after check
			if (VerbosityConstants.checkSanityPDImplementn
					&& Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
			{
				boolean a = ae.equalsWrtPrimaryDimension(activityAtRecommPoint);
				boolean b = ae.getActivityName().equals(activityAtRecommPoint.getActivityName());
				Sanity.eq(a, b, "ae.equalsWrtPrimaryDimension(activityAtRecommPoint) =" + a
						+ " != ae.getActivityName().equals(activityAtRecommPoint.getActivityName()) =" + b);
			}
			// end sanity check

			if (ae.equalsWrtPrimaryDimension(activityAtRecommPoint)) // same name as current activity)
			// if (ae.getActivityName().equals(activityAtRecommPoint.getActivityName())) // same name as current
			// activity
			{
				// extracted logic to separate method on 7 Mar 2019
				Triple<ArrayList<ActivityObject2018>, ActivityObject2018, Pair<Long, Long>> aosAndNextAOForCand = getAOsForNewCandidateTimelineMUHours(
						trainingTimeline, matchingUnitInHours, ae);

				ArrayList<ActivityObject2018> activityObjectsForCandidate = aosAndNextAOForCand.getFirst();
				ActivityObject2018 nextValidActivityForCandidate = aosAndNextAOForCand.getSecond();

				if (nextValidActivityForCandidate == null)
				{
					numCandsRejectedDueToNoValidNextActivity += 1;
					System.out.println("\tThis candidate rejected due to no next valid activity object;");
					continue;
				}
				TimelineWithNext newCandidate = new TimelineWithNext(activityObjectsForCandidate,
						nextValidActivityForCandidate, false, true);// trainingTimeline.getActivityObjectsBetweenTime(newCandStartTimestamp,newCandEndTimestamp));
				// $$System.out.println("Created new candidate timeline (with next)");
				// $$System.out.println("\tActivity names:" + newCandidate.getActivityObjectNamesInSequence());
				// $$System.out.println("\tNext activity:" + newCandidate.getNextActivityObject().getActivityName());
				candidateTimelines.put(newCandidate.getTimelineID(), newCandidate);

				if (VerbosityConstants.verbose)// sanity check Dec 18 2018
				{
					long matchingUnitInMilliSeconds = aosAndNextAOForCand.getThird().getFirst();
					Timestamp newCandStartTimestampP = new Timestamp(aosAndNextAOForCand.getThird().getSecond());

					System.out.println("\n index in timeline = " + i + "\nae= " + ae.getPrimaryDimensionVal("__") + ":"
							+ ae.getStartTimestamp() + "--" + ae.getEndTimestamp() + "\nnewCandStartTimestamp="
							+ newCandStartTimestampP + "\nmatchingUnitInMilliSeconds= " + matchingUnitInMilliSeconds
							+ "\nCandidateTimeline\n" + newCandidate.getActivityObjectNamesWithTimestampsInSequence());
				}
			}
		}

		// Start of sanity check Dec 18 2018
		// if (VerbosityConstants.verbose || true)
		// {
		// // Timeline trainingTimeline,double matchingUnitInHours, ActivityObject2018 activityAtRecommPoint
		// StringBuilder sb = new StringBuilder();
		// sb.append("Debug18Dec2018: Timeline:\n" + trainingTimeline.getActivityObjectNamesWithTimestampsInSequence()
		// + "\nmatchingUnitInHours=" + matchingUnitInHours + "\nactivityAtRecommPoint="
		// + activityAtRecommPoint.getPrimaryDimensionVal("_") + "CandTimelines=\n");
		// candidateTimelines.entrySet().stream().forEachOrdered(e -> sb.append(
		// "\n\n" + e.getKey() + "----" + e.getValue().getActivityObjectNamesWithTimestampsInSequence()));
		// System.out.println(sb.toString() + "\n");
		// }
		// End of sanity check Dec 18 2018
		return candidateTimelines;
	}

	/**
	 * @param trainingTimeline
	 * @param matchingUnitInHours
	 * @param ae
	 * @return Triple{activityObjectsForCandidate, nextValidActivityForCandidate, new Pair<>(matchingUnitInMilliSeconds,
	 *         newCandEndTimestamp.getTime())}
	 * @since 7 March 2019 (extracted from parent method for reusability)
	 */
	private static Triple<ArrayList<ActivityObject2018>, ActivityObject2018, Pair<Long, Long>> getAOsForNewCandidateTimelineMUHours(
			Timeline trainingTimeline, double matchingUnitInHours, ActivityObject2018 ae)
	{
		Timestamp newCandEndTimestamp = null;
		if (Constant.getDatabaseName().equals("dcu_data_2") || Constant.getDatabaseName().equals("geolife1"))
		{// decreasing 1 second (because this convention followed in data generation (not for Gowalla IMHO)
			newCandEndTimestamp = new Timestamp(
					ae.getStartTimestamp().getTime() + ae.getDurationInSeconds() * 1000 - 1000);
		}
		else
		{
			newCandEndTimestamp = new Timestamp(ae.getStartTimestamp().getTime() + ae.getDurationInSeconds() * 1000);
			// added on 18 Dec 2018
		}
		// NOTE: going back matchingUnitHours FROM THE START TIMESTAMP and not the end timestamp.

		// this cast is safe because in this case number of milliseconds won't be in decimals
		long matchingUnitInMilliSeconds = (long) (matchingUnitInHours * 60 * 60 * 1000);// .multiply(new
		// BigDecimal(60*60*1000)).longValue();
		Timestamp newCandStartTimestamp = new Timestamp(ae.getStartTimestamp().getTime() - matchingUnitInMilliSeconds);

		// $$System.out.println("\n\tStarttime of candidate timeline=" + newCandStartTimestamp);
		// $$System.out.println("\tEndtime of candidate timeline=" + newCandEndTimestamp);

		/*
		 * Note: if newCandStartTimestamp here is earlier than when the training timeline started, even then this works
		 * correctly since we are considering intersection of what is available
		 */
		ArrayList<ActivityObject2018> activityObjectsForCandidate = trainingTimeline
				.getActivityObjectsBetweenTime(newCandStartTimestamp, newCandEndTimestamp);
		ActivityObject2018 nextValidActivityForCandidate = trainingTimeline
				.getNextValidActivityAfterActivityAtThisTime(newCandEndTimestamp);

		Triple<ArrayList<ActivityObject2018>, ActivityObject2018, Pair<Long, Long>> aosAndNextAOForCand = new Triple<>(
				activityObjectsForCandidate, nextValidActivityForCandidate,
				new Pair<>(matchingUnitInMilliSeconds, newCandEndTimestamp.getTime()));

		return aosAndNextAOForCand;
	}

	/**
	 * Fetches the current timeline from the given longer timeline from the recommendation point back until the matching
	 * unit count Activity Objects.
	 * 
	 * @param longerTimeline
	 *            the timelines (test timeline) from which the current timeline is to be extracted
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCounts
	 * @return Pair(current_TimelineWithNext, reductionInMU)
	 */
	public static Pair<TimelineWithNext, Double> getCurrentTimelineFromLongerTimelineMUCount(Timeline longerTimeline,
			Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm, double matchingUnitInCountsD)
	{
		// $$System.out.println("------Inside getCurrentTimelineFromLongerTimelineMUCount");
		double reductionInMU = 0;
		int matchingUnitInCounts = (int) matchingUnitInCountsD;

		Timestamp currentEndTimestamp = new Timestamp(dateAtRecomm.getYear(), dateAtRecomm.getMonth(),
				dateAtRecomm.getDate(), timeAtRecomm.getHours(), timeAtRecomm.getMinutes(), timeAtRecomm.getSeconds(),
				0);
		// long currentEndTime=currentEndTimestamp.getTime();

		int indexOfCurrentEnd = longerTimeline.getIndexOfActivityObjectAtTime(currentEndTimestamp);

		if (indexOfCurrentEnd - matchingUnitInCounts < 0)
		{
			reductionInMU = matchingUnitInCounts - indexOfCurrentEnd;
			System.out.println("Warning: reducing mu since not enough past,indexOfCurrentEnd=" + indexOfCurrentEnd
					+ ", muInCounts=" + matchingUnitInCounts + ", new MU=" + indexOfCurrentEnd);
			matchingUnitInCounts = indexOfCurrentEnd;
		}

		// this is a safe cast in this case
		// long matchingUnitInMilliSeconds= (long)(matchingUnitInHours*60*60*1000);//multiply(new
		// BigDecimal(60*60*1000))).longValue();
		// Timestamp currentStartTimestamp = new Timestamp(currentEndTime- matchingUnitInMilliSeconds);

		int indexOfCurrentStart = indexOfCurrentEnd - matchingUnitInCounts;

		if (VerbosityConstants.verbose)
		{
			System.out.println("longer timeline=" + longerTimeline.getActivityObjectNamesWithTimestampsInSequence());// getActivityObjectNamesInSequence());
			// currentTimeline.getActivityObjectNamesWithTimestampsInSequence());
			System.out.println("Start index of current timeline=" + indexOfCurrentStart
					+ "\nEnd index of current timeline=" + indexOfCurrentEnd + "\nAdjusted MU:" + matchingUnitInCounts);
		}

		// identify the recommendation point in longer timeline
		ArrayList<ActivityObject2018> activityObjectsInCurrentTimeline = longerTimeline
				.getActivityObjectsInTimelineFromToIndex(indexOfCurrentStart, indexOfCurrentEnd + 1);

		System.out.println("AOsInCurrTimeline.size()=" + activityObjectsInCurrentTimeline.size());

		ActivityObject2018 nextValidActivityObject = longerTimeline
				.getNextValidActivityAfterActivityAtThisPositionPD(indexOfCurrentEnd);
		ActivityObject2018 nextActivityObject = longerTimeline
				.getNextActivityAfterActivityAtThisPosition(indexOfCurrentEnd);

		int isInvalid = nextActivityObject.isInvalidActivityName() ? 1 : -1;
		TimelineWithNext currentTimeline = new TimelineWithNext(activityObjectsInCurrentTimeline,
				nextValidActivityObject, false, true);
		currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);

		// System.out.println("Current timeline="+currentTimeline.getActivityObjectNamesInSequence());
		if (currentTimeline.getActivityObjectsInTimeline().size() != (matchingUnitInCounts + 1))
		// note: this is matching unit in counts reduced
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error: the current timeline does not have #activity objs = adjusted matching unit"));
		}

		// $$System.out.println("------Exiting getCurrentTimelineFromLongerTimelineMUCount");
		return new Pair<>(currentTimeline, reductionInMU);
	}

	/**
	 * Fetches the current timeline from the given longer timeline from the recommendation point back until the matching
	 * unit length.
	 * 
	 * @param longerTimeline
	 *            the timelines (test timeline) from which the current timeline is to be extracted
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInHours
	 * @return
	 */
	public static TimelineWithNext getCurrentTimelineFromLongerTimelineMUHours(Timeline longerTimeline,
			Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm, double matchingUnitInHours)
	{
		System.out.println("------- Inside getCurrentTimelineFromLongerTimelineMUHours");

		Timestamp currentEndTimestamp = new Timestamp(dateAtRecomm.getYear(), dateAtRecomm.getMonth(),
				dateAtRecomm.getDate(), timeAtRecomm.getHours(), timeAtRecomm.getMinutes(), timeAtRecomm.getSeconds(),
				0);
		long currentEndTime = currentEndTimestamp.getTime();

		// this is a safe cast in this case
		long matchingUnitInMilliSeconds = (long) (matchingUnitInHours * 60 * 60 * 1000);// multiply(new
																						// BigDecimal(60*60*1000))).longValue();

		Timestamp currentStartTimestamp = new Timestamp(currentEndTime - matchingUnitInMilliSeconds);

		System.out.println("Starttime of current timeline=" + currentStartTimestamp + "\nEndtime of current timeline="
				+ currentEndTimestamp);

		// identify the recommendation point in longer timeline
		ArrayList<ActivityObject2018> activityObjectsInCurrentTimeline = longerTimeline
				.getActivityObjectsBetweenTime(currentStartTimestamp, currentEndTimestamp);

		ActivityObject2018 nextValidActivityObject = longerTimeline
				.getNextValidActivityAfterActivityAtThisTime(currentEndTimestamp);
		ActivityObject2018 nextActivityObject = longerTimeline
				.getNextActivityAfterActivityAtThisTime(currentEndTimestamp);

		int isInvalid = nextActivityObject.isInvalidActivityName() ? 1 : -1;
		TimelineWithNext currentTimeline = new TimelineWithNext(activityObjectsInCurrentTimeline,
				nextValidActivityObject, false, true);
		currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);
		System.out.println("------- Exiting getCurrentTimelineFromLongerTimelineMUHours");
		return currentTimeline;
	}

	/**
	 * Fetches the current timeline from the given longer timeline from the recommendation point back until the matching
	 * unit count Activity Objects.
	 * 
	 * @param longerTimeline
	 *            the timelines (test timeline) from which the current timeline is to be extracted
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userIDAtRecomm
	 * @param matchingUnitInCounts
	 * @return
	 */
	public static TimelineWithNext getCurrentTimelineFromLongerTimelineDaywise(
			LinkedHashMap<Date, Timeline> testDayTimelines, Date dateAtRecomm, Time timeAtRecomm, String userIDAtRecomm)
	{
		// $$System.out.println("------Inside getCurrentTimelineFromLongerTimelineDaywise");

		///////////////////////////////////////////////////////////////////
		Timestamp currentEndTimestamp = new Timestamp(dateAtRecomm.getYear(), dateAtRecomm.getMonth(),
				dateAtRecomm.getDate(), timeAtRecomm.getHours(), timeAtRecomm.getMinutes(), timeAtRecomm.getSeconds(),
				0);
		// Timestamp currentEndTimestamp2 = new Timestamp(timeAtRecomm.getTime()); INCORRECT 1970 year
		// check if timestamps are actually equally, if yes, prefer the cleaner method INCORRECT 1970 year
		// System.out.println("Debug sanity check Note: currentEndTimestamp2.equals(currentEndTimestamp) ="
		// + currentEndTimestamp.equals(currentEndTimestamp));
		// System.out.println("Debug sanity check Note: currentEndTimestamp2 =" + currentEndTimestamp
		// + "currentEndTimestamp = " + currentEndTimestamp);
		///////////////////////////////////////////////////////////////////
		Timeline currentDayTimeline = testDayTimelines.get(dateAtRecomm);

		if (VerbosityConstants.verbose)
		{
			LocalDate dateOfFetchedDayTimeline = currentDayTimeline.getActivityObjectsInTimeline().get(0).getEndDate();
			System.out.println("Debug sanity check Note: dateOfFetchedDayTimeline = " + dateOfFetchedDayTimeline
					+ " dateAtRecomm = " + dateAtRecomm + "equals = " + dateAtRecomm.equals(dateOfFetchedDayTimeline));
		}
		if (currentDayTimeline == null || !(currentDayTimeline.size() > 0))
		{
			System.err.println(
					PopUps.getTracedErrorMsg("Error: currentDayTimeline.size() =" + currentDayTimeline.size()));
		}
		///////////////////////////////////////////////////////////////////

		int indexOfCurrentEnd = currentDayTimeline.getIndexOfActivityObjectAtTime(currentEndTimestamp);

		// identify the recommendation point in longer timeline
		ArrayList<ActivityObject2018> activityObjectsInCurrentTimeline = currentDayTimeline
				.getActivityObjectsInTimelineFromToIndex(0, indexOfCurrentEnd + 1);

		ActivityObject2018 nextValidActivityObject = currentDayTimeline
				.getNextValidActivityAfterActivityAtThisPositionPD(indexOfCurrentEnd);
		ActivityObject2018 nextActivityObject = currentDayTimeline
				.getNextActivityAfterActivityAtThisPosition(indexOfCurrentEnd);

		int isInvalid = nextActivityObject.isInvalidActivityName() ? 1 : -1;

		TimelineWithNext currentTimeline = new TimelineWithNext(activityObjectsInCurrentTimeline,
				nextValidActivityObject, true, true);
		currentTimeline.setImmediateNextActivityIsInvalid(isInvalid);

		// if (VerbosityConstants.verbose)
		// System.out.println("Current timeline=" + currentTimeline.getActivityObjectNamesInSequence());

		if (!(currentTimeline.size() > 0))
		{
			System.err.println(PopUps.getTracedErrorMsg("Error: currentTimeline.size() =" + currentTimeline.size()));
		}

		// $$System.out.println("------Exiting getCurrentTimelineFromLongerTimelineDaywise");
		return currentTimeline;
	}

}
