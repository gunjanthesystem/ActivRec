package org.activity.recomm;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.VerbosityConstants;
import org.activity.evaluation.Evaluation;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.TimelineExtractors;
import org.activity.util.TimelineUtils;

/**
 * Fork of org.activity.recomm.RecommendationMasterBaseClosestTime to create a cleaner and better version (Decided in
 * meeting on Oct 13, 2014) </br>
 * The primary purpose of this class is to provide a simple recommender which will be used as a baseline to compare our
 * proposed two-level recommender </br>
 * Algorithm in words:
 * <ol>
 * <li>For each RT, the candidate timelines are all timelines in the training.</li>
 * <li>To get the top recommended activity: from each candidate timeline, select candidate Activity Objects which is the
 * valid Activity Object whose start time is nearest to RT.</li>
 * <li>Measure the similarity of each of the candidate activity object as 1- min( 1, |Stcand - RT|/60mins)</li>
 * <li>The recommended item is a list ranked by score, ScoreAO, scores of activity objects of same activity name are
 * aggregated</li>
 * </ol>
 * Score (A<sub>O</sub>) = ∑ { 1- min( 1, |Stcand - RT|/60mins) } </br>
 * Score (A<sub>O</sub>) = &#8721; { 1- min( 1, |Stcand - RT|/60mins) } </br>
 * 
 * @since October 17, 2014
 * @author gunjan
 *
 */
public class RecommendationMasterBaseClosestTimeMar2017 // implements RecommendationMasterI
{
	LinkedHashMap<Date, Timeline> trainingTimelines, testTimelines, candidateTimelines;

	Timeline userDayTimelineAtRecomm;

	Date dateAtRecomm;
	Time timeAtRecomm;
	String userAtRecomm;
	String userIDAtRecomm;
	Timestamp timestampPointAtRecomm;

	ArrayList<ActivityObject> activitiesGuidingRecomm; // sequence of activity events happening one or before the recomm
														// point on that day
	ActivityObject activityAtRecommPoint;

	/**
	 * {Cand Date, Pair{Index of ST nearest AO, distance as diff of this ST with ST of CO}}
	 * 
	 * distance is the difference of the start time of the Current Activity Object and the Activity Object from the
	 * candidate timelines whose start time is nearest to the start time of current Activity Object. this LinkedHashMap
	 * is sorted by the value of distance in ascending order
	 * 
	 */
	private LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> startTimeDistanceSortedMap; //

	/** Recommended Activity names with their rank score */
	private LinkedHashMap<String, Double> recommendedActivityNamesWithRankscores;

	private String rankedRecommendedActNamesWithRankScores;
	private String rankedRecommendedActNamesWithoutRankScores;

	private boolean hasCandidateTimelines, errorExists, nextActivityJustAfterRecommPointIsInvalid;

	// public static String commonPath;// = Constant.commonPath;

	public int totalNumberOfProbableCands;
	public int numCandsRejectedDueToNoCurrentActivityAtNonLast;
	public int numCandsRejectedDueToNoNextActivity;

	// /// Dummy
	private double thresholdAsDistance = 99999999;

	// PARAMETER to set
	/**
	 * Score (A<sub>O</sub>) = ∑ { 1- min( 1, |Stcand - RT|/60mins) }
	 **/
	private final double timeInSecsForRankScoreNormalisation = 60 * 60; // 60 mins

