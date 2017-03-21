package org.activity.recomm;

import java.util.ArrayList;
import java.util.Set;

import org.activity.objects.ActivityObject;
import org.activity.objects.Timeline;

public interface RecommendationMasterI
{
	public int getNumOfCandidateTimelines();

	public int getNumberOfActivitiesInActivitesGuidingRecommendation();

	public int getNumberOfValidActivitiesInActivitesGuidingRecommendation();

	public Timeline getCandidateTimesline(String timelineID);

	public ArrayList<Timeline> getOnlyCandidateTimeslines();

	public Set getCandidateTimelineIDs();

	public boolean hasCandidateTimeslines();

	public boolean hasCandidateTimelinesBelowThreshold();

	public boolean hasThresholdPruningNoEffect();

	public boolean isNextActivityJustAfterRecommPointIsInvalid();

	public ActivityObject getActivityObjectAtRecomm();

	public double getThresholdAsDistance();

	public int getNumberOfCandidateTimelinesBelowThreshold();

	public String getTopNextActivityNamesWithDistanceString();

	public String getTopNextActivityNamesWithoutDistanceString();

	public String getActivityNamesGuidingRecommwithTimestamps();

	public String getRankedRecommendedActivityNamesWithoutRankScores();

	public String getRankedRecommendedActivityNamesWithRankScores();

	public int getNumberOfDistinctRecommendations();

	/**
	 * not implemented in daywise and baseline st. yet. relevant for case-based approach
	 * 
	 * @return
	 */
	public double getAvgEndSimilarity();

	//
	// public String getRankedRecommendationWithRankScores();

}
