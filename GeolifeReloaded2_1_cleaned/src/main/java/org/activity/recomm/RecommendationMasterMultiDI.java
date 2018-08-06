package org.activity.recomm;

/**
 * 
 * 
 * @author gunjan
 * @since 5 Aug 2018
 */
public interface RecommendationMasterMultiDI extends RecommendationMasterI
{
	public int getNumOfSecDimCandTimelines();

	public String getRankedRecommendedSecDimValsWithoutRankScores();

	public String getRankedRecommendedSecDimValsWithRankScores();

	public int getNumOfDistinctSecDimRecommendations();

}