	// ///
	/**
	 * 
	 * @param trainingTimelines
	 * @param testTimelines
	 * @param dateAtRecomm
	 * @param timeAtRecomm
	 * @param userAtRecomm
	 */
	public RecommendationMasterBaseClosestTimeMar2017(LinkedHashMap<Date, Timeline> trainingTimelines,
			LinkedHashMap<Date, Timeline> testTimelines, String dateAtRecomm, String timeAtRecomm, int userAtRecomm)
	// ,double // trainingPercentage)
	{
		System.out.println("\n^^^^^^^^^^^^^^^^Inside  RecommendationMasterBaseClosestTime");
		// commonPath = Constant.getCommonPath();
		hasCandidateTimelines = true;

		// dd/mm/yyyy // okay java.sql.Date with no hidden time
		this.dateAtRecomm = DateTimeUtils.getDateFromDDMMYYYY(dateAtRecomm, RegexUtils.patternForwardSlash);
		this.timeAtRecomm = Time.valueOf(timeAtRecomm);
		this.userAtRecomm = Integer.toString(userAtRecomm);
		this.userIDAtRecomm = Integer.toString(userAtRecomm);

		System.out.println("	User at Recomm = " + this.userAtRecomm + "\n	Date at Recomm = " + this.dateAtRecomm
				+ "\n	Time at Recomm = " + this.timeAtRecomm);

		userDayTimelineAtRecomm = TimelineUtils.getUserDayTimelineByDateFromMap(testTimelines, this.dateAtRecomm);

		if (userDayTimelineAtRecomm == null)
		{
			System.err.println("Error: day timeline not found (in test timelines) for the recommendation day");
		}

		// if (VerbosityConstants.verbose)
		{
			System.out.println("The Activities on the recomm day are:");
			userDayTimelineAtRecomm.printActivityObjectNamesWithTimestampsInSequence();
			System.out.println();
		}

		timestampPointAtRecomm = new Timestamp(this.dateAtRecomm.getYear(), this.dateAtRecomm.getMonth(),
				this.dateAtRecomm.getDate(), this.timeAtRecomm.getHours(), this.timeAtRecomm.getMinutes(),
				this.timeAtRecomm.getSeconds(), 0);
		System.out.println("timestampPointAtRecomm = " + timestampPointAtRecomm);

		this.activitiesGuidingRecomm = userDayTimelineAtRecomm
				.getActivityObjectsStartingOnBeforeTimeSameDay(timestampPointAtRecomm);
		this.activityAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1);
		System.out.println("\nActivity at Recomm point =" + this.activityAtRecommPoint.getActivityName());

		// /////IMPORTANT
		candidateTimelines = TimelineExtractors.extractDaywiseCandidateTimelines(trainingTimelines, this.dateAtRecomm,
				this.activityAtRecommPoint);// trainingTimelines;

