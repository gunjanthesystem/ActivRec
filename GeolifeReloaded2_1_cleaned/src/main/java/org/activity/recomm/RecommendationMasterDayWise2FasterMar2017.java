package org.activity.recomm;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.distances.AlignmentBasedDistance;
import org.activity.distances.HJEditDistance;
import org.activity.evaluation.Evaluation;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.objects.UserDayTimeline;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.RegexUtils;
import org.activity.util.StringCode;
import org.activity.util.TimelineUtils;

/**
 * Fork of org.activity.recomm.RecommendationMasterDayWise2FasterJan2016
 * 
 * @author gunjan
 *
 */
/* Modified version of RecommendationMasterDayWise2Faster. This uses daywise timelines */
public class RecommendationMasterDayWise2FasterMar2017 implements RecommendationMasterI
{
	private LinkedHashMap<Date, UserDayTimeline> trainingTimelines;
	private LinkedHashMap<Date, UserDayTimeline> testTimelines;
	private LinkedHashMap<Date, UserDayTimeline> candidateTimelines;

	private UserDayTimeline currentDayTimeline;// userDayTimelineAtRecomm

	private Date dateAtRecomm;
	private Time timeAtRecomm;
	private String userAtRecomm;
	private String userIDAtRecomm;

	/**
	 * Current Timeline
	 */
	private ArrayList<ActivityObject> activitiesGuidingRecomm; // (activitiesGuidingRecomm) sequence of activity events
																// happening one or before the recomm point on that day
	private ActivityObject activityAtRecommPoint;
	// Date dateOfMostSimilar

	// private String singleNextRecommendedActivity;
	private String nextActivityNamesFromCandsByEditDistString;

	private LinkedHashMap<Date, String> nextActivityNamesFromCandsByEditDistMap;

	/**
	 * (Cand TimelineID, (End point index of least distant subsequence, String containing the trace of edit operations
	 * performed, edit distance of least distant subsequence)) this LinkedHashMap is sorted by the value of edit
	 * distance in ascending order (second component of the Pair (value))
	 * 
	 */
	private LinkedHashMap<Date, Triple<Integer, String, Double>> editDistancesSortedMap;

	private LinkedHashMap<String, Double> recommendedActivityNamesWithRankScores;
	private String rankedRecommendationWithRankScores;
	private String rankedRecommendationWithoutRankScores;

	private boolean hasCandidateTimelines, hasCandidateTimelinesBelowThreshold;

	private boolean nextActivityJustAfterRecommPointIsInvalid;
	private double thresholdAsDistance = 99999999;

	private boolean thresholdPruningNoEffect;

	// public int totalNumberOfProbableCands;
	// public int numCandsRejectedDueToNoCurrentActivityAtNonLast;
	// public int numCandsRejectedDueToNoNextActivity;

	private static String commonPath;// = Constant.commonPath;

	/*
	 * threshold for choosing candidate timelines, those candidate timelines whose distance from the 'activity events
	 * guiding recommendations' is higher than the cost of replacing 'percentageDistanceThresh' % of Activity Events in
	 * the activities guiding recommendation are pruned out from set of candidate timelines
	 */

	public RecommendationMasterDayWise2FasterMar2017(LinkedHashMap<Date, UserDayTimeline> trainingTimelines,
			LinkedHashMap<Date, UserDayTimeline> testTimelines, String dateAtRecomm, String timeAtRecomm,
			int userAtRecomm, double thresholdVal, Enums.TypeOfThreshold typeOfThreshold)
	{
		System.out.println("\n----------------Starting RecommendationMasterDayWise2FasterDec ");
		commonPath = Constant.getCommonPath();
		// LinkedHashMap<Date, Pair<Integer, Double>> editDistancesMapUnsorted;

		hasCandidateTimelines = true;
		nextActivityJustAfterRecommPointIsInvalid = false;
		// dateAtRecomm.split("/"); //
		String[] splittedDate = RegexUtils.patternFromSlash.split(dateAtRecomm);// dd/mm/yyyy
		this.dateAtRecomm = new Date(Integer.parseInt(splittedDate[2]) - 1900, Integer.parseInt(splittedDate[1]) - 1,
				Integer.parseInt(splittedDate[0]));
		String[] splittedTime = RegexUtils.patternColon.split(timeAtRecomm);// timeAtRecomm.split(":"); // hh:mm:ss
		this.timeAtRecomm = Time.valueOf(timeAtRecomm);
		this.userAtRecomm = Integer.toString(userAtRecomm);
		this.userIDAtRecomm = Integer.toString(userAtRecomm);

		System.out.println("\n^^^^^^^^^^^^^^^^Inside RecommendationMasterDayWise2Faster");
		System.out.println("	User at Recomm = " + this.userAtRecomm + "\n	Date at Recomm = " + this.dateAtRecomm
				+ "\n	Time at Recomm = " + this.timeAtRecomm);

		// $$21Oct
		currentDayTimeline = TimelineUtils.getUserDayTimelineByDateFromMap(testTimelines, this.dateAtRecomm);

		if (currentDayTimeline == null)
		{
			System.err.println("Error: current day timeline not found (in test timelines) for the recommendation day");
		}

		System.out.println("The Activities on the recomm day are:");
		currentDayTimeline.printActivityObjectNamesWithTimestampsInSequence();
		System.out.println();

		Timestamp timestampPointAtRecomm = new Timestamp(this.dateAtRecomm.getYear(), this.dateAtRecomm.getMonth(),
				this.dateAtRecomm.getDate(), this.timeAtRecomm.getHours(), this.timeAtRecomm.getMinutes(),
				this.timeAtRecomm.getSeconds(), 0);
		System.out.println("timestampPointAtRecomm = " + timestampPointAtRecomm);

		this.activitiesGuidingRecomm = currentDayTimeline
				.getActivityObjectsStartingOnBeforeTime(timestampPointAtRecomm);
		System.out.println("Activity starting on or before the time to recommend (on recommendation day) are: \t");
		activitiesGuidingRecomm.stream().forEach(e -> System.out.print(e.getActivityName() + " "));

		this.activityAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1);
		System.out.println("\nActivity at Recomm point =" + this.activityAtRecommPoint.getActivityName());

