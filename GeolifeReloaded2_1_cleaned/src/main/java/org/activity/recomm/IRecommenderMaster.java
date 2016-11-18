/**
 * 
 */
package org.activity.recomm;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.activity.objects.ActivityObject;
import org.activity.objects.Triple;
import org.activity.util.*;

/**
 * @author gunjan
 *
 */
public interface IRecommenderMaster
{
	/**
	 * 
	 * @return
	 */
	public int getNumberOfDistinctRecommendations();
	
	/**
	 * Returns the current ActivityObject, i.e., the Activity Object at recommendation instant
	 * 
	 * @return
	 */
	public ActivityObject getActivityObjectAtRecomm();
	
	/**
	 * Return the Activity Names of the Activity Objects in the current timeline, i,e, the names from 'Activity Objects guiding recommendation'
	 * 
	 * @return
	 */
	public String getActivityNamesGuidingRecomm();
	
	public String getActivityNamesGuidingRecommwithTimestamps();
	
	/**
	 * Generates a ranked list of recommended Activity Events according to CaseBasedV1_2 scheme: rankScore= (1d-(editDistanceValExceptEnd/maxEditDistanceValExceptEnd))* simEndPointActivityObject and sets recommendedActivityNamesRankscorePairs
	 * 
	 * and calls the following setter methods setRecommendedActivityNamesWithRankscores setRankedRecommendedActivityNamesWithRankScores setRankedRecommendedActivityNamesWithoutRankScores
	 * 
	 * @param topNextActivityEventsWithDistance
	 * @param similarityOfEndPointActivityEventCand
	 */
	public void createRankedTopRecommendedActivityNamesCaseBasedV1_2(ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityEventsWithDistance, LinkedHashMap<Integer, Double> similarityOfEndPointActivityEventCand); // we might remove these arguments as these are already member variables of this class
	
	/**
	 * Generates a ranked list of recommended Activity Events according to CaseBasedV1 scheme: rankScore= (1d-(editDistanceValExceptEnd/maxEditDistanceValExceptEnd))* (1d- (endPointActivityEditDistanceVal/maxEditDistanceValOfEnd))
	 * 
	 * and sets recommendedActivityNamesRankscorePairs
	 * 
	 * and calls the following setter methods setRecommendedActivityNamesWithRankscores setRankedRecommendedActivityNamesWithRankScores setRankedRecommendedActivityNamesWithoutRankScores
	 * 
	 * @param topNextActivityEventsWithDistance
	 * @param similarityOfEndPointActivityEventCand
	 */
	public void createRankedTopRecommendedActivityNamesCaseBasedV1(ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityEventsWithDistance, LinkedHashMap<Integer, Double> editDistanceOfEndPointActivityEventCand); // we might remove these arguments as these are already member variables of this class
	
	/**
	 * Generates a ranked list of recommended Activity Events according to SimpleV3 scheme: rankScore= 1d - (editDistanceVal/maxEditDistanceVal);
	 * 
	 * and sets recommendedActivityNamesRankscorePairs
	 * 
	 * and calls the following setter methods setRecommendedActivityNamesWithRankscores setRankedRecommendedActivityNamesWithRankScores setRankedRecommendedActivityNamesWithoutRankScores
	 * 
	 * @param topNextActivityEventsWithDistance
	 */
	public void createRankedTopRecommendedActivityNames(ArrayList<Triple<ActivityObject, Double, Integer>> topNextActivityEventsWithDistance);
	
	/**
	 * set Map of <recommended Activity Name, rank score>
	 * 
	 * @param recommendedActivityNamesRankscorePairs
	 */
	public void setRecommendedActivityNamesWithRankscores(LinkedHashMap<String, Double> recommendedActivityNamesRankscorePairs);
	
	public LinkedHashMap<String, Double> getRecommendedActivityNamesWithRankscores();
	
	/**
	 * Sets the string rankedRecommendedActivityNameWithRankScores as '__recommendedActivityName1:rankScore1__recommendedActivityName2:rankScore2'
	 * 
	 * @param recommendedActivityNameRankscorePairs
	 */
	public void setRankedRecommendedActivityNamesWithRankScores(LinkedHashMap<String, Double> recommendedActivityNameRankscorePairs);
	
	public String getRankedRecommendedActivityNamesWithRankScores();
	
	/**
	 * Sets the string rankedRecommendedActivityNameWithoutRankScores as '__recommendedActivityName1__recommendedActivityName2'
	 * 
	 * @param recommendedActivityNameRankscorePairs
	 * @return
	 */
	public void setRankedRecommendedActivityNamesWithoutRankScores(LinkedHashMap<String, Double> recommendedActivityNameRankscorePairs);
	
	public String getRankedRecommendedActivityNamesWithoutRankScores();
	
	/**
	 * Removes the recommendation instant Activity name, i.e., the Current Activity Name from the recommendedActivityNamesWithRankscores
	 */
	public void removeRecommPointActivityFromRankedRecomm();
	
	/**
	 * CHeck if the recommendation has candidate timelines
	 * 
	 * @return
	 */
	public boolean hasCandidateTimeslines();
	
	public String getTopNextActivityNamesWithoutDistanceString();
}