		totalNumberOfProbableCands = candidateTimelines.size();
		numCandsRejectedDueToNoCurrentActivityAtNonLast = 0;
		numCandsRejectedDueToNoNextActivity = 0;
		// //////
		System.out.println("Number of candidate timelines =" + candidateTimelines.size());
		// System.out.println("the candidate timelines are as follows:");
		// $$traverseMapOfDayTimelines(candidateTimelines);
		if (candidateTimelines.size() == 0)
		{
			System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm
					+ " at time:" + timeAtRecomm + "  because there are no candidate timelines");
			hasCandidateTimelines = false;
			// $$this.topNextActivityObjects=null;
			this.startTimeDistanceSortedMap = null;
			return;
		}
		else
		{
			this.hasCandidateTimelines = true;
		}
		LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> startTimeDistanceUnsortedMap = getDistancesforCandidateTimeline(
				candidateTimelines, activityAtRecommPoint);
		// activitiesGuidingRecomm);

		// ########Sanity check
		if (startTimeDistanceUnsortedMap.size() != candidateTimelines.size())
		{
			System.err.println("Error at Sanity 161: startTimeDistanceUnsortedMap.size() != candidateTimelines.size()");
			errorExists = true;
		}
		// ##############

		this.startTimeDistanceSortedMap = (LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>>) ComparatorUtils
				.sortByValueAscending5(startTimeDistanceUnsortedMap);

		System.out.println("---------startTimeDistanceSortedMap.size()=" + startTimeDistanceSortedMap.size());
		if (VerbosityConstants.verbose)
		{
			System.out.println(
					">>>>>>>>>>>>>>startTimeDistanceSortedMap is:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n Date, Index of nearest, name of nearest, dist of nearest");
			StringBuilder temp1 = new StringBuilder();
			for (Map.Entry<Date, Triple<Integer, ActivityObject, Double>> entry : startTimeDistanceSortedMap.entrySet())
			{
				temp1.append("\t" + entry.getKey() + ":" + entry.getValue().getFirst() + "__"
						+ entry.getValue().getSecond().getActivityName() + "__" + entry.getValue().getThird() + "\n");
			}
			System.out.println(temp1.toString());
		}

		createRankedTopRecommendedActivityNames(startTimeDistanceSortedMap);

		WToFile.writeStartTimeDistancesSorted(this.startTimeDistanceSortedMap);
		System.out.println("\n^^^^^^^^^^^^^^^^Exiting RecommendationMasterBaseClosestTime");
	}

	/**
	 * Gets the start time distances of the (valid) Activity Object in each candidate timeline which is nearest to the
	 * start time of the current Activity Object
	 * 
	 * @param candidateTimelines
	 * @param activityObjectAtRecommPoint
	 * @return
	 */
	// used
	public LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> getDistancesforCandidateTimeline(
			LinkedHashMap<Date, Timeline> candidateTimelines, ActivityObject activityObjectAtRecommPoint)
	{
		// <Date of CandidateTimeline, <Index for the nearest Activity Object, Diff of Start time of nearest Activity
		// Object with start time of current Activity Object>
		LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> distances = new LinkedHashMap<>();

		for (Map.Entry<Date, Timeline> entry : candidateTimelines.entrySet())
		{
			/*
			 * For this cand timeline, find the Activity Object with start timestamp nearest to the start timestamp of
			 * current Activity Object and the distance is diff of their start times
			 */
			Triple<Integer, ActivityObject, Double> score = (entry.getValue()
					.getTimeDiffValidAOInDayWithStartTimeNearestTo(activityObjectAtRecommPoint.getStartTimestamp()));
			distances.put(entry.getKey(), score);
			// System.out.println("now we put "+entry.getKey()+" and score="+score);
		}
		return distances;
	}

	public LinkedHashMap<Date, Timeline> getCandidateTimeslines()
	{
		return this.candidateTimelines;
	}

	public Set getCandidateTimelineIDs()
	{
		return this.startTimeDistanceSortedMap.keySet();
	}

	public Timeline getCandidateTimeline(String timelineID)
	{
		List<Timeline> foundTimelines = this.candidateTimelines.entrySet().stream()
				.filter(e -> e.getValue().toString().equals(timelineID)).map(e -> e.getValue())
				.collect(Collectors.toList());
		if (foundTimelines.size() != 1)
		{
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in getCandidateTimesline(String timelineID): foundTimelines.size()=" + foundTimelines.size()
							+ " while expected 1"));
		}
		return foundTimelines.get(0);
	}

	public ArrayList<Timeline> getOnlyCandidateTimeslines()
	{
		return (ArrayList<Timeline>) this.candidateTimelines.entrySet().stream().map(e -> e.getValue())
				.collect(Collectors.toList());
	}

	public int getNumOfCandidateTimelines()
	{
		return this.candidateTimelines.size();
	}

	/**
	 * 
	 * @param startTimeDistanceSortedMap
	 */
	// used
	public void createRankedTopRecommendedActivityNames(
			LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> startTimeDistanceSortedMap)
	{
		System.out.println("Debug inside createRankedTopRecommendedActivityNames:");
		int numberOfTopNextActivityObjects = startTimeDistanceSortedMap.size();

		LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs = new LinkedHashMap<>(); // <ActivityName,RankScore>

		for (Map.Entry<Date, Triple<Integer, ActivityObject, Double>> entry : startTimeDistanceSortedMap.entrySet())
		{
			String topNextActivityName = entry.getValue().getSecond().getActivityName();
			Double rankScore = 1d - Math.min(1, (entry.getValue().getThird()) / timeInSecsForRankScoreNormalisation);
			// 60 * 60);

			if (recommendedActivityNamesRankscorePairs.containsKey(topNextActivityName) == false)
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName, rankScore);
			}
			else
			{
				recommendedActivityNamesRankscorePairs.put(topNextActivityName,
						recommendedActivityNamesRankscorePairs.get(topNextActivityName) + rankScore);
			}
		}
		System.out.println();
		recommendedActivityNamesRankscorePairs = (LinkedHashMap<String, Double>) ComparatorUtils
				.sortByValueDesc(recommendedActivityNamesRankscorePairs);
		// Sorted in descending order of ranked score: higher ranked score means higher value of rank

		// ///////////IMPORTANT //////////////////////////////////////////////////////////
		this.setRecommendedActivityNamesWithRankscores(recommendedActivityNamesRankscorePairs);
		this.setRankedRecommendedActivityNamesWithRankScores(recommendedActivityNamesRankscorePairs);
		this.setRankedRecommendedActivityNamesWithoutRankScores(recommendedActivityNamesRankscorePairs);

		// /////////////////////////////////////////////////////////////////////
		System.out.println("Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithScore=  "
				+ getRankedRecommendedActivityNamesWithRankScores());
		System.out
				.println("Debug inside createRankedTopRecommendedActivityObjects: topRankedActivityNamesWithoutScore=  "
						+ getRankedRecommendedActivityNamesWithoutRankScores());
	}

	// /////////////////////////////////////////////////////////////////////
	/**
	 * set Map of <recommended Activity Name, rank score>
	 * 
	 * @param recommendedActivityNamesRankscorePairs
	 */
	// used
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
	 * Set string as '__recommendedActivityName1:rankScore1__recommendedActivityName2:rankScore2'
	 * 
	 * @param recommendedActivityNameRankscorePairs
	 */
	// used
	public void setRankedRecommendedActivityNamesWithRankScores(
			LinkedHashMap<String, Double> recommendedActivityNameRankscorePairs)
	{
		StringBuilder topRankedString = new StringBuilder();// String topRankedString= new String();

		for (Map.Entry<String, Double> entry : recommendedActivityNameRankscorePairs.entrySet())
		{
			topRankedString.append("__" + entry.getKey() + ":" + Evaluation.round(entry.getValue(), 4));// topRankedString+=
																										// "__"+entry.getKey()+":"+TestStats.round(entry.getValue(),4);
		}

		this.rankedRecommendedActNamesWithRankScores = topRankedString.toString();
	}

	public String getRankedRecommendedActivityNamesWithRankScores()
	{
		return this.rankedRecommendedActNamesWithRankScores;
	}

	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	/**
	 * Set string as '__recommendedActivityName1__recommendedActivityName2'
	 * 
	 * @param recommendedActivityNameRankscorePairs
	 * @return
	 */
	// used
	public void setRankedRecommendedActivityNamesWithoutRankScores(
			LinkedHashMap<String, Double> recommendedActivityNameRankscorePairs)
	{
		StringBuilder rankedRecommendationWithoutRankScores = new StringBuilder();

		for (Map.Entry<String, Double> entry : recommendedActivityNameRankscorePairs.entrySet())
		{
			rankedRecommendationWithoutRankScores.append("__" + entry.getKey());
			// rankedRecommendationWithoutRankScores+= "__"+entry.getKey();
		}

		this.rankedRecommendedActNamesWithoutRankScores = rankedRecommendationWithoutRankScores.toString();
	}

	public String getRankedRecommendedActivityNamesWithoutRankScores()
	{
		return this.rankedRecommendedActNamesWithoutRankScores;
	}

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Returns top next activity names as String
	 * 
	 * @return
	 */
	public String getNextActivityNamesWithoutDistanceString()
	{
		StringBuffer result = new StringBuffer("");// String result="";

		for (Map.Entry<Date, Triple<Integer, ActivityObject, Double>> entry : this.startTimeDistanceSortedMap
				.entrySet())
		{
			Triple<Integer, ActivityObject, Double> tripleAO = entry.getValue();

			result.append("__" + tripleAO.getSecond().getActivityName());// result=result+"__"+pairAE.getFirst().getActivityName();

		}

		return result.toString();
	}

	/**
	 * Returns top next activity names with distance as String
	 * 
	 * @return
	 */
	public String getNextActivityNamesWithDistanceString()
	{
		StringBuffer result = new StringBuffer("");// String result="";

		for (Map.Entry<Date, Triple<Integer, ActivityObject, Double>> entry : this.startTimeDistanceSortedMap
				.entrySet())
		{
			Triple<Integer, ActivityObject, Double> tripleAO = entry.getValue();

			result.append("__" + tripleAO.getSecond().getActivityName() + ":" + tripleAO.getThird().toString());// result=result+"__"+pairAE.getFirst().getActivityName();
		}
		return result.toString();
	}

	public int getNumberOfValidActivitiesInActivitesGuidingRecommendation()
	{
		int count = 0;
		for (ActivityObject ae : this.activitiesGuidingRecomm)
		{
			if (!((ae.getActivityName().equalsIgnoreCase("Others"))
					|| (ae.getActivityName().equalsIgnoreCase("Unknown"))))
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

	public boolean isNextActivityJustAfterRecommPointIsInvalid()
	{
		nextActivityJustAfterRecommPointIsInvalid = false;
		int indexOfRecommPointInDayTimeline = userDayTimelineAtRecomm
				.getIndexOfActivityObjectAtTime(timestampPointAtRecomm);
		if (indexOfRecommPointInDayTimeline + 1 > userDayTimelineAtRecomm.getActivityObjectsInDay().size() - 1)
		{
			System.err.println(
					"Error(Almost Warning) in isNextActivityJustAfterRecommPointIsInvalid: No activity event after recomm point");
		}

		else
		{
			ActivityObject activityJustAfterRecommPoint = userDayTimelineAtRecomm
					.getActivityObjectAtPosition(indexOfRecommPointInDayTimeline + 1);
			if (activityJustAfterRecommPoint.getActivityName().equals("Unknown")
					|| activityJustAfterRecommPoint.getActivityName().equals("Others"))
			{
				nextActivityJustAfterRecommPointIsInvalid = true;
			}
		}
		return this.nextActivityJustAfterRecommPointIsInvalid;
	}

	public LinkedHashMap<Date, Triple<Integer, ActivityObject, Double>> getStartDistancesSortedMap()
	{
		return this.startTimeDistanceSortedMap;
	}

	public ActivityObject getActivityObjectAtRecomm()
	{
		return this.activityAtRecommPoint;
	}

	public String getActivityNamesGuidingRecomm()
	{
		StringBuilder res = new StringBuilder();
		for (ActivityObject ae : activitiesGuidingRecomm)
		{
			res.append(">>" + ae.getActivityName());
		}
		return res.toString();
	}

	public String getActivityNamesGuidingRecommwithTimestamps()
	{
		StringBuilder res = new StringBuilder();
		for (ActivityObject ae : activitiesGuidingRecomm)
		{
			res.append("  " + ae.getActivityName() + "__" + ae.getStartTimestamp() + "_to_" + ae.getEndTimestamp());
		}
		return res.toString();
	}

	public void removeRecommPointActivityFromRankedRecomm()
	{
		String activityNameAtRecommPoint = activityAtRecommPoint.getActivityName();
		this.recommendedActivityNamesWithRankscores.remove(activityNameAtRecommPoint);
	}

	public boolean hasCandidateTimeslines()
	{
		return hasCandidateTimelines;
	}

	public double getThresholdAsDistance()
	{
		return thresholdAsDistance;
	}

	public boolean hasThresholdPruningNoEffect()
	{
		return true;
	}

	public boolean hasCandidateTimelinesBelowThreshold()
	{
		return true;
	}

	public int getNumberOfCandidateTimelinesBelowThreshold()
	{
		return this.candidateTimelines.size();
	}

	/**
	 * TODO: CHECK if this method is conceptually correct
	 * 
	 * @return
	 */
	public int getNumberOfDistinctRecommendations()
	{
		return recommendedActivityNamesWithRankscores.size();
	}

	public double getAvgEndSimilarity()
	{
		return Double.NaN;
	}

	// /**
	// * Find Candidate timelines, which are the timelines which contain the activity at the recommendation point
	// (current Activity). Also, this candidate timeline must contain the
	// * activityAtRecomm point at non-last position and there is atleast one valid activity after this activityAtRecomm
	// point
	// *
	// * @param dayTimelinesForUser
	// * @param activitiesGuidingRecomm
	// * @return
	// */
	// public LinkedHashMap<Date, UserDayTimeline> getCandidateTimelines(LinkedHashMap<Date, UserDayTimeline>
	// dayTimelinesForUser,
	// ArrayList<ActivityObject> activitiesGuidingRecomm, Date dateAtRecomm)
	// {
	// LinkedHashMap<Date, UserDayTimeline> candidateTimelines = new LinkedHashMap<Date, UserDayTimeline>();
	// int count = 0;
	//
	// totalNumberOfProbableCands = 0;
	// numCandsRejectedDueToNoCurrentActivityAtNonLast = 0;
	// numCandsRejectedDueToNoNextActivity = 0;
	//
	// for (Map.Entry<Date, UserDayTimeline> entry : dayTimelinesForUser.entrySet())
	// {
	// totalNumberOfProbableCands += 1;
	//
	// // Check if the timeline contains the activityAtRecomm point at non-last and the timeline is not same for the day
	// to be recommended (this should nt be the case because
	// // test and trainin set
	// // are diffferent)
	// // and there is atleast one valid activity after this activityAtRecomm point
	// if (entry.getValue().countContainsActivityButNotAsLast(this.activityAtRecommPoint) > 0)// &&
	// (entry.getKey().toString().equals(dateAtRecomm.toString())==false))
	// {
	// if (entry.getKey().toString().equals(dateAtRecomm.toString()) == true)
	// {
	// System.err.println(
	// "Error: a prospective candidate timelines is of the same date as the dateToRecommend. Thus, not using training
	// and test set correctly");
	// continue;
	// }
	//
	// if (entry.getValue().containsOnlySingleActivity() == false
	// && entry.getValue().hasAValidActivityAfterFirstOccurrenceOfThisActivity(this.activityAtRecommPoint) == true)
	// {
	// candidateTimelines.put(entry.getKey(), entry.getValue());
	// count++;
	// }
	// else
	// numCandsRejectedDueToNoNextActivity += 1;
	// }
	// else
	// numCandsRejectedDueToNoCurrentActivityAtNonLast += 1;
	// }
	// if (count == 0)
	// System.err.println("No candidate timelines found");
	// return candidateTimelines;
	// }
	//
	/*
	 * public String getTopFiveRecommendedActivities() { return this.topRecommendedActivities; }
	 * 
	 * public String getTopRecommendedActivitiesWithoutDistance() { String result=""; String
	 * topActivities=this.topRecommendedActivities; String[] splitted1= topActivities.split("__");
	 * 
	 * for(int i=1;i<splitted1.length;i++) { String[] splitted2=splitted1[i].split(":");
	 * result=result+"__"+splitted2[0]; }
	 * 
	 * return result; }
	 */
	// //

	// public int getEndPointIndexOfSubsequenceAsCodeWithHighestSimilarity(String activitySequenceAsCode, String
	// activitiesGuidingAsStringCode)

	// getTopFivWithDistanceeRecommendedActivities getTopNextRecommendedActivityNamesWithDistanceEditDistance

	/*
	 * public String getTopNextRecommendedActivityNames(ArrayList<ActivityObject> activitiesGuidingRecomm,
	 * LinkedHashMap<Date,Double> distanceScoresSorted,LinkedHashMap<Date,UserDayTimeline> dayTimelinesForUser) {
	 * 
	 * }
	 */

	/*
	 * public void traverseMapOfKeyPair(LinkedHashMap<Date,Pair<Integer,Double>> map) { for (Map.Entry<Date,
	 * UserDayTimeline> entry : map.entrySet()) { System.out.print("Date: "+entry.getKey());
	 * entry.getValue().printActivityObjectNamesInSequence(); System.out.println(); } System.out.println("-----------");
	 * }
	 */
}
