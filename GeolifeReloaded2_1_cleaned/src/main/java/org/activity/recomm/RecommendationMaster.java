package org.activity.recomm;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import org.activity.objects.ActivityObject;
import org.activity.objects.TimelineWithNext;

public abstract class RecommendationMaster
{
	private Date dateAtRecomm;
	private Time timeAtRecomm;
	private String userAtRecomm;
	private String userIDAtRecomm;

	private TimelineWithNext currentTimeline; // =current timelines
	private ArrayList<ActivityObject> activitiesGuidingRecomm; // Current Timeline ,
	private ActivityObject activityAtRecommPoint; // current Activity Object

	private String rankedRecommendedActNamesWithRankScores;
	private String rankedRecommendedActNamesWithoutRankScores;

	public RecommendationMaster()
	{
	}

}
