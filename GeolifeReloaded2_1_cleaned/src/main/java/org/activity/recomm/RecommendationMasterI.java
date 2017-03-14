package org.activity.recomm;

import java.util.LinkedHashMap;

import org.activity.objects.TimelineI;

public interface RecommendationMasterI
{
	public LinkedHashMap<?, TimelineI> getCandidateTimeslines();
}