		// All check OK
		this.candidateTimelines = TimelineUtils.extractDaywiseCandidateTimelines(trainingTimelines,
				this.activitiesGuidingRecomm, this.dateAtRecomm, this.activityAtRecommPoint);

		System.out.println("Number of candidate timelines =" + candidateTimelines.size());
		System.out.println("the candidate timelines are as follows:");
		// $$traverseMapOfDayTimelines(candidateTimelines);

		if (candidateTimelines.size() == 0)
		{
			System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
					+ " at time:" + timeAtRecomm + "  because there are no candidate timelines");
			// this.singleNextRecommendedActivity = null;
			hasCandidateTimelines = false;
			this.nextActivityNamesFromCandsByEditDistString = null;
			this.thresholdPruningNoEffect = true;
			this.nextActivityNamesFromCandsByEditDistMap = null;
			return;
		}
		else
		{
			hasCandidateTimelines = true;
		}

		// Note: not currently sorted
		LinkedHashMap<Date, Triple<Integer, String, Double>> editDistancesUnSortedMap = getEditDistancesForCandidateTimelines(
				candidateTimelines, activitiesGuidingRecomm, this.userIDAtRecomm, this.dateAtRecomm.toString(),
				this.timeAtRecomm.toString());

		// ########Sanity check
		if (editDistancesUnSortedMap.size() != candidateTimelines.size())
		{
			System.err.println("Error at Sanity 10: editDistancesNotSortedMap.size() != candidateTimelines.size()");
		}
		// ##############

		// /// REMOVE candidate timelines which are above the distance THRESHOLD. (actually here we remove the entry for
		// such candidate timelines from the
		// distance scores map
		if (typeOfThreshold.equals(Enums.TypeOfThreshold.Global))// IgnoreCase("Global"))
		{
			this.thresholdAsDistance = thresholdVal;
		}
		else if (typeOfThreshold.equals(Enums.TypeOfThreshold.Percent))// "Percent"))
		{
			double maxEditDistance = (new AlignmentBasedDistance()).maxEditDistance(activitiesGuidingRecomm);
			this.thresholdAsDistance = maxEditDistance * (thresholdVal / 100);
		}
		else
		{
			System.err.println("Error: type of threshold unknown in recommendation master");
			System.exit(-2);
		}

		int countCandBeforeThresholdPruning = editDistancesUnSortedMap.size();
		editDistancesUnSortedMap = TimelineUtils.removeAboveThresholdDISD(editDistancesUnSortedMap,
				thresholdAsDistance);
		int countCandAfterThresholdPruning = editDistancesUnSortedMap.size();
		this.thresholdPruningNoEffect = (countCandBeforeThresholdPruning == countCandAfterThresholdPruning);
		// ////////////////////////////////

		if (editDistancesUnSortedMap.size() == 0)
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

		// endPointIndexWithLeastDistanceForCandidateTimelines =
		// getEndPointIndicesWithLeastDistanceInCandidateTimeline(candidateTimelines,activitiesGuidingRecomm);
		// sort by edit distance ascending
		editDistancesSortedMap = (LinkedHashMap<Date, Triple<Integer, String, Double>>) ComparatorUtils
				.sortTripleByThirdValueAscending6(editDistancesUnSortedMap);

		// this.nextActivityNamesFromCandsByEditDistance =
		// getNextActivityNamesFromCandsByEditDistances(activitiesGuidingRecomm, editDistancesSortedMap,
		// trainingTimelines);
		Pair<String, LinkedHashMap<Date, String>> nextActsPairRes = getNextActivityNamesFromCandsByEditDistance(
				activitiesGuidingRecomm, editDistancesSortedMap, trainingTimelines);
		this.nextActivityNamesFromCandsByEditDistString = nextActsPairRes.getFirst();
		this.nextActivityNamesFromCandsByEditDistMap = nextActsPairRes.getSecond();

		// this.nextActivityNamesFromCandsByEditDist =
		// getNextActivityNamesFromCandsByEditDistance(activitiesGuidingRecomm, editDistancesSortedMap,
		// trainingTimelines);
		// this.topRecommendedActivitiesWithDistance = =
		// getTopNextRecommendedActivityNamesWithDistanceEditDistance(activitiesGuidingRecomm,editDistancesSortedMap,dayTimelinesForUser);

		// ########Sanity check
		String sanity11[] = RegexUtils.patternDoubleUnderScore.split(nextActivityNamesFromCandsByEditDistString);
		// nextActivityNamesFromCandsByEditDistString.split(Pattern.quote("__"));
		if (sanity11.length - 1 != editDistancesSortedMap.size())
		{
			System.err.println("Error at Sanity 11: num of topRecommendedActivities (without wtd ranking)(="
					+ (sanity11.length - 1)
					+ " is not equal to the number of dates for which distance has been calculated:(="
					+ editDistancesSortedMap.size() + ")");
		}
		else
		{
			System.out.println("Sanity Check 11 Passed");
		}

		// to keep recommRes local keeping it in a block
		{
			Triple<String, String, LinkedHashMap<String, Double>> recommRes = createRankedTopRecommendation(
					this.nextActivityNamesFromCandsByEditDistString);
			this.rankedRecommendationWithRankScores = recommRes.getFirst();
			this.rankedRecommendationWithoutRankScores = recommRes.getSecond();
			this.recommendedActivityNamesWithRankScores = recommRes.getThird();
		}

		// this.rankedRecommendationWithRankScores
		// System.out.println("Next recommended 5 activity is: "+this.topFiveRecommendedActivities);

		int indexOfRecommPointInDayTimeline = currentDayTimeline
				.getIndexOfActivityObjectsAtTime(timestampPointAtRecomm);

		if (indexOfRecommPointInDayTimeline + 1 > currentDayTimeline.getActivityObjectsInDay().size() - 1)
		{
			System.err.println(
					"Error in Recommendation Master: No activity event after recomm point in this day timeline");
		}

		else
		{
			ActivityObject activityJustAfterRecommPoint = currentDayTimeline
					.getActivityObjectAtPosition(indexOfRecommPointInDayTimeline + 1);
			if (activityJustAfterRecommPoint.getActivityName().equals(Constant.INVALID_ACTIVITY1)
					|| activityJustAfterRecommPoint.getActivityName().equals(Constant.INVALID_ACTIVITY2))
			{
				nextActivityJustAfterRecommPointIsInvalid = true;
			}
		}

		// the next activity just after recomm point is valid,i.e., there exists a valid actual activity happening after
		// current activity in current timeline. Thus, the current activity is to be removed from the list of
		// recommendations.
		if (!nextActivityJustAfterRecommPointIsInvalid)
		{
			// removeRecommPointActivityFromRankedRecomm();
			this.recommendedActivityNamesWithRankScores.remove(activityAtRecommPoint.getActivityName());
			System.out.println("removing recomm point activity");
		}

		WritingToFile.writeDistanceScoresSortedMap(this.userAtRecomm, this.dateAtRecomm, this.timeAtRecomm,
				this.editDistancesSortedMap, this.candidateTimelines, this.nextActivityNamesFromCandsByEditDistMap,
				this.activitiesGuidingRecomm);

		System.out.println("\n^^^^^^^^^^^^^^^^Exiting Recommendation Master");
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 
	 * @return
	 */
	public int getNumberOfValidActivitiesInActivitesGuidingRecommendation()
	{
		int count = 0;
		for (ActivityObject ae : this.activitiesGuidingRecomm)
		{
			if (!((ae.getActivityName().equalsIgnoreCase(Constant.INVALID_ACTIVITY2))
					|| (ae.getActivityName().equalsIgnoreCase(Constant.INVALID_ACTIVITY1))))
			{
				count++;
			}
		}
		return count++;
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberOfActivitiesInActivitesGuidingRecommendation()
	{
		return this.activitiesGuidingRecomm.size();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberOfCandidateTimelinesBelowThreshold() // satisfying threshold
	{
		// if(hasCandidateTimelinesBelowThreshold==false)
		// {
		// System.err.println("Error: Sanity Check RM60 failed: trying to get number of candidate timelines below
		// threshold while there is no candidate below threshold, u shouldnt
		// have called this function");
		// }
		return editDistancesSortedMap.size();
	}

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

	/**
	 * (Cand TimelineID, (End point index of least distant subsequence, String containing the trace of edit operations
	 * performed, edit distance of least distant subsequence)) this LinkedHashMap is sorted by the value of edit
	 * distance in ascending order (second component of the Pair (value))
	 * 
	 */
	public LinkedHashMap<Date, Triple<Integer, String, Double>> getDistanceScoresSorted()
	{
		return this.editDistancesSortedMap;
	}

	/**
	 * TODO: CHECK if this method is conceptually correct
	 * 
	 * @return
	 */
	public int getNumberOfDistinctRecommendations()
	{
		return recommendedActivityNamesWithRankScores.size();
	}

	public ActivityObject getActivityObjectAtRecomm()
	{
		return this.activityAtRecommPoint;
	}

	public String getActivitiesGuidingRecomm()
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

	/**
	 * 
	 * @param nextActivityNamesFromCandsByWithEditDist
	 * @return Triple(rankedRecommendedActivityNamesWithRankScoresString,
	 *         rankedRecommendedActivityNamesWithoutRankScoresString, recommendedActivityNamesWithRankScores)
	 */
	public Triple<String, String, LinkedHashMap<String, Double>> createRankedTopRecommendation(
			String nextActivityNamesFromCandsByWithEditDist)
	{
		String rankedRecommendedActivityNamesWithRankScoresString = new String();
		String rankedRecommendedActivityNamesWithoutRankScoresString = new String();

		String[] splitted1 = RegexUtils.patternDoubleUnderScore.split(nextActivityNamesFromCandsByWithEditDist);
		// nextActivityNamesFromCandsByWithEditDist.split("__");
		int numOfNextActNames = splitted1.length - 1;

		LinkedHashMap<String, Double> recommendedActivityNamesWithRankScores = new LinkedHashMap<>();
		Double maxEditDistOverCands = 0d;

		System.out.println("Debug inside createRankedTopRecommendation: nextActivityNamesFromCandsByWithEditDist="
				+ nextActivityNamesFromCandsByWithEditDist);
		System.out.println("Debug inside createRankedTopRecommendation: numOfNextActNames=splitted1.length-1="
				+ numOfNextActNames);
		System.out.print("Debug inside createRankedTopRecommendation: the read recommendation are:  ");

		// get the max edit distance over these candidates
		for (int i = 1; i < splitted1.length; i++)
		{
			String[] nextActivityNameStringSplitted = RegexUtils.patternColon.split(splitted1[i]);
			// splitted1[i].split(Pattern.quote(":"));
			Double editDist = Double.valueOf(nextActivityNameStringSplitted[1]);
			if (editDist > maxEditDistOverCands)
			{
				maxEditDistOverCands = editDist;
			}
		}

		for (int i = 1; i < splitted1.length; i++)
		{
			String[] nextActivityNameStringSplitted = RegexUtils.patternColon.split(splitted1[i]);
			// splitted1[i].split(Pattern.quote(":"));
			String nextActivityName = nextActivityNameStringSplitted[0];
			Double editDist = Double.valueOf(nextActivityNameStringSplitted[1]);
			System.out.print(nextActivityName + "  ");
			Double rankScore = 1d - (editDist / maxEditDistOverCands);

			if (recommendedActivityNamesWithRankScores.containsKey(nextActivityName) == false)
			{
				recommendedActivityNamesWithRankScores.put(nextActivityName, rankScore);
			}

			else
			{
				recommendedActivityNamesWithRankScores.put(nextActivityName,
						recommendedActivityNamesWithRankScores.get(nextActivityName) + rankScore);
			}
		}

		System.out.println();
		recommendedActivityNamesWithRankScores = (LinkedHashMap<String, Double>) ComparatorUtils
				.sortByValueDesc(recommendedActivityNamesWithRankScores);
		// Sorted in descending order of ranked score: higher ranked score means higher rank

		// this.recommendationRankscorePairs = recommendationRankscorePairs;

		// for (Map.Entry<String, Double> entry : recommendationRankscorePairs.entrySet())
		// {
		// topRankedString+= "__"+entry.getKey()+":"+TestStats.round(entry.getValue(),4);
		// }
		//
		// this.rankedRecommendationWithRankScores =topRankedString;

		// String rankedRecommendationWithoutRankScores=new String();
		// for (Map.Entry<String, Double> entry : recommendationRankscorePairs.entrySet())
		// {
		// rankedRecommendationWithoutRankScores+= "__"+entry.getKey();
		// }
		//
		// this.rankedRecommendationWithoutRankScores = rankedRecommendationWithoutRankScores;
		//
		rankedRecommendedActivityNamesWithRankScoresString = convertRankedRecommendationWithRankScoresAsString(
				recommendedActivityNamesWithRankScores);
		rankedRecommendedActivityNamesWithoutRankScoresString = convertRankedRecommendationWithoutRankScoresAsString(
				recommendedActivityNamesWithRankScores);
		System.out.println("Debug inside createRankedTopRecommendation: topRankedString=  "
				+ rankedRecommendedActivityNamesWithRankScoresString);
		System.out.println("Debug inside createRankedTopRecommendation: topRankedStringWithoutScore=  "
				+ rankedRecommendedActivityNamesWithoutRankScoresString);

		// /return topRankedActivityNamesString;
		return new Triple<String, String, LinkedHashMap<String, Double>>(
				rankedRecommendedActivityNamesWithRankScoresString,
				rankedRecommendedActivityNamesWithoutRankScoresString, recommendedActivityNamesWithRankScores);

	}

	// /**
	// * Not used anymore
	// */
	// public void removeRecommPointActivityFromRankedRecomm()
	// {
	// String activityNameAtRecommPoint = activityAtRecommPoint.getActivityName();
	//
	// this.recommendedActivityNamesWithRankScores.remove(activityNameAtRecommPoint);
	// // I can use this instead of this method:
	// this.recommendedActivityNamesWithRankScores.remove(activityAtRecommPoint.getActivityName());
	// }
	//

	public String convertRankedRecommendationWithRankScoresAsString(
			LinkedHashMap<String, Double> recommendationRankscorePairs)
	{
		StringBuilder topRankedString = new StringBuilder();
		for (Map.Entry<String, Double> entry : recommendationRankscorePairs.entrySet())
		{
			topRankedString.append(
					"__" + entry.getKey() + ":" + Evaluation.round(entry.getValue(), Constant.RoundingPrecision));
		}
		// this.rankedRecommendationWithRankScores = topRankedString;
		return topRankedString.toString();
	}

	public String getRankedRecommendationWithRankScoresAsString()
	{
		return this.rankedRecommendationWithRankScores;
	}

	public String convertRankedRecommendationWithoutRankScoresAsString(
			LinkedHashMap<String, Double> recommendationRankscorePairs)
	{
		StringBuilder rankedRecommendationWithoutRankScores = new StringBuilder();
		for (Map.Entry<String, Double> entry : recommendationRankscorePairs.entrySet())
		{
			rankedRecommendationWithoutRankScores.append("__" + entry.getKey());
		}
		// this.rankedRecommendationWithoutRankScores = rankedRecommendationWithoutRankScores;
		return rankedRecommendationWithoutRankScores.toString();
	}

	public String getRankedRecommendationWithoutRankScoresAsString()
	{
		return this.rankedRecommendationWithoutRankScores;
	}

	public LinkedHashMap<String, Double> getRecommendationRankscorePairs()
	{
		return this.recommendedActivityNamesWithRankScores;
	}

	public boolean hasCandidateTimeslines()
	{
		return hasCandidateTimelines;
	}

	/**
	 * Useful for mu breaking of timelines to find if a particular rt will have recommendation for daywise approach so
	 * that they can be compared
	 * 
	 * @param trainingTimelines
	 * @param activitiesGuidingRecomm
	 * @param dateAtRecomm
	 * @param activityAtRecommPoint
	 * @return
	 */
	public static boolean hasDaywiseCandidateTimelines(LinkedHashMap<Date, UserDayTimeline> trainingTimelines,
			ArrayList<ActivityObject> activitiesGuidingRecomm, Date dateAtRecomm, ActivityObject activityAtRecommPoint)
	{
		LinkedHashMap<Date, UserDayTimeline> candidateTimelines = TimelineUtils.extractDaywiseCandidateTimelines(
				trainingTimelines, activitiesGuidingRecomm, dateAtRecomm, activityAtRecommPoint);
		if (candidateTimelines.size() > 0)
			return true;
		else
			return false;
	}

	/*
	 * public String getSingleNextRecommendedActivity() { return this.singleNextRecommendedActivity; }
	 */

	public String getNextActivityNamesByEditDist() // old name: getTopFiveRecommendedActivities()
	{
		return this.nextActivityNamesFromCandsByEditDistString;
	}

	// getTopNextActivityNamesWithoutDistanceString
	public String getTopRecommendedActivityNamesWithoutDistanceString()// old name
																		// getTopRecommendedActivitiesWithoutDistance()
	{
		String result = "";
		String topActivities = this.nextActivityNamesFromCandsByEditDistString;
		String[] splitted1 = topActivities.split("__");

		for (int i = 1; i < splitted1.length; i++)
		{
			String[] splitted2 = splitted1[i].split(":");
			result = result + "__" + splitted2[0];
		}

		return result;
	}

	// //

	// public int getEndPointIndexOfSubsequenceAsCodeWithHighestSimilarity(String activitySequenceAsCode, String
	// activitiesGuidingAsStringCode)

	// getTopFivWithDistanceeRecommendedActivities getTopNextRecommendedActivityNamesWithDistanceEditDistance

	// IMPORTANT : returns all posssibilities , not just top 5 but top as much as possible
	/**
	 * NOT USED: Return the next activity names for each candidate with the decreasing order of similarity to the
	 * current timeline, i.e., increasing order of distance. (NOTE: may be we can do away with this method in future as
	 * this and the next method below is similar except return type, not sure) Removed as the next method can do the
	 * same thing if returns pair
	 * 
	 * @param activitiesGuidingRecomm
	 * @param distanceScoresSorted
	 *            edit distances sorted in ascending order
	 * @param dayTimelinesForUser
	 * @return
	 */
	public String getNextActivityNamesFromCandsByEditDistances(ArrayList<ActivityObject> activitiesGuidingRecomm,
			LinkedHashMap<Date, Triple<Integer, String, Double>> distanceScoresSorted,
			LinkedHashMap<Date, UserDayTimeline> dayTimelinesForUser)
	{
		String nextActivityNames = new String();
		// LinkedHashMap<Date,String> topNextForThisCand= new LinkedHashMap<Date,String>();
		UserDayTimeline candUserDayTimeline = null;
		System.out.println("\n-----------------Inside getNextRecommendedActivityNamesEditDistance");
		int nextCount = 0;

		if (distanceScoresSorted.size() < 5)
		{
			System.err
					.println("\nWarning: the number of similar timelines =" + distanceScoresSorted.size() + " < " + 5);
		}

		for (Map.Entry<Date, Triple<Integer, String, Double>> distEntryOfCand : distanceScoresSorted.entrySet())
		{
			int endPointIndexOfCand = distEntryOfCand.getValue().getFirst();
			Double distanceOfCandTimeline = distEntryOfCand.getValue().getThird();
			Date dateOfCandTimeline = distEntryOfCand.getKey();

			// fetching the candidate timeline again
			candUserDayTimeline = TimelineUtils.getUserDayTimelineByDateFromMap(dayTimelinesForUser,
					dateOfCandTimeline);

			ActivityObject nextValidAO = candUserDayTimeline
					.getNextValidActivityAfterActivityAtThisPosition(endPointIndexOfCand);

			// topNextForThisCand.put(distEntryOfCand.getKey(), nextValidAO.getActivityName());

			nextActivityNames = nextActivityNames + "__" + nextValidAO.getActivityName() + ":" + distanceOfCandTimeline;
			nextCount++;

			System.out.println("-----------------------------------------------");
		}

		System.out.println("-------exiting getNextRecommendedActivityNamesEditDistance\n");
		return nextActivityNames;
	}

	/**
	 * 
	 * @param activitiesGuidingRecomm
	 * @param distanceScoresSorted
	 * @param dayTimelinesForUser
	 * @return
	 */
	public Pair<String, LinkedHashMap<Date, String>> getNextActivityNamesFromCandsByEditDistance(
			ArrayList<ActivityObject> activitiesGuidingRecomm,
			LinkedHashMap<Date, Triple<Integer, String, Double>> distanceScoresSorted,
			LinkedHashMap<Date, UserDayTimeline> dayTimelinesForUser)
	{
		LinkedHashMap<Date, String> nextActivityNamesFromCandsByEditDistMap = new LinkedHashMap<Date, String>();
		String nextActivityNamesFromCandsByEditDistString = new String();

		// this.nextActivityNamesFromCandsByEditDist = new LinkedHashMap<Date, String>();
		// LinkedHashMap<Date,String> topNextForThisCand= new LinkedHashMap<Date,String>();

		UserDayTimeline candUserDayTimeline = null;
		System.out.println("\n-----------------Inside getNextActivityNamesFromCandsByEditDistance");
		int nextActCount = 0;

		if (distanceScoresSorted.size() < 5)
		{
			System.err
					.println("\nWarning: the number of similar timelines =" + distanceScoresSorted.size() + " < " + 5);
		}

		for (Map.Entry<Date, Triple<Integer, String, Double>> distEntryOfCand : distanceScoresSorted.entrySet())
		{
			int endPointIndexOfCand = distEntryOfCand.getValue().getFirst();
			Double distanceOfCandTimeline = distEntryOfCand.getValue().getThird();
			Date dateOfCandTimeline = distEntryOfCand.getKey();

			candUserDayTimeline = TimelineUtils.getUserDayTimelineByDateFromMap(dayTimelinesForUser,
					dateOfCandTimeline);

			ActivityObject nextValidAO = candUserDayTimeline
					.getNextValidActivityAfterActivityAtThisPosition(endPointIndexOfCand);

			nextActivityNamesFromCandsByEditDistString += "__" + nextValidAO.getActivityName() + ":"
					+ distanceOfCandTimeline;

			nextActivityNamesFromCandsByEditDistMap.put(dateOfCandTimeline,
					nextValidAO.getActivityName() + ":" + distanceOfCandTimeline);

			nextActCount++;

			// System.out.println("-----------------------------------------------");
		}

		System.out.println("-------exiting getNextActivityNamesFromCandsByEditDistance\n");
		// return nextActivityNamesFromCandsByEditDist;
		return new Pair<String, LinkedHashMap<Date, String>>(nextActivityNamesFromCandsByEditDistString,
				nextActivityNamesFromCandsByEditDistMap);
	}

	/**
	 * 
	 * @param topSimilarUserDayActivitiesAsStringCode
	 * @param endPointIndexForSubsequenceWithHighestSimilarity
	 * @return
	 */
	public static String getNextValidActivityNameAsCode(String topSimilarUserDayActivitiesAsStringCode,
			int endPointIndexForSubsequenceWithHighestSimilarity)
	{
		String unknownAsCode = "", othersAsCode = "";
		if (Constant.hasInvalidActivityNames)
		{
			unknownAsCode = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY1);
			othersAsCode = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY2);
		}

		String nextValidActivityAsCode = null;

		for (int i = endPointIndexForSubsequenceWithHighestSimilarity + 1; i < topSimilarUserDayActivitiesAsStringCode
				.length(); i++)
		{
			String actNameStringCode = String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i));

			if ((Constant.hasInvalidActivityNames)
					&& (actNameStringCode.equals(unknownAsCode) || actNameStringCode.equals(othersAsCode)))
			{
				continue;
			}
			else
			{
				nextValidActivityAsCode = actNameStringCode;
				break;// Added on 27th October.... ALERT
			}
		}
		return nextValidActivityAsCode;
	}

	/**
	 * Returns the edit distance for each candidate timeline. For multiple occurrence of the current activity in
	 * candidate timeline, the least distance from current timeline is considered
	 * 
	 * @param candidateTimelines
	 * @param activitiesGuidingRecomm
	 * @param userAtRecomm
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @return (Cand TimelineID, (End point index of least distant subsequence, String containing the trace of edit
	 *         operations performed, edit distance of least distant subsequence))
	 */
	public LinkedHashMap<Date, Triple<Integer, String, Double>> getEditDistancesForCandidateTimelines(
			LinkedHashMap<Date, UserDayTimeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm,
			String userAtRecomm, String dateAtRecomm, String timeAtRecomm)
	{
		// <Date of CandidateTimeline, (End point index of least distant subsequence, String containing the trace of
		// edit operations performed, edit distance of
		// least distant subsequence)>
		LinkedHashMap<Date, Triple<Integer, String, Double>> distancesRes = new LinkedHashMap<>();

		for (Map.Entry<Date, UserDayTimeline> candidate : candidateTimelines.entrySet())
		{
			UserDayTimeline candidateTimeline = candidate.getValue();
			// similarityScores.put(entry.getKey(), getSimilarityScore(entry.getValue(),activitiesGuidingRecomm));
			// (Activity Events in Candidate Day, activity events on or before recomm on recomm day)
			Long candTimelineID = candidate.getKey().getTime();// used as dummy, not exactly useful right now
			Triple<Integer, String, Double> score = getDistanceScoreModifiedEdit(candidateTimeline,
					activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm, candTimelineID);

			distancesRes.put(candidate.getKey(), score);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}

		return distancesRes;
	}

	/**
	 * Get distance scores using modified edit distance.
	 * 
	 * <end point index, edit operations trace, edit distance> The distance score is the distance between the activities
	 * guiding recommendation and the least distant subcandidate (which has a valid activity after it) from the
	 * candidate timeline. (subcandidate is a subsequence from candidate timeline, from the start of the candidate
	 * timeline to any occurrence of the ActivityGuiding Recomm or current activity.
	 * 
	 * @param userDayTimeline
	 * @param activitiesGuidingRecomm
	 * @return
	 */
	public Triple<Integer, String, Double> getDistanceScoreModifiedEdit(UserDayTimeline userDayTimeline,
			ArrayList<ActivityObject> activitiesGuidingRecomm, String userAtRecomm, String dateAtRecomm,
			String timeAtRecomm, Long candidateID)
	{
		System.out.println("Inside getDistanceScoreModifiedEdit");
		// find the end points in the userDayTimeline
		// ///////

		String activityAtRecommPointAsStringCode = String
				.valueOf(activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1).getStringCode());
		String activitiesGuidingAsStringCode = StringCode.getStringCodeForActivityObjects(activitiesGuidingRecomm);
		String userDayTimelineAsStringCode = userDayTimeline.getActivityObjectsAsStringCode();

		// IMPORTANAT CHANGE on 24 OCt
		// old
		ArrayList<Integer> indicesOfEndPointActivityInDay1 = getIndicesOfEndPointActivityInDayButNotAsLast(
				userDayTimelineAsStringCode, activityAtRecommPointAsStringCode);

		// new
		ArrayList<Integer> indicesOfEndPointActivityInDay = getIndicesOfEndPointActivityInDayButNotLastValid(
				userDayTimelineAsStringCode, activityAtRecommPointAsStringCode);

		if (indicesOfEndPointActivityInDay.equals(indicesOfEndPointActivityInDay1) == false)
		{
			PopUps.showMessage(
					"Alert in getDistanceScoreModifiedEdit: indicesOfEndPointActivityInDay.equals(indicesOfEndPointActivityInDay1) == false");
		}
		// ///
		// $$WritingToFile.writeEndPoinIndexCheck24Oct(activityAtRecommPointAsStringCode,userDayTimelineAsStringCode,indicesOfEndPointActivityInDay1,indicesOfEndPointActivityInDay);

		// System.out.println("Debug oct 24 1pm: indicesOfEndPointActivityInDay1" + indicesOfEndPointActivityInDay1);
		// System.out.println("Debug oct 24 1pm: indicesOfEndPointActivityInDay" + indicesOfEndPointActivityInDay);
		// System.out.println(
		// "same=" + indicesOfEndPointActivityInDay1.toString().equals(indicesOfEndPointActivityInDay.toString()));
		// //////

		/** index of end point, edit operations trace, edit distance **/
		LinkedHashMap<Integer, Pair<String, Double>> distanceScoresForEachSubsequence = new LinkedHashMap<>();

		// getting distance scores for each subcandidate
		for (int i = 0; i < indicesOfEndPointActivityInDay1.size(); i++)
		{
			HJEditDistance editSimilarity = new HJEditDistance();

			Pair<String, Double> distance = editSimilarity.getHJEditDistanceWithTrace(
					userDayTimeline.getActivityObjectsInDayFromToIndex(0, indicesOfEndPointActivityInDay1.get(i) + 1),
					activitiesGuidingRecomm, userAtRecomm, dateAtRecomm, timeAtRecomm, candidateID);
			/*
			 * public final Pair<String, Double> getHJEditDistanceWithTrace(ArrayList<ActivityObject>
			 * activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original, String userAtRecomm, String
			 * dateAtRecomm, String timeAtRecomm, Integer candidateTimelineId)
			 */
			distanceScoresForEachSubsequence.put(indicesOfEndPointActivityInDay1.get(i), distance);

			// System.out.println("Distance between:\n
			// activitiesGuidingRecomm:"+UtilityBelt.getActivityNamesFromArrayList(activitiesGuidingRecomm)+
			// "\n and subsequence of
			// Cand:"+UtilityBelt.getActivityNamesFromArrayList(userDayTimeline.getActivityObjectsInDayFromToIndex(0,indicesOfEndPointActivityInDay1.get(i)+1)));
		}
		// ///////////////////

		distanceScoresForEachSubsequence = (LinkedHashMap<Integer, Pair<String, Double>>) ComparatorUtils
				.sortByValueAscendingIntStrDoub(distanceScoresForEachSubsequence);
		// $$WritingToFile.writeEditDistancesOfAllEndPoints(activitiesGuidingRecomm,userDayTimeline,distanceScoresForEachSubsequence);

		// we only consider the most similar subsequence . i.e. the first entry in this Map
		for (Map.Entry<Integer, Pair<String, Double>> entry1 : distanceScoresForEachSubsequence.entrySet())
		{
			System.out.println("End point= " + entry1.getKey() + " distance=" + entry1.getValue().getSecond());
			// +"trace="+distance.getFirst());
		}

		if (distanceScoresForEachSubsequence.size() == 0)
		{
			System.err.println("Error in getDistanceScoreModifiedEdit: no subsequence to be considered for similarity");
		}

		int endPointIndexForSubsequenceWithHighestSimilarity = -1;
		double distanceScoreForSubsequenceWithHighestSimilarity = -9999;
		String traceEditOperationsForSubsequenceWithHighestSimilarity = "";

		// finding the end point index with highest similarity WHICH HAS A VALID ACTIVITY AFTER IT
		for (Map.Entry<Integer, Pair<String, Double>> entry1 : distanceScoresForEachSubsequence.entrySet())
		{// we only consider the most similar timeline . i.e. the first entry in this Map
			endPointIndexForSubsequenceWithHighestSimilarity = entry1.getKey().intValue();

			if (getNextValidActivityNameAsCode(userDayTimelineAsStringCode,
					endPointIndexForSubsequenceWithHighestSimilarity) == null)
			{// there DOES NOT exists a valid activity after this end point
				continue;
			}

			else
			{
				endPointIndexForSubsequenceWithHighestSimilarity = entry1.getKey().intValue();
				traceEditOperationsForSubsequenceWithHighestSimilarity = entry1.getValue().getFirst();
				distanceScoreForSubsequenceWithHighestSimilarity = entry1.getValue().getSecond().doubleValue();
				break;
			}
		}

		// to check: remove later
		System.out.println("end points indices not last:" + indicesOfEndPointActivityInDay1.toString());
		System.out.println("end point indices not last valid:" + indicesOfEndPointActivityInDay.toString());

		indicesOfEndPointActivityInDay1.removeAll(indicesOfEndPointActivityInDay);
		System.out.println("Contained in not last but no valid after= " + indicesOfEndPointActivityInDay1);

		if (indicesOfEndPointActivityInDay1.contains(endPointIndexForSubsequenceWithHighestSimilarity))
		{
			System.out.println(
					"ajooba: the end point index with highest similarity has not valid activity after it in day");
		}
		// /////

		System.out.println("---Debug: getDistanceScoreModifiedEdit---- ");
		System.out.println("activitiesGuidingAsStringCode=" + activitiesGuidingAsStringCode);
		System.out.println("activityAtRecommPointAsStringCode=" + activityAtRecommPointAsStringCode);
		System.out.println("userDayTimelineAsStringCode=" + userDayTimelineAsStringCode);

		System.out.println(
				"endPointIndexForSubsequenceWithHighestSimilarity=" + endPointIndexForSubsequenceWithHighestSimilarity);
		System.out.println(
				"distanceScoreForSubsequenceWithHighestSimilarity=" + distanceScoreForSubsequenceWithHighestSimilarity);

		distanceScoreForSubsequenceWithHighestSimilarity = StatsUtils
				.round(distanceScoreForSubsequenceWithHighestSimilarity, Constant.RoundingPrecision);

		return new Triple(endPointIndexForSubsequenceWithHighestSimilarity,
				traceEditOperationsForSubsequenceWithHighestSimilarity,
				distanceScoreForSubsequenceWithHighestSimilarity);
	}

	/**
	 * 
	 * @param userDayActivitiesAsStringCode
	 * @param codeOfEndPointActivity
	 * @return
	 */
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

	/**
	 * Indices of current activity occurrence with atleast one valid activity after it.
	 * 
	 * @param userDayActivitiesAsStringCode
	 * @param codeOfEndPointActivity
	 * @return
	 */
	public ArrayList<Integer> getIndicesOfEndPointActivityInDayButNotLastValid(String userDayActivitiesAsStringCode,
			String codeOfEndPointActivity)
	{
		System.out.println("\nDebug getIndicesOfEndPointActivityInDayButNotLastValid: userDayActivitiesAsStringCode="
				+ userDayActivitiesAsStringCode + "  and codeOfEndPointActivity=" + codeOfEndPointActivity);

		// get indices of valid activity ActivityNames
		ArrayList<Integer> indicesOfValids = new ArrayList<Integer>();

		if (Constant.hasInvalidActivityNames)
		{ // find the indices of valid acts
			String codeUn = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY1);
			String codeO = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY2);

			for (int i = 0; i < userDayActivitiesAsStringCode.length(); i++)
			{
				String codeToCheck = userDayActivitiesAsStringCode.substring(i, i + 1); // only one character
				// here codeToCheck is of length 1, hence, using endsWith or equals below shouldn't make difference
				// sanity checked in org.activity.sanityChecks.TestDummy2.checkString1()
				if (codeToCheck.endsWith(codeUn) || codeToCheck.equals(codeO))
				{
					continue;
				}
				else
					indicesOfValids.add(i);
			}
		}
		// all indices are valid acts
		{
			indicesOfValids = (ArrayList<Integer>) IntStream.rangeClosed(0, userDayActivitiesAsStringCode.length() - 1)
					.boxed().collect(Collectors.toList());
		}

		ArrayList<Integer> endPoints = new ArrayList<Integer>();

		for (int i = 0; i < indicesOfValids.size() - 1; i++)
		{ // skip the last valid because there is no valid activity to recommend after that.
			int indexOfValid = indicesOfValids.get(i);
			if (userDayActivitiesAsStringCode.substring(indexOfValid, indexOfValid + 1).equals(codeOfEndPointActivity))
			{
				endPoints.add(indexOfValid);
			}
		}

		System.out.println("indices of valids=" + indicesOfValids);
		System.out.println("end points considered" + endPoints);

		return endPoints;
	}

	/**
	 * 
	 * @return LinkedHashMap<Date, UserDayTimeline>
	 */
	public LinkedHashMap<Date, UserDayTimeline> getCandidateTimeslines()
	{
		return this.candidateTimelines;
	}

}
