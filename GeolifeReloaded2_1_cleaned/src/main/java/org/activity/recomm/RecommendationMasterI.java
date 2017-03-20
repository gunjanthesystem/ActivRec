package org.activity.recomm;

import org.activity.objects.ActivityObject;

public interface RecommendationMasterI
{
	// public LinkedHashMap<?, TimelineI> getCandidateTimeslines();
	public boolean hasCandidateTimeslines();

	public boolean hasCandidateTimelinesBelowThreshold();

	public boolean hasThresholdPruningNoEffect();

	public ActivityObject getActivityObjectAtRecomm();

	public double getThresholdAsDistance();

	public int getNumberOfCandidateTimelinesBelowThreshold();

	public String getActivityNamesGuidingRecommwithTimestamps();

	public String getRankedRecommendedActivityNamesWithoutRankScores();

	public String getRankedRecommendedActivityNamesWithRankScores();

	//
	// public String getRankedRecommendationWithRankScores();

}
