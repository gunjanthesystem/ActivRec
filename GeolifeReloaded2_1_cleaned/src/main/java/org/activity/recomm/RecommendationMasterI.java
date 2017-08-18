package org.activity.recomm;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;

/**
 * 
 * @author gunjan
 *
 */
public interface RecommendationMasterI
{
	public int getNumOfCandidateTimelines();

	public int getNumOfActsInActsGuidingRecomm();

	public int getNumOfValidActsInActsGuidingRecomm();

	public Timeline getCandidateTimeline(String timelineID);

	public ArrayList<Timeline> getOnlyCandidateTimeslines();

	public Set<String> getCandidateTimelineIDs();

	public boolean hasCandidateTimeslines();

	public boolean hasCandidateTimelinesBelowThreshold();

	public boolean hasThresholdPruningNoEffect();

	public boolean isNextActivityJustAfterRecommPointIsInvalid();

	public ActivityObject getActivityObjectAtRecomm();

	public ArrayList<ActivityObject> getActsGuidingRecomm();

	public int getNumOfCandTimelinesBelowThresh();

	public String getActivityNamesGuidingRecommwithTimestamps();

	public String getActivityNamesGuidingRecomm();

	public String getRankedRecommendedActNamesWithoutRankScores();

	public String getRankedRecommendedActNamesWithRankScores();

	public int getNumOfDistinctRecommendations();

	// Specific for distance based approaches: could be later taken out into a separate interface
	public double getThresholdAsDistance();

	public String getNextActNamesWithDistString();

	public String getNextActNamesWithoutDistString();

	public LinkedHashMap<String, Pair<String, Double>> getDistancesSortedMap();

	public LinkedHashMap<String, Integer> getEndPointIndicesConsideredInCands();

	public Date getDateAtRecomm();

	public LinkedHashMap<String, String> getCandUserIDs();

	// /**
	// * not implemented in daywise and baseline st. yet. relevant for case-based approach
	// *
	// * @return
	// */
	// public double getAvgEndSimilarity();

	//
	// public String getRankedRecommendationWithRankScores();

}
