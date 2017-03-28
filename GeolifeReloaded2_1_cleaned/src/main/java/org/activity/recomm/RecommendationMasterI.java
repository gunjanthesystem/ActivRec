package org.activity.recomm;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;

public interface RecommendationMasterI
{
	public int getNumOfCandidateTimelines();

	public int getNumberOfActivitiesInActivitesGuidingRecommendation();

	public int getNumberOfValidActivitiesInActivitesGuidingRecommendation();

	public Timeline getCandidateTimeline(String timelineID);

	public ArrayList<Timeline> getOnlyCandidateTimeslines();

	public Set<String> getCandidateTimelineIDs();

	public boolean hasCandidateTimeslines();

	public boolean hasCandidateTimelinesBelowThreshold();

	public boolean hasThresholdPruningNoEffect();

	public boolean isNextActivityJustAfterRecommPointIsInvalid();

	public ActivityObject getActivityObjectAtRecomm();

	public ArrayList<ActivityObject> getActivitiesGuidingRecomm();

	public int getNumberOfCandidateTimelinesBelowThreshold();

	public String getActivityNamesGuidingRecommwithTimestamps();

	public String getRankedRecommendedActivityNamesWithoutRankScores();

	public String getRankedRecommendedActivityNamesWithRankScores();

	public int getNumberOfDistinctRecommendations();

	// Specific for distance based approaches: could be later taken out into a separate interface
	public double getThresholdAsDistance();

	public String getNextActivityNamesWithDistanceString();

	public String getNextActivityNamesWithoutDistanceString();

	public LinkedHashMap<String, Pair<String, Double>> getDistancesSortedMap();

	public LinkedHashMap<String, Integer> getEndPointIndicesConsideredInCands();

	public Date getDateAtRecomm();

	// /**
	// * not implemented in daywise and baseline st. yet. relevant for case-based approach
	// *
	// * @return
	// */
	// public double getAvgEndSimilarity();

	//
	// public String getRankedRecommendationWithRankScores();

}
